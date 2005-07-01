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

package nextapp.echo2.testapp.interactive.testscreen;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.IllegalChildException;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.WindowPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.list.DefaultListModel;
import nextapp.echo2.app.list.ListModel;
import nextapp.echo2.testapp.interactive.InteractiveApp;

/**
 * A test to ensure proper rendering when arbitrary components are 
 * user-selected together in parent child relationships. 
 */
public class HierarchyTest extends SplitPane {
    
    private abstract class ComponentEntry {
        
        public abstract String getName();
        
        public abstract Component  newInstance(); 
        
        public String toString() {
            return getName();
        }
    }
    
    private ComponentEntry selectFieldEntry = new ComponentEntry(){
        public String getName() {
            return "Select Field";
        }

        public Component newInstance() {
            SelectField selectField = new SelectField(new String[]{"alpha", "bravo", "charlie", "delta"});
            selectField.setStyleName("Default");
            return selectField;
        }
    };

    private ComponentEntry textFieldEntry = new ComponentEntry(){
        public String getName() {
            return "Text Field";
        }

        public Component newInstance() {
            TextField textField = new TextField();
            textField.setStyleName("Default");
            textField.setText("TextField");
            return textField;
        }
    };

    private ComponentEntry windowPaneEntry = new ComponentEntry(){
        public String getName() {
            return "WindowPane";
        }

        public Component newInstance() {
            WindowPane windowPane = new WindowPane();
            windowPane.setStyleName("Default");
            return windowPane;
        }
    };
    
    private ListModel componentListModel = new DefaultListModel(new Object[]{
        "(None)",
        selectFieldEntry,
        textFieldEntry, 
        windowPaneEntry
    });
    
    private ContentPane testPane;
    private SelectField parentSelectField, childSelectField;
    private Component parentComponentInstance, childComponentInstance;
    
    public HierarchyTest() {
        super(SplitPane.ORIENTATION_HORIZONTAL, new Extent(250, Extent.PX));
        setStyleName("DefaultResizable");
        
        Column controlGroupsColumn = new Column();
        controlGroupsColumn.setCellSpacing(new Extent(5));
        controlGroupsColumn.setStyleName("TestControlsColumn");
        add(controlGroupsColumn);
        
        Column parentSelectColumn = new Column();
        controlGroupsColumn.add(parentSelectColumn);
        
        parentSelectColumn.add(new Label("Parent"));
                        
        parentSelectField = new SelectField(componentListModel);
        parentSelectField.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if (parentSelectField.getSelectedItem() instanceof ComponentEntry) {
                    parentComponentInstance = ((ComponentEntry) parentSelectField.getSelectedItem()).newInstance();
                } else {
                    parentComponentInstance = null;
                }
                update();
            }
        });
        parentSelectColumn.add(parentSelectField);
        
        Column childSelectColumn = new Column();
        controlGroupsColumn.add(childSelectColumn);
        
        childSelectColumn.add(new Label("Child"));
                        
        childSelectField = new SelectField(componentListModel);
        childSelectField.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if (childSelectField.getSelectedItem() instanceof ComponentEntry) {
                    childComponentInstance = ((ComponentEntry) childSelectField.getSelectedItem()).newInstance();
                } else {
                    childComponentInstance = null;
                }
                update();
            }
        });
        childSelectColumn.add(childSelectField);
        
        testPane = new ContentPane();
        add(testPane);
    }
    
    public void update() {
        testPane.removeAll();
        if (parentComponentInstance == null) {
            return;
        }
        parentComponentInstance.removeAll();
        if (childComponentInstance != null) {
            try {
                parentComponentInstance.add(childComponentInstance);
            } catch (IllegalChildException ex) {
                InteractiveApp.getApp().consoleWrite(ex.toString());
            }
        }
        testPane.add(parentComponentInstance);
    }
}
