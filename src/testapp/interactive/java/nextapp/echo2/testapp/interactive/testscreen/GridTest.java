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
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.layout.GridCellLayoutData;
import nextapp.echo2.testapp.interactive.ButtonRow;
import nextapp.echo2.testapp.interactive.StyleUtil;
import nextapp.echo2.testapp.interactive.Styles;

/**
 * 
 */
public class GridTest extends SplitPane {

    private int nextCellNumber = 0;
    private Button selectedButton;
    
    private ActionListener cellButtonActionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            Button button = (Button) e.getSource();
            selectCellButton(button);
        }
    };
    
    public GridTest() {
        super(SplitPane.ORIENTATION_HORIZONTAL, new Extent(250, Extent.PX));
        setResizable(true);
        
        Row groupContainerRow = new Row();
        groupContainerRow.setCellSpacing(new Extent(5));
        groupContainerRow.setStyleName(Styles.TEST_CONTROLS_ROW_STYLE_NAME);
        add(groupContainerRow);
        
        Row testRow = new Row();
        add(testRow);

        ButtonRow controlsRow;
        
        controlsRow = new ButtonRow();
        controlsRow.add(new Label("Insert/Delete Cells"));
        groupContainerRow.add(controlsRow);
        
        final Grid grid = new Grid(4);
        grid.setBorder(new Border(new Extent(1), Color.BLUE, Border.STYLE_SOLID));
        while (nextCellNumber < 17) {
            grid.add(createGridCellButton());
        }
        testRow.add(grid);

        controlsRow.addButton("Clear Selection", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectCellButton(null);
            }
        });

        controlsRow.addButton("Insert Cell Before Selected", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedButton != null) {
                    grid.add(createGridCellButton(), grid.indexOf(selectedButton));
                }
            }
        });

        controlsRow.addButton("Append New Cell", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Button button = createGridCellButton(); 
                grid.add(button);
                selectCellButton(button);
            }
        });

        controlsRow.addButton("Delete Selected Cell", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedButton != null) {
                    int index = grid.indexOf(selectedButton);
                    grid.remove(selectedButton);
                    if (grid.getComponentCount() != 0) {
                        if (index < grid.getComponentCount()) {
                            selectCellButton((Button) grid.getComponent(index));
                        } else {
                            selectCellButton((Button) grid.getComponent(grid.getComponentCount() - 1));
                        }
                    } else {
                        selectCellButton(null);
                    }
                }
            }
        });
        
        controlsRow = new ButtonRow();
        controlsRow.add(new Label("Configure Grid"));
        groupContainerRow.add(controlsRow);
        
        controlsRow.addButton("[+] Size", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                grid.setSize(grid.getSize() + 1);
            }
        });

        controlsRow.addButton("[-] Size", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (grid.getSize() > 1) {
                    grid.setSize(grid.getSize() - 1);
                }
            }
        });
        controlsRow.addButton("Change Foreground", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                grid.setForeground(StyleUtil.randomColor());
            }
        });
        controlsRow.addButton("Change Background", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                grid.setBackground(StyleUtil.randomColor());
            }
        });
        controlsRow.addButton("Change Border (All Attributes)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                grid.setBorder(StyleUtil.randomBorder());
            }
        });
        controlsRow.addButton("Change Border Color", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Border border = grid.getBorder();
                grid.setBorder(new Border(border.getSize(), StyleUtil.randomColor(), border.getStyle()));
            }
        });
        controlsRow.addButton("Change Border Size", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                grid.setBorder(StyleUtil.nextBorderSize(grid.getBorder()));
            }
        });
        controlsRow.addButton("Change Border Style", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                grid.setBorder(StyleUtil.nextBorderStyle(grid.getBorder()));
            }
        });
        
        controlsRow.addButton("Set Insets 0px", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                grid.setInsets(new Insets(0));
            }
        });
        controlsRow.addButton("Set Insets 2px", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                grid.setInsets(new Insets(2));
            }
        });
        controlsRow.addButton("Set Insets 10/5px", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                grid.setInsets(new Insets(10, 5));
            }
        });
        controlsRow.addButton("Set Insets 10/20/30/40px", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                grid.setInsets(new Insets(10, 20, 30, 40));
            }
        });
        
        controlsRow = new ButtonRow();
        controlsRow.add(new Label("Configure Cell"));
        groupContainerRow.add(controlsRow);
        
        controlsRow.addButton("[+] Column Span", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedButton != null) {
                    GridCellLayoutData layoutData = (GridCellLayoutData) selectedButton.getLayoutData();
                    layoutData.setColumnSpan(layoutData.getColumnSpan() + 1);
                    selectedButton.setLayoutData(layoutData);
                    retitle(selectedButton);
                }
            }
        });

        controlsRow.addButton("[-] Column Span", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedButton != null) {
                    GridCellLayoutData layoutData = (GridCellLayoutData) selectedButton.getLayoutData();
                    if (layoutData.getColumnSpan() > 1) {
                        layoutData.setColumnSpan(layoutData.getColumnSpan() - 1);
                    }
                    selectedButton.setLayoutData(layoutData);
                    retitle(selectedButton);
                }
            }
        });
        
        controlsRow.addButton("[+] Row Span", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedButton != null) {
                    GridCellLayoutData layoutData = (GridCellLayoutData) selectedButton.getLayoutData();
                    layoutData.setRowSpan(layoutData.getRowSpan() + 1);
                    selectedButton.setLayoutData(layoutData);
                    retitle(selectedButton);
                }
            }
        });

        controlsRow.addButton("[-] Row Span", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedButton != null) {
                    GridCellLayoutData layoutData = (GridCellLayoutData) selectedButton.getLayoutData();
                    if (layoutData.getRowSpan() > 1) {
                        layoutData.setRowSpan(layoutData.getRowSpan() - 1);
                    }
                    selectedButton.setLayoutData(layoutData);
                    retitle(selectedButton);
                }
            }
        });

        controlsRow.addButton("Set Insets 0px", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedButton != null) {
                    GridCellLayoutData layoutData = (GridCellLayoutData) selectedButton.getLayoutData();
                    layoutData.setInsets(new Insets(0));
                    selectedButton.setLayoutData(layoutData);
                }
            }
        });
        controlsRow.addButton("Set Insets 2px", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedButton != null) {
                    GridCellLayoutData layoutData = (GridCellLayoutData) selectedButton.getLayoutData();
                    layoutData.setInsets(new Insets(2));
                    selectedButton.setLayoutData(layoutData);
                }
            }
        });
        controlsRow.addButton("Set Insets 10/5px", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedButton != null) {
                    GridCellLayoutData layoutData = (GridCellLayoutData) selectedButton.getLayoutData();
                    layoutData.setInsets(new Insets(10, 5));
                    selectedButton.setLayoutData(layoutData);
                }
            }
        });
        controlsRow.addButton("Set Insets 10/20/30/40px", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedButton != null) {
                    GridCellLayoutData layoutData = (GridCellLayoutData) selectedButton.getLayoutData();
                    layoutData.setInsets(new Insets(10, 20, 30, 40));
                    selectedButton.setLayoutData(layoutData);
                }
            }
        });
        controlsRow.addButton("Toggle Line Wrap", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedButton != null) {
                    GridCellLayoutData layoutData = (GridCellLayoutData) selectedButton.getLayoutData();
                    layoutData.setLineWrap(!layoutData.isLineWrap());
                    selectedButton.setLayoutData(layoutData);
                }
            }
        });
    }

    public Button createGridCellButton() {
        Button button = new Button("Grid Cell #" + nextCellNumber++);
        GridCellLayoutData layoutData = new GridCellLayoutData();
        button.setLayoutData(layoutData);
        button.addActionListener(cellButtonActionListener);
        return button;
    }
    
    private void retitle(Button button) {
        StringBuffer out = new StringBuffer();
        GridCellLayoutData layoutData = (GridCellLayoutData) selectedButton.getLayoutData();
        if (layoutData.getColumnSpan() > 1 || layoutData.getRowSpan() > 1) {
            out.append("[" + layoutData.getColumnSpan() + "x" + layoutData.getRowSpan() + "]"); 
        }
        String text = button.getText();
        if (text.indexOf(":") == -1) {
            if (out.length() == 0) {
                return;
            }
            text = text + " : " + out;
        } else {
            if (out.length() == 0) {
                text = text.substring(0, text.indexOf(":"));
            } else {
                text = text.substring(0, text.indexOf(":") + 2) + out;
            }
        }
        button.setText(text);
    }
    
    private void selectCellButton(Button button) {
        GridCellLayoutData layoutData;
        if (selectedButton != null) {
            layoutData = (GridCellLayoutData) selectedButton.getLayoutData();
            layoutData.setBackground(null);
            selectedButton.setLayoutData(layoutData);
        }
        if (button != null) {
            layoutData = (GridCellLayoutData) button.getLayoutData();
            layoutData.setBackground(new Color(0xefefaf));
            button.setLayoutData(layoutData);
        }
        selectedButton = button;
    }
}
