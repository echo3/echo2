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

package nextapp.echo2.webrender;

import java.io.Serializable;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

//BUGBUG. Javadocs.

public abstract class UserInstance
implements HttpSessionBindingListener, Serializable {
    
    private transient ServiceRegistry services = new ServiceRegistry();
    private String applicationUri;
    private String characterEncoding = "UTF-8";
    private ClientProperties clientProperties;
    private transient HttpSession session;

    public UserInstance() {
        super();
    }
    
    public String getApplicationUri() {
        return applicationUri;
    }
    
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    /**
     * Retrieves the <code>ClientProperties</code> object providing information
     * about the client of this instance.
     * 
     * @return the relevant <code>ClientProperties</code>
     */
    public ClientProperties getClientProperties() {
        return clientProperties;
    }
    
    public String getServiceUri(Service service) {
        return applicationUri + "?serviceId=" + service.getId();
    }
    
    public String getServiceUri(Service service, String[] parameterNames, String[] parameterValues) {
        StringBuffer out = new StringBuffer(applicationUri);
        out.append("?serviceId=");
        out.append(service.getId());
        for (int i = 0; i < parameterNames.length; ++i) {
            //BUGBUG. Need to URL encode parameters.
            out.append("&");
            out.append(parameterNames[i]);
            out.append("=");
            out.append(parameterValues[i]);
        }
        return out.toString();
    }
    
    /**
     * Returns the <code>ServiceRegistry</code> that is used by this instance
     * to hold all <code>Service</code> objects available to it.
     *
     * @return This instance's <code>ServiceRegistry</code>.
     */
    public ServiceRegistry getServiceRegistry() {
        return services;
    }
    
    /**
     * Returns the <code>HttpSession</code> containing this <code>UserInstance</code>.
     * 
     * @return the <code>HttpSession</code>
     */
    public HttpSession getSession() {
        return session;
    }
    
    public void setApplicationUri(String applicationUri) {
        this.applicationUri = applicationUri;
    }
    
    /**
     * Stores the <code>ClientProperties</code> object that provides 
     * information about the client of this instance.
     * 
     * @param clientProperties the relevant <code>ClientProperties</code>
     */
    public void setClientProperties(ClientProperties clientProperties) {
        this.clientProperties = clientProperties;
    }
    
    /**
     * Listener implementation of <code>HttpSessionBindingListener</code>.
     * Stores reference to session when invoked.
     *
     * @see javax.servlet.http.HttpSessionBindingListener#valueBound(HttpSessionBindingEvent)
     */
    public void valueBound(HttpSessionBindingEvent e) {
        session = (HttpSession) e.getSource();
    }
    
    /**
     * Listener implementation of <code>HttpSessionBindingListener</code>.
     * Removes reference to session when invoked.
     *
     * @see javax.servlet.http.HttpSessionBindingListener#valueUnbound(HttpSessionBindingEvent)
     */
    public void valueUnbound(HttpSessionBindingEvent e) {
        session = null;
    }
}
