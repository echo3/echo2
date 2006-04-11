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

EchoWindowPane = function(elementId, containerElementId) {
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
    this.titleBackground = EchoWindowPane.DEFAULT_TITLE_BACKGROUND;
    this.titleBackgroundImage = null;
    this.titleFont = null;
    this.titleForeground = EchoWindowPane.DEFAULT_TITLE_FOREGROUND;
    this.titleHeight = EchoWindowPane.DEFAULT_TITLE_HEIGHT;
    this.titleInsets = EchoWindowPane.DEFAULT_TITLE_INSETS;
    this.width = EchoWindowPane.DEFAULT_WIDTH;
};

EchoWindowPane.activeInstance = null;

EchoWindowPane.DEFAULT_CLOSE_ICON_INSETS = "4px";
EchoWindowPane.DEFAULT_ICON_INSETS = "4px";
EchoWindowPane.DEFAULT_TITLE_INSETS = "4px";
EchoWindowPane.DEFAULT_WIDTH = 400;
EchoWindowPane.DEFAULT_BACKGROUND = "#ffffff";
EchoWindowPane.DEFAULT_TITLE_BACKGROUND = "#005faf";
EchoWindowPane.DEFAULT_TITLE_FOREGROUND = "#ffffff";
EchoWindowPane.DEFAULT_TITLE_HEIGHT = 28;
EchoWindowPane.DEFAULT_HEIGHT = 300;
EchoWindowPane.DEFAULT_BORDER = new EchoCoreProperties.FillImageBorder("#00007f", new EchoCoreProperties.Insets(20), 
        new EchoCoreProperties.Insets(3));

EchoWindowPane.prototype.create = function() {
    var containerElement = document.getElementById(this.containerElementId);
    
    this.windowPaneDivElement = document.createElement("div");
    this.windowPaneDivElement.id = this.elementId;
    this.windowPaneDivElement.style.position = "absolute";
    this.windowPaneDivElement.style.zIndex = "1";
    
    var containerWidth = this.getContainerWidth();
    var containerHeight = this.getContainerHeight();
    
    if (this.positionX == null) {
        if (containerWidth && containerHeight) {
            // Only center window if valid data exist for container width and height.
	        this.positionX = Math.round((containerWidth - this.width) / 2);
	        if (this.positionX < 0) {
	            this.positionX = 0;
	        }
        } else {
            this.positionX = 0;
        }
    }
    if (this.positionY == null) {
        if (containerWidth && containerHeight) {
            // Only center window if valid data exist for container width and height.
	        this.positionY = Math.round((containerHeight - this.height) / 2);
	        if (this.positionY < 0) {
	            this.positionY = 0;
	        }
        } else {
            this.positionY = 0;
        }
    }
    this.windowPaneDivElement.style.left = this.positionX + "px";
    this.windowPaneDivElement.style.top = this.positionY + "px";
    this.windowPaneDivElement.style.width = this.width + "px";
    this.windowPaneDivElement.style.height = this.height + "px";
    
    var borderSideWidth = this.width - this.border.borderInsets.left - this.border.borderInsets.right;
    var borderSideHeight = this.height - this.border.borderInsets.top - this.border.borderInsets.bottom;
    
    // Render top row
    if (this.border.borderInsets.top > 0) {
        // Render top left corner
        if (this.border.borderInsets.left > 0) {
            this.borderTLDivElement = document.createElement("div");
            this.borderTLDivElement.id = this.elementId + "_border_tl";
            this.borderTLDivElement.style.position = "absolute";
            this.borderTLDivElement.style.left = "0px";
            this.borderTLDivElement.style.top = "0px";
            this.borderTLDivElement.style.width = this.border.borderInsets.left + "px";
            this.borderTLDivElement.style.height = this.border.borderInsets.top + "px";
            if (this.border.color != null) {
                this.borderTLDivElement.style.backgroundColor = this.border.color;
            }
            if (this.resizable) {
                this.borderTLDivElement.style.cursor = "nw-resize";
            }
            if (this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_TL]) {
                EchoCssUtil.applyStyle(this.borderTLDivElement, 
                        this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_TL]);
            }
            this.windowPaneDivElement.appendChild(this.borderTLDivElement);
        }
        
        // Render top side
        this.borderTDivElement = document.createElement("div");
        this.borderTDivElement.id = this.elementId + "_border_t";
        this.borderTDivElement.style.position = "absolute";
        this.borderTDivElement.style.left = this.border.borderInsets.left + "px";
        this.borderTDivElement.style.top = "0px";
        this.borderTDivElement.style.width = borderSideWidth + "px";
        this.borderTDivElement.style.height = this.border.borderInsets.top + "px";
        if (this.border.color != null) {
            this.borderTDivElement.style.backgroundColor = this.border.color;
        }
        if (this.resizable) {
            this.borderTDivElement.style.cursor = "n-resize";
        }
        if (this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_T]) {
            EchoCssUtil.applyStyle(this.borderTDivElement, 
                    this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_T]);
        }
        this.windowPaneDivElement.appendChild(this.borderTDivElement);

        // Render top right corner
        if (this.border.borderInsets.right > 0) {
            this.borderTRDivElement = document.createElement("div");
            this.borderTRDivElement.id = this.elementId + "_border_tr";
            this.borderTRDivElement.style.position = "absolute";
            this.borderTRDivElement.style.right = "0px";
            this.borderTRDivElement.style.top = "0px";
            this.borderTRDivElement.style.width = this.border.borderInsets.right + "px";
            this.borderTRDivElement.style.height = this.border.borderInsets.top + "px";
            if (this.border.color != null) {
                this.borderTRDivElement.style.backgroundColor = this.border.color;
            }
            if (this.resizable) {
                this.borderTRDivElement.style.cursor = "ne-resize";
            }
            if (this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_TR]) {
                EchoCssUtil.applyStyle(this.borderTRDivElement,
                        this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_TR]);
            }
            this.windowPaneDivElement.appendChild(this.borderTRDivElement);
        }
    }
    
    // Render left side
    if (this.border.borderInsets.left > 0) {
        this.borderLDivElement = document.createElement("div");
        this.borderLDivElement.id = this.elementId + "_border_l";
        this.borderLDivElement.style.position = "absolute";
        this.borderLDivElement.style.left = "0px";
        this.borderLDivElement.style.top = this.border.borderInsets.top + "px";
        this.borderLDivElement.style.width = this.border.borderInsets.left + "px";
        this.borderLDivElement.style.height = borderSideHeight + "px";
        if (this.border.color != null) {
            this.borderLDivElement.style.backgroundColor = this.border.color;
        }
        if (this.resizable) {
            this.borderLDivElement.style.cursor = "w-resize";
        }
        if (this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_L]) {
            EchoCssUtil.applyStyle(this.borderLDivElement,
                    this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_L]);
        }
        this.windowPaneDivElement.appendChild(this.borderLDivElement);
    }
    
    // Render right side
    if (this.border.borderInsets.right > 0) {
        this.borderRDivElement = document.createElement("div");
        this.borderRDivElement.id = this.elementId + "_border_r";
        this.borderRDivElement.style.position = "absolute";
        this.borderRDivElement.style.right = "0px";
        this.borderRDivElement.style.top = this.border.borderInsets.top + "px";
        this.borderRDivElement.style.width = this.border.borderInsets.right + "px";
        this.borderRDivElement.style.height = borderSideHeight + "px";
        if (this.border.color != null) {
            this.borderRDivElement.style.backgroundColor = this.border.color;
        }
        if (this.resizable) {
            this.borderRDivElement.style.cursor = "e-resize";
        }
        if (this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_R]) {
            EchoCssUtil.applyStyle(this.borderRDivElement,
                    this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_R]);
        }
        this.windowPaneDivElement.appendChild(this.borderRDivElement);
    }
    
    // Render bottom row
    if (this.border.borderInsets.bottom > 0) {
        // Render bottom left corner
        if (this.border.borderInsets.left > 0) {
            this.borderBLDivElement = document.createElement("div");
            this.borderBLDivElement.id = this.elementId + "_border_bl";
            this.borderBLDivElement.style.position = "absolute";
            this.borderBLDivElement.style.left = "0px";
            this.borderBLDivElement.style.bottom = "0px";
            this.borderBLDivElement.style.width = this.border.borderInsets.left + "px";
            this.borderBLDivElement.style.height = this.border.borderInsets.bottom + "px";
            if (this.border.color != null) {
                this.borderBLDivElement.style.backgroundColor = this.border.color;
            }
            if (this.resizable) {
                this.borderBLDivElement.style.cursor = "sw-resize";
            }
            if (this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_BL]) {
                EchoCssUtil.applyStyle(this.borderBLDivElement, 
                        this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_BL]);
            }
            this.windowPaneDivElement.appendChild(this.borderBLDivElement);
        }

        // Render bottom side
        this.borderBDivElement = document.createElement("div");
        this.borderBDivElement.id = this.elementId + "_border_b";
        this.borderBDivElement.style.position = "absolute";
        this.borderBDivElement.style.left = this.border.borderInsets.left + "px";
        this.borderBDivElement.style.bottom = "0px";
        this.borderBDivElement.style.width = borderSideWidth + "px";
        this.borderBDivElement.style.height = this.border.borderInsets.bottom + "px";
        if (this.border.color != null) {
            this.borderBDivElement.style.backgroundColor = this.border.color;
        }
        if (this.resizable) {
            this.borderBDivElement.style.cursor = "s-resize";
        }
        if (this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_B]) {
            EchoCssUtil.applyStyle(this.borderBDivElement, 
                    this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_B]);
        }
        this.windowPaneDivElement.appendChild(this.borderBDivElement);
        
        // Render bottom right corner
        if (this.border.borderInsets.right > 0) {
            this.borderBRDivElement = document.createElement("div");
            this.borderBRDivElement.id = this.elementId + "_border_br";
            this.borderBRDivElement.style.position = "absolute";
            this.borderBRDivElement.style.right = "0px";
            this.borderBRDivElement.style.bottom = "0px";
            this.borderBRDivElement.style.width = this.border.borderInsets.right + "px";
            this.borderBRDivElement.style.height = this.border.borderInsets.bottom + "px";
            if (this.border.color != null) {
                this.borderBRDivElement.style.backgroundColor = this.border.color;
            }
            if (this.resizable) {
                this.borderBRDivElement.style.cursor = "se-resize";
            }
            if (this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_BR]) {
                EchoCssUtil.applyStyle(this.borderBRDivElement, 
                        this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_BR]);
            }
            this.windowPaneDivElement.appendChild(this.borderBRDivElement);
        }
    }
    
    // Render Title Bar
    this.titleBarDivElement = document.createElement("div");
    this.titleBarDivElement.id = this.elementId + "_titlebar";
    this.titleBarDivElement.style.position = "absolute";
    this.titleBarDivElement.style.zIndex = 3;
    this.titleBarDivElement.style.backgroundColor = this.titleBackground;
    if (this.titleBackgroundImage) {
       EchoCssUtil.applyStyle(this.titleBarDivElement, this.titleBackgroundImage);
    }
    this.titleBarDivElement.style.color = this.titleForeground;
    this.titleBarDivElement.style.top = this.border.contentInsets.top + "px";
    this.titleBarDivElement.style.left = this.border.contentInsets.left + "px";
    this.titleBarDivElement.style.width = (this.width - this.border.contentInsets.left - this.border.contentInsets.right) + "px";
    this.titleBarDivElement.style.height = this.titleHeight + "px";
    this.titleBarDivElement.style.overflow = "hidden";
    if (this.movable) {
        this.titleBarDivElement.style.cursor = "move";
    }
    
    if (this.icon) {
        var titleIconDivElement = document.createElement("div");
        titleIconDivElement.style.position = "absolute";
        titleIconDivElement.style.left = "0px";
        if (this.iconInsets != null) {
            titleIconDivElement.style.padding = this.iconInsets;
        }
        this.titleBarDivElement.appendChild(titleIconDivElement);
        var iconImgElement = document.createElement("img");
        iconImgElement.setAttribute("src", this.icon);
        titleIconDivElement.appendChild(iconImgElement);
    }
    
    if (this.title) {
        var titleTextDivElement = document.createElement("div");
        titleTextDivElement.id = this.elementId + "_titletext";
        titleTextDivElement.style.position = "absolute";
        titleTextDivElement.style.left = "0px";
        titleTextDivElement.style.textAlign = "left";
        if (this.icon) {
            titleTextDivElement.style.left = "32px";
        }
        titleTextDivElement.style.whiteSpace = "nowrap";
        if (this.titleInsets != null) {
            titleTextDivElement.style.padding = this.titleInsets;
        }
        if (this.titleForeground != null) {
            titleTextDivElement.style.color = this.titleForeground;
        }
	    if (this.titleFont) {
	        EchoCssUtil.applyStyle(titleTextDivElement, this.titleFont);
	    }
        titleTextDivElement.appendChild(document.createTextNode(this.title));
        this.titleBarDivElement.appendChild(titleTextDivElement);
    }

    if (this.closable) {
        this.closeDivElement = document.createElement("div");
        this.closeDivElement.id = this.elementId + "_close";
        this.closeDivElement.style.position = "absolute";
        this.closeDivElement.style.right = "0px";
        this.closeDivElement.style.cursor = "pointer";
        if (this.closeIconInsets) {
            this.closeDivElement.style.padding = this.closeIconInsets;
        }
        if (this.closeIcon) {
            var closeImgElement = document.createElement("img");
            closeImgElement.setAttribute("src", this.closeIcon);
            this.closeDivElement.appendChild(closeImgElement);
        } else {
            this.closeDivElement.appendChild(document.createTextNode("[X]"));
        }
        this.titleBarDivElement.appendChild(this.closeDivElement);
    }

    this.windowPaneDivElement.appendChild(this.titleBarDivElement);
    
    // Render Content Area
    
    var contentDivElement = document.createElement("div");
    contentDivElement.id = this.elementId + "_content";
    contentDivElement.style.position = "absolute";
    contentDivElement.style.zIndex = 2;
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
    this.windowPaneDivElement.appendChild(contentDivElement);

    if (EchoClientProperties.get("quirkIESelectZIndex")) {
        // Render Select Field Masking IFRAME.
        var maskDivElement = document.createElement("div");
        maskDivElement.id = this.elementId + "_mask";
        maskDivElement.style.zIndex = 1;
        maskDivElement.style.position = "absolute";
	    maskDivElement.style.top = this.border.contentInsets.top + "px";
	    maskDivElement.style.left = this.border.contentInsets.left + "px";
	    maskDivElement.style.right = this.border.contentInsets.right + "px";
	    maskDivElement.style.bottom = this.border.contentInsets.bottom + "px";
        maskDivElement.style.borderWidth = 0;
        
        var maskIFrameElement = document.createElement("iframe");
        maskIFrameElement.style.width = "100%";
        maskIFrameElement.style.height = "100%";
        maskDivElement.appendChild(maskIFrameElement);
        
	    EchoVirtualPosition.register(maskDivElement.id);
	    this.windowPaneDivElement.appendChild(maskDivElement);
    }
    
    containerElement.appendChild(this.windowPaneDivElement);

    EchoDomPropertyStore.setPropertyValue(this.windowPaneDivElement, "component", this);
    
    if (this.movable) {
        EchoEventProcessor.addHandler(this.titleBarDivElement, "mousedown", 
                "EchoWindowPane.processTitleBarMouseDown");
    }

    if (this.resizable) {
        EchoEventProcessor.addHandler(this.borderTLDivElement, "mousedown", "EchoWindowPane.processBorderMouseDown");
        EchoEventProcessor.addHandler(this.borderTDivElement, "mousedown", "EchoWindowPane.processBorderMouseDown");
        EchoEventProcessor.addHandler(this.borderTRDivElement, "mousedown", "EchoWindowPane.processBorderMouseDown");
        EchoEventProcessor.addHandler(this.borderLDivElement, "mousedown", "EchoWindowPane.processBorderMouseDown");
        EchoEventProcessor.addHandler(this.borderRDivElement, "mousedown", "EchoWindowPane.processBorderMouseDown");
        EchoEventProcessor.addHandler(this.borderBLDivElement, "mousedown", "EchoWindowPane.processBorderMouseDown");
        EchoEventProcessor.addHandler(this.borderBDivElement, "mousedown", "EchoWindowPane.processBorderMouseDown");
        EchoEventProcessor.addHandler(this.borderBRDivElement, "mousedown", "EchoWindowPane.processBorderMouseDown");
    }
    
    if (this.closable) {
        // MouseDown event handler is added to avoid initiating a title-bar drag when close button is clicked.
        EchoEventProcessor.addHandler(this.closeDivElement, "mousedown", "EchoWindowPane.nullEventHandler");
        EchoEventProcessor.addHandler(this.closeDivElement, "click", "EchoWindowPane.processClose");
    }

    EchoWindowPane.ZIndexManager.add(this.containerComponentElementId, this.elementId);
};

EchoWindowPane.prototype.dispose = function() {
    EchoDomUtil.removeEventListener(document, "mousemove", EchoWindowPane.processTitleBarMouseMove);
    EchoDomUtil.removeEventListener(document, "mouseup", EchoWindowPane.processTitleBarMouseUp);
    EchoDomUtil.removeEventListener(document, "mousemove", EchoWindowPane.processBorderMouseMove);
    EchoDomUtil.removeEventListener(document, "mouseup", EchoWindowPane.processBorderMouseUp);

    if (this.movable) {
        EchoEventProcessor.removeHandler(this.titleBarDivElement, "mousedown");
    }
    
    if (this.closable) {
        EchoEventProcessor.removeHandler(this.closeDivElement, "mousedown");
        EchoEventProcessor.removeHandler(this.closeDivElement, "click");
    }

    if (this.resizable) {
        EchoEventProcessor.removeHandler(this.borderTLDivElement, "mousedown", "EchoWindowPane.processBorderMouseDown");
        EchoEventProcessor.removeHandler(this.borderTDivElement, "mousedown", "EchoWindowPane.processBorderMouseDown");
        EchoEventProcessor.removeHandler(this.borderTRDivElement, "mousedown", "EchoWindowPane.processBorderMouseDown");
        EchoEventProcessor.removeHandler(this.borderLDivElement, "mousedown", "EchoWindowPane.processBorderMouseDown");
        EchoEventProcessor.removeHandler(this.borderRDivElement, "mousedown", "EchoWindowPane.processBorderMouseDown");
        EchoEventProcessor.removeHandler(this.borderBLDivElement, "mousedown", "EchoWindowPane.processBorderMouseDown");
        EchoEventProcessor.removeHandler(this.borderBDivElement, "mousedown", "EchoWindowPane.processBorderMouseDown");
        EchoEventProcessor.removeHandler(this.borderBRDivElement, "mousedown", "EchoWindowPane.processBorderMouseDown");
    }

    if (EchoClientProperties.get("browserInternetExplorer")) {
        EchoDomUtil.removeEventListener(document, "selectstart", EchoWindowPane.selectStart, false);
    }
    
    EchoWindowPane.ZIndexManager.remove(this.containerComponentElementId, this.elementId);
    
    EchoDomPropertyStore.dispose(this.windowPaneDivElement);

    this.windowPaneDivElement = undefined;
    this.closeDivElement = undefined;
    this.titleBarDivElement = undefined;
         
    this.borderTLDivElement = undefined;
    this.borderTDivElement = undefined;
    this.borderTRDivElement = undefined;
    this.borderLDivElement = undefined;
    this.borderRDivElement = undefined;
    this.borderBLDivElement = undefined;
    this.borderBDivElement = undefined;
    this.borderBRDivElement = undefined;
};

EchoWindowPane.prototype.getContainerHeight = function() {
    var containerElement = document.getElementById(this.containerElementId);
    var height = containerElement.offsetHeight;
    if (height == 0) {
        height = containerElement.parentNode.offsetHeight;
    }
    return height;
};

EchoWindowPane.prototype.getContainerWidth = function() {
    var containerElement = document.getElementById(this.containerElementId);
    var width = containerElement.offsetWidth;
    if (width == 0) {
        width = containerElement.parentNode.offsetWidth;
    }
    return width;
};

EchoWindowPane.prototype.processBorderMouseDown = function(echoEvent) {
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

    EchoDomUtil.addEventListener(document, "mousemove", EchoWindowPane.processBorderMouseMove);
    EchoDomUtil.addEventListener(document, "mouseup", EchoWindowPane.processBorderMouseUp);
    if (EchoClientProperties.get("browserInternetExplorer")) {
        EchoDomUtil.addEventListener(document, "selectstart", EchoWindowPane.selectStart, false);
    }
};

EchoWindowPane.prototype.processBorderMouseMove = function(e) {
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
};

EchoWindowPane.prototype.processBorderMouseUp = function(e) {
    EchoDomUtil.removeEventListener(document, "mousemove", EchoWindowPane.processBorderMouseMove);
    EchoDomUtil.removeEventListener(document, "mouseup", EchoWindowPane.processBorderMouseUp);
    if (EchoClientProperties.get("browserInternetExplorer")) {
        EchoDomUtil.removeEventListener(document, "selectstart", EchoWindowPane.selectStart, false);
    }
    this.resizingBorderElementId = null;
    EchoWindowPane.activeInstance = null;
    
    EchoClientMessage.setPropertyValue(this.elementId, "positionX", this.positionX + "px");
    EchoClientMessage.setPropertyValue(this.elementId, "positionY", this.positionY + "px");
    EchoClientMessage.setPropertyValue(this.elementId, "width", this.width + "px");
    EchoClientMessage.setPropertyValue(this.elementId, "height", this.height + "px");

    EchoVirtualPosition.redraw();
};

EchoWindowPane.prototype.processClose = function(echoEvent) {
    if (!this.enabled || !EchoClientEngine.verifyInput(this.elementId)) {
        return;
    }
    EchoClientMessage.setActionValue(this.elementId, "close");
    EchoServerTransaction.connect();
};

EchoWindowPane.prototype.processTitleBarMouseDown = function(echoEvent) {
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
    EchoDomUtil.addEventListener(document, "mousemove", EchoWindowPane.processTitleBarMouseMove);
    EchoDomUtil.addEventListener(document, "mouseup", EchoWindowPane.processTitleBarMouseUp);
    if (EchoClientProperties.get("browserInternetExplorer")) {
        EchoDomUtil.addEventListener(document, "selectstart", EchoWindowPane.selectStart, false);
    }
};

EchoWindowPane.prototype.processTitleBarMouseMove = function(e) {
    this.setPosition(this.dragInitPositionX + e.clientX - this.dragOriginX,
            this.dragInitPositionY + e.clientY - this.dragOriginY);
    this.redraw();
};

EchoWindowPane.prototype.processTitleBarMouseUp = function(e) {
    EchoDomUtil.removeEventListener(document, "mousemove", EchoWindowPane.processTitleBarMouseMove);
    EchoDomUtil.removeEventListener(document, "mouseup", EchoWindowPane.processTitleBarMouseUp);
    if (EchoClientProperties.get("browserInternetExplorer")) {
        EchoDomUtil.removeEventListener(document, "selectstart", EchoWindowPane.selectStart, false);
    }
    EchoWindowPane.activeInstance = null;
    
    EchoClientMessage.setPropertyValue(this.elementId, "positionX", this.positionX + "px");
    EchoClientMessage.setPropertyValue(this.elementId, "positionY", this.positionY + "px");
    
    EchoVirtualPosition.redraw();
};

EchoWindowPane.prototype.raise = function() {
    var zIndex = EchoWindowPane.ZIndexManager.raise(this.containerComponentElementId, this.elementId);
    EchoClientMessage.setPropertyValue(this.elementId, "zIndex",  zIndex);
};

EchoWindowPane.prototype.redraw = function() {
    var borderSideWidth = this.width - this.border.borderInsets.left - this.border.borderInsets.right;
    var borderSideHeight = this.height - this.border.borderInsets.top - this.border.borderInsets.bottom;

    this.windowPaneDivElement.style.left = this.positionX + "px";
    this.windowPaneDivElement.style.top = this.positionY + "px";
    this.windowPaneDivElement.style.width = this.width + "px";
    this.windowPaneDivElement.style.height = this.height + "px";

    this.titleBarDivElement.style.width = (this.width - this.border.contentInsets.left - this.border.contentInsets.right) + "px";

    this.borderTDivElement.style.width = borderSideWidth + "px";
    this.borderBDivElement.style.width = borderSideWidth + "px";
    this.borderLDivElement.style.height = borderSideHeight + "px";
    this.borderRDivElement.style.height = borderSideHeight + "px";
    
    var contentElement = document.getElementById(this.elementId + "_content");
    
    EchoVirtualPosition.redraw(contentElement);
    if (EchoClientProperties.get("quirkIESelectZIndex")) {
        var maskDivElement = document.getElementById(this.elementId + "_mask");
        EchoVirtualPosition.redraw(maskDivElement);
    }
}

EchoWindowPane.prototype.setPosition = function(positionX, positionY) {
    if (positionX < 0) {
        positionX = 0;
    } else {
        var containerWidth = this.getContainerWidth();
        if (containerWidth > 0 && positionX > containerWidth - this.width) {
            positionX = containerWidth - this.width;
        }
    }
    if (positionY < 0) {
        positionY = 0;
    } else {
        var containerHeight = this.getContainerHeight();
        if (containerHeight > 0 && positionY > containerHeight - this.height) {
            positionY = containerHeight - this.height;
        }
    }
    

    this.positionX = positionX;
    this.positionY = positionY;
};

EchoWindowPane.prototype.setSize = function(width, height) {
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
};

/**
 * Returns the WindowPane data object instance based on the root element
 * of the WindowPane.
 *
 * @param element the root element or element id of the WindowPane
 * @return the relevant WindowPane instance
 */
EchoWindowPane.getComponent = function(element) {
    return EchoDomPropertyStore.getPropertyValue(element, "component");
};

/**
 * Do-nothing event handler.
 */
EchoWindowPane.nullEventHandler = function(echoEvent) { };

EchoWindowPane.processBorderMouseDown = function(echoEvent) {
    var componentId = EchoDomUtil.getComponentId(echoEvent.registeredTarget.id);
    var windowPane = EchoWindowPane.getComponent(componentId);
    windowPane.processBorderMouseDown(echoEvent);
};

EchoWindowPane.processBorderMouseMove = function(e) {
    e = e ? e : window.event;
    if (EchoWindowPane.activeInstance) {
	    EchoWindowPane.activeInstance.processBorderMouseMove(e);
    }
};

EchoWindowPane.processBorderMouseUp = function(e) {
    e = e ? e : window.event;
    if (EchoWindowPane.activeInstance) {
        EchoWindowPane.activeInstance.processBorderMouseUp(e);
    }
};

EchoWindowPane.processClose = function(echoEvent) { 
    var componentId = EchoDomUtil.getComponentId(echoEvent.registeredTarget.id);
    var windowPane = EchoWindowPane.getComponent(componentId);
    windowPane.processClose(echoEvent);
};

/**
 * Event handler for "SelectStart" events to disable selection while dragging
 * the Window.  (Internet Explorer specific)
 */
EchoWindowPane.selectStart = function() {
    EchoDomUtil.preventEventDefault(window.event);
};

EchoWindowPane.processTitleBarMouseDown = function(echoEvent) {
    var componentId = EchoDomUtil.getComponentId(echoEvent.registeredTarget.id);
    var windowPane = EchoWindowPane.getComponent(componentId);
    windowPane.processTitleBarMouseDown(echoEvent);
};

EchoWindowPane.processTitleBarMouseMove = function(e) {
    e = e ? e : window.event;
    if (EchoWindowPane.activeInstance) {
        EchoWindowPane.activeInstance.processTitleBarMouseMove(e);
    }
};

EchoWindowPane.processTitleBarMouseUp = function(e) {
    e = e ? e : window.event;
    if (EchoWindowPane.activeInstance) {
        EchoWindowPane.activeInstance.processTitleBarMouseUp(e);
    }
};

/**
 * Static object/namespace for WindowPane MessageProcessor 
 * implementation.
 */
EchoWindowPane.MessageProcessor = function() { };

/**
 * MessageProcessor process() implementation 
 * (invoked by ServerMessage processor).
 *
 * @param messagePartElement the <code>message-part</code> element to process
 */
EchoWindowPane.MessageProcessor.process = function(messagePartElement) {
    for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
        if (messagePartElement.childNodes[i].nodeType == 1) {
            switch (messagePartElement.childNodes[i].tagName) {
            case "init":
                EchoWindowPane.MessageProcessor.processInit(messagePartElement.childNodes[i]);
                break;
            case "dispose":
                EchoWindowPane.MessageProcessor.processDispose(messagePartElement.childNodes[i]);
                break;
            }
        }
    }
};

/**
 * Processes a <code>dispose</code> message to finalize the state of a
 * WindowPane that is being removed.
 *
 * @param disposeMessageElement the <code>dispose</code> element to process
 */
EchoWindowPane.MessageProcessor.processDispose = function(disposeElement) {
    var elementId = disposeElement.getAttribute("eid");
    var windowPane = EchoWindowPane.getComponent(elementId);
    if (windowPane) {
        windowPane.dispose();
    }
};

/**
 * Processes an <code>init</code> message to initialize the state of a 
 * WindowPane that is being added.
 *
 * @param initMessageElement the <code>init</code> element to process
 */
EchoWindowPane.MessageProcessor.processInit = function(initElement) {
    var elementId = initElement.getAttribute("eid");
    var containerElementId = initElement.getAttribute("container-eid");
    
    var windowPane = new EchoWindowPane(elementId, containerElementId);
    
    windowPane.enabled = initElement.getAttribute("enabled") != "false";

    windowPane.closable = initElement.getAttribute("closable") == "true";
    windowPane.movable = initElement.getAttribute("movable") == "true";
    windowPane.resizable = initElement.getAttribute("resizable") == "true";
    
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
    if (initElement.getAttribute("height")) {
        windowPane.height = parseInt(initElement.getAttribute("height"));
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
    if (initElement.getAttribute("position-x")) {
        windowPane.positionX = parseInt(initElement.getAttribute("position-x"));
    }
    if (initElement.getAttribute("position-y")) {
        windowPane.positionY = parseInt(initElement.getAttribute("position-y"));
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
    if (initElement.getAttribute("width")) {
        windowPane.width = parseInt(initElement.getAttribute("width"));
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
};

/**
 * Static object/namespace to manage z-index ordering of multiple WindowPanes
 * with the same parent component.
 */
EchoWindowPane.ZIndexManager = function() { };

/**
 * Associative array mapping container ids to arrays of element ids.
 */
EchoWindowPane.ZIndexManager.containerIdToElementIdArrayMap = new EchoCollectionsMap();

/**
 * Adds a WindowPane to be managed by the ZIndexManager.
 *
 * @param containerId the id of the Element containing the WindowPane
 * @param elementId the id of the WindowPane
 */
EchoWindowPane.ZIndexManager.add = function(containerId, elementId) {
    var elementIdArray = EchoWindowPane.ZIndexManager.containerIdToElementIdArrayMap.get(containerId);
    if (!elementIdArray) {
        elementIdArray = new Array();
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
    EchoWindowPane.ZIndexManager.raise(containerId, elementId);
};

/**
 * Raises a WindowPane being managed by the ZIndexManager to the above all 
 * other WindowPanes within its container.
 *
 * @param containerId the id of the Element containing the WindowPane
 * @param elementId the id of the WindowPane
 */
EchoWindowPane.ZIndexManager.raise = function(containerId, elementId) {
    var windowElement = document.getElementById(elementId);

    var elementIdArray = EchoWindowPane.ZIndexManager.containerIdToElementIdArrayMap.get(containerId);
    if (!elementIdArray) {
        throw new Error("Invalid container id.");
    }

    var raiseIndex = 0;
    
    for (var i = 0; i < elementIdArray.length; ++i) {
        var testWindowElement = document.getElementById(elementIdArray[i]);
        var zIndex = parseInt(testWindowElement.style.zIndex);
        if (!isNaN(zIndex) && zIndex >= raiseIndex) {
            if (elementIdArray[i] == elementId) {
                raiseIndex = zIndex;
            } else {
                raiseIndex = zIndex + 1;
            }
        }
    }

    windowElement.style.zIndex = raiseIndex;
    
    return raiseIndex;
};

/**
 * Removes a WindowPane from being managed by the ZIndexManager.
 *
 * @param containerId the id of the Element containing the WindowPane
 * @param elementId the id of the WindowPane
 */
EchoWindowPane.ZIndexManager.remove = function(containerId, elementId) {
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
};
