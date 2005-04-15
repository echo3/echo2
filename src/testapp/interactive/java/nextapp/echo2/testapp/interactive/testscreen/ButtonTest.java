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

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.button.AbstractButton;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.layout.SplitPaneLayoutData;
import nextapp.echo2.testapp.interactive.ButtonRow;
import nextapp.echo2.testapp.interactive.StyleUtil;
import nextapp.echo2.testapp.interactive.Styles;

/**
 * 
 */
public class ButtonTest 
extends SplitPane {

    private interface Applicator {
        
        public void apply(AbstractButton button);
    }
    
    private Row testRow;

    public ButtonTest() {
        super(SplitPane.ORIENTATION_HORIZONTAL, new Extent(250, Extent.PX));
        setResizable(true);

        SplitPaneLayoutData splitPaneLayoutData;
        
        ButtonRow controlsRow = new ButtonRow();
        controlsRow.setStyleName(Styles.TEST_CONTROLS_ROW_STYLE_NAME);
        add(controlsRow);

        testRow = new Row();
        testRow.setCellSpacing(new Extent(15));
        splitPaneLayoutData = new SplitPaneLayoutData();
        splitPaneLayoutData.setInsets(new Insets(15));
        testRow.setLayoutData(splitPaneLayoutData);
        add(testRow);
        
        addButtons();
        
        controlsRow.addButton("Toggle Container Cell Spacing", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (testRow.getCellSpacing() == null) {
                    testRow.setCellSpacing(new Extent(15));
                } else {
                    testRow.setCellSpacing(null);
                }
            }
        });
        controlsRow.addButton("Set Foreground", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final Color color = StyleUtil.randomColor();
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setForeground(color);
                    }
                });
            }
        });
        controlsRow.addButton("Clear Foreground", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setForeground(null);
                    }
                });
            }
        });
        controlsRow.addButton("Set Background", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final Color color = StyleUtil.randomColor();
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setBackground(color);
                    }
                });
            }
        });
        controlsRow.addButton("Clear Background", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setBackground(null);
                    }
                });
            }
        });
        controlsRow.addButton("Set Font", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final Font font = StyleUtil.randomFont();
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setFont(font);
                    }
                });
            }
        });
        controlsRow.addButton("Clear Font", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setFont(null);
                    }
                });
            }
        });
        
        controlsRow.addButton("Set StyleName = Null", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setStyleName(null);
                    }
                });
            }
        });
        
        controlsRow.addButton("Set StyleName = Default", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply(new Applicator() {
                    public void apply(AbstractButton button) {
                        button.setStyleName(Styles.DEFAULT_STYLE_NAME);
                    }
                });
            }
        });

    }
    
    private void addButtons() {
        testRow.removeAll();
        testRow.add(new Button("Test Button"));
        testRow.add(new Button(Styles.ICON_LOGO));
//        testRow.add(new Button("Test Button", Styles.ICON_LOGO));
    }
    
    public void apply(Applicator applicator) {
        Component[] components = testRow.getComponents();
        for (int i = 0; i < components.length; ++i) {
            applicator.apply((AbstractButton) components[i]);
        }
    }
}
