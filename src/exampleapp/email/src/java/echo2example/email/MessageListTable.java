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

import java.io.Serializable;
import java.util.Date;
import java.util.EventListener;
import java.util.EventObject;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.table.AbstractTableModel;
import nextapp.echo2.app.table.TableCellRenderer;

/**
 * A selectable <code>Table</code> which displays a list of messages.
 */
public class MessageListTable extends Table {
    
    private static final int COLUMN_FROM = 0;
    private static final int COLUMN_SUBJECT = 1; 
    private static final int COLUMN_DATE = 2;
    
   /**
     * An event describing a message selection.
     */
    public class MessageSelectionEvent extends EventObject {
        
        private Message message;
        
        /**
         * Creates a <code>MessageSelectionEvent</code>.
         * 
         * @param message the selected message
         */
        public MessageSelectionEvent(Message message) {
            super(MessageListTable.this);
            this.message = message;
        }
        
        /**
         * Returns the selected message.
         * 
         * @return the selected message
         */
        public Message getMessage() {
            return message;
        }
    }
    
    /**
     * A listener interface for receiving notification of message selections.
     */
    public static interface MessageSelectionListener extends EventListener, Serializable {
        
        /**
         * Invoked when a message is selected.
         * 
         * @param e an event describing the selection
         */
        public void messageSelected(MessageSelectionEvent e);
    }

    /**
     * A <code>TableModel</code> that pulls message header data from the 
     * mail server.
     */
    private AbstractTableModel messageTableModel = new AbstractTableModel() {
        
        /**
         * @see nextapp.echo2.app.table.TableModel#getColumnCount()
         */
        public int getColumnCount() {
            return 3;
        }

        /**
         * @see nextapp.echo2.app.table.TableModel#getRowCount()
         */
        public int getRowCount() {
            return displayedMessages == null ? 0 : displayedMessages.length;
        }
        
        /**
         * @see nextapp.echo2.app.table.TableModel#getValueAt(int, int)
         */
        public Object getValueAt(int column, int row) {
            try {
                switch (column) {
                case COLUMN_FROM:
                    // Return the sender's address.
                    Address[] from = displayedMessages[row].getFrom();
                    if (from != null) {
                        return ((InternetAddress) from[0]).getPersonal();
                    } else {
                        return Messages.getString("MessageListTable.UnknownSenderText");
                    }
                case COLUMN_SUBJECT:
                    // Return the subject.
                    String subject = displayedMessages[row].getSubject();
                    if (subject != null && subject.length() > 40) {
                        subject = subject.substring(0, 40);
                    }
                    return subject;
                case COLUMN_DATE:
                    return displayedMessages[row].getSentDate();
                default: 
                    throw new IllegalArgumentException("Invalid column.");
                }
            } catch (MessagingException ex) {
                // Generally should not occur.
                return null;
            }
        }
        
        /**
         * @see nextapp.echo2.app.table.TableModel#getColumnName(int)
         */
        public String getColumnName(int column) {
            switch (column) {
            case COLUMN_FROM:    return Messages.getString("MessageListTable.ColumnHeaderFrom");
            case COLUMN_SUBJECT: return Messages.getString("MessageListTable.ColumnHeaderSubject");
            case COLUMN_DATE:    return Messages.getString("MessageListTable.ColumnHeaderDate");
            default: throw new IllegalArgumentException("Invalid column.");
            }
        }
    };
    
    /**
     * A renderer for the header data contained in the table of messages.
     */
    private TableCellRenderer messageTableCellRenderer = new TableCellRenderer() {
        
        /**
         * @see nextapp.echo2.app.table.TableCellRenderer#getTableCellRendererComponent(
         *      nextapp.echo2.app.Table, java.lang.Object, int, int)
         */
        public Component getTableCellRendererComponent(Table table, Object value, int column, int row) {
            Label label;
            if (column == COLUMN_DATE) {
                label = new Label(Messages.formatDateTimeMedium((Date) value));
            } else {
                label = new Label(value == null ? (String) null : value.toString());
            }
            if (row % 2 == 0) {
                label.setStyleName("MessageListTable.EvenRowLabel");
            } else {
                label.setStyleName("MessageListTable.OddRowLabel");
            }
            return label;
        }
    };

    private ActionListener tableActionListener = new ActionListener() {
        
        /**
         * @see nextapp.echo2.app.event.ActionListener#actionPerformed(nextapp.echo2.app.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            fireMessageSelection();
        }
    };
    
    private Message[] displayedMessages;
    private Folder folder;
    private int totalMessages; 
    private int pageIndex;
     
    /**
     * Creates a new <code>MessageListTable</code>.
     */
    public MessageListTable() {
        super();
        addActionListener(tableActionListener);
        setStyleName("MessageListTable.Table");
        setModel(messageTableModel);
        setDefaultRenderer(Object.class, messageTableCellRenderer);
    }

    /**
     * Adds a <code>MessageSelectionListener</code> to be notified of 
     * message selections.
     * 
     * @param l the listener to add
     */
    public void addMessageSelectionListener(MessageSelectionListener l) {
        getEventListenerList().addListener(MessageSelectionListener.class, l);
    }

    /**
     * Notifies <code>MessageSelectionListener</code>s of a message selection.
     */
    private void fireMessageSelection() {
        Message message;
        int selectedRow = getSelectionModel().getMinSelectedIndex();
        if (selectedRow == -1) {
            message = null;
        } else {
            message = displayedMessages[selectedRow];
        }
        MessageSelectionEvent e = new MessageSelectionEvent(message);
        EventListener[] listeners = getEventListenerList().getListeners(MessageSelectionListener.class);
        for (int i = 0; i < listeners.length; ++i) {
            ((MessageSelectionListener) listeners[i]).messageSelected(e);
        }
    }

    /**
     * Refereshes the current page.
     */
    private void refresh() {
        if (folder == null) {
            displayedMessages = null;
            messageTableModel.fireTableDataChanged();
            return;
        }
        try {
            int firstMessage = pageIndex * EmailApp.MESSAGES_PER_PAGE + 1;
            int lastMessage = firstMessage + EmailApp.MESSAGES_PER_PAGE - 1;
            if (lastMessage > totalMessages) {
                lastMessage = totalMessages;
            }
            
            displayedMessages = folder.getMessages(firstMessage, lastMessage);
            messageTableModel.fireTableDataChanged();
        } catch (MessagingException ex) {
            ((EmailApp) getApplicationInstance()).processFatalException(ex);
        }
    }

    /**
     * Removes a <code>MessageSelectionListener</code> from being notified of 
     * message selections.
     * 
     * @param l the listener to remove
     */
    public void removeMessageSelectionListener(MessageSelectionListener l) {
        getEventListenerList().removeListener(MessageSelectionListener.class, l);
    }

    /**
     * Sets the displayed folder.
     * 
     * @param newValue The new <code>Folder</code> to display.
     */
    public void setFolder(Folder newValue) {
        try {
            if (folder != null) {
                folder.close(false);
                folder = null;
            }
            folder = newValue;
            if (folder != null) {
                folder.open(Folder.READ_ONLY);
                totalMessages = folder.getMessageCount();
            }
            
        } catch (MessagingException ex) {
            ((EmailApp) getApplicationInstance()).processFatalException(ex);
        }
        getSelectionModel().clearSelection();
        refresh();
        fireMessageSelection();
    }
    
    /**
     * Sets the displayed page.
     */
    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
        getSelectionModel().clearSelection();
        refresh();
        fireMessageSelection();
    }
}