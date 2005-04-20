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

import nextapp.echo2.app.BackgroundImage;
import nextapp.echo2.app.Border;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
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
import nextapp.echo2.webcontainer.propertyrender.BorderRender;
import nextapp.echo2.webcontainer.propertyrender.ColorRender;
import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
import nextapp.echo2.webcontainer.propertyrender.FontRender;
import nextapp.echo2.webcontainer.propertyrender.ImageReferenceRender;
import nextapp.echo2.webcontainer.propertyrender.InsetsRender;
import nextapp.echo2.webrender.clientupdate.DomUpdate;
import nextapp.echo2.webrender.clientupdate.EventUpdate;
import nextapp.echo2.webrender.clientupdate.ServerMessage;
import nextapp.echo2.webrender.output.CssStyle;
import nextapp.echo2.webrender.server.Service;
import nextapp.echo2.webrender.server.WebRenderServlet;
import nextapp.echo2.webrender.services.JavaScriptService;
import nextapp.echo2.webrender.util.DomUtil;

/**
 * Synchronization peer for <code>nextapp.echo2.app.WindowPane</code> components.
 */
public class WindowPanePeer 
implements ActionProcessor, DomUpdateSupport, ImageRenderSupport, PropertyUpdateProcessor,  SynchronizePeer {
    
    private static final Border DEFAULT_BORDER = new Border(new Extent(1, Extent.PX), Color.BLACK, Border.STYLE_SOLID);
    private static final Extent DEFAULT_POSITION_X = new Extent(64, Extent.PX);
    private static final Extent DEFAULT_POSITION_Y = new Extent(64, Extent.PX);
    
    private static final String DEFAULT_WIDTH = "512px";
    private static final String DEFAULT_HEIGHT = "256px";

    private static final String IMAGE_ID_TITLE_BACKGROUND = "background";
    private static final String IMAGE_ID_CLOSE_ICON = "close";
    
    private static final ImageReference DEFAULT_CLOSE_ICON 
            = new ResourceImageReference("/nextapp/echo2/webcontainer/resource/image/DefaultCloseButton.gif");
    
    /**
     * Service to provide supporting JavaScript library.
     */
    public static final Service DRAG_WINDOW_SERVICE = JavaScriptService.forResource("Echo.DragWindow", 
            "/nextapp/echo2/webcontainer/resource/js/DragWindow.js");

    static {
        WebRenderServlet.getServiceRegistry().add(DRAG_WINDOW_SERVICE);
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
            BackgroundImage backgroundImage = (BackgroundImage) component.getRenderProperty(
                    WindowPane.PROPERTY_TITLE_BACKGROUND_IMAGE);
            if (backgroundImage == null) {
                return null;
            } else {
                return backgroundImage.getImage();
            }
        } else if (IMAGE_ID_CLOSE_ICON.equals(imageId)) {
            return (ImageReference) component.getRenderProperty(WindowPane.PROPERTY_CLOSE_ICON,
                    DEFAULT_CLOSE_ICON);
        } else {
            return null;
        }
    }

    /**
     * @see nextapp.echo2.webcontainer.ActionProcessor#processAction(nextapp.echo2.webcontainer.ContainerInstance, nextapp.echo2.app.Component, org.w3c.dom.Element)
     */
    public void processAction(ContainerInstance ci, Component component, Element actionElement) {
        ci.getUpdateManager().addClientPropertyUpdate(component, WindowPane.INPUT_CLOSE, null);
    }

    /**
     * @see nextapp.echo2.webcontainer.PropertyUpdateProcessor#processPropertyUpdate(
     *      nextapp.echo2.webcontainer.ContainerInstance, nextapp.echo2.app.Component, org.w3c.dom.Element)
     */
    public void processPropertyUpdate(ContainerInstance ci, Component component, Element propertyElement) {
        String propertyName = propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_NAME);
        if ("positionX".equals(propertyName)) {
            ci.getUpdateManager().addClientPropertyUpdate(component, WindowPane.PROPERTY_POSITION_X, 
                    ExtentRender.toExtent(propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_VALUE)));
        } else if ("positionY".equals(propertyName)) {
            ci.getUpdateManager().addClientPropertyUpdate(component, WindowPane.PROPERTY_POSITION_Y, 
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
     * @see nextapp.echo2.webcontainer.SynchronizePeer#renderDispose(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, nextapp.echo2.app.Component)
     */
    public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component) {
        String elementId = ContainerInstance.getElementId(component);
        EventUpdate.createEventRemove(rc.getServerMessage(), "click", elementId + "_close");
        EventUpdate.createEventRemove(rc.getServerMessage(), "mousedown", elementId + "_close");
        EventUpdate.createEventRemove(rc.getServerMessage(), "mousedown", elementId + "_title");
    }
    
    /**
     * @see nextapp.echo2.webcontainer.DomUpdateSupport#renderHtml(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, org.w3c.dom.Element, nextapp.echo2.app.Component)
     */
    public void renderHtml(RenderContext rc, ServerComponentUpdate update,
            Element parentElement, Component component) {
        WindowPane windowPane = (WindowPane) component;
        String elementId = ContainerInstance.getElementId(windowPane);
        String title = (String) windowPane.getRenderProperty(WindowPane.PROPERTY_TITLE);
        Extent width = (Extent) windowPane.getRenderProperty(WindowPane.PROPERTY_WIDTH);
        Extent height = (Extent) windowPane.getRenderProperty(WindowPane.PROPERTY_HEIGHT);
        
        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(DRAG_WINDOW_SERVICE.getId(), true);
        
        Document document = serverMessage.getDocument();
        Element windowDivElement = document.createElement("div");
        windowDivElement.setAttribute("id", elementId);
        
        CssStyle outerDivCssStyle = new CssStyle();
        outerDivCssStyle.setAttribute("padding", "0px");
        outerDivCssStyle.setAttribute("position", "absolute");
        outerDivCssStyle.setAttribute("top", ExtentRender.renderCssAttributeValue(
                (Extent) windowPane.getRenderProperty(WindowPane.PROPERTY_POSITION_Y, DEFAULT_POSITION_Y)));
        outerDivCssStyle.setAttribute("left", ExtentRender.renderCssAttributeValue(
                (Extent) windowPane.getRenderProperty(WindowPane.PROPERTY_POSITION_X, DEFAULT_POSITION_X)));
        if (width == null) {
            outerDivCssStyle.setAttribute("width", DEFAULT_WIDTH);
        } else {
            outerDivCssStyle.setAttribute("width", ExtentRender.renderCssAttributeValue(width));
        }
        ColorRender.renderToStyle(outerDivCssStyle, (Color) windowPane.getRenderProperty(WindowPane.PROPERTY_FOREGROUND),
                (Color) windowPane.getRenderProperty(WindowPane.PROPERTY_BACKGROUND));
        FontRender.renderToStyle(outerDivCssStyle, (Font) windowPane.getRenderProperty(WindowPane.PROPERTY_FONT));
        if (outerDivCssStyle.getAttribute("background-color") == null) {
            outerDivCssStyle.setAttribute("background-color", "white");
        }
        BorderRender.renderToStyle(outerDivCssStyle, 
                (Border) windowPane.getRenderProperty(WindowPane.PROPERTY_BORDER, DEFAULT_BORDER));
        windowDivElement.setAttribute("style", outerDivCssStyle.renderInline());

        Element outerTitleDivElement = document.createElement("div"); 
        outerTitleDivElement.setAttribute("id", elementId + "_title");
        CssStyle outerTitleDivCssStyle = new CssStyle();
        outerTitleDivCssStyle.setAttribute("cursor", "move");
        ColorRender.renderToStyle(outerTitleDivCssStyle, 
                (Color) windowPane.getRenderProperty(WindowPane.PROPERTY_TITLE_FOREGROUND, Color.WHITE),
                (Color) windowPane.getRenderProperty(WindowPane.PROPERTY_TITLE_BACKGROUND, Color.BLUE));
        FontRender.renderToStyle(outerTitleDivCssStyle, (Font) windowPane.getRenderProperty(WindowPane.PROPERTY_TITLE_FONT));
                
        outerTitleDivCssStyle.setAttribute("width", "100%");
        outerTitleDivElement.setAttribute("style", outerTitleDivCssStyle.renderInline());
        
        Element innerTitleDivElement = document.createElement("div");
        innerTitleDivElement.setAttribute("id", elementId + "_innertitle");
        CssStyle innerTitleDivCssStyle = new CssStyle();
        InsetsRender.renderToStyle(innerTitleDivCssStyle, "padding", 
                (Insets) windowPane.getRenderProperty(WindowPane.PROPERTY_TITLE_INSETS));
        if (innerTitleDivCssStyle.hasAttributes()) {
            innerTitleDivElement.setAttribute("style", innerTitleDivCssStyle.renderInline());
        }
        outerTitleDivElement.appendChild(innerTitleDivElement);
        
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

        Element contentDivElement = document.createElement("div");
        contentDivElement.setAttribute("id", elementId + "_content");
        CssStyle contentDivCssStyle = new CssStyle();
        contentDivCssStyle.setAttribute("overflow", "auto");
        contentDivCssStyle.setAttribute("padding", "0px");
        contentDivCssStyle.setAttribute("position", "relative");
        if (height == null) {
            contentDivCssStyle.setAttribute("height", DEFAULT_HEIGHT);
        } else {
            contentDivCssStyle.setAttribute("height", ExtentRender.renderCssAttributeValue(height));
        }
        contentDivElement.setAttribute("style", contentDivCssStyle.renderInline());
        
        windowDivElement.appendChild(outerTitleDivElement);
        windowDivElement.appendChild(contentDivElement);
        parentElement.appendChild(windowDivElement);
        
        EventUpdate.createEventAdd(serverMessage, "click", elementId + "_close", "EchoDragWindow.userCloseClick");
        EventUpdate.createEventAdd(serverMessage, "mousedown", elementId + "_close", "EchoDragWindow.userCloseMouseDown");
        EventUpdate.createEventAdd(serverMessage, "mousedown", elementId + "_title", "EchoDragWindow.mouseDown");

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
