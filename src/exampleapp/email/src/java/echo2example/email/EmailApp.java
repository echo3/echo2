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

import java.util.Properties;
import java.util.ResourceBundle;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Window;

/**
 * Email Application Instance.
 */
public class EmailApp extends ApplicationInstance {

    /**
     * Flag indicating whether we should use fake e-mail data.  Enabling this 
     * flag will cause the <code>FauxStore</code> e-mail store to be used and
     * will disable sending of messages. 
     */
    public static final boolean FAUX_MODE;
    public static final String MAIL_DOMAIN, RECEIVE_MAIL_SERVER, RECEIVE_PROTOCOL, SEND_MAIL_SERVER, SEND_MAIL_PORT;
    public static final int MESSAGES_PER_PAGE;

    static {
        // Static initializer to retrieve information from configuration properties file.
        ResourceBundle config = ResourceBundle.getBundle("/echo2example/email/Configuration");
        FAUX_MODE = "true".equals(config.getString("FauxMode"));
        MAIL_DOMAIN = config.getString("MailDomain");
        RECEIVE_MAIL_SERVER = config.getString("ReceiveMailServer");
        RECEIVE_PROTOCOL = config.getString("ReceiveProtocol");
        SEND_MAIL_SERVER = config.getString("SendMailServer");
        SEND_MAIL_PORT = config.getString("SendMailPort");
        MESSAGES_PER_PAGE = Integer.parseInt(config.getString("MessagesPerPage"));
    }
    
    /**
     * The user name of the currently logged in user.  This property
     * will be null when no user is logged in.
     */
    private String emailAddress;
    
    /**
     * The <code>javax.mail.Store</code> used to retrieve messages from the mail server.
     * See the Sun JavaMail API Specification for details.
     */
    private Store store;
    
    /**
     * The <code>javax.mail.Session</code> used to connect ot he mail server.
     * See the Sun JavaMail API Specification for details.
     */
    private Session mailSession;
    
    /**
     * Convenience method to return the active email application as a
     * <code>EmailApp</code>.
     * 
     * @return the active <code>EmailApp</code>
     */
    public static EmailApp getApp() {
        return (EmailApp) getActive();
    }
    
    /**
     * Connects to the mail server with the given e-mail address and
     * password.   Displays the <code>MailScreen</code> on success.
     *
     * @param emailAddress e-mail address 
     * @param password the password
     * @return true if the application was able to connect to the
     *         server using the specified information, false if not.
     */
    public boolean connect(String emailAddress, String password) {
        Properties properties = System.getProperties();
        if (!FAUX_MODE) {
            properties.put("mail.smtp.host", SEND_MAIL_SERVER);
            properties.put("mail.smtp.port", SEND_MAIL_PORT);
        }
        try {
            mailSession = Session.getDefaultInstance(properties, null);
            store = mailSession.getStore(RECEIVE_PROTOCOL);
            store.connect(RECEIVE_MAIL_SERVER, emailAddress, password);

            // Store user name.
            this.emailAddress = emailAddress;
            
            // Display MailScreen.
            MailScreen mailScreen = new MailScreen();
            mailScreen.setStore(store);
            getDefaultWindow().setContent(mailScreen);
        } catch (AuthenticationFailedException ex) {
            // Return false to indicate the user was not successfully authenticated.
            return false;
        } catch (MessagingException ex) {
            processFatalException(ex);
        }
        
        // Return indicating that the user was successfully authenticated.
        return true;
    }
    
    /**
     * Disconnects the session with the mail server and displays
     * the authentication screen.
     */
    public void disconnect() {
        if (store != null) {
            try {
                store.close();
            } catch (MessagingException ex) {
                // Do nothing.
            }
            store = null;
        }
        
        emailAddress = null;
        getDefaultWindow().setContent(new LoginScreen());
    }

    /**
     * Returns the email address of the active user, or null if none.
     * 
     * @return the email address
     */
    public String getEmailAddress() {
        return emailAddress;
    }
    
    /**
     * Returns the active JavaMail <code>Session</code> object.
     * 
     * @return the <code>Session</code>
     */
    public Session getMailSession() {
        return mailSession;
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
     * Handles a fatal exception.
     * This method is invoked when a component of the application 
     * encounters a fatal error that can not be resolved.  This
     * method will log off any currently logged in user and 
     * display an error dialog screen.
     *
     * @param ex the fatal exception
     */
    public void processFatalException(Exception ex) {
        ex.printStackTrace();        
        disconnect();
        MessageDialog messageDialog = new MessageDialog(Messages.getString("FatalException.Title"), ex.toString(), 
                MessageDialog.TYPE_ERROR, MessageDialog.CONTROLS_OK);
        getDefaultWindow().getContent().add(messageDialog);
    }
}
