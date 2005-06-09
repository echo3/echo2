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
import nextapp.echo2.app.FillImage;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.LayoutData;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.layout.TableCellLayoutData;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.DomUpdateSupport;
import nextapp.echo2.webcontainer.PartialUpdateManager;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.ComponentSynchronizePeer;
import nextapp.echo2.webcontainer.SynchronizePeerFactory;
import nextapp.echo2.webcontainer.image.ImageRenderSupport;
import nextapp.echo2.webcontainer.propertyrender.BorderRender;
import nextapp.echo2.webcontainer.propertyrender.CellLayoutDataRender;
import nextapp.echo2.webcontainer.propertyrender.ColorRender;
import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
import nextapp.echo2.webcontainer.propertyrender.FillImageRender;
import nextapp.echo2.webcontainer.propertyrender.FontRender;
import nextapp.echo2.webcontainer.propertyrender.InsetsRender;
import nextapp.echo2.webrender.ClientProperties;
import nextapp.echo2.webrender.ServerMessage;
import nextapp.echo2.webrender.Service;
import nextapp.echo2.webrender.WebRenderServlet;
import nextapp.echo2.webrender.output.CssStyle;
import nextapp.echo2.webrender.servermessage.DomUpdate;
import nextapp.echo2.webrender.service.JavaScriptService;

//BUGBUG. sort component visiblity with regard to rendered components
// (may simply want to require returned components be visible (at Table level) and validate this)

/**
 * Synchronization peer for <code>nextapp.echo2.app.Table</code> components.
 * <p>
 * This class should not be extended or used by classes outside of the
 * Echo framework.
 */
public class TablePeer 
implements DomUpdateSupport, ImageRenderSupport, ComponentSynchronizePeer {

    private static final String[] TABLE_INIT_KEYS = new String[]{"defaultstyle", "rolloverstyle", "selectionstyle"};
    
    private static final String IMAGE_ID_ROLLOVER_BACKGROUND = "rolloverBackground";
    private static final String IMAGE_ID_SELECTION_BACKGROUND = "selectionBackground";
 
    /**
     * Service to provide supporting JavaScript library.
     */
    private static final Service TABLE_SERVICE = JavaScriptService.forResource("Echo.Table", 
            "/nextapp/echo2/webcontainer/resource/js/Table.js");

    static {
        WebRenderServlet.getServiceRegistry().add(TABLE_SERVICE);
    }

    protected PartialUpdateManager propertyRenderRegistry;
    
    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#getContainerId(nextapp.echo2.app.Component)
     */
    public String getContainerId(Component child) {
        return ContainerInstance.getElementId(child.getParent()) + "_cell_" + child.getParent().indexOf(child);
    }
    
    /**
     * @see nextapp.echo2.webcontainer.image.ImageRenderSupport#getImage(nextapp.echo2.app.Component, java.lang.String)
     */
    public ImageReference getImage(Component component, String imageId) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Returns the <code>TableCellLayoutData</code> of the given child,
     * or null if it does not provide <code>TableCellLayoutData</code>.
     * 
     * @param child the child component
     * @return the layout data
     */
    private TableCellLayoutData getLayoutData(Component child) {
        LayoutData layoutData = (LayoutData) child.getRenderProperty(Table.PROPERTY_LAYOUT_DATA);
        if (layoutData instanceof TableCellLayoutData) {
            return (TableCellLayoutData) layoutData;
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
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderDispose(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, nextapp.echo2.app.Component)
     */
    public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component) {
        renderDisposeDirective(rc, (Table) component);
    }
    
    /**
     * Renders a directive to the outgoing <code>ServerMessage</code> to 
     * dispose the state of a table, performing tasks such as deregistering
     * event listeners on the client.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param table the table
     */
    private void renderDisposeDirective(RenderContext rc, Table table) {
        ServerMessage serverMessage = rc.getServerMessage();
        Element itemizedUpdateElement = serverMessage.getItemizedDirective(ServerMessage.GROUP_ID_PREREMOVE,
                "EchoTable.MessageProcessor", "dispose",  new String[0], new String[0]);
        Element itemElement = serverMessage.getDocument().createElement("item");
        itemElement.setAttribute("eid", ContainerInstance.getElementId(table));
        itemizedUpdateElement.appendChild(itemElement);
    }

    /**
     * @see nextapp.echo2.webcontainer.DomUpdateSupport#renderHtml(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, org.w3c.dom.Node, nextapp.echo2.app.Component)
     */
    public void renderHtml(RenderContext rc, ServerComponentUpdate update, Node parentNode, Component component) {
        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(TABLE_SERVICE.getId(), true);
        Table table = (Table) component;
        
        renderInitDirective(rc, table);
        
        Border border = (Border) table.getRenderProperty(Table.PROPERTY_BORDER);
        Extent borderSize = border == null ? null : border.getSize();

        String elementId = ContainerInstance.getElementId(table);
        
        Document document = parentNode.getOwnerDocument();
        Element tableElement = document.createElement("table");
        tableElement.setAttribute("id", elementId);

        CssStyle tableCssStyle = new CssStyle();
        tableCssStyle.setAttribute("border-collapse", "collapse");
        
        Insets tableInsets = (Insets) table.getRenderProperty(Table.PROPERTY_INSETS);
        String defaultInsetsAttributeValue = tableInsets == null ? "0px" : InsetsRender.renderCssAttributeValue(tableInsets);
        
        ColorRender.renderToStyle(tableCssStyle, component);
        FontRender.renderToStyle(tableCssStyle, component);
        BorderRender.renderToStyle(tableCssStyle, border);
        if (borderSize != null) {
            if (!rc.getContainerInstance().getClientProperties().getBoolean(
                    ClientProperties.QUIRK_CSS_BORDER_COLLAPSE_MARGIN)) {
                tableCssStyle.setAttribute("margin", ExtentRender.renderCssAttributeValueHalf(borderSize));
            }
        }
        ExtentRender.renderToStyle(tableCssStyle, "width", (Extent) table.getRenderProperty(Table.PROPERTY_WIDTH));
        tableElement.setAttribute("style", tableCssStyle.renderInline());
        
        parentNode.appendChild(tableElement);
        
        Element tbodyElement = document.createElement("tbody");
        tbodyElement.setAttribute("id", elementId + "_tbody");
        tableElement.appendChild(tbodyElement);

        if (table.isHeaderVisible()) {
            renderRow(rc, update, tbodyElement, table, Table.HEADER_ROW, defaultInsetsAttributeValue);
        }
        
        int rows = table.getModel().getRowCount();
        for (int rowIndex = 0; rowIndex < rows; ++rowIndex) {
            renderRow(rc, update, tbodyElement, table, rowIndex, defaultInsetsAttributeValue);
        }
    }
    
    /**
     * Renders a directive to the outgoing <code>ServerMessage</code> to 
     * initialize the state of a <code>Table</code>, performing tasks such as 
     * registering event listeners on the client.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param table the table
     */
    private void renderInitDirective(RenderContext rc, Table table) {
        String elementId = ContainerInstance.getElementId(table);
        ServerMessage serverMessage = rc.getServerMessage();
        
        boolean rolloverEnabled = ((Boolean) table.getRenderProperty(Table.PROPERTY_ROLLOVER_ENABLED, 
                Boolean.FALSE)).booleanValue();
        boolean selectionEnabled = ((Boolean) table.getRenderProperty(Table.PROPERTY_SELECTION_ENABLED, 
                Boolean.FALSE)).booleanValue();
        
        String selectionStyle = "";
        String defaultStyle = "";
        String rolloverStyle = "";

        if (rolloverEnabled || selectionEnabled) {
            CssStyle defaultCssStyle = new CssStyle();
            ColorRender.renderToStyle(defaultCssStyle, (Color) table.getRenderProperty(Table.PROPERTY_FOREGROUND), 
                    (Color) table.getRenderProperty(Table.PROPERTY_BACKGROUND), true);
            FontRender.renderToStyle(defaultCssStyle, (Font) table.getRenderProperty(Table.PROPERTY_FONT));
            defaultStyle = defaultCssStyle.renderInline();
            
            if (rolloverEnabled) {
                CssStyle rolloverCssStyle = new CssStyle();
                ColorRender.renderToStyle(rolloverCssStyle, 
                        (Color) table.getRenderProperty(Table.PROPERTY_ROLLOVER_FOREGROUND),
                        (Color) table.getRenderProperty(Table.PROPERTY_ROLLOVER_BACKGROUND));
                FontRender.renderToStyle(rolloverCssStyle, 
                        (Font) table.getRenderProperty(Table.PROPERTY_ROLLOVER_FONT));
                FillImageRender.renderToStyle(rolloverCssStyle, rc, this, table, IMAGE_ID_ROLLOVER_BACKGROUND,
                        (FillImage) table.getRenderProperty(Table.PROPERTY_ROLLOVER_BACKGROUND_IMAGE), true);
                if (rolloverCssStyle.hasAttributes()) {
                    rolloverStyle = rolloverCssStyle.renderInline();
                }
            }
            
            if (selectionEnabled) {
                CssStyle selectionCssStyle = new CssStyle();
                ColorRender.renderToStyle(selectionCssStyle, 
                        (Color) table.getRenderProperty(Table.PROPERTY_SELECTION_FOREGROUND),
                        (Color) table.getRenderProperty(Table.PROPERTY_SELECTION_BACKGROUND));
                FontRender.renderToStyle(selectionCssStyle, 
                        (Font) table.getRenderProperty(Table.PROPERTY_SELECTION_FONT));
                FillImageRender.renderToStyle(selectionCssStyle, rc, this, table, IMAGE_ID_SELECTION_BACKGROUND,
                        (FillImage) table.getRenderProperty(Table.PROPERTY_SELECTION_BACKGROUND_IMAGE), true);
                if (selectionCssStyle.hasAttributes()) {
                    selectionStyle = selectionCssStyle.renderInline();
                }
            }
        }
        
        Element itemizedUpdateElement = serverMessage.getItemizedDirective(ServerMessage.GROUP_ID_POSTUPDATE,
                "EchoTable.MessageProcessor", "init",  TABLE_INIT_KEYS, 
                new String[]{defaultStyle, rolloverStyle, selectionStyle});
        Element itemElement = serverMessage.getDocument().createElement("item");
        itemElement.setAttribute("eid", elementId);
        if (table.hasActionListeners()) {
            itemElement.setAttribute("servernotify", "true");
        }

        itemizedUpdateElement.appendChild(itemElement);
    }
    
    /**
     * Renders a single row of a table.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the <code>ServerComponentUpdate</code> being processed
     * @param tbodyElement the <code>tbody</code> element to which to append 
     *        the rendered content
     * @param table the <code>Table</code> being rendered
     * @param rowIndex the row to render
     * @param defaultInsetsAttributeValue the default CSS padding attribute value
     */
    private void renderRow(RenderContext rc, ServerComponentUpdate update, Element tbodyElement, Table table, int rowIndex,
            String defaultInsetsAttributeValue) {
        Document document = tbodyElement.getOwnerDocument();
        String elementId = ContainerInstance.getElementId(table);
        
        Element trElement = document.createElement("tr");
        trElement.setAttribute("id", elementId + "_tr_" + rowIndex); // BUGBUG. may not be suitable id (changing row index)
        tbodyElement.appendChild(trElement);
        
        int columns = table.getColumnModel().getColumnCount();
        for (int columnIndex = 0; columnIndex < columns; ++columnIndex) {
            Component childComponent = table.getCellComponent(columnIndex, rowIndex);
            Element tdElement = document.createElement("td");
            tdElement.setAttribute("id", elementId + "_cell_" + childComponent.getRenderId());
            
            CssStyle tdCssStyle = new CssStyle();
            BorderRender.renderToStyle(tdCssStyle, (Border) table.getRenderProperty(Table.PROPERTY_BORDER));
            CellLayoutDataRender.renderToStyle(tdCssStyle, getLayoutData(childComponent), defaultInsetsAttributeValue);
            tdElement.setAttribute("style", tdCssStyle.renderInline());
            
            trElement.appendChild(tdElement);
            renderAddChild(rc, update, tdElement, childComponent);
        }
    }
    
    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderUpdate(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String)
     */
    public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update, String targetId) {
        DomUpdate.renderElementRemove(rc.getServerMessage(), ContainerInstance.getElementId(update.getParent()));
        renderAdd(rc, update, targetId, update.getParent());
        return true;
    }
}
