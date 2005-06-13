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

package echo2example.email.faux;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * A minimal implementation of a JavaMail <code>Folder</code> to serve the
 * requirements of the e-mail application.
 */
public class FauxFolder extends Folder {
    
    private static final int MESSAGE_COUNT = 140;
    
    private static final Message[] INBOX_MESSAGES;
    static {
        try {
            MessageGenerator messageGenerator = new MessageGenerator();
            SortedSet sortingSet = new TreeSet(new Comparator(){
                 public int compare(Object a, Object b) {
                     try {
                         Message message1 = (Message) a;
                         Message message2 = (Message) b;
                         int dateDelta = message1.getSentDate().compareTo(message2.getSentDate());
                         if (dateDelta != 0) {
                             return dateDelta;
                         }
                         return message1.toString().compareTo(message2.toString());
                     } catch (MessagingException ex) {
                         throw new RuntimeException(ex);
                     }
                 }
            });
            for (int i = 0; i < MESSAGE_COUNT; ++i) {
                sortingSet.add(messageGenerator.generateMessage());
            }
            INBOX_MESSAGES = (Message[]) sortingSet.toArray(new Message[sortingSet.size()]);
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private int type;
    private FauxStore store;
    
    /**
     * Creates the "INBOX" folder.
     * 
     * @param store the relevant <code>Store</code>
     * @return the created INBOX
     */
    public static final FauxFolder createInbox(FauxStore store) {
        return new FauxFolder(store, HOLDS_MESSAGES);
    }
    
    /**
     * Creates the root folder.
     * 
     * @param store the relevant <code>Store</code>
     * @return the created root folder
     */
    public static final FauxFolder createRoot(FauxStore store) {
        return new FauxFolder(store, HOLDS_FOLDERS);
    }
    
    /**
     * Creates a new <code>FauxFolder</code>
     * 
     * @param store the relevant <code>Store</code>
     * @param type the folder type
     */
    private FauxFolder(FauxStore store, int type) {
        super(store);
        this.store = store;
        this.type = type;
    }

    /**
     * @see javax.mail.Folder#appendMessages(javax.mail.Message[])
     */
    public void appendMessages(Message[] messages) 
    throws MessagingException {
        throw new UnsupportedOperationException();
    }
    
    /**
     * @see javax.mail.Folder#close(boolean)
     */
    public void close(boolean expunge) 
    throws MessagingException { }

    /**
     * @see javax.mail.Folder#create(int)
     */
    public boolean create(int type) throws MessagingException {
        return true;
    }
    
    /**
     * @see javax.mail.Folder#delete(boolean)
     */
    public boolean delete(boolean recurse) 
    throws MessagingException {
        return false;
    }
    
    /**
     * @see javax.mail.Folder#exists()
     */
    public boolean exists() throws MessagingException {
        return type == HOLDS_MESSAGES;
    }
    
    /**
     * @see javax.mail.Folder#expunge()
     */
    public Message[] expunge() throws MessagingException {
        return new Message[0];
    }

    /**
     * @see javax.mail.Folder#getFolder(java.lang.String)
     */
    public Folder getFolder(String name) 
    throws MessagingException {
        if (type == HOLDS_FOLDERS) {
            return store.inboxFolder;
        } else {
            throw new MessagingException();
        }
    }

    /**
     * @see javax.mail.Folder#getFullName()
     */
    public String getFullName() {
        return getName();
    }
    
    /**
     * @see javax.mail.Folder#getMessage(int)
     */
    public Message getMessage(int index) throws MessagingException {
        return INBOX_MESSAGES[index - 1];
    }

    /**
     * @see javax.mail.Folder#getMessageCount()
     */
    public int getMessageCount() throws MessagingException {
        return INBOX_MESSAGES.length;
    }
    
    /**
     * @see javax.mail.Folder#getName()
     */
    public String getName() {
        switch (type) {
        case HOLDS_FOLDERS:
            return "/";
        case HOLDS_MESSAGES:
            return "INBOX";
        default:
            return "Unknown";
        }
    }
    
    /**
     * @see javax.mail.Folder#getParent()
     */
    public Folder getParent() throws MessagingException {
        return type == HOLDS_MESSAGES ? store.rootFolder : null;
    }
    
    /**
     * @see javax.mail.Folder#getPermanentFlags()
     */
    public Flags getPermanentFlags() {
        return new Flags();
    }
    
    /**
     * @see javax.mail.Folder#getSeparator()
     */
    public char getSeparator() throws MessagingException {
        return '\u0000';
    }
    
    /**
     * @see javax.mail.Folder#getType()
     */
    public int getType() throws MessagingException {
        return type;
    }

    /**
     * @see javax.mail.Folder#hasNewMessages()
     */
    public boolean hasNewMessages() throws MessagingException {
        return false;
    }
    
    /**
     * @see javax.mail.Folder#isOpen()
     */
    public boolean isOpen() {
        return true;
    }
    
    /**
     * @see javax.mail.Folder#list(java.lang.String)
     */
    public Folder[] list(String pattern) throws MessagingException {
        if (type == HOLDS_FOLDERS) {
            return new Folder[]{store.inboxFolder};
        } else {
            throw new MessagingException();
        }
    }
    
    /**
     * @see javax.mail.Folder#open(int)
     */
    public void open(int mode) throws MessagingException {
    }

    /**
     * @see javax.mail.Folder#renameTo(javax.mail.Folder)
     */
    public boolean renameTo(Folder folder) throws MessagingException {
        return false;
    }
}