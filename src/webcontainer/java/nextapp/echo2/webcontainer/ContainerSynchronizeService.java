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

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Command;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Window;
import nextapp.echo2.app.update.PropertyUpdate;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.app.update.ServerUpdateManager;
import nextapp.echo2.app.update.UpdateManager;
import nextapp.echo2.webrender.Connection;
import nextapp.echo2.webrender.ServerMessage;
import nextapp.echo2.webrender.UserInstance;
import nextapp.echo2.webrender.service.SynchronizeService;
import nextapp.echo2.webrender.util.DomUtil;

//BUGBUG. Potentially move real work out of the service, notably processServerUpdates()

/**
 * A service which synchronizes the state of the client with that of the
 * server.  Requests made to this service are in the form of "ClientMessage"
 * XML documents which describe the users actions since the last 
 * synchronization, e.g., input typed into text fields and the action taken
 * (e.g., a button press) which caused the server interaction.
 * The service then communicates these changes to the server-side application,
 * and then generates an output "ServerMessage" containing instructions to 
 * update the client-side state of the application to the updated server-side
 * state.
 * <p>
 * This class is derived from the base class <code>SynchronizeService</code>
 * of the web renderer, which handles the lower-level work.
 */
public class ContainerSynchronizeService extends SynchronizeService {
    
    /**
     * A single shared instance of this stateless service.
     */
    public static final ContainerSynchronizeService INSTANCE = new ContainerSynchronizeService();

    /**
     * Determines if any of the <code>Component</code> object in the provided 
     * set of "potential" ancestors is in fact an ancestor of 
     * <code>component</code>.
     * 
     * @param potentialAncestors a set containing <code>Component</code>s
     * @param component the <code>Component</code> to evaluate
     * @return true if any component in <code>potentialAncestors</code> is an
     *         ancestor of <code>component</code>
     */
    private static boolean isAncestor(Set potentialAncestors, Component component) {
        component = component.getParent();
        while (component != null) {
            if (potentialAncestors.contains(component)) {
                return true;
            }
            component = component.getParent();
        }
        return false;
    }

    /**
     * <code>ClientMessagePartProcessor</code> to process user-interface 
     * component input message parts.
     */
    private ClientMessagePartProcessor propertyUpdateProcessor = new ClientMessagePartProcessor() {
        
        /**
         * @see nextapp.echo2.webrender.service.SynchronizeService.ClientMessagePartProcessor#getName()
         */
        public String getName() {
            return "EchoPropertyUpdate";
        }
        
        /**
         * @see nextapp.echo2.webrender.service.SynchronizeService.ClientMessagePartProcessor#process(
         *      nextapp.echo2.webrender.UserInstance, org.w3c.dom.Element)
         */
        public void process(UserInstance userInstance, Element messagePartElement) {
            ContainerInstance ci = (ContainerInstance) userInstance;
            Element[] propertyElements = DomUtil.getChildElementsByTagName(messagePartElement, "property");
            for (int i = 0; i < propertyElements.length; ++i) {
                String componentId = propertyElements[i].getAttribute("component-id");
                Component component = ci.getComponentByElementId(componentId);
                if (component == null) {
                    // Component removed.  This should not frequently occur, however in certain cases,
                    // e.g., dragging a window during an during before, during, after a server pushed update
                    // can result in the condition where input is received from a component which no longer
                    // is registered.
                    continue;
                }
                ComponentSynchronizePeer syncPeer = SynchronizePeerFactory.getPeerForComponent(component.getClass());
                if (!(syncPeer instanceof PropertyUpdateProcessor)) {
                    throw new IllegalStateException("Target peer is not an PropertyUpdateProcessor.");
                }
                ((PropertyUpdateProcessor) syncPeer).processPropertyUpdate(ci, component, propertyElements[i]);
            }
        }
    };

    /**
     * <code>ClientMessagePartProcessor</code> to process user-interface 
     * component action message parts.
     */
    private ClientMessagePartProcessor actionProcessor = new ClientMessagePartProcessor() {
        
        /**
         * @see nextapp.echo2.webrender.service.SynchronizeService.ClientMessagePartProcessor#getName()
         */
        public String getName() {
            return "EchoAction";
        }
        
        /**
         * @see nextapp.echo2.webrender.service.SynchronizeService.ClientMessagePartProcessor#process(
         *      nextapp.echo2.webrender.UserInstance, org.w3c.dom.Element)
         */
        public void process(UserInstance userInstance, Element messagePartElement) {
            ContainerInstance ci = (ContainerInstance) userInstance;
            Element actionElement = DomUtil.getChildElementByTagName(messagePartElement, "action");
            String componentId = actionElement.getAttribute("component-id");
            Component component = ci.getComponentByElementId(componentId);
            if (component == null) {
                // Component removed.  This should not frequently occur, however in certain cases,
                // e.g., dragging a window during an during before, during, after a server pushed update
                // can result in the condition where input is received from a component which no longer
                // is registered.
                return;
            }
            ComponentSynchronizePeer syncPeer = SynchronizePeerFactory.getPeerForComponent(component.getClass());
            if (!(syncPeer instanceof ActionProcessor)) {
                throw new IllegalStateException("Target peer is not an ActionProcessor.");
            }
            ((ActionProcessor) syncPeer).processAction(ci, component, actionElement);
        }
    };
    
    /**
     * Creates a new <code>ContainerSynchronizeService</code>.
     * Installs "ClientMessage" part processors.
     */
    private ContainerSynchronizeService() {
        super();
        registerClientMessagePartProcessor(propertyUpdateProcessor);
        registerClientMessagePartProcessor(actionProcessor);
    }
    
    /**
     * Performs disposal operations on components which have been removed from
     * the hierarchy. Removes any <code>RenderState</code> objects being
     * stored in the <code>ContainerInstance</code> for the disposed
     * components. Invokes <code>ComponentSynchronizePeer.renderDispose()</code>
     * such that the peers of the components can dispose of resources on the
     * client.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param componentUpdate the <code>ServerComponentUpdate</code> causing
     *        components to be disposed.
     * @param disposedComponents the components to dispose
     */
    private void disposeComponents(RenderContext rc, ServerComponentUpdate componentUpdate, Component[] disposedComponents) {
        ContainerInstance ci = rc.getContainerInstance();
        for (int i = 0; i < disposedComponents.length; ++i) {
            ComponentSynchronizePeer disposedSyncPeer = SynchronizePeerFactory.getPeerForComponent(
                    disposedComponents[i].getClass());
            disposedSyncPeer.renderDispose(rc, componentUpdate, disposedComponents[i]);
            ci.removeRenderState(disposedComponents[i]);
        }
    }
    
    /**
     * Retrieves information about the current focused component on the client,
     * if provided, and in such case notifies the 
     * <code>ApplicationInstance</code> of the focus.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param clientMessageDocument the ClientMessage <code>Document</code> to 
     *        retrieve focus information from
     */
    private void processClientFocusedComponent(RenderContext rc, Document clientMessageDocument) {
        if (clientMessageDocument.getDocumentElement().hasAttribute("focus")) {
            String focusedComponentId = clientMessageDocument.getDocumentElement().getAttribute("focus");
            Component component = null;
            if (focusedComponentId.length() > 2) {
                // Valid component id.
                component = rc.getContainerInstance().getComponentByElementId(focusedComponentId);
            }
            ApplicationInstance applicationInstance = rc.getContainerInstance().getApplicationInstance();
            applicationInstance.getUpdateManager().getClientUpdateManager().setApplicationProperty(
                    ApplicationInstance.FOCUSED_COMPONENT_CHANGED_PROPERTY, component);
        }
    }
    
    /**
     * Processes updates from the application, generating an outgoing
     * <code>ServerMessage</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     */
    private void processServerUpdates(RenderContext rc) {
        UpdateManager updateManager = rc.getContainerInstance().getUpdateManager();
        ServerUpdateManager serverUpdateManager = updateManager.getServerUpdateManager();
        ServerComponentUpdate[] componentUpdates = updateManager.getServerUpdateManager().getComponentUpdates();
        
        if (serverUpdateManager.isFullRefreshRequired()) {
            Window window = rc.getContainerInstance().getApplicationInstance().getDefaultWindow();
            ServerComponentUpdate fullRefreshUpdate = componentUpdates[0];
            
            // Dispose of removed descendants.
            Component[] removedDescendants = fullRefreshUpdate.getRemovedDescendants();
            disposeComponents(rc, fullRefreshUpdate, removedDescendants);
            
            // Perform full refresh.
            RootSynchronizePeer rootSyncPeer 
                    = (RootSynchronizePeer) SynchronizePeerFactory.getPeerForComponent(window.getClass());
            rootSyncPeer.renderRefresh(rc, fullRefreshUpdate, window);
            
            setRootLayoutDirection(rc);
        } else {
            // Set of Components whose HTML was entirely re-rendered, negating the need
            // for updates of their children to be processed.
            Set fullyReplacedHierarchies = new HashSet();
    
            for (int i = 0; i < componentUpdates.length; ++i) {
                // Dispose of removed children.
                Component[] removedChildren = componentUpdates[i].getRemovedChildren();
                disposeComponents(rc, componentUpdates[i], removedChildren);
    
                // Dispose of removed descendants.
                Component[] removedDescendants = componentUpdates[i].getRemovedDescendants();
                disposeComponents(rc, componentUpdates[i], removedDescendants);
    
                // Perform update.
                Component parentComponent = componentUpdates[i].getParent();
                if (!isAncestor(fullyReplacedHierarchies, parentComponent)) {
                    // Only perform update if ancestor of updated component is NOT contained in
                    // the set of components whose descendants were fully replaced.
                    ComponentSynchronizePeer syncPeer = SynchronizePeerFactory.getPeerForComponent(parentComponent.getClass());
                    String targetId;
                    if (parentComponent.getParent() == null) {
                        targetId = null;
                    } else {
                        ComponentSynchronizePeer parentSyncPeer 
                                = SynchronizePeerFactory.getPeerForComponent(parentComponent.getParent().getClass());
                        targetId = parentSyncPeer.getContainerId(parentComponent);
                    }
                    boolean fullReplacement = syncPeer.renderUpdate(rc, componentUpdates[i], targetId);
                    if (fullReplacement) {
                        fullyReplacedHierarchies.add(parentComponent);
                    }
                }
            }
        }

        // Execute queued commands.
        Command[] commands = serverUpdateManager.getCommands();
        for (int i = 0; i < commands.length; i++) {
            CommandSynchronizePeer peer = SynchronizePeerFactory.getPeerForCommand(commands[i].getClass()); 
            peer.render(rc,commands[i]);
        }
    }
    
    /**
     * @see nextapp.echo2.webrender.service.SynchronizeService#renderInit(nextapp.echo2.webrender.Connection,
     *      org.w3c.dom.Document)
     */
    protected ServerMessage renderInit(Connection conn, Document clientMessageDocument) {
        ServerMessage serverMessage = new ServerMessage();
        RenderContext rc = new RenderContextImpl(conn, serverMessage);
        ApplicationInstance applicationInstance = rc.getContainerInstance().getApplicationInstance();
        try {
            ApplicationInstance.setActive(applicationInstance);
            processClientMessage(conn, clientMessageDocument);
            applicationInstance.getUpdateManager().purge();

            Window window = applicationInstance.getDefaultWindow();
            ContentPane content = window.getContent();
            
            ServerComponentUpdate componentUpdate = new ServerComponentUpdate(content);
            ComponentSynchronizePeer syncPeer = SynchronizePeerFactory.getPeerForComponent(content.getClass());
            ComponentSynchronizePeer parentSyncPeer = SynchronizePeerFactory.getPeerForComponent(window.getClass());
            String targetId = parentSyncPeer.getContainerId(content);
            syncPeer.renderAdd(rc, componentUpdate, targetId, content);
            
            //BUGBUG. This method of setting server delay messages is bogus.
            ServerDelayMessageConfigurator.configureDefault(rc);
            
            //BUGBUG. clean-up how these operations are invoked on init/update.
            setAsynchronousMonitorInterval(rc);
            setFocus(rc, true);
            setModalContextRootId(rc);
            setRootLayoutDirection(rc);
            
            return serverMessage;
        } finally {
            ApplicationInstance.setActive(null);
        }
    }
    
    /**
     * @see nextapp.echo2.webrender.service.SynchronizeService#renderUpdate(nextapp.echo2.webrender.Connection,
     *      org.w3c.dom.Document)
     */
    protected ServerMessage renderUpdate(Connection conn, Document clientMessageDocument) {
        ServerMessage serverMessage = new ServerMessage();
        RenderContext rc = new RenderContextImpl(conn, serverMessage);
        
        ApplicationInstance applicationInstance = rc.getContainerInstance().getApplicationInstance();
        
        try {
            // Mark instance as active.
            ApplicationInstance.setActive(applicationInstance);
            
            UpdateManager updateManager = applicationInstance.getUpdateManager();
            
            processClientFocusedComponent(rc, clientMessageDocument);
            
            // Process updates from client.
            processClientMessage(conn, clientMessageDocument);
            
            updateManager.processClientUpdates();
            applicationInstance.processQueuedTasks();
            
            // Process updates from server.
            processServerUpdates(rc);
            
            setAsynchronousMonitorInterval(rc);
            setFocus(rc, false);
            setModalContextRootId(rc);
            
            updateManager.purge();
            
            return serverMessage;
        } finally {
            // Mark instance as inactive.
            ApplicationInstance.setActive(null);
        }
    }

    /**
     * Sets the interval between asynchronous monitor requests.
     * 
     * @param rc the relevant <code>RenderContext</code>.
     */
    private void setAsynchronousMonitorInterval(RenderContext rc) {
        boolean hasTaskQueues = rc.getContainerInstance().getApplicationInstance().hasTaskQueues();
        if (hasTaskQueues) {
            int interval = rc.getContainerInstance().getCallbackInterval();
            rc.getServerMessage().setAsynchronousMonitorInterval(interval);
        } else {
            rc.getServerMessage().setAsynchronousMonitorInterval(-1);
        }
    }
    
    /**
     * Update the <code>ServerMessage</code> to set the focused component if
     * required.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param initial a flag indicating whether the initial synchronization is
     *        being performed, i.e., whether this method is being invoked from
     *        <code>renderInit()</code>
     */
    private void setFocus(RenderContext rc, boolean initial) {
        ApplicationInstance applicationInstance = rc.getContainerInstance().getApplicationInstance();
        Component focusedComponent = null;
        if (initial) {
            focusedComponent = applicationInstance.getFocusedComponent();
        } else {
            ServerUpdateManager serverUpdateManager = applicationInstance.getUpdateManager().getServerUpdateManager();
            PropertyUpdate focusUpdate = 
                    serverUpdateManager.getApplicationPropertyUpdate(ApplicationInstance.FOCUSED_COMPONENT_CHANGED_PROPERTY);
            if (focusUpdate != null) {
                focusedComponent = (Component) focusUpdate.getNewValue();
            }
        }

        if (focusedComponent != null) {
            ComponentSynchronizePeer componentSyncPeer 
                    = SynchronizePeerFactory.getPeerForComponent(focusedComponent.getClass());
            if (componentSyncPeer instanceof FocusSupport) {
                ((FocusSupport) componentSyncPeer).renderSetFocus(rc, focusedComponent);
            }
        }
    }
    
    /**
     * Update the <code>ServerMessage</code> to describe the current root
     * element of the modal context.
     * 
     * @param rc the relevant <code>RenderContext</code>
     */
    private void setModalContextRootId(RenderContext rc) {
        ApplicationInstance applicationInstance = rc.getContainerInstance().getApplicationInstance();
        Component modalContextRoot = applicationInstance.getModalContextRoot();
        if (modalContextRoot == null) {
            rc.getServerMessage().setModalContextRootId(null);
        } else {
            rc.getServerMessage().setModalContextRootId(ContainerInstance.getElementId(modalContextRoot));
        }
    }
    
    /**
     * Update the <code>ServerMessage</code> to describe the current root
     * layout direction
     * 
     * @param rc the relevant <code>RenderContext</code>
     */
    private void setRootLayoutDirection(RenderContext rc) {
        ApplicationInstance applicationInstance = rc.getContainerInstance().getApplicationInstance();
        rc.getServerMessage().setRootLayoutDirection(applicationInstance.getLayoutDirection().isLeftToRight()
                ? ServerMessage.LEFT_TO_RIGHT : ServerMessage.RIGHT_TO_LEFT);
    }
}
