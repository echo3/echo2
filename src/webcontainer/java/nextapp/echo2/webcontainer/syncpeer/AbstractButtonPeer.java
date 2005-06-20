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
import nextapp.echo2.app.FillImage;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.RadioButton;
import nextapp.echo2.app.ResourceImageReference;
import nextapp.echo2.app.button.AbstractButton;
import nextapp.echo2.app.button.ButtonGroup;
import nextapp.echo2.app.button.ToggleButton;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ActionProcessor;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.DomUpdateSupport;
import nextapp.echo2.webcontainer.PropertyUpdateProcessor;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.ComponentSynchronizePeer;
import nextapp.echo2.webcontainer.image.ImageRenderSupport;
import nextapp.echo2.webcontainer.image.ImageTools;
import nextapp.echo2.webcontainer.propertyrender.AlignmentRender;
import nextapp.echo2.webcontainer.propertyrender.BorderRender;
import nextapp.echo2.webcontainer.propertyrender.ColorRender;
import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
import nextapp.echo2.webcontainer.propertyrender.FillImageRender;
import nextapp.echo2.webcontainer.propertyrender.FontRender;
import nextapp.echo2.webcontainer.propertyrender.ImageReferenceRender;
import nextapp.echo2.webcontainer.propertyrender.InsetsRender;
import nextapp.echo2.webcontainer.propertyrender.LayoutDirectionRender;
import nextapp.echo2.webrender.ServerMessage;
import nextapp.echo2.webrender.Service;
import nextapp.echo2.webrender.WebRenderServlet;
import nextapp.echo2.webrender.output.CssStyle;
import nextapp.echo2.webrender.servermessage.DomUpdate;
import nextapp.echo2.webrender.service.JavaScriptService;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Synchronization peer for 
 * <code>nextapp.echo2.app.AbstractButton</code>-derived components.
 * <p>
 * This class should not be extended or used by classes outside of the
 * Echo framework.
 */
public class AbstractButtonPeer 
implements ActionProcessor, DomUpdateSupport, ImageRenderSupport, PropertyUpdateProcessor, ComponentSynchronizePeer {
    
    //BUGBUG. support rollover and pressed properties where default property value is null (currently renders wrong).

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
    
    private static final String[] BUTTON_INIT_KEYS = new String[]{"default-style", "rollover-style", "pressed-style"};
    
    private static final String IMAGE_ID_BACKGROUND = "background";
    private static final String IMAGE_ID_ICON = "icon";
    private static final String IMAGE_ID_ROLLOVER_BACKGROUND = "rolloverBackground";
    private static final String IMAGE_ID_ROLLOVER_ICON = "rolloverIcon";
    private static final String IMAGE_ID_PRESSED_BACKGROUND = "pressedBackground";
    private static final String IMAGE_ID_PRESSED_ICON = "pressedIcon";
    private static final String IMAGE_ID_STATE_ICON = "stateIcon";
    private static final String IMAGE_ID_SELECTED_STATE_ICON = "selectedStateIcon";
    
    /**
     * Service to provide supporting JavaScript library.
     */
    private static final Service BUTTON_SERVICE = JavaScriptService.forResource("Echo.Button", 
            "/nextapp/echo2/webcontainer/resource/js/Button.js");

    static {
        WebRenderServlet.getServiceRegistry().add(BUTTON_SERVICE);
    }
    
    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#getContainerId(nextapp.echo2.app.Component)
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
        } else if (IMAGE_ID_BACKGROUND.equals(imageId)) {
            FillImage backgroundImage 
                    = (FillImage) component.getRenderProperty(AbstractButton.PROPERTY_BACKGROUND_IMAGE);
            if (backgroundImage == null) {
                return null;
            } else {
                return backgroundImage.getImage();
            }
        } else if (IMAGE_ID_ROLLOVER_BACKGROUND.equals(imageId)) {
            FillImage backgroundImage 
                    = (FillImage) component.getRenderProperty(AbstractButton.PROPERTY_ROLLOVER_BACKGROUND_IMAGE);
            if (backgroundImage == null) {
                return null;
            } else {
                return backgroundImage.getImage();
            }
        } else if (IMAGE_ID_PRESSED_BACKGROUND.equals(imageId)) {
            FillImage backgroundImage 
                    = (FillImage) component.getRenderProperty(AbstractButton.PROPERTY_PRESSED_BACKGROUND_IMAGE);
            if (backgroundImage == null) {
                return null;
            } else {
                return backgroundImage.getImage();
            }
        } else {
            return null;
        }
    }

    /**
     * Determines the selected state icon of the specified
     * <code>ToggleButton</code>.
     * 
     * @param toggleButton the <code>ToggleButton</code>
     * @return the selected state icon
     */
    private ImageReference getSelectedStateIcon(ToggleButton toggleButton) {
        ImageReference selectedStateIcon 
                = (ImageReference) toggleButton.getRenderProperty(ToggleButton.PROPERTY_SELECTED_STATE_ICON);
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
     * Determines the default (non-selected) state icon of the specified
     * <code>ToggleButton</code>.
     * 
     * @param toggleButton the <code>ToggleButton</code>
     * @return the state icon
     */
    private ImageReference getStateIcon(ToggleButton toggleButton) {
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
    
    /**
     * @see nextapp.echo2.webcontainer.ActionProcessor#processAction(nextapp.echo2.webcontainer.ContainerInstance, 
     *      nextapp.echo2.app.Component, org.w3c.dom.Element)
     */
    public void processAction(ContainerInstance ci, Component component, Element actionElement) {
        ci.getUpdateManager().setClientAction(component, AbstractButton.INPUT_CLICK, null);
    }

    /**
     * @see nextapp.echo2.webcontainer.PropertyUpdateProcessor#processPropertyUpdate(
     *      nextapp.echo2.webcontainer.ContainerInstance, nextapp.echo2.app.Component, org.w3c.dom.Element)
     */
    public void processPropertyUpdate(ContainerInstance ci, Component component, Element propertyElement) {
        if (ToggleButton.SELECTED_CHANGED_PROPERTY.equals(propertyElement.getAttribute(PROPERTY_NAME))) {
            ToggleButton toggleButton = (ToggleButton) component;
            toggleButton.setSelected("true".equals(propertyElement.getAttribute("value")));
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
     * Renders the containing DIV element of a button.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param parentNode the parent node
     * @param button the <code>AbstractButton</code> being rendered
     * @return the rendered DIV element (note that this element will already 
     *         have been appended to the parent)
     */
    private Element renderButtonContainer(RenderContext rc, Node parentNode, AbstractButton button) {
        Element divElement = parentNode.getOwnerDocument().createElement("div");
        divElement.setAttribute("id", ContainerInstance.getElementId(button));
        
        if (button.isFocusTraversalParticipant()) {
            divElement.setAttribute("tabindex", Integer.toString(button.getFocusTraversalIndex()));
        } else {
            divElement.setAttribute("tabindex", "-1");
        }
        
        FillImage backgroundImage = (FillImage) button.getRenderProperty(AbstractButton.PROPERTY_BACKGROUND_IMAGE);
        CssStyle cssStyle = new CssStyle();
        cssStyle.setAttribute("cursor", "pointer");
        cssStyle.setAttribute("margin", "0px");
        cssStyle.setAttribute("border-spacing", "0px");
        LayoutDirectionRender.renderToStyle(cssStyle, button.getLayoutDirection(), button.getLocale());
        ExtentRender.renderToStyle(cssStyle, "width", (Extent) button.getRenderProperty(AbstractButton.PROPERTY_WIDTH));
        Extent height = (Extent) button.getRenderProperty(AbstractButton.PROPERTY_HEIGHT);
        if (height != null) {
            ExtentRender.renderToStyle(cssStyle, "height", height);
            cssStyle.setAttribute("overflow", "hidden");
        }
        if (Boolean.FALSE.equals(button.getRenderProperty(AbstractButton.PROPERTY_LINE_WRAP))) {
            cssStyle.setAttribute("white-space", "nowrap");
        }
        BorderRender.renderToStyle(cssStyle, (Border) button.getRenderProperty(AbstractButton.PROPERTY_BORDER));
        ColorRender.renderToStyle(cssStyle, (Color) button.getRenderProperty(AbstractButton.PROPERTY_FOREGROUND), 
                (Color) button.getRenderProperty(AbstractButton.PROPERTY_BACKGROUND));
        InsetsRender.renderToStyle(cssStyle, "padding", (Insets) button.getRenderProperty(AbstractButton.PROPERTY_INSETS));
        FontRender.renderToStyle(cssStyle, (Font) button.getRenderProperty(AbstractButton.PROPERTY_FONT));
        FillImageRender.renderToStyle(cssStyle, rc, this, button, IMAGE_ID_BACKGROUND, backgroundImage, 
                FillImageRender.FLAG_DISABLE_FIXED_MODE);
        
        divElement.setAttribute("style", cssStyle.renderInline());
        parentNode.appendChild(divElement);
        return divElement;
    }
    
    /**
     * Renders the content of the button, i.e., its text, icon, and/or state icon.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param buttonContainerElement the <code>Element</code> which will 
     *        contain the content
     * @param button the <code>AbstractButton</code> being rendered
     */
    private void renderButtonContent(RenderContext rc, Element buttonContainerElement, AbstractButton button) {
        Node contentNode;
        Document document = rc.getServerMessage().getDocument();
        ToggleButton toggleButton = button instanceof ToggleButton ? (ToggleButton) button : null;
        String elementId = ContainerInstance.getElementId(button);
        
        String text = (String) button.getRenderProperty(AbstractButton.PROPERTY_TEXT);
        ImageReference icon = (ImageReference) button.getRenderProperty(AbstractButton.PROPERTY_ICON);
        
        // Create entities.
        Text textNode = text == null ? null : rc.getServerMessage().getDocument().createTextNode(
                (String) button.getRenderProperty(AbstractButton.PROPERTY_TEXT));
        
        Element iconElement;
        if (icon == null) {
            iconElement = null;
        } else {
            iconElement = ImageReferenceRender.renderImageReferenceElement(rc, AbstractButtonPeer.this, button, 
                    IMAGE_ID_ICON);
            iconElement.setAttribute("id", elementId + "_icon");
        }

        Element stateIconElement;
        if (toggleButton == null) {
            stateIconElement = null;
        } else {
            stateIconElement = ImageReferenceRender.renderImageReferenceElement(rc, AbstractButtonPeer.this, button, 
                    toggleButton.isSelected() ? IMAGE_ID_SELECTED_STATE_ICON : IMAGE_ID_STATE_ICON);
            stateIconElement.setAttribute("id", elementId + "_stateicon");
        }
        
        int entityCount = (textNode == null ? 0 : 1) + (iconElement == null ? 0 : 1) + (stateIconElement == null ? 0 : 1);
        
        Extent iconTextMargin;
        Alignment textPosition;
        
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
            iconTextMargin = (Extent) button.getRenderProperty(AbstractButton.PROPERTY_ICON_TEXT_MARGIN, 
                    DEFAULT_ICON_TEXT_MARGIN);
            TriCellTable tct;
            textPosition = (Alignment) button.getRenderProperty(AbstractButton.PROPERTY_TEXT_POSITION, 
                    DEFAULT_TEXT_POSITION);
            if (stateIconElement == null) {
                // Not rendering a ToggleButton.
                int orientation = TriCellTableConfigurator.convertIconTextPositionToOrientation(textPosition, button);
                tct = new TriCellTable(document, elementId, orientation, iconTextMargin);
                
                renderCellText(tct, textNode, button);
                renderCellIcon(tct, iconElement, 1);
                
                Element tableElement = tct.getTableElement();
                tableElement.setAttribute("id", elementId + "_table");
                contentNode = tableElement;
            } else {
                 // Rendering a ToggleButton.
                Extent stateMargin = (Extent) button.getRenderProperty(ToggleButton.PROPERTY_STATE_MARGIN, 
                        DEFAULT_ICON_TEXT_MARGIN);
                Alignment statePosition = (Alignment) button.getRenderProperty(ToggleButton.PROPERTY_STATE_POSITION,
                        DEFAULT_STATE_POSITION);
                int orientation = TriCellTableConfigurator.convertStatePositionToOrientation(statePosition, button);
                tct = new TriCellTable(document, elementId, orientation, stateMargin);

                if (textNode == null) {
                    renderCellIcon(tct, iconElement, 0);
                } else {
                    renderCellText(tct, textNode, button);
                }
                renderCellState(tct, stateIconElement, 1, button);
                
                Element tableElement = tct.getTableElement();
                tableElement.setAttribute("id", elementId + "_table");
                contentNode = tableElement;
            }
            break;
        case 3:
            iconTextMargin = (Extent) button.getRenderProperty(AbstractButton.PROPERTY_ICON_TEXT_MARGIN, 
                    DEFAULT_ICON_TEXT_MARGIN);
            textPosition = (Alignment) button.getRenderProperty(AbstractButton.PROPERTY_TEXT_POSITION, 
                    DEFAULT_TEXT_POSITION);
            Extent stateMargin = (Extent) button.getRenderProperty(ToggleButton.PROPERTY_STATE_MARGIN, 
                    DEFAULT_ICON_TEXT_MARGIN);
            Alignment statePosition = (Alignment) button.getRenderProperty(ToggleButton.PROPERTY_STATE_POSITION,
                    DEFAULT_STATE_POSITION);
            int stateOrientation = TriCellTableConfigurator.convertStatePositionToOrientation(statePosition, button);
            int orientation = TriCellTableConfigurator.convertIconTextPositionToOrientation(textPosition, button);
            tct = new TriCellTable(document, elementId, orientation, iconTextMargin, stateOrientation, stateMargin);

            renderCellText(tct, textNode, button);
            renderCellIcon(tct, iconElement, 1);
            renderCellState(tct, stateIconElement, 2, button);
            
            Element tableElement = tct.getTableElement();
            tableElement.setAttribute("id", elementId + "_table");
            contentNode = tableElement;
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
     * Renders the content of the <code>TriCellTable</code> cell which 
     * contains the button's icon.
     * 
     * @param tct the <code>TriCellTable</code> to update
     * @param iconElement the icon element
     * @param cellIndex the index of the cell in the <code>TriCellTable</code>
     *        that should contain the icon
     */
    private void renderCellIcon(TriCellTable tct, Element iconElement, int cellIndex) {
        Element iconTdElement = tct.getTdElement(cellIndex);
        iconTdElement.setAttribute("style", "padding: 0px");
        iconTdElement.appendChild(iconElement);
    }
    
    /**
     * Renders the content of the <code>TriCellTable</code> cell which 
     * contains the button's state icon.
     * 
     * @param tct the <code>TriCellTable</code> to update
     * @param stateIconElement the state icon element
     * @param cellIndex the index of the cell in the <code>TriCellTable</code>
     *        that should contain the state icon
     * @param button the <code>AbstractButton</code> being rendered
     */
    private void renderCellState(TriCellTable tct, Element stateIconElement, int cellIndex, AbstractButton button) {
        Element stateTdElement = tct.getTdElement(cellIndex);
        CssStyle stateTdCssStyle = new CssStyle();
        stateTdCssStyle.setAttribute("padding", "0px");
        AlignmentRender.renderToStyle(stateTdCssStyle,  
                (Alignment) button.getRenderProperty(ToggleButton.PROPERTY_STATE_ALIGNMENT), button);
        stateTdElement.setAttribute("style", stateTdCssStyle.renderInline());
        stateTdElement.appendChild(stateIconElement);
    }
    
    /**
     * Renders the content of the <code>TriCellTable</code> cell which 
     * contains the button's text.
     * Text is always rendered in cell #0 of the table.
     * 
     * @param tct the <code>TriCellTable</code> to update
     * @param textNode the text
     * @param button the <code>AbstractButton</code> being rendered
     */
    private void renderCellText(TriCellTable tct, Text textNode, AbstractButton button) {
        Element textTdElement = tct.getTdElement(0);
        CssStyle textTdCssStyle = new CssStyle();
        if (Boolean.FALSE.equals(button.getRenderProperty(AbstractButton.PROPERTY_LINE_WRAP))) {
            textTdCssStyle.setAttribute("white-space", "nowrap");
        }
        textTdCssStyle.setAttribute("padding", "0px");
        AlignmentRender.renderToStyle(textTdCssStyle, 
                (Alignment) button.getRenderProperty(AbstractButton.PROPERTY_TEXT_ALIGNMENT), button);
        textTdElement.setAttribute("style", textTdCssStyle.renderInline());
        textTdElement.appendChild(textNode);
    }
    
    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderDispose(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, nextapp.echo2.app.Component)
     */
    public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component) {
        renderDisposeDirective(rc, (AbstractButton) component);
    }

    /**
     * Renders a directive to the outgoing <code>ServerMessage</code> to 
     * dispose the state of a button, performing tasks such as deregistering
     * event listeners on the client.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param button the button
     */
    private void renderDisposeDirective(RenderContext rc, AbstractButton button) {
        ServerMessage serverMessage = rc.getServerMessage();
        Element itemizedUpdateElement = serverMessage.getItemizedDirective(ServerMessage.GROUP_ID_PREREMOVE,
                "EchoButton.MessageProcessor", "dispose",  new String[0], new String[0]);
        Element itemElement = serverMessage.getDocument().createElement("item");
        itemElement.setAttribute("eid", ContainerInstance.getElementId(button));
        itemizedUpdateElement.appendChild(itemElement);
    }
    
    /**
     * @see nextapp.echo2.webcontainer.DomUpdateSupport#renderHtml(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, org.w3c.dom.Node, nextapp.echo2.app.Component)
     */
    public void renderHtml(RenderContext rc, ServerComponentUpdate update, Node parentNode, Component component) {
        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(BUTTON_SERVICE.getId(), true);
        AbstractButton button = (AbstractButton) component;
        Element containerDivElement = renderButtonContainer(rc, parentNode, button);
        renderInitDirective(rc, button);
        renderButtonContent(rc, containerDivElement, button);
    }

    /**
     * Renders a directive to the outgoing <code>ServerMessage</code> to 
     * initialize the state of a button, performing tasks such as registering
     * event listeners on the client.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param button the button
     */
    private void renderInitDirective(RenderContext rc, AbstractButton button) {
        String elementId = ContainerInstance.getElementId(button);
        ServerMessage serverMessage = rc.getServerMessage();
        FillImage backgroundImage = (FillImage) button.getRenderProperty(AbstractButton.PROPERTY_BACKGROUND_IMAGE);
        
        boolean rolloverEnabled = ((Boolean) button.getRenderProperty(AbstractButton.PROPERTY_ROLLOVER_ENABLED, 
                Boolean.FALSE)).booleanValue();
        boolean pressedEnabled = ((Boolean) button.getRenderProperty(AbstractButton.PROPERTY_PRESSED_ENABLED, 
                Boolean.FALSE)).booleanValue();
        
        String pressedStyle = "";
        String defaultStyle = "";
        String rolloverStyle = "";
        
        String defaultIconUri = null;
        String rolloverIconUri = null;
        String pressedIconUri = null;
        
        if (rolloverEnabled || pressedEnabled) {
            boolean hasIcon = button.getRenderProperty(AbstractButton.PROPERTY_ICON) != null;
            CssStyle defaultCssStyle = new CssStyle();
            BorderRender.renderToStyle(defaultCssStyle, (Border) button.getRenderProperty(AbstractButton.PROPERTY_BORDER));
            ColorRender.renderToStyle(defaultCssStyle, (Color) button.getRenderProperty(AbstractButton.PROPERTY_FOREGROUND), 
                    (Color) button.getRenderProperty(AbstractButton.PROPERTY_BACKGROUND), true);
            FontRender.renderToStyle(defaultCssStyle, (Font) button.getRenderProperty(AbstractButton.PROPERTY_FONT));
            FillImageRender.renderToStyle(defaultCssStyle, rc, this, button, IMAGE_ID_BACKGROUND,
                    (FillImage) button.getRenderProperty(AbstractButton.PROPERTY_BACKGROUND_IMAGE), 
                    FillImageRender.FLAG_DISABLE_FIXED_MODE);
            defaultStyle = defaultCssStyle.renderInline();

            if (hasIcon) {
                defaultIconUri = ImageTools.getUri(rc, this, button, IMAGE_ID_ICON);
            }
            
            if (rolloverEnabled) {
                CssStyle rolloverCssStyle = new CssStyle();
                BorderRender.renderToStyle(rolloverCssStyle, 
                        (Border) button.getRenderProperty(AbstractButton.PROPERTY_ROLLOVER_BORDER));
                ColorRender.renderToStyle(rolloverCssStyle, 
                        (Color) button.getRenderProperty(AbstractButton.PROPERTY_ROLLOVER_FOREGROUND),
                        (Color) button.getRenderProperty(AbstractButton.PROPERTY_ROLLOVER_BACKGROUND));
                FontRender.renderToStyle(rolloverCssStyle, 
                        (Font) button.getRenderProperty(AbstractButton.PROPERTY_ROLLOVER_FONT));
                if (backgroundImage != null) {
                    FillImageRender.renderToStyle(rolloverCssStyle, rc, this, button, IMAGE_ID_ROLLOVER_BACKGROUND,
                            (FillImage) button.getRenderProperty(AbstractButton.PROPERTY_ROLLOVER_BACKGROUND_IMAGE), 
                            FillImageRender.FLAG_DISABLE_FIXED_MODE);
                }
                if (rolloverCssStyle.hasAttributes()) {
                    rolloverStyle = rolloverCssStyle.renderInline();
                }
                if (hasIcon) {
                    ImageReference rolloverIcon = (ImageReference) button.getRenderProperty(AbstractButton.PROPERTY_ROLLOVER_ICON);
                    if (rolloverIcon != null) {
                        rolloverIconUri = ImageTools.getUri(rc, this, button, IMAGE_ID_ROLLOVER_ICON);
                    }
                }
            }
            
            if (pressedEnabled) {
                CssStyle pressedCssStyle = new CssStyle();
                BorderRender.renderToStyle(pressedCssStyle, 
                        (Border) button.getRenderProperty(AbstractButton.PROPERTY_PRESSED_BORDER));
                ColorRender.renderToStyle(pressedCssStyle, 
                        (Color) button.getRenderProperty(AbstractButton.PROPERTY_PRESSED_FOREGROUND),
                        (Color) button.getRenderProperty(AbstractButton.PROPERTY_PRESSED_BACKGROUND));
                FontRender.renderToStyle(pressedCssStyle, 
                        (Font) button.getRenderProperty(AbstractButton.PROPERTY_PRESSED_FONT));
                if (backgroundImage != null) {
                    FillImageRender.renderToStyle(pressedCssStyle, rc, this, button, IMAGE_ID_PRESSED_BACKGROUND,
                            (FillImage) button.getRenderProperty(AbstractButton.PROPERTY_PRESSED_BACKGROUND_IMAGE), 
                            FillImageRender.FLAG_DISABLE_FIXED_MODE);
                }
                if (pressedCssStyle.hasAttributes()) {
                    pressedStyle = pressedCssStyle.renderInline();
                }
                if (hasIcon) {
                    ImageReference pressedIcon = (ImageReference) button.getRenderProperty(AbstractButton.PROPERTY_PRESSED_ICON);
                    if (pressedIcon != null) {
                        pressedIconUri = ImageTools.getUri(rc, this, button, IMAGE_ID_PRESSED_ICON);
                    }
                }
            }
        }
        
        Element itemizedUpdateElement = serverMessage.getItemizedDirective(ServerMessage.GROUP_ID_POSTUPDATE,
                "EchoButton.MessageProcessor", "init",  BUTTON_INIT_KEYS, new String[]{defaultStyle, rolloverStyle, pressedStyle});
        Element itemElement = serverMessage.getDocument().createElement("item");
        itemElement.setAttribute("eid", elementId);
        if (defaultIconUri != null) {
            itemElement.setAttribute("default-icon", defaultIconUri);
        }
        if (rolloverIconUri != null) {
            itemElement.setAttribute("rollover-icon", rolloverIconUri);
        }
        if (pressedIconUri != null) {
            itemElement.setAttribute("pressed-icon", pressedIconUri);
        }
        if (!button.hasActionListeners()) {
            itemElement.setAttribute("server-notify", "false");
        }

        if (button instanceof ToggleButton) {
            ToggleButton toggleButton = (ToggleButton) button;
            itemElement.setAttribute("toggle", "true");
            itemElement.setAttribute("selected", toggleButton.isSelected() ? "true" : "false");
            itemElement.setAttribute("state-icon", ImageTools.getUri(rc, this, toggleButton, IMAGE_ID_STATE_ICON));
            itemElement.setAttribute("selected-state-icon", ImageTools.getUri(rc, this, toggleButton, 
                    IMAGE_ID_SELECTED_STATE_ICON));
            if (button instanceof RadioButton) {
                ButtonGroup buttonGroup = ((RadioButton) toggleButton).getGroup();
                if (buttonGroup != null) {
                    rc.getContainerInstance().getIdTable().register(buttonGroup);
                    itemElement.setAttribute("group", buttonGroup.getRenderId());
                }
            }
        }
        
        itemizedUpdateElement.appendChild(itemElement);
    }
    
    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderUpdate(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String)
     */
    public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update, String targetId) {
        String parentId = ContainerInstance.getElementId(update.getParent());
        DomUpdate.renderElementRemove(rc.getServerMessage(), parentId);
        renderAdd(rc, update, targetId, update.getParent());
        return false;
    }
}
