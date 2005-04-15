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

package nextapp.echo2.webrender.server;

import java.io.Serializable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * A registry of <code>Service</code> objects that may be recalled based
 * on <code>Id</code> values.
 */
public class ServiceRegistry 
implements Serializable {

    /** Maps service Ids to services */
    private final Map serviceMap = new HashMap();
    
    /** Maps a service to a set of aliases */
    private final Map serviceAliases = new HashMap();

    /**
     * Creates a new <code>ServiceRegistry</code>.
     */
    public ServiceRegistry() {
        super();
    }
    
    /** 
     * Adds a service to the registry.
     *
     * @param service The service to be added.
     */
    public synchronized void add(Service service) {
        if (serviceMap.containsKey(service.getId()) && serviceMap.get(service.getId()) != service) {
            throw new IllegalArgumentException("Identifier already in use by another service.");
        }
        serviceMap.put(service.getId(), service);
    }
    
    /**
     * Adds an aliased service to the registry.  Aliased service are only
     * removed when all of their aliases have been removed.  This enables
     * ancillary services to be used by multiple components.
     *
     * @param service The service to be added.
     * @param alias The alias to use for the service.
     */
    public synchronized void add(Service service, String alias) {
        // Retrieve the service's aliases.
        Set aliases = (Set) serviceAliases.get(service);
        
        if (aliases == null) {
            // The service has no aliases, create the alias set.
            aliases = new HashSet();
            // Store the alias set for this service.
            serviceAliases.put(service, aliases);
        }
        
        if (! aliases.contains(alias)) {
            // The current alias is not yet in use.
        
            // Add the alias to the alias set.
            aliases.add(alias);
            
            // Add the service.
            add(service);
        }
    }
    
    /**
     * Returns the service with the specified <code>Id</code>.
     *
     * @param id The <code>Id</code> of the service to be retrieved.
     * @return The service which is identified by <code>id</code>.
     */
    public Service get(String id) {
        return (Service) serviceMap.get(id);
    }
    
    /** 
     * Removes a service from the registry.
     *
     * @param service The service to be removed.
     */
    public synchronized void remove(Service service) {
        serviceMap.remove(service.getId());
        serviceAliases.remove(service);
    }
    
    /**
     * Removes an aliased service from the registry.  Aliased service are only
     * removed when all of their aliases have been removed.  This enables
     * ancillary services to be used by multiple components.
     *
     * @param service The service to be removed.
     * @param alias The alias of the service.
     */
    public synchronized void remove(Service service, String alias) {
        Set aliases = (Set) serviceAliases.get(service);
        if (aliases == null) {
            // The service has no aliases: remove the service from the registry.
            remove(service);
        } else {
            // The service has aliases: don't remove the service from the
            // registry yet, it might be in use under a different alias.
            aliases.remove(alias);
            
            if (aliases.size() == 0) {
                // The service has had its last alias removed: remove the 
                // service from the registry, delete the alias set.
                remove(service);
                serviceAliases.remove(service);
            }
        }
    }
}
