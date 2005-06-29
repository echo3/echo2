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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;

/**
 * The faux <code>Message</code> implementation.
 * This class provides only the minimal implementation of the 
 * <code>Message</code> base class necessary for operation of
 * the web mail example.
 */
public class FauxMessage extends Message {
    
    private Address[] from;
    private String subject;
    private String content;
    private Date receivedDate;
    private Address[] to, cc, bcc;
    
    /**
     * Creates a new <code>FauxMessage</code>
     * 
     * @param from the sender
     * @param receivedDate the date the message was received
     * @param to an array of "to" recipients
     * @param cc an array of "cc" recipients
     * @param bcc an array of "bcc" recipients
     * @param subject the subject of the message
     * @param content the content of the message
     */
    public FauxMessage(Address from, Date receivedDate, 
            Address[] to, Address[] cc, Address[] bcc, 
            String subject, String content) {
        this.receivedDate = receivedDate;
        this.from = new Address[]{from};
        this.to = to;
        this.cc = cc;
        this.bcc = bcc;
        this.subject = subject;
        this.content = content;
    }

    /**
     * @see javax.mail.Message#addFrom(javax.mail.Address[])
     */
    public void addFrom(Address[] from) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Part#addHeader(java.lang.String, java.lang.String)
     */
    public void addHeader(String arg0, String arg1) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Message#addRecipients(javax.mail.Message.RecipientType, javax.mail.Address[])
     */
    public void addRecipients(RecipientType arg0, Address[] arg1) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Part#getAllHeaders()
     */
    public Enumeration getAllHeaders() throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Part#getContent()
     */
    public Object getContent() throws IOException, MessagingException {
        return content;
    }

    /**
     * @see javax.mail.Part#getContentType()
     */
    public String getContentType() throws MessagingException {
        return "text/plain";
    }

    /**
     * @see javax.mail.Part#getDataHandler()
     */
    public DataHandler getDataHandler() throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Part#getDescription()
     */
    public String getDescription() throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Part#getDisposition()
     */
    public String getDisposition() throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Part#getFileName()
     */
    public String getFileName() throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Message#getFlags()
     */
    public Flags getFlags() throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Message#getFrom()
     */
    public Address[] getFrom() throws MessagingException {
        return from;
    }

    /**
     * @see javax.mail.Part#getHeader(java.lang.String)
     */
    public String[] getHeader(String arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Part#getInputStream()
     */
    public InputStream getInputStream() throws IOException, MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Part#getLineCount()
     */
    public int getLineCount() throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Part#getMatchingHeaders(java.lang.String[])
     */
    public Enumeration getMatchingHeaders(String[] arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Part#getNonMatchingHeaders(java.lang.String[])
     */
    public Enumeration getNonMatchingHeaders(String[] arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Message#getReceivedDate()
     */
    public Date getReceivedDate() throws MessagingException {
        return receivedDate;
    }

    /**
     * @see javax.mail.Message#getRecipients(javax.mail.Message.RecipientType)
     */
    public Address[] getRecipients(RecipientType recipientType) throws MessagingException {
        if (recipientType.equals(RecipientType.TO)) {
            return to;
        } else if (recipientType.equals(RecipientType.CC)) {
            return cc;
        } else if (recipientType.equals(RecipientType.BCC)) {
            return bcc;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @see javax.mail.Message#getSentDate()
     */
    public Date getSentDate() throws MessagingException {
        return receivedDate;
    }

    /**
     * @see javax.mail.Part#getSize()
     */
    public int getSize() throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Message#getSubject()
     */
    public String getSubject() throws MessagingException {
        return subject;
    }

    /**
     * @see javax.mail.Part#isMimeType(java.lang.String)
     */
    public boolean isMimeType(String arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Part#removeHeader(java.lang.String)
     */
    public void removeHeader(String arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Message#reply(boolean)
     */
    public Message reply(boolean arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Message#saveChanges()
     */
    public void saveChanges() throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Part#setContent(javax.mail.Multipart)
     */
    public void setContent(Multipart arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Part#setContent(java.lang.Object, java.lang.String)
     */
    public void setContent(Object arg0, String arg1) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Part#setDataHandler(javax.activation.DataHandler)
     */
    public void setDataHandler(DataHandler arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Part#setDescription(java.lang.String)
     */
    public void setDescription(String arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Part#setDisposition(java.lang.String)
     */
    public void setDisposition(String arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Part#setFileName(java.lang.String)
     */
    public void setFileName(String arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Message#setFlags(javax.mail.Flags, boolean)
     */
    public void setFlags(Flags arg0, boolean arg1) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Message#setFrom()
     */
    public void setFrom() throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Message#setFrom(javax.mail.Address)
     */
    public void setFrom(Address arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Part#setHeader(java.lang.String, java.lang.String)
     */
    public void setHeader(String arg0, String arg1) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Message#setRecipients(javax.mail.Message.RecipientType, javax.mail.Address[])
     */
    public void setRecipients(RecipientType arg0, Address[] arg1) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Message#setSentDate(java.util.Date)
     */
    public void setSentDate(Date arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Message#setSubject(java.lang.String)
     */
    public void setSubject(String arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Part#setText(java.lang.String)
     */
    public void setText(String arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.mail.Part#writeTo(java.io.OutputStream)
     */
    public void writeTo(OutputStream arg0) throws IOException, MessagingException {
        throw new UnsupportedOperationException();
    }
}