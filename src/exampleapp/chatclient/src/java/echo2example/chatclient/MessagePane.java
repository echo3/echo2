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

import java.text.DateFormat;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;

public class MessagePane extends ContentPane {
    
    private static final DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
    
    /**
     * Maximum number of messages to display simultaneously.
     */
    private static final int MAX_MESSAGE_COUNT = 100;
    
    private static final Extent BOTTOM = new Extent(-1, Extent.PX);
    
    private Column listColumn;
    private String userName;
    
    public MessagePane() {
        super();
        listColumn = new Column();
        listColumn.setStyleName("MessagePane.ListColumn");
        add(listColumn);
        userName = ChatApp.getApp().getUserName();
    }

    public void update() {
        ChatApp app = ChatApp.getApp();
        ChatSession.Message[] messages = app.getNewMessages();
        Label label;
        
        for (int i = 0; i < messages.length; ++i) {
            Row row = new Row();
            row.setStyleName("MessagePane.MessageRow");
            
            label = new Label(dateFormat.format(messages[i].getDate()));
            label.setStyleName("MessagePane.DateLabel");
            row.add(label);

            if (messages[i].getUserName() != null) {
                label = new Label(messages[i].getUserName());
                label.setStyleName(userName.equals(messages[i].getUserName()) 
                        ? "MessagePane.UserNameLabel.Yourself" : "MessagePane.UserNameLabel.Other");
                row.add(label);
                
                label =  new Label(messages[i].getContent());
                row.add(label);
            } else {
                label =  new Label(messages[i].getContent());
                label.setStyleName("MessagePane.SystemMessageLabel");
                row.add(label);
            }
            
            listColumn.add(row);
        }
        
        // Remove leading messages greater than MAX_MESSAGE_COUNT.
        int componentCount = listColumn.getComponentCount();
        for (int i = MAX_MESSAGE_COUNT; i < componentCount; ++i) {
            listColumn.remove(0);
        }
        
        setVerticalScroll(BOTTOM);
    }
    
}
