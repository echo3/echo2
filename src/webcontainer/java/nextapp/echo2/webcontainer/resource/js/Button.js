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

//BUGBUG. Changing button states are sometimes not reflected due to broken rollover/presss code.
//BUGBUG. Cut this class up into a generic style-replacement system.

//__________________
// Object EchoButton

EchoButton = function() { };

EchoButton.buttonGroupIdToButtonArrayMap = new Array();

EchoButton.STATE_DEFAULT = 0;
EchoButton.STATE_ROLLOVER = 1;
EchoButton.STATE_PRESSED = 2;

/**
 * ServerMessage processor.
 */
EchoButton.MessageProcessor = function() { };

/**
 * ServerMessage process() implementation.
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

EchoButton.MessageProcessor.processInit = function(initMessageElement) {
    var defaultStyle = initMessageElement.getAttribute("defaultstyle");
    var rolloverStyle = initMessageElement.getAttribute("rolloverstyle");
    var pressedStyle = initMessageElement.getAttribute("pressedstyle");

    for (var item = initMessageElement.firstChild; item; item = item.nextSibling) {
        var elementId = item.getAttribute("eid");
        
        if (defaultStyle) {
            EchoDomPropertyStore.setPropertyValue(elementId, "defaultStyle", defaultStyle);
        }
        if (rolloverStyle) {
            EchoDomPropertyStore.setPropertyValue(elementId, "rolloverStyle", rolloverStyle);
        }
        if (pressedStyle) {
            EchoDomPropertyStore.setPropertyValue(elementId, "pressedStyle", pressedStyle);
        }
        
        if (item.getAttribute("defaulticon")) {
            EchoDomPropertyStore.setPropertyValue(elementId, "defaultIcon", item.getAttribute("defaulticon"));
        }
        if (item.getAttribute("rollovericon")) {
            EchoDomPropertyStore.setPropertyValue(elementId, "rolloverIcon", item.getAttribute("rollovericon"));
        }
        if (item.getAttribute("pressedicon")) {
            EchoDomPropertyStore.setPropertyValue(elementId, "pressedIcon", item.getAttribute("pressedicon"));
        }
        
        if (item.getAttribute("toggle") == "true") {
            // ToggleButton
            EchoDomPropertyStore.setPropertyValue(elementId, "toggle", "true");
            EchoDomPropertyStore.setPropertyValue(elementId, "selected", item.getAttribute("selected"));
            EchoDomPropertyStore.setPropertyValue(elementId, "stateIcon", item.getAttribute("stateicon"));
            EchoDomPropertyStore.setPropertyValue(elementId, "selectedStateIcon", item.getAttribute("selectedstateicon"));
            if (item.getAttribute("group")) {
                EchoButton.setButtonGroup(elementId, item.getAttribute("group"));
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

EchoButton.applyStyle = function(element, cssText) {
    var styleProperties = cssText.split(";");
    var styleData = new Array();
    for (var i = 0; i < styleProperties.length; ++i) {
        var separatorIndex = styleProperties[i].indexOf(":");
        if (separatorIndex == -1) {
            continue;
        }
        var attributeName = styleProperties[i].substring(0, separatorIndex);
        var propertyName = EchoDomUtil.cssAttributeNameToPropertyName(attributeName);
        var propertyValue = styleProperties[i].substring(separatorIndex + 1);
        element.style[propertyName] = propertyValue;
    }
};

EchoButton.deselectRadioButton = function(groupId) {
    var buttonArray = EchoButton.buttonGroupIdToButtonArrayMap[groupId];
    if (!buttonArray) {
        //BUGBUG? can this ever happen?
        return;
    }
    for (var i = 0; i < buttonArray.length; ++i) {
        var elementId = buttonArray[i];
        if (EchoButton.getSelectionState(elementId)) {
            EchoButton.setSelectionState(elementId, false);
        }
    }
};

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

EchoButton.getButtonGroup = function(elementId) {
    return EchoDomPropertyStore.getPropertyValue(elementId, "buttonGroup");
};

EchoButton.getSelectionState = function(elementId) {
    return "true" == EchoDomPropertyStore.getPropertyValue(elementId, "selected");
};

/**
 * Forces quirky IE clients to repaint immediately for aesthetic 
 * performance improvement.
 */
EchoButton.ieRepaint = function(buttonElement) {
    if (EchoClientProperties.get("quirkDomPerformanceIERepaint")) {
        var originalWidth = buttonElement.style.width;
        var temporaryWidth = parseInt(buttonElement.clientWidth) + 1;
        buttonElement.style.width = temporaryWidth + "px";
        buttonElement.style.width = originalWidth;
    }
};

EchoButton.processClick = function(echoEvent) {
    if (EchoServerTransaction.active) {
        return;
    }
    var elementId = echoEvent.registeredTarget.getAttribute("id");
    EchoButton.doAction(elementId);
};

EchoButton.processKeyPressed = function(echoEvent) {
    if (EchoServerTransaction.active) {
        return;
    }
    if (echoEvent.keyCode == 13 || echoEvent.keyCode == 32) {
	    var elementId = echoEvent.registeredTarget.getAttribute("id");
	    EchoButton.doAction(elementId);
    }
};

EchoButton.processPressed = function(echoEvent) {
    EchoDomUtil.preventEventDefault(echoEvent);
    if (EchoServerTransaction.active) {
        return;
    }
    var eventTarget = echoEvent.registeredTarget;
    EchoButton.setState(eventTarget, EchoButton.STATE_PRESSED);
};

EchoButton.processReleased = function(echoEvent) {
    EchoButton.setState(echoEvent.registeredTarget, EchoButton.STATE_DEFAULT);
};

EchoButton.processRolloverEnter = function(echoEvent) {
    if (EchoServerTransaction.active) {
        return;
    }
    var eventTarget = echoEvent.registeredTarget;
    EchoButton.setState(echoEvent.registeredTarget, EchoButton.STATE_ROLLOVER);
};

EchoButton.processRolloverExit = function(echoEvent) {
    EchoButton.setState(echoEvent.registeredTarget, EchoButton.STATE_DEFAULT);
};

EchoButton.setButtonGroup = function(elementId, groupId) {
    var buttonArray = EchoButton.buttonGroupIdToButtonArrayMap[groupId];
    if (!buttonArray) {
        buttonArray = new Array();
        EchoButton.buttonGroupIdToButtonArrayMap[groupId] = buttonArray;
    }
    buttonArray.push(elementId);
    EchoDomPropertyStore.setPropertyValue(elementId, "buttonGroup", groupId);
};

EchoButton.setIcon = function(elementId, newIconUri) {
    var iconElement = document.getElementById(elementId + "_icon");
    iconElement.src = newIconUri;
};

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

EchoButton.setState = function(buttonElement, newState) {
    if (buttonElement.nodeType != 1) {
        // Prevent TextNode events.
        return;
    }

    var newStyle, newIcon;
    switch (newState) {
    case EchoButton.STATE_ROLLOVER:
        newStyle = EchoDomPropertyStore.getPropertyValue(buttonElement.id, "rolloverStyle");
        newIcon = EchoDomPropertyStore.getPropertyValue(buttonElement.id, "rolloverIcon");
        break;
    case EchoButton.STATE_PRESSED:
        newStyle = EchoDomPropertyStore.getPropertyValue(buttonElement.id, "pressedStyle");
        newIcon = EchoDomPropertyStore.getPropertyValue(buttonElement.id, "pressedIcon");
        break;
    default:
        newStyle = EchoDomPropertyStore.getPropertyValue(buttonElement.id, "defaultStyle");
        newIcon = EchoDomPropertyStore.getPropertyValue(buttonElement.id, "defaultIcon");
    }
    if (newIcon) {
        EchoButton.setIcon(buttonElement.id, newIcon);
    }
    if (newStyle) {
        EchoButton.applyStyle(buttonElement, newStyle);
    }
    EchoButton.ieRepaint(buttonElement);
};
