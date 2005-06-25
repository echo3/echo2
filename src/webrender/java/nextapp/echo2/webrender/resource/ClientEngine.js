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

// _____________________________
// Object EchoAsyncMonitor

/**
 * Static object/namespace for polling server to monitor for asynchronous
 * server-initiated updates to the client ("server push").
 */
function EchoAsyncMonitor() { }

/**
 * The time, in milleseconds, between polling requests.
 */
EchoAsyncMonitor.timeInterval = 500;

/**
 * Server query for polling service.
 */
EchoAsyncMonitor.pollServiceRequest = "?serviceId=Echo.AsyncMonitor";

/**
 * Timeout identifier for the delayed invocation of the next poll request
 * (used to cancel the timeout if required).
 */
EchoAsyncMonitor.timeoutId = null;

/**
 * Initiates an HTTP request to the asynchronous monitor poll service to
 * determine if the server has the need to update the client.
 */
EchoAsyncMonitor.connect = function() {
    var conn = new EchoHttpConnection(EchoClientEngine.baseServerUri + EchoAsyncMonitor.pollServiceRequest, "GET");
    conn.responseHandler = EchoAsyncMonitor.responseHandler;
    conn.invalidResponseHandler = EchoAsyncMonitor.invalidResponseHandler;
    conn.connect();
};

/**
 * Processes an invalid response to the poll request
 */
EchoAsyncMonitor.invalidResponseHandler = function() {
    alert("Invalid response from server to asynchronous polling connection.");
};

/**
 * Starts the countdown to the next poll request.
 */
EchoAsyncMonitor.start = function() {
    if (!EchoServerTransaction.active) {
        EchoAsyncMonitor.timeoutId = window.setTimeout("EchoAsyncMonitor.connect();", 
                EchoAsyncMonitor.timeInterval);
    }
};

/**
 * Cancels the countdown to the next poll request, if necessary.
 */
EchoAsyncMonitor.stop = function() {
    if (EchoAsyncMonitor.timeoutId) {
        window.clearTimeout(EchoAsyncMonitor.timeoutId);
        EchoAsyncMonitor.timeoutId = null;
    }
};

/**
 * Processes a response from the HTTP request made to the polling
 * service.
 *
 * @param conn the EchoHttpConnection containing the response information.
 */
EchoAsyncMonitor.responseHandler = function(conn) {
    if ("true" == conn.getResponseXml().documentElement.getAttribute("requestsync")) {
        // Server is requesting synchronization: Initiate server transaction.
        EchoServerTransaction.connect();
    } else {
        // Server does not require synchronization: restart countdown to next poll request.
        EchoAsyncMonitor.start();
    }
    
};

// _____________________________
// Object EchoServerDelayMessage

 /**
  * Static object/namespace to manage configuration and activation of the
  * server-delay message.  
  * The server-delay message serves the following purposes:
  * <ul>
  *  <li>Provides an additional barrier to user input (this should not be
  *   be relied upon in any fashion, as components should themselves 
  *   check the state of EchoServerTransaction.active before accepting
  *   input.</li>
  *  <li>Displays a "wait" (hourglass) mouse cursor.</li>
  *  <li>Displays a "please wait" message after a time interval has elapsed
  *   during a longer-than-normal server transaction.</li>
  * </ul>
  */
function EchoServerDelayMessage() { }

/**
 * MessagePartProcessor implementation for EchoServerDelayMessage.
 */
EchoServerDelayMessage.MessageProcessor = function() { }

/**
 * MessagePartProcessor process() method implementation.
 */
EchoServerDelayMessage.MessageProcessor.process = function(messagePartElement) {
    for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
        if (messagePartElement.childNodes[i].nodeType == 1) {
            switch (messagePartElement.childNodes[i].tagName) {
            case "set-message":
                EchoServerDelayMessage.MessageProcessor.processSetDelayMessage(messagePartElement.childNodes[i]);
                break;
            }
        }
    }
};

/**
 * ServerMessage parser to handle requests to set delay message text.
 */
EchoServerDelayMessage.MessageProcessor.processSetDelayMessage = function(setDelayMessageElement) {
    var serverDelayMessage = document.getElementById(EchoServerDelayMessage.MESSAGE_ELEMENT_ID);
    var i;
    // Remove existing children.
    for (i = serverDelayMessage.childNodes.length - 1; i >= 0; --i) {
        serverDelayMessage.removeChild(serverDelayMessage.childNodes[i]);
    }
    // Add new children.
    var contentElement = setDelayMessageElement.getElementsByTagName("content")[0];
    for (i = 0; i < contentElement.childNodes.length; ++i) {
        serverDelayMessage.appendChild(EchoDomUtil.importNode(document, contentElement.childNodes[i], true));
    }
};

EchoServerDelayMessage.MESSAGE_ELEMENT_ID = "serverDelayMessage";
EchoServerDelayMessage.MESSAGE_ELEMENT_LONG_ID = "serverDelayMessageLong";

/** 
 * Id of HTML element providing "long-running" delay message.  
 * This element is shown/hidden as necessary when long-running delays
 * occur or are completed.
 */
EchoServerDelayMessage.delayMessageTimeoutId = null;

/** Timeout (in ms) before "long-running" delay message is displayed. */ 
EchoServerDelayMessage.timeout = 750;

/**
 * Activates/shows the server delay message pane.
 */
EchoServerDelayMessage.activate = function() {
    var serverDelayMessage = document.getElementById(EchoServerDelayMessage.MESSAGE_ELEMENT_ID);
    if (!serverDelayMessage) {
        return;
    }
    serverDelayMessage.style.visibility = "visible";
    EchoServerDelayMessage.delayMessageTimeoutId = window.setTimeout("EchoServerDelayMessage.activateDelayMessage()", 
            EchoServerDelayMessage.timeout);
};

/**
 * Activates the "long-running" delay message (requires server delay message
 * to have been previously activated).
 */
EchoServerDelayMessage.activateDelayMessage = function() {
    EchoServerDelayMessage.cancelDelayMessageTimeout();
    var longMessage = document.getElementById(EchoServerDelayMessage.MESSAGE_ELEMENT_LONG_ID);
    if (!longMessage) {
        return;
    }
    longMessage.style.visibility = "visible";
};

/**
 * Cancels timeout object that will raise delay message when server
 * transaction is determined to be "long running".
 */
EchoServerDelayMessage.cancelDelayMessageTimeout = function() {
    if (EchoServerDelayMessage.delayMessageTimeoutId) {
        window.clearTimeout(EchoServerDelayMessage.delayMessageTimeoutId);
        EchoServerDelayMessage.delayMessageTimeoutId = null;
    }
};

/**
 * Deactives/hides the server delay message.
 */
EchoServerDelayMessage.deactivate = function() {
    EchoServerDelayMessage.cancelDelayMessageTimeout();
    var serverDelayMessage = document.getElementById(EchoServerDelayMessage.MESSAGE_ELEMENT_ID);
    var longMessage = document.getElementById(EchoServerDelayMessage.MESSAGE_ELEMENT_LONG_ID);
    if (serverDelayMessage) {
        serverDelayMessage.style.visibility = "hidden";
    }
    if (longMessage) {
        longMessage.style.visibility = "hidden";
    }
};

// _________________________
// Object EchoClientAnalyzer

/**
  * Static object/namespace to perform analysis of client browser's 
  * version information, capabilities, and quirks and store the information
  * in the outgoing ClientMessage.
  */
function EchoClientAnalyzer() { }

/**
 * Analyzes properties of client browser and stores them in the outgoing
 * ClientMessage.
 */
EchoClientAnalyzer.analyze = function() {
    var messagePartElement = EchoClientMessage.getMessagePart("EchoClientAnalyzer");
    EchoClientAnalyzer.setTextProperty(messagePartElement, "navigatorAppName", window.navigator.appName);
    EchoClientAnalyzer.setTextProperty(messagePartElement, "navigatorAppVersion", window.navigator.appVersion);
    EchoClientAnalyzer.setTextProperty(messagePartElement, "navigatorAppCodeName", window.navigator.appCodeName);
    EchoClientAnalyzer.setBooleanProperty(messagePartElement, "navigatorCookieEnabled", window.navigator.cookieEnabled);
    EchoClientAnalyzer.setBooleanProperty(messagePartElement, "navigatorJavaEnabled", window.navigator.javaEnabled());
    EchoClientAnalyzer.setTextProperty(messagePartElement, "navigatorLanguage", 
            window.navigator.language ? window.navigator.language : window.navigator.userLanguage);
    EchoClientAnalyzer.setTextProperty(messagePartElement, "navigatorPlatform", window.navigator.platform);
    EchoClientAnalyzer.setTextProperty(messagePartElement, "navigatorUserAgent", window.navigator.userAgent);
    if (window.screen) {
        // Capture Display Information
        EchoClientAnalyzer.setIntegerProperty(messagePartElement, "screenWidth", window.screen.width);
        EchoClientAnalyzer.setIntegerProperty(messagePartElement, "screenHeight", window.screen.height);
        EchoClientAnalyzer.setIntegerProperty(messagePartElement, "screenColorDepth", window.screen.colorDepth);
    }
    EchoClientAnalyzer.setIntegerProperty(messagePartElement, "utcOffset", 0 - parseInt((new Date()).getTimezoneOffset()));
};

/**
 * Stores a boolean client property in the ClientMessage.
 *
 * @param messagePartElement the XMLElement in which generated property 
 *        XML elements should be stored
 * @param propertyName the property name
 * @param propertyValue the property value
 */
EchoClientAnalyzer.setBooleanProperty = function(messagePartElement, propertyName, propertyValue) {
    propertyElement = messagePartElement.ownerDocument.createElement("property");
    propertyElement.setAttribute("type", "boolean");
    propertyElement.setAttribute("name", propertyName);
    propertyElement.setAttribute("value", propertyValue ? "true" : "false");
    messagePartElement.appendChild(propertyElement);
};

/**
 * Stores a integer client property in the ClientMessage.
 *
 * @param messagePartElement the XMLElement in which generated property 
 *        XML elements should be stored
 * @param propertyName the property name
 * @param propertyValue the property value
 */
EchoClientAnalyzer.setIntegerProperty = function(messagePartElement, propertyName, propertyValue) {
    var intValue = parseInt(propertyValue);
    if (isNaN(intValue)) {
        return;
    }
    propertyElement = messagePartElement.ownerDocument.createElement("property");
    propertyElement.setAttribute("type", "integer");
    propertyElement.setAttribute("name", propertyName);
    propertyElement.setAttribute("value", intValue);
    messagePartElement.appendChild(propertyElement);
};

/**
 * Stores a text client property in the ClientMessage.
 *
 * @param messagePartElement the XMLElement in which generated property 
 *        XML elements should be stored
 * @param propertyName the property name
 * @param propertyValue the property value
 */
EchoClientAnalyzer.setTextProperty = function(messagePartElement, propertyName, propertyValue) {
    propertyElement = messagePartElement.ownerDocument.createElement("property");
    propertyElement.setAttribute("type", "text");
    propertyElement.setAttribute("name", propertyName);
    propertyElement.setAttribute("value", propertyValue);
    messagePartElement.appendChild(propertyElement);
};

// _______________________
// Object EchoClientEngine

/**
  * Static object/namespace.
  */
function EchoClientEngine() { }

EchoClientEngine.baseServerUri = null;

EchoClientEngine.init = function(baseServerUri) {
    // Store base URI.
    EchoClientEngine.baseServerUri = baseServerUri;
    
    // Launch debug window if requested in URI.
    if (window.location.search && window.location.search.toLowerCase().indexOf("debug") != -1) {
        EchoDebugManager.launch();
    }

    // Confiugre initial client message.
    EchoClientMessage.setInitialize();
    
    // Analyze client information.
    EchoClientAnalyzer.analyze();
    
    // Synchronize initial state from server.
    EchoServerTransaction.connect();
};

/**
 * Verifies that the given element is eligible to receive input at this time.
 * This method should be invoked before procsesing any user input.
 * This method will ensure that no server transaction is active and the element
 * is within the current modal context.
 */
EchoClientEngine.verifyInput = function(elementId) {
    if (EchoServerTransaction.active) {
        return false;
    }
    if (!EchoModalManager.isElementInModalContext(elementId)) {
        return false;
    }
    return true;
};

// ________________________
// Object EchoClientMessage

function EchoClientMessage() { }

EchoClientMessage.messageDocument = null;

/**
 * Creates a property element in the EchoClientMessage.
 *
 * @param componentId The id of the component.
 * @param propertyName The name of the property.
 * @return the created 'property' element.
 */
EchoClientMessage.createPropertyElement = function(componentId, propertyName) {
    var messagePartElement = EchoClientMessage.getMessagePart("EchoPropertyUpdate");
    var propertyElements = messagePartElement.getElementsByTagName("property");
    var propertyElement = null;

    for (var i = 0; i < propertyElements.length; ++i) {
        if (componentId == propertyElements[i].getAttribute("component-id") &&
                propertyName == propertyElements[i].getAttribute("name")) {
            propertyElement = propertyElements[i];
            break;
        } 
    }
    if (!propertyElement) {
        propertyElement = messagePartElement.ownerDocument.createElement("property");
        propertyElement.setAttribute("component-id", componentId);
        propertyElement.setAttribute("name", propertyName);
        messagePartElement.appendChild(propertyElement);
    }
    
    return propertyElement;
};

/**
 * Creates or retrieves the "message-part" DOM element from the 
 * EchoClientMessage with the specified processor attribute.
 *
 * @param processor A string representing the processor type of
 *        the message-part element to return.
 * @return The "message-part" DOM element.
 */
EchoClientMessage.getMessagePart = function(processor) {
    // Return existing <message-part> if available.
    var messagePartElements = EchoClientMessage.messageDocument.getElementsByTagName("message-part");
    for (var i = 0; i < messagePartElements.length; ++i) {
        if (messagePartElements[i].getAttribute("processor") == processor) {
            return messagePartElements[i];
        }
    }
    
    // Create new <message-part> if none exists.
    var messagePartElement = EchoClientMessage.messageDocument.createElement("message-part");
    messagePartElement.setAttribute("processor", processor);
    EchoClientMessage.messageDocument.documentElement.appendChild(messagePartElement);
    return messagePartElement;
};

//BUGBUG. add a getPropertyElement method.

/**
 * Retrieves the value of a string property in the EchoClientMessage.
 *
 * @param componentId The id of the component.
 * @param propertyName The name of the property.
 * @return  The value of the property.
 */
EchoClientMessage.getPropertyValue = function(componentId, propertyName) {
    var messagePartElement = EchoClientMessage.getMessagePart("EchoPropertyUpdate");
    var propertyElements = messagePartElement.getElementsByTagName("property");
    for (var i = 0; i < propertyElements.length; ++i) {
        if (componentId == propertyElements[i].getAttribute("component-id") &&
                propertyName == propertyElements[i].getAttribute("name")) {
            return propertyElements[i].getAttribute("value");
        }
    }
    return null;
};

EchoClientMessage.reset = function() {
    EchoClientMessage.messageDocument = null;
    EchoClientMessage.messageDocument 
            = EchoDomUtil.createDocument("http://www.nextapp.com/products/echo2/climsg", "client-message");
};

/**
 * Sets the action of the EchoClientMessage.  This method should only be
 * called one time, and the EchoClientMessage should be submitted to the
 * server soon thereafter.
 *
 * @param componentId The id of the component which initiated the action.
 * @param actionName The name of the action.
 * @param actionValue The value of the action (optional).
 */
 //BUGBUG. use same design for actions as we do for properties.
EchoClientMessage.setActionValue = function(componentId, actionName, actionValue) {
    var messagePartElement = EchoClientMessage.getMessagePart("EchoAction");
    var actionElement = messagePartElement.ownerDocument.createElement("action");
    actionElement.setAttribute("component-id", componentId);
    actionElement.setAttribute("name", actionName);
    if (actionValue) {
        actionElement.setAttribute("value", actionValue);
    }
    messagePartElement.appendChild(actionElement);

    EchoDebugManager.updateClientMessage();
};

/**
 * Stores the value of a string property in in the EchoClientMessage.
 *
 * @param componentId The id of the component.
 * @param propertyName The name of the property.
 * @param newValue The new value of the property.
 */
 //BUGBUG. rename this setPropertyValue.
EchoClientMessage.setPropertyValue = function(componentId, propertyName, newValue) {
    var propertyElement = EchoClientMessage.createPropertyElement(componentId, propertyName);
    
    propertyElement.setAttribute("value", newValue);

    EchoDebugManager.updateClientMessage();
};

EchoClientMessage.setInitialize = function() {
    EchoClientMessage.messageDocument.documentElement.setAttribute("type", "initialize");
};

// ___________________________
// Object EchoClientProperties

function EchoClientProperties() { }

EchoClientProperties.propertyMap = new Array();

EchoClientProperties.MessageProcessor = function() { };

EchoClientProperties.MessageProcessor.process = function(messagePartElement) {
    for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
        if (messagePartElement.childNodes[i].nodeType == 1) {
            switch (messagePartElement.childNodes[i].tagName) {
            case "store":
                EchoClientProperties.MessageProcessor.processStore(messagePartElement.childNodes[i]);
                break;
            }
        }
    }
};

EchoClientProperties.MessageProcessor.processStore = function(storeElement) {
    var propertyElements = storeElement.getElementsByTagName("property");
    for (var i = 0; i < propertyElements.length; ++i) {
        var name = propertyElements[i].getAttribute("name");
        var value = propertyElements[i].getAttribute("value");
        if (value == "true") {
            value = true;
        } else if (value == "false") {
            value = false;
        }
        EchoClientProperties.propertyMap[name] = value;
    }
};

EchoClientProperties.get = function(name) {
    return EchoClientProperties.propertyMap[name];
};

function EchoCssUtil() { }

EchoCssUtil.applyStyle = function(element, cssText) {
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

EchoCssUtil.applyTemporaryStyle = function(element, cssStyle) {
    // Set original style if not already set.
    if (!EchoDomPropertyStore.getPropertyValue(element.id, "originalStyle")) {
        if (element.style.cssText) {
            EchoDomPropertyStore.setPropertyValue(element.id, "originalStyle", element.style.cssText);
        } else {
            EchoDomPropertyStore.setPropertyValue(element.id, "originalStyle", "-");
        }
    }
    EchoCssUtil.applyStyle(element, cssStyle);
};

EchoCssUtil.restoreOriginalStyle = function(element) {
    var originalStyle = EchoDomPropertyStore.getPropertyValue(element.id, "originalStyle");
    if (!originalStyle) {
        return;
    }
    element.style.cssText = originalStyle == "-" ? "" : originalStyle;
};

// _______________________
// Object EchoDebugManager

/**
 * Static object/namespace for managing the Debug Window.
 */
function EchoDebugManager() { }

/**
 * Reference to the active debug window.
 */
EchoDebugManager.debugWindow = null;

/**
 * Writes a message to the console.
 *
 * @param message the message to write
 */
EchoDebugManager.consoleWrite = function(message) {
    if (!EchoDebugManager.debugWindow || EchoDebugManager.debugWindow.closed) {
        return;
    }
    EchoDebugManager.debugWindow.EchoDebug.consoleWrite(message);
};

/**
 * Opens the debug window.
 */
EchoDebugManager.launch = function() {
    EchoDebugManager.debugWindow = window.open(EchoClientEngine.baseServerUri + "?serviceId=Echo.Debug", "EchoDebug", 
            "width=400,height=650,resizable=yes", true);
};

/**
 * Updates the current state of the ClientMessage in the synchronization tab.
 */
EchoDebugManager.updateClientMessage = function() {
    if (!EchoDebugManager.debugWindow || EchoDebugManager.debugWindow.closed) {
        return;
    }
    EchoDebugManager.debugWindow.EchoDebug.displayClientMessage(EchoClientMessage.messageDocument);
};

/**
 * Updates the current state of the ServerMessage in the synchronization tab.
 */
EchoDebugManager.updateServerMessage = function() {
    if (!EchoDebugManager.debugWindow || EchoDebugManager.debugWindow.closed) {
        return;
    }
    EchoDebugManager.debugWindow.EchoDebug.displayServerMessage(EchoServerMessage.messageDocument);
};

// ___________________________
// Object EchoDomPropertyStore

/**
 * An EchoServerMessage processor object which stores non-rendered property 
 * values in JavaScript HTML element objects.
 */
function EchoDomPropertyStore() { }

EchoDomPropertyStore.MessageProcessor = function() { }

EchoDomPropertyStore.MessageProcessor.process = function(messagePartElement) {
    for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
        if (messagePartElement.childNodes[i].nodeType == 1) {
            switch (messagePartElement.childNodes[i].tagName) {
            case "store-property":
                EchoDomPropertyStore.MessageProcessor.processStoreProperty(messagePartElement.childNodes[i]);
                break;
            }
        }
    }
};

EchoDomPropertyStore.MessageProcessor.processStoreProperty = function(storePropertyElement) {
    var propertyName = storePropertyElement.getAttribute("name");
    var propertyValue = storePropertyElement.getAttribute("value");
    var items = storePropertyElement.getElementsByTagName("item");
    for (var i = 0; i < items.length; ++i) {
        var elementId = items[i].getAttribute("eid");
        EchoDomPropertyStore.setPropertyValue(elementId, propertyName, propertyValue);
    }
};

EchoDomPropertyStore.getPropertyValue = function(elementId, propertyName) {
    var element = document.getElementById(elementId);
    if (!element) {
        return null;
    }
    if (element.echoDomPropertyStore) {
        return element.echoDomPropertyStore[propertyName];
    } else {
        return null;
    }
};

EchoDomPropertyStore.setPropertyValue = function(elementId, propertyName, propertyValue) {
    var element = document.getElementById(elementId);
    if (!element) {
        return;
    }
    if (!element.echoDomPropertyStore) {
        element.echoDomPropertyStore = new Array();
    }
    element.echoDomPropertyStore[propertyName] = propertyValue;
};

// ____________________
// Object EchoDomUpdate

/**
 * An EchoServerMessage processor object which performs DOM updates to 
 * the HTML document.  
 * This is a static, non-instantiable class.
 * @constructor
 */
function EchoDomUpdate() { }

EchoDomUpdate.MessageProcessor = function() { }

/**
 *
 */
EchoDomUpdate.MessageProcessor.process = function(messagePartElement) {
    for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
        if (messagePartElement.childNodes[i].nodeType == 1) {
            switch (messagePartElement.childNodes[i].tagName) {
            case "dom-add":
                this.processAdd(messagePartElement.childNodes[i]);
                break;
            case "dom-remove":
                this.processRemove(messagePartElement.childNodes[i]);
                break;
            case "dom-remove-children":
                this.processRemoveChildren(messagePartElement.childNodes[i]);
                break;
            case "attribute-update":
                this.processAttributeUpdate(messagePartElement.childNodes[i]);
                break;
            case "style-update":
                this.processStyleUpdate(messagePartElement.childNodes[i]);
                break;
            }
        }
    }
};

EchoDomUpdate.MessageProcessor.processAdd = function(domAddElement) {
    var parentId = domAddElement.getAttribute("parent-id");
    var siblingId = domAddElement.getAttribute("sibling-id");
    var parentElement = document.getElementById(parentId);
    var siblingElement = null;
    if (siblingId) {
        siblingElement = document.getElementById(siblingId);
        if (!siblingElement) {
            throw new EchoDomUpdate.TargetNotFoundException("Add", "sibling", siblingId);
        }
    }
    if (!parentElement) {
         throw new EchoDomUpdate.TargetNotFoundException("Add", "parent", parentId);
    }
    var contentElement = domAddElement.getElementsByTagName("content")[0];
    for (var i = 0; i < contentElement.childNodes.length; ++i) {
        var importedNode = EchoDomUtil.importNode(parentElement.ownerDocument, contentElement.childNodes[i], true);
        
        var newElementId = importedNode.getAttribute("id");
        if (!newElementId) {
            throw "Cannot add element without id attribute.";
        }
        if (document.getElementById(newElementId)) {
            throw "Element " + newElementId + " already exists in document; cannot add.";
        }
        
        if (siblingElement) {
            // If sibling specified, perform insert operation.
            parentElement.insertBefore(importedNode, siblingElement);
        } else {
            // Perform append operation.
            parentElement.appendChild(importedNode);
        }
    }
};

EchoDomUpdate.MessageProcessor.processAttributeUpdate = function(attributeUpdateElement) {
    var targetId = attributeUpdateElement.getAttribute("target-id");
    var targetElement = document.getElementById(targetId);
    if (!targetElement) {
        throw new EchoDomUpdate.TargetNotFoundException("AttributeUpdate", "target", targetId);
    }
    targetElement[attributeUpdateElement.getAttribute("name")] = attributeUpdateElement.getAttribute("value");
};

EchoDomUpdate.MessageProcessor.processRemove = function(domRemoveElement) {
    var targetId = domRemoveElement.getAttribute("target-id");
    var targetElement = document.getElementById(targetId);
    if (!targetElement) {
        return;
    }
    targetElement.parentNode.removeChild(targetElement);
};

EchoDomUpdate.MessageProcessor.processRemoveChildren = function(domRemoveElement) {
    var targetId = domRemoveElement.getAttribute("target-id");
    var targetElement = document.getElementById(targetId);
    if (!targetElement) {
        return;
    }
    var childNode = targetElement.firstChild;
    while (childNode) {
        var nextChildNode = childNode.nextSibling;
        targetElement.removeChild(childNode);
        childNode = nextChildNode;
    }
};

EchoDomUpdate.MessageProcessor.processStyleUpdate = function(styleUpdateElement) {
    var targetId = styleUpdateElement.getAttribute("target-id");
    var targetElement = document.getElementById(targetId);
    if (!targetElement) {
        throw new EchoDomUpdate.TargetNotFoundException("StyleUpdate", "target", targetId);
    }
    targetElement.style[styleUpdateElement.getAttribute("name")] = styleUpdateElement.getAttribute("value");
};

// ____________________________________________
// Object EchoDomUpdate.TargetNotFoundException

/**
 * An exception thrown when an <code>EchoDomUpdate</code> opeation fails 
 */
EchoDomUpdate.TargetNotFoundException = function(updateType, targetType, targetId) {
    this.targetId = targetId;
    this.targetType = targetType;
    this.updateType = updateType;
}

EchoDomUpdate.TargetNotFoundException.prototype.toString = function() {
    return "Failed to perform \"" + this.updateType + "\": " + this.targetType + " element \"" + this.targetId + "\" not found.";
};

// __________________
// Object EchoDomUtil

/** 
 * Static object/namespace for performing cross-platform DOM-related 
 * operations.  Most methods in this object/namespace are provided due
 * to nonstandard behavior in clients.
 */
function EchoDomUtil() { }

/**
 * Adds an event listener to an object, using the client's supported event 
 * model.
 *
 * @param eventSource the event source
 * @param eventType the type of event (the 'on' prefix should NOT be included
 *        in the event type, i.e., for mouse rollover events, "mouseover" would
 *        be specified instead of "onmouseover")
 * @param eventListener the event listener to be invoked when the event occurs
 * @param useCapture a flag indicating whether the event listener should capture
 *        events in the final phase of propogation (only supported by 
 *        DOM Level 2 event model)
 */
EchoDomUtil.addEventListener = function(eventSource, eventType, eventListener, useCapture) {
    if (eventSource.addEventListener) {
        eventSource.addEventListener(eventType, eventListener, useCapture);
    } else if (eventSource.attachEvent) {
        eventSource.attachEvent("on" + eventType, eventListener);
    }
};

/**
 * Creates a new XML DOM.
 *
 * @param namespaceUri the unique URI of the namespace of the root element in 
 *        the created document (not supported for
 *        Internet Explorer 6 clients, null may be specified for all clients)
 * @param qualifiedName the name of the root element of the new document (this
 *        element will be created automatically)
 * @return the created DOM
 */
EchoDomUtil.createDocument = function(namespaceUri, qualifiedName) {
    if (document.implementation && document.implementation.createDocument) {
        // DOM Level 2 Browsers
        var dom = document.implementation.createDocument(namespaceUri, qualifiedName, null);
        if (!dom.documentElement) {
            dom.appendChild(dom.createElement(qualifiedName));
        }
        return dom;
    } else if (window.ActiveXObject) {
        // Internet Explorer
        var createdDocument = new ActiveXObject("Microsoft.XMLDOM");
        var documentElement = createdDocument.createElement(qualifiedName);
        createdDocument.appendChild(documentElement);
        return createdDocument;
    } else {
        throw "Unable to create new Document.";
    }
};

/**
 * Converts a hyphen-separated CSS attribute name into a camelCase
 * property name.
 *
 * @param attribute the CSS attribute name, e.g., border-color
 * @return the style property name, e.g., borderColor
 */
EchoDomUtil.cssAttributeNameToPropertyName = function(attribute) {
    var segments = attribute.split("-");
    var out = segments[0];
    for (var i = 1; i < segments.length; ++i) {
        out += segments[i].substring(0, 1).toUpperCase();
        out += segments[i].substring(1);
    }
    return out;
};

/**
 * Returns the base component id of an extended id.
 * Example: for value "c_333_foo", "c_333" would be returned.
 *
 * @param elementId the extended id
 * @return the component id, or null if it cannot be determined
 */
EchoDomUtil.getComponentId = function(elementId) {
    if ("c_" != elementId.substring(0, 2)) {
        // Not a component id.
        return null;
    }
    var extensionStart = elementId.indexOf("_", 2);
    if (extensionStart == -1) {
        // id has now extension.
        return elementId;
    } else {
        return elementId.substring(0, extensionStart);
    }
};

/**
 * Returns the target of an event, using the client's supported event model.
 *
 * @param e the event
 * @return the target
 */
EchoDomUtil.getEventTarget = function(e) {
    return e.target ? e.target : e.srcElement;
};

/**
 * Imports a node into a document.  This method is a 
 * cross-browser replacement for DOMImplementation.importNode, as Internet
 * Explorer 6 clients do not provide such a method.
 * This method will directly invoke targetDocument.importNode() in the event
 * that a client provides such a method.
 *
 * @param targetDocument the document into which the node/hierarchy is to be
 *        imported
 * @param sourceNode the node to import
 * @param importChildren a boolean flag indicating whether child nodes should
 *        be recursively imported
 */
EchoDomUtil.importNode = function(targetDocument, sourceNode, importChildren) {
    if (targetDocument.importNode) {
        // DOM Level 2 Browsers
        return targetDocument.importNode(sourceNode, importChildren);
    } else {
        // Internet Explorer Browsers
        return EchoDomUtil.importNodeImpl(targetDocument, sourceNode, importChildren);
    }
};

//BUGBUG. EchoDomUtil.importNodeImpl() needs to be updated with a translation table between XHTML attribute
// names and properties.

/**
 * Manual implementation of DOMImplementation.importNode() for clients that do
 * not provide their own (i.e., Internet Explorer 6).
 *
 * @param targetDocument the document into which the node/hierarchy is to be
 *        imported
 * @param sourceNode the node to import
 * @param importChildren a boolean flag indicating whether child nodes should
 *        be recursively imported
 */
EchoDomUtil.importNodeImpl = function(targetDocument, sourceNode, importChildren) {
    var targetNode, i;
    switch (sourceNode.nodeType) {
    case 1:
        targetNode = targetDocument.createElement(sourceNode.nodeName);
        for (i = 0; i < sourceNode.attributes.length; ++i) {
            var attribute = sourceNode.attributes[i];
            if ("style" == attribute.name) {
                targetNode.style.cssText = attribute.value;
            } else if ("tabindex" == attribute.name) {
                targetNode.tabIndex = attribute.value;
            } else if ("colspan" == attribute.name) {
                targetNode.colSpan = attribute.value;
            } else if ("rowspan" == attribute.name) {
                targetNode.rowSpan = attribute.value;
            } else {
                targetNode[attribute.name] = attribute.value;
            }
        }
        break;
    case 3:
        targetNode = targetDocument.createTextNode(sourceNode.nodeValue);
        break;
    }
    
    if (importChildren && sourceNode.hasChildNodes()) {
        for (var sourceChildNode = sourceNode.firstChild; sourceChildNode; sourceChildNode = sourceChildNode.nextSibling) {
            var targetChildNode = EchoDomUtil.importNodeImpl(targetDocument, sourceChildNode, true);
            targetNode.appendChild(targetChildNode);
        }
    }
    return targetNode;
};

/**
 * Determines if <code>ancestorNode</code> is or is an ancestor of
 * <code>descendantNode</code>.
 *
 * @param ancestorNode the potential ancestor node
 * @param descendantNode the potential descendant node
 * @return true if <code>ancestorNode</code> is or is an ancestor of
 *         <code>descendantNode</code>.
 */
EchoDomUtil.isAncestorOf = function(ancestorNode, descendantNode) {
    var testNode = descendantNode;
    while (testNode !== null) {
        if (testNode == ancestorNode) {
            return true;
        }
        testNode = testNode.parentNode;
    }
    return false;
};

/**
 * Prevents the default action of an event from occurring, using the
 * client's supported event model.
 * On clients which support the W3C DOM Level 2 event specification,
 * the preventDefault() method is invoked.
 * On clients which support only the Internet Explorer event model,
 * the 'returnValue' property is set to false.
 *
 * @param e the event
 */
EchoDomUtil.preventEventDefault = function(e) {
    if (e.preventDefault) {
        e.preventDefault();
    } else {
        e.returnValue = false;
    }
};

/**
 * Removes an event listener from an object, using the client's supported 
 * event model.
 *
 * @param eventSource the event source
 * @param eventType the type of event (the 'on' prefix should NOT be included
 *        in the event type, i.e., for mouse rollover events, "mouseover" would
 *        be specified instead of "onmouseover")
 * @param eventListener the event listener to be invoked when the event occurs
 * @param useCapture a flag indicating whether the event listener should capture
 *        events in the final phase of propogation (only supported by 
 *        DOM Level 2 event model)
 */
EchoDomUtil.removeEventListener = function(eventSource, eventType, eventListener, useCapture) {
    if (eventSource.removeEventListener) {
        eventSource.removeEventListener(eventType, eventListener, useCapture);
    } else if (eventSource.detachEvent) {
        eventSource.detachEvent("on" + eventType, eventListener);
    }
};

EchoDomUtil.stopPropogation = function(e) {
    if (e.stopPropogation) {
        e.stopPropogation();
    } else {
        e.cancelBubble = true;
    }
};

// ________________________
// Object EchoEventProcessor

/**
 * Static object/namespace to provide event handler management.
 */
function EchoEventProcessor() { }

EchoEventProcessor.eventTypeToHandlersMap = new Array();

/**
 * Registers an event handler.
 *
 * @param elementId the target elementId of the event
 * @param eventType the type of the event (should be specified using 
 *        DOM level 2 event names, e.g., "mouseover" or "click", without
 *        the "on" prefix)
 * @param handler the name of the handler, as a String
 */
EchoEventProcessor.addHandler = function(elementId, eventType, handler) {
    var element = document.getElementById(elementId);
    EchoDomUtil.addEventListener(element, eventType, EchoEventProcessor.processEvent, false);
    
    var elementIdToHandlerMap = EchoEventProcessor.eventTypeToHandlersMap[eventType];
    if (!elementIdToHandlerMap) {
        elementIdToHandlerMap = new Array();
        EchoEventProcessor.eventTypeToHandlersMap[eventType] = elementIdToHandlerMap;
    }
    elementIdToHandlerMap[elementId] = handler;
};

EchoEventProcessor.getHandler = function(eventType, elementId) {
    var elementIdToHandlerMap = EchoEventProcessor.eventTypeToHandlersMap[eventType];
    if (!elementIdToHandlerMap) {
        return null;
    }
    return elementIdToHandlerMap[elementId];
};

/**
 * Returns the element ids of all event handlers of the specified type.
 */
EchoEventProcessor.getHandlerElementIds = function(eventType) {
    var elementIds = new Array();
    var elementIdToHandlerMap = EchoEventProcessor.eventTypeToHandlersMap[eventType];
    if (elementIdToHandlerMap) {
        for (var elementId in elementIdToHandlerMap) {
            elementIds.push(elementId);
        }
    }
    return elementIds;
};

/**
 * Returns the types of all registered event handlers.
 */
EchoEventProcessor.getHandlerEventTypes = function() {
    var handlerTypes = new Array();
    for (var eventType in EchoEventProcessor.eventTypeToHandlersMap) {
        handlerTypes.push(eventType);
    }
    return handlerTypes;
};

/**
 * Master event handler for all DOM events.
 * This method is registered as the event handler for all
 * client-side event registrations.  Events are processed,
 * modified, and then dispatched to the appropriate
 * Echo event handlers by this method.
 */
EchoEventProcessor.processEvent = function(e) {
    e = e ? e : window.event;
    var eventType = e.type;
    if (!e.target && e.srcElement) {
        // The Internet Explorer event model stores the target element in the 'srcElement' property of an event.
        // Modify the event such the target is retrievable using the W3C DOM Level 2 specified property 'target'.
        e.target = e.srcElement;
    }
    var targetElement = e.target;
    var handlerName = null;
    while (!handlerName && targetElement) {
        if (targetElement.nodeType == 1) { // Element Node
            handlerName = EchoEventProcessor.getHandler(eventType, targetElement.getAttribute("id"));
        }
        if (!handlerName) {
            targetElement = targetElement.parentNode;
        }
    }
    e.registeredTarget = targetElement;
    
    if (!handlerName) {
        //BUGBUG. IE can hit this case under rapid clicking.  Ensure this is not indicative of a problem.
        return;
    }

    var handler;
    try {
        handler = eval(handlerName);
    } catch (ex) {
        throw "Invalid handler: " + handlerName + " (" + ex + ")";
    }
    //BUGBUG. above code does not effectively check if handler is invalid.
    
    handler(e);
};

/**
 * Unregisters an event handler.
 *
 * @param elementId the target elementId of the event
 * @param eventType the type of the event (should be specified using 
 *        DOM level 2 event names, e.g., "mouseover" or "click", without
 *        the "on" prefix)
 * @param handler the name of the handler, as a String
 */
EchoEventProcessor.removeHandler = function(elementId, eventType) {
    var element = document.getElementById(elementId);
    if (element) {
        EchoDomUtil.removeEventListener(element, eventType, EchoEventProcessor.processEvent, false);
    }

    var elementIdToHandlerMap = EchoEventProcessor.eventTypeToHandlersMap[eventType];
    if (!elementIdToHandlerMap) {
        return;
    }
    if (elementIdToHandlerMap[elementId]) {
        delete elementIdToHandlerMap[elementId];
    }
};

// ______________________
// Object EchoEventUpdate

/* Do not instantiate */
function EchoEventUpdate() { }

EchoEventUpdate.MessageProcessor = function() { }

/**
 * EchoEventUpdate Message Part Procesor implementation.
 *
 * @param messagePartElement the ServerMessage "messagepart"
 *        element containing event add and remove directives
 *        to process.
 */
EchoEventUpdate.MessageProcessor.process = function(messagePartElement) {
    var i, j, k, eventTypes, handlers, addItems, removeItems, elementId;
    var adds = messagePartElement.getElementsByTagName("event-add");
    var removes = messagePartElement.getElementsByTagName("event-remove");
    for (i = 0; i < adds.length; ++i) {
    
        eventTypes = adds[i].getAttribute("type").split(",");
        handlers = adds[i].getAttribute("handler").split(",");
        if (handlers.length != eventTypes.length) {
            throw "Handler count and event type count do not match.";
        }
        
        addItems = adds[i].getElementsByTagName("item");
        for (j = 0; j < addItems.length; ++j) {
            elementId = addItems[j].getAttribute("eid");
            for (k = 0; k < eventTypes.length; ++k) {
                EchoEventProcessor.addHandler(elementId, eventTypes[k], handlers[k]);
            }
        }
    }
    for (i = 0; i < removes.length; ++i) {
        eventTypes = removes[i].getAttribute("type").split(",");
        removeItems = removes[i].getElementsByTagName("item");
        for (j = 0; j < removeItems.length; ++j) {
            elementId = removeItems[j].getAttribute("eid");
            for (k = 0; k < eventTypes.length; ++k) {
                EchoEventProcessor.removeHandler(elementId, eventTypes[k]);
            }
        }
    }
};

// _______________________
// Object EchoFocusManager

/**
 * Static object/namespace to manage component focus.
 */
function EchoFocusManager() { };

/**
 * Sets the focused state of a component.
 *
 * @param componentId the identifier of the component
 * @param focusState a boolean property describing whether the component is 
 *        focused
 */
EchoFocusManager.setFocusedState = function(componentId, focusState) {
    if (focusState) {
        EchoClientMessage.messageDocument.documentElement.setAttribute("focus", componentId);
    } else {
        var focusedComponentId = EchoClientMessage.messageDocument.documentElement.getAttribute("focus");
        if (!focusedComponentId || componentId == focusedComponentId) {
            // Set focus state to indeterminate if either no focused componentId is set 
            // or if focused componentId matches specified componentId.
            EchoClientMessage.messageDocument.documentElement.setAttribute("focus", "");
        }
    }
    EchoDebugManager.updateClientMessage();
};

// _________________________
// Object EchoHttpConnection

/**
 * Creates a new <code>EchoHttpConnection</code>.
 * This method simply configures the connection, the connection
 * will not be opened until <code>connect()</code> is invoked.
 * The responseHandler method property of the connection must be set
 * before the connection is executed.
 * An invalidResponseHandler method property may optionally be set to
 * be invoked if an invalid response is received.
 *
 * @param url the target URL
 * @param method the connection method, i.e., GET or POST
 * @param messageObject the message to send (may be a String or XML DOM)
 * @param contentType the request content-type
 */
function EchoHttpConnection(url, method, messageObject, contentType) {
    this.url = url;
    this.contentType = contentType;
    this.method = method;
    this.messageObject = messageObject;
    this.responseHandler = null;
    this.invalidResponseHandler = null;
}

/**
 * Executes the HTTP connection.
 * The responseHandler property of the connection must be set
 * before the connection is executed.
 * This method will return asynchronously; the <code>responseHandler</code>
 * callback will be invoked when a valid response is received.
 */
EchoHttpConnection.prototype.connect = function() {
    if (!this.responseHandler) {
        throw "EchoHttpConnection response handler not set.";
    }
    var usingActiveXObject = false;
    if (window.XMLHttpRequest) {
        this.xmlHttpRequest = new XMLHttpRequest();
    } else if (window.ActiveXObject) {
        usingActiveXObject = true;
        this.xmlHttpRequest = new ActiveXObject("Microsoft.XMLHTTP");
    } else {
        throw "Connect failed: Cannot create XMLHttpRequest.";
    }
    var instance = this;
    this.xmlHttpRequest.onreadystatechange = function() { instance.processReadyStateChange(); };
    this.xmlHttpRequest.open(this.method, this.url, true);
    if (this.contentType && (usingActiveXObject || this.xmlHttpRequest.setRequestHeader)) {
        this.xmlHttpRequest.setRequestHeader("Content-Type", this.contentType);
    }
    this.xmlHttpRequest.send(this.messageObject ? this.messageObject : null);
};

/**
 * Returns the response as text.
 * This method may only be invoked from a response handler.
 *
 * @return the response, as text
 */
EchoHttpConnection.prototype.getResponseText = function() {
    return this.xmlHttpRequest ? this.xmlHttpRequest.responseText : null;
};

/**
 * Returns the response as an XML DOM.
 * This method may only be invoked from a response handler.
 *
 * @return the response, as an XML DOM
 */
EchoHttpConnection.prototype.getResponseXml = function() {
    return this.xmlHttpRequest ? this.xmlHttpRequest.responseXML : null;
};

/**
 * Event listener for <code>readystatechange</code> events received from
 * the <code>XMLHttpRequest</code>.
 */
EchoHttpConnection.prototype.processReadyStateChange = function() {
    if (this.xmlHttpRequest.readyState == 4) {
        if (this.xmlHttpRequest.status == 200) {
            if (!this.responseHandler) {
                throw "EchoHttpConnection response handler not set.";
            }
            this.responseHandler(this);
        } else {
            if (this.invalidResponseHandler) {
                this.invalidResponseHandler(this);
            } else {
                throw "Invalid HTTP Response code (" + this.xmlHttpRequest.status + ") and no handler set.";
            }
        }
    }
};

// _______________________
// Object EchoModalManager

/**
 * Manages the modal state of the application.
 */
function EchoModalManager() { }

/**
 * Element id of current modal object, or null if no object is currently 
 * modal.
 */
EchoModalManager.modalElementId = null;

/**
 * Determines if a particular element lies within the modal context.
 *
 * @param elementId the id of the element to analyze
 * @return true if the specified <code>elementId</code> is within the modal
 *         context
 */
EchoModalManager.isElementInModalContext = function(elementId) {
    var element = document.getElementById(elementId);
    if (EchoModalManager.modalElementId) {
        var modalElement = document.getElementById(EchoModalManager.modalElementId);
        return EchoDomUtil.isAncestorOf(modalElement, element);
    } else {
        return true;
    }
};

// _______________________________
// Object EchoScriptLibraryManager

/**
 * Static object/namespace to manage dynamic loading of client libraries.
 */
function EchoScriptLibraryManager() { }

/**
 * Library load-state constant indicating a library has been requested from 
 * the server.
 */
EchoScriptLibraryManager.STATE_REQUESTED = 1;

/**
 * Library load-state constant indicating a library has been successfully
 * retrieved and installed from the server.
 */
EchoScriptLibraryManager.STATE_LOADED = 2;

/**
 * Associative array mapping library service ids to load-states.
 */
EchoScriptLibraryManager.libraryLoadStateMap = new Array();

/**
 * Queries the state of the specified libary.
 *
 * @param serviceId the server service identifier of the library.
 * @return the load-state of the library, either
 *         <code>STATE_REQUESTED</code> or <code>STATE_LOADED</code>.
 */
EchoScriptLibraryManager.getState = function(serviceId) {
    return EchoScriptLibraryManager.libraryLoadStateMap[serviceId];
};

/**
 * Loads a JavaScript library.
 */
EchoScriptLibraryManager.loadLibrary = function(serviceId) {
    if (EchoScriptLibraryManager.getState(serviceId)) {
        // Library already present.
        return;
    }

    var conn = new EchoHttpConnection(EchoClientEngine.baseServerUri + "?serviceId=" + serviceId, "GET");
    conn.serviceId = serviceId;
    conn.responseHandler = EchoScriptLibraryManager.responseHandler;
    conn.connect();
    
    // Mark state as "requested" so that application will wait for library to load.
    EchoScriptLibraryManager.libraryLoadStateMap[serviceId] = EchoScriptLibraryManager.STATE_REQUESTED;
};

/**
 * Processes a valid response from the server.
 *
 * @param conn the EchoHttpConnection containing the response information.
 */
EchoScriptLibraryManager.responseHandler = function(conn) {
    // Execute library code.
    eval(conn.getResponseText());

    // Mark state as "loaded" so that application will discontinue waiting for library to load.
    EchoScriptLibraryManager.libraryLoadStateMap[conn.serviceId] = EchoScriptLibraryManager.STATE_LOADED;
};

// ________________________
// Object EchoServerMessage

/**
 * Static object to process synchronization messages received from server.
 */
function EchoServerMessage() { }

EchoServerMessage.STATUS_INITIALIZED = 0;
EchoServerMessage.STATUS_PROCESSING = 1;
EchoServerMessage.STATUS_PROCESSING_COMPLETE = 2;
EchoServerMessage.STATUS_PROCESSING_FAILED = 3;

/**
 * DOM of the ServerMessage.
 */
EchoServerMessage.messageDocument = null;

/**
 * Identifier returned from setInterval() for asynchronously monitoring 
 * loading of JavaScript libraries.
 */
EchoServerMessage.backgroundIntervalId = null;

/**
 * Callback to invoke when ServerMessage processing has completed.
 * Set via init() method.
 */
EchoServerMessage.processingCompleteListener = null;

/**
 * ServerMessage processing status, one of the following constants:
 * <ul>
 *  <li><code>EchoServerMessage.STATUS_INITIALIZED</code></li>
 *  <li><code>EchoServerMessage.STATUS_PROCESSING</code></li>
 *  <li><code>EchoServerMessage.STATUS_PROCESSING_COMPLETE</code></li>
 *  <li><code>EchoServerMessage.STATUS_PROCESSING_FAILED</code></li>
 * </ul>
 */
EchoServerMessage.status = EchoServerMessage.STATUS_INITIALIZED;

/**
 * Initializes the state of the ServerMessage processor.
 *
 * @param messageDocument the ServerMessage XML document to be processed
 * @param processingCompleteListener a callback to be invoked when processing
 *        has been completed
 */
EchoServerMessage.init = function(messageDocument, processingCompleteListener) {
    EchoServerMessage.messageDocument = messageDocument;
    EchoServerMessage.backgroundIntervalId = null;
    EchoServerMessage.processingCompleteListener = processingCompleteListener;
    EchoDebugManager.updateServerMessage();
};

/**
 * Determines if all required JavaScript libraries have been loaded.
 *
 * @return true if all libraries have been loaded.
 */
EchoServerMessage.isLibraryLoadComplete = function() {
    // 'complete' flag is used to set status to 'failed' in the event of an exception.
    var complete = false;
    try {
        var librariesElement = EchoServerMessage.messageDocument.getElementsByTagName("libraries").item(0);
        var libraryElements = librariesElement.getElementsByTagName("library");
        
        var returnValue = true;
        for (var i = 0; i < libraryElements.length; ++i) {
            var serviceId = libraryElements.item(i).getAttribute("service-id");
            if (EchoScriptLibraryManager.getState(serviceId) != EchoScriptLibraryManager.STATE_LOADED) {
                // A library that requires immediate loading is not yet available.
                returnValue = false;
                break;
            }
        }
        complete = true;
        return returnValue;
    } finally {
        if (!complete) {
            EchoServerMessage.status = EchoServerMessage.STATUS_PROCESSING_FAILED;
        }
    }
};

/**
 * Parses 'libraries' element of ServerMessage and dynamically loads
 * external JavaScript resources.
 * The libraries are loaded asynchronously--invocation of this
 * method will return before the actual libraries have been loaded.
 */
EchoServerMessage.loadLibraries = function() {
    var librariesElement = EchoServerMessage.messageDocument.getElementsByTagName("libraries").item(0);
    var headElement = document.getElementsByTagName("head").item(0);
    if (!librariesElement) {
        return;
    }
    var libraryElements = librariesElement.getElementsByTagName("library");
    for (var i = 0; i < libraryElements.length; ++i) {
        var serviceId = libraryElements.item(i).getAttribute("service-id");
        EchoScriptLibraryManager.loadLibrary(serviceId);
    }
};

/**
 * Processes the ServerMessage.
 * The processing is performed asynchronously--this method will return before
 * the processing has been completed.
 */
EchoServerMessage.process = function() {
    EchoServerMessage.processPhase1();
};

/**
 * Configures the asynchronous server polling system by retrieving
 * the "asyncinterval" attribute from the ServerMessage and starting
 * the asynchronous monitor if required.
 */
EchoServerMessage.processAsyncConfig = function() {
    var timeInterval = parseInt(EchoServerMessage.messageDocument.documentElement.getAttribute("async-interval"));
    if (!isNaN(timeInterval)) {
        EchoAsyncMonitor.timeInterval = timeInterval;
        EchoAsyncMonitor.start();
    }
};

/**
 * Configures various application level properties, including:
 * <ul>
 *  <li>Modal context</li>
 *  <li>Layout direction (LTR/RTL)</li>
 * </ul>
 */
EchoServerMessage.processApplicationProperties = function() {
    EchoModalManager.modalElementId = EchoServerMessage.messageDocument.documentElement.getAttribute("modal-id");
    if (EchoServerMessage.messageDocument.documentElement.getAttribute("root-layout-direction")) {
        document.documentElement.style.direction 
                = EchoServerMessage.messageDocument.documentElement.getAttribute("root-layout-direction");
    }
};

/**
 * Processes all of the 'messagepart' directives contained in the ServerMessage.
 */
EchoServerMessage.processMessageParts = function() {
    // 'complete' flag is used to set status to 'failed' in the event of an exception.
    var complete = false;
    try {
        var messagePartElements = EchoServerMessage.messageDocument.getElementsByTagName("message-part");
        for (var i = 0; i < messagePartElements.length; ++i) {
            var messagePartElement = messagePartElements[i];
            var processorName = messagePartElement.getAttribute("processor");
            var processor = eval(processorName);
            if (!processor || !processor.process) {
                throw "Processor \"" + processorName + "\" not available.";
            }
            processor.process(messagePartElement);
        }
        complete = true;
    } finally {
        EchoServerMessage.status = complete ? EchoServerMessage.STATUS_PROCESSING_COMPLETE 
                : EchoServerMessage.STATUS_PROCESSING_FAILED;
    }
};

/**
 * Performs FIRST phase of synchronization response processing.
 * During this phase required JavaScript libraries are loaded.
 * Library loading is performed asynchronously, so a callback
 * interval is established to wait for the libraries to load
 * before the second phase of processing is invoked.
 */
EchoServerMessage.processPhase1 = function() {
    EchoServerMessage.status = EchoServerMessage.STATUS_PROCESSING;
    EchoServerMessage.loadLibraries();
    if (EchoServerMessage.isLibraryLoadComplete()) {
        EchoServerMessage.processPhase2();
    } else {
        EchoServerMessage.backgroundIntervalId = window.setInterval("EchoServerMessage.waitForLibraries();", 20);
    }
};

/**
 * Performs SECOND phase of synchronization response processing.
 * This phase may only be performed after all required JavaScript
 * libraries have been loaded.   During this phase, directives provided
 * in the ServerMessage are processed.  Additionally, the client modal 
 * state and asynchoronous serevr polling configurations are established.
 */
EchoServerMessage.processPhase2 = function() {
    EchoServerMessage.processMessageParts();
    EchoServerMessage.processApplicationProperties();
    if (EchoServerMessage.processingCompleteListener) {
        EchoServerMessage.processingCompleteListener();
    }
    EchoServerMessage.processAsyncConfig();
};

/**
 * Executed at intervals (via setInterval) to determine if all libraries
 * have been loaded.  In the event that all libraries have been loaded,
 * the interval operation is cleared and message part processing is started.
 */
EchoServerMessage.waitForLibraries = function() {
    if (EchoServerMessage.isLibraryLoadComplete()) {
        window.clearInterval(EchoServerMessage.backgroundIntervalId);
        EchoServerMessage.backgroundIntervalId = null;
        EchoServerMessage.processPhase2();
    }
};

// ____________________________
// Object EchoServerTransaction

/**
 * Static object/namespace to manage HTTP synchronization connections to 
 * server.
 */
function EchoServerTransaction() { }

/**
 * Flag specifying whether a server transaction is active.
 * This flag should be queried by event handlers before
 * performing operations that are not compatible with
 * an open server transaction.
 */
EchoServerTransaction.active = false;

/**
 * Servlet query for synchronize service.
 */
EchoServerTransaction.synchronizeServiceRequest = "?serviceId=Echo.Synchronize";

/**
 * Initiates a client-server transaction my making a request to the server.
 * This operation is asynchronous; this method will return before the server
 * issues a response.
 */
EchoServerTransaction.connect = function() {
    EchoServerDelayMessage.activate();
    EchoAsyncMonitor.stop();
    var conn = new EchoHttpConnection(EchoClientEngine.baseServerUri + EchoServerTransaction.synchronizeServiceRequest, 
            "POST", EchoClientMessage.messageDocument, "text/xml");
    conn.responseHandler = EchoServerTransaction.responseHandler;
    conn.invalidResponseHandler = EchoServerTransaction.invalidResponseHandler;
    EchoServerTransaction.active = true;
    conn.connect();
    
    // Reset client message.
    EchoClientMessage.reset();
};

/**
 * Processes an invalid HTTP response to the synchronize request.
 *
 * @param conn the EchoHttpConnection containing the response information. 
 */
EchoServerTransaction.invalidResponseHandler = function(conn) {
    EchoServerTransaction.postProcess();
    alert("Invalid response from server: " + conn.getResponseText());
};

/**
 * Handles post processing cleanup tasks, i.e., disabling server message delay
 * pane and setting active flag state to false.
 */
EchoServerTransaction.postProcess = function() {
    EchoServerDelayMessage.deactivate();
    EchoServerTransaction.active = false;
};

/**
 * Processes a (valid) HTTP response to the synchronize request.
 *
 * @param conn the EchoHttpConnection containing the response information.
 */
EchoServerTransaction.responseHandler = function(conn) {
    EchoServerMessage.init(conn.getResponseXml(), EchoServerTransaction.postProcess);
    EchoServerMessage.process();
};

// _______________________
// Object EchoWindowUpdate

/**
 * MessageProcessor implementation to perform browser window-related updates,
 * such as setting the window title.
 */
function EchoWindowUpdate() { }

/**
 * MessageProcessor process() implementation.
 */
EchoWindowUpdate.process = function(messagePartElement) {
    for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
        if (messagePartElement.childNodes[i].nodeType == 1) {
            switch (messagePartElement.childNodes[i].tagName) {
            case "set-focus":
                EchoWindowUpdate.processSetFocus(messagePartElement.childNodes[i]);
                break;
            case "set-title":
                EchoWindowUpdate.processSetTitle(messagePartElement.childNodes[i]);
                break;
            }
        }
    }
};

/**
 * Processes a server directive to set the focused component.
 *
 * @param setFocusElement the "set-focus" directive element
 */
EchoWindowUpdate.processSetFocus = function(setFocusElement) {
    var elementId = setFocusElement.getAttribute("element-id");
    var element = document.getElementById(elementId);
    if (element.focus) {
        element.focus();
    }
};

/**
 * Processes a server directive to set the window title.
 *
 * @param setTitleElement the "set-title" directive element
 */
EchoWindowUpdate.processSetTitle = function(setTitleElement) {
    document.title = setTitleElement.getAttribute("title");
};

// _____________________
// Static Initialization

EchoClientMessage.reset();
