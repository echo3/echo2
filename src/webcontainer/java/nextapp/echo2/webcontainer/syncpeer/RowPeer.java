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
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.LayoutData;
import nextapp.echo2.app.layout.CellLayoutData;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.DomUpdateSupport;
import nextapp.echo2.webcontainer.PartialUpdateManager;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.RenderState;
import nextapp.echo2.webcontainer.ComponentSynchronizePeer;
import nextapp.echo2.webcontainer.SynchronizePeerFactory;
import nextapp.echo2.webcontainer.partialupdate.BorderUpdate;
import nextapp.echo2.webcontainer.partialupdate.ColorUpdate;
import nextapp.echo2.webcontainer.partialupdate.InsetsUpdate;
import nextapp.echo2.webcontainer.propertyrender.BorderRender;
import nextapp.echo2.webcontainer.propertyrender.CellLayoutDataRender;
import nextapp.echo2.webcontainer.propertyrender.ColorRender;
import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
import nextapp.echo2.webcontainer.propertyrender.FontRender;
import nextapp.echo2.webcontainer.propertyrender.InsetsRender;
import nextapp.echo2.webrender.ClientProperties;
import nextapp.echo2.webrender.output.CssStyle;
import nextapp.echo2.webrender.servermessage.DomUpdate;

/**
 * Synchronization peer for <code>nextapp.echo2.app.Row</code> components.
 * <p>
 * This class should not be extended or used by classes outside of the
 * Echo framework.
 */
public class RowPeer 
implements ComponentSynchronizePeer, DomUpdateSupport  {

    /**
     * <code>RenderState</code> implementation.
     */
    private static class RowPeerRenderState 
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
    
    protected PartialUpdateManager partialUpdateManager;
    
    /**
     * Default constructor.
     */
    public RowPeer() {
        partialUpdateManager = new PartialUpdateManager();
        partialUpdateManager.add(Row.PROPERTY_BORDER, new BorderUpdate(Row.PROPERTY_BORDER, null,
                BorderUpdate.CSS_BORDER));
        partialUpdateManager.add(Row.PROPERTY_FOREGROUND, new ColorUpdate(Row.PROPERTY_FOREGROUND, null, 
                ColorUpdate.CSS_COLOR));
        partialUpdateManager.add(Row.PROPERTY_BACKGROUND, new ColorUpdate(Row.PROPERTY_BACKGROUND, null, 
                ColorUpdate.CSS_BACKGROUND_COLOR));
        partialUpdateManager.add(Row.PROPERTY_INSETS, new InsetsUpdate(Row.PROPERTY_INSETS, null, 
                InsetsUpdate.CSS_PADDING));
    }
    
    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#getContainerId(nextapp.echo2.app.Component)
     */
    public String getContainerId(Component child) {
        return ContainerInstance.getElementId(child.getParent()) + "_cell_" + ContainerInstance.getElementId(child);
    }
    
    /**
     * Returns the <code>CellLayoutData</code> of the given child,
     * or null if it does not provide <code>CellLayoutData</code>.
     * 
     * @param child the child component
     * @return the layout data
     */
    private CellLayoutData getLayoutData(Component child) {
        LayoutData layoutData = (LayoutData) child.getRenderProperty(Component.PROPERTY_LAYOUT_DATA);
        if (layoutData instanceof CellLayoutData) {
            return (CellLayoutData) layoutData;
        } else {
            return null;
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
     * Renders child components which were added to a <code>Colunm</code>, as 
     * described in the provided <code>ServerComponentUpdate</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update
     */
    private void renderAddChildren(RenderContext rc, ServerComponentUpdate update) {
        Row row = (Row) update.getParent();
        String elementId = ContainerInstance.getElementId(row);
        String trElementId = elementId + "_tr";
        
        Component[] components = update.getParent().getVisibleComponents();
        Component[] addedChildren = update.getAddedChildren();
        
        for (int componentIndex = components.length - 1; componentIndex >= 0; --componentIndex) {
            boolean childFound = false;
            for (int addedChildrenIndex = 0; !childFound && addedChildrenIndex < addedChildren.length; ++addedChildrenIndex) {
                if (addedChildren[addedChildrenIndex] == components[componentIndex]) {
                    DocumentFragment htmlFragment = rc.getServerMessage().getDocument().createDocumentFragment();
                    renderChild(rc, update, htmlFragment, row, components[componentIndex]);
                    if (componentIndex == components.length - 1) {
                        DomUpdate.renderElementAdd(rc.getServerMessage(), trElementId, htmlFragment);
                    } else {
                        DomUpdate.renderElementAdd(rc.getServerMessage(), trElementId, 
                                elementId + "_cell_" + ContainerInstance.getElementId(components[componentIndex + 1]), 
                                htmlFragment);
                    }
                    childFound = true;
                }
            }
        }

        // Special case: Recall the child which was rendered at the last index of the row on the previous
        // rendering.  If this child is still present but is no longer at the last index, render a spacing
        // row beneath it (if necessary).
        RowPeerRenderState renderState = (RowPeerRenderState) rc.getContainerInstance().getRenderState(row);
        if (renderState != null && renderState.lastChild != null) {
            int previousLastChildIndex = row.visibleIndexOf(renderState.lastChild);
            if (previousLastChildIndex != -1 && previousLastChildIndex != row.getVisibleComponentCount() - 1) {
                // Child which was previously last is present, but no longer last.
                DocumentFragment htmlFragment = rc.getServerMessage().getDocument().createDocumentFragment();
                renderCellSpacingRow(htmlFragment, row, renderState.lastChild);
                DomUpdate.renderElementAdd(rc.getServerMessage(), trElementId,
                        elementId + "_cell_" + ContainerInstance.getElementId(components[previousLastChildIndex + 1]),
                        htmlFragment);
            }
        }
    }
    
    /**
     * Renders an individual child component of the <code>Row</code>.
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
        Element tdElement = document.createElement("td");
        String cellId = ContainerInstance.getElementId(component) + "_cell_" + childId;
        tdElement.setAttribute("id", cellId);
        
        // Configure cell style.
        CssStyle cssStyle = new CssStyle();
        CellLayoutDataRender.renderToStyle(cssStyle, getLayoutData(child), "0px");
        tdElement.setAttribute("style", cssStyle.renderInline());
        
        parentNode.appendChild(tdElement);
        
        renderCellSpacingRow(parentNode, (Row) component, child);
        
        renderAddChild(rc, update, tdElement, child);
    }
    
    /**
     * Renders a "spacing row" beneath a row cell to provide
     * cell spacing.
     * 
     * @param parentNode the containing node to which the child
     *        should be appended
     * @param row the <code>Row</code> being updated
     * @param child the child preceeding the spacing row
     */
    private void renderCellSpacingRow(Node parentNode, Row row, Component child) {
        Extent cellSpacing = (Extent) row.getRenderProperty(Row.PROPERTY_CELL_SPACING);
        if (!ExtentRender.isZeroLength(cellSpacing) && row.visibleIndexOf(child) != row.getVisibleComponentCount() - 1) {
            Element spacingElement = parentNode.getOwnerDocument().createElement("td");
            spacingElement.setAttribute("id", ContainerInstance.getElementId(row) + "_spacing_" 
                    + ContainerInstance.getElementId(child));
            CssStyle spacingCssStyle = new CssStyle();
            spacingCssStyle.setAttribute("width", ExtentRender.renderCssAttributeValue(cellSpacing));
            spacingCssStyle.setAttribute("font-size", "1px");
            spacingCssStyle.setAttribute("line-height", "0px");
            spacingElement.setAttribute("style", spacingCssStyle.renderInline());
            parentNode.appendChild(spacingElement);
        }
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
        Row row = (Row) component;
        Border border = (Border) row.getRenderProperty(Grid.PROPERTY_BORDER);
        
        String elementId = ContainerInstance.getElementId(row);
        
        Document document = parentNode.getOwnerDocument();
        Element tableElement = document.createElement("table");
        tableElement.setAttribute("id", elementId);
        parentNode.appendChild(tableElement);
        
        CssStyle tableCssStyle = new CssStyle();
        
        BorderRender.renderToStyle(tableCssStyle, border);
        ColorRender.renderToStyle(tableCssStyle, (Color) row.getRenderProperty(Row.PROPERTY_FOREGROUND), 
                (Color) row.getRenderProperty(Row.PROPERTY_BACKGROUND));
        FontRender.renderToStyle(tableCssStyle, (Font) row.getRenderProperty(Row.PROPERTY_FONT));
        Insets insets = (Insets) row.getRenderProperty(Row.PROPERTY_INSETS);
        if (insets == null) {
            tableCssStyle.setAttribute("padding", "0px");
        } else {
            InsetsRender.renderToStyle(tableCssStyle, "padding", insets);
        }

        tableCssStyle.setAttribute("border-collapse", "collapse");
        Extent borderSize = border == null ? null : border.getSize();
        if (borderSize != null) {
            if (!rc.getContainerInstance().getClientProperties().getBoolean(ClientProperties.QUIRK_CSS_BORDER_COLLAPSE_MARGIN)) {
                tableCssStyle.setAttribute("margin", ExtentRender.renderCssAttributeValueHalf(borderSize));
            }
        }
        
        tableElement.setAttribute("style", tableCssStyle.renderInline());
        
        Element tbodyElement = document.createElement("tbody");
        tbodyElement.setAttribute("id", elementId + "_tbody");
        tableElement.appendChild(tbodyElement);
        
        Element trElement = document.createElement("tr");
        trElement.setAttribute("id", elementId + "_tr");
        tbodyElement.appendChild(trElement);
        
        Component[] children = row.getVisibleComponents();
        for (int i = 0; i < children.length; ++i) {
            renderChild(rc, update, trElement, component, children[i]);
        }
        
        storeRenderState(rc, row);
    }
    
    /**
     * Renders removal operations for child components which were removed from 
     * a <code>Row</code>, as described in the provided 
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
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderUpdate(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String)
     */
    public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update, String targetId) {
        // Determine if fully replacing the component is required.
        boolean fullReplace = false;
        if (update.hasUpdatedLayoutDataChildren()) {
            // BUGBUG. performing unnecessary full replace on layout changes.
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
     * @param component the <code>Row</code> component
     */
    private void storeRenderState(RenderContext rc, Component component) {
        int componentCount = component.getVisibleComponentCount();
        RowPeerRenderState renderState = new RowPeerRenderState();
        if (componentCount > 0) {
            renderState.lastChild = component.getVisibleComponent(componentCount - 1);
        }
        rc.getContainerInstance().setRenderState(component, renderState);
    }
}
