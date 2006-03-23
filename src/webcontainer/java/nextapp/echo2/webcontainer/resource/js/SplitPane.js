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

EchoSplitPane = function(elementId, containerElementId, orientation, position) {
    this.elementId = elementId;
    this.containerElementId = containerElementId;
    this.orientation = orientation;
    this.position = position;
    
    this.background = null;
    this.foreground = null;
    this.font = null;
    
    this.dragInitMouseOffset = null;
    this.dragInitPosition = position;
    
    this.separatorSize = 4;
    this.separatorColor = "#3f3f4f";
    this.separatorImage = null;
    this.resizable = true;
    
    this.paneData = new Array(new EchoSplitPane.PaneData(), new EchoSplitPane.PaneData());
};

EchoSplitPane.activeInstance = null;

EchoSplitPane.ORIENTATION_VERTICAL_TOP_BOTTOM = 0;
EchoSplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP = 1;
EchoSplitPane.ORIENTATION_HORIZONTAL_LEFT_RIGHT = 2;
EchoSplitPane.ORIENTATION_HORIZONTAL_RIGHT_LEFT = 3;

EchoSplitPane.getPaddingHeight = function(pane) {
    return pane.insets ? pane.insets.top + pane.insets.bottom : 0;
};

EchoSplitPane.getPaddingWidth = function(pane) {
    return pane.insets ? pane.insets.left + pane.insets.right : 0;
};

/**
 * Renders the SplitPane to the DOM, beneath its previously specified
 * container element.
 *
 * Note: When the split pane is destroyed,  the dispose() method must be invoked
 * to release resources allocated by this method.
 */
EchoSplitPane.prototype.create = function() {
    var containerElement = document.getElementById(this.containerElementId);
    var splitPaneDivElement = document.createElement("div");
    splitPaneDivElement.id = this.elementId;
    splitPaneDivElement.style.position = "absolute";
    splitPaneDivElement.style.overflow = "hidden";
    splitPaneDivElement.style.top = "0px"
    splitPaneDivElement.style.bottom = "0px"
    splitPaneDivElement.style.left = "0px"
    splitPaneDivElement.style.right = "0px"
    EchoVirtualPosition.register(splitPaneDivElement.id);
    if (this.background != null) {
        splitPaneDivElement.style.background = this.background;
    }
    if (this.foreground != null) {
        splitPaneDivElement.style.foreground = this.foreground;
    }
    if (this.font != null) {
        EchoCssUtil.applyStyle(splitPaneDivElement, this.font);
    }
    
    var paneDivElements = new Array();
    for (var i = 0; i < 2; ++i) {
        paneDivElements[i] = document.createElement("div");
        paneDivElements[i].id = this.elementId + "_pane" + i;
        paneDivElements[i].style.position = "absolute";
        paneDivElements[i].style.overflow = "auto";
        this.paneData[i].applyStyle(paneDivElements[i]);
    }
    
    var separatorDivElement = null;
    if (this.separatorSize > 0) {
        separatorDivElement = document.createElement("div");
        separatorDivElement.id = this.elementId + "_separator";
        separatorDivElement.style.position = "absolute";
        separatorDivElement.style.backgroundColor = this.separatorColor;
        separatorDivElement.style.fontSize = "1px";
        separatorDivElement.style.lineHeight = "0";
        if (this.separatorImage != null) {
            EchoCssUtil.applyStyle(separatorDivElement, this.separatorImage);
        }
    }
    
    switch (this.orientation) {
    case EchoSplitPane.ORIENTATION_VERTICAL_TOP_BOTTOM:
        paneDivElements[0].style.top = "0px";
        paneDivElements[0].style.left = "0px";
        paneDivElements[0].style.right = "0px";
        
        paneDivElements[1].style.bottom = "0px";
        paneDivElements[1].style.left = "0px";
        paneDivElements[1].style.right = "0px";
        
        if (separatorDivElement) {
            separatorDivElement.style.height = this.separatorSize + "px";
            separatorDivElement.style.left = "0px";
            separatorDivElement.style.right = "0px";
            if (this.resizable) {
                separatorDivElement.style.cursor = "n-resize";
            }
        }
        break;
    case EchoSplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP:
        paneDivElements[0].style.bottom = "0px";
        paneDivElements[0].style.left = "0px";
        paneDivElements[0].style.right = "0px";

        paneDivElements[1].style.top = "0px";
        paneDivElements[1].style.left = "0px";
        paneDivElements[1].style.right = "0px";
        
        if (separatorDivElement) {
            separatorDivElement.style.height = this.separatorSize + "px";
            separatorDivElement.style.left = "0px";
            separatorDivElement.style.right = "0px";
            if (this.resizable) {
                separatorDivElement.style.cursor = "s-resize";
            }
        }
        break;
    case EchoSplitPane.ORIENTATION_HORIZONTAL_LEFT_RIGHT:
        paneDivElements[0].style.top = "0px";
        paneDivElements[0].style.bottom = "0px";
        paneDivElements[0].style.left = "0px";
        
        paneDivElements[1].style.top = "0px";
        paneDivElements[1].style.bottom = "0px";
        paneDivElements[1].style.right = "0px";
        
        if (separatorDivElement) {
            separatorDivElement.style.width = this.separatorSize + "px";
            separatorDivElement.style.top = "0px";
            separatorDivElement.style.bottom = "0px";
            if (this.resizable) {
                separatorDivElement.style.cursor = "w-resize";
            }
        }
        break;
    case EchoSplitPane.ORIENTATION_HORIZONTAL_RIGHT_LEFT:
        paneDivElements[0].style.top = "0px";
        paneDivElements[0].style.bottom = "0px";
        paneDivElements[0].style.right = "0px";
        
        paneDivElements[1].style.top = "0px";
        paneDivElements[1].style.bottom = "0px";
        paneDivElements[1].style.left = "0px";
        
        if (separatorDivElement) {
            separatorDivElement.style.width = this.separatorSize + "px";
            separatorDivElement.style.top = "0px";
            separatorDivElement.style.bottom = "0px";
            if (this.resizable) {
                separatorDivElement.style.cursor = "e-resize";
            }
        }
        break;
    default:
        throw new Error("Illegal orientation: " + this.orientation);
    }
        
    this.update(paneDivElements[0], paneDivElements[1], separatorDivElement);

    splitPaneDivElement.appendChild(paneDivElements[0]);
    splitPaneDivElement.appendChild(paneDivElements[1]);
    if (separatorDivElement) {
        splitPaneDivElement.appendChild(separatorDivElement);
    }
    
    containerElement.appendChild(splitPaneDivElement);
    
    EchoVirtualPosition.register(paneDivElements[0].id);
    EchoVirtualPosition.register(paneDivElements[1].id);
    if (separatorDivElement) {
        EchoVirtualPosition.register(separatorDivElement.id);
    }
    
    EchoDomPropertyStore.setPropertyValue(splitPaneDivElement, "component", this);
    
    if (separatorDivElement && this.resizable) {
        EchoEventProcessor.addHandler(separatorDivElement, "mousedown", "EchoSplitPane.processSeparatorMouseDown");
    }
    
    EchoVirtualPosition.redraw(splitPaneDivElement);
    EchoVirtualPosition.redraw(paneDivElements[0]);
    EchoVirtualPosition.redraw(paneDivElements[1]);
};

EchoSplitPane.prototype.dispose = function() {
    EchoEventProcessor.removeHandler(this.elementId + "_separator", "mousedown");
    EchoDomUtil.removeEventListener(document, "mousemove", EchoSplitPane.processSeparatorMouseMove);
    EchoDomUtil.removeEventListener(document, "mouseup", EchoSplitPane.processSeparatorMouseUp);
};

EchoSplitPane.prototype.isOrientationVertical = function() {
    return this.orientation == EchoSplitPane.ORIENTATION_VERTICAL_TOP_BOTTOM 
            || this.orientation == EchoSplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP;
};

EchoSplitPane.prototype.processSeparatorMouseDown = function(echoEvent) {
    EchoSplitPane.activeInstance = this;
    this.dragInitPosition = this.position;
    if (this.isOrientationVertical()) {
        this.dragInitMouseOffset = echoEvent.clientY;
    } else {
        this.dragInitMouseOffset = echoEvent.clientX;
    }
    
    EchoDomUtil.addEventListener(document, "mousemove", EchoSplitPane.processSeparatorMouseMove);
    EchoDomUtil.addEventListener(document, "mouseup", EchoSplitPane.processSeparatorMouseUp);
};

EchoSplitPane.prototype.processSeparatorMouseMove = function(e) {
    switch (this.orientation) {
    case EchoSplitPane.ORIENTATION_VERTICAL_TOP_BOTTOM:
        this.setPosition(this.dragInitPosition + e.clientY - this.dragInitMouseOffset);
        break;
    case EchoSplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP:
        this.setPosition(this.dragInitPosition - e.clientY + this.dragInitMouseOffset);
        break;
    case EchoSplitPane.ORIENTATION_HORIZONTAL_LEFT_RIGHT:
        this.setPosition(this.dragInitPosition + e.clientX - this.dragInitMouseOffset);
        break;
    case EchoSplitPane.ORIENTATION_HORIZONTAL_RIGHT_LEFT:
        this.setPosition(this.dragInitPosition - e.clientX + this.dragInitMouseOffset);
        break;
    }
    this.update();
};

EchoSplitPane.prototype.processSeparatorMouseUp = function(e) {
    EchoSplitPane.activeInstance = null;
    EchoDomUtil.removeEventListener(document, "mousemove", EchoSplitPane.processSeparatorMouseMove);
    EchoDomUtil.removeEventListener(document, "mouseup", EchoSplitPane.processSeparatorMouseUp);
    EchoClientMessage.setPropertyValue(this.elementId, "separatorPosition",  this.position + "px");
    EchoVirtualPosition.redraw();
};

EchoSplitPane.prototype.resetPane = function(index) {
    var paneData = new EchoSplitPane.PaneData();
    var paneDivElement = document.getElementById(this.elementId + "_pane" + index);
    while (paneDivElement.childNodes.length > 0) {
        paneDivElement.removeChild(paneDivElement.lastChild);
    }
    if (index == 0) {
        this.paneData[0] = paneData;
    } else {
        this.paneData[1] = paneData;
    }
    paneData.applyStyle(paneDivElement);
};

EchoSplitPane.prototype.setPosition = function(newValue) {
    var divElement = document.getElementById(this.elementId);
    var vertical = this.isOrientationVertical();
    var totalSize = vertical ? divElement.offsetHeight : divElement.offsetWidth;
    
    if (newValue < this.paneData[0].minimumSize) {
	    this.position = this.paneData[0].minimumSize;
    } else if (this.paneData[0].maximumSize != -1 && newValue > this.paneData[0].maximumSize) {
	    this.position = this.paneData[0].maximumSize;
    } else if (newValue > totalSize - this.paneData[1].minimumSize - this.separatorSize) {
	    this.position = totalSize - this.paneData[1].minimumSize - this.separatorSize
    } else if (this.paneData[1].maximumSize != -1 && newValue < totalSize - this.paneData[1].maximumSize - this.separatorSize) {
	    this.position = totalSize - this.paneData[1].maximumSize - this.separatorSize;
    } else {
	    this.position = newValue;
    }
};

EchoSplitPane.prototype.update = function(firstPaneDivElement, secondPaneDivElement, separatorDivElement) {
    if (arguments.length == 0) {
        firstPaneDivElement = document.getElementById(this.elementId + "_pane0");
        secondPaneDivElement = document.getElementById(this.elementId + "_pane1");
        separatorDivElement = document.getElementById(this.elementId + "_separator");
    }

    switch (this.orientation) {
    case EchoSplitPane.ORIENTATION_VERTICAL_TOP_BOTTOM:
        var firstHeight = this.position - EchoSplitPane.getPaddingHeight(this.paneData[0]);
        firstPaneDivElement.style.height = (firstHeight > 0 ? firstHeight : 0) + "px";
        secondPaneDivElement.style.top = (this.position + this.separatorSize) + "px";
        if (separatorDivElement) {
            separatorDivElement.style.top = this.position + "px";
        }
        break;
    case EchoSplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP:
        var firstHeight = this.position - EchoSplitPane.getPaddingHeight(this.paneData[0]);
        firstPaneDivElement.style.height = (firstHeight > 0 ? firstHeight : 0) + "px";
        secondPaneDivElement.style.bottom = (this.position + this.separatorSize) + "px";
        if (separatorDivElement) {
            separatorDivElement.style.bottom = this.position + "px";
        }
        break;
    case EchoSplitPane.ORIENTATION_HORIZONTAL_LEFT_RIGHT:
        var firstWidth = this.position - EchoSplitPane.getPaddingWidth(this.paneData[0]);
        firstPaneDivElement.style.width = (firstWidth > 0 ? firstWidth : 0) + "px";
        secondPaneDivElement.style.left = (this.position + this.separatorSize) + "px";
        if (separatorDivElement) {
            separatorDivElement.style.left = this.position + "px";
        }
        break;
    case EchoSplitPane.ORIENTATION_HORIZONTAL_RIGHT_LEFT:
        var firstWidth = this.position - EchoSplitPane.getPaddingWidth(this.paneData[0]);
        firstPaneDivElement.style.width = (firstWidth > 0 ? firstWidth : 0) + "px";
        secondPaneDivElement.style.right = (this.position + this.separatorSize) + "px";
        if (separatorDivElement) {
            separatorDivElement.style.right = this.position + "px";
        }
        break;
    default:
        throw new Error("Illegal orientation: " + this.orientation);
        break;
    }
};

/**
 * Returns the SplitPane data object instance based on the root element id
 * of the SplitPane.
 *
 * @param componentId the root element id of the SplitPane
 * @return the relevant SplitPane instance
 */
EchoSplitPane.getComponent = function(componentId) {
    return EchoDomPropertyStore.getPropertyValue(componentId, "component");
};

EchoSplitPane.processSeparatorMouseDown = function(echoEvent) {
    var componentId = EchoDomUtil.getComponentId(echoEvent.registeredTarget.id);
    if (!EchoClientEngine.verifyInput(componentId)) {
        return;
    }
    var splitPane = EchoSplitPane.getComponent(componentId);
    splitPane.processSeparatorMouseDown(echoEvent);
};

EchoSplitPane.processSeparatorMouseMove = function(e) {
    e = e ? e : window.event;
    if (EchoSplitPane.activeInstance) {
	    EchoSplitPane.activeInstance.processSeparatorMouseMove(e);
    }
};

EchoSplitPane.processSeparatorMouseUp = function(e) {
    e = e ? e : window.event;
    if (EchoSplitPane.activeInstance) {
	    EchoSplitPane.activeInstance.processSeparatorMouseUp(e);
    }
};

/**
 * Static object/namespace for SplitPane MessageProcessor 
 * implementation.
 */
EchoSplitPane.MessageProcessor = function() { };

/**
 * MessageProcessor process() implementation 
 * (invoked by ServerMessage processor).
 *
 * @param messagePartElement the <code>message-part</code> element to process.
 */
EchoSplitPane.MessageProcessor.process = function(messagePartElement) {
    for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
        if (messagePartElement.childNodes[i].nodeType === 1) {
            switch (messagePartElement.childNodes[i].tagName) {
            case "add-child":
                EchoSplitPane.MessageProcessor.processAddChild(messagePartElement.childNodes[i]);
                break;
            case "dispose":
                EchoSplitPane.MessageProcessor.processDispose(messagePartElement.childNodes[i]);
                break;
            case "init":
                EchoSplitPane.MessageProcessor.processInit(messagePartElement.childNodes[i]);
                break;
            case "remove-child":
                EchoSplitPane.MessageProcessor.processRemoveChild(messagePartElement.childNodes[i]);
                break;
            case "set-separator-position":
                EchoSplitPane.MessageProcessor.processSetSeparatorPosition(messagePartElement.childNodes[i]);
                break;
            }
        }
    }
};

/**
 * Processes an <code>add-child</code> message to create an SplitPane.
 *
 * @param addChildMessageElement the <code>add-child</code> element to process
 */
EchoSplitPane.MessageProcessor.processAddChild = function(addChildMessageElement) {
    var elementId = addChildMessageElement.getAttribute("eid");
    var splitPane = EchoSplitPane.getComponent(elementId);
    var index = parseInt(addChildMessageElement.getAttribute("index"));
    
    var layoutDataElements = addChildMessageElement.getElementsByTagName("layout-data");
    if (layoutDataElements.length > 0) {
        EchoSplitPane.MessageProcessor.processLayoutData(layoutDataElements[0], splitPane.paneData[index]);
	    splitPane.paneData[index].applyStyle(document.getElementById(elementId + "_pane" + index));
    }
};

/**
 * Processes an <code>dispose</code> message to dispose the state of a 
 * SplitPane component that is being removed.
 *
 * @param disposeMessageElement the <code>dispose</code> element to process
 */
EchoSplitPane.MessageProcessor.processDispose = function(disposeMessageElement) {
    var elementId = disposeMessageElement.getAttribute("eid");
    var splitPane = EchoSplitPane.getComponent(elementId);
    if (splitPane) {
        splitPane.dispose();
    }
};

/**
 * Processes an <code>init</code> message to create an SplitPane.
 *
 * @param initMessageElement the <code>init</code> element to process
 */
EchoSplitPane.MessageProcessor.processInit = function(initMessageElement) {
    var elementId = initMessageElement.getAttribute("eid");
    var containerElementId = initMessageElement.getAttribute("container-eid");
    var orientation;
    switch(initMessageElement.getAttribute("orientation")) {
    case "l-r":
        orientation = EchoSplitPane.ORIENTATION_HORIZONTAL_LEFT_RIGHT;
        break;
    case "r-l":
        orientation = EchoSplitPane.ORIENTATION_HORIZONTAL_RIGHT_LEFT;
        break;
    case "t-b":
        orientation = EchoSplitPane.ORIENTATION_VERTICAL_TOP_BOTTOM;
        break;
    case "b-t":
        orientation = EchoSplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP;
        break;
    default:
        throw new Error("Illegal orientation.");
    }
    var position = parseInt(initMessageElement.getAttribute("position"));
    
    var splitPane = new EchoSplitPane(elementId, containerElementId, orientation, position);
    
    if (initMessageElement.getAttribute("background")) {
        splitPane.background = initMessageElement.getAttribute("background");
    }
    if (initMessageElement.getAttribute("foreground")) {
        splitPane.foreground = initMessageElement.getAttribute("foreground");
    }
    if (initMessageElement.getAttribute("font")) {
        splitPane.font = initMessageElement.getAttribute("font");
    }
    if (initMessageElement.getAttribute("separator-size")) {
        splitPane.separatorSize = parseInt(initMessageElement.getAttribute("separator-size"));
    }
    if (initMessageElement.getAttribute("separator-color")) {
        splitPane.separatorColor = initMessageElement.getAttribute("separator-color");
    }
    if (initMessageElement.getAttribute("separator-image")) {
        splitPane.separatorImage = initMessageElement.getAttribute("separator-image");
    }
    if (initMessageElement.getAttribute("resizable")) {
        splitPane.resizable = initMessageElement.getAttribute("resizable") == "true";
    }
    
    var layoutDataElements = initMessageElement.getElementsByTagName("layout-data");
    for (var i = 0; i < layoutDataElements.length; ++i) {
        var paneData = layoutDataElements[i].getAttribute("index") == 0 ? splitPane.paneData[0] : splitPane.paneData[1];
        EchoSplitPane.MessageProcessor.processLayoutData(layoutDataElements[i], paneData);
    }
    
    splitPane.create();
    
    if (initMessageElement.getAttribute("enabled") == "false") {
        EchoDomPropertyStore.setPropertyValue(elementId, "EchoClientEngine.inputDisabled", true);
    }
};

EchoSplitPane.MessageProcessor.processLayoutData = function(layoutDataElement, paneData) {
    if (layoutDataElement.getAttribute("alignment")) {
        paneData.alignment = layoutDataElement.getAttribute("alignment");
    }
    if (layoutDataElement.getAttribute("background")) {
        paneData.background = layoutDataElement.getAttribute("background");
    }
    if (layoutDataElement.getAttribute("background-image")) {
        paneData.backgroundImage = layoutDataElement.getAttribute("background-image");
    }
    if (layoutDataElement.getAttribute("insets")) {
        paneData.insets = new EchoCoreProperties.Insets(layoutDataElement.getAttribute("insets"));
    }
    if (layoutDataElement.getAttribute("overflow")) {
        paneData.overflow = layoutDataElement.getAttribute("overflow");
    }
    if (layoutDataElement.getAttribute("min-size")) {
        paneData.minimumSize = parseInt(layoutDataElement.getAttribute("min-size"));
    }
    if (layoutDataElement.getAttribute("max-size")) {
        paneData.maximumSize = parseInt(layoutDataElement.getAttribute("max-size"));
    }
};

/**
 * Processes an <code>remove-child</code> message to create an SplitPane.
 *
 * @param removeChildMessageElement the <code>remove-child</code> element to process
 */
EchoSplitPane.MessageProcessor.processRemoveChild = function(removeChildMessageElement) {
    var elementId = removeChildMessageElement.getAttribute("eid");
    var splitPane = EchoSplitPane.getComponent(elementId);
    var index = parseInt(removeChildMessageElement.getAttribute("index"));
    splitPane.resetPane(index);
};

/**
 * Processes an <code>set-separator-position</code> message to adjust the 
 * position of a SplitPane's separator.
 *
 * @param setSeparatorPositionMessageElement the 
 *        <code>set-separator-position</code> element to process
 */
EchoSplitPane.MessageProcessor.processSetSeparatorPosition = function(setSeparatorPositionMessageElement) {
    var elementId = setSeparatorPositionMessageElement.getAttribute("eid");
    var splitPane = EchoSplitPane.getComponent(elementId);
    splitPane.position = parseInt(setSeparatorPositionMessageElement.getAttribute("position"));
    splitPane.update();
};

EchoSplitPane.PaneData = function() {
    this.alignment = null;
    this.background = null;
    this.backgroundImage = null;
    this.insets = null;
    this.minimumSize = 0;
    this.maximumSize = -1;
    this.overflow = null;
};

EchoSplitPane.PaneData.prototype.applyStyle = function(paneDivElement) {
    if (this.alignment) {
        EchoCssUtil.applyStyle(paneDivElement, this.alignment);
    } else {
        paneDivElement.style.textAlign = "";
        paneDivElement.style.verticalAlign = "";
    }
    if (this.background) {
        paneDivElement.style.backgroundColor = this.background;
    } else {
        paneDivElement.style.backgroundColor = "";
    }
    if (this.backgroundImage) {
        EchoCssUtil.applyStyle(paneDivElement, this.backgroundImage);
    } else {
        paneDivElement.style.backgroundImage = "";
    }
    if (this.overflow) {
        paneDivElement.style.overflow = this.overflow;
    } else {
        paneDivElement.style.overflow = "auto";
    }
    if (this.insets) {
        paneDivElement.style.padding = this.insets.toString();
    } else {
        paneDivElement.style.padding = "0";
    }
};
