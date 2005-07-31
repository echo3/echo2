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

package echo2example.chatclient;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.TextArea;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

public class ChatScreen extends ContentPane {

    private TextArea postField;
    private MessageList messageList;
    
    public ChatScreen() {
        super();
        
        SplitPane outerSplitPane = new SplitPane(SplitPane.ORIENTATION_VERTICAL_TOP_BOTTOM, new Extent(32));
        add(outerSplitPane);
        
        Row controlsRow = new Row();
        controlsRow.setStyleName("ControlPane");
        outerSplitPane.add(controlsRow);
        
        Button logoutButton = new Button(Messages.getString("Generic.Exit"), Styles.ICON_24_EXIT);
        logoutButton.setStyleName("ControlPane.Button");
        logoutButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                ChatApp.getApp().disconnect();
            }
        });
        controlsRow.add(logoutButton);
        
        SplitPane mainSplitPane = new SplitPane(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP);
        outerSplitPane.add(mainSplitPane);
        
        Row postInputRow = new Row();
        mainSplitPane.add(postInputRow);

        Label userNameLabel = new Label(ChatApp.getApp().getUserName() + ":");
        postInputRow.add(userNameLabel);
        
        postField = new TextArea();
        postField.setStyleName("Default");
        postField.setWidth(new Extent(600, Extent.PX));
        postField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                postMessage();
            }
        });
        postInputRow.add(postField);
        
        Button submitButton = new Button("Submit");
        submitButton.setStyleName("Default");
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                postMessage();
            }
        });
        postInputRow.add(submitButton);

        SplitPane chatAndUsersPane = new SplitPane(SplitPane.ORIENTATION_HORIZONTAL_TRAILING_LEADING);
        mainSplitPane.add(chatAndUsersPane);
        
        chatAndUsersPane.add(new Label()); // user list.
        
        messageList = new MessageList();
        chatAndUsersPane.add(messageList);
    }
    
    public void updateMessageList() {
        messageList.update();
    }
    
    private void postMessage() {
        if (postField.getText().trim().length() != 0) {
            ChatApp app = ChatApp.getApp();
            app.postMessage(postField.getText());
            app.setFocusedComponent(postField);
        }
        postField.setText("");
        messageList.update();
    }
}
