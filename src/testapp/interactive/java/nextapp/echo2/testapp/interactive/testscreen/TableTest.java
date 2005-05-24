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

import nextapp.echo2.app.Border;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.layout.SplitPaneLayoutData;
import nextapp.echo2.app.table.AbstractTableModel;
import nextapp.echo2.app.table.DefaultTableModel;
import nextapp.echo2.testapp.interactive.ButtonColumn;
import nextapp.echo2.testapp.interactive.StyleUtil;
import nextapp.echo2.testapp.interactive.Styles;

/**
 * A test for <code>Tables</code>s.
 */
public class TableTest extends SplitPane {
    
    private Table testTable;
    
    private class MultiplicationTableModel extends AbstractTableModel {

        /**
         * @see nextapp.echo2.app.table.TableModel#getColumnCount()
         */
        public int getColumnCount() {
            return 12;
        }
        
        /**
         * @see nextapp.echo2.app.table.TableModel#getRowCount()
         */
        public int getRowCount() {
            return 12;
        }
        
        /**
         * @see nextapp.echo2.app.table.TableModel#getValueAt(int, int)
         */
        public Object getValueAt(int column, int row) {
            return new Integer((column + 1) * (row + 1));
        }
    }
    
    public TableTest() {
        super(SplitPane.ORIENTATION_HORIZONTAL, new Extent(250, Extent.PX));
        setStyleName("defaultResizable");
        
        Column groupContainerColumn = new Column();
        groupContainerColumn.setCellSpacing(new Extent(5));
        groupContainerColumn.setStyleName(Styles.TEST_CONTROLS_COLUMN_STYLE_NAME);
        add(groupContainerColumn);
        
        Column testColumn = new Column();
        SplitPaneLayoutData splitPaneLayoutData = new SplitPaneLayoutData();
        splitPaneLayoutData.setInsets(new Insets(10, 5));
        testColumn.setLayoutData(splitPaneLayoutData);
        add(testColumn);

        ButtonColumn controlsColumn;
        
        controlsColumn = new ButtonColumn();
        groupContainerColumn.add(controlsColumn);

        controlsColumn.add(new Label("TableModel"));
        
        controlsColumn.addButton("Multiplication Model", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testTable.setModel(new MultiplicationTableModel());
            }
        });
        
        controlsColumn.addButton("DefaultTableModel", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testTable.setModel(new DefaultTableModel());
            }
        });
        
        testTable = new Table();
        testTable.setBorder(new Border(new Extent(1), Color.BLUE, Border.STYLE_SOLID));
        testColumn.add(testTable);

        controlsColumn.add(new Label("Appearance"));
        
        controlsColumn.addButton("Change Foreground", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testTable.setForeground(StyleUtil.randomColor());
            }
        });
        controlsColumn.addButton("Change Background", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testTable.setBackground(StyleUtil.randomColor());
            }
        });
        controlsColumn.addButton("Change Border (All Attributes)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testTable.setBorder(StyleUtil.randomBorder());
            }
        });
        controlsColumn.addButton("Change Border Color", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Border border = testTable.getBorder();
                testTable.setBorder(new Border(border.getSize(), StyleUtil.randomColor(), border.getStyle()));
            }
        });
        controlsColumn.addButton("Change Border Size", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testTable.setBorder(StyleUtil.nextBorderSize(testTable.getBorder()));
            }
        });
        controlsColumn.addButton("Change Border Style", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testTable.setBorder(StyleUtil.nextBorderStyle(testTable.getBorder()));
            }
        });
        
        controlsColumn.addButton("Set Insets 0px", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testTable.setInsets(new Insets(0));
            }
        });
        controlsColumn.addButton("Set Insets 2px", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testTable.setInsets(new Insets(2));
            }
        });
        controlsColumn.addButton("Set Insets 10/5px", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testTable.setInsets(new Insets(10, 5));
            }
        });
        controlsColumn.addButton("Set Insets 10/20/30/40px", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testTable.setInsets(new Insets(10, 20, 30, 40));
            }
        });
        controlsColumn.addButton("Set Width = null", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testTable.setWidth(null);
            }
        });
        controlsColumn.addButton("Set Width = 500px", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testTable.setWidth(new Extent(500));
            }
        });
        controlsColumn.addButton("Set Width = 100%", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testTable.setWidth(new Extent(100, Extent.PERCENT));
            }
        });
    }
}
