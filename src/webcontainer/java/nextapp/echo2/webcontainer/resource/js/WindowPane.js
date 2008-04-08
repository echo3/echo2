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

EchoWindowPane = Core.extend({

    $static: {
    
        activeInstance: null,
        
        /**
         * Id suffixes of border elements.
         */
        BORDER_ELEMENT_ID_SUFFIXES: ["_border_tl", "_border_t", "_border_tr",
                "_border_l", "_border_r", "_border_bl", "_border_b", "_border_br"],
        
        DEFAULT_CLOSE_ICON_INSETS: "4px",
        DEFAULT_ICON_INSETS: "4px",
        DEFAULT_TITLE_INSETS: "4px",
        DEFAULT_WIDTH: 400,
        DEFAULT_BACKGROUND: "#ffffff",
        DEFAULT_TITLE_BACKGROUND: "#005faf",
        DEFAULT_TITLE_FOREGROUND: "#ffffff",
        DEFAULT_TITLE_HEIGHT: 28,
        DEFAULT_HEIGHT: 300,
        DEFAULT_BORDER: new EchoCoreProperties.FillImageBorder("#00007f", new EchoCoreProperties.Insets(20), 
                new EchoCoreProperties.Insets(3)),
        DEFAULT_TITLE_BAR_INSETS: new EchoCoreProperties.Insets("0px"),
        
        /**
         * Returns the WindowPane data object instance based on the root element
         * of the WindowPane.
         *
         * @param element the root element or element id of the WindowPane
         * @return the relevant WindowPane instance
         */
        getComponent: function(element) {
            return EchoDomPropertyStore.getPropertyValue(element, "component");
        },
        
        /**
         * Do-nothing event handler.
         */
        nullEventHandler: function(echoEvent) { },
        
        processBorderMouseDown: function(echoEvent) {
            var componentId = EchoDomUtil.getComponentId(echoEvent.registeredTarget.id);
            var windowPane = EchoWindowPane.getComponent(componentId);
            windowPane.processBorderMouseDown(echoEvent);
        },
        
        processBorderMouseMove: function(e) {
            e = e ? e : window.event;
            if (EchoWindowPane.activeInstance) {
                EchoWindowPane.activeInstance.processBorderMouseMove(e);
            }
        },
        
        processBorderMouseUp: function(e) {
            e = e ? e : window.event;
            if (EchoWindowPane.activeInstance) {
                EchoWindowPane.activeInstance.processBorderMouseUp(e);
            }
        },
        
        processClose: function(echoEvent) {
            var componentId = EchoDomUtil.getComponentId(echoEvent.registeredTarget.id);
            var windowPane = EchoWindowPane.getComponent(componentId);
            windowPane.processClose(echoEvent);
        },
        
        /**
         * Event handler for "SelectStart" events to disable selection while dragging
         * the Window.  (Internet Explorer specific)
         */
        selectStart: function() {
            EchoDomUtil.preventEventDefault(window.event);
        },
        
        processRaiseClick: function(echoEvent) { 
            var componentId = EchoDomUtil.getComponentId(echoEvent.registeredTarget.id);
            var windowPane = EchoWindowPane.getComponent(componentId);
            windowPane.processRaise(echoEvent);
            return true;
        },
        
        processTitleBarMouseDown: function(echoEvent) {
            var componentId = EchoDomUtil.getComponentId(echoEvent.registeredTarget.id);
            var windowPane = EchoWindowPane.getComponent(componentId);
            windowPane.processTitleBarMouseDown(echoEvent);
        },
        
        processTitleBarMouseMove: function(e) {
            e = e ? e : window.event;
            if (EchoWindowPane.activeInstance) {
                EchoWindowPane.activeInstance.processTitleBarMouseMove(e);
            }
        },
        
        processTitleBarMouseUp: function(e) {
            e = e ? e : window.event;
            if (EchoWindowPane.activeInstance) {
                EchoWindowPane.activeInstance.processTitleBarMouseUp(e);
            }
        }
    },
    
    $construct: function(elementId, containerElementId) {
        this.elementId = elementId;
        this.containerElementId = containerElementId;
        this.containerComponentElementId = EchoDomUtil.getComponentId(containerElementId);
        
        this.resizingBorderElementId = null;
        
        this.dragOriginX = null;
        this.dragOriginY = null;
        this.dragInitPositionX = null;
        this.dragInitPositionY = null;
        this.dragInitWidth = null;
        this.dragInitHeight = null;
        this.resizeX = 0;
        this.resizeY = 0;
        
        this.background = EchoWindowPane.DEFAULT_BACKGROUND;
        this.backgroundImage = null;
        this.border = EchoWindowPane.DEFAULT_BORDER;
        this.closable = true;
        this.closeIcon = null;
        this.closeIconInsets = EchoWindowPane.DEFAULT_CLOSE_ICON_INSETS;
        this.enabled = true;
        this.font = null;
        this.foreground = null;
        this.height = EchoWindowPane.DEFAULT_HEIGHT;
        this.icon = null;
        this.iconInsets = EchoWindowPane.DEFAULT_ICON_INSETS;
        this.insets = null;
        this.maximumWidth = null;
        this.maximumHeight = null;
        this.minimumWidth = 100;
        this.minimumHeight = 100;
        this.movable = true;
        this.overflow = "auto";
        this.positionX = null;
        this.positionY = null;
        this.resizable = true;
        this.title = null;
        this.titleBackground = null;
        this.titleBackgroundImage = null;
        this.titleBarInsets = EchoWindowPane.DEFAULT_TITLE_BAR_INSETS;
        this.titleFont = null;
        this.titleForeground = EchoWindowPane.DEFAULT_TITLE_FOREGROUND;
        this.titleHeight = EchoWindowPane.DEFAULT_TITLE_HEIGHT;
        this.titleInsets = EchoWindowPane.DEFAULT_TITLE_INSETS;
        this.width = EchoWindowPane.DEFAULT_WIDTH;
        this.zIndex = -1;
    },
    
    create: function() {
        var containerElement = document.getElementById(this.containerElementId);
        var windowPaneDivElement = document.createElement("div");
        windowPaneDivElement.id = this.elementId;
        windowPaneDivElement.style.position = "absolute";
        if (this.zIndex != -1) {
            windowPaneDivElement.style.zIndex = this.zIndex;
        }
        
        this.loadContainerSize();
        
        if (this.positionX == null) {
            if (this.containerWidth && this.containerHeight) {
                // Only center window if valid data exist for container width and height.
                this.positionX = Math.round((this.containerWidth - this.width) / 2);
                if (this.positionX < 0) {
                    this.positionX = 0;
                }
            } else {
                this.positionX = 0;
            }
        }
        if (this.positionY == null) {
            if (this.containerWidth && this.containerHeight) {
                // Only center window if valid data exist for container width and height.
                this.positionY = Math.round((this.containerHeight - this.height) / 2);
                if (this.positionY < 0) {
                    this.positionY = 0;
                }
            } else {
                this.positionY = 0;
            }
        }
        windowPaneDivElement.style.left = this.positionX + "px";
        windowPaneDivElement.style.top = this.positionY + "px";
        windowPaneDivElement.style.width = this.width + "px";
        windowPaneDivElement.style.height = this.height + "px";
        
        var borderSideWidth = this.width - this.border.borderInsets.left - this.border.borderInsets.right;
        var borderSideHeight = this.height - this.border.borderInsets.top - this.border.borderInsets.bottom;
        
        var borderDivElements = new Array(8);
        
        // Render top row
        if (this.border.borderInsets.top > 0) {
            // Render top left corner
            if (this.border.borderInsets.left > 0) {
                borderDivElements[0] = document.createElement("div");
                borderDivElements[0].id = this.elementId + "_border_tl";
                borderDivElements[0].style.zIndex = 2;
                borderDivElements[0].style.position = "absolute";
                borderDivElements[0].style.left = "0px";
                borderDivElements[0].style.top = "0px";
                borderDivElements[0].style.width = this.border.borderInsets.left + "px";
                borderDivElements[0].style.height = this.border.borderInsets.top + "px";
                if (this.border.color != null) {
                    borderDivElements[0].style.backgroundColor = this.border.color;
                }
                if (this.resizable) {
                    borderDivElements[0].style.cursor = "nw-resize";
                }
                if (this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_TL]) {
                    EchoCssUtil.applyStyle(borderDivElements[0], 
                            this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_TL]);
                }
                windowPaneDivElement.appendChild(borderDivElements[0]);
            }
            
            // Render top side
            borderDivElements[1] = document.createElement("div");
            borderDivElements[1].id = this.elementId + "_border_t";
            borderDivElements[1].style.zIndex = 2;
            borderDivElements[1].style.position = "absolute";
            borderDivElements[1].style.left = this.border.borderInsets.left + "px";
            borderDivElements[1].style.top = "0px";
            borderDivElements[1].style.width = borderSideWidth + "px";
            borderDivElements[1].style.height = this.border.borderInsets.top + "px";
            if (this.border.color != null) {
                borderDivElements[1].style.backgroundColor = this.border.color;
            }
            if (this.resizable) {
                borderDivElements[1].style.cursor = "n-resize";
            }
            if (this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_T]) {
                EchoCssUtil.applyStyle(borderDivElements[1], 
                        this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_T]);
            }
            windowPaneDivElement.appendChild(borderDivElements[1]);
    
            // Render top right corner
            if (this.border.borderInsets.right > 0) {
                borderDivElements[2] = document.createElement("div");
                borderDivElements[2].id = this.elementId + "_border_tr";
                borderDivElements[2].style.zIndex = 2;
                borderDivElements[2].style.position = "absolute";
                borderDivElements[2].style.right = "0px";
                borderDivElements[2].style.top = "0px";
                borderDivElements[2].style.width = this.border.borderInsets.right + "px";
                borderDivElements[2].style.height = this.border.borderInsets.top + "px";
                if (this.border.color != null) {
                    borderDivElements[2].style.backgroundColor = this.border.color;
                }
                if (this.resizable) {
                    borderDivElements[2].style.cursor = "ne-resize";
                }
                if (this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_TR]) {
                    EchoCssUtil.applyStyle(borderDivElements[2],
                            this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_TR]);
                }
                windowPaneDivElement.appendChild(borderDivElements[2]);
            }
        }
        
        // Render left side
        if (this.border.borderInsets.left > 0) {
            borderDivElements[3] = document.createElement("div");
            borderDivElements[3].id = this.elementId + "_border_l";
            borderDivElements[3].style.zIndex = 2;
            borderDivElements[3].style.position = "absolute";
            borderDivElements[3].style.left = "0px";
            borderDivElements[3].style.top = this.border.borderInsets.top + "px";
            borderDivElements[3].style.width = this.border.borderInsets.left + "px";
            borderDivElements[3].style.height = borderSideHeight + "px";
            if (this.border.color != null) {
                borderDivElements[3].style.backgroundColor = this.border.color;
            }
            if (this.resizable) {
                borderDivElements[3].style.cursor = "w-resize";
            }
            if (this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_L]) {
                EchoCssUtil.applyStyle(borderDivElements[3],
                        this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_L]);
            }
            windowPaneDivElement.appendChild(borderDivElements[3]);
        }
        
        // Render right side
        if (this.border.borderInsets.right > 0) {
            borderDivElements[4] = document.createElement("div");
            borderDivElements[4].id = this.elementId + "_border_r";
            borderDivElements[4].style.zIndex = 2;
            borderDivElements[4].style.position = "absolute";
            borderDivElements[4].style.right = "0px";
            borderDivElements[4].style.top = this.border.borderInsets.top + "px";
            borderDivElements[4].style.width = this.border.borderInsets.right + "px";
            borderDivElements[4].style.height = borderSideHeight + "px";
            if (this.border.color != null) {
                borderDivElements[4].style.backgroundColor = this.border.color;
            }
            if (this.resizable) {
                borderDivElements[4].style.cursor = "e-resize";
            }
            if (this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_R]) {
                EchoCssUtil.applyStyle(borderDivElements[4],
                        this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_R]);
            }
            windowPaneDivElement.appendChild(borderDivElements[4]);
        }
        
        // Render bottom row
        if (this.border.borderInsets.bottom > 0) {
            // Render bottom left corner
            if (this.border.borderInsets.left > 0) {
                borderDivElements[5] = document.createElement("div");
                borderDivElements[5].id = this.elementId + "_border_bl";
                borderDivElements[5].style.zIndex = 2;
                borderDivElements[5].style.position = "absolute";
                borderDivElements[5].style.left = "0px";
                borderDivElements[5].style.bottom = "0px";
                borderDivElements[5].style.width = this.border.borderInsets.left + "px";
                borderDivElements[5].style.height = this.border.borderInsets.bottom + "px";
                if (this.border.color != null) {
                    borderDivElements[5].style.backgroundColor = this.border.color;
                }
                if (this.resizable) {
                    borderDivElements[5].style.cursor = "sw-resize";
                }
                if (this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_BL]) {
                    EchoCssUtil.applyStyle(borderDivElements[5], 
                            this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_BL]);
                }
                windowPaneDivElement.appendChild(borderDivElements[5]);
            }
    
            // Render bottom side
            borderDivElements[6] = document.createElement("div");
            borderDivElements[6].id = this.elementId + "_border_b";
            borderDivElements[6].style.zIndex = 2;
            borderDivElements[6].style.position = "absolute";
            borderDivElements[6].style.left = this.border.borderInsets.left + "px";
            borderDivElements[6].style.bottom = "0px";
            borderDivElements[6].style.width = borderSideWidth + "px";
            borderDivElements[6].style.height = this.border.borderInsets.bottom + "px";
            if (this.border.color != null) {
                borderDivElements[6].style.backgroundColor = this.border.color;
            }
            if (this.resizable) {
                borderDivElements[6].style.cursor = "s-resize";
            }
            if (this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_B]) {
                EchoCssUtil.applyStyle(borderDivElements[6], 
                        this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_B]);
            }
            windowPaneDivElement.appendChild(borderDivElements[6]);
            
            // Render bottom right corner
            if (this.border.borderInsets.right > 0) {
                borderDivElements[7] = document.createElement("div");
                borderDivElements[7].id = this.elementId + "_border_br";
                borderDivElements[7].style.zIndex = 2;
                borderDivElements[7].style.position = "absolute";
                borderDivElements[7].style.right = "0px";
                borderDivElements[7].style.bottom = "0px";
                borderDivElements[7].style.width = this.border.borderInsets.right + "px";
                borderDivElements[7].style.height = this.border.borderInsets.bottom + "px";
                if (this.border.color != null) {
                    borderDivElements[7].style.backgroundColor = this.border.color;
                }
                if (this.resizable) {
                    borderDivElements[7].style.cursor = "se-resize";
                }
                if (this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_BR]) {
                    EchoCssUtil.applyStyle(borderDivElements[7], 
                            this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_BR]);
                }
                windowPaneDivElement.appendChild(borderDivElements[7]);
            }
        }
        
        // Render Title Bar
        var titleBarDivElement = document.createElement("div");
        titleBarDivElement.id = this.elementId + "_titlebar";
        titleBarDivElement.style.position = "absolute";
        titleBarDivElement.style.zIndex = 4;
    
        if (this.titleBackground) {
            titleBarDivElement.style.backgroundColor = this.titleBackground;
        }
    
        titleBarDivElement.style.backgroundColor = this.titleBackground;
        if (this.titleBackgroundImage) {
           EchoCssUtil.applyStyle(titleBarDivElement, this.titleBackgroundImage);
        }
        if (!this.titleBackground && !this.titleBackgroundImage) {
            titleBarDivElement.style.backgroundColor = EchoWindowPane.DEFAULT_TITLE_BACKGROUND;
        }
        titleBarDivElement.style.color = this.titleForeground;
        titleBarDivElement.style.top = this.border.contentInsets.top + "px";
        titleBarDivElement.style.left = this.border.contentInsets.left + this.titleBarInsets.left + "px";
        titleBarDivElement.style.width = (this.width - this.border.contentInsets.left - this.titleBarInsets.left
                - this.border.contentInsets.right - this.titleBarInsets.right) + "px";
        titleBarDivElement.style.height = this.titleHeight + "px";
        titleBarDivElement.style.overflow = "hidden";
        if (this.movable) {
            titleBarDivElement.style.cursor = "move";
        }
        
        if (this.icon) {
            var titleIconDivElement = document.createElement("div");
            titleIconDivElement.style.position = "absolute";
            titleIconDivElement.style.left = "0px";
            if (this.iconInsets != null) {
                titleIconDivElement.style.padding = this.iconInsets;
            }
            titleBarDivElement.appendChild(titleIconDivElement);
            var iconImgElement = document.createElement("img");
            iconImgElement.setAttribute("src", this.icon);
            titleIconDivElement.appendChild(iconImgElement);
        }
        
        var titleTextDivElement = document.createElement("div");
        titleTextDivElement.id = this.elementId + "_titletext";
        titleTextDivElement.style.position = "absolute";
        titleTextDivElement.style.left = "0px";
        titleTextDivElement.style.textAlign = "left";
        titleTextDivElement.style.whiteSpace = "nowrap";
        if (this.icon) {
            titleTextDivElement.style.left = "32px";
        }
        if (this.titleInsets != null) {
            titleTextDivElement.style.padding = this.titleInsets;
        }
        if (this.titleForeground != null) {
            titleTextDivElement.style.color = this.titleForeground;
        }
        if (this.titleFont) {
            EchoCssUtil.applyStyle(titleTextDivElement, this.titleFont);
        }
        if (this.title) {
            titleTextDivElement.appendChild(document.createTextNode(this.title));
        }
        titleBarDivElement.appendChild(titleTextDivElement);
    
        var closeDivElement = null;
        if (this.closable) {
            closeDivElement = document.createElement("div");
            closeDivElement.id = this.elementId + "_close";
            closeDivElement.style.position = "absolute";
            closeDivElement.style.right = "0px";
            closeDivElement.style.cursor = "pointer";
            if (this.closeIconInsets) {
                closeDivElement.style.padding = this.closeIconInsets;
            }
            if (this.closeIcon) {
                var closeImgElement = document.createElement("img");
                closeImgElement.setAttribute("src", this.closeIcon);
                closeDivElement.appendChild(closeImgElement);
            } else {
                closeDivElement.appendChild(document.createTextNode("[X]"));
            }
            titleBarDivElement.appendChild(closeDivElement);
        }
    
        windowPaneDivElement.appendChild(titleBarDivElement);
        
        // Render Content Area
        
        var contentDivElement = document.createElement("div");
        contentDivElement.id = this.elementId + "_content";
        contentDivElement.style.position = "absolute";
        contentDivElement.style.zIndex = 3;
        contentDivElement.style.backgroundColor = this.background;
        if (this.foreground) {
            contentDivElement.style.color = this.foreground;
        }
        if (this.backgroundImage) {
            EchoCssUtil.applyStyle(contentDivElement, this.backgroundImage);
        }
        if (this.font) {
            EchoCssUtil.applyStyle(contentDivElement, this.font);
        }
        contentDivElement.style.top = (this.border.contentInsets.top + this.titleHeight) + "px";
        contentDivElement.style.left = this.border.contentInsets.left + "px";
        contentDivElement.style.right = this.border.contentInsets.right + "px";
        contentDivElement.style.bottom = this.border.contentInsets.bottom + "px";
        EchoVirtualPosition.register(contentDivElement.id);
        
        contentDivElement.style.overflow = "auto";
        if (this.insets != null) {
            contentDivElement.style.padding = this.insets;
        }
        windowPaneDivElement.appendChild(contentDivElement);
    
        if (EchoClientProperties.get("quirkIESelectZIndex")) {
            // Render Select Field Masking Transparent IFRAME.
            var maskDivElement = document.createElement("div");
            maskDivElement.id = this.elementId + "_mask";
            maskDivElement.style.filter = "alpha(opacity=0)";
            maskDivElement.style.zIndex = 1;
            maskDivElement.style.position = "absolute";
            maskDivElement.style.top = 0;
            maskDivElement.style.left = 0;
            maskDivElement.style.right = 0;
            maskDivElement.style.bottom = 0;
            maskDivElement.style.borderWidth = 0;
            
            var maskIFrameElement = document.createElement("iframe");
            maskIFrameElement.style.width = "100%";
            maskIFrameElement.style.height = "100%";
            maskIFrameElement.src = EchoClientEngine.baseServerUri + "?serviceId=Echo.WindowPane.IFrame";
            maskDivElement.appendChild(maskIFrameElement);
            
            EchoVirtualPosition.register(maskDivElement.id);
            windowPaneDivElement.appendChild(maskDivElement);
        }
        
        containerElement.appendChild(windowPaneDivElement);
    
        EchoDomPropertyStore.setPropertyValue(windowPaneDivElement, "component", this);
        
        EchoEventProcessor.addHandler(windowPaneDivElement, "click", "EchoWindowPane.processRaiseClick", true);
        
        if (this.movable) {
            EchoEventProcessor.addHandler(titleBarDivElement, "mousedown", 
                    "EchoWindowPane.processTitleBarMouseDown");
        }
    
        if (this.resizable) {
            for (var i = 0; i < borderDivElements.length; ++i) {
                if (borderDivElements[i]) {
                    EchoEventProcessor.addHandler(borderDivElements[i], "mousedown", "EchoWindowPane.processBorderMouseDown");
                }
            }
        }
        
        if (this.closable) {
            // MouseDown event handler is added to avoid initiating a title-bar drag when close button is clicked.
            EchoEventProcessor.addHandler(closeDivElement, "mousedown", "EchoWindowPane.nullEventHandler");
            EchoEventProcessor.addHandler(closeDivElement, "click", "EchoWindowPane.processClose");
        }
    
        EchoWindowPane.ZIndexManager.add(this.containerComponentElementId, this.elementId);
        
        if (this.zIndex == -1) {
            EchoWindowPane.ZIndexManager.raise(this.containerComponentElementId, this.elementId);
        }
    },
    
    dispose: function() {
        this.removeListeners();
    
        var windowPaneDivElement = document.getElementById(this.elementId);
        EchoEventProcessor.removeHandler(windowPaneDivElement, "click", "EchoWindowPane.processRaiseClick");
    
        if (this.movable) {
            var titleBarDivElement = document.getElementById(this.elementId + "_titlebar");
            EchoEventProcessor.removeHandler(titleBarDivElement, "mousedown");
        }
        
        if (this.closable) {
            var closeDivElement = document.getElementById(this.elementId + "_close");
            EchoEventProcessor.removeHandler(closeDivElement, "mousedown");
            EchoEventProcessor.removeHandler(closeDivElement, "click");
        }
    
        if (this.resizable) {
            for (var i = 0; i < EchoWindowPane.BORDER_ELEMENT_ID_SUFFIXES.length; ++i) {
                var borderElementId = this.elementId + EchoWindowPane.BORDER_ELEMENT_ID_SUFFIXES[i];
                EchoEventProcessor.removeHandler(borderElementId, "mousedown");
            }
        }
    
        EchoWindowPane.ZIndexManager.remove(this.containerComponentElementId, this.elementId);
        
        EchoDomPropertyStore.dispose(this.elementId);
    },
    
    /**
     * Determines dimensions of containing region and stores resultant values in
     * containerWidth/containerHeight properties of WindowPane object.
     */
    loadContainerSize: function() {
        var containerElement = document.getElementById(this.containerElementId);
        this.containerWidth = containerElement.offsetWidth;
        if (this.containerWidth == 0) {
            this.containerWidth = containerElement.parentNode.offsetWidth;
        }
        this.containerHeight = containerElement.offsetHeight;
        if (this.containerHeight == 0) {
            this.containerHeight = containerElement.parentNode.offsetHeight;
        }
    },
    
    processBorderMouseDown: function(echoEvent) {
        if (!this.enabled || !EchoClientEngine.verifyInput(this.elementId)) {
            return;
        }
        EchoDomUtil.preventEventDefault(echoEvent);
        this.raise();
        EchoWindowPane.activeInstance = this;
        this.resizingBorderElementId = echoEvent.registeredTarget.id;
        this.dragInitPositionX = this.positionX;
        this.dragInitPositionY = this.positionY;
        this.dragInitWidth = this.width;
        this.dragInitHeight = this.height;
        this.dragOriginX = echoEvent.clientX;
        this.dragOriginY = echoEvent.clientY;
        
        var directionId = this.resizingBorderElementId.substring(this.resizingBorderElementId.lastIndexOf("_") + 1);
        switch(directionId) {
        case "tl": this.resizeX = -1; this.resizeY = -1; break;
        case "t":  this.resizeX =  0; this.resizeY = -1; break;
        case "tr": this.resizeX =  1; this.resizeY = -1; break;
        case "l":  this.resizeX = -1; this.resizeY =  0; break;
        case "r":  this.resizeX =  1; this.resizeY =  0; break;
        case "bl": this.resizeX = -1; this.resizeY =  1; break;
        case "b":  this.resizeX =  0; this.resizeY =  1; break;
        case "br": this.resizeX =  1; this.resizeY =  1; break;
        }
    
        // Remove all listeners to avoid possible retention issues in IE.
        this.removeListeners();
        
        EchoDomUtil.addEventListener(document, "mousemove", EchoWindowPane.processBorderMouseMove, false);
        EchoDomUtil.addEventListener(document, "mouseup", EchoWindowPane.processBorderMouseUp, false);
        if (EchoClientProperties.get("browserInternetExplorer")) {
            EchoDomUtil.addEventListener(document, "selectstart", EchoWindowPane.selectStart, false);
        }
    },
    
    processBorderMouseMove: function(e) {
        var width = this.width;
        var height = this.height;
        var positionX = this.positionX;
        var positionY = this.positionY;
        
        if (this.resizeX == -1) {
            width = this.dragInitWidth - (e.clientX - this.dragOriginX);
        } else if (this.resizeX ==1 ) {
            width = this.dragInitWidth + e.clientX - this.dragOriginX;
        }
        if (this.resizeY == -1) {
            height = this.dragInitHeight - (e.clientY - this.dragOriginY);
        } else if (this.resizeY ==1) {
            height = this.dragInitHeight + e.clientY - this.dragOriginY;
        }
    
        this.setSize(width, height);
        
        // If Resizing Up or Left, calculate new position based on new width/height such that
        // bottom right corner remains stationary.  This is done with this.width/this.height
        // in case width or height setting was bounded by setSize().
        if (this.resizeX == -1) {
            positionX = this.dragInitPositionX + this.dragInitWidth - this.width;
        }
        if (this.resizeY == -1) {
            positionY = this.dragInitPositionY + this.dragInitHeight - this.height;
        }
        
        this.setPosition(positionX, positionY);
        
        this.redraw();
    },
    
    processBorderMouseUp: function(e) {
        this.removeListeners();
        
        this.resizingBorderElementId = null;
        EchoWindowPane.activeInstance = null;
        
        EchoClientMessage.setPropertyValue(this.elementId, "positionX", this.positionX + "px");
        EchoClientMessage.setPropertyValue(this.elementId, "positionY", this.positionY + "px");
        EchoClientMessage.setPropertyValue(this.elementId, "width", this.width + "px");
        EchoClientMessage.setPropertyValue(this.elementId, "height", this.height + "px");
    
        EchoVirtualPosition.redraw();
    },
    
    processClose: function(echoEvent) {
        if (!this.enabled || !EchoClientEngine.verifyInput(this.elementId)) {
            return;
        }
    
        EchoClientMessage.setActionValue(this.elementId, "close");
        EchoServerTransaction.connect();
    },
    
    processRaise: function(echoEvent) {
        if (!this.enabled || !EchoClientEngine.verifyInput(this.elementId)) {
            return;
        }
        
        this.raise();
    },
    
    processTitleBarMouseDown: function(echoEvent) {
        if (!this.enabled || !EchoClientEngine.verifyInput(this.elementId)) {
            return;
        }
        EchoDomUtil.preventEventDefault(echoEvent);
        this.raise();
        EchoWindowPane.activeInstance = this;
        this.dragInitPositionX = this.positionX;
        this.dragInitPositionY = this.positionY;
        this.dragOriginX = echoEvent.clientX;
        this.dragOriginY = echoEvent.clientY;
        
        // Remove all listeners to avoid possible retention issues in IE.
        this.removeListeners();
        
        EchoDomUtil.addEventListener(document, "mousemove", EchoWindowPane.processTitleBarMouseMove, false);
        EchoDomUtil.addEventListener(document, "mouseup", EchoWindowPane.processTitleBarMouseUp, false);
        if (EchoClientProperties.get("browserInternetExplorer")) {
            EchoDomUtil.addEventListener(document, "selectstart", EchoWindowPane.selectStart, false);
        }
    },
    
    processTitleBarMouseMove: function(e) {
        this.setPosition(this.dragInitPositionX + e.clientX - this.dragOriginX,
                this.dragInitPositionY + e.clientY - this.dragOriginY);
        this.redraw();
    },
    
    processTitleBarMouseUp: function(e) {
        this.removeListeners();
        
        EchoWindowPane.activeInstance = null;
        
        EchoClientMessage.setPropertyValue(this.elementId, "positionX", this.positionX + "px");
        EchoClientMessage.setPropertyValue(this.elementId, "positionY", this.positionY + "px");
        
        EchoVirtualPosition.redraw();
    },
    
    raise: function() {
        var zIndex = EchoWindowPane.ZIndexManager.raise(this.containerComponentElementId, this.elementId);
        EchoClientMessage.setPropertyValue(this.elementId, "zIndex",  zIndex);
    },
    
    redraw: function() {
        var windowPaneDivElement = document.getElementById(this.elementId);
    
        var titleBarDivElement = document.getElementById(this.elementId + "_titlebar");
    
        var borderTDivElement = document.getElementById(this.elementId + "_border_t");
        var borderBDivElement = document.getElementById(this.elementId + "_border_b");
        var borderLDivElement = document.getElementById(this.elementId + "_border_l");
        var borderRDivElement = document.getElementById(this.elementId + "_border_r");
    
        var borderSideWidth = this.width - this.border.borderInsets.left - this.border.borderInsets.right;
        var borderSideHeight = this.height - this.border.borderInsets.top - this.border.borderInsets.bottom;
    
        windowPaneDivElement.style.left = this.positionX + "px";
        windowPaneDivElement.style.top = this.positionY + "px";
        windowPaneDivElement.style.width = this.width + "px";
        windowPaneDivElement.style.height = this.height + "px";
    
        titleBarDivElement.style.width = (this.width - this.border.contentInsets.left - this.titleBarInsets.left 
                - this.border.contentInsets.right - this.titleBarInsets.right) + "px";
    
        borderTDivElement.style.width = borderSideWidth + "px";
        borderBDivElement.style.width = borderSideWidth + "px";
        borderLDivElement.style.height = borderSideHeight + "px";
        borderRDivElement.style.height = borderSideHeight + "px";
    
        var contentElement = document.getElementById(this.elementId + "_content");
        
        EchoVirtualPosition.redraw(contentElement);
        if (EchoClientProperties.get("quirkIESelectZIndex")) {
            var maskDivElement = document.getElementById(this.elementId + "_mask");
            EchoVirtualPosition.redraw(maskDivElement);
        }
    },
    
    redrawTitle: function() {
        var titleTextDivElement = document.getElementById(this.elementId + "_titletext");
        while (titleTextDivElement.firstChild) {
            titleTextDivElement.removeChild(titleTextDivElement.firstChild);
        }
        titleTextDivElement.appendChild(document.createTextNode(this.title));
    },
    
    removeListeners: function() {
        EchoDomUtil.removeEventListener(document, "mousemove", EchoWindowPane.processTitleBarMouseMove, false);
        EchoDomUtil.removeEventListener(document, "mouseup", EchoWindowPane.processTitleBarMouseUp, false);
        EchoDomUtil.removeEventListener(document, "mousemove", EchoWindowPane.processBorderMouseMove, false);
        EchoDomUtil.removeEventListener(document, "mouseup", EchoWindowPane.processBorderMouseUp, false);
        if (EchoClientProperties.get("browserInternetExplorer")) {
            EchoDomUtil.removeEventListener(document, "selectstart", EchoWindowPane.selectStart, false);
        }
    },
    
    setPosition: function(positionX, positionY) {
        this.loadContainerSize();
        
        if (positionX < 0) {
            positionX = 0;
        } else {
            if (this.containerWidth > 0 && positionX > this.containerWidth - this.width) {
                positionX = this.containerWidth - this.width;
            }
        }
        if (positionY < 0) {
            positionY = 0;
        } else {
            if (this.containerHeight > 0 && positionY > this.containerHeight - this.height) {
                positionY = this.containerHeight - this.height;
            }
        }
        
    
        this.positionX = positionX;
        this.positionY = positionY;
    },
    
    setSize: function(width, height) {
        if (this.minimumWidth != null && width < this.minimumWidth) {
            width = this.minimumWidth;
        } else if (this.maximumWidth != null && width > this.maximumWidth) {
            width = this.maximumWidth;
        }
        if (this.minimumHeight != null && height < this.minimumHeight) {
            height = this.minimumHeight;
        } else if (this.maximumHeight != null && height > this.maximumHeight) {
            height = this.maximumHeight;
        }
        this.width = width;
        this.height = height;
    }
});

/**
 * Static object/namespace for WindowPane MessageProcessor 
 * implementation.
 */
EchoWindowPane.MessageProcessor = {

    loadProperties: function(propertiesElement, windowPane) {
        if (propertiesElement.getAttribute("position-x")) {
            windowPane.positionX = parseInt(propertiesElement.getAttribute("position-x"));
        }
        if (propertiesElement.getAttribute("position-y")) {
            windowPane.positionY = parseInt(propertiesElement.getAttribute("position-y"));
        }
        if (propertiesElement.getAttribute("width")) {
            windowPane.width = parseInt(propertiesElement.getAttribute("width"));
        }
        if (propertiesElement.getAttribute("height")) {
            windowPane.height = parseInt(propertiesElement.getAttribute("height"));
        }
        if (propertiesElement.getAttribute("title")) {
            windowPane.title = propertiesElement.getAttribute("title");
        }
    },
    
    /**
     * MessageProcessor process() implementation 
     * (invoked by ServerMessage processor).
     *
     * @param messagePartElement the <code>message-part</code> element to process
     */
    process: function(messagePartElement) {
        for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
            if (messagePartElement.childNodes[i].nodeType == 1) {
                switch (messagePartElement.childNodes[i].tagName) {
                case "dispose":
                    EchoWindowPane.MessageProcessor.processDispose(messagePartElement.childNodes[i]);
                    break;
                case "init":
                    EchoWindowPane.MessageProcessor.processInit(messagePartElement.childNodes[i]);
                    break;
                case "update":
                    EchoWindowPane.MessageProcessor.processUpdate(messagePartElement.childNodes[i]);
                    break;
                }
            }
        }
    },
    
    /**
     * Processes a <code>dispose</code> message to finalize the state of a
     * WindowPane that is being removed.
     *
     * @param disposeMessageElement the <code>dispose</code> element to process
     */
    processDispose: function(disposeElement) {
        var elementId = disposeElement.getAttribute("eid");
        var windowPane = EchoWindowPane.getComponent(elementId);
        if (windowPane) {
            windowPane.dispose();
        }
    },
    
    processUpdate: function(updateElement) {
        var elementId = updateElement.getAttribute("eid");
        var windowPane = EchoWindowPane.getComponent(elementId);
        if (!windowPane) {
            throw new Error("No WindowPane with id: " + elementId);
        }
        EchoWindowPane.MessageProcessor.loadProperties(updateElement, windowPane);
        windowPane.redraw();
        windowPane.redrawTitle();
    },
    
    /**
     * Processes an <code>init</code> message to initialize the state of a 
     * WindowPane that is being added.
     *
     * @param initMessageElement the <code>init</code> element to process
     */
    processInit: function(initElement) {
        var elementId = initElement.getAttribute("eid");
        var containerElementId = initElement.getAttribute("container-eid");
        
        var windowPane = new EchoWindowPane(elementId, containerElementId);
        
        EchoWindowPane.MessageProcessor.loadProperties(initElement, windowPane);
        
        windowPane.enabled = initElement.getAttribute("enabled") != "false";
    
        windowPane.closable = initElement.getAttribute("closable") == "true";
        windowPane.movable = initElement.getAttribute("movable") == "true";
        windowPane.resizable = initElement.getAttribute("resizable") == "true";
        if (initElement.getAttribute("z-index")) {
            windowPane.zIndex = initElement.getAttribute("z-index");
        }
        if (initElement.getAttribute("background")) {
            windowPane.background = initElement.getAttribute("background");
        }
        if (initElement.getAttribute("background-image")) {
            windowPane.backgroundImage = initElement.getAttribute("background-image");
        }
        if (initElement.getAttribute("close-icon")) {
            windowPane.closeIcon = initElement.getAttribute("close-icon");
        }
        if (initElement.getAttribute("close-icon-insets")) {
            windowPane.closeIconInsets = initElement.getAttribute("close-icon-insets");
        }
        if (initElement.getAttribute("font")) {
            windowPane.font = initElement.getAttribute("font");
        }
        if (initElement.getAttribute("foreground")) {
            windowPane.foreground = initElement.getAttribute("foreground");
        }
        if (initElement.getAttribute("icon")) {
            windowPane.icon = initElement.getAttribute("icon");
        }
        if (initElement.getAttribute("icon-insets")) {
            windowPane.iconInsets = initElement.getAttribute("icon-insets");
        }
        if (initElement.getAttribute("insets")) {
            windowPane.insets = initElement.getAttribute("insets");
        }
        if (initElement.getAttribute("maximum-height")) {
            windowPane.maximumHeight = parseInt(initElement.getAttribute("maximum-height"));
        }
        if (initElement.getAttribute("maximum-width")) {
            windowPane.maximumWidth = parseInt(initElement.getAttribute("maximum-width"));
        }
        if (initElement.getAttribute("minimum-height")) {             
            windowPane.minimumHeight = parseInt(initElement.getAttribute("minimum-height"));
        }
        if (initElement.getAttribute("minimum-width")) {
            windowPane.minimumWidth = parseInt(initElement.getAttribute("minimum-width"));
        }
        if (initElement.getAttribute("title")) {
            windowPane.title = initElement.getAttribute("title");
        }
        if (initElement.getAttribute("title-background")) {
            windowPane.titleBackground = initElement.getAttribute("title-background");
        }
        if (initElement.getAttribute("title-background-image")) {
            windowPane.titleBackgroundImage = initElement.getAttribute("title-background-image");
        }
        if (initElement.getAttribute("title-bar-insets")) {
            windowPane.titleBarInsets = new EchoCoreProperties.Insets(initElement.getAttribute("title-bar-insets"));
        }
        if (initElement.getAttribute("title-font")) {
            windowPane.titleFont = initElement.getAttribute("title-font");
        }
        if (initElement.getAttribute("title-foreground")) {
            windowPane.titleForeground = initElement.getAttribute("title-foreground");
        }
        if (initElement.getAttribute("title-height")) {
            windowPane.titleHeight = parseInt(initElement.getAttribute("title-height"));
        }
        if (initElement.getAttribute("title-insets")) {
            windowPane.titleInsets = initElement.getAttribute("title-insets");
        }
        
        var borderElements = initElement.getElementsByTagName("border");
        if (borderElements.length != 0) {
            var borderElement = borderElements[0];
            var color = borderElement.getAttribute("color");
            var borderInsets = new EchoCoreProperties.Insets(borderElement.getAttribute("border-insets"));
            var contentInsets = new EchoCoreProperties.Insets(borderElement.getAttribute("content-insets"));
            var imageElements = borderElement.childNodes;
            var images = new Array(8);
            var index;
            for (var i = 0; i < imageElements.length; ++i) {
                if (imageElements[i].nodeName != "image") {
                    continue;
                }
                switch(imageElements[i].getAttribute("name")) {
                case "tl": index = EchoCoreProperties.FillImageBorder.IMAGE_TL; break;
                case "t":  index = EchoCoreProperties.FillImageBorder.IMAGE_T;  break;
                case "tr": index = EchoCoreProperties.FillImageBorder.IMAGE_TR; break;
                case "l":  index = EchoCoreProperties.FillImageBorder.IMAGE_L;  break;
                case "r":  index = EchoCoreProperties.FillImageBorder.IMAGE_R;  break;
                case "bl": index = EchoCoreProperties.FillImageBorder.IMAGE_BL; break;
                case "b":  index = EchoCoreProperties.FillImageBorder.IMAGE_B;  break;
                case "br": index = EchoCoreProperties.FillImageBorder.IMAGE_BR; break;
                }
                images[index] = imageElements[i].getAttribute("value");
            }
            windowPane.border = new EchoCoreProperties.FillImageBorder(color, borderInsets, contentInsets, images);
        }
        
        windowPane.create();
    }
};

/**
 * Static object/namespace to manage z-index ordering of multiple WindowPanes
 * with the same parent component.
 */
EchoWindowPane.ZIndexManager = {

    /**
     * Associative array mapping container ids to arrays of element ids.
     */
    containerIdToElementIdArrayMap: new EchoCollectionsMap(),
    
    /**
     * Adds a WindowPane to be managed by the ZIndexManager.
     *
     * @param containerId the id of the Element containing the WindowPane
     * @param elementId the id of the WindowPane
     */
    add: function(containerId, elementId) {
        var elementIdArray = EchoWindowPane.ZIndexManager.containerIdToElementIdArrayMap.get(containerId);
        if (!elementIdArray) {
            elementIdArray = [];
            EchoWindowPane.ZIndexManager.containerIdToElementIdArrayMap.put(containerId, elementIdArray);
        }
        var containsElement = false;
        for (var i = 0; i < elementIdArray.length; ++i) {
            if (elementIdArray[i] == elementId) {
                // Do nothing if re-rendering.
                return;
            }
        }
        elementIdArray.push(elementId);
    },
    
    /**
     * Raises a WindowPane being managed by the ZIndexManager to the above all 
     * other WindowPanes within its container.
     *
     * @param containerId the id of the Element containing the WindowPane
     * @param elementId the id of the WindowPane
     */
    raise: function(containerId, elementId) {
        var windowElement = document.getElementById(elementId);
    
        var elementIdArray = EchoWindowPane.ZIndexManager.containerIdToElementIdArrayMap.get(containerId);
        if (!elementIdArray) {
            throw new Error("Invalid container id.");
        }
    
        var raiseIndex = 1;
        
        for (var i = 0; i < elementIdArray.length; ++i) {
            if (elementIdArray[i] != elementId) {
                var testWindowElement = document.getElementById(elementIdArray[i]);
                var zIndex = parseInt(testWindowElement.style.zIndex);
                if (raiseIndex <= zIndex) {
                    raiseIndex = zIndex + 1;
                }
            }
        }
    
        windowElement.style.zIndex = raiseIndex;
        
        return raiseIndex;
    },
    
    /**
     * Removes a WindowPane from being managed by the ZIndexManager.
     *
     * @param containerId the id of the Element containing the WindowPane
     * @param elementId the id of the WindowPane
     */
    remove: function(containerId, elementId) {
        var elementIdArray = EchoWindowPane.ZIndexManager.containerIdToElementIdArrayMap.get(containerId);
        if (!elementIdArray) {
            throw new Error("ZIndexManager.remove: no data for container with id \"" + containerId + "\".");
        }
        for (var i = 0; i < elementIdArray.length; ++i) {
            if (elementIdArray[i] == elementId) {
                if (elementIdArray.length == 1) {
                    EchoWindowPane.ZIndexManager.containerIdToElementIdArrayMap.remove(containerId);
                } else {
                    if (i < elementIdArray.length - 1) {
                        elementIdArray[i] = elementIdArray[elementIdArray.length - 1];
                    }
                    --elementIdArray.length;
                }
                return;
            }
        }
        throw new Error("ZIndexManager.remove: Element with id \"" + elementId + 
                "\" does not exist in container with id \"" + containerId + "\".");
    }
};    
