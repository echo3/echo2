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

import nextapp.echo2.app.Border;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.button.AbstractButton;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ActionProcessor;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.DomUpdateSupport;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.SynchronizePeer;
import nextapp.echo2.webcontainer.image.ImageRenderSupport;
import nextapp.echo2.webcontainer.propertyrender.BorderRender;
import nextapp.echo2.webcontainer.propertyrender.ColorRender;
import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
import nextapp.echo2.webcontainer.propertyrender.FontRender;
import nextapp.echo2.webcontainer.propertyrender.ImageReferenceRender;
import nextapp.echo2.webcontainer.propertyrender.InsetsRender;
import nextapp.echo2.webrender.clientupdate.DomPropertyStore;
import nextapp.echo2.webrender.clientupdate.DomUpdate;
import nextapp.echo2.webrender.clientupdate.EventUpdate;
import nextapp.echo2.webrender.clientupdate.ServerMessage;
import nextapp.echo2.webrender.output.CssStyle;
import nextapp.echo2.webrender.server.Service;
import nextapp.echo2.webrender.server.WebRenderServlet;
import nextapp.echo2.webrender.services.JavaScriptService;
import nextapp.echo2.webrender.util.DomUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Synchronization peer for <code>nextapp.echo2.app.AbstractButton</code>-derived 
 * components.
 */
public class AbstractButtonPeer 
implements ActionProcessor, DomUpdateSupport, ImageRenderSupport, SynchronizePeer {

//    private static final String IMAGE_ID_BACKGROUND = "background";
    private static final String IMAGE_ID_ICON = "icon";
//    private static final String IMAGE_ID_ROLLOVER_BACKGROUND = "rolloverBackground";
    private static final String IMAGE_ID_ROLLOVER_ICON = "rolloverIcon";
//    private static final String IMAGE_ID_ROLLOVER_STATE_ICON = "rolloverStateIcon";
//    private static final String IMAGE_ID_PRESSED_BACKGROUND = "pressedBackground";
    private static final String IMAGE_ID_PRESSED_ICON = "pressedIcon";
//    private static final String IMAGE_ID_PRESSED_STATE_ICON = "pressedStateIcon";
//    private static final String IMAGE_ID_STATE_ICON = "stateIcon";
    
    /**
     * Service to provide supporting JavaScript library.
     */
    public static final Service BUTTON_SERVICE = JavaScriptService.forResource("Echo.Button", 
            "/nextapp/echo2/webcontainer/resource/js/Button.js");

    static {
        WebRenderServlet.getServiceRegistry().add(BUTTON_SERVICE);
    }
    
    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#getContainerId(nextapp.echo2.app.Component)
     */
    public String getContainerId(Component child) {
        throw new UnsupportedOperationException("Component does not support children.");
    }
    
    /**
     * @see nextapp.echo2.webcontainer.image.ImageRenderSupport#getImage(nextapp.echo2.app.Component, 
     *      java.lang.String)
     */
    public ImageReference getImage(Component component, String imageId) {
        if (IMAGE_ID_ICON.equals(imageId)) {
            return (ImageReference) component.getRenderProperty(AbstractButton.PROPERTY_ICON);
        } else if (IMAGE_ID_ROLLOVER_ICON.equals(imageId)) {
            return (ImageReference) component.getRenderProperty(AbstractButton.PROPERTY_ROLLOVER_ICON);
        } else if (IMAGE_ID_PRESSED_ICON.equals(imageId)) {
            return (ImageReference) component.getRenderProperty(AbstractButton.PROPERTY_PRESSED_ICON);
        } else {
            return null;
        }
    }

    /**
     * @see nextapp.echo2.webcontainer.ActionProcessor#processAction(nextapp.echo2.webcontainer.ContainerInstance, 
     *      nextapp.echo2.app.Component, org.w3c.dom.Element)
     */
    public void processAction(ContainerInstance ci, Component component, Element actionElement) {
        ci.getUpdateManager().addClientPropertyUpdate(component, AbstractButton.INPUT_CLICK, null);
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
        ServerMessage serverMessage = rc.getServerMessage();
        String id = ContainerInstance.getElementId(component);
        EventUpdate.createEventRemove(serverMessage, "click", id);
        EventUpdate.createEventRemove(serverMessage, "mouseover", id);
        EventUpdate.createEventRemove(serverMessage, "mousedown", id);
    }
    
    /**
     * @see nextapp.echo2.webcontainer.DomUpdateSupport#renderHtml(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, org.w3c.dom.Element, nextapp.echo2.app.Component)
     */
    public void renderHtml(RenderContext rc, ServerComponentUpdate update, Element parent, Component component) {
        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(BUTTON_SERVICE.getId(), true);
        AbstractButton button = (AbstractButton) component;
        
        Document document = parent.getOwnerDocument();
        Element divElement = document.createElement("div");
        
        String id = ContainerInstance.getElementId(button);
        
        divElement.setAttribute("id", id);
        
        CssStyle cssStyle = new CssStyle();
        cssStyle.setAttribute("position", "relative");
        cssStyle.setAttribute("cursor", "pointer");
        cssStyle.setAttribute("margin", "0px");
        cssStyle.setAttribute("border-spacing", "0px");
        ExtentRender.renderToStyle(cssStyle, "width", (Extent) button.getRenderProperty(AbstractButton.PROPERTY_WIDTH));
        Extent height = (Extent) button.getRenderProperty(AbstractButton.PROPERTY_HEIGHT);
        if (height != null) {
            ExtentRender.renderToStyle(cssStyle, "height", height);
            cssStyle.setAttribute("overflow", "hidden");
        }
        BorderRender.renderToStyle(cssStyle, (Border) button.getRenderProperty(AbstractButton.PROPERTY_BORDER));
        ColorRender.renderToStyle(cssStyle, (Color) button.getRenderProperty(AbstractButton.PROPERTY_FOREGROUND), 
                (Color) button.getRenderProperty(AbstractButton.PROPERTY_BACKGROUND));
        InsetsRender.renderToStyle(cssStyle, "padding", (Insets) button.getRenderProperty(AbstractButton.PROPERTY_INSETS));
        FontRender.renderToStyle(cssStyle, (Font) button.getRenderProperty(AbstractButton.PROPERTY_FONT));
        divElement.setAttribute("style", cssStyle.renderInline());
        
        parent.appendChild(divElement);
        EventUpdate.createEventAdd(serverMessage, "click", id, "EchoButton.processAction");
        EventUpdate.createEventAdd(serverMessage, "mousedown", id, "EchoButton.processPressed");
        
        boolean rolloverEnabled = ((Boolean) button.getRenderProperty(AbstractButton.PROPERTY_ROLLOVER_ENABLED, 
                Boolean.FALSE)).booleanValue();
        boolean pressedEnabled = ((Boolean) button.getRenderProperty(AbstractButton.PROPERTY_PRESSED_ENABLED, 
                Boolean.FALSE)).booleanValue();
        
        if (rolloverEnabled || pressedEnabled) {
            CssStyle baseCssStyle = new CssStyle();
            BorderRender.renderToStyle(baseCssStyle, (Border) button.getRenderProperty(AbstractButton.PROPERTY_BORDER));
            ColorRender.renderToStyle(baseCssStyle, (Color) button.getRenderProperty(AbstractButton.PROPERTY_FOREGROUND), 
                    (Color) button.getRenderProperty(AbstractButton.PROPERTY_BACKGROUND));
            FontRender.renderToStyle(baseCssStyle, (Font) button.getRenderProperty(AbstractButton.PROPERTY_FONT));
            DomPropertyStore.createDomPropertyStore(serverMessage, id, "baseStyle", 
                    baseCssStyle.renderInline());
            
            if (rolloverEnabled) {
                CssStyle rolloverCssStyle = new CssStyle();
                BorderRender.renderToStyle(rolloverCssStyle, (Border) button.getRenderProperty(AbstractButton.PROPERTY_ROLLOVER_BORDER));
                ColorRender.renderToStyle(rolloverCssStyle, (Color) button.getRenderProperty(AbstractButton.PROPERTY_ROLLOVER_FOREGROUND),
                        (Color) button.getRenderProperty(AbstractButton.PROPERTY_ROLLOVER_BACKGROUND));
                FontRender.renderToStyle(rolloverCssStyle, (Font) button.getRenderProperty(AbstractButton.PROPERTY_ROLLOVER_FONT));
                if (rolloverCssStyle.hasAttributes()) {
                    DomPropertyStore.createDomPropertyStore(serverMessage, id, "rolloverStyle", 
                            rolloverCssStyle.renderInline());
                    EventUpdate.createEventAdd(serverMessage, "mouseover", id, "EchoButton.processRolloverEnter");
                }
            }
            
            if (pressedEnabled) {
                CssStyle pressedCssStyle = new CssStyle();
                BorderRender.renderToStyle(pressedCssStyle, (Border) button.getRenderProperty(AbstractButton.PROPERTY_PRESSED_BORDER));
                ColorRender.renderToStyle(pressedCssStyle, (Color) button.getRenderProperty(AbstractButton.PROPERTY_PRESSED_FOREGROUND),
                        (Color) button.getRenderProperty(AbstractButton.PROPERTY_PRESSED_BACKGROUND));
                FontRender.renderToStyle(pressedCssStyle, (Font) button.getRenderProperty(AbstractButton.PROPERTY_PRESSED_FONT));
                if (pressedCssStyle.hasAttributes()) {
                    DomPropertyStore.createDomPropertyStore(serverMessage, id, "pressedStyle", 
                            pressedCssStyle.renderInline());
                }
            }
        }
        
        int itemCount = 0;
        
        ImageReference icon = (ImageReference) button.getRenderProperty(AbstractButton.PROPERTY_ICON);
        String text = (String) button.getRenderProperty(AbstractButton.PROPERTY_TEXT);
        ImageReference stateIcon = null; // TODO. 
        
        if (text != null) {
            ++itemCount;
        }
        if (icon != null) {
            ++itemCount;
        }
        if (itemCount == 1) {
            if (text != null) {
                renderText(divElement, button);
            }
            if (icon !=  null) {
                renderIcon(rc, divElement, button);
            }
        }
        
        if (itemCount == 2) {
            if (stateIcon == null) {
                // Icon & Text.
                
            }
        }
    }
    
    /**
     * @param rc
     * @param containerElement
     * @param button
     */
    private void renderIcon(RenderContext rc, Element containerElement, AbstractButton button) {
        Element imageElement = ImageReferenceRender.renderImageReferenceElement(rc, this, button, IMAGE_ID_ICON);
        containerElement.appendChild(imageElement);
    }
    
    /**
     * @param containerElement
     * @param button
     */
    private void renderText(Element containerElement, AbstractButton button) {
        DomUtil.setElementText(containerElement, (String) button.getRenderProperty(AbstractButton.PROPERTY_TEXT));
    }

    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#renderUpdate(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String)
     */
    public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update, String targetId) {
        String parentId = ContainerInstance.getElementId(update.getParent());
        DomUpdate.createDomRemove(rc.getServerMessage(), parentId);
        renderAdd(rc, update, targetId, update.getParent());
        return false;
    }
}
