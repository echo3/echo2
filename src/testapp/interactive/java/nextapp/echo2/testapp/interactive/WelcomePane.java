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

import nextapp.echo2.app.Border;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

/**
 * <code>ContentPane</code> which displays a welcome/instruction message to 
 * users when they initially visit the application.
 */
public class WelcomePane extends ContentPane {
    
    /**
     * Default constructor.
     */
    public WelcomePane() {
        Column column = new Column();
        column.setBorder(new Border(3, new Color(0x4f4f7f), Border.STYLE_OUTSET));
        column.setBackground(new Color(0x4f4f7f));
        column.setForeground(Color.WHITE);
        column.setInsets(new Insets(50));
        column.setCellSpacing(new Extent(20));
        add(column);
        
        Label label;
        
        label = new Label("N E X T A P P | E C H O 2");
        label.setFont(new Font(Font.COURIER_NEW, Font.BOLD, new Extent(32)));
        column.add(label);
        
        label = new Label("Welcome to the NextApp Echo2 Test Application.  "
                + "This application was built to interactively test components of Echo2 during development.  "
                + "It is also being (mis)used as a public demonstration of Echo2's capabilities. "
                + "Note that if this is a development version of Echo, then some "
                + "of the features and capabilities demonstrated in this application may not be complete.");
        column.add(label);
        
        label = new Label("Note that you may watch the AJAX XML messages being sent between the client and server by "
                + "enabling \"Debug Mode\".  Debug Mode may be enabled "
                + "by appending \"?debug\" to the end of the URL of this application (for example: "
                + "\"http://demo.nextapp.com/InteractiveTest/ia?debug\"). "
                + "Please be aware that Debug Mode will in most cases result in EXTREMELY SLOW PERFORMANCE. "
                + "You may exit Debug Mode at any time by simply closing the Debug window.");
        column.add(label);

        label = new Label("Please visit the Echo2 Home Page @ http://www.nextapp.com/products/echo2 for more information.");
        column.add(label);
        
        label = new Label(Styles.ICON_LOGO);
        column.add(label);

        Button continueButton = new Button("Continue");
        continueButton.setStyleName("Default");
        continueButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                InteractiveApp.getApp().displayTestPane();
            }
        });
        column.add(continueButton);
    }

}
