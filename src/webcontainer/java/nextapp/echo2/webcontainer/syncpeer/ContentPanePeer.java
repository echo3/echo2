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
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.FillImage;
import nextapp.echo2.app.FloatingPane;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.button.AbstractButton;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.DomUpdateSupport;
import nextapp.echo2.webcontainer.PartialUpdateManager;
import nextapp.echo2.webcontainer.PartialUpdateParticipant;
import nextapp.echo2.webcontainer.PropertyUpdateProcessor;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.ComponentSynchronizePeer;
import nextapp.echo2.webcontainer.SynchronizePeerFactory;
import nextapp.echo2.webcontainer.image.ImageRenderSupport;
import nextapp.echo2.webcontainer.propertyrender.ColorRender;
import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
import nextapp.echo2.webcontainer.propertyrender.FillImageRender;
import nextapp.echo2.webcontainer.propertyrender.FontRender;
import nextapp.echo2.webrender.ServerMessage;
import nextapp.echo2.webrender.Service;
import nextapp.echo2.webrender.WebRenderServlet;
import nextapp.echo2.webrender.output.CssStyle;
import nextapp.echo2.webrender.servermessage.DomUpdate;
import nextapp.echo2.webrender.servermessage.VirtualPosition;
import nextapp.echo2.webrender.service.JavaScriptService;

/**
 * Synchronization peer for <code>nextapp.echo2.app.ContentPane</code> components.
 * <p>
 * This class should not be extended or used by classes outside of the
 * Echo framework.
 */
public class ContentPanePeer 
implements ComponentSynchronizePeer, DomUpdateSupport, ImageRenderSupport, PropertyUpdateProcessor {

    //TODO: This needs to become a client-rendered component.
    //TODO: Performance can be improved by implementing MORE PartialUpdateManagers.

    private static final Extent EXTENT_0 = new Extent(0);
    private static final Insets DEFAULT_INSETS = new Insets(EXTENT_0);
    
    private static final String IMAGE_ID_BACKGROUND = "background";

    /**
     * Service to provide supporting JavaScript library.
     */
    private static final Service CONTENT_PANE_SERVICE = JavaScriptService.forResource("Echo.ContentPane",
            "/nextapp/echo2/webcontainer/resource/js/ContentPane.js");

    static {
        WebRenderServlet.getServiceRegistry().add(CONTENT_PANE_SERVICE);
    }
    
    private PartialUpdateManager partialUpdateManager; 
    
    /**
     * Default constructor.
     */
    public ContentPanePeer() {
        super();
        partialUpdateManager = new PartialUpdateManager();
        partialUpdateManager.add(ContentPane.PROPERTY_HORIZONTAL_SCROLL, new PartialUpdateParticipant() {
        
            public void renderProperty(RenderContext rc, ServerComponentUpdate update) {
                renderScrollDirective(rc, (ContentPane) update.getParent(), true);
            }
        
            public boolean canRenderProperty(RenderContext rc, ServerComponentUpdate update) {
                return true;
            }
        });
        partialUpdateManager.add(ContentPane.PROPERTY_VERTICAL_SCROLL, new PartialUpdateParticipant() {
        
            public void renderProperty(RenderContext rc, ServerComponentUpdate update) {
                renderScrollDirective(rc, (ContentPane) update.getParent(), false);
            }
        
            public boolean canRenderProperty(RenderContext rc, ServerComponentUpdate update) {
                return true;
            }
        });
    }
    
    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#getContainerId(nextapp.echo2.app.Component)
     */
    public String getContainerId(Component child) {
        return getContainerId(child.getParent(), child);
    }
    
    private String getContainerId(Component parent, Component child) {
        if (child instanceof FloatingPane) {
            return ContainerInstance.getElementId(parent) + "_float_" + ContainerInstance.getElementId(child);
        } else {
            return ContainerInstance.getElementId(parent) + "_content_" + ContainerInstance.getElementId(child);
        }
    }
    
    /**
     * @see nextapp.echo2.webcontainer.image.ImageRenderSupport#getImage(nextapp.echo2.app.Component, java.lang.String)
     */
    public ImageReference getImage(Component component, String imageId) {
        if (IMAGE_ID_BACKGROUND.equals(imageId)) {
            FillImage backgroundImage 
                    = (FillImage) component.getRenderProperty(AbstractButton.PROPERTY_BACKGROUND_IMAGE);
            if (backgroundImage == null) {
                return null;
            } else {
                return backgroundImage.getImage();
            }
        } else {
            return null;
        }
    }

    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderAdd(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String, nextapp.echo2.app.Component)
     */
    public void renderAdd(RenderContext rc, ServerComponentUpdate update, String targetId, Component component) {
        Element domAddElement = DomUpdate.renderElementAdd(rc.getServerMessage());
        DocumentFragment htmlFragment = rc.getServerMessage().getDocument().createDocumentFragment();
        renderHtml(rc, update, htmlFragment, component);
        DomUpdate.renderElementAddContent(rc.getServerMessage(), domAddElement, targetId, htmlFragment);
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
        ContentPane contentPane = (ContentPane) update.getParent();
        String elementId = ContainerInstance.getElementId(contentPane);
        Component[] components = update.getParent().getVisibleComponents();
        Component[] addedChildren = update.getAddedChildren();
        
        for (int componentIndex = components.length - 1; componentIndex >= 0; --componentIndex) {
            boolean childFound = false;
            for (int addedChildrenIndex = 0; !childFound && addedChildrenIndex < addedChildren.length; ++addedChildrenIndex) {
                if (addedChildren[addedChildrenIndex] == components[componentIndex]) {
                    Element domAddElement = DomUpdate.renderElementAdd(rc.getServerMessage());
                    DocumentFragment htmlFragment = rc.getServerMessage().getDocument().createDocumentFragment();
                    renderChild(rc, update, htmlFragment, contentPane, components[componentIndex]);
                    
                    if (componentIndex == components.length - 1) {
                        DomUpdate.renderElementAddContent(rc.getServerMessage(), domAddElement, elementId, htmlFragment);
                    } else {
                        DomUpdate.renderElementAddContent(rc.getServerMessage(), domAddElement, 
                                elementId, getContainerId(components[componentIndex + 1]), htmlFragment);
                    }

                    childFound = true;
                }
            }
        }
    }
    
    /**
     * Renders an individual child component of the <code>ContentPane</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the <code>ServerComponentUpdate</code> being performed
     * @param parentNode the outer &lt;div&gt; element of the 
     *        <code>ContentPane</code>
     * @param contentPane the containing <code>ContentPane</code> 
     * @param child the child <code>Component</code> to be rendered
     */
    private void renderChild(RenderContext rc, ServerComponentUpdate update, Node parentNode, 
            ContentPane contentPane, Component child) {
        Element containerDivElement = parentNode.getOwnerDocument().createElement("div");
        String containerId = getContainerId(child);
        containerDivElement.setAttribute("id", containerId);
        if (!(child instanceof FloatingPane)) {
            CssStyle style = new CssStyle();
            style.setAttribute("position", "absolute");
            style.setAttribute("overflow", "auto");
            Insets insets = (Insets) contentPane.getRenderProperty(ContentPane.PROPERTY_INSETS, DEFAULT_INSETS);
            style.setAttribute("top", ExtentRender.renderCssAttributeValue(insets.getTop()));
            style.setAttribute("left", ExtentRender.renderCssAttributeValue(insets.getLeft()));
            style.setAttribute("right", ExtentRender.renderCssAttributeValue(insets.getRight()));
            style.setAttribute("bottom", ExtentRender.renderCssAttributeValue(insets.getBottom()));
            containerDivElement.setAttribute("style", style.renderInline());
            VirtualPosition.renderRegister(rc.getServerMessage(), containerId);
        }
        
        parentNode.appendChild(containerDivElement);
        ComponentSynchronizePeer syncPeer = SynchronizePeerFactory.getPeerForComponent(child.getClass());
        if (syncPeer instanceof DomUpdateSupport) {
            ((DomUpdateSupport) syncPeer).renderHtml(rc, update, containerDivElement, child);
        } else {
            syncPeer.renderAdd(rc, update, containerId, child);
        }
    }
    
    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderDispose(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, nextapp.echo2.app.Component)
     */
    public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component) {
        rc.getServerMessage().addLibrary(CONTENT_PANE_SERVICE.getId());
        renderDisposeDirective(rc, (ContentPane) component);
    }
    
    /**
     * Renders a directive to the outgoing <code>ServerMessage</code> to 
     * dispose the state of a <code>ContentPane</code>, performing tasks such as
     * unregistering event listeners on the client.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param contentPane the <code>ContentPane</code>
     */
    private void renderDisposeDirective(RenderContext rc, ContentPane contentPane) {
        ServerMessage serverMessage = rc.getServerMessage();
        Element itemizedUpdateElement = serverMessage.getItemizedDirective(ServerMessage.GROUP_ID_PREREMOVE,
                "EchoContentPane.MessageProcessor", "dispose",  new String[0], new String[0]);
        Element itemElement = serverMessage.getDocument().createElement("item");
        itemElement.setAttribute("eid", ContainerInstance.getElementId(contentPane));
        itemizedUpdateElement.appendChild(itemElement);
    }
    
    /**
     * @see nextapp.echo2.webcontainer.DomUpdateSupport#renderHtml(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, org.w3c.dom.Node, nextapp.echo2.app.Component)
     */
    public void renderHtml(RenderContext rc, ServerComponentUpdate update, Node parentNode, Component component) {
        ContentPane contentPane = (ContentPane) component;
        
        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(CONTENT_PANE_SERVICE.getId());

        Document document = parentNode.getOwnerDocument();
        Element divElement = document.createElement("div");
        divElement.setAttribute("id", ContainerInstance.getElementId(component));
        
        CssStyle cssStyle = new CssStyle();
        cssStyle.setAttribute("position", "absolute");
        cssStyle.setAttribute("width", "100%");
        cssStyle.setAttribute("height", "100%");
        cssStyle.setAttribute("overflow", "hidden");
        ColorRender.renderToStyle(cssStyle, (Color) contentPane.getRenderProperty(ContentPane.PROPERTY_FOREGROUND),
                (Color) contentPane.getRenderProperty(ContentPane.PROPERTY_BACKGROUND));
        FontRender.renderToStyle(cssStyle, (Font) contentPane.getRenderProperty(ContentPane.PROPERTY_FONT));
        FillImageRender.renderToStyle(cssStyle, rc, this, contentPane, IMAGE_ID_BACKGROUND, 
                (FillImage) contentPane.getRenderProperty(ContentPane.PROPERTY_BACKGROUND_IMAGE), 0);
        divElement.setAttribute("style", cssStyle.renderInline());
        
        parentNode.appendChild(divElement);

        // Render initialization directive.
        renderInitDirective(rc, contentPane);
        
        Component[] children = contentPane.getVisibleComponents();
        for (int i = 0; i < children.length; ++i) {
            renderChild(rc, update, divElement, contentPane, children[i]);
        }
    }

    /**
     * Renders a directive to the outgoing <code>ServerMessage</code> to 
     * initialize the state of a <code>ContentPane</code>, performing tasks 
     * such as registering event listeners on the client.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param contentPane the <code>ContentPane</code>
     */
    private void renderInitDirective(RenderContext rc, ContentPane contentPane) {
        String elementId = ContainerInstance.getElementId(contentPane);
        ServerMessage serverMessage = rc.getServerMessage();

        Element itemizedUpdateElement = serverMessage.getItemizedDirective(ServerMessage.GROUP_ID_POSTUPDATE,
                "EchoContentPane.MessageProcessor", "init", new String[0], new String[0]);
        Element itemElement = serverMessage.getDocument().createElement("item");
        itemElement.setAttribute("eid", elementId);
        Extent horizontalScroll = (Extent) contentPane.getRenderProperty(ContentPane.PROPERTY_HORIZONTAL_SCROLL);
        if (horizontalScroll != null && horizontalScroll.getValue() != 0) {
            itemElement.setAttribute("horizontal-scroll", ExtentRender.renderCssAttributeValue(horizontalScroll));
        }
        Extent verticalScroll = (Extent) contentPane.getRenderProperty(ContentPane.PROPERTY_VERTICAL_SCROLL);
        if (verticalScroll != null && verticalScroll.getValue() != 0) {
            itemElement.setAttribute("vertical-scroll", ExtentRender.renderCssAttributeValue(verticalScroll));
        }
        itemizedUpdateElement.appendChild(itemElement);
    }
    
    /**
     * Renders a directive to the outgoing <code>ServerMessage</code> to update
     * the scroll bar positions of a <code>ContentPane</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param contentPane the <code>ContentPane</code>
     * @param horizontal a flag indicating whether the horizontal (true) or 
     *        vertical (false) scroll bar position should be updated
     */
    private void renderScrollDirective(RenderContext rc, ContentPane contentPane, boolean horizontal) {
        ServerMessage serverMessage = rc.getServerMessage();
        Element scrollElement = 
                serverMessage.appendPartDirective(ServerMessage.GROUP_ID_POSTUPDATE, "EchoContentPane.MessageProcessor",
                horizontal ? "scroll-horizontal" : "scroll-vertical");
        Extent position = (Extent) contentPane.getRenderProperty(
                horizontal ? ContentPane.PROPERTY_HORIZONTAL_SCROLL : ContentPane.PROPERTY_VERTICAL_SCROLL, EXTENT_0);
        scrollElement.setAttribute("eid", ContainerInstance.getElementId(contentPane));
        scrollElement.setAttribute("position", ExtentRender.renderCssAttributeValue(position));
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
            DomUpdate.renderElementRemove(rc.getServerMessage(), getContainerId(update.getParent(), removedChildren[i]));
        }
    }
    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderUpdate(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String)
     */
    public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update, String targetId) {
        boolean fullReplace = false;
        if (update.hasUpdatedLayoutDataChildren()) {
            fullReplace = true;
        } else if (update.hasUpdatedProperties()) {
            if (!partialUpdateManager.canProcess(rc, update)) {
                fullReplace = true;
            }
        }
        
        if (fullReplace) {
            // Perform full update.
            DomUpdate.renderElementRemove(rc.getServerMessage(), 
                    ContainerInstance.getElementId(update.getParent()));
            renderAdd(rc, update, targetId, update.getParent());
        } else {
            partialUpdateManager.process(rc, update);
            if (update.hasAddedChildren() || update.hasRemovedChildren()) {
                renderContentChange(rc, update);
            }
        }
        return fullReplace;
    }
    
    /**
     * Processes a change to the content of a <code>ContentPane</code>.
     * This method will invoke <code>renderRemoveChildren()</code> and
     * <code>renderAddChildren()</cdoe>.  If the main content has changed,
     * i.e., the non-<code>FloatingPane</code> child, it will unregister
     * and re-register scrolling listeners by rendering dispose and then
     * init directives. 
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update
     */
    private void renderContentChange(RenderContext rc, ServerComponentUpdate update) {
        boolean primaryContentChanged = false;
        Component[] addedChildren = update.getAddedChildren();
        for (int i = 0; i < addedChildren.length; ++i) {
            if (!(addedChildren[i] instanceof FloatingPane)) {
                primaryContentChanged = true;
                break;
            }
        }
        if (!primaryContentChanged) {
            Component[] removedChildren = update.getRemovedChildren();
            for (int i = 0; i < removedChildren.length; ++i) {
                if (!(removedChildren[i] instanceof FloatingPane)) {
                    primaryContentChanged = true;
                    break;
                }
            }
        }
        
        if (primaryContentChanged) {
            renderDisposeDirective(rc, (ContentPane) update.getParent());
        }

        renderRemoveChildren(rc, update);
        renderAddChildren(rc, update);

        if (primaryContentChanged) {
            renderInitDirective(rc, (ContentPane) update.getParent());
        }
    }
    
    /**
     * @see nextapp.echo2.webcontainer.PropertyUpdateProcessor#processPropertyUpdate(
     *      nextapp.echo2.webcontainer.ContainerInstance,
     *      nextapp.echo2.app.Component, org.w3c.dom.Element)
     */
    public void processPropertyUpdate(ContainerInstance ci, Component component, Element propertyElement) {
        if ("horizontalScroll".equals(propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_NAME))) {
            Extent newValue = ExtentRender.toExtent(propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_VALUE)); 
            ci.getUpdateManager().getClientUpdateManager().setComponentProperty(component, 
                    ContentPane.PROPERTY_HORIZONTAL_SCROLL, newValue);
        } else if ("verticalScroll".equals(propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_NAME))) {
            Extent newValue = ExtentRender.toExtent(propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_VALUE)); 
            ci.getUpdateManager().getClientUpdateManager().setComponentProperty(component, 
                    ContentPane.PROPERTY_VERTICAL_SCROLL, newValue);
        } 
    }
}
