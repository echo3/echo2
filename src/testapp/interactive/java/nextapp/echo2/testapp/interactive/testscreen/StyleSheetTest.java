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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.StyleSheet;
import nextapp.echo2.app.TextArea;
import nextapp.echo2.app.WindowPane;
import nextapp.echo2.app.componentxml.ComponentXmlException;
import nextapp.echo2.app.componentxml.StyleSheetLoader;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.layout.SplitPaneLayoutData;
import nextapp.echo2.testapp.interactive.InteractiveApp;
import nextapp.echo2.testapp.interactive.Styles;

/**
 * A test for <code>StyleSheet</code>s.
 */
public class StyleSheetTest extends Column {
    
    private static final String DEFAULT_STYLE_SHEET_TEXT;
    static {
        InputStream in = null;
        try {
            in = StyleSheetTest.class.getResourceAsStream(Styles.STYLE_PATH + "Default.stylesheet");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuffer out = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
                out.append("\n");
            }
            DEFAULT_STYLE_SHEET_TEXT = out.toString();
        } catch (IOException ex) {
            throw new RuntimeException(ex.toString());
        } finally {
            if (in != null) { try { in.close(); } catch (IOException ex) { } }
        }
    }
    
    private TextArea styleSheetEntryTextArea;
    
    public StyleSheetTest() {
        super();
        
        SplitPaneLayoutData splitPaneLayoutData = new SplitPaneLayoutData();
        splitPaneLayoutData.setInsets(new Insets(10));
        setLayoutData(splitPaneLayoutData);
        setCellSpacing(new Extent(20));
        
        Column controlsColumn = new Column();
        add(controlsColumn);
        
        Button defaultButton = new Button("Slate Blue Style Sheet (DEFAULT)");
        defaultButton.setStyleName("Default");
        defaultButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getApplicationInstance().setStyleSheet(Styles.DEFAULT_STYLE_SHEET);
            }
        });
        controlsColumn.add(defaultButton);
        
        Button greenButton = new Button("Forest Green Style Sheet");
        greenButton.setStyleName("Default");
        greenButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getApplicationInstance().setStyleSheet(Styles.GREEN_STYLE_SHEET);
            }
        });
        controlsColumn.add(greenButton);
        
        Button nullButton = new Button("No Style Sheet");
        nullButton.setStyleName("Default");
        nullButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getApplicationInstance().setStyleSheet(null);
            }
        });
        controlsColumn.add(nullButton);
        
        Button customButton = new Button("Custom Style Sheet (Edit Below)");
        customButton.setStyleName("Default");
        customButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    StyleSheet styleSheet = loadStyleSheet(styleSheetEntryTextArea.getDocument().getText());
                    getApplicationInstance().setStyleSheet(styleSheet);
                } catch (ComponentXmlException ex) {
                    displayCustomStyleError(ex);
                }
            }
        });
        controlsColumn.add(customButton);
        
        styleSheetEntryTextArea = new TextArea();
        styleSheetEntryTextArea.getDocument().setText(DEFAULT_STYLE_SHEET_TEXT);
        styleSheetEntryTextArea.setStyleName("Default");
        styleSheetEntryTextArea.setWidth(new Extent(600));
        styleSheetEntryTextArea.setHeight(new Extent(300));
        splitPaneLayoutData = new SplitPaneLayoutData();
        splitPaneLayoutData.setInsets(new Insets(10));
        styleSheetEntryTextArea.setLayoutData(splitPaneLayoutData);
        add(styleSheetEntryTextArea);
    }
    
    private void displayCustomStyleError(Exception exception) {
        try {
            StringWriter w = new StringWriter();
            exception.printStackTrace(new PrintWriter(w));
            w.close();
            WindowPane windowPane = new WindowPane();
            windowPane.setStyleName("Default");
            windowPane.setTitle("Exception Setting Custom Style");
            windowPane.add(new Label(w.toString()));
            InteractiveApp.getApp().addDialogWindowPane(windowPane);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private StyleSheet loadStyleSheet(String text) 
    throws ComponentXmlException {
        InputStream in = null;
        try {
            //BUGBUG. Not i18n safe.
            in = new ByteArrayInputStream(text.getBytes());
            StyleSheet styleSheet = StyleSheetLoader.load(in, StyleSheetTest.class.getClassLoader());
            return styleSheet;
        } finally {
            if (in != null) { try { in.close(); } catch (IOException ex) { } }
        }
    }
}
