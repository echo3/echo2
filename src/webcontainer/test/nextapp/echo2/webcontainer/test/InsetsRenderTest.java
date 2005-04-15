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

package nextapp.echo2.webcontainer.test;

import junit.framework.TestCase;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Insets;
import nextapp.echo2.webcontainer.propertyrender.InsetsRender;
import nextapp.echo2.webrender.output.CssStyle;

/**
 * 
 */
public class InsetsRenderTest extends TestCase {
    
    private static final Extent EXTENT_1_PX = new Extent(1, Extent.PX);
    private static final Extent EXTENT_2_PX = new Extent(2, Extent.PX);
    private static final Extent EXTENT_3_PX = new Extent(3, Extent.PX);
    private static final Extent EXTENT_4_PX = new Extent(4, Extent.PX);
    private static final Extent EXTENT_2_PT = new Extent(2, Extent.PT);
    
    public void testRenderUnique() {
        CssStyle cssStyle = new CssStyle();
        InsetsRender.renderToStyle(cssStyle, "padding", new Insets(EXTENT_1_PX, EXTENT_2_PX, EXTENT_3_PX, EXTENT_4_PX));
        assertEquals("2px 3px 4px 1px", cssStyle.getAttribute("padding"));
    }

    public void testRenderHV() {
        CssStyle cssStyle = new CssStyle();
        InsetsRender.renderToStyle(cssStyle, "padding", new Insets(EXTENT_2_PX, EXTENT_2_PT));
        assertEquals("2pt 2px 2pt 2px", cssStyle.getAttribute("padding"));
    }

    public void testRenderSame() {
        CssStyle cssStyle = new CssStyle();
        InsetsRender.renderToStyle(cssStyle, "padding", new Insets(EXTENT_2_PT));
        assertEquals("2pt", cssStyle.getAttribute("padding"));
    }
}
