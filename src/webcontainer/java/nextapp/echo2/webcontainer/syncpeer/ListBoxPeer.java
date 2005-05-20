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

import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.ListBox;
import nextapp.echo2.app.list.AbstractListComponent;
import nextapp.echo2.app.list.ListModel;
import nextapp.echo2.app.list.ListSelectionModel;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.propertyrender.ColorRender;
import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
import nextapp.echo2.webcontainer.propertyrender.FontRender;
import nextapp.echo2.webcontainer.propertyrender.InsetsRender;
import nextapp.echo2.webrender.clientupdate.DomPropertyStore;
import nextapp.echo2.webrender.clientupdate.ServerMessage;
import nextapp.echo2.webrender.output.CssStyle;
import nextapp.echo2.webrender.server.ClientProperties;
import nextapp.echo2.webrender.server.Service;
import nextapp.echo2.webrender.server.WebRenderServlet;
import nextapp.echo2.webrender.services.JavaScriptService;

import org.w3c.dom.Element;

/**
 * Synchronization peer for <code>nextapp.echo2.app.ListBox</code> components.
 */
public class ListBoxPeer extends AbstractListComponentPeer {
    
    public static final int DEFAULT_ROW_COUNT = 5;

    /**
     * Service to provide supporting JavaScript library.
     */
    public static final Service LIST_COMPONENT_DHTML_SERVICE = JavaScriptService.forResource("Echo.ListComponentDhtml",
            "/nextapp/echo2/webcontainer/resource/js/ListComponentDhtml.js");

    static {
        WebRenderServlet.getServiceRegistry().add(LIST_COMPONENT_DHTML_SERVICE);
    }

    /**
     * @see nextapp.echo2.webcontainer.syncpeer.AbstractListComponentPeer#appendDefaultCssStyle(
     *      nextapp.echo2.webrender.output.CssStyle, nextapp.echo2.app.Component)
     */
    private void appendDefaultCssStyle(CssStyle style, Component component) {
        Color foreground = (Color) ensureValue(component.getRenderProperty(Component.PROPERTY_FOREGROUND), DEFAULT_FOREGROUND);
        Color background = (Color) ensureValue(component.getRenderProperty(Component.PROPERTY_BACKGROUND), DEFAULT_BACKGROUND);
        ColorRender.renderToStyle(style, foreground, background);
    }

    /**
     * Appends the selected style to the given style for an inner div of the
     * DHTML rendering based off of properties on the given
     * <code>ListBox</code>.
     * 
     * @param style the <code>CssStyle</code> to append to
     * @param component the <code>ListBox</code> instance
     */
    private void appendSelectedCssStyle(CssStyle style, ListBox listbox) {
        ColorRender.renderToStyle(style, DEFAULT_SELECTED_FOREGROUND, DEFAULT_SELECTED_BACKGROUND);
    }

    /**
     * Creates the default style for an inner div of the DHTML rendering
     * based off of properties on the given <code>ListBox</code>.
     * 
     * @param component the <code>ListBox</code> instance
     * @return the style
     */
    private CssStyle createDefaultCssStyle(ListBox listBox) {
        CssStyle style = new CssStyle();
        appendDefaultCssStyle(style, listBox);
        return style;
    }

    /**
     * Creates the style for the outer div of the DHTML rendering based off of
     * properties on the given <code>ListBox</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param component the <code>ListBox</code> instance
     * @return the style
     */
    private CssStyle createListBoxCssStyle(RenderContext rc, ListBox listBox) {
        CssStyle style = new CssStyle();

        // Ensure defaults since proper rendering depends on reasonable values
        Extent width = (Extent) ensureValue(listBox.getRenderProperty(ListBox.PROPERTY_WIDTH), DEFAULT_WIDTH);
        Extent height = (Extent) ensureValue(listBox.getRenderProperty(ListBox.PROPERTY_HEIGHT), getDefaultHeight());
        Insets insets = (Insets) ensureValue(listBox.getRenderProperty(ListBox.PROPERTY_INSETS), DEFAULT_INSETS);

        appendDefaultCssStyle(style, listBox);
        FontRender.renderToStyle(style, (Font) listBox.getRenderProperty(ListBox.PROPERTY_FONT));

        InsetsRender.renderToStyle(style, "padding", insets);

        style.setAttribute("width", ExtentRender.renderCssAttributeValue(width));
        style.setAttribute("height", ExtentRender.renderCssAttributeValue(height));

        style.setAttribute("position", "relative");
        style.setAttribute("border", "2px inset");
        style.setAttribute("overflow", "auto");

        style.setAttribute("cursor", "default");

        return style;
    }

    /**
     * Returns the rollover style for the DHTML rendering derived from
     * properties on the given <code>ListBox</code>.
     * 
     * @param component the <code>ListBox</code> instance
     * @return the style
     */
    private CssStyle createRolloverCssStyle(ListBox listBox) {
        CssStyle style = new CssStyle();
        Color foregroundHighlight = (Color) ensureValue(listBox.getRenderProperty(ListBox.PROPERTY_ROLLOVER_FOREGROUND),
                DEFAULT_ROLLOVER_FOREGROUND);
        Color backgroundHighlight = (Color) ensureValue(listBox.getRenderProperty(ListBox.PROPERTY_ROLLOVER_BACKGROUND),
                DEFAULT_ROLLOVER_BACKGROUND);

        ColorRender.renderToStyle(style, foregroundHighlight, backgroundHighlight);
        return style;
    }

    /**
     * Creates the selected style for an inner div of the DHTML rendering based
     * off of properties on the given <code>ListBox</code>.
     * 
     * @param component the <code>ListBox</code> instance
     * @return the style
     */
    private CssStyle createSelectedCssStyle(ListBox listBox) {
        CssStyle style = new CssStyle();
        appendSelectedCssStyle(style, listBox);
        return style;
    }

    /**
     * @see nextapp.echo2.webcontainer.syncpeer.AbstractSelectListPeer#getDefaultHeight()
     */
    protected Extent getDefaultHeight() {
        return new Extent(5, Extent.EM);
    }

    /**
     * @see nextapp.echo2.webcontainer.syncpeer.AbstractSelectListPeer#getListModel(AbstractListComponent)
     */
    protected ListModel getListModel(AbstractListComponent selectList) {
        return ((ListBox) selectList).getModel();
    }

    /**
     * @see nextapp.echo2.webcontainer.syncpeer.AbstractSelectListPeer#isIndexSelected(AbstractListComponent, int)
     */
    protected boolean isIndexSelected(AbstractListComponent selectList, int index) {
        return ((ListBox) selectList).isSelectedIndex(index);
    }
    
    private boolean isDhtmlComponentRequired(RenderContext rc) {
        ClientProperties clientProperties = rc.getContainerInstance().getClientProperties();
        return clientProperties.getBoolean(ClientProperties.QUIRK_IE_SELECT_MULTIPLE_DOM_UPDATE);
    }

    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#renderDispose(nextapp.echo2.webcontainer.RenderContext,
     *      nextapp.echo2.app.update.ServerComponentUpdate, nextapp.echo2.app.Component)
     */
    public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component) {
        if (isDhtmlComponentRequired(rc)) {
            renderDhtmlDispose(rc, update, component);
        } else {
            renderSelectElementDispose(rc, update, component);
        }
    }
    
    private void renderDhtmlDispose(RenderContext rc, ServerComponentUpdate update, Component component) {
        renderDhtmlDisposeDirective(rc.getServerMessage(), ContainerInstance.getElementId(component));
    }

    /**
     * Renders a directive to the outgoing <code>ServerMessage</code> to 
     * dispose the state of a list component, performing tasks such as 
     * deregistering event listeners on the client.
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
     * @param elementId the HTML element id of the list component
     */
    private void renderDhtmlInitDirective(ServerMessage serverMessage, String elementId) {
        Element itemizedUpdateElement = serverMessage.getItemizedDirective(ServerMessage.GROUP_ID_POSTUPDATE,
                "EchoListComponentDhtml.MessageProcessor", "init",  new String[0], new String[0]);
        Element itemElement = serverMessage.getDocument().createElement("item");
        itemElement.setAttribute("eid", elementId);
        itemizedUpdateElement.appendChild(itemElement);
    }

    /**
     * Renders a DHTML scrollable div-based ListBox.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update
     * @param parentElement the HTML element which should contain the child
     * @param child the child component to render
     */
    protected void renderDynamicHtml(RenderContext rc, ServerComponentUpdate update, Element parent, Component component) {
        ListBox listBox = (ListBox) component;
        String elementId = ContainerInstance.getElementId(component);
        
        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(LIST_COMPONENT_DHTML_SERVICE.getId(), true);

        renderDhtmlInitDirective(serverMessage, elementId);

        Element listBoxElement = parent.getOwnerDocument().createElement("div");
        listBoxElement.setAttribute("id", elementId);

        ListModel model = listBox.getModel();

        for (int i = 0; i < model.size(); i++) {

            boolean selected = listBox.getSelectionModel().isSelectedIndex(i);

            Element optionElement = parent.getOwnerDocument().createElement("div");
            String optionId = getOptionId(elementId, i);
            optionElement.setAttribute("id", optionId);
            optionElement.appendChild(rc.getServerMessage().getDocument().createTextNode(model.get(i).toString()));

            if (selected) {
                optionElement.setAttribute("style", createSelectedCssStyle(listBox).renderInline());
                DomPropertyStore.createDomPropertyStore(serverMessage, optionId, "selectedState", "selected");
            }

            listBoxElement.appendChild(optionElement);
        }

        CssStyle cssStyle = createListBoxCssStyle(rc, listBox);
        listBoxElement.setAttribute("style", cssStyle.renderInline());

        CssStyle defaultCssStyle = createDefaultCssStyle(listBox);
        DomPropertyStore.createDomPropertyStore(serverMessage, elementId, "defaultStyle", defaultCssStyle.renderInline());

        CssStyle selectedCssStyle = createSelectedCssStyle(listBox);
        DomPropertyStore.createDomPropertyStore(serverMessage, elementId, "selectedStyle", selectedCssStyle.renderInline());

        CssStyle rolloverCssStyle = createRolloverCssStyle(listBox);
        DomPropertyStore.createDomPropertyStore(serverMessage, elementId, "rolloverStyle", rolloverCssStyle.renderInline());

        if (ListSelectionModel.SINGLE_SELECTION == listBox.getSelectionMode()) {
            DomPropertyStore.createDomPropertyStore(serverMessage, elementId, "singleSelect", "true");
        }

        parent.appendChild(listBoxElement);
    }

    /**
     * Renders the appropriate <code>ListBox</code> control based off of the
     * detection of the <code>QUIRK_IE_SELECT_MULTIPLE_DOM_UPDATE quirk</code>.
     * 
     * @see nextapp.echo2.webcontainer.DomUpdateSupport#renderHtml(RenderContext,
     *      ServerComponentUpdate, Element, Component)
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update
     * @param parent the HTML element representing the parent of the given
     *        component
     * @param component the <code>ListBox</code> instance
     */
    public void renderHtml(RenderContext rc, ServerComponentUpdate update, Element parent, Component component) {
        if (isDhtmlComponentRequired(rc)) {
            renderDynamicHtml(rc, update, parent, component);
        } else {
            renderStandardHtml(rc, update, parent, component);
        }
    }

    /**
     * Renders a standard select-based ListBox using the parent class's
     * implementation.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update
     * @param parentElement the HTML element which should contain the child
     * @param component the <code>ListBox</code> instance
     */
    public void renderStandardHtml(RenderContext rc, ServerComponentUpdate update, Element parentElement, Component component) {
        ListBox listBox = (ListBox) component;
        boolean multiple = listBox.getSelectionMode() == ListSelectionModel.MULTIPLE_SELECTION;
        int visibleRows = listBox.getVisibleRowCount() <= 1 ? DEFAULT_ROW_COUNT : listBox.getVisibleRowCount();

        renderSelectElementHtml(rc, update, parentElement, listBox, multiple, visibleRows);
    }

    /**
     * @see nextapp.echo2.webcontainer.syncpeer.AbstractSelectListPeer#setSelectedIndex(AbstractListComponent,
     *      int)
     */
    protected void setSelectedIndex(AbstractListComponent selectList, int index) {
        ((ListBox) selectList).setSelectedIndex(index, true);
    }
}