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

import org.w3c.dom.Element;

import nextapp.echo2.app.FillImage;
import nextapp.echo2.app.Border;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.text.TextComponent;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.DomUpdateSupport;
import nextapp.echo2.webcontainer.PropertyRenderRegistry;
import nextapp.echo2.webcontainer.PropertyUpdateProcessor;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.SynchronizePeer;
import nextapp.echo2.webcontainer.image.ImageRenderSupport;
import nextapp.echo2.webcontainer.propertyrender.FillImageRender;
import nextapp.echo2.webcontainer.propertyrender.BorderRender;
import nextapp.echo2.webcontainer.propertyrender.ColorRender;
import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
import nextapp.echo2.webcontainer.propertyrender.FontRender;
import nextapp.echo2.webcontainer.propertyrender.InsetsRender;
import nextapp.echo2.webrender.clientupdate.DomUpdate;
import nextapp.echo2.webrender.clientupdate.EventUpdate;
import nextapp.echo2.webrender.output.CssStyle;
import nextapp.echo2.webrender.server.Service;
import nextapp.echo2.webrender.server.WebRenderServlet;
import nextapp.echo2.webrender.services.JavaScriptService;
import nextapp.echo2.webrender.util.DomUtil;

/**
 * Abstract base synchronization peer for the built-in
 * <code>nextapp.echo2.app.text.TextComponent</code>-derived components.
 */
public abstract class TextComponentPeer
implements DomUpdateSupport, ImageRenderSupport, PropertyUpdateProcessor, SynchronizePeer {
    
    private static final String IMAGE_ID_BACKGROUND = "background";
    
    /**
     * Service to provide supporting JavaScript library.
     */
    public static final Service TEXT_COMPONENT_SERVICE = JavaScriptService.forResource("Echo.TextComponent", 
            "/nextapp/echo2/webcontainer/resource/js/TextComponent.js");
    
    static {
        WebRenderServlet.getServiceRegistry().add(TEXT_COMPONENT_SERVICE);
    }
    
    private PropertyRenderRegistry propertyRenderRegistry;
    
    /**
     * Default constructor.
     */
    public TextComponentPeer() {
        propertyRenderRegistry = new PropertyRenderRegistry();
        //BUGBUG. add property renderers to registry.
    }
    
    /**
     * Creates a base <code>CssStyle</code> for properties common to text 
     * components.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param textComponent the text component
     * @return the style
     */
    protected CssStyle createBaseCssStyle(RenderContext rc, TextComponent textComponent) {
        Extent width = (Extent) textComponent.getRenderProperty(TextComponent.PROPERTY_WIDTH);
        Extent height = (Extent) textComponent.getRenderProperty(TextComponent.PROPERTY_HEIGHT);
        FillImage backgroundImage = (FillImage) textComponent.getRenderProperty(
                TextComponent.PROPERTY_BACKGROUND_IMAGE);

        CssStyle cssStyle = new CssStyle();
        
        BorderRender.renderToStyle(cssStyle, (Border) textComponent.getRenderProperty(TextComponent.PROPERTY_BORDER));
        ColorRender.renderToStyle(cssStyle, (Color) textComponent.getRenderProperty(TextComponent.PROPERTY_FOREGROUND), 
                (Color) textComponent.getRenderProperty(TextComponent.PROPERTY_BACKGROUND));
        FontRender.renderToStyle(cssStyle, (Font) textComponent.getRenderProperty(TextComponent.PROPERTY_FONT));
        if (backgroundImage != null) {
            FillImageRender.renderToStyle(cssStyle, rc, this, textComponent, IMAGE_ID_BACKGROUND, 
                    backgroundImage, true);
        }
        InsetsRender.renderToStyle(cssStyle, "padding", 
                (Insets) textComponent.getRenderProperty(TextComponent.PROPERTY_INSETS));
        if (width != null) {
            cssStyle.setAttribute("width", ExtentRender.renderCssAttributeValue(width));
        }
        if (height != null) {
            cssStyle.setAttribute("height", ExtentRender.renderCssAttributeValue(height));
        }
        return cssStyle;
    }
    
    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#getContainerId(nextapp.echo2.app.Component)
     */
    public String getContainerId(Component child) {
        throw new UnsupportedOperationException("Component does not support children.");
    }
    
    /**
     * @see nextapp.echo2.webcontainer.image.ImageRenderSupport#getImage(nextapp.echo2.app.Component, java.lang.String)
     */
    public ImageReference getImage(Component component, String imageId) {
        if (IMAGE_ID_BACKGROUND.equals(imageId)) {
            FillImage backgroundImage = (FillImage) component.getRenderProperty(
                    TextComponent.PROPERTY_BACKGROUND_IMAGE);
            if (backgroundImage == null) {
                return null;
            } else {
                return backgroundImage.getImage();
            }
        } else {
            return null;
        }
    }
    
    /**
     * @see nextapp.echo2.webcontainer.PropertyUpdateProcessor#processPropertyUpdate(
     *      nextapp.echo2.webcontainer.ContainerInstance, nextapp.echo2.app.Component, org.w3c.dom.Element)
     */
    public void processPropertyUpdate(ContainerInstance ci, Component component, Element propertyElement) {
        String propertyName = propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_NAME);
        if (TextComponent.TEXT_CHANGED_PROPERTY.equals(propertyName)) {
            String propertyValue = DomUtil.getElementText(propertyElement);
            ci.getUpdateManager().addClientPropertyUpdate(component, TextComponent.TEXT_CHANGED_PROPERTY, propertyValue);
        }
    }

    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#renderAdd(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String, nextapp.echo2.app.Component)
     */
    public void renderAdd(RenderContext rc, ServerComponentUpdate update,
            String targetId, Component component) {
        Element contentElement = DomUpdate.createDomAdd(rc.getServerMessage(), targetId);
        renderHtml(rc, update, contentElement, component);
    }

    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#renderDispose(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, nextapp.echo2.app.Component)
     */
    public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component) {
        EventUpdate.createEventRemove(rc.getServerMessage(), "blur", ContainerInstance.getElementId(component));
        EventUpdate.createEventRemove(rc.getServerMessage(), "keyup", ContainerInstance.getElementId(component));
    }
    
    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#renderUpdate(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String)
     */
    public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update, String targetId) {
        boolean fullReplace = false;
        if (update.hasUpdatedProperties()) {
            if (!propertyRenderRegistry.canProcess(rc, update)) {
                fullReplace = true;
            }
        }
        
        if (fullReplace) {
            // Perform full update.
            DomUpdate.createDomRemove(rc.getServerMessage(), ContainerInstance.getElementId(update.getParent()));
            renderAdd(rc, update, targetId, update.getParent());
        } else {
            propertyRenderRegistry.process(rc, update);
        }
        
        return false;
    }
}
