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

import org.w3c.dom.Element;

/**
 * An optional interface which may be implemented by 
 * <code>SynchronizePeer</code>s that wish to support rending directly
 * to HTML.
 */
public interface DomUpdateSupport {

    /**
     * Renders the component in its entirity as a child of the provided 
     * parent <code>Element</code>.  The implementation should additionally
     * render any child components, either by invoking their 
     * <code>renderHtml()</code> methods if their peers also implement
     * <code>DomUpdateSupport</code> or by invoking their 
     * <code>SynchronizePeer.renderAdd()</code> methods if they do
     * not.
     * <p>
     * The implementation must also perform any non-HTML-rendering operations
     * which are performed in the <code>SynchronizePeer.renderAdd()</code>
     * method, e.g., adding messageparts that register event listeners on
     * the client.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the <code>ServerComponentUpdate</code> for which this
     *        rendering is being performed
     * @param parentElement the parent DOM element to which this child should 
     *        add HTML code
     * @param component the <code>Component</code> to be rendered
     */
    public void renderHtml(RenderContext rc, ServerComponentUpdate update, Element parentElement, Component component);
}
