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

import java.io.IOException;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.TaskQueueHandle;
import nextapp.echo2.app.Window;

//TODO Remove under-development flag on completion.

/**
 * Chat Client <code>ApplicationInstance</code> implementation.
 * 
 * NOTE: The chat client is currently under development.
 * Some features/components may be incomplete or missing.
 */
public class ChatApp extends ApplicationInstance {

    /**
     * Returns the active <code>ChatApp</code>.
     * 
     * @return the active <code>ChatApp</code>
     */
    public static ChatApp getApp() {
        return (ChatApp) getActive();
    }
    
    private Server server;
    private TaskQueueHandle incomingMessageQueue;
    
    /**
     * Attempts to connects to the chat server with the specified user name.
     * Displays a <code>ChatScreen</code> for the user if the user
     * is successfully connected.  Performs no action if the user is not
     * successfully connected.
     * 
     * @return true if the operation was successfully completed,
     *         false otherwise
     */
    public boolean connect(String userName) {
        try {
            server = Server.forUserName(userName);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        if (server == null) {
            return false;
        } else {
            if (incomingMessageQueue != null) {
                throw new IllegalStateException();
            }
            incomingMessageQueue = createTaskQueue();
            getDefaultWindow().setContent(new ChatScreen());
            return true;
        }
    }
    
    /**
     * Disconnects from the chat server and logs the current user out.
     * Displays the <code>LoginScreen</code>.
     */
    public void disconnect() {
        try {
            server.dispose();
            server = null;
            if (incomingMessageQueue != null) {
                removeTaskQueue(incomingMessageQueue);
                incomingMessageQueue = null;
            }
            getDefaultWindow().setContent(new LoginScreen());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Retrieves new messages from the server.  Once the new messages are
     * deleted they are removed from the queue of 'new' messages.
     * Invoking this method thus alters the state of the new message queue.
     * 
     * @return an array of new messages 
     */
    public Server.Message[] getNewMessages() {
        return server.getNewMessages();
    }
    
    /**
     * Returns the user name of the currently logged-in user.
     * 
     * @return the user name
     */
    public String getUserName() {
        return server == null ? null : server.getUserName();
    }
    
    /**
     * @see nextapp.echo2.app.ApplicationInstance#hasQueuedTasks()
     */
    public boolean hasQueuedTasks() {
        if (pollServer()) {
            final ChatScreen chatScreen = (ChatScreen) getDefaultWindow().getContent();
            enqueueTask(incomingMessageQueue, new Runnable(){
                public void run() {
                    chatScreen.updateMessageList();
                }
            });
        }
        
        return super.hasQueuedTasks();
    }
    
    /**
     * @see nextapp.echo2.app.ApplicationInstance#init()
     */
    public Window init() {
        setStyleSheet(Styles.DEFAULT_STYLE_SHEET);
        Window window = new Window();
        window.setTitle(Messages.getString("Application.Title.Window"));
        window.setContent(new LoginScreen());
        return window;
    }
    
    /**
     * Polls the <code>Server</code> to determine if any new messages are
     * present.
     * 
     * @return true if any new messages are present
     */
    private boolean pollServer() {
        if (server == null) {
            return false;
        }
        try {
            server.pollServer();
            return server.hasNewMessages();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Posts a message to the chat server for the logged-in user.
     * 
     * @param content the content of the message to post
     */
    public void postMessage(String content) {
        try {
            server.postMessage(content);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
