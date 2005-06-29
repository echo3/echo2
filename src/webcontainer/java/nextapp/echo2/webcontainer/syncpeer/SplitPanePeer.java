/* 
 * This file is part of the Echo Web Application Framework (hereinafter "Echo").
 * Copyright (C) 2002-2005 NextApp, Inc.
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 */

package nextapp.echo2.webcontainer.syncpeer;

import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.FillImage;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.LayoutData;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.layout.SplitPaneLayoutData;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.DomUpdateSupport;
import nextapp.echo2.webcontainer.PartialUpdateManager;
import nextapp.echo2.webcontainer.PropertyUpdateProcessor;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.RenderState;
import nextapp.echo2.webcontainer.ComponentSynchronizePeer;
import nextapp.echo2.webcontainer.SynchronizePeerFactory;
import nextapp.echo2.webcontainer.image.ImageRenderSupport;
import nextapp.echo2.webcontainer.propertyrender.AlignmentRender;
import nextapp.echo2.webcontainer.propertyrender.ColorRender;
import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
import nextapp.echo2.webcontainer.propertyrender.FillImageRender;
import nextapp.echo2.webcontainer.propertyrender.FontRender;
import nextapp.echo2.webcontainer.propertyrender.InsetsRender;
import nextapp.echo2.webrender.ClientProperties;
import nextapp.echo2.webrender.ServerMessage;
import nextapp.echo2.webrender.Service;
import nextapp.echo2.webrender.WebRenderServlet;
import nextapp.echo2.webrender.output.CssStyle;
import nextapp.echo2.webrender.servermessage.DomUpdate;
import nextapp.echo2.webrender.service.JavaScriptService;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Synchronization peer for <code>nextapp.echo2.app.SplitPane</code> components.
 * <p>
 * This class should not be extended or used by classes outside of the
 * Echo framework.
 */
public class SplitPanePeer 
implements DomUpdateSupport, ImageRenderSupport, PropertyUpdateProcessor, ComponentSynchronizePeer {

    //BUGBUG. SplitPane support for bidirectional is not rendering correctly at moment.
    
    private static final String IMAGE_ID_HORIZONTAL_SEPARATOR = "horizontalSeparator";
    private static final String IMAGE_ID_PANE_0_BACKGROUND = "pane0Background";
    private static final String IMAGE_ID_PANE_1_BACKGROUND = "pane1Background";
    private static final String IMAGE_ID_VERTICAL_SEPARATOR = "verticalSeparator";

    /**
     * <code>RenderState</code> implementation.
     */
    private static class RenderStateImpl 
    implements RenderState {
        
        /**
         * The pane which was rendered at index 0.
         */
        private String pane0;

        /**
         * The pane which was rendered at index 1.
         */
        private String pane1;

        /**
         * Creates a new <code>RenderState</code> based on the state of the
         * given <code>splitPane</code>.
         * 
         * @param splitPane the split pane
         */
        private RenderStateImpl(Component splitPane) {
            int componentCount = splitPane.getVisibleComponentCount();
            pane0 = (componentCount < 1 || splitPane.getVisibleComponent(0) == null) 
                        ? null : ContainerInstance.getElementId(splitPane.getVisibleComponent(0));
            pane1 = (componentCount < 2 || splitPane.getVisibleComponent(1) == null) 
                        ? null : ContainerInstance.getElementId(splitPane.getVisibleComponent(1));
        }
    }
    
    /**
     * Utility method to evaluate equality of objects in a null-safe fashion.
     */
    private static final boolean equal(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    /** Default separator color. */
    private static final Color DEFAULT_SEPARATOR_COLOR = new Color(0x2f2f47);

    /**
     * Service to provide supporting JavaScript library.
     */
    private static final Service SPLIT_PANE_SERVICE = JavaScriptService.forResource("Echo.SplitPane",
            "/nextapp/echo2/webcontainer/resource/js/SplitPane.js");

    static {
        WebRenderServlet.getServiceRegistry().add(SPLIT_PANE_SERVICE);
    }
    
    private PartialUpdateManager partialUpdateManager;

    /**
     * Default constructor.
     */
    public SplitPanePeer() {
        super();
        partialUpdateManager = new PartialUpdateManager();
        //BUGBUG. add property renderers to registry.
    }
    
    /**
     * Calculates the pixel position of the separator.
     * 
     * @param splitPane the <code>SplitPane</code> to evaluate
     * @return the position of the separator in pixels
     */
    private int calculateSeparatorPosition(SplitPane splitPane) {
        int separatorPosition = ExtentRender.toPixels(
                (Extent) splitPane.getRenderProperty(SplitPane.PROPERTY_SEPARATOR_POSITION), 100);
        return Math.abs(separatorPosition);
    }

    /**
     * Calculates the pixel size of the separator.
     * 
     * @param splitPane the <code>SplitPane</code> to evaluate
     * @return the size of the separator in pixels
     */
    private int calculateSeparatorSize(SplitPane splitPane) {
        Boolean booleanValue = (Boolean) splitPane.getRenderProperty(SplitPane.PROPERTY_RESIZABLE);
        boolean resizable = booleanValue == null ? false : booleanValue.booleanValue();
        boolean verticalOrientation = isOrientationVertical(splitPane);
        if (resizable) {
            return ExtentRender.toPixels((Extent) splitPane.getRenderProperty(
                    verticalOrientation ? SplitPane.PROPERTY_SEPARATOR_HEIGHT : SplitPane.PROPERTY_SEPARATOR_WIDTH), 4);
        } else {
            return ExtentRender.toPixels((Extent) splitPane.getRenderProperty(
                    verticalOrientation ? SplitPane.PROPERTY_SEPARATOR_HEIGHT : SplitPane.PROPERTY_SEPARATOR_WIDTH), 0);
        }
    }
    
    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#getContainerId(nextapp.echo2.app.Component)
     */
    public String getContainerId(Component child) {
        return ContainerInstance.getElementId(child.getParent()) + "_panecontent_"
                + child.getParent().visibleIndexOf(child);
    }
    
    /**
     * @see nextapp.echo2.webcontainer.image.ImageRenderSupport#getImage(nextapp.echo2.app.Component, java.lang.String)
     */
    public ImageReference getImage(Component component, String imageId) {
        if (IMAGE_ID_PANE_0_BACKGROUND.equals(imageId)) {
            return getPaneBackgroundImage(component);
        } else if (IMAGE_ID_PANE_1_BACKGROUND.equals(imageId)) {
            return getPaneBackgroundImage(component);
        } else if (IMAGE_ID_HORIZONTAL_SEPARATOR.equals(imageId)) {
            FillImage fillImage = (FillImage) component.getRenderProperty(SplitPane.PROPERTY_SEPARATOR_HORIZONTAL_IMAGE);
            return fillImage == null ? null : fillImage.getImage();
        } else if (IMAGE_ID_VERTICAL_SEPARATOR.equals(imageId)) {
            FillImage fillImage = (FillImage) component.getRenderProperty(SplitPane.PROPERTY_SEPARATOR_VERTICAL_IMAGE);
            return fillImage == null ? null : fillImage.getImage();
        } else {
            return null;
        }
    }

    /**
     * Retrieves the <code>ImageReference</code> of the background image 
     * defined in the layout data of the specified component (pane).
     * Returns null if the image cannot be retrieved for any reason.
     * 
     * @param component the child pane component
     * @return the background image 
     */
    private ImageReference getPaneBackgroundImage(Component component) {
        LayoutData layoutData = (LayoutData) component.getRenderProperty(SplitPane.PROPERTY_LAYOUT_DATA);
        if (!(layoutData instanceof SplitPaneLayoutData)) {
            return null;
        }
        FillImage backgroundImage = ((SplitPaneLayoutData) layoutData).getBackgroundImage();
        if (backgroundImage == null) {
            return null;
        }
        return backgroundImage.getImage();
    }
    
    /**
     * Determines if the orientation of a <code>SplitPane</code> is horizontal
     * or vertical.
     * 
     * @param splitPane the <code>SplitPane</code> to analyze
     * @return true if the orientation is vertical, false if it is horizontal
     */
    private boolean isOrientationVertical(SplitPane splitPane) {
        Integer orientationValue = (Integer) splitPane.getRenderProperty(SplitPane.PROPERTY_ORIENTATION);
        int orientation = orientationValue == null ? SplitPane.ORIENTATION_HORIZONTAL : orientationValue.intValue();
        return orientation == SplitPane.ORIENTATION_VERTICAL_TOP_BOTTOM || 
                orientation == SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP;
    }

    /**
     * Determines the number of the pane which should be rendered in the 
     * top of left position.  This method will transform leading/trailing
     * values based on the rendered layout direction of the 
     * <code>SplitPane</code>.
     * 
     * @param splitPane the <code>SplitPane</code> to analyze
     * @return the number  of the pane which should be rendered in the 
     *         top of left position
     */
    private int getTopLeftPaneNumber(SplitPane splitPane) {
        Integer orientationValue = (Integer) splitPane.getRenderProperty(SplitPane.PROPERTY_ORIENTATION);
        int orientation = orientationValue == null ? SplitPane.ORIENTATION_HORIZONTAL : orientationValue.intValue();
        switch (orientation) {
        case SplitPane.ORIENTATION_HORIZONTAL_LEADING_TRAILING:
            return splitPane.getRenderLayoutDirection().isLeftToRight() ? 0 : 1;
        case SplitPane.ORIENTATION_HORIZONTAL_TRAILING_LEADING:
            return splitPane.getRenderLayoutDirection().isLeftToRight() ? 1 : 0;
        case SplitPane.ORIENTATION_HORIZONTAL_LEFT_RIGHT:
            return 0;
        case SplitPane.ORIENTATION_HORIZONTAL_RIGHT_LEFT:
            return 1;
        case SplitPane.ORIENTATION_VERTICAL_TOP_BOTTOM:
            return 0;
        case SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP:
            return 1;
        default:
            throw new IllegalStateException("Invalid SplitPane orientation.");
        }
    }

    /**
     * @see nextapp.echo2.webcontainer.PropertyUpdateProcessor#processPropertyUpdate(
     *      nextapp.echo2.webcontainer.ContainerInstance, nextapp.echo2.app.Component, org.w3c.dom.Element)
     */
    public void processPropertyUpdate(ContainerInstance ci, Component component, Element propertyElement) {
        if ("separatorPosition".equals(propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_NAME))) {
            Extent newValue = ExtentRender.toExtent(propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_VALUE)); 
            ci.getUpdateManager().getClientUpdateManager().setComponentProperty(component, 
                    SplitPane.PROPERTY_SEPARATOR_POSITION, newValue);
        }
    }

    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderAdd(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String, nextapp.echo2.app.Component)
     */
    public void renderAdd(RenderContext rc, ServerComponentUpdate update, String targetId, Component component) {
        DocumentFragment htmlFragment = rc.getServerMessage().getDocument().createDocumentFragment();
        renderHtml(rc, update, htmlFragment, component);
        DomUpdate.renderElementAdd(rc.getServerMessage(), targetId, htmlFragment);
    }

    /**
     * Renders child components which were added to a 
     * <code>SplitPane</code>, as described in the provided 
     * <code>ServerComponentUpdate</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update
     */
    private void renderAddChildren(RenderContext rc, ServerComponentUpdate update) {
        SplitPane splitPane = (SplitPane) update.getParent();
        String elementId = ContainerInstance.getElementId(splitPane);
        ContainerInstance ci = rc.getContainerInstance();
        RenderStateImpl previousRenderState = (RenderStateImpl) ci.getRenderState(splitPane);
        RenderStateImpl currentRenderState = new RenderStateImpl(splitPane);
        if (!equal(previousRenderState.pane0, currentRenderState.pane0)) {
            if (currentRenderState.pane0 != null) {
                DocumentFragment htmlFragment = rc.getServerMessage().getDocument().createDocumentFragment();
                renderPane(rc, update, htmlFragment, splitPane, 0);
                DomUpdate.renderElementAdd(rc.getServerMessage(), elementId, elementId + "_separator", htmlFragment);
            }
        }
        if (!equal(previousRenderState.pane1, currentRenderState.pane1)) {
            if (currentRenderState.pane1 != null) {
                DocumentFragment htmlFragment = rc.getServerMessage().getDocument().createDocumentFragment();
                renderPane(rc, update, htmlFragment, splitPane, 1);
                DomUpdate.renderElementAdd(rc.getServerMessage(), elementId, htmlFragment);
            }
        }
    }

    /**
     * Renders an individual child component of the <code>SplitPane</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the <code>ServerComponentUpdate</code> being performed
     * @param parentElement the <code>Element</code> to contain the child
     * @param child the child <code>Component</code> to be rendered
     */
    private void renderChild(RenderContext rc, ServerComponentUpdate update, Element parentElement, Component child) {
        ComponentSynchronizePeer syncPeer = SynchronizePeerFactory.getPeerForComponent(child.getClass());
        if (syncPeer instanceof DomUpdateSupport) {
            ((DomUpdateSupport) syncPeer).renderHtml(rc, update, parentElement, child);
        } else {
            syncPeer.renderAdd(rc, update, getContainerId(child), child);
        }
    }

    private void renderCssPositionExpression(CssStyle cssStyle, String parentElementId, int position, boolean vertical) {
        if (vertical) {
            cssStyle.setAttribute("height", 
                    "expression((document.getElementById('" + parentElementId + "').clientHeight-" + position + ")+'px')");
        } else {
            cssStyle.setAttribute("width", 
                    "expression((document.getElementById('" + parentElementId + "').clientWidth-" + position + ")+'px')");
        }
    }
    
    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderDispose(nextapp.echo2.webcontainer.RenderContext,
     *      nextapp.echo2.app.update.ServerComponentUpdate, nextapp.echo2.app.Component)
     */
    public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component) {
        String elementId = ContainerInstance.getElementId(component);
        
        renderDisposeDirective(rc, (SplitPane) component);
        if (rc.getContainerInstance().getClientProperties()
                .getBoolean(ClientProperties.QUIRK_DOM_PERFORMANCE_REMOVE_LARGE_HIERARCHY)) {
            // Performance Hack for Mozilla/Firefox Browsers:
            // BUGBUG. this hack needs to get moved to client side.
            if (!update.hasRemovedChild(component)) {
                DomUpdate.renderElementRemove(rc.getServerMessage(), elementId);
            }
        }
    }

    /**
     * Renders a directive to the outgoing <code>ServerMessage</code> to 
     * dispose the state of a split pane, performing tasks such as unregistering
     * event listeners on the client.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param splitPane the <code>SplitPane</code>
     */
    private void renderDisposeDirective(RenderContext rc, SplitPane splitPane) {
        ServerMessage serverMessage = rc.getServerMessage();
        Element itemizedUpdateElement = serverMessage.getItemizedDirective(ServerMessage.GROUP_ID_PREREMOVE,
                "EchoSplitPane.MessageProcessor", "dispose",  new String[0], new String[0]);
        Element itemElement = serverMessage.getDocument().createElement("item");
        itemElement.setAttribute("eid", ContainerInstance.getElementId(splitPane));
        itemizedUpdateElement.appendChild(itemElement);
    }

    /**
     * @see nextapp.echo2.webcontainer.DomUpdateSupport#renderHtml(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, org.w3c.dom.Node, nextapp.echo2.app.Component)
     */
    public void renderHtml(RenderContext rc, ServerComponentUpdate update, Node parentNode, Component component) {
        SplitPane splitPane = (SplitPane) component;
        
        String elementId = ContainerInstance.getElementId(splitPane);
        
        Extent separatorPosition = (Extent) splitPane.getRenderProperty(SplitPane.PROPERTY_SEPARATOR_POSITION);
        if (separatorPosition == null) {
            separatorPosition = new Extent(100, Extent.PX);
        }

        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(SPLIT_PANE_SERVICE.getId());

        Document document = parentNode.getOwnerDocument();
        Element outerDivElement = document.createElement("div");
        outerDivElement.setAttribute("id", elementId);

        CssStyle outerDivStyle = new CssStyle();
        outerDivStyle.setAttribute("position", "absolute");
        outerDivStyle.setAttribute("padding", "0px");
        outerDivStyle.setAttribute("width", "100%");
        outerDivStyle.setAttribute("height", "100%");
        ColorRender.renderToStyle(outerDivStyle, (Color) splitPane.getRenderProperty(ContentPane.PROPERTY_FOREGROUND),
                (Color) splitPane.getRenderProperty(ContentPane.PROPERTY_BACKGROUND));
        FontRender.renderToStyle(outerDivStyle, (Font) splitPane.getRenderProperty(ContentPane.PROPERTY_FONT));
        outerDivElement.setAttribute("style", outerDivStyle.renderInline());

        parentNode.appendChild(outerDivElement);

        int componentCount = splitPane.getVisibleComponentCount();
        if (componentCount >= 1) {
            renderPane(rc, update, outerDivElement, splitPane, 0);
        }
        renderPaneSeparator(rc, outerDivElement, splitPane);
        if (componentCount >= 2) {
            renderPane(rc, update, outerDivElement, splitPane, 1);
        }

        // Render initialization directive.
        renderInitDirective(rc, splitPane);
        
        updateRenderState(rc, component);
        
    }
    
    /**
     * Renders a directive to the outgoing <code>ServerMessage</code> to 
     * initialize the state of a split pane, performing tasks such as 
     * registering event listeners on the client.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param splitPane the <code>WindowPane</code>
     */
    private void renderInitDirective(RenderContext rc, SplitPane splitPane) {
        String elementId = ContainerInstance.getElementId(splitPane);
        ServerMessage serverMessage = rc.getServerMessage();
        Boolean booleanValue = (Boolean) splitPane.getRenderProperty(SplitPane.PROPERTY_RESIZABLE);
        boolean resizable = booleanValue == null ? false : booleanValue.booleanValue();

        Element itemizedUpdateElement = serverMessage.getItemizedDirective(ServerMessage.GROUP_ID_POSTUPDATE,
                "EchoSplitPane.MessageProcessor", "init", new String[0], new String[0]);
        Element itemElement = serverMessage.getDocument().createElement("item");
        itemElement.setAttribute("eid", elementId);
        itemElement.setAttribute("top-left-pane", Integer.toString(getTopLeftPaneNumber(splitPane)));
        if (resizable) {
            itemElement.setAttribute("resizable", resizable ? "true" : "false");
        }
        itemizedUpdateElement.appendChild(itemElement);
    }

    /**
     * Renders the CSS positioning information for a pane of a <code>SplitPane</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param splitPane the <code>SplitPane</code> instance
     * @param paneNumber the pane number, either 0 or 1
     * @param paneDivCssStyle the <code>CssStyle</code> in which the 
     *        positioning data should be updated
     */
    private void renderPaneCssPositioning(RenderContext rc, SplitPane splitPane, int paneNumber, CssStyle paneDivCssStyle) {
        int separatorPosition = calculateSeparatorPosition(splitPane);
        int topLeftPaneNumber = getTopLeftPaneNumber(splitPane);
        boolean renderingTopLeftPane = paneNumber == topLeftPaneNumber;
        boolean renderPositioningBothSides = !rc.getContainerInstance().getClientProperties()
                .getBoolean(ClientProperties.QUIRK_CSS_POSITIONING_ONE_SIDE_ONLY);
        boolean renderSizeExpression = !renderPositioningBothSides && rc.getContainerInstance().getClientProperties()
                .getBoolean(ClientProperties.PROPRIETARY_IE_CSS_EXPRESSIONS_SUPPORTED);
        String elementId = ContainerInstance.getElementId(splitPane);

        if (isOrientationVertical(splitPane)) { // Vertical Orientation
            paneDivCssStyle.setAttribute("width", "100%");
            if (paneNumber == 0) {
                paneDivCssStyle.setAttribute(renderingTopLeftPane ? "top" : "bottom", "0px");
                paneDivCssStyle.setAttribute("height", separatorPosition + "px");
            } else {
                int separatorSize = calculateSeparatorSize(splitPane);
                paneDivCssStyle.setAttribute(renderingTopLeftPane ? "bottom" : "top", (separatorPosition + separatorSize) + "px");
                if (renderPositioningBothSides) {
                    paneDivCssStyle.setAttribute(renderingTopLeftPane ? "top" : "bottom", "0px");
                }
                if (renderSizeExpression) {
                    renderCssPositionExpression(paneDivCssStyle, elementId, separatorPosition + separatorSize, true);
                }
            }
        } else { // Horizontal Orientation
            paneDivCssStyle.setAttribute("height", "100%");
            if (paneNumber == 0) {
                paneDivCssStyle.setAttribute(renderingTopLeftPane ? "left" : "right", "0px");
                paneDivCssStyle.setAttribute("width", separatorPosition + "px");
            } else {
                int separatorSize = calculateSeparatorSize(splitPane);
                paneDivCssStyle.setAttribute(renderingTopLeftPane ? "right" : "left", (separatorPosition + separatorSize) + "px");
                if (renderPositioningBothSides) {
                    paneDivCssStyle.setAttribute(renderingTopLeftPane ? "left" : "right", "0px");
                }
                if (renderSizeExpression) {
                    renderCssPositionExpression(paneDivCssStyle, elementId, separatorPosition + separatorSize, false);
                }
            }
        }
    }

    /**
     * Renders a single pane container.
     * The component must have a child at the specified 
     * <code>paneNumber</code> index.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the <code>ServerComponentUpdate</code>
     * @param parentNode the DOM node to which the rendered HTML should be
     *        appended
     * @param splitPane the <code>SplitPane</code> component
     * @param paneNumber the pane number, either 0 or 1
     */
    private void renderPane(RenderContext rc, ServerComponentUpdate update, Node parentNode, 
            SplitPane splitPane, int paneNumber) {
        Document document = parentNode.getOwnerDocument();
        Component paneComponent = splitPane.getVisibleComponent(paneNumber);
        String elementId = ContainerInstance.getElementId(splitPane);
        
        Element paneDivElement = document.createElement("div");
        String paneDivElementId = elementId + "_pane_" + paneNumber;
        paneDivElement.setAttribute("id", paneDivElementId); 
        CssStyle paneDivCssStyle = new CssStyle();
        paneDivCssStyle.setAttribute("overflow", "auto");
        paneDivCssStyle.setAttribute("position", "absolute");
        paneDivCssStyle.setAttribute("padding", "0px");
        
        renderPaneCssPositioning(rc, splitPane, paneNumber, paneDivCssStyle);
        
        Element paneContentDivElement = document.createElement("div");
        paneContentDivElement.setAttribute("id", elementId + "_panecontent_" + paneNumber);
        paneDivElement.appendChild(paneContentDivElement);

        LayoutData layoutData = (LayoutData) paneComponent.getRenderProperty(Component.PROPERTY_LAYOUT_DATA);
        SplitPaneLayoutData splitPaneLayoutData = (layoutData instanceof SplitPaneLayoutData) 
                ? (SplitPaneLayoutData) layoutData : null;

        if (splitPaneLayoutData != null) {
            //BUGBUG. sort whether we want to use renderToElement, i.e., using (transitional) div align attribute....
            // and if so, apply this elsewhere as well.
            AlignmentRender.renderToElement(paneContentDivElement, splitPaneLayoutData.getAlignment(), paneComponent);
            if (splitPaneLayoutData.getBackground() != null) {
                paneDivCssStyle.setAttribute("background-color", 
                        ColorRender.renderCssAttributeValue(splitPaneLayoutData.getBackground()));
            }
            if (splitPaneLayoutData.getBackgroundImage() != null) {
                FillImageRender.renderToStyle(paneDivCssStyle, rc, this, paneComponent, 
                        paneNumber == 0 ? IMAGE_ID_PANE_0_BACKGROUND : IMAGE_ID_PANE_1_BACKGROUND, 
                        splitPaneLayoutData.getBackgroundImage(), 0);
            }
            switch (splitPaneLayoutData.getOverflow()) {
            case SplitPaneLayoutData.OVERFLOW_AUTO:
                paneDivCssStyle.setAttribute("overflow", "auto");
                break;
            case SplitPaneLayoutData.OVERFLOW_HIDDEN:
                paneDivCssStyle.setAttribute("overflow", "hidden");
                break;
            case SplitPaneLayoutData.OVERFLOW_SCROLL:
                paneDivCssStyle.setAttribute("overflow", "scroll");
                break;
            }

            CssStyle paneContentDivCssStyle = new CssStyle();
            InsetsRender.renderToStyle(paneContentDivCssStyle, "padding", splitPaneLayoutData.getInsets());
            if (paneContentDivCssStyle.hasAttributes()) {
                paneContentDivElement.setAttribute("style", paneContentDivCssStyle.renderInline());
            }
        }
        
        renderUpdatePaneDirective(rc, splitPane, paneNumber);

        renderChild(rc, update, paneContentDivElement, paneComponent);
        
        paneDivElement.setAttribute("style", paneDivCssStyle.renderInline());
        parentNode.appendChild(paneDivElement);
    }
    
    /**
     * Renders the separator.
     */
    private void renderPaneSeparator(RenderContext rc, Element parentElement, SplitPane splitPane) {
        Document document = parentElement.getOwnerDocument();
        Boolean booleanValue = (Boolean) splitPane.getRenderProperty(SplitPane.PROPERTY_RESIZABLE);
        boolean resizable = booleanValue == null ? false : booleanValue.booleanValue();
        int separatorSize = calculateSeparatorSize(splitPane);
        String separatorElementId = ContainerInstance.getElementId(splitPane) + "_separator";

        Element separatorDivElement = document.createElement("div");
        separatorDivElement.setAttribute("id", separatorElementId);
        int separatorPosition = calculateSeparatorPosition(splitPane);
        int fixedPaneNumber = getTopLeftPaneNumber(splitPane);
        
        CssStyle separatorDivCssStyle = new CssStyle();
        // font-size/line-height styles correct for IE minimum DIV height rendering issue.
        separatorDivCssStyle.setAttribute("font-size", "1px");
        separatorDivCssStyle.setAttribute("line-height", "0px");
        separatorDivCssStyle.setAttribute("position", "absolute");
        separatorDivCssStyle.setAttribute("padding", "0px");
        separatorDivCssStyle.setAttribute("overflow", "hidden");
        ColorRender.renderToStyle(separatorDivCssStyle, null, 
                (Color) splitPane.getRenderProperty(SplitPane.PROPERTY_SEPARATOR_COLOR, DEFAULT_SEPARATOR_COLOR));
        
        if (isOrientationVertical(splitPane)) {
            FillImageRender.renderToStyle(separatorDivCssStyle, rc, this, splitPane, IMAGE_ID_VERTICAL_SEPARATOR, 
                    (FillImage) splitPane.getRenderProperty(SplitPane.PROPERTY_SEPARATOR_VERTICAL_IMAGE), 0);
            if (fixedPaneNumber == 0) {
                separatorDivCssStyle.setAttribute("top", separatorPosition + "px");
            } else {
                separatorDivCssStyle.setAttribute("bottom", separatorPosition + "px");
            }
            separatorDivCssStyle.setAttribute("width", "100%");
            if (separatorSize == 0) {
                separatorDivCssStyle.setAttribute("visibility", "hidden");
                separatorDivCssStyle.setAttribute("height", "1px");
            } else {
                separatorDivCssStyle.setAttribute("height", separatorSize + "px");
            }
            if (resizable) {
                separatorDivCssStyle.setAttribute("cursor", "s-resize");
            }
        } else {
            FillImageRender.renderToStyle(separatorDivCssStyle, rc, this, splitPane, IMAGE_ID_HORIZONTAL_SEPARATOR, 
                    (FillImage) splitPane.getRenderProperty(SplitPane.PROPERTY_SEPARATOR_HORIZONTAL_IMAGE), 0);
            if (fixedPaneNumber == 0) {
                separatorDivCssStyle.setAttribute("left", separatorPosition + "px");
            } else {
                separatorDivCssStyle.setAttribute("right", separatorPosition + "px");
            }
            if (separatorSize == 0) {
                separatorDivCssStyle.setAttribute("visibility", "hidden");
                separatorDivCssStyle.setAttribute("width", "1px");
            } else {
                separatorDivCssStyle.setAttribute("width", separatorSize + "px");
            }
            separatorDivCssStyle.setAttribute("height", "100%");
            if (resizable) {
                separatorDivCssStyle.setAttribute("cursor", "e-resize");
            }
        }

        separatorDivElement.setAttribute("style", separatorDivCssStyle.renderInline());
        parentElement.appendChild(separatorDivElement);
    }
    
    /**
     * Renders removal operations for child components which were removed from 
     * a <code>SplitPane</code>, as described in the provided 
     * <code>ServerComponentUpdate</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update
     */
    private void renderRemoveChildren(RenderContext rc, ServerComponentUpdate update) {
        ContainerInstance ci = rc.getContainerInstance();
        Component parent = update.getParent();
        String parentId = ContainerInstance.getElementId(parent);
        RenderStateImpl previousRenderState = (RenderStateImpl) ci.getRenderState(parent);
        
        RenderStateImpl currentRenderState = new RenderStateImpl(parent);
        if (!equal(previousRenderState.pane0, currentRenderState.pane0)) {
            if (previousRenderState.pane0 != null) {
                DomUpdate.renderElementRemove(rc.getServerMessage(), parentId + "_pane_0");
            }
        }
        if (!equal(previousRenderState.pane1, currentRenderState.pane1)) {
            if (previousRenderState.pane1 != null) {
                DomUpdate.renderElementRemove(rc.getServerMessage(), parentId + "_pane_1");
            }
        }
    }
    
    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderUpdate(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String)
     */
    public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update, String targetId) {
        boolean fullReplace = false;
        if (update.hasUpdatedLayoutDataChildren()) {
            fullReplace = true;
        } else if (update.hasUpdatedProperties()) {
            if (!partialUpdateManager.canProcess(rc, update)) {
                fullReplace = true;
            }
        }
        
        if (fullReplace) {
            // Perform full update.
            DomUpdate.renderElementRemove(rc.getServerMessage(), ContainerInstance.getElementId(update.getParent()));
            renderAdd(rc, update, targetId, update.getParent());
        } else {
            partialUpdateManager.process(rc, update);
            if (update.hasAddedChildren() || update.hasRemovedChildren()) {
                renderRemoveChildren(rc, update);
                renderAddChildren(rc, update);
            }
        }
        
        updateRenderState(rc, update.getParent());
        return fullReplace;
    }
    
    /**
     * Renders a directive to the outgoing <code>ServerMessage</code> to 
     * update client-side information about a specific pane of a 
     * <code>SplitPane</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param splitPane the <code>SplitPane</code>
     * @param paneNumber the pane number, either 0 or 1
     */
    private void renderUpdatePaneDirective(RenderContext rc, SplitPane splitPane, int paneNumber) {
        Component paneComponent = splitPane.getVisibleComponent(paneNumber);
        LayoutData layoutData = (LayoutData) paneComponent.getRenderProperty(Component.PROPERTY_LAYOUT_DATA);
        SplitPaneLayoutData splitPaneLayoutData = (layoutData instanceof SplitPaneLayoutData) 
                ? (SplitPaneLayoutData) layoutData : null;
        String elementId = ContainerInstance.getElementId(splitPane);
        String paneDivElementId = elementId + "_pane_" + paneNumber;
        ServerMessage serverMessage = rc.getServerMessage();

        Element itemizedUpdateElement = serverMessage.getItemizedDirective(ServerMessage.GROUP_ID_POSTUPDATE,
                "EchoSplitPane.MessageProcessor", "update-pane", new String[0], new String[0]);
        Element itemElement = serverMessage.getDocument().createElement("item");
        itemElement.setAttribute("eid", paneDivElementId);
        itemizedUpdateElement.appendChild(itemElement);

        if (splitPaneLayoutData != null) {
            if (splitPaneLayoutData.getMinimumSize() != null) {
                itemElement.setAttribute("minimum-size", 
                        ExtentRender.renderCssAttributePixelValue(splitPaneLayoutData.getMinimumSize())); 
            }
            if (splitPaneLayoutData.getMaximumSize() != null) {
                itemElement.setAttribute("maximum-size", 
                        ExtentRender.renderCssAttributePixelValue(splitPaneLayoutData.getMaximumSize())); 
            }
        }
    }

    /**
     * Update the stored <code>RenderState</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param component the <code>SplitPane</code> component
     */
    private void updateRenderState(RenderContext rc, Component component) {
        rc.getContainerInstance().setRenderState(component, new RenderStateImpl(component));
    }
}
