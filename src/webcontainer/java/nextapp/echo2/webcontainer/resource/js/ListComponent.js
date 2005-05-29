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

//_________________________
// Object EchoListComponent

EchoListComponent = function() { };

/**
 * ServerMessage processor.
 */
EchoListComponent.MessageProcessor = function() { };

/**
 * ServerMessage process() implementation.
 */
EchoListComponent.MessageProcessor.process = function(messagePartElement) {
    for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
        if (messagePartElement.childNodes[i].nodeType == 1) {
            switch (messagePartElement.childNodes[i].tagName) {
            case "init":
                EchoListComponent.MessageProcessor.processInit(messagePartElement.childNodes[i]);
                break;
            case "dispose":
                EchoListComponent.MessageProcessor.processDispose(messagePartElement.childNodes[i]);
                break;
            }
        }
    }
};

EchoListComponent.MessageProcessor.processDispose = function(disposeMessageElement) {
    for (var item = disposeMessageElement.firstChild; item; item = item.nextSibling) {
        var elementId = item.getAttribute("eid");
	    var selectElement = document.getElementById(elementId + "_select");
	    var optionElements = selectElement.options;
	    EchoEventProcessor.removeHandler(selectElement.id, "change");
	    for (var i = 0; i < optionElements.length; ++i) {
	        EchoEventProcessor.removeHandler(optionElements[i].id, "mouseout");
	        EchoEventProcessor.removeHandler(optionElements[i].id, "mouseover");
	    }
    }
};

EchoListComponent.MessageProcessor.processInit = function(initMessageElement) {
    for (var item = initMessageElement.firstChild; item; item = item.nextSibling) {
        var elementId = item.getAttribute("eid");
	    var selectElement = document.getElementById(elementId + "_select");
	    var optionElements = selectElement.options;
	    EchoEventProcessor.addHandler(selectElement.id, "change", "EchoListComponent.processSelection");
	    for (var i = 0; i < optionElements.length; ++i) {
	        EchoEventProcessor.addHandler(optionElements[i].id, "mouseout", "EchoListComponent.processRolloverExit");
	        EchoEventProcessor.addHandler(optionElements[i].id, "mouseover", "EchoListComponent.processRolloverEnter");
	    }
    }
};

// BUGBUG Copied verbatim from Button.js
EchoListComponent.applyStyle = function(element, cssText) {
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

EchoListComponent.createUpdates = function(elementId,propertyElement){
    var element = document.getElementById(elementId);

    // remove previous values
    while(propertyElement.hasChildNodes()){
        var removed = propertyElement.removeChild(propertyElement.firstChild);
    }

    var options = element.options;

    // add new values        
    for (var i = 0; i < options.length; ++i) {
        if (options[i].selected) {
            var optionId = options[i].id;
            var optionElement = EchoClientMessage.messageDocument.createElement("option");
            optionElement.setAttribute("id", optionId);
            // EchoDebugManager.consoleWrite("added " + optionId);
            propertyElement.appendChild(optionElement);
        }
    }
};

EchoListComponent.processRolloverEnter = function(echoEvent) {
    var optionElement = echoEvent.registeredTarget;

    if (!optionElement.selected){
        var style = EchoDomPropertyStore.getPropertyValue(optionElement.parentNode.id, "rolloverStyle");
        EchoListComponent.applyStyle(optionElement, style);
    }
};

EchoListComponent.processRolloverExit = function(echoEvent) {
    var optionElement = echoEvent.registeredTarget;
    var style = EchoDomPropertyStore.getPropertyValue(optionElement.parentNode.parentNode.id, "defaultStyle");
    EchoListComponent.applyStyle(optionElement, style);
};

EchoListComponent.processSelection = function(echoEvent) {
    var optionElement = echoEvent.registeredTarget;
    EchoListComponent.processUpdates(echoEvent.registeredTarget.id);
};

EchoListComponent.processUpdates = function(elementId) {
    var element = document.getElementById(elementId);
    var propertyElement = EchoClientMessage.createPropertyElement(element.parentNode.id, "selectedObjects");

    EchoListComponent.createUpdates(element.id,propertyElement);

    EchoDebugManager.updateClientMessage();
};