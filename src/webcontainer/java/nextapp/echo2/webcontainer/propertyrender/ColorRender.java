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

package nextapp.echo2.webcontainer.propertyrender;

import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.webrender.ServerMessage;
import nextapp.echo2.webrender.output.CssStyle;
import nextapp.echo2.webrender.servermessage.DomUpdate;

/**
 * Utility class for rendering <code>nextapp.echo2.app.Color</code>
 * properties to CSS.
 */
public class ColorRender {

    private static final String COLOR_MASK = "#000000";
    
    /**
     * Renders a <code>Color</code> property directly to a 
     * <code>ServerMessage</code> as a DOM update.
     * 
     * @param serverMessage the outgoing <code>ServerMessage</code>
     * @param targetId the id of the HTML element to update
     * @param color the property value
     * @param background true if the updated property is the background color,
     *        false if it is the foreground color
     */
    public static void renderServerMessageUpdate(ServerMessage serverMessage, String targetId, Color color, boolean background) {
        if (color == null) {
            DomUpdate.renderStyleUpdate(serverMessage, targetId, background ? "backgroundColor" : "color", "none");
        } else {
            DomUpdate.renderStyleUpdate(serverMessage, targetId, background ? "backgroundColor" : "color", 
                    renderCssAttributeValue(color));
        }
    }

    /**
     * Renders foreground and background <code>Color</code> properties 
     * to the given CSS style.  Null property values are ignored.
     * 
     * @param cssStyle the target <code>CssStyle</code>
     * @param foreground the foreground color
     * @param background the background color
     */
    public static void renderToStyle(CssStyle cssStyle, Color foreground, Color background) {
        if (foreground != null) {
            cssStyle.setAttribute("color", renderCssAttributeValue(foreground));
        }
        if (background != null) {
            cssStyle.setAttribute("background-color", renderCssAttributeValue(background));
        }
    }
    
    /**
     * Renders the foreground and background <code>Color</code> properties 
     * of the provided <code>Component</code> to a CSS style.
     * 
     * @param cssStyle the target <code>CssStyle</code>
     * @param component the component
     */
    public static void renderToStyle(CssStyle cssStyle, Component component) {
        renderToStyle(cssStyle, (Color) component.getRenderProperty(Component.PROPERTY_FOREGROUND), 
                (Color) component.getRenderProperty(Component.PROPERTY_BACKGROUND));
    }
    
    /**
     * Renders a <code>Color</code> property value to a CSS color attribute 
     * value.
     * 
     * @param color the property value
     * @return the CSS attribute value
     */
    public static final String renderCssAttributeValue(Color color) {
        int rgb = color.getRgb();
        String colorString = Integer.toString(rgb, 16);
        return COLOR_MASK.substring(0, 7 - colorString.length()) + colorString;
    }
    
    /** Non-instantiable class. */
    private ColorRender() { }
}
