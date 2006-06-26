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
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.list.ListSelectionModel;
import nextapp.echo2.app.table.TableColumnModel;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ActionProcessor;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.DomUpdateSupport;
import nextapp.echo2.webcontainer.PartialUpdateManager;
import nextapp.echo2.webcontainer.PropertyUpdateProcessor;
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
import nextapp.echo2.webrender.util.DomUtil;

/**
 * Synchronization peer for <code>nextapp.echo2.app.Table</code> components.
 * <p>
 * This class should not be extended or used by classes outside of the
 * Echo framework.
 */
public class TablePeer 
implements ActionProcessor, ComponentSynchronizePeer, DomUpdateSupport, ImageRenderSupport, PropertyUpdateProcessor  {

    //TODO: Add full support for partial rendering on row insertions/deletions.

    private static final String[] TABLE_INIT_KEYS = new String[]{"rollover-style", "selection-style"};
    
    private static final String PROPERTY_SELECTION = "selection";
    
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
        return ContainerInstance.getElementId(child.getParent()) + "_cell_" + child.getRenderId();
    }
    
    /**
     * @see nextapp.echo2.webcontainer.image.ImageRenderSupport#getImage(nextapp.echo2.app.Component, java.lang.String)
     */
    public ImageReference getImage(Component component, String imageId) {
        if (IMAGE_ID_ROLLOVER_BACKGROUND.equals(imageId)) {
            FillImage backgroundImage 
                    = (FillImage) component.getRenderProperty(Table.PROPERTY_ROLLOVER_BACKGROUND_IMAGE);
            if (backgroundImage == null) {
                return null;
            } else {
                return backgroundImage.getImage();
            }
        } else if (IMAGE_ID_SELECTION_BACKGROUND.equals(imageId)) {
            FillImage backgroundImage 
                    = (FillImage) component.getRenderProperty(Table.PROPERTY_SELECTION_BACKGROUND_IMAGE);
            if (backgroundImage == null) {
                return null;
            } else {
                return backgroundImage.getImage();
            }
        } else {
            // Retrieve CellLayoutData background image if applicable.
            return CellLayoutDataRender.getCellLayoutDataBackgroundImage(component, imageId);
        }
    }

    /**
     * Returns the <code>TableLayoutData</code> of the given child,
     * or null if it does not provide layout data.
     *
     * @param child the child component
     * @return the layout data
     * @throws java.lang.RuntimeException if the the provided
     *         <code>LayoutData</code> is not a <code>TableLayoutData</code>
     */
    private TableLayoutData getLayoutData(Component child) {
        LayoutData layoutData = (LayoutData) child.getRenderProperty(Component.PROPERTY_LAYOUT_DATA);
        if (layoutData == null) {
            return null;
        } else if (layoutData instanceof TableLayoutData) {
            return (TableLayoutData) layoutData;
        } else {
            throw new RuntimeException("Invalid LayoutData for Table Child: " + layoutData.getClass().getName());
        }
    }
    
    /**
     * @see nextapp.echo2.webcontainer.ActionProcessor#processAction(nextapp.echo2.webcontainer.ContainerInstance, 
     *      nextapp.echo2.app.Component, org.w3c.dom.Element)
     */
    public void processAction(ContainerInstance ci, Component component, Element actionElement) {
        ci.getUpdateManager().getClientUpdateManager().setComponentAction(component, Table.INPUT_ACTION, null);
    }
    
    /**
     * @see nextapp.echo2.webcontainer.PropertyUpdateProcessor#processPropertyUpdate(
     *      nextapp.echo2.webcontainer.ContainerInstance, nextapp.echo2.app.Component, org.w3c.dom.Element)
     */
    public void processPropertyUpdate(ContainerInstance ci, Component component, Element propertyElement) {
        String propertyName = propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_NAME);
        if (PROPERTY_SELECTION.equals(propertyName)) {
            Element[] optionElements = DomUtil.getChildElementsByTagName(propertyElement, "row");
            int[] selectedIndices = new int[optionElements.length];
            for (int i = 0; i < optionElements.length; ++i) {
                selectedIndices[i] = Integer.parseInt(optionElements[i].getAttribute("index"));
            }
            ci.getUpdateManager().getClientUpdateManager().setComponentProperty(component, 
                    Table.SELECTION_CHANGED_PROPERTY, selectedIndices);
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
     * Renders a child component.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update
     * @param parentElement the HTML element which should contain the child
     * @param child the child component to render
     */
    private void renderAddChild(RenderContext rc, ServerComponentUpdate update, Element parentElement, Component child) {
        if (!child.isVisible()) {
            // Do nothing.
            return;
        }
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
        rc.getServerMessage().addLibrary(TABLE_SERVICE.getId());
        renderDisposeDirective(rc, (Table) component);
    }
    
    /**
     * Renders a directive to the outgoing <code>ServerMessage</code> to 
     * dispose the state of a table, performing tasks such as unregistering
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
        serverMessage.addLibrary(TABLE_SERVICE.getId());
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
        
        if (((Boolean) table.getRenderProperty(Table.PROPERTY_SELECTION_ENABLED, Boolean.FALSE)).booleanValue()) {
            tableCssStyle.setAttribute("cursor", "pointer");
        }
        
        Insets tableInsets = (Insets) table.getRenderProperty(Table.PROPERTY_INSETS);
        String defaultInsetsAttributeValue = tableInsets == null ? "0px" : InsetsRender.renderCssAttributeValue(tableInsets);
        
        ColorRender.renderToStyle(tableCssStyle, component);
        FontRender.renderToStyle(tableCssStyle, component);
        BorderRender.renderToStyle(tableCssStyle, border);
        if (borderSize != null) {
            if (!rc.getContainerInstance().getClientProperties().getBoolean(
                    ClientProperties.QUIRK_CSS_BORDER_COLLAPSE_INSIDE)) {
                tableCssStyle.setAttribute("margin", ExtentRender.renderCssAttributeValueHalf(borderSize));
            }
        }
        
        Extent width = (Extent) table.getRenderProperty(Table.PROPERTY_WIDTH);
        boolean enable100PercentWidthWorkaround = false;
        if (rc.getContainerInstance().getClientProperties().getBoolean(
                ClientProperties.QUIRK_IE_TABLE_PERCENT_WIDTH_SCROLLBAR_ERROR)) {
            if (width != null && width.getUnits() == Extent.PERCENT && width.getValue() == 100) {
                width = null;
                enable100PercentWidthWorkaround = true;
            }
        }
        ExtentRender.renderToStyle(tableCssStyle, "width", width);

        tableElement.setAttribute("style", tableCssStyle.renderInline());
        
        parentNode.appendChild(tableElement);
        
        TableColumnModel columnModel = table.getColumnModel();
        int columnCount = columnModel.getColumnCount();
        
        boolean someColumnsHaveWidths = false;
        for (int i = 0; i < columnCount; ++i) {
            if (columnModel.getColumn(i).getWidth() != null) {
                someColumnsHaveWidths = true;
            }
        }
        if (someColumnsHaveWidths || enable100PercentWidthWorkaround) {
            Element colGroupElement = document.createElement("colgroup");
            if (enable100PercentWidthWorkaround) {
                int screenWidth = rc.getContainerInstance().getClientProperties().getInt(ClientProperties.SCREEN_WIDTH, 1024);
                colGroupElement.setAttribute("width", screenWidth + "px");
            }
            tableElement.appendChild(colGroupElement);
            
            boolean allColumnsHaveWidths = true;
            for (int i = 0; i < columnCount; ++i) {
                Element colElement = document.createElement("col");
                if (enable100PercentWidthWorkaround && allColumnsHaveWidths && i == columnCount - 1) {
                    // Special case: Don't add widths for ALL columns in cases where IE 100-percent Table
                    // workaround is in use.  (Do nothing)
                } else {
                    Extent columnWidth = columnModel.getColumn(i).getWidth();
                    if (columnWidth == null) {
                        allColumnsHaveWidths = false;
                    } else {
                        colElement.setAttribute("width", ExtentRender.renderCssAttributeValue(columnWidth));
                    }
                }
                colGroupElement.appendChild(colElement);
            }
        }
        
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
        Document document = serverMessage.getDocument();
        
        boolean rolloverEnabled = ((Boolean) table.getRenderProperty(Table.PROPERTY_ROLLOVER_ENABLED, 
                Boolean.FALSE)).booleanValue();
        boolean selectionEnabled = ((Boolean) table.getRenderProperty(Table.PROPERTY_SELECTION_ENABLED, 
                Boolean.FALSE)).booleanValue();
        
        String rolloverStyle = "";
        if (rolloverEnabled) {
            CssStyle rolloverCssStyle = new CssStyle();
            ColorRender.renderToStyle(rolloverCssStyle, 
                    (Color) table.getRenderProperty(Table.PROPERTY_ROLLOVER_FOREGROUND),
                    (Color) table.getRenderProperty(Table.PROPERTY_ROLLOVER_BACKGROUND));
            FontRender.renderToStyle(rolloverCssStyle, 
                    (Font) table.getRenderProperty(Table.PROPERTY_ROLLOVER_FONT));
            FillImageRender.renderToStyle(rolloverCssStyle, rc, this, table, IMAGE_ID_ROLLOVER_BACKGROUND,
                    (FillImage) table.getRenderProperty(Table.PROPERTY_ROLLOVER_BACKGROUND_IMAGE),
                    FillImageRender.FLAG_DISABLE_FIXED_MODE);
            if (rolloverCssStyle.hasAttributes()) {
                rolloverStyle = rolloverCssStyle.renderInline();
            }
        }
        
        String selectionStyle = "";
        if (selectionEnabled) {
            CssStyle selectionCssStyle = new CssStyle();
            ColorRender.renderToStyle(selectionCssStyle, 
                    (Color) table.getRenderProperty(Table.PROPERTY_SELECTION_FOREGROUND),
                    (Color) table.getRenderProperty(Table.PROPERTY_SELECTION_BACKGROUND));
            FontRender.renderToStyle(selectionCssStyle, 
                    (Font) table.getRenderProperty(Table.PROPERTY_SELECTION_FONT));
            FillImageRender.renderToStyle(selectionCssStyle, rc, this, table, IMAGE_ID_SELECTION_BACKGROUND,
                    (FillImage) table.getRenderProperty(Table.PROPERTY_SELECTION_BACKGROUND_IMAGE), 
                    FillImageRender.FLAG_DISABLE_FIXED_MODE);
            if (selectionCssStyle.hasAttributes()) {
                selectionStyle = selectionCssStyle.renderInline();
            }
        }
        
        Element itemizedUpdateElement = serverMessage.getItemizedDirective(ServerMessage.GROUP_ID_POSTUPDATE,
                "EchoTable.MessageProcessor", "init",  TABLE_INIT_KEYS, 
                new String[]{rolloverStyle, selectionStyle});
        Element itemElement = document.createElement("item");
        itemElement.setAttribute("eid", elementId);
        if (table.isHeaderVisible()) {
            itemElement.setAttribute("header-visible", "true");
        }
        
        if (table.hasActionListeners()) {
            itemElement.setAttribute("server-notify", "true");
        }
        
        if (rolloverEnabled) {
            itemElement.setAttribute("rollover-enabled", "true");
        }
        
        if (selectionEnabled) {
            itemElement.setAttribute("selection-enabled", "true");
            ListSelectionModel selectionModel = table.getSelectionModel();
            if (selectionModel.getSelectionMode() == ListSelectionModel.MULTIPLE_SELECTION) {
                itemElement.setAttribute("selection-mode", "multiple");
            }
            if (selectionModel.getMinSelectedIndex() != -1) {
                Element selectionElement = document.createElement("selection");
                int minimumIndex = selectionModel.getMinSelectedIndex();
                int maximumIndex = selectionModel.getMaxSelectedIndex();
                if (maximumIndex > table.getModel().getRowCount() - 1) {
                    maximumIndex = table.getModel().getRowCount() - 1;
                }
                for (int i = minimumIndex; i <= maximumIndex; ++i) {
                    if (selectionModel.isSelectedIndex(i)) {
                        Element rowElement = document.createElement("row");
                        rowElement.setAttribute("index", Integer.toString(i));
                        selectionElement.appendChild(rowElement);
                    }
                }
                itemElement.appendChild(selectionElement);
            }
        }
        
        if (!table.isRenderEnabled()) {
            itemElement.setAttribute("enabled", "false");
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
        if (rowIndex == Table.HEADER_ROW) {
            trElement.setAttribute("id", elementId + "_tr_header");
        } else {
            trElement.setAttribute("id", elementId + "_tr_" + rowIndex); 
        }
        tbodyElement.appendChild(trElement);
        
        int columns = table.getColumnModel().getColumnCount();
        for (int columnIndex = 0; columnIndex < columns; ++columnIndex) {
            Component childComponent = table.getCellComponent(columnIndex, rowIndex);
            Element tdElement = document.createElement("td");
            tdElement.setAttribute("id", elementId + "_cell_" + childComponent.getRenderId());
            
            CssStyle tdCssStyle = new CssStyle();
            BorderRender.renderToStyle(tdCssStyle, (Border) table.getRenderProperty(Table.PROPERTY_BORDER));
            CellLayoutDataRender.renderToElementAndStyle(tdElement, tdCssStyle, childComponent, getLayoutData(childComponent), 
                    defaultInsetsAttributeValue);
            CellLayoutDataRender.renderBackgroundImageToStyle(tdCssStyle, rc, this, table, childComponent);
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
        Table table = (Table) update.getParent();
        renderDisposeDirective(rc, table);
        DomUpdate.renderElementRemove(rc.getServerMessage(), ContainerInstance.getElementId(table));
        renderAdd(rc, update, targetId, table);
        return true;
    }
}
