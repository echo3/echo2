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

import java.util.HashMap;
import java.util.Map;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.update.UpdateManager;
import nextapp.echo2.webrender.RenderInstance;
import nextapp.echo2.webrender.server.Connection;

/**
 * Web application container user instance.
 */
public class ContainerInstance extends RenderInstance {
    
    /**
     * Creates a new Web Application Container instance using the provided
     * client <code>Connection</code>.  The instance will automatically
     * be stored in the relevant <code>HttpSession</code>
     */
    public static void newInstance(Connection conn) {
        new ContainerInstance(conn);
    }
    
    private Map componentToRenderStateMap = new HashMap();
    private ApplicationInstance applicationInstance;
    
    /**
     * Default constructor for serialization use only.
     * 
     * @see #newInstance(nextapp.echo2.webrender.server.Connection)
     */
    public ContainerInstance() {
        super();
    }
    
    /**
     * Creates a new <code>ContainerInstance</code>.
     * 
     * @see #newInstance(nextapp.echo2.webrender.server.Connection)
     */
    private ContainerInstance(Connection conn) {
        WebContainerServlet servlet = (WebContainerServlet) conn.getServlet();
        applicationInstance = servlet.newApplicationInstance();
        
        getServiceRegistry().add(WindowHtmlService.INSTANCE);

        conn.setUserInstance(this);
        
        applicationInstance.setContextProperty(ContainerContext.CONTEXT_PROPERTY_NAME, 
                new ContainerContextImpl(this));
        
        //BUGBUG. It is probably better if the app be initialized in response to the initial
        // sync service, rather than on the initial invocation.
        // Note that this code doesn't necessarily move..just the place from where it's invoked.
        try {
            ApplicationInstance.setActive(applicationInstance);
            applicationInstance.doInit();
        } finally {
            ApplicationInstance.setActive(null);
        }
    }

    /**
     * Returns the corresponding <code>ApplicationInstance</code>
     * for this user instance.
     * 
     * @return the relevant <code>ApplicationInstance</code>
     */
    public ApplicationInstance getApplicationInstance() {
        return applicationInstance;
    }
    
    /**
     * Retrieves the <code>RenderState</code> of the specified
     * <code>Component</code>.
     * 
     * @param component the component
     * @return the rendering state
     */
    public RenderState getRenderState(Component component) {
        return (RenderState) componentToRenderStateMap.get(component);
    }
    
    //BUGBUG. now a convenience method....redoc at minimum.
    /**
     * Returns the <code>UpdateManager</code> being used to synchronize
     * client and server states.
     * 
     * @return the <code>UpdateManager</code>
     */
    public UpdateManager getUpdateManager() {
        return applicationInstance.getUpdateManager();
    }
    
    /**
     * Removes the <code>RenderState</code> of the specified
     * <code>Component</code>.
     * 
     * @param component the component
     */
    public void removeRenderState(Component component) {
        componentToRenderStateMap.remove(component);
    }
    
    /**
     * Sets the <code>RenderState</code> of the specified 
     * <code>Component</code>.
     * 
     * @param component the component
     * @param renderState the render state
     */
    public void setRenderState(Component component, RenderState renderState) {
        componentToRenderStateMap.put(component, renderState);
    }
}
