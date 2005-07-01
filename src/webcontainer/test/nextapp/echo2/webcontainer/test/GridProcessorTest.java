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
import nextapp.echo2.app.layout.GridLayoutData;
import nextapp.echo2.webcontainer.syncpeer.GridProcessor;
import junit.framework.TestCase;

/**
 * Unit tests for <code>GridProcessor</code>.
 */
public class GridProcessorTest extends TestCase {
    
    /**
     * Test a grid with no child components.
     */
    public void testEmptyGrid() {
        Grid grid = new Grid();
        GridProcessor gridProcessor = new GridProcessor(grid);
        assertEquals(0, gridProcessor.getGridXSize());
        assertEquals(0, gridProcessor.getGridYSize());
    }
    
    /** 
     * Test behavior of Grid with very little content.
     * This is a special case for the <code>GridProcessor</code>.
     *  __ __ __ __
     * |  |XX|XX|XX|
     * |__|XX|XX|XX|
     */
    
    public void testLessThanSize() {
        Grid grid = new Grid();
        grid.setSize(4);
        
        grid.add(new Label());
        
        GridProcessor gridProcessor = new GridProcessor(grid);
        assertEquals(1, gridProcessor.getGridXSize());
        assertEquals(1, gridProcessor.getGridYSize());
    }
    
    /** 
     * Test behavior of Grid with very little content and a needless
     * row span.
     * This is a special case for the <code>GridProcessor</code>.
     *  __ __ __ __
     * |  |XX|XX|XX|
     * |  |XX|XX|XX|
     * |  |XX|XX|XX|
     * |  |XX|XX|XX|
     * |  |XX|XX|XX|
     * |__|XX|XX|XX|
     */
    
    public void testLessThanSizeWithRowSpan() {
        Grid grid = new Grid();
        grid.setSize(4);
        
        Label label = new Label();
        GridLayoutData gridLayoutData = new GridLayoutData();
        gridLayoutData.setRowSpan(3);
        label.setLayoutData(gridLayoutData);
        grid.add(label);
        
        GridProcessor gridProcessor = new GridProcessor(grid);
        assertEquals(1, gridProcessor.getGridXSize());
        assertEquals(1, gridProcessor.getGridYSize());
    }
    
    /**
     * Tests span collisions.
     * 
     * Requested:   Rendered:
     *  __ __ __     __ __ __
     * |0 |VC|1 |   |0 |VC|1 |
     * |__|  |__|   |__|  |__|
     * |HC \/   |   |HC|  |2 |
     * |__ /\ __|   |__|  |__|
     * |2 |  |3 |   |3 |  |  |
     * |__|__|__|   |__|__|__|
     */
    public void testInvalidAttemptCollision() {
        GridLayoutData layoutData;
        Grid grid = new Grid(3);
        grid.add(new Label("0"));
        Label verticalCollider = new Label("VC");
        layoutData = new GridLayoutData();
        layoutData.setRowSpan(3);
        verticalCollider.setLayoutData(layoutData);
        grid.add(verticalCollider);
        grid.add(new Label("1"));
        Label horizontalCollider = new Label("HC");
        layoutData = new GridLayoutData();
        layoutData.setColumnSpan(3);
        horizontalCollider.setLayoutData(layoutData);
        grid.add(horizontalCollider);
        grid.add(new Label("2"));
        grid.add(new Label("3"));
        GridProcessor gridProcessor = new GridProcessor(grid);
        assertEquals("0", ((Label) gridProcessor.getContent(0, 0)).getText());
        assertEquals("VC", ((Label) gridProcessor.getContent(1, 0)).getText());
        assertEquals("1", ((Label) gridProcessor.getContent(2, 0)).getText());
        assertEquals("HC", ((Label) gridProcessor.getContent(0, 1)).getText());
        assertEquals("VC", ((Label) gridProcessor.getContent(1, 1)).getText());
        assertEquals("2", ((Label) gridProcessor.getContent(2, 1)).getText());
        assertEquals("3", ((Label) gridProcessor.getContent(0, 2)).getText());
        assertEquals("VC", ((Label) gridProcessor.getContent(1, 2)).getText());
    }
    
    /**
     * Tests row reduction to ensure proper rendering of the following:
     * 
     * (specified)   (rendered)
     *  __ __         __ __
     * |0 |1 |    0  |0 |1 |
     * |  |  |       |__|  |
     * |XX|  |    1  |XX|  |
     * |XX|  |       |XX|__|
     * |XX|  |    2
     * |XX|  |
     * |XX|  |    3
     * |XX|__|
     */
    public void testOutOfBoundsRowSpan() {
        Grid grid = new Grid();
        grid.add(new Label());

        Label label = new Label();
        GridLayoutData layoutData = new GridLayoutData();
        layoutData.setRowSpan(4);
        label.setLayoutData(layoutData);
        grid.add(label);
        
        GridProcessor gridProcessor =  new GridProcessor(grid);

        // Verify Grid size is correct.
        assertEquals(2, gridProcessor.getGridXSize());
        assertEquals(2, gridProcessor.getGridYSize());
        
        // Verify components are at correct positions.
        assertEquals(0, gridProcessor.getComponentIndex(0, 0));
        assertEquals(1, gridProcessor.getComponentIndex(1, 0));
        assertEquals(-1, gridProcessor.getComponentIndex(0, 1));
        assertEquals(1, gridProcessor.getComponentIndex(1, 1));
        
        // Verify x-spans were untouched.
        assertEquals(1, gridProcessor.getXSpan(0, 0));
        assertEquals(1, gridProcessor.getXSpan(1, 0));
        assertEquals(-1, gridProcessor.getXSpan(0, 1));
        assertEquals(1, gridProcessor.getXSpan(1, 1));

        // Verify y-spans were properly reduced.
        assertEquals(1, gridProcessor.getYSpan(0, 0));
        assertEquals(2, gridProcessor.getYSpan(1, 0));
        assertEquals(-1, gridProcessor.getYSpan(0, 1));
        assertEquals(2, gridProcessor.getYSpan(1, 1));
    }
    
    /**
     * Tests column reduction to ensure proper rendering of the following:
     * 
     *  0  1  2  3  4  5  6    (specified column)
     *  __ _____ ___________
     * |0 |1    |2    |3    |
     * |__|_____|_____|_____|
     * |4 |5    |6 |7 |8    |
     * |__|_____|__|__|     |
     * |9 |10   |11   |     |
     * |__|_____|_____|_____|
     * 
     * 0   1     2  3  4       (rendered column)
     */
    public void testReduceColumnComplex() {
        Grid grid = new Grid();
        grid.setSize(7);
        for (int i = 0; i < 12; ++i) {
            Label label = new Label(Integer.toString(i));
            if (i == 1 || i == 2 || i == 3 || i == 5 || i == 8 || i == 10 || i == 11) {
                GridLayoutData layoutData = new GridLayoutData();
                layoutData.setColumnSpan(2);
                if (i == 8) {
                    layoutData.setRowSpan(2);
                }
                label.setLayoutData(layoutData);
            }
            grid.add(label);
        }

        GridProcessor gridProcessor = new GridProcessor(grid);
        
        // Verify Grid size is correct.
        assertEquals(5, gridProcessor.getGridXSize());
        assertEquals(3, gridProcessor.getGridYSize());
        
        // Verify components are at correct positions.
        assertEquals(0, gridProcessor.getComponentIndex(0, 0));
        assertEquals(1, gridProcessor.getComponentIndex(1, 0));
        assertEquals(2, gridProcessor.getComponentIndex(2, 0));
        assertEquals(2, gridProcessor.getComponentIndex(3, 0));
        assertEquals(3, gridProcessor.getComponentIndex(4, 0));
        assertEquals(4, gridProcessor.getComponentIndex(0, 1));
        assertEquals(5, gridProcessor.getComponentIndex(1, 1));
        assertEquals(6, gridProcessor.getComponentIndex(2, 1));
        assertEquals(7, gridProcessor.getComponentIndex(3, 1));
        assertEquals(8, gridProcessor.getComponentIndex(4, 1));
        assertEquals(9, gridProcessor.getComponentIndex(0, 2));
        assertEquals(10, gridProcessor.getComponentIndex(1, 2));
        assertEquals(11, gridProcessor.getComponentIndex(2, 2));
        assertEquals(11, gridProcessor.getComponentIndex(3, 2));
        assertEquals(8, gridProcessor.getComponentIndex(4, 2));
        
        // Verify x-spans were property reduced.
        assertEquals(1, gridProcessor.getXSpan(0, 0));
        assertEquals(1, gridProcessor.getXSpan(1, 0));
        assertEquals(2, gridProcessor.getXSpan(2, 0));
        assertEquals(2, gridProcessor.getXSpan(3, 0));
        assertEquals(1, gridProcessor.getXSpan(4, 0));
        assertEquals(1, gridProcessor.getXSpan(0, 1));
        assertEquals(1, gridProcessor.getXSpan(1, 1));
        assertEquals(1, gridProcessor.getXSpan(2, 1));
        assertEquals(1, gridProcessor.getXSpan(3, 1));
        assertEquals(1, gridProcessor.getXSpan(4, 1));
        assertEquals(1, gridProcessor.getXSpan(0, 2));
        assertEquals(1, gridProcessor.getXSpan(1, 2));
        assertEquals(2, gridProcessor.getXSpan(2, 2));
        assertEquals(2, gridProcessor.getXSpan(3, 2));
        assertEquals(1, gridProcessor.getXSpan(4, 2));
        
        // Verify y-spans were untouched.
        assertEquals(1, gridProcessor.getYSpan(0, 0));
        assertEquals(1, gridProcessor.getYSpan(1, 0));
        assertEquals(1, gridProcessor.getYSpan(2, 0));
        assertEquals(1, gridProcessor.getYSpan(3, 0));
        assertEquals(1, gridProcessor.getYSpan(4, 0));
        assertEquals(1, gridProcessor.getYSpan(0, 1));
        assertEquals(1, gridProcessor.getYSpan(1, 1));
        assertEquals(1, gridProcessor.getYSpan(3, 1));
        assertEquals(2, gridProcessor.getYSpan(4, 1));
        assertEquals(1, gridProcessor.getYSpan(0, 2));
        assertEquals(1, gridProcessor.getYSpan(1, 2));
        assertEquals(1, gridProcessor.getYSpan(2, 2));
        assertEquals(1, gridProcessor.getYSpan(3, 2));
        assertEquals(2, gridProcessor.getYSpan(4, 2));
    }
    
    /**
     * Tests column reduction to ensure the column #2 is not rendered.
     * 
     *  0  1  2  3   (specified column)
     *  __ _____ __
     * |0 |1    |2 |
     * |__|_____|__|
     * |3 |4    |5 |
     * |__|_____|__|
     * 
     * 0   1     2   (rendered column)
     */
    public void testReduceColumnSimple() {
        Grid grid = new Grid();
        grid.setSize(4);
        for (int i = 0; i < 6; ++i) {
            Label label = new Label(Integer.toString(i));
            if (i == 1 || i == 4) {
                GridLayoutData layoutData = new GridLayoutData();
                layoutData.setColumnSpan(2);
                label.setLayoutData(layoutData);
            }
            grid.add(label);
        }
        GridProcessor gridProcessor = new GridProcessor(grid);
        assertEquals(3, gridProcessor.getGridXSize());
        assertEquals(2, gridProcessor.getGridYSize());
        assertEquals(0, gridProcessor.getComponentIndex(0, 0));
        assertEquals(1, gridProcessor.getComponentIndex(1, 0));
        assertEquals(2, gridProcessor.getComponentIndex(2, 0));
        assertEquals(3, gridProcessor.getComponentIndex(0, 1));
        assertEquals(4, gridProcessor.getComponentIndex(1, 1));
        assertEquals(5, gridProcessor.getComponentIndex(2, 1));
    }
    
    /**
     * Tests column reduction to ensure proper rendering of the following:
     *            specified  rendered              
     *  __ __ __  row:       row:
     * |0 |1 |2 |   
     * |__|__|__|     0        0
     * |3 |4 |5 |
     * |  |  |  |     1        1
     * |  |  |  |
     * |__|__|__|     2
     * |6 |7 |8 |
     * |  |__|  |     3        2
     * |  |9 |  |
     * |__|__|__|     4        3
     * |10|11   |
     * |  |     |     5        4
     * |  |     |
     * |__|_____|     6
     */
    public void testReduceRowComplex() {
        Grid grid = new Grid();
        grid.setSize(3);
        for (int i = 0; i < 12; ++i) {
            Label label = new Label(Integer.toString(i));
            if (i == 3 || i == 4 || i == 5 || i == 6 || i == 8 || i == 10 || i == 11) {
                GridLayoutData layoutData = new GridLayoutData();
                layoutData.setRowSpan(2);
                if (i == 11) {
                    layoutData.setColumnSpan(2);
                }
                label.setLayoutData(layoutData);
            }
            grid.add(label);
        }
        
        GridProcessor gridProcessor = new GridProcessor(grid);
        
        // Verify Grid size is correct.
        assertEquals(3, gridProcessor.getGridXSize());
        assertEquals(5, gridProcessor.getGridYSize());

        // Verify components are at correct positions.
        assertEquals(0, gridProcessor.getComponentIndex(0, 0));
        assertEquals(1, gridProcessor.getComponentIndex(1, 0));
        assertEquals(2, gridProcessor.getComponentIndex(2, 0));
        assertEquals(3, gridProcessor.getComponentIndex(0, 1));
        assertEquals(4, gridProcessor.getComponentIndex(1, 1));
        assertEquals(5, gridProcessor.getComponentIndex(2, 1));
        assertEquals(6, gridProcessor.getComponentIndex(0, 2));
        assertEquals(7, gridProcessor.getComponentIndex(1, 2));
        assertEquals(8, gridProcessor.getComponentIndex(2, 2));
        assertEquals(6, gridProcessor.getComponentIndex(0, 3));
        assertEquals(9, gridProcessor.getComponentIndex(1, 3));
        assertEquals(8, gridProcessor.getComponentIndex(2, 3));
        assertEquals(10, gridProcessor.getComponentIndex(0, 4));
        assertEquals(11, gridProcessor.getComponentIndex(1, 4));
        assertEquals(11, gridProcessor.getComponentIndex(2, 4));
        
        // Verify x-spans were untouched.
        assertEquals(1, gridProcessor.getXSpan(0, 0));
        assertEquals(1, gridProcessor.getXSpan(1, 0));
        assertEquals(1, gridProcessor.getXSpan(2, 0));
        assertEquals(1, gridProcessor.getXSpan(0, 1));
        assertEquals(1, gridProcessor.getXSpan(1, 1));
        assertEquals(1, gridProcessor.getXSpan(2, 1));
        assertEquals(1, gridProcessor.getXSpan(0, 2));
        assertEquals(1, gridProcessor.getXSpan(1, 2));
        assertEquals(1, gridProcessor.getXSpan(2, 2));
        assertEquals(1, gridProcessor.getXSpan(0, 3));
        assertEquals(1, gridProcessor.getXSpan(1, 3));
        assertEquals(1, gridProcessor.getXSpan(2, 3));
        assertEquals(1, gridProcessor.getXSpan(0, 4));
        assertEquals(2, gridProcessor.getXSpan(1, 4));
        assertEquals(2, gridProcessor.getXSpan(2, 4));
        
        // Verify y-spans were properly reduced.
        assertEquals(1, gridProcessor.getYSpan(0, 0));
        assertEquals(1, gridProcessor.getYSpan(1, 0));
        assertEquals(1, gridProcessor.getYSpan(2, 0));
        assertEquals(1, gridProcessor.getYSpan(0, 1));
        assertEquals(1, gridProcessor.getYSpan(1, 1));
        assertEquals(1, gridProcessor.getYSpan(2, 1));
        assertEquals(2, gridProcessor.getYSpan(0, 2));
        assertEquals(1, gridProcessor.getYSpan(1, 2));
        assertEquals(2, gridProcessor.getYSpan(2, 2));
        assertEquals(2, gridProcessor.getYSpan(0, 3));
        assertEquals(1, gridProcessor.getYSpan(1, 3));
        assertEquals(2, gridProcessor.getYSpan(2, 3));
        assertEquals(1, gridProcessor.getYSpan(0, 4));
        assertEquals(1, gridProcessor.getYSpan(1, 4));
        assertEquals(1, gridProcessor.getYSpan(2, 4));
    }
    
    /**
     * Tests row reduction to ensure the row #2 is not rendered.
     * 
     *  __ __  Row:
     * |0 |1 |  0
     * |__|__|
     * |2 |3 |  1
     * |  |  |
     * |  |  |  2
     * |__|__|
     * |4 |5 |  3
     * |__|__|
     */
    public void testReduceRowSimple() {
        Grid grid = new Grid();
        grid.setSize(2);
        for (int i = 0; i < 6; ++i) {
            Label label = new Label(Integer.toString(i));
            if (i == 2 || i == 3) {
                GridLayoutData layoutData = new GridLayoutData();
                layoutData.setRowSpan(2);
                label.setLayoutData(layoutData);
            }
            grid.add(label);
        }
        GridProcessor gridProcessor = new GridProcessor(grid);
        assertEquals(2, gridProcessor.getGridXSize());
        assertEquals(3, gridProcessor.getGridYSize());
        assertEquals(0, gridProcessor.getComponentIndex(0, 0));
        assertEquals(1, gridProcessor.getComponentIndex(1, 0));
        assertEquals(2, gridProcessor.getComponentIndex(0, 1));
        assertEquals(3, gridProcessor.getComponentIndex(1, 1));
        assertEquals(4, gridProcessor.getComponentIndex(0, 2));
        assertEquals(5, gridProcessor.getComponentIndex(1, 2));
    }
    
    /**
     * Test Grid with row and column reduction
     * 
     * (specified)      
     * 0  1  2    (spec) (rend)
     *  __ _____
     * |0 |1    |   0     0
     * |__|_____|
     * |2 |3    |   1     1
     * |  |     |
     * |  |     |   2
     * |__|_____|
     * 
     * 0   1
     * (rendered)
     */
    public void testReduceColumnAndRowSimple() {
        Grid grid = new Grid();
        grid.setSize(3);
        for (int i = 0; i < 4; ++i) {
            Label label = new Label(Integer.toString(i));
            if (i > 0) {
                GridLayoutData layoutData = new GridLayoutData();
                if (i == 1 || i == 3) {
                    layoutData.setColumnSpan(2);
                }
                if (i == 2 || i == 3) {
                    layoutData.setRowSpan(2);
                }
                label.setLayoutData(layoutData);
            }
            grid.add(label);
        }

        GridProcessor gridProcessor = new GridProcessor(grid);
        
        // Verify Grid size is correct.
        assertEquals(2, gridProcessor.getGridXSize());
        assertEquals(2, gridProcessor.getGridYSize());

        // Verify components are at correct positions.
        assertEquals(0, gridProcessor.getComponentIndex(0, 0));
        assertEquals(1, gridProcessor.getComponentIndex(1, 0));
        assertEquals(2, gridProcessor.getComponentIndex(0, 1));
        assertEquals(3, gridProcessor.getComponentIndex(1, 1));
        
        // Verify x-spans were properly reduced.
        assertEquals(1, gridProcessor.getXSpan(0, 0));
        assertEquals(1, gridProcessor.getXSpan(1, 0));
        assertEquals(1, gridProcessor.getXSpan(0, 1));
        assertEquals(1, gridProcessor.getXSpan(1, 1));

        // Verify y-spans were properly reduced.
        assertEquals(1, gridProcessor.getYSpan(0, 0));
        assertEquals(1, gridProcessor.getYSpan(1, 0));
        assertEquals(1, gridProcessor.getYSpan(0, 1));
        assertEquals(1, gridProcessor.getYSpan(1, 1));
    }
    
    
    /**
     * Test reduction of symmetric column spans.
     * 
     *  0  1  2  3  (specified)
     *  ________ __
     * |        |  |
     * |________|__|
     * |        |  |
     * |________|__|
     * |        |  |
     * |________|__|
     * 
     *  0        1  (rendered)         
     */
    public void testReduceColumnLong() {
        Grid grid = new Grid();
        grid.setSize(4);
        
        for (int i = 0; i < 6; ++i) {
            Label label = new Label(Integer.toString(i));
            if (i % 2 == 0) {
                GridLayoutData layoutData = new GridLayoutData();
                layoutData.setColumnSpan(3);
                label.setLayoutData(layoutData);
            }
            grid.add(label);
        }

        GridProcessor gridProcessor = new GridProcessor(grid);
        
        // Verify Grid size is correct.
        assertEquals(2, gridProcessor.getGridXSize());
        assertEquals(3, gridProcessor.getGridYSize());

        // Verify components are at correct positions.
        assertEquals(0, gridProcessor.getComponentIndex(0, 0));
        assertEquals(1, gridProcessor.getComponentIndex(1, 0));
        assertEquals(2, gridProcessor.getComponentIndex(0, 1));
        assertEquals(3, gridProcessor.getComponentIndex(1, 1));
        assertEquals(4, gridProcessor.getComponentIndex(0, 2));
        assertEquals(5, gridProcessor.getComponentIndex(1, 2));

        // Verify x-spans were properly reduced.
        assertEquals(1, gridProcessor.getXSpan(0, 0));
        assertEquals(1, gridProcessor.getXSpan(1, 0));
        assertEquals(1, gridProcessor.getXSpan(0, 1));
        assertEquals(1, gridProcessor.getXSpan(1, 1));
        assertEquals(1, gridProcessor.getXSpan(0, 2));
        assertEquals(1, gridProcessor.getXSpan(1, 2));

        // Verify y-spans were untouched.
        assertEquals(1, gridProcessor.getYSpan(0, 0));
        assertEquals(1, gridProcessor.getYSpan(1, 0));
        assertEquals(1, gridProcessor.getYSpan(0, 1));
        assertEquals(1, gridProcessor.getYSpan(1, 1));
        assertEquals(1, gridProcessor.getYSpan(0, 2));
        assertEquals(1, gridProcessor.getYSpan(1, 2));
    }
    
    /**
     * Test reduction of symmetric row spans.
     * 
     *  __ __ __  (spec)  (rend)  
     * |  |  |  |     
     * |  |  |  |   0      0       
     * |  |  |  |     
     * |  |  |  |   1    
     * |  |  |  | 
     * |__|__|__|   2
     * |  |  |  | 
     * |__|__|__|   3      1
     */
    public void testReduceRowLong() {
        Grid grid = new Grid();
        grid.setSize(3);
        
        for (int i = 0; i < 6; ++i) {
            Label label = new Label(Integer.toString(i));
            if (i < 3) {
                GridLayoutData layoutData = new GridLayoutData();
                layoutData.setRowSpan(3);
                label.setLayoutData(layoutData);
            }
            grid.add(label);
        }
        
        GridProcessor gridProcessor = new GridProcessor(grid);
        
        // Verify Grid size is correct.
        assertEquals(3, gridProcessor.getGridXSize());
        assertEquals(2, gridProcessor.getGridYSize());
        
        // Verify components are at correct positions.
        assertEquals(0, gridProcessor.getComponentIndex(0, 0));
        assertEquals(1, gridProcessor.getComponentIndex(1, 0));
        assertEquals(2, gridProcessor.getComponentIndex(2, 0));
        assertEquals(3, gridProcessor.getComponentIndex(0, 1));
        assertEquals(4, gridProcessor.getComponentIndex(1, 1));
        assertEquals(5, gridProcessor.getComponentIndex(2, 1));

        // Verify x-spans were untouched.
        assertEquals(1, gridProcessor.getXSpan(0, 0));
        assertEquals(1, gridProcessor.getXSpan(1, 0));
        assertEquals(1, gridProcessor.getXSpan(2, 0));
        assertEquals(1, gridProcessor.getXSpan(0, 1));
        assertEquals(1, gridProcessor.getXSpan(1, 1));
        assertEquals(1, gridProcessor.getXSpan(2, 1));

        // Verify y-spans were properly reduced.
        assertEquals(1, gridProcessor.getYSpan(0, 0));
        assertEquals(1, gridProcessor.getYSpan(1, 0));
        assertEquals(1, gridProcessor.getYSpan(2, 0));
        assertEquals(1, gridProcessor.getYSpan(0, 1));
        assertEquals(1, gridProcessor.getYSpan(1, 1));
        assertEquals(1, gridProcessor.getYSpan(2, 1));
    }

    /**
     *  Test Grid that consists a single row-spanned and column-spanned cell.
     *  
     *  0  1  2
     *  __ __ __
     * |0       |  0
     * |        |
     * |        |  1
     * |        |
     * |        |  2
     * |        |
     * |        |  3
     * |        |
     * |        |  4
     * |________|
     */
    public void testReduceColumnAndRowOneBigCell() {
        Grid grid = new Grid();
        grid.setSize(3);
        Label label = new Label("0");
        GridLayoutData layoutData = new GridLayoutData();
        layoutData.setColumnSpan(3);
        layoutData.setRowSpan(5);
        label.setLayoutData(layoutData);
        grid.add(label);

        GridProcessor gridProcessor = new GridProcessor(grid);
        
        // Verify Grid size is correct.
        assertEquals(1, gridProcessor.getGridXSize());
        assertEquals(1, gridProcessor.getGridYSize());
        
        // Verify components are at correct positions.
        assertEquals(0, gridProcessor.getComponentIndex(0, 0));

        // Verify x-spans and y-spans were properly reduced.
        assertEquals(1, gridProcessor.getXSpan(0, 0));
        assertEquals(1, gridProcessor.getYSpan(0, 0));
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
    public void testRowSpanSimple() {
        Grid grid = new Grid();
        for (int i = 0; i < 7; ++i) {
            Label label = new Label(Integer.toString(i));
            if (i == 2) {
                GridLayoutData layoutData = new GridLayoutData();
                layoutData.setRowSpan(2);
                label.setLayoutData(layoutData);
            }
            grid.add(label);
        }
        
        GridProcessor gridProcessor = new GridProcessor(grid);
        
        // Verify Grid size is correct.
        assertEquals(2, gridProcessor.getGridXSize());
        assertEquals(4, gridProcessor.getGridYSize());
        
        // Verify components are at correct positions.
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
     * Test a simple grid consisting of ten cells in default 2 columns and 5 rows.
     *  __ __
     * |0 |1 |
     * |__|__|
     * |2 |3 |
     * |__|__|
     * |4 |5 |
     * |__|__|
     * |6 |7 |
     * |__|__|
     * |8 |9 |
     * |__|__|
     */
    public void testSimple() {
        Grid grid = new Grid();
        for (int i = 0; i < 10; ++i) {
            grid.add(new Label("test"));
        }

        GridProcessor gridProcessor = new GridProcessor(grid);
        
        // Verify Grid size is correct.
        assertEquals(2, gridProcessor.getGridXSize());
        assertEquals(5, gridProcessor.getGridYSize());
    }
    
    /**
     * Test a a simple Grid consisting of ten cells with one of them 
     * spanning two columns. 
     *  __ __
     * |0 |1 |
     * |__|__|
     * |2 |3 |
     * |__|__|
     * |4    |
     * |_____|
     * |5 |6 |
     * |__|__|
     * |7 |8 |
     * |__|__|
     * |9 |XX|
     * |__|XX|
     */
    public void testSimpleWithColumnSpan() {
        Grid grid = new Grid();
        for (int i = 0; i < 10; ++i) {
            Label label = new Label();
            if (i == 4) {
                GridLayoutData layoutData = new GridLayoutData();
                layoutData.setColumnSpan(2);
                label.setLayoutData(layoutData);
            }
            grid.add(label);
        }
        
        GridProcessor gridProcessor = new GridProcessor(grid);
        
        // Verify Grid size is correct.
        assertEquals(2, gridProcessor.getGridXSize());
        assertEquals(6, gridProcessor.getGridYSize());
        
        // Verify components are at correct positions.
        assertEquals(0, gridProcessor.getComponentIndex(0, 0));
        assertEquals(1, gridProcessor.getComponentIndex(1, 0));
        assertEquals(2, gridProcessor.getComponentIndex(0, 1));
        assertEquals(3, gridProcessor.getComponentIndex(1, 1));
        assertEquals(4, gridProcessor.getComponentIndex(0, 2));
        assertEquals(4, gridProcessor.getComponentIndex(1, 2));
        assertEquals(5, gridProcessor.getComponentIndex(0, 3));
        assertEquals(6, gridProcessor.getComponentIndex(1, 3));
        assertEquals(7, gridProcessor.getComponentIndex(0, 4));
        assertEquals(8, gridProcessor.getComponentIndex(1, 4));
        assertEquals(9, gridProcessor.getComponentIndex(0, 5));
        assertEquals(-1, gridProcessor.getComponentIndex(1, 5));
        
        // Verify x-spans were untouched.
        assertEquals(1, gridProcessor.getXSpan(0, 0));
        assertEquals(1, gridProcessor.getXSpan(1, 0));
        assertEquals(1, gridProcessor.getXSpan(0, 1));
        assertEquals(1, gridProcessor.getXSpan(1, 1));
        assertEquals(2, gridProcessor.getXSpan(0, 2));
        assertEquals(2, gridProcessor.getXSpan(1, 2));
        assertEquals(1, gridProcessor.getXSpan(0, 3));
        assertEquals(1, gridProcessor.getXSpan(1, 3));
        assertEquals(1, gridProcessor.getXSpan(0, 4));
        assertEquals(1, gridProcessor.getXSpan(1, 4));
        assertEquals(1, gridProcessor.getXSpan(0, 5));
        assertEquals(-1, gridProcessor.getXSpan(1, 5));

        // Verify y-spans were properly reduced.
        assertEquals(1, gridProcessor.getYSpan(0, 0));
        assertEquals(1, gridProcessor.getYSpan(1, 0));
        assertEquals(1, gridProcessor.getYSpan(0, 1));
        assertEquals(1, gridProcessor.getYSpan(1, 1));
        assertEquals(1, gridProcessor.getYSpan(0, 2));
        assertEquals(1, gridProcessor.getYSpan(1, 2));
        assertEquals(1, gridProcessor.getYSpan(0, 3));
        assertEquals(1, gridProcessor.getYSpan(1, 3));
        assertEquals(1, gridProcessor.getYSpan(0, 4));
        assertEquals(1, gridProcessor.getYSpan(1, 4));
        assertEquals(1, gridProcessor.getYSpan(0, 5));
        assertEquals(-1, gridProcessor.getYSpan(1, 5));
    }
    
    /**
     * Test a a simple Grid consisting of ten cells with one of them 
     * spanning three columns (even though only two are available). 
     *  __ __
     * |0 |1 |
     * |__|__|
     * |2 |3 |
     * |__|__|__
     * |4       |
     * |________|
     * |5 |6 |
     * |__|__|
     * |7 |8 |
     * |__|__|
     * |9 |XX|
     * |__|XX|
     */
    public void testSimpleWithOversidedColumnSpan() {
        Grid grid = new Grid();
        for (int i = 0; i < 10; ++i) {
            Label label = new Label();
            if (i == 4) {
                GridLayoutData layoutData = new GridLayoutData();
                layoutData.setColumnSpan(3);
                label.setLayoutData(layoutData);
            }
            grid.add(label);
        }
        
        GridProcessor gridProcessor = new GridProcessor(grid);
        
        // Verify Grid size is correct.
        assertEquals(2, gridProcessor.getGridXSize());
        assertEquals(6, gridProcessor.getGridYSize());
        
        // Verify components are at correct positions.
        assertEquals(0, gridProcessor.getComponentIndex(0, 0));
        assertEquals(1, gridProcessor.getComponentIndex(1, 0));
        assertEquals(2, gridProcessor.getComponentIndex(0, 1));
        assertEquals(3, gridProcessor.getComponentIndex(1, 1));
        assertEquals(4, gridProcessor.getComponentIndex(0, 2));
        assertEquals(4, gridProcessor.getComponentIndex(1, 2));
        assertEquals(5, gridProcessor.getComponentIndex(0, 3));
        assertEquals(6, gridProcessor.getComponentIndex(1, 3));
        assertEquals(7, gridProcessor.getComponentIndex(0, 4));
        assertEquals(8, gridProcessor.getComponentIndex(1, 4));
        assertEquals(9, gridProcessor.getComponentIndex(0, 5));
        assertEquals(-1, gridProcessor.getComponentIndex(1, 5));
        
        // Verify x-spans were untouched for the most part,
        // except for oversized cell which is reduced.
        assertEquals(1, gridProcessor.getXSpan(0, 0));
        assertEquals(1, gridProcessor.getXSpan(1, 0));
        assertEquals(1, gridProcessor.getXSpan(0, 1));
        assertEquals(1, gridProcessor.getXSpan(1, 1));
        assertEquals(2, gridProcessor.getXSpan(0, 2));
        assertEquals(2, gridProcessor.getXSpan(1, 2));
        assertEquals(1, gridProcessor.getXSpan(0, 3));
        assertEquals(1, gridProcessor.getXSpan(1, 3));
        assertEquals(1, gridProcessor.getXSpan(0, 4));
        assertEquals(1, gridProcessor.getXSpan(1, 4));
        assertEquals(1, gridProcessor.getXSpan(0, 5));
        assertEquals(-1, gridProcessor.getXSpan(1, 5));

        // Verify y-spans were properly reduced.
        assertEquals(1, gridProcessor.getYSpan(0, 0));
        assertEquals(1, gridProcessor.getYSpan(1, 0));
        assertEquals(1, gridProcessor.getYSpan(0, 1));
        assertEquals(1, gridProcessor.getYSpan(1, 1));
        assertEquals(1, gridProcessor.getYSpan(0, 2));
        assertEquals(1, gridProcessor.getYSpan(1, 2));
        assertEquals(1, gridProcessor.getYSpan(0, 3));
        assertEquals(1, gridProcessor.getYSpan(1, 3));
        assertEquals(1, gridProcessor.getYSpan(0, 4));
        assertEquals(1, gridProcessor.getYSpan(1, 4));
        assertEquals(1, gridProcessor.getYSpan(0, 5));
        assertEquals(-1, gridProcessor.getYSpan(1, 5));
    }
    
    /**
     * Test staggered row-spanned cells:
     *  __ __
     * |0 |1 |
     * |  |__|
     * |  |2 |
     * |__|  |
     * |3 |  |
     * |__|__|
     */
    public void testStaggeredSpans() {
        Grid grid = new Grid();
        for (int i = 0; i < 4; ++i) {
            Label label = new Label();
            if (i % 2 == 0) {
                GridLayoutData gridLayoutData = new GridLayoutData();
                gridLayoutData.setRowSpan(2);
                label.setLayoutData(gridLayoutData);
            }
            grid.add(label);
        }
        
        GridProcessor gridProcessor = new GridProcessor(grid);
        
        // Verify Grid size is correct.
        assertEquals(2, gridProcessor.getGridXSize());
        assertEquals(3, gridProcessor.getGridYSize());
        
        // Verify components are at correct positions.
        assertEquals(0, gridProcessor.getComponentIndex(0, 0));
        assertEquals(1, gridProcessor.getComponentIndex(1, 0));
        assertEquals(0, gridProcessor.getComponentIndex(0, 1));
        assertEquals(2, gridProcessor.getComponentIndex(1, 1));
        assertEquals(3, gridProcessor.getComponentIndex(0, 2));
        assertEquals(2, gridProcessor.getComponentIndex(1, 2));
        
        // Verify y-spans were untouched.
        assertEquals(1, gridProcessor.getXSpan(0, 0));
        assertEquals(1, gridProcessor.getXSpan(1, 0));
        assertEquals(1, gridProcessor.getXSpan(0, 1));
        assertEquals(1, gridProcessor.getXSpan(1, 1));
        assertEquals(1, gridProcessor.getXSpan(0, 2));
        assertEquals(1, gridProcessor.getXSpan(1, 2));
        
        // Verify y-spans were untouched.
        assertEquals(2, gridProcessor.getYSpan(0, 0));
        assertEquals(1, gridProcessor.getYSpan(1, 0));
        assertEquals(2, gridProcessor.getYSpan(0, 1));
        assertEquals(2, gridProcessor.getYSpan(1, 1));
        assertEquals(1, gridProcessor.getYSpan(0, 2));
        assertEquals(2, gridProcessor.getYSpan(1, 2));
    }
}
