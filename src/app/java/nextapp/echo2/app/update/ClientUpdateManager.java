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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import nextapp.echo2.app.Component;

/**
 * Stores inputs received from the application container and notifies
 * components about them via the  <code>Component.processInput()</code> method.
 * 
 * @see nextapp.echo2.app.Component#processInput(java.lang.String, java.lang.Object)
 */
public class ClientUpdateManager {
     
    private Map clientUpdates = new HashMap();
    
    /**
     * Creates a new <Code>ClientUpdateManager</code>.
     */
    ClientUpdateManager() { }
    
    /**
     * Adds a property update received from the client.
     * 
     * @param component the updated component
     * @param inputName the name of the input property
     * @param inputValue the value of the input property
     */
    void addPropertyUpdate(Component component, String inputName, Object inputValue) {
        ClientComponentUpdate clientUpdate = (ClientComponentUpdate) clientUpdates.get(component);
        if (clientUpdate == null) {
            clientUpdate = new ClientComponentUpdate(component);
            clientUpdates.put(component, clientUpdate);
        }
        clientUpdate.addInput(inputName, inputValue);
    }
    
    /**
     * Retrieves the <code>ClientComponentUpdate</code> object representing
     * the specified <code>Component</code>, or null, if no client updates
     * have been made to the <code>Component</code>.
     * 
     * @param component the <code>Component</code>
     * @return the representing <code>ClientComponentUpdate</code>
     */
    public ClientComponentUpdate getUpdate(Component component) {
        return (ClientComponentUpdate) clientUpdates.get(component); 
    }
    
    /**
     * Purges all updates from the <code>ClientUpdateManager</code>.
     */
    public void purge() {
        clientUpdates.clear();
    }
    
    /**
     * Notifies components of input from the client via the 
     * <code>Component.processInput()</code> method.
     * Purges stored updates.
     * 
     * @see nextapp.echo2.app.Component#processInput(java.lang.String, java.lang.Object)
     */
    void process() {
        Iterator updateIt = clientUpdates.values().iterator();
        while (updateIt.hasNext()) {
            ClientComponentUpdate update = (ClientComponentUpdate) updateIt.next();
            Iterator inputNameIt = update.getInputNames();
            while (inputNameIt.hasNext()) {
                String inputName = (String) inputNameIt.next();
                update.getComponent().processInput(inputName, update.getInputValue(inputName));
            }
        }
    }
}
