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

import java.util.Locale;

import nextapp.echo2.app.LayoutDirection;
import nextapp.echo2.webrender.output.CssStyle;

/**
 * Utility class for rendering 
 * <code>nextapp.echo2.app.LayoutDirection</code> properties.
 */
public class LayoutDirectionRender {
    
    /**
     * Renders the layout direction of a component to the given CSS style, 
     * based on the provided <code>LayoutDirection</code> and 
     * <code>Locale</code> property values.  Null property values are ignored.
     * <p> 
     * The provided <code>locale</code> and <code>layoutDirection</code>
     * properties should represent the specific settings of a single 
     * <code>Component</code>, NOT those derived recursively from within its
     * hierarchy.  Using the recursively retrieved versions will result in
     * direction information being rendered in cases where it is unncessary.
     * 
     * @param cssStyle the target <code>CssStyle</code>
     * @param layoutDirection the <code>LayoutDirection</code>
     * @param locale the <code>Locale</code>
     */
    public static void renderToStyle(CssStyle cssStyle, LayoutDirection layoutDirection, Locale locale) {
        if (layoutDirection == null) {
            if (locale == null) {
                return;
            }
            layoutDirection = LayoutDirection.forLocale(locale);
        }
        cssStyle.setAttribute("direction", renderCssAttributeValue(layoutDirection));
    }

    /**
     * Renders a CSS attribute value representation of a 
     * <code>LayoutDirection</code>.
     * 
     * @param layoutDirection the <code>LayoutDirection</code> to render
     * @return a CSS attribute value representation
     */
    public static String renderCssAttributeValue(LayoutDirection layoutDirection) {
        return layoutDirection.isLeftToRight() ? "ltr" : "rtl";
    }
}
