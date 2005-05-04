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

package nextapp.echo2.app.update;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Component;

/**
 * Primary interface to update management architecture.
 */
public class UpdateManager {

    private ClientUpdateManager clientUpdateManager;
    private ServerUpdateManager serverUpdateManager;
    private ApplicationInstance applicationInstance;
    
    /**
     * Creates a new <code>UpdateManager</code>.
     * <strong>Warning:</strong> the creator must take care to invoke the
     * <code>dispose()</code> method before dereferencing this object.
     * 
     * @param applicationInstance the <code>ApplicationInstance</code> which
     *        this manager will service
     */
    public UpdateManager(ApplicationInstance applicationInstance) {
        super();
        this.applicationInstance = applicationInstance;
        clientUpdateManager = new ClientUpdateManager();
        serverUpdateManager = new ServerUpdateManager();
        
        serverUpdateManager.init(clientUpdateManager);
    }
    
    /**
     * Adds an client property update describing a change made to an 
     * input property on the client.
     * 
     * @param component the updated component
     * @param inputName the name of the input property
     * @param inputValue the value of the input property
     */
    public void addClientPropertyUpdate(Component component, String inputName, Object inputValue) {
        clientUpdateManager.addPropertyUpdate(component, inputName, inputValue);
    }
    
    /**
     * Returns the <code>ClientUpdateManager</code>, which is responsible for 
     * queueing and processing updates received from the client. 
     * 
     * @return the <code>ClientUpdateManager</code>
     */
    public ClientUpdateManager getClientUpdateManager() {
        return clientUpdateManager;
    }
    
    /**
     * Retrieves all <code>ServerComponentUpdate</code>s.
     * The state of the server updates in unchanged by this operation (they
     * are not purged).  The updates will be returned
     * sorted by depth of their parent components within the hierarchy, but in 
     * otherwise random order.
     * 
     * @return the <code>ServerComponentUpdate<code>s.
     */
    public ServerComponentUpdate[] getServerComponentUpdates() {
        return serverUpdateManager.getComponentUpdates();
    }
    
    /**
     * Returns the <code>ServerUpdateManager</code>, which is responsible for
     * queueing server-side updates and rendering them to the client.
     * 
     * @return the <code>ServerUpdateManager</code>
     */
    public ServerUpdateManager getServerUpdateManager() {
        return serverUpdateManager;
    }
    
    /**
     * Processes client updates, purging them automatically at completion.
     * Validates the component hierarchy for rendering in response to updates.
     */
    public void processClientUpdates() {
        clientUpdateManager.process();
        
        // Validate the state of the hierarchy prior to rendering.
        applicationInstance.doValidation();
    }
    
    /**
     * Purges all client and server updates.
     */
    public void purge() {
        clientUpdateManager.purge();
        serverUpdateManager.purge();
    }

    /**
     * Sets the action received from the client.  The 'action' describes the
     * client-side update which necessitated the occurrence of this 
     * client-server interaction.  The application will be notified of the 
     * action AFTER it has been notified of all other property updates.
     * 
     * @param actionComponent the action-producing component
     * @param actionName the name of the action
     * @param actionValue the value of the action
     */
    public void setClientAction(Component actionComponent, String actionName, Object actionValue) {
        clientUpdateManager.setAction(actionComponent, actionName, actionValue);
    }
}
