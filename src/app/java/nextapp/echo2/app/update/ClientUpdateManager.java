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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nextapp.echo2.app.Component;

/**
 * Stores inputs received from the application container and notifies
 * components about them via the  <code>Component.processInput()</code> method.
 * 
 * @see nextapp.echo2.app.Component#processInput(java.lang.String, java.lang.Object)
 */
class ClientUpdateManager {
     
    private List propertyUpdates = new ArrayList();
    
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
        propertyUpdates.add(new ClientComponentUpdate(component, inputName, inputValue));
    }
    
    /**
     * Notifies components of input from the client via the 
     * <code>Component.processInput()</code> method.
     * Purges stored updates.
     * 
     * @see nextapp.echo2.app.Component#processInput(java.lang.String, java.lang.Object)
     */
    void process() {
        Iterator it = propertyUpdates.iterator();
        while (it.hasNext()) {
            ClientComponentUpdate update = (ClientComponentUpdate) it.next();
            update.getComponent().processInput(update.getInputName(), update.getInputValue());
        }
        propertyUpdates.clear();
    }
}
