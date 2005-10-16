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

import nextapp.echo2.app.Border;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.list.AbstractListComponent;
import nextapp.echo2.app.list.ListCellRenderer;
import nextapp.echo2.app.list.ListModel;
import nextapp.echo2.app.list.ListSelectionModel;
import nextapp.echo2.app.list.StyledListCell;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ActionProcessor;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.DomUpdateSupport;
import nextapp.echo2.webcontainer.PropertyUpdateProcessor;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.ComponentSynchronizePeer;
import nextapp.echo2.webcontainer.propertyrender.BorderRender;
import nextapp.echo2.webcontainer.propertyrender.ColorRender;
import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
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

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Abstract synchronization peer for the built-in
 * <code>nextapp.echo2.app.AbstractListComponent</code>-derived components.
 * <p>
 * This class should not be extended or used by classes outside of the
 * Echo framework.
 */
public abstract class AbstractListComponentPeer 
implements ActionProcessor, DomUpdateSupport, PropertyUpdateProcessor, ComponentSynchronizePeer {

    private static final String PROPERTY_SELECTION = "selection";
    
    // Default Colors
    static final Color DEFAULT_BACKGROUND = Color.WHITE;
    static final Color DEFAULT_FOREGROUND = Color.BLACK;

    // Default Sizes
    protected static final Extent DEFAULT_WIDTH = new Extent(100, Extent.PERCENT);
    protected static final Insets DEFAULT_INSETS = new Insets(new Extent(0), new Extent(0));

    /**
     * Service to provide supporting JavaScript library.
     */
    private static final Service LIST_COMPONENT_SERVICE = JavaScriptService.forResource("Echo.ListComponent",
            "/nextapp/echo2/webcontainer/resource/js/ListComponent.js");

    static {
        WebRenderServlet.getServiceRegistry().add(LIST_COMPONENT_SERVICE);
    }

    /**
     * Appends the base style to the given style based off of properties on the
     * given <code>nextapp.echo2.app.AbstractListComponent</code>
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param listComponent the <code>nextapp.echo2.app.AbstractListComponent</code>
     */
    private CssStyle createListComponentCssStyle(RenderContext rc, AbstractListComponent listComponent) {
        CssStyle style = new CssStyle();

        Extent width = (Extent) listComponent.getRenderProperty(AbstractListComponent.PROPERTY_WIDTH, DEFAULT_WIDTH);
        if (rc.getContainerInstance().getClientProperties().getBoolean(ClientProperties.QUIRK_IE_SELECT_PERCENT_WIDTH)
                && width.getUnits() == Extent.PERCENT) {
            // Render default width. 
            width = null;
        }
        
        Extent height = (Extent) listComponent.getRenderProperty(AbstractListComponent.PROPERTY_HEIGHT);
        Insets insets = (Insets) listComponent.getRenderProperty(AbstractListComponent.PROPERTY_INSETS, DEFAULT_INSETS);

        BorderRender.renderToStyle(style, (Border) listComponent.getRenderProperty(AbstractListComponent.PROPERTY_BORDER));
        FontRender.renderToStyle(style, (Font) listComponent.getRenderProperty(AbstractListComponent.PROPERTY_FONT));
        InsetsRender.renderToStyle(style, "padding", insets);
        ColorRender.renderToStyle(style, 
                (Color) listComponent.getRenderProperty(AbstractListComponent.PROPERTY_FOREGROUND, DEFAULT_FOREGROUND),
                (Color) listComponent.getRenderProperty(AbstractListComponent.PROPERTY_BACKGROUND, DEFAULT_BACKGROUND));
        ExtentRender.renderToStyle(style, "width", width);
        ExtentRender.renderToStyle(style, "height", height);
        style.setAttribute("position", "relative");

        return style;
    }

    /**
     * Creates the rollover style based off of properties on the given
     * <code>nextapp.echo2.app.AbstractListComponent</code>
     * 
     * @param listComponent the <code>AbstractListComponent</code> instance
     * @return the style
     */
    CssStyle createRolloverCssStyle(AbstractListComponent listComponent) {
        CssStyle style = new CssStyle();
        Color rolloverForeground = (Color) listComponent.getRenderProperty(AbstractListComponent.PROPERTY_ROLLOVER_FOREGROUND);
        Color rolloverBackground = (Color) listComponent.getRenderProperty(AbstractListComponent.PROPERTY_ROLLOVER_BACKGROUND);
        ColorRender.renderToStyle(style, rolloverForeground, rolloverBackground);
        FontRender.renderToStyle(style, (Font) listComponent.getRenderProperty(AbstractListComponent.PROPERTY_ROLLOVER_FONT));
        return style;
    }

    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#getContainerId(nextapp.echo2.app.Component)
     */
    public String getContainerId(Component child) {
        throw new UnsupportedOperationException("Component does not support children.");
    }

    /**
     * Determines the HTML element id of the rendered OPTION element with the 
     * specified index.
     * 
     * @param elementId the element id of the root element of the rendered 
     *        SELECT object.
     */
    protected String getOptionId(String elementId, int index) {
        return elementId + "_" + index;
    }
    
    /**
     * @see nextapp.echo2.webcontainer.ActionProcessor#processAction(nextapp.echo2.webcontainer.ContainerInstance, 
     *      nextapp.echo2.app.Component, org.w3c.dom.Element)
     */
    public void processAction(ContainerInstance ci, Component component, Element actionElement) {
        ci.getUpdateManager().getClientUpdateManager().setComponentAction(component, AbstractListComponent.INPUT_ACTION, null);
    }

    /**
     * @see nextapp.echo2.webcontainer.PropertyUpdateProcessor#processPropertyUpdate(
     *      nextapp.echo2.webcontainer.ContainerInstance, nextapp.echo2.app.Component, org.w3c.dom.Element)
     */
    public void processPropertyUpdate(ContainerInstance ci, Component component, Element propertyElement) {
        String propertyName = propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_NAME);
        if (PROPERTY_SELECTION.equals(propertyName)) {
            Element[] itemElements = DomUtil.getChildElementsByTagName(propertyElement, "item");
            int[] selectedIndices = new int[itemElements.length];
            for (int i = 0; i < itemElements.length; ++i) {
                String id = itemElements[i].getAttribute("id");
                int selectedIndex = Integer.parseInt(id.substring(id.lastIndexOf("_") + 1));
                selectedIndices[i] = selectedIndex;
            }
            ci.getUpdateManager().getClientUpdateManager().setComponentProperty(component, 
                    AbstractListComponent.SELECTION_CHANGED_PROPERTY, selectedIndices);
        }
    }

    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderAdd(nextapp.echo2.webcontainer.RenderContext,
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String,
     *      nextapp.echo2.app.Component)
     */
    public void renderAdd(RenderContext rc, ServerComponentUpdate update, String targetId, Component component) {
        DocumentFragment htmlFragment = rc.getServerMessage().getDocument().createDocumentFragment();
        renderHtml(rc, update, htmlFragment, component);
        DomUpdate.renderElementAdd(rc.getServerMessage(), targetId, htmlFragment);
    }

    /**
     * Renders a directive to the outgoing <code>ServerMessage</code> to 
     * dispose the state of a list component, performing tasks such as 
     * unregistering event listeners on the client.
     * 
     * @param serverMessage the <code>serverMessage</code>
     * @param listComponent the list component
     */
    private void renderDisposeDirective(ServerMessage serverMessage, AbstractListComponent listComponent) {
        Element itemizedUpdateElement = serverMessage.getItemizedDirective(ServerMessage.GROUP_ID_PREREMOVE,
                "EchoListComponent.MessageProcessor", "dispose",  new String[0], new String[0]);
        Element itemElement = serverMessage.getDocument().createElement("item");
        itemElement.setAttribute("eid", ContainerInstance.getElementId(listComponent));
        itemizedUpdateElement.appendChild(itemElement);
    }

    /**
     * Renders a directive to the outgoing <code>ServerMessage</code> to 
     * initialize the state of a list component, performing tasks such as 
     * registering event listeners on the client.
     * 
     * @param serverMessage the <code>serverMessage</code>
     * @param listComponent the list component
     */
    private void renderInitDirective(ServerMessage serverMessage, AbstractListComponent listComponent) {
        Element itemizedUpdateElement = serverMessage.getItemizedDirective(ServerMessage.GROUP_ID_POSTUPDATE,
                "EchoListComponent.MessageProcessor", "init",  new String[0], new String[0]);
        Element itemElement = serverMessage.getDocument().createElement("item");
        itemElement.setAttribute("eid", ContainerInstance.getElementId(listComponent));
        if (!listComponent.isEnabled()) {
            itemElement.setAttribute("enabled", "false");
        }
        if (listComponent.hasActionListeners()) {
            itemElement.setAttribute("server-notify", "true");
        }
        
        Boolean rolloverEnabled = (Boolean) listComponent.getRenderProperty(AbstractListComponent.PROPERTY_ROLLOVER_ENABLED);
        if (Boolean.TRUE.equals(rolloverEnabled)) {
            CssStyle rolloverCssStyle = createRolloverCssStyle(listComponent);
            itemElement.setAttribute("rollover-style", rolloverCssStyle.renderInline());
        }
        itemizedUpdateElement.appendChild(itemElement);
    }

    /**
     * Renders CSS style information to an item element.
     * This operation is only performed in the event the 
     * <code>renderedValue</code> implements the <code>StyledListCell</code>
     * interface.
     * 
     * @param itemElement the item HTML element upon which to configure the 
     *        style
     * @param renderedValue the rendered model that corresponds to the specified
     *        item element
     */
    void renderItemStyle(Element itemElement, Object renderedValue) {
        if (!(renderedValue instanceof StyledListCell)) {
            return;
        }
        CssStyle itemStyle = new CssStyle();
        StyledListCell styledListCell = (StyledListCell) renderedValue;
        ColorRender.renderToStyle(itemStyle, styledListCell.getForeground(), styledListCell.getBackground());
        FontRender.renderToStyle(itemStyle, styledListCell.getFont());
        if (itemStyle.hasAttributes()) {
            itemElement.setAttribute("style", itemStyle.renderInline());
        }
    }

    /**
     * Renders disposal code for a standard SELECT-based control.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update
     * @param component the <code>AbstractListComponent</code> being disposed
     */
    protected void renderSelectElementDispose(RenderContext rc, ServerComponentUpdate update, Component component) {
        rc.getServerMessage().addLibrary(LIST_COMPONENT_SERVICE.getId());
        renderDisposeDirective(rc.getServerMessage(), (AbstractListComponent) component);
    }
    
    /**
     * Renders the select control.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update
     * @param parentNode the parent node to which HTML elements should be 
     *        appended
     * @param component the <code>nextapp.echo2.app.AbstractListComponent</code>
     *        instance
     * @param renderAsListBox a flag indicating whether the component should
     *        be rendered as a list box (true) or as drop-down select (false)
     * @param multiple a flag indicating whether multiple items may be selected
     *        at the same time (valid only for list box rendering)
     */
    void renderSelectElementHtml(RenderContext rc, ServerComponentUpdate update, Node parentNode, Component component, 
            boolean renderAsListBox, boolean multiple) {
        renderInitDirective(rc.getServerMessage(), (AbstractListComponent) component);

        AbstractListComponent listComponent = (AbstractListComponent) component;
        String elementId = ContainerInstance.getElementId(component);

        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(LIST_COMPONENT_SERVICE.getId());

        Element listComponentElement = parentNode.getOwnerDocument().createElement(
                "select");
        listComponentElement.setAttribute("id", elementId + "_select");
        listComponentElement.setAttribute("name", elementId + "_select");
        listComponentElement.setAttribute("size", renderAsListBox ? "5" : "1");

        if (multiple) {
            listComponentElement.setAttribute("multiple", "multiple");
        }
        
        if (listComponent.isFocusTraversalParticipant()) {
            listComponentElement.setAttribute("tabindex", Integer.toString(listComponent.getFocusTraversalIndex()));
        } else {
            listComponentElement.setAttribute("tabindex", "-1");
        }

        String toolTipText = (String) listComponent.getRenderProperty(AbstractListComponent.PROPERTY_TOOL_TIP_TEXT);
        if (toolTipText != null) {
            listComponentElement.setAttribute("title", toolTipText);
        }
        
        ListModel model = listComponent.getModel();
        ListSelectionModel selectionModel = listComponent.getSelectionModel();
        ListCellRenderer renderer = listComponent.getCellRenderer();

        for (int i = 0; i < model.size(); i++) {
            boolean selected = selectionModel.isSelectedIndex(i);
            
            Element optionElement = parentNode.getOwnerDocument().createElement("option");
            String optionId = getOptionId(elementId, i);
            optionElement.setAttribute("id", optionId);
            optionElement.setAttribute("value", optionId);

            Object value = model.get(i);
            Object renderedValue = renderer.getListCellRendererComponent(listComponent, value, i);
            optionElement.appendChild(optionElement.getOwnerDocument().createTextNode(renderedValue.toString()));

            if (selected) {
                optionElement.setAttribute("selected", "true");
            }
            
            renderItemStyle(optionElement, renderedValue);

            listComponentElement.appendChild(optionElement);
        }

        CssStyle cssStyle = createListComponentCssStyle(rc, listComponent);
        listComponentElement.setAttribute("style", cssStyle.renderInline());

        Element containingDiv = parentNode.getOwnerDocument().createElement("div");
        containingDiv.setAttribute("id", elementId);
        containingDiv.appendChild(listComponentElement);

        parentNode.appendChild(containingDiv);
    }
    
    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderUpdate(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String)
     */
    public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update, String targetId) {
        DomUpdate.renderElementRemove(rc.getServerMessage(), ContainerInstance.getElementId(update.getParent()));
        renderAdd(rc, update, targetId, update.getParent());
        return false;
    }
}