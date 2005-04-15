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

import nextapp.echo2.app.BackgroundImage;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.LayoutData;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.layout.SplitPaneLayoutData;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.DomUpdateSupport;
import nextapp.echo2.webcontainer.PropertyUpdateProcessor;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.RenderState;
import nextapp.echo2.webcontainer.SynchronizePeer;
import nextapp.echo2.webcontainer.SynchronizePeerFactory;
import nextapp.echo2.webcontainer.image.ImageRenderSupport;
import nextapp.echo2.webcontainer.propertyrender.AlignmentRender;
import nextapp.echo2.webcontainer.propertyrender.ColorRender;
import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
import nextapp.echo2.webcontainer.propertyrender.InsetsRender;
import nextapp.echo2.webrender.clientupdate.DomPropertyStore;
import nextapp.echo2.webrender.clientupdate.DomUpdate;
import nextapp.echo2.webrender.clientupdate.EventUpdate;
import nextapp.echo2.webrender.clientupdate.ServerMessage;
import nextapp.echo2.webrender.output.CssStyle;
import nextapp.echo2.webrender.server.ClientProperties;
import nextapp.echo2.webrender.server.Service;
import nextapp.echo2.webrender.server.WebRenderServlet;
import nextapp.echo2.webrender.services.JavaScriptService;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Synchronization peer for <code>nextapp.echo2.app.SplitPane</code> components.
 */
public class SplitPanePeer 
implements DomUpdateSupport, ImageRenderSupport, PropertyUpdateProcessor, SynchronizePeer {

    private static final String IMAGE_ID_PANE_0_BACKGROUND = "pane0Background";
    private static final String IMAGE_ID_PANE_1_BACKGROUND = "pane1Background";

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
            pane0 = (splitPane.getComponentCount() < 1 || splitPane.getComponent(0) == null) 
                        ? null : ContainerInstance.getElementId(splitPane.getComponent(0));
            pane1 = (splitPane.getComponentCount() < 2 || splitPane.getComponent(1) == null) 
                        ? null : ContainerInstance.getElementId(splitPane.getComponent(1));
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
    public static final Service DRAG_PANE_SERVICE = JavaScriptService.forResource("Echo.DragPane",
            "/nextapp/echo2/webcontainer/resource/js/DragPane.js");

    static {
        WebRenderServlet.getServiceRegistry().add(DRAG_PANE_SERVICE);
    }

    /**
     * Calculates the pixel position of the separator.
     * 
     * @param splitPane the <code>SplitPane</code> to evaluate
     * @return the position of the separator in pixels
     */
    private int calculateSeparatorPosition(SplitPane splitPane) {
        return ExtentRender.toPixels((Extent) splitPane.getRenderProperty(SplitPane.PROPERTY_SEPARATOR_POSITION), 100);
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
        if (resizable) {
            return ExtentRender.toPixels((Extent) splitPane.getRenderProperty(SplitPane.PROPERTY_SEPARATOR_SIZE), 4);
        } else {
            return ExtentRender.toPixels((Extent) splitPane.getRenderProperty(SplitPane.PROPERTY_SEPARATOR_SIZE), 0);
        }
    }

    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#getContainerId(nextapp.echo2.app.Component)
     */
    public String getContainerId(Component child) {
        return ContainerInstance.getElementId(child.getParent()) + "_panecontent_"
                + child.getParent().indexOf(child);
    }
    
    /**
     * @see nextapp.echo2.webcontainer.image.ImageRenderSupport#getImage(nextapp.echo2.app.Component, java.lang.String)
     */
    public ImageReference getImage(Component component, String imageId) {
System.err.println(component);        
        if (IMAGE_ID_PANE_0_BACKGROUND.equals(imageId)) {
            return getPaneBackgroundImage(component);
        } else if (IMAGE_ID_PANE_1_BACKGROUND.equals(imageId)) {
            return getPaneBackgroundImage(component);
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
        BackgroundImage backgroundImage = ((SplitPaneLayoutData) layoutData).getBackgroundImage();
        if (backgroundImage == null) {
            return null;
        }
        return backgroundImage.getImage();
    }

    /**
     * @see nextapp.echo2.webcontainer.PropertyUpdateProcessor#processPropertyUpdate(
     *      nextapp.echo2.webcontainer.ContainerInstance, nextapp.echo2.app.Component, org.w3c.dom.Element)
     */
    public void processPropertyUpdate(ContainerInstance ci, Component component, Element propertyElement) {
        if ("separatorPosition".equals(propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_NAME))) {
            ci.getUpdateManager().addClientPropertyUpdate(component, SplitPane.INPUT_SEPARATOR_POSITION, 
                    ExtentRender.toExtent(propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_VALUE)));
        }
    }

    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#renderAdd(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String, nextapp.echo2.app.Component)
     */
    public void renderAdd(RenderContext rc, ServerComponentUpdate update, String targetId, Component component) {
        Element contentElement = DomUpdate.createDomAdd(rc.getServerMessage(), targetId);
        renderHtml(rc, update, contentElement, component);
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
                Element contentElement = DomUpdate.createDomAdd(rc.getServerMessage(), elementId, elementId + "_separator");
                renderPane(rc, update, contentElement, splitPane, 0);
            }
        }
        if (!equal(previousRenderState.pane1, currentRenderState.pane1)) {
            if (currentRenderState.pane1 != null) {
                Element contentElement = DomUpdate.createDomAdd(rc.getServerMessage(), elementId);
                renderPane(rc, update, contentElement, splitPane, 1);
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
        SynchronizePeer syncPeer = SynchronizePeerFactory.getPeerForComponent(child.getClass());
        if (syncPeer instanceof DomUpdateSupport) {
            ((DomUpdateSupport) syncPeer).renderHtml(rc, update, parentElement, child);
        } else {
            syncPeer.renderAdd(rc, update, getContainerId(child), child);
        }
    }

    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#renderDispose(nextapp.echo2.webcontainer.RenderContext,
     *      nextapp.echo2.app.update.ServerComponentUpdate, nextapp.echo2.app.Component)
     */
    public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component) {
        String elementId = ContainerInstance.getElementId(component);
        EventUpdate.createEventRemove(rc.getServerMessage(), "mousedown", elementId + "_separator");
        if (rc.getContainerInstance().getClientProperties()
                .getBoolean(ClientProperties.QUIRK_DOM_PERFORMANCE_REMOVE_LARGE_HIERARCHY)) {
            // Performance Hack for Mozilla/Firefox Browsers:
            // BUGBUG. this hack needs to get moved to client side.
            if (!update.hasRemovedChild(component)) {
                DomUpdate.createDomRemove(rc.getServerMessage(), elementId);
            }
        }
    }

    /**
     * @see nextapp.echo2.webcontainer.DomUpdateSupport#renderHtml(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, org.w3c.dom.Element, nextapp.echo2.app.Component)
     */
    public void renderHtml(RenderContext rc, ServerComponentUpdate update, Element parent, Component component) {
        SplitPane splitPane = (SplitPane) component;
        Extent separatorPosition = (Extent) splitPane.getRenderProperty(SplitPane.PROPERTY_SEPARATOR_POSITION);
        if (separatorPosition == null) {
            separatorPosition = new Extent(100, Extent.PX);
        }

        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(DRAG_PANE_SERVICE.getId(), true);

        Document document = parent.getOwnerDocument();
        Element outerDivElement = document.createElement("div");
        outerDivElement.setAttribute("id", ContainerInstance.getElementId(splitPane));

        CssStyle outerDivStyle = new CssStyle();
        outerDivStyle.setAttribute("position", "absolute");
        outerDivStyle.setAttribute("padding", "0px");
        outerDivStyle.setAttribute("width", "100%");
        outerDivStyle.setAttribute("height", "100%");
        outerDivElement.setAttribute("style", outerDivStyle.renderInline());

        parent.appendChild(outerDivElement);

        if (splitPane.getComponentCount() >= 1) {
            renderPane(rc, update, outerDivElement, splitPane, 0);
        }
        renderPaneSeparator(rc, outerDivElement, splitPane);
        if (splitPane.getComponentCount() >= 2) {
            renderPane(rc, update, outerDivElement, splitPane, 1);
        }
        
        updateRenderState(rc, component);
    }

    /**
     * Renders a single pane container.
     * Precondition: Component must have a child at the specified 
     * <code>paneNumber</code> index. 
     */
    private void renderPane(RenderContext rc, ServerComponentUpdate update, Element parentElement, 
            SplitPane splitPane, int paneNumber) {
        boolean renderPositioningBothSides = !rc.getContainerInstance().getClientProperties()
                .getBoolean(ClientProperties.QUIRK_CSS_POSITIONING_ONE_SIDE_ONLY);
        boolean renderSizeExpression = !renderPositioningBothSides && rc.getContainerInstance().getClientProperties()
                .getBoolean(ClientProperties.PROPRIETARY_IE_CSS_EXPRESSIONS_SUPPORTED);
        Document document = parentElement.getOwnerDocument();
        Component paneComponent = splitPane.getComponent(paneNumber);
        String elementId = ContainerInstance.getElementId(splitPane);
        
        int separatorPosition = calculateSeparatorPosition(splitPane);
        
        Element paneDivElement = document.createElement("div");
        String paneDivElementId = elementId + "_pane_" + paneNumber;
        paneDivElement.setAttribute("id", paneDivElementId); 
        CssStyle paneDivCssStyle = new CssStyle();
        paneDivCssStyle.setAttribute("overflow", "auto");
        paneDivCssStyle.setAttribute("position", "absolute");
        paneDivCssStyle.setAttribute("padding", "0px");
        Integer orientationValue = (Integer) splitPane.getRenderProperty(SplitPane.PROPERTY_ORIENTATION);
        boolean orientationVertical = (orientationValue == null ? SplitPane.ORIENTATION_HORIZONTAL : orientationValue.intValue()) 
                == SplitPane.ORIENTATION_VERTICAL;
        if (orientationVertical) {
            if (paneNumber == 0) {
                paneDivCssStyle.setAttribute("top", "0px");
                paneDivCssStyle.setAttribute("height", separatorPosition + "px");
                paneDivCssStyle.setAttribute("width", "100%");
            } else {
                int separatorSize = calculateSeparatorSize(splitPane);
                paneDivCssStyle.setAttribute("top", (separatorPosition + separatorSize) + "px");
                if (renderPositioningBothSides) {
                    paneDivCssStyle.setAttribute("bottom", "0px");
                }
                paneDivCssStyle.setAttribute("width", "100%");
                if (renderSizeExpression) {
                    paneDivCssStyle.setAttribute("height", "expression((document.getElementById('" 
                            + elementId + "').clientHeight-" + (separatorPosition + separatorSize) + ")+'px')");
                }
            }
        } else {
            if (paneNumber == 0) {
                paneDivCssStyle.setAttribute("left", "0px");
                paneDivCssStyle.setAttribute("width", separatorPosition + "px");
                paneDivCssStyle.setAttribute("height", "100%");
            } else {
                int separatorSize = calculateSeparatorSize(splitPane);
                paneDivCssStyle.setAttribute("left", (separatorPosition + separatorSize) + "px");
                if (renderPositioningBothSides) {
                    paneDivCssStyle.setAttribute("right", "0px");
                }
                if (renderSizeExpression) {
                    paneDivCssStyle.setAttribute("width", "expression((document.getElementById('" 
                            + elementId + "').clientWidth-" + (separatorPosition + separatorSize) + ")+'px')");
                }
                paneDivCssStyle.setAttribute("height", "100%");
            }
        }
        
        Element paneContentDivElement = document.createElement("div");
        paneContentDivElement.setAttribute("id", elementId + "_panecontent_" + paneNumber);
        paneDivElement.appendChild(paneContentDivElement);

        LayoutData layoutData = (LayoutData) paneComponent.getRenderProperty(Component.PROPERTY_LAYOUT_DATA);
        SplitPaneLayoutData splitPaneLayoutData = (layoutData instanceof SplitPaneLayoutData) 
                ? (SplitPaneLayoutData) layoutData : null;

        if (splitPaneLayoutData != null) {
            if (splitPaneLayoutData.getMinimumSize() != null) {
                DomPropertyStore.createDomPropertyStore(rc.getServerMessage(), paneDivElementId, "minimumSize", 
                        ExtentRender.renderCssAttributeValue(splitPaneLayoutData.getMinimumSize()));
            }
            if (splitPaneLayoutData.getMaximumSize() != null) {
                DomPropertyStore.createDomPropertyStore(rc.getServerMessage(), paneDivElementId, "maximumSize", 
                        ExtentRender.renderCssAttributeValue(splitPaneLayoutData.getMaximumSize()));
            }
            AlignmentRender.renderToStyle(paneDivCssStyle, splitPaneLayoutData.getAlignment());
            if (splitPaneLayoutData.getBackground() != null) {
                paneDivCssStyle.setAttribute("background-color", 
                        ColorRender.renderCssAttributeValue(splitPaneLayoutData.getBackground()));
            }
            if (splitPaneLayoutData.getBackgroundImage() != null) {
//BUGBUG. bgimage stuff is very broken.                
/*                
                BackgroundImageRender.renderToStyle(paneDivCssStyle, rc, this, paneComponent, 
                        paneNumber == 0 ? IMAGE_ID_PANE_0_BACKGROUND : IMAGE_ID_PANE_1_BACKGROUND, 
                        splitPaneLayoutData.getBackgroundImage(), false);
*/                        
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

        renderChild(rc, update, paneContentDivElement, paneComponent);
        
        paneDivElement.setAttribute("style", paneDivCssStyle.renderInline());
        parentElement.appendChild(paneDivElement);
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
        
        if (resizable) {
            EventUpdate.createEventAdd(rc.getServerMessage(), "mousedown", separatorElementId, "EchoDragPane.mouseDown");
        }

        Element separatorDivElement = document.createElement("div");
        separatorDivElement.setAttribute("id", separatorElementId);
        int separatorPosition = calculateSeparatorPosition(splitPane);
        
        CssStyle separatorDivCssStyle = new CssStyle();
        // font-size/line-height styles correct for IE minimum DIV height rendering issue.
        separatorDivCssStyle.setAttribute("font-size", "1px");
        separatorDivCssStyle.setAttribute("line-height", "0px");
        separatorDivCssStyle.setAttribute("position", "absolute");
        separatorDivCssStyle.setAttribute("padding", "0px");
        separatorDivCssStyle.setAttribute("overflow", "none");
        ColorRender.renderToStyle(separatorDivCssStyle, null, 
                (Color) splitPane.getRenderProperty(SplitPane.PROPERTY_SEPARATOR_COLOR, DEFAULT_SEPARATOR_COLOR));
        Integer orientationValue = (Integer) splitPane.getRenderProperty(SplitPane.PROPERTY_ORIENTATION);
        int orientation = orientationValue == null ? SplitPane.ORIENTATION_HORIZONTAL : orientationValue.intValue();
        if (orientation == SplitPane.ORIENTATION_VERTICAL) {
            separatorDivCssStyle.setAttribute("top", separatorPosition + "px");
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
            separatorDivCssStyle.setAttribute("left", separatorPosition + "px");
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
                DomUpdate.createDomRemove(rc.getServerMessage(), parentId + "_pane_0");
            }
        }
        if (!equal(previousRenderState.pane1, currentRenderState.pane1)) {
            if (previousRenderState.pane1 != null) {
                DomUpdate.createDomRemove(rc.getServerMessage(), parentId + "_pane_1");
            }
        }
    }
    
    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#renderUpdate(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String)
     */
    public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update, String targetId) {
        boolean fullReplace = false;
        if (update.hasUpdatedLayoutDataChildren()) {
            fullReplace = true;
        } else if (update.hasUpdatedProperties()) {
            fullReplace = true;
        }
        
        if (fullReplace) {
            // Perform full update.
            DomUpdate.createDomRemove(rc.getServerMessage(), ContainerInstance.getElementId(update.getParent()));
            renderAdd(rc, update, targetId, update.getParent());
        } else {
            // Note that these render methods are called regardless of whether updates
            renderRemoveChildren(rc, update);
            renderAddChildren(rc, update);
        }
        
        updateRenderState(rc, update.getParent());
        return fullReplace;
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
