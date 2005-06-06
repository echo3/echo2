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
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.PropertyRenderRegistry;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.propertyrender.BorderRender;
import nextapp.echo2.webcontainer.propertyrender.ColorRender;
import nextapp.echo2.webcontainer.propertyrender.InsetsRender;
import nextapp.echo2.webrender.servermessage.DomUpdate;

/**
 * 
 */
class PropertyAdapters {
    
    //BUGBUG. very likely break these up to individual items and make them publicly accessible.
    
    static class BorderPropertyAdapter extends PropertyRenderRegistry.PropertyRenderAdapter  {
        
        static final String CSS_BORDER = "border";
        
        private String componentPropertyName;
        private String renderPropertyName;
        private String idSuffix;
        
        BorderPropertyAdapter(String componentPropertyName, String idSuffix, String renderPropertyName) {
            super();
            this.componentPropertyName = componentPropertyName;
            this.idSuffix = idSuffix;
            this.renderPropertyName = renderPropertyName;
        }
        
        /**
         * @see nextapp.echo2.webcontainer.PropertyRenderRegistry.PropertyRender#renderProperty(
         *      nextapp.echo2.webcontainer.RenderContext, nextapp.echo2.app.update.ServerComponentUpdate)
         */
        public void renderProperty(RenderContext rc, ServerComponentUpdate update) {
            Border border = (Border) update.getParent().getRenderProperty(componentPropertyName);
            String elementId = idSuffix == null ? ContainerInstance.getElementId(update.getParent())
                    : ContainerInstance.getElementId(update.getParent()) + idSuffix;
            if (border == null) {
                DomUpdate.renderStyleUpdate(rc.getServerMessage(), elementId, renderPropertyName, "");
            } else {
                DomUpdate.renderStyleUpdate(rc.getServerMessage(), elementId, renderPropertyName, 
                        BorderRender.renderCssAttributeValue(border));
            }
        }
    }

    static class ColorPropertyAdapter extends PropertyRenderRegistry.PropertyRenderAdapter {
        
        static final String CSS_BACKGROUND_COLOR = "backgroundColor";
        static final String CSS_COLOR = "color";
        
        private String componentPropertyName;
        private String renderPropertyName;
        private String idSuffix;
        
        ColorPropertyAdapter(String componentPropertyName, String idSuffix, String renderPropertyName) {
            super();
            this.componentPropertyName = componentPropertyName;
            this.idSuffix = idSuffix;
            this.renderPropertyName = renderPropertyName;
        }

        /**
         * @see nextapp.echo2.webcontainer.PropertyRenderRegistry.PropertyRender#renderProperty(
         *      nextapp.echo2.webcontainer.RenderContext, nextapp.echo2.app.update.ServerComponentUpdate)
         */
        public void renderProperty(RenderContext rc, ServerComponentUpdate update) {
            Color color = (Color) update.getParent().getRenderProperty(componentPropertyName);
            String elementId = idSuffix == null ? ContainerInstance.getElementId(update.getParent())
                    : ContainerInstance.getElementId(update.getParent()) + idSuffix;
            if (color == null) {
                DomUpdate.renderStyleUpdate(rc.getServerMessage(), elementId, renderPropertyName, "");
            } else {
                DomUpdate.renderStyleUpdate(rc.getServerMessage(), elementId, renderPropertyName, 
                        ColorRender.renderCssAttributeValue(color));
            }
        }
    }
    
    static class InsetsPropertyAdapter extends PropertyRenderRegistry.PropertyRenderAdapter {
        
        static final String CSS_PADDING = "padding";
        static final String CSS_MARGIN = "margin";
        
        private String componentPropertyName;
        private String renderPropertyName;
        private String idSuffix;
        
        InsetsPropertyAdapter(String componentPropertyName, String idSuffix, String renderPropertyName) {
            super();
            this.componentPropertyName = componentPropertyName;
            this.idSuffix = idSuffix;
            this.renderPropertyName = renderPropertyName;
        }

        /**
         * @see nextapp.echo2.webcontainer.PropertyRenderRegistry.PropertyRender#renderProperty(
         *      nextapp.echo2.webcontainer.RenderContext, nextapp.echo2.app.update.ServerComponentUpdate)
         */
        public void renderProperty(RenderContext rc, ServerComponentUpdate update) {
            Insets insets = (Insets) update.getParent().getRenderProperty(componentPropertyName);
            String elementId = idSuffix == null ? ContainerInstance.getElementId(update.getParent())
                    : ContainerInstance.getElementId(update.getParent()) + idSuffix;
            if (insets == null) {
                DomUpdate.renderStyleUpdate(rc.getServerMessage(), elementId, renderPropertyName, "");
            } else {
                DomUpdate.renderStyleUpdate(rc.getServerMessage(), elementId, renderPropertyName, 
                        InsetsRender.renderCssAttributeValue(insets));
            }
        }
    }

    
}
