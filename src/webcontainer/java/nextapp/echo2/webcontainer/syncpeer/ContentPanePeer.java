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

package nextapp.echo2.webcontainer.syncpeer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.DomUpdateSupport;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.SynchronizePeer;
import nextapp.echo2.webcontainer.SynchronizePeerFactory;
import nextapp.echo2.webcontainer.propertyrender.ColorRender;
import nextapp.echo2.webcontainer.propertyrender.FontRender;
import nextapp.echo2.webrender.clientupdate.DomUpdate;
import nextapp.echo2.webrender.output.CssStyle;

/**
 * Synchronization peer for <code>nextapp.echo2.app.ContentPane</code> components.
 */
public class ContentPanePeer 
implements DomUpdateSupport, SynchronizePeer {
    
    /**
     * Default constructor.
     */
    public ContentPanePeer() {
        super();
        //BUGBUG. add registry property renderers for color, font....
    }
    
    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#getContainerId(nextapp.echo2.app.Component)
     */
    public String getContainerId(Component child) {
        String parentId = ContainerInstance.getElementId(child.getParent());
        return parentId + "_container_" + ContainerInstance.getElementId(child);
    }
    
    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#renderAdd(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String, nextapp.echo2.app.Component)
     */
    public void renderAdd(RenderContext rc, ServerComponentUpdate update, String targetId, Component component) {
        Element contentElement = DomUpdate.createDomAdd(rc.getServerMessage(), targetId);
        renderHtml(rc, update, contentElement, component);
    }

    /**
     * Renders child components which were added to a 
     * <code>ContentPane</code>, as described in the provided 
     * <code>ServerComponentUpdate</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update
     */
    private void renderAddChildren(RenderContext rc, ServerComponentUpdate update) {
        Component component = update.getParent();
        String elementId = ContainerInstance.getElementId(component);
        Component[] components = update.getParent().getComponents();
        Component[] addedChildren = update.getAddedChildren();
        
        for (int componentIndex = components.length - 1; componentIndex >= 0; --componentIndex) {
            for (int addedChildrenIndex = 0; addedChildrenIndex < addedChildren.length; ++addedChildrenIndex) {
                if (addedChildren[addedChildrenIndex] == components[componentIndex]) {
                    Element contentElement;
                    if (componentIndex == components.length - 1) {
                        contentElement = DomUpdate.createDomAdd(rc.getServerMessage(), elementId);
                    } else {
                        contentElement = DomUpdate.createDomAdd(rc.getServerMessage(), 
                                elementId, getContainerId(components[componentIndex + 1]));
                    }
                    renderChild(rc, update, contentElement, component, components[componentIndex]);

                    //BUGBUG. ahem...this should be continuing the OUTER for loop, eh?
                    // set a flag or something, as this is beyond meaningless:
                    continue;
                }
            }
        }
    }
    
    /**
     * Renders an individual child component of the <code>ContentPane</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the <code>ServerComponentUpdate</code> being performed
     * @param containerElement the outer &lt;div&gt; element of the 
     *        <code>ContentPane</code>
     * @param child the child <code>Component</code> to be rendered
     */
    private void renderChild(RenderContext rc, ServerComponentUpdate update, Element containerElement, 
            Component component, Component child) {
        Element containerDivElement = containerElement.getOwnerDocument().createElement("div");
        String containerId = getContainerId(child);
        containerDivElement.setAttribute("id", containerId);
        containerElement.appendChild(containerDivElement);
        SynchronizePeer syncPeer = SynchronizePeerFactory.getPeerForComponent(child.getClass());
        if (syncPeer instanceof DomUpdateSupport) {
            ((DomUpdateSupport) syncPeer).renderHtml(rc, update, containerDivElement, child);
        } else {
            syncPeer.renderAdd(rc, update, containerId, child);
        }
    }
    
    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#renderDispose(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, nextapp.echo2.app.Component)
     */
    public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component) {
        // Do nothing.
    }
    
    /**
     * @see nextapp.echo2.webcontainer.DomUpdateSupport#renderHtml(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, org.w3c.dom.Element, nextapp.echo2.app.Component)
     */
    public void renderHtml(RenderContext rc, ServerComponentUpdate update, Element parent, Component component) {
        ContentPane contentPane = (ContentPane) component;
        
        Document document = parent.getOwnerDocument();
        Element divElement = document.createElement("div");
        divElement.setAttribute("id", ContainerInstance.getElementId(component));
        
        CssStyle cssStyle = new CssStyle();
        cssStyle.setAttribute("position", "absolute");
        cssStyle.setAttribute("width", "100%");
        cssStyle.setAttribute("height", "100%");
        ColorRender.renderToStyle(cssStyle, (Color) contentPane.getRenderProperty(ContentPane.PROPERTY_FOREGROUND),
                (Color) contentPane.getRenderProperty(ContentPane.PROPERTY_BACKGROUND));
        FontRender.renderToStyle(cssStyle, (Font) contentPane.getRenderProperty(ContentPane.PROPERTY_FONT));
        divElement.setAttribute("style", cssStyle.renderInline());
        
        parent.appendChild(divElement);
        
        Component[] children = contentPane.getComponents();
        for (int i = 0; i < children.length; ++i) {
            renderChild(rc, update, divElement, component, children[i]);
        }
    }

    /**
     * Renders removal operations for child components which were removed from 
     * a <code>ContentPane</code>, as described in the provided 
     * <code>ServerComponentUpdate</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update
     */
    private void renderRemoveChildren(RenderContext rc, ServerComponentUpdate update) {
        Component[] removedChildren = update.getRemovedChildren();
        for (int i = 0; i < removedChildren.length; ++i) {
            DomUpdate.createDomRemove(rc.getServerMessage(), 
                    ContainerInstance.getElementId(update.getParent()) + "_container_" +
                    ContainerInstance.getElementId(removedChildren[i]));
        }
    }
    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#renderUpdate(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String)
     */
    public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update, String targetId) {
        boolean fullReplace = false;
        if (update.hasUpdatedLayoutDataChildren()) {
            fullReplace = true;
        } else if (update.hasUpdatedProperties()) {
            fullReplace = true;
        }
        
        if (fullReplace) {
            // Perform full update.
            DomUpdate.createDomRemove(rc.getServerMessage(), 
                    ContainerInstance.getElementId(update.getParent()));
            renderAdd(rc, update, targetId, update.getParent());
        } else {
            renderRemoveChildren(rc, update);
            renderAddChildren(rc, update);
        }
        return fullReplace;
    }
}
