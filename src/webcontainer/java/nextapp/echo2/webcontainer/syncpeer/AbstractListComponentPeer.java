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
import nextapp.echo2.app.list.AbstractListComponent;
import nextapp.echo2.app.list.ListCellRenderer;
import nextapp.echo2.app.list.ListModel;
import nextapp.echo2.app.list.ListSelectionModel;
import nextapp.echo2.app.list.StyledListCell;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.DomUpdateSupport;
import nextapp.echo2.webcontainer.PropertyUpdateProcessor;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.SynchronizePeer;
import nextapp.echo2.webcontainer.propertyrender.ColorRender;
import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
import nextapp.echo2.webcontainer.propertyrender.FontRender;
import nextapp.echo2.webcontainer.propertyrender.InsetsRender;
import nextapp.echo2.webrender.clientupdate.DomPropertyStore;
import nextapp.echo2.webrender.clientupdate.DomUpdate;
import nextapp.echo2.webrender.clientupdate.EventUpdate;
import nextapp.echo2.webrender.clientupdate.ServerMessage;
import nextapp.echo2.webrender.output.CssStyle;
import nextapp.echo2.webrender.server.Service;
import nextapp.echo2.webrender.server.WebRenderServlet;
import nextapp.echo2.webrender.services.JavaScriptService;
import nextapp.echo2.webrender.util.DomUtil;

import org.w3c.dom.Element;

/**
 * Abstract synchronization peer for the built-in
 * <code>nextapp.echo2.app.AbstractListComponent</code> -derived components.
 * The intention of this class is to serve as the superclass for any rendering
 * of a select element.
 * 
 * Since there are deviations in how to access models in the two current
 * subclasses, <code>nextapp.echo2.webcontainer.syncpeer.ListBoxPeer</code>
 * and <code>nextapp.echo2.webcontainer.syncpeer.SelectFieldPeer</code>,
 * certain methods are made abstract to provide access to and manipulation of
 * the <code>nextapp.echo2.app.list.ListModel</code>
 *  
 */
public abstract class AbstractListComponentPeer 
implements DomUpdateSupport, PropertyUpdateProcessor, SynchronizePeer {

    // Default Colors
    protected static final Color DEFAULT_BACKGROUND = Color.WHITE;
    protected static final Color DEFAULT_FOREGROUND = Color.BLACK;
    protected static final Color DEFAULT_ROLLOVER_BACKGROUND = new Color(255, 255, 150);
    protected static final Color DEFAULT_ROLLOVER_FOREGROUND = Color.BLACK;
    protected static final Color DEFAULT_SELECTED_BACKGROUND = new Color(10, 36, 106);
    protected static final Color DEFAULT_SELECTED_FOREGROUND = Color.WHITE;

    // Default Sizes
    protected static final Extent DEFAULT_WIDTH = new Extent(5, Extent.EM);
    protected static final Insets DEFAULT_INSETS = new Insets(new Extent(0), new Extent(0));

    /**
     * Service to provide supporting JavaScript library.
     */
    public static final Service LIST_BOX_COMPONENT_SERVICE = JavaScriptService.forResource("Echo.ListBox",
            "/nextapp/echo2/webcontainer/resource/js/ListBox.js");

    static {
        WebRenderServlet.getServiceRegistry().add(LIST_BOX_COMPONENT_SERVICE);
    }

    /**
     * Appends the default style to the given style based off of properties
     * on the given <code>nextapp.echo2.app.AbstractListComponent</code>
     * 
     * @param style the style to append to
     * @param the <code>nextapp.echo2.app.AbstractListComponent</code>
     */
    private  void appendDefaultCssStyle(CssStyle style, Component component) {
        Color foreground = (Color) ensureValue(component.getRenderProperty(Component.PROPERTY_FOREGROUND), DEFAULT_FOREGROUND);
        Color background = (Color) ensureValue(component.getRenderProperty(Component.PROPERTY_BACKGROUND), DEFAULT_BACKGROUND);
        ColorRender.renderToStyle(style, foreground, background);
    }

    /**
     * Applies CellRenderer style custimization for the given AbstractListComponent
     * based on the given ListModel and index.
     * 
     * @param option the option element
     * @param listComponent the select list instance
     * @param model the ListModel
     * @param index he position in the model representing the given option.
     *  
     */
    protected void applyCellRendererStyle(Element option, CssStyle style, AbstractListComponent listComponent, 
            ListModel model, int index) {
        ListCellRenderer renderer = listComponent.getCellRenderer();
        Object value = model.get(index);
        Object renderedValue = renderer.getListCellRendererComponent(listComponent, value, index);
        option.appendChild(option.getOwnerDocument().createTextNode(renderedValue.toString()));

        if (renderedValue instanceof StyledListCell) {
            StyledListCell styledListCell = (StyledListCell) renderedValue;
            ColorRender.renderToStyle(style, styledListCell.getForeground(), styledListCell.getBackground());
            FontRender.renderToStyle(style, styledListCell.getFont());
        }
    }

    /**
     * Creates the default style based off of properties on the given
     * <code>nextapp.echo2.app.AbstractListComponent</code>
     * 
     * @param component the <code>ListBox</code> instance
     * @return the style
     */
    private CssStyle createDefaultCssStyle(AbstractListComponent listComponent) {
        CssStyle style = new CssStyle();
        appendDefaultCssStyle(style, listComponent);
        return style;
    }

    /**
     * Appends the base style to the given style based off of properties on the
     * given <code>nextapp.echo2.app.AbstractListComponent</code>
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param listComponent the <code>nextapp.echo2.app.AbstractListComponent</code>
     */
    private CssStyle createListBoxCssStyle(RenderContext rc, AbstractListComponent listComponent) {
        CssStyle style = new CssStyle();

        // Ensure defaults since proper rendering depends on reasonable values
        Extent width = (Extent) ensureValue(listComponent.getRenderProperty(AbstractListComponent.PROPERTY_WIDTH), DEFAULT_WIDTH);
        Extent height = (Extent) ensureValue(listComponent.getRenderProperty(AbstractListComponent.PROPERTY_HEIGHT),
                getDefaultHeight());
        Insets insets = (Insets) ensureValue(listComponent.getRenderProperty(AbstractListComponent.PROPERTY_INSETS), DEFAULT_INSETS);

        appendDefaultCssStyle(style, listComponent);
        FontRender.renderToStyle(style, (Font) listComponent.getRenderProperty(AbstractListComponent.PROPERTY_FONT));
        InsetsRender.renderToStyle(style, "padding", insets);

        style.setAttribute("width", ExtentRender.renderCssAttributeValue(width));
        style.setAttribute("height", ExtentRender.renderCssAttributeValue(height));
        style.setAttribute("position", "relative");

        return style;
    }

    /**
     * Creates the rollover style based off of properties on the given
     * <code>nextapp.echo2.app.AbstractListComponent</code>
     * 
     * @param component the <code>ListBox</code> instance
     * @return the style
     */
    private CssStyle createRolloverCssStyle(AbstractListComponent listComponent) {
        CssStyle style = new CssStyle();
        Color foregroundHighlight = (Color) ensureValue(
                listComponent.getRenderProperty(AbstractListComponent.PROPERTY_ROLLOVER_FOREGROUND), DEFAULT_ROLLOVER_FOREGROUND);
        Color backgroundHighlight = (Color) ensureValue(
                listComponent.getRenderProperty(AbstractListComponent.PROPERTY_ROLLOVER_BACKGROUND), DEFAULT_ROLLOVER_BACKGROUND);
        ColorRender.renderToStyle(style, foregroundHighlight, backgroundHighlight);
        return style;
    }
    
    /**
     * Renders the select control reflecting the given multiple and visibleRows
     * parameters.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update
     * @param parent the HTML element representing the parent of the given 
     *        component
     * @param component the <code>nextapp.echo2.app.AbstractListComponent</code>
     *        instance
     */
    protected void renderSelectElementHtml(RenderContext rc, ServerComponentUpdate update, Element parent, Component component, 
            boolean multiple, int visibleRows) {
        renderInitDirective(rc.getServerMessage(), ContainerInstance.getElementId(component));

        AbstractListComponent listComponent = (AbstractListComponent) component;
        String elementId = ContainerInstance.getElementId(component);

        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(LIST_BOX_COMPONENT_SERVICE.getId(), true);

        Element listBoxElement = parent.getOwnerDocument().createElement(
                "select");
        listBoxElement.setAttribute("id", elementId + "_select");
        listBoxElement.setAttribute("name", elementId + "_select");
        listBoxElement.setAttribute("size", "" + visibleRows);

        if (multiple) {
            listBoxElement.setAttribute("multiple", "multiple");
        }

        ListModel model = listComponent.getModel();
        ListSelectionModel selectionModel = listComponent.getSelectionModel();

        for (int i = 0; i < model.size(); i++) {
            boolean selected = selectionModel.isSelectedIndex(i);
            
            Element optionElement = parent.getOwnerDocument().createElement("option");
            String optionId = getOptionId(elementId, i);
            optionElement.setAttribute("id", optionId);
            optionElement.setAttribute("value", optionId);

            CssStyle optionStyle = new CssStyle();

            ListCellRenderer renderer = listComponent.getCellRenderer();
            Object value = model.get(i);
            Object renderedValue = renderer.getListCellRendererComponent(listComponent, value, i);
            optionElement.appendChild(optionElement.getOwnerDocument().createTextNode(renderedValue.toString()));

            if (selected) {
                optionElement.setAttribute("selected", "true");
            } else if (renderedValue instanceof StyledListCell) {
                StyledListCell styledListCell = (StyledListCell) renderedValue;
                ColorRender.renderToStyle(optionStyle, styledListCell.getForeground(), styledListCell.getBackground());
                FontRender.renderToStyle(optionStyle, styledListCell.getFont());
            } else {
                appendDefaultCssStyle(optionStyle, listComponent);
            }

//            EventUpdate.createEventAdd(rc.getServerMessage(), "mouseover,mouseout", optionId, 
//                    "EchoListBox.doRolloverEnter,EchoListBox.doRolloverExit");
            listBoxElement.appendChild(optionElement);
        }

        CssStyle cssStyle = createListBoxCssStyle(rc, listComponent);
        listBoxElement.setAttribute("style", cssStyle.renderInline());

        CssStyle defaultCssStyle = createDefaultCssStyle(listComponent);
        DomPropertyStore.createDomPropertyStore(serverMessage, elementId, "defaultStyle", defaultCssStyle.renderInline());

        CssStyle rolloverCssStyle = createRolloverCssStyle(listComponent);
        DomPropertyStore.createDomPropertyStore(serverMessage, elementId, "rolloverStyle", rolloverCssStyle.renderInline());

        EventUpdate.createEventAdd(rc.getServerMessage(), "change", elementId + "_select", "EchoListBox.processSelection");

        Element containingDiv = parent.getOwnerDocument().createElement("div");
        containingDiv.setAttribute("id", elementId);
        containingDiv.appendChild(listBoxElement);

        parent.appendChild(containingDiv);
    }

    protected Object ensureValue(Object value, Object defaultValue) {
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }

    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#getContainerId(nextapp.echo2.app.Component)
     */
    public String getContainerId(Component child) {
        throw new UnsupportedOperationException("Component does not support children.");
    }

    /**
     * Allows subclasses to define a default height for the rendered select
     * control in the event that one has not been set on a particular
     * <code>nextapp.echo2.app.AbstractListComponent</code>.
     * 
     * @return the default height of the component
     */
    protected abstract Extent getDefaultHeight();

    //BUGBUG. doc.
    protected String getOptionId(String elementId, int index) {
        return elementId + "_" + index;
    }

    /**
     * @see nextapp.echo2.webcontainer.PropertyUpdateProcessor#processPropertyUpdate(
     *      nextapp.echo2.webcontainer.ContainerInstance, nextapp.echo2.app.Component, org.w3c.dom.Element)
     */
    public void processPropertyUpdate(ContainerInstance ci, Component component, Element propertyElement) {
        AbstractListComponent listComponent = (AbstractListComponent) component;
        ListSelectionModel selectionModel = listComponent.getSelectionModel();
        Element[] selected = DomUtil.getChildElementsByTagName(propertyElement, "option");
        for (int i = 0; i < selected.length; i++) {
            Element option = selected[i];
            String attribute = option.getAttribute("id");
            int index = Integer.parseInt(attribute.substring(attribute.lastIndexOf("_") + 1));
            
            //BUGBUG! need to add deselect!
            selectionModel.setSelectedIndex(index, true);
        }
    }

    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#renderAdd(nextapp.echo2.webcontainer.RenderContext,
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String,
     *      nextapp.echo2.app.Component)
     */
    public void renderAdd(RenderContext rc, ServerComponentUpdate update, String targetId, Component component) {
        Element contentElement = DomUpdate.createDomAdd(rc.getServerMessage(), targetId);
        renderHtml(rc, update, contentElement, component);
    }

    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#renderDispose(nextapp.echo2.webcontainer.RenderContext,
     *      nextapp.echo2.app.update.ServerComponentUpdate, nextapp.echo2.app.Component)
     */
    public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component) {
        renderDisposeDirective(rc.getServerMessage(), ContainerInstance.getElementId(component));
    }

    /**
     * Renders a directive to the outgoing <code>ServerMessage</code> to 
     * dispose the state of a list component, performing tasks such as 
     * deregistering event listeners on the client.
     * 
     * @param serverMessage the <code>serverMessage</code>
     * @param elementId the HTML element id of the list component
     */
    private void renderDisposeDirective(ServerMessage serverMessage, String elementId) {
        Element itemizedUpdateElement = serverMessage.getItemizedDirective(ServerMessage.GROUP_ID_PREREMOVE,
                "EchoListBox.MessageProcessor", "dispose",  new String[0], new String[0]);
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
    private void renderInitDirective(ServerMessage serverMessage, String elementId) {
        Element itemizedUpdateElement = serverMessage.getItemizedDirective(ServerMessage.GROUP_ID_POSTUPDATE,
                "EchoListBox.MessageProcessor", "init",  new String[0], new String[0]);
        Element itemElement = serverMessage.getDocument().createElement("item");
        itemElement.setAttribute("eid", elementId);
        itemizedUpdateElement.appendChild(itemElement);
    }

    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#renderUpdate(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String)
     */
    public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update, String targetId) {
        DomUpdate.createDomRemove(rc.getServerMessage(), ContainerInstance.getElementId(update.getParent()));
        renderAdd(rc, update, targetId, update.getParent());
        return false;
    }
}