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

package nextapp.echo2.webrender.test;

import junit.framework.TestCase;
import nextapp.echo2.webrender.output.CssStyle;

/**
 * A unit test for <code>nextapp.echo2.webrender.output.CssStyle</code>.
 */
public class CssStyleTest 
extends TestCase {

    /**
     * Test basic operations.
     */
    public void testBasic() {
        CssStyle style = new CssStyle();
        assertNull(style.getAttribute("color"));
        assertEquals("", style.renderInline());
        assertFalse(style.hasAttributes());
        
        style.setAttribute("border", "10px solid red");
        assertEquals("10px solid red", style.getAttribute("border"));
        assertEquals("border:10px solid red;", style.renderInline());
        assertTrue(style.hasAttributes());
        
        style.setAttribute("border", "10px solid blue");
        assertEquals("10px solid blue", style.getAttribute("border"));
        assertEquals("border:10px solid blue;", style.renderInline());
        
        style.setAttribute("background-color", "green");
        // Note that we know ordering here only because we know the underlying implementation
        // uses the proprietary low-memory associative array technique.
        assertEquals("border:10px solid blue;background-color:green;", style.renderInline());
        
        assertNull(style.getAttribute("color"));
        assertEquals("10px solid blue", style.getAttribute("border"));
        assertEquals("green", style.getAttribute("background-color"));
    }
}
