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
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.layout.RowLayoutData;
import nextapp.echo2.testapp.interactive.ButtonColumn;
import nextapp.echo2.testapp.interactive.StyleUtil;
import nextapp.echo2.testapp.interactive.Styles;

public class RowTest extends SplitPane {
    
    private int nextValue = 0;
    
    public RowTest() {
        super(SplitPane.ORIENTATION_HORIZONTAL, new Extent(250));
        setStyleName("defaultResizable");
        
        ButtonColumn controlsColumn = new ButtonColumn();
        controlsColumn.setStyleName(Styles.TEST_CONTROLS_COLUMN_STYLE_NAME);
        add(controlsColumn);

        final Row testRow = new Row();
        testRow.setBorder(new Border(new Extent(1), Color.BLUE, Border.STYLE_SOLID));
        add(testRow);
        
        controlsColumn.addButton("Add Item (at beginning)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testRow.add(new Label("Added item [" + nextValue++ + "]"), 0);
            }
        });
        controlsColumn.addButton("Add Item (at end)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testRow.add(new Label("Added item [" + nextValue++ + "]"));
            }
        });
        controlsColumn.addButton("Remove Last Item", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (testRow.getComponentCount() > 0) {
                    testRow.remove(testRow.getComponentCount() - 1);
                }
            }
        });
        controlsColumn.addButton("Add Some Items, Remove Some Items", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int count = 1 + ((int) (Math.random() * 10));
                for (int i = 0; i < count; ++i) {
                    int componentCount = testRow.getComponentCount();
                    if (componentCount > 0 && ((int) (Math.random() * 2)) == 0) {
                        // Perform remove.
                        int position = (int) (Math.random() * componentCount);
                        testRow.remove(position);
                    } else {
                        // Perform add.
                        int position = (int) (Math.random() * (componentCount + 1));
                        testRow.add(new Label("Added item [" + nextValue++ + "]"), position);
                    }
                }
            }
        });
        controlsColumn.addButton("Randomly Remove and Re-insert Item", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int itemCount = testRow.getComponentCount();
                if (itemCount == 0) {
                    return;
                }
                Component item = testRow.getComponent((int) (Math.random() * itemCount));
                testRow.remove(item);
                testRow.add(item, (int) (Math.random() * (itemCount - 1)));
            }
        }); 
        controlsColumn.addButton("Set Foreground", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testRow.setForeground(StyleUtil.randomColor());
            }
        });
        controlsColumn.addButton("Clear Foreground", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testRow.setForeground(null);
            }
        });
        controlsColumn.addButton("Set Background", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testRow.setBackground(StyleUtil.randomColor());
            }
        });
        controlsColumn.addButton("Clear Background", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testRow.setBackground(null);
            }
        });
        controlsColumn.addButton("Set Border (All Attributes)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testRow.setBorder(StyleUtil.randomBorder());
            }
        });
        controlsColumn.addButton("Set Border Color", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Border border = testRow.getBorder();
                if (border == null) {
                    border = new Border(new Extent(1), Color.BLUE, Border.STYLE_SOLID);
                }
                testRow.setBorder(new Border(border.getSize(), StyleUtil.randomColor(), border.getStyle()));
            }
        });
        controlsColumn.addButton("Set Border Size", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testRow.setBorder(StyleUtil.nextBorderSize(testRow.getBorder()));
            }
        });
        controlsColumn.addButton("Set Border Style", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testRow.setBorder(StyleUtil.nextBorderStyle(testRow.getBorder()));
            }
        });
        controlsColumn.addButton("Remove Border", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testRow.setBorder(null);
            }
        });
        controlsColumn.addButton("Cell Spacing -> 0px", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testRow.setCellSpacing(new Extent(0, Extent.PX));
            }
        });
        controlsColumn.addButton("Cell Spacing -> 2px", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testRow.setCellSpacing(new Extent(2, Extent.PX));
            }
        });
        controlsColumn.addButton("Cell Spacing -> 20px", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testRow.setCellSpacing(new Extent(20, Extent.PX));
            }
        });
        controlsColumn.addButton("Insets -> null", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testRow.setInsets(null);
            }
        });
        controlsColumn.addButton("Insets -> 0px", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testRow.setInsets(new Insets(0));
            }
        });
        controlsColumn.addButton("Insets -> 5px", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testRow.setInsets(new Insets(5));
            }
        });
        controlsColumn.addButton("Insets -> 10/20/30/40px", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testRow.setInsets(new Insets(10, 20, 30, 40));
            }
        });
        controlsColumn.addButton("Set Layout Data (of random item)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int componentCount = testRow.getComponentCount();
                if (componentCount == 0) {
                    return;
                }
                Component component =  testRow.getComponent((int) (Math.random() * componentCount));
                RowLayoutData rowLayoutData = new RowLayoutData();
                rowLayoutData.setInsets(new Insets((int) (Math.random() * 30)));
                rowLayoutData.setBackground(StyleUtil.randomBrightColor());
                component.setLayoutData(rowLayoutData);
            }
        });
        controlsColumn.addButton("Add Item, Randomize Column Insets", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testRow.add(new Label("Added item [" + nextValue++ + "]"));
                testRow.setInsets(new Insets((int) (Math.random() * 50)));
            }
        });
    }
}
