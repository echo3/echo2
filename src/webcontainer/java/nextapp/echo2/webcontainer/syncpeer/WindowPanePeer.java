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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import nextapp.echo2.app.FillImage;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.FillImageBorder;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.ResourceImageReference;
import nextapp.echo2.app.WindowPane;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ActionProcessor;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.DomUpdateSupport;
import nextapp.echo2.webcontainer.PropertyUpdateProcessor;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.SynchronizePeer;
import nextapp.echo2.webcontainer.SynchronizePeerFactory;
import nextapp.echo2.webcontainer.image.ImageRenderSupport;
import nextapp.echo2.webcontainer.propertyrender.FillImageRender;
import nextapp.echo2.webcontainer.propertyrender.ColorRender;
import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
import nextapp.echo2.webcontainer.propertyrender.FontRender;
import nextapp.echo2.webcontainer.propertyrender.ImageReferenceRender;
import nextapp.echo2.webcontainer.propertyrender.InsetsRender;
import nextapp.echo2.webrender.clientupdate.DomUpdate;
import nextapp.echo2.webrender.clientupdate.EventUpdate;
import nextapp.echo2.webrender.clientupdate.ServerMessage;
import nextapp.echo2.webrender.output.CssStyle;
import nextapp.echo2.webrender.server.ClientProperties;
import nextapp.echo2.webrender.server.Service;
import nextapp.echo2.webrender.server.WebRenderServlet;
import nextapp.echo2.webrender.services.JavaScriptService;
import nextapp.echo2.webrender.util.DomUtil;

/**
 * Synchronization peer for <code>nextapp.echo2.app.WindowPane</code> components.
 */
public class WindowPanePeer 
implements ActionProcessor, DomUpdateSupport, ImageRenderSupport, PropertyUpdateProcessor,  SynchronizePeer {
    
    private static final FillImageBorder DEFAULT_BORDER 
            = new FillImageBorder(new Color(0x00007f), new Insets(20), new Insets(3));
    private static final Extent DEFAULT_POSITION_X = new Extent(64, Extent.PX);
    private static final Extent DEFAULT_POSITION_Y = new Extent(64, Extent.PX);
    
    private static final String DEFAULT_WIDTH = "512px";
    private static final String DEFAULT_HEIGHT = "256px";

    private static final String IMAGE_ID_TITLE_BACKGROUND = "background";
    private static final String IMAGE_ID_CLOSE_ICON = "close";
    
    private static final String IMAGE_ID_BORDER_NW = "borderNW";
    private static final String IMAGE_ID_BORDER_N = "borderN";
    private static final String IMAGE_ID_BORDER_NE = "borderNE";
    private static final String IMAGE_ID_BORDER_W = "borderW";
    private static final String IMAGE_ID_BORDER_E = "borderE";
    private static final String IMAGE_ID_BORDER_SW = "borderSW";
    private static final String IMAGE_ID_BORDER_S = "borderS";
    private static final String IMAGE_ID_BORDER_SE = "borderSE";
    
    private static final ImageReference DEFAULT_CLOSE_ICON 
            = new ResourceImageReference("/nextapp/echo2/webcontainer/resource/image/DefaultCloseButton.gif");
    
    /**
     * Service to provide supporting JavaScript library.
     */
    public static final Service WINDOW_PANE_SERVICE = JavaScriptService.forResource("Echo.WindowPane", 
            "/nextapp/echo2/webcontainer/resource/js/WindowPane.js");

    static {
        WebRenderServlet.getServiceRegistry().add(WINDOW_PANE_SERVICE);
    }

    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#getContainerId(nextapp.echo2.app.Component)
     */
    public String getContainerId(Component child) {
        return ContainerInstance.getElementId(child.getParent()) + "_content";
    }
    
    /**
     * @see nextapp.echo2.webcontainer.image.ImageRenderSupport#getImage(nextapp.echo2.app.Component, 
     *      java.lang.String)
     */
    public ImageReference getImage(Component component, String imageId) {
        if (IMAGE_ID_TITLE_BACKGROUND.equals(imageId)) {
            FillImage backgroundImage = (FillImage) component.getRenderProperty(
                    WindowPane.PROPERTY_TITLE_BACKGROUND_IMAGE);
            if (backgroundImage == null) {
                return null;
            } else {
                return backgroundImage.getImage();
            }
        } else if (IMAGE_ID_CLOSE_ICON.equals(imageId)) {
            return (ImageReference) component.getRenderProperty(WindowPane.PROPERTY_CLOSE_ICON,
                    DEFAULT_CLOSE_ICON);
        } else if (IMAGE_ID_BORDER_NW.equals(imageId)) {
            return ((FillImageBorder) component.getRenderProperty(WindowPane.PROPERTY_BORDER)).getNorthWest().getImage();
        } else if (IMAGE_ID_BORDER_N.equals(imageId)) {
            return ((FillImageBorder) component.getRenderProperty(WindowPane.PROPERTY_BORDER)).getNorth().getImage();
        } else if (IMAGE_ID_BORDER_NE.equals(imageId)) {
            return ((FillImageBorder) component.getRenderProperty(WindowPane.PROPERTY_BORDER)).getNorthEast().getImage();
        } else if (IMAGE_ID_BORDER_W.equals(imageId)) {
            return ((FillImageBorder) component.getRenderProperty(WindowPane.PROPERTY_BORDER)).getWest().getImage();
        } else if (IMAGE_ID_BORDER_E.equals(imageId)) {
            return ((FillImageBorder) component.getRenderProperty(WindowPane.PROPERTY_BORDER)).getEast().getImage();
        } else if (IMAGE_ID_BORDER_SW.equals(imageId)) {
            return ((FillImageBorder) component.getRenderProperty(WindowPane.PROPERTY_BORDER)).getSouthWest().getImage();
        } else if (IMAGE_ID_BORDER_S.equals(imageId)) {
            return ((FillImageBorder) component.getRenderProperty(WindowPane.PROPERTY_BORDER)).getSouth().getImage();
        } else if (IMAGE_ID_BORDER_SE.equals(imageId)) {
            return ((FillImageBorder) component.getRenderProperty(WindowPane.PROPERTY_BORDER)).getSouthEast().getImage();
        } else {
            return null;
        }
    }

    /**
     * @see nextapp.echo2.webcontainer.ActionProcessor#processAction(nextapp.echo2.webcontainer.ContainerInstance, nextapp.echo2.app.Component, org.w3c.dom.Element)
     */
    public void processAction(ContainerInstance ci, Component component, Element actionElement) {
        ci.getUpdateManager().setClientAction(component, WindowPane.INPUT_CLOSE, null);
    }

    /**
     * @see nextapp.echo2.webcontainer.PropertyUpdateProcessor#processPropertyUpdate(
     *      nextapp.echo2.webcontainer.ContainerInstance, nextapp.echo2.app.Component, org.w3c.dom.Element)
     */
    public void processPropertyUpdate(ContainerInstance ci, Component component, Element propertyElement) {
        String propertyName = propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_NAME);
        WindowPane windowPane = (WindowPane) component;
        boolean movable = ((Boolean) windowPane.getRenderProperty(WindowPane.PROPERTY_MOVABLE, Boolean.TRUE)).booleanValue();
        boolean resizable = ((Boolean) windowPane.getRenderProperty(WindowPane.PROPERTY_RESIZABLE, Boolean.TRUE)).booleanValue();
        if (WindowPane.PROPERTY_POSITION_X.equals(propertyName)) {
            if (movable) {
                ci.getUpdateManager().addClientPropertyUpdate(component, WindowPane.PROPERTY_POSITION_X, 
                        ExtentRender.toExtent(propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_VALUE)));
            }
        } else if (WindowPane.PROPERTY_POSITION_Y.equals(propertyName)) {
            if (movable) {
                ci.getUpdateManager().addClientPropertyUpdate(component, WindowPane.PROPERTY_POSITION_Y, 
                        ExtentRender.toExtent(propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_VALUE)));
            }
        } else if (WindowPane.PROPERTY_WIDTH.equals(propertyName)) {
            if (resizable) {
                ci.getUpdateManager().addClientPropertyUpdate(component, WindowPane.PROPERTY_WIDTH, 
                        ExtentRender.toExtent(propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_VALUE)));
            }
        } else if (WindowPane.PROPERTY_HEIGHT.equals(propertyName)) {
            if (resizable) {
            ci.getUpdateManager().addClientPropertyUpdate(component, WindowPane.PROPERTY_HEIGHT, 
                    ExtentRender.toExtent(propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_VALUE)));
            }
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
     * Renders the <code>FillImageBorder</code> as HTML.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param windowDivElement the main DIV element of the window
     * @param windowPane the <code>WindowPane</code> instance
     */
    private void renderBorder(RenderContext rc, Element windowDivElement, WindowPane windowPane) {
        FillImageBorder border = (FillImageBorder) windowPane.getRenderProperty(WindowPane.PROPERTY_BORDER, DEFAULT_BORDER);
        Color borderColor = border.getColor();
        Insets borderInsets = border.getBorderInsets() == null 
                ? new Insets(0) : border.getBorderInsets();
        Document document = rc.getServerMessage().getDocument();
        String elementId = ContainerInstance.getElementId(windowPane);
        boolean resizable = ((Boolean) windowPane.getRenderProperty(WindowPane.PROPERTY_RESIZABLE, Boolean.TRUE)).booleanValue();
                
        boolean renderPositioningBothSides = !rc.getContainerInstance().getClientProperties()
                .getBoolean(ClientProperties.QUIRK_CSS_POSITIONING_ONE_SIDE_ONLY);
        boolean renderSizeExpression = !renderPositioningBothSides && rc.getContainerInstance().getClientProperties()
                .getBoolean(ClientProperties.PROPRIETARY_IE_CSS_EXPRESSIONS_SUPPORTED);
        int borderTopPixels = ExtentRender.toPixels(borderInsets.getTop(), 0);
        int borderLeftPixels = ExtentRender.toPixels(borderInsets.getLeft(), 0);
        int borderRightPixels = ExtentRender.toPixels(borderInsets.getRight(), 0);
        int borderBottomPixels = ExtentRender.toPixels(borderInsets.getBottom(), 0);
        int borderVerticalPixels = borderTopPixels + borderBottomPixels;
        int borderHorizontalPixels = borderLeftPixels + borderRightPixels;

        // North West
        Element borderDivElement = document.createElement("div");
        borderDivElement.setAttribute("id", elementId + "_border_nw");
        CssStyle borderCssStyle = new CssStyle();
        ColorRender.renderToStyle(borderCssStyle, null, borderColor);
        borderCssStyle.setAttribute("position", "absolute");
        borderCssStyle.setAttribute("top", "0px");
        borderCssStyle.setAttribute("left", "0px");
        borderCssStyle.setAttribute("height", borderTopPixels + "px");
        borderCssStyle.setAttribute("width", borderLeftPixels + "px");
        if (resizable) {
            borderCssStyle.setAttribute("cursor", "nw-resize");
        }
        FillImageRender.renderToStyle(borderCssStyle, rc, this, windowPane, IMAGE_ID_BORDER_NW, 
                border.getNorthWest(), false);
        borderDivElement.setAttribute("style", borderCssStyle.renderInline());
        windowDivElement.appendChild(borderDivElement);
        
        // North
        borderDivElement = document.createElement("div");
        borderDivElement.setAttribute("id", elementId + "_border_n");
        borderCssStyle = new CssStyle();
        ColorRender.renderToStyle(borderCssStyle, null, borderColor);
        borderCssStyle.setAttribute("position", "absolute");
        borderCssStyle.setAttribute("top", "0px");
        borderCssStyle.setAttribute("left", borderLeftPixels + "px");
        borderCssStyle.setAttribute("height", borderTopPixels + "px");
        if (renderPositioningBothSides) {
            borderCssStyle.setAttribute("right", borderRightPixels + "px");
        } else if (renderSizeExpression) {
            borderCssStyle.setAttribute("width", "expression((document.getElementById('" 
                    + elementId + "').clientWidth-" + (borderHorizontalPixels) + ")+'px')");
        }
        if (resizable) {
            borderCssStyle.setAttribute("cursor", "n-resize");
        }
        FillImageRender.renderToStyle(borderCssStyle, rc, this, windowPane, IMAGE_ID_BORDER_N, 
                border.getNorth(), false);
        borderDivElement.setAttribute("style", borderCssStyle.renderInline());
        windowDivElement.appendChild(borderDivElement);
        
        // North East
        borderDivElement = document.createElement("div");
        borderDivElement.setAttribute("id", elementId + "_border_ne");
        borderCssStyle = new CssStyle();
        ColorRender.renderToStyle(borderCssStyle, null, borderColor);
        borderCssStyle.setAttribute("position", "absolute");
        borderCssStyle.setAttribute("top", "0px");
        borderCssStyle.setAttribute("right", "0px");
        borderCssStyle.setAttribute("height", borderTopPixels + "px");
        borderCssStyle.setAttribute("width", borderRightPixels + "px");
        if (resizable) {
            borderCssStyle.setAttribute("cursor", "ne-resize");
        }
        FillImageRender.renderToStyle(borderCssStyle, rc, this, windowPane, IMAGE_ID_BORDER_NE, 
                border.getNorthEast(), false);
        borderDivElement.setAttribute("style", borderCssStyle.renderInline());
        windowDivElement.appendChild(borderDivElement);
        
        // West
        borderDivElement = document.createElement("div");
        borderDivElement.setAttribute("id", elementId + "_border_w");
        borderCssStyle = new CssStyle();
        ColorRender.renderToStyle(borderCssStyle, null, borderColor);
        borderCssStyle.setAttribute("position", "absolute");
        borderCssStyle.setAttribute("top", borderTopPixels + "px");
        borderCssStyle.setAttribute("left", "0px");
        borderCssStyle.setAttribute("width", borderLeftPixels + "px");
        if (renderPositioningBothSides) {
            borderCssStyle.setAttribute("bottom", borderRightPixels + "px");
        } else if (renderSizeExpression) {
            borderCssStyle.setAttribute("height", "expression((document.getElementById('" 
                    + elementId + "').clientHeight-" + (borderVerticalPixels) + ")+'px')");
            
        }
        if (resizable) {
            borderCssStyle.setAttribute("cursor", "w-resize");
        }
        FillImageRender.renderToStyle(borderCssStyle, rc, this, windowPane, IMAGE_ID_BORDER_W, 
                border.getWest(), false);
        borderDivElement.setAttribute("style", borderCssStyle.renderInline());
        windowDivElement.appendChild(borderDivElement);
        
        // East
        borderDivElement = document.createElement("div");
        borderDivElement.setAttribute("id", elementId + "_border_e");
        borderCssStyle = new CssStyle();
        ColorRender.renderToStyle(borderCssStyle, null, borderColor);
        borderCssStyle.setAttribute("position", "absolute");
        borderCssStyle.setAttribute("top", borderTopPixels + "px");
        borderCssStyle.setAttribute("right", "0px");
        borderCssStyle.setAttribute("width", borderRightPixels + "px");
        if (renderPositioningBothSides) {
            borderCssStyle.setAttribute("bottom", borderRightPixels + "px");
        } else if (renderSizeExpression) {
            borderCssStyle.setAttribute("height", "expression((document.getElementById('" 
                    + elementId + "').clientHeight-" + (borderVerticalPixels) + ")+'px')");
            
        }
        if (resizable) {
            borderCssStyle.setAttribute("cursor", "e-resize");
        }
        FillImageRender.renderToStyle(borderCssStyle, rc, this, windowPane, IMAGE_ID_BORDER_E, 
                border.getEast(), false);
        borderDivElement.setAttribute("style", borderCssStyle.renderInline());
        windowDivElement.appendChild(borderDivElement);
        
        // South West
        borderDivElement = document.createElement("div");
        borderDivElement.setAttribute("id", elementId + "_border_sw");
        borderCssStyle = new CssStyle();
        ColorRender.renderToStyle(borderCssStyle, null, borderColor);
        borderCssStyle.setAttribute("position", "absolute");
        borderCssStyle.setAttribute("bottom", "0px");
        borderCssStyle.setAttribute("left", "0px");
        borderCssStyle.setAttribute("height", borderBottomPixels + "px");
        borderCssStyle.setAttribute("width", borderLeftPixels + "px");
        if (resizable) {
            borderCssStyle.setAttribute("cursor", "sw-resize");
        }
        FillImageRender.renderToStyle(borderCssStyle, rc, this, windowPane, IMAGE_ID_BORDER_SW, 
                border.getSouthWest(), false);
        borderDivElement.setAttribute("style", borderCssStyle.renderInline());
        windowDivElement.appendChild(borderDivElement);
        
        // South
        borderDivElement = document.createElement("div");
        borderDivElement.setAttribute("id", elementId + "_border_s");
        borderCssStyle = new CssStyle();
        ColorRender.renderToStyle(borderCssStyle, null, borderColor);
        borderCssStyle.setAttribute("position", "absolute");
        borderCssStyle.setAttribute("bottom", "0px");
        borderCssStyle.setAttribute("left", borderLeftPixels + "px");
        borderCssStyle.setAttribute("height", borderBottomPixels + "px");
        if (renderPositioningBothSides) {
            borderCssStyle.setAttribute("right", borderRightPixels + "px");
        } else if (renderSizeExpression) {
            borderCssStyle.setAttribute("width", "expression((document.getElementById('" 
                    + elementId + "').clientWidth-" + (borderHorizontalPixels) + ")+'px')");
            
        }
        if (resizable) {
            borderCssStyle.setAttribute("cursor", "s-resize");
        }
        FillImageRender.renderToStyle(borderCssStyle, rc, this, windowPane, IMAGE_ID_BORDER_S, 
                border.getSouth(), false);
        borderDivElement.setAttribute("style", borderCssStyle.renderInline());
        windowDivElement.appendChild(borderDivElement);
        
        // South East
        borderDivElement = document.createElement("div");
        borderDivElement.setAttribute("id", elementId + "_border_se");
        borderCssStyle = new CssStyle();
        ColorRender.renderToStyle(borderCssStyle, null, borderColor);
        borderCssStyle.setAttribute("position", "absolute");
        borderCssStyle.setAttribute("bottom", "0px");
        borderCssStyle.setAttribute("right", "0px");
        borderCssStyle.setAttribute("height", borderBottomPixels + "px");
        borderCssStyle.setAttribute("width", borderRightPixels + "px");
        if (resizable) {
            borderCssStyle.setAttribute("cursor", "se-resize");
        }
        FillImageRender.renderToStyle(borderCssStyle, rc, this, windowPane, IMAGE_ID_BORDER_SE, 
                border.getSouthEast(), false);
        borderDivElement.setAttribute("style", borderCssStyle.renderInline());
        windowDivElement.appendChild(borderDivElement);
        
        ServerMessage serverMessage = rc.getServerMessage();
        
        if (resizable) {
            EventUpdate.createEventAdd(serverMessage, "mousedown", elementId + "_border_nw", 
                    "EchoWindowPane.windowResizeMouseDown");
            EventUpdate.createEventAdd(serverMessage, "mousedown", elementId + "_border_n", 
                    "EchoWindowPane.windowResizeMouseDown");
            EventUpdate.createEventAdd(serverMessage, "mousedown", elementId + "_border_ne", 
                    "EchoWindowPane.windowResizeMouseDown");
            EventUpdate.createEventAdd(serverMessage, "mousedown", elementId + "_border_w", 
                    "EchoWindowPane.windowResizeMouseDown");
            EventUpdate.createEventAdd(serverMessage, "mousedown", elementId + "_border_e", 
                    "EchoWindowPane.windowResizeMouseDown");
            EventUpdate.createEventAdd(serverMessage, "mousedown", elementId + "_border_sw", 
                    "EchoWindowPane.windowResizeMouseDown");
            EventUpdate.createEventAdd(serverMessage, "mousedown", elementId + "_border_s", 
                    "EchoWindowPane.windowResizeMouseDown");
            EventUpdate.createEventAdd(serverMessage, "mousedown", elementId + "_border_se", 
                    "EchoWindowPane.windowResizeMouseDown");
        }
    }
    
    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#renderDispose(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, nextapp.echo2.app.Component)
     */
    public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component) {
        String elementId = ContainerInstance.getElementId(component);
        EventUpdate.createEventRemove(rc.getServerMessage(), "click", elementId + "_close");
        EventUpdate.createEventRemove(rc.getServerMessage(), "mousedown", elementId + "_close");
        EventUpdate.createEventRemove(rc.getServerMessage(), "mousedown", elementId + "_title");
        EventUpdate.createEventRemove(rc.getServerMessage(), "mousedown", elementId + "_border_nw");
        EventUpdate.createEventRemove(rc.getServerMessage(), "mousedown", elementId + "_border_n");
        EventUpdate.createEventRemove(rc.getServerMessage(), "mousedown", elementId + "_border_ne");
        EventUpdate.createEventRemove(rc.getServerMessage(), "mousedown", elementId + "_border_w");
        EventUpdate.createEventRemove(rc.getServerMessage(), "mousedown", elementId + "_border_e");
        EventUpdate.createEventRemove(rc.getServerMessage(), "mousedown", elementId + "_border_sw");
        EventUpdate.createEventRemove(rc.getServerMessage(), "mousedown", elementId + "_border_s");
        EventUpdate.createEventRemove(rc.getServerMessage(), "mousedown", elementId + "_border_se");
    }
    
    /**
     * @see nextapp.echo2.webcontainer.DomUpdateSupport#renderHtml(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, org.w3c.dom.Element, nextapp.echo2.app.Component)
     */
    public void renderHtml(RenderContext rc, ServerComponentUpdate update,
            Element parentElement, Component component) {
        WindowPane windowPane = (WindowPane) component;
        String elementId = ContainerInstance.getElementId(windowPane);
        String bodyElementId = elementId + "_body";
        String title = (String) windowPane.getRenderProperty(WindowPane.PROPERTY_TITLE);
        Extent width = (Extent) windowPane.getRenderProperty(WindowPane.PROPERTY_WIDTH);
        Extent height = (Extent) windowPane.getRenderProperty(WindowPane.PROPERTY_HEIGHT);
        int titleHeightPixels = ExtentRender.toPixels(
                (Extent) windowPane.getRenderProperty(WindowPane.PROPERTY_TITLE_HEIGHT), 28);
        boolean movable = ((Boolean) windowPane.getRenderProperty(WindowPane.PROPERTY_MOVABLE, Boolean.TRUE)).booleanValue();
        boolean renderPositioningBothSides = !rc.getContainerInstance().getClientProperties()
                .getBoolean(ClientProperties.QUIRK_CSS_POSITIONING_ONE_SIDE_ONLY);
        boolean renderSizeExpression = !renderPositioningBothSides && rc.getContainerInstance().getClientProperties()
                .getBoolean(ClientProperties.PROPRIETARY_IE_CSS_EXPRESSIONS_SUPPORTED);
        
        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(WINDOW_PANE_SERVICE.getId(), true);
        Document document = serverMessage.getDocument();
        Element windowDivElement = document.createElement("div");
        windowDivElement.setAttribute("id", elementId);
        
        // Create main window DIV element.
        CssStyle windowDivCssStyle = new CssStyle();
        windowDivCssStyle.setAttribute("z-index", "2"); //BUGBUG. Hardcoded z-index.
        windowDivCssStyle.setAttribute("padding", "0px");
        windowDivCssStyle.setAttribute("position", "absolute");
        windowDivCssStyle.setAttribute("top", ExtentRender.renderCssAttributeValue(
                (Extent) windowPane.getRenderProperty(WindowPane.PROPERTY_POSITION_Y, DEFAULT_POSITION_Y)));
        windowDivCssStyle.setAttribute("left", ExtentRender.renderCssAttributeValue(
                (Extent) windowPane.getRenderProperty(WindowPane.PROPERTY_POSITION_X, DEFAULT_POSITION_X)));
        if (width == null) {
            windowDivCssStyle.setAttribute("width", DEFAULT_WIDTH);
        } else {
            windowDivCssStyle.setAttribute("width", ExtentRender.renderCssAttributeValue(width));
        }
        if (height == null) {
            windowDivCssStyle.setAttribute("height", DEFAULT_HEIGHT);
        } else {
            windowDivCssStyle.setAttribute("height", ExtentRender.renderCssAttributeValue(height));
        }
        windowDivElement.setAttribute("style", windowDivCssStyle.renderInline());
        parentElement.appendChild(windowDivElement);
        
        // Render window border.
        renderBorder(rc, windowDivElement, windowPane);
        
        // Create window body DIV element (container of title and content areas).
        Element windowBodyDivElement = document.createElement("div");
        windowBodyDivElement.setAttribute("id", bodyElementId);
        CssStyle windowBodyDivCssStyle = new CssStyle();
        ColorRender.renderToStyle(windowBodyDivCssStyle, (Color) windowPane.getRenderProperty(WindowPane.PROPERTY_FOREGROUND),
                (Color) windowPane.getRenderProperty(WindowPane.PROPERTY_BACKGROUND));
        FontRender.renderToStyle(windowBodyDivCssStyle, (Font) windowPane.getRenderProperty(WindowPane.PROPERTY_FONT));
        if (windowBodyDivCssStyle.getAttribute("background-color") == null) {
            windowBodyDivCssStyle.setAttribute("background-color", "white");
        }
        windowBodyDivCssStyle.setAttribute("position", "absolute");
        windowBodyDivCssStyle.setAttribute("z-index", "1");
        FillImageBorder border = (FillImageBorder) windowPane.getRenderProperty(WindowPane.PROPERTY_BORDER, DEFAULT_BORDER);
        Insets contentInsets = border.getContentInsets() == null 
                ? new Insets(0) : border.getContentInsets();
        int left = ExtentRender.toPixels(contentInsets.getLeft(), 0);
        int right = ExtentRender.toPixels(contentInsets.getRight(), 0);
        int top = ExtentRender.toPixels(contentInsets.getTop(), 0);
        int bottom = ExtentRender.toPixels(contentInsets.getBottom(), 0);
        windowBodyDivCssStyle.setAttribute("top", top + "px");
        windowBodyDivCssStyle.setAttribute("left", left + "px");
        if (renderPositioningBothSides) {
            windowBodyDivCssStyle.setAttribute("bottom", bottom + "px");
            windowBodyDivCssStyle.setAttribute("right", right + "px");
        } else if (renderSizeExpression) {
            windowBodyDivCssStyle.setAttribute("height", "expression((document.getElementById('" 
                    + elementId + "').clientHeight-" + (top + bottom) + ")+'px')");
            windowBodyDivCssStyle.setAttribute("width", "expression((document.getElementById('" 
                    + elementId + "').clientWidth-" + (left + right) + ")+'px')");
        }
        windowBodyDivElement.setAttribute("style", windowBodyDivCssStyle.renderInline());
        windowDivElement.appendChild(windowBodyDivElement);

        // Create outer title DIV element.
        Element outerTitleDivElement = document.createElement("div"); 
        outerTitleDivElement.setAttribute("id", elementId + "_title");
        CssStyle outerTitleDivCssStyle = new CssStyle();
        if (movable) {
            outerTitleDivCssStyle.setAttribute("cursor", "move");
        }
        ColorRender.renderToStyle(outerTitleDivCssStyle, 
                (Color) windowPane.getRenderProperty(WindowPane.PROPERTY_TITLE_FOREGROUND, Color.WHITE),
                (Color) windowPane.getRenderProperty(WindowPane.PROPERTY_TITLE_BACKGROUND, Color.BLUE));
        FontRender.renderToStyle(outerTitleDivCssStyle, (Font) windowPane.getRenderProperty(WindowPane.PROPERTY_TITLE_FONT));
        outerTitleDivCssStyle.setAttribute("position", "absolute");
        outerTitleDivCssStyle.setAttribute("top", "0px");
        outerTitleDivCssStyle.setAttribute("height", titleHeightPixels + "px");
        outerTitleDivCssStyle.setAttribute("width", "100%");
        outerTitleDivElement.setAttribute("style", outerTitleDivCssStyle.renderInline());
        windowBodyDivElement.appendChild(outerTitleDivElement);
        
        // Create inner title DIV element.
        Element innerTitleDivElement = document.createElement("div");
        innerTitleDivElement.setAttribute("id", elementId + "_innertitle");
        CssStyle innerTitleDivCssStyle = new CssStyle();
        InsetsRender.renderToStyle(innerTitleDivCssStyle, "padding",
                (Insets) windowPane.getRenderProperty(WindowPane.PROPERTY_TITLE_INSETS));
        if (innerTitleDivCssStyle.hasAttributes()) {
            innerTitleDivElement.setAttribute("style", innerTitleDivCssStyle.renderInline());
        }
        outerTitleDivElement.appendChild(innerTitleDivElement);
        
        // Create title layout TABLE.
        Element titleTableElement = document.createElement("table");
        titleTableElement.setAttribute("style", "width:100%; padding: 0px; border-collapse: collapse;");
        Element titleTbodyElement = document.createElement("tbody");
        titleTableElement.appendChild(titleTbodyElement);
        Element titleTrElement = document.createElement("tr");
        titleTbodyElement.appendChild(titleTrElement);
        Element titleLabelTdElement = document.createElement("td");
        titleLabelTdElement.setAttribute("style", "padding: 0px; text-align: left;");
        titleTrElement.appendChild(titleLabelTdElement);
        Element titleControlsTdElement = document.createElement("td");
        titleControlsTdElement.setAttribute("style", "padding: 0px; text-align: right;");
        titleTrElement.appendChild(titleControlsTdElement);
        if (title != null) {
            DomUtil.setElementText(titleLabelTdElement, title);
        }
        Element closeSpanElement = document.createElement("span");
        closeSpanElement.setAttribute("id", elementId + "_close");
        closeSpanElement.setAttribute("style", "cursor: pointer;");
        titleControlsTdElement.appendChild(closeSpanElement);
        ImageReference closeIcon = (ImageReference) windowPane.getRenderProperty(WindowPane.PROPERTY_CLOSE_ICON,
                DEFAULT_CLOSE_ICON);
        if (closeIcon == null) {
            DomUtil.setElementText(closeSpanElement, "[X]");
        } else {
            Element imgElement = ImageReferenceRender.renderImageReferenceElement(rc, this, windowPane, 
                    IMAGE_ID_CLOSE_ICON);
            closeSpanElement.appendChild(imgElement);
        }
        innerTitleDivElement.appendChild(titleTableElement);

        // Create outer content DIV Element.
        Element outerContentDivElement = document.createElement("div");
        outerContentDivElement.setAttribute("id", elementId + "_outercontent");
        CssStyle outerContentDivCssStyle = new CssStyle();
        outerContentDivCssStyle.setAttribute("overflow", "auto");
        outerContentDivCssStyle.setAttribute("padding", "0px");
        outerContentDivCssStyle.setAttribute("position", "absolute");
        outerContentDivCssStyle.setAttribute("width", "100%");
        outerContentDivCssStyle.setAttribute("top", titleHeightPixels + "px");
        if (renderPositioningBothSides) {
            outerContentDivCssStyle.setAttribute("bottom", "0px");
        }
        if (renderSizeExpression) {
            outerContentDivCssStyle.setAttribute("height", "expression((document.getElementById('" 
                    + bodyElementId + "').clientHeight-" + titleHeightPixels + ")+'px')");
        }
        outerContentDivElement.setAttribute("style", outerContentDivCssStyle.renderInline());
        windowBodyDivElement.appendChild(outerContentDivElement);
        
        // Create inset content DIV Element.
        Element contentDivElement = document.createElement("div");
        contentDivElement.setAttribute("id", elementId + "_content");
        CssStyle contentDivCssStyle = new CssStyle();
        InsetsRender.renderToStyle(contentDivCssStyle, "padding", 
                (Insets) windowPane.getRenderProperty(WindowPane.PROPERTY_INSETS));
        contentDivElement.setAttribute("style", contentDivCssStyle.renderInline());
        outerContentDivElement.appendChild(contentDivElement);

        // Add event listeners.
        EventUpdate.createEventAdd(serverMessage, "click", elementId + "_close", "EchoWindowPane.userCloseClick");
        if (movable) {
            EventUpdate.createEventAdd(serverMessage, "mousedown", elementId + "_close", "EchoWindowPane.userCloseMouseDown");
            EventUpdate.createEventAdd(serverMessage, "mousedown", elementId + "_title", "EchoWindowPane.windowMoveMouseDown");
        }

        // Render child.
        if (windowPane.getComponentCount() != 0) {
            Component child = windowPane.getComponent(0);
            SynchronizePeer syncPeer = SynchronizePeerFactory.getPeerForComponent(child.getClass());
            if (syncPeer instanceof DomUpdateSupport) {
                ((DomUpdateSupport) syncPeer).renderHtml(rc, update, contentDivElement, child);
            } else {
                syncPeer.renderAdd(rc, update, elementId, child);
            }
        }
    }

    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#renderUpdate(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String)
     */
    public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update, String targetId) {
        DomUpdate.createDomRemove(rc.getServerMessage(), ContainerInstance.getElementId(update.getParent()));
        renderAdd(rc, update, targetId, update.getParent());
        return true;
    }
}
