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

//__________________
// Object EchoButton

/**
 * Static object/namespace for Button support.
 * This object/namespace should not be used externally.
 */
EchoButton = function() { };

/**
 * Associative arary mapping button group ids to
 * arrays of button element ids.
 */
EchoButton.buttonGroupIdToButtonArrayMap = new Array();

/** State constant indicating default button state. */
EchoButton.STATE_DEFAULT = 0;

/** State constant indicating rollover button state. */
EchoButton.STATE_ROLLOVER = 1;

/** State constant indicating pressed button state. */
EchoButton.STATE_PRESSED = 2;

/**
 * Static object/namespace for Button MessageProcessor 
 * implementation.
 */
EchoButton.MessageProcessor = function() { };

/**
 * MessageProcessor process() implementation 
 * (invoked by ServerMessage processor).
 *
 * @param messagePartElement the <code>message-part</code> element to process.
 */
EchoButton.MessageProcessor.process = function(messagePartElement) {
    for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
        if (messagePartElement.childNodes[i].nodeType == 1) {
            switch (messagePartElement.childNodes[i].tagName) {
            case "init":
                EchoButton.MessageProcessor.processInit(messagePartElement.childNodes[i]);
                break;
            case "dispose":
                EchoButton.MessageProcessor.processDispose(messagePartElement.childNodes[i]);
                break;
            }
        }
    }
};

/**
 * Processes a <code>dispose</code> message to finalize the state of a
 * button component that is being removed.
 *
 * @param disposeMessageElement the <code>dispose</code> element to process
 */
EchoButton.MessageProcessor.processDispose = function(disposeMessageElement) {
    for (var item = disposeMessageElement.firstChild; item; item = item.nextSibling) {
        var elementId = item.getAttribute("eid");
        EchoEventProcessor.removeHandler(elementId, "click");
        EchoEventProcessor.removeHandler(elementId, "keypress");
        EchoEventProcessor.removeHandler(elementId, "mousedown");
        EchoEventProcessor.removeHandler(elementId, "mouseout");
        EchoEventProcessor.removeHandler(elementId, "mouseover");
        EchoEventProcessor.removeHandler(elementId, "mouseup");
    }
};

/**
 * Processes an <code>init</code> message to initialize the state of a 
 * button component that is being added.
 *
 * @param initMessageElement the <code>init</code> element to process
 */
EchoButton.MessageProcessor.processInit = function(initMessageElement) {
    var rolloverStyle = initMessageElement.getAttribute("rollover-style");
    var pressedStyle = initMessageElement.getAttribute("pressed-style");

    for (var item = initMessageElement.firstChild; item; item = item.nextSibling) {
        var elementId = item.getAttribute("eid");
        
        if (rolloverStyle) {
            EchoDomPropertyStore.setPropertyValue(elementId, "rolloverStyle", rolloverStyle);
        }
        if (pressedStyle) {
            EchoDomPropertyStore.setPropertyValue(elementId, "pressedStyle", pressedStyle);
        }
        if (item.getAttribute("enabled") == "false") {
            EchoDomPropertyStore.setPropertyValue(elementId, "EchoClientEngine.inputDisabled", true);
        }
        if (item.getAttribute("server-notify")) {
            EchoDomPropertyStore.setPropertyValue(elementId, "serverNotify", item.getAttribute("server-notify"));
        }
        if (item.getAttribute("default-icon")) {
            EchoDomPropertyStore.setPropertyValue(elementId, "defaultIcon", item.getAttribute("default-icon"));
        }
        if (item.getAttribute("rollover-icon")) {
            EchoDomPropertyStore.setPropertyValue(elementId, "rolloverIcon", item.getAttribute("rollover-icon"));
        }
        if (item.getAttribute("pressed-icon")) {
            EchoDomPropertyStore.setPropertyValue(elementId, "pressedIcon", item.getAttribute("pressed-icon"));
        }
        
        if (item.getAttribute("toggle") == "true") {
            // ToggleButton-specific properties.
            EchoDomPropertyStore.setPropertyValue(elementId, "toggle", "true");
            EchoDomPropertyStore.setPropertyValue(elementId, "selected", item.getAttribute("selected"));
            EchoDomPropertyStore.setPropertyValue(elementId, "stateIcon", item.getAttribute("state-icon"));
            EchoDomPropertyStore.setPropertyValue(elementId, "selectedStateIcon", item.getAttribute("selected-state-icon"));
            if (item.getAttribute("group")) {
                EchoButton.setButtonGroup(elementId, item.getAttribute("group"));
            }
            if (rolloverStyle) {
                EchoDomPropertyStore.setPropertyValue(elementId, "rolloverStateIcon", 
                        item.getAttribute("rollover-state-icon"));
                EchoDomPropertyStore.setPropertyValue(elementId, "rolloverSelectedStateIcon",
                        item.getAttribute("rollover-selected-state-icon"));
            }
            if (pressedStyle) {
                EchoDomPropertyStore.setPropertyValue(elementId, "pressedStateIcon", 
                        item.getAttribute("pressed-state-icon"));
                EchoDomPropertyStore.setPropertyValue(elementId, "pressedSelectedStateIcon",
                        item.getAttribute("pressed-selected-state-icon"));
            }
        }

        EchoEventProcessor.addHandler(elementId, "click", "EchoButton.processClick");
        EchoEventProcessor.addHandler(elementId, "keypress", "EchoButton.processKeyPressed");
        EchoEventProcessor.addHandler(elementId, "mousedown", "EchoButton.processPressed");
        EchoEventProcessor.addHandler(elementId, "mouseout", "EchoButton.processRolloverExit");
        EchoEventProcessor.addHandler(elementId, "mouseover", "EchoButton.processRolloverEnter");
        EchoEventProcessor.addHandler(elementId, "mouseup", "EchoButton.processReleased");
    }
};

/**
 * Deselects the selected radio button in the specified group.
 *
 * @param groupId the id of the button group to deselect
 */
EchoButton.deselectRadioButton = function(groupId) {
    var buttonArray = EchoButton.buttonGroupIdToButtonArrayMap[groupId];
    if (!buttonArray) {
        return;
    }
    for (var i = 0; i < buttonArray.length; ++i) {
        var elementId = buttonArray[i];
        if (EchoButton.getSelectionState(elementId)) {
            EchoButton.setSelectionState(elementId, false);
        }
    }
};

/**
 * Programmatically invokes a button's action.
 * Togglebuttons will have their state toggled as a result.
 * The server will be notified of the action in the event
 * the button has server-side <code>ActionListener</code>s.
 *
 * @param elementId the button element id
 */
EchoButton.doAction = function(elementId) {
    if ("true" == EchoDomPropertyStore.getPropertyValue(elementId, "toggle")) {
        EchoButton.doToggle(elementId);
    }
    
    if ("false" == EchoDomPropertyStore.getPropertyValue(elementId, "serverNotify")) {
        return;
    }
    
    if (document.selection && document.selection.empty) {
        document.selection.empty();
    }
    EchoClientMessage.setActionValue(elementId, "click");
    
    EchoServerTransaction.connect();
};

/**
 * Toggles the state of a toggle button.
 *
 * @param elementId the button element id
 */
EchoButton.doToggle = function(elementId) {
    var newState = !EchoButton.getSelectionState(elementId);
    
    var groupId = EchoButton.getButtonGroup(elementId);
    if (groupId) {
        if (!newState) {
            // Clicking on selected radio button: do nothing.
            return;
        }
        EchoButton.deselectRadioButton(groupId);
    }
    
    EchoButton.setSelectionState(elementId, newState);
};

/**
 * Determines the button group id of a specific radio button.
 *
 * @param elementId the radio button element id
 * @return the button group id
 */
EchoButton.getButtonGroup = function(elementId) {
    return EchoDomPropertyStore.getPropertyValue(elementId, "buttonGroup");
};

/**
 * Determines the selection state of a specific toggle button.
 *
 * @param elementId the toggle button element id
 * @return true if the button is selected
 */
EchoButton.getSelectionState = function(elementId) {
    return "true" == EchoDomPropertyStore.getPropertyValue(elementId, "selected");
};

/**
 * Forces quirky IE clients to repaint immediately for a sometimes very 
 * signficant aesthetic performance improvement.
 * Invoking this method performs no operation/has no effect for other browser 
 * clients that do not suffer this quirk.
 *
 * @param the button element to force repaint
 */
EchoButton.ieRepaint = function(buttonElement) {
    if (EchoClientProperties.get("quirkIERepaint")) {
        var originalWidth = buttonElement.style.width;
        var temporaryWidth = parseInt(buttonElement.clientWidth) + 1;
        buttonElement.style.width = temporaryWidth + "px";
        buttonElement.style.width = originalWidth;
    }
};

/**
 * Processes a button mouse click event.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoButton.processClick = function(echoEvent) {
    var elementId = echoEvent.registeredTarget.getAttribute("id");
    if (!EchoClientEngine.verifyInput(elementId)) {
        return;
    }
    EchoButton.doAction(elementId);
};

/**
 * Processes a button key press event.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoButton.processKeyPressed = function(echoEvent) {
    var elementId = echoEvent.registeredTarget.getAttribute("id");
    if (!EchoClientEngine.verifyInput(elementId)) {
        return;
    }
    if (echoEvent.keyCode == 13 || echoEvent.keyCode == 32) {
        EchoButton.doAction(elementId);
    }
};

/**
 * Processes a button mouse press event.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoButton.processPressed = function(echoEvent) {
    EchoDomUtil.preventEventDefault(echoEvent);
    var elementId = echoEvent.registeredTarget.getAttribute("id");
    if (!EchoClientEngine.verifyInput(elementId)) {
        return;
    }
    EchoButton.setVisualState(echoEvent.registeredTarget, EchoButton.STATE_PRESSED);
};

/**
 * Processes a button mouse release event.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoButton.processReleased = function(echoEvent) {
    EchoButton.setVisualState(echoEvent.registeredTarget, EchoButton.STATE_DEFAULT);
};

/**
 * Processes a button mouse rollover enter event.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoButton.processRolloverEnter = function(echoEvent) {
    var elementId = echoEvent.registeredTarget.getAttribute("id");
    if (!EchoClientEngine.verifyInput(elementId)) {
        return;
    }
    EchoButton.setVisualState(echoEvent.registeredTarget, EchoButton.STATE_ROLLOVER);
};

/**
 * Processes a button mouse rollover exit event.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoButton.processRolloverExit = function(echoEvent) {
    EchoButton.setVisualState(echoEvent.registeredTarget, EchoButton.STATE_DEFAULT);
};

/**
 * Sets the button group id of a specific radio button.
 *
 * @param elementId the button element id
 * @param groupId the id of the button group
 */
EchoButton.setButtonGroup = function(elementId, groupId) {
    var buttonArray = EchoButton.buttonGroupIdToButtonArrayMap[groupId];
    if (!buttonArray) {
        buttonArray = new Array();
        EchoButton.buttonGroupIdToButtonArrayMap[groupId] = buttonArray;
    }
    buttonArray.push(elementId);
    EchoDomPropertyStore.setPropertyValue(elementId, "buttonGroup", groupId);
};

/**
 * Sets the displayed icon of a specific button.
 *
 * @param elementId the button element id
 * @param newIconUri the URI of the new icon to display
 */
EchoButton.setIcon = function(elementId, newIconUri) {
    var iconElement = document.getElementById(elementId + "_icon");
    iconElement.src = newIconUri;
};

/**
 * Sets the displayed state icon of a specific button.
 *
 * @param elementId the button element id
 * @param newStateIconUri the URI of the new state icon to display
 */
EchoButton.setStateIcon = function(elementId, newStateIconUri) {
    var iconElement = document.getElementById(elementId + "_stateicon");
    iconElement.src = newStateIconUri;
};

/**
 * Sets the selection state of a toggle button.
 *
 * @param elementId the id of the toggle button
 * @param newState a boolean flag indicating the new selection state
 */
EchoButton.setSelectionState = function(elementId, newState) {
    EchoDomPropertyStore.setPropertyValue(elementId, "selected", newState ? "true" : "false");

    var stateIconUri = EchoDomPropertyStore.getPropertyValue(elementId, newState ? "selectedStateIcon" : "stateIcon");
    if (!stateIconUri) {
        throw "State icon not specified for selection state: " + newState;
    }
    var imgElement = document.getElementById(elementId + "_stateicon");
    imgElement.src = stateIconUri;
    
    EchoClientMessage.setPropertyValue(elementId, "selected", newState ? "true" : "false");
};

/**
 * Sets the visual state of a specific button.
 *
 * @param buttonElement the button element
 * @param newState the new visual state, one of the following values:
 *        <ul>
 *         <li><code>EchoButton.STATE_DEFFAULT</code></li>
 *         <li><code>EchoButton.STATE_PRESSED</code></li>
 *         <li><code>EchoButton.STATE_ROLLOVER</code></li>
 *        </ul>
 */
EchoButton.setVisualState = function(buttonElement, newState) {
    if (buttonElement.nodeType != 1) {
        // Prevent TextNode events.
        return;
    }

    var selectionState = EchoButton.getSelectionState(buttonElement.id);
    var newStyle, newIcon, newStateIcon;
    
    switch (newState) {
    case EchoButton.STATE_ROLLOVER:
        newStyle = EchoDomPropertyStore.getPropertyValue(buttonElement.id, "rolloverStyle");
        newIcon = EchoDomPropertyStore.getPropertyValue(buttonElement.id, "rolloverIcon");
        newStateIcon = EchoDomPropertyStore.getPropertyValue(buttonElement.id, 
                selectionState ? "rolloverSelectedStateIcon" : "rolloverStateIcon");
        break;
    case EchoButton.STATE_PRESSED:
        newStyle = EchoDomPropertyStore.getPropertyValue(buttonElement.id, "pressedStyle");
        newIcon = EchoDomPropertyStore.getPropertyValue(buttonElement.id, "pressedIcon");
        newStateIcon = EchoDomPropertyStore.getPropertyValue(buttonElement.id, 
                selectionState ? "pressedSelectedStateIcon" : "pressedStateIcon");
        break;
    default:
        newIcon = EchoDomPropertyStore.getPropertyValue(buttonElement.id, "defaultIcon");
        newStateIcon = EchoDomPropertyStore.getPropertyValue(buttonElement.id, 
                selectionState ? "selectedStateIcon" : "stateIcon");
    }
    if (newIcon) {
        EchoButton.setIcon(buttonElement.id, newIcon);
    }
    if (newStateIcon) {
        EchoButton.setStateIcon(buttonElement.id, newStateIcon);
    }
    EchoCssUtil.restoreOriginalStyle(buttonElement);
    if (newStyle) {
        EchoCssUtil.applyTemporaryStyle(buttonElement, newStyle);
    }
    EchoButton.ieRepaint(buttonElement);
};
