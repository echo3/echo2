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

package nextapp.echo2.app.test.componentxml;

import java.io.InputStream;

import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.ResourceImageReference;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.Style;
import nextapp.echo2.app.StyleSheet;
import nextapp.echo2.app.componentxml.StyleSheetLoader;
import nextapp.echo2.app.layout.SplitPaneLayoutData;
import junit.framework.TestCase;

/**
 * 
 */
public class SplitPaneLayoutDataPeerTest extends TestCase {
    
    private StyleSheet styleSheet;
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp()
    throws Exception {
        InputStream in = BackgroundImagePeerTest.class.getResourceAsStream("SplitPaneLayoutDataPeerTest.stylesheet");
        styleSheet = StyleSheetLoader.load(in, StyleSheetLoaderTest.class.getClassLoader());
        in.close();
    }
    
    public void testBasic() {
        Style alphaStyle = styleSheet.getStyle(Row.class, "alpha");
        SplitPaneLayoutData layoutData = (SplitPaneLayoutData) alphaStyle.getProperty(Component.PROPERTY_LAYOUT_DATA);
        assertEquals(new Color(0xabcdef), layoutData.getBackground());
        assertEquals(new Insets(20, 30), layoutData.getInsets());
        assertNotNull(layoutData.getBackgroundImage());
        ImageReference imageReference = layoutData.getBackgroundImage().getImage();
        assertNotNull(imageReference);
        assertTrue(imageReference instanceof ResourceImageReference);
        assertEquals("/nextapp/echo2/test/componentxml/BackgroundImage.png", 
                ((ResourceImageReference) imageReference).getResource());
    }
}
