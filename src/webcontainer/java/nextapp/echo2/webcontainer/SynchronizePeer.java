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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.update.ServerComponentUpdate;

/**
 * A stateless peer object used to synchronize the state of a given type of 
 * <code>nextapp.echo2.app.Component</code> between the server and client.
 * <p>
 * A <code>SynchronizePeer</code> may implement optional interfaces such as
 * <code>DomUpdateSupport</code> which enables rendering hierarchies of
 * components directly to (X)HTML code.  The optional 
 * <code>ActionProcessor</code> and <code>InputProcessor</code> interfaces
 * may be used when the client-side rendering of the component may send back
 * information to the server in response to user input.
 * <p>
 * A <strong>single</strong> instance of a given <code>SynchronizePeer</code> 
 * will be created to synchronize the state of <strong>ALL</strong> instances of 
 * a particular class of <code>Component</code>.  Thus, it is not possible to
 * store information about a component's state in this object (in contrast
 * to Echo v1.x, where a peer was created for each component instance).  Such 
 * rendering state information should now be stored in the 
 * <code>ContainerInstance</code>, see the
 * <code>ContainerInstance.setRenderState()</code> method for details.
 */
public interface SynchronizePeer {
    
    /**
     * Returns the id of the HTML element in which the specified 
     * <code>component</code> should be rendered.  The specified
     * <code>component</code> must be an immediate child of
     * an instance of the class of component that this peer supports.
     * A child component's renderer may invoke this method to 
     * determine where it should place its rendered content.
     * 
     * @param child a <code>Component</code> whose parent is of the type
     *        synchronized by this peer object.
     * @return the id of the element which should contain the child
     *         component's rendered HTML.
     */
    public String getContainerId(Component child);
    
    /**
     * Renders a client update which adds an HTML representation of the 
     * provided component to the client DOM as a child of the HTML element
     * identified by <code>targetId</code>. 
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the <code>ServerComponentUpdate</code> for which this
     *        operation is being performed
     * @param targetId the id of the HTML element in which the component's
     *        HTML output should be rendered
     * @param component the component to be rendered (this component must
     *        be of a type supported by this synchronization peer).
     */
    public void renderAdd(RenderContext rc, ServerComponentUpdate update, String targetId, Component component);
    
    /**
     * Renders a client update to dispose of resources/listeners created
     * for the specified component on the client.  Operations such as
     * removing event listeners on the client should be performed by the
     * implementation.  In cases where no such clean-up work is required, an 
     * empty implementation is sufficient. Note that the actual removal of 
     * HTML code will be performed by an ancestor component's 
     * <code>renderUpdate()</code> method being invoked, and thus 
     * implementations SHOULD NOT redundantly attempt
     * to remove the HTML in this method.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the <code>ServerComponentUpdate</code> for which this
     *        operation is being performed
     */
    public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component);

    /**
     * Renders the specified <code>ServerComponentUpdate</code> by adding and 
     * removing children and updating properties of the specified 
     * <code>component</code>.
     * <p>
     * If the component is not a container, the implementation only needs to
     * analyze the updated properties of the component.  If the component is
     * a container, the implementation should additionally query 
     * <code>update</code> for information about added children, removed 
     * children, and children with updated <code>LayoutData</code> states.
     * <p>
     * The implementation is responsible for rendering added children by 
     * obtaining their <code>SynchronizePeer</code>s and invoking their
     * <code>renderAdd()</code> methods.  Alternatively, if a child's 
     * <code>SynchronizePeer</code> implements the 
     * <code>DomUpdateSupport</code> interface, the implementation may invoke
     * the child peer's <code>renderHtml()</code> method instead.
     * <p>
     * This method should return true if, in the course of its rendering 
     * operation, it has re-rendered the entire component hierarchy beneath
     * the parent component of the update.  Returning true will ensure
     * that updates to descendants are NOT rendered.  The method should 
     * return false in all cases if the component is not a container.
     *
     * @param rc the relevant <code>RenderContext</code>
     * @param update the <code>ServerComponentUpdate</code> for which this
     *        operation is being performed
     * @param targetId the id of the HTML element inside of which the 
     *        components HTML code should be rendered.
     * @return true if updates to descendants should NOT be performed
     */
    public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update, String targetId);
}