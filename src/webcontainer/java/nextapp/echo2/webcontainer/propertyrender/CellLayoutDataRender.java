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

import org.w3c.dom.Element;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.layout.CellLayoutData;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.image.ImageRenderSupport;
import nextapp.echo2.webrender.output.CssStyle;

/**
 * Utility class for rendering 
 * <code>nextapp.echo2.app.layout.CellLayoutData</code>
 * layout data properties to CSS.
 */
public class CellLayoutDataRender {
    
    private static String IMAGE_ID_PREFIX_LAYOUT_DATA_BACKGROUND_IMAGE = "CellLayoutData.BackgroundImage.";
    private static int IMAGE_ID_PREFIX_LAYOUT_DATA_BACKGROUND_IMAGE_LENGTH = IMAGE_ID_PREFIX_LAYOUT_DATA_BACKGROUND_IMAGE.length();

    /**
     * A delegate method to be invoked by the container <code>Component</code>'s
     * <code>ComponentSynchronizePeer</code>'s
     * <code>ImageRenderSupport.getImage()</code> implementation. This method
     * will return the appropriate <code>CellLayoutData</code> background
     * image if the identifier corresponds to one, otherwise null is returned.
     * 
     * @param component the container <code>Component</code>
     * @param imageId the identifier of the image
     * @return the <code>ImageReference</code> or null if the specified
     *         <code>imageId</code> does not specify a
     *         <code>CellLayoutData</code> <code>BackgroundImage</code>
     * @see #renderBackgroundImageToStyle(CssStyle, RenderContext,
     *      ImageRenderSupport, Component, Component)
     */
    public static ImageReference getCellLayoutDataBackgroundImage(Component component, String imageId) {
        if (imageId.startsWith(IMAGE_ID_PREFIX_LAYOUT_DATA_BACKGROUND_IMAGE)) {
            String childRenderId = imageId.substring(IMAGE_ID_PREFIX_LAYOUT_DATA_BACKGROUND_IMAGE_LENGTH);
            int childCount = component.getComponentCount();
            for (int i = 0; i < childCount; ++i) {
                Component child = component.getComponent(i);
                if (child.getRenderId().equals(childRenderId)) {
                    return ((CellLayoutData) child.getRenderProperty(Component.PROPERTY_LAYOUT_DATA))
                            .getBackgroundImage().getImage();
                }
            }
        }
        return null;
    }
    
    /**
     * Renders the <code>backgroundImage</code> property of a 
     * <code>CellLayoutDataRender</code> to a <code>CssStyle</code>.
     * The image will be assigned an identifier by this object 
     * prefaced with the namespace "CellLayoutData".
     * The <code>ImageRenderSupport.getImage()</code> implementation
     * may obtain images based on these ids by invoking the
     * <code>getCellLayoutDataBackgroundImage()</code> method.  Note
     * that any image id may be safely passed to 
     * <code>getCellLayoutDataBackgroundImage()</code> as it will return
     * null if it does not have an image to match the specified id.
     * 
     * @param cssStyle the target <code>CssStyle</code>
     * @param rc the relevant <code>RenderContext</code>
     * @param irs the <code>ImageRenderSupport</code> which will provide 
     *        identified images
     * @param parent the parent <code>Component</code>
     * @param child the child <code>Component</code>
     * @see #getCellLayoutDataBackgroundImage(Component, String)
     */
    public static void renderBackgroundImageToStyle(CssStyle cssStyle, RenderContext rc, ImageRenderSupport irs,
            Component parent, Component child) {
        CellLayoutData layoutData = (CellLayoutData) child.getRenderProperty(Component.PROPERTY_LAYOUT_DATA);
        if (layoutData == null || layoutData.getBackgroundImage() == null) {
            return;
        }
        FillImageRender.renderToStyle(cssStyle, rc, irs, parent, 
                IMAGE_ID_PREFIX_LAYOUT_DATA_BACKGROUND_IMAGE + child.getRenderId(), layoutData.getBackgroundImage(), 0);
    }
    
    /**
     * Renders a <code>CellLayoutData</code> property to the given CSS style
     * and HTML element. Null property values are handled properly (and default
     * insets are still rendered if provided in such cases).
     * <code>Alignment</code> information will be added to the
     * <code>Element</code> such that block elements contained within it will
     * be properly aligned. All other properties will be rendered using the
     * <code>CssSyle</code>.  Use of this method requires a "transitional"
     * DOCTYPE.
     * 
     * @param element the target <code>Element</code>
     * @param cssStyle the target <code>CssStyle</code>
     * @param component the child <code>Component</code> being laid out (used 
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

    /**
     * Renders a <code>CellLayoutData</code> property to the given CSS style.
     * Null property values are handled properly (and default insets are still
     * rendered if provided in such cases).
     *
     * @param cssStyle the target <code>CssStyle</code>
     * @param component the child <code>Component</code> being laid out (used 
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
    
    /** Non-instantiable class. */
    private CellLayoutDataRender() { }
}
