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

// _____________________
// Object EchoWindowPane

/**
 * Static object to provide support for dragging virtual windows.
 *
 * Do not instantiate.
 */
EchoWindowPane = function() { };

EchoWindowPane.MessageProcessor = function() { };

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

EchoWindowPane.MessageProcessor.processDispose = function(disposeMessageElement) {
    for (var item = disposeMessageElement.firstChild; item; item = item.nextSibling) {
        var elementId = item.getAttribute("eid");
        EchoEventProcessor.removeHandler(elementId + "_close", "click");
        EchoEventProcessor.removeHandler(elementId + "_close", "mousedown");
        EchoEventProcessor.removeHandler(elementId + "_title", "mousedown");
        EchoEventProcessor.removeHandler(elementId + "_border_tl", "mousedown");
        EchoEventProcessor.removeHandler(elementId + "_border_t", "mousedown");
        EchoEventProcessor.removeHandler(elementId + "_border_tr", "mousedown");
        EchoEventProcessor.removeHandler(elementId + "_border_l", "mousedown");
        EchoEventProcessor.removeHandler(elementId + "_border_r", "mousedown");
        EchoEventProcessor.removeHandler(elementId + "_border_bl", "mousedown");
        EchoEventProcessor.removeHandler(elementId + "_border_b", "mousedown");
        EchoEventProcessor.removeHandler(elementId + "_border_br", "mousedown");

        var containerId = EchoDomPropertyStore.getPropertyValue(elementId, "containerId");
        EchoWindowPane.ZIndexManager.removeElement(containerId, elementId);
    }
};

EchoWindowPane.MessageProcessor.processInit = function(initMessageElement) {
    for (var item = initMessageElement.firstChild; item; item = item.nextSibling) {
        var elementId = item.getAttribute("eid");
        var movable = item.getAttribute("movable") == "true";
        var resizable = item.getAttribute("resizable") == "true";
        var containerId = item.getAttribute("containerid");
        
        if (item.getAttribute("minimumwidth")) {
            EchoDomPropertyStore.setPropertyValue(elementId, "minimumWidth", item.getAttribute("minimumwidth"));
        }
        if (item.getAttribute("maximumwidth")) {
            EchoDomPropertyStore.setPropertyValue(elementId, "maximumWidth", item.getAttribute("maximumwidth"));
        }
        if (item.getAttribute("minimumheight")) {             
            EchoDomPropertyStore.setPropertyValue(elementId, "minimumHeight", item.getAttribute("minimumheight"));
        }
        if (item.getAttribute("maximumheight")) {
            EchoDomPropertyStore.setPropertyValue(elementId, "maximumHeight", item.getAttribute("maximumheight"));
        }
        
        EchoDomPropertyStore.setPropertyValue(elementId, "containerId", containerId);
        EchoWindowPane.ZIndexManager.addElement(containerId, elementId);
        EchoEventProcessor.addHandler(elementId + "_close", "click", "EchoWindowPane.processCloseClick");
        if (movable) {
            EchoEventProcessor.addHandler(elementId + "_close", "mousedown", "EchoWindowPane.processCloseMouseDown");
            EchoEventProcessor.addHandler(elementId + "_title", "mousedown", "EchoWindowPane.processTitleMouseDown");
        }
        if (resizable) {
            EchoEventProcessor.addHandler(elementId + "_border_tl", "mousedown", "EchoWindowPane.processBorderMouseDown");
            EchoEventProcessor.addHandler(elementId + "_border_t", "mousedown", "EchoWindowPane.processBorderMouseDown");
            EchoEventProcessor.addHandler(elementId + "_border_tr", "mousedown", "EchoWindowPane.processBorderMouseDown");
            EchoEventProcessor.addHandler(elementId + "_border_l", "mousedown", "EchoWindowPane.processBorderMouseDown");
            EchoEventProcessor.addHandler(elementId + "_border_r", "mousedown", "EchoWindowPane.processBorderMouseDown");
            EchoEventProcessor.addHandler(elementId + "_border_bl", "mousedown", "EchoWindowPane.processBorderMouseDown");
            EchoEventProcessor.addHandler(elementId + "_border_b", "mousedown", "EchoWindowPane.processBorderMouseDown");
            EchoEventProcessor.addHandler(elementId + "_border_br", "mousedown", "EchoWindowPane.processBorderMouseDown");
        }
    }
};

EchoWindowPane.ZIndexManager = function() { };

/**
 * Associative array mapping container ids to arrays of element ids.
 */
EchoWindowPane.ZIndexManager.containerIdToElementIdArrayMap = new Array();

EchoWindowPane.ZIndexManager.addElement = function(containerId, elementId) {
    var elementIdArray = EchoWindowPane.ZIndexManager.containerIdToElementIdArrayMap[containerId];
    if (!elementIdArray) {
        elementIdArray = new Array();
        EchoWindowPane.ZIndexManager.containerIdToElementIdArrayMap[containerId] = elementIdArray;
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

EchoWindowPane.ZIndexManager.raise = function(containerId, elementId) {
    var windowElement = document.getElementById(elementId);

    var elementIdArray = EchoWindowPane.ZIndexManager.containerIdToElementIdArrayMap[containerId];
    if (!elementIdArray) {
        throw "Invalid container id.";
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

EchoWindowPane.ZIndexManager.removeElement = function(containerId, elementId) {
    var elementIdArray = EchoWindowPane.ZIndexManager.containerIdToElementIdArrayMap[containerId];
    if (!elementIdArray) {
        throw "ZIndexManager.removeElement: no data for container with id \"" + containerId + "\".";
    }
    for (var i = 0; i < elementIdArray.length; ++i) {
        if (elementIdArray[i] == elementId) {
            if (elementIdArray.length == 1) {
                delete EchoWindowPane.ZIndexManager.containerIdToElementIdArrayMap[containerId];
            } else {
	            if (i < elementIdArray.length - 1) {
	                elementIdArray[i] = elementIdArray[elementIdArray.length - 1];
	            }
	            --elementIdArray.length;
            }
            return;
        }
    }
    throw "ZIndexManager.removeElement: Element with id \"" + elementId + 
            "\" does not exist in container with id \"" + containerId + "\".";
};

/** The initial horizontal position of the window. */
EchoWindowPane.initialWindowX = -1;

/** The initial vertical position of the window. */
EchoWindowPane.initialWindowY = -1;

/** The initial width of the window. */
EchoWindowPane.initialWidth = -1;

/** The initial height of the window. */
EchoWindowPane.initialHeight = -1;

/** The horizontal drag position of the mouse relative to the window. */
EchoWindowPane.mouseOffsetX = -1;

/** The vertical drag position of the mouse relative to the window. */
EchoWindowPane.mouseOffsetY = -1;

/** The active window DIV element being dragged */
EchoWindowPane.activeElement = null;

EchoWindowPane.resizeModeHorizontal = 0;
EchoWindowPane.resizeModeVertical = 0;

EchoWindowPane.minimumWidth = -1;
EchoWindowPane.minimumHeight = -1;
EchoWindowPane.maximumWidth= -1;
EchoWindowPane.maximumHeight = -1;

/** 
 * The maximum allowed horizontal position of the upper-left corner of 
 * the window. 
 */
EchoWindowPane.maxX = -1;

/** 
 * The maximum allowed vertical position of the upper-left corner of 
 * the window. 
 */
EchoWindowPane.maxY = -1;

/**
 * Calculates height of region in which window may be moved.
 *
 * @param element the container element within which the window may
 *        be moved
 * @return the width of the region
 */
EchoWindowPane.getClientHeight = function(element) {
    if (element.clientHeight && element.clientHeight > 0) {
        return element.clientHeight;
    } else if (element.parentNode) {
        return EchoWindowPane.getClientHeight(element.parentNode);
    }
    return 0;
};

//BUGBUG. getBorderHeight/Width need to be redone.

/**
 * Determines the height of the floating window's border.
 * This method currently only calculates the size correctly for
 * elements which have equal borders on every side.
 *
 * @param element the window's DIV element
 * @return the height of the border
 */
EchoWindowPane.getBorderHeight = function(element) {
    var sizeData = element.style.borderWidth;
    var heightValue = parseInt(sizeData);
    return isNaN(heightValue) ? 5 : heightValue;
};

/**
 * Determines the width of the floating window's border.
 * This method currently only calculates the size correctly for
 * elements which have equal borders on every side.
 *
 * @param element the window's DIV element
 * @return the width of the border
 */
EchoWindowPane.getBorderWidth = function(element) {
    var sizeData = element.style.borderWidth;
    var widthValue = parseInt(sizeData);
    return isNaN(widthValue) ? 5 : widthValue;
};

/**
 * Calculates width of region in which window may be moved.
 *
 * @param element the container element within which the window may
 *        be moved
 * @return the width of the region
 */
EchoWindowPane.getClientWidth = function(element) {
    if (element.clientWidth && element.clientWidth > 0) {
        return element.clientWidth;
    } else if (element.parentNode) {
        return EchoWindowPane.getClientWidth(element.parentNode);
    }
    return 0;
};

EchoWindowPane.processBorderMouseDown = function(echoEvent) {
    var elementId = echoEvent.registeredTarget.getAttribute("id");
    if (!EchoClientEngine.verifyInput(elementId)) {
        return;
    }
    var componentId = EchoDomUtil.getComponentId(elementId);
    EchoWindowPane.raise(componentId);
    EchoWindowPane.processBorderDragMouseDown(echoEvent);
};

EchoWindowPane.processBorderDragMouseDown = function(echoEvent) {
    EchoDomUtil.preventEventDefault(echoEvent);
    var elementId = echoEvent.registeredTarget.getAttribute("id");
    
    var lastUnderscoreIndex = elementId.lastIndexOf("_");
    if (lastUnderscoreIndex == -1) {
        // Should not occur.
        throw "Invalid element id: " + elementId;
    }
    var directionName = elementId.substring(lastUnderscoreIndex + 1);
    var verticalMode, horizontalMode;
    switch(directionName) {
    case "tl": EchoWindowPane.resizeModeHorizontal = -1; EchoWindowPane.resizeModeVertical = -1; break;
    case "t":  EchoWindowPane.resizeModeHorizontal =  0; EchoWindowPane.resizeModeVertical = -1; break;
    case "tr": EchoWindowPane.resizeModeHorizontal =  1; EchoWindowPane.resizeModeVertical = -1; break;
    case "l":  EchoWindowPane.resizeModeHorizontal = -1; EchoWindowPane.resizeModeVertical =  0; break;
    case "r":  EchoWindowPane.resizeModeHorizontal =  1; EchoWindowPane.resizeModeVertical =  0; break;
    case "bl": EchoWindowPane.resizeModeHorizontal = -1; EchoWindowPane.resizeModeVertical =  1; break;
    case "b":  EchoWindowPane.resizeModeHorizontal =  0; EchoWindowPane.resizeModeVertical =  1; break;
    case "br": EchoWindowPane.resizeModeHorizontal =  1; EchoWindowPane.resizeModeVertical =  1; break;
    default: throw "Invalid direction name: " + directionName;
    }

    var componentId = EchoDomUtil.getComponentId(elementId);
    var minimumWidth = parseInt(EchoDomPropertyStore.getPropertyValue(componentId, "minimumWidth"));
    var minimumHeight = parseInt(EchoDomPropertyStore.getPropertyValue(componentId, "minimumHeight"));
    var maximumWidth = parseInt(EchoDomPropertyStore.getPropertyValue(componentId, "maximumWidth"));
    var maximumHeight = parseInt(EchoDomPropertyStore.getPropertyValue(componentId, "maximumHeight"));
    var windowPaneElement = document.getElementById(componentId);

    if (windowPaneElement != EchoWindowPane.activeElement) {
	    EchoWindowPane.minimumWidth = isNaN(minimumWidth) ? 100 : minimumWidth;
	    EchoWindowPane.minimumHeight = isNaN(minimumHeight) ? 100 : minimumHeight;
	    EchoWindowPane.maximumWidth = isNaN(maximumWidth) ? 800 : maximumWidth;
	    EchoWindowPane.maximumHeight = isNaN(maximumHeight) ? 600 : maximumHeight;

        EchoWindowPane.activeElement = windowPaneElement;
        EchoWindowPane.mouseOffsetX = echoEvent.clientX;
        EchoWindowPane.mouseOffsetY = echoEvent.clientY;
        EchoWindowPane.initialWindowX = parseInt(EchoWindowPane.activeElement.style.left);
        EchoWindowPane.initialWindowY = parseInt(EchoWindowPane.activeElement.style.top);
        EchoWindowPane.initialWindowWidth = parseInt(EchoWindowPane.activeElement.style.width);
        EchoWindowPane.initialWindowHeight = parseInt(EchoWindowPane.activeElement.style.height);

        EchoDomUtil.addEventListener(document, "mousemove", EchoWindowPane.processBorderDragMouseMove, true);
        EchoDomUtil.addEventListener(document, "mouseup", EchoWindowPane.processBorderDragMouseUp, true);
        EchoDomUtil.addEventListener(document, "selectstart", EchoWindowPane.selectStart, true);
    }
};

EchoWindowPane.processBorderDragMouseMove = function(e) {
    e = (e) ? e : ((window.event) ? window.event : "");

    if (EchoWindowPane.resizeModeHorizontal !== 0) {
        var newWidth = (EchoWindowPane.initialWindowWidth + 
                (e.clientX - EchoWindowPane.mouseOffsetX) * EchoWindowPane.resizeModeHorizontal);
        if (newWidth < EchoWindowPane.minimumWidth) {
            newWidth = EchoWindowPane.minimumWidth;
        } else if (newWidth > EchoWindowPane.maximumWidth) {
            newWidth = EchoWindowPane.maximumWidth;
        }
        EchoWindowPane.activeElement.style.width = newWidth + "px";
        if (EchoWindowPane.resizeModeHorizontal === -1) {
            var newX = EchoWindowPane.initialWindowX + EchoWindowPane.initialWindowWidth - newWidth;
            EchoWindowPane.activeElement.style.left = newX + "px";
        }
    }

    if (EchoWindowPane.resizeModeVertical !== 0) {
        var newHeight = (EchoWindowPane.initialWindowHeight + 
                (e.clientY - EchoWindowPane.mouseOffsetY) * EchoWindowPane.resizeModeVertical);
        if (newHeight < EchoWindowPane.minimumHeight) {
            newHeight = EchoWindowPane.minimumHeight;
        } else if (newHeight > EchoWindowPane.maximumHeight) {
            newHeight = EchoWindowPane.maximumHeight;
        }
        EchoWindowPane.activeElement.style.height = newHeight + "px";
        if (EchoWindowPane.resizeModeVertical === -1) {
            var newY = EchoWindowPane.initialWindowY + EchoWindowPane.initialWindowHeight - newHeight;
            EchoWindowPane.activeElement.style.top = newY + "px";
        }

        if (EchoClientProperties.get("quirkIERepaint")) {
            // Tickle window width because Internet Explorer will only recompute size of hidden IFRAME
            // on *WIDTH* adjustments, not height adjustments (this is a bug in IE).
            // This only need be applied when vertical resizes can be performed.
            var initialWidth = parseInt(EchoWindowPane.activeElement.style.width);
            EchoWindowPane.activeElement.style.width = (initialWidth + 1) + "px";
            EchoWindowPane.activeElement.style.width = initialWidth + "px";
        }
    }
};

EchoWindowPane.processBorderDragMouseUp = function(e) {
    e = (e) ? e : ((window.event) ? window.event : "");

    var id = EchoWindowPane.activeElement.getAttribute("id");
    EchoClientMessage.setPropertyValue(id, "positionX",  EchoWindowPane.activeElement.style.left);
    EchoClientMessage.setPropertyValue(id, "positionY",  EchoWindowPane.activeElement.style.top);
    EchoClientMessage.setPropertyValue(id, "width",  EchoWindowPane.activeElement.style.width);
    EchoClientMessage.setPropertyValue(id, "height",  EchoWindowPane.activeElement.style.height);

    EchoWindowPane.activeElement = null;
    EchoDomUtil.removeEventListener(document, "mousemove", EchoWindowPane.processBorderDragMouseMove, true);
    EchoDomUtil.removeEventListener(document, "mouseup", EchoWindowPane.processBorderDragMouseUp, true);
    EchoDomUtil.removeEventListener(document, "selectstart", EchoWindowPane.selectStart, true);
};

/**
 * Event handler for "Click" events on the close button.
 * Notifies server of user action.
 *
 * @param echoEvent the event
 */
EchoWindowPane.processCloseClick = function(echoEvent) {
    var elementId = echoEvent.registeredTarget.getAttribute("id");
    if (!EchoClientEngine.verifyInput(elementId)) {
        return;
    }
    var componentId = EchoDomUtil.getComponentId(elementId);
    EchoClientMessage.setActionValue(componentId, "close");
    EchoServerTransaction.connect();
};

/**
 * Event handler for "MouseDown" events on the close button.
 * Avoids close button mouse-down clicks from being processed by drag initiation handler.
 *
 * @param echoEvent the event
 */
EchoWindowPane.processCloseMouseDown = function(echoEvent) {
    EchoDomUtil.preventEventDefault(echoEvent);
};

EchoWindowPane.processTitleDragMouseDown = function(echoEvent) {
    var componentId = EchoDomUtil.getComponentId(echoEvent.registeredTarget.getAttribute("id"));
    EchoDomUtil.preventEventDefault(echoEvent);
    var windowPaneElement = document.getElementById(componentId);
    
    if (windowPaneElement != EchoWindowPane.activeElement) {
        EchoWindowPane.activeElement = windowPaneElement;
        EchoWindowPane.mouseOffsetX = echoEvent.clientX;
        EchoWindowPane.mouseOffsetY = echoEvent.clientY;
        EchoWindowPane.initialWindowX = parseInt(EchoWindowPane.activeElement.style.left);
        EchoWindowPane.initialWindowY = parseInt(EchoWindowPane.activeElement.style.top);
        
        var borderWidth = 2 * EchoWindowPane.getBorderWidth(EchoWindowPane.activeElement);
        var borderHeight = 2 * EchoWindowPane.getBorderHeight(EchoWindowPane.activeElement);
        
        var containerWidth = EchoWindowPane.getClientWidth(EchoWindowPane.activeElement.parentNode);
        if (containerWidth === 0) {
            containerWidth = 800;
        }
        EchoWindowPane.maxX = containerWidth - EchoWindowPane.activeElement.clientWidth - borderWidth;
        if (EchoWindowPane.maxX < 0) {
            EchoWindowPane.maxX = 0;
        }

        var containerHeight = EchoWindowPane.getClientHeight(EchoWindowPane.activeElement.parentNode);
        if (containerHeight === 0) {
            containerHeight = 600;
        }
        EchoWindowPane.maxY = containerHeight - EchoWindowPane.activeElement.clientHeight - borderHeight;
        if (EchoWindowPane.maxY < 0) {
            EchoWindowPane.maxY = 0;
        }
        
        EchoDomUtil.addEventListener(document, "mousemove", EchoWindowPane.processTitleDragMouseMove, true);
        EchoDomUtil.addEventListener(document, "mouseup", EchoWindowPane.processTitleDragMouseUp, true);
        EchoDomUtil.addEventListener(document, "selectstart", EchoWindowPane.selectStart, true);
    }
};

/**
 * Event handler for "MouseMove" events.  Registered when drag is initiated.
 *
 * @param e The event (only provided when using DOM Level 2 Event Model)
 */
EchoWindowPane.processTitleDragMouseMove = function(e) {
    e = (e) ? e : ((window.event) ? window.event : "");
    var newX = (EchoWindowPane.initialWindowX + e.clientX - EchoWindowPane.mouseOffsetX);
    var newY = (EchoWindowPane.initialWindowY + e.clientY - EchoWindowPane.mouseOffsetY);
    
    newX = newX >= 0 ? newX : 0;
    newX = newX <= EchoWindowPane.maxX ? newX : EchoWindowPane.maxX;
    newY = newY >= 0 ? newY : 0;
    newY = newY <= EchoWindowPane.maxY ? newY : EchoWindowPane.maxY;
    EchoWindowPane.activeElement.style.left = newX + "px";
    EchoWindowPane.activeElement.style.top = newY + "px";

    if (EchoClientProperties.get("quirkDomPerformanceIERepaint")) {
        // Tickle width to force repaint for IE repaint, resulting in aesthetic performance increase.
	    var initialWidth = parseInt(EchoWindowPane.activeElement.style.width);
	    EchoWindowPane.activeElement.style.width = (initialWidth + 1) + "px";
	    EchoWindowPane.activeElement.style.width = initialWidth + "px";
    }
};

/**
 * Event handler for "MouseUp" events.  Registered when drag is initiated.
 *
 * @param e The event (only provided when using DOM Level 2 Event Model)
 */
EchoWindowPane.processTitleDragMouseUp = function(e) {
    e = (e) ? e : ((window.event) ? window.event : "");
    
    var id = EchoWindowPane.activeElement.getAttribute("id");
    EchoClientMessage.setPropertyValue(id, "positionX",  EchoWindowPane.activeElement.style.left);
    EchoClientMessage.setPropertyValue(id, "positionY",  EchoWindowPane.activeElement.style.top);
    
    EchoWindowPane.activeElement = null;
    EchoDomUtil.removeEventListener(document, "mousemove", EchoWindowPane.processTitleDragMouseMove, true);
    EchoDomUtil.removeEventListener(document, "mouseup", EchoWindowPane.processTitleDragMouseUp, true);
    EchoDomUtil.removeEventListener(document, "selectstart", EchoWindowPane.selectStart, true);
};

/**
 * Event handler for "MouseDown" events.  Permanently registered.
 *
 * @param echoEvent the "MouseDown" event, preprocessed by the
 *        EchoEventProcessor
 */
EchoWindowPane.processTitleMouseDown = function(echoEvent) {
    var elementId = echoEvent.registeredTarget.getAttribute("id");
    if (!EchoClientEngine.verifyInput(elementId)) {
        return;
    }
    var componentId = EchoDomUtil.getComponentId(echoEvent.registeredTarget.getAttribute("id"));
    EchoWindowPane.raise(componentId);
    EchoWindowPane.processTitleDragMouseDown(echoEvent);
};

/**
 * Raises the specified WindowPane to the top.
 *
 * @param windowComponentId the id of the window pane
 */
EchoWindowPane.raise = function(windowComponentId) {
    var containerId = EchoDomPropertyStore.getPropertyValue(windowComponentId, "containerId");
    var zIndex = EchoWindowPane.ZIndexManager.raise(containerId, windowComponentId);
    EchoClientMessage.setPropertyValue(windowComponentId, "zIndex",  zIndex);
};

/**
 * Event handler for "SelectStart" events to disable selection while dragging the Window.
 *
 * @param e The event (only provided when using DOM Level 2 Event Model)
 */
EchoWindowPane.selectStart = function(e) {
    e = (e) ? e : ((window.event) ? window.event : "");
    EchoDomUtil.preventEventDefault(e);
};
