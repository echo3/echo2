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

import nextapp.echo2.app.event.TableColumnModelEvent;
import nextapp.echo2.app.event.TableColumnModelListener;
import nextapp.echo2.app.event.TableModelEvent;
import nextapp.echo2.app.event.TableModelListener;
import nextapp.echo2.app.table.DefaultTableCellRenderer;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.DefaultTableModel;
import nextapp.echo2.app.table.TableCellRenderer;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
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

    public static final String AUTO_CREATE_COLUMNS_FROM_MODEL_CHANGED_PROPERTY = "autoCreateColumnsFromModel";
    public static final String COLUMN_MODEL_CHANGED_PROPERTY = "columnModel";
    public static final String MODEL_CHANGED_PROPERTY = "model";
    
    private boolean autoCreateColumnsFromModel;
    private TableModel model;
    private TableColumnModel columnModel;
    private boolean valid;
    private Map rendererMap = new HashMap();
    
    /**
     * Listener to monitor changes to model.
     */
    private TableModelListener modelListener = new TableModelListener() {
        
        /**
         * @see nextapp.echo2.app.event.TableModelListener#tableChanged(nextapp.echo2.app.event.TableModelEvent)
         */
        public void tableChanged(TableModelEvent e) {
            invalidate();
            if ((e == null || e.getType() == TableModelEvent.STRUCTURE_CHANGED) && isAutoCreateColumnsFromModel()) {
                createDefaultColumnsFromModel();
            }
        }
    };
    
    /**
     * Listener to monitor changes to column model.
     */
    private TableColumnModelListener columnModelListener = new TableColumnModelListener() {

        /**
         * @see nextapp.echo2.app.event.TableColumnModelListener#columnAdded(nextapp.echo2.app.event.TableColumnModelEvent)
         */
        public void columnAdded(TableColumnModelEvent e) {
            invalidate();
        }

        /**
         * @see nextapp.echo2.app.event.TableColumnModelListener#columnMoved(nextapp.echo2.app.event.TableColumnModelEvent)
         */
        public void columnMoved(TableColumnModelEvent e) {
            invalidate();
        }

        /**
         * @see nextapp.echo2.app.event.TableColumnModelListener#columnRemoved(nextapp.echo2.app.event.TableColumnModelEvent)
         */
        public void columnRemoved(TableColumnModelEvent e) {
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
        this(model, null);
    }
    
    /** 
     * Creates a <code>Table</code> with the supplied 
     * <code>TableModel</code> and the specified <code>TableColumnModel</code>.
     *
     * @param modelthe initial model
     */
    public Table(TableModel model, TableColumnModel columnModel) {
        super();

        if (columnModel == null) {
            setColumnModel(new DefaultTableColumnModel());
            setAutoCreateColumnsFromModel(true);
        } else {
            setColumnModel(columnModel);
        }

        setModel(model);
    }
    
    /**
     * Creates a <code>TableColumnModel</code> based on the 
     * <code>TableModel</code>.  This method is invoked automatically when the 
     * <code>TableModel</code>'s structure changes if the 
     * <code>autoCreateColumnsFromModel</code> flag is set.
     */
    public void createDefaultColumnsFromModel() {
        if (model != null) {
            while (columnModel.getColumnCount() > 0) {
                columnModel.removeColumn(columnModel.getColumn(0));
            }
            
            int columnCount = model.getColumnCount();
            for (int index = 0; index < columnCount; ++index) {
                columnModel.addColumn(new TableColumn(index));
            }
        }
    }

    /**
     * Re-renders changed rows.
     */
    protected void doRender() {
        //BUGBUG. no header rendering yet.
        //BUGBUG. currently always performs whole rerender.
        removeAll();
        int rowCount = model.getRowCount();
        int columnCount = columnModel.getColumnCount();
        
        TableColumn[] tableColumns = new TableColumn[columnCount];
        TableCellRenderer[] columnRenderers = new TableCellRenderer[columnCount];
        
        for (int columnIndex = 0; columnIndex < columnCount; ++columnIndex) {
            tableColumns[columnIndex] = columnModel.getColumn(columnIndex);
            
            TableCellRenderer renderer = tableColumns[columnIndex].getCellRenderer();
            if (renderer == null) {
                Class columnClass = model.getColumnClass(tableColumns[columnIndex].getModelIndex());
                renderer = getDefaultRenderer(columnClass);
                if (renderer == null) {
                    renderer = DEFAULT_TABLE_CELL_RENDERER;
                }
            }
            columnRenderers[columnIndex] = renderer;
        }
        
        for (int rowIndex = 0; rowIndex < rowCount; ++rowIndex) {
            for (int columnIndex = 0; columnIndex < columnCount; ++columnIndex) {
                int modelColumnIndex = tableColumns[columnIndex].getModelIndex();
                Object modelValue = model.getValueAt(modelColumnIndex, rowIndex);
                Component renderedComponent 
                        = columnRenderers[columnIndex].getTableCellRendererComponent(this, modelValue, modelColumnIndex, rowIndex);
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
     * Returns the <code>TableColumnModel</code> describing this table's 
     * columns.
     *
     * @return the column model
     */
    public TableColumnModel getColumnModel() {
        return columnModel;
    }
    
    /**
     * Returns the default <code>TableCellRenderer</code> for the specified 
     * column class.  The default renderer will be used in the event that
     * a <code>TableColumn</code> does not provide a specific renderer.
     * 
     * @param columnClass the column <code>Class</code>
     * @return the <code>TableCellRenderer</code>
     */
    public TableCellRenderer getDefaultRenderer(Class columnClass) {
        return (TableCellRenderer) rendererMap.get(columnClass);
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
     * Determines whether the <code>TableColumnModel</code> will be created
     * automatically from the <code>TableModel</code>.  If this flag is set,
     * changes to the <code>TableModel</code> will automatically cause the
     * <code>TableColumnModel</code> to be re-created.  This flag is true
     * by default unless a <code>TableColumnModel</code> is specified in the
     * constructor.
     *
     * @return true if the <code>TableColumnModel</code> will be created
     *         automatically from the <code>TableModel</code>
     */
    public boolean isAutoCreateColumnsFromModel() {
        return autoCreateColumnsFromModel;
    }
        
    /**
     * Sets whether the <code>TableColumnModel</code> will be created
     * automatically from the <code>TableModel</code>.
     *
     * @param newValue true if the <code>TableColumnModel</code> should be 
     *         created automatically from the <code>TableModel</code>
     * @see #isAutoCreateColumnsFromModel()
     */
    public void setAutoCreateColumnsFromModel(boolean newValue) {
        boolean oldValue = autoCreateColumnsFromModel;
        autoCreateColumnsFromModel = newValue;
        
        if (!oldValue && newValue) {
            createDefaultColumnsFromModel();
        }
        
        firePropertyChange(AUTO_CREATE_COLUMNS_FROM_MODEL_CHANGED_PROPERTY, 
                Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
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
     * Sets the <code>TableColumnModel</code> describing this table's 
     * columns.
     *
     * @return the new column model
     */
    public void setColumnModel(TableColumnModel newValue) {
        invalidate();
        
        if (newValue == null) {
            throw new IllegalArgumentException("The model may not be null.");
        }
        
        TableColumnModel oldValue = columnModel;
        if (oldValue != null) {
            oldValue.removeColumnModelListener(columnModelListener);
        }
        columnModel = newValue;
        newValue.addColumnModelListener(columnModelListener);
        firePropertyChange(COLUMN_MODEL_CHANGED_PROPERTY, oldValue, newValue);
    }
    
    /**
     * Sets the default <code>TableCellRenderer</code> for the specified 
     * column class.  The default renderer will be used in the event that
     * a <code>TableColumn</code> does not provide a specific renderer.
     * 
     * @param columnClass the column <code>Class</code>
     * @param renderer the <code>TableCellRenderer</code>
     */
    public void setDefaultRenderer(Class columnClass, TableCellRenderer renderer) {
        if (renderer == null) {
            rendererMap.remove(renderer);
        } else {
            rendererMap.put(columnClass, renderer);
        }
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
        invalidate();
        
        if (newValue == null) {
            throw new IllegalArgumentException("The model may not be null.");
        }
        
        TableModel oldValue = model;
        if (oldValue != null) {
            oldValue.removeTableModelListener(modelListener);
        }
        model = newValue;
        newValue.addTableModelListener(modelListener);
        
        if (isAutoCreateColumnsFromModel()) {
            createDefaultColumnsFromModel();
        }
        
        firePropertyChange(MODEL_CHANGED_PROPERTY, oldValue, newValue);
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
