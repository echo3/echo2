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

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Border;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.RadioButton;
import nextapp.echo2.app.ResourceImageReference;
import nextapp.echo2.app.button.AbstractButton;
import nextapp.echo2.app.button.ToggleButton;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ActionProcessor;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.DomUpdateSupport;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.SynchronizePeer;
import nextapp.echo2.webcontainer.image.ImageRenderSupport;
import nextapp.echo2.webcontainer.propertyrender.AlignmentRender;
import nextapp.echo2.webcontainer.propertyrender.BorderRender;
import nextapp.echo2.webcontainer.propertyrender.ColorRender;
import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
import nextapp.echo2.webcontainer.propertyrender.FontRender;
import nextapp.echo2.webcontainer.propertyrender.ImageReferenceRender;
import nextapp.echo2.webcontainer.propertyrender.InsetsRender;
import nextapp.echo2.webrender.clientupdate.DomPropertyStore;
import nextapp.echo2.webrender.clientupdate.DomUpdate;
import nextapp.echo2.webrender.clientupdate.EventUpdate;
import nextapp.echo2.webrender.clientupdate.ServerMessage;
import nextapp.echo2.webrender.output.CssStyle;
import nextapp.echo2.webrender.server.Service;
import nextapp.echo2.webrender.server.WebRenderServlet;
import nextapp.echo2.webrender.services.JavaScriptService;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Synchronization peer for <code>nextapp.echo2.app.AbstractButton</code>-derived 
 * components.
 */
public class AbstractButtonPeer 
implements ActionProcessor, DomUpdateSupport, ImageRenderSupport, SynchronizePeer {

    private static final Alignment DEFAULT_TEXT_POSITION = new Alignment(Alignment.TRAILING, Alignment.DEFAULT);
    private static final Alignment DEFAULT_STATE_POSITION = new Alignment(Alignment.LEADING, Alignment.DEFAULT);
    private static final Extent DEFAULT_ICON_TEXT_MARGIN = new Extent(3);
    private static final ImageReference DEFAULT_CHECKBOX_ICON
            = new ResourceImageReference("/nextapp/echo2/webcontainer/resource/image/CheckBoxOff.gif");
    private static final ImageReference DEFAULT_SELECTED_CHECKBOX_ICON 
            = new ResourceImageReference("/nextapp/echo2/webcontainer/resource/image/CheckBoxOn.gif");
    private static final ImageReference DEFAULT_RADIOBUTTON_ICON
            = new ResourceImageReference("/nextapp/echo2/webcontainer/resource/image/RadioButtonOff.gif");
    private static final ImageReference DEFAULT_SELECTED_RADIOBUTTON_ICON 
            = new ResourceImageReference("/nextapp/echo2/webcontainer/resource/image/RadioButtonOn.gif");
    
//    private static final String IMAGE_ID_BACKGROUND = "background";
    private static final String IMAGE_ID_ICON = "icon";
//    private static final String IMAGE_ID_ROLLOVER_BACKGROUND = "rolloverBackground";
    private static final String IMAGE_ID_ROLLOVER_ICON = "rolloverIcon";
//    private static final String IMAGE_ID_ROLLOVER_STATE_ICON = "rolloverStateIcon";
//    private static final String IMAGE_ID_PRESSED_BACKGROUND = "pressedBackground";
    private static final String IMAGE_ID_PRESSED_ICON = "pressedIcon";
//    private static final String IMAGE_ID_PRESSED_STATE_ICON = "pressedStateIcon";
    private static final String IMAGE_ID_STATE_ICON = "stateIcon";
    private static final String IMAGE_ID_SELECTED_STATE_ICON = "selectedStateIcon";
    
    /**
     * Service to provide supporting JavaScript library.
     */
    public static final Service BUTTON_SERVICE = JavaScriptService.forResource("Echo.Button", 
            "/nextapp/echo2/webcontainer/resource/js/Button.js");

    static {
        WebRenderServlet.getServiceRegistry().add(BUTTON_SERVICE);
    }
    
    private static int convertIconTextPositionToOrientation(Alignment alignment, Component component) {
        if (alignment.getVertical() == Alignment.DEFAULT) {
            if (alignment.getRenderedHorizontal(component) == Alignment.LEFT) {
                return TriCellTable.LEFT_RIGHT;
            } else {
                return TriCellTable.RIGHT_LEFT;
            }
        } else {
            if (alignment.getVertical() == Alignment.TOP) {
                return TriCellTable.TOP_BOTTOM;
            } else {
                return TriCellTable.BOTTOM_TOP;
            }
        }
    }
    
    private static int convertStatePositionToOrientation(Alignment alignment, Component component) {
        if (alignment.getVertical() == Alignment.DEFAULT) {
            if (alignment.getRenderedHorizontal(component) == Alignment.LEFT) {
                return TriCellTable.RIGHT_LEFT;
            } else {
                return TriCellTable.LEFT_RIGHT;
            }
        } else {
            if (alignment.getVertical() == Alignment.TOP) {
                return TriCellTable.BOTTOM_TOP;
            } else {
                return TriCellTable.TOP_BOTTOM;
            }
        }
    }
    
    private static ImageReference getStateIcon(ToggleButton toggleButton) {
        ImageReference stateIcon = (ImageReference) toggleButton.getRenderProperty(ToggleButton.PROPERTY_STATE_ICON);
        if (stateIcon == null) {
            if (toggleButton instanceof CheckBox) {
                stateIcon = DEFAULT_CHECKBOX_ICON;
            } else if (toggleButton instanceof RadioButton) {
                stateIcon = DEFAULT_RADIOBUTTON_ICON;
            }
        }
        return stateIcon;
    }
    
    private static ImageReference getSelectedStateIcon(ToggleButton toggleButton) {
        ImageReference selectedStateIcon = (ImageReference) toggleButton.getRenderProperty(ToggleButton.PROPERTY_SELECTED_STATE_ICON);
        if (selectedStateIcon == null) {
            if (toggleButton instanceof CheckBox) {
                selectedStateIcon = DEFAULT_SELECTED_CHECKBOX_ICON;
            } else if (toggleButton instanceof RadioButton) {
                selectedStateIcon = DEFAULT_SELECTED_RADIOBUTTON_ICON;
            }
        }
        return selectedStateIcon;
    }
    
    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#getContainerId(nextapp.echo2.app.Component)
     */
    public String getContainerId(Component child) {
        throw new UnsupportedOperationException("Component does not support children.");
    }
    
    /**
     * @see nextapp.echo2.webcontainer.image.ImageRenderSupport#getImage(nextapp.echo2.app.Component, 
     *      java.lang.String)
     */
    public ImageReference getImage(Component component, String imageId) {
        if (IMAGE_ID_ICON.equals(imageId)) {
            return (ImageReference) component.getRenderProperty(AbstractButton.PROPERTY_ICON);
        } else if (IMAGE_ID_ROLLOVER_ICON.equals(imageId)) {
            return (ImageReference) component.getRenderProperty(AbstractButton.PROPERTY_ROLLOVER_ICON);
        } else if (IMAGE_ID_PRESSED_ICON.equals(imageId)) {
            return (ImageReference) component.getRenderProperty(AbstractButton.PROPERTY_PRESSED_ICON);
        } else if (IMAGE_ID_STATE_ICON.equals(imageId)) {
            return getStateIcon((ToggleButton) component);
        } else if (IMAGE_ID_SELECTED_STATE_ICON.equals(imageId)) {
            return getSelectedStateIcon((ToggleButton) component);
        } else {
            return null;
        }
    }

    /**
     * @see nextapp.echo2.webcontainer.ActionProcessor#processAction(nextapp.echo2.webcontainer.ContainerInstance, 
     *      nextapp.echo2.app.Component, org.w3c.dom.Element)
     */
    public void processAction(ContainerInstance ci, Component component, Element actionElement) {
        ci.getUpdateManager().addClientPropertyUpdate(component, AbstractButton.INPUT_CLICK, null);
    }

    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#renderAdd(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String, nextapp.echo2.app.Component)
     */
    public void renderAdd(RenderContext rc, ServerComponentUpdate update, String targetId, Component component) {
        Element contentElement = DomUpdate.createDomAdd(rc.getServerMessage(), targetId);
        renderHtml(rc, update, contentElement, component);
    }
    
    private void renderButtonContent(RenderContext rc, Element buttonContainerElement, AbstractButton button) {
        Node contentNode;
        Document document = rc.getServerMessage().getDocument();
        ToggleButton toggleButton = button instanceof ToggleButton ? (ToggleButton) button : null;
        String elementId = ContainerInstance.getElementId(button);
        
        String text = (String) button.getRenderProperty(AbstractButton.PROPERTY_TEXT);
        ImageReference icon = (ImageReference) button.getRenderProperty(AbstractButton.PROPERTY_ICON);
        
        // Create entities.
        Element iconElement = icon == null ? null : ImageReferenceRender.renderImageReferenceElement(
                rc, AbstractButtonPeer.this, button, IMAGE_ID_ICON);
        Text textNode = text == null ? null : rc.getServerMessage().getDocument().createTextNode(
                (String) button.getRenderProperty(AbstractButton.PROPERTY_TEXT));
        
        Element stateIconElement = toggleButton == null ? null :
                ImageReferenceRender.renderImageReferenceElement(rc, AbstractButtonPeer.this, button, 
                toggleButton.isSelected() ? IMAGE_ID_SELECTED_STATE_ICON : IMAGE_ID_STATE_ICON);
        
        int entityCount = (textNode == null ? 0 : 1) + (iconElement == null ? 0 : 1) + (stateIconElement == null ? 0 : 1);
        
        switch (entityCount) {
        case 1:
            if (textNode != null) {
                contentNode = textNode;
            } else if (iconElement != null) {
                contentNode = iconElement;
            } else { // stateIconElement must not be null.
                contentNode = stateIconElement;
            }
            break;
        case 2:
            Extent iconTextMargin = (Extent) button.getRenderProperty(AbstractButton.PROPERTY_ICON_TEXT_MARGIN, 
                    DEFAULT_ICON_TEXT_MARGIN);
            TriCellTable tct;
            Alignment textPosition = (Alignment) button.getRenderProperty(AbstractButton.PROPERTY_TEXT_POSITION, 
                    DEFAULT_TEXT_POSITION);
            if (stateIconElement == null) {
                // Not rendering a ToggleButton.
                int orientation = convertIconTextPositionToOrientation(textPosition, button);
                tct = new TriCellTable(document, elementId, orientation, iconTextMargin);
                
                Element textTdElement = tct.getTdElement(0);
                CssStyle textTdCssStyle = new CssStyle();
                textTdCssStyle.setAttribute("padding", "0px");
                AlignmentRender.renderToStyle(textTdCssStyle, button, 
                        (Alignment) button.getRenderProperty(AbstractButton.PROPERTY_TEXT_ALIGNMENT));
                textTdElement.setAttribute("style", textTdCssStyle.renderInline());
                textTdElement.appendChild(textNode);
         
                Element iconTdElement = tct.getTdElement(1);
                iconTdElement.setAttribute("style", "padding: 0px");
                iconTdElement.appendChild(iconElement);
                
                Element tableElement = tct.getTableElement();
                tableElement.setAttribute("id", elementId + "_table");
                contentNode = tableElement;
            } else {
                 // Rendering a ToggleButton.
                Extent stateMargin = (Extent) button.getRenderProperty(ToggleButton.PROPERTY_STATE_MARGIN, 
                        DEFAULT_ICON_TEXT_MARGIN);
                Alignment statePosition = (Alignment) button.getRenderProperty(ToggleButton.PROPERTY_STATE_POSITION,
                        DEFAULT_STATE_POSITION);
                int orientation = convertStatePositionToOrientation(statePosition, button);
                tct = new TriCellTable(document, elementId, orientation, stateMargin);

                Element textOrIconTdElement = tct.getTdElement(0);
                CssStyle textOrIconTdCssStyle = new CssStyle();
                textOrIconTdCssStyle.setAttribute("padding", "0px");
                textOrIconTdElement.setAttribute("style", textOrIconTdCssStyle.renderInline());
                if (textNode == null) {
                    textOrIconTdElement.appendChild(iconElement);
                } else {
                    textOrIconTdElement.appendChild(textNode);
                }

                Element stateTdElement = tct.getTdElement(1);
                CssStyle stateTdCssStyle = new CssStyle();
                stateTdCssStyle.setAttribute("padding", "0px");
                AlignmentRender.renderToStyle(stateTdCssStyle, button, 
                        (Alignment) button.getRenderProperty(ToggleButton.PROPERTY_STATE_ALIGNMENT));
                stateTdElement.setAttribute("style", stateTdCssStyle.renderInline());
                stateTdElement.appendChild(stateIconElement);
                
                Element tableElement = tct.getTableElement();
                tableElement.setAttribute("id", elementId + "_table");
                contentNode = tableElement;
            }
            break;
        case 3:
            //BUGBUG. Implement three-item button renderer.
            contentNode = null;
            break;
        default:
            // 0 element button.
            contentNode = null;
        }
        
        if (contentNode != null) {
            buttonContainerElement.appendChild(contentNode);
        }
    }
    
    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#renderDispose(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, nextapp.echo2.app.Component)
     */
    public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component) {
        ServerMessage serverMessage = rc.getServerMessage();
        String id = ContainerInstance.getElementId(component);
        EventUpdate.createEventRemove(serverMessage, "click", id);
        EventUpdate.createEventRemove(serverMessage, "mouseover", id);
        EventUpdate.createEventRemove(serverMessage, "mousedown", id);
    }
    
    public void renderHtml(RenderContext rc, ServerComponentUpdate update, Element parent, Component component) {
        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(BUTTON_SERVICE.getId(), true);
        AbstractButton button = (AbstractButton) component;
        
        Document document = parent.getOwnerDocument();
        Element divElement = document.createElement("div");
        
        String id = ContainerInstance.getElementId(button);
        
        divElement.setAttribute("id", id);
        
        CssStyle cssStyle = new CssStyle();
        cssStyle.setAttribute("position", "relative");
        cssStyle.setAttribute("cursor", "pointer");
        cssStyle.setAttribute("margin", "0px");
        cssStyle.setAttribute("border-spacing", "0px");
        ExtentRender.renderToStyle(cssStyle, "width", (Extent) button.getRenderProperty(AbstractButton.PROPERTY_WIDTH));
        Extent height = (Extent) button.getRenderProperty(AbstractButton.PROPERTY_HEIGHT);
        if (height != null) {
            ExtentRender.renderToStyle(cssStyle, "height", height);
            cssStyle.setAttribute("overflow", "hidden");
        }
        BorderRender.renderToStyle(cssStyle, (Border) button.getRenderProperty(AbstractButton.PROPERTY_BORDER));
        ColorRender.renderToStyle(cssStyle, (Color) button.getRenderProperty(AbstractButton.PROPERTY_FOREGROUND), 
                (Color) button.getRenderProperty(AbstractButton.PROPERTY_BACKGROUND));
        InsetsRender.renderToStyle(cssStyle, "padding", (Insets) button.getRenderProperty(AbstractButton.PROPERTY_INSETS));
        FontRender.renderToStyle(cssStyle, (Font) button.getRenderProperty(AbstractButton.PROPERTY_FONT));
        divElement.setAttribute("style", cssStyle.renderInline());
        
        parent.appendChild(divElement);
        EventUpdate.createEventAdd(serverMessage, "click", id, "EchoButton.processAction");
        EventUpdate.createEventAdd(serverMessage, "mousedown", id, "EchoButton.processPressed");
        
        boolean rolloverEnabled = ((Boolean) button.getRenderProperty(AbstractButton.PROPERTY_ROLLOVER_ENABLED, 
                Boolean.FALSE)).booleanValue();
        boolean pressedEnabled = ((Boolean) button.getRenderProperty(AbstractButton.PROPERTY_PRESSED_ENABLED, 
                Boolean.FALSE)).booleanValue();
        
        if (rolloverEnabled || pressedEnabled) {
            CssStyle baseCssStyle = new CssStyle();
            BorderRender.renderToStyle(baseCssStyle, (Border) button.getRenderProperty(AbstractButton.PROPERTY_BORDER));
            ColorRender.renderToStyle(baseCssStyle, (Color) button.getRenderProperty(AbstractButton.PROPERTY_FOREGROUND), 
                    (Color) button.getRenderProperty(AbstractButton.PROPERTY_BACKGROUND));
            FontRender.renderToStyle(baseCssStyle, (Font) button.getRenderProperty(AbstractButton.PROPERTY_FONT));
            DomPropertyStore.createDomPropertyStore(serverMessage, id, "baseStyle", 
                    baseCssStyle.renderInline());
            
            if (rolloverEnabled) {
                CssStyle rolloverCssStyle = new CssStyle();
                BorderRender.renderToStyle(rolloverCssStyle, (Border) button.getRenderProperty(AbstractButton.PROPERTY_ROLLOVER_BORDER));
                ColorRender.renderToStyle(rolloverCssStyle, (Color) button.getRenderProperty(AbstractButton.PROPERTY_ROLLOVER_FOREGROUND),
                        (Color) button.getRenderProperty(AbstractButton.PROPERTY_ROLLOVER_BACKGROUND));
                FontRender.renderToStyle(rolloverCssStyle, (Font) button.getRenderProperty(AbstractButton.PROPERTY_ROLLOVER_FONT));
                if (rolloverCssStyle.hasAttributes()) {
                    DomPropertyStore.createDomPropertyStore(serverMessage, id, "rolloverStyle", 
                            rolloverCssStyle.renderInline());
                    EventUpdate.createEventAdd(serverMessage, "mouseover", id, "EchoButton.processRolloverEnter");
                }
            }
            
            if (pressedEnabled) {
                CssStyle pressedCssStyle = new CssStyle();
                BorderRender.renderToStyle(pressedCssStyle, (Border) button.getRenderProperty(AbstractButton.PROPERTY_PRESSED_BORDER));
                ColorRender.renderToStyle(pressedCssStyle, (Color) button.getRenderProperty(AbstractButton.PROPERTY_PRESSED_FOREGROUND),
                        (Color) button.getRenderProperty(AbstractButton.PROPERTY_PRESSED_BACKGROUND));
                FontRender.renderToStyle(pressedCssStyle, (Font) button.getRenderProperty(AbstractButton.PROPERTY_PRESSED_FONT));
                if (pressedCssStyle.hasAttributes()) {
                    DomPropertyStore.createDomPropertyStore(serverMessage, id, "pressedStyle", 
                            pressedCssStyle.renderInline());
                }
            }
        }
        
        renderButtonContent(rc, divElement, button);
    }

    /**
     * @see nextapp.echo2.webcontainer.SynchronizePeer#renderUpdate(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String)
     */
    public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update, String targetId) {
        String parentId = ContainerInstance.getElementId(update.getParent());
        DomUpdate.createDomRemove(rc.getServerMessage(), parentId);
        renderAdd(rc, update, targetId, update.getParent());
        return false;
    }
}
