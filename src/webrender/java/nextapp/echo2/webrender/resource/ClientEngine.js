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
 * Processes an invalid response to the poll request.
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
    if ("true" == conn.getResponseXml().documentElement.getAttribute("request-sync")) {
        // Server is requesting synchronization: Initiate server transaction.
        EchoServerTransaction.connect();
    } else {
        // Server does not require synchronization: restart countdown to next poll request.
        EchoAsyncMonitor.start();
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
 * @param messagePartElement the <code>message-part</code> element
 *        in which generated property XML elements should be stored
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
 * @param messagePartElement the <code>message-part</code> element
 *        in which generated property XML elements should be stored
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
 * @param messagePartElement the <code>message-part</code> element
 *        in which generated property XML elements should be stored
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

// ______________________________
// Object EchoClientConfiguration

/**
 * Static/object namespace which provides general-purpose Client Engine
 * configuration settings.
 */
function EchoClientConfiguration() { }

/**
 * MessagePartProcessor implementation for EchoClientConfiguration.
 */
EchoClientConfiguration.MessageProcessor = function() { };

/**
 * MessagePartProcessor process() method implementation.
 */
EchoClientConfiguration.MessageProcessor.process = function(messagePartElement) {
    for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
        if (messagePartElement.childNodes[i].nodeType == 1) {
            switch (messagePartElement.childNodes[i].tagName) {
            case "store":
                EchoClientConfiguration.MessageProcessor.processStore(messagePartElement.childNodes[i]);
                break;
            }
        }
    }
};

/**
 * Proceses a <code>store</code> message to store ClientConfiguration information
 * received from the server.
 *
 * @param storeElement the <code>store</code> element to process
 */
EchoClientConfiguration.MessageProcessor.processStore = function(storeElement) {
    var propertyElements = storeElement.getElementsByTagName("property");
    for (var i = 0; i < propertyElements.length; ++i) {
        var name = propertyElements[i].getAttribute("name");
        var value = propertyElements[i].getAttribute("value");
        EchoClientConfiguration.propertyMap[name] = value;
    }
};

EchoClientConfiguration.propertyMap = new Array();

EchoClientConfiguration.propertyMap["defaultServerErrorMessage"] 
        = "An application error has occurred.  Your session has been reset.";
EchoClientConfiguration.propertyMap["defaultSessionExpirationMessage"] 
        = "Your session has been reset due to inactivity.";

// _______________________
// Object EchoClientEngine

/**
 * Static object/namespace providing core client engine functionality.
 */
function EchoClientEngine() { }

/**
 * The base URI of the Echo application server.
 */
EchoClientEngine.baseServerUri = null;

/**
 * Flag indicating whether debugging options (e.g., Debug Pane) are enabled.
 */
EchoClientEngine.debugEnabled = true;

/**
 * Configures browser-specific settings based on ClientProperties results.
 * Invoked when ClientProperties are stored.
 */
EchoClientEngine.configure = function() {
    if (EchoClientProperties.get("browserInternetExplorer")) {
        EchoVirtualPosition.init();
    }
};

/**
 * Initializes the Echo2 Client Engine.
 *
 * @param baseServerUri the base URI of the Echo application server
 */
EchoClientEngine.init = function(baseServerUri, debugEnabled) {
    EchoClientEngine.baseServerUri = baseServerUri;
    EchoClientEngine.debugEnabled = debugEnabled;
    
    // Launch debug window if requested in URI.
    if (EchoClientEngine.debugEnabled && 
            window.location.search && window.location.search.toLowerCase().indexOf("debug") != -1) {
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
 * Handles a client-side error.
 */
EchoClientEngine.processClientError = function(message, ex) {
    var errorText;
    if (ex instanceof Error) {
        errorText = "Error Name: " + ex.name + "\n"
                + "Error Message: " + ex.message;
    } else {
        errorText = "Error: " + ex;
    }

    alert("The following client application error has occurred:\n\n" 
            + "---------------------------------\n" 
            + message + "\n\n" 
            + errorText + "\n"
            + "---------------------------------\n\n"
            + "Please contact your server administrator.\n\n");
};

/**
 * Handles the expiration of server-side session information.
 */
EchoClientEngine.processSessionExpiration = function() {
    var message = EchoClientConfiguration.propertyMap["sessionExpirationMessage"];
    var uri = EchoClientConfiguration.propertyMap["sessionExpirationUri"];

    if (message || uri) {
	    if (message) {
	        alert(message);
	    }
	    if (uri) {
	        window.location.href = uri;
	    } else {
            window.location.reload();
	    }
    } else {
        message = EchoClientConfiguration.propertyMap["defaultSessionExpirationMessage"];
        alert(message);
        window.location.reload();
    }
};

/**
 * Handles a server-side error.
 */
EchoClientEngine.processServerError = function() {
    var message = EchoClientConfiguration.propertyMap["serverErrorMessage"];
    var uri = EchoClientConfiguration.propertyMap["serverErrorUri"];

    if (message || uri) {
	    if (message) {
	        alert(message);
	    }
	    if (uri) {
	        window.location.href = uri;
	    } else {
	        if (EchoServerMessage.transactionCount > 0) {
	            // Only reload if not on first transaction to avoid infinite crash loop.
                window.location.reload();
	        }
	    }
    } else {
        message = EchoClientConfiguration.propertyMap["defaultServerErrorMessage"];
        alert(message);
        if (EchoServerMessage.transactionCount > 0) {
            // Only reload if not on first transaction to avoid infinite crash loop.
            window.location.reload();
        }
    }
};

/**
 * Verifies that the given element is eligible to receive input based on the 
 * current condition of the user interface.
 * This method should be invoked by UI listeners before procsesing any user input.
 * This method will ensure that no server transaction is active and the element
 * is within the current modal context.
 * The method will additionally verify that the element does not have the 
 * <code>EchoDomPropertyStore</code> property 
 * <code>EchoClientEngine.inputDisabled</code> set to true.
 *
 * The 'allowInputDuringTransaction' flag (default value = false) may be used to
 * allow input while client/server transactions are being processed.  This flag
 * should be used with extreme caution, as you'll then need to handle scenarios
 * such as when the user makes an input during a server transaction, and, during
 * that server transaction, the server manipulates the state of the component to
 * be incompatible with the new client state.  This flag is used for components
 * such as text-entry fields where blocking user input during server 
 * transactions has significant negative consequences for the user experience.
 * For example, were it not for this capability, if a server-pushed 
 * synchronization occurred while the user were typing text into a field, some
 * of the his/her entered characters would be silently dropped.
 *
 * @param elementId the id of the element
 * @param allowInputDuringTransaction flag indicating that input should be 
 *        allowed during client/server transactions (Use of this flag is
 *        recommended against in MOST situations, see above)
 */
EchoClientEngine.verifyInput = function(elementId, allowInputDuringTransaction) {
    if (!allowInputDuringTransaction && EchoServerTransaction.active) {
        return false;
    }
    if (!EchoModalManager.isElementInModalContext(elementId)) {
        return false;
    }
    if (EchoDomPropertyStore.getPropertyValue(elementId, "EchoClientEngine.inputDisabled")) {
        return false;
    }
    return true;
};

// ________________________
// Object EchoClientMessage

/**
 * Static object/namespace representing the outgoing ClientMessage.
 */
function EchoClientMessage() { }

/**
 * The current outgoing ClientMessage XML document.
 */
EchoClientMessage.messageDocument = null;

/**
 * Creates a property element in the EchoClientMessage.
 *
 * @param componentId the id of the component
 * @param propertyName the name of the property
 * @return the created 'property' element
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

/**
 * Removes a property element (if present) from the EchoClientMessage.
 *
 * @param componentId the id of the component
 * @param propertyName the name of the property
 */
EchoClientMessage.removePropertyElement = function(componentId, propertyName) {
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
    if (propertyElement) {
        messagePartElement.removeChild(propertyElement);
    }
};

/**
 * Resets the state of the ClientMessage.
 * This method is invoked after a ClientMessage has been sent to the server
 * and is thus no longer relevant.
 */
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
EchoClientMessage.setActionValue = function(componentId, actionName, actionValue) {
    var messagePartElement = EchoClientMessage.getMessagePart("EchoAction");
    var actionElement = messagePartElement.ownerDocument.createElement("action");
    actionElement.setAttribute("component-id", componentId);
    actionElement.setAttribute("name", actionName);
    if (actionValue !== undefined) {
        // Only set value if argument is provided.
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
EchoClientMessage.setPropertyValue = function(componentId, propertyName, newValue) {
    var propertyElement = EchoClientMessage.createPropertyElement(componentId, propertyName);
    
    propertyElement.setAttribute("value", newValue);

    EchoDebugManager.updateClientMessage();
};

/**
 * Sets the "type" attribute of the <code>client-message</code> element to 
 * indicate that this is the initial client message.
 */
EchoClientMessage.setInitialize = function() {
    EchoClientMessage.messageDocument.documentElement.setAttribute("type", "initialize");
};

// ___________________________
// Object EchoClientProperties

/**
 * Static object/namespace providing information about the make/model/version,
 * capabilities, quirks, and limitations of the browser client.
 */
function EchoClientProperties() { }

/**
 * Associative array mapping client property names to values.
 */
EchoClientProperties.propertyMap = new Array();

/**
 * MessagePartProcessor implementation for EchoClientProperties.
 */
EchoClientProperties.MessageProcessor = function() { };

/**
 * MessagePartProcessor process() method implementation.
 */
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

/**
 * Proceses a <code>store</code> message to store ClientProperties information
 * received from the server.
 *
 * @param storeElement the <code>store</code> element to process
 */
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
    EchoClientEngine.configure();
};

/**
 * Returns the specified value from the ClientProperties store.
 *
 * @param name the name of the property
 * @param value the value of the property
 */
EchoClientProperties.get = function(name) {
    return EchoClientProperties.propertyMap[name];
};

// _________________________
// Object EchoCollectionsMap

/**
 * Class which provides associative mapping between keys and values.
 * Implmentation is based on an associative array.
 * Array is periodically recreated after a significant number of
 * element removals to eliminate side effects from memory management 
 * flaws with Internet Explorer.
 * Null values are not permitted as keys.  Setting a key to a null value
 * will result in the key being removed.
 */
EchoCollectionsMap = function() {
    this.removeCount = 0;
    this.garbageCollectionInterval = 250;
    this.associations = new Array();
};

/**
 * Retrieves the value referenced by the spcefied key.
 *
 * @param key the key
 * @return the value, or null if no value is referenced
 */
EchoCollectionsMap.prototype.get = function(key) {
    return this.associations[key];
};

/**
 * Stores a value referenced by the specified key.
 *
 * @param key the key
 * @param value the value
 */
EchoCollectionsMap.prototype.put = function(key, value) {
    if (value === null) {
        this.remove(key);
        return;
    }
    this.associations[key] = value;
};

/**
 * Performs 'garbage-collection' operations, recreating the array.
 * This operation is necessary due to Internet Explorer memory leak
 * issues.  It will be called automatically--there is no reason to
 * directly invoke this method.
 */
EchoCollectionsMap.prototype.garbageCollect = function() {
    this.removeCount = 0;
    var newAssociations = new Array();
    var i = 0;
    for (var key in this.associations) {
        newAssociations[key] = this.associations[key];
        ++i;
    }
    this.associations = newAssociations;
};

/**
 * Removes the value referenced by the specified key.
 *
 * @param key the key
 */
EchoCollectionsMap.prototype.remove = function(key) {
    delete this.associations[key];
    ++this.removeCount;
    if (this.removeCount >= this.garbageCollectionInterval) {
        this.garbageCollect();
    }
};

// __________________
// Object EchoCssUtil

/**
 * Static object/namespace to provide cascading style-sheet (CSS) manipulation
 * utilities.
 */
function EchoCssUtil() { }

/**
 * Updates CSS information about a specific element.
 * 
 * @param element the DOM element to update
 * @param cssText the CSS style text describing the updates to perform to
 *        the existing CSS information of the element
 */
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

/**
 * Temporarily adjusts the CSS style of an element.  The 'original' style will
 * be stored in a DomPropertyStore attribute of the element (if it has not been
 * already) before the new style is applied.  This method is useful for applying
 * selection/rollover states temporarily to elements.  Note that there may
 * only be one 'original style' set on a particular element.
 *
 * @param element the DOM element to update
 * @param cssStyleText the CSS style text to temporarily apply to the element 
 *        (via <code>applyStyle</code>)
 */
EchoCssUtil.applyTemporaryStyle = function(element, cssStyleText) {
    // Set original style if not already set.
    if (!EchoDomPropertyStore.getPropertyValue(element.id, "EchoCssUtil.originalStyle")) {
        if (element.getAttribute("style")) {
            EchoDomPropertyStore.setPropertyValue(element.id, "EchoCssUtil.originalStyle", EchoDomUtil.getCssText(element));
        } else {
            // Flag that no original CSS text existed.
            EchoDomPropertyStore.setPropertyValue(element.id, "EchoCssUtil.originalStyle", "-");
        }
    }
    
    // Apply new style.
    EchoCssUtil.applyStyle(element, cssStyleText);
};

/**
 * Restores the original style to an element whose style was updated via
 * <code>applyTemporaryStyle</code>.  Removes the 'original' style from the 
 * DomPropertyStore attribute of the element.
 *
 * @param element the DOM element to restore to its original state
 */
EchoCssUtil.restoreOriginalStyle = function(element) {
    // Obtain original style.
    var originalStyle = EchoDomPropertyStore.getPropertyValue(element.id, "EchoCssUtil.originalStyle");
    
    // Do nothing if no original style exists.
    if (!originalStyle) {
        return;
    }

    // Reset original style on element.
    EchoDomUtil.setCssText(element, originalStyle == "-" ? "" : originalStyle);
    
    // Clear original style from EchoDomPropertyStore.
    EchoDomPropertyStore.setPropertyValue(element.id, "EchoCssUtil.originalStyle", null);
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
    EchoDebugManager.debugWindow.EchoDebug.Console.write(message);
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
    EchoDebugManager.debugWindow.EchoDebug.Sync.displayClientMessage(EchoClientMessage.messageDocument);
};

/**
 * Updates the current state of the ServerMessage in the synchronization tab.
 */
EchoDebugManager.updateServerMessage = function() {
    if (!EchoDebugManager.debugWindow || EchoDebugManager.debugWindow.closed) {
        return;
    }
    EchoDebugManager.debugWindow.EchoDebug.Sync.displayServerMessage(EchoServerMessage.messageDocument);
};

// ___________________________
// Object EchoDomPropertyStore

/**
 * Static object/namespace which provides storage of non-rendered property values
 * relevant to specific DOM elements within the DOM elmenets themselves.
 * Values are stored within associative arrays that are stored in a
 * 'echoDomPropertyStore' property of a DOM element.
 */
function EchoDomPropertyStore() { }

/**
 * Static object/namespace for EchoDomPropertyStore MessageProcessor 
 * implementation.  Provides capability to set EchoDomPropertyStore
 * properties from ServerMessage directives.
 */
EchoDomPropertyStore.MessageProcessor = function() { };

/**
 * MessageProcessor process() implementation 
 * (invoked by ServerMessage processor).
 *
 * @param messagePartElement the <code>message-part</code> element to process
 */
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

/**
 * Processes a <code>store-property</code> message to set the state of an
 * <code>EchoDomPropertyStore</code> property.
 *
 * @param storePropertyElement the <code>store-property</code> element to process
 */
EchoDomPropertyStore.MessageProcessor.processStoreProperty = function(storePropertyElement) {
    var propertyName = storePropertyElement.getAttribute("name");
    var propertyValue = storePropertyElement.getAttribute("value");
    var items = storePropertyElement.getElementsByTagName("item");
    for (var i = 0; i < items.length; ++i) {
        var elementId = items[i].getAttribute("eid");
        EchoDomPropertyStore.setPropertyValue(elementId, propertyName, propertyValue);
    }
};

/**
 * Retrieves the value of a specific <code>EchoDomPropertyStore</code> property.
 *
 * @param elementId the id of the DOM element
 * @param propertyName the name of the property
 * @return the property value, or null if it is not set
 */
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

/**
 * Sets the value of a specific <code>EchoDomPropertyStore</code> property.
 *
 * @param elementId the id of the DOM element
 * @param propertyName the name of the property
 * @param propertyValue the new value of the property
 */
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
 * Static object/namespace for performing server-directed updates to the
 * client DOM.
 */
function EchoDomUpdate() { }

/**
 * Static object/namespace for EchoDomUpdate MessageProcessor 
 * implementation.
 */
EchoDomUpdate.MessageProcessor = function() { };

/**
 * MessageProcessor process() implementation 
 * (invoked by ServerMessage processor).
 *
 * @param messagePartElement the <code>message-part</code> element to process
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

/**
 * Processes a <code>dom-add</code> directive to append or insert new nodes into
 * the DOM.
 *
 * @param domAddElement the <code>dom-add</code> element to process
 */
EchoDomUpdate.MessageProcessor.processAdd = function(domAddElement) {
    var contentElements = domAddElement.getElementsByTagName("content");
    for (var i = 0; i < contentElements.length; ++i) {
        var parentId = contentElements[i].getAttribute("parent-id");
	    var siblingId = contentElements[i].getAttribute("sibling-id");
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

	    for (var j = 0; j < contentElements[i].childNodes.length; ++j) {
	        var importedNode = EchoDomUtil.importNode(parentElement.ownerDocument, contentElements[i].childNodes[j], true);
	        
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
    }
};

/**
 * Processes an <code>attribute-update</code> directive to update an attribute
 * of a DOM element.
 *
 * @param attributeUpdateElement the <code>attribute-update</code> element to 
 *        process
 */
EchoDomUpdate.MessageProcessor.processAttributeUpdate = function(attributeUpdateElement) {
    var targetId = attributeUpdateElement.getAttribute("target-id");
    var targetElement = document.getElementById(targetId);
    if (!targetElement) {
        throw new EchoDomUpdate.TargetNotFoundException("AttributeUpdate", "target", targetId);
    }
    targetElement[attributeUpdateElement.getAttribute("name")] = attributeUpdateElement.getAttribute("value");
};

/**
 * Processes a <code>dom-remove</code> directive to delete nodes from the DOM.
 *
 * @param domRemoveElement the <code>dom-remove</code> element to process
 */
EchoDomUpdate.MessageProcessor.processRemove = function(domRemoveElement) {
    var targetId = domRemoveElement.getAttribute("target-id");
    var targetElement = document.getElementById(targetId);
    if (!targetElement) {
        return;
    }
    targetElement.parentNode.removeChild(targetElement);
};

/**
 * Processes a <code>dom-remove-children</code> directive to delete all child 
 * nodes from a specific DOM element.
 *
 * @param domRemoveChildrenElement the <code>dom-remove-children</code> element 
 *        to process
 */
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

/**
 * Processes a <code>style-update</code> directive to update an the CSS style
 * of a DOM element.
 *
 * @param styleUpdateElement the <code>style-update</code> element to 
 *        process
 */
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
 * An exception describing a failure of an EchoDomUpdate operation due to a 
 * target node not existing.
 * 
 * @param the type of update, e.g., DomRemove or StyleUpdate
 * @param targetType the type of target that caused the failure
 * @param targetId the id of the target
 */
EchoDomUpdate.TargetNotFoundException = function(updateType, targetType, targetId) {
    this.targetId = targetId;
    this.targetType = targetType;
    this.updateType = updateType;
};

/**
 * Renders a String representation of the exception.
 *
 * @return the String representation
 */
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
 * An associative array which maps between HTML attributes and HTMLElement 
 * property  names.  These values generally match, but multiword attribute
 * names tend to have "camelCase" property names, e.g., the 
 * "cellspacing" attribute corresponds to the "cellSpacing" property.
 * This map is required by the Internet Explorer-specific importNode() 
 * implementation
 */
EchoDomUtil.attributeToPropertyMap = null;

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
 *        events in the final phase of propagation (only supported by 
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
 * Initializes the attribute-to-property map required for importing HTML
 * elements into a DOM in Internet Explorer browsers.
 */
EchoDomUtil.initAttributeToPropertyMap = function() {
    var m = new Array();
    m["accesskey"] = "accessKey";
    m["cellpadding"] = "cellPadding";
    m["cellspacing"] = "cellSpacing";
    m["class"] = "className";
    m["codebase"] = "codeBase";
    m["codetype"] = "codeType";
    m["colspan"] = "colSpan";
    m["datetime"] = "dateTime";
    m["frameborder"] = "frameBorder";
    m["longdesc"] = "longDesc";
    m["marginheight"] = "marginHeight";
    m["marginwidth"] = "marginWidth";
    m["maxlength"] = "maxLength";
    m["noresize"] = "noResize";
    m["noshade"] = "noShade";
    m["nowrap"] = "noWrap";
    m["readonly"] = "readOnly";
    m["rowspan"] = "rowSpan";
    m["tabindex"] = "tabIndex";
    m["usemap"] = "useMap";
    m["valign"] = "vAlign";
    m["valueType"] = "valueType";
    EchoDomUtil.attributeToPropertyMap = m;    
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
 * Recursively fixes broken element attributes in a Safari DOM.
 * Safari2 does not properly unescape attributes when retrieving
 * them from an XMLHttpRequest's response DOM.  For example, the attribute
 * abc="x&y" would return the value "X&#38;y" in Safari2.  This simply scans
 * the DOM for any attributes containing "&#38;" and replaces instances of it
 * with a simple ampersand ("&").  This method should be invoked once per DOM
 * if it has been determined that a version of Safari that suffers this bug is
 * in use.
 *
 * @param node the starting node whose attributes are to be fixed (child 
 *        elements will be fixed recursively)
 */
EchoDomUtil.fixSafariAttrs = function(node) {
    if (node.nodeType == 1) {
        for (i = 0; i < node.attributes.length; ++i) {
            var attribute = node.attributes[i];
            node.setAttribute(attribute.name, attribute.value.replace("\x26\x2338\x3B", "&"));
        }
    }
    
    for (var childNode = node.firstChild; childNode; childNode = childNode.nextSibling) {
        EchoDomUtil.fixSafariAttrs(childNode);
    }
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
 * Cross-platform method to retrieve the CSS text of a DOM element.
 *
 * @param element the element
 * @return cssText the CSS text
 */
EchoDomUtil.getCssText = function(element) {
    if (EchoClientProperties.get("quirkOperaNoCssText")) {
        return element.getAttribute("style");
    } else {
        return element.style.cssText;
    }
}

/**
 * Returns the target of an event, using the client's supported event model.
 * On clients which support the W3C DOM Level 2 event specification,
 * the <code>target</code> property of the event is returned.
 * On clients which support only the Internet Explorer event model,
 * the <code>srcElement</code> property of the event is returned.
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
            } else {
                if (EchoDomUtil.attributeToPropertyMap === null) {
                    EchoDomUtil.initAttributeToPropertyMap();
                }
                var propertyName = EchoDomUtil.attributeToPropertyMap[attribute.name];
                if (propertyName) {
                    targetNode[propertyName] = attribute.value;
                } else {
                    targetNode[attribute.name] = attribute.value;
                }
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
            if (targetChildNode) {
	            targetNode.appendChild(targetChildNode);
            }
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
 *         <code>descendantNode</code>
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
 * the preventDefault() method of the event is invoked.
 * On clients which support only the Internet Explorer event model,
 * the 'returnValue' property of the event is set to false.
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
 *        events in the final phase of propagation (only supported by 
 *        DOM Level 2 event model)
 */
EchoDomUtil.removeEventListener = function(eventSource, eventType, eventListener, useCapture) {
    if (eventSource.removeEventListener) {
        eventSource.removeEventListener(eventType, eventListener, useCapture);
    } else if (eventSource.detachEvent) {
        eventSource.detachEvent("on" + eventType, eventListener);
    }
};

/**
 * Cross-platform method to set the CSS text of a DOM element.
 *
 * @param element the element
 * @param cssText the new CSS text
 */
EchoDomUtil.setCssText = function(element, cssText) {
    if (EchoClientProperties.get("quirkOperaNoCssText")) {
	    element.setAttribute("style", cssText);
    } else {
	    element.style.cssText = cssText;
    }
};

/**
 * Stops an event from propagating ("bubbling") to parent nodes in the DOM, 
 * using the client's supported event model.
 * On clients which support the W3C DOM Level 2 event specification,
 * the stopPropagation() method of the event is invoked.
 * On clients which support only the Internet Explorer event model,
 * the 'cancelBubble' property of the event is set to true.
 *
 * @param e the event
 */
EchoDomUtil.stopPropagation = function(e) {
    if (e.stopPropagation) {
        e.stopPropagation();
    } else {
        e.cancelBubble = true;
    }
};

// _________________________
// Object EchoEventProcessor

/**
 * Static object/namespace providing higher-level event management.
 * Echo2 client-side components may optionally register, unregister,
 * and receive events using the EchoEventProcessor.
 *
 * Event handlers registered with the EchoEventProcessor will result
 * in additional information being sent when events are fired.
 * This information includes the DOM element on which the
 * event was actually registered (the "registeredTarget" property) as
 * well as the target that fired the event (the "target" property).
 *
 * Additionally, use of the EchoEventProcessor minimizes "DHTML memory
 * leaks" and provides a mechanism for inspecting registered event handlers
 * using the Debug Window.
 */
function EchoEventProcessor() { }

/**
 * Static object/namespace for EchoEventProcessor MessageProcessor 
 * implementation.
 */
EchoEventProcessor.MessageProcessor = function() { };

/**
 * MessageProcessor process() implementation 
 * (invoked by ServerMessage processor).
 *
 * @param messagePartElement the <code>message-part</code> element to process
 */
EchoEventProcessor.MessageProcessor.process = function(messagePartElement) {
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

/**
 * An associative array mapping event types (e.g. "mouseover") to
 * associate arrays that map DOM element ids to the String names
 * of event handlers.
 */
EchoEventProcessor.eventTypeToHandlersMap = new EchoCollectionsMap();

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
    
    var elementIdToHandlerMap = EchoEventProcessor.eventTypeToHandlersMap.get(eventType);
    if (!elementIdToHandlerMap) {
        elementIdToHandlerMap = new EchoCollectionsMap();
        EchoEventProcessor.eventTypeToHandlersMap.put(eventType, elementIdToHandlerMap);
    }
    elementIdToHandlerMap.put(elementId, handler);
};

/**
 * Retrieves the event handler of a specific type for a specific element.
 *
 * @param eventType the type of the event (should be specified using 
 *        DOM level 2 event names, e.g., "mouseover" or "click", without
 *        the "on" prefix)
 * @param elementId the elementId the id of the DOM element
 */
EchoEventProcessor.getHandler = function(eventType, elementId) {
    var elementIdToHandlerMap = EchoEventProcessor.eventTypeToHandlersMap.get(eventType);
    if (!elementIdToHandlerMap) {
        return null;
    }
    return elementIdToHandlerMap.get(elementId);
};

/**
 * Returns the element ids of all event handlers of the specified type.
 */
EchoEventProcessor.getHandlerElementIds = function(eventType) {
    var elementIds = new Array();
    var elementIdToHandlerMap = EchoEventProcessor.eventTypeToHandlersMap.get(eventType);
    if (elementIdToHandlerMap) {
        for (var elementId in elementIdToHandlerMap.associations) {
            elementIds.push(elementId);
        }
    }
    return elementIds;
};

/**
 * Returns the types of all registered event handlers.
 *
 * @return an array of Strings containing the type names of all registered
 *         event handlers
 */
EchoEventProcessor.getHandlerEventTypes = function() {
    var handlerTypes = new Array();
    for (var eventType in EchoEventProcessor.eventTypeToHandlersMap.associations) {
        handlerTypes.push(eventType);
    }
    return handlerTypes;
};

/**
 * Master event handler for all DOM events.
 * This method is registered as the event handler for all
 * EchoEventProcessor event registrations.  Events are processed,
 * modified, and then dispatched to the appropriate
 * Echo event handlers by this method.
 *
 * @param e the event fired by the DOM
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
        return;
    }

    var handler;
    try {
        handler = eval(handlerName);
    } catch (ex) {
        throw "Invalid handler: " + handlerName + " (" + ex + ")";
    }
    
    handler(e);
    EchoDomUtil.stopPropagation(e);
};

/**
 * Unregisters an event handler.
 *
 * @param elementId the target elementId of the event
 * @param eventType the type of the event (should be specified using 
 *        DOM level 2 event names, e.g., "mouseover" or "click", without
 *        the "on" prefix)
 */
EchoEventProcessor.removeHandler = function(elementId, eventType) {
    var element = document.getElementById(elementId);
    if (element) {
        EchoDomUtil.removeEventListener(element, eventType, EchoEventProcessor.processEvent, false);
    }

    var elementIdToHandlerMap = EchoEventProcessor.eventTypeToHandlersMap.get(eventType);
    if (!elementIdToHandlerMap) {
        return;
    }
    elementIdToHandlerMap.remove(elementId);
};

// _______________________
// Object EchoFocusManager

/**
 * Static object/namespace to manage component focus.
 */
function EchoFocusManager() { }

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
    this.disposed = false;
}

/**
 * Empty method assigned to onreadystatechange handler to avoid IE memory leaks.
 */
EchoHttpConnection.cancelReadyStateChange = function() { };

/**
 * Executes the HTTP connection.
 * The responseHandler property of the connection must be set
 * before the connection is executed.
 * This method will return asynchronously; the <code>responseHandler</code>
 * callback property will be invoked when a valid response is received.
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
    this.xmlHttpRequest.onreadystatechange = function() { 
        if (!instance) {
            return;
        }
        try {
            instance.processReadyStateChange();
        } finally {
            if (instance.disposed) {
                // Release instance reference to allow garbage collection.
                instance = null;
            }
        }
    };
    
    this.xmlHttpRequest.open(this.method, this.url, true);
    if (this.contentType && (usingActiveXObject || this.xmlHttpRequest.setRequestHeader)) {
        this.xmlHttpRequest.setRequestHeader("Content-Type", this.contentType);
    }
    this.xmlHttpRequest.send(this.messageObject ? this.messageObject : null);
};

EchoHttpConnection.prototype.dispose = function() {
this.onreadystatechange
    this.onreadystatechange = this.cancelReadyStateChange;
    this.messageObject = null;
    this.responseHandler = null;
    this.invalidResponseHandler = null;
    this.xmlHttpRequest = null;
    this.disposed = true;
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
    if (this.disposed) {
        return;
    }
    if (this.xmlHttpRequest.readyState == 4) {
        if (this.xmlHttpRequest.status == 200) {
            if (!this.responseHandler) {
                this.dispose();
                throw "EchoHttpConnection response handler not set.";
            }
            this.responseHandler(this);
            this.dispose();
        } else {
            if (this.invalidResponseHandler) {
                this.invalidResponseHandler(this);
                this.dispose();
            } else {
                var statusValue = this.xmlHttpRequest.status;
                this.dispose();
                throw "Invalid HTTP Response code (" + statusValue + ") and no handler set.";
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
 * retrieved from the server.
 */
EchoScriptLibraryManager.STATE_LOADED = 2;

/**
 * Library load-state constant indicating a library has been successfully
 * installed.
 */
EchoScriptLibraryManager.STATE_INSTALLED = 3;

/**
 * Associative array mapping library service ids to library source code.
 */
EchoScriptLibraryManager.librarySourceMap = new Array();

/**
 * Associative array mapping library service ids to load-states.
 */
EchoScriptLibraryManager.libraryLoadStateMap = new Array();

/**
 * Queries the state of the specified libary.
 *
 * @param serviceId the server service identifier of the library
 * @return the load-state of the library, either
 *         <code>STATE_REQUESTED</code> or <code>STATE_LOADED</code>
 */
EchoScriptLibraryManager.getState = function(serviceId) {
    return EchoScriptLibraryManager.libraryLoadStateMap[serviceId];
};

/**
 * Installs a previously loaded JavaScript library.
 *
 * @param serviceId the server service identifier of the library
 */
EchoScriptLibraryManager.installLibrary = function(serviceId) {
    if (EchoScriptLibraryManager.getState(serviceId) == EchoScriptLibraryManager.STATE_INSTALLED) {
        // Library already installed.
        return;
    }

    try {
        // Obtain source.
	    var source = EchoScriptLibraryManager.librarySourceMap[serviceId];

	    // Execute library code.
        eval(source);
        
        // Clear source.
        EchoScriptLibraryManager.librarySourceMap[serviceId] = null;

        // Mark state as installed.
        EchoScriptLibraryManager.libraryLoadStateMap[serviceId] = EchoScriptLibraryManager.STATE_INSTALLED;
    } catch (ex) {
        EchoClientEngine.processClientError("Cannot load script module \"" + serviceId + "\"", ex);
        throw ex;
    }
};

/**
 * Loads a JavaScript library and stores it for execution.
 *
 * @param serviceId the server service identifier of the library
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
    EchoScriptLibraryManager.librarySourceMap[conn.serviceId] = conn.getResponseText();
    EchoScriptLibraryManager.libraryLoadStateMap[conn.serviceId] = EchoScriptLibraryManager.STATE_LOADED;
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
 *   invoke EchoClientEngine.verifyInput() before accepting input.</li>
 *  <li>Displays a "wait" (hourglass) mouse cursor.</li>
 *  <li>Displays a "please wait" message after a time interval has elapsed
 *   during a longer-than-normal server transaction.</li>
 * </ul>
 */
function EchoServerDelayMessage() { }

/**
 * MessagePartProcessor implementation for EchoServerDelayMessage.
 */
EchoServerDelayMessage.MessageProcessor = function() { };

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
EchoServerDelayMessage.MessageProcessor.processSetDelayMessage = function(setDirectiveElement) {
    var i;
    var serverDelayMessage = document.getElementById(EchoServerDelayMessage.MESSAGE_ELEMENT_ID);
    if (serverDelayMessage) {
        // Remove existing message.
        serverDelayMessage.parentNode.removeChild(serverDelayMessage);
    }
    
    var contentElement = setDirectiveElement.getElementsByTagName("content")[0];
    
    if (!contentElement) {
        // Setting message to null.
        return;
    }

    // Add server message content.
    var bodyElement = document.getElementsByTagName("body")[0];
    for (i = 0; i < contentElement.childNodes.length; ++i) {
        bodyElement.appendChild(EchoDomUtil.importNode(document, contentElement.childNodes[i], true));
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
 * Count of successfully processed server messages.
 */
EchoServerMessage.transactionCount = 0;

/**
 * DOM of the ServerMessage.
 */
EchoServerMessage.messageDocument = null;

/**
 * Identifier returned from setInterval() for asynchronously monitoring 
 * loading of JavaScript libraries.
 */
EchoServerMessage.backgroundIntervalId = null;

EchoServerMessage.enableFixSafariAttrs = false;

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
            var libraryState = EchoScriptLibraryManager.getState(serviceId);
            if (libraryState != EchoScriptLibraryManager.STATE_LOADED 
                    && libraryState != EchoScriptLibraryManager.STATE_INSTALLED) {
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
 * Installs the dynamically retrieved JavaScript libraries.
 */
EchoServerMessage.installLibraries = function() {
    var librariesElement = EchoServerMessage.messageDocument.getElementsByTagName("libraries").item(0);
    var headElement = document.getElementsByTagName("head").item(0);
    if (!librariesElement) {
        return;
    }
    var libraryElements = librariesElement.getElementsByTagName("library");
    for (var i = 0; i < libraryElements.length; ++i) {
        var serviceId = libraryElements.item(i).getAttribute("service-id");
        EchoScriptLibraryManager.installLibrary(serviceId);
    }
};

/**
 * Processes the ServerMessage.
 * The processing is performed asynchronously--this method will return before
 * the processing has been completed.
 */
EchoServerMessage.process = function() {
    EchoServerMessage.prepare();
    EchoServerMessage.processPhase1();
};

/**
 * Prepares the ServerMessage for prcocessing.
 *
 * This step presently only fixes Safari2's defective DOM attribute 
 * values (if required).
 */
EchoServerMessage.prepare = function() {
    // Test to determine if the document element contains a "XML Attribute Test" attribute.
    // The attribute should have a value of "x&y" if the browser is working properly.
    // The attribute will return the value of "x&#38;y" in the case of Safari2's broken DOM.
    if (EchoServerMessage.messageDocument.documentElement.getAttribute("xml-attr-test") == "x&#38;y") {
        // If attributes are broken, set flag.
        EchoServerMessage.enableFixSafariAttrs = true;
    }
    
    // If broken attribute flag has been set, run workaround code.
    if (EchoServerMessage.enableFixSafariAttrs) {
        EchoDomUtil.fixSafariAttrs(EchoServerMessage.messageDocument.documentElement);
    }
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
 * Processes all of the 'message-part' directives contained in the ServerMessage.
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
    try {
	    EchoServerMessage.status = EchoServerMessage.STATUS_PROCESSING;
	    EchoServerMessage.loadLibraries();
	    if (EchoServerMessage.isLibraryLoadComplete()) {
	        EchoServerMessage.processPhase2();
	    } else {
	        EchoServerMessage.backgroundIntervalId = window.setInterval("EchoServerMessage.waitForLibraries();", 20);
	    }
    } catch (ex) {
        EchoClientEngine.processClientError("Cannot process ServerMessage (Phase 1)", ex);
        throw ex;
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
    try {
        EchoServerMessage.installLibraries();
		EchoServerMessage.processMessageParts();
		EchoServerMessage.processApplicationProperties();
		if (EchoServerMessage.processingCompleteListener) {
		    EchoServerMessage.processingCompleteListener();
		}
		EchoServerMessage.processAsyncConfig();
    } catch (ex) {
        EchoClientEngine.processClientError("Cannot process ServerMessage (Phase 2)", ex);
        throw ex;
    }
    
    EchoVirtualPosition.redraw();
    ++EchoServerMessage.transactionCount;
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
    if (conn.xmlHttpRequest.status == 500) {
        EchoClientEngine.processServerError();
    } else if (conn.xmlHttpRequest.status == 400) {
        EchoClientEngine.processSessionExpiration();
    } else {
        alert("Invalid/unknown response from server: " + conn.getResponseText());
    }
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

// __________________________
// Object EchoVirtualPosition

/**
 * Static object/namespace which provides cross-platform CSS positioning 
 * capabilities.  Internet Explorer 6 is ordinarily handicapped by its lack
 * of support for setting 'left' and 'right' or 'top' and 'bottom' positions
 * simultaneously on a single document element.
 *
 * To use the Virtual Position capability, an element should be rendered with
 * one horizontal and/or vertical side positioned traditionally, e.g., by 
 * setting the "top" or "left" properties of its CSSStyleDeclaration.  The 
 * opposite side should be set using the EchoVirtualPosition object, e.g., by
 * invoking EchoVirtualPosition.setRight() and/or 
 * EchoVirtualPosition.setBottom() on the element.  Note that when the element 
 * is disposed the EchoVirtualPosition.clear() method MUST be invoked with the
 * removed element id or RESOURCES WILL BE LEAKED.
 *
 * When the HTML rendering of a component that may contain other components 
 * CHANGES IN SIZE, the EchoVirtualPosition.redraw() method MUST be invoked, or
 * components that use virtual positioning will appear correctly on screen.
 * This should be done even if the container component itself does not use
 * the virtual position capability, due to the fact that a child component might
 * be using it.
 * 
 * The EchoVirtualPosition.redraw() method is invoked automatically whenever
 * a Client/Server synchronization is completed.
 */
EchoVirtualPosition = function() { };

// Position constants - internal use only.
EchoVirtualPosition.BOTTOM = 1;
EchoVirtualPosition.LEFT = 2;
EchoVirtualPosition.RIGHT = 3;
EchoVirtualPosition.TOP = 4;

EchoVirtualPosition.positionMap = new EchoCollectionsMap();
EchoVirtualPosition.elementIdList = new Array();

/** Flag indicating whether virtual positioning is required/enabled. */
EchoVirtualPosition.enabled = true;

/** 
 * Calculates the appropriate height setting for an element to simulate the 
 * specified top/bottom settings.  The calculation makes allowances for padding
 * and margins.
 *
 * @param element the element whose height setting is to be calculated
 * @param virtualPositionSide a position constant, either 
 *        EchoVirtualPosition.BOTTOM or EchoVirtualPosition.TOP, specifying
 *        which side uses virtual positioning.
 *
 * This method is for internal use only by EchoVirtualPosition.
 */
EchoVirtualPosition.calculateHeight = function(element, virtualPositionSide) {
    var parentHeight = element.parentNode.offsetHeight;
    var elementData = EchoVirtualPosition.positionMap.get(element.id);
    var virtualSidePixels = virtualPositionSide == EchoVirtualPosition.TOP ? elementData.top : elementData.bottom;
    var otherSidePixels = EchoVirtualPosition.parsePx(
            virtualPositionSide == EchoVirtualPosition.TOP ? element.style.bottom : element.style.top);
    var paddingPixels = EchoVirtualPosition.parsePx(element.style.paddingTop) 
            + EchoVirtualPosition.parsePx(element.style.paddingBottom);
    var marginPixels = EchoVirtualPosition.parsePx(element.style.marginTop) 
            + EchoVirtualPosition.parsePx(element.style.marginBottom);
    var calculatedHeight = parentHeight - virtualSidePixels - otherSidePixels - paddingPixels - marginPixels;
    if (calculatedHeight <= 0) {
        return 0;
    } else {
        return calculatedHeight + "px";
    }
};

/** 
 * Calculates the appropriate width setting for an element to simulate the 
 * specified left/right settings.  The calculation makes allowances for padding
 * and margins.
 *
 * @param element the element whose height setting is to be calculated
 * @param virtualPositionSide a position constant, either 
 *        EchoVirtualPosition.RIGHT or EchoVirtualPosition.LEFT, specifying
 *        which side uses virtual positioning.
 *
 * This method is for internal use only by EchoVirtualPosition.
 */
EchoVirtualPosition.calculateWidth = function(element, virtualPositionSide) {
    var parentWidth = element.parentNode.offsetWidth;
    var elementData = EchoVirtualPosition.positionMap.get(element.id);
    var virtualSidePixels = virtualPositionSide == EchoVirtualPosition.LEFT ? elementData.left : elementData.right;
    var otherSidePixels = EchoVirtualPosition.parsePx(
            virtualPositionSide == EchoVirtualPosition.LEFT ? element.style.right : element.style.left);
    var paddingPixels = EchoVirtualPosition.parsePx(element.style.paddingLeft) 
            + EchoVirtualPosition.parsePx(element.style.paddingRight);
    var marginPixels = EchoVirtualPosition.parsePx(element.style.marginLeft) 
            + EchoVirtualPosition.parsePx(element.style.marginRight);
    var calculatedWidth = parentWidth - virtualSidePixels - otherSidePixels - paddingPixels - marginPixels;
    if (calculatedWidth <= 0) {
        return 0;
    } else {
        return calculatedWidth + "px";
    }
};

/**
 * Creates or retrieves the ElementData object corresponding to a specific
 * element id.
 *
 * @param the elementId the id of the element
 * @return the retrieved/created ElementData
 */
EchoVirtualPosition.createOrRetrieveElementData = function(elementId) {
    var elementData = EchoVirtualPosition.positionMap.get(elementId);
    if (!elementData) {
        elementData = new EchoVirtualPosition.ElementData();
        EchoVirtualPosition.positionMap.put(elementId, elementData);
        EchoVirtualPosition.elementIdList.push(elementId);
    }
    return elementData;
};

/**
 * Enables and initializes the virtual positioning system.
 */
EchoVirtualPosition.init = function() {
    EchoVirtualPosition.enabled = true;
    EchoDomUtil.addEventListener(window, "resize", EchoVirtualPosition.resizeListener, false);
};

EchoVirtualPosition.parsePx = function(value) {
    value = parseInt(value);
    return isNaN(value) ? 0 : value;
};

EchoVirtualPosition.redraw = function(elementId) {
    if (!EchoVirtualPosition.enabled) {
        return;
    }
    for (var i = 0; i < EchoVirtualPosition.elementIdList.length; ++i) {
        var elementId = EchoVirtualPosition.elementIdList[i];
        EchoVirtualPosition.redrawElement(elementId);
    }
}

EchoVirtualPosition.redrawElement = function(elementId) {
    if (!EchoVirtualPosition.enabled) {
        return;
    }
    var element = document.getElementById(elementId);
    if (!element) {
        return;
    }
    var elementData = EchoVirtualPosition.positionMap.get(elementId);
    if (elementData.bottom != null) {
        element.style.height = EchoVirtualPosition.calculateHeight(element, EchoVirtualPosition.BOTTOM);
    } else if (elementData.top != null) {
        element.style.height = EchoVirtualPosition.calculateHeight(element, EchoVirtualPosition.TOP);
    }
    if (elementData.right != null) {
        element.style.width = EchoVirtualPosition.calculateWidth(element, EchoVirtualPosition.RIGHT);
    } else if (elementData.left != null) {
        element.style.width = EchoVirtualPosition.calculateWidth(element, EchoVirtualPosition.LEFT);
    }
};

EchoVirtualPosition.resizeListener = function(e) {
    e = e ? e : window.event;
    EchoVirtualPosition.redraw();
};

EchoVirtualPosition.clear = function(elementId) {
    if (!EchoVirtualPosition.enabled) {
        return;
    }

    var i, index = null;

    // Remove element from mapping.
    EchoVirtualPosition.positionMap.remove(elementId);

    // Find index of elementId in elementIdList
    for (i = 0; i < EchoVirtualPosition.elementIdList.length; ++i) {
        if (EchoVirtualPosition.elementIdList[i] == elementId) {
            index = i;
            break;
        }
    }
    if (index == null) {
        return;
    }
    
    // Remove index from elementIdList
    for (i = index; i < EchoVirtualPosition.elementIdList.length - 1; ++i) {
        EchoVirtualPosition.elementIdList[i] = EchoVirtualPosition.elementIdList[i + 1];
    }
    EchoVirtualPosition.elementIdList.length = EchoVirtualPosition.elementIdList.length - 1;
};

EchoVirtualPosition.setBottom = function(element, bottomPixels) {
    if (EchoVirtualPosition.enabled) {
        // IE6 browser.
        var elementData = EchoVirtualPosition.createOrRetrieveElementData(element.id);
        elementData.bottom = bottomPixels;
    } else {
        // Normal browser.
        element.style.bottom = bottomPixels + "px";
    }
};

EchoVirtualPosition.setLeft = function(element, leftPixels) {
    if (EchoVirtualPosition.enabled) {
        // IE6 browser.
        var elementData = EchoVirtualPosition.createOrRetrieveElementData(element.id);
        elementData.left = leftPixels;
    } else {
        // Normal browser.
        element.style.left = leftPixels + "px";
    }
};

EchoVirtualPosition.setRight = function(element, rightPixels) {
    if (EchoVirtualPosition.enabled) {
        // IE6 browser.
        var elementData = EchoVirtualPosition.createOrRetrieveElementData(element.id);
        elementData.right = rightPixels;
    } else {
        // Normal browser.
        element.style.right = rightPixels + "px";
    }
};

EchoVirtualPosition.setTop = function(element, topPixels) {
    if (EchoVirtualPosition.enabled) {
        // IE6 browser.
        var elementData = EchoVirtualPosition.createOrRetrieveElementData(element.id);
        elementData.top = topPixels;
    } else {
        // Normal browser.
        element.style.top = topPixels + "px";
    }
};
    
EchoVirtualPosition.ElementData = function() {
    this.top = null;
    this.bottom = null;
    this.left = null;
    this.right = null;
};

EchoVirtualPosition.ElementData.prototype.toString = function() {
    return this.top + " " + this.right + " " + this.bottom + " "  + this.left + " ";
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
    if (element && element.focus) {
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
