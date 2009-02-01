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

package nextapp.echo2.webcontainer.propertyrender;

import nextapp.echo2.app.Border;
import nextapp.echo2.webrender.output.CssStyle;

/**
 * Utility class for rendering <code>nextapp.echo2.app.Border</code>
 * properties to CSS.
 */
public class BorderRender {
    
    /**
     * Returns the CSS border style value for a given
     * Border.STYLE_XXX constant.
     * 
     * @param style the style constant
     * @return the CSS style value
     */
    public static final String getStyleValue(int style) {
        switch (style) {
        case Border.STYLE_NONE:
            return "none";
        case Border.STYLE_INSET:
            return "inset";
        case Border.STYLE_OUTSET:
            return "outset";
        case Border.STYLE_SOLID:
            return "solid";
        case Border.STYLE_DOTTED:
            return "dotted";
        case Border.STYLE_DASHED:
            return "dashed";
        case Border.STYLE_GROOVE:
            return "groove";
        case Border.STYLE_RIDGE:
            return "ridge";
        case Border.STYLE_DOUBLE:
            return "double";
        default:
            return "none";
        }
    }
    
    /**
     * Renders a <code>Border</code> property value to a CSS border attribute 
     * value.
     * 
     * @param border the property value
     * @return the CSS attribute value
     */
    public static String renderCssAttributeValue(Border border) {
        StringBuffer out = new StringBuffer();
        if (border.getSize() != null) {
            out.append(ExtentRender.renderCssAttributeValue(border.getSize()));
            out.append(" ");
        }
        out.append(getStyleValue(border.getStyle()));
        if (border.getColor() != null) {
            out.append(" ");
            out.append(ColorRender.renderCssAttributeValue(border.getColor()));
        }
        return out.toString();
    }

    /**
     * Renders a <code>Border</code> property to the given CSS style.
     * Null property values are ignored.
     * 
     * @param cssStyle the target <code>CssStyle</code>
     * @param border the property value
     */
    public static void renderToStyle(CssStyle cssStyle, Border border) {
        if (border == null) {
            return;
        }
        cssStyle.setAttribute("border", renderCssAttributeValue(border));
    }
    
    /** Non-instantiable class. */
    private BorderRender() { }
}
