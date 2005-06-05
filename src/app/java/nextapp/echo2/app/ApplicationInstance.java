/* 
 * This file is part of the Echo Web Application Framework (hereinafter "Echo").
 * Copyright (C) 2002-2004 NextApp, Inc.
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

package nextapp.echo2.app;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import nextapp.echo2.app.update.ServerUpdateManager;
import nextapp.echo2.app.update.UpdateManager;
import nextapp.echo2.app.util.Uid;

/**
 * A single user-instance of an Echo application.
 */
public abstract class ApplicationInstance 
implements Serializable {

    /** The name and version of the Echo API in use. */
    public static final String ID_STRING = "NextApp Echo v2.0alpha14";

    /** 
     * Holds a thread local reference to the active ApplicationInstance for
     * easy retrieval.
     */ 
    private static final ThreadLocal activeInstance = new InheritableThreadLocal();

    public static final String FOCUSED_COMPONENT_CHANGED_PROPERTY = "focusedComponent";
    public static final String MODAL_COMPONENTS_CHANGED_PROPERTY = "modalComponents";
    public static final String LOCALE_CHANGED_PROPERTY = "locale";
    public static final String WINDOWS_CHANGED_PROPERTY = "windows";
    
    /**
     * Generates a system-level identifier (an identifier which is not simply unique to
     * a single <code>ApplicationInstance</code>).
     * 
     * @return the generated identifier
     */
    public static final String generateSystemId() {
        return Uid.generateUidString();
    }
    
    /**
     * Returns a reference to the <code>ApplicationInstance</code> that is 
     * relevant to the current thread.
     * 
     * @return The relevant <code>ApplicationInstance</code>.
     */
    public static final ApplicationInstance getActive() {
        return (ApplicationInstance) activeInstance.get();
    }

    /**
     * Returns a reference to the <code>ApplicationInstance</code> that is 
     * relevant to the current thread.
     * 
     * @param applicationInstance The relevant <code>ApplicationInstance</code>.
     */
    public static final void setActive(ApplicationInstance applicationInstance) {
        activeInstance.set(applicationInstance);
    }

    /**
     * The presently focused component.
     */
    private WeakReference focusedComponent;

    /** 
     * The default locale of the component. 
     */
    Locale locale;

    /** 
     * Contextual data 
     */
    private Map context;
    
    /**
     * Mapping between the ids of all registered components and the components
     * themselves.
     */
    private Map idToComponentMap;
    
    /**
     * Mapping between <code>TaskQueue</code> handles and <code>List</code>s
     * of tasks.  Values may be null if a particular <code>TaskQueue</code>
     * does not contain any tasks. 
     */
    private HashMap taskQueueMap;
    
    /**
     * Fires property change events for the instance object.
     */
    private PropertyChangeSupport propertyChangeSupport;

    /**
     * The <code>UpdateManager</code> handling updates from this application.
     */
    private UpdateManager updateManager;
    
    /**
     * The set of all windows registered with the application.
     */
    private List windows;
    
    /**
     * The <code>StyleSheet</code> used by the application.
     */
    private StyleSheet styleSheet;

    private List modalComponents;
    
    private long nextId;
    
    /** 
     * Creates an <code>ApplicationInstance</code>. 
     */
    public ApplicationInstance() {
        super();
        propertyChangeSupport = new PropertyChangeSupport(this);
        updateManager = new UpdateManager(this);
        idToComponentMap = new HashMap();
        windows = new ArrayList();
        taskQueueMap = new HashMap();
    }
    
    /**
     * Adds a <code>PropertyChangeListener</code> to receive notification of
     * application-level property changes.
     * 
     * @param l the listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }
    
    /**
     * Adds a top-level window.
     * 
     * <strong>Current support is limited to a single top-level window.</strong>
     */
    private void addWindow(Window window) {
        if (windows.size() > 0) {
            throw new UnsupportedOperationException("Current support is limited to a single top-level window.");
        }

        if (!windows.contains(window)) {
            windows.add(window);
            window.setApplicationInstance(this);
            firePropertyChange(WINDOWS_CHANGED_PROPERTY, null, window);
        }
    }
    
    /**
     * Creates a new task queue.  A handle object representing the created task
     * queue is returned.
     * 
     * @return the new task queue handle
     */
    public TaskQueue createTaskQueue() {
        TaskQueue taskQueue = new TaskQueue() { };
        synchronized (taskQueueMap) {
            taskQueueMap.put(taskQueue, null);
        }
        return taskQueue;
    }
    
    /**
     * Initializes the <code>ApplicationInstance</code>.
     * This method should be invoked from the application container.
     * 
     * @return the initial <code>Window</code> of the application
     */
    public Window doInit() {
        Window mainWindow = init();
        addWindow(mainWindow);
        doValidation();
        return mainWindow;
    }
    
    /**
     * Validates all components registered with the application.
     */
    public final void doValidation() {
        for (Iterator it = windows.iterator(); it.hasNext();) {
            doValidation((Window) it.next());
        }
    }
    
    /**
     * Validates a single component and then recursively validates its 
     * children.
     *
     * @param c The component to be validated.
     */
    private void doValidation(Component c) {
        c.validate();
        int size = c.getComponentCount();
        for (int index = 0; index < size; ++index) {
            doValidation(c.getComponent(index));
        }
    }

    /**
     * Queues the given <code>Command</code> for execution on the next server
     * update. 
     */
    public void enqueueCommand(Command command) {
        updateManager.getServerUpdateManager().enqueueCommand(command);
    }
    
    /**
     * Enqueues a task to be run on during the next client/server 
     * synchronization.  The task will be run 
     * <b>synchronously</b> in the UI processing thread.
     * Enqueuing a task in response to an external event will result 
     * in changes being pushed to the client.
     * 
     * @param taskQueue the <code>TaskQueue</code> handle representing the
     *        queue into which this task should be placed
     * @param task the task to run when on client/server synchronization
     *        
     */
    public void enqueueTask(TaskQueue taskQueue, Runnable task) {
        synchronized (taskQueueMap) {
            List taskList = (List) taskQueueMap.get(taskQueue);
            if (taskList == null) {
                taskList = new ArrayList();
                taskQueueMap.put(taskQueue, taskList);
            }
            taskList.add(task);
        }
    }
    
    /**
     * Reports a bound property change.
     *
     * @param propertyName the name of the changed property
     * @param oldValue the previous value of the property
     * @param newValue the present value of the property
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
    
    /**
     * Generates an application-wide unique identifier.
     * 
     * @return the unique identifer
     */
    public String generateId() {
        return Long.toString(nextId++);
    }
    
    /**
     * Returns a contextual property object.
     * Contextual properties can be used to provide an interface
     * to an application container (without creating a dependency
     * to it from the component framework).
     * 
     * @param propertyName the name of the object
     * @return the object
     */
    public Object getContextProperty(String propertyName) {
        return context == null ? null : context.get(propertyName);
    }

    /**
     * Retrieves the component currently registered with the application 
     * with the specified <code>id</code>.
     * 
     * @param id the id of the component
     * @return the component (or null if no component with the specified
     *         <code>id</code> is registered)
     */
    public Component getComponent(String id) {
        return (Component) idToComponentMap.get(id);
    }
    
    /**
     * Returns the presently focused component, if known.
     * 
     * @return the focused component
     */
    public Component getFocusedComponent() {
        if (focusedComponent == null) {
            return null;
        } else {
            return (Component) focusedComponent.get();
        }
    }
    
    /**
     * Returns the application instance's default locale.
     *
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }
    
    /**
     * Retrieves the root component of the current modal context, or null
     * if no modal context exists.  Components which are not within the 
     * descendant hierarchy of the modal context are barred from receiving
     * user input.
     * 
     * @return the root component of the modal context
     */
    public Component getModalContextRoot() {
        if (modalComponents == null || modalComponents.size() == 0) {
            return null;
        } else {
            return (Component) modalComponents.get(modalComponents.size() - 1);
        }
    }
    
    /**
     * Retrieves the style for the specified specified class of 
     * component / style name.
     * 
     * @param componentClass the component <code>Class</code>
     * @param styleName the component's specified style name
     * @return the appropriate application-wide style, or null
     *         if none exists
     */
    public Style getStyle(Class componentClass, String styleName) {
        if (styleSheet == null) {
            return null;
        } else {
            return styleSheet.getStyle(componentClass, styleName);
        }
    }

    /**
     * Retrieves the <code>UpdateManager</code> monitoring updates to the state
     * of this application instance for client/server synchronization.
     * 
     * @return the <code>UpdateManager</code>
     */
    public UpdateManager getUpdateManager() {
        return updateManager;
    }
    
    /**
     * Returns an array of all windows registered with the application 
     * instance.
     *
     * @return an array of all windows registered with the application 
     * instance
     */
    public Window[] getWindows() {
        return (Window[]) windows.toArray(new Window[windows.size()]);
    }
    
    /**
     * Determines if this application instance currently has any active
     * tasks queues, which might be monitoring external events.
     * 
     * @return true if the instance has any task queues
     */
    public boolean hasTaskQueues() {
        return taskQueueMap.size() > 0;
    }
    
    /**
     * Determines if there are any queued tasks in any of the task
     * queues associated with this application instance.
     * 
     * @return true if any tasks are queued
     */
    public boolean hasQueuedTasks() {
        if (taskQueueMap.size() == 0) {
            return false;
        }
        Iterator it = taskQueueMap.values().iterator();
        while (it.hasNext()) {
            List taskList = (List) it.next();
            if (taskList != null && taskList.size() > 0) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Determines if the given component is modal (i.e., that only components
     * below it in the hierarchy should be enabled).
     * 
     * @param component the <code>Component</code>
     * @return true if the <code>Component</code> is modal 
     */
    private boolean isModal(Component component) {
        return modalComponents != null && modalComponents.contains(component);
    }
    
    /**
     * Invoked to initialize the application, returning the primary window.
     * The returned window must be visible.
     *
     * @return the primary window of the application
     */
    public abstract Window init();
    
    //BUGBUG? notifyComponentPropertyChange....
    //        This is part of the new direct-invocation-of-updates architecture.
    //        This needs to be carefully looked into in the interest of ensuring
    //        that this move was not a bad decision.
    /**
     * Notifies the update manager in response to a component property change 
     * or child addition/removal.
     * 
     * @param parent the parent/updated component
     * @param propertyName the name of the property changed
     * @param oldValue the previous value of the property 
     *        (or the removed component in the case of a
     *        <code>CHILDREN_CHANGED_PROPERTY</code>)
     * @param newValue the new value of the property 
     *        (or the added component in the case of a
     *        <code>CHILDREN_CHANGED_PROPERTY</code>)
     */
    void notifyComponentPropertyChange(Component parent, String propertyName, Object oldValue, Object newValue) {
        ServerUpdateManager serverUpdateManager = updateManager.getServerUpdateManager();
        if (Component.CHILDREN_CHANGED_PROPERTY.equals(propertyName)) {
            if (newValue == null) {
                serverUpdateManager.processComponentRemove(parent, (Component) oldValue);
            } else {
                serverUpdateManager.processComponentAdd(parent, (Component) newValue);
            }
        } else if (Component.PROPERTY_LAYOUT_DATA.equals(propertyName)) {
            serverUpdateManager.processLayoutDataUpdate(parent);
        } else if (Component.VISIBLE_CHANGED_PROPERTY.equals(propertyName)) {
            serverUpdateManager.processVisibleUpdate(parent);
        } else {
            if (parent instanceof ModalSupport && ModalSupport.MODAL_CHANGED_PROPERTY.equals(propertyName)) {
                setModal(parent, ((Boolean) newValue).booleanValue());
            }
            serverUpdateManager.processPropertyUpdate(parent, propertyName, oldValue, newValue);
        }
    }

    /**
     * Processes all queued tasks.  This method may only be invoked
     * from within a UI thread.
     */
    public void processQueuedTasks() {
        if (taskQueueMap.size() == 0) {
            return;
        }
        
        List currentTasks = new ArrayList();
        synchronized (taskQueueMap) {
            Iterator taskListsIt = taskQueueMap.values().iterator();
            while (taskListsIt.hasNext()) {
                List tasks = (List) taskListsIt.next();
                if (tasks != null) {
                    currentTasks.addAll(tasks);
                    tasks.clear();
                }
            }
        }
        Iterator it = currentTasks.iterator();
        while (it.hasNext()) {
            ((Runnable) it.next()).run();
        }
    }
    
    /**
     * Registers a component with the <code>ApplicationInstance</code>.
     * <p>
     * This method is invoked by <code>Component.setApplicationInstance()</code>
     * 
     * @param component the component to unregister
     */
    void registerComponent(Component component) {
        if (component.getId() == null) {
            component.setId(generateId());
        }
        idToComponentMap.put(component.getId(), component); 
        if (component instanceof ModalSupport && ((ModalSupport) component).isModal()) {
            setModal(component, true);
        }
    }
    
    /**
     * Removes a <code>PropertyChangeListener</code> from receiving 
     * notification of application-level property changes.
     * 
     * @param l the listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }
    
    /**
     * Removes the <code>TaskQueue</code> referenced by the specified handle.
     * 
     * @param taskQueue the handle specifying the <code>TaskQueue</code> to
     *        remove.
     */
    public void removeTaskQueue(TaskQueue taskQueue) {
        synchronized(taskQueueMap) {
            taskQueueMap.remove(taskQueue);
        }
    }
    
    /**
     * Sets a contextual property.
     * 
     * @param propertyName the property name
     * @param propertyValue the property value
     * 
     * @see #getContextProperty(java.lang.String)
     */
    public void setContextProperty(String propertyName, Object propertyValue) {
        if (context == null) {
            context = new HashMap();
        }
        if (propertyValue == null) {
            context.remove(propertyName);
        } else {
            context.put(propertyName, propertyValue);
        }
    }
    
    /**
     * Sets the presently focused component.
     * 
     * @param newValue the compoennt to be focused
     */
    public void setFocusedComponent(Component newValue) {
        Component oldValue = getFocusedComponent();
        if (newValue == null) {
            focusedComponent = null;
        } else {
            focusedComponent = new WeakReference(newValue);
        }
        propertyChangeSupport.firePropertyChange(FOCUSED_COMPONENT_CHANGED_PROPERTY, oldValue, newValue);
    }
    
    /**
     * Sets the modal state of a component (i.e, whether only components below
     * it in the hierarchy should be enabled.
     * 
     * @param component the <code>Component</code>
     * @param newValue the new modal state
     */
    private void setModal(Component component, boolean newValue) {
        boolean oldValue = isModal(component);
        if (newValue) {
            if (modalComponents == null) {
                modalComponents = new ArrayList();
            }
            if (!modalComponents.contains(component)) {
                modalComponents.add(component);
            }
        } else {
            if (modalComponents != null) {
                modalComponents.remove(component);
            }
        }
        firePropertyChange(MODAL_COMPONENTS_CHANGED_PROPERTY, new Boolean(oldValue), new Boolean(newValue));
    }

    /**
     * Sets the <code>StyleSheet</code> of this instance.
     * Components that belong to this instance will retrieve
     * properties from <code>StyleSheet</code>.
     * when property values are not specified directly
     * in a Component or in its <code>Style</code>.
     * <p>
     * Note that setting the style sheet should be
     * done sparingly, given that doing so causes the entire
     * client state must be updated.  Generally style sheets should
     * only be reconfigured at application initialization and/or when
     * the user changes the visual theme of a themeable application.
     * 
     * @param styleSheet the new style sheet
     */
    public void setStyleSheet(StyleSheet styleSheet) {
        this.styleSheet = styleSheet;
        updateManager.getServerUpdateManager().processFullRefresh();
    }

    /**
     * Unregisters a component with from the <code>ApplicationInstance</code>.
     * <p>
     * This method is invoked by <code>Component.setApplicationInstance()</code>
     * 
     * @param component the component to unregister
     */
    void unregisterComponent(Component component) {
        idToComponentMap.remove(component.getId());
        if (component instanceof ModalSupport && ((ModalSupport) component).isModal()) {
            setModal(component, false);
        }
    }
    
    /**
     * Verifies that a component is within the modal context, i.e., that if a
     * modal component is present, that it either is or is a child of that 
     * component.
     * 
     * @param component the component to evaluate
     * @return true if the component is within the current modal context
     */
    boolean verifyModalContext(Component component) {
        Component modalContextRoot = getModalContextRoot();
        return modalContextRoot == null || modalContextRoot.isAncestorOf(component);
    }
}
