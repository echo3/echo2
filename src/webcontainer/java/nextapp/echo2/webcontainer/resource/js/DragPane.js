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

// ___________________
// Object EchoDragPane

/**
 * Static object to provide support for dragging virtual panes.
 *
 * Do not instantiate.
 */
function EchoDragPane() { }

EchoDragPane.DEFAULT_MINIMUM_SIZE = 80;
EchoDragPane.verticalDrag = false;
EchoDragPane.initialWindowPosition = -1;
EchoDragPane.mouseOffset = -1;
EchoDragPane.activePaneId = null;
EchoDragPane.activePaneDiv = null;
EchoDragPane.activePane0Div = null;
EchoDragPane.activePane1Div = null;
EchoDragPane.activePane0MinimumSize = -1;
EchoDragPane.activePane1MinimumSize = -1;
EchoDragPane.activePane0MaximumSize = -1;
EchoDragPane.activePane1MaximumSize = -1;
EchoDragPane.activePaneSeparatorDiv = null;

/**
 * Event handler for "MouseDown" events.  Permanently registered.
 *
 * @param echoEvent the "MouseDown" event, preprocessed by the
 *        EchoEventProcessor
 */
EchoDragPane.mouseDown = function(echoEvent) {
    var mouseDownElement = echoEvent.target;
    if (mouseDownElement != EchoDragPane.separatorElement) {
        EchoDragPane.activePaneSeparatorDiv = mouseDownElement;
        EchoDragPane.verticalDrag = EchoDragPane.activePaneSeparatorDiv.style.cursor == "s-resize";
        EchoDragPane.activePaneId = mouseDownElement.parentNode.getAttribute("id");
        EchoDragPane.activePaneDiv = document.getElementById(EchoDragPane.activePaneId);
        EchoDragPane.activePane0Div = document.getElementById(EchoDragPane.activePaneId + "_pane_0");
        EchoDragPane.activePane0MinimumSize = parseInt(EchoDomPropertyStore.getPropertyValue(
                EchoDragPane.activePaneId + "_pane_0", "minimumSize"));
        if (isNaN(EchoDragPane.activePane0MinimumSize)) {
            EchoDragPane.activePane0MinimumSize = EchoDragPane.DEFAULT_MINIMUM_SIZE;
        }
        EchoDragPane.activePane1Div = document.getElementById(EchoDragPane.activePaneId + "_pane_1");
        EchoDragPane.activePane1MinimumSize = parseInt(EchoDomPropertyStore.getPropertyValue(
                EchoDragPane.activePaneId + "_pane_1", "minimumSize"));
        if (isNaN(EchoDragPane.activePane1MinimumSize)) {
            EchoDragPane.activePane1MinimumSize = EchoDragPane.DEFAULT_MINIMUM_SIZE;
        }
        EchoDragPane.activePane0MaximumSize = parseInt(EchoDomPropertyStore.getPropertyValue(
                EchoDragPane.activePaneId + "_pane_0", "maximumSize"));
        if (isNaN(EchoDragPane.activePane0MaximumSize)) {
            EchoDragPane.activePane0MaximumSize = -1;
        }
        EchoDragPane.activePane1Div = document.getElementById(EchoDragPane.activePaneId + "_pane_1");
        EchoDragPane.activePane1MaximumSize = parseInt(EchoDomPropertyStore.getPropertyValue(
                EchoDragPane.activePaneId + "_pane_1", "maximumSize"));
        if (isNaN(EchoDragPane.activePane1MaximumSize)) {
            EchoDragPane.activePane1MaximumSize = -1;
        }
        if (EchoDragPane.verticalDrag) {
            EchoDragPane.mouseOffset = echoEvent.clientY;
            EchoDragPane.initialWindowPosition = parseInt(EchoDragPane.activePaneSeparatorDiv.style.top);
        } else {
            EchoDragPane.mouseOffset = echoEvent.clientX;
            EchoDragPane.initialWindowPosition = parseInt(EchoDragPane.activePaneSeparatorDiv.style.left);
        }
        EchoDomUtil.addEventListener(document, "mousemove", EchoDragPane.mouseMove, true);
        EchoDomUtil.addEventListener(document, "mouseup", EchoDragPane.mouseUp, true);
        EchoDomUtil.addEventListener(document, "selectstart", EchoDragPane.selectStart, true);
    }
};

/**
 * Constrains the movement of the separator.
 *
 * @param newSeparatorPosition the user-requested position of the separator
 * @param regionSize the size of the entire region in which the separator might
 *        theoretically move
 * @return a constrained value indicating the permissable separator position
 *         (if the originally provided separator position was valid, the same
 *         value will be returned).
 */
EchoDragPane.constrainSeparatorPosition = function(newSeparatorPosition, regionSize) {
    // Constrain pane 1 maximum size.
    if (EchoDragPane.activePane1MaximumSize > 0 && newSeparatorPosition < regionSize - EchoDragPane.activePane1MaximumSize) {
        newSeparatorPosition = regionSize - EchoDragPane.activePane1MaximumSize;
    }
    // Constrain pane 0 maximum size.
    if (EchoDragPane.activePane0MaximumSize > 0 && newSeparatorPosition > EchoDragPane.activePane0MaximumSize) {
        newSeparatorPosition = EchoDragPane.activePane0MaximumSize;
    }
    // Constrain pane 1 minimum size.
    if (newSeparatorPosition > regionSize - EchoDragPane.activePane1MinimumSize) {
        newSeparatorPosition = regionSize - EchoDragPane.activePane1MinimumSize;
    }
    // Constrain pane 0 minimum size.
    if (newSeparatorPosition < EchoDragPane.activePane0MinimumSize) {
        newSeparatorPosition = EchoDragPane.activePane0MinimumSize;
    }
    // Return constrained position.
    return newSeparatorPosition;
};

/**
 * Event handler for "MouseMove" events.  
 * Registered when drag is initiated, deregistered when drag is complete.
 *
 * @param e The event (only provided when using DOM Level 2 Event Model)
 */
EchoDragPane.mouseMove = function(e) {
    e = (e) ? e : ((window.event) ? window.event : "");
    
    if (!EchoDragPane.activePaneId) {
        // Handle invalid state.
        EchoDragPane.dispose();
        return;
    }
    
    var newSeparatorBegin, newSeparatorEnd;
    if (EchoDragPane.verticalDrag) {
        newSeparatorBegin = (EchoDragPane.initialWindowPosition + e.clientY - EchoDragPane.mouseOffset);
        newSeparatorBegin = EchoDragPane.constrainSeparatorPosition(newSeparatorBegin, EchoDragPane.activePaneDiv.clientHeight);
        newSeparatorEnd = newSeparatorBegin + 4;

        EchoDragPane.activePaneSeparatorDiv.style.top = newSeparatorBegin + "px";
        if (EchoDragPane.activePane0Div) {
            EchoDragPane.activePane0Div.style.height = newSeparatorBegin + "px";
        }
        if (EchoDragPane.activePane1Div) {
            EchoDragPane.activePane1Div.style.top = newSeparatorEnd + "px";
            if (EchoDragPane.activePane1Div.style.setExpression) {
                EchoDragPane.activePane1Div.style.setExpression("height", "(document.getElementById('" 
                        + EchoDragPane.activePaneId + "').clientHeight-" + newSeparatorEnd + ") + 'px'");
            }
        }
    } else {
        newSeparatorBegin = (EchoDragPane.initialWindowPosition + e.clientX - EchoDragPane.mouseOffset);
        newSeparatorBegin = EchoDragPane.constrainSeparatorPosition(newSeparatorBegin, EchoDragPane.activePaneDiv.clientWidth);
        newSeparatorEnd = newSeparatorBegin + 4;

        EchoDragPane.activePaneSeparatorDiv.style.left = newSeparatorBegin + "px";
        if (EchoDragPane.activePane0Div) {
            EchoDragPane.activePane0Div.style.width = newSeparatorBegin + "px";
        }
        if (EchoDragPane.activePane1Div) {
            EchoDragPane.activePane1Div.style.left = newSeparatorEnd + "px";
            if (EchoDragPane.activePane1Div.style.setExpression) {
                EchoDragPane.activePane1Div.style.setExpression("width", "(document.getElementById('" 
                        + EchoDragPane.activePaneId + "').clientWidth-" + newSeparatorEnd + ") + 'px'");
            }
        }
    }
};

/**
 * Event handler for "MouseUp" events.
 * Registered when drag is initiated, deregistered when drag is complete.
 *
 * @param e The event (only provided when using DOM Level 2 Event Model)
 */
EchoDragPane.mouseUp = function(e) {
    e = (e) ? e : ((window.event) ? window.event : "");
    
    if (EchoDragPane.verticalDrag) {
        EchoClientMessage.setPropertyValue(EchoDragPane.activePaneId, "separatorPosition", 
                EchoDragPane.activePaneSeparatorDiv.style.top);
    } else {
        EchoClientMessage.setPropertyValue(EchoDragPane.activePaneId, "separatorPosition", 
                EchoDragPane.activePaneSeparatorDiv.style.left);
    }
    
    EchoDragPane.dispose();
};

/**
 * End-of-lifecycle method to dispose of resources and deregister
 * event listeners.  This method is invoked when the draggable
 * pane is removed from the DOM.
 */
EchoDragPane.dispose = function() {
    EchoDragPane.activePaneId = null;
    EchoDragPane.activePaneDiv = null;
    EchoDragPane.activePane0Div = null;
    EchoDragPane.activePane1Div = null;
    EchoDragPane.activePaneSeparatorDiv = null;

    EchoDomUtil.removeEventListener(document, "mousemove", EchoDragPane.mouseMove, true);
    EchoDomUtil.removeEventListener(document, "mouseup", EchoDragPane.mouseUp, true);
    EchoDomUtil.removeEventListener(document, "selectstart", EchoDragPane.selectStart, true);
};

/**
 * Event handler for "SelectStart" events to disable selection while dragging.
 * Registered when drag is initiated, deregistered when drag is complete.
 *
 * @param e The event (only provided when using DOM Level 2 Event Model)
 */
EchoDragPane.selectStart = function(e) {
    e = (e) ? e : ((window.event) ? window.event : "");
    EchoDomUtil.preventEventDefault(e);
};

EchoScriptLibraryManager.setStateLoaded("Echo.DragPane");
