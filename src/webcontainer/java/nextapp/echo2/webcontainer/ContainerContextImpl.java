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

package nextapp.echo2.webcontainer;

import java.io.Serializable;
import java.security.Principal;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import nextapp.echo2.app.TaskQueueHandle;
import nextapp.echo2.webrender.ClientConfiguration;
import nextapp.echo2.webrender.ClientProperties;
import nextapp.echo2.webrender.Connection;
import nextapp.echo2.webrender.WebRenderServlet;

//BUGBUG? Forced session invalidation.

/**
 * <code>ContainerContext</code> implementation.
 */
class ContainerContextImpl 
implements ContainerContext, Serializable {
    
    private ContainerInstance containerInstance;
    
    /**
     * Creates a new <code>ContainerContextImpl</code>
     * 
     * @param containerInstance the relevant <code>ContainerInstance</code>
     */
    ContainerContextImpl(ContainerInstance containerInstance) {
        super();
        this.containerInstance = containerInstance;
    }

    /**
     * @see nextapp.echo2.webcontainer.ContainerContext#getClientProperties()
     */
    public ClientProperties getClientProperties() {
        return containerInstance.getClientProperties();
    }
    
    /**
     * @see nextapp.echo2.webcontainer.ContainerContext#getCookies()
     */
    public Cookie[] getCookies() {
        Connection conn = WebRenderServlet.getActiveConnection();
        if (conn == null) {
            return null;
        } else {
            return conn.getRequest().getCookies();
        }
    }
    
    /**
     * @see nextapp.echo2.webcontainer.ContainerContext#getInitialRequestParameterMap()
     */
    public Map getInitialRequestParameterMap() {
        return containerInstance.getInitialRequestParameterMap();
    }
    
    /**
     * @see nextapp.echo2.webcontainer.ContainerContext#getSession()
     */
    public HttpSession getSession() {
        return containerInstance.getSession();
    }
    
    /**
     * @see nextapp.echo2.webcontainer.ContainerContext#getUserPrincipal()
     */
    public Principal getUserPrincipal() {
        Connection conn = WebRenderServlet.getActiveConnection();
        if (conn == null) {
            return null;
        } else {
            return conn.getRequest().getUserPrincipal();
        }
    }
    
    /**
     * @see nextapp.echo2.webcontainer.ContainerContext#isUserInRole(java.lang.String)
     */
    public boolean isUserInRole(String role) {
        Connection conn = WebRenderServlet.getActiveConnection();
        if (conn == null) {
            return false;
        } else {
            return conn.getRequest().isUserInRole(role);
        }
    }
    
    /**
     * @see nextapp.echo2.webcontainer.ContainerContext#setClientConfiguration(nextapp.echo2.webrender.ClientConfiguration)
     */
    public void setClientConfiguration(ClientConfiguration clientConfiguration) {
        containerInstance.setClientConfiguration(clientConfiguration);
    }

    /**
     * @see nextapp.echo2.webcontainer.ContainerContext#setTaskQueueCallbackInterval(nextapp.echo2.app.TaskQueueHandle, int)
     */
    public void setTaskQueueCallbackInterval(TaskQueueHandle taskQueue, int ms) {
        containerInstance.setTaskQueueCallbackInterval(taskQueue, ms);
    }
}
