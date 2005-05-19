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
            case "setgroup":
                EchoButton.MessageProcessor.processSetGroup(messagePartElement.childNodes[i]);
                break;
            }
        }
    }
};

EchoButton.MessageProcessor.processDispose = function(disposeMessageElement) {
    for (var item = disposeMessageElement.firstChild; item; item = item.nextSibling) {
        var elementId = item.getAttribute("eid");
        EchoButton.dispose(elementId);
    }
};

EchoButton.MessageProcessor.processInit = function(initMessageElement) {
    for (var item = initMessageElement.firstChild; item; item = item.nextSibling) {
        var elementId = item.getAttribute("eid");
        EchoButton.init(elementId);
    }
};

EchoButton.MessageProcessor.processSetGroup = function(setGroupMessageElement) {
    var groupId = setGroupMessageElement.getAttribute("groupid");
    for (var item = setGroupMessageElement.firstChild; item; item = item.nextSibling) {
        var elementId = item.getAttribute("eid");
        EchoButton.setButtonGroup(elementId, groupId);
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

EchoButton.dispose = function(elementId) {
    EchoEventProcessor.removeHandler(elementId, "mouseover");
    EchoEventProcessor.removeHandler(elementId, "mouseout");
    EchoEventProcessor.removeHandler(elementId, "mousedown");
    EchoEventProcessor.removeHandler(elementId, "mouseup");
    EchoEventProcessor.removeHandler(elementId, "click");
};

EchoButton.doAction = function(echoEvent) {
    if (EchoServerTransaction.active) {
        return;
    }
    
    var elementId = echoEvent.registeredTarget.getAttribute("id");

    if ("true" == EchoDomPropertyStore.getPropertyValue(elementId, "toggle")) {
        EchoButton.doToggle(echoEvent.registeredTarget);
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

EchoButton.doPressed = function(echoEvent) {
    EchoDomUtil.preventEventDefault(echoEvent);
    if (EchoServerTransaction.active) {
        return;
    }
    var eventTarget = echoEvent.registeredTarget;
    EchoButton.setState(eventTarget, EchoButton.STATE_PRESSED);
};

EchoButton.doReleased = function(echoEvent) {
    EchoButton.setState(echoEvent.registeredTarget, EchoButton.STATE_DEFAULT);
};

EchoButton.doRolloverEnter = function(echoEvent) {
    if (EchoServerTransaction.active) {
        return;
    }
    var eventTarget = echoEvent.registeredTarget;
    EchoButton.setState(echoEvent.registeredTarget, EchoButton.STATE_ROLLOVER);
};

EchoButton.doRolloverExit = function(echoEvent) {
    EchoButton.setState(echoEvent.registeredTarget, EchoButton.STATE_DEFAULT);
};

EchoButton.doToggle = function(buttonElement) {
    var elementId = buttonElement.getAttribute("id");
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

EchoButton.init = function(elementId) {
    EchoEventProcessor.addHandler(elementId, "mouseover", "EchoButton.doRolloverEnter");
    EchoEventProcessor.addHandler(elementId, "mouseout", "EchoButton.doRolloverExit");
    EchoEventProcessor.addHandler(elementId, "mousedown", "EchoButton.doPressed");
    EchoEventProcessor.addHandler(elementId, "mouseup", "EchoButton.doReleased");
    EchoEventProcessor.addHandler(elementId, "click", "EchoButton.doAction");
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

EchoButton.setIcon = function(elementId, newIconUri) {
    var iconElement = document.getElementById(elementId + "_icon");
    iconElement.src = newIconUri;
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
    if (newStyle) {
        EchoButton.applyStyle(buttonElement, newStyle);
    }
    if (newIcon) {
        EchoButton.setIcon(buttonElement.id, newIcon);
    }
};
