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

import org.w3c.dom.Element;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.layout.CellLayoutData;
import nextapp.echo2.webrender.output.CssStyle;

/**
 * Utility class for rendering 
 * <code>nextapp.echo2.app.layout.CellLayoutData</code>
 * layout data properties to CSS.
 */
public class CellLayoutDataRender {
    
    /**
     * Renders a <code>CellLayoutData</code> property to the given CSS style
     * and HTML element. Null property values are handled properly (and default
     * insets are still rendered if provided in such cases).
     * <code>Alignment</code> information will be added to the
     * <code>Element</code> such that block elements contained within it will
     * be properly aligned. All other properties will be rednered using the
     * <code>CssSyle</code>.
     * 
     * @param element the target <code>Element</code>
     * @param cssStyle the target <code>CssStyle</code>
     * @param component the child <code>Component</code> being layed out (used 
     *        to determine <code>LayoutDirection</code> (LTR/RTL). 
     * @param layoutData the property value
     * @param defaultInsetsAttributeValue the default insets for the cell
     *        (provided as a string in the interest of performance to avoid
     *        repeatedly rendering the same <code>Insets</code> object for
     *        each cell
     */
    public static void renderToElementAndStyle(Element element, CssStyle cssStyle, Component component, CellLayoutData layoutData, 
            String defaultInsetsAttributeValue) {
        if (layoutData == null) {
            if (defaultInsetsAttributeValue != null) {
                cssStyle.setAttribute("padding", defaultInsetsAttributeValue);
            }
            return;
        }
        
        // Render padding.
        Insets cellInsets = layoutData.getInsets();
        if (cellInsets == null) {
            if (defaultInsetsAttributeValue != null) {
                cssStyle.setAttribute("padding", defaultInsetsAttributeValue);
            }
        } else {
            cssStyle.setAttribute("padding", InsetsRender.renderCssAttributeValue(cellInsets));
        }
        
        // Render background.
        ColorRender.renderToStyle(cssStyle, null, layoutData.getBackground());
        
        // Render alignment.
        AlignmentRender.renderToElement(element, layoutData.getAlignment(), component);
    }
    
    /** Non-instantiable class. */
    private CellLayoutDataRender() { }

    /**
     * Renders a <code>CellLayoutData</code> property to the given CSS style.
     * Null property values are handled properly (and default insets are still
     * rendered if provided in such cases).
     *
     * @param cssStyle the target <code>CssStyle</code>
     * @param component the child <code>Component</code> being layed out (used 
     *        to determine <code>LayoutDirection</code> (LTR/RTL). 
     * @param layoutData the property value
     * @param defaultInsetsAttributeValue the default insets for the cell 
     *        (provided as a string in the interest of performance to avoid
     *        repeatedly rendering the same <code>Insets</code> object for
     *        each cell 
     */
    public static void renderToStyle(CssStyle cssStyle, Component component, CellLayoutData layoutData, 
            String defaultInsetsAttributeValue) {
        if (layoutData == null) {
            if (defaultInsetsAttributeValue != null) {
                cssStyle.setAttribute("padding", defaultInsetsAttributeValue);
            }
            return;
        }
        
        // Render padding.
        Insets cellInsets = layoutData.getInsets();
        if (cellInsets == null) {
            if (defaultInsetsAttributeValue != null) {
                cssStyle.setAttribute("padding", defaultInsetsAttributeValue);
            }
        } else {
            cssStyle.setAttribute("padding", InsetsRender.renderCssAttributeValue(cellInsets));
        }
        
        // Render background.
        ColorRender.renderToStyle(cssStyle, null, layoutData.getBackground());
        
        // Render alignment.
        AlignmentRender.renderToStyle(cssStyle, layoutData.getAlignment(), component);
    }
}
