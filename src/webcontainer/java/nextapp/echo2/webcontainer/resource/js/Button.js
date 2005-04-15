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

//BUGBUG. Cut this class up into a generic style-replacement system.

//__________________
// Object EchoButton

function EchoButton() { }

EchoButton.activeElementId = null;
EchoButton.baseCssText = null;
EchoButton.pressedCssText = null;
EchoButton.rolloverCssText = null;
EchoButton.pressedActive = false;
EchoButton.rolloverActive = false;

EchoButton.STATE_ROLLOVER = 1;
EchoButton.STATE_PRESSED = 2;

EchoButton.activate = function(buttonElement) {
    EchoButton.activeElementId = buttonElement.getAttribute("id");
    EchoButton.baseCssText = EchoDomPropertyStore.getPropertyValue(EchoButton.activeElementId, "baseStyle");
    EchoButton.pressedCssText = EchoDomPropertyStore.getPropertyValue(EchoButton.activeElementId, "pressedStyle");
    EchoButton.rolloverCssText = EchoDomPropertyStore.getPropertyValue(EchoButton.activeElementId, "rolloverStyle");
    EchoButton.rolloverActive = false;
    EchoButton.pressedActive = false;
};

EchoButton.deactivate = function() {
    var buttonElement = document.getElementById(EchoButton.activeElementId);
    if (buttonElement) {
        EchoButton.applyStyle(buttonElement, EchoButton.baseCssText);
    }
	EchoButton.activeElementId = null;
	EchoButton.baseCssText = null;
	EchoButton.pressedCssText = null;
	EchoButton.rolloverCssText = null;
	EchoButton.pressedActive = false;
	EchoButton.rolloverActive = false;
};

EchoButton.applyStyle = function(element, cssText) {
    if (!cssText) {
        //BUGBUG. Temporary fix to prevent exceptions for child element (image) issue.
        return;
    }
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

EchoButton.processAction = function(echoEvent) {
    if (EchoServerTransaction.active) {
        return;
    }
    EchoButton.deactivate();
    EchoButton.removeAllListeners();
    var elementId = echoEvent.registeredTarget.getAttribute("id");
    if (document.selection && document.selection.clear) {
        document.selection.clear();
    }
    EchoClientMessage.setActionValue(elementId, "click");
    EchoServerTransaction.connect();
};

EchoButton.processPressed = function(e) {
    EchoDomUtil.preventEventDefault(e);
    if (EchoServerTransaction.active) {
        return;
    }
    var eventTarget = EchoDomUtil.getEventTarget(e);
    if (!EchoDomPropertyStore.getPropertyValue(eventTarget.getAttribute("id"), "pressedStyle")) {
        // Return if the button has not pressed effects.
        return;
    }
    EchoButton.setState(eventTarget, EchoButton.STATE_PRESSED, true);
    EchoDomUtil.addEventListener(eventTarget, "mouseup", EchoButton.processReleased, true);
};

EchoButton.processReleased = function(e) {
    var eventTarget = EchoDomUtil.getEventTarget(e);
    EchoButton.setState(eventTarget, EchoButton.STATE_PRESSED, false);
    EchoDomUtil.removeEventListener(eventTarget, "mouseup", EchoButton.processReleased, true);
};

EchoButton.processRolloverEnter = function(e) {
    if (EchoServerTransaction.active) {
        return;
    }
//BUGBUG. looking at registeredTarget here....do we want to look at registeredtarget universally?
//        and if so, do we want to use EchoEventProcessor for *ALL* event registrations?
    var eventTarget = e.registeredTarget;
//BUGBUG*    
//EchoDebugManager.consoleWrite("rolloverEnter: " + e.registeredTarget.getAttribute("id"));
    EchoButton.setState(eventTarget, EchoButton.STATE_ROLLOVER, true);
    EchoDomUtil.addEventListener(eventTarget, "mouseout", EchoButton.processRolloverExit, true);
};

EchoButton.processRolloverExit = function(e) {
    var eventTarget = EchoDomUtil.getEventTarget(e);
//BUGBUG*    
//EchoDebugManager.consoleWrite("rolloverExit: " + eventTarget.getAttribute("id"));
    EchoButton.setState(eventTarget, EchoButton.STATE_ROLLOVER, false);
    EchoDomUtil.removeEventListener(eventTarget, "mouseout", EchoButton.processRolloverExit, true);
};

EchoButton.removeAllListeners = function() {
    if (!EchoButton.activeElementId) {
        return;
    }
    var buttonElement = document.getElementById(EchoButton.activeElementId);
    if (!buttonElement) {
        return;
    }
    EchoDomUtil.removeEventListener(eventTarget, "mouseup", EchoButton.processReleased, true);
    EchoDomUtil.removeEventListener(eventTarget, "mouseout", EchoButton.processRolloverExit, true);
};

EchoButton.setState = function(buttonElement, stateType, newValue) {
    if (buttonElement.nodeType != 1) {
        // Prevent TextNode events.
        return;
    }

    if (buttonElement.getAttribute("id") != EchoButton.activeElementId) {
        EchoButton.deactivate();
        EchoButton.activate(buttonElement);
    }

    switch (stateType) {
    case EchoButton.STATE_ROLLOVER:
        EchoButton.rolloverActive = newValue;
        break;
    case EchoButton.STATE_PRESSED:
        EchoButton.pressedActive = newValue;
        break;
    }

    if (EchoButton.pressedActive && EchoButton.pressedCssText) {
        EchoButton.applyStyle(buttonElement, EchoButton.pressedCssText);
    } else if (EchoButton.rolloverActive && EchoButton.rolloverCssText) {
        EchoButton.applyStyle(buttonElement, EchoButton.rolloverCssText);
    } else {
        EchoButton.applyStyle(buttonElement, EchoButton.baseCssText);
    }
};

EchoScriptLibraryManager.setStateLoaded("Echo.Button");
