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

/**
 * Id suffixes of border elements.
 */
EchoWindowPane.BORDER_ELEMENT_ID_SUFFIXES = new Array("_border_tl", "_border_t", "_border_tr",
        "_border_l", "_border_r", "_border_bl", "_border_b", "_border_br");

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
    var windowPaneDivElement = document.createElement("div");
    windowPaneDivElement.id = this.elementId;
    windowPaneDivElement.style.position = "absolute";
    windowPaneDivElement.style.zIndex = "1";
    
    if (this.positionX == null) {
        this.positionX = Math.round((containerElement.offsetWidth - this.width) / 2);
        if (this.positionX < 0) {
            this.positionX = 0;
        }
    }
    if (this.positionY == null) {
        this.positionY = Math.round((this.getContainerHeight() - this.height) / 2);
        if (this.positionY < 0) {
            this.positionY = 0;
        }
    }
    windowPaneDivElement.style.left = this.positionX + "px";
    windowPaneDivElement.style.top = this.positionY + "px";
    windowPaneDivElement.style.width = this.width + "px";
    windowPaneDivElement.style.height = this.height + "px";
    
    var borderSideWidth = this.width - this.border.borderInsets.left - this.border.borderInsets.right;
    var borderSideHeight = this.height - this.border.borderInsets.top - this.border.borderInsets.bottom;
    
    // Render top row
    if (this.border.borderInsets.top > 0) {
        // Render top left corner
        if (this.border.borderInsets.left > 0) {
            var borderTLDivElement = document.createElement("div");
            borderTLDivElement.id = this.elementId + "_border_tl";
            borderTLDivElement.style.position = "absolute";
            borderTLDivElement.style.left = "0px";
            borderTLDivElement.style.top = "0px";
            borderTLDivElement.style.width = this.border.borderInsets.left + "px";
            borderTLDivElement.style.height = this.border.borderInsets.top + "px";
            if (this.border.color != null) {
                borderTLDivElement.style.backgroundColor = this.border.color;
            }
            if (this.resizable) {
                borderTLDivElement.style.cursor = "nw-resize";
            }
            if (this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_TL]) {
                EchoCssUtil.applyStyle(borderTLDivElement, 
                        this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_TL]);
            }
            windowPaneDivElement.appendChild(borderTLDivElement);
        }
        
        // Render top side
        var borderTDivElement = document.createElement("div");
        borderTDivElement.id = this.elementId + "_border_t";
        borderTDivElement.style.position = "absolute";
        borderTDivElement.style.left = this.border.borderInsets.left + "px";
        borderTDivElement.style.top = "0px";
        borderTDivElement.style.width = borderSideWidth + "px";
        borderTDivElement.style.height = this.border.borderInsets.top + "px";
        if (this.border.color != null) {
            borderTDivElement.style.backgroundColor = this.border.color;
        }
        if (this.resizable) {
            borderTDivElement.style.cursor = "n-resize";
        }
        if (this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_T]) {
            EchoCssUtil.applyStyle(borderTDivElement, 
                    this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_T]);
        }
        windowPaneDivElement.appendChild(borderTDivElement);

        // Render top right corner
        if (this.border.borderInsets.right > 0) {
            var borderTRDivElement = document.createElement("div");
            borderTRDivElement.id = this.elementId + "_border_tr";
            borderTRDivElement.style.position = "absolute";
            borderTRDivElement.style.right = "0px";
            borderTRDivElement.style.top = "0px";
            borderTRDivElement.style.width = this.border.borderInsets.right + "px";
            borderTRDivElement.style.height = this.border.borderInsets.top + "px";
            if (this.border.color != null) {
                borderTRDivElement.style.backgroundColor = this.border.color;
            }
            if (this.resizable) {
                borderTRDivElement.style.cursor = "ne-resize";
            }
            if (this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_TR]) {
                EchoCssUtil.applyStyle(borderTRDivElement,
                        this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_TR]);
            }
            windowPaneDivElement.appendChild(borderTRDivElement);
        }
    }
    
    // Render left side
    if (this.border.borderInsets.left > 0) {
        // Render top side
        var borderLDivElement = document.createElement("div");
        borderLDivElement.id = this.elementId + "_border_l";
        borderLDivElement.style.position = "absolute";
        borderLDivElement.style.left = "0px";
        borderLDivElement.style.top = this.border.borderInsets.top + "px";
        borderLDivElement.style.width = this.border.borderInsets.left + "px";
        borderLDivElement.style.height = borderSideHeight + "px";
        if (this.border.color != null) {
            borderLDivElement.style.backgroundColor = this.border.color;
        }
        if (this.resizable) {
            borderLDivElement.style.cursor = "w-resize";
        }
        if (this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_L]) {
            EchoCssUtil.applyStyle(borderLDivElement,
                    this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_L]);
        }
        windowPaneDivElement.appendChild(borderLDivElement);
    }
    
    // Render right side
    if (this.border.borderInsets.right > 0) {
        // Render top side
        var borderRDivElement = document.createElement("div");
        borderRDivElement.id = this.elementId + "_border_r";
        borderRDivElement.style.position = "absolute";
        borderRDivElement.style.right = "0px";
        borderRDivElement.style.top = this.border.borderInsets.top + "px";
        borderRDivElement.style.width = this.border.borderInsets.right + "px";
        borderRDivElement.style.height = borderSideHeight + "px";
        if (this.border.color != null) {
            borderRDivElement.style.backgroundColor = this.border.color;
        }
        if (this.resizable) {
            borderRDivElement.style.cursor = "e-resize";
        }
        if (this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_R]) {
            EchoCssUtil.applyStyle(borderRDivElement,
                    this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_R]);
        }
        windowPaneDivElement.appendChild(borderRDivElement);
    }
    
    // Render bottom row
    if (this.border.borderInsets.bottom > 0) {
        // Render bottom left corner
        if (this.border.borderInsets.left > 0) {
            var borderBLDivElement = document.createElement("div");
            borderBLDivElement.id = this.elementId + "_border_bl";
            borderBLDivElement.style.position = "absolute";
            borderBLDivElement.style.left = "0px";
            borderBLDivElement.style.bottom = "0px";
            borderBLDivElement.style.width = this.border.borderInsets.left + "px";
            borderBLDivElement.style.height = this.border.borderInsets.bottom + "px";
            if (this.border.color != null) {
                borderBLDivElement.style.backgroundColor = this.border.color;
            }
            if (this.resizable) {
                borderBLDivElement.style.cursor = "sw-resize";
            }
            if (this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_BL]) {
                EchoCssUtil.applyStyle(borderBLDivElement, 
                        this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_BL]);
            }
            windowPaneDivElement.appendChild(borderBLDivElement);
        }

        // Render bottom side
        var borderBDivElement = document.createElement("div");
        borderBDivElement.id = this.elementId + "_border_b";
        borderBDivElement.style.position = "absolute";
        borderBDivElement.style.left = this.border.borderInsets.left + "px";
        borderBDivElement.style.bottom = "0px";
        borderBDivElement.style.width = borderSideWidth + "px";
        borderBDivElement.style.height = this.border.borderInsets.bottom + "px";
        if (this.border.color != null) {
            borderBDivElement.style.backgroundColor = this.border.color;
        }
        if (this.resizable) {
            borderBDivElement.style.cursor = "s-resize";
        }
        if (this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_B]) {
            EchoCssUtil.applyStyle(borderBDivElement, 
                    this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_B]);
        }
        windowPaneDivElement.appendChild(borderBDivElement);
        
        // Render bottom right corner
        if (this.border.borderInsets.right > 0) {
            var borderBRDivElement = document.createElement("div");
            borderBRDivElement.id = this.elementId + "_border_br";
            borderBRDivElement.style.position = "absolute";
            borderBRDivElement.style.right = "0px";
            borderBRDivElement.style.bottom = "0px";
            borderBRDivElement.style.width = this.border.borderInsets.right + "px";
            borderBRDivElement.style.height = this.border.borderInsets.bottom + "px";
            if (this.border.color != null) {
                borderBRDivElement.style.backgroundColor = this.border.color;
            }
            if (this.resizable) {
                borderBRDivElement.style.cursor = "se-resize";
            }
            if (this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_BR]) {
                EchoCssUtil.applyStyle(borderBRDivElement, 
                        this.border.fillImages[EchoCoreProperties.FillImageBorder.IMAGE_BR]);
            }
            windowPaneDivElement.appendChild(borderBRDivElement);
        }
    }
    
    // Render Title Bar
    var titleBarDivElement = document.createElement("div");
    titleBarDivElement.id = this.elementId + "_titlebar";
    titleBarDivElement.style.position = "absolute";
    titleBarDivElement.style.zIndex = 2;
    titleBarDivElement.style.backgroundColor = this.titleBackground;
    if (this.titleBackgroundImage) {
       EchoCssUtil.applyStyle(titleBarDivElement, this.titleBackgroundImage);
    }
    titleBarDivElement.style.color = this.titleForeground;
    titleBarDivElement.style.top = this.border.contentInsets.top + "px";
    titleBarDivElement.style.left = this.border.contentInsets.left + "px";
    titleBarDivElement.style.width = (this.width - this.border.contentInsets.left - this.border.contentInsets.right) + "px";
    titleBarDivElement.style.height = this.titleHeight + "px";
    titleBarDivElement.style.overflow = "hidden";
    if (this.movable) {
        titleBarDivElement.style.cursor = "move";
    }
    
    if (this.icon) {
        var titleIconDivElement = document.createElement("div");
        titleIconDivElement.style.position = "absolute";
        if (this.iconInsets != null) {
            titleIconDivElement.style.padding = this.iconInsets;
        }
        titleBarDivElement.appendChild(titleIconDivElement);
        var iconImgElement = document.createElement("img");
        iconImgElement.setAttribute("src", this.icon);
        titleIconDivElement.appendChild(iconImgElement);
    }
    
    if (this.title) {
        var titleTextDivElement = document.createElement("div");
        titleTextDivElement.id = this.elementId + "_titletext";
        titleTextDivElement.style.position = "absolute";
        if (this.icon) {
            titleTextDivElement.style.left = "32px";
        }
        titleTextDivElement.style.whiteSpace = "nowrap";
        if (this.titleInsets != null) {
            titleTextDivElement.style.padding = this.titleInsets;
        }
        titleTextDivElement.appendChild(document.createTextNode(this.title));
        titleBarDivElement.appendChild(titleTextDivElement);
    }

    if (this.closable) {
        var closeDivElement = document.createElement("div");
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
    contentDivElement.style.zIndex = 1;
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
    
    containerElement.appendChild(windowPaneDivElement);

    EchoDomPropertyStore.setPropertyValue(this.elementId, "component", this);
    
    if (this.movable) {
        EchoEventProcessor.addHandler(this.elementId + "_titlebar", "mousedown", 
                "EchoWindowPane.processTitleBarMouseDown");
    }

    if (this.resizable) {
        for (var i = 0; i < EchoWindowPane.BORDER_ELEMENT_ID_SUFFIXES.length; ++i) {
            var borderElementId = this.elementId + EchoWindowPane.BORDER_ELEMENT_ID_SUFFIXES[i];
            EchoEventProcessor.addHandler(borderElementId, "mousedown", "EchoWindowPane.processBorderMouseDown");
        }
    }
    
    if (this.closable) {
        // MouseDown event handler is added to avoid initiating a title-bar drag when close button is clicked.
        EchoEventProcessor.addHandler(closeDivElement.id, "mousedown", "EchoWindowPane.nullEventHandler");
        EchoEventProcessor.addHandler(closeDivElement.id, "click", "EchoWindowPane.processClose");
    }

    //BUGBUG.
    if (EchoClientProperties.get("browserInternetExplorer")) {
        EchoDomUtil.addEventListener(document, "selectstart", EchoWindowPane.selectStart, false);
    }

    EchoWindowPane.ZIndexManager.add(this.containerComponentElementId, this.elementId);
};

EchoWindowPane.prototype.dispose = function() {
    EchoDomUtil.removeEventListener(document, "mousemove", EchoWindowPane.processTitleBarMouseMove);
    EchoDomUtil.removeEventListener(document, "mouseup", EchoWindowPane.processTitleBarMouseUp);
    EchoDomUtil.removeEventListener(document, "mousemove", EchoWindowPane.processBorderMouseMove);
    EchoDomUtil.removeEventListener(document, "mouseup", EchoWindowPane.processBorderMouseUp);
    EchoEventProcessor.removeHandler(this.elementId + "_close", "mousedown");
    EchoEventProcessor.removeHandler(this.elementId + "_close", "click");
    EchoEventProcessor.removeHandler(this.elementId + "_titlebar", "mousedown");
    for (var i = 0; i < EchoWindowPane.BORDER_ELEMENT_ID_SUFFIXES.length; ++i) {
        var borderElementId = this.elementId + EchoWindowPane.BORDER_ELEMENT_ID_SUFFIXES[i];
        EchoEventProcessor.removeHandler(borderElementId, "mousedown");
    }
    if (EchoClientProperties.get("browserIntenetExplorer")) {
        EchoDomUtil.removeEventListener(document, "selectstart", EchoWindowPane.selectStart, false);
    }
    
    EchoWindowPane.ZIndexManager.remove(this.containerComponentElementId, this.elementId);
};

EchoWindowPane.prototype.getContainerHeight = function() {
    var containerElement = document.getElementById(this.containerElementId);
    var height = containerElement.offsetHeight;
    if (height == 0) {
        height = containerElement.parentNode.offsetHeight;
    }
    return height;
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
    this.raise();
    EchoWindowPane.activeInstance = this;
    var windowPaneDivElement = document.getElementById(this.elementId);
    this.dragInitPositionX = this.positionX;
    this.dragInitPositionY = this.positionY;
    this.dragOriginX = echoEvent.clientX;
    this.dragOriginY = echoEvent.clientY;
    EchoDomUtil.addEventListener(document, "mousemove", EchoWindowPane.processTitleBarMouseMove);
    EchoDomUtil.addEventListener(document, "mouseup", EchoWindowPane.processTitleBarMouseUp);
};

EchoWindowPane.prototype.processTitleBarMouseMove = function(e) {
    this.setPosition(this.dragInitPositionX + e.clientX - this.dragOriginX,
            this.dragInitPositionY + e.clientY - this.dragOriginY);
    this.redraw();
};

EchoWindowPane.prototype.processTitleBarMouseUp = function(e) {
    EchoDomUtil.removeEventListener(document, "mousemove", EchoWindowPane.processTitleBarMouseMove);
    EchoDomUtil.removeEventListener(document, "mouseup", EchoWindowPane.processTitleBarMouseUp);
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

    titleBarDivElement.style.width = (this.width - this.border.contentInsets.left - this.border.contentInsets.right) + "px";

    borderTDivElement.style.width = borderSideWidth + "px";
    borderBDivElement.style.width = borderSideWidth + "px";
    borderLDivElement.style.height = borderSideHeight + "px";
    borderRDivElement.style.height = borderSideHeight + "px";
    
    var contentElement = document.getElementById(this.elementId + "_content");
    
    EchoVirtualPosition.redraw(contentElement);
}

EchoWindowPane.prototype.setPosition = function(positionX, positionY) {
    var windowPaneDivElement = document.getElementById(this.elementId);

    if (positionX < 0) {
        positionX = 0;
    } else if (positionX > windowPaneDivElement.parentNode.offsetWidth - this.width) {
        positionX = windowPaneDivElement.parentNode.offsetWidth - this.width;
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
 * Returns the WindowPane data object instance based on the root element id
 * of the WindowPane.
 *
 * @param componentId the root element id of the WindowPane
 * @return the relevant WindowPane instance
 */
EchoWindowPane.getComponent = function(componentId) {
    return EchoDomPropertyStore.getPropertyValue(componentId, "component");
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
