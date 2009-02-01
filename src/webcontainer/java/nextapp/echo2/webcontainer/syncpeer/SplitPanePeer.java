/* 
 * This file is part of the Echo Web Application Framework (hereinafter "Echo").
 * Copyright (C) 2002-2009 NextApp, Inc.
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

import nextapp.echo2.app.FillImage;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.LayoutData;
import nextapp.echo2.app.LayoutDirection;
import nextapp.echo2.app.Pane;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.layout.SplitPaneLayoutData;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.PartialUpdateManager;
import nextapp.echo2.webcontainer.PartialUpdateParticipant;
import nextapp.echo2.webcontainer.PropertyUpdateProcessor;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.ComponentSynchronizePeer;
import nextapp.echo2.webcontainer.RenderState;
import nextapp.echo2.webcontainer.SynchronizePeerFactory;
import nextapp.echo2.webcontainer.image.ImageRenderSupport;
import nextapp.echo2.webcontainer.propertyrender.AlignmentRender;
import nextapp.echo2.webcontainer.propertyrender.ColorRender;
import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
import nextapp.echo2.webcontainer.propertyrender.FillImageRender;
import nextapp.echo2.webcontainer.propertyrender.FontRender;
import nextapp.echo2.webcontainer.propertyrender.InsetsRender;
import nextapp.echo2.webrender.ServerMessage;
import nextapp.echo2.webrender.Service;
import nextapp.echo2.webrender.WebRenderServlet;
import nextapp.echo2.webrender.output.CssStyle;
import nextapp.echo2.webrender.servermessage.DomUpdate;
import nextapp.echo2.webrender.service.JavaScriptService;

import org.w3c.dom.Element;

/**
 * Synchronization peer for <code>nextapp.echo2.app.SplitPane</code> components.
 * <p>
 * This class should not be extended or used by classes outside of the
 * Echo framework.
 */
public class SplitPanePeer 
implements ImageRenderSupport, PropertyUpdateProcessor, ComponentSynchronizePeer {
    
    private static final String IMAGE_ID_HORIZONTAL_SEPARATOR = "horizontalSeparator";
    private static final String IMAGE_ID_PANE_0_BACKGROUND = "pane0Background";
    private static final String IMAGE_ID_PANE_1_BACKGROUND = "pane1Background";
    private static final String IMAGE_ID_VERTICAL_SEPARATOR = "verticalSeparator";
    
    private static final Extent DEFAULT_SEPARATOR_POSITION = new Extent(100);

    /**
     * <code>RenderState</code> implementation.
     */
    static class RenderStateImpl 
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
        private RenderStateImpl(SplitPane splitPane) {
            int componentCount = splitPane.getVisibleComponentCount();
            pane0 = (componentCount < 1 || splitPane.getVisibleComponent(0) == null) 
                        ? null : ContainerInstance.getElementId(splitPane.getVisibleComponent(0));
            pane1 = (componentCount < 2 || splitPane.getVisibleComponent(1) == null) 
                        ? null : ContainerInstance.getElementId(splitPane.getVisibleComponent(1));
        }
    }

    /**
     * Service to provide supporting JavaScript library.
     */
    private static final Service SPLIT_PANE_SERVICE = JavaScriptService.forResource("Echo.SplitPane",
            "/nextapp/echo2/webcontainer/resource/js/SplitPane.js");

    static {
        WebRenderServlet.getServiceRegistry().add(SPLIT_PANE_SERVICE);
    }
    
    /**
     * Utility method to evaluate equality of objects in a null-safe fashion.
     */
    private static final boolean equal(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    /**
     * <code>PartialUpdateParticipant</code> to update position of separator.
     */
    private PartialUpdateParticipant separatorPositionUpdate = new PartialUpdateParticipant() {
    
        /**
         * @see nextapp.echo2.webcontainer.PartialUpdateParticipant#renderProperty(nextapp.echo2.webcontainer.RenderContext,
         *      nextapp.echo2.app.update.ServerComponentUpdate)
         */
        public void renderProperty(RenderContext rc, ServerComponentUpdate update) {
            SplitPane splitPane = (SplitPane) update.getParent();
            renderSetSeparatorPositionDirective(rc, splitPane);
        }
    
        /**
         * @see nextapp.echo2.webcontainer.PartialUpdateParticipant#canRenderProperty(nextapp.echo2.webcontainer.RenderContext,
         *      nextapp.echo2.app.update.ServerComponentUpdate)
         */
        public boolean canRenderProperty(RenderContext rc, ServerComponentUpdate update) {
            return true;
        }
    
    }; 
    
    private PartialUpdateManager partialUpdateManager;

    /**
     * Default constructor.
     */
    public SplitPanePeer() {
        super();
        partialUpdateManager = new PartialUpdateManager();
        partialUpdateManager.add(SplitPane.PROPERTY_SEPARATOR_POSITION, separatorPositionUpdate);
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
        return ContainerInstance.getElementId(child.getParent()) + "_pane" + child.getParent().visibleIndexOf(child);
    }
    
    /**
     * @see nextapp.echo2.webcontainer.image.ImageRenderSupport#getImage(nextapp.echo2.app.Component, java.lang.String)
     */
    public ImageReference getImage(Component component, String imageId) {
        if (IMAGE_ID_PANE_0_BACKGROUND.equals(imageId)) {
            return getPaneBackgroundImage(component.getVisibleComponent(0));
        } else if (IMAGE_ID_PANE_1_BACKGROUND.equals(imageId)) {
            return getPaneBackgroundImage(component.getVisibleComponent(1));
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
     * Determines the rendered orientation of the SplitPane, converting leading/trailing orientations to left/right
     * orientations based on layout direction.
     * 
     * @param splitPane the <code>SplitPane</code>
     * @return the rendered orientation
     */
    private int getRenderOrientation(SplitPane splitPane) {
        Integer orientationValue = (Integer) splitPane.getRenderProperty(SplitPane.PROPERTY_ORIENTATION);
        int orientation = orientationValue == null ? SplitPane.ORIENTATION_HORIZONTAL_LEADING_TRAILING 
                : orientationValue.intValue();
        if (orientation == SplitPane.ORIENTATION_HORIZONTAL_LEADING_TRAILING
                || orientation == SplitPane.ORIENTATION_HORIZONTAL_TRAILING_LEADING) {
            LayoutDirection layoutDirection = splitPane.getRenderLayoutDirection();
            if (orientation == SplitPane.ORIENTATION_HORIZONTAL_LEADING_TRAILING) {
                orientation = layoutDirection.isLeftToRight() ? SplitPane.ORIENTATION_HORIZONTAL_LEFT_RIGHT
                        : SplitPane.ORIENTATION_HORIZONTAL_RIGHT_LEFT;
            } else {
                orientation = layoutDirection.isLeftToRight() ? SplitPane.ORIENTATION_HORIZONTAL_RIGHT_LEFT
                        : SplitPane.ORIENTATION_HORIZONTAL_LEFT_RIGHT;
            }
        }
        return orientation;
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
        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(SPLIT_PANE_SERVICE.getId());
        SplitPane splitPane = (SplitPane) component;
        renderInitDirective(rc, splitPane, targetId);
        Component[] children = splitPane.getVisibleComponents();
        for (int i = 0; i < children.length; ++i) {
            renderChild(rc, update, splitPane, children[i]);
        }
        updateRenderState(rc, splitPane);
    }
    
    /**
     * Renders a directive to add a child component.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update
     * @param splitPane the <code>SplitPane</code>
     * @param index the index of the child to add
     */
    private void renderAddChildDirective(RenderContext rc, ServerComponentUpdate update, SplitPane splitPane, int index) {
        String elementId = ContainerInstance.getElementId(splitPane);
        ServerMessage serverMessage = rc.getServerMessage();
        Element partElement = serverMessage.addPart(ServerMessage.GROUP_ID_UPDATE, "EchoSplitPane.MessageProcessor");
        Element addChildElement = serverMessage.getDocument().createElement("add-child");
        addChildElement.setAttribute("eid", elementId);
        addChildElement.setAttribute("index", Integer.toString(index));
        Component child = splitPane.getVisibleComponent(index); 
        renderLayoutData(rc, addChildElement, child, index);
        partElement.appendChild(addChildElement);
        renderChild(rc, update, splitPane, child); 
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
        RenderStateImpl currentRenderState = new RenderStateImpl(splitPane);

        Component[] addedChildren = update.getAddedChildren();
        for (int i = 0; i < addedChildren.length; i++) {
            String id = ContainerInstance.getElementId(addedChildren[i]);
            if (equal(id, currentRenderState.pane0)) {
                renderAddChildDirective(rc, update, splitPane, 0);
            } else if (equal(id, currentRenderState.pane1)) {
                renderAddChildDirective(rc, update, splitPane, 1);
            }
        }
    }
    
    /**
     * Renders an individual child component of the <code>SplitPane</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the <code>ServerComponentUpdate</code> being performed
     * @param child The child <code>Component</code> to be rendered
     */
    private void renderChild(RenderContext rc, ServerComponentUpdate update, SplitPane splitPane, Component child) {
        ComponentSynchronizePeer syncPeer = SynchronizePeerFactory.getPeerForComponent(child.getClass());
        syncPeer.renderAdd(rc, update, getContainerId(child), child);
    }

    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderDispose(nextapp.echo2.webcontainer.RenderContext,
     *      nextapp.echo2.app.update.ServerComponentUpdate, nextapp.echo2.app.Component)
     */
    public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component) {
        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(SPLIT_PANE_SERVICE.getId());
        renderDisposeDirective(rc, (SplitPane) component);
    }

    /**
     * Renders a directive to the outgoing <code>ServerMessage</code> to 
     * dispose the state of a <code>SplitPane</code>, performing tasks such as
     * unregistering event listeners on the client.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param splitPane the <code>SplitPane</code>
     */
    private void renderDisposeDirective(RenderContext rc, SplitPane splitPane) {
        String elementId = ContainerInstance.getElementId(splitPane);
        ServerMessage serverMessage = rc.getServerMessage();
        Element initElement = serverMessage.appendPartDirective(ServerMessage.GROUP_ID_PREREMOVE, 
                "EchoSplitPane.MessageProcessor", "dispose");
        initElement.setAttribute("eid", elementId);
    }

    /**
     * Determines the separator position of the <code>SplitPane</code>.
     * 
     * @param splitPane the <code>SplitPane</code>
     * @return the separator position
     */
    private int getSeparatorPosition(SplitPane splitPane) {
        Extent separatorPosition = (Extent) splitPane.getRenderProperty(SplitPane.PROPERTY_SEPARATOR_POSITION, 
                DEFAULT_SEPARATOR_POSITION);
        return ExtentRender.toPixels(separatorPosition, 100);
    }
    
    /**
     * Determines if the specified update has caused either child of the SplitPane to
     * be relocated (i.e., a child which existed before continues to exist, but at a
     * different index).
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update
     * @return true if any children have been relocated
     */
    private boolean hasRelocatedChildren(RenderContext rc, ServerComponentUpdate update) {
        ContainerInstance ci = rc.getContainerInstance();
        SplitPane splitPane = (SplitPane) update.getParent();
        RenderStateImpl previous = (RenderStateImpl) ci.getRenderState(splitPane);
        if (previous == null) {
            return false;
        }
        RenderStateImpl current = new RenderStateImpl(splitPane);
        
        return (previous.pane0 != null && previous.pane0.equals(current.pane1))
                || (previous.pane1 != null && previous.pane1.equals(current.pane0));
    }
    
    /**
     * Renders a directive to the outgoing <code>ServerMessage</code> to 
     * render a <code>SplitPane</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param splitPane the <code>SplitPane</code>
     * @param targetId the id of the container element
     */
    private void renderInitDirective(RenderContext rc, SplitPane splitPane, String targetId) {
        String elementId = ContainerInstance.getElementId(splitPane);
        boolean vertical = isOrientationVertical(splitPane);
        ServerMessage serverMessage = rc.getServerMessage();
        Element partElement = serverMessage.addPart(ServerMessage.GROUP_ID_UPDATE, "EchoSplitPane.MessageProcessor");
        Element initElement = serverMessage.getDocument().createElement("init");
        initElement.setAttribute("container-eid", targetId);
        initElement.setAttribute("eid", elementId);

        initElement.setAttribute("position", Integer.toString(getSeparatorPosition(splitPane)));
        
        int orientation = getRenderOrientation(splitPane);
        switch (orientation) {
        case SplitPane.ORIENTATION_HORIZONTAL_LEFT_RIGHT:
            initElement.setAttribute("orientation", "l-r");
            break;
        case SplitPane.ORIENTATION_HORIZONTAL_RIGHT_LEFT:
            initElement.setAttribute("orientation", "r-l");
            break;
        case SplitPane.ORIENTATION_VERTICAL_TOP_BOTTOM:
            initElement.setAttribute("orientation", "t-b");
            break;
        case SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP:
            initElement.setAttribute("orientation", "b-t");
            break;
        default:
            throw new IllegalStateException("Invalid orientation: " + orientation);
        }
        
        if (!splitPane.isRenderEnabled()) {
            initElement.setAttribute("enabled", "false");
        }
        Color background = (Color) splitPane.getRenderProperty(SplitPane.PROPERTY_BACKGROUND);
        if (background != null) {
            initElement.setAttribute("background", ColorRender.renderCssAttributeValue(background));
        }
        Color foreground = (Color) splitPane.getRenderProperty(SplitPane.PROPERTY_FOREGROUND);
        if (foreground != null) {
            initElement.setAttribute("foreground", ColorRender.renderCssAttributeValue(foreground));
        }
        Font font = (Font) splitPane.getRenderProperty(SplitPane.PROPERTY_FONT);
        if (font != null) {
            CssStyle fontCssStyle = new CssStyle();
            FontRender.renderToStyle(fontCssStyle, font);
            initElement.setAttribute("font", fontCssStyle.renderInline());
        }

        Boolean booleanValue = (Boolean) splitPane.getRenderProperty(SplitPane.PROPERTY_RESIZABLE);
        boolean resizable = booleanValue == null ? false : booleanValue.booleanValue();
        initElement.setAttribute("resizable", resizable ? "true" : "false");
        
        initElement.setAttribute("separator-size", Integer.toString(calculateSeparatorSize(splitPane)));
        
        Color separatorColor = (Color) splitPane.getRenderProperty(SplitPane.PROPERTY_SEPARATOR_COLOR);
        if (separatorColor != null) {
            initElement.setAttribute("separator-color", ColorRender.renderCssAttributeValue(separatorColor));
        }
        
        FillImage separatorImage = vertical 
                ? (FillImage) splitPane.getRenderProperty(SplitPane.PROPERTY_SEPARATOR_VERTICAL_IMAGE)
                : (FillImage) splitPane.getRenderProperty(SplitPane.PROPERTY_SEPARATOR_HORIZONTAL_IMAGE);
        if (separatorImage != null) {
            CssStyle fillImageCssStyle = new CssStyle();
            FillImageRender.renderToStyle(fillImageCssStyle, rc, this, splitPane, 
                    vertical ? IMAGE_ID_VERTICAL_SEPARATOR : IMAGE_ID_HORIZONTAL_SEPARATOR, separatorImage, 0);
            initElement.setAttribute("separator-image", fillImageCssStyle.renderInline());
        }
        
        Component[] children = splitPane.getVisibleComponents();
        for (int i = 0; i < children.length; ++i) {
            renderLayoutData(rc, initElement, children[i], i);
        }

        partElement.appendChild(initElement);
    }
    
    /**
     * Renders layout data information for a child component.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param containerElement the container element
     * @param component the child component
     * @param index the child index
     */
    private void renderLayoutData(RenderContext rc, Element containerElement, Component component, int index) {
        SplitPaneLayoutData layoutData = (SplitPaneLayoutData) component.getRenderProperty(SplitPane.PROPERTY_LAYOUT_DATA);
        if (layoutData == null) {
            return;
        }
        Element layoutDataElement = rc.getServerMessage().getDocument().createElement("layout-data");
        layoutDataElement.setAttribute("index", Integer.toString(index));
        if (layoutData.getAlignment() != null) {
            CssStyle alignmentStyle = new CssStyle();
            AlignmentRender.renderToStyle(alignmentStyle, layoutData.getAlignment(), component);
            layoutDataElement.setAttribute("alignment", alignmentStyle.renderInline());
        }
        if (layoutData.getBackground() != null) {
            layoutDataElement.setAttribute("background", ColorRender.renderCssAttributeValue(layoutData.getBackground()));
        }
        if (layoutData.getBackgroundImage() != null) {
            CssStyle backgroundImageStyle = new CssStyle();
            FillImageRender.renderToStyle(backgroundImageStyle, rc, this, component.getParent(),  
                    index == 0 ? IMAGE_ID_PANE_0_BACKGROUND : IMAGE_ID_PANE_1_BACKGROUND, layoutData.getBackgroundImage(), 0);
            layoutDataElement.setAttribute("background-image", backgroundImageStyle.renderInline());
        }
        if (!(component instanceof Pane) && layoutData.getInsets() != null) {
            layoutDataElement.setAttribute("insets", InsetsRender.renderCssAttributeValue(layoutData.getInsets()));
        }
        switch (layoutData.getOverflow()) {
        case SplitPaneLayoutData.OVERFLOW_AUTO:
            layoutDataElement.setAttribute("overflow", "auto");
            break;
        case SplitPaneLayoutData.OVERFLOW_HIDDEN:
            layoutDataElement.setAttribute("overflow", "hidden");
            break;
        case SplitPaneLayoutData.OVERFLOW_SCROLL:
            layoutDataElement.setAttribute("overflow", "scroll");
            break;
        }
        if (layoutData.getMinimumSize() != null) {
            layoutDataElement.setAttribute("min-size", Integer.toString(ExtentRender.toPixels(layoutData.getMinimumSize(), -1)));
        }
        if (layoutData.getMaximumSize() != null) {
            layoutDataElement.setAttribute("max-size", Integer.toString(ExtentRender.toPixels(layoutData.getMaximumSize(), -1)));
        }
        
        containerElement.appendChild(layoutDataElement);
    }
    
    /**
     * Renders directives to remove children.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update
     */
    private void renderRemoveChildren(RenderContext rc, ServerComponentUpdate update) {
        ContainerInstance ci = rc.getContainerInstance();
        SplitPane splitPane = (SplitPane) update.getParent();
        RenderStateImpl previousRenderState = (RenderStateImpl) ci.getRenderState(splitPane);

        Component[] removedChildren = update.getRemovedChildren();
        for (int i = 0; i < removedChildren.length; i++) {
            String id = ContainerInstance.getElementId(removedChildren[i]);
            
            if (equal(id, previousRenderState.pane0)) {
                renderRemoveChildDirective(rc, splitPane, 0);
            } else if (equal(id, previousRenderState.pane1)) {
                renderRemoveChildDirective(rc, splitPane, 1);
            }
        }
    }
    
    /**
     * Renders a directive to remove a child component.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param splitPane the <code>SplitPane</code>
     * @param index the index of the child to remove
     */
    private void renderRemoveChildDirective(RenderContext rc, SplitPane splitPane, int index) {
        String elementId = ContainerInstance.getElementId(splitPane);
        ServerMessage serverMessage = rc.getServerMessage();
        Element partElement = serverMessage.addPart(ServerMessage.GROUP_ID_REMOVE, "EchoSplitPane.MessageProcessor");
        Element removeChildElement = serverMessage.getDocument().createElement("remove-child");
        removeChildElement.setAttribute("eid", elementId);
        removeChildElement.setAttribute("index", Integer.toString(index));
        partElement.appendChild(removeChildElement);
    }

    /**
     * Renders a directive to set the separator position.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param splitPane the <code>SplitPane</code>
     */
    private void renderSetSeparatorPositionDirective(RenderContext rc, SplitPane splitPane) {
        String elementId = ContainerInstance.getElementId(splitPane);
        ServerMessage serverMessage = rc.getServerMessage();
        Element partElement = serverMessage.addPart(ServerMessage.GROUP_ID_REMOVE, "EchoSplitPane.MessageProcessor");
        Element setSeparatorPositionElement = serverMessage.getDocument().createElement("set-separator-position");
        setSeparatorPositionElement.setAttribute("eid", elementId);
        setSeparatorPositionElement.setAttribute("position", Integer.toString(getSeparatorPosition(splitPane)));
        partElement.appendChild(setSeparatorPositionElement);
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
        } else if (hasRelocatedChildren(rc, update)) {
            fullReplace = true;
        }
        
        if (fullReplace) {
            // Perform full update.
            renderDisposeDirective(rc, (SplitPane) update.getParent());
            DomUpdate.renderElementRemove(rc.getServerMessage(), ContainerInstance.getElementId(update.getParent()));
            renderAdd(rc, update, targetId, update.getParent());
        } else {
            partialUpdateManager.process(rc, update);
            if (update.hasAddedChildren() || update.hasRemovedChildren()) {
                renderRemoveChildren(rc, update);
                renderAddChildren(rc, update);
            }
        }
        
        updateRenderState(rc, (SplitPane) update.getParent());
        return fullReplace;
    }
    
    /**
     * Update the stored <code>RenderState</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param splitPane the <code>SplitPane</code> component
     */
    private void updateRenderState(RenderContext rc, SplitPane splitPane) {
        rc.getContainerInstance().setRenderState(splitPane, new RenderStateImpl(splitPane));
    }
}
