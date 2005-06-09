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

import nextapp.echo2.app.Color;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Font;
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
import nextapp.echo2.app.list.ListSelectionModel;
import nextapp.echo2.testapp.interactive.ButtonColumn;
import nextapp.echo2.testapp.interactive.InteractiveApp;
import nextapp.echo2.testapp.interactive.StyleUtil;
import nextapp.echo2.testapp.interactive.Styles;

/**
 * An interactive test for <code>ListBox</code>es.
 */
public class ListBoxTest extends SplitPane {

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
    
    private Column testColumn;

    public ListBoxTest() {
        super(SplitPane.ORIENTATION_HORIZONTAL, new Extent(250, Extent.PX));
        setStyleName("defaultResizable");

        SplitPaneLayoutData splitPaneLayoutData;

        ButtonColumn controlsColumn = new ButtonColumn();
        controlsColumn.setStyleName(Styles.TEST_CONTROLS_COLUMN_STYLE_NAME);
        add(controlsColumn);

        testColumn = new Column();
        testColumn.setCellSpacing(new Extent(15));
        splitPaneLayoutData = new SplitPaneLayoutData();
        splitPaneLayoutData.setInsets(new Insets(15));
        testColumn.setLayoutData(splitPaneLayoutData);
        add(testColumn);

        final ListBox listBox = new ListBox(new String[] { "Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight",
                "Nine", "Ten" });
        listBox.setHeight(new Extent(75));
        testColumn.add(listBox);

        final SelectField selectField = new SelectField(new String[] { "Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven",
                "Eight", "Nine", "Ten" });
        testColumn.add(selectField);

        controlsColumn.add(new Label("Global"));

        controlsColumn.addButton("Add ActionListener", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox.addActionListener(actionListener);
                selectField.addActionListener(actionListener);
            }
        });
        controlsColumn.addButton("Remove ActionListener", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox.removeActionListener(actionListener);
                selectField.removeActionListener(actionListener);
            }
        });
        controlsColumn.addButton("Add ChangeListener", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox.getSelectionModel().addChangeListener(changeListener);
                selectField.getSelectionModel().addChangeListener(changeListener);
            }
        });
        controlsColumn.addButton("Remove ChangeListener", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox.getSelectionModel().removeChangeListener(changeListener);
                selectField.getSelectionModel().removeChangeListener(changeListener);
            }
        });
        controlsColumn.addButton("Add ListDataListener", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox.getModel().addListDataListener(listDataListener);
                selectField.getModel().addListDataListener(listDataListener);
            }
        });
        controlsColumn.addButton("Remove ListDataListener", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox.getModel().removeListDataListener(listDataListener);
                selectField.getModel().removeListDataListener(listDataListener);
            }
        });
        
        controlsColumn.add(new Label("List Box"));

        controlsColumn.addButton("Toggle Multiple Select", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (ListSelectionModel.MULTIPLE_SELECTION == listBox.getSelectionMode()) {
                    listBox.setSelectedIndices(new int[] {});
                    listBox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                } else {
                    listBox.setSelectionMode(ListSelectionModel.MULTIPLE_SELECTION);
                }
            }
        });
        controlsColumn.addButton("Set Default Foreground ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = StyleUtil.randomColor();
                listBox.setForeground(color);
            }
        });
        controlsColumn.addButton("Set Default Background ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = StyleUtil.randomColor();
                listBox.setBackground(color);
            }
        });
        controlsColumn.addButton("Set Rollover Foreground ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = StyleUtil.randomColor();
                listBox.setRolloverForeground(color);
            }
        });
        controlsColumn.addButton("Set Rollover Background ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = StyleUtil.randomColor();
                listBox.setRolloverBackground(color);
            }
        });
        controlsColumn.addButton("Clear Selections", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox.setSelectedIndices(new int[] {});
            }
        });
        controlsColumn.addButton("Select Even Indices", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox.setSelectedIndices(new int[] { 0, 2, 4, 6, 8 });
            }
        });
        controlsColumn.addButton("Select Odd Indices", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox.setSelectedIndices(new int[] { 1, 3, 5, 7, 9 });
            }
        });
        controlsColumn.addButton("Increase Width (15 px)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (listBox.getWidth() == null) {
                    listBox.setWidth(new Extent(75));
                }
                listBox.setWidth(new Extent(listBox.getWidth().getValue() + 15));
            }
        });
        controlsColumn.addButton("Decrease Width (15 px)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (listBox.getWidth() == null) {
                    listBox.setWidth(new Extent(75));
                }
                listBox.setWidth(new Extent(listBox.getWidth().getValue() - 15));
            }
        });
        controlsColumn.addButton("Increase Height (15 px)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (listBox.getHeight() == null) {
                    listBox.setHeight(new Extent(75));
                }
                listBox.setHeight(new Extent(listBox.getHeight().getValue() + 15));
            }
        });
        controlsColumn.addButton("Decrease Height (15 px)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (listBox.getHeight() == null) {
                    listBox.setHeight(new Extent(75));
                }
                listBox.setHeight(new Extent(listBox.getHeight().getValue() - 15));
            }
        });

        controlsColumn.addButton("Set Font", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final Font font = StyleUtil.randomFont();
                listBox.setFont(font);
            }
        });
        controlsColumn.addButton("Clear Font", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listBox.setFont(null);
            }
        });

        controlsColumn.add(new Label("Select Field"));

        controlsColumn.addButton("Set Default Foreground ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = StyleUtil.randomColor();
                selectField.setForeground(color);
            }
        });
        controlsColumn.addButton("Set Default Background ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = StyleUtil.randomColor();
                selectField.setBackground(color);
            }
        });
        controlsColumn.addButton("Set Rollover Foreground ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = StyleUtil.randomColor();
                selectField.setRolloverForeground(color);
            }
        });
        controlsColumn.addButton("Set Rollover Background ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = StyleUtil.randomColor();
                selectField.setRolloverBackground(color);
            }
        });
        controlsColumn.addButton("Clear Selections", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectField.setSelectedIndex(0);
            }
        });
//BUGBUG. these are broken when clicked without selection.        
/*        
        controlsColumn.addButton("Select Next Even Index", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if ((select.getSelectedIndex() % 2) != 0) {
                    select.setSelectedIndex(select.getSelectedIndex() - 1);
                }
                if (select.getSelectedIndex() < 0 || select.getSelectedIndex() >= 10) {
                    select.setSelectedIndex(0);
                } else {
                    select.setSelectedIndex(select.getSelectedIndex() + 2);
                }
            }
        });
        controlsColumn.addButton("Select Next Odd Index", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if ((select.getSelectedIndex() % 2) == 0) {
                    select.setSelectedIndex(select.getSelectedIndex() - 1);
                }
                if (select.getSelectedIndex() == -1 || select.getSelectedIndex() == 9) {
                    select.setSelectedIndex(1);
                } else {
                    select.setSelectedIndex(select.getSelectedIndex() + 2);
                }
            }
        });
*/        
        controlsColumn.addButton("Increase Width (15 px)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectField.getWidth() == null) {
                    selectField.setWidth(new Extent(75));
                }
                selectField.setWidth(new Extent(selectField.getWidth().getValue() + 15));
            }
        });
        controlsColumn.addButton("Decrease Width (15 px)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectField.getWidth() == null) {
                    selectField.setWidth(new Extent(75));
                }
                selectField.setWidth(new Extent(selectField.getWidth().getValue() - 15));
            }
        });
        controlsColumn.addButton("Increase Height (15 px)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectField.getHeight() == null) {
                    selectField.setHeight(new Extent(75));
                }
                selectField.setHeight(new Extent(selectField.getHeight().getValue() + 15));
            }
        });
        controlsColumn.addButton("Decrease Height (15 px)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectField.getHeight() == null) {
                    selectField.setHeight(new Extent(75));
                }
                selectField.setHeight(new Extent(selectField.getHeight().getValue() - 15));
            }
        });

        controlsColumn.addButton("Set Font", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final Font font = StyleUtil.randomFont();
                selectField.setFont(font);
            }
        });
        controlsColumn.addButton("Clear Font", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectField.setFont(null);
            }
        });
    }
}