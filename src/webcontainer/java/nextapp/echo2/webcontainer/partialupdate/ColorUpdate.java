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

package nextapp.echo2.webcontainer.partialupdate;

import nextapp.echo2.app.Color;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.PartialUpdateParticipant;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.propertyrender.ColorRender;
import nextapp.echo2.webrender.servermessage.DomUpdate;

/**
 * A <code>PartialUpdateParticipant</code> to update a CSS property 
 * representing a color, e.g., 'color' or 'background-color'.
 */
public class ColorUpdate
implements PartialUpdateParticipant {
    
    public static final String CSS_BACKGROUND_COLOR = "backgroundColor";
    public static final String CSS_COLOR = "color";
    
    private String componentPropertyName;
    private String cssAttributeName;
    private String idSuffix;
    
    /**
     * Creates a new <code>ColorUpdate</code>.
     * 
     * @param componentPropertyName the name of the property of the component
     * @param idSuffix the suffix to append to the root client-side identifier
     *        of the component (should be null in typical case of no suffix)
     * @param cssAttributeName the name of the CSS attribute to update (see 
     *        CSS constants provided in this class)
     */
    public ColorUpdate(String componentPropertyName, String idSuffix, String cssAttributeName) {
        super();
        this.componentPropertyName = componentPropertyName;
        this.idSuffix = idSuffix;
        this.cssAttributeName = cssAttributeName;
    }

    /**
     * @see nextapp.echo2.webcontainer.PartialUpdateParticipant#canRenderProperty(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate)
     */
    public boolean canRenderProperty(RenderContext rc, ServerComponentUpdate update) {
        return true;
    }

    /**
     * @see nextapp.echo2.webcontainer.PartialUpdateParticipant#renderProperty(
     *      nextapp.echo2.webcontainer.RenderContext, nextapp.echo2.app.update.ServerComponentUpdate)
     */
    public void renderProperty(RenderContext rc, ServerComponentUpdate update) {
        Color color = (Color) update.getParent().getRenderProperty(componentPropertyName);
        String elementId = idSuffix == null ? ContainerInstance.getElementId(update.getParent())
                : ContainerInstance.getElementId(update.getParent()) + idSuffix;
        if (color == null) {
            DomUpdate.renderStyleUpdate(rc.getServerMessage(), elementId, cssAttributeName, "");
        } else {
            DomUpdate.renderStyleUpdate(rc.getServerMessage(), elementId, cssAttributeName, 
                    ColorRender.renderCssAttributeValue(color));
        }
    }
}