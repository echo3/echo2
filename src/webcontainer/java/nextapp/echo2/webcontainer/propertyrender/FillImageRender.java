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

import nextapp.echo2.app.FillImage;
import nextapp.echo2.app.Component;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.image.ImageRenderSupport;
import nextapp.echo2.webcontainer.image.ImageTools;
import nextapp.echo2.webrender.output.CssStyle;
import nextapp.echo2.webrender.server.ClientProperties;

/**
 * Utility class for rendering <code>nextapp.echo2.FillImage</code>
 * properties to CSS.
 */
public class FillImageRender {
    
    /** Non-instantiable class. */
    private FillImageRender() { }
    
    /**
     * Renders a <code>FillImage</code> to a CSS style.
     * 
     * @param cssStyle the CSS style to be updated
     * @param rc the relevant <code>RenderContext</code>
     * @param irs a <code>SynchronizePeer</code> providing 
     *        <code>ImageRenderSupport</code>
     * @param component the relevant <code>Component</code>
     * @param imageId the image id of the background image
     * @param fillImage the <code>FillImage</code> property value
     * @param disableFixedMode a flag indicating that the 'fixed' property of 
     *        the <code>FillImage</code> should be ignored
     */
    public static void renderToStyle(CssStyle cssStyle, RenderContext rc, ImageRenderSupport irs, 
            Component component, String imageId, FillImage fillImage, boolean disableFixedMode) {
        if (fillImage == null) {
            return;
        }
        String imageUri = ImageTools.getUri(rc, irs, component, imageId);
        
        if (rc.getContainerInstance().getClientProperties().getBoolean(
                ClientProperties.PROPRIETARY_IE_PNG_ALPHA_FILTER_REQUIRED)) {
            cssStyle.setAttribute("filter",
                    "progid:DXImageTransform.Microsoft.AlphaImageLoader(enabled=true, sizingMethod=scale src="
                    + imageUri + "')");
        } else {
            cssStyle.setAttribute("background-image", "url(" + imageUri  + ")");
        }
        
        if (!disableFixedMode && fillImage.getAttachment() == FillImage.ATTACHMENT_FIXED) {
            cssStyle.setAttribute("background-attachment", "fixed");
        }
        switch (fillImage.getRepeat()) {
        case FillImage.NO_REPEAT:
            cssStyle.setAttribute("background-repeat", "no-repeat");
            break;
        case FillImage.REPEAT_HORIZONTAL:
            cssStyle.setAttribute("background-repeat", "repeat-x");
            break;
        case FillImage.REPEAT_VERTICAL:
            cssStyle.setAttribute("background-repeat", "repeat-y");
            break;
        default:
            cssStyle.setAttribute("background-repeat", "repeat");
        }
        if (fillImage.getHorizontalOffset() != null || fillImage.getVerticalOffset() != null) {
            StringBuffer positionText = new StringBuffer();
            if (fillImage.getHorizontalOffset() != null) {
                positionText.append("0px");
            } else {
                positionText.append(ExtentRender.renderCssAttributeValue(fillImage.getHorizontalOffset()));
            }
            positionText.append(" " );
            if (fillImage.getVerticalOffset() != null) {
                positionText.append("0px");
            } else {
                positionText.append(ExtentRender.renderCssAttributeValue(fillImage.getVerticalOffset()));
            }
        }
    }    
}
