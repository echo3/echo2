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

package nextapp.echo2.webcontainer.syncpeer;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.LayoutData;
import nextapp.echo2.app.layout.GridCellLayoutData;

/**
 * Provides analysis of a Grid for rendering purposes.  
 * <p>
 * This class should not be extended or used by classes outside of the
 * Echo framework.
 */
public class GridProcessor {
    
    private Component[][] componentMatrix;
    private int[][] indexMatrix;
    private int gridXSize, gridYSize;
    private int[] xSpans, ySpans;
    
    public GridProcessor(Grid grid) {
        int totalArea = 0;
        gridXSize = grid.getSize();
        Component[] children = grid.getVisibleComponents();
        boolean horizontalOrientation = grid.getOrientation() != Grid.ORIENTATION_VERTICAL;

        xSpans = new int[children.length];
        ySpans = new int[children.length];
        
        for (int i = 0; i < children.length; ++i) {
            LayoutData layoutData = children[i].getLayoutData();
            if (layoutData instanceof GridCellLayoutData) {
                GridCellLayoutData gcLayoutData = (GridCellLayoutData) layoutData;
                xSpans[i] = horizontalOrientation ? gcLayoutData.getColumnSpan() : gcLayoutData.getRowSpan();
                ySpans[i] = horizontalOrientation ? gcLayoutData.getRowSpan() : gcLayoutData.getColumnSpan();
                totalArea += xSpans[i] * ySpans[i];
            } else {
                xSpans[i] = 1;
                ySpans[i] = 1;
                totalArea += 1;
            }
        }
        gridYSize = (totalArea / gridXSize) + ((totalArea % gridXSize == 0) ? 0 : 1);
        componentMatrix = new Component[gridYSize][gridXSize];
        indexMatrix = new int[gridYSize][gridXSize];
        
        int x = 0, y = 0; 
        for (int i = 0; i < children.length; ++i) {
            if (xSpans[i] > gridXSize - x) {
                // Limit xSize in case cell has a span greater than remaining space.
                xSpans[i] = gridXSize - x;
            }
            if (xSpans[i] != 1 || ySpans[i] != 1) {
                // Scan to ensure no rowspans are blocking this columnspan.
                // If a rowspan is blocking, shorten the columnspan to not interfere.
                for (int xIndex = 1; xIndex < xSpans[i]; ++xIndex) {
                    if (componentMatrix[y][x + xIndex] != null) {
                        xSpans[i] = xIndex;
                    }
                }
                
                //BUGBUG! This is a temporary hack to prevent indexoutofbounds on overzealous rowspans
                //    This hack still results in mis-rendered tables under such scenarios!
                //    (note....if we have to take a "row-reduction" pass....this method might be
                //    just fine).
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
    }
    
    public Component getContent(int x, int y) {
        return componentMatrix[y][x];
    }
    
    public int getComponentIndex(int x, int y) {
        return indexMatrix[y][x];
    }
    
    public int getGridXSize() {
        return gridXSize;
    }
    
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
