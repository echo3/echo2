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

// ____________________
// Object EchoSplitPane

/**
 * Static object/namespace for SplitPane support.
 * This object/namespace should not be used externally.
 */
EchoSplitPane = function() { };

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
        if (messagePartElement.childNodes[i].nodeType == 1) {
            switch (messagePartElement.childNodes[i].tagName) {
            case "dispose":
                EchoSplitPane.MessageProcessor.processDispose(messagePartElement.childNodes[i]);
                break;
            case "init":
                EchoSplitPane.MessageProcessor.processInit(messagePartElement.childNodes[i]);
                break;
            case "update-pane":
                EchoSplitPane.MessageProcessor.processUpdatePane(messagePartElement.childNodes[i]);
                break;
            }
        }
    }
};

/**
 * Processes a <code>dispose</code> message to finalize the state of a
 * split pane component that is being removed.
 *
 * @param disposeMessageElement the <code>dispose</code> element to process
 */
EchoSplitPane.MessageProcessor.processDispose = function(disposeMessageElement) {
    for (var item = disposeMessageElement.firstChild; item; item = item.nextSibling) {
        var elementId = item.getAttribute("eid");
        EchoEventProcessor.removeHandler(elementId + "_separator", "mousedown");
    }
};

/**
 * Processes an <code>init</code> message to initialize the state of a 
 * split pane component that is being added.
 *
 * @param initMessageElement the <code>init</code> element to process
 */
EchoSplitPane.MessageProcessor.processInit = function(initMessageElement) {
    for (var item = initMessageElement.firstChild; item; item = item.nextSibling) {
        var elementId = item.getAttribute("eid");
        var resizable = item.getAttribute("resizable") == "true";
        
        EchoDomPropertyStore.setPropertyValue(elementId, "topLeftPane", item.getAttribute("top-left-pane"));
        if (resizable) {
            EchoEventProcessor.addHandler(elementId + "_separator", "mousedown", "EchoSplitPane.mouseDown");
        }
    }
};

/**
 * Processes an <code>update-pane</code> message to update non-rendered 
 * properties of a specific pane.
 *
 * @param updatePaneMessageElement the <code>update-pane</code> element to 
 *        process
 */
EchoSplitPane.MessageProcessor.processUpdatePane = function(updatePaneMessageElement) {
    for (var item = updatePaneMessageElement.firstChild; item; item = item.nextSibling) {
        var elementId = item.getAttribute("eid");
        if (item.getAttribute("minimum-size")) {
            EchoDomPropertyStore.setPropertyValue(elementId, "minimumSize", item.getAttribute("minimum-size"));
        }
        if (item.getAttribute("maximum-size")) {
            EchoDomPropertyStore.setPropertyValue(elementId, "maximumSize", item.getAttribute("maximum-size"));
        }
    }
};

EchoSplitPane.DEFAULT_MINIMUM_SIZE = 80;
EchoSplitPane.verticalDrag = false;
EchoSplitPane.topLeftPane = 0;
EchoSplitPane.initialWindowPosition = -1;
EchoSplitPane.mouseOffset = -1;
EchoSplitPane.activePaneId = null;
EchoSplitPane.activePaneDiv = null;
EchoSplitPane.activePane0Div = null;
EchoSplitPane.activePane1Div = null;
EchoSplitPane.activePane0MinimumSize = -1;
EchoSplitPane.activePane1MinimumSize = -1;
EchoSplitPane.activePane0MaximumSize = -1;
EchoSplitPane.activePane1MaximumSize = -1;
EchoSplitPane.separatorSize = 0;
EchoSplitPane.activePaneSeparatorDiv = null;

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
EchoSplitPane.constrainSeparatorPosition = function(newSeparatorPosition, regionSize) {
    // Constrain pane 1 maximum size.
    if (EchoSplitPane.activePane1MaximumSize > 0 && newSeparatorPosition < regionSize - EchoSplitPane.activePane1MaximumSize) {
        newSeparatorPosition = regionSize - EchoSplitPane.activePane1MaximumSize;
    }
    // Constrain pane 0 maximum size.
    if (EchoSplitPane.activePane0MaximumSize > 0 && newSeparatorPosition > EchoSplitPane.activePane0MaximumSize) {
        newSeparatorPosition = EchoSplitPane.activePane0MaximumSize;
    }
    // Constrain pane 1 minimum size.
    if (newSeparatorPosition > regionSize - EchoSplitPane.activePane1MinimumSize) {
        newSeparatorPosition = regionSize - EchoSplitPane.activePane1MinimumSize;
    }
    // Constrain pane 0 minimum size.
    if (newSeparatorPosition < EchoSplitPane.activePane0MinimumSize) {
        newSeparatorPosition = EchoSplitPane.activePane0MinimumSize;
    }
    // Return constrained position.
    return newSeparatorPosition;
};

/**
 * End-of-lifecycle method to dispose of resources and deregister
 * event listeners.  This method is invoked when the draggable
 * pane is removed from the DOM.
 */
EchoSplitPane.dispose = function() {
    EchoSplitPane.activePaneId = null;
    EchoSplitPane.activePaneDiv = null;
    EchoSplitPane.activePane0Div = null;
    EchoSplitPane.activePane1Div = null;
    EchoSplitPane.activePaneSeparatorDiv = null;
    EchoSplitPane.separatorSize = 0;
    EchoDomUtil.removeEventListener(document, "mousemove", EchoSplitPane.mouseMove, true);
    EchoDomUtil.removeEventListener(document, "mouseup", EchoSplitPane.mouseUp, true);
    EchoDomUtil.removeEventListener(document, "selectstart", EchoSplitPane.selectStart, true);
};

/**
 * Event handler for "MouseDown" events.  Permanently registered using 
 * EchoEventProcessor.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoSplitPane.mouseDown = function(echoEvent) {
    var elementId = echoEvent.registeredTarget.getAttribute("id");
    if (!EchoClientEngine.verifyInput(elementId)) {
        return;
    }
    var mouseDownElement = echoEvent.target;
    if (mouseDownElement != EchoSplitPane.separatorElement) {
        EchoSplitPane.activePaneSeparatorDiv = mouseDownElement;
        EchoSplitPane.verticalDrag = EchoSplitPane.activePaneSeparatorDiv.style.cursor == "s-resize";
        if (EchoSplitPane.verticalDrag) {
            EchoSplitPane.separatorSize = parseInt(mouseDownElement.style.height);
        } else {
            EchoSplitPane.separatorSize = parseInt(mouseDownElement.style.width);
        }
        EchoSplitPane.activePaneId = mouseDownElement.parentNode.getAttribute("id");
        EchoSplitPane.topLeftPane = EchoDomPropertyStore.getPropertyValue(
                EchoSplitPane.activePaneId, "topLeftPane") == "1" ? 1 : 0;
        EchoSplitPane.activePaneDiv = document.getElementById(EchoSplitPane.activePaneId);
        EchoSplitPane.activePane0Div = document.getElementById(EchoSplitPane.activePaneId + "_pane_0");
        EchoSplitPane.activePane0MinimumSize = parseInt(EchoDomPropertyStore.getPropertyValue(
                EchoSplitPane.activePaneId + "_pane_0", "minimumSize"));
        if (isNaN(EchoSplitPane.activePane0MinimumSize)) {
            EchoSplitPane.activePane0MinimumSize = EchoSplitPane.DEFAULT_MINIMUM_SIZE;
        }
        EchoSplitPane.activePane1Div = document.getElementById(EchoSplitPane.activePaneId + "_pane_1");
        EchoSplitPane.activePane1MinimumSize = parseInt(EchoDomPropertyStore.getPropertyValue(
                EchoSplitPane.activePaneId + "_pane_1", "minimumSize"));
        if (isNaN(EchoSplitPane.activePane1MinimumSize)) {
            EchoSplitPane.activePane1MinimumSize = EchoSplitPane.DEFAULT_MINIMUM_SIZE;
        }
        EchoSplitPane.activePane0MaximumSize = parseInt(EchoDomPropertyStore.getPropertyValue(
                EchoSplitPane.activePaneId + "_pane_0", "maximumSize"));
        if (isNaN(EchoSplitPane.activePane0MaximumSize)) {
            EchoSplitPane.activePane0MaximumSize = -1;
        }
        EchoSplitPane.activePane1Div = document.getElementById(EchoSplitPane.activePaneId + "_pane_1");
        EchoSplitPane.activePane1MaximumSize = parseInt(EchoDomPropertyStore.getPropertyValue(
                EchoSplitPane.activePaneId + "_pane_1", "maximumSize"));
        if (isNaN(EchoSplitPane.activePane1MaximumSize)) {
            EchoSplitPane.activePane1MaximumSize = -1;
        }
        if (EchoSplitPane.verticalDrag) {
            EchoSplitPane.mouseOffset = echoEvent.clientY;
            if (EchoSplitPane.topLeftPane == 1) {
                EchoSplitPane.initialWindowPosition = parseInt(EchoSplitPane.activePaneSeparatorDiv.style.bottom);
            } else {
                EchoSplitPane.initialWindowPosition = parseInt(EchoSplitPane.activePaneSeparatorDiv.style.top);
            }
        } else {
            EchoSplitPane.mouseOffset = echoEvent.clientX;
            if (EchoSplitPane.topLeftPane == 1) {
                EchoSplitPane.initialWindowPosition = parseInt(EchoSplitPane.activePaneSeparatorDiv.style.right);
            } else {
                EchoSplitPane.initialWindowPosition = parseInt(EchoSplitPane.activePaneSeparatorDiv.style.left);
            }
        }
        EchoDomUtil.addEventListener(document, "mousemove", EchoSplitPane.mouseMove, true);
        EchoDomUtil.addEventListener(document, "mouseup", EchoSplitPane.mouseUp, true);
        EchoDomUtil.addEventListener(document, "selectstart", EchoSplitPane.selectStart, true);
    }
};

/**
 * Event handler for "MouseMove" events.  
 * Registered when drag is initiated, deregistered when drag is complete.
 *
 * @param e The event (only provided when using DOM Level 2 Event Model)
 */
EchoSplitPane.mouseMove = function(e) {
    e = (e) ? e : ((window.event) ? window.event : "");
    
    if (!EchoSplitPane.activePaneId) {
        // Handle invalid state.
        EchoSplitPane.dispose();
        return;
    }
    
    var newSeparatorBegin, newSeparatorEnd;
    if (EchoSplitPane.verticalDrag) {
        if (EchoSplitPane.topLeftPane == 1) {
            newSeparatorBegin = (EchoSplitPane.initialWindowPosition - e.clientY + EchoSplitPane.mouseOffset);
            newSeparatorBegin = EchoSplitPane.constrainSeparatorPosition(newSeparatorBegin, 
                    EchoSplitPane.activePaneDiv.clientHeight);
            newSeparatorEnd = newSeparatorBegin + EchoSplitPane.separatorSize;
            EchoSplitPane.activePaneSeparatorDiv.style.bottom = newSeparatorBegin + "px";
            if (EchoSplitPane.activePane0Div) {
                EchoSplitPane.activePane0Div.style.height = newSeparatorBegin + "px";
            }
            if (EchoSplitPane.activePane1Div) {
                EchoSplitPane.activePane1Div.style.bottom = newSeparatorEnd + "px";
                if (EchoSplitPane.activePane1Div.style.setExpression) {
                    EchoSplitPane.activePane1Div.style.setExpression("height", "(document.getElementById('" 
                            + EchoSplitPane.activePaneId + "').clientHeight-" + newSeparatorEnd + ") + 'px'");
                }
            }
        } else {
            newSeparatorBegin = (EchoSplitPane.initialWindowPosition + e.clientY - EchoSplitPane.mouseOffset);
            newSeparatorBegin = EchoSplitPane.constrainSeparatorPosition(newSeparatorBegin, 
                    EchoSplitPane.activePaneDiv.clientHeight);
            newSeparatorEnd = newSeparatorBegin + EchoSplitPane.separatorSize;
            EchoSplitPane.activePaneSeparatorDiv.style.top = newSeparatorBegin + "px";
            if (EchoSplitPane.activePane0Div) {
                EchoSplitPane.activePane0Div.style.height = newSeparatorBegin + "px";
            }
            if (EchoSplitPane.activePane1Div) {
                EchoSplitPane.activePane1Div.style.top = newSeparatorEnd + "px";
                if (EchoSplitPane.activePane1Div.style.setExpression) {
                    EchoSplitPane.activePane1Div.style.setExpression("height", "(document.getElementById('" 
                            + EchoSplitPane.activePaneId + "').clientHeight-" + newSeparatorEnd + ") + 'px'");
                }
            }
        }
    } else {
        if (EchoSplitPane.topLeftPane == 1) {
            newSeparatorBegin = (EchoSplitPane.initialWindowPosition - e.clientX + EchoSplitPane.mouseOffset);
            newSeparatorBegin = EchoSplitPane.constrainSeparatorPosition(newSeparatorBegin, 
                    EchoSplitPane.activePaneDiv.clientWidth);
            newSeparatorEnd = newSeparatorBegin + EchoSplitPane.separatorSize;
            EchoSplitPane.activePaneSeparatorDiv.style.right = newSeparatorBegin + "px";
            if (EchoSplitPane.activePane0Div) {
                EchoSplitPane.activePane0Div.style.width = newSeparatorBegin + "px";
            }
            if (EchoSplitPane.activePane1Div) {
                EchoSplitPane.activePane1Div.style.right = newSeparatorEnd + "px";
                if (EchoSplitPane.activePane1Div.style.setExpression) {
                    EchoSplitPane.activePane1Div.style.setExpression("width", "(document.getElementById('" 
                            + EchoSplitPane.activePaneId + "').clientWidth-" + newSeparatorEnd + ") + 'px'");
                }
            }
        } else {
            newSeparatorBegin = (EchoSplitPane.initialWindowPosition + e.clientX - EchoSplitPane.mouseOffset);
            newSeparatorBegin = EchoSplitPane.constrainSeparatorPosition(newSeparatorBegin, 
                    EchoSplitPane.activePaneDiv.clientWidth);
            newSeparatorEnd = newSeparatorBegin + EchoSplitPane.separatorSize;
            EchoSplitPane.activePaneSeparatorDiv.style.left = newSeparatorBegin + "px";
            if (EchoSplitPane.activePane0Div) {
                EchoSplitPane.activePane0Div.style.width = newSeparatorBegin + "px";
            }
            if (EchoSplitPane.activePane1Div) {
                EchoSplitPane.activePane1Div.style.left = newSeparatorEnd + "px";
                if (EchoSplitPane.activePane1Div.style.setExpression) {
                    EchoSplitPane.activePane1Div.style.setExpression("width", "(document.getElementById('" 
                            + EchoSplitPane.activePaneId + "').clientWidth-" + newSeparatorEnd + ") + 'px'");
                }
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
EchoSplitPane.mouseUp = function(e) {
    if (EchoSplitPane.activePaneId) {
        e = (e) ? e : ((window.event) ? window.event : "");
    
        if (EchoSplitPane.verticalDrag) {
            if (EchoSplitPane.topLeftPane == 1) {
                EchoClientMessage.setPropertyValue(EchoSplitPane.activePaneId, "separatorPosition", 
                        EchoSplitPane.activePaneSeparatorDiv.style.bottom);
            } else {
                EchoClientMessage.setPropertyValue(EchoSplitPane.activePaneId, "separatorPosition", 
                        EchoSplitPane.activePaneSeparatorDiv.style.top);
            }
        } else {
            if (EchoSplitPane.topLeftPane == 1) {
                EchoClientMessage.setPropertyValue(EchoSplitPane.activePaneId, "separatorPosition", 
                        EchoSplitPane.activePaneSeparatorDiv.style.right);
            } else {
                EchoClientMessage.setPropertyValue(EchoSplitPane.activePaneId, "separatorPosition", 
                        EchoSplitPane.activePaneSeparatorDiv.style.left);
            }
        }
    }
    
    EchoSplitPane.dispose();
};

/**
 * Event handler for "SelectStart" events to disable selection while dragging.
 * Registered when drag is initiated, deregistered when drag is complete.
 *
 * @param e The event (only provided when using DOM Level 2 Event Model)
 */
EchoSplitPane.selectStart = function(e) {
    e = (e) ? e : ((window.event) ? window.event : "");
    EchoDomUtil.preventEventDefault(e);
};
