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

package nextapp.echo2.webcontainer.test;

import junit.framework.TestCase;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.LayoutDirection;
import nextapp.echo2.webcontainer.propertyrender.AlignmentRender;
import nextapp.echo2.webrender.output.CssStyle;

/**
 * Unit tests for <code>nextapp.echo2.webcontainer.propertyrender.AlignmentRender</code> 
 */
public class AlignmentRenderTest extends TestCase {
    
    private class NullComponent extends Component { }
    
    public void testHorizontalWithComponentLTR() {
        Alignment alignment;
        CssStyle cssStyle = new CssStyle();
        Component component = new NullComponent();
        component.setLayoutDirection(LayoutDirection.LTR);
        
        alignment = new Alignment(Alignment.DEFAULT, Alignment.DEFAULT);
        AlignmentRender.renderToStyle(cssStyle, alignment, component);
        assertNull(cssStyle.getAttribute("text-align"));
        
        alignment = new Alignment(Alignment.TOP, Alignment.DEFAULT); // Invalid
        AlignmentRender.renderToStyle(cssStyle, alignment, component);
        assertNull(cssStyle.getAttribute("text-align"));
        
        alignment = new Alignment(Alignment.LEFT, Alignment.DEFAULT);
        AlignmentRender.renderToStyle(cssStyle, alignment, component);
        assertEquals("left", cssStyle.getAttribute("text-align"));
        
        alignment = new Alignment(Alignment.CENTER, Alignment.DEFAULT);
        AlignmentRender.renderToStyle(cssStyle, alignment, component);
        assertEquals("center", cssStyle.getAttribute("text-align"));
        
        alignment = new Alignment(Alignment.RIGHT, Alignment.DEFAULT);
        AlignmentRender.renderToStyle(cssStyle, alignment, component);
        assertEquals("right", cssStyle.getAttribute("text-align"));

        alignment = new Alignment(Alignment.LEADING, Alignment.DEFAULT);
        AlignmentRender.renderToStyle(cssStyle, alignment, component);
        assertEquals("left", cssStyle.getAttribute("text-align"));
        
        alignment = new Alignment(Alignment.TRAILING, Alignment.DEFAULT);
        AlignmentRender.renderToStyle(cssStyle, alignment, component);
        assertEquals("right", cssStyle.getAttribute("text-align"));
    }
    
    public void testHorizontalWithComponentRTL() {
        Alignment alignment;
        CssStyle cssStyle = new CssStyle();
        Component component = new NullComponent();
        component.setLayoutDirection(LayoutDirection.RTL);
        
        alignment = new Alignment(Alignment.DEFAULT, Alignment.DEFAULT);
        AlignmentRender.renderToStyle(cssStyle, alignment, component);
        assertNull(cssStyle.getAttribute("text-align"));
        
        alignment = new Alignment(Alignment.TOP, Alignment.DEFAULT); // Invalid
        AlignmentRender.renderToStyle(cssStyle, alignment, component);
        assertNull(cssStyle.getAttribute("text-align"));
        
        alignment = new Alignment(Alignment.LEFT, Alignment.DEFAULT);
        AlignmentRender.renderToStyle(cssStyle, alignment, component);
        assertEquals("left", cssStyle.getAttribute("text-align"));
        
        alignment = new Alignment(Alignment.CENTER, Alignment.DEFAULT);
        AlignmentRender.renderToStyle(cssStyle, alignment, component);
        assertEquals("center", cssStyle.getAttribute("text-align"));
        
        alignment = new Alignment(Alignment.RIGHT, Alignment.DEFAULT);
        AlignmentRender.renderToStyle(cssStyle, alignment, component);
        assertEquals("right", cssStyle.getAttribute("text-align"));
        
        alignment = new Alignment(Alignment.TRAILING, Alignment.DEFAULT);
        AlignmentRender.renderToStyle(cssStyle, alignment, component);
        assertEquals("left", cssStyle.getAttribute("text-align"));

        alignment = new Alignment(Alignment.LEADING, Alignment.DEFAULT);
        AlignmentRender.renderToStyle(cssStyle, alignment, component);
        assertEquals("right", cssStyle.getAttribute("text-align"));
    }
    
    public void testHorizontalWithoutComponent() {
        Alignment alignment;
        CssStyle cssStyle = new CssStyle();
        
        alignment = new Alignment(Alignment.DEFAULT, Alignment.DEFAULT);
        AlignmentRender.renderToStyle(cssStyle, alignment);
        assertNull(cssStyle.getAttribute("text-align"));
        
        alignment = new Alignment(Alignment.TOP, Alignment.DEFAULT); // Invalid
        AlignmentRender.renderToStyle(cssStyle, alignment);
        assertNull(cssStyle.getAttribute("text-align"));
        
        alignment = new Alignment(Alignment.LEFT, Alignment.DEFAULT);
        AlignmentRender.renderToStyle(cssStyle, alignment);
        assertEquals("left", cssStyle.getAttribute("text-align"));
        
        alignment = new Alignment(Alignment.CENTER, Alignment.DEFAULT);
        AlignmentRender.renderToStyle(cssStyle, alignment);
        assertEquals("center", cssStyle.getAttribute("text-align"));
        
        alignment = new Alignment(Alignment.RIGHT, Alignment.DEFAULT);
        AlignmentRender.renderToStyle(cssStyle, alignment);
        assertEquals("right", cssStyle.getAttribute("text-align"));

        alignment = new Alignment(Alignment.LEADING, Alignment.DEFAULT);
        AlignmentRender.renderToStyle(cssStyle, alignment);
        assertEquals("left", cssStyle.getAttribute("text-align"));
        
        alignment = new Alignment(Alignment.TRAILING, Alignment.DEFAULT);
        AlignmentRender.renderToStyle(cssStyle, alignment);
        assertEquals("right", cssStyle.getAttribute("text-align"));
    }
    
    public void testVertical() {
        Alignment alignment;
        CssStyle cssStyle = new CssStyle();
        
        alignment = new Alignment(Alignment.DEFAULT, Alignment.DEFAULT);
        AlignmentRender.renderToStyle(cssStyle, alignment);
        assertNull(cssStyle.getAttribute("vertical-align"));
        
        alignment = new Alignment(Alignment.DEFAULT, Alignment.LEFT); // Invalid
        AlignmentRender.renderToStyle(cssStyle, alignment);
        assertNull(cssStyle.getAttribute("vertical-align"));
        
        alignment = new Alignment(Alignment.DEFAULT, Alignment.TOP);
        AlignmentRender.renderToStyle(cssStyle, alignment);
        assertEquals("top", cssStyle.getAttribute("vertical-align"));
        
        alignment = new Alignment(Alignment.DEFAULT, Alignment.CENTER);
        AlignmentRender.renderToStyle(cssStyle, alignment);
        assertEquals("middle", cssStyle.getAttribute("vertical-align"));
        
        alignment = new Alignment(Alignment.DEFAULT, Alignment.BOTTOM);
        AlignmentRender.renderToStyle(cssStyle, alignment);
        assertEquals("bottom", cssStyle.getAttribute("vertical-align"));
    }
}
