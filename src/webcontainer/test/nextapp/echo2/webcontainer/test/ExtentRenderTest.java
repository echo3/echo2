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

import nextapp.echo2.app.Extent;
import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
import junit.framework.TestCase;

/**
 * Unit tests for 
 * <code>nextapp.echo2.webcontainer.propertyrender.ExtentRender</code>.
 */
public class ExtentRenderTest extends TestCase {

    /**
     * Test <code>ExtentRender.renderCssAttributeValue()</code>.
     */
    public void testExtentRender() {
        assertEquals("50cm", ExtentRender.renderCssAttributeValue(new Extent(50, Extent.CM)));
        assertEquals("50em", ExtentRender.renderCssAttributeValue(new Extent(50, Extent.EM)));
        assertEquals("50ex", ExtentRender.renderCssAttributeValue(new Extent(50, Extent.EX)));
        assertEquals("50in", ExtentRender.renderCssAttributeValue(new Extent(50, Extent.IN)));
        assertEquals("50mm", ExtentRender.renderCssAttributeValue(new Extent(50, Extent.MM)));
        assertEquals("50%", ExtentRender.renderCssAttributeValue(new Extent(50, Extent.PERCENT)));
        assertEquals("50pc", ExtentRender.renderCssAttributeValue(new Extent(50, Extent.PC)));
        assertEquals("50pt", ExtentRender.renderCssAttributeValue(new Extent(50, Extent.PT)));
        assertEquals("50px", ExtentRender.renderCssAttributeValue(new Extent(50, Extent.PX)));
    }

    /**
     * Test <code>ExtentRender.toExtent()</code>.
     */
    public void testToExtent() {
        assertEquals(new Extent(50, Extent.CM), ExtentRender.toExtent("50cm"));
        assertEquals(new Extent(50, Extent.EM), ExtentRender.toExtent("50em"));
        assertEquals(new Extent(50, Extent.EX), ExtentRender.toExtent("50ex"));
        assertEquals(new Extent(50, Extent.IN), ExtentRender.toExtent("50in"));
        assertEquals(new Extent(50, Extent.MM), ExtentRender.toExtent("50mm"));
        assertEquals(new Extent(50, Extent.PERCENT), ExtentRender.toExtent("50%"));
        assertEquals(new Extent(50, Extent.PC), ExtentRender.toExtent("50pc"));
        assertEquals(new Extent(50, Extent.PT), ExtentRender.toExtent("50pt"));
        assertEquals(new Extent(50, Extent.PX), ExtentRender.toExtent("50px"));
        assertEquals(null, ExtentRender.toExtent(null));
        assertEquals(null, ExtentRender.toExtent("50foo"));
        assertEquals(null, ExtentRender.toExtent("50"));
        assertEquals(null, ExtentRender.toExtent("foo"));
        assertEquals(null, ExtentRender.toExtent("50m30cm"));
    }
}
