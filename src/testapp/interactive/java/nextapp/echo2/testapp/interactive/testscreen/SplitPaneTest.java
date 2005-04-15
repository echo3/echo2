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

import nextapp.echo2.app.BackgroundImage;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.layout.SplitPaneLayoutData;
import nextapp.echo2.testapp.interactive.ButtonRow;
import nextapp.echo2.testapp.interactive.StyleUtil;
import nextapp.echo2.testapp.interactive.Styles;

/**
 * 
 */
public class SplitPaneTest extends SplitPane {
    
    private class PaneControlsRow extends ButtonRow {
        
        private PaneControlsRow(final int paneNumber) {
            add(new Label("Confgiure Pane #" + paneNumber));
    
            addButton("Fill With Text", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (testPane.getComponentCount() < paneNumber + 1) {
                        return;
                    }
                    Label label = (Label) testPane.getComponent(paneNumber);
                    label.setText(StyleUtil.QUASI_LATIN_TEXT_1);
                }
            });
            addButton("Change Background Color", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (testPane.getComponentCount() < paneNumber + 1) {
                        return;
                    }
                    SplitPaneLayoutData splitPaneLayoutData = getLayoutData(paneNumber);
                    splitPaneLayoutData.setBackground(StyleUtil.randomBrightColor());
                    testPane.getComponent(paneNumber).setLayoutData(splitPaneLayoutData);
                }
            });
            addButton("MIN Size = Default", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (testPane.getComponentCount() < paneNumber + 1) {
                        return;
                    }
                    SplitPaneLayoutData splitPaneLayoutData = getLayoutData(paneNumber);
                    splitPaneLayoutData.setMinimumSize(null);
                    testPane.getComponent(paneNumber).setLayoutData(splitPaneLayoutData);
                }
            });
            addButton("MIN Size = 30", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (testPane.getComponentCount() < paneNumber + 1) {
                        return;
                    }
                    SplitPaneLayoutData splitPaneLayoutData = getLayoutData(paneNumber);
                    splitPaneLayoutData.setMinimumSize(new Extent(30));
                    testPane.getComponent(paneNumber).setLayoutData(splitPaneLayoutData);
                }
            });
            addButton("MAX Size = Default", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (testPane.getComponentCount() < paneNumber + 1) {
                        return;
                    }
                    SplitPaneLayoutData splitPaneLayoutData = getLayoutData(paneNumber);
                    splitPaneLayoutData.setMaximumSize(null);
                    testPane.getComponent(paneNumber).setLayoutData(splitPaneLayoutData);
                }
            });
            addButton("MAX Size = 120", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (testPane.getComponentCount() < paneNumber + 1) {
                        return;
                    }
                    SplitPaneLayoutData splitPaneLayoutData = getLayoutData(paneNumber);
                    splitPaneLayoutData.setMaximumSize(new Extent(120));
                    testPane.getComponent(paneNumber).setLayoutData(splitPaneLayoutData);
                }
            });
            addButton("Toggle Background Image", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (testPane.getComponentCount() < paneNumber + 1) {
                        return;
                    }
                    SplitPaneLayoutData splitPaneLayoutData = getLayoutData(paneNumber);
                    BackgroundImage backgroundImage = splitPaneLayoutData.getBackgroundImage();
                    if (backgroundImage == null) {
                        splitPaneLayoutData.setBackgroundImage(Styles.BG_NW_SHADOW);
                    } else {
                        splitPaneLayoutData.setBackgroundImage(null);
                    }
                    testPane.getComponent(paneNumber).setLayoutData(splitPaneLayoutData);
                }
            });
            addButton("Insets = null", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (testPane.getComponentCount() < paneNumber + 1) {
                        return;
                    }
                    SplitPaneLayoutData splitPaneLayoutData = getLayoutData(paneNumber);
                    splitPaneLayoutData.setInsets(null);
                    testPane.getComponent(paneNumber).setLayoutData(splitPaneLayoutData);
                }
            });
            addButton("Insets = 0px", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (testPane.getComponentCount() < paneNumber + 1) {
                        return;
                    }
                    SplitPaneLayoutData splitPaneLayoutData = getLayoutData(paneNumber);
                    splitPaneLayoutData.setInsets(new Insets(0));
                    testPane.getComponent(paneNumber).setLayoutData(splitPaneLayoutData);
                }
            });
            addButton("Insets = 5px", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (testPane.getComponentCount() < paneNumber + 1) {
                        return;
                    }
                    SplitPaneLayoutData splitPaneLayoutData = getLayoutData(paneNumber);
                    splitPaneLayoutData.setInsets(new Insets(5));
                    testPane.getComponent(paneNumber).setLayoutData(splitPaneLayoutData);
                }
            });
            addButton("Insets = 10/20/30/40px", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (testPane.getComponentCount() < paneNumber + 1) {
                        return;
                    }
                    SplitPaneLayoutData splitPaneLayoutData = getLayoutData(paneNumber);
                    splitPaneLayoutData.setInsets(new Insets(10, 20, 30, 40));
                    testPane.getComponent(paneNumber).setLayoutData(splitPaneLayoutData);
                }
            });
            addButton("Overflow = Auto", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (testPane.getComponentCount() < paneNumber + 1) {
                        return;
                    }
                    SplitPaneLayoutData splitPaneLayoutData = getLayoutData(paneNumber);
                    splitPaneLayoutData.setOverflow(SplitPaneLayoutData.OVERFLOW_AUTO);
                    testPane.getComponent(paneNumber).setLayoutData(splitPaneLayoutData);
                }
            });
            addButton("Overflow = Hidden", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (testPane.getComponentCount() < paneNumber + 1) {
                        return;
                    }
                    SplitPaneLayoutData splitPaneLayoutData = getLayoutData(paneNumber);
                    splitPaneLayoutData.setOverflow(SplitPaneLayoutData.OVERFLOW_HIDDEN);
                    testPane.getComponent(paneNumber).setLayoutData(splitPaneLayoutData);
                }
            });
            addButton("Overflow = Scroll", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (testPane.getComponentCount() < paneNumber + 1) {
                        return;
                    }
                    SplitPaneLayoutData splitPaneLayoutData = getLayoutData(paneNumber);
                    splitPaneLayoutData.setOverflow(SplitPaneLayoutData.OVERFLOW_SCROLL);
                    testPane.getComponent(paneNumber).setLayoutData(splitPaneLayoutData);
                }
            });
        }
        
        private SplitPaneLayoutData getLayoutData(int paneNumber) {
            SplitPaneLayoutData splitPaneLayoutData = (SplitPaneLayoutData) testPane.getComponent(paneNumber).getLayoutData();
            if (splitPaneLayoutData == null) {
                splitPaneLayoutData = new SplitPaneLayoutData();
            }
            return splitPaneLayoutData;
        }
    }

    private SplitPane testPane;
    
    public SplitPaneTest() {
        super(SplitPane.ORIENTATION_HORIZONTAL, new Extent(250, Extent.PX));
        setResizable(true);
        
        Row groupContainerRow = new Row();
        groupContainerRow.setCellSpacing(new Extent(5));
        groupContainerRow.setStyleName(Styles.TEST_CONTROLS_ROW_STYLE_NAME);
        add(groupContainerRow);

        ButtonRow controlsRow;
        
        controlsRow = new ButtonRow();
        controlsRow.add(new Label("Add / Remove Panes"));
        groupContainerRow.add(controlsRow);
        
        controlsRow.addButton("Remove Pane 0", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (testPane.getComponentCount() >= 1) {
                    testPane.remove(0);
                }
            }
        });
        controlsRow.addButton("Remove Pane 1", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (testPane.getComponentCount() >= 2) {
                    testPane.remove(1);
                }
            }
        });
        controlsRow.addButton("Replace Pane 0", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (testPane.getComponentCount() >= 1) {
                    testPane.remove(0);
                }
                testPane.add(createPaneLabel("Replacement for Pane 0"), 0);
            }
        });
        controlsRow.addButton("Replace Pane 1", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (testPane.getComponentCount() >= 2) {
                    testPane.remove(1);
                }
                testPane.add(createPaneLabel("Replacement for Pane 1"));
            }
        });
        controlsRow.addButton("Add at Beginning", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (testPane.getComponentCount() < 2) {
                    testPane.add(createPaneLabel("Added at Beginning"), 0);
                }
            }
        });
        controlsRow.addButton("Add at End", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (testPane.getComponentCount() < 2) {
                    testPane.add(createPaneLabel("Added at End"));
                }
            }
        });
        
        controlsRow = new ButtonRow();
        controlsRow.add(new Label("Configure SplitPane"));
        groupContainerRow.add(controlsRow);
        
        controlsRow.addButton("Swap Orientation", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (testPane.getOrienation() == SplitPane.ORIENTATION_VERTICAL) {
                    testPane.setOrientation(SplitPane.ORIENTATION_HORIZONTAL);
                } else {
                    testPane.setOrientation(SplitPane.ORIENTATION_VERTICAL);
                }
            }
        });
        
        controlsRow.addButton("Disable Resize", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testPane.setResizable(!testPane.isResizable());
                ((Button) e.getSource()).setText(testPane.isResizable() ? "Disable Resize" : "Enable Resize");
            }
        });
        
        groupContainerRow.add(new PaneControlsRow(0));
        groupContainerRow.add(new PaneControlsRow(1));

        testPane = new SplitPane(ORIENTATION_VERTICAL, new Extent(200, Extent.PX));
        testPane.setResizable(true);
        add(testPane);
    }
    
    private Label createPaneLabel(String text) {
        Label label = new Label(text);
        SplitPaneLayoutData splitPaneLayoutData = new SplitPaneLayoutData();
        splitPaneLayoutData.setBackground(StyleUtil.randomBrightColor());
        label.setLayoutData(splitPaneLayoutData);
        return label;
    }
}
