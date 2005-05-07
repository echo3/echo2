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

package nextapp.echo2.app;

/**
 * A layout component which displays its contents in a grid.  Each component
 * is contained within a "cell" of the grid.  <code>GridLayoutData</code>
 * layout data objects may used to cause cells to expand to fill multiple
 * columns or rows.
 */
public class Grid extends Component {

    public static final int ORIENTATION_HORIZONTAL = 0;
    public static final int ORIENTATION_VERTICAL = 1;
    
    public static final int DEFAULT_SIZE = 2;
    
    public static final String PROPERTY_BORDER = "border";
    public static final String PROPERTY_COLUMN_WIDTH = "columnWidth";
    public static final String PROPERTY_HEIGHT = "height";
    public static final String PROPERTY_INSETS = "insets";
    public static final String PROPERTY_ORIGIN = "origin";
    public static final String PROPERTY_ORIENTATION = "orientation";
    public static final String PROPERTY_ROW_HEIGHT = "rowHeight";
    public static final String PROPERTY_SIZE = "size";
    public static final String PROPERTY_WIDTH = "width";

    /**
     * Creates a new horizontally-oriented <code>Grid</code> with the
     * default size (2).
     */
    public Grid() {
        this(2);
    }
    
    /**
     * Creates a new hoirzontally-oriented <code>Grid</code> with the 
     * specified size.
     * 
     * @param size the number of columns
     * @see #getSize()
     */
    public Grid(int size) {
        super();
        setSize(size);
    }

    /**
     * Returns the <code>Border</code>.
     * 
     * @return the border
     */
    public Border getBorder() {
        return (Border) getProperty(PROPERTY_BORDER);
    }

    /**
     * Returns the width of the specified column.
     * 
     * @param columnIndex the column index
     * @return the width
     */
    public Extent getColumnWidth(int columnIndex) {
        return (Extent) getIndexedProperty(PROPERTY_COLUMN_WIDTH, columnIndex);
    }
    
    /**
     * Returns the overall height.
     * 
     * @return the height
     */
    public Extent getHeight() {
        return (Extent) getProperty(PROPERTY_HEIGHT);
    }
    
    /**
     * Returns the default cell insets.
     * 
     * @return the default cell insets
     */
    public Insets getInsets() {
        return (Insets) getProperty(PROPERTY_INSETS);
    }
    
    /**
     * Returns the orientation of the grid (either horizontal or vertical).
     * 
     * @return the orientation, one of the following values:
     *         <ul>
     *         <li><code>ORIENTATION_HORIZONTAL</code> (the default)</li>
     *         <li><code>ORIENTATION_VERTICAL</code></li>
     *         </ul>
     * @see #setOrientation
     */
    public int getOrientation() {
        Integer orientationValue = (Integer) getProperty(PROPERTY_ORIENTATION);
        return orientationValue == null ? ORIENTATION_HORIZONTAL : orientationValue.intValue();
    }
    
    /**
     * Returns an <code>Alignment</code> representing the origin of the grid.
     * 
     * @return the origin value
     * @see #setOrigin
     */
    public Alignment getOrigin() {
        return (Alignment) getProperty(PROPERTY_ORIGIN);
    }
    
    /**
     * Returns the height of the specified row.
     * 
     * @param rowIndex the row index
     * @return the width
     */
    public Extent getRowHeight(int rowIndex) {
        return (Extent) getIndexedProperty(PROPERTY_ROW_HEIGHT, rowIndex);
    }
    
    /**
     * Returns the number of columns or rows in the grid.
     * If cells are being rendered "across and then down", the "size"
     * represents the number of columns in the grid.  If the cells
     * are being rendered "down and then across", the "size" represents
     * the number of rows in the grid.
     * 
     * @return the number of columns or rows  
     */
    public int getSize() {
        Integer sizeValue = (Integer) getProperty(PROPERTY_SIZE);
        if (sizeValue == null) {
            return DEFAULT_SIZE;
        } else {
            return sizeValue.intValue();
        }
    }
    
    /**
     * Returns the overall width of the grid.
     * 
     * @return the width
     */
    public Extent getWidth() {
        return (Extent) getProperty(PROPERTY_WIDTH);
    }
    
    /**
     * Sets the <code>Border</code>.
     * 
     * @param newValue the new border
     */
    public void setBorder(Border newValue) {
        setProperty(PROPERTY_BORDER, newValue);
    }
    
    /**
     * Sets the width of the specified column.
     * 
     * @param columnIndex the column index
     * @param newValue the new width
     */
    public void setColumnWidth(int columnIndex, Extent newValue) {
        setIndexedProperty(PROPERTY_COLUMN_WIDTH, columnIndex, newValue);
    }
    
    /**
     * Sets the overall height of the grid.
     * 
     * @param newValue the new height
     */
    public void setHeight(Extent newValue) {
        setProperty(PROPERTY_HEIGHT, newValue);
    }
    
    /**
     * Sets the default cell insets.
     * 
     * @param newValue the new default cell insets
     */
    public void setInsets(Insets newValue) {
        setProperty(PROPERTY_INSETS, newValue);
    }

    //BUGBUG. Fully describe concept of "orientation".
    /**
     * Returns the orientation of the grid (either horizontal or vertical).
     * 
     * @param newValue the new orientation, one of the following values:
     *        <ul>
     *        <li><code>ORIENTATION_HORIZONTAL</code> (the default)</li>
     *        <li><code>ORIENTATION_VERTICAL</code></li>
     *        </ul>
     */
    public void setOrientation(int newValue) {
        setProperty(PROPERTY_ORIENTATION, new Integer(newValue));
    }
    
    //BUGBUG. Fully describe concept of "origin".
    /**
     * Sets the origin of the grid.
     * 
     * @param newValue the new origin
     */
    public void setOrigin(Alignment newValue) {
        setProperty(PROPERTY_ORIGIN, newValue);
    }
    
    /**
     * Sets the height of the specified row.
     * 
     * @param rowIndex the row index
     * @param newValue the new height
     */
    public void setRowHeight(int rowIndex, Extent newValue) {
        setIndexedProperty(PROPERTY_ROW_HEIGHT, rowIndex, newValue);
    }
    
    /**
     * Sets the number of columns or rows in the grid.
     * 
     * @param newValue the number of columns or rows
     * @see #getSize()
     */
    public void setSize(int newValue) {
        setProperty(PROPERTY_SIZE, new Integer(newValue));
    }
    
    /**
     * Sets the overall width of the grid.
     * 
     * @param newValue the new width
     */
    public void setWidth(Extent newValue) {
        setProperty(PROPERTY_WIDTH, newValue);
    }
}
