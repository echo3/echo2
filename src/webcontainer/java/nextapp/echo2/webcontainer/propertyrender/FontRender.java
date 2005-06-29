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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Font;
import nextapp.echo2.webrender.output.CssStyle;

/**
 * Utility class for rendering <code>nextapp.echo2.app.Font</code>
 * properties to CSS.
 */
public class FontRender {

    /**
     * Renders a <code>Font</code> property to the given CSS style.
     * Null property values are ignored.
     * 
     * @param cssStyle the target <code>CssStyle</code>
     * @param font the property value
     */
    public static void renderToStyle(CssStyle cssStyle, Font font) {
        if (font == null) {
            return;
        }
        Font.Typeface typeFace = font.getTypeface();
        if (typeFace != null) {
            StringBuffer out = new StringBuffer(typeFace.getName());
            typeFace = typeFace.getAlternate();
            while (typeFace != null) {
                out.append(",");
                out.append(typeFace.getName());
                typeFace = typeFace.getAlternate();
            }
            cssStyle.setAttribute("font-family", out.toString());
        }
        if (font.getSize() != null) {
            cssStyle.setAttribute("font-size", ExtentRender.renderCssAttributeValue(font.getSize()));
        }
        if (!font.isPlain()) {
            if (font.isBold()) {
                cssStyle.setAttribute("font-weight", "bold");
            }
            if (font.isItalic()) {
                cssStyle.setAttribute("font-style", "italic");
            }
            if (font.isUnderline() || font.isOverline() || font.isLineThrough()) {
                StringBuffer out = new StringBuffer();
                if (font.isUnderline()) {
                    out.append("underline");
                }
                if (font.isOverline()) {
                    if (out.length() > 0) {
                        out.append(" ");
                    }
                    out.append("overline");
                }
                if (font.isLineThrough()) {
                    if (out.length() > 0) {
                        out.append(" ");
                    }
                    out.append("line-through");
                }
                cssStyle.setAttribute("text-decoration", out.toString()); 
            }
        }
    }

    /**
     * Renders the <code>Font</code> properties of the provided 
     * <code>Component</code> to a CSS style.
     * 
     * @param cssStyle the target <code>CssStyle</code>
     * @param component the component
     */
    public static void renderToStyle(CssStyle cssStyle, Component component) {
        renderToStyle(cssStyle, (Font) component.getRenderProperty(Component.PROPERTY_FONT));
    }
    
    /** Non-instantiable class. */
    private FontRender() { }
}
