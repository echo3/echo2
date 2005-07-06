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
 * Static object/namespace for WindowPane support.
 * This object/namespace should not be used externally.
 */
EchoWindowPane = function() { };

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
    EchoWindowPane.ZIndexManager.remove(containerId, elementId);
};

/**
 * Processes an <code>init</code> message to initialize the state of a 
 * WindowPane that is being added.
 *
 * @param initMessageElement the <code>init</code> element to process
 */
EchoWindowPane.MessageProcessor.processInit = function(initElement) {
    var elementId = initElement.getAttribute("eid");
    var movable = initElement.getAttribute("movable") == "true";
    var resizable = initElement.getAttribute("resizable") == "true";
    var containerId = initElement.getAttribute("container-id");
    
    if (initElement.getAttribute("minimum-width")) {
        EchoDomPropertyStore.setPropertyValue(elementId, "minimumWidth", initElement.getAttribute("minimum-width"));
    }
    if (initElement.getAttribute("maximum-width")) {
        EchoDomPropertyStore.setPropertyValue(elementId, "maximumWidth", initElement.getAttribute("maximum-width"));
    }
    if (initElement.getAttribute("minimum-height")) {             
        EchoDomPropertyStore.setPropertyValue(elementId, "minimumHeight", initElement.getAttribute("minimum-height"));
    }
    if (initElement.getAttribute("maximum-height")) {
        EchoDomPropertyStore.setPropertyValue(elementId, "maximumHeight", initElement.getAttribute("maximum-height"));
    }
    
    EchoDomPropertyStore.setPropertyValue(elementId, "containerId", containerId);
    EchoWindowPane.ZIndexManager.add(containerId, elementId);
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
};

/**
 * Static object/namespace to manage z-index ordering of multiple WindowPanes
 * with the same parent component.
 */
EchoWindowPane.ZIndexManager = function() { };

/**
 * Associative array mapping container ids to arrays of element ids.
 */
EchoWindowPane.ZIndexManager.containerIdToElementIdArrayMap = new Array();

/**
 * Adds a WindowPane to be managed by the ZIndexManager.
 *
 * @param containerId the id of the Element containing the WindowPane
 * @param elementId the id of the WindowPane
 */
EchoWindowPane.ZIndexManager.add = function(containerId, elementId) {
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

/**
 * Raises a WindowPane being managed by the ZIndexManager to the above all 
 * other WindowPanes within its container.
 *
 * @param containerId the id of the Element containing the WindowPane
 * @param elementId the id of the WindowPane
 */
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

/**
 * Remvoes a WindowPane from being managed by the ZIndexManager.
 *
 * @param containerId the id of the Element containing the WindowPane
 * @param elementId the id of the WindowPane
 */
EchoWindowPane.ZIndexManager.remove = function(containerId, elementId) {
    var elementIdArray = EchoWindowPane.ZIndexManager.containerIdToElementIdArrayMap[containerId];
    if (!elementIdArray) {
        throw "ZIndexManager.remove: no data for container with id \"" + containerId + "\".";
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
    throw "ZIndexManager.remove: Element with id \"" + elementId + 
            "\" does not exist in container with id \"" + containerId + "\".";
};

/** The initial horizontal position of the WindowPane being moved/resized. */
EchoWindowPane.initialWindowX = -1;

/** The initial vertical position of the WindowPane being moved/resized. */
EchoWindowPane.initialWindowY = -1;

/** The initial width of the WindowPane being moved/resized. */
EchoWindowPane.initialWidth = -1;

/** The initial height of the WindowPane being moved/resized. */
EchoWindowPane.initialHeight = -1;

/** The horizontal drag position of the mouse relative to the WindowPane. */
EchoWindowPane.mouseOffsetX = -1;

/** The vertical drag position of the mouse relative to the WindowPane. */
EchoWindowPane.mouseOffsetY = -1;

/** The DIV element of the WindowPane being moved/resized. */
EchoWindowPane.activeElement = null;

/**
 * Indicates the direction (if any) that the current WindowPane is being
 * resized in the horizontal direction.  This value may be one of
 * the following values:
 * <ul>
 *  <lil>-1: the WindowPane is being resized by its left border</li>
 *  <lil> 0: the WindowPane is not being resized horizontally</li>
 *  <lil> 1: the WindowPane is being resized by its right border</li>
 * </ul>
 */
EchoWindowPane.resizeModeHorizontal = 0;

/**
 * Indicates the direction (if any) that the current WindowPane is being
 * resized in the vertical direction.  This value may be one of
 * the following values:
 * <ul>
 *  <lil>-1: the WindowPane is being resized by its top border</li>
 *  <lil> 0: the WindowPane is not being resized vertically</li>
 *  <lil> 1: the WindowPane is being resized by its bottom border</li>
 * </ul>
 */
EchoWindowPane.resizeModeVertical = 0;

/** The minimum width of the WindowPane currently being resized */
EchoWindowPane.minimumWidth = -1;

/** The minimum height of the WindowPane  currently being resized */
EchoWindowPane.minimumHeight = -1;

/** The maximum width of the WindowPane  currently being resized */
EchoWindowPane.maximumWidth= -1;

/** The maximum height of the WindowPane  currently being resized */
EchoWindowPane.maximumHeight = -1;

/** 
 * The maximum allowed horizontal position of the upper-left corner of 
 * the WindowPane currently being moved/resized.  This value is calculated
 * based on the available movement area.
 */
EchoWindowPane.maxX = -1;

/** 
 * The maximum allowed vertical position of the upper-left corner of 
 * the WindowPane currently being moved/resized.  This value is calculated
 * based on the available movement area.
 */
EchoWindowPane.maxY = -1;

/**
 * Calculates the height of region in which window may be moved.
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
 * Calculates width of the region in which window may be moved.
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

/**
 * Processes a mouse down event within the border region of a WindowPane.
 * This handler first raises the window and then delegates border
 * dragging work to <code>processBorderDragMouseDown()</code>.
 * This event listener is permanently registered to the window border
 * elements using the EchoEventProcessor.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoWindowPane.processBorderMouseDown = function(echoEvent) {
    var elementId = echoEvent.registeredTarget.getAttribute("id");
    if (!EchoClientEngine.verifyInput(elementId)) {
        return;
    }
    var componentId = EchoDomUtil.getComponentId(elementId);
    EchoWindowPane.raise(componentId);
    EchoWindowPane.processBorderDragMouseDown(echoEvent);
};

/**
 * Configures a window border for resizing in response to a mousedown
 * event within the border region of a WindowPane.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
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

/**
 * Processes a mouse move event when the user is resizing a 
 * WindowPane.  Adjusts the size of the WindowPane
 * based on the user's mouse position.
 *
 * This event listener is temporarily registered by the 
 * <code>processBorderDragMouseDown()</code> method when the user begins
 * resizing a WindowPane by dragging its border.  The event listener is 
 * unregistered when the user releases the mouse and
 * <code>processBorderDragMouseUp()</code> is invoked.
 *
 * @param e the event (only provided when using DOM Level 2 Event Model)
 */
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

/**
 * Processes a mouse button release event when the user is resizing a 
 * WindowPane.  Stores the updated state of the WindowPane in
 * the ClientMessage.
 *
 * This method will automatically unregister the
 * temporarily set mouse move/release listeners as its invocation indicates
 * the completion of a window resize operation.
 * This event listener is temporarily registered by the 
 * <code>processBorderDragMouseDown()</code> method when the user begins
 * resizing a WindowPane by dragging its border.  
 *
 * @param e the event (only provided when using DOM Level 2 Event Model)
 */
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
 * Event handler for mouse click events on the close button.
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
 * Event handler for mouse down events on the close button.
 * Prevents close button mouse-down clicks from being processed by drag 
 * initiation handler.
 *
 * @param echoEvent the event
 */
EchoWindowPane.processCloseMouseDown = function(echoEvent) {
    EchoDomUtil.preventEventDefault(echoEvent);
};

/**
 * Configures a WindowPane for dragging in response to a mousedown
 * event within the title region of a WindowPane.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
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
 * Processes a mouse move event when the user is dragging a 
 * WindowPane.  Adjusts the position of the WindowPane
 * based on the user's mouse position.
 *
 * This event listener is temporarily registered by the 
 * <code>processBorderDragMouseDown()</code> method when the user begins
 * dragging a WindowPane by dragging its title.  The event listener is 
 * unregistered when the user releases the mouse and
 * <code>processTitleDragMouseUp()</code> is invoked.
 *
 * @param e the event (only provided when using DOM Level 2 Event Model)
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

    if (EchoClientProperties.get("quirkIERepaint")) {
        // Tickle width to force repaint for IE repaint, resulting in aesthetic performance increase.
        var initialWidth = parseInt(EchoWindowPane.activeElement.style.width);
        EchoWindowPane.activeElement.style.width = (initialWidth + 1) + "px";
        EchoWindowPane.activeElement.style.width = initialWidth + "px";
    }
};

/**
 * Processes a mouse button release event when the user is dragging a 
 * WindowPane.  Stores the updated state of the WindowPane in
 * the ClientMessage.
 *
 * This method will automatically unregister the
 * temporarily set mouse move/release listeners as its invocation indicates
 * the completion of a window drag operation.
 * This event listener is temporarily registered by the 
 * <code>processTitleDragMouseDown()</code> method when the user begins
 * dragging a WindowPane by dragging its title.
 *
 * @param e the event (only provided when using DOM Level 2 Event Model)
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
 * Processes a mouse down event within the title region of a WindowPane.
 * This handler first raises the window and then delegates window
 * dragging work to <code>processTitleDragMouseDown()</code>.
 * This event listener is permanently registered to the window border
 * elements using the EchoEventProcessor.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
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
 * @param windowComponentId the id of the WindowPane
 */
EchoWindowPane.raise = function(windowComponentId) {
    var containerId = EchoDomPropertyStore.getPropertyValue(windowComponentId, "containerId");
    var zIndex = EchoWindowPane.ZIndexManager.raise(containerId, windowComponentId);
    EchoClientMessage.setPropertyValue(windowComponentId, "zIndex",  zIndex);
};

/**
 * Event handler for "SelectStart" events to disable selection while dragging
 * the Window.  (Internet Explorer specific)
 *
 * @param e The event (only provided when using DOM Level 2 Event Model)
 */
EchoWindowPane.selectStart = function(e) {
    e = (e) ? e : ((window.event) ? window.event : "");
    EchoDomUtil.preventEventDefault(e);
};
