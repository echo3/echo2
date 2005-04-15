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

import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.layout.GridCellLayoutData;
import nextapp.echo2.webcontainer.syncpeer.GridProcessor;
import junit.framework.TestCase;

/**
 * 
 */
public class GridPeerTest extends TestCase {
    
    public void testSimple() {
        Grid grid = new Grid();
        for (int i = 0; i < 10; ++i) {
            grid.add(new Label("test"));
        }
        new GridProcessor(grid);
    }
    
    public void testSimpleWithColumnSpan() {
        Grid grid = new Grid();
        for (int i = 0; i < 10; ++i) {
            Label label = new Label();
            if (i == 4) {
                GridCellLayoutData layoutData = new GridCellLayoutData();
                layoutData.setColumnSpan(2);
                label.setLayoutData(layoutData);
            }
            grid.add(label);
        }
        new GridProcessor(grid);
    }

    /**
     * Tests a grid with a cell spanning two rows:
     *  __ __
     * |0 |1 |
     * |__|__|
     * |2 |3 |
     * |  |__|
     * |  |4 |
     * |__|__|
     * |5 |6 |
     * |__|__|
     */
    public void testSimpleWithRowSpan() {
        Grid grid = new Grid();
        for (int i = 0; i < 7; ++i) {
            Label label = new Label(Integer.toString(i));
            if (i == 2) {
                GridCellLayoutData layoutData = new GridCellLayoutData();
                layoutData.setRowSpan(2);
                label.setLayoutData(layoutData);
            }
            grid.add(label);
        }
        GridProcessor gridProcessor = new GridProcessor(grid);
        assertEquals(2, gridProcessor.getGridXSize());
        assertEquals(4, gridProcessor.getGridYSize());
        assertEquals("0", ((Label) gridProcessor.getContent(0, 0)).getText());
        assertEquals("1", ((Label) gridProcessor.getContent(1, 0)).getText());
        assertEquals("2", ((Label) gridProcessor.getContent(0, 1)).getText());
        assertEquals("3", ((Label) gridProcessor.getContent(1, 1)).getText());
        assertEquals("2", ((Label) gridProcessor.getContent(0, 2)).getText());
        assertEquals("4", ((Label) gridProcessor.getContent(1, 2)).getText());
        assertEquals("5", ((Label) gridProcessor.getContent(0, 3)).getText());
        assertEquals("6", ((Label) gridProcessor.getContent(1, 3)).getText());
    }
    
    /**
     * Tests span collisions.
     * 
     * Requested:   Rendered:
     *  __ __ __     __ __ __
     * |0 |vc|1 |   |0 |vc|1 |
     * |__|  |__|   |__|  |__|
     * |hc \/   |   |hc|  |2 |
     * |__ /\ __|   |__|  |__|
     * |2 |  |3 |   |3 |  |  |
     * |__|__|__|   |__|__|__|
     */
    public void testInvalidAttemptCollision() {
        GridCellLayoutData layoutData;
        Grid grid = new Grid(3);
        grid.add(new Label("0"));
        Label verticalCollider = new Label("vc");
        layoutData = new GridCellLayoutData();
        layoutData.setRowSpan(3);
        verticalCollider.setLayoutData(layoutData);
        grid.add(verticalCollider);
        grid.add(new Label("1"));
        Label horizontalCollider = new Label("hc");
        layoutData = new GridCellLayoutData();
        layoutData.setColumnSpan(3);
        horizontalCollider.setLayoutData(layoutData);
        grid.add(horizontalCollider);
        grid.add(new Label("2"));
        grid.add(new Label("3"));
        GridProcessor gridProcessor = new GridProcessor(grid);
        assertEquals("0", ((Label) gridProcessor.getContent(0, 0)).getText());
        assertEquals("vc", ((Label) gridProcessor.getContent(1, 0)).getText());
        assertEquals("1", ((Label) gridProcessor.getContent(2, 0)).getText());
        assertEquals("hc", ((Label) gridProcessor.getContent(0, 1)).getText());
        assertEquals("vc", ((Label) gridProcessor.getContent(1, 1)).getText());
        assertEquals("2", ((Label) gridProcessor.getContent(2, 1)).getText());
        assertEquals("3", ((Label) gridProcessor.getContent(0, 2)).getText());
        assertEquals("vc", ((Label) gridProcessor.getContent(1, 2)).getText());
    }
    
    public void testSimpleWithOversidedColumnSpan() {
        Grid grid = new Grid();
        for (int i = 0; i < 10; ++i) {
            Label label = new Label();
            if (i == 4) {
                GridCellLayoutData layoutData = new GridCellLayoutData();
                layoutData.setColumnSpan(3);
                label.setLayoutData(layoutData);
            }
            grid.add(label);
        }
        new GridProcessor(grid);
    }

}
