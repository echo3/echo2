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

import java.util.StringTokenizer;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.TextArea;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.WindowPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;

/**
 * The message composition window.
 */
public class ComposeWindow extends WindowPane {
    
    /**
     * Adds recipient e-mail addresses contained in a comma-delimited string 
     * to an outgoing e-mail <code>Message</code>.
     *
     * @param message the <code>Message</code> object to which the addresses
     *        will be added
     * @param recipientType the type of recipient to which the addresses 
     *        should be added (to, cc, or bcc)
     * @param recipients a comma-delimited string of recipient addresses
     */
    private static void addRecipients(Message message, Message.RecipientType recipientType, String recipients) 
    throws AddressException, MessagingException {
        if (recipients != null && recipients.trim().length() > 0) {
            // Tokenize the recipient string based on commas. 
            StringTokenizer tokenizer = new StringTokenizer(recipients, ",");
            while (tokenizer.hasMoreTokens()) {
                // Add each recipient.
                String recipient = tokenizer.nextToken();
                message.addRecipient(recipientType, new InternetAddress(recipient));
            }
        }
    }

    private TextField toField;
    private TextField ccField;
    private TextField bccField;
    private TextField subjectField;
    private TextArea messageField;

    /**
     * Creates a new <code>ComposeWindow</code>.
     * 
     * @param replyMessage the message being replied to, or null if composing
     *        a new message.
     */
    public ComposeWindow(Message replyMessage) {
        super(Messages.getString("ComposeWindow.Title"), new Extent(500), new Extent(480));
        setResizable(false);
        setDefaultCloseOperation(WindowPane.DO_NOTHING_ON_CLOSE);
        setStyleName("Default");
        
        addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent e) {
                processDiscard();
            }
        });
        
        SplitPane mainPane = new SplitPane(SplitPane.ORIENTATION_VERTICAL, new Extent(32));
        add(mainPane);
        
        Row controlPane = new Row();
        controlPane.setStyleName("ControlPane");
        mainPane.add(controlPane);
        
        Button sendButton = new Button(Messages.getString("ComposeWindow.SendButton"),
                Styles.ICON_24_YES);
        sendButton.setStyleName("ControlPane.Button");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (sendMessage()) {
                    ((EmailApp) getApplicationInstance()).getDefaultWindow().getContent().remove(ComposeWindow.this);
                }
            }
        });
        controlPane.add(sendButton);

        Button cancelButton = new Button(Messages.getString("ComposeWindow.DiscardButton"),
                Styles.ICON_24_NO);
        cancelButton.setStyleName("ControlPane.Button");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processDiscard();
            }
        });
        controlPane.add(cancelButton);

        Column layoutColumn = new Column();
        layoutColumn.setCellSpacing(new Extent(10));
        layoutColumn.setInsets(new Insets(10));
        mainPane.add(layoutColumn);
        
        Grid headerGrid = new Grid();
        headerGrid.setInsets(new Insets(0, 2));
        layoutColumn.add(headerGrid);
        
        Label label;
        
        label = new Label(Messages.getString("Message.PromptLabel.To"));
        headerGrid.add(label);
        
        toField = new TextField();
        toField.setStyleName("Default");
        toField.setWidth(new Extent(350));
        headerGrid.add(toField);
        
        label = new Label(Messages.getString("Message.PromptLabel.Cc"));
        headerGrid.add(label);
        
        ccField = new TextField();
        ccField.setStyleName("Default");
        ccField.setWidth(new Extent(350));
        headerGrid.add(ccField);
        
        label = new Label(Messages.getString("Message.PromptLabel.Bcc"));
        headerGrid.add(label);
        
        bccField = new TextField();
        bccField.setStyleName("Default");
        bccField.setWidth(new Extent(350));
        headerGrid.add(bccField);
        
        label = new Label(Messages.getString("Message.PromptLabel.Subject"));
        headerGrid.add(label);
        
        subjectField = new TextField();
        subjectField.setStyleName("Default");
        subjectField.setWidth(new Extent(350));
        headerGrid.add(subjectField);
        
        messageField = new TextArea();
        messageField.setStyleName("Default");
        messageField.setWidth(new Extent(420));
        messageField.setHeight(new Extent(15, Extent.EM));
        layoutColumn.add(messageField);
        
        if (replyMessage != null) {
            try {
                toField.setText(replyMessage.getFrom()[0].toString());
                subjectField.setText(replyMessage.getSubject());
            } catch (MessagingException ex) {
                EmailApp.getApp().processFatalException(ex);
            }
        }
    }
    
    /**
     * Handles a user request to discard the message being composed.
     */
    private void processDiscard() {
        if (messageField.getText().trim().length() > 0) {
            MessageDialog confirmDialog = new MessageDialog(Messages.getString("ComposeWindow.ConfirmDiscard.Title"),
                    Messages.getString("ComposeWindow.ConfirmDiscard.Message"), MessageDialog.TYPE_CONFIRM,
                    MessageDialog.CONTROLS_YES_NO);
            confirmDialog.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (MessageDialog.COMMAND_OK.equals(e.getActionCommand())) {
                        getParent().remove(ComposeWindow.this);
                    }
                }
            });
            getApplicationInstance().getDefaultWindow().getContent().add(confirmDialog);
        } else {
            getParent().remove(this);
        }
    }

    /**
     * Sends the message.
     * This message will display an error dialog if the message cannot be
     * sent.
     *
     * @return True if the message was sent.
     */
    private boolean sendMessage() {
        MimeMessage message = new MimeMessage(((EmailApp) getApplicationInstance()).getMailSession());
        try {
            // Ensure message has a subject.  If it does not, raise an error and do not send.
            if (subjectField.getText() == null || subjectField.getText().length() == 0) {
                MessageDialog messageDialog = new MessageDialog(Messages.getString("ComposeWindow.NoSubjectError.Title"),
                        Messages.getString("ComposeWindow.NoSubjectError.Message"), MessageDialog.TYPE_ERROR, 
                        MessageDialog.CONTROLS_OK);
                getApplicationInstance().getDefaultWindow().getContent().add(messageDialog);
                return false;
            }
            
            // Ensure message has a body.  If it does not, raise an error and do not send.
            if (messageField.getText() == null || messageField.getText().length() == 0) {
                MessageDialog messageDialog = new MessageDialog(Messages.getString("ComposeWindow.NoMessageError.Title"),
                        Messages.getString("ComposeWindow.NoMessageError.Message"), MessageDialog.TYPE_ERROR, 
                        MessageDialog.CONTROLS_OK);
                getApplicationInstance().getDefaultWindow().getContent().add(messageDialog);
                return false;
            }
            
            // Set sender address.
            message.setFrom(new InternetAddress(((EmailApp) getApplicationInstance()).getEmailAddress()));
            
            // Add recipients contained in recipient text fields.
            addRecipients(message, Message.RecipientType.TO, toField.getText()); 
            addRecipients(message, Message.RecipientType.CC, ccField.getText()); 
            addRecipients(message, Message.RecipientType.BCC, bccField.getText());
            
            // Set the subject and content of the message.
            message.setSubject(subjectField.getText());
            message.setText(messageField.getText());
            
            // Attempt to send the message.
            if (!EmailApp.FAUX_MODE) {
                Transport.send(message);
            }
            return true;
        } catch (AddressException ex) {
            // Process an exception pertaining to an invalid recipient e-mail address: Raise an error.
            MessageDialog messageDialog = new MessageDialog(Messages.getString("ComposeWindow.InvalidAddressError.Title"),
                    Messages.getString("ComposeWindow.InvalidAddressError.Message"), MessageDialog.TYPE_ERROR, 
                    MessageDialog.CONTROLS_OK);
            getApplicationInstance().getDefaultWindow().getContent().add(messageDialog);
            return false;
        } catch (MessagingException ex) {
            // Handle a fatal exception.
            EmailApp.getApp().processFatalException(ex);
            return false;
        }
    }
}
