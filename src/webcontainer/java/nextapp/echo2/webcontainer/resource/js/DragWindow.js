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
// Object EchoDragWindow

/**
 * Static object to provide support for dragging virtual windows.
 *
 * Do not instantiate.
 */
function EchoDragWindow() { }

/** The initial horizontal position of the window. */
EchoDragWindow.initialWindowX = -1;

/** The initial vertical position of the window. */
EchoDragWindow.initialWindowY = -1;

/** The horizontal drag position of the mouse relative to the window. */
EchoDragWindow.mouseOffsetX = -1;

/** The vertical drag position of the mouse relative to the window. */
EchoDragWindow.mouseOffsetY = -1;

/** The active window DIV element being dragged */
EchoDragWindow.activeElement = null;

/** 
 * The maximum allowed horizontal position of the upper-left corner of 
 * the window. 
 */
EchoDragWindow.maxX = -1;

/** 
 * The maximum allowed vertical position of the upper-left corner of 
 * the window. 
 */
EchoDragWindow.maxY = -1;

/**
 * Calculates height of region in which window may be moved.
 *
 * @param element the container element within which the window may
 *        be moved
 * @return the width of the region
 */
EchoDragWindow.getClientHeight = function(element) {
    if (element.clientHeight && element.clientHeight > 0) {
        return element.clientHeight;
    } else if (element.parentNode) {
        return EchoDragWindow.getClientHeight(element.parentNode);
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
EchoDragWindow.getClientWidth = function(element) {
    if (element.clientWidth && element.clientWidth > 0) {
        return element.clientWidth;
    } else if (element.parentNode) {
        return EchoDragWindow.getClientWidth(element.parentNode);
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
EchoDragWindow.getBorderHeight = function(element) {
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
EchoDragWindow.getBorderWidth = function(element) {
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
EchoDragWindow.mouseDown = function(echoEvent) {
    EchoDomUtil.preventEventDefault(echoEvent);
    var mouseDownElement = echoEvent.registeredTarget.parentNode;
    if (mouseDownElement != EchoDragWindow.activeElement) {
        EchoDragWindow.activeElement = mouseDownElement;
        EchoDragWindow.mouseOffsetX = echoEvent.clientX;
        EchoDragWindow.mouseOffsetY = echoEvent.clientY;
        EchoDragWindow.initialWindowX = parseInt(EchoDragWindow.activeElement.style.left);
        EchoDragWindow.initialWindowY = parseInt(EchoDragWindow.activeElement.style.top);
        
        var borderWidth = 2 * EchoDragWindow.getBorderWidth(EchoDragWindow.activeElement);
        var borderHeight = 2 * EchoDragWindow.getBorderHeight(EchoDragWindow.activeElement);
        
        var containerWidth = EchoDragWindow.getClientWidth(EchoDragWindow.activeElement.parentNode);
        if (containerWidth === 0) {
            containerWidth = 800;
        }
        EchoDragWindow.maxX = containerWidth - EchoDragWindow.activeElement.clientWidth - borderWidth;
        if (EchoDragWindow.maxX < 0) {
            EchoDragWindow.maxX = 0;
        }

        var containerHeight = EchoDragWindow.getClientHeight(EchoDragWindow.activeElement.parentNode);
        if (containerHeight === 0) {
            containerHeight = 600;
        }
        EchoDragWindow.maxY = containerHeight- EchoDragWindow.activeElement.clientHeight - borderHeight;
        if (EchoDragWindow.maxY < 0) {
            EchoDragWindow.maxY = 0;
        }
        
        EchoDomUtil.addEventListener(document, "mousemove", EchoDragWindow.mouseMove, true);
        EchoDomUtil.addEventListener(document, "mouseup", EchoDragWindow.mouseUp, true);
        EchoDomUtil.addEventListener(document, "selectstart", EchoDragWindow.selectStart, true);
    }
};

/**
 * Event handler for "MouseMove" events.  Registered when drag is initiated.
 *
 * @param e The event (only provided when using DOM Level 2 Event Model)
 */
EchoDragWindow.mouseMove = function(e) {
    e = (e) ? e : ((window.event) ? window.event : "");
    var newX = (EchoDragWindow.initialWindowX + e.clientX - EchoDragWindow.mouseOffsetX);
    var newY = (EchoDragWindow.initialWindowY + e.clientY - EchoDragWindow.mouseOffsetY);
    
    newX = newX >= 0 ? newX : 0;
    newX = newX <= EchoDragWindow.maxX ? newX : EchoDragWindow.maxX;
    newY = newY >= 0 ? newY : 0;
    newY = newY <= EchoDragWindow.maxY ? newY : EchoDragWindow.maxY;
    EchoDragWindow.activeElement.style.left = newX + "px";
    EchoDragWindow.activeElement.style.top = newY + "px";
};

/**
 * Event handler for "MouseUp" events.  Registered when drag is initiated.
 *
 * @param e The event (only provided when using DOM Level 2 Event Model)
 */
EchoDragWindow.mouseUp = function(e) {
    e = (e) ? e : ((window.event) ? window.event : "");
    
    var id = EchoDragWindow.activeElement.getAttribute("id");
    EchoClientMessage.setPropertyValue(id, "positionX",  EchoDragWindow.activeElement.style.left);
    EchoClientMessage.setPropertyValue(id, "positionY",  EchoDragWindow.activeElement.style.top);
    
    EchoDragWindow.activeElement = null;
    EchoDomUtil.removeEventListener(document, "mousemove", EchoDragWindow.mouseMove, true);
    EchoDomUtil.removeEventListener(document, "mouseup", EchoDragWindow.mouseUp, true);
    EchoDomUtil.removeEventListener(document, "selectstart", EchoDragWindow.selectStart, true);
};

/**
 * Event handler for "MouseDown" events on the close button.
 * Avoids close button mouse-down clicks from being processed by drag initiation handler.
 *
 * @param echoEvent the event
 */
EchoDragWindow.userCloseMouseDown = function(echoEvent) {
    EchoDomUtil.preventEventDefault(echoEvent);
};

/**
 * Event handler for "Click" events on the close button.
 * Notifies server of user action.
 *
 * @param echoEvent the event
 */
EchoDragWindow.userCloseClick = function(echoEvent) {
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
EchoDragWindow.selectStart = function(e) {
    e = (e) ? e : ((window.event) ? window.event : "");
    EchoDomUtil.preventEventDefault(e);
};

EchoScriptLibraryManager.setStateLoaded("Echo.DragWindow");
