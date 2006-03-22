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
import nextapp.echo2.app.ListBox;
import nextapp.echo2.app.list.ListCellRenderer;
import nextapp.echo2.app.list.ListModel;
import nextapp.echo2.app.list.ListSelectionModel;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.RenderContext;
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
import nextapp.echo2.webrender.service.JavaScriptService;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Synchronization peer for <code>nextapp.echo2.app.ListBox</code> components.
 * <p>
 * This class should not be extended or used by classes outside of the
 * Echo framework.
 */
public class ListBoxPeer extends AbstractListComponentPeer {
    
    private static final boolean FORCE_DHTML_RENDERING = false;
    
    private static final Border DEFAULT_DHTML_BORDER = new Border(2, null, Border.STYLE_INSET);
    private static final Extent DEFAULT_HEIGHT = new Extent(80);
    
    private static final Color SELECTION_BACKGROUND = new Color(10, 36, 106);
    private static final Color SELECTION_FOREGROUND = Color.WHITE;
    private static final String SELECTION_CSS_STYLE_TEXT;
    static {
        CssStyle style = new CssStyle();
        ColorRender.renderToStyle(style, SELECTION_FOREGROUND, SELECTION_BACKGROUND);
        SELECTION_CSS_STYLE_TEXT = style.renderInline();
    }

    /**
     * Service to provide supporting JavaScript library.
     */
    private static final Service LIST_COMPONENT_DHTML_SERVICE = JavaScriptService.forResource("Echo.ListComponentDhtml",
            "/nextapp/echo2/webcontainer/resource/js/ListComponentDhtml.js");

    static {
        WebRenderServlet.getServiceRegistry().add(LIST_COMPONENT_DHTML_SERVICE);
    }

    /**
     * Creates the style for the outer div of the DHTML rendering based off of
     * properties on the given <code>ListBox</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param listBox the <code>ListBox</code> instance
     * @return the style
     */
    private CssStyle createDhtmlListBoxCssStyle(RenderContext rc, ListBox listBox) {
        CssStyle cssStyle = new CssStyle();
        
        boolean renderEnabled = listBox.isRenderEnabled();

        Border border;
        Color foreground, background;
        Font font;
        if (!renderEnabled) {
            // Retrieve disabled style information.
            background = (Color) listBox.getRenderProperty(ListBox.PROPERTY_DISABLED_BACKGROUND);
            border = (Border) listBox.getRenderProperty(ListBox.PROPERTY_DISABLED_BORDER);
            font = (Font) listBox.getRenderProperty(ListBox.PROPERTY_DISABLED_FONT);
            foreground = (Color) listBox.getRenderProperty(ListBox.PROPERTY_DISABLED_FOREGROUND);

            // Fallback to normal styles.
            if (background == null) {
                background = (Color) listBox.getRenderProperty(ListBox.PROPERTY_BACKGROUND,
                        DEFAULT_BACKGROUND);
            }
            if (border == null) {
                border = (Border) listBox.getRenderProperty(ListBox.PROPERTY_BORDER, DEFAULT_DHTML_BORDER);
            }
            if (font == null) {
                font = (Font) listBox.getRenderProperty(ListBox.PROPERTY_FONT);
            }
            if (foreground == null) {
                foreground = (Color) listBox.getRenderProperty(ListBox.PROPERTY_FOREGROUND,
                        DEFAULT_FOREGROUND);
            }
        } else {
            border = (Border) listBox.getRenderProperty(ListBox.PROPERTY_BORDER, DEFAULT_DHTML_BORDER);
            foreground = (Color) listBox.getRenderProperty(ListBox.PROPERTY_FOREGROUND, DEFAULT_FOREGROUND);
            background = (Color) listBox.getRenderProperty(ListBox.PROPERTY_BACKGROUND, DEFAULT_BACKGROUND);
            font = (Font) listBox.getRenderProperty(ListBox.PROPERTY_FONT);
        }
        
        BorderRender.renderToStyle(cssStyle, border);
        ColorRender.renderToStyle(cssStyle, foreground, background);
        FontRender.renderToStyle(cssStyle, font);
        
        Extent height = (Extent) listBox.getRenderProperty(ListBox.PROPERTY_HEIGHT, DEFAULT_HEIGHT);
        cssStyle.setAttribute("height", ExtentRender.renderCssAttributeValue(height));

        Extent width = (Extent) listBox.getRenderProperty(ListBox.PROPERTY_WIDTH, DEFAULT_WIDTH);
        if (!width.equals(DEFAULT_WIDTH) || !isDhtmlComponentRequired(rc)) {
            // For components using DHTML list box implementation, there is no reason to set width to 100%.
            // This also conveniently avoids another IE bug.
            cssStyle.setAttribute("width", ExtentRender.renderCssAttributeValue(width));
        }

        Insets insets = (Insets) listBox.getRenderProperty(ListBox.PROPERTY_INSETS, DEFAULT_INSETS);
        InsetsRender.renderToStyle(cssStyle, "padding", insets);

        cssStyle.setAttribute("position", "relative");
        cssStyle.setAttribute("overflow", "auto");
        cssStyle.setAttribute("cursor", "default");

        return cssStyle;
    }
    
    /**
     * Determines whether the use of the custom DHTML list box widget is 
     * required based on browser quirk information.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @return true if the custom DHTML list box widget is required for the 
     *         target client
     */
    private boolean isDhtmlComponentRequired(RenderContext rc) {
        if (FORCE_DHTML_RENDERING) {
            return true;
        }
        ClientProperties clientProperties = rc.getContainerInstance().getClientProperties();
        return clientProperties.getBoolean(ClientProperties.QUIRK_IE_SELECT_LIST_DOM_UPDATE);
    }

    /**
     * Renders disposal code for a list box rendered as a custom DHTML widget.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update
     * @param component the <code>AbstractListComponent</code> being disposed
     */
    private void renderDhtmlDispose(RenderContext rc, ServerComponentUpdate update, Component component) {
        rc.getServerMessage().addLibrary(LIST_COMPONENT_DHTML_SERVICE.getId());
        renderDhtmlDisposeDirective(rc.getServerMessage(), ContainerInstance.getElementId(component));
    }

    /**
     * Renders a directive to the outgoing <code>ServerMessage</code> to 
     * dispose the state of a list component, performing tasks such as 
     * unregistering event listeners on the client.
     * 
     * @param serverMessage the <code>serverMessage</code>
     * @param elementId the HTML element id of the list component
     */
    private void renderDhtmlDisposeDirective(ServerMessage serverMessage, String elementId) {
        Element itemizedUpdateElement = serverMessage.getItemizedDirective(ServerMessage.GROUP_ID_PREREMOVE,
                "EchoListComponentDhtml.MessageProcessor", "dispose",  new String[0], new String[0]);
        Element itemElement = serverMessage.getDocument().createElement("item");
        itemElement.setAttribute("eid", elementId);
        itemizedUpdateElement.appendChild(itemElement);
    }

    /**
     * Renders a directive to the outgoing <code>ServerMessage</code> to 
     * initialize the state of a list component, performing tasks such as 
     * registering event listeners on the client.
     * 
     * @param serverMessage the <code>serverMessage</code>
     * @param listBox the <code>ListBox</code>
     */
    private void renderDhtmlInitDirective(ServerMessage serverMessage, ListBox listBox) {
        String elementId = ContainerInstance.getElementId(listBox);
        Element itemizedUpdateElement = serverMessage.getItemizedDirective(ServerMessage.GROUP_ID_POSTUPDATE,
                "EchoListComponentDhtml.MessageProcessor", "init",  new String[0], new String[0]);
        Element itemElement = serverMessage.getDocument().createElement("item");
        itemElement.setAttribute("eid", elementId);
        if (!listBox.isRenderEnabled()) {
            itemElement.setAttribute("enabled", "false");
        }
        if (listBox.hasActionListeners()) {
            itemElement.setAttribute("server-notify", "true");
        }
        
        Boolean rolloverEnabled = (Boolean) listBox.getRenderProperty(ListBox.PROPERTY_ROLLOVER_ENABLED);
        if (Boolean.TRUE.equals(rolloverEnabled)) {
            CssStyle rolloverCssStyle = createRolloverCssStyle(listBox);
            itemElement.setAttribute("rollover-style", rolloverCssStyle.renderInline());
        }

        itemElement.setAttribute("selection-style", SELECTION_CSS_STYLE_TEXT);
        itemElement.setAttribute("selection-mode", 
                ListSelectionModel.MULTIPLE_SELECTION == listBox.getSelectionMode() ? "multiple" : "single");

        Element selectionElement = serverMessage.getDocument().createElement("selection");
        ListModel model = listBox.getModel();
        for (int i = 0; i < model.size(); i++) {
            boolean selected = listBox.getSelectionModel().isSelectedIndex(i);
            if (selected) {
                Element selectionItemElement = serverMessage.getDocument().createElement("selection-item");
                selectionItemElement.setAttribute("item-id", getOptionId(elementId, i));
                selectionElement.appendChild(selectionItemElement);
            }
        }
        if (selectionElement.hasChildNodes()) {
            itemElement.appendChild(selectionElement);
        }
        
        itemizedUpdateElement.appendChild(itemElement);
    }

    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderDispose(nextapp.echo2.webcontainer.RenderContext,
     *      nextapp.echo2.app.update.ServerComponentUpdate, nextapp.echo2.app.Component)
     */
    public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component) {
        if (isDhtmlComponentRequired(rc)) {
            renderDhtmlDispose(rc, update, component);
        } else {
            renderSelectElementDispose(rc, update, component);
        }
    }
    
    /**
     * Renders a custom DHTML list box widget (used only for clients that have
     * quirks using traditional SELECT-based list boxes, i.e., Internet
     * Explorer 6).
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update
     * @param parentNode the parent node to which HTML elements should be 
     *        appended
     * @param component the child component to render
     */
    private void renderDynamicHtml(RenderContext rc, ServerComponentUpdate update, Node parentNode, Component component) {
        ListBox listBox = (ListBox) component;
        String elementId = ContainerInstance.getElementId(component);
        
        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(LIST_COMPONENT_DHTML_SERVICE.getId());

        renderDhtmlInitDirective(serverMessage, listBox);

        Element listBoxElement = parentNode.getOwnerDocument().createElement("div");
        listBoxElement.setAttribute("id", elementId);

        String toolTipText = (String) listBox.getRenderProperty(ListBox.PROPERTY_TOOL_TIP_TEXT);
        if (toolTipText != null) {
            listBoxElement.setAttribute("title", toolTipText);
        }

        ListModel model = listBox.getModel();
        ListCellRenderer renderer = listBox.getCellRenderer();

        for (int i = 0; i < model.size(); ++i) {
            Element optionElement = parentNode.getOwnerDocument().createElement("div");
            String optionId = getOptionId(elementId, i);
            optionElement.setAttribute("id", optionId);
            Object value = model.get(i);
            Object renderedValue = renderer.getListCellRendererComponent(listBox, value, i);
            optionElement.appendChild(rc.getServerMessage().getDocument().createTextNode(renderedValue.toString()));
            renderItemStyle(optionElement, renderedValue);
            listBoxElement.appendChild(optionElement);
        }

        CssStyle cssStyle = createDhtmlListBoxCssStyle(rc, listBox);
        listBoxElement.setAttribute("style", cssStyle.renderInline());

        parentNode.appendChild(listBoxElement);
    }

    /**
     * @see nextapp.echo2.webcontainer.DomUpdateSupport#renderHtml(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, org.w3c.dom.Node, nextapp.echo2.app.Component)
     */
    public void renderHtml(RenderContext rc, ServerComponentUpdate update, Node parentNode, Component component) {
        if (isDhtmlComponentRequired(rc)) {
            renderDynamicHtml(rc, update, parentNode, component);
        } else {
            renderStandardHtml(rc, update, parentNode, component);
        }
    }
    
    /**
     * Renders a standard select-based ListBox using the parent class's
     * implementation.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update
     * @param parentNode the parent node to which HTML elements should be 
     *        appended
     * @param component the <code>ListBox</code> instance
     */
    private void renderStandardHtml(RenderContext rc, ServerComponentUpdate update, Node parentNode, Component component) {
        ListBox listBox = (ListBox) component;
        boolean multiple = listBox.getSelectionMode() == ListSelectionModel.MULTIPLE_SELECTION;
        renderSelectElementHtml(rc, update, parentNode, listBox, true, multiple);
    }
}