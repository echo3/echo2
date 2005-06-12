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

package echo2example.email;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.PasswordField;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

/**
 * Login screen <code>ContentPane</code>.
 */
public class LoginScreen extends ContentPane {

    private static final Extent PX_300 = new Extent(300, Extent.PX);
    private static final Insets PLACEMENT_INSETS = new Insets(240,270,0,0);

    private TextField emailAddressField;
    private PasswordField passwordField;
    
    /**
     * Creates a new <code>LoginScreen</cdoe>.
     */
    public LoginScreen() {
        super();
        setStyleName("LoginScreen.ContentPane");

        Column column = new Column();
        column.setInsets(PLACEMENT_INSETS);
        add(column);
        
        Grid layoutGrid = new Grid();
        layoutGrid.setStyleName("LoginScreen.LayoutGrid");
        column.add(layoutGrid);
        
        Label label;
        
//        label = new Label(Messages.getString("LoginScreen.WelcomeLabel"));
//        label.setStyleName("LoginScreen.WelcomeLabel");
//        layoutGrid.add(label);
        
        label = new Label(Messages.getString("LoginScreen.PromptEmailAddress"));
        label.setStyleName("LoginScreen.Prompt");
        layoutGrid.add(label);
        
        emailAddressField = new TextField();
        emailAddressField.setWidth(PX_300);
        emailAddressField.setStyleName("Default");
        layoutGrid.add(emailAddressField);
        
        label = new Label(Messages.getString("LoginScreen.PromptPassword"));
        label.setStyleName("LoginScreen.Prompt");
        layoutGrid.add(label);
        
        passwordField = new PasswordField();
        passwordField.setWidth(PX_300);
        passwordField.setStyleName("Default");
        layoutGrid.add(passwordField);
        
        Button button = new Button(Messages.getString("LoginScreen.Continue"));
        button.setStyleName("LoginScreen.Continue");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processLogin();
            }
        });
        layoutGrid.add(button);

        if (EmailApp.FAUX_MODE) {
            emailAddressField.setText("joe.smith@test.nextapp.com");
            passwordField.setText("Joshua");
        }
    }
    
    /**
     * Processes a user log-in request.
     */
    private void processLogin() {
        if (!EmailApp.getApp().connect(emailAddressField.getText(), passwordField.getText())) {
            MessageDialog messageDialog = new MessageDialog(Messages.getString("LoginScreen.InvalidLogin.Title"),
                    Messages.getString("LoginScreen.InvalidLogin.Message"), MessageDialog.TYPE_ERROR, MessageDialog.CONTROLS_OK);
            getApplicationInstance().getDefaultWindow().getContent().add(messageDialog);
        }
    }
}