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

//BUGBUG. hardcoded.
EchoWindowPane.minimumWidth = 100;
EchoWindowPane.minimumHeight = 100;
EchoWindowPane.maximumWidth= 800;
EchoWindowPane.maximumHeight = 600;

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
 * Event handler for "MouseDown" events.  Permanently registered.
 *
 * @param echoEvent the "MouseDown" event, preprocessed by the
 *        EchoEventProcessor
 */
EchoWindowPane.windowMoveMouseDown = function(echoEvent) {
    EchoDomUtil.preventEventDefault(echoEvent);
    var componentId = EchoDomUtil.getComponentId(echoEvent.registeredTarget.getAttribute("id"));
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
        EchoWindowPane.maxY = containerHeight- EchoWindowPane.activeElement.clientHeight - borderHeight;
        if (EchoWindowPane.maxY < 0) {
            EchoWindowPane.maxY = 0;
        }
        
        EchoDomUtil.addEventListener(document, "mousemove", EchoWindowPane.windowMoveMouseMove, true);
        EchoDomUtil.addEventListener(document, "mouseup", EchoWindowPane.windowMoveMouseUp, true);
        EchoDomUtil.addEventListener(document, "selectstart", EchoWindowPane.selectStart, true);
    }
};

/**
 * Event handler for "MouseMove" events.  Registered when drag is initiated.
 *
 * @param e The event (only provided when using DOM Level 2 Event Model)
 */
EchoWindowPane.windowMoveMouseMove = function(e) {
    e = (e) ? e : ((window.event) ? window.event : "");
    var newX = (EchoWindowPane.initialWindowX + e.clientX - EchoWindowPane.mouseOffsetX);
    var newY = (EchoWindowPane.initialWindowY + e.clientY - EchoWindowPane.mouseOffsetY);
    
    newX = newX >= 0 ? newX : 0;
    newX = newX <= EchoWindowPane.maxX ? newX : EchoWindowPane.maxX;
    newY = newY >= 0 ? newY : 0;
    newY = newY <= EchoWindowPane.maxY ? newY : EchoWindowPane.maxY;
    EchoWindowPane.activeElement.style.left = newX + "px";
    EchoWindowPane.activeElement.style.top = newY + "px";

    if ("true" == EchoDomPropertyStore.getPropertyValue(EchoWindowPane.activeElement.getAttribute("id"), "quirkResizeOnMove")) {
        // Tickle window size for dramatic performance increase in Internet Explorer.
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
EchoWindowPane.windowMoveMouseUp = function(e) {
    e = (e) ? e : ((window.event) ? window.event : "");
    
    var id = EchoWindowPane.activeElement.getAttribute("id");
    EchoClientMessage.setPropertyValue(id, "positionX",  EchoWindowPane.activeElement.style.left);
    EchoClientMessage.setPropertyValue(id, "positionY",  EchoWindowPane.activeElement.style.top);
    
    EchoWindowPane.activeElement = null;
    EchoDomUtil.removeEventListener(document, "mousemove", EchoWindowPane.windowMoveMouseMove, true);
    EchoDomUtil.removeEventListener(document, "mouseup", EchoWindowPane.windowMoveMouseUp, true);
    EchoDomUtil.removeEventListener(document, "selectstart", EchoWindowPane.selectStart, true);
};

EchoWindowPane.windowResizeMouseDown = function(echoEvent) {
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
    case "nw": EchoWindowPane.resizeModeHorizontal = -1; EchoWindowPane.resizeModeVertical = -1; break;
    case "n":  EchoWindowPane.resizeModeHorizontal =  0; EchoWindowPane.resizeModeVertical = -1; break;
    case "ne": EchoWindowPane.resizeModeHorizontal =  1; EchoWindowPane.resizeModeVertical = -1; break;
    case "w":  EchoWindowPane.resizeModeHorizontal = -1; EchoWindowPane.resizeModeVertical =  0; break;
    case "e":  EchoWindowPane.resizeModeHorizontal =  1; EchoWindowPane.resizeModeVertical =  0; break;
    case "sw": EchoWindowPane.resizeModeHorizontal = -1; EchoWindowPane.resizeModeVertical =  1; break;
    case "s":  EchoWindowPane.resizeModeHorizontal =  0; EchoWindowPane.resizeModeVertical =  1; break;
    case "se": EchoWindowPane.resizeModeHorizontal =  1; EchoWindowPane.resizeModeVertical =  1; break;
    default: throw "Invalid direction name: " + directionName;
    }
    
    var componentId = EchoDomUtil.getComponentId(elementId);
    var windowPaneElement = document.getElementById(componentId);
    if (windowPaneElement != EchoWindowPane.activeElement) {

        EchoWindowPane.activeElement = windowPaneElement;
        EchoWindowPane.mouseOffsetX = echoEvent.clientX;
        EchoWindowPane.mouseOffsetY = echoEvent.clientY;
        EchoWindowPane.initialWindowX = parseInt(EchoWindowPane.activeElement.style.left);
        EchoWindowPane.initialWindowY = parseInt(EchoWindowPane.activeElement.style.top);
        EchoWindowPane.initialWindowWidth = parseInt(EchoWindowPane.activeElement.style.width);
        EchoWindowPane.initialWindowHeight = parseInt(EchoWindowPane.activeElement.style.height);

        EchoDomUtil.addEventListener(document, "mousemove", EchoWindowPane.windowResizeMouseMove, true);
        EchoDomUtil.addEventListener(document, "mouseup", EchoWindowPane.windowResizeMouseUp, true);
        EchoDomUtil.addEventListener(document, "selectstart", EchoWindowPane.selectStart, true);
    }
};

EchoWindowPane.windowResizeMouseMove = function(e) {
    e = (e) ? e : ((window.event) ? window.event : "");

    if (EchoWindowPane.resizeModeHorizontal != 0) {
        var newWidth = (EchoWindowPane.initialWindowWidth + 
                (e.clientX - EchoWindowPane.mouseOffsetX) * EchoWindowPane.resizeModeHorizontal);
        if (newWidth < EchoWindowPane.minimumWidth) {
            newWidth = EchoWindowPane.minimumWidth;
        } else if (newWidth > EchoWindowPane.maximumWidth) {
            newWidth = EchoWindowPane.maximumWidth;
        }
        EchoWindowPane.activeElement.style.width = newWidth + "px";
        if (EchoWindowPane.resizeModeHorizontal == -1) {
            var newX = EchoWindowPane.initialWindowX + EchoWindowPane.initialWindowWidth - newWidth;
            EchoWindowPane.activeElement.style.left = newX + "px";
        }
    }

    if (EchoWindowPane.resizeModeVertical != 0) {
        var newHeight = (EchoWindowPane.initialWindowHeight + 
                (e.clientY - EchoWindowPane.mouseOffsetY) * EchoWindowPane.resizeModeVertical);
        if (newHeight < EchoWindowPane.minimumHeight) {
            newHeight = EchoWindowPane.minimumHeight;
        } else if (newHeight > EchoWindowPane.maximumHeight) {
            newHeight = EchoWindowPane.maximumHeight;
        }
        EchoWindowPane.activeElement.style.height = newHeight + "px";
        if (EchoWindowPane.resizeModeVertical == -1) {
            var newY = EchoWindowPane.initialWindowY + EchoWindowPane.initialWindowHeight - newHeight;
            EchoWindowPane.activeElement.style.top = newY + "px";
        }

	    if ("true" == EchoDomPropertyStore.getPropertyValue(EchoWindowPane.activeElement.getAttribute("id"), "quirkResizeOnMove")) {
	        // Tickle window width because Internet Explorer will only recompute size of hidden IFRAME
	        // on *WIDTH* adjustments, not height adjustments (this is a bug in IE).
	        // This only need be applied when vertical resizes can be performed.
		    var initialWidth = parseInt(EchoWindowPane.activeElement.style.width);
		    EchoWindowPane.activeElement.style.width = (initialWidth + 1) + "px";
		    EchoWindowPane.activeElement.style.width = initialWidth + "px";
	    }
    }
};

EchoWindowPane.windowResizeMouseUp = function(e) {
    e = (e) ? e : ((window.event) ? window.event : "");

    var id = EchoWindowPane.activeElement.getAttribute("id");
    EchoClientMessage.setPropertyValue(id, "positionX",  EchoWindowPane.activeElement.style.left);
    EchoClientMessage.setPropertyValue(id, "positionY",  EchoWindowPane.activeElement.style.top);
    EchoClientMessage.setPropertyValue(id, "width",  EchoWindowPane.activeElement.style.width);
    EchoClientMessage.setPropertyValue(id, "height",  EchoWindowPane.activeElement.style.height);

    EchoWindowPane.activeElement = null;
    EchoDomUtil.removeEventListener(document, "mousemove", EchoWindowPane.windowResizeMouseMove, true);
    EchoDomUtil.removeEventListener(document, "mouseup", EchoWindowPane.windowResizeMouseUp, true);
    EchoDomUtil.removeEventListener(document, "selectstart", EchoWindowPane.selectStart, true);
};

/**
 * Event handler for "MouseDown" events on the close button.
 * Avoids close button mouse-down clicks from being processed by drag initiation handler.
 *
 * @param echoEvent the event
 */
EchoWindowPane.userCloseMouseDown = function(echoEvent) {
    EchoDomUtil.preventEventDefault(echoEvent);
};

/**
 * Event handler for "Click" events on the close button.
 * Notifies server of user action.
 *
 * @param echoEvent the event
 */
EchoWindowPane.userCloseClick = function(echoEvent) {
    if (EchoServerTransaction.active) {
        return;
    }
    var elementId = echoEvent.registeredTarget.getAttribute("id");
    var componentId = EchoDomUtil.getComponentId(elementId);
    EchoClientMessage.setActionValue(componentId, "close");
    EchoServerTransaction.connect();
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
