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

import java.util.HashMap;
import java.util.Map;

import nextapp.echo2.app.event.TableModelEvent;
import nextapp.echo2.app.event.TableModelListener;
import nextapp.echo2.app.table.DefaultTableCellRenderer;
import nextapp.echo2.app.table.DefaultTableModel;
import nextapp.echo2.app.table.TableCellRenderer;
import nextapp.echo2.app.table.TableModel;

/**
 * A component used to display data in a tabular format.
 *
 * @see nextapp.echo.table
 */
public class Table extends Component {

    /**
     * The default renderer for table cells. 
     */
    public static final TableCellRenderer DEFAULT_TABLE_CELL_RENDERER = new DefaultTableCellRenderer();

    public static final String PROPERTY_BORDER = "border";
    public static final String PROPERTY_HEIGHT = "height";
    public static final String PROPERTY_INSETS = "insets";
    public static final String PROPERTY_WIDTH = "width";

    public static final String MODEL_CHANGED_PROPERTY = "model";
    
    private TableModel model;
    private boolean valid = false;
    private Map rendererMap = new HashMap();
    
    private TableModelListener renderListener = new TableModelListener() {
        
        /**
         * @see nextapp.echo2.app.event.TableModelListener#tableChanged(nextapp.echo2.app.event.TableModelEvent)
         */
        public void tableChanged(TableModelEvent e) {
            invalidate();
        }
    };
    
    /**
     * Creates a new <code>Table</code> with an empty
     * <code>DefaultTableModel</code>.
     */
    public Table() {
        this(new DefaultTableModel());
    }
    
    /**
     * Creates a new <code>Table</code> with a new
     * <code>DefaultTableModel</code> with the specified dimensions.
     *
     * @param columns the initial column count
     * @param rows the initial row count
     */
    public Table(int columns, int rows) {
        this(new DefaultTableModel(columns, rows));
    }
    
    /** 
     * Creates a <code>Table</code> using the supplied 
     * <code>TableModel</code>.
     *
     * @param modelthe initial model
     */
    public Table(TableModel model) {
        super();
        setModel(model);
    }
    
    /**
     * Re-renders changed rows.
     */
    protected void doRender() {
        removeAll();
        int rowCount = model.getRowCount();
        int columnCount = model.getColumnCount();
        
        TableCellRenderer[] columnRenderers = new TableCellRenderer[columnCount];
        for (int columnIndex = 0; columnIndex < columnCount; ++columnIndex) {
            Class columnClass = model.getColumnClass(columnIndex);
            if (columnClass == null) {
                columnRenderers[columnIndex] = DEFAULT_TABLE_CELL_RENDERER;
            } else {
                columnRenderers[columnIndex] = getRenderer(columnClass);
            }
        }
        
        for (int rowIndex = 0; rowIndex < rowCount; ++rowIndex) {
            for (int columnIndex = 0; columnIndex < columnCount; ++columnIndex) {
                Object modelValue = model.getValueAt(columnIndex, rowIndex);
                Component renderedComponent 
                        = columnRenderers[columnIndex].getTableCellRendererComponent(this, modelValue, columnIndex, rowIndex);
                add(renderedComponent);
            }
        }
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
     * Returns the <code>TableModel</code> being visualized by this 
     * <code>Table</code>.
     *
     * @return the model
     */
    public TableModel getModel() {
        return model;
    }
    
    /**
     * Returns the <code>TableCellRenderer</code> for the specified column 
     * class.
     * 
     * @param columnClass the column <code>Class</code>
     * @return the <code>TableCellRenderer</code>
     */
    public TableCellRenderer getRenderer(Class columnClass) {
        return (TableCellRenderer) rendererMap.get(columnClass);
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
     * Marks the table as needing to be re-rendered.
     */
    protected void invalidate() {
        valid = false;
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
    
    /**
     * Sets the <code>TableModel</code> being visualized.
     * 
     * @param newValue the new model (may not be null)
     */
    public void setModel(TableModel newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException("The model may not be null.");
        }
        
        TableModel oldValue = model;
        if (oldValue != null) {
            oldValue.removeTableModelListener(renderListener);
        }
        model = newValue;
        newValue.addTableModelListener(renderListener);
        firePropertyChange(MODEL_CHANGED_PROPERTY, oldValue, newValue);
    }
    
    /**
     * Sets the <code>TableCellRenderer</code> for the specified column 
     * class.
     * 
     * @param columnClass the column <code>Class</code>
     * @param renderer the <code>TableCellRenderer</code>
     */
    public void setRenderer(Class columnClass, TableCellRenderer renderer) {
        if (renderer == null) {
            rendererMap.remove(renderer);
        } else {
            rendererMap.put(columnClass, renderer);
        }
    }

    /**
     * Sets the overall width of the grid.
     * 
     * @param newValue the new width
     */
    public void setWidth(Extent newValue) {
        setProperty(PROPERTY_WIDTH, newValue);
    }
    
    /**
     * @see nextapp.echo2.app.Component#validate()
     */
    public void validate() {
        super.validate();
        while (!valid) {
            valid = true;
            doRender();
        }
    }
}
