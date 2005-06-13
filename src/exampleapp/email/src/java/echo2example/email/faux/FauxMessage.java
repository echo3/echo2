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
 * 
 */
public class FauxMessage extends Message {
    
    private Address[] from;
    private String subject;
    private String content;
    private Date receivedDate;
    private Address[] to, cc, bcc;
    
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

    public void addFrom(Address[] from) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public void addHeader(String arg0, String arg1) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public void addRecipients(RecipientType arg0, Address[] arg1) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public Enumeration getAllHeaders() throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public Object getContent() throws IOException, MessagingException {
        return content;
    }

    public String getContentType() throws MessagingException {
        return "text/plain";
    }

    public DataHandler getDataHandler() throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public String getDescription() throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public String getDisposition() throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public String getFileName() throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public Flags getFlags() throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public Address[] getFrom() throws MessagingException {
        return from;
    }

    public String[] getHeader(String arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public InputStream getInputStream() throws IOException, MessagingException {
        throw new UnsupportedOperationException();
    }

    public int getLineCount() throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public Enumeration getMatchingHeaders(String[] arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public Enumeration getNonMatchingHeaders(String[] arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public Date getReceivedDate() throws MessagingException {
        return receivedDate;
    }

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

    public Date getSentDate() throws MessagingException {
        return receivedDate;
    }

    public int getSize() throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public String getSubject() throws MessagingException {
        return subject;
    }

    public boolean isMimeType(String arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public void removeHeader(String arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public Message reply(boolean arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public void saveChanges() throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public void setContent(Multipart arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public void setContent(Object arg0, String arg1) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public void setDataHandler(DataHandler arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public void setDescription(String arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public void setDisposition(String arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public void setFileName(String arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public void setFlags(Flags arg0, boolean arg1) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public void setFrom() throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public void setFrom(Address arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public void setHeader(String arg0, String arg1) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public void setRecipients(RecipientType arg0, Address[] arg1) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public void setSentDate(Date arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public void setSubject(String arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public void setText(String arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public void writeTo(OutputStream arg0) throws IOException, MessagingException {
        throw new UnsupportedOperationException();
    }
}