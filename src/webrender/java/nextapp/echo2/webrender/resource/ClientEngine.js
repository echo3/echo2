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

function EchoAsyncMonitor() { }

/**
 * The time, in milleseconds, between polling requests.
 */
EchoAsyncMonitor.timeInterval = 500;

EchoAsyncMonitor.pollServiceRequest = "?serviceId=Echo.AsyncMonitor";

EchoAsyncMonitor.timeoutId = null;

EchoAsyncMonitor.connect = function() {
    var conn = new EchoHttpConnection(EchoClientEngine.baseServerUri + EchoAsyncMonitor.pollServiceRequest, "GET");
    conn.responseHandler = EchoAsyncMonitor.responseHandler;
    conn.invalidResponseHandler = EchoAsyncMonitor.invalidResponseHandler;
    conn.connect();
};

/**
 * Processes an invalid response from the server.
 */
EchoAsyncMonitor.invalidResponseHandler = function() {
    alert("Invalid response from server to asynchronous polling connection.");
};

EchoAsyncMonitor.start = function() {
    if (!EchoServerTransaction.active) {
        EchoAsyncMonitor.timeoutId = window.setTimeout("EchoAsyncMonitor.connect();", 
                EchoAsyncMonitor.timeInterval);
    }
};

EchoAsyncMonitor.stop = function() {
    if (EchoAsyncMonitor.timeoutId) {
        window.clearTimeout(EchoAsyncMonitor.timeoutId);
        EchoAsyncMonitor.timeoutId = null;
    }
};

/**
 * Processes a valid response from the server.
 *
 * @param conn the EchoHttpConnection containing the response information.
 */
EchoAsyncMonitor.responseHandler = function(conn) {
    if ("true" == conn.getResponseXml().documentElement.getAttribute("requestsync")) {
        EchoServerTransaction.connect();
    } else {
        EchoAsyncMonitor.start();
    }
    
};

// _______________________
// Object EchoBlockingPane

/** 
 * Temporarily disconnects UI from input during synchronous client/server
 * transactions.  Handles display/removal of "please wait"-type messages,
 * "spinner graphics", etc.
 *
 * Do not instantiate. 
 */
function EchoBlockingPane() { }

/** 
 * id of HTML element providing "long-running" delay message.  
 * This element is shown/hidden as necessary when long-running delays
 * occur or are completed.
 */
EchoBlockingPane.delayMessageTimeoutId = null;

/** Timeout (in ms) before "long-running" delay message is displayed. */ 
EchoBlockingPane.timeout = 500;

EchoBlockingPane.activate = function() {
    var blockingPane = document.getElementById("blockingPane");
    if (!blockingPane) {
        return;
    }
    blockingPane.style.visibility = "visible";
    EchoBlockingPane.delayMessageTimeoutId = window.setTimeout("EchoBlockingPane.activateDelayMessage()", 
            EchoBlockingPane.timeout);
};

EchoBlockingPane.activateDelayMessage = function() {
    EchoBlockingPane.cancelDelayMessageTimeout();
    var delayPane = document.getElementById("blockingPaneDelayMessage");
    if (!delayPane) {
        return;
    }
    delayPane.style.visibility = "visible";
};

EchoBlockingPane.cancelDelayMessageTimeout = function() {
    if (EchoBlockingPane.delayMessageTimeoutId) {
        window.clearTimeout(EchoBlockingPane.delayMessageTimeoutId);
        EchoBlockingPane.delayMessageTimeoutId = null;
    }
};

EchoBlockingPane.deactivate = function() {
    EchoBlockingPane.cancelDelayMessageTimeout();
    var blockingPane = document.getElementById("blockingPane");
    var delayPane = document.getElementById("blockingPaneDelayMessage");
    if (blockingPane) {
        blockingPane.style.visibility = "hidden";
    }
    if (delayPane) {
        delayPane.style.visibility = "hidden";
    }
};

EchoBlockingPane.process = function(messagePartElement) {
    for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
        if (messagePartElement.childNodes[i].nodeType == 1) {
            switch (messagePartElement.childNodes[i].tagName) {
            case "setdelaymessage":
                EchoBlockingPane.processSetDelayMessage(messagePartElement.childNodes[i]);
                break;
            }
        }
    }
};

EchoBlockingPane.processSetDelayMessage = function(setDelayMessageElement) {
    var blockingPane = document.getElementById("blockingPane");
    var i;
    // Remove existing children.
    for (i = blockingPane.childNodes.length - 1; i >= 0; --i) {
        blockingPane.removeChild(blockingPane.childNodes[i]);
    }
    // Add new children.
    for (i = 0; i < setDelayMessageElement.childNodes.length; ++i) {
        blockingPane.appendChild(EchoDomUtil.importNode(document, setDelayMessageElement.childNodes[i], true));
    }
};

// _________________________
// Object EchoClientAnalyzer

/** Do not instantiate. */
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
 * Stores a boolean client property.
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
 * Stores a integer client property.
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
 * Stores a text client property.
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
        if (componentId == propertyElements[i].getAttribute("componentid") &&
                propertyName == propertyElements[i].getAttribute("name")) {
            propertyElement = propertyElements[i];
            break;
        } 
    }
    if (!propertyElement) {
        propertyElement = messagePartElement.ownerDocument.createElement("property");
        propertyElement.setAttribute("componentid", componentId);
        propertyElement.setAttribute("name", propertyName);
        messagePartElement.appendChild(propertyElement);
    }
    
    return propertyElement;
};

/**
 * Creates or retrieves the "messagepart" DOM element from the 
 * EchoClientMessage with the specified processor attribute.
 *
 * @param processor A string representing the processor type of
 *        the messagepart element to return.
 * @return The "messagepart" DOM element.
 */
EchoClientMessage.getMessagePart = function(processor) {
    // Return existing <messagepart> if available.
    var messagePartElements = EchoClientMessage.messageDocument.getElementsByTagName("messagepart");
    for (var i = 0; i < messagePartElements.length; ++i) {
        if (messagePartElements[i].getAttribute("processor") == processor) {
            return messagePartElements[i];
        }
    }
    
    // Create new <messagepart> if none exists.
    var messagePartElement = EchoClientMessage.messageDocument.createElement("messagepart");
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
        if (componentId == propertyElements[i].getAttribute("componentid") &&
                propertyName == propertyElements[i].getAttribute("name")) {
            return propertyElements[i].getAttribute("value");
        }
    }
    return null;
};

EchoClientMessage.reset = function() {
    EchoClientMessage.messageDocument = null;
    EchoClientMessage.messageDocument 
            = EchoDomUtil.createDocument("http://www.nextapp.com/products/echo2/climsg", "clientmessage");
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
    actionElement.setAttribute("componentid", componentId);
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

// _______________________
// Object EchoDebugManager

function EchoDebugManager() { }

EchoDebugManager.debugWindow = null;

EchoDebugManager.consoleWrite = function(message) {
    if (!EchoDebugManager.debugWindow || EchoDebugManager.debugWindow.closed) {
        return;
    }
    EchoDebugManager.debugWindow.EchoDebug.consoleWrite(message);
};

EchoDebugManager.launch = function() {
    EchoDebugManager.debugWindow = window.open(EchoClientEngine.baseServerUri + "?serviceId=Echo.Debug", "EchoDebug", 
            "width=400,height=650,resizable=yes", true);
};

EchoDebugManager.updateClientMessage = function() {
    if (!EchoDebugManager.debugWindow || EchoDebugManager.debugWindow.closed) {
        return;
    }
    EchoDebugManager.debugWindow.EchoDebug.displayClientMessage(EchoClientMessage.messageDocument);
};

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

//BUGBUG. move this and similar process methods into inner static processor classes
EchoDomPropertyStore.process = function(messagePartElement) {
    for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
        if (messagePartElement.childNodes[i].nodeType == 1) {
            switch (messagePartElement.childNodes[i].tagName) {
            case "storeproperty":
                EchoDomPropertyStore.processStoreProperty(messagePartElement.childNodes[i]);
                break;
            }
        }
    }
};

EchoDomPropertyStore.processStoreProperty = function(storePropertyElement) {
    var propertyName = storePropertyElement.getAttribute("name");
    var propertyValue = storePropertyElement.getAttribute("value");
    var items = storePropertyElement.getElementsByTagName("item");
    for (var i = 0; i < items.length; ++i) {
        var elementId = items[i].getAttribute("eid");
        EchoDomPropertyStore.setPropertyValue(elementId, propertyName, propertyValue);
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

/**
 *
 */
EchoDomUpdate.process = function(messagePartElement) {
    for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
        if (messagePartElement.childNodes[i].nodeType == 1) {
            switch (messagePartElement.childNodes[i].tagName) {
            case "domadd":
                this.processAdd(messagePartElement.childNodes[i]);
                break;
            case "domremove":
                this.processRemove(messagePartElement.childNodes[i]);
                break;
            case "domremovechildren":
                this.processRemoveChildren(messagePartElement.childNodes[i]);
                break;
            case "attributeupdate":
                this.processAttributeUpdate(messagePartElement.childNodes[i]);
                break;
            case "styleupdate":
                this.processStyleUpdate(messagePartElement.childNodes[i]);
                break;
            }
        }
    }
};

EchoDomUpdate.processAdd = function(domAddElement) {
    var parentId = domAddElement.getAttribute("parentid");
    var siblingId = domAddElement.getAttribute("siblingid");
    var parentElement = document.getElementById(parentId);
    var siblingElement = null;
    if (siblingId) {
        siblingElement = document.getElementById(siblingId);
        if (!siblingElement) {
            throw new EchoDomUpdateTargetNotFoundException("Add", "sibling", siblingId);
        }
    }
    if (!parentElement) {
         throw new EchoDomUpdateTargetNotFoundException("Add", "parent", parentId);
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

EchoDomUpdate.processAttributeUpdate = function(attributeUpdateElement) {
    var targetId = attributeUpdateElement.getAttribute("targetid");
    var targetElement = document.getElementById(targetId);
    if (!targetElement) {
        throw new EchoDomUpdateTargetNotFoundException("AttributeUpdate", "target", targetId);
    }
    targetElement[attributeUpdateElement.getAttribute("name")] = attributeUpdateElement.getAttribute("value");
};

EchoDomUpdate.processRemove = function(domRemoveElement) {
    var targetId = domRemoveElement.getAttribute("targetid");
    var targetElement = document.getElementById(targetId);
    if (!targetElement) {
        return;
    }
    targetElement.parentNode.removeChild(targetElement);
};

EchoDomUpdate.processRemoveChildren = function(domRemoveElement) {
    var targetId = domRemoveElement.getAttribute("targetid");
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

EchoDomUpdate.processStyleUpdate = function(styleUpdateElement) {
    var targetId = styleUpdateElement.getAttribute("targetid");
    var targetElement = document.getElementById(targetId);
    if (!targetElement) {
        throw new EchoDomUpdateTargetNotFoundException("StyleUpdate", "target", targetId);
    }
    targetElement.style[styleUpdateElement.getAttribute("name")] = styleUpdateElement.getAttribute("value");
};

// ___________________________________________
// Object EchoDomUpdateTargetNotFoundException

function EchoDomUpdateTargetNotFoundException(updateType, targetType, targetId) {
    this.targetId = targetId;
    this.targetType = targetType;
    this.updateType = updateType;
}

EchoDomUpdateTargetNotFoundException.prototype.toString = function() {
    return "Failed to perform \"" + this.updateType + "\": " + this.targetType + " element \"" + this.targetId + "\" not found.";
};

// __________________
// Object EchoDomUtil

/** Do not instantiate. */
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

EchoDomUtil.applyStyle = function(element, cssText) {
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

/* Do not instantiate */
function EchoEventProcessor() { }

EchoEventProcessor.eventTypeToHandlersMap = new Array();

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
//    if (targetElement.nodeType != 1) {
        // Only process events from element nodes.
//        return;
//    }
    
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

/**
 * EchoEventUpdate Message Part Procesor implementation.
 *
 * @param messagePartElement the ServerMessage "messagepart"
 *        element containing event add and remove directives
 *        to process.
 */
EchoEventUpdate.process = function(messagePartElement) {
    var i, j, k, eventTypes, handlers, addItems, removeItems, elementId;
    var adds = messagePartElement.getElementsByTagName("eventadd");
    var removes = messagePartElement.getElementsByTagName("eventremove");
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

// _________________________
// Object EchoHttpConnection

function EchoHttpConnection(url, method, messageObject, contentType) {
    this.url = url;
    this.contentType = contentType;
    this.method = method;
    this.messageObject = messageObject;
    this.responseHandler = null;
    this.invalidResponseHandler = null;
}

EchoHttpConnection.prototype.connect = function() {
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
 * Manages active modal objects.
 */
function EchoModalManager() { }

EchoModalManager.modalElementId = null;

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
 * Manages dynamic loading of external JavaScript libraries.
 */
function EchoScriptLibraryManager() { }

EchoScriptLibraryManager.STATE_REQUESTED = 1;
EchoScriptLibraryManager.STATE_LOADED = 2;

EchoScriptLibraryManager.loadedLibraries = new Array();

EchoScriptLibraryManager.getState = function(serviceId) {
    return EchoScriptLibraryManager.loadedLibraries[serviceId];
};

EchoScriptLibraryManager.isLoaded = function(serviceId) {
    return EchoScriptLibraryManager.loadedLibraries[serviceId] == EchoScriptLibraryManager.STATE_LOADED;
};

EchoScriptLibraryManager.loadLibrary = function(serviceId, serviceUri) {
    if (EchoScriptLibraryManager.getState(serviceId)) {
        // Library already present.
        return;
    }

    var conn = new EchoHttpConnection(serviceUri, "GET");
    conn.serviceId = serviceId;
    conn.responseHandler = EchoScriptLibraryManager.responseHandler;
    conn.connect();
    
    // Mark state as "requested" so that application will wait for library to load.
    EchoScriptLibraryManager.loadedLibraries[serviceId] = EchoScriptLibraryManager.STATE_REQUESTED;
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
    EchoScriptLibraryManager.loadedLibraries[conn.serviceId] = EchoScriptLibraryManager.STATE_LOADED;
};

// ________________________
// Object EchoServerMessage

/**
 * Static object to process messages received from server.
 * Do not instantiate.
 */
function EchoServerMessage() { }

EchoServerMessage.STATUS_INITIALIZED = 0;
EchoServerMessage.STATUS_PROCESSING = 1;
EchoServerMessage.STATUS_PROCESSING_COMPLETE = 2;
EchoServerMessage.STATUS_PROCESSING_FAILED = 3;

/**
 * DOM of the server message.
 */
EchoServerMessage.messageDocument = null;

/**
 * Identifier returned from setInterval() for asynchronously monitoring 
 * loading of JavaScript libraries.
 */
EchoServerMessage.backgroundIntervalId = null;

EchoServerMessage.processingCompleteListener = null;

EchoServerMessage.status = EchoServerMessage.STATUS_INITIALIZED;

/**
 * Initializes (or resets) the life-cycle of the server message processor.
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
            var serviceId = libraryElements.item(i).getAttribute("serviceid");
            if (libraryElements.item(i).getAttribute("wait") == "true" &&
                    EchoScriptLibraryManager.getState(serviceId) != EchoScriptLibraryManager.STATE_LOADED) {
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
 * Parses 'libraries' element of server message and adds 'script' elements
 * to main DOM to dynamically load JavaScript libraries.
 * The libraries themselves are loaded asynchronously--invocation of this
 * method will return before the actual libraries have been loaded.
 */
EchoServerMessage.loadLibraries = function() {
    var applicationUri = EchoServerMessage.messageDocument.documentElement.getAttribute("applicationuri");
    var librariesElement = EchoServerMessage.messageDocument.getElementsByTagName("libraries").item(0);
    var headElement = document.getElementsByTagName("head").item(0);
    if (!librariesElement) {
        return;
    }
    var libraryElements = librariesElement.getElementsByTagName("library");
    for (var i = 0; i < libraryElements.length; ++i) {
        var serviceId = libraryElements.item(i).getAttribute("serviceid");
        var serviceUri = applicationUri + "?serviceId=" + serviceId;
        EchoScriptLibraryManager.loadLibrary(serviceId, serviceUri);
    }
};

/**
 * Processes the server message.
 * The processing is done asynchronously--this method will return before
 * the processing has been completed.
 */
EchoServerMessage.process = function() {
    EchoServerMessage.processPhase1();
};

EchoServerMessage.processAsyncConfig = function() {
    var timeInterval = parseInt(EchoServerMessage.messageDocument.documentElement.getAttribute("asyncinterval"));
    if (!isNaN(timeInterval)) {
        EchoAsyncMonitor.timeInterval = timeInterval;
        EchoAsyncMonitor.start();
    }
};

EchoServerMessage.processModalState = function() {
    EchoModalManager.modalElementId = EchoServerMessage.messageDocument.documentElement.getAttribute("modalid");
};

/**
 * Processes all of the 'messagepart' directives contained in the server message.
 */
EchoServerMessage.processMessageParts = function() {
    // 'complete' flag is used to set status to 'failed' in the event of an exception.
    var complete = false;
    try {
        var messagePartElements = EchoServerMessage.messageDocument.getElementsByTagName("messagepart");
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

EchoServerMessage.processPhase1 = function() {
    EchoServerMessage.status = EchoServerMessage.STATUS_PROCESSING;
    EchoServerMessage.loadLibraries();
    if (EchoServerMessage.isLibraryLoadComplete()) {
        EchoServerMessage.processPhase2();
    } else {
        EchoServerMessage.backgroundIntervalId = window.setInterval("EchoServerMessage.waitForLibraries();", 20);
    }
};

EchoServerMessage.processPhase2 = function() {
    EchoServerMessage.processMessageParts();
    EchoServerMessage.processModalState();
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
 * Static object representing a client-server transaction over an
 * XMLHttpRequest.
 * 
 * Do not instantiate.
 */
function EchoServerTransaction() { }

/**
 * Flag specifying whether a server transaction is active.
 * This flag should be queried by event handlers before
 * performing operations that are not compatible with
 * an open server transaction.
 */
EchoServerTransaction.active = false;

EchoServerTransaction.synchronizeServiceRequest = "?serviceId=Echo.Synchronize";

/**
 * Initiates a client-server transaction my making a request to the server.
 * This operation is asynchronous; this method will return before the server
 * issued a response.
 */
EchoServerTransaction.connect = function() {
    EchoBlockingPane.activate();
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

EchoServerTransaction.postProcess = function() {
    EchoBlockingPane.deactivate();
    EchoServerTransaction.active = false;
};

/**
 * Processes an invalid response from the server.
 */
EchoServerTransaction.invalidResponseHandler = function(conn) {
    EchoServerTransaction.postProcess();
    alert("Invalid response from server: " + conn.getResponseText());
};

/**
 * Processes a valid response from the server.
 *
 * @param conn the EchoHttpConnection containing the response information.
 */
EchoServerTransaction.responseHandler = function(conn) {
    EchoServerMessage.init(conn.getResponseXml(), EchoServerTransaction.postProcess);
    EchoServerMessage.process();
};

// _______________________
// Object EchoWindowUpdate

function EchoWindowUpdate() { }

EchoWindowUpdate.process = function(messagePartElement) {
    for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
        if (messagePartElement.childNodes[i].nodeType == 1) {
            switch (messagePartElement.childNodes[i].tagName) {
            case "settitle":
                EchoWindowUpdate.processSetTitle(messagePartElement.childNodes[i]);
                break;
            }
        }
    }
};

EchoWindowUpdate.processSetTitle = function(setTitleElement) {
    document.title = setTitleElement.getAttribute("title");
};

// _____________________
// Static Initialization

EchoClientMessage.reset();
