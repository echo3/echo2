/*
 * This file is part of the Echo Web Application Framework (hereinafter "Echo").
 * Copyright (C) 2002-2005 NextApp, Inc.
 * 
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or the
 * GNU Lesser General Public License Version 2.1 or later (the "LGPL"), in which
 * case the provisions of the GPL or the LGPL are applicable instead of those
 * above. If you wish to allow use of your version of this file only under the
 * terms of either the GPL or the LGPL, and not to allow others to use your
 * version of this file under the terms of the MPL, indicate your decision by
 * deleting the provisions above and replace them with the notice and other
 * provisions required by the GPL or the LGPL. If you do not delete the
 * provisions above, a recipient may use your version of this file under the
 * terms of any one of the MPL, the GPL or the LGPL.
 */

package nextapp.echo2.webcontainer.syncpeer;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.LayoutData;
import nextapp.echo2.app.layout.GridLayoutData;

/**
 * Provides analysis of a <code>Grid</code> for rendering purposes.
 * <p>
 * This object defines the <code>Grid</code> in terms of two axes, "x" and
 * "y". The axes are transposed based on whether the <code>origin</code>
 * property of the <code>Grid</code> is horizontal or vertical. For
 * horizontally-oriented <code>Grid</code>s, the x-axis represents columns
 * and the y-axis represents rows. For vertically oriented <code>Grid</code>s,
 * the x-axis represents rows and the y-axis represents columns.
 * <p>
 * Once a <code>GridProcessor</code> has been instantiated, the rendering
 * <code>GridPeer</code> can make inquiries to it to determine how the
 * HTML table representing the Grid should be rendered.  
 * <p>
 * Upon instantiation, the dimensions of the grid are calculated, and the
 * content of each cell within those dimensions is determined.  By specifying
 * an "x" and "y" coordinate to various getXXX() methods, the renderer can
 * determine what <code>Component</code> exists at a particular coordinate,
 * how many rows and columns that <code>Component</code> spans, and the index
 * of the <code>Component</code> within its parent <code>Grid</code>'s 
 * children.
 * <p>
 * This class should not be extended or used by classes outside of the Echo
 * framework.
 */
public class GridProcessor {
    
    /**
     * Internal representation of a rendered cell at a specific coordinate.
     */
    private class Cell {
        
        /**
         * Creates a new <code>Cell</code>.
         * 
         * @param component the represented <code>Component</code>
         * @param index the index of <code>component</code> within the
         *        parent <code>Grid</code>
         * @param xSpan the current calculated x-span of the cell
         * @param ySpan the current calculated y-span of the cell
         */
        private Cell(Component component, int index, int xSpan, int ySpan) {
            super();
            this.component = component;
            this.index = index;
            this.xSpan = xSpan;
            this.ySpan = ySpan;
        }
        
        /** the current calculated x-span of the cell */
        private int xSpan;

        /** the current calculated y-span of the cell */
        private int ySpan;

        /** the represented <code>Component</code> */
        private Component component;
        
        /** 
         * the index of <code>component</code> within the parent 
         * <code>Grid</code>
         */
        private int index;
    }
    
    /**
     * A <code>List</code> containing arrays of <code>Cell</code>s.
     * Each contained array of <code>Cell</code>s represents a single point on
     * the y-axis.  The cell indices represent points on the x-axis.
     * Individual y-axis entries should be obtained via the 
     * <code>getCellArray()</code> method.
     * 
     * @see #getCellArray(int, boolean)
     */
    private List cellArrays;

    /** The current calculated  size of the x-axis. */
    private int gridXSize;
    
    /** The current calculated  size of the y-axis. */
    private int gridYSize;

    /**
     * Creates a new <code>GridProcessor</code> for the specified
     * <code>Grid</code>. Creating a new <code>GridProcessor</code> will
     * cause immediately analyze the <code>Grid</code> which will immediately
     * consume processor and memory resources. Such operations should be done at
     * most once per rendering.
     * 
     * @param grid the <code>Grid</code>
     */
    public GridProcessor(Grid grid) {
        super();
        cellArrays = new ArrayList();
        
        Component[] children = grid.getVisibleComponents();
        
        if (children.length == 0) {
            // Special case: empty Grid.
            gridXSize = 0;
            gridYSize = 0;
            return;
        }
        
        Cell[] cells = new Cell[children.length];

        gridXSize = grid.getSize();

        Integer orientationValue = (Integer) grid.getRenderProperty(Grid.PROPERTY_ORIENTATION);
        int orientation = orientationValue == null ? Grid.ORIENTATION_HORIZONTAL : orientationValue.intValue();
        boolean horizontalOrientation = orientation != Grid.ORIENTATION_VERTICAL;
        
        for (int i = 0; i < children.length; ++i) {
            LayoutData layoutData = (LayoutData) children[i].getRenderProperty(Grid.PROPERTY_LAYOUT_DATA);
            if (layoutData instanceof GridLayoutData) {
                GridLayoutData gcLayoutData = (GridLayoutData) layoutData;
                int xSpan = horizontalOrientation ? gcLayoutData.getColumnSpan() : gcLayoutData.getRowSpan();
                int ySpan = horizontalOrientation ? gcLayoutData.getRowSpan() : gcLayoutData.getColumnSpan();
                cells[i] = new Cell(children[i], i, xSpan, ySpan);
            } else {
                cells[i] = new Cell(children[i], i, 1, 1);
            }
        }
        
        int x = 0, y = 0;
        Cell[] yCells = getCellArray(y, true);
        for (int componentIndex = 0; componentIndex < children.length; ++componentIndex) {
            
            if (cells[componentIndex].xSpan > gridXSize - x) {
                // Limit xSpan in case cell has a span greater than remaining
                // space.
                cells[componentIndex].xSpan = gridXSize - x;
            }
            if (cells[componentIndex].xSpan != 1 || cells[componentIndex].ySpan != 1) {
                // Scan to ensure no y-spans are blocking this x-span.
                // If a y-span is blocking, shorten the x-span to not
                // interfere.
                for (int xIndex = 1; xIndex < cells[componentIndex].xSpan; ++xIndex) {
                    if (yCells[x + xIndex] != null) {
                        // Blocking component found.
                        cells[componentIndex].xSpan = xIndex;
                        break;
                    }
                }
                for (int yIndex = 0; yIndex < cells[componentIndex].ySpan; ++yIndex) {
                    Cell[] yIndexCells = getCellArray(y + yIndex, true);
                    for (int xIndex = 0; xIndex < cells[componentIndex].xSpan; ++xIndex) {
                        yIndexCells[x + xIndex] = cells[componentIndex];
                    }
                }
            }
            yCells[x] = cells[componentIndex];

            if (componentIndex < children.length - 1) {
                // Move rendering cursor.
                boolean nextRenderPointFound = false;
                while (!nextRenderPointFound) {
                    if (x < gridXSize - 1) {
                        ++x;
                    } else {
                        // Move cursor to next line.
                        x = 0;
                        ++y;
                        yCells = getCellArray(y, true);
                        
                    }
                    nextRenderPointFound = yCells[x] == null;
                }
            }
        }

        // Recalculate actual 'y' dimension.
        gridYSize = cellArrays.size();
        
        reduceY();
        reduceX();
        trimX();
    }
    
    /**
     * Retrieves the array of cells representing a particular coordinate on
     * the y-axis of the rendered representation.
     * 
     * @param y the y-axis index
     * @param expand a flag indicating whether the <code>CellArray</code> 
     *        should be expanded in the event the given y-axis does not
     *        exist.
     * @see #cellArrays
     */
    private Cell[] getCellArray(int y, boolean expand) {
        while (expand && y >= cellArrays.size()) {
            cellArrays.add(new Cell[gridXSize]);
        }
        return (Cell[]) cellArrays.get(y);
    }
    
    /**
     * Returns the child <code>Component</code> at the specified
     * rendered coordinate.
     * @param x the "x" coordinate
     * @param y the "y" coordinate
     * 
     * @return the <code>Component</code>
     */
    public Component getContent(int x, int y) {
        Cell cell = getCellArray(y, false)[x];
        return cell == null ? null : cell.component;
    }

    /**
     * Returns the component index within the <code>Grid</code> 
     * of the component at the specified rendered coordinate.
     * 
     * @param x the "x" coordinate
     * @param y the "y" coordinate
     * @return the index of the component within the parent <code>Grid</code>
     *         component, or -1 if no component exists at the specified index.
     */
    public int getComponentIndex(int x, int y) {
        Cell cell = getCellArray(y, false)[x];
        return cell == null ? -1 : cell.index;
    }

    /**
     * Returns the x-axis dimension of the <code>Grid</code>
     * 
     * @return the x-axis dimension
     */
    public int getGridXSize() {
        return gridXSize;
    }

    /**
     * Returns the y-axis dimension of the <code>Grid</code>
     * 
     * @return the y-axis dimension
     */
    public int getGridYSize() {
        return gridYSize;
    }

    /**
     * Retrieves the x-span of the cell at the specified rendered coordinate.
     * 
     * @param x the "x" coordinate
     * @param y the "y" coordinate
     * @return the cell x-span, or -1 in the event that no cell exists at the 
     *         coordinate.
     */
    public int getXSpan(int x, int y) {
        Cell cell = getCellArray(y, false)[x];
        return cell == null ? -1 : cell.xSpan;
    }

    /**
     * Retrieves the y-span of the cell at the specified rendered coordinate.
     * 
     * @param x the "x" coordinate
     * @param y the "y" coordinate
     * @return the cell y-span, or -1 in the event that no cell exists at the 
     *         coordinate.
     */
    public int getYSpan(int x, int y) {
        Cell cell = getCellArray(y, false)[x];
        return cell == null ? -1 : cell.ySpan;
    }
    
    /**
     * Remove duplicates from the x-axis where all cells simply
     * "span over" a given x-axis coordinate. 
     */
    private void reduceX() {
        // Determine duplicate cell sets on x-axis.
        BitSet xRemoves = new BitSet();
        int x = 1;
        int length = getCellArray(0, false).length;
        while (x  < length) {
            int y = 0;
            boolean identical = true;
            while (y < cellArrays.size()) {
                if (getCellArray(y, false)[x] != getCellArray(y, false)[x - 1]) {
                    identical = false;
                    break;
                }
                ++y;
            }
            if (identical) {
                xRemoves.set(x, true);
            }
            ++x;
        }

        // If no reductions are necessary on the x-axis, do nothing.
        if (xRemoves.nextSetBit(0) == -1) {
            return;
        }
        
        for (int removedX = gridXSize -1; removedX >= 0; --removedX) {
            if (!xRemoves.get(removedX)) {
                continue;
            }

            for (int y = 0; y < gridYSize; ++y) {
                if (y == 0 || getCellArray(y, false)[removedX - 1] != getCellArray(y - 1, false)[removedX - 1]) {
                    // Reduce x-span, taking care not to reduce it multiple times if cell has a y-span.
                    Cell[] cellArray = getCellArray(y, false);
                    if (cellArray[removedX - 1] != null) {
                        --getCellArray(y, false)[removedX - 1].xSpan;
                    }
                }
                for (x = removedX; x < gridXSize - 1; ++x) {
                    getCellArray(y, false)[x] = getCellArray(y, false)[x + 1];
                }
            }
            --gridXSize;
        }
    }
    
    /**
     * Remove duplicates from the y-axis where all cells simply
     * "span over" a given y-axis coordinate. 
     */
    private void reduceY() {
        // Determine duplicate cell sets on y-axis.
        BitSet yRemoves = new BitSet();
        int y = 1;
        
        int size = cellArrays.size();
        Cell[] previousCellArray;
        Cell[] currentCellArray = getCellArray(0, false);
        
        while (y < size) {
            previousCellArray = currentCellArray;
            currentCellArray = getCellArray(y, false);
            
            int x = 0;
            boolean identical = true;

            while (x < currentCellArray.length) {
                if (currentCellArray[x] != previousCellArray[x]) {
                    identical = false;
                    break;
                }
                ++x;
            }
            if (identical) {
                yRemoves.set(y, true);
            }
            
            ++y;
        }
        
        // If no reductions are necessary on the y-axis, do nothing.
        if (yRemoves.nextSetBit(0) == -1) {
            return;
        }
            
        for (int removedY = gridYSize -1; removedY >= 0; --removedY) {
            if (!yRemoves.get(removedY)) {
                continue;
            }
            
            // Shorten the y-spans of the cell array that will be retained to 
            // reflect the fact that a cell array is being removed. 
            Cell[] retainedCellArray = getCellArray(removedY - 1, false);
            for (int x = 0; x < gridXSize; ++x) {
                if (x == 0 || retainedCellArray[x] != retainedCellArray[x - 1]) {
                    // Reduce y-span, taking care not to reduce it multiple times if cell has a x-span.
                    if (retainedCellArray[x] != null) {
                        --retainedCellArray[x].ySpan;
                    }
                }
            }
            
            // Remove the duplicate cell array.
            cellArrays.remove(removedY);
            
            // Decrement the grid size to reflect cell array removal.
            --gridYSize;
        }
    }
    
    /**
     * Special case: Trim excess null cells from Grid x-size if the 
     * Grid y-size is 1.
     */
    private void trimX() {
        if (gridYSize == 1) {
            Cell[] cellArray = getCellArray(0, false);
            for (int i = 0; i < gridXSize; ++i) {
                if (cellArray[i] == null) {
                    gridXSize = i;
                }
            }
        }
    }
}
