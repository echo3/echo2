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
import nextapp.echo2.app.layout.SplitPaneLayoutData;
import nextapp.echo2.app.list.ListSelectionModel;
import nextapp.echo2.testapp.interactive.ButtonColumn;
import nextapp.echo2.testapp.interactive.StyleUtil;
import nextapp.echo2.testapp.interactive.Styles;

/**
 * An interactive test for <code>ListBox</code>es.
 */
public class ListBoxTest extends SplitPane {

    private Column testColumn;

    public ListBoxTest() {
        super(SplitPane.ORIENTATION_HORIZONTAL, new Extent(250, Extent.PX));
        setResizable(true);

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

        final ListBox listbox = new ListBox(new String[] { "Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight",
                "Nine", "Ten" });
        listbox.setHeight(new Extent(75));
        testColumn.add(listbox);

        final SelectField select = new SelectField(new String[] { "Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven",
                "Eight", "Nine", "Ten" });
        testColumn.add(select);

        testColumn.add(new Label(" "));

        controlsColumn.add(new Label("List Box"));

        controlsColumn.addButton("Toggle Multiple Select", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (ListSelectionModel.MULTIPLE_SELECTION == listbox.getSelectionMode()) {
                    listbox.setSelectedIndices(new int[] {});
                    listbox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                } else {
                    listbox.setSelectionMode(ListSelectionModel.MULTIPLE_SELECTION);
                }
            }
        });
        controlsColumn.addButton("Set Default Foreground ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = StyleUtil.randomColor();
                listbox.setForeground(color);
            }
        });
        controlsColumn.addButton("Set Default Background ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = StyleUtil.randomColor();
                listbox.setBackground(color);
            }
        });
        controlsColumn.addButton("Set Rollover Foreground ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = StyleUtil.randomColor();
                listbox.setRolloverForeground(color);
            }
        });
        controlsColumn.addButton("Set Rollover Background ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = StyleUtil.randomColor();
                listbox.setRolloverBackground(color);
            }
        });
        controlsColumn.addButton("Clear Selections", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listbox.setSelectedIndices(new int[] {});
            }
        });
        controlsColumn.addButton("Select Even Indices", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listbox.setSelectedIndices(new int[] { 0, 2, 4, 6, 8 });
            }
        });
        controlsColumn.addButton("Select Odd Indices", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listbox.setSelectedIndices(new int[] { 1, 3, 5, 7, 9 });
            }
        });
        controlsColumn.addButton("Increase Width (15 px)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (listbox.getWidth() == null) {
                    listbox.setWidth(new Extent(75));
                }
                listbox.setWidth(new Extent(listbox.getWidth().getValue() + 15));
            }
        });
        controlsColumn.addButton("Decrease Width (15 px)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (listbox.getWidth() == null) {
                    listbox.setWidth(new Extent(75));
                }
                listbox.setWidth(new Extent(listbox.getWidth().getValue() - 15));
            }
        });
        controlsColumn.addButton("Increase Height (15 px)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (listbox.getHeight() == null) {
                    listbox.setHeight(new Extent(75));
                }
                listbox.setHeight(new Extent(listbox.getHeight().getValue() + 15));
            }
        });
        controlsColumn.addButton("Decrease Height (15 px)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (listbox.getHeight() == null) {
                    listbox.setHeight(new Extent(75));
                }
                listbox.setHeight(new Extent(listbox.getHeight().getValue() - 15));
            }
        });

        controlsColumn.addButton("Set Font", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final Font font = StyleUtil.randomFont();
                listbox.setFont(font);
            }
        });
        controlsColumn.addButton("Clear Font", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listbox.setFont(null);
            }
        });

        controlsColumn.add(new Label("Select Field"));

        controlsColumn.addButton("Set Default Foreground ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = StyleUtil.randomColor();
                select.setForeground(color);
            }
        });
        controlsColumn.addButton("Set Default Background ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = StyleUtil.randomColor();
                select.setBackground(color);
            }
        });
        controlsColumn.addButton("Set Rollover Foreground ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = StyleUtil.randomColor();
                select.setRolloverForeground(color);
            }
        });
        controlsColumn.addButton("Set Rollover Background ", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = StyleUtil.randomColor();
                select.setRolloverBackground(color);
            }
        });
        controlsColumn.addButton("Clear Selections", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                select.setSelectedIndex(0);
            }
        });
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
        controlsColumn.addButton("Increase Width (15 px)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (select.getWidth() == null) {
                    select.setWidth(new Extent(75));
                }
                select.setWidth(new Extent(select.getWidth().getValue() + 15));
            }
        });
        controlsColumn.addButton("Decrease Width (15 px)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (select.getWidth() == null) {
                    select.setWidth(new Extent(75));
                }
                select.setWidth(new Extent(select.getWidth().getValue() - 15));
            }
        });
        controlsColumn.addButton("Increase Height (15 px)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (select.getHeight() == null) {
                    select.setHeight(new Extent(75));
                }
                select.setHeight(new Extent(select.getHeight().getValue() + 15));
            }
        });
        controlsColumn.addButton("Decrease Height (15 px)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (select.getHeight() == null) {
                    select.setHeight(new Extent(75));
                }
                select.setHeight(new Extent(select.getHeight().getValue() - 15));
            }
        });

        controlsColumn.addButton("Set Font", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final Font font = StyleUtil.randomFont();
                select.setFont(font);
            }
        });
        controlsColumn.addButton("Clear Font", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                select.setFont(null);
            }
        });
    }
}