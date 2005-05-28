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

import nextapp.echo2.app.Extent;
import nextapp.echo2.webrender.output.CssStyle;

/**
 * Utility class for rendering <code>nextapp.echo2.app.Extent</code>
 * properties to CSS.
 */
public class ExtentRender {
    
    /**
     * Determines if the given <code>Extent</code> is of zero length.
     * This method interprets null <code>Extent</code>s to be of zero 
     * length.
     * 
     * @param extent the extent
     * @return true if the extent is of zero length
     */
    public static boolean isZeroLength(Extent extent) {
        return extent == null || extent.getValue() == 0;
    }
    
    /**
     * Attempts to render a given <code>Extent</code> to a pixel CSS attribute 
     * value.  Returns null if the the extent can not be represented by a pixel
     * value. 
     * 
     * @param extent the property value
     * @return the CSS attribute value
     */
    public static final String renderCssAttributePixelValue(Extent extent) {
        if (extent != null && extent.getUnits() == Extent.PX) {
            return extent.getValue() + "px";
        } else {
            return null;
        }
    }
    
    /**
     * Renders an <code>Extent</code> property value to a CSS dimensioned
     * attribute value.
     * 
     * @param extent the property value
     * @return the CSS attribute value
     */
    public static final String renderCssAttributeValue(Extent extent) {
        return extent.getValue() + renderUnits(extent.getUnits());
    }
    
    /**
     * Renders 1/2 the distance specified by an <code>Extent</code> property 
     * value to a CSS dimensioned attribute.
     * For example, an extent that would normally render as "3px" would be
     * rendered as "1.5px" by this method.
     * 
     * @param extent the property value
     * @return the CSS attribute value
     */
    public static final String renderCssAttributeValueHalf(Extent extent) {
        if (extent.getValue() % 2 == 0) {
            return (extent.getValue() / 2) + renderUnits(extent.getUnits());
        } else {
            return (extent.getValue() / 2) + ".5" + renderUnits(extent.getUnits());
        }
    }
    
    /**
     * Renders an <code>Extent</code> property to the given CSS style.
     * Null property values are ignored.
     * 
     * @param cssStyle the target <code>CssStyle</code>
     * @param cssAttribute the CSS attribute name, e.g., "width" or "height".
     * @param extent the property value
     */
    public static final void renderToStyle(CssStyle cssStyle, String cssAttribute, Extent extent) {
        if (extent == null) {
            return;
        }
        cssStyle.setAttribute(cssAttribute, renderCssAttributeValue(extent));
    }
     
    /**
     * Renders the given <code>Extent</code> units constant into a CSS
     * unit suffix.
     * 
     * @param units the <code>Extent</code> units constant value
     * @return the CSS unit suffix
     */
    public static final String renderUnits(int units) {
        switch (units) {
        case Extent.CM:      return "cm";
        case Extent.EM:      return "em";
        case Extent.EX:      return "ex";
        case Extent.IN:      return "in";
        case Extent.MM:      return "mm";
        case Extent.PC:      return "pc";
        case Extent.PERCENT: return "%";
        case Extent.PT:      return "pt";
        case Extent.PX:      return "px";
        default:
            throw new IllegalArgumentException("Invalid extent.");
        }
    }
    
    /**
     * Creates an <code>Extent</code> from the given CSS dimensioned attribute 
     * value.
     * 
     * @param extentString the CSS dimensioned attribute value
     * @return an equivalent <code>Extent</code>, or null if the input 
     * is not valid.
     */
    public static final Extent toExtent(String extentString) {
        try {
            if (extentString == null) {
                return null;
            } else if (extentString.endsWith("px")) {
                int position = Integer.parseInt(extentString.substring(0, extentString.length() - 2));
                return new Extent(position, Extent.PX);
            } else {
                //BUGBUG. currently only supports pixel values.
                return null;
            }
        } catch (NumberFormatException ex) {
            return null;
        }
    }
    
    /**
     * Attempts to convert a given <code>Extent</code> to a pixel value. 
     * Returns <code>defaultPixels</code> if the conversion is not possible.
     * 
     * @param extent the <code>Extent</code> to convert
     * @param defaultPixels the pixel value to return if conversion is 
     *        impossible.
     * @return the value of the <code>extent</code> in pixels 
     */
    public static final int toPixels(Extent extent, int defaultPixels) {
        if (extent != null && extent.getUnits() == Extent.PX) {
            return extent.getValue();
        } else {
            return defaultPixels;
        }
    }
    
    /** Non-instantiable class. */
    private ExtentRender() { }
}
