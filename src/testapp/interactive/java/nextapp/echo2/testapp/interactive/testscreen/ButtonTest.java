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

import java.util.ArrayList;
import java.util.List;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.RadioButton;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.button.AbstractButton;
import nextapp.echo2.app.button.ToggleButton;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.layout.GridCellLayoutData;
import nextapp.echo2.app.layout.SplitPaneLayoutData;
import nextapp.echo2.testapp.interactive.ButtonColumn;
import nextapp.echo2.testapp.interactive.StyleUtil;
import nextapp.echo2.testapp.interactive.Styles;
import nextapp.echo2.testapp.interactive.TestGrid;

/**
 * 
 */
public class ButtonTest 
extends SplitPane {

    private List buttonList;
    
    private interface Applicator {
        
        public void apply(AbstractButton button);
    }
    
    public ButtonTest() {
        super(SplitPane.ORIENTATION_HORIZONTAL, new Extent(250, Extent.PX));
        setResizable(true);

        SplitPaneLayoutData splitPaneLayoutData;
        
        Column controlGroupsColumn = new Column();
        controlGroupsColumn.setCellSpacing(new Extent(5));
        controlGroupsColumn.setStyleName(Styles.TEST_CONTROLS_COLUMN_STYLE_NAME);
        add(controlGroupsColumn);

        TestGrid testGrid = new TestGrid();
        splitPaneLayoutData = new SplitPaneLayoutData();
        splitPaneLayoutData.setInsets(new Insets(15));
        testGrid.setLayoutData(splitPaneLayoutData);
        add(testGrid);
        
        Label warningLabel = new Label("Note: ToggleButtons are under development and mostly non-functional.");
        warningLabel.setForeground(Color.YELLOW);
        GridCellLayoutData gridCellLayoutData = new GridCellLayoutData();
        gridCellLayoutData.setBackground(Color.RED);
        gridCellLayoutData.setColumnSpan(2);
        warningLabel.setLayoutData(gridCellLayoutData);
        testGrid.add(warningLabel);

        buttonList = new ArrayList();
        
        Button button;
        testGrid.addHeaderCell("Button");

        button = new Button();
        testGrid.addTestCell("No Content", button);
        buttonList.add(button);

        button = new Button("Test Button");
        testGrid.addTestCell("Text", button);
        buttonList.add(button);
        
        button = new Button(Styles.ICON_LOGO);
        testGrid.addTestCell("Icon", button);
        buttonList.add(button);
        
        button = new Button("Test Button", Styles.ICON_LOGO);
        testGrid.addTestCell("Text and Icon", button);
        buttonList.add(button);
        
        CheckBox checkBox;
        testGrid.addHeaderCell("CheckBox");

        checkBox = new CheckBox();
        testGrid.addTestCell("No Content", checkBox);
        buttonList.add(checkBox);

        checkBox = new CheckBox("Test CheckBox");
        testGrid.addTestCell("Text", checkBox);
        buttonList.add(checkBox);
        
        checkBox = new CheckBox(Styles.ICON_LOGO);
        testGrid.addTestCell("Icon", checkBox);
        buttonList.add(checkBox);
        
        checkBox = new CheckBox("Test CheckBox", Styles.ICON_LOGO);
        testGrid.addTestCell("Text and Icon", checkBox);
        buttonList.add(checkBox);
        
        RadioButton radioButton;
        testGrid.addHeaderCell("RadioButton");

        radioButton = new RadioButton();
        testGrid.addTestCell("No Content", radioButton);
        buttonList.add(radioButton);

        radioButton = new RadioButton("Test RadioButton");
        testGrid.addTestCell("Text", radioButton);
        buttonList.add(radioButton);
        
        radioButton = new RadioButton(Styles.ICON_LOGO);
        testGrid.addTestCell("Icon", radioButton);
        buttonList.add(radioButton);
        
        radioButton = new RadioButton("Test RadioButton", Styles.ICON_LOGO);
        testGrid.addTestCell("Text and Icon", radioButton);
        buttonList.add(radioButton);
    
/*        
        controlsColumn.addButton("Toggle Container Cell Spacing", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (testColumn.getCellSpacing() == null) {
                    testColumn.setCellSpacing(new Extent(15));
                } else {
                    testColumn.setCellSpacing(null);
                }
            }
        });
*/      

        ButtonColumn  controlsColumn;
        
        // Create 'AbstractButton Controls Group'
        
        controlsColumn = new ButtonColumn();
        controlGroupsColumn.add(controlsColumn);
        
        controlsColumn.add(new Label("AbstractButton Controls"));
        
        controlsColumn.addButton("Set Foreground", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final Color color = StyleUtil.randomColor();
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setForeground(color);
                    }
                });
            }
        });
        controlsColumn.addButton("Clear Foreground", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setForeground(null);
                    }
                });
            }
        });
        controlsColumn.addButton("Set Background", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final Color color = StyleUtil.randomColor();
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setBackground(color);
                    }
                });
            }
        });
        controlsColumn.addButton("Clear Background", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setBackground(null);
                    }
                });
            }
        });
        controlsColumn.addButton("Set Font", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final Font font = StyleUtil.randomFont();
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setFont(font);
                    }
                });
            }
        });
        controlsColumn.addButton("Clear Font", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setFont(null);
                    }
                });
            }
        });
        controlsColumn.addButton("Set StyleName = Null", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setStyleName(null);
                    }
                });
            }
        });
        controlsColumn.addButton("Set StyleName = Default", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setStyleName(Styles.DEFAULT_STYLE_NAME);
                    }
                });
            }
        });
        controlsColumn.addButton("TextPosition = Default", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setTextPosition(null);
                    }
                });
            }
        });
        controlsColumn.addButton("TextPosition = Top", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setTextPosition(new Alignment(Alignment.DEFAULT, Alignment.TOP));
                    }
                });
            }
        });
        controlsColumn.addButton("TextPosition = Bottom", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setTextPosition(new Alignment(Alignment.DEFAULT, Alignment.BOTTOM));
                    }
                });
            }
        });
        controlsColumn.addButton("TextPosition = Left", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setTextPosition(new Alignment(Alignment.LEFT, Alignment.DEFAULT));
                    }
                });
            }
        });
        controlsColumn.addButton("TextPosition = Right", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setTextPosition(new Alignment(Alignment.RIGHT, Alignment.DEFAULT));
                    }
                });
            }
        });
        controlsColumn.addButton("TextAlignment = Default", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setTextAlignment(null);
                    }
                });
            }
        });
        controlsColumn.addButton("TextAlignment = Top", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setTextAlignment(new Alignment(Alignment.DEFAULT, Alignment.TOP));
                    }
                });
            }
        });
        controlsColumn.addButton("TextAlignment = Center (V)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setTextAlignment(new Alignment(Alignment.DEFAULT, Alignment.CENTER));
                    }
                });
            }
        });
        controlsColumn.addButton("TextAlignment = Bottom", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setTextAlignment(new Alignment(Alignment.DEFAULT, Alignment.BOTTOM));
                    }
                });
            }
        });
        controlsColumn.addButton("TextAlignment = Left", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setTextAlignment(new Alignment(Alignment.LEFT, Alignment.DEFAULT));
                    }
                });
            }
        });
        controlsColumn.addButton("TextAlignment = Center (H)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setTextAlignment(new Alignment(Alignment.CENTER, Alignment.DEFAULT));
                    }
                });
            }
        });
        controlsColumn.addButton("TextAlignment = Right", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setTextAlignment(new Alignment(Alignment.RIGHT, Alignment.DEFAULT));
                    }
                });
            }
        });
        controlsColumn.addButton("IconTextMargin = default", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setIconTextMargin(null);
                    }
                });
            }
        });
        controlsColumn.addButton("IconTextMargin = 0px", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setIconTextMargin(new Extent(0));
                    }
                });
            }
        });
        controlsColumn.addButton("IconTextMargin = 10px", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setIconTextMargin(new Extent(10));
                    }
                });
            }
        });
        controlsColumn.addButton("IconTextMargin = 1in", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setIconTextMargin(new Extent(1, Extent.IN));
                    }
                });
            }
        });

        // Create 'ToggleButton Controls Group'
        
        controlsColumn = new ButtonColumn();
        controlGroupsColumn.add(controlsColumn);
        
        controlsColumn.add(new Label("ToggleButton Controls"));
        
        controlsColumn.addButton("Selected = False", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        if (button instanceof ToggleButton) {
                            ((ToggleButton) button).setSelected(false);
                        }
                    }
                });
            }
        });
        controlsColumn.addButton("Selected = True", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        if (button instanceof ToggleButton) {
                            ((ToggleButton) button).setSelected(true);
                        }
                    }
                });
            }
        });
        controlsColumn.addButton("StatePosition = Default", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        if (button instanceof ToggleButton) {
                            ((ToggleButton) button).setStatePosition(null);
                        }
                    }
                });
            }
        });
        controlsColumn.addButton("StatePosition = Top", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        if (button instanceof ToggleButton) {
                            ((ToggleButton) button).setStatePosition(new Alignment(Alignment.DEFAULT, Alignment.TOP));
                        }
                    }
                });
            }
        });
        controlsColumn.addButton("StatePosition = Bottom", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        if (button instanceof ToggleButton) {
                            ((ToggleButton) button).setStatePosition(new Alignment(Alignment.DEFAULT, Alignment.BOTTOM));
                        }
                    }
                });
            }
        });
        controlsColumn.addButton("StatePosition = Left", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        if (button instanceof ToggleButton) {
                            ((ToggleButton) button).setStatePosition(new Alignment(Alignment.LEFT, Alignment.DEFAULT));
                        }
                    }
                });
            }
        });
        controlsColumn.addButton("StatePosition = Right", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        if (button instanceof ToggleButton) {
                            ((ToggleButton) button).setStatePosition(new Alignment(Alignment.RIGHT, Alignment.DEFAULT));
                        }
                    }
                });
            }
        });
        controlsColumn.addButton("StateAlignment = Default", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        if (button instanceof ToggleButton) {
                            ((ToggleButton) button).setStateAlignment(null);
                        }
                    }
                });
            }
        });
        controlsColumn.addButton("StateAlignment = Top", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        if (button instanceof ToggleButton) {
                            ((ToggleButton) button).setStateAlignment(new Alignment(Alignment.DEFAULT, Alignment.TOP));
                        }
                    }
                });
            }
        });
        controlsColumn.addButton("StateAlignment = Center (V)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        if (button instanceof ToggleButton) {
                            ((ToggleButton) button).setStateAlignment(new Alignment(Alignment.DEFAULT, Alignment.CENTER));
                        }
                    }
                });
            }
        });
        controlsColumn.addButton("StateAlignment = Bottom", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        if (button instanceof ToggleButton) {
                            ((ToggleButton) button).setStateAlignment(new Alignment(Alignment.DEFAULT, Alignment.BOTTOM));
                        }
                    }
                });
            }
        });
        controlsColumn.addButton("StateAlignment = Left", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        if (button instanceof ToggleButton) {
                            ((ToggleButton) button).setStateAlignment(new Alignment(Alignment.LEFT, Alignment.DEFAULT));
                        }
                    }
                });
            }
        });
        controlsColumn.addButton("StateAlignment = Center (H)", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        if (button instanceof ToggleButton) {
                            ((ToggleButton) button).setStateAlignment(new Alignment(Alignment.CENTER, Alignment.DEFAULT));
                        }
                    }
                });
            }
        });
        controlsColumn.addButton("StateAlignment = Right", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        if (button instanceof ToggleButton) {
                            ((ToggleButton) button).setStateAlignment(new Alignment(Alignment.RIGHT, Alignment.DEFAULT));
                        }
                    }
                });
            }
        });
    }
    
    public void apply(Applicator applicator) {
        AbstractButton[] buttons = (AbstractButton[]) buttonList.toArray(new AbstractButton[buttonList.size()]);
        for (int i = 0; i < buttons.length; ++i) {
            applicator.apply(buttons[i]);
        }
    }
}
