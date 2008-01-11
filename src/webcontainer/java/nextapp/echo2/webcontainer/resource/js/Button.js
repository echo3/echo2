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
 * <p>
 * Creates a new Button data object.
 */
EchoButton = function(elementId) { 
    this.elementId = elementId;
};

/** State constant indicating default button state. */
EchoButton.STATE_DEFAULT = 0;

/** State constant indicating rollover button state. */
EchoButton.STATE_ROLLOVER = 1;

/** State constant indicating pressed button state. */
EchoButton.STATE_PRESSED = 2;

/**
 * Disposes of a button data object, unregistering event listeners
 * and cleaning up resources of underlying element.
 */
EchoButton.prototype.dispose = function() {
    var element = this.getElement();
    EchoEventProcessor.removeHandler(element, "click");
    EchoEventProcessor.removeHandler(element, "keypress");
    EchoEventProcessor.removeHandler(element, "mousedown");
    EchoEventProcessor.removeHandler(element, "mouseup");
    if (EchoClientProperties.get("proprietaryEventMouseEnterLeaveSupported")) {
        EchoEventProcessor.removeHandler(element, "mouseenter");
        EchoEventProcessor.removeHandler(element, "mouseleave");
    } else {
        EchoEventProcessor.removeHandler(element, "mouseout");
        EchoEventProcessor.removeHandler(element, "mouseover");
    }
    
    if (this.groupId) {
        EchoButton.Group.remove(this.groupId, this.elementId);
    }

    EchoDomPropertyStore.dispose(element);
};

/**
 * Programmatically invokes a button's action.
 * Togglebuttons will have their state toggled as a result.
 * The server will be notified of the action in the event
 * the button has server-side <code>ActionListener</code>s.
 *
 * @param elementId the button element id
 */
EchoButton.prototype.doAction = function() {
    if (this.toggle) {
        this.doToggle();
    }
    
    if (!this.serverNotify) {
        return;
    }
    
    if (document.selection && document.selection.empty) {
        document.selection.empty();
    }
    
    EchoClientMessage.setActionValue(this.elementId, "click");
    
    EchoServerTransaction.connect();
};

/**
 * Toggles the state of a toggle button.
 */
EchoButton.prototype.doToggle = function() {
    var newState = !this.selected;
    if (this.groupId) {
        if (!newState) {
            // Clicking on selected radio button: do nothing.
            return;
        }
        EchoButton.Group.deselect(this.groupId);
    }
    this.setSelected(newState);
};

EchoButton.prototype.getElement = function() {
    return document.getElementById(this.elementId);
};

/**
 * Initializes the state of a configured button data object.
 * Registers event listeners for the underlying element.
 */
EchoButton.prototype.init = function() {
    if (this.groupId) {
        EchoButton.Group.add(this.groupId, this.elementId);
    }
    
    var element = this.getElement();
    
    EchoEventProcessor.addHandler(element, "click", "EchoButton.processClick");
    EchoEventProcessor.addHandler(element, "keypress", "EchoButton.processKeyPressed");
    EchoEventProcessor.addHandler(element, "mousedown", "EchoButton.processPressed");
    EchoEventProcessor.addHandler(element, "mouseup", "EchoButton.processReleased");
    if (EchoClientProperties.get("proprietaryEventMouseEnterLeaveSupported")) {
        EchoEventProcessor.addHandler(element, "mouseenter", "EchoButton.processRolloverEnter");
        EchoEventProcessor.addHandler(element, "mouseleave", "EchoButton.processRolloverExit");
    } else {
        EchoEventProcessor.addHandler(element, "mouseout", "EchoButton.processRolloverExit");
        EchoEventProcessor.addHandler(element, "mouseover", "EchoButton.processRolloverEnter");
    }
    
    EchoDomPropertyStore.setPropertyValue(element, "component", this);
};

/**
 * Processes a button mouse click event.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoButton.prototype.processClick = function(echoEvent) {
    if (!this.enabled || !EchoClientEngine.verifyInput(this.elementId)) {
        return;
    }
    this.doAction();
};

/**
 * Processes a button key press event.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoButton.prototype.processKeyPressed = function(echoEvent) {
    if (!this.enabled || !EchoClientEngine.verifyInput(this.elementId)) {
        return;
    }
    if (echoEvent.keyCode == 13 || echoEvent.keyCode == 32) {
        this.doAction();
    }
};

/**
 * Processes a button mouse press event.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoButton.prototype.processPressed = function(echoEvent) {
    if (!this.enabled || !EchoClientEngine.verifyInput(this.elementId)) {
        return;
    }
    EchoDomUtil.preventEventDefault(echoEvent);
    this.setVisualState(EchoButton.STATE_PRESSED);
};

/**
 * Processes a button mouse release event.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoButton.prototype.processReleased = function(echoEvent) {
    this.setVisualState(EchoButton.STATE_DEFAULT);
};

/**
 * Processes a button mouse rollover enter event.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoButton.prototype.processRolloverEnter = function(echoEvent) {
    if (!this.enabled || !EchoClientEngine.verifyInput(this.elementId)) {
        return;
    }
    this.setVisualState(EchoButton.STATE_ROLLOVER);
};

/**
 * Processes a button mouse rollover exit event.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoButton.prototype.processRolloverExit = function(echoEvent) {
    this.setVisualState(EchoButton.STATE_DEFAULT);
};

/**
 * Sets the selection state of a toggle button.
 * @param newState a boolean flag indicating the new selection state
 */
EchoButton.prototype.setSelected = function(newState) {
    this.selected = newState;

    var stateIconUri = newState ? this.selectedStateIcon : this.stateIcon;
    if (!stateIconUri) {
        throw new Error("State icon not specified for selection state: " + newState);
    }
    var stateIconElement = document.getElementById(this.elementId + "_stateicon");
    stateIconElement.src = stateIconUri;
    
    EchoClientMessage.setPropertyValue(this.elementId, "selected", newState ? "true" : "false");
};

/**
 * Sets the visual state of a specific button.
 *
 * @param newState the new visual state, one of the following values:
 *        <ul>
 *         <li><code>EchoButton.STATE_DEFFAULT</code></li>
 *         <li><code>EchoButton.STATE_PRESSED</code></li>
 *         <li><code>EchoButton.STATE_ROLLOVER</code></li>
 *        </ul>
 */
EchoButton.prototype.setVisualState = function(newState) {
    var newStyle, newIcon, newStateIcon;
    
    switch (newState) {
    case EchoButton.STATE_ROLLOVER:
        newStyle = this.rolloverStyle;
        newIcon = this.rolloverIcon;
        newStateIcon = this.selected ? this.rolloverSelectedStateIcon : this.rolloverStateIcon;
        break;
    case EchoButton.STATE_PRESSED:
        newStyle = this.pressedStyle;
        newIcon = this.pressedIcon;
        newStateIcon = this.selected ? this.pressedSelectedStateIcon : this.pressedStateIcon;
        break;
    default:
        newIcon = this.defaultIcon;
        newStateIcon = this.selected ? this.selectedStateIcon : this.stateIcon;
    }
    
    if (newIcon) {
        var iconElement = document.getElementById(this.elementId + "_icon");
        iconElement.src = newIcon;
    }
    if (newStateIcon) {
        var stateIconElement = document.getElementById(this.elementId + "_stateicon");
        stateIconElement.src = newStateIcon;
    }
    
    var element = this.getElement();
    
    EchoCssUtil.restoreOriginalStyle(element);
    if (newStyle) {
        EchoCssUtil.applyTemporaryStyle(element, newStyle);
    }
    EchoButton.ieRepaint(element);
};

/**
 * Returns the Button data object instance based on the root element id
 * of the Button.
 *
 * @param element the root element or element id of the Button
 * @return the relevant Button instance
 */
EchoButton.getComponent = function(element) {
    return EchoDomPropertyStore.getPropertyValue(element, "component");
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
 * This method delegates to corresponding method
 * on EchoButton data object instance.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoButton.processClick = function(echoEvent) {
    var button = EchoButton.getComponent(echoEvent.registeredTarget);
    button.processClick(echoEvent);
};

/**
 * Processes a button key press event.
 * This method delegates to corresponding method
 * on EchoButton data object instance.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoButton.processKeyPressed = function(echoEvent) {
    var button = EchoButton.getComponent(echoEvent.registeredTarget);
    button.processKeyPressed(echoEvent);
};

/**
 * Processes a button mouse press event.
 * This method delegates to corresponding method
 * on EchoButton data object instance.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoButton.processPressed = function(echoEvent) {
    var button = EchoButton.getComponent(echoEvent.registeredTarget);
    button.processPressed(echoEvent);
};

/**
 * Processes a button mouse release event.
 * This method delegates to corresponding method
 * on EchoButton data object instance.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoButton.processReleased = function(echoEvent) {
    var button = EchoButton.getComponent(echoEvent.registeredTarget);
    button.processReleased(echoEvent);
};

/**
 * Processes a button mouse rollover enter event.
 * This method delegates to corresponding method
 * on EchoButton data object instance.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoButton.processRolloverEnter = function(echoEvent) {
    var button = EchoButton.getComponent(echoEvent.registeredTarget);
    button.processRolloverEnter(echoEvent);
};

/**
 * Processes a button mouse rollover exit event.
 * This method delegates to corresponding method
 * on EchoButton data object instance.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoButton.processRolloverExit = function(echoEvent) {
    var button = EchoButton.getComponent(echoEvent.registeredTarget);
    button.processRolloverExit(echoEvent);
};


/**
 * Static object/namespace for RadioButton group management.
 */
EchoButton.Group = { };

/**
 * Associative arary mapping button group ids to
 * arrays of button element ids.
 */
EchoButton.Group.idToButtonArrayMap = new EchoCollectionsMap();

/**
 * Adds a button to a button group.
 *
 * @param groupId the id of the button group
 */
EchoButton.Group.add = function(groupId, buttonId) {
    var buttonArray = EchoButton.Group.idToButtonArrayMap.get(groupId);
    if (!buttonArray) {
        buttonArray = new Array();
        EchoButton.Group.idToButtonArrayMap.put(groupId, buttonArray);
    }
    buttonArray.push(buttonId);
};

EchoButton.Group.deselect = function(groupId) {
    var buttonArray = EchoButton.Group.idToButtonArrayMap.get(groupId);
    if (!buttonArray) {
        return;
    }
    for (var i = 0; i < buttonArray.length; ++i) {
        var elementId = buttonArray[i];
        var button = EchoButton.getComponent(elementId);
        if (button.selected) {
            button.setSelected(false);
        }
    }
};

EchoButton.Group.remove = function(groupId, buttonId) {
    // Obtain appropriate button group.
    var buttonArray = EchoButton.Group.idToButtonArrayMap.get(groupId);
    
    if (!buttonArray) {
        // No such button group exists.
        throw new Error("No such group: " + groupId);
    }

    // Find index of button in array.
    var arrayIndex = -1;
    for (var i = 0; i < buttonArray.length; ++i) {
        if (buttonArray[i] == buttonId) {
            arrayIndex = i;
            break;
        }
    }
    
    if (arrayIndex == -1) {
        // Button does not exist in group.
        throw new Error("No such button: " + buttonId + " in group: " + groupId);
    }
    
    if (buttonArray.length == 1) {
        // Array will now be empty, remove button group entirely.
        EchoButton.Group.idToButtonArrayMap.remove(groupId);
    } else {
        // Buttons remain, remove button from button group.
        buttonArray[arrayIndex] = buttonArray[buttonArray.length - 1];
        buttonArray.length = buttonArray.length - 1;
    }
};

/**
 * Static object/namespace for Button MessageProcessor 
 * implementation.
 */
EchoButton.MessageProcessor = { };

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
        var button = EchoButton.getComponent(elementId);
        if (button) {
            button.dispose();
        }
    }
};
    
/**
 * Processes an <code>init</code> message to initialize the state of a 
 * button component that is being added.
 *
 * @param initMessageElement the <code>init</code> element to process
 */
EchoButton.MessageProcessor.processInit = function(initMessageElement) {
    var defaultStyle = initMessageElement.getAttribute("default-style");
    var rolloverStyle = initMessageElement.getAttribute("rollover-style");
    var pressedStyle = initMessageElement.getAttribute("pressed-style");
    
    for (var item = initMessageElement.firstChild; item; item = item.nextSibling) {
        var elementId = item.getAttribute("eid");
        var element = document.getElementById(elementId);
        EchoDomUtil.setCssText(element, defaultStyle);
        element.style.visibility = "visible";
        element.style.cursor = "pointer";
        element.style.margin = "0px";
        element.style.borderSpacing = "0px";
        
        var button = new EchoButton(elementId);
        
        if (rolloverStyle) {
            button.rolloverStyle = rolloverStyle;
        }
        if (pressedStyle) {
            button.pressedStyle = pressedStyle;
        }
        button.enabled = item.getAttribute("enabled") != "false";
        button.serverNotify = item.getAttribute("server-notify") != "false";
        if (item.getAttribute("default-icon")) {
            button.defaultIcon = item.getAttribute("default-icon");
        }
        if (item.getAttribute("rollover-icon")) {
            button.rolloverIcon = item.getAttribute("rollover-icon");
        }
        if (item.getAttribute("pressed-icon")) {
            button.pressedIcon = item.getAttribute("pressed-icon");
        }
        
        if (item.getAttribute("toggle") == "true") {
            // ToggleButton-specific properties.
            button.toggle = true;
            button.selected = item.getAttribute("selected") == "true";
            button.stateIcon = item.getAttribute("state-icon");
            button.selectedStateIcon = item.getAttribute("selected-state-icon");
            if (item.getAttribute("group")) {
                button.groupId = item.getAttribute("group");
            }
            if (rolloverStyle) {
                button.rolloverStateIcon = item.getAttribute("rollover-state-icon"); 
                button.rolloverSelectedStateIcon = item.getAttribute("rollover-selected-state-icon"); 
            }
            if (pressedStyle) {
                button.pressedStateIcon = item.getAttribute("pressed-state-icon"); 
                button.pressedSelectedStateIcon = item.getAttribute("pressed-selected-state-icon"); 
            }
        }
        
        button.init();
    }
};
