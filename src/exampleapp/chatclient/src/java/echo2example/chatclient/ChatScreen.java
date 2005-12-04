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

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.TextArea;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

/**
 * A screen which displays the current state of the chat room and allow the 
 * user to submit chat input.
 */
public class ChatScreen extends ContentPane {

    private TextArea postField;
    private MessagePane messagePane;
    
    /**
     * Creates a new <code>ChatScreen</code>.
     */
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
        
        SplitPane mainSplitPane = new SplitPane(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP, new Extent(80));
        outerSplitPane.add(mainSplitPane);
        
        Row postInputRow = new Row();
        postInputRow.setStyleName("ChatScreen.PostInputRow");
        mainSplitPane.add(postInputRow);

        Label currentUserLabel = new Label(ChatApp.getApp().getUserName() + ":");
        currentUserLabel.setStyleName("ChatScreen.CurrentUserLabel");
        postInputRow.add(currentUserLabel);
        
        postField = new TextArea();
        postField.setStyleName("Default");
        postField.setWidth(new Extent(600, Extent.PX));
        postField.setHeight(new Extent(70, Extent.PX));
        postField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                postMessage();
            }
        });
        postInputRow.add(postField);
        
        Button submitButton = new Button("Submit", Styles.ICON_24_RIGHT_ARROW);
        submitButton.setTextPosition(new Alignment(Alignment.LEFT, Alignment.DEFAULT));
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                postMessage();
            }
        });
        postInputRow.add(submitButton);

        messagePane = new MessagePane();
        mainSplitPane.add(messagePane);
        
        ChatApp app = ChatApp.getApp();
        app.setFocusedComponent(postField);
    }
    
    /**
     * Updates the list of messages.
     */
    public void updateMessageList() {
        messagePane.update();
    }
    
    /**
     * Posts the text currently entered as a new message.
     */
    private void postMessage() {
        ChatApp app = ChatApp.getApp();
        if (postField.getText().trim().length() != 0) {
            app.postMessage(postField.getText());
        }
        postField.setText("");
        app.setFocusedComponent(postField);
        messagePane.update();
    }
}
