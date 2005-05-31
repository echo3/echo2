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

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.webrender.output.CssStyle;

/**
 * Utility class for rendering <code>nextapp.echo2.app.Alignment</code>
 * properties to CSS.
 */
public class AlignmentRender {
    
    /**
     * Renders an <code>Alignment</code> property to the given element.
     * The 'align' and 'valign' attributes will be set if they
     * can be derived from the provided <code>Alignment</code>.
     * Null property values are ignored.
     * 
     * @param element the target <code>Element</code>
     * @param alignment the property value
     */
    public static void renderToElement(Element element, Alignment alignment) {
        renderToElement(element, null, alignment);
    }
    
    /**
     * Renders an <code>Alignment</code> property to the given element.
     * The 'align' and 'valign' attributes will be set if they
     * can be derived from the provided <code>Alignment</code>.
     * Null property values are ignored.
     * 
     * @param element the target <code>Element</code>
     * @param component The <code>Component</code> for which the style is being
     *        rendered (necessary for property translation of leading/trailing
     *        alignment settings).
     * @param alignment the property value
     */
    public static void renderToElement(Element element, Component component, Alignment alignment) {
        if (alignment == null) {
            return;
        }
        
        String horizontal = getHorizontal(component, alignment);
        if (horizontal != null) {
            element.setAttribute("align", horizontal);
        }
        String vertical = getVertical(alignment);
        if (vertical != null) {
            element.setAttribute("valign", vertical);
        }
    }

    /**
     * Renders an <code>Alignment</code> property to the given CSS style.
     * The 'text-align' and 'vertical-align' properties will be set if they
     * can be derived from the provided <code>Alignment</code>.
     * Null property values are ignored.
     * 
     * @param cssStyle the target <code>CssStyle</code>
     * @param alignment the property value
     */
    public static void renderToStyle(CssStyle cssStyle, Alignment alignment) {
        renderToStyle(cssStyle, null, alignment);
    }
    
    /**
     * Renders an <code>Alignment</code> property to the given CSS style.
     * The 'text-align' and 'vertical-align' properties will be set if they
     * can be derived from the provided <code>Alignment</code>.
     * Null property values are ignored.
     * 
     * @param cssStyle the target <code>CssStyle</code>
     * @param component The <code>Component</code> for which the style is being
     *        rendered (necessary for property translation of leading/trailing
     *        alignment settings).
     * @param alignment the property value
     */
    public static void renderToStyle(CssStyle cssStyle, Component component, Alignment alignment) {
        if (alignment == null) {
            return;
        }
        
        String horizontal = getHorizontal(component, alignment);
        if (horizontal != null) {
            cssStyle.setAttribute("text-align", horizontal);
        }
        String vertical = getVertical(alignment);
        if (vertical != null) {
            cssStyle.setAttribute("vertical-align", vertical);
        }
    }
    
    private static String getHorizontal(Component component, Alignment alignment) {
        switch (component == null ? alignment.getHorizontal() : alignment.getRenderedHorizontal(component)) {
        case Alignment.LEADING:
        case Alignment.LEFT:
            return "left";
        case Alignment.CENTER:
            return "center";
        case Alignment.TRAILING:
        case Alignment.RIGHT:
            return "right";
        default:
            return null;
        }
    }
    
    private static String getVertical(Alignment alignment) {
        switch (alignment.getVertical()) {
        case Alignment.TOP:
            return "top";
        case Alignment.CENTER:
            return "middle";
        case Alignment.BOTTOM:
            return "bottom";
        default:
            return null;
        }
    }
    
    /** Non-instantiable class. */
    private AlignmentRender() { }
}
