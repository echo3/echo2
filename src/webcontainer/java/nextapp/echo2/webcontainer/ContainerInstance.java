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
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.TaskQueueHandle;
import nextapp.echo2.app.update.UpdateManager;
import nextapp.echo2.webcontainer.util.IdTable;
import nextapp.echo2.webrender.Connection;
import nextapp.echo2.webrender.UserInstance;

/**
 * Web application container user instance.
 */
public class ContainerInstance extends UserInstance {
    
    /**
     * Default asynchronous monitor callback interval (in milliseconds).
     */
    private static final int DEFAULT_CALLBACK_INTERVAL = 500;
    
    /**
     * Returns the base HTML element id that should be used when rendering the
     * specified <code>Component</code>.
     * 
     * @param component the component 
     * @return the base HTML element id
     */
    public static String getElementId(Component component) {
        return "c_" + component.getRenderId();
    }
    
    /**
     * Creates a new Web Application Container instance using the provided
     * client <code>Connection</code>.  The instance will automatically
     * be stored in the relevant <code>HttpSession</code>
     * 
     * @param conn the client/server <code>Connection</code> for which the 
     *        instance is being instantiated
     */
    public static void newInstance(Connection conn) {
        new ContainerInstance(conn);
    }
    
    private ApplicationInstance applicationInstance;
    private Map componentToRenderStateMap = new HashMap();
    private Map taskQueueToCallbackIntervalMap;
    private transient IdTable idTable;
    private Map initialRequestParameterMap;
    
    /**
     * Creates a new <code>ContainerInstance</code>.
     * 
     * @param conn the client/server <code>Connection</code> for which the 
     *        instance is being instantiated
     * @see #newInstance(nextapp.echo2.webrender.Connection)
     */
    private ContainerInstance(Connection conn) {
        super(conn);
        WebContainerServlet servlet = (WebContainerServlet) conn.getServlet();
        initialRequestParameterMap = new HashMap(conn.getRequest().getParameterMap());
        applicationInstance = servlet.newApplicationInstance();
        
        applicationInstance.setContextProperty(ContainerContext.CONTEXT_PROPERTY_NAME, 
                new ContainerContextImpl(this));
        
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
    
    //BUGBUG. current method of iterating weak-keyed map of task queues
    // is not adequate.  If the application were to for whatever reason hold on
    // to a dead task queue, its interval setting would effect the
    // calculation.
    // One solution: add a getTaskQueues() method to ApplicationInstance
    /**
     * Determines the application-specified asynchronous monitoring
     * service callback interval.
     * 
     * @return the callback interval, in ms
     */
    public int getCallbackInterval() {
        if (taskQueueToCallbackIntervalMap == null || taskQueueToCallbackIntervalMap.size() == 0) {
            return DEFAULT_CALLBACK_INTERVAL;
        }
        Iterator it = taskQueueToCallbackIntervalMap.values().iterator();
        int returnInterval = Integer.MAX_VALUE;
        while (it.hasNext()) {
            int interval = ((Integer) it.next()).intValue();
            if (interval < returnInterval) {
                returnInterval = interval;
            }
        }
        return returnInterval;
    }
    
    /**
     * Retrieves the <code>Component</code> with the specified element id.
     * 
     * @param elementId the element id, e.g., "c_42323"
     * @return the component (e.g., the component whose id is "42323")
     */
    public Component getComponentByElementId(String elementId) {
        try {
            return applicationInstance.getComponentByRenderId(elementId.substring(2));
        } catch (IndexOutOfBoundsException ex) {
            throw new IllegalArgumentException("Invalid component element id: " + elementId);
        }
    }
    
    /**
     * Retrieves the <code>IdTable</code> used by this 
     * <code>ContainerInstance</code> to assign weakly-referenced unique 
     * identifiers to arbitrary objects.
     * 
     * @return the <code>IdTable</code>
     */
    public IdTable getIdTable() {
        if (idTable == null) {
            idTable = new IdTable();
        }
        return idTable;
    }
    
    /**
     * Returns an immutable <code>Map</code> containing the HTTP form 
     * parameters sent on the initial request to the application.
     * 
     * @return the initial request parameter map
     */
    public Map getInitialRequestParameterMap() {
        return initialRequestParameterMap;
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
    
    /**
     * Convenience method to retrieve the application's 
     * <code>UpdateManager</code>, which is used to synchronize
     * client and server states.
     * This method is equivalent to invoking
     * <code>getApplicationInstance().getUpdateManager()</code>.
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
    
    /**
     * Sets the interval between asynchronous callbacks from the client to check
     * for queued tasks for a given <code>TaskQueue</code>.  If multiple 
     * <code>TaskQueue</code>s are active, the smallest specified interval should
     * be used.  The default interval is 500ms.
     * Application access to this method should be accessed via the 
     * <code>ContainerContext</code>.
     * 
     * @param taskQueue the <code>TaskQueue</code>
     * @param ms the number of milliseconds between asynchronous client 
     *        callbacks
     * @see nextapp.echo2.webcontainer.ContainerContext#setTaskQueueCallbackInterval(nextapp.echo2.app.TaskQueueHandle, int)
     */
    public void setTaskQueueCallbackInterval(TaskQueueHandle taskQueue, int ms) {
        if (taskQueueToCallbackIntervalMap == null) {
            taskQueueToCallbackIntervalMap = new WeakHashMap();
        }
        taskQueueToCallbackIntervalMap.put(taskQueue, new Integer(ms));
    }
}
