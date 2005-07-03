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
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.ListBox;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.ChangeEvent;
import nextapp.echo2.app.event.ChangeListener;
import nextapp.echo2.app.event.ListDataEvent;
import nextapp.echo2.app.event.ListDataListener;
import nextapp.echo2.app.layout.SplitPaneLayoutData;
import nextapp.echo2.app.list.AbstractListComponent;
import nextapp.echo2.app.list.ListCellRenderer;
import nextapp.echo2.app.list.ListSelectionModel;
import nextapp.echo2.app.list.StyledListCell;
import nextapp.echo2.testapp.interactive.ButtonColumn;
import nextapp.echo2.testapp.interactive.InteractiveApp;
import nextapp.echo2.testapp.interactive.StyleUtil;

/**
 * An interactive test for <code>ListBox</code>es.
 */
public class ListBoxTest extends SplitPane {
    
    public static final String[] NUMBERS = new String[] { "Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight",
                "Nine", "Ten" };

    /**
     * Writes <code>ActionEvent</code>s to console.
     */
    private ActionListener actionListener = new ActionListener() {

        /**
         * @see nextapp.echo2.app.event.ActionListener#actionPerformed(nextapp.echo2.app.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            ((InteractiveApp) getApplicationInstance()).consoleWrite(e.toString());
        }
    };
    
    /**
     * Writes <code>ChangeEvent</code>s to console.
     */
    private ChangeListener changeListener = new ChangeListener() {

        /**
         * @see nextapp.echo2.app.event.ChangeListener#stateChanged(nextapp.echo2.app.event.ChangeEvent)
         */
        public void stateChanged(ChangeEvent e) {
            ((InteractiveApp) getApplicationInstance()).consoleWrite(e.toString());
        }
    };
    
    /**
     * Writes <code>ListDataListener</code>s to console.
     */
    private ListDataListener listDataListener = new ListDataListener() {
        
        /**
         * @see nextapp.echo2.app.event.ListDataListener#contentsChanged(nextapp.echo2.app.event.ListDataEvent)
         */
        public void contentsChanged(ListDataEvent e) {
            ((InteractiveApp) getApplicationInstance()).consoleWrite(e.toString());
        }

        /**
         * @see nextapp.echo2.app.event.ListDataListener#intervalAdded(nextapp.echo2.app.event.ListDataEvent)
         */
        public void intervalAdded(ListDataEvent e) {
            ((InteractiveApp) getApplicationInstance()).consoleWrite(e.toString());
        }

        /**
         * @see nextapp.echo2.app.event.ListDataListener#intervalRemoved(nextapp.echo2.app.event.ListDataEvent)
         */
        public void intervalRemoved(ListDataEvent e) {
            ((InteractiveApp) getApplicationInstance()).consoleWrite(e.toString());
        }
    };
    
    private ListCellRenderer evenOddListCellRenderer = new ListCellRenderer(){
    
        private Color foreground1 = new Color(0x007f00);
        private Color background1 = new Color(0xafffaf);
        private Color foreground2 = new Color(0x7f0000);
        private Color background2 = new Color(0xffafaf);
        private Font font1 = new Font(Font.MONOSPACE, Font.BOLD, null);
        
        /**
         * @see nextapp.echo2.app.list.ListCellRenderer#getListCellRendererComponent(nextapp.echo2.app.Component, 
         *      java.lang.Object, int)
         */
        public Object getListCellRendererComponent(Component list, final Object value, final int index) {
            return new StyledListCell() {
            
                public Color getForeground() {
                    return index % 2 == 0 ? foreground1 : foreground2;
                }
            
                public Font getFont() {
                    return index % 2 == 0 ? font1 : null;
                }
            
                public Color getBackground() {
                    return index % 2 == 0 ? background1 : background2;
                }
                
                public String toString() {
                    return value == null ? null : value.toString();
                }
            };
        }
    };
    
    private Column testColumn;
    private ListBox listBox1, listBox2;
    private SelectField selectField1, selectField2;

    public ListBoxTest() {
        super(SplitPane.ORIENTATION_HORIZONTAL, new Extent(250, Extent.PX));
        setStyleName("DefaultResizable");

        SplitPaneLayoutData splitPaneLayoutData;

        ButtonColumn controlsColumn = new ButtonColumn();
        controlsColumn.setStyleName("TestControlsColumn");
        add(controlsColumn);

        testColumn = new Column();
        testColumn.setCellSpacing(new Extent(15));
        splitPaneLayoutData = new SplitPaneLayoutData();
        splitPaneLayoutData.setInsets(new Insets(15));
        testColumn.setLayoutData(splitPaneLayoutData);
        add(testColumn);

        listBox1 = new ListBox(NUMBERS);
        testColumn.add(listBox1);

        selectField1 = new SelectField(NUMBERS);
        testColumn.add(selectField1);
        
        Grid grid = new Grid();
        grid.setBorder(new Border(1, Color.BLACK, Border.STYLE_SOLID));
        testColumn.add(grid);
        
        selectField2 = new SelectField(NUMBERS);
        grid.add(selectField2);
        
        listBox2 = new ListBox(NUMBERS);
        grid.add(listBox2);
        
        controlsColumn.add(new Label("Global"));

        controlsColumn.addButton("Add ActionListener", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox1.addActionListener(actionListener);
                selectField1.addActionListener(actionListener);
            }
        });
        controlsColumn.addButton("Remove ActionListener", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox1.removeActionListener(actionListener);
                selectField1.removeActionListener(actionListener);
            }
        });
        controlsColumn.addButton("Add ChangeListener", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox1.getSelectionModel().addChangeListener(changeListener);
                selectField1.getSelectionModel().addChangeListener(changeListener);
            }
        });
        controlsColumn.addButton("Remove ChangeListener", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox1.getSelectionModel().removeChangeListener(changeListener);
                selectField1.getSelectionModel().removeChangeListener(changeListener);
            }
        });
        controlsColumn.addButton("Add ListDataListener", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox1.getModel().addListDataListener(listDataListener);
                selectField1.getModel().addListDataListener(listDataListener);
            }
        });
        controlsColumn.addButton("Remove ListDataListener", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox1.getModel().removeListDataListener(listDataListener);
                selectField1.getModel().removeListDataListener(listDataListener);
            }
        });
        controlsColumn.addButton("Toggle Enabled State", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox1.setEnabled(!listBox1.isEnabled());
                selectField1.setEnabled(!selectField1.isEnabled());
            }
        });
        controlsColumn.addButton("Set ListCellRenderer", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox1.setCellRenderer(evenOddListCellRenderer);
                selectField1.setCellRenderer(evenOddListCellRenderer);
            }
        });
        controlsColumn.addButton("Clear ListCellRenderer", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox1.setCellRenderer(AbstractListComponent.DEFAULT_LIST_CELL_RENDERER);
                selectField1.setCellRenderer(AbstractListComponent.DEFAULT_LIST_CELL_RENDERER);
            }
        });
        controlsColumn.addButton("Set Border", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox1.setBorder(StyleUtil.randomBorder());
                selectField1.setBorder(StyleUtil.randomBorder());
            }
        });
        controlsColumn.addButton("Clear Border", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox1.setBorder(null);
                selectField1.setBorder(null);
            }
        });
        
        controlsColumn.add(new Label("List Box"));

        controlsColumn.addButton("Toggle Multiple Select", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (ListSelectionModel.MULTIPLE_SELECTION == listBox1.getSelectionMode()) {
                    listBox1.setSelectedIndices(new int[] {});
                    listBox1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                } else {
                    listBox1.setSelectionMode(ListSelectionModel.MULTIPLE_SELECTION);
                }
            }
        });
        controlsColumn.addButton("Set Default Foreground ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = StyleUtil.randomColor();
                listBox1.setForeground(color);
            }
        });
        controlsColumn.addButton("Set Default Background ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = StyleUtil.randomColor();
                listBox1.setBackground(color);
            }
        });
        controlsColumn.addButton("Set Rollover Foreground ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = StyleUtil.randomColor();
                listBox1.setRolloverForeground(color);
            }
        });
        controlsColumn.addButton("Set Rollover Background ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = StyleUtil.randomColor();
                listBox1.setRolloverBackground(color);
            }
        });
        controlsColumn.addButton("Set Rollover Font ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox1.setRolloverFont(StyleUtil.randomFont());
            }
        });
        controlsColumn.addButton("Clear Selections", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox1.setSelectedIndices(new int[] {});
            }
        });
        controlsColumn.addButton("Select Even Indices", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox1.setSelectedIndices(new int[] { 0, 2, 4, 6, 8 });
            }
        });
        controlsColumn.addButton("Select Odd Indices", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox1.setSelectedIndices(new int[] { 1, 3, 5, 7, 9 });
            }
        });
        controlsColumn.addButton("Increase Width (15 px)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (listBox1.getWidth() == null) {
                    listBox1.setWidth(new Extent(75));
                }
                listBox1.setWidth(new Extent(listBox1.getWidth().getValue() + 15));
            }
        });
        controlsColumn.addButton("Decrease Width (15 px)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (listBox1.getWidth() == null) {
                    listBox1.setWidth(new Extent(75));
                }
                listBox1.setWidth(new Extent(listBox1.getWidth().getValue() - 15));
            }
        });
        controlsColumn.addButton("Increase Height (15 px)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (listBox1.getHeight() == null) {
                    listBox1.setHeight(new Extent(75));
                }
                listBox1.setHeight(new Extent(listBox1.getHeight().getValue() + 15));
            }
        });
        controlsColumn.addButton("Decrease Height (15 px)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (listBox1.getHeight() == null) {
                    listBox1.setHeight(new Extent(75));
                }
                listBox1.setHeight(new Extent(listBox1.getHeight().getValue() - 15));
            }
        });

        controlsColumn.addButton("Set Font", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final Font font = StyleUtil.randomFont();
                listBox1.setFont(font);
            }
        });
        controlsColumn.addButton("Clear Font", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox1.setFont(null);
            }
        });

        controlsColumn.add(new Label("Select Field"));

        controlsColumn.addButton("Set Default Foreground ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = StyleUtil.randomColor();
                selectField1.setForeground(color);
            }
        });
        controlsColumn.addButton("Set Default Background ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = StyleUtil.randomColor();
                selectField1.setBackground(color);
            }
        });
        controlsColumn.addButton("Set Rollover Foreground ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = StyleUtil.randomColor();
                selectField1.setRolloverForeground(color);
            }
        });
        controlsColumn.addButton("Set Rollover Background ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = StyleUtil.randomColor();
                selectField1.setRolloverBackground(color);
            }
        });
        controlsColumn.addButton("Set Rollover Font ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectField1.setRolloverFont(StyleUtil.randomFont());
            }
        });
        controlsColumn.addButton("Clear Selections", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectField1.setSelectedIndex(0);
            }
        });
        controlsColumn.addButton("Increase Width (15 px)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectField1.getWidth() == null) {
                    selectField1.setWidth(new Extent(75));
                }
                selectField1.setWidth(new Extent(selectField1.getWidth().getValue() + 15));
            }
        });
        controlsColumn.addButton("Decrease Width (15 px)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectField1.getWidth() == null) {
                    selectField1.setWidth(new Extent(75));
                }
                selectField1.setWidth(new Extent(selectField1.getWidth().getValue() - 15));
            }
        });
        controlsColumn.addButton("Increase Height (15 px)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectField1.getHeight() == null) {
                    selectField1.setHeight(new Extent(75));
                }
                selectField1.setHeight(new Extent(selectField1.getHeight().getValue() + 15));
            }
        });
        controlsColumn.addButton("Decrease Height (15 px)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectField1.getHeight() == null) {
                    selectField1.setHeight(new Extent(75));
                }
                selectField1.setHeight(new Extent(selectField1.getHeight().getValue() - 15));
            }
        });

        controlsColumn.addButton("Set Font", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final Font font = StyleUtil.randomFont();
                selectField1.setFont(font);
            }
        });
        controlsColumn.addButton("Clear Font", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectField1.setFont(null);
            }
        });
    }
}