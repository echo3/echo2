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

/**
 * Static object/namespace for SelectField/ListBox support.
 * This object/namespace should not be used externally.
 */
EchoListComponent = function() { };

/**
 * Static object/namespace for SelectField/ListBox MessageProcessor 
 * implementation.
 */
EchoListComponent.MessageProcessor = function() { };

/**
 * MessageProcessor process() implementation 
 * (invoked by ServerMessage processor).
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

/**
 * Processes a <code>dispose</code> message to finalize the state of a
 * selection component that is being removed.
 *
 * @param disposeMessageElement the <code>dispose</code> element to process
 */
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

/**
 * Processes an <code>init</code> message to initialize the state of a 
 * selection component that is being added.
 *
 * @param initMessageElement the <code>init</code> element to process
 */
EchoListComponent.MessageProcessor.processInit = function(initMessageElement) {
    for (var item = initMessageElement.firstChild; item; item = item.nextSibling) {
        var elementId = item.getAttribute("eid");
        var rolloverStyle = item.getAttribute("rollover-style");

        if (item.getAttribute("enabled") == "false") {
            EchoDomPropertyStore.setPropertyValue(elementId, "EchoClientEngine.inputDisabled", true);
        }
        if (item.getAttribute("server-notify") == "true") {
            EchoDomPropertyStore.setPropertyValue(elementId, "serverNotify", true);
        }
        EchoDomPropertyStore.setPropertyValue(elementId, "rolloverStyle", rolloverStyle);

	    var selectElement = document.getElementById(elementId + "_select");
	    var itemElements = selectElement.options;
        EchoEventProcessor.addHandler(selectElement.id, "change", "EchoListComponent.processSelection");
	    for (var i = 0; i < itemElements.length; ++i) {
	        EchoEventProcessor.addHandler(itemElements[i].id, "mouseout", "EchoListComponent.processRolloverExit");
	        EchoEventProcessor.addHandler(itemElements[i].id, "mouseover", "EchoListComponent.processRolloverEnter");
	    }
        
        // Store initial selection in DomPropertyStore such that it may be recalled if component is changed
        // when input is disabled.
        var selectionItems = selectElement.options;
        var selectedItemIds = new Array();
        for (i = 0; i < selectionItems.length; ++i) {
            if (selectionItems[i].selected) {
                selectedItemIds.push(selectionItems[i].id);
            }
        }
        EchoDomPropertyStore.setPropertyValue(elementId, "initialSelection", selectedItemIds);
    }
};

/**
 * Processes an item mouse exit event.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoListComponent.processRolloverEnter = function(echoEvent) {
    var itemElement = echoEvent.registeredTarget;
    var componentId = EchoDomUtil.getComponentId(itemElement.id);
    if (!EchoClientEngine.verifyInput(componentId)) {
        return;
    }
    if (!itemElement.selected) {
        var style = EchoDomPropertyStore.getPropertyValue(componentId, "rolloverStyle");
        if (style) {
            EchoCssUtil.applyTemporaryStyle(itemElement, style);
        }
    }
};

/**
 * Processes an item mouse over event.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoListComponent.processRolloverExit = function(echoEvent) {
    var itemElement = echoEvent.registeredTarget;
    var listElement = itemElement.parentNode;
    if (!EchoClientEngine.verifyInput(listElement.id)) {
        return;
    }
    EchoCssUtil.restoreOriginalStyle(itemElement);
};

/**
 * Processes an item selection (change) event.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoListComponent.processSelection = function(echoEvent) {
    var listElement = echoEvent.registeredTarget;
    var componentId = EchoDomUtil.getComponentId(listElement.id);
    
    if (!EchoClientEngine.verifyInput(componentId)) {
        EchoListComponent.revertToInitialSelection(componentId);
        return;
    }
    EchoListComponent.updateClientMessage(componentId);
};

/**
 * Reverts the selection state to the initial selection.
 * This method is invoked when a user modifies the state of a disabled
 * or modally obscured selection component.  There is no means to prevent
 * the selection from occurring, so it is simply immediately reverted to
 * its prior state once it occurs.
 *
 * @param componentId the id of the selection component to revert
 */
EchoListComponent.revertToInitialSelection = function(componentId) {
    var i;
    var selectComponent = document.getElementById(componentId);
    var options = selectComponent.getElementsByTagName("option");
    for (i = 0; i < options.length; ++i) {
        options[i].selected = false;
    }
    
    var selectedItemIds = EchoDomPropertyStore.getPropertyValue(componentId, "initialSelection");
    if (!selectedItemIds) {
        return;
    }
    for (i = 0; i < selectedItemIds.length; ++i) {
        var optionElement = document.getElementById(selectedItemIds[i]);
        optionElement.selected = true;
    }
};

/**
 * Updates the selection state in the outgoing <code>ClientMessage</code>.
 * If any server-side <code>ActionListener</code>s are registered, an action
 * will be set in the ClientMessage and a client-server connection initiated.
 *
 * @param componentId the id of the selection component
 */
EchoListComponent.updateClientMessage = function(componentId) {
    var propertyElement = EchoClientMessage.createPropertyElement(componentId, "selection");

    // remove previous values
    while(propertyElement.hasChildNodes()){
        propertyElement.removeChild(propertyElement.firstChild);
    }

    var componentElement = document.getElementById(componentId);
    var itemElements = componentElement.getElementsByTagName("option");

    // add new values        
    for (var i = 0; i < itemElements.length; ++i) {
        if (itemElements[i].selected) {
            var clientMessageItemElement = EchoClientMessage.messageDocument.createElement("item");
            clientMessageItemElement.setAttribute("id", itemElements[i].id);
            propertyElement.appendChild(clientMessageItemElement);
        }
    }

    EchoDebugManager.updateClientMessage();
    
    if (EchoDomPropertyStore.getPropertyValue(componentId, "serverNotify")) {
        EchoClientMessage.setActionValue(componentId, "action");
        EchoServerTransaction.connect();
    }
};