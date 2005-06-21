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
	    var itemElements = selectElement.options;
	    EchoEventProcessor.removeHandler(selectElement.id, "change");
	    for (var i = 0; i < itemElements.length; ++i) {
	        EchoEventProcessor.removeHandler(itemElements[i].id, "mouseout");
	        EchoEventProcessor.removeHandler(itemElements[i].id, "mouseover");
	    }
    }
};

EchoListComponent.MessageProcessor.processInit = function(initMessageElement) {
    for (var item = initMessageElement.firstChild; item; item = item.nextSibling) {
        var elementId = item.getAttribute("eid");
        var rolloverStyle = item.getAttribute("rollover-style");

        EchoDomPropertyStore.setPropertyValue(elementId, "rolloverStyle", rolloverStyle);
        if (item.getAttribute("server-notify")) {
            EchoDomPropertyStore.setPropertyValue(elementId, "serverNotify", item.getAttribute("server-notify"));
        }

	    var selectElement = document.getElementById(elementId + "_select");
	    var itemElements = selectElement.options;
	    EchoEventProcessor.addHandler(selectElement.id, "change", "EchoListComponent.processSelection");
	    for (var i = 0; i < itemElements.length; ++i) {
	        EchoEventProcessor.addHandler(itemElements[i].id, "mouseout", "EchoListComponent.processRolloverExit");
	        EchoEventProcessor.addHandler(itemElements[i].id, "mouseover", "EchoListComponent.processRolloverEnter");
	    }
    }
};

EchoListComponent.doChange = function(elementId) {
    var element = document.getElementById(elementId);
    var listElementId = element.parentNode.id;
    var propertyElement = EchoClientMessage.createPropertyElement(listElementId, "selection");

    // remove previous values
    while(propertyElement.hasChildNodes()){
        propertyElement.removeChild(propertyElement.firstChild);
    }

    var items = element.options;

    // add new values        
    for (var i = 0; i < items.length; ++i) {
        if (items[i].selected) {
            var itemId = items[i].id;
            var itemElement = EchoClientMessage.messageDocument.createElement("item");
            itemElement.setAttribute("id", itemId);
            propertyElement.appendChild(itemElement);
        }
    }

    EchoDebugManager.updateClientMessage();
    
    if ("true" == EchoDomPropertyStore.getPropertyValue(listElementId, "serverNotify")) {
	    EchoClientMessage.setActionValue(listElementId, "action");
	    EchoServerTransaction.connect();
    }
};

EchoListComponent.processRolloverEnter = function(echoEvent) {
    var itemElement = echoEvent.registeredTarget;
    var elementId = itemElement.id;
    if (!EchoClientEngine.verifyInput(elementId)) {
        return;
    }
    if (!itemElement.selected) {
        var style = EchoDomPropertyStore.getPropertyValue(EchoDomUtil.getComponentId(itemElement.id), "rolloverStyle");
        EchoCssUtil.applyTemporaryStyle(itemElement, style);
    }
};

EchoListComponent.processRolloverExit = function(echoEvent) {
    var itemElement = echoEvent.registeredTarget;
    var elementId = itemElement.id;
    if (!EchoClientEngine.verifyInput(elementId)) {
        return;
    }
    EchoCssUtil.restoreOriginalStyle(itemElement);
};

EchoListComponent.processSelection = function(echoEvent) {
    var itemElement = echoEvent.registeredTarget;
    var elementId = itemElement.id;
    if (!EchoClientEngine.verifyInput(elementId)) {
        EchoDomUtil.preventEventDefault(echoEvent);
        return;
    }
    EchoListComponent.doChange(elementId);
};