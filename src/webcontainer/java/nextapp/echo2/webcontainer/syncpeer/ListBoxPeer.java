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
import nextapp.echo2.webrender.clientupdate.EventUpdate;
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
    protected static final String DEFAULT_LISTENER = "EchoListBoxDhtml.processSelection";

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
     * @see nextapp.echo2.webcontainer.syncpeer.AbstractSelectListPeer#setSelectedIndex(AbstractListComponent,
     *      int)
     */
    protected void setSelectedIndex(AbstractListComponent selectList, int index) {
        ((ListBox) selectList).setSelectedIndex(index, true);
    }

    /**
     * @see nextapp.echo2.webcontainer.syncpeer.AbstractSelectListPeer#isIndexSelected(AbstractListComponent,
     *      int)
     */
    protected boolean isIndexSelected(AbstractListComponent selectList, int index) {
        return ((ListBox) selectList).isSelectedIndex(index);
    }

    /**
     * Service to provide supporting JavaScript library.
     */
    public static final Service LIST_BOX_COMPONENT_DHTML_SERVICE = JavaScriptService.forResource("Echo.ListBoxDhtml",
            "/nextapp/echo2/webcontainer/resource/js/ListBoxDhtml.js");

    static {
        WebRenderServlet.getServiceRegistry().add(LIST_BOX_COMPONENT_DHTML_SERVICE);
    }

    /**
     * Creates the selected style for an inner div of the DHTML rendering based
     * off of properties on the given <code>ListBox</code>.
     * 
     * @param component the <code>ListBox</code> instance
     * @return the style
     */
    protected CssStyle createSelectedCssStyle(ListBox listBox) {
        CssStyle style = new CssStyle();
        appendSelectedCssStyle(style, listBox);
        return style;
    }

    /**
     * Creates the default style for an inner div of the DHTML rendering
     * based off of properties on the given <code>ListBox</code>.
     * 
     * @param component the <code>ListBox</code> instance
     * @return the style
     */
    protected CssStyle createDefaultCssStyle(ListBox listBox) {
        CssStyle style = new CssStyle();
        appendDefaultCssStyle(style, listBox);
        return style;
    }

    /**
     * Returns the rollover style for the DHTML rendering derived from
     * properties on the given <code>ListBox</code>.
     * 
     * @param component the <code>ListBox</code> instance
     * @return the style
     */
    protected CssStyle createRolloverCssStyle(ListBox listBox) {
        CssStyle style = new CssStyle();
        Color foregroundHighlight = (Color) ensureValue(listBox.getRenderProperty(ListBox.PROPERTY_ROLLOVER_FOREGROUND),
                DEFAULT_ROLLOVER_FOREGROUND);
        Color backgroundHighlight = (Color) ensureValue(listBox.getRenderProperty(ListBox.PROPERTY_ROLLOVER_BACKGROUND),
                DEFAULT_ROLLOVER_BACKGROUND);

        ColorRender.renderToStyle(style, foregroundHighlight, backgroundHighlight);
        return style;
    }

    /**
     * Appends the selected style to the given style for an inner div of the
     * DHTML rendering based off of properties on the given
     * <code>ListBox</code>.
     * 
     * @param style the <code>CssStyle</code> to append to
     * @param component the <code>ListBox</code> instance
     */
    protected void appendSelectedCssStyle(CssStyle style, ListBox listbox) {
        ColorRender.renderToStyle(style, DEFAULT_SELECTED_FOREGROUND, DEFAULT_SELECTED_BACKGROUND);
    }

    /**
     * @see nextapp.echo2.webcontainer.syncpeer.AbstractListComponentPeer#appendDefaultCssStyle(
     *      nextapp.echo2.webrender.output.CssStyle, nextapp.echo2.app.Component)
     */
    protected void appendDefaultCssStyle(CssStyle style, Component component) {
        Color foreground = (Color) ensureValue(component.getRenderProperty(Component.PROPERTY_FOREGROUND), DEFAULT_FOREGROUND);
        Color background = (Color) ensureValue(component.getRenderProperty(Component.PROPERTY_BACKGROUND), DEFAULT_BACKGROUND);
        ColorRender.renderToStyle(style, foreground, background);
    }

    /**
     * Creates the style for the outer div of the DHTML rendering based off of
     * properties on the given <code>ListBox</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param component the <code>ListBox</code> instance
     * @return the style
     */
    protected CssStyle createListBoxCssStyle(RenderContext rc, ListBox listBox) {
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
     * Renders the appropriate <code>ListBox</code> control based off of the
     * detection of the QUIRK_IE_SELECT_MULTIPLE_DOM_UPDATE quirk.
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
        ClientProperties props = rc.getContainerInstance().getClientProperties();
        if (true || props.getBoolean(ClientProperties.QUIRK_IE_SELECT_MULTIPLE_DOM_UPDATE)) {
            doRenderDynamicHtml(rc, update, parent, component);
        } else {
            doRenderStandardHtml(rc, update, parent, component);
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
    public void doRenderStandardHtml(RenderContext rc, ServerComponentUpdate update, Element parentElement, Component component) {

        ListBox listBox = (ListBox) component;
        boolean multiple = listBox.getSelectionMode() == ListSelectionModel.MULTIPLE_SELECTION;

        int visibleRows = listBox.getVisibleRowCount() <= 1 ? DEFAULT_ROW_COUNT : listBox.getVisibleRowCount();

        doRenderHtml(rc, update, parentElement, listBox, multiple, visibleRows);
    }

    /**
     * Renders a DHTML scrollable div-based ListBox.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update
     * @param parentElement the HTML element which should contain the child
     * @param child the child component to render
     */
    protected void doRenderDynamicHtml(RenderContext rc, ServerComponentUpdate update, Element parent, Component component) {

        ListBox listBox = (ListBox) component;
        String elementId = ContainerInstance.getElementId(component);

        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(LIST_BOX_COMPONENT_DHTML_SERVICE.getId(), true);

        Element listBoxElement = parent.getOwnerDocument().createElement("div");
        listBoxElement.setAttribute("id", elementId);

        ListModel model = listBox.getModel();

        for (int i = 0; i < model.size(); i++) {

            boolean selected = listBox.getSelectionModel().isSelectedIndex(i);

            Element optionElement = parent.getOwnerDocument().createElement("div");
            String optionId = toOptionId(elementId, i);
            optionElement.setAttribute("id", optionId);
            optionElement.appendChild(rc.getServerMessage().getDocument().createTextNode(model.get(i).toString()));

            if (selected) {
                optionElement.setAttribute("style", createSelectedCssStyle(listBox).renderInline());
                DomPropertyStore.createDomPropertyStore(serverMessage, optionId, "selectedState", "selected");
            }

            EventUpdate.createEventAdd(rc.getServerMessage(), "click,mouseover,mouseout", optionId, 
                    DEFAULT_LISTENER + ",EchoListBoxDhtml.doRolloverEnter,EchoListBoxDhtml.doRolloverExit");
            listBoxElement.appendChild(optionElement);
        }

        CssStyle cssStyle = createListBoxCssStyle(rc, listBox);
        listBoxElement.setAttribute("style", cssStyle.renderInline());

        CssStyle selectedCssStyle = createSelectedCssStyle(listBox);
        DomPropertyStore.createDomPropertyStore(serverMessage, elementId, "selectedStyle", selectedCssStyle.renderInline());

        CssStyle defaultCssStyle = createDefaultCssStyle(listBox);
        DomPropertyStore.createDomPropertyStore(serverMessage, elementId, "defaultStyle", defaultCssStyle.renderInline());

        CssStyle rolloverCssStyle = createRolloverCssStyle(listBox);
        DomPropertyStore.createDomPropertyStore(serverMessage, elementId, "rolloverStyle", rolloverCssStyle.renderInline());

        if (ListSelectionModel.SINGLE_SELECTION == listBox.getSelectionMode()) {
            DomPropertyStore.createDomPropertyStore(serverMessage, elementId, "singleSelect", "true");
        }

        parent.appendChild(listBoxElement);
    }
}