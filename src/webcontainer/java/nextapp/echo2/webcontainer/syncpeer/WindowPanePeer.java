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

import java.io.IOException;

import org.w3c.dom.Element;

import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.FillImage;
import nextapp.echo2.app.FillImageBorder;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.ResourceImageReference;
import nextapp.echo2.app.WindowPane;
import nextapp.echo2.app.update.PropertyUpdate;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ActionProcessor;
import nextapp.echo2.webcontainer.ComponentSynchronizePeer;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.PartialUpdateManager;
import nextapp.echo2.webcontainer.PartialUpdateParticipant;
import nextapp.echo2.webcontainer.PropertyUpdateProcessor;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.SynchronizePeerFactory;
import nextapp.echo2.webcontainer.image.ImageRenderSupport;
import nextapp.echo2.webcontainer.image.ImageTools;
import nextapp.echo2.webcontainer.propertyrender.ColorRender;
import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
import nextapp.echo2.webcontainer.propertyrender.FillImageRender;
import nextapp.echo2.webcontainer.propertyrender.FontRender;
import nextapp.echo2.webcontainer.propertyrender.InsetsRender;
import nextapp.echo2.webrender.Connection;
import nextapp.echo2.webrender.ContentType;
import nextapp.echo2.webrender.ServerMessage;
import nextapp.echo2.webrender.Service;
import nextapp.echo2.webrender.WebRenderServlet;
import nextapp.echo2.webrender.output.CssStyle;
import nextapp.echo2.webrender.servermessage.DomUpdate;
import nextapp.echo2.webrender.service.JavaScriptService;

/**
 * Synchronization peer for <code>nextapp.echo2.app.WindowPane</code>
 * components.
 * <p>
 * This class should not be extended or used by classes outside of the Echo
 * framework.
 */
public class WindowPanePeer implements ActionProcessor, ImageRenderSupport,
        PropertyUpdateProcessor, ComponentSynchronizePeer {

    /**
     * A boolean property which may be assigned to <code>WindowPane</code>s
     * in order to enable the proprietary Internet Explorer transparent PNG
     * alpha renderer for rendering the <code>border</code> property of the
     * window pane.
     */
    public static final String PROPERTY_IE_ALPHA_RENDER_BORDER
            = "nextapp.echo2.webcontainer.syncpeer.WindowPanePeer.ieAlphaRenderBorder";
    
    private static final String IMAGE_ID_TITLE_BACKGROUND = "titleBackground";
    private static final String IMAGE_ID_CLOSE_ICON = "close";
    private static final String IMAGE_ID_ICON = "icon";
    private static final String IMAGE_ID_BACKGROUND = "background";
    private static final String IMAGE_ID_BORDER_TOP_LEFT = "borderTopLeft";
    private static final String IMAGE_ID_BORDER_TOP = "borderTop";
    private static final String IMAGE_ID_BORDER_TOP_RIGHT = "borderTopRight";
    private static final String IMAGE_ID_BORDER_LEFT = "borderLeft";
    private static final String IMAGE_ID_BORDER_RIGHT = "borderRight";
    private static final String IMAGE_ID_BORDER_BOTTOM_LEFT = "borderBottomLeft";
    private static final String IMAGE_ID_BORDER_BOTTOM = "borderBottom";
    private static final String IMAGE_ID_BORDER_BOTTOM_RIGHT = "borderBottomRight";

    private static final ImageReference DEFAULT_CLOSE_ICON = new ResourceImageReference(
            "/nextapp/echo2/webcontainer/resource/image/DefaultCloseButton.gif");

    private static final String[] FILL_IMAGE_NAMES = {"tl", "t", "tr", "l", "r", "bl", "b", "br"};
    private static final String[] FILL_IMAGE_IDS = {
            IMAGE_ID_BORDER_TOP_LEFT, IMAGE_ID_BORDER_TOP, IMAGE_ID_BORDER_TOP_RIGHT, IMAGE_ID_BORDER_LEFT,
            IMAGE_ID_BORDER_RIGHT, IMAGE_ID_BORDER_BOTTOM_LEFT, IMAGE_ID_BORDER_BOTTOM, IMAGE_ID_BORDER_BOTTOM_RIGHT };
    
    private static final String BLANK_HTML_STRING = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" "
            + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
            + "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title></title><body></body></html>";
    
    private static final Service BLANK_HTML_SERVICE = new Service() {

        /**
         * @see nextapp.echo2.webrender.Service#getId()
         */
        public String getId() {
            return "Echo.WindowPane.IFrame";
        }
    
        /**
         * @see nextapp.echo2.webrender.Service#getVersion()
         */
        public int getVersion() {
            return 0;
        }
    
        /**
         * @see nextapp.echo2.webrender.Service#service(nextapp.echo2.webrender.Connection)
         */
        public void service(Connection conn) throws IOException {
            conn.setContentType(ContentType.TEXT_HTML);
            conn.getWriter().write(BLANK_HTML_STRING);
        }
    };
    
    /**
     * Service to provide supporting JavaScript library.
     */
    public static final Service WINDOW_PANE_SERVICE = JavaScriptService.forResource("Echo.WindowPane",
            "/nextapp/echo2/webcontainer/resource/js/WindowPane.js");

    static {
        WebRenderServlet.getServiceRegistry().add(WINDOW_PANE_SERVICE);
        WebRenderServlet.getServiceRegistry().add(BLANK_HTML_SERVICE);
    }

    private static void renderPixelProperty(WindowPane windowPane, String propertyName, Element element, String attributeName) {
        String pixelValue;
        Extent extent = (Extent) windowPane.getRenderProperty(propertyName);
        if (extent != null) {
            pixelValue = ExtentRender.renderCssAttributePixelValue(extent);
            if (pixelValue != null) {
                element.setAttribute(attributeName, pixelValue);
            }
        }
    }

    private PartialUpdateParticipant placeHolder = new PartialUpdateParticipant() {

        public boolean canRenderProperty(RenderContext rc, ServerComponentUpdate update) {
            return true;
        }

        public void renderProperty(RenderContext rc, ServerComponentUpdate update) {
            // Do nothing.
        }
    };
    
    private PartialUpdateManager partialUpdateManager;

    /**
     * Default constructor.
     */
    public WindowPanePeer() {
        super();
        partialUpdateManager = new PartialUpdateManager();
        partialUpdateManager.add(WindowPane.PROPERTY_POSITION_X, placeHolder);
        partialUpdateManager.add(WindowPane.PROPERTY_POSITION_Y, placeHolder);
        partialUpdateManager.add(WindowPane.PROPERTY_WIDTH, placeHolder);
        partialUpdateManager.add(WindowPane.PROPERTY_HEIGHT, placeHolder);
        partialUpdateManager.add(WindowPane.PROPERTY_TITLE, placeHolder);
    }
    
    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#getContainerId(nextapp.echo2.app.Component)
     */
    public String getContainerId(Component child) {
        return ContainerInstance.getElementId(child.getParent()) + "_content";
    }
    
    /**
     * @see nextapp.echo2.webcontainer.image.ImageRenderSupport#getImage(nextapp.echo2.app.Component,
     *      java.lang.String)
     */
    public ImageReference getImage(Component component, String imageId) {
        if (IMAGE_ID_TITLE_BACKGROUND.equals(imageId)) {
            FillImage backgroundImage = (FillImage) component.getRenderProperty(WindowPane.PROPERTY_TITLE_BACKGROUND_IMAGE);
            return backgroundImage == null ? null : backgroundImage.getImage();
        } else if (IMAGE_ID_BACKGROUND.equals(imageId)) {
            FillImage backgroundImage = (FillImage) component.getRenderProperty(WindowPane.PROPERTY_BACKGROUND_IMAGE);
            return backgroundImage == null ? null : backgroundImage.getImage();
        } else if (IMAGE_ID_ICON.equals(imageId)) {
            return (ImageReference) component.getRenderProperty(WindowPane.PROPERTY_ICON);
        } else if (IMAGE_ID_CLOSE_ICON.equals(imageId)) {
            return (ImageReference) component.getRenderProperty(WindowPane.PROPERTY_CLOSE_ICON, DEFAULT_CLOSE_ICON);
        } else if (IMAGE_ID_BORDER_TOP_LEFT.equals(imageId)) {
            FillImageBorder fillImageBorder = ((FillImageBorder) component.getRenderProperty(WindowPane.PROPERTY_BORDER));
            FillImage fillImage = fillImageBorder == null ? null : fillImageBorder.getFillImage(FillImageBorder.TOP_LEFT);
            return fillImage == null ? null : fillImage.getImage();
        } else if (IMAGE_ID_BORDER_TOP.equals(imageId)) {
            FillImageBorder fillImageBorder = ((FillImageBorder) component.getRenderProperty(WindowPane.PROPERTY_BORDER));
            FillImage fillImage = fillImageBorder == null ? null : fillImageBorder.getFillImage(FillImageBorder.TOP);
            return fillImage == null ? null : fillImage.getImage();
        } else if (IMAGE_ID_BORDER_TOP_RIGHT.equals(imageId)) {
            FillImageBorder fillImageBorder = ((FillImageBorder) component.getRenderProperty(WindowPane.PROPERTY_BORDER));
            FillImage fillImage = fillImageBorder == null ? null : fillImageBorder.getFillImage(FillImageBorder.TOP_RIGHT);
            return fillImage == null ? null : fillImage.getImage();
        } else if (IMAGE_ID_BORDER_LEFT.equals(imageId)) {
            FillImageBorder fillImageBorder = ((FillImageBorder) component.getRenderProperty(WindowPane.PROPERTY_BORDER));
            FillImage fillImage = fillImageBorder == null ? null : fillImageBorder.getFillImage(FillImageBorder.LEFT);
            return fillImage == null ? null : fillImage.getImage();
        } else if (IMAGE_ID_BORDER_RIGHT.equals(imageId)) {
            FillImageBorder fillImageBorder = ((FillImageBorder) component.getRenderProperty(WindowPane.PROPERTY_BORDER));
            FillImage fillImage = fillImageBorder == null ? null : fillImageBorder.getFillImage(FillImageBorder.RIGHT);
            return fillImage == null ? null : fillImage.getImage();
        } else if (IMAGE_ID_BORDER_BOTTOM_LEFT.equals(imageId)) {
            FillImageBorder fillImageBorder = ((FillImageBorder) component.getRenderProperty(WindowPane.PROPERTY_BORDER));
            FillImage fillImage = fillImageBorder == null ? null : fillImageBorder.getFillImage(FillImageBorder.BOTTOM_LEFT);
            return fillImage == null ? null : fillImage.getImage();
        } else if (IMAGE_ID_BORDER_BOTTOM.equals(imageId)) {
            FillImageBorder fillImageBorder = ((FillImageBorder) component.getRenderProperty(WindowPane.PROPERTY_BORDER));
            FillImage fillImage = fillImageBorder == null ? null : fillImageBorder.getFillImage(FillImageBorder.BOTTOM);
            return fillImage == null ? null : fillImage.getImage();
        } else if (IMAGE_ID_BORDER_BOTTOM_RIGHT.equals(imageId)) {
            FillImageBorder fillImageBorder = ((FillImageBorder) component.getRenderProperty(WindowPane.PROPERTY_BORDER));
            FillImage fillImage = fillImageBorder == null ? null : fillImageBorder.getFillImage(FillImageBorder.BOTTOM_RIGHT);
            return fillImage == null ? null : fillImage.getImage();
        } else {
            return null;
        }
    }

    /**
     * @see nextapp.echo2.webcontainer.ActionProcessor#processAction(nextapp.echo2.webcontainer.ContainerInstance,
     *      nextapp.echo2.app.Component, org.w3c.dom.Element)
     */
    public void processAction(ContainerInstance ci, Component component, Element actionElement) {
        WindowPane windowPane = (WindowPane) component;
        boolean closable = ((Boolean) windowPane.getRenderProperty(WindowPane.PROPERTY_CLOSABLE, Boolean.TRUE)).booleanValue();
        if (closable) {
            ci.getUpdateManager().getClientUpdateManager().setComponentAction(component, WindowPane.INPUT_CLOSE, null);
        }
    }

    /**
     * @see nextapp.echo2.webcontainer.PropertyUpdateProcessor#processPropertyUpdate(
     *      nextapp.echo2.webcontainer.ContainerInstance,
     *      nextapp.echo2.app.Component, org.w3c.dom.Element)
     */
    public void processPropertyUpdate(ContainerInstance ci, Component component, Element propertyElement) {
        String propertyName = propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_NAME);
        WindowPane windowPane = (WindowPane) component;
        boolean movable = ((Boolean) windowPane.getRenderProperty(WindowPane.PROPERTY_MOVABLE, Boolean.TRUE)).booleanValue();
        boolean resizable = ((Boolean) windowPane.getRenderProperty(WindowPane.PROPERTY_RESIZABLE, Boolean.TRUE)).booleanValue();
        if (WindowPane.PROPERTY_POSITION_X.equals(propertyName)) {
            if (movable) {
                ci.getUpdateManager().getClientUpdateManager().setComponentProperty(component, WindowPane.PROPERTY_POSITION_X,
                        ExtentRender.toExtent(propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_VALUE)));
            }
        } else if (WindowPane.PROPERTY_POSITION_Y.equals(propertyName)) {
            if (movable) {
                ci.getUpdateManager().getClientUpdateManager().setComponentProperty(component, WindowPane.PROPERTY_POSITION_Y,
                        ExtentRender.toExtent(propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_VALUE)));
            }
        } else if (WindowPane.PROPERTY_WIDTH.equals(propertyName)) {
            if (resizable) {
                ci.getUpdateManager().getClientUpdateManager().setComponentProperty(component, WindowPane.PROPERTY_WIDTH,
                        ExtentRender.toExtent(propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_VALUE)));
            }
        } else if (WindowPane.PROPERTY_HEIGHT.equals(propertyName)) {
            if (resizable) {
                ci.getUpdateManager().getClientUpdateManager().setComponentProperty(component, WindowPane.PROPERTY_HEIGHT,
                        ExtentRender.toExtent(propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_VALUE)));
            }
        } else if (WindowPane.Z_INDEX_CHANGED_PROPERTY.equals(propertyName)) {
            ci.getUpdateManager().getClientUpdateManager().setComponentProperty(component, WindowPane.Z_INDEX_CHANGED_PROPERTY,
                    new Integer(propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_VALUE)));
        }
    }


    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderAdd(nextapp.echo2.webcontainer.RenderContext,
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String,
     *      nextapp.echo2.app.Component)
     */
    public void renderAdd(RenderContext rc, ServerComponentUpdate update,
            String targetId, Component component) {
        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(WINDOW_PANE_SERVICE.getId());
        WindowPane windowPane = (WindowPane) component;
        renderInitDirective(rc, windowPane, targetId);
        Component[] children = windowPane.getVisibleComponents();
        if (children.length != 0) {
            ComponentSynchronizePeer syncPeer = SynchronizePeerFactory.getPeerForComponent(children[0].getClass());
            syncPeer.renderAdd(rc, update, getContainerId(children[0]), children[0]);
        }
    }

    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderDispose(nextapp.echo2.webcontainer.RenderContext,
     *      nextapp.echo2.app.update.ServerComponentUpdate,
     *      nextapp.echo2.app.Component)
     */
    public void renderDispose(RenderContext rc, ServerComponentUpdate update,
            Component component) {
        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(WINDOW_PANE_SERVICE.getId());
        renderDisposeDirective(rc, (WindowPane) component);
    }

    /**
     * Renders a directive to the outgoing <code>ServerMessage</code> to 
     * dispose the state of a <code>WindowPane</code>, performing tasks such as
     * unregistering event listeners on the client.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param windowPane the <code>WindowPane</code>
     */
    private void renderDisposeDirective(RenderContext rc, WindowPane windowPane) {
        String elementId = ContainerInstance.getElementId(windowPane);
        ServerMessage serverMessage = rc.getServerMessage();
        Element initElement = serverMessage.appendPartDirective(ServerMessage.GROUP_ID_PREREMOVE, 
                "EchoWindowPane.MessageProcessor", "dispose");
        initElement.setAttribute("eid", elementId);
    }

    /**
     * Renders a directive to the outgoing <code>ServerMessage</code> to 
     * render and intialize the state of a <code>WindowPane</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param windowPane the <code>WindowPane</code>
     * @param targetId the id of the container element
     */
    private void renderInitDirective(RenderContext rc, WindowPane windowPane, String targetId) {
        String elementId = ContainerInstance.getElementId(windowPane);
        ServerMessage serverMessage = rc.getServerMessage();
        Element partElement = serverMessage.addPart(ServerMessage.GROUP_ID_UPDATE, "EchoWindowPane.MessageProcessor");
        Element initElement = serverMessage.getDocument().createElement("init");
        initElement.setAttribute("container-eid", targetId);
        initElement.setAttribute("eid", elementId);

        if (!windowPane.isRenderEnabled()) {
            initElement.setAttribute("enabled", "false");
        }
        if (windowPane.getZIndex() != 0) {
            initElement.setAttribute("z-index", Integer.toString(windowPane.getZIndex()));
        }
        
        // Content Appearance
        Insets insets = (Insets) windowPane.getRenderProperty(WindowPane.PROPERTY_INSETS);
        if (insets != null) {
            initElement.setAttribute("insets", InsetsRender.renderCssAttributeValue(insets));
        }
        Color background = (Color) windowPane.getRenderProperty(WindowPane.PROPERTY_BACKGROUND);
        if (background != null) {
            initElement.setAttribute("background", ColorRender.renderCssAttributeValue(background));
        }
        FillImage backgroundImage = (FillImage) windowPane.getRenderProperty(WindowPane.PROPERTY_BACKGROUND_IMAGE);
        if (backgroundImage != null) {
            CssStyle backgroundImageCssStyle = new CssStyle();
            FillImageRender.renderToStyle(backgroundImageCssStyle, rc, this, windowPane, IMAGE_ID_BACKGROUND, 
                    backgroundImage, 0);
            initElement.setAttribute("background-image", backgroundImageCssStyle.renderInline());
        }
        Color foreground = (Color) windowPane.getRenderProperty(WindowPane.PROPERTY_FOREGROUND);
        if (foreground != null) {
            initElement.setAttribute("foreground", ColorRender.renderCssAttributeValue(foreground));
        }
        Font font = (Font) windowPane.getRenderProperty(WindowPane.PROPERTY_FONT);
        if (font != null) {
            CssStyle fontCssStyle = new CssStyle();
            FontRender.renderToStyle(fontCssStyle, font);
            initElement.setAttribute("font", fontCssStyle.renderInline());
        }

        // Positioning
        renderPixelProperty(windowPane, WindowPane.PROPERTY_POSITION_X, initElement, "position-x");
        renderPixelProperty(windowPane, WindowPane.PROPERTY_POSITION_Y, initElement, "position-y");
        renderPixelProperty(windowPane, WindowPane.PROPERTY_WIDTH, initElement, "width");
        renderPixelProperty(windowPane, WindowPane.PROPERTY_HEIGHT, initElement, "height");
        renderPixelProperty(windowPane, WindowPane.PROPERTY_MINIMUM_WIDTH, initElement, "minimum-width");
        renderPixelProperty(windowPane, WindowPane.PROPERTY_MINIMUM_HEIGHT, initElement, "minimum-height");
        renderPixelProperty(windowPane, WindowPane.PROPERTY_MAXIMUM_WIDTH, initElement, "maximum-width");
        renderPixelProperty(windowPane, WindowPane.PROPERTY_MAXIMUM_HEIGHT, initElement, "maximum-height");
        
        int fillImageRenderFlags = ((Boolean) windowPane.getRenderProperty(PROPERTY_IE_ALPHA_RENDER_BORDER, 
                Boolean.FALSE)).booleanValue() ? FillImageRender.FLAG_ENABLE_IE_PNG_ALPHA_FILTER : 0;
    
        // Title-related
        if (windowPane.getRenderProperty(WindowPane.PROPERTY_ICON) != null) {
            initElement.setAttribute("icon", ImageTools.getUri(rc, this, windowPane, IMAGE_ID_ICON));
            Insets iconInsets = (Insets) windowPane.getRenderProperty(WindowPane.PROPERTY_ICON_INSETS);
            if (iconInsets != null) {
                initElement.setAttribute("icon-insets", InsetsRender.renderCssAttributeValue(iconInsets));
            }
        }
        String title = (String) windowPane.getRenderProperty(WindowPane.PROPERTY_TITLE);
        if (title != null) {
            initElement.setAttribute("title", title);
            Insets titleInsets = (Insets) windowPane.getRenderProperty(WindowPane.PROPERTY_TITLE_INSETS);
            Color titleForeground = (Color) windowPane.getRenderProperty(WindowPane.PROPERTY_TITLE_FOREGROUND);
            if (titleForeground != null) {
                initElement.setAttribute("title-foreground", ColorRender.renderCssAttributeValue(titleForeground));
            }
            if (titleInsets != null) {
                initElement.setAttribute("title-insets", InsetsRender.renderCssAttributeValue(titleInsets));
            }
            Font titleFont = (Font) windowPane.getRenderProperty(WindowPane.PROPERTY_TITLE_FONT);
            if (titleFont != null) {
                CssStyle fontCssStyle = new CssStyle();
                FontRender.renderToStyle(fontCssStyle, titleFont);
                initElement.setAttribute("title-font", fontCssStyle.renderInline());
            }
        }
        Insets titleBarInsets = (Insets) windowPane.getRenderProperty(WindowPane.PROPERTY_TITLE_BAR_INSETS);
        if (titleBarInsets != null) {
            initElement.setAttribute("title-bar-insets", InsetsRender.renderCssAttributeValue(titleBarInsets));
        }
        renderPixelProperty(windowPane, WindowPane.PROPERTY_TITLE_HEIGHT, initElement, "title-height");
        Color titleBackground = (Color) windowPane.getRenderProperty(WindowPane.PROPERTY_TITLE_BACKGROUND);
        if (titleBackground != null) {
            initElement.setAttribute("title-background", ColorRender.renderCssAttributeValue(titleBackground));
        }
        FillImage titleBackgroundImage = (FillImage) windowPane.getRenderProperty(WindowPane.PROPERTY_TITLE_BACKGROUND_IMAGE);
        if (titleBackgroundImage != null) {
            CssStyle titleBackgroundImageCssStyle = new CssStyle();
            FillImageRender.renderToStyle(titleBackgroundImageCssStyle, rc, this, windowPane, IMAGE_ID_TITLE_BACKGROUND, 
                    titleBackgroundImage, fillImageRenderFlags);
            initElement.setAttribute("title-background-image", titleBackgroundImageCssStyle.renderInline());
        }
        
        // Move/Close/Resize
        Boolean resizableBoolean = (Boolean) windowPane.getRenderProperty(WindowPane.PROPERTY_RESIZABLE);
        boolean resizable = resizableBoolean == null ? true : resizableBoolean.booleanValue();
        initElement.setAttribute("resizable", resizable ? "true" : "false");
        Boolean closableBoolean = (Boolean) windowPane.getRenderProperty(WindowPane.PROPERTY_CLOSABLE);
        boolean closable = closableBoolean == null ? true : closableBoolean.booleanValue();
        initElement.setAttribute("closable", closable ? "true" : "false");
        if (closable) {
            if (getImage(windowPane, IMAGE_ID_CLOSE_ICON) != null) {
                initElement.setAttribute("close-icon", ImageTools.getUri(rc, this, windowPane, IMAGE_ID_CLOSE_ICON));
                Insets closeIconInsets = (Insets) windowPane.getRenderProperty(WindowPane.PROPERTY_CLOSE_ICON_INSETS);
                if (closeIconInsets != null) {
                    initElement.setAttribute("close-icon-insets", InsetsRender.renderCssAttributeValue(closeIconInsets));
                }
            }
        }
        Boolean movableBoolean = (Boolean) windowPane.getRenderProperty(WindowPane.PROPERTY_MOVABLE);
        boolean movable = movableBoolean == null ? true : movableBoolean.booleanValue();
        initElement.setAttribute("movable", movable ? "true" : "false");

        // Border
        FillImageBorder border = (FillImageBorder) windowPane.getRenderProperty(WindowPane.PROPERTY_BORDER);
        if (border != null && border.getBorderInsets() != null && border.getContentInsets() != null) {
            Element borderElement = serverMessage.getDocument().createElement("border");
            if (border.getColor() != null) {
                borderElement.setAttribute("color", ColorRender.renderCssAttributeValue(border.getColor()));
            }
            borderElement.setAttribute("border-insets", InsetsRender.renderCssAttributeValue(border.getBorderInsets()));
            borderElement.setAttribute("content-insets", InsetsRender.renderCssAttributeValue(border.getContentInsets()));
            for (int i = 0; i < 8; ++i) {
                FillImage fillImage = border.getFillImage(i);
                if (fillImage != null) {
                    Element imageElement = serverMessage.getDocument().createElement("image");
                    imageElement.setAttribute("name", FILL_IMAGE_NAMES[i]);
                    CssStyle fillImageCssStyle = new CssStyle();
                    FillImageRender.renderToStyle(fillImageCssStyle, rc, this, windowPane, FILL_IMAGE_IDS[i], fillImage, 
                            fillImageRenderFlags);
                    imageElement.setAttribute("value", fillImageCssStyle.renderInline());
                    borderElement.appendChild(imageElement);
                }
            }
            initElement.appendChild(borderElement);
        }
        
        partElement.appendChild(initElement);
    }
    
    private void renderSetContent(RenderContext rc, ServerComponentUpdate update) {
        //TODO. implement
    }
    
    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderUpdate(nextapp.echo2.webcontainer.RenderContext,
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String)
     */
    public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update,
            String targetId) {
        boolean fullReplace = false;
        if (update.hasUpdatedLayoutDataChildren()) {
            fullReplace = true;
        } else if (update.hasUpdatedProperties()) {
            if (partialUpdateManager.canProcess(rc, update)) {
                renderUpdateDirective(rc, update);
            } else {
                fullReplace = true;
            }
        }

        if (update.hasAddedChildren() || update.hasRemovedChildren() || update.hasUpdatedLayoutDataChildren()) {
            //TODO. temporary, renderSetContent needs impl
            fullReplace = true;
        }
        
        if (fullReplace) {
            // Perform full update.
            renderDisposeDirective(rc, (WindowPane) update.getParent());
            DomUpdate.renderElementRemove(rc.getServerMessage(), ContainerInstance.getElementId(update.getParent()));
            renderAdd(rc, update, targetId, update.getParent());
        } else {
            partialUpdateManager.process(rc, update);
            if (update.hasAddedChildren() || update.hasRemovedChildren()) {
                renderSetContent(rc, update);
            }
        }
        
        return fullReplace;
    }
    
    private void renderUpdateDirective(RenderContext rc, ServerComponentUpdate update) {
        WindowPane windowPane = (WindowPane) update.getParent();

        Element updateElement = rc.getServerMessage().appendPartDirective(ServerMessage.GROUP_ID_PREREMOVE, 
                "EchoWindowPane.MessageProcessor", "update");
        String elementId = ContainerInstance.getElementId(windowPane);
        updateElement.setAttribute("eid", elementId);

        PropertyUpdate positionX = update.getUpdatedProperty(WindowPane.PROPERTY_POSITION_X);
        if (positionX != null) {
            renderPixelProperty(windowPane, WindowPane.PROPERTY_POSITION_X, updateElement, "position-x");
        }

        PropertyUpdate positionY = update.getUpdatedProperty(WindowPane.PROPERTY_POSITION_Y);
        if (positionY != null) {
            renderPixelProperty(windowPane, WindowPane.PROPERTY_POSITION_Y, updateElement, "position-y");
        }
        
        PropertyUpdate width = update.getUpdatedProperty(WindowPane.PROPERTY_WIDTH);
        if (width != null) {
            renderPixelProperty(windowPane, WindowPane.PROPERTY_WIDTH, updateElement, "width");
        }

        PropertyUpdate height = update.getUpdatedProperty(WindowPane.PROPERTY_HEIGHT);
        if (height != null) {
            renderPixelProperty(windowPane, WindowPane.PROPERTY_HEIGHT, updateElement, "height");
        }
        
        if (update.getUpdatedProperty(WindowPane.PROPERTY_TITLE) != null) {
            updateElement.setAttribute("title", (String) windowPane.getRenderProperty(WindowPane.PROPERTY_TITLE, " "));
        }
    }
}
