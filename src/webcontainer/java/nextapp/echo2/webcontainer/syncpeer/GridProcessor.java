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
 * This class should not be extended or used by classes outside of the Echo
 * framework.
 */
public class GridProcessor {
    
    private Component[][] componentMatrix;

    private int[][] indexMatrix;

    private int gridXSize;

    private int gridYSize;

    /**
     * Array containing the x-axis span of each child component in the
     * <code>Grid</code>. The indices of this array correspond to indices of
     * the <strong>visible</strong> child <code>Component</code>s within the
     * parent <code>Grid</code>.
     */
    private int[] xSpans;

    /**
     * Array containing the y-axis span of each child component in the
     * <code>Grid</code>. The indices of this array correspond to indices of
     * the <strong>visible</strong> child <code>Component</code>s within the
     * parent <code>Grid</code>.
     */
    private int[] ySpans;

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
        Component[] children = grid.getVisibleComponents();

        int totalArea = 0;
        
        // Store x-axis and y-axis spans of all child Components.
        boolean horizontalOrientation = grid.getOrientation() != Grid.ORIENTATION_VERTICAL;
        xSpans = new int[children.length];
        ySpans = new int[children.length];
        for (int i = 0; i < children.length; ++i) {
            LayoutData layoutData = (LayoutData) children[i].getRenderProperty(Grid.PROPERTY_LAYOUT_DATA);
            if (layoutData instanceof GridLayoutData) {
                GridLayoutData gcLayoutData = (GridLayoutData) layoutData;
                xSpans[i] = horizontalOrientation ? gcLayoutData.getColumnSpan() : gcLayoutData.getRowSpan();
                ySpans[i] = horizontalOrientation ? gcLayoutData.getRowSpan() : gcLayoutData.getColumnSpan();
                totalArea += xSpans[i] * ySpans[i];
            } else {
                xSpans[i] = 1;
                ySpans[i] = 1;
                totalArea += 1;
            }
        }

        gridXSize = grid.getSize();
        gridYSize = (totalArea / gridXSize) + ((totalArea % gridXSize == 0) ? 0 : 1); // Theoretical maximum
        
        componentMatrix = new Component[gridYSize][gridXSize];
        indexMatrix = new int[gridYSize][gridXSize];

        int x = 0, y = 0;
        for (int i = 0; i < children.length; ++i) {
            if (xSpans[i] > gridXSize - x) {
                // Limit xSize in case cell has a span greater than remaining
                // space.
                int oldSize = xSpans[i];
                int newSize = gridXSize - x;
                totalArea = totalArea - oldSize + newSize;
                xSpans[i] = newSize;
            }
            if (xSpans[i] != 1 || ySpans[i] != 1) {
                // Scan to ensure no y-spans are blocking this x-span.
                // If a y-span is blocking, shorten the x-span to not
                // interfere.
                for (int xIndex = 1; xIndex < xSpans[i]; ++xIndex) {
                    if (componentMatrix[y][x + xIndex] != null) {
                        // Blocking component found.
                        int oldSize = xSpans[i];
                        int newSize = xIndex;
                        totalArea = totalArea - oldSize + newSize;
                        xSpans[i] = newSize;
                        break;
                    }
                }

                // BUGBUG! This is a temporary hack to prevent
                // index-out-of-bounds on overzealous row spans
                // This hack still results in mis-rendered tables under such
                // scenarios!
                // (note....if we have to take a "row-reduction" pass....this
                // method might be
                // just fine).
                if (y + ySpans[i] > componentMatrix.length) {
                    ySpans[i] = componentMatrix.length - y;
                }

                for (int yIndex = 0; yIndex < ySpans[i]; ++yIndex) {
                    for (int xIndex = 0; xIndex < xSpans[i]; ++xIndex) {
                        componentMatrix[y + yIndex][x + xIndex] = children[i];
                    }
                }
            }
            componentMatrix[y][x] = children[i];
            indexMatrix[y][x] = i;

            if (i < children.length - 1) {
                // Move rendering cursor.
                boolean nextRenderPointFound = false;
                while (!nextRenderPointFound) {
                    if (x < gridXSize - 1) {
                        ++x;
                    } else {
                        x = 0;
                        ++y;
                    }
                    nextRenderPointFound = componentMatrix[y][x] == null;
                }
            }
        }

        // Recalculate actual 'y' dimension.
        gridYSize = (totalArea / gridXSize) + ((totalArea % gridXSize == 0) ? 0 : 1);
    }

    public Component getContent(int x, int y) {
        return componentMatrix[y][x];
    }

    public int getComponentIndex(int x, int y) {
        return indexMatrix[y][x];
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

    public int getXSpan(int componentIndex) {
        return xSpans[componentIndex];
    }

    public int getYSpan(int componentIndex) {
        return ySpans[componentIndex];
    }
}
