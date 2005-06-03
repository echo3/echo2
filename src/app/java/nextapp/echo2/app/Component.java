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

package nextapp.echo2.app;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import nextapp.echo2.app.event.EventListenerList;

//BUGBUG. Removal of custom id assigment idea needs to be reviewed.
//BUGBUG. Add documentation to discuss local style/shared style/application style relationship.

//BUGBUG. Component ids are now being assigned on registration...
//  While this guarantees a component has a unique identifier within the application,
//  it does not permit components to be placed into a pool and reused by multiple applications
//  (the first app would assign an id, later apps would retain the old app's id, which is not
//  guaranteed to be unique in the app.
// Solution idea 1: hold weakref to application....if it's ever null, reset id.
// Solution idea 2: provide lifecycle method which must be called when adding component to application.

/**
 * A representation of an Echo component.
 * This is an abstact base class from which all Echo components are derived.
 */
public abstract class Component 
implements IdSupport {
    
    private static final int CHILD_LIST_CAPACITY = 3;
    private static final Component[] EMPTY_COMPONENT_ARRAY = new Component[0];
    
    public static final String PROPERTY_BACKGROUND = "background";
    public static final String PROPERTY_FONT = "font";
    public static final String PROPERTY_FOREGROUND = "foreground";
    public static final String PROPERTY_LAYOUT_DATA = "layoutData";
    
    public static final String CHILDREN_CHANGED_PROPERTY = "children";
    public static final String ENABLED_CHANGED_PROPERTY = "enabled";
    public static final String LOCALE_CHANGED_PROPERTY = "locale";
    public static final String STYLE_CHANGED_PROPERTY = "style";
    public static final String STYLE_NAME_CHANGED_PROPERTY = "styleName";
    public static final String FOCUS_TRAVERSAL_INDEX_CHANGED_PROPERTY = "focusTraversalIndex";
    public static final String FOCUS_TRAVERSAL_PARTICIPANT_CHANGED_PROPERTY = "focusTraversalParticipant";
    public static final String VISIBLE_CHANGED_PROPERTY = "visible";

    private static final int FLAG_ENABLED = 0x1;
    private static final int FLAG_VISIBLE = 0x2;
    private static final int FLAG_FOCUS_TRAVERSAL_PARTICIPANT= 0x4;
    private static final int FLAGS_FOCUS_TRAVERSAL_INDEX = 0x7fff0000;
    
    /**
     * Boolean flags for this component, including enabled state, visibility, 
     * and registration.  Multiple booleans are wrapped in a single integer
     * to save memory, since many <code>Component</code>instances will be 
     * created.
     */
    private int flags;
    
    /** A collection of references to child components. */
    private List children;
    
    /** A application-wide unique identifier for this component. */
    private String id;

    /** The locale of the component. */
    private Locale locale;
    
    /** Listener storage. */
    private EventListenerList listenerList;
    
    /** Local style data storage for properties directly set on component itself. */
    private MutableStyle localStyle;
    
    /** The <code>ApplicationInstance</code> to which the component is registered. */
    private ApplicationInstance applicationInstance;
    
    /** The parent of the component. */
    private Component parent;
    
    /** The propery change event dispatcher. */
    private PropertyChangeSupport propertyChangeSupport;
    
    /** Shared style. */
    private Style sharedStyle;
    
    /** Name of style to use from application style sheet */
    private String styleName;
    
    /**
     * Creates a new <code>Component</code>.
     */
    public Component() {
        super();
        flags = FLAG_ENABLED | FLAG_VISIBLE | FLAG_FOCUS_TRAVERSAL_PARTICIPANT;
        listenerList = new EventListenerList();
        propertyChangeSupport = null;
        localStyle = new MutableStyle();
    }
    
    /**
     * Adds the specified Component at the end of this Component's children.
     *
     * @param c The child component to add.
     */ 
    public void add(Component c) {
        add(c, -1);
    }

    /**
     * Adds the specified Component as the nth child
     * All component add operations use this method to add components.
     * Components that require notification of all child additions should 
     * override this method (making sure to call the superclass' 
     * implementation).
     *
     * @param c the child component to add
     * @param n the index at which to add the child component, or -1 to add the
     *          component at the end
     * @throws DuplicateIdException if the id of the child conflicts with the
     *         id of an existing component in the hierarchy
     * @throws IllegalChildException if the child is not allowed to be added
     *         to this component, either because it is not valid for the 
     *         component's state or is of an invalid type.
     */
    public void add(Component c, int n) 
    throws IllegalChildException {
        
        if (!isValidChild(c)) {
            throw new IllegalChildException(this, c);
        }
        
        if (!c.isValidParent(this)) {
            throw new IllegalChildException(this, c);
        }

        // Flag child as registered.
        if (applicationInstance != null) {
            c.setApplicationInstance(applicationInstance);
        }
        
        if (c.parent != null) {
            // Request component's current parent remove component.
            c.parent.remove(c);
        }
        
        // Lazy-create child collection if necessary.
        if (children == null) {
            children = new ArrayList(CHILD_LIST_CAPACITY);
        }

        // Connect child to parent.
        c.parent = this;
        if (n == -1) {
            children.add(c);
        } else {
            children.add(n, c);
        }
        
        // Invoke registration lifecycle method.
        c.init();

        // Notify PropertyChangeListeners of change.
        firePropertyChange(CHILDREN_CHANGED_PROPERTY, null, c);
    }
    
	/**
     * Adds a property change listener to this component.
     *
     * @param l the listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        propertyChangeSupport.addPropertyChangeListener(l);
    }
    
    /**
     * Lifecycle method invoked when the <code>Component</code> is removed 
     * from a registered hierarchy.  Implementations should always invoke
     * <code>super.dispose()</code>.
     */
    public void dispose() { }
    
    /**
     * Returns the component with the specified id.  The search is performed
     * on this component and its children only (ancestors of this component
     * will not be searched).
     * 
     * @param searchId the id of the <code>Component</code> to find
     * @return the <code>Component</code> with the given id, or null if none
     *         is found
     */
    public Component findComponent(String searchId) {
        if (id.equals(searchId)) {
            return this;
        }
        
        if (children == null) {
            return null;
        }

        Iterator it = children.iterator();
        while (it.hasNext()) {
            Component component = ((Component) it.next()).findComponent(searchId);
            if (component != null) {
                return component;
            }
        }
        return null;
    }
    
    /**
     * Reports a bound property change.
     *
     * @param propertyName the name of the changed property
     * @param oldValue the previous value of the property
     * @param newValue the present value of the property
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (propertyChangeSupport != null) {
            // Report to property change listeners.
            propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
        }
        
        // Report to application.
        if (applicationInstance != null) {
            applicationInstance.notifyComponentPropertyChange(this, propertyName, oldValue, newValue);
        }
    }
    
    /**
     * Returns the <code>ApplicationInstance</code> to which this component 
     * is registered.
     * 
     * @return the application instance
     */
    public ApplicationInstance getApplicationInstance() {
        return applicationInstance;
    }
    
    /**
     * Returns the background color of the component.
     *
     * @return the background color of the component
     */
    public Color getBackground() {
        return (Color) localStyle.getProperty(PROPERTY_BACKGROUND);
    }
    
    /**
     * Gets the nth immediate child component.
     *
     * @param n the index of the component to retrieve
     * @return the component at index n
     * @throws IndexOutOfBoundsException when the index is invalid
     */
    public Component getComponent(int n) {
        if (children == null) {
            throw new IndexOutOfBoundsException();
        }
        
        return (Component) children.get(n);
    }
    
    /**
     * Recursively searches for the component with the specified id
     * by querying this component and its descendants.
     * 
     * @param id the id of the component to be retrieved
     * @return the component with the specified id if it either is this
     *         component or is a descendant of it, or null otherwise
     */
    public Component getComponent(String id) {
        if (id.equals(this.id)) {
            return this;
        }
        if (children != null) {
	        Iterator it = children.iterator();
	        while (it.hasNext()) {
	            Component testComponent = (Component) it.next();
	            Component targetComponent = testComponent.getComponent(id);
	            if (targetComponent != null) {
	                return targetComponent;
	            }
	        }
        }
        return null;
    }
    
    /**
     * Returns the number of immediate child components.
     *
     * @return the number of immediate child components
     */
    public int getComponentCount() {
        if (children == null) {
            return 0;
        } else {
            return children.size();
        }
    }
    
    /**
     * Returns an array of all immediate child components.
     *
     * @return an array of all immediate child components
     */
    public Component[] getComponents() {
        if (children == null) {
            return EMPTY_COMPONENT_ARRAY;
        } else {
            return (Component[]) children.toArray(new Component[children.size()]);
        }
    }
    
    /**
     * Returns the local <code>EventListenerList</code>.
     * The listener list is lazily created.
     * 
     * @return the listener list
     */
    protected EventListenerList getEventListenerList() {
        if (listenerList == null) {
            listenerList = new EventListenerList();
        }
        return listenerList;
    }
    
    /**
     * Returns the font of the component.
     *
     * @return the font of the component
     */
    public Font getFont() {
        return (Font) localStyle.getProperty(PROPERTY_FONT);
    }
    
    /**
     * Returns the foreground color of the component.
     *
     * @return the foreground color of the component
     */
    public Color getForeground() {
        return (Color) localStyle.getProperty(PROPERTY_FOREGROUND);
    }
    
    /**
     * Returns the application-wide unique id of this component.
     * This id is only guaranteed to be unique within the application
     * to which this component is registered.  This method returns
     * null in the event that the component is not registered to an
     * application.
     * 
     * @return the application-wide unique id of this component
     */
    public String getId() {
        return id;
    }
    
    /**
     * Returns the <code>LayoutData</code> object used to describe how this
     * component should be layed out within its parent container.
     * 
     * @return the layout data, or null if unset
     */
    public LayoutData getLayoutData() {
        return (LayoutData) localStyle.getProperty(PROPERTY_LAYOUT_DATA);
    }
    
    /**
     * Returns the locale of the component.  If the component does not have a
     * locale, its ancestors will be queried for a locale.  If no ancestors
     * have locales set, the application instance's locale will be returned.
     * If the component is not registered, the active application instance's
     * will be returned.
     *
     * @return the locale for this component
     */
    public Locale getLocale() {
        if (locale == null) {
            if (parent == null) {
                if (applicationInstance == null) {
                    return null;
                } else {
                    return applicationInstance.getLocale();
                }
            } else {
                return parent.getLocale();
            }
        } else {
            return locale;
        }
    }
    
    /**
     * Returns the parent component.
     * 
     * @return the parent component, or null if this component has no parent
     */
    public Component getParent() {
        return parent;
    }
    
    /**
     * Returns the state of the specified property.
     * 
     * @param propertyName the name of the proeprty
     * @return the state of the specified property
     */
    public Object getProperty(String propertyName) {
        return localStyle.getProperty(propertyName);
    }
    
    /**
     * Returns the state of the specified indexed property
     * 
     * @param propertyName the name of the property
     * @param propertyIndex the index of the property
     * @return the state of the specified indexed property
     */
    public Object getIndexedProperty(String propertyName, int propertyIndex) {
        return localStyle.getIndexedProperty(propertyName, propertyIndex);
    }

    /**
     * Determines the &quot;rendered state&quot; of a property by querying
     * the state of a property in both local and shared <code>Style</code>
     * information.  An application container should invoke this method
     * rather than individual property getter methods to determine the state
     * of properties when rendering.
     * 
     * @param propertyName the name of the property
     * @return the property state
     */
    public Object getRenderProperty(String propertyName) {
        return getRenderProperty(propertyName, null);
    }
        
    /**
     * Determines the &quot;rendered state&quot; of a property by querying
     * the state of a property in both local and shared <code>Style</code>
     * information.  An application container should invoke this method
     * rather than individual property getter methods to determine the state
     * of properties when rendering.
     * 
     * @param propertyName the name of the property
     * @param defaultValue the value to be returned if the property is not set
     * @return the property state
     */ 
    public Object getRenderProperty(String propertyName, Object defaultValue) {
        if (localStyle.isPropertySet(propertyName)) {
            // Return local style value.
            return localStyle.getProperty(propertyName);
        } else if (sharedStyle != null && sharedStyle.isPropertySet(propertyName)) {
            // Return style value specified in shared style.
            return sharedStyle.getProperty(propertyName);
        } else {
            if (applicationInstance != null) {
                Style applicationStyle = applicationInstance.getStyle(getClass(), styleName);
                if (applicationStyle != null && applicationStyle.isPropertySet(propertyName)) {
                    // Return style value specified in application.
                    return applicationStyle.getProperty(propertyName);
                }
            }
            return defaultValue;
        }
    }

    /**
     * Determines the &quot;rendered state&quot; of an indexed property by 
     * querying the state of a property in both local and shared 
     * <code>Style</code> information.  An application container should invoke 
     * this method rather than individual property getter methods to determine 
     * the state of indexed properties when rendering.
     * 
     * @param propertyName the name of the property
     * @param propertyIndex the index of the property
     * @return the property state
     */ 
    public Object getRenderIndexedProperty(String propertyName, int propertyIndex) {
        return getRenderIndexedProperty(propertyName, propertyIndex, null);
    }
    
    /**
     * Determines the &quot;rendered state&quot; of an indexed property by 
     * querying the state of a property in both local and shared 
     * <code>Style</code> information.  An application container should invoke 
     * this method rather than individual property getter methods to determine 
     * the state of indexed properties when rendering.
     * 
     * @param propertyName the name of the property
     * @param propertyIndex the index of the property
     * @param defaultValue the value to be returned if the property is not set
     * @return the property state
     */ 
    public Object getRenderIndexedProperty(String propertyName, int propertyIndex, Object defaultValue) {
        if (localStyle.isIndexedPropertySet(propertyName, propertyIndex)) {
            // Return local style value.
            return localStyle.getIndexedProperty(propertyName, propertyIndex);
        } else if (sharedStyle != null && sharedStyle.isIndexedPropertySet(propertyName, propertyIndex)) {
            // Return style value specified in shared style.
            return sharedStyle.getIndexedProperty(propertyName, propertyIndex);
        } else {
            if (applicationInstance != null) {
                Style applicationStyle = applicationInstance.getStyle(getClass(), styleName);
                if (applicationStyle != null && applicationStyle.isIndexedPropertySet(propertyName, propertyIndex)) {
                    // Return style value specified in application.
                    return applicationStyle.getIndexedProperty(propertyName, propertyIndex);
                }
            }
            return defaultValue;
        }
    }
    
    /**
     * Returns the shared <code>Style</code> object assigned to this 
     * <code>Component</code>.
     * As its name implies, the <strong>shared</strong> <code>Style</code> 
     * may be shared amongst multiple <code>Component</code>s.
     * 
     * @return the shared <code>Style</code>
     */
    public final Style getStyle() {
        return sharedStyle;
    }
    
    /**
     * Returns the name of the style to use from the application-level
     * style-sheets.
     * 
     * @return the style name
     */
    public final String getStyleName() {
        return styleName;
    }
    
    /**
     * Returns the focus traversal (tab) index of the component.
     * 
     * @return the focus traversalindex
     */
    public int getFocusTraversalIndex() {
        return (flags & FLAGS_FOCUS_TRAVERSAL_INDEX) >> 16;
    }
    
    /**
     * Gets the nth immediate visible child component.
     *
     * @param n the index of the component to retrieve
     * @return the component at index n
     * @throws IndexOutOfBoundsException when the index is invalid
     */
    public Component getVisibleComponent(int n) {
        if (children == null) {
            throw new IndexOutOfBoundsException(Integer.toString(n));
        }
        int visibleComponentCount = 0;
        Component component = null;
        Iterator it = children.iterator();
        while (visibleComponentCount <= n) {
            if (!it.hasNext()) {
              throw new IndexOutOfBoundsException(Integer.toString(n));
            }
            component = (Component) it.next();
            if (component.isVisible()) {
                ++visibleComponentCount;
            }
        }
        return component;
    }
    
    /**
     * Returns an array of all immediate visible child components.
     *
     * @return an array of all immediate visible child components
     */
    public Component[] getVisibleComponents() {
        if (children == null) {
            return EMPTY_COMPONENT_ARRAY;
        } else {
            Iterator it = children.iterator();
            List visibleChildList = new ArrayList();
            while (it.hasNext()) {
                Component component = (Component) it.next();
                if (component.isVisible()) {
                    visibleChildList.add(component);
                }
            }
            return (Component[]) visibleChildList.toArray(new Component[visibleChildList.size()]);
        }
    }

    /**
     * Returns the number of immediate visible child components.
     *
     * @return the number of immediate visible child components
     */
    public int getVisibleComponentCount() {
        if (children == null) {
            return 0;
        } else {
            int visibleComponentCount = 0;
            Iterator it = children.iterator();
            while (it.hasNext()) {
                Component component = (Component) it.next();
                if (component.isVisible()) {
                    ++visibleComponentCount;
                }
            }
            return visibleComponentCount;
        }
    }
    
    /**
     * Determines the index of the given <code>Component</code> within the 
     * children of this <code>Component</code>.  If the given 
     * <code>Component</code> is not a child, returns -1.
     * 
     * @param c the <code>Component</code> to analyze
     * @return the index of the given <code>Component</code> amongst the 
     *         children of this <code>Component</code>
     */
    public final int indexOf(Component c) {
        return children == null ? -1 : children.indexOf(c);
    }
    
    /**
     * Lifecycle method invoked when the <code>Component</code> is added 
     * to a registered hierarchy.  Implementations should always invoke
     * <code>super.init()</code>.
     */
    public void init() { }
    
    /**
     * Determines if this <code>Component</code> is or is an ancestor of 
     * the specified <code>Component</code>.
     * 
     * @param c the <code>Component</code> to test for ancestry
     * @return true if this <code>Component</code> is an ancestor of the 
     *         specified <code>Component</code>
     */
    public final boolean isAncestorOf(Component c) {
        while (c != null && c != this) {
            c = c.parent;
        }
        return c == this;
    }
    
    /**
     * Determines the enabled state of this <code>Component</code>.
     * 
     * @return true if the component is enabled
     */
    public boolean isEnabled() {
        return (flags & FLAG_ENABLED) != 0;
    }
    
    /**
     * Determines if the component participates in (tab) focus traversal.
     * 
     * @return true if the component participates in focus traversal
     */
    public boolean isFocusTraversalParticipant() {
        return (flags & FLAG_FOCUS_TRAVERSAL_PARTICIPANT) != 0;
    }
    
    /**
     * Determines if the component and all of its parents are visible.
     * 
     * @return true if the component is recursively visible
     */
    public boolean isRecursivelyVisible() {
        Component component = this;
        while (component != null) {
            if ((component.flags & FLAG_VISIBLE) == 0) {
                return false;
            }
            component = component.parent;
        }
        return true;
    }

    /**
     * Returns true if the <code>Component</code> is registered to an application.
     * 
     * @return true if the <code>Component</code> is registered to an application
     */
    public final boolean isRegistered() {
        return applicationInstance != null;
    }
    
    /**
     * Determines if a given component is valid to be added as a child
     * to this component.  Default implementation always returns true,
     * may be overridden to provide specific behavior.
     * 
     * @param component the component to evaluate as a child
     * @return true if the component is a valid child
     */
    public boolean isValidChild(Component component) {
        return true;
    }
    
    /**
     * Determines if this component is valid to be added as a child
     * of the given parent.  Default implementation always returns true,
     * may be overridden to provide specific behavior.
     * 
     * @param component the component to evaluate as a parent
     * @return true if the component is a valid parent
     */
    public boolean isValidParent(Component component) {
        return true;
    }
    
    /**
     * Returns the visibility state of this <code>Component</code>.
     *
     * @return the visibility state of this <code>Component</code>
     */
    public final boolean isVisible() {
        return (FLAG_VISIBLE & flags) != 0;
    }
    
    /**
     * Processes an input from the user-interface received via the
     * <code>UpdateManager</code>.
     * 
     * @param inputName the name of the input
     * @param inputValue the value of the input
     */
    public void processInput(String inputName, Object inputValue) { }
    
    /**
     * Removes the specified child component.
     * 
     * All <code>Component</code> remove operations use this method to 
     * remove <code>Component</code>s. <code>Component</code>s that require 
     * notification of all child removals should 
     * override this method (making sure to call the superclass' 
     * implementation).
     * 
     * @param c the child <code>Component</code> to remove
     */
    public void remove(Component c) {

        if (children == null || !children.contains(c)) {
            // Do-nothing if component is not a child.
            return;
        }
        
        // Invoke disposal lifecycle method.
        c.dispose();

        // Dissolve references between parent and child.
        children.remove(c);
        c.parent = null;

        // Deregister child.
        if (isRegistered()) {
            c.setApplicationInstance(null);
        }
        
        // Notify PropertyChangeListeners of change.
        firePropertyChange(CHILDREN_CHANGED_PROPERTY, c, null);
    }
    
    /**
     * Removes the <code>Component</code> at the given index.
     *
     * @param n the index of the child <code>Component</code> to remove
     * @throws IndexOutOfBoundsException if the index is not valid
     */
    public void remove(int n) {
        if (children == null) {
            throw new IndexOutOfBoundsException();
        }
        remove(getComponent(n));
    }
    
    /**
     * Removes all child <code>Component</code>s.
     */
    public void removeAll() {
        if (children != null) {
            while (children.size() > 0) {
                Component c = (Component) children.get(children.size() - 1);
                remove(c);
            }
            children = null;
        }
    }
    
    /**
     * Removes a property change listener from this <code>Component</code>.
     *
     * @param l the listener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (propertyChangeSupport != null) {
            propertyChangeSupport.removePropertyChangeListener(l);
        }
    }
    
    /**
     * Sets the <code>ApplicationInstance</code> to which this component 
     * is registered.
     * <p>
     * The <code>ApplicationInstance</code> to which a component is registered
     * may not be changed directly from one to another, i.e., if the component
     * is registered to instance "A" and you attempt to set it to instance "B",
     * an <code>IllegalStateException</code> will be thrown.  In order to change
     * the instance to which a component is registered, the instance must first
     * be set to null.
     * 
     * @param newValue the new <code>ApplicationInstance</code>.
     */
    void setApplicationInstance(ApplicationInstance newValue) {
        if (applicationInstance != null && newValue != null 
                && !applicationInstance.equals(newValue)) {
            throw new IllegalStateException("Cannot reassign ApplicationInstance.");
        }
        
        if (newValue == null && applicationInstance != null) {
            applicationInstance.unregisterComponent(this);
        }
        
        applicationInstance = newValue;
        
        if (newValue != null) {
            applicationInstance.registerComponent(this);
        }
        
        if (children != null) {
            Iterator it = children.iterator();
            while (it.hasNext()) {
                ((Component) it.next()).setApplicationInstance(newValue);
            }
        }
    }
    
    /**
     * Sets the default background color of the <code>Component</code>.
     * 
     * @param background the new background <code>Color</code>
     */
    public void setBackground(Color background) {
        setProperty(PROPERTY_BACKGROUND, background);
    }
    
    /**
     * Sets the enabled state of the <code>Component</code>.
     * 
     * @param newValue the new state
     */
    public void setEnabled(boolean newValue) {
        boolean oldValue = (flags & FLAG_ENABLED) != 0;
        if (oldValue != newValue) {
            flags ^= FLAG_ENABLED; // Toggle FLAG_ENABLED bit.
            firePropertyChange(ENABLED_CHANGED_PROPERTY, new Boolean(oldValue), new Boolean(newValue));
        }
    }
    
    /**
     * Sets the default text font of the <code>Component</code>.
     * 
     * @param font the new <code>Font</code>
     */
    public void setFont(Font font) {
        setProperty(PROPERTY_FONT, font);
    }
    
    /**
     * Sets the default foreground color of the <code>Component</code>.
     * 
     * @param foreground the new foreground <code>Color</code>
     */
    public void setForeground(Color foreground) {
        setProperty(PROPERTY_FOREGROUND, foreground);
    }
    
    /**
     * Sets the unique identifier of this <code>Component</code>.
     * This method is invoked by the <code>ApplicationInstance</code>
     * when the component is registered or deregistered.
     * 
     * @param id the new identifier
     */
    void setId(String id) {
        this.id = id;
    }
    
    /**
     * Sets a generic indexed property of the <code>Component</code>.
     * The value will be stored in this <code>Component</code>'s local style.
     * 
     * @param propertyName the name of the property
     * @param propertyIndex the index of the property
     * @param newValue the value of the property
     */
    public void setIndexedProperty(String propertyName, int propertyIndex, Object newValue) {
        localStyle.setIndexedProperty(propertyName, propertyIndex, newValue);
        firePropertyChange(propertyName, null, null);
    }
    
    /**
     * Sets the <code>LayoutData</code> of this <code>Component</code>.
     * A <code>LayoutData</code> implementation describes how this
     * <code>Component</code> is layed out within/interacts with its 
     * containing parent <code>Component</code>.
     * 
     * @param layoutData the new <code>LayoutData</code>
     */
    public void setLayoutData(LayoutData layoutData) {
        setProperty(PROPERTY_LAYOUT_DATA, layoutData);
    }
    
    /**
     * Sets the locale of the <code>Component</code>.
     *
     * @param newValue the new locale
     */
    public void setLocale(Locale newValue) {
        Locale oldValue = locale;
        locale = newValue;
        firePropertyChange(LOCALE_CHANGED_PROPERTY, oldValue, newValue);
    }
    
    //BUGBUG. Currently firing null, null in the event that oldValue = newValue
    // and propname = layoutdata
    //due to PCS design....perhaps we want to use something other than PCS.
    //(case where we want prop event fired on equal items: setting attributes of
    // layout data.)
    /**
     * Sets a generic property of the <code>Component</code>.
     * The value will be stored in this <code>Component</code>'s local style.
     * 
     * @param propertyName the name of the property
     * @param newValue the value of the property
     */
    public void setProperty(String propertyName, Object newValue) {
        Object oldValue = localStyle.getProperty(propertyName);
        localStyle.setProperty(propertyName, newValue);
        if (PROPERTY_LAYOUT_DATA.equals(propertyName) && oldValue != null && newValue != null && oldValue.equals(newValue)) {
            firePropertyChange(propertyName, null, null);
        } else {
            firePropertyChange(propertyName, oldValue, newValue);
        }
    }
    
    /**
     * Sets the shared style of the <code>Component</code>.
     * Setting the shared style will have no impact on the local stylistic
     * properties of the component.
     * 
     * @param newValue the new shared style
     */
    public void setStyle(Style newValue) {
        Style oldValue = sharedStyle;
        sharedStyle = newValue;
        firePropertyChange(STYLE_CHANGED_PROPERTY, oldValue, newValue);
    }
    
    /**
     * Sets the name of the style to use from the application-level
     * style-sheets.
     * Setting the style name wil have no impact on the local stylistic
     * properties of the component.
     * 
     * @param newValue the new style name
     */
    public void setStyleName(String newValue) {
        String oldValue = styleName;
        styleName = newValue;
        firePropertyChange(STYLE_NAME_CHANGED_PROPERTY, oldValue, newValue);
    }
    
    /**
     * Sets the focus traversal (tab) index of the component.
     * 
     * @param newValue the new focus traversal index
     */
    public void setFocusTraversalIndex(int newValue) {
        int oldValue = getFocusTraversalIndex();
        newValue &= 0x7fff;
        flags = flags & ((~FLAGS_FOCUS_TRAVERSAL_INDEX)) | (newValue << 16);
        firePropertyChange(FOCUS_TRAVERSAL_INDEX_CHANGED_PROPERTY, new Integer(oldValue), new Integer(newValue));
    }
    
    /**
     * Sets whether the component participates in the focus traversal order 
     * (tab order).
     * 
     * @param newValue true if the component participates in the focus 
     *        traversal order
     */
    public void setFocusTraversalParticipant(boolean newValue) {
        boolean oldValue = isFocusTraversalParticipant();
        if (oldValue != newValue) {
            flags ^= FLAG_FOCUS_TRAVERSAL_PARTICIPANT; // Toggle FLAG_FOCUS_TRAVERSAL_PARTICIPANT bit.
            firePropertyChange(FOCUS_TRAVERSAL_PARTICIPANT_CHANGED_PROPERTY, new Boolean(oldValue), new Boolean(newValue));
        }
    }

    /**
     * Sets the visibility state of this <code>Component</code>.
     * 
     * @param newValue the new visibility state
     */
    public void setVisible(boolean newValue) {
        boolean oldValue = (flags & FLAG_VISIBLE) != 0;
        if (oldValue != newValue) {
            flags ^= FLAG_VISIBLE; // Toggle FLAG_VISIBLE bit.
            firePropertyChange(VISIBLE_CHANGED_PROPERTY, new Boolean(oldValue), new Boolean(newValue));
        }
    }

    /**
     * A life-cycle method invoked before the component is rendered to ensure
     * it is in a valid state.
     */
    public void validate() { }
    
    /**
     * Invoked by <code>ClientUpdateManager</code> on each component in the
     * hierarchy whose <code>processInput()</code> method will layer be invoked
     * in the current transaction.  This method should return true if the 
     * component will be capable of processing the given input in its current 
     * state or false otherwise.  This method should not do any of the actual
     * processing work if overridden (any actual processing should be done in
     * the <code>processInput()</code> implementation.
     * <p>
     * The default implementation verifies that the component is visible, 
     * enabled, and not "obscured" by the presence of any modal component.
     * If overriding this method, your implementation should invoke
     * <code>super.verifyInput()</code> if you wish to retain these behaviors.
     * 
     * @param inputName the name of the input
     * @param inputValue the value of the input
     * @return true if the input is allowed to be processed by this component
     *         in its current state
     */
    public boolean verifyInput(String inputName, Object inputValue) {
        //BUGBUG. what if the enabled state changes on the client and then it receives input--or do we
        //        just want to make such practice illegal...or does the component simply need to handle 
        //        (mind the fact that ordering of inputs cannot be guaranteed).
        if (applicationInstance != null && !applicationInstance.verifyModalContext(this)) {
            return false;
        }
        return isVisible() && isEnabled();
    }

    /**
     * Determines the index of the given <code>Component</code> within the 
     * visible children of this <code>Component</code>.  If the given 
     * <code>Component</code> is not a child, returns -1.
     * 
     * @param c the <code>Component</code> to analyze
     * @return the index of the given <code>Component</code> amongst the 
     *         visible children of this <code>Component</code>
     */
    public final int visibleIndexOf(Component c) {
        if (!c.isVisible()) {
            return -1;
        }
        if (children == null) {
            return -1;
        }
        int visibleIndex = 0;
        Iterator it = children.iterator();
        while (it.hasNext()) {
            Component component = (Component) it.next();
            if (!component.isVisible()) {
                continue;
            }
            if (component.equals(c)) {
                return visibleIndex;
            }
            ++visibleIndex;
        }
        return -1;
    }
    
}
