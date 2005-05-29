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

//______________________________
// Object EchoListComponentDhtml

EchoListComponentDhtml = function() { };

/**
 * ServerMessage processor.
 */
EchoListComponentDhtml.MessageProcessor = function() { };

/**
 * ServerMessage process() implementation.
 */
EchoListComponentDhtml.MessageProcessor.process = function(messagePartElement) {
    for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
        if (messagePartElement.childNodes[i].nodeType == 1) {
            switch (messagePartElement.childNodes[i].tagName) {
            case "init":
                EchoListComponentDhtml.MessageProcessor.processInit(messagePartElement.childNodes[i]);
                break;
            case "dispose":
                EchoListComponentDhtml.MessageProcessor.processDispose(messagePartElement.childNodes[i]);
                break;
            }
        }
    }
};

EchoListComponentDhtml.MessageProcessor.processDispose = function(disposeMessageElement) {
    for (var item = disposeMessageElement.firstChild; item; item = item.nextSibling) {
        var elementId = item.getAttribute("eid");
	    var selectElement = document.getElementById(elementId);
	    var optionElements = selectElement.getElementsByTagName("div");
	    for (var i = 0; i < optionElements.length; ++i) {
	        EchoEventProcessor.removeHandler(optionElements[i].id, "click");
	        EchoEventProcessor.removeHandler(optionElements[i].id, "mouseout");
	        EchoEventProcessor.removeHandler(optionElements[i].id, "mouseover");
	    }
    }
};

EchoListComponentDhtml.MessageProcessor.processInit = function(initMessageElement) {
    for (var item = initMessageElement.firstChild; item; item = item.nextSibling) {
        var elementId = item.getAttribute("eid");
        var defaultStyle = item.getAttribute("defaultstyle");
        var rolloverStyle = item.getAttribute("rolloverstyle");
        var selectedStyle = item.getAttribute("selectedstyle");
        var multiple = item.getAttribute("multiple") == "true";
        var i;

	    var selectElement = document.getElementById(elementId);
	    var optionElements = selectElement.getElementsByTagName("div");
	    for (i = 0; i < optionElements.length; ++i) {
	        EchoEventProcessor.addHandler(optionElements[i].id, "click", "EchoListComponentDhtml.processSelection");
	        EchoEventProcessor.addHandler(optionElements[i].id, "mouseout", "EchoListComponentDhtml.doRolloverExit");
	        EchoEventProcessor.addHandler(optionElements[i].id, "mouseover", "EchoListComponentDhtml.doRolloverEnter");
	    }
        
        var selectionItems = item.getElementsByTagName("selectionitem");
        for (i = 0; i < selectionItems.length; ++i) {
            var optionId = selectionItems[i].getAttribute("optionid");
            EchoDomPropertyStore.setPropertyValue(optionId, "selectedState", true);
        }
        
        EchoDomPropertyStore.setPropertyValue(elementId, "defaultStyle", defaultStyle);
        EchoDomPropertyStore.setPropertyValue(elementId, "rolloverStyle", rolloverStyle);
        EchoDomPropertyStore.setPropertyValue(elementId, "selectedStyle", selectedStyle);
    }
};

// BUGBUG Copied verbatim from Button.js
EchoListComponentDhtml.applyStyle = function(element, cssText) {
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

EchoListComponentDhtml.clearSelectedValues = function(elementId) {
    var element = document.getElementById(elementId);
    var optionDivElements = element.getElementsByTagName("div");
    for (var i = 0; i < optionDivElements.length; ++i) {
        var style = EchoDomPropertyStore.getPropertyValue(elementId, "defaultStyle");
        EchoListComponentDhtml.applyStyle(optionDivElements[i], style);
        EchoDomPropertyStore.setPropertyValue(optionDivElements[i].id, "selectedState", false);
    }
};

EchoListComponentDhtml.createUpdates = function(elementId,propertyElement){
    var element = document.getElementById(elementId);

    // remove previous values
    while(propertyElement.hasChildNodes()){
        var removed = propertyElement.removeChild(propertyElement.firstChild);
    }

    var optionDivElements = element.getElementsByTagName("div");

    // add new values        
    for (var i = 0; i < optionDivElements.length; ++i){
        if (EchoDomPropertyStore.getPropertyValue(optionDivElements[i].id, "selectedState")) {
            var optionId = optionDivElements[i].id;
            var optionElement = EchoClientMessage.messageDocument.createElement("option");
            optionElement.setAttribute("id",optionId);
            propertyElement.appendChild(optionElement);
        }
    }
};

EchoListComponentDhtml.deselectItem = function(elementId) {
    var target = document.getElementById(elementId);
    EchoDomPropertyStore.setPropertyValue(target.id, "selectedState", false);
    var style = EchoDomPropertyStore.getPropertyValue(target.parentNode.id, "defaultStyle");
    EchoListComponentDhtml.applyStyle(target,style);
};

EchoListComponentDhtml.doRolloverEnter = function(e) {
    EchoDomUtil.preventEventDefault(e);
    var target = EchoDomUtil.getEventTarget(e);
    if (!EchoDomPropertyStore.getPropertyValue(target.id, "selectedState")) {
        var style = EchoDomPropertyStore.getPropertyValue(target.parentNode.id, "rolloverStyle");
        EchoListComponentDhtml.applyStyle(target,style);
    }
};

EchoListComponentDhtml.doRolloverExit = function(e) {
    EchoDomUtil.preventEventDefault(e);
    var target = EchoDomUtil.getEventTarget(e);
    if (!EchoDomPropertyStore.getPropertyValue(target.id, "selectedState")) {
        var style = EchoDomPropertyStore.getPropertyValue(target.parentNode.id, "defaultStyle");
        EchoListComponentDhtml.applyStyle(target,style);
    }
};

EchoListComponentDhtml.processSelection = function(e) {
    EchoDomUtil.preventEventDefault(e);

    // BUGBUG: Move into something common
    if (document.selection && document.selection.empty) {
        document.selection.empty();
    }

    var target = EchoDomUtil.getEventTarget(e);
    var selectedState = EchoDomPropertyStore.getPropertyValue(target.id, "selectedState");

    if (selectedState) {
        EchoListComponentDhtml.deselectItem(target.id);
    } else {
        EchoListComponentDhtml.selectItem(target.id);
    }
    EchoListComponentDhtml.processUpdates(target.id);
};

EchoListComponentDhtml.processUpdates = function(elementId) {
    var element = document.getElementById(elementId).parentNode;
    var propertyElement  = EchoClientMessage.createPropertyElement(element.id, "selectedObjects");

    EchoListComponentDhtml.createUpdates(element.id,propertyElement);

    EchoDebugManager.updateClientMessage();
};

EchoListComponentDhtml.selectItem = function(elementId) {
    var target = document.getElementById(elementId);
    var style = EchoDomPropertyStore.getPropertyValue(target.parentNode.id, "selectedStyle");
    var multiple = EchoDomPropertyStore.getPropertyValue(target.parentNode.id, "multiple");

    if (!multiple) {
        EchoListComponentDhtml.clearSelectedValues(target.parentNode.id);
    }
    EchoDomPropertyStore.setPropertyValue(target.id, "selectedState", true);

    EchoListComponentDhtml.applyStyle(target,style);
};

