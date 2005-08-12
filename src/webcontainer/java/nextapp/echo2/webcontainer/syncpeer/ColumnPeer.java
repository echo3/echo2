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

import nextapp.echo2.app.Border;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.LayoutData;
import nextapp.echo2.app.layout.ColumnLayoutData;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.DomUpdateSupport;
import nextapp.echo2.webcontainer.PartialUpdateManager;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.RenderState;
import nextapp.echo2.webcontainer.ComponentSynchronizePeer;
import nextapp.echo2.webcontainer.SynchronizePeerFactory;
import nextapp.echo2.webcontainer.image.ImageRenderSupport;
import nextapp.echo2.webcontainer.partialupdate.BorderUpdate;
import nextapp.echo2.webcontainer.partialupdate.ColorUpdate;
import nextapp.echo2.webcontainer.partialupdate.InsetsUpdate;
import nextapp.echo2.webcontainer.propertyrender.BorderRender;
import nextapp.echo2.webcontainer.propertyrender.CellLayoutDataRender;
import nextapp.echo2.webcontainer.propertyrender.ColorRender;
import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
import nextapp.echo2.webcontainer.propertyrender.FontRender;
import nextapp.echo2.webcontainer.propertyrender.InsetsRender;
import nextapp.echo2.webrender.output.CssStyle;
import nextapp.echo2.webrender.servermessage.DomUpdate;

/**
 * Synchronization peer for <code>nextapp.echo2.app.Column</code> components.
 * <p>
 * This class should not be extended or used by classes outside of the
 * Echo framework.
 */
public class ColumnPeer 
implements ComponentSynchronizePeer, DomUpdateSupport, ImageRenderSupport {
    
    /**
     * <code>RenderState</code> implementation.
     */
    private static class ColumnPeerRenderState 
    implements RenderState {
        
        /**
         * The child <code>Component</code> which had the highest index during 
         * the last rendering.  This information is necessary when rendering 
         * cell spacing, as the last component will not have a "spacing" row
         * beneath it.  Thus, if it is no longer the last component due to an
         * add, one will need to be added beneath it. 
         */
        public Component lastChild;
    }
    
    private PartialUpdateManager partialUpdateManager;
    
    /**
     * Default constructor.
     */
    public ColumnPeer() {
        partialUpdateManager = new PartialUpdateManager();
        partialUpdateManager.add(Column.PROPERTY_BORDER, new BorderUpdate(Column.PROPERTY_BORDER, null,
                BorderUpdate.CSS_BORDER));
        partialUpdateManager.add(Column.PROPERTY_FOREGROUND, new ColorUpdate(Column.PROPERTY_FOREGROUND, null, 
                ColorUpdate.CSS_COLOR));
        partialUpdateManager.add(Column.PROPERTY_BACKGROUND, new ColorUpdate(Column.PROPERTY_BACKGROUND, null, 
                ColorUpdate.CSS_BACKGROUND_COLOR));
        partialUpdateManager.add(Column.PROPERTY_INSETS, new InsetsUpdate(Column.PROPERTY_INSETS, null, 
                InsetsUpdate.CSS_PADDING));
    }
    
    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#getContainerId(nextapp.echo2.app.Component)
     */
    public String getContainerId(Component child) {
        return ContainerInstance.getElementId(child.getParent()) + "_cell_" + ContainerInstance.getElementId(child);
    }
    
    /**
     * @see nextapp.echo2.webcontainer.image.ImageRenderSupport#getImage(nextapp.echo2.app.Component, java.lang.String)
     */
    public ImageReference getImage(Component component, String imageId) {
        // Only source of ImageReferences from this component are CellLayoutData background images:
        // Delegate work to CellLayoutDataRender convenience method.
        return CellLayoutDataRender.getCellLayoutDataBackgroundImage(component, imageId);
    }

    /**
     * Returns the <code>ColumnLayoutData</code> of the given child,
     * or null if it does not provide layout data.
     *
     * @param child the child component
     * @return the layout data
     * @throws java.lang.RuntimeException if the the provided
     *         <code>LayoutData</code> is not a <code>ColumnLayoutData</code>
     */
    private ColumnLayoutData getLayoutData(Component child) {
        LayoutData layoutData = (LayoutData) child.getRenderProperty(Component.PROPERTY_LAYOUT_DATA);
        if (layoutData == null) {
            return null;
        } else if (layoutData instanceof ColumnLayoutData) {
            return (ColumnLayoutData) layoutData;
        } else {
            throw new RuntimeException("Invalid LayoutData for Column Child: " + layoutData.getClass().getName());
        }
    }

    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderAdd(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String, nextapp.echo2.app.Component)
     */
    public void renderAdd(RenderContext rc, ServerComponentUpdate update, String targetId, Component component) {
        DocumentFragment htmlFragment = rc.getServerMessage().getDocument().createDocumentFragment();
        renderHtml(rc, update, htmlFragment, component);
        DomUpdate.renderElementAdd(rc.getServerMessage(), targetId, htmlFragment);
    }
    
    /**
     * Renders a child component.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update
     * @param parentElement the HTML element which should contain the child
     * @param child the child component to render
     */
    private void renderAddChild(RenderContext rc, ServerComponentUpdate update, Element parentElement, Component child) {
        ComponentSynchronizePeer syncPeer = SynchronizePeerFactory.getPeerForComponent(child.getClass());
        if (syncPeer instanceof DomUpdateSupport) {
            ((DomUpdateSupport) syncPeer).renderHtml(rc, update, parentElement, child);
        } else {
            syncPeer.renderAdd(rc, update, getContainerId(child), child);
        }
    }
    
    /**
     * Renders child components which were added to a <code>Column</code>, as 
     * described in the provided <code>ServerComponentUpdate</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update
     */
    private void renderAddChildren(RenderContext rc, ServerComponentUpdate update) {
        Column column = (Column) update.getParent();
        String elementId = ContainerInstance.getElementId(column);
        
        Component[] components = update.getParent().getVisibleComponents();
        Component[] addedChildren = update.getAddedChildren();
        
        for (int componentIndex = components.length - 1; componentIndex >= 0; --componentIndex) {
            boolean childFound = false;
            for (int addedChildrenIndex = 0; !childFound && addedChildrenIndex < addedChildren.length; ++addedChildrenIndex) {
                if (addedChildren[addedChildrenIndex] == components[componentIndex]) {
                    DocumentFragment htmlFragment = rc.getServerMessage().getDocument().createDocumentFragment();
                    renderChild(rc, update, htmlFragment, column, components[componentIndex]);
                    if (componentIndex == components.length - 1) {
                        DomUpdate.renderElementAdd(rc.getServerMessage(), elementId, htmlFragment);
                    } else {
                        DomUpdate.renderElementAdd(rc.getServerMessage(), elementId, 
                                elementId + "_cell_" + ContainerInstance.getElementId(components[componentIndex + 1]), 
                                htmlFragment);
                    }
                    childFound = true;
                }
            }
        }

        // Special case: Recall the child which was rendered at the last index of the column on the previous
        // rendering.  If this child is still present but is no longer at the last index, render a spacing
        // cell beneath it (if necessary).
        ColumnPeerRenderState renderState = (ColumnPeerRenderState) rc.getContainerInstance().getRenderState(column);
        if (renderState != null && renderState.lastChild != null) {
            int previousLastChildIndex = column.visibleIndexOf(renderState.lastChild);
            if (previousLastChildIndex != -1 && previousLastChildIndex != column.getVisibleComponentCount() - 1) {
                // At this point it is known that the child which was previously last is present, but is no longer last.

                // In the event the child was removed and re-added, the special case is unnecessary.
                boolean lastChildMoved = false;
                for (int i = 0; i < addedChildren.length; ++i) {
                    if (renderState.lastChild == addedChildren[i]) {
                        lastChildMoved = true;
                    }
                }
                if (!lastChildMoved) {
                    DocumentFragment htmlFragment = rc.getServerMessage().getDocument().createDocumentFragment();
                    renderSpacingCell(htmlFragment, column, renderState.lastChild);
                    DomUpdate.renderElementAdd(rc.getServerMessage(), elementId,
                            elementId + "_cell_" + ContainerInstance.getElementId(components[previousLastChildIndex + 1]),
                            htmlFragment);
                }
            }
        }
    }
    
    /**
     * Renders an individual child component of the <code>Column</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the <code>ServerComponentUpdate</code> being performed
     * @param parentNode the containing node to which the child should be 
     *        appended
     * @param child The child <code>Component</code> to be rendered
     */
    private void renderChild(RenderContext rc, ServerComponentUpdate update, Node parentNode, 
            Component component, Component child) {
        Document document = parentNode.getOwnerDocument();
        String childId = ContainerInstance.getElementId(child);
        Element divElement = document.createElement("div");
        String cellId = ContainerInstance.getElementId(component) + "_cell_" + childId;
        divElement.setAttribute("id", cellId);
        
        // Configure cell style.
        CssStyle cssStyle = new CssStyle();
        ColumnLayoutData layoutData = getLayoutData(child);
        CellLayoutDataRender.renderToElementAndStyle(divElement, cssStyle, child, layoutData, "0px");
        CellLayoutDataRender.renderBackgroundImageToStyle(cssStyle, rc, this, component, child);
        if (layoutData != null) {
            ExtentRender.renderToStyle(cssStyle, "height", layoutData.getHeight());
        }
        divElement.setAttribute("style", cssStyle.renderInline());
        
        parentNode.appendChild(divElement);
        
        renderSpacingCell(parentNode, (Column) component, child);
        
        renderAddChild(rc, update, divElement, child);
    }
    
    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderDispose(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, nextapp.echo2.app.Component)
     */
    public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component) { }

    /**
     * @see nextapp.echo2.webcontainer.DomUpdateSupport#renderHtml(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, org.w3c.dom.Node, nextapp.echo2.app.Component)
     */
    public void renderHtml(RenderContext rc, ServerComponentUpdate update, Node parentNode, Component component) {
        Column column = (Column) component;
        
        Document document = parentNode.getOwnerDocument();
        Element divElement = document.createElement("div");
        divElement.setAttribute("id", ContainerInstance.getElementId(column));
        
        CssStyle divCssStyle = new CssStyle();
        BorderRender.renderToStyle(divCssStyle, (Border) column.getRenderProperty(Column.PROPERTY_BORDER));
        ColorRender.renderToStyle(divCssStyle, (Color) column.getRenderProperty(Column.PROPERTY_FOREGROUND), 
                (Color) column.getRenderProperty(Column.PROPERTY_BACKGROUND));
        FontRender.renderToStyle(divCssStyle, (Font) column.getRenderProperty(Column.PROPERTY_FONT));
        Insets insets = (Insets) column.getRenderProperty(Column.PROPERTY_INSETS);
        if (insets == null) {
            divCssStyle.setAttribute("padding", "0px");
        } else {
            InsetsRender.renderToStyle(divCssStyle, "padding", insets);
        }
        
        divElement.setAttribute("style", divCssStyle.renderInline());
        
        parentNode.appendChild(divElement);
        
        Component[] children = column.getVisibleComponents();
        for (int i = 0; i < children.length; ++i) {
            renderChild(rc, update, divElement, component, children[i]);
        }
        
        storeRenderState(rc, column);
    }
    
    /**
     * Renders removal operations for child components which were removed from 
     * a <code>Column</code>, as described in the provided 
     * <code>ServerComponentUpdate</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update
     */
    private void renderRemoveChildren(RenderContext rc, ServerComponentUpdate update) {
        Component[] removedChildren = update.getRemovedChildren();
        Component parent = update.getParent();
        String parentId = ContainerInstance.getElementId(parent);
        for (int i = 0; i < removedChildren.length; ++i) {
            String childId = ContainerInstance.getElementId(removedChildren[i]);
            DomUpdate.renderElementRemove(rc.getServerMessage(), 
                    parentId + "_cell_" + childId);
            DomUpdate.renderElementRemove(rc.getServerMessage(), 
                    parentId + "_spacing_" + childId);
        }

        int componentCount = parent.getVisibleComponentCount();
        if (componentCount > 0) {
            DomUpdate.renderElementRemove(rc.getServerMessage(), parentId + "_spacing_" 
                    + ContainerInstance.getElementId(parent.getVisibleComponent(componentCount - 1)));
        }
    }
    
    /**
     * Renders a "spacing cell" beneath a column cell to provide
     * cell spacing.
     * 
     * @param parentNode the containing node to which the child
     *        should be appended
     * @param column the <code>Column</code> being updated
     * @param child the child preceeding the spacing column
     */
    private void renderSpacingCell(Node parentNode, Column column, Component child) {
        Extent cellSpacing = (Extent) column.getRenderProperty(Column.PROPERTY_CELL_SPACING);
        if (!ExtentRender.isZeroLength(cellSpacing) && column.visibleIndexOf(child) != column.getVisibleComponentCount() - 1) {
            Element spacingElement = parentNode.getOwnerDocument().createElement("div");
            spacingElement.setAttribute("id", ContainerInstance.getElementId(column) + "_spacing_" 
                    + ContainerInstance.getElementId(child));
            CssStyle spacingCssStyle = new CssStyle();
            spacingCssStyle.setAttribute("height", ExtentRender.renderCssAttributeValue(cellSpacing));
            spacingCssStyle.setAttribute("font-size", "1px");
            spacingCssStyle.setAttribute("line-height", "0px");
            spacingElement.setAttribute("style", spacingCssStyle.renderInline());
            parentNode.appendChild(spacingElement);
        }
    }
    
    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderUpdate(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String)
     */
    public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update, String targetId) {
        // Determine if fully replacing the component is required.
        boolean fullReplace = false;
        if (update.hasUpdatedLayoutDataChildren()) {
            // TODO: Perform fractional update on LayoutData change instead of full replace.
            fullReplace = true;
        } else if (update.hasUpdatedProperties()) {
            if (!partialUpdateManager.canProcess(rc, update)) {
                fullReplace = true;
            }
        }
        
        if (fullReplace) {
            // Perform full update.
            DomUpdate.renderElementRemove(rc.getServerMessage(), ContainerInstance.getElementId(update.getParent()));
            renderAdd(rc, update, targetId, update.getParent());
        } else {
            // Perform incremental updates.
            if (update.hasRemovedChildren()) {
                renderRemoveChildren(rc, update);
            }
            if (update.hasUpdatedProperties()) {
                partialUpdateManager.process(rc, update);
            }
            if (update.hasAddedChildren()) {
                renderAddChildren(rc, update);
            }
        }
        
        storeRenderState(rc, update.getParent());
        return fullReplace;
    }

    /**
     * Update the stored <code>RenderState</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param component the <code>Column</code> component
     */
    private void storeRenderState(RenderContext rc, Component component) {
        int componentCount = component.getVisibleComponentCount();
        ColumnPeerRenderState renderState = new ColumnPeerRenderState();
        if (componentCount > 0) {
            renderState.lastChild = component.getVisibleComponent(componentCount - 1);
        }
        rc.getContainerInstance().setRenderState(component, renderState);
    }
}
