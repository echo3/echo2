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

import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.WindowPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.testapp.interactive.ButtonColumn;
import nextapp.echo2.testapp.interactive.InteractiveApp;
import nextapp.echo2.testapp.interactive.StyleUtil;

/**
 * Interactive test module for <code>ContentPane</code>s.
 */
public class ContentPaneTest extends SplitPane {

    public ContentPaneTest() {
        super(SplitPane.ORIENTATION_HORIZONTAL, new Extent(250, Extent.PX));
        setStyleName("DefaultResizable");
        
        final ContentPane rootContentPane = InteractiveApp.getApp().getDefaultWindow().getContent();
        final Label contentLabel = new Label(StyleUtil.QUASI_LATIN_TEXT_1 + StyleUtil.QUASI_LATIN_TEXT_1);
        
        ButtonColumn controlsColumn = new ButtonColumn();
        controlsColumn.setStyleName("TestControlsColumn");
        add(controlsColumn);
        
        final ContentPane testContentPane = new ContentPane();
        add(testContentPane);

        controlsColumn.add(new Label("Root Content Pane"));

        controlsColumn.addButton("Reset", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rootContentPane.setBackground(null);
                rootContentPane.setForeground(null);
                rootContentPane.setFont(null);
            }
        });
        controlsColumn.addButton("Change Background", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rootContentPane.setBackground(StyleUtil.randomColor());
            }
        });
        controlsColumn.addButton("Change Foreground", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rootContentPane.setForeground(StyleUtil.randomColor());
            }
        });
        controlsColumn.addButton("Change Font", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rootContentPane.setFont(StyleUtil.randomFont());
            }
        });
        
        controlsColumn.add(new Label("Test Content Pane"));
        
        controlsColumn.addButton("Reset", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testContentPane.setBackground(null);
                testContentPane.setForeground(null);
                testContentPane.setFont(null);
            }
        });
        controlsColumn.addButton("Change Background", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testContentPane.setBackground(StyleUtil.randomColor());
            }
        });
        controlsColumn.addButton("Change Foreground", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testContentPane.setForeground(StyleUtil.randomColor());
            }
        });
        controlsColumn.addButton("Change Font", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testContentPane.setFont(StyleUtil.randomFont());
            }
        });
        
        controlsColumn.addButton("Add Label", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (testContentPane.indexOf(contentLabel) == -1) {
                    testContentPane.add(contentLabel);
                }
            }
        });
        controlsColumn.addButton("Add WindowPane", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testContentPane.add(new WindowPane());
            }
        });
        
        controlsColumn.addButton("Set Horizontal Scroll = null", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testContentPane.setHorizontalScroll(null);
            }
        });
        controlsColumn.addButton("Set Horizontal Scroll = 0", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testContentPane.setHorizontalScroll(new Extent(0));
            }
        });
        controlsColumn.addButton("Set Horizontal Scroll = 50", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testContentPane.setHorizontalScroll(new Extent(50));
            }
        });
        controlsColumn.addButton("Set Horizontal Scroll = 100", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testContentPane.setHorizontalScroll(new Extent(100));
            }
        });

        controlsColumn.addButton("Set Vertical Scroll = null", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testContentPane.setVerticalScroll(null);
            }
        });
        controlsColumn.addButton("Set Vertical Scroll = 0", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testContentPane.setVerticalScroll(new Extent(0));
            }
        });
        controlsColumn.addButton("Set Vertical Scroll = 50", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testContentPane.setVerticalScroll(new Extent(50));
            }
        });
        controlsColumn.addButton("Set Vertical Scroll = 100", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testContentPane.setVerticalScroll(new Extent(100));
            }
        });
    }
}
