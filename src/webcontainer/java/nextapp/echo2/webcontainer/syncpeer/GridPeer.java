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

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import nextapp.echo2.app.Border;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.LayoutData;
import nextapp.echo2.app.layout.GridCellLayoutData;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.DomUpdateSupport;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.SynchronizePeer;
import nextapp.echo2.webcontainer.SynchronizePeerFactory;
import nextapp.echo2.webcontainer.propertyrender.BorderRender;
import nextapp.echo2.webcontainer.propertyrender.CellLayoutDataRender;
import nextapp.echo2.webcontainer.propertyrender.ColorRender;
import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
import nextapp.echo2.webcontainer.propertyrender.FontRender;
import nextapp.echo2.webcontainer.propertyrender.InsetsRender;
import nextapp.echo2.webrender.clientupdate.DomUpdate;
import nextapp.echo2.webrender.output.CssStyle;
import nextapp.echo2.webrender.server.ClientProperties;

//BUGBUG? find way to line-wrap cross platform or remove. 

/**
 * Synchronization peer for <code>nextapp.echo2.app.Grid</code> components.
 * <p>
 * This class should not be extended or used by classes outside of the
 * Echo framework.
 */
public class GridPeer 
implements DomUpdateSupport, SynchronizePeer {
    
    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#getContainerId(nextapp.echo2.app.Component)
     */
    public String getContainerId(Component child) {
        return ContainerInstance.getElementId(child.getParent()) + "_td_" 
                + ContainerInstance.getElementId(child);
    }
    
    /**
     * Returns the <code>GridCellLayoutData</code> of the given child,
     * or null if it does not provide <code>GridCellLayoutData</code>.
     * 
     * @param child the child component
     * @return the layout data
     */
    private GridCellLayoutData getLayoutData(Component child) {
        LayoutData layoutData = (LayoutData) child.getRenderProperty(Grid.PROPERTY_LAYOUT_DATA);
        if (layoutData instanceof GridCellLayoutData) {
            return (GridCellLayoutData) layoutData;
        } else {
            return null;
        }
    }
    
    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#renderAdd(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String, nextapp.echo2.app.Component)
     */
    public void renderAdd(RenderContext rc, ServerComponentUpdate update, String targetId, Component component) {
        DocumentFragment htmlFragment = rc.getServerMessage().getDocument().createDocumentFragment();
        renderHtml(rc, update, htmlFragment, component);
        DomUpdate.createDomAdd(rc.getServerMessage(), targetId, htmlFragment);
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
        SynchronizePeer syncPeer = SynchronizePeerFactory.getPeerForComponent(child.getClass());
        if (syncPeer instanceof DomUpdateSupport) {
            ((DomUpdateSupport) syncPeer).renderHtml(rc, update, parentElement, child);
        } else {
            syncPeer.renderAdd(rc, update, getContainerId(child), child);
        }
    }
    
    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#renderDispose(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, nextapp.echo2.app.Component)
     */
    public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component) { }

    /**
     * @see nextapp.echo2.webcontainer.DomUpdateSupport#renderHtml(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, org.w3c.dom.Node, nextapp.echo2.app.Component)
     */
    public void renderHtml(RenderContext rc, ServerComponentUpdate update, Node parentNode, Component component) {
        
//BUGBUG. Eliminate fully spanned over rows/columns.
//BUGBUG. Render in any direction.
//BUGBUG. Fill remaining cells.
        
        Grid grid = (Grid) component;
        Border border = (Border) grid.getRenderProperty(Grid.PROPERTY_BORDER);
        Extent borderSize = border == null ? null : border.getSize();
        
        String elementId = ContainerInstance.getElementId(grid);
        
        Document document = parentNode.getOwnerDocument();
        Element tableElement = document.createElement("table");
        tableElement.setAttribute("id", elementId);

        CssStyle tableCssStyle = new CssStyle();
        tableCssStyle.setAttribute("border-collapse", "collapse");
        
        Insets gridInsets = (Insets) grid.getRenderProperty(Grid.PROPERTY_INSETS);
        String defaultInsetsAttributeValue = gridInsets == null 
                ? "0px" : InsetsRender.renderCssAttributeValue(gridInsets);
        
        ColorRender.renderToStyle(tableCssStyle, component);
        FontRender.renderToStyle(tableCssStyle, component);
        BorderRender.renderToStyle(tableCssStyle, border);
        if (borderSize != null) {
            if (!rc.getContainerInstance().getClientProperties().getBoolean(
                    ClientProperties.QUIRK_CSS_BORDER_COLLAPSE_MARGIN)) {
                tableCssStyle.setAttribute("margin", ExtentRender.renderCssAttributeValueHalf(borderSize));
            }
        }
        tableElement.setAttribute("style", tableCssStyle.renderInline());
        
        parentNode.appendChild(tableElement);
        
        Element tbodyElement = document.createElement("tbody");
        tbodyElement.setAttribute("id", elementId + "_tbody");
        tableElement.appendChild(tbodyElement);
        
        GridProcessor gridProcessor = new GridProcessor(grid);

        int gridXSize = gridProcessor.getGridXSize();
        int gridYSize = gridProcessor.getGridYSize();
        Set renderedCells = new HashSet();
        
        for (int y = 0; y < gridYSize; ++y) {
            Element trElement = document.createElement("tr");
            trElement.setAttribute("id", elementId + "_tr_" + y);
            tbodyElement.appendChild(trElement);
            for (int x = 0; x < gridXSize; ++x) {
                Component cell = gridProcessor.getContent(x, y);
                if (cell == null) {
                    //BUGBUG. breaking out here is a TEMPORARY solution..this cause major breakage for stuff like RTL tables.
                    break;
                }
                if (renderedCells.contains(cell)) {
                    // Cell already rendered.
                    continue;
                }
                renderedCells.add(cell);
                
                Element tdElement = document.createElement("td");
                tdElement.setAttribute("id", elementId + "_td_" + ContainerInstance.getElementId(cell));
                trElement.appendChild(tdElement);

                int componentIndex = gridProcessor.getComponentIndex(x, y);
                if (gridProcessor.getXSpan(componentIndex) > 1) {
                    tdElement.setAttribute("colspan", Integer.toString(gridProcessor.getXSpan(componentIndex)));
                }
                
                if (gridProcessor.getYSpan(componentIndex) > 1) {
                    tdElement.setAttribute("rowspan", Integer.toString(gridProcessor.getYSpan(componentIndex)));
                }
            
                CssStyle tdCssStyle = new CssStyle();
                BorderRender.renderToStyle(tdCssStyle, (Border) grid.getRenderProperty(Grid.PROPERTY_BORDER));
                CellLayoutDataRender.renderToStyle(tdCssStyle, getLayoutData(cell), defaultInsetsAttributeValue);
                tdElement.setAttribute("style", tdCssStyle.renderInline());
                
                renderAddChild(rc, update, tdElement, cell);
            }
        }
    }
    
    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#renderUpdate(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String)
     */
    public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update, String targetId) {
        String parentId = ContainerInstance.getElementId(update.getParent());
        DomUpdate.createDomRemove(rc.getServerMessage(), parentId);
        renderAdd(rc, update, targetId, update.getParent());
        return true;
    }
}
