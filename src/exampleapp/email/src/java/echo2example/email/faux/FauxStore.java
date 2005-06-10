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

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

/**
 * A minimal implementation of a JavaMail <code>Store</code> to serve the
 * requirements of the e-mail application.
 */
public class FauxStore extends Store {
    
    FauxFolder rootFolder;
    FauxFolder inboxFolder;
    
    /**
     * Creates a <code>FauxStore</code>.
     * 
     * @param session the <code>Sesssion</code> object for this 
     *        <code>Store</code>
     * @param urlName the <code>URLName</code> object for this 
     *        <code>Store</code>
     */
    public FauxStore(Session session, URLName urlName) {
        super(session, urlName);
        try {
            rootFolder = FauxFolder.createRoot(this);
            inboxFolder = FauxFolder.createInbox(this);
            MessageGenerator generator = new MessageGenerator(session);
            for (int i = 0; i < 500; ++i) {
                inboxFolder.appendMessages(new Message[]{generator.generateMessage()});
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @see javax.mail.Store#getDefaultFolder()
     */
    public Folder getDefaultFolder() throws MessagingException {
        return rootFolder;
    }
    
    /**
     * @see javax.mail.Store#getFolder(java.lang.String)
     */
    public Folder getFolder(String arg0) throws MessagingException {
        return null;
    }
    
    /**
     * @see javax.mail.Store#getFolder(javax.mail.URLName)
     */
    public Folder getFolder(URLName arg0) throws MessagingException {
        return null;
    }

    /**
     * @see javax.mail.Service#protocolConnect(java.lang.String, int, java.lang.String, java.lang.String)
     */
    protected boolean protocolConnect(String arg0, int arg1, String arg2, String arg3) 
    throws MessagingException {
        return true;
    }
}
