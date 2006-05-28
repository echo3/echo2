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
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionEvent;

/**
 * An abstract base class representing a single user-instance of an application
 * hosted in an application container.
 */
public abstract class UserInstance 
implements HttpSessionActivationListener, HttpSessionBindingListener, Serializable {
    
    public static final String PROPERTY_CLIENT_CONFIGURATION = "clientConfiguration";
    public static final String PROPERTY_SERVER_DELAY_MESSAGE = "serverDelayMessage";

    /**
     * The default character encoding in which responses should be rendered.
     */
    private String characterEncoding = "UTF-8";
    
    /**
     * <code>ClientConfiguration</code> information containing 
     * application-specific client behavior settings.
     */
    private ClientConfiguration clientConfiguration;

    /**
     * The <code>ServerDelayMessage</code> displayed during 
     * client/server-interactions.
     */
    private ServerDelayMessage serverDelayMessage;
    
    /**
     * A <code>ClientProperties</code> object describing the web browser
     * client.
     */
    private ClientProperties clientProperties;
    
    /**
     * The URI of the servlet.
     */
    private String servletUri;
    
    /**
     * Reference to the <code>HttpSession</code> in which this
     * <code>UserInstance</code> is stored.
     */
    private transient HttpSession session;
    
    /**
     * Provides information about updated <code>UserInstance</code> properties.
     */
    private UserInstanceUpdateManager updateManager;

    /**
     * The current transactionId.  Used to ensure incoming ClientMessages reflect
     * changes made by user against current server-side state of user interface.
     * This is used to eliminate issues that could be encountered with two
     * browser windows pointing at the same application instance.
     */
    private long transactionId = 0;
    
    /**
     * Creates a new <code>UserInstance</code>.
     * 
     * @param conn the client/server <code>Connection</code> for which the
     *        instance is being instantiated
     */
    public UserInstance(Connection conn) {
        super();
        updateManager = new UserInstanceUpdateManager();
        conn.initUserInstance(this);
    }

    /**
     * Returns the default character encoding in which responses should be
     * rendered.
     * 
     * @return the default character encoding in which responses should be
     *         rendered
     */
    public String getCharacterEncoding() {
        return characterEncoding;
    }
    
    /**the <code>ServerDelayMessage</code> displayed during 
     * client/server-interactions.
     * Retrieves the <code>ClientConfiguration</code> information containing
     * application-specific client behavior settings.
     * 
     * @return the relevant <code>ClientProperties</code>
     */
    public ClientConfiguration getClientConfiguration() {
        return clientConfiguration;
    }
    
    /**
     * Retrieves the <code>ClientProperties</code> object providing
     * information about the client of this instance.
     * 
     * @return the relevant <code>ClientProperties</code>
     */
    public ClientProperties getClientProperties() {
        return clientProperties;
    }
    
    /**
     * Returns the current transaction id.
     * 
     * @return the current transaction id
     */
    public long getCurrentTransactionId() {
        return transactionId;
    }
    
    /**
     * Increments the current transaction id and returns it.
     * 
     * @return the current transaction id, after an increment
     */
    public long getNextTransactionId() {
        ++transactionId;
        return transactionId;
    }

    /**
     * Retrieves the <code>ServerDelayMessage</code> displayed during 
     * client/server-interactions.
     * 
     * @return the <code>ServerDelayMessage</code>
     */
    public ServerDelayMessage getServerDelayMessage() {
        return serverDelayMessage;
    }
    
    /**
     * Determines the URI to invoke the specified <code>Service</code>.
     * 
     * @param service the <code>Service</code>
     * @return the URI
     */
    public String getServiceUri(Service service) {
        return servletUri + "?serviceId=" + service.getId();
    }

    /**
     * Determines the URI to invoke the specified <code>Service</code> with
     * additional request parameters. The additional parameters are provided by
     * way of the <code>parameterNames</code> and <code>parameterValues</code>
     * arrays. The value of a parameter at a specific index in the
     * <code>parameterNames</code> array is provided in the
     * <code>parameterValues</code> array at the same index. The arrays must
     * thus be of equal length. Null values are allowed in the
     * <code>parameterValues</code> array, and in such cases only the parameter
     * name will be rendered in the returned URI.
     * 
     * @param service the <code>Service</code>
     * @param parameterNames the names of the additional URI parameters
     * @param parameterValues the values of the additional URI parameters
     * @return the URI
     */
    public String getServiceUri(Service service, String[] parameterNames, String[] parameterValues) {
        StringBuffer out = new StringBuffer(servletUri);
        out.append("?serviceId=");
        out.append(service.getId());
        for (int i = 0; i < parameterNames.length; ++i) {
            out.append("&");
            out.append(parameterNames[i]);
            if (parameterValues[i] != null) {
                out.append("=");
                out.append(parameterValues[i]);
            }
        }
        return out.toString();
    }

    /**
     * Returns the URI of the servlet managing this <code>UserInstance</code>.
     * 
     * @return the URI
     */
    public String getServletUri() {
        return servletUri;
    }

    /**
     * Returns the <code>UserInstanceUpdateManager</code> providing information
     * about updated <code>UserInstance</code> properties.
     * 
     * @return the <code>UserInstanceUpdateManager</code>
     */
    public UserInstanceUpdateManager getUserInstanceUpdateManager() {
        return updateManager;
    }
    
    /**
     * Returns the <code>HttpSession</code> containing this
     * <code>UserInstance</code>.
     * 
     * @return the <code>HttpSession</code>
     */
    public HttpSession getSession() {
        return session;
    }
    
    /**
     * @see javax.servlet.http.HttpSessionActivationListener#sessionDidActivate(javax.servlet.http.HttpSessionEvent)
     */
    public void sessionDidActivate(HttpSessionEvent e) {
        session = e.getSession();
    }

    /**
     * @see javax.servlet.http.HttpSessionActivationListener#sessionWillPassivate(javax.servlet.http.HttpSessionEvent)
     */
    public void sessionWillPassivate(HttpSessionEvent e) {
        session = null;
    }

    /**
     * Sets the <code>ClientConfiguration</code> information containing
     * application-specific client behavior settings.
     * 
     * @param clientConfiguration the new <code>ClientConfiguration</code>
     */
    public void setClientConfiguration(ClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;
        updateManager.processPropertyUpdate(PROPERTY_CLIENT_CONFIGURATION);
    }
    
   /**
     * Stores the <code>ClientProperties</code> object that provides
     * information about the client of this instance.
     * 
     * @param clientProperties the relevant <code>ClientProperties</code>
     */
    void setClientProperties(ClientProperties clientProperties) {
        this.clientProperties = clientProperties;
    }
    
    /**
     * Sets the <code>ServerDelayMessage</code> displayed during 
     * client/server-interactions.
     * 
     * @param serverDelayMessage the new <code>ServerDelayMessage</code>
     */
    public void setServerDelayMessage(ServerDelayMessage serverDelayMessage) {
        this.serverDelayMessage = serverDelayMessage;
        updateManager.processPropertyUpdate(PROPERTY_SERVER_DELAY_MESSAGE);
    }

    /**
     * Sets the URI of the servlet managing this <code>UserInstance</code>.
     * 
     * @param servletUri the URI
     */
    void setServletUri(String servletUri) {
        this.servletUri = servletUri;
    }

    /**
     * Listener implementation of <code>HttpSessionBindingListener</code>.
     * Stores reference to session when invoked.
     * 
     * @see javax.servlet.http.HttpSessionBindingListener#valueBound(HttpSessionBindingEvent)
     */
    public void valueBound(HttpSessionBindingEvent e) {
        session = e.getSession();
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
