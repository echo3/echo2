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

package nextapp.echo2.testapp.interactive;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

/**
 * Main InteractiveTest <code>ContentPane</code> which displays a menu
 * of available tests.
 */
public class TestPane extends ContentPane {

    private SplitPane horizontalPane;
    
    private ActionListener commandActionListener = new ActionListener() {
        
        private Button activeButton = null;
        
        /**
         * @see nextapp.echo2.app.event.ActionListener#actionPerformed(nextapp.echo2.app.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            try {
                if (activeButton != null) {
                    activeButton.setStyleName("Default");
                }
                Button button = (Button) e.getSource();
                button.setStyleName("Selected");
                activeButton = button;
                String screenClassName = "nextapp.echo2.testapp.interactive.testscreen." + e.getActionCommand();
                Class screenClass = Class.forName(screenClassName);
                Component content = (Component) screenClass.newInstance();
                if (horizontalPane.getComponentCount() > 1) {
                    horizontalPane.remove(1);
                }
                horizontalPane.add(content);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex.toString());
            } catch (InstantiationException ex) {
                throw new RuntimeException(ex.toString());
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex.toString());
            }
        }
    };
    
    private Column testLaunchButtonsColumn;
    
    public TestPane() {
        super();
        
        SplitPane verticalPane = new SplitPane(SplitPane.ORIENTATION_VERTICAL);
        verticalPane.setStyleName("TestPane");
        add(verticalPane);

        Label titleLabel = new Label("NextApp Echo2 Test Application");
        titleLabel.setStyleName("TitleLabel");
        verticalPane.add(titleLabel);
        
        horizontalPane = new SplitPane(SplitPane.ORIENTATION_HORIZONTAL, new Extent(215));
        horizontalPane.setStyleName("DefaultResizable");
        verticalPane.add(horizontalPane);
        
        Column controlsColumn = new Column();
        controlsColumn.setStyleName("ApplicationControlsColumn");
        controlsColumn.setCellSpacing(new Extent(5));
        
        horizontalPane.add(controlsColumn);
        
        testLaunchButtonsColumn = new Column();
        controlsColumn.add(testLaunchButtonsColumn);

        addTest("Button", "ButtonTest");
        addTest("Client Configuration", "ClientConfigurationTest");
        addTest("Column", "ColumnTest");
        addTest("Command", "CommandTest");
        addTest("Container Context", "ContainerContextTest");
        addTest("ContentPane", "ContentPaneTest");
        addTest("Delay", "DelayTest");
        addTest("Exception", "ExceptionTest");
        addTest("Grid", "GridTest");
        addTest("Hierarchy", "HierarchyTest");
        addTest("Image", "ImageReferenceTest");
        addTest("Label", "LabelTest");
        addTest("ListBox", "ListBoxTest");
        addTest("Localization", "LocalizationTest");
        addTest("Push (Basic)", "PushTest");
        addTest("Push (Ghost Test)", "PushGhostTest");
        addTest("Random Click", "RandomClickTest");
        addTest("Row", "RowTest");
        addTest("SplitPane (Basic)", "SplitPaneTest");
        addTest("SplitPane (Nested)", "SplitPaneNestedTest");
        addTest("StyleSheet", "StyleSheetTest");
        addTest("Table", "TableTest");
        addTest("TextComponent", "TextComponentTest");
        addTest("Visibility", "VisibilityTest");
        addTest("Window", "WindowTest");
        addTest("WindowPane", "WindowPaneTest");
        
        Column applicationControlsColumn = new Column();
        controlsColumn.add(applicationControlsColumn);

        Button button = new Button("Exit");
        button.setStyleName("Default");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                InteractiveApp.getApp().displayWelcomePane();
            }
        });
        applicationControlsColumn.add(button);
    }
    
    private void addTest(String name, String action) {
        Button button = new Button(name);
        button.setActionCommand(action);
        button.setStyleName("Default");
        button.addActionListener(commandActionListener);
        testLaunchButtonsColumn.add(button);
    }
}
