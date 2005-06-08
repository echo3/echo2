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

import java.io.IOException;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;

/**
 * 
 */
public class MessagePane extends Column {
    
    private Label toFieldPromptLabel;
    private Label toFieldValueLabel;
    private Label ccFieldPromptLabel;
    private Label ccFieldValueLabel;
    private Label bccFieldPromptLabel;
    private Label bccFieldValueLabel;
    private Label subjectFieldValueLabel;
    private Column messageColumn;
    
    public MessagePane() {
        super();
        setVisible(false);
        
        setCellSpacing(new Extent(10));
        setInsets(new Insets(3));
        
        Grid headerGrid = new Grid();
        headerGrid.setStyleName("MessagePane.HeaderGrid");
        add(headerGrid);

        toFieldPromptLabel = new Label(Messages.getString("Message.PromptLabel.To"));
        toFieldPromptLabel.setStyleName("MessagePane.HeaderGridPrompt");
        headerGrid.add(toFieldPromptLabel);
        
        toFieldValueLabel = new Label();
        headerGrid.add(toFieldValueLabel);
        
        ccFieldPromptLabel = new Label(Messages.getString("Message.PromptLabel.Cc"));
        ccFieldPromptLabel.setStyleName("MessagePane.HeaderGridPrompt");
        headerGrid.add(ccFieldPromptLabel);
        
        ccFieldValueLabel = new Label();
        headerGrid.add(ccFieldValueLabel);
        
        bccFieldPromptLabel = new Label(Messages.getString("Message.PromptLabel.Bcc"));
        bccFieldPromptLabel.setStyleName("MessagePane.HeaderGridPrompt");
        headerGrid.add(bccFieldPromptLabel);
        
        bccFieldValueLabel = new Label();
        headerGrid.add(bccFieldValueLabel);
        
        Label subjectFieldPromptLabel = new Label(Messages.getString("Message.PromptLabel.Subject"));
        subjectFieldPromptLabel.setStyleName("MessagePane.HeaderGridPrompt");
        headerGrid.add(subjectFieldPromptLabel);
        
        subjectFieldValueLabel = new Label();
        headerGrid.add(subjectFieldValueLabel);
        
        messageColumn = new Column();
        add(messageColumn);
    }

    /**
     * Returns the recipients of a message as a comma-delimited String.
     * 
     * @param message the <code>Message</code>
     * @param type the recipient type
     */
    private String formatRecipients(Message message, Message.RecipientType type) 
    throws MessagingException {
        Address[] recipients = message.getRecipients(type);
        if (recipients == null || recipients.length == 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (int recipientIndex = 0; recipientIndex < recipients.length; ++recipientIndex) {
            sb.append(recipients[recipientIndex].toString());
            if (recipientIndex < recipients.length - 1) {
                // Seperate recipient names with a comma.
                sb.append(", ");
            }
        }
        return sb.toString();
    }
    
    /**
     * Returns an array of components containing the parts of a multipart 
     * message.
     *
     * @param message The message to parse.
     * @return An array of components, each containing a part of the message.
     */
    private Component[] renderMessageContent(Message message) 
    throws MessagingException {
        try {
            Object content = message.getContent();
            if (content instanceof String) {
                // Content is a string, return it enclosed in a Label.
                return new Component[]{new Label((String) content)};
            } else if (content instanceof Multipart) {
                // Content is multi-part, parse each part.
                Multipart multipart = (Multipart) content;
                Component[] data = new Component[multipart.getCount()];
                // Iterate through parts.
                for (int index = 0; index < data.length; ++index) {
                    BodyPart part = multipart.getBodyPart(index);
                    Object partContent = part.getContent();
                    if (partContent instanceof String) {
                        // Part content is a string, add it to returned array of Components as a Label.
                        data[index] = new Label((String) partContent);
                    } else {
                        // Part content is not a string, add it to returned array as a Label containing its content type.
                        data[index] = new Label(part.getContentType());
                    }
                }
                return data;
            } else {
                // Unhandled type, should not generally occur.
                return new Component[]{new Label(Messages.getString("Messages.UnableToParseError"))};
            }
        } catch (IOException ex) {
            // Generally should not occur.
            return new Component[]{new Label(Messages.getString("Messages.UnableToParseError"))};
        }
    }

    /**
     * Sets the displayed <code>Message</code>.
     * 
     * @param message the <code>Message</code> to display
     */
    public void setMessage(Message message) 
    throws MessagingException {
        if (message == null) {
            setVisible(false);
        } else {
            setVisible(true);
            updateRecipientData(message, Message.RecipientType.TO, toFieldPromptLabel, toFieldValueLabel);
            updateRecipientData(message, Message.RecipientType.CC, ccFieldPromptLabel, ccFieldValueLabel);
            updateRecipientData(message, Message.RecipientType.BCC, bccFieldPromptLabel, bccFieldValueLabel);
            subjectFieldValueLabel.setText(message.getSubject());
        }
        
        messageColumn.removeAll();
        if (message != null) {
            Component[] messageComponents = renderMessageContent(message);
            for (int i = 0; i < messageComponents.length; ++i) {
                messageColumn.add(messageComponents[i]);
            }
        }
    }


    /**
     * Updates the visual presentation of recipient information,
     * showing recipient types that are present and hiding those
     * which are not.
     */
    private void updateRecipientData(Message message, Message.RecipientType type,
            Label promptLabel, Label valueLabel) 
    throws MessagingException {
        String recipients = formatRecipients(message, type);
        if (recipients == null) {
            promptLabel.setVisible(false);
            valueLabel.setVisible(false);
            valueLabel.setText(null);
        } else {
            promptLabel.setVisible(true);
            valueLabel.setVisible(true);
            valueLabel.setText(recipients);
        }
    }
}
