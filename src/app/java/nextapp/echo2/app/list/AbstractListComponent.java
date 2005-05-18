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

package nextapp.echo2.app.list;

import java.io.Serializable;

import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.event.ChangeEvent;
import nextapp.echo2.app.event.ChangeListener;
import nextapp.echo2.app.event.ListDataEvent;
import nextapp.echo2.app.event.ListDataListener;

//BUGBUG. Add support for rollover fonts, border.

/**
 * An abstract base class for list components.
 */
public class AbstractListComponent extends Component {
    
	public static final String LIST_DATA_CHANGED_PROPERTY = "listData";
	public static final String LIST_MODEL_CHANGED_PROPERTY = "listModel";
    public static final String LIST_CELL_RENDERER_CHANGED_PROPERTY = "listCellRenderer";
    public static final String SELECTION_MODEL_CHANGED_PROPERTY = "listSelectionModel";
    public static final String SELECTION_CHANGED_PROPERTY = "listSelectionChanged";

	public static final String PROPERTY_HEIGHT = "height";
	public static final String PROPERTY_INSETS = "insets";
	public static final String PROPERTY_ROLLOVER_BACKGROUND = "rolloverBackground";
	public static final String PROPERTY_ROLLOVER_FOREGROUND = "rolloverForeground";
	public static final String PROPERTY_WIDTH = "width";

    public static final DefaultListCellRenderer DEFAULT_LIST_CELL_RENDERER = new DefaultListCellRenderer();

    /**
     * Local handler for list selection events.
     */
    private class ChangeHandler
    implements ChangeListener, Serializable {

        /**
         * @see nextapp.echo2.app.event.ChangeListener#stateChanged(nextapp.echo2.app.event.ChangeEvent)
         */
        public void stateChanged(ChangeEvent e) {
            firePropertyChange(SELECTION_CHANGED_PROPERTY, null, null);
        }
    }

    /**
     * Local handler of <code>ListDataEvent</code>s.
     */
    private class ListDataHandler 
    implements ListDataListener, Serializable {
    
        /**
         * @see nextapp.echo.event.ListDataListener#contentsChanged(ListDataEvent)
         */
        public void contentsChanged(ListDataEvent e) {
            firePropertyChange(LIST_DATA_CHANGED_PROPERTY, null, null);
        }

        /**
         * @see nextapp.echo.event.ListDataListener#intervalAdded(ListDataEvent)
         */
        public void intervalAdded(ListDataEvent e) {
            firePropertyChange(LIST_DATA_CHANGED_PROPERTY, null, null);
        }

        /**
         * @see nextapp.echo.event.ListDataListener#intervalRemoved(ListDataEvent)
         */
        public void intervalRemoved(ListDataEvent e) {
            firePropertyChange(LIST_DATA_CHANGED_PROPERTY, null, null);
        }
    }
    
    private ListDataListener listDataHandler = new ListDataHandler();
    private ChangeListener changeHandler = new ChangeHandler();
    private ListCellRenderer listCellRenderer = DEFAULT_LIST_CELL_RENDERER;
    private ListModel model;
    private ListSelectionModel selectionModel;
    
    /**
     * Creates a new <code>AbstractListComponent</code> with default models.
     */
    public AbstractListComponent() {
        this(null, null);
    }
    
    /**
     * Creates a new <code>AbstractListComponent</code> with the specified 
     * models.
     * 
     * @param model the list data model
     * @param selectionModel the selection model
     */
    public AbstractListComponent(ListModel model, ListSelectionModel selectionModel) {
        super();
        if (model == null) {
            model = new DefaultListModel();
        }
        if (selectionModel == null) {
            selectionModel = new DefaultListSelectionModel();
        }
        setModel(model);
        setSelectionModel(selectionModel);
    }
    
    /**
     * Returns the <code>ListCellRenderer</code> used to render items.
     * 
     * @return the renderer
     */
    public ListCellRenderer getCellRenderer() {
    	return listCellRenderer;
    }
	
	/**
     * Returns the height.
     * 
     * @return the height
     */
    public Extent getHeight() {
        return (Extent) getProperty(PROPERTY_HEIGHT);
    }
    
    /**
     * Returns the inset margin around between the list components border and content.
     * 
     * @return the inset margin
     */
    public Insets getInsets() {
        return (Insets) getProperty(PROPERTY_INSETS);
    }
    
    /**
     * Returns the model.
     * 
     * @return the model
     */
    public ListModel getModel() {
        return model;
    }
    
	/**
     * Returns the rollover background.
     * 
     * @return the rollover background
     */
    public Color getRolloverBackground() {
        return (Color) getProperty(PROPERTY_ROLLOVER_BACKGROUND);
    }
    
    /**
     * Returns the rollover foreground.
     * 
     * @return the rollover foreground
     */
    public Color getRolloverForeground() {
        return (Color) getProperty(PROPERTY_ROLLOVER_FOREGROUND);
    }
    
    /**
     * Returns the selection model.
     * 
     * @return the selection model
     */
    public ListSelectionModel getSelectionModel() {
        return selectionModel;
    }
    
	/**
     * Returns the width.
     * 
     * @return the width
     */
    public Extent getWidth() {
        return (Extent) getProperty(PROPERTY_WIDTH);
    }
	
    /**
     * Sets the renderer for items.
     * 
     * @param newValue the new renderer
     */
    public void setCellRenderer(ListCellRenderer newValue) {
        ListCellRenderer oldValue = listCellRenderer;
        listCellRenderer = newValue;
        firePropertyChange(LIST_CELL_RENDERER_CHANGED_PROPERTY, oldValue, newValue);
    }
	
	/**
     * Sets the height.
     * 
     * @param newValue the new height
     */
    public void setHeight(Extent newValue) {
        setProperty(PROPERTY_HEIGHT, newValue);
    }
    
    /**
     * Sets the inset margin around between the list components border and content.
     * 
     * @param newValue the new inset margin
     */
    public void setInsets(Insets newValue) {
        setProperty(PROPERTY_INSETS, newValue);
    }
    
    /**
     * Sets the model.
     * The model may not be null.
     *
     * @param newValue the new model
     */
    public void setModel(ListModel newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException("Model may not be null.");
        }
        ListModel oldValue = model;
        if (oldValue != null) {
            oldValue.removeListDataListener(listDataHandler);
        }
        newValue.addListDataListener(listDataHandler);
        model = newValue;
    	firePropertyChange(LIST_MODEL_CHANGED_PROPERTY, oldValue, newValue);
    }
    
    /**
     * Sets the rollover background.
     * 
     * @param newValue the new rollover background
     */
    public void setRolloverBackground(Color newValue) {
        setProperty(PROPERTY_ROLLOVER_BACKGROUND, newValue);
    }
    
    /**
     * Sets the rollover foreground.
     * 
     * @param newValue the new rollover foreground
     */
    public void setRolloverForeground(Color newValue) {
        setProperty(PROPERTY_ROLLOVER_FOREGROUND, newValue);
    }
    
    /**
     * Sets the selection model.
     * The selection model may not be null.
     * 
     * @param newValue the new selection model
     */
    public void setSelectionModel(ListSelectionModel newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException("Selection model may not be null.");
        }
        ListSelectionModel oldValue = selectionModel;
        if (oldValue != null) {
            oldValue.removeChangeListener(changeHandler);
        }
        newValue.addChangeListener(changeHandler);
        selectionModel = newValue;
        firePropertyChange(SELECTION_MODEL_CHANGED_PROPERTY, oldValue, newValue);
    }
    
    /**
     * Sets the width.
     *
     * @param newValue the new width 
     */
    public void setWidth(Extent newValue) {
        setProperty(PROPERTY_WIDTH, newValue);
    }
}
