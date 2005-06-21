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

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.WindowPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.layout.SplitPaneLayoutData;
import nextapp.echo2.testapp.interactive.ButtonColumn;
import nextapp.echo2.testapp.interactive.InteractiveApp;
import nextapp.echo2.testapp.interactive.StyleUtil;
import nextapp.echo2.testapp.interactive.Styles;

public class WindowPaneTest extends SplitPane {
    
    private int windowNumber = 0;
    
    private int nextPosition = 0;
    
    private class WindowTestControls extends ButtonColumn {
        
        private WindowTestControls(String targetName, final ContentPane targetContentPane) {
            add(new Label(targetName));
            addButton("Add Label Window", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    targetContentPane.add(createSimpleWindow("Simple"));
                }
            });
            addButton("Add Modal Window", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    WindowPane windowPane = createTestWindow("Modal");
                    windowPane.setModal(true);
                    targetContentPane.add(windowPane);
                }
            });
            addButton("Add Three Modal Windows", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    for (int i = 0; i < 3; ++i) {
                        WindowPane windowPane = createTestWindow("3Modal");
                        windowPane.setModal(true);
                        targetContentPane.add(windowPane);
                    }
                }
            });
            addButton("Add Constrained Size Window", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    WindowPane windowPane = createSimpleWindow("Constrained");
                    windowPane.setMinimumWidth(new Extent(400));
                    windowPane.setMaximumWidth(new Extent(500));
                    windowPane.setMinimumHeight(new Extent(200));
                    windowPane.setMaximumHeight(new Extent(280));
                    targetContentPane.add(windowPane);
                }
            });
            addButton("Add Default-Border Window", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    final WindowPane windowPane = new WindowPane();
                    positionWindowPane(windowPane);
                    windowPane.setTitle("Default-Border Window #" + windowNumber++);
                    targetContentPane.add(windowPane);
                    
                    Column windowPaneColumn = new Column();
                    windowPane.add(windowPaneColumn);
                    windowPaneColumn.add(new Label("First Name:"));
                    windowPaneColumn.add(new TextField());
                    windowPaneColumn.add(new Label("Last Name:"));
                    windowPaneColumn.add(new TextField());
                }
            });
            addButton("Add Immovable Window", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    WindowPane windowPane = createSimpleWindow("Immovable");
                    windowPane.setMovable(false);
                    targetContentPane.add(windowPane);
                }
            });
            addButton("Add Fixed Size Window", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    WindowPane windowPane = createSimpleWindow("Fixed Size");
                    windowPane.setResizable(false);
                    targetContentPane.add(windowPane);
                }
            });
            addButton("Add Immovable Fixed Size Window", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    WindowPane windowPane = createSimpleWindow("Immovable Fixed Size");
                    windowPane.setMovable(false);
                    windowPane.setResizable(false);
                    targetContentPane.add(windowPane);
                }
            });
            addButton("Add SplitPane Window", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    final WindowPane windowPane = new WindowPane();
                    positionWindowPane(windowPane);
                    targetContentPane.add(windowPane);
                    windowPane.setTitle("SplitPane Window #" + windowNumber++);
                    windowPane.setTitleInsets(new Insets(10, 5));
                    windowPane.setStyleName("default");
                    windowPane.setTitleBackground(new Color(0x2f2f4f));
                    windowPane.setWidth(new Extent(500, Extent.PX));
                    windowPane.setHeight(new Extent(300, Extent.PX));
                    SplitPane splitPane = new SplitPane(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP, new Extent(42));
                    SplitPaneLayoutData splitPaneLayoutData;
                    
                    Button okButton = new Button("Ok");
                    okButton.addActionListener(new ActionListener() {
                        /**
                         * @see nextapp.echo2.app.event.ActionListener#actionPerformed(nextapp.echo2.app.event.ActionEvent)
                         */
                        public void actionPerformed(ActionEvent e) {
                            windowPane.getParent().remove(windowPane);
                        }
                    });
                    splitPaneLayoutData = new SplitPaneLayoutData();
                    splitPaneLayoutData.setBackground(new Color(0x5f5f9f));
                    splitPaneLayoutData.setInsets(new Insets(8));
                    splitPaneLayoutData.setAlignment(new Alignment(Alignment.CENTER, Alignment.DEFAULT));
                    splitPaneLayoutData.setOverflow(SplitPaneLayoutData.OVERFLOW_HIDDEN);
                    okButton.setLayoutData(splitPaneLayoutData);
                    okButton.setWidth(new Extent(100));
                    okButton.setStyleName(Styles.DEFAULT_STYLE_NAME);
                    splitPane.add(okButton);
                    
                    Label contentLabel = new Label(StyleUtil.QUASI_LATIN_TEXT_1);
                    splitPaneLayoutData = new SplitPaneLayoutData();
                    splitPaneLayoutData.setBackground(new Color(0xefefff));
                    contentLabel.setLayoutData(splitPaneLayoutData);
                    splitPane.add(contentLabel);
                    
                    windowPane.add(splitPane);
                }
            });
    
            addButton("Add Multiple SplitPane Window", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    final WindowPane windowPane = new WindowPane();
                    positionWindowPane(windowPane);
                    targetContentPane.add(windowPane);
                    windowPane.setTitle("Multiple SplitPane Window #" + windowNumber++);
                    windowPane.setTitleInsets(new Insets(10, 5));
                    windowPane.setStyleName("default");
                    windowPane.setTitleBackground(new Color(0x2f2f4f));
                    windowPane.setWidth(new Extent(700, Extent.PX));
                    windowPane.setWidth(new Extent(500, Extent.PX));
                    
                    SplitPane splitPane1 = new SplitPane(SplitPane.ORIENTATION_HORIZONTAL, new Extent(100));
                    splitPane1.setStyleName("defaultResizable");
                    SplitPaneLayoutData splitPaneLayoutData;
                    
                    Label label;
                    
                    label = new Label(StyleUtil.QUASI_LATIN_TEXT_1);
                    splitPaneLayoutData = new SplitPaneLayoutData();
                    splitPaneLayoutData.setBackground(new Color(0x3fbf5f));
                    splitPaneLayoutData.setInsets(new Insets(5));
                    label.setLayoutData(splitPaneLayoutData);
                    splitPane1.add(label);

                    SplitPane splitPane2 = new SplitPane(SplitPane.ORIENTATION_VERTICAL, new Extent(120));
                    splitPane2.setStyleName("defaultResizable");
                    
                    SplitPane splitPane3 = new SplitPane(SplitPane.ORIENTATION_HORIZONTAL, new Extent(200));
                    splitPane3.setStyleName("defaultResizable");
                    splitPane2.add(splitPane3);
                    
                    SplitPane splitPane4 = new SplitPane(SplitPane.ORIENTATION_HORIZONTAL, new Extent(300));
                    splitPane4.setStyleName("defaultResizable");
                    splitPane2.add(splitPane4);
                    
                    label = new Label(StyleUtil.QUASI_LATIN_TEXT_1);
                    splitPaneLayoutData = new SplitPaneLayoutData();
                    splitPaneLayoutData.setBackground(new Color(0x5f3fbf));
                    splitPaneLayoutData.setInsets(new Insets(5));
                    label.setLayoutData(splitPaneLayoutData);
                    splitPane3.add(label);
                    
                    label = new Label(StyleUtil.QUASI_LATIN_TEXT_1);
                    splitPaneLayoutData = new SplitPaneLayoutData();
                    splitPaneLayoutData.setBackground(new Color(0x3f5fbf));
                    splitPaneLayoutData.setInsets(new Insets(5));
                    label.setLayoutData(splitPaneLayoutData);
                    splitPane3.add(label);
                    
                    label = new Label(StyleUtil.QUASI_LATIN_TEXT_1);
                    splitPaneLayoutData = new SplitPaneLayoutData();
                    splitPaneLayoutData.setBackground(new Color(0xbf5f3f));
                    splitPaneLayoutData.setInsets(new Insets(5));
                    label.setLayoutData(splitPaneLayoutData);
                    splitPane4.add(label);
                    
                    label = new Label(StyleUtil.QUASI_LATIN_TEXT_1);
                    splitPaneLayoutData = new SplitPaneLayoutData();
                    splitPaneLayoutData.setBackground(new Color(0xbf3f5f));
                    splitPaneLayoutData.setInsets(new Insets(5));
                    label.setLayoutData(splitPaneLayoutData);
                    splitPane4.add(label);
    
                    splitPane1.add(splitPane2);
                    
                    windowPane.add(splitPane1);
                }
            });
        }
    }
    
    public WindowPaneTest() {
        super(SplitPane.ORIENTATION_HORIZONTAL, new Extent(250, Extent.PX));
        setStyleName("defaultResizable");
        
        ButtonColumn controlsColumn = new ButtonColumn();
        controlsColumn.setCellSpacing(new Extent(5));
        controlsColumn.setStyleName(Styles.TEST_CONTROLS_COLUMN_STYLE_NAME);
        add(controlsColumn);
        
        final ContentPane contentPane = new ContentPane();
        add(contentPane);

        WindowTestControls windowTestControls;
        windowTestControls = new WindowTestControls("Root Level", InteractiveApp.getApp().getMainWindow().getContent());
        controlsColumn.add(windowTestControls);
        windowTestControls = new WindowTestControls("Embedded", contentPane);
        controlsColumn.add(windowTestControls);
    }
    
    private WindowPane createSimpleWindow(String name) {
        WindowPane windowPane = new WindowPane();
        positionWindowPane(windowPane);
        windowPane.setTitle(name + " Window #" + windowNumber++);
        windowPane.setTitleInsets(new Insets(10, 5));
        windowPane.setTitleBackground(new Color(0x2f2f4f));
        windowPane.setInsets(new Insets(10));
        windowPane.setWidth(new Extent(500));
        windowPane.setHeight(new Extent(280));
        windowPane.setStyleName("default");
        windowPane.add(new Label(StyleUtil.QUASI_LATIN_TEXT_1));
        return windowPane;
    }
    
    private WindowPane createTestWindow(String name) {
        final WindowPane windowPane = new WindowPane();
        positionWindowPane(windowPane);
        windowPane.setTitle(name + " Window #" + windowNumber++);
        windowPane.setTitleInsets(new Insets(10, 5));
        windowPane.setTitleBackground(new Color(0x2f2f4f));
        windowPane.setInsets(new Insets(10));
        windowPane.setWidth(new Extent(500));
        windowPane.setHeight(new Extent(280));
        windowPane.setStyleName("default");
        
        ButtonColumn column = new ButtonColumn();
        column.addButton("Add Modal Window", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ContentPane contentPane = (ContentPane) windowPane.getParent();
                WindowPane newWindowPane = createTestWindow("YetAnotherModal");
                newWindowPane.setModal(true);
                contentPane.add(newWindowPane);
            }
        });
        windowPane.add(column);
        return windowPane;
    }
    
    private void positionWindowPane(WindowPane windowPane) {
        Extent positionExtent = new Extent(nextPosition, Extent.PX);
        windowPane.setPositionX(positionExtent);
        windowPane.setPositionY(positionExtent);
        nextPosition += 20;
        if (nextPosition > 200) {
            nextPosition = 0;
        }
    }
}
