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

/**
 * Namespace for core functionality.
 * @namespace
 */
Core = {

    /**
     * Creates a duplicate copy of a function by wrapping the original in a closure.
     *
     * @param f the function
     * @return an effectively identical copy
     */
    _copyFunction: function(f) {
        return function() {
            f.apply(this, arguments);
        };
    },
    
    /**
     * Creates an empty function.
     */
    _createFunction: function() {
        return function() { };
    },
    
    /**
     * Creates a new class, optionally extending an existing class.
     * This method may be called with one or two parameters as follows:
     * <p>
     * <code>Core.extend(definition)</code>
     * <code>Core.extend(baseClass, definition)</code>
     * <p>
     * Each property of the definition object will be added to the prototype of the returned defined class.
     * Properties that begin with a dollar-sign (<code>$</code>) will be processed specially:
     * <p>
     * <ul>
     * <li>The <code>$construct</code> property, which must be a function, will be used as the constructor.
     * The <code>$load</code> property, which must be a function, f provided, will be used as a static initializer,
     * executed once when the class is *defined*.  The this pointer will be set to the class when
     * this method is executed.</li>
     * <li>The <code>$static</code> property, an object, if provided, will have its properties installed as class variables.</li>
     * <li>The <code>$abstract</code> property, an object or <code>true</code>, if provided, will define methods that
     * must be implemented by derivative classes.  If the value is simply <code>true</code>, the object will be marked as
     * abstract (such that it does not necessarily need to provide implementations of abstract methods defined in its 
     * base class.)</li>
     * <li>The <code>$virtual</code> property, an object, if provided, defines methods that will be placed into the prototype
     * that may be overridden by subclasses.  Attempting to override a property/method of the superclass that
     * is not defined in the virtual block will result in an exception.  Having the default behavior NOT allow
     * for overriding ensures that namespacing between super- and sub-types if all internal variables are instance
     * during <code>Core.extend()</code>.</li>
     * </ul>
     * <p>
     * Use of this method enables a class to be derived WITHOUT executing the constructor of the base class
     * in order to create a prototype for the derived class.  This method uses a "shared prototype" architecture,
     * where two objects are created, a "prototype class" and a "constructor class".  These two objects share
     * the same prototype, but the "prototype class" has an empty constructor.  When a class created with
     * this method is derived, the "prototype class" is used to create a prototype for the derivative.
     * <p>
     * This method will return the constructor class, which contains an internal reference to the 
     * prototype class that will be used if the returned class is later derived by this method.
     * 
     * @param {Function} baseClass the base class
     * @param {Object} definition an associative array containing methods and properties of the class
     * @return the constructor class
     */
    extend: function() {
        // Configure baseClass/definition arguments.
        var baseClass = arguments.length == 1 ? null : arguments[0];
        var definition = arguments.length == 1 ? arguments[0] : arguments[1];
        
        var x, name;
        
        // Perform argument error checking.
        if (arguments.length == 2) {
            if (typeof(baseClass) != "function") {
                throw new Error("Base class is not a function, cannot derive.");
            }
        }
        if (!definition) {
            throw new Error("Object definition not provided.");
        }
        
        // Create the constructor class.
        var constructorClass;
        if (definition.$construct) {
            // Definition provides constructor, provided constructor function will be used as object.
            constructorClass = definition.$construct;
            
            // Remove property such that it will not later be added to the object prototype.
            delete definition.$construct;
        } else {
            // Definition does not provide constructor.
            if (baseClass) {
                // Base class available: copy constructor function from base class.
                // Note: should function copying not be supported by a future client,
                // it is possible to simply create a new constructor which invokes the base
                // class constructor (using closures and Function.apply()) to achieve the
                // same effect (with a slight performance penalty).
                constructorClass = Core._copyFunction(baseClass);
            } else {
                // No base class: constructor is an empty function.
                constructorClass = Core._createFunction();
            }
        }
        
        // Create virtual property storage.
        constructorClass.$virtual = {};
        
        // Store reference to base class in constructor class.
        constructorClass.$super = baseClass;

        if (baseClass) {
            // Create class with empty constructor that shares prototype of base class.
            var prototypeClass = Core._createFunction();
            prototypeClass.prototype = baseClass.prototype;
            
            // Create new instance of constructor-less prototype for use as prototype of new class.
            constructorClass.prototype = new prototypeClass();
        }
        
        // Assign constructor correctly.
        constructorClass.prototype.constructor = constructorClass;

        // Add abstract properties.
        if (definition.$abstract) {
            constructorClass.$abstract = {};
            if (baseClass && baseClass.$abstract) {
                // Copy abstract properties from base class.
                for (x in baseClass.$abstract) {
                    constructorClass.$abstract[x] = baseClass.$abstract[x];
                }
            }

            if (definition.$abstract instanceof Object) {
                // Add abstract properties from definition.
                for (x in definition.$abstract) {
                    constructorClass.$abstract[x] = true;
                    constructorClass.$virtual[x] = true;
                }
            }
            
            // Remove property such that it will not later be added to the object prototype.
            delete definition.$abstract;
        }
        
        // Copy virtual property flags from base class to shared prototype.
        if (baseClass) {
            for (name in baseClass.$virtual) {
                constructorClass.$virtual[name] = baseClass.$virtual[name];
            }
        }
        
        // Add virtual instance properties from definition to shared prototype.
        if (definition.$virtual) {
            Core._inherit(constructorClass.prototype, definition.$virtual, constructorClass.$virtual);
            for (name in definition.$virtual) {
                constructorClass.$virtual[name] = true;
            }

            // Remove property such that it will not later be added to the object prototype.
            delete definition.$virtual;
        }
        
        // Add toString and valueOf manually, as they will not be iterated
        // by for-in iteration in Internet Explorer.
        if (definition.hasOwnProperty("toString")) {
            constructorClass.prototype.toString = definition.toString;
        }
        if (definition.hasOwnProperty("valueOf")) {
            constructorClass.prototype.valueOf = definition.valueOf;
        }

        // Remove properties such that they will not later be added to the object prototype.
        delete definition.toString;
        delete definition.valueOf;

        // Add Mixins.
        if (definition.$include) {
            // Reverse order of mixins, such that later-defined mixins will override earlier ones.
            // (Mixins will only be added if they will NOT override an existing method.)
            var mixins = definition.$include.reverse();
            Core._processMixins(constructorClass, mixins);
            
            // Remove property such that it will not later be added to the object prototype.
            delete definition.$include;
        }

        // Store $load static initializer and remove from definition so it is not inherited in static processing.
        var loadMethod = null;
        if (definition.$load) {
            loadMethod = definition.$load;

            // Remove property such that it will not later be added to the object prototype.
            delete definition.$load;
        }
        
        // Process static properties and methods defined in the '$static' object.
        if (definition.$static) {
            Core._inherit(constructorClass, definition.$static);

            // Remove property such that it will not later be added to the object prototype.
            delete definition.$static;
        }

        // Process instance properties and methods.
        Core._inherit(constructorClass.prototype, definition, constructorClass.$virtual);
        
        // If class is concrete, verify all abstract methods are provided.
        if (!constructorClass.$abstract) {
            this._verifyAbstractImpl(constructorClass);
        }
        
        // Invoke static constructors.
        if (loadMethod) {
            // Invoke $load() function with "this" pointer set to class.
            loadMethod.call(constructorClass);
        }
        
        return constructorClass;
    },
    
    /**
     * Retrieves a value from an object hierarchy.
     *
     * Examples: 
     * Given the following object 'o': <code>{ a: { b: 4, c: 2 }}</code>
     * <ul>
     * <li><code>Core.get(o, ["a", "b"]) will return <code>4</code>.</li>
     * <li><code>Core.get(o, ["a", "c"]) will return <code>2</code>.</li>
     * <li><code>Core.get(o, ["a", "d"]) will return <code>null</code>.</li>
     * <li><code>Core.get(o, ["a"]) will return <code>{ b: 4, c: 2 }</code>.</li>
     * <li><code>Core.get(o, ["b"]) will return <code>null</code>.</li>
     * <li><code>Core.get(o, ["d"]) will return <code>null</code>.</li>
     * </ul>
     *
     * @param object an arbitrary object from which the value should be retrieved
     * @param {Array} path an array of object property names describing the path to retrieve
     * @return the value, if found, or null if it does not exist
     */
    get: function(object, path) {
        for (var i = 0; i < path.length; ++i) {
            object = object[path[i]];
            if (!object) {
                return null;
            }
        }

        return object;
    },
    
    /**
     * Determines if the specified propertyName of the specified object is a virtual
     * property, i.e., that it can be overridden by subclasses.
     */
    _isVirtual: function(virtualProperties, propertyName) {
        switch (propertyName) {
        case "toString":
        case "valueOf":
            return true;
        }
        
        return virtualProperties[propertyName];
    },
    
    /**
     * Installs properties from source object into destination object.
     * <p>
     * In the case where the destination object already has a property defined
     * and the "virtualProperties" argument is provided, the "virtualProperties"
     * collection will be checked to ensure that property is allowed to be
     * overridden.  If "virtualProperties" is omitted, any property may be
     * overridden.
     *
     * @param destination the destination object
     * @param soruce the source object
     * @param virtualProperties (optional) collection of virtual properties from base class.
     */
    _inherit: function(destination, source, virtualProperties) {
        for (var name in source) {
            if (virtualProperties && destination[name] !== undefined && !this._isVirtual(virtualProperties, name)) {
                // Property exists in destination as is not marked as virtual.
                throw new Error("Cannot override non-virtual property \"" + name + "\".");
            } else {
                destination[name] = source[name];
            }
        }
    },
    
    /**
     * Creates a new function which executes a specific method of an object instance.
     * Any arguments passed to the returned function will be passed to the method.
     * The return value of the method will be returned by the function.
     *
     * CAUTION: When adding and removing methods as listeners, note that two separately
     * constructed methods will not be treated as equal, even if their instance and method
     * properties are the same.  Failing to heed this warning can result in a memory leak,
     * as listeners would never be removed.
     *
     * @param instance the object instance
     * @param {Function} method the method to be invoked on the instance
     * @return the return value provided by the method
     */
    method: function(instance, method) {
        return function() {
            return method.apply(instance, arguments);
        };
    },
    
    /**
     * Add properties of mixin objects to destination object.
     * Mixins will be added in order, and any property which is already
     * present in the destination object will not be overridden.
     *
     * @param destination the destination object
     * @param {Array} mixins the mixin objects to add 
     */
    _processMixins: function(destination, mixins) {
        for (var i = 0; i < mixins.length; ++i) {
            for (var mixinProperty in mixins[i]) {
                if (destination.prototype[mixinProperty]) { 
                    // Ignore mixin properties that already exist.
                    continue;
                }
                destination.prototype[mixinProperty] = mixins[i][mixinProperty];
            }
        }
    },
    
    /**
     * Sets a value in an object hierarchy.
     *
     * Examples: 
     * Given the following object 'o': <code>{ a: { b: 4, c: 2 } }</code>
     * <ul>
     * <li><code>Core.set(o, ["a", "b"], 5)</code> will update the value of 'o' to be: <code>{ a: { b: 5, c: 2 } }</code></li>
     * <li><code>Core.set(o, ["a", "d"], 7)</code> will update the value of 'o' to be:
     * <code>{ a: { b: 4, c: 2, d: 7 } }</code></li>
     * <li><code>Core.set(o, ["e"], 9)</code> will update the value of 'o' to be: <code>{ a: { b: 4, c: 2 }, e: 9 }</code></li>
     * <li><code>Core.set(o, ["f", "g"], 8)</code> will update the value of 'o' to be: 
     * <code>{ a: { b: 4, c: 2 }, f: { g: 8 } }</code></li>
     * <li><code>Core.set(o, ["a"], 10)</code> will update the value of 'o' to be: <code>{ a: 10 }</code></li>
     * </ul>
     *
     * @param object an arbitrary object from which the value should be retrieved
     * @param {Array} path an array of object property names describing the path to retrieve
     * @return the value, if found, or null if it does not exist
     */
    set: function(object, path, value) {
        var parentObject = null;
        
        // Find or create container object.
        for (var i = 0; i < path.length - 1; ++i) {
            parentObject = object; 
            object = object[path[i]];
            if (!object) {
                object = {};
                parentObject[path[i]] = object;
            }
        }
        
        // Assign value.
        object[path[path.length - 1]] = value;
    },
    
    /**
     * Verifies that a concrete derivative of an abstract class implements
     * abstract properties present in the base class.
     *
     * @param constructorClass the class to verify
     */
    _verifyAbstractImpl: function(constructorClass) {
         var baseClass = constructorClass.$super;
         if (!baseClass || !baseClass.$abstract || baseClass.$abstract === true) {
             return;
         }
         
         for (var x in baseClass.$abstract) {
             if (constructorClass.prototype[x] == null) {
                 throw new Error("Concrete class does not provide implementation of abstract method \"" + x + "\".");
             }
         }
    }
};

// _______________________
// Object EchoAsyncMonitor

/**
 * Static object/namespace for polling server to monitor for asynchronous
 * server-initiated updates to the client ("server push").
 */
EchoAsyncMonitor = {
    
    /**
     * The time, in milleseconds, between polling requests.
     */
    timeInterval: 500,
    
    /**
     * Server query for polling service.
     */
    pollServiceRequest: "?serviceId=Echo.AsyncMonitor",
    
    /**
     * Timeout identifier for the delayed invocation of the next poll request
     * (used to cancel the timeout if required).
     */
    timeoutId: null,
    
    /**
     * Initiates an HTTP request to the asynchronous monitor poll service to
     * determine if the server has the need to update the client.
     */
    connect: function() {
        var conn = new EchoHttpConnection(EchoClientEngine.baseServerUri + EchoAsyncMonitor.pollServiceRequest, "GET");
        conn.responseHandler = EchoAsyncMonitor.responseHandler;
        conn.invalidResponseHandler = EchoAsyncMonitor.invalidResponseHandler;
        conn.connect();
    },
    
    /**
     * Processes an invalid response to the poll request.
     */
    invalidResponseHandler: function() {
        alert("Invalid response from server to asynchronous polling connection.");
    },
    
    /**
     * Starts the countdown to the next poll request.
     */
    start: function() {
        if (!EchoServerTransaction.active) {
            EchoAsyncMonitor.timeoutId = window.setTimeout("EchoAsyncMonitor.connect();", 
                    EchoAsyncMonitor.timeInterval);
        }
    },
    
    /**
     * Cancels the countdown to the next poll request, if necessary.
     */
    stop: function() {
        if (EchoAsyncMonitor.timeoutId) {
            window.clearTimeout(EchoAsyncMonitor.timeoutId);
            EchoAsyncMonitor.timeoutId = null;
        }
    },
    
    /**
     * Processes a response from the HTTP request made to the polling
     * service.
     *
     * @param conn the EchoHttpConnection containing the response information.
     */
    responseHandler: function(conn) {
        if ("true" == conn.getResponseXml().documentElement.getAttribute("request-sync")) {
            // Server is requesting synchronization: Initiate server transaction.
            EchoServerTransaction.connect();
        } else {
            // Server does not require synchronization: restart countdown to next poll request.
            EchoAsyncMonitor.start();
        }
    }
};

// _________________________
// Object EchoClientAnalyzer

/**
  * Static object/namespace to perform analysis of client browser's 
  * version information, capabilities, and quirks and store the information
  * in the outgoing ClientMessage.
  */
EchoClientAnalyzer = {
    
    /**
     * Analyzes properties of client browser and stores them in the outgoing
     * ClientMessage.
     */
    analyze: function() {
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
        EchoClientAnalyzer.setIntegerProperty(messagePartElement, "utcOffset", 0 - parseInt((new Date()).getTimezoneOffset(), 10));
        EchoClientAnalyzer.setTextProperty(messagePartElement, "unescapedXhrTest", "&amp;");
    },
    
    /**
     * Stores a boolean client property in the ClientMessage.
     *
     * @param messagePartElement the <code>message-part</code> element
     *        in which generated property XML elements should be stored
     * @param propertyName the property name
     * @param propertyValue the property value
     */
    setBooleanProperty: function(messagePartElement, propertyName, propertyValue) {
        propertyElement = messagePartElement.ownerDocument.createElement("property");
        propertyElement.setAttribute("type", "boolean");
        propertyElement.setAttribute("name", propertyName);
        propertyElement.setAttribute("value", propertyValue ? "true" : "false");
        messagePartElement.appendChild(propertyElement);
    },
    
    /**
     * Stores a integer client property in the ClientMessage.
     *
     * @param messagePartElement the <code>message-part</code> element
     *        in which generated property XML elements should be stored
     * @param propertyName the property name
     * @param propertyValue the property value
     */
    setIntegerProperty: function(messagePartElement, propertyName, propertyValue) {
        var intValue = parseInt(propertyValue, 10);
        if (isNaN(intValue)) {
            return;
        }
        propertyElement = messagePartElement.ownerDocument.createElement("property");
        propertyElement.setAttribute("type", "integer");
        propertyElement.setAttribute("name", propertyName);
        propertyElement.setAttribute("value", intValue);
        messagePartElement.appendChild(propertyElement);
    },
    
    /**
     * Stores a text client property in the ClientMessage.
     *
     * @param messagePartElement the <code>message-part</code> element
     *        in which generated property XML elements should be stored
     * @param propertyName the property name
     * @param propertyValue the property value
     */
    setTextProperty: function(messagePartElement, propertyName, propertyValue) {
        propertyElement = messagePartElement.ownerDocument.createElement("property");
        propertyElement.setAttribute("type", "text");
        propertyElement.setAttribute("name", propertyName);
        propertyElement.setAttribute("value", propertyValue);
        messagePartElement.appendChild(propertyElement);
    }
};

// ______________________________
// Object EchoClientConfiguration

/**
 * Static/object namespace which provides general-purpose Client Engine
 * configuration settings.
 */
EchoClientConfiguration = {

    /**
     * MessagePartProcessor implementation for EchoClientConfiguration.
     */
    MessageProcessor: { 
    
        /**
         * MessagePartProcessor process() method implementation.
         */
        process: function(messagePartElement) {
            for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
                if (messagePartElement.childNodes[i].nodeType == 1) {
                    if (messagePartElement.childNodes[i].tagName == "store") {
                        EchoClientConfiguration.MessageProcessor.processStore(messagePartElement.childNodes[i]);
                    }
                }
            }
        },
        
        /**
         * Proceses a <code>store</code> message to store ClientConfiguration information
         * received from the server.
         *
         * @param storeElement the <code>store</code> element to process
         */
        processStore: function(storeElement) {
            var propertyElements = storeElement.getElementsByTagName("property");
            for (var i = 0; i < propertyElements.length; ++i) {
                var name = propertyElements[i].getAttribute("name");
                var value = propertyElements[i].getAttribute("value");
                EchoClientConfiguration.propertyMap[name] = value;
            }
        }
    },

    /**
     * Client property map.
     */
    propertyMap: {
        defaultOutOfSyncErrorMessage: "A synchronization error has occurred.  Click \"Ok\" to re-synchronize.",
        defaultServerErrorMessage: "An application error has occurred.  Your session has been reset.",
        defaultSessionExpirationMessage : "Your session has been reset due to inactivity."
    }
};

// _______________________
// Object EchoClientEngine

/**
 * Static object/namespace providing core client engine functionality.
 */
EchoClientEngine = {

    /**
     * The base URI of the Echo application server.
     */
    baseServerUri: null,
    
    /**
     * Flag indicating whether debugging options (e.g., Debug Pane) are enabled.
     */
    debugEnabled: true,
    
    /**
     * Initialization loading status (progress bar length).
     */
    loadStatus: 2,
    
    /**
     * Current transaction id, retrieved from ServerMessage.
     */
    transactionId: "",
    
    /**
     * Configures browser-specific settings based on ClientProperties results.
     * Invoked when ClientProperties are stored.
     */
    configure: function() {
        if (EchoClientProperties.get("quirkCssPositioningOneSideOnly")) {
            // Initialize virtual positioning system used to emulate CSS positioning in MSIE.
            EchoVirtualPosition.init();
            
            // Set body.style.overflow to hidden in order to hide root scrollbar in IE.
            // This is a non-standard CSS property.
            document.documentElement.style.overflow = "hidden";
        }
    },
    
    /**
     * Disposes the Echo2 Client Engine, unregistering event listeners 
     * and releasing resources.
     */
    dispose: function() {
        EchoEventProcessor.dispose();
        EchoDomPropertyStore.disposeAll();
    },
    
    /**
     * Initializes the Echo2 Client Engine.
     *
     * @param baseServerUri the base URI of the Echo application server
     */
    init: function(baseServerUri, debugEnabled) {
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
    
        // Add disposal listener.
        EchoDomUtil.addEventListener(window, "unload", EchoClientEngine.dispose);
    
        // Increment progress bar showing loading status.
        EchoClientEngine.incrementLoadStatus();
    },
    
    /**
     * Handles a client-side error.
     */
    processClientError: function(message, ex) {
        var errorText;
        if (ex instanceof Error) {
            errorText = "Error Name: " + ex.name + "\n" + "Error Message: " + ex.message;
        } else {
            errorText = "Error: " + ex;
        }
    
        alert("The following client application error has occurred:\n\n" +
                "---------------------------------\n" +
                message + "\n\n" +
                errorText + "\n" +
                "---------------------------------\n\n" +
                "Please contact your server administrator.\n\n");
    },
    
    /**
     * Handles the expiration of server-side session information.
     */
    processSessionExpiration: function() {
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
    },
    
    /**
     * Handles a server-side error.
     */
    processServerError: function() {
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
    },
    
    /**
     * Disable (remove) loading status progress bar.
     * Performs no operation if application is not starting up, i.e.,
     * if first synchronization has been completed.
     */
    disableLoadStatus: function() {
        if (EchoClientEngine.loadStatus != -1) {
            EchoClientEngine.loadStatus = -1;
            EchoClientEngine.renderLoadStatus();
        }
    },
    
    /**
     * Increment loading status progress bar and re-render.
     * Performs no operation if application is not starting up, i.e.,
     * if first synchronization has been completed.
     */
    incrementLoadStatus: function() {
        if (EchoClientEngine.loadStatus != -1) {
            ++EchoClientEngine.loadStatus;
            EchoClientEngine.renderLoadStatus();
        }
    },
    
    /**
     * Render current loading status.
     * Performs no operation if element with id "loadstatus" 
     * does not exist.
     */
    renderLoadStatus: function() {
        var loadStatusDivElement = document.getElementById("loadstatus");
        if (!loadStatusDivElement) {
            return;
        }
        if (EchoClientEngine.loadStatus == -1) {
            loadStatusDivElement.parentNode.removeChild(loadStatusDivElement);
        } else {
            var text = "";
            for (var i = 0; i < EchoClientEngine.loadStatus; ++i) {
                text += "|";
            }
            loadStatusDivElement.removeChild(loadStatusDivElement.firstChild);
            loadStatusDivElement.appendChild(document.createTextNode(text));
        }
    },
    
    /**
     * Update current transaction id, storing new value in client engine and outgoing client message.
     * 
     * @param newValue the new transaction id
     */
    updateTransactionId: function(newValue) {
        EchoClientEngine.transactionId = newValue;
        EchoClientMessage.messageDocument.documentElement.setAttribute("trans-id", EchoClientEngine.transactionId);
    },
    
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
     * @param element the elment or id of the element
     * @param allowInputDuringTransaction flag indicating that input should be 
     *        allowed during client/server transactions (Use of this flag is
     *        recommended against in MOST situations, see above)
     */
    verifyInput: function(element, allowInputDuringTransaction) {
        if (!allowInputDuringTransaction && EchoServerTransaction.active) {
            return false;
        }
        if (!EchoModalManager.isElementInModalContext(element)) {
            return false;
        }
        if (EchoDomPropertyStore.getPropertyValue(element, "EchoClientEngine.inputDisabled")) {
            return false;
        }
        return true;
    }
};

// ________________________
// Object EchoClientMessage

/**
 * Static object/namespace representing the outgoing ClientMessage.
 */
EchoClientMessage = { 
            
    /**
     * The current outgoing ClientMessage XML document.
     */
    messageDocument: null,
    
    /**
     * Creates a property element in the EchoClientMessage.
     *
     * @param componentId the id of the component
     * @param propertyName the name of the property
     * @return the created 'property' element
     */
    createPropertyElement: function(componentId, propertyName) {
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
    },
    
    /**
     * Creates or retrieves the "message-part" DOM element from the 
     * EchoClientMessage with the specified processor attribute.
     *
     * @param processor a string representing the processor type of
     *        the message-part element to return
     * @return the "message-part" DOM element
     */
    getMessagePart: function(processor) {
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
    },
    
    /**
     * Retrieves the value of a string property in the EchoClientMessage.
     *
     * @param componentId the id of the component.
     * @param propertyName the name of the property.
     * @return the value of the property
     */
    getPropertyValue: function(componentId, propertyName) {
        var messagePartElement = EchoClientMessage.getMessagePart("EchoPropertyUpdate");
        var propertyElements = messagePartElement.getElementsByTagName("property");
        for (var i = 0; i < propertyElements.length; ++i) {
            if (componentId == propertyElements[i].getAttribute("component-id") &&
                    propertyName == propertyElements[i].getAttribute("name")) {
                return propertyElements[i].getAttribute("value");
            }
        }
        return null;
    },
    
    /**
     * Removes a property element (if present) from the EchoClientMessage.
     *
     * @param componentId the id of the component
     * @param propertyName the name of the property
     */
    removePropertyElement: function(componentId, propertyName) {
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
    },
    
    /**
     * Resets the state of the ClientMessage.
     * This method is invoked after a ClientMessage has been sent to the server
     * and is thus no longer relevant.
     */
    reset: function() {
        // Clear existing message document.
        EchoClientMessage.messageDocument = null;
    
        // Create new message document.
        EchoClientMessage.messageDocument =
                EchoDomUtil.createDocument("http://www.nextapp.com/products/echo2/climsg", "client-message");
    },
    
    /**
     * Sets the action of the EchoClientMessage.  This method should only be
     * called one time, and the EchoClientMessage should be submitted to the
     * server soon thereafter.
     *
     * @param componentId the id of the component which initiated the action
     * @param actionName the name of the action
     * @param actionValue the value of the action (optional)
     * @return the created 'action' element
     */
    setActionValue: function(componentId, actionName, actionValue) {
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
        
        return actionElement;
    },
    
    /**
     * Stores the value of a string property in in the EchoClientMessage.
     *
     * @param componentId the id of the component
     * @param propertyName the name of the property
     * @param newValue the new value of the property
     */
    setPropertyValue: function(componentId, propertyName, newValue) {
        var propertyElement = EchoClientMessage.createPropertyElement(componentId, propertyName);
        
        propertyElement.setAttribute("value", newValue);
    
        EchoDebugManager.updateClientMessage();
    },
    
    /**
     * Sets the "type" attribute of the <code>client-message</code> element to 
     * indicate that this is the initial client message.
     */
    setInitialize: function() {
        EchoClientMessage.messageDocument.documentElement.setAttribute("type", "initialize");
    }
};

// ___________________________
// Object EchoClientProperties

/**
 * Static object/namespace providing information about the make/model/version,
 * capabilities, quirks, and limitations of the browser client.
 */
EchoClientProperties = {

    /**
     * Associative array mapping client property names to values.
     */
    propertyMap: { },
    
    /**
     * MessagePartProcessor implementation for EchoClientProperties.
     */
    MessageProcessor: {
    
        /**
         * MessagePartProcessor process() method implementation.
         */
        process: function(messagePartElement) {
            for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
                if (messagePartElement.childNodes[i].nodeType == 1) {
                    if (messagePartElement.childNodes[i].tagName == "store") {
                        EchoClientProperties.MessageProcessor.processStore(messagePartElement.childNodes[i]);
                    }
                }
            }
        },
        
        /**
         * Proceses a <code>store</code> message to store ClientProperties information
         * received from the server.
         *
         * @param storeElement the <code>store</code> element to process
         */
        processStore: function(storeElement) {
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
        }
    },
    
    /**
     * Returns the specified value from the ClientProperties store.
     *
     * @param name the name of the property
     * @param value the value of the property
     */
    get: function(name) {
        return EchoClientProperties.propertyMap[name];
    }
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
EchoCollectionsMap = Core.extend({

    /**
     * Creates a new map.
     */
    $construct: function() {
        this.removeCount = 0;
        this.garbageCollectionInterval = 250;
        this.associations = { };
    },
    
    /**
     * Retrieves the value referenced by the spcefied key.
     *
     * @param key the key
     * @return the value, or null if no value is referenced
     */
    get: function(key) {
        return this.associations[key];
    },
    
    /**
     * Stores a value referenced by the specified key.
     *
     * @param key the key
     * @param value the value
     */
    put: function(key, value) {
        if (value === null) {
            this.remove(key);
            return;
        }
        this.associations[key] = value;
    },
    
    /**
     * Performs 'garbage-collection' operations, recreating the array.
     * This operation is necessary due to Internet Explorer memory leak
     * issues.  It will be called automatically--there is no reason to
     * directly invoke this method.
     */
    garbageCollect: function() {
        this.removeCount = 0;
        var newAssociations = { };
        var i = 0;
        for (var key in this.associations) {
            newAssociations[key] = this.associations[key];
            ++i;
        }
        this.associations = newAssociations;
    },
    
    /**
     * Removes the value referenced by the specified key.
     *
     * @param key the key
     */
    remove: function(key) {
        delete this.associations[key];
        ++this.removeCount;
        if (this.removeCount >= this.garbageCollectionInterval) {
            this.garbageCollect();
        }
    }
});

// __________________
// Object EchoCssUtil

/**
 * Static object/namespace to provide cascading style-sheet (CSS) manipulation
 * utilities.
 */
EchoCssUtil = { 
    
    /**
     * Object used to determine the actual on-screen pixel bounds 
     * (top/left/width/height) of an element.
     * Craetes a new <code>Bounds</code> instance.
     * 
     * @param element the element to analyze
     */
    Bounds: Core.extend({
        
        /**
         * Creates a new <code>Bounds</code> instance.
         * 
         * @param element the element to measure
         */
        $construct: function(element) {
            this.left = 0;
            this.top = 0;
            this.width = element.offsetWidth;
            this.height = element.offsetHeight;
            while (element != null) {
                if (element.scrollTop) {
                    this.top -= element.scrollTop;
                }
                if (element.scrollLeft) {
                    this.left -= element.scrollLeft;
                }
                this.left += element.offsetLeft;
                this.top += element.offsetTop;
                element = element.offsetParent;
            }
        },
        
        /**
         * Renders a <code>Bounds</code> object to a human-readable debug string.
         */
        toString: function() {
            return "(" + this.left + ", " + this.top + ") [" + this.width + "x" + this.height + "]";
        }
    }),

    /**
     * Adds a rule to the document stylesheet.
     * This method does not function in Safari/KHTML.
     * 
     * @param selectorText the selector
     * @param style the style information
     */
    addRule: function(selectorText, style) {
        var ss = document.styleSheets[0];
        if (ss.insertRule) {
            // W3C DOM Browsers.
            ss.insertRule(selectorText + " {" + style + "}", ss.cssRules.length);
        } else if (ss.addRule) {
            // IE6.
            ss.addRule(selectorText, style);
        }
    },
    
    /**
     * Updates CSS information about a specific element.
     * 
     * @param element the DOM element to update
     * @param cssText the CSS style text describing the updates to perform to
     *        the existing CSS information of the element
     */
    applyStyle: function(element, cssText) {
        var styleProperties = cssText.split(";");
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
    },
    
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
    applyTemporaryStyle: function(element, cssStyleText) {
        // Set original style if not already set.
    
        if (!EchoDomPropertyStore.getPropertyValue(element, "EchoCssUtil.originalStyle")) {
            if (EchoDomUtil.getCssText(element)) {
                EchoDomPropertyStore.setPropertyValue(element, "EchoCssUtil.originalStyle", EchoDomUtil.getCssText(element));
            } else {
                // Flag that no original CSS text existed.
                EchoDomPropertyStore.setPropertyValue(element, "EchoCssUtil.originalStyle", "-");
            }
        }
        
        // Apply new style.
        EchoCssUtil.applyStyle(element, cssStyleText);
    },
    
    /**
     * Removes a rule from the document stylesheet.
     * This method does not function in Safari/KHTML.
     * 
     * @param selectorText the selector
     */
    removeRule: function(selectorText) {
        selectorText = selectorText.toLowerCase();
        var ss = document.styleSheets[0];
        
        // Retrieve rules object for W3C DOM : IE6.
        var rules = ss.cssRules ? ss.cssRules : ss.rules;
        
        for (var i = 0; i < rules.length; ++i) {
            if (rules[i].type == 1 && rules[i].selectorText.toLowerCase() == selectorText) {
                if (ss.deleteRule) {
                    // Delete rule: W3C DOM.
                    ss.deleteRule(i);
                    break;
                } else if (ss.removeRule) {
                    // Delete rule: IE6.
                    ss.deleteRule(i);
                    break;
                } else {
                    alert("rem fail");
                }
            }
        }
    },
    
    /**
     * Restores the original style to an element whose style was updated via
     * <code>applyTemporaryStyle</code>.  Removes the 'original' style from the 
     * DomPropertyStore attribute of the element.
     *
     * @param element the DOM element to restore to its original state
     */
    restoreOriginalStyle: function(element) {
        // Obtain original style.
        var originalStyle = EchoDomPropertyStore.getPropertyValue(element, "EchoCssUtil.originalStyle");
        
        // Do nothing if no original style exists.
        if (!originalStyle) {
            return;
        }
    
        // Reset original style on element.
        EchoDomUtil.setCssText(element, originalStyle == "-" ? "" : originalStyle);
        
        // Clear original style from EchoDomPropertyStore.
        EchoDomPropertyStore.setPropertyValue(element, "EchoCssUtil.originalStyle", null);
    }
};

// _______________________
// Object EchoDebugManager

/**
 * Static object/namespace for managing the Debug Window.
 */
EchoDebugManager = {

    /**
     * Reference to the active debug window.
     */
    debugWindow: null,
    
    /**
     * Writes a message to the console.
     *
     * @param message the message to write
     */
    consoleWrite: function(message) {
        if (!EchoDebugManager.debugWindow || EchoDebugManager.debugWindow.closed) {
            return;
        }
        EchoDebugManager.debugWindow.EchoDebug.Console.write(message);
    },
    
    /**
     * Opens the debug window.
     */
    launch: function() {
        EchoDebugManager.debugWindow = window.open(EchoClientEngine.baseServerUri + "?serviceId=Echo.Debug", "EchoDebug", 
                "width=400,height=650,resizable=yes", true);
    },
    
    /**
     * Updates the current state of the ClientMessage in the synchronization tab.
     */
    updateClientMessage: function() {
        if (!EchoDebugManager.debugWindow || EchoDebugManager.debugWindow.closed) {
            return;
        }
        EchoDebugManager.debugWindow.EchoDebug.Sync.displayClientMessage(EchoClientMessage.messageDocument);
    },
    
    /**
     * Updates the current state of the ServerMessage in the synchronization tab.
     */
    updateServerMessage: function() {
        if (!EchoDebugManager.debugWindow || EchoDebugManager.debugWindow.closed) {
            return;
        }
        EchoDebugManager.debugWindow.EchoDebug.Sync.displayServerMessage(EchoServerMessage.messageDocument);
    }
};

// ___________________________
// Object EchoDomPropertyStore

/**
 * Static object/namespace which provides storage of non-rendered property values
 * relevant to specific DOM elements within the DOM elmenets themselves.
 * Values are stored within associative arrays that are stored in a
 * 'echoDomPropertyStore' property of a DOM element.
 */
EchoDomPropertyStore = {

    /**
     * Static object/namespace for EchoDomPropertyStore MessageProcessor 
     * implementation.  Provides capability to set EchoDomPropertyStore
     * properties from ServerMessage directives.
     */
    MessageProcessor: {
        
        /**
         * MessageProcessor process() implementation 
         * (invoked by ServerMessage processor).
         *
         * @param messagePartElement the <code>message-part</code> element to process
         */
        process: function(messagePartElement) {
            for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
                if (messagePartElement.childNodes[i].nodeType == 1) {
                    if (messagePartElement.childNodes[i].tagName == "store-property") {
                        EchoDomPropertyStore.MessageProcessor.processStoreProperty(messagePartElement.childNodes[i]);
                    }
                }
            }
        },
        
        /**
         * Processes a <code>store-property</code> message to set the state of an
         * <code>EchoDomPropertyStore</code> property.
         *
         * @param storePropertyElement the <code>store-property</code> element to process
         */
        processStoreProperty: function(storePropertyElement) {
            var propertyName = storePropertyElement.getAttribute("name");
            var propertyValue = storePropertyElement.getAttribute("value");
            var items = storePropertyElement.getElementsByTagName("item");
            for (var i = 0; i < items.length; ++i) {
                var elementId = items[i].getAttribute("eid");
                EchoDomPropertyStore.setPropertyValue(elementId, propertyName, propertyValue);
            }
        }
    },
    
    /**
     * Disposes of any <code>EchoDomPropertyStore</code> properties stored for a 
     * specific element.
     * 
     * @param element the element or element id on which all properties should 
     *        be removed
     */
    dispose: function(element) {
        if (typeof element == "string") {
            element = document.getElementById(element);
        }
        if (!element) {
            throw new Error("Element not found.");
        }
        if (element.echoDomPropertyStore) {
            for (var elementProperty in element.echoDomPropertyStore) {
                delete element.echoDomPropertyStore[elementProperty];
            }
        }
        element.echoDomPropertyStore = undefined;
    },
    
    /**
     * Destorys EchoDomPropertyStore instances for all elements in document.
     */
    disposeAll: function() {
        EchoDomPropertyStore.disposeAllRecurse(document.documentElement);
    },
    
    /**
     * Recursive implementation of disposeAll().
     * Disposes of an element's property store (if applicable) and
     * recursively invokes on child elements.
     * 
     * @param element the elemnt to dispose
     */
    disposeAllRecurse: function(element) {
        if (element.echoDomPropertyStore) {
            for (var elementProperty in element.echoDomPropertyStore) {
                delete element.echoDomPropertyStore[elementProperty];
            }
        }
        element.echoDomPropertyStore = undefined;
        
        for (var childElement = element.firstChild; childElement; childElement = childElement.nextSibling) {
            if (childElement.nodeType == 1) {
                EchoDomPropertyStore.disposeAllRecurse(childElement);
            }
        }
    },
    
    /**
     * Retrieves the value of a specific <code>EchoDomPropertyStore</code> property.
     *
     * @param element the element or element id on which to set the property
     * @param propertyName the name of the property
     * @return the property value, or null if it is not set
     */
    getPropertyValue: function(element, propertyName) {
        if (typeof element == "string") {
            element = document.getElementById(element);
        }
        
        if (!element) {
            return null;
        }
        if (element.echoDomPropertyStore) {
            return element.echoDomPropertyStore[propertyName];
        } else {
            return null;
        }
    },
    
    /**
     * Sets the value of a specific <code>EchoDomPropertyStore</code> property.
     *
     * @param element the element or element id on which to set the property
     * @param propertyName the name of the property
     * @param propertyValue the new value of the property
     */
    setPropertyValue: function(element, propertyName, propertyValue) {
        if (typeof element == "string") {
            element = document.getElementById(element);
        }
        if (!element) {
            return;
        }
        if (!element.echoDomPropertyStore) {
            element.echoDomPropertyStore = { };
        }
        element.echoDomPropertyStore[propertyName] = propertyValue;
    }
};

// ____________________
// Object EchoDomUpdate

/**
 * Static object/namespace for performing server-directed updates to the
 * client DOM.
 */
EchoDomUpdate = {

    /**
     * Static object/namespace for EchoDomUpdate MessageProcessor 
     * implementation.
     */
    MessageProcessor: {
    
        /**
         * MessageProcessor process() implementation 
         * (invoked by ServerMessage processor).
         *
         * @param messagePartElement the <code>message-part</code> element to process
         */
        process: function(messagePartElement) {
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
                    case "stylesheet-add-rule":
                        this.processStyleSheetAddRule(messagePartElement.childNodes[i]);
                        break;
                    case "stylesheet-remove-rule":
                        this.processStyleSheetRemoveRule(messagePartElement.childNodes[i]);
                        break;
                    }
                }
            }
        },
        
        /**
         * Processes a <code>dom-add</code> directive to append or insert new nodes into
         * the DOM.
         *
         * @param domAddElement the <code>dom-add</code> element to process
         */
        processAdd: function(domAddElement) {
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
        },
        
        /**
         * Processes an <code>attribute-update</code> directive to update an attribute
         * of a DOM element.
         *
         * @param attributeUpdateElement the <code>attribute-update</code> element to 
         *        process
         */
        processAttributeUpdate: function(attributeUpdateElement) {
            var targetId = attributeUpdateElement.getAttribute("target-id");
            var targetElement = document.getElementById(targetId);
            if (!targetElement) {
                throw new EchoDomUpdate.TargetNotFoundException("AttributeUpdate", "target", targetId);
            }
            targetElement[attributeUpdateElement.getAttribute("name")] = attributeUpdateElement.getAttribute("value");
        },
        
        /**
         * Processes a <code>dom-remove</code> directive to delete nodes from the DOM.
         *
         * @param domRemoveElement the <code>dom-remove</code> element to process
         */
        processRemove: function(domRemoveElement) {
            var targetId = domRemoveElement.getAttribute("target-id");
            var targetElement = document.getElementById(targetId);
            if (!targetElement) {
                return;
            }
            targetElement.parentNode.removeChild(targetElement);
        },
        
        /**
         * Processes a <code>dom-remove-children</code> directive to delete all child 
         * nodes from a specific DOM element.
         *
         * @param domRemoveChildrenElement the <code>dom-remove-children</code> element 
         *        to process
         */
        processRemoveChildren: function(domRemoveElement) {
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
        },
        
        /**
         * Processes a <code>stylesheet-add-rule</code> directive to add a new CSS style sheet rule.
         * 
         * @param addRuleElement the <code>stylesheet-add-rule</code> element to process
         */
        processStyleSheetAddRule: function(addRuleElement) {
            var selectorText = addRuleElement.getAttribute("selector");
            var style = addRuleElement.getAttribute("style");
            EchoCssUtil.addRule(selectorText, style);
        },
        
        /**
         * Processes a <code>stylesheet-add-remove</code> directive to remove a new CSS style sheet rule.
         * 
         * @param removeRuleElement the <code>stylesheet-remove-rule</code> element to process
         */
        processStyleSheetRemoveRule: function(removeRuleElement) {
            var selectorText = removeRuleElement.getAttribute("selector");
            EchoCssUtil.removeRule(selectorText);
        },
        
        /**
         * Processes a <code>style-update</code> directive to update an the CSS style
         * of a DOM element.
         *
         * @param styleUpdateElement the <code>style-update</code> element to 
         *        process
         */
        processStyleUpdate: function(styleUpdateElement) {
            var targetId = styleUpdateElement.getAttribute("target-id");
            var targetElement = document.getElementById(targetId);
            if (!targetElement) {
                throw new EchoDomUpdate.TargetNotFoundException("StyleUpdate", "target", targetId);
            }
            targetElement.style[styleUpdateElement.getAttribute("name")] = styleUpdateElement.getAttribute("value");
        }
    },
    
    /**
     * An exception describing a failure of an EchoDomUpdate operation due to a 
     * target node not existing.
     */
    TargetNotFoundException: Core.extend({

        /**
         * Creates a new <code>TargetNotFoundException</code>.
         * 
         * @param updateType the type of update, e.g., DomRemove or StyleUpdate
         * @param targetType the type of target that caused the failure
         * @param targetId the id of the target
         */
        $construct: function(updateType, targetType, targetId) {
            this.targetId = targetId;
            this.targetType = targetType;
            this.updateType = updateType;
        },
    
        /**
         * Renders a String representation of the exception.
         *
         * @return the String representation
         */
        toString: function() {
            return "Failed to perform \"" + this.updateType + "\": " + this.targetType +
                    " element \"" + this.targetId + "\" not found.";
        }
    })
};

// __________________
// Object EchoDomUtil

/** 
 * Static object/namespace for performing cross-platform DOM-related 
 * operations.  Most methods in this object/namespace are provided due
 * to nonstandard behavior in clients.
 */
EchoDomUtil = {

    /**
     * An associative array which maps between HTML attributes and HTMLElement 
     * property  names.  These values generally match, but multiword attribute
     * names tend to have "camelCase" property names, e.g., the 
     * "cellspacing" attribute corresponds to the "cellSpacing" property.
     * This map is required by the Internet Explorer-specific importNode() 
     * implementation
     */
    attributeToPropertyMap: {
        accesskey: "accessKey",
        cellpadding: "cellPadding",
        cellspacing: "cellSpacing",
        "class": "className",
        codebase: "codeBase",
        codetype: "codeType",
        colspan: "colSpan",
        datetime: "dateTime",
        frameborder: "frameBorder",
        longdesc: "longDesc",
        marginheight: "marginHeight",
        marginwidth: "marginWidth",
        maxlength: "maxLength",
        noresize: "noResize",
        noshade: "noShade",
        nowrap: "noWrap",
        readonly: "readOnly",
        rowspan: "rowSpan",
        tabindex: "tabIndex",
        usemap: "useMap",
        valign: "vAlign",
        valueType: "valueType"
    }, 

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
    addEventListener: function(eventSource, eventType, eventListener, useCapture) {
        if (eventSource.addEventListener) {
            eventSource.addEventListener(eventType, eventListener, useCapture);
        } else if (eventSource.attachEvent) {
            eventSource.attachEvent("on" + eventType, eventListener);
        }
    },
    
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
    createDocument: function(namespaceUri, qualifiedName) {
        if (document.implementation && document.implementation.createDocument) {
            // DOM Level 2 Browsers
            var dom;
            if (EchoClientProperties.get("browserMozillaFirefox") && 
                    EchoClientProperties.get("browserVersionMajor") == 3 && EchoClientProperties.get("browserVersionMinor") === 0) {
                // https://bugzilla.mozilla.org/show_bug.cgi?id=431701
                dom = new DOMParser().parseFromString("<?xml version='1.0' encoding='UTF-8'?><" + qualifiedName + "/>",
                        "application/xml");
            } else {
                dom = document.implementation.createDocument(namespaceUri, qualifiedName, null);
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
    },
    
    /**
     * Converts a hyphen-separated CSS attribute name into a camelCase
     * property name.
     *
     * @param attribute the CSS attribute name, e.g., border-color
     * @return the style property name, e.g., borderColor
     */
    cssAttributeNameToPropertyName: function(attribute) {
        var segments = attribute.split("-");
        var out = segments[0];
        for (var i = 1; i < segments.length; ++i) {
            out += segments[i].substring(0, 1).toUpperCase();
            out += segments[i].substring(1);
        }
        return out;
    },
    
    /**
     * Escapes the text if this browser has a quirk where it does not properly escape
     * the out going XML.
     */
    escapeText: function(text) {
        text = text.replace(/&/g, "&amp;");
        text = text.replace(/</g, "&lt;");
        return text;
    },

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
    fixSafariAttrs: function(node) {
        if (node.nodeType == 1) {
            for (var i = 0; i < node.attributes.length; ++i) {
                var attribute = node.attributes[i];
                node.setAttribute(attribute.name, attribute.value.replace("&#38;", "&"));
            }
        }
        
        for (var childNode = node.firstChild; childNode; childNode = childNode.nextSibling) {
            EchoDomUtil.fixSafariAttrs(childNode);
        }
    },
    
    /**
     * Recursively fixes broken element attributes in a Safari DOM.
     * Safari does not properly escape attributes when sending them
     * via XHR. Therefore, we must do this ourselves, manually.
     *
     * @param node the starting node whose attributes are to be fixed (child 
     *        elements will be fixed recursively)
     */
    fixSafariEscaping: function(node) {
        // Element node attributes
        if (node.nodeType == 1) {
            /*
             * NOTE:
             * The below code to iterate over the attributes of a node
             * is the way it is due to a bug in Safari 2.0 (atleast
             * WebKit 418.x through 419.x). Normally, we would be able
             * to simply do:
             * 
             * for (i = 0; i < node.attributes.length; ++i) {
             *       var attribute = node.attributes[i];
             * 
             * However, in the buggy versions of WebKit, merly calling
             * node.attributes[i] for some reason causes all attribute
             * values to be empty when serializing the DOM out during the
             * XHR response. Therefore, we must work around this bug
             * by iterating over all child nodes and finding the ones
             * that are attribute nodes, instead.
             */
            for (var i = 0; i < node.childNodes.length; ++i) {
                var cn = node.childNodes[i];
                if (cn.nodeType == 2) {
                    var attribute = cn;
                    node.setAttribute(attribute.name, EchoDomUtil.escapeText(attribute.nodeValue));
                }
            }
        }
        
        // Text Nodes
        if (node.nodeType == 3) {
            node.nodeValue = EchoDomUtil.escapeText(node.nodeValue);
        }
        
        for (var childNode = node.firstChild; childNode; childNode = childNode.nextSibling) {
            EchoDomUtil.fixSafariEscaping(childNode);
        }
    },

    /**
     * Returns the base component id of an extended id.
     * Example: for value "c_333_foo", "c_333" would be returned.
     *
     * @param elementId the extended id
     * @return the component id, or null if it cannot be determined
     */
    getComponentId: function(elementId) {
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
    },
    
    /**
     * Cross-platform method to retrieve the CSS text of a DOM element.
     *
     * @param element the element
     * @return cssText the CSS text
     */
    getCssText: function(element) {
        if (EchoClientProperties.get("quirkOperaNoCssText")) {
            return element.getAttribute("style");
        } else {
            return element.style.cssText;
        }
    },
    
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
    getEventTarget: function(e) {
        return e.target ? e.target : e.srcElement;
    },
    
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
    importNode: function(targetDocument, sourceNode, importChildren) {
        if (targetDocument.importNode) {
            // DOM Level 2 Browsers
            return targetDocument.importNode(sourceNode, importChildren);
        } else {
            // Internet Explorer Browsers
            return EchoDomUtil.importNodeImpl(targetDocument, sourceNode, importChildren);
        }
    },
    
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
    importNodeImpl: function(targetDocument, sourceNode, importChildren) {
        var targetNode;  
       
        if (importChildren || !sourceNode.hasChildNodes()) {
            // Attempt to use innerHTML to import node.
            // This approach cannot be used if importing children is not desired and
            // the element has children.
            targetNode = EchoDomUtil.importNodeByInnerHtml(targetDocument, sourceNode, importChildren);
            if (targetNode != null) {
                // Return created node if successfully imported, otherwise continue.
                return targetNode;
            }
        }
       
        // Import single node (but not its children).
        targetNode = EchoDomUtil.importNodeByDom(targetDocument, sourceNode);
        
        if (importChildren && sourceNode.hasChildNodes()) {
            // Recurse to import children.
            for (var sourceChildNode = sourceNode.firstChild; sourceChildNode; sourceChildNode = sourceChildNode.nextSibling) {
                var targetChildNode = EchoDomUtil.importNodeImpl(targetDocument, sourceChildNode, true);
                if (targetChildNode) {
                    targetNode.appendChild(targetChildNode);
                }
             }
        }
        return targetNode;
    },
   
    /**
    * Import a node by re-creating it using DOM methods.
    * This method will not import child elements.
    *
    * @param targetDocument the document into which the node/hierarchy is to be
    *        imported
    * @param sourceNode the node to import
    */
    importNodeByDom: function(targetDocument, sourceNode) {
        var targetNode, i;
    
        switch (sourceNode.nodeType) {
        case 1:
            targetNode = targetDocument.createElement(sourceNode.nodeName);
            for (i = 0; i < sourceNode.attributes.length; ++i) {
                var attribute = sourceNode.attributes[i];
                if ("style" == attribute.name) {
                    targetNode.style.cssText = attribute.value;
                } else {
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
    
        return targetNode;
    },
    
    /**
     * Try importing a node using innerHTML, on IE6 this is much faster than manually
     * traversing the DOM-tree.
     *
     * @param targetDocument the document into which the node/hierarchy is to be
     *        imported
     * @param sourceNode the node to import
     */
    importNodeByInnerHtml: function(targetDocument, sourceNode) {
        var targetNode;
        if (sourceNode.nodeName.match(/tr|tbody|table|thead|tfoot|colgroup/)) {
            // The innerHTML-property is read-only for the following objects:
            // COL, COLGROUP, FRAMESET, HTML, STYLE, TABLE, TBODY, TFOOT, THEAD, TITLE, TR
            // so we have to import nodes of such types using DOM-methods.
            // see: http://msdn.microsoft.com/workshop/author/dhtml/reference/properties/innerhtml.asp
            return null;
        }
    
        //Get the html of the sourcenode as a string.
        var sourceHTML = "";
        for (var sourceChildNode = sourceNode.firstChild; sourceChildNode; sourceChildNode = sourceChildNode.nextSibling) {
            //We are importing an xml-node.
            var childHTML = sourceChildNode.xml;
            if (childHTML == null) {
                return null;
            }
            sourceHTML += childHTML;
        }
       
        // Import the topnode
        targetNode = EchoDomUtil.importNodeByDom(targetDocument, sourceNode);
        
        // IE fails to recognize <tag/> as a closed tag when using innerHTML, so replace it with <tag></tag>
        // except for br, img, and hr elements.
        var expr = new RegExp("<(?:(?!br|img|hr)([a-zA-Z]+))([^>]*)/>", "ig");
        sourceHTML = sourceHTML.replace(expr, "<$1$2></$1>");
    
        if (EchoStringUtil.trim(sourceHTML).length > 0) {
            // Adding an empty string throws an 'Unknown error'.
            targetNode.innerHTML = sourceHTML;
        }
     
        return targetNode;
    },
    
    /**
     * Determines if <code>ancestorNode</code> is or is an ancestor of
     * <code>descendantNode</code>.
     *
     * @param ancestorNode the potential ancestor node
     * @param descendantNode the potential descendant node
     * @return true if <code>ancestorNode</code> is or is an ancestor of
     *         <code>descendantNode</code>
     */
    isAncestorOf: function(ancestorNode, descendantNode) {
        var testNode = descendantNode;
        while (testNode !== null) {
            if (testNode == ancestorNode) {
                return true;
            }
            testNode = testNode.parentNode;
        }
        return false;
    },
    
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
    preventEventDefault: function(e) {
        if (e.preventDefault) {
            e.preventDefault();
        } else {
            e.returnValue = false;
        }
    },
    
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
    removeEventListener: function(eventSource, eventType, eventListener, useCapture) {
        if (eventSource.removeEventListener) {
            eventSource.removeEventListener(eventType, eventListener, useCapture);
        } else if (eventSource.detachEvent) {
            eventSource.detachEvent("on" + eventType, eventListener);
        }
    },
    
    /**
     * Cross-platform method to set the CSS text of a DOM element.
     *
     * @param element the element
     * @param cssText the new CSS text
     */
    setCssText: function(element, cssText) {
        if (EchoClientProperties.get("quirkOperaNoCssText")) {
            if (cssText) {
                element.setAttribute("style", cssText);
            } else {
                element.removeAttribute("style");
            }
        } else {
            element.style.cssText = cssText;
        }
    },
    
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
    stopPropagation: function(e) {
        if (e.stopPropagation) {
            e.stopPropagation();
        } else {
            e.cancelBubble = true;
        }
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
 * Event handlers should return true if they wish for the event to continue
 * propagating.  Returning false (or nothing at all) will result in the
 * event not being fired to additional handlers.
 * 
 * Events may be registered with "capture" mode enabled.  Capturing event
 * handlers will receive notification of an event on a child control before
 * the child (whereas non-capturing handlers will receive notification
 * after the child element does).  This behavior works as described here even
 * on Internet Explorer browsers which lack support for the W3C Event Model and
 * its similar capability of event capturing.
 *
 * Additionally, use of the EchoEventProcessor minimizes "DHTML memory
 * leaks" and provides a mechanism for inspecting registered event handlers
 * using the Debug Window.
 */
EchoEventProcessor = {

    /**
     * Static object/namespace for EchoEventProcessor MessageProcessor 
     * implementation.
     */
    MessageProcessor: {
    
        /**
         * MessageProcessor process() implementation 
         * (invoked by ServerMessage processor).
         *
         * @param messagePartElement the <code>message-part</code> element to process
         */
        process: function(messagePartElement) {
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
        }
    },
    
    /**
     * An associative array mapping event types (e.g. "mouseover") to
     * associate arrays that map DOM element ids to the String names
     * of event handlers.
     */
    eventTypeToHandlersMap: new EchoCollectionsMap(),
    
    /**
     * Registers an event handler.
     *
     * @param element the target element or the id of the target element on which
     *        to register the event
     * @param eventType the type of the event (should be specified using 
     *        DOM level 2 event names, e.g., "mouseover" or "click", without
     *        the "on" prefix)
     * @param handler the name of the handler, as a String
     * @param capture flag indicating whether listener should be registered as a 
     *        capturing event handler (NOTE: this flag operates as expected even 
     *        on IE browsers)
     */
    addHandler: function(element, eventType, handler, capture) {
        var elementId;
        if (typeof element == "string") {
            elementId = element;
            element = document.getElementById(element);
        } else {
            elementId = element.id;
        }
    
        EchoDomUtil.addEventListener(element, eventType, EchoEventProcessor.processEvent, false);
        
        var elementIdToHandlerMap = EchoEventProcessor.eventTypeToHandlersMap.get(eventType);
        if (!elementIdToHandlerMap) {
            elementIdToHandlerMap = new EchoCollectionsMap();
            EchoEventProcessor.eventTypeToHandlersMap.put(eventType, elementIdToHandlerMap);
        }
        
        var handlerData = new EchoEventProcessor.EventHandlerData(handler, capture);
        
        elementIdToHandlerMap.put(elementId, handlerData);
    },
    
    /**
     * Disposes the EchoEventProcessor, releasing all resources.
     * This method is invoked when the client engine is disposed.
     */
    dispose: function() {
        for (var eventType in EchoEventProcessor.eventTypeToHandlersMap.associations) {
            var elementIdToHandlerMap = EchoEventProcessor.eventTypeToHandlersMap.associations[eventType];
            for (var elementId in elementIdToHandlerMap.associations) {
                var element = document.getElementById(elementId);
                if (element) {
                    EchoDomUtil.removeEventListener(element, eventType, EchoEventProcessor.processEvent, false);
                }
                elementIdToHandlerMap.remove(elementId);
            }
            EchoEventProcessor.eventTypeToHandlersMap.remove(eventType);
        }
    },
    
    /**
     * Retrieves the event handler of a specific type for a specific element.
     *
     * @param eventType the type of the event (should be specified using 
     *        DOM level 2 event names, e.g., "mouseover" or "click", without
     *        the "on" prefix)
     * @param elementId the elementId the id of the DOM element
     * @return the event handler name
     */
    getHandler: function(eventType, elementId) {
        var handlerData = EchoEventProcessor.getHandlerData(eventType, elementId);
        return handlerData ? handlerData.handlerName : null;
    },
    
    /**
     * Retrieves the event handler data object of a specific type for a specific element.
     *
     * @param eventType the type of the event (should be specified using 
     *        DOM level 2 event names, e.g., "mouseover" or "click", without
     *        the "on" prefix)
     * @param elementId the elementId the id of the DOM element
     */
    getHandlerData: function(eventType, elementId) {
        var elementIdToHandlerMap = EchoEventProcessor.eventTypeToHandlersMap.get(eventType);
        if (!elementIdToHandlerMap) {
            return null;
        }
        return elementIdToHandlerMap.get(elementId);
    },
    
    /**
     * Returns the element ids of all event handlers of the specified type.
     */
    getHandlerElementIds: function(eventType) {
        var elementIds = [];
        var elementIdToHandlerMap = EchoEventProcessor.eventTypeToHandlersMap.get(eventType);
        if (elementIdToHandlerMap) {
            for (var elementId in elementIdToHandlerMap.associations) {
                elementIds.push(elementId);
            }
        }
        return elementIds;
    },
    
    /**
     * Returns the types of all registered event handlers.
     *
     * @return an array of Strings containing the type names of all registered
     *         event handlers
     */
    getHandlerEventTypes: function() {
        var handlerTypes = [];
        for (var eventType in EchoEventProcessor.eventTypeToHandlersMap.associations) {
            handlerTypes.push(eventType);
        }
        return handlerTypes;
    },
    
    /**
     * Master event handler for all DOM events.
     * This method is registered as the event handler for all
     * EchoEventProcessor event registrations.  Events are processed,
     * modified, and then dispatched to the appropriate
     * Echo event handlers by this method.
     *
     * @param e the event fired by the DOM
     */
    processEvent: function(e) {
        var i, handler;
        
        e = e ? e : window.event;
        var eventType = e.type;
        if (!e.target && e.srcElement) {
            // The Internet Explorer event model stores the target element in the 'srcElement' property of an event.
            // Modify the event such the target is retrievable using the W3C DOM Level 2 specified property 'target'.
            e.target = e.srcElement;
        }
        var targetElement = e.target;
        var handlerDatas = [];
        var targetElements = [];
        
        var hasCapturingHandlers = false;
        
        while (targetElement) {
            if (targetElement.nodeType == 1) { // Element Node
                var handlerData = EchoEventProcessor.getHandlerData(eventType, targetElement.id);
                if (handlerData) {
                    hasCapturingHandlers |= handlerData.capture;
                    handlerDatas.push(handlerData);
                    targetElements.push(targetElement);
                }
            }
            targetElement = targetElement.parentNode;
        }
        
        if (handlerDatas.length === 0) {
            return;
        }
        
        var propagate = true;
        
        if (hasCapturingHandlers) {
            // Process capturing event handlers.
            for (i = handlerDatas.length - 1; i >=0; --i) {
                if (!handlerDatas[i].capture) {
                    // Ignore non-capturing handlers.
                    continue;
                }
    
                // Load handler.
                try {
                    handler = eval(handlerDatas[i].handlerName);
                } catch (ex1) {
                    throw "Invalid handler: " + handlerDatas[i].handlerName + " (" + ex1 + ")";
                }
        
                // Set registered target on event.
                e.registeredTarget = targetElements[i];
                
                // Invoke handler.
                propagate = handler(e);
                
                // Stop propagation if required.
                if (!propagate) {
                    break;
                }
            }
        }
    
        if (propagate) {
            // Process non-capturing event handlers.
            for (i = 0; i < handlerDatas.length; ++i) {
                if (handlerDatas[i].capture) {
                    // Ignore capturing handlers.
                    continue;
                }
                
                // Load handler.
                try {
                    handler = eval(handlerDatas[i].handlerName);
                } catch (ex2) {
                    throw "Invalid handler: " + handlerDatas[i].handlerName + " (" + ex2 + ")";
                }
        
                // Set registered target on event.
                e.registeredTarget = targetElements[i];
                
                // Invoke handler.
                propagate = handler(e);
                
                // Stop propagation if required.
                if (!propagate) {
                    break;
                }
            }
        }
        
        if (!propagate) {
            // Stop propagation of event.
            EchoDomUtil.stopPropagation(e);
        }
    },
    
    /**
     * Unregisters an event handler.
     *
     * @param element the target element or the id of the target element on which
     *        to register the event
     * @param eventType the type of the event (should be specified using 
     *        DOM level 2 event names, e.g., "mouseover" or "click", without
     *        the "on" prefix)
     */
    removeHandler: function(element, eventType) {
        var elementId;
        if (typeof element == "string") {
            elementId = element;
            element = document.getElementById(element);
        } else {
            elementId = element.id;
        }
        
        if (element) {
            EchoDomUtil.removeEventListener(element, eventType, EchoEventProcessor.processEvent, false);
        }
    
        var elementIdToHandlerMap = EchoEventProcessor.eventTypeToHandlersMap.get(eventType);
        if (!elementIdToHandlerMap) {
            return;
        }
        elementIdToHandlerMap.remove(elementId);
    }
};

/**
 * Data object describing an event handler.
 * 
 * @param handlerName the name of the event handler
 * @param capture flag indicating whether the handler is a capturing listener
 */
EchoEventProcessor.EventHandlerData = function(handlerName, capture) { 
    this.handlerName = handlerName;
    this.capture = capture;
};

// _______________________
// Object EchoFocusManager

/**
 * Static object/namespace to manage component focus.
 */
EchoFocusManager = {
    
    /**
     * Sets the focused state of a component.
     *
     * @param componentId the identifier of the component
     * @param focusState a boolean property describing whether the component is 
     *        focused
     */
    setFocusedState: function(componentId, focusState) {
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
    }
};

// _________________________
// Object EchoHttpConnection

EchoHttpConnection = Core.extend({
    
    $static: {
        nullMethod: function() { }
    },
    
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
    $construct: function(url, method, messageObject, contentType) {
        this.url = url;
        this.contentType = contentType;
        this.method = method;
        this.messageObject = messageObject;
        this.responseHandler = null;
        this.invalidResponseHandler = null;
        this.disposed = false;
    },
    
    /**
     * Executes the HTTP connection.
     * The responseHandler property of the connection must be set
     * before the connection is executed.
     * This method will return asynchronously; the <code>responseHandler</code>
     * callback property will be invoked when a valid response is received.
     */
    connect: function() {
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
    },
    
    dispose: function() {
        this.messageObject = null;
        this.responseHandler = null;
        this.invalidResponseHandler = null;
        this.xmlHttpRequest = null;
        this.disposed = true;
    },
    
    /**
     * Returns the response as text.
     * This method may only be invoked from a response handler.
     *
     * @return the response, as text
     */
    getResponseText: function() {
        return this.xmlHttpRequest ? this.xmlHttpRequest.responseText : null;
    },
    
    /**
     * Returns the response as an XML DOM.
     * This method may only be invoked from a response handler.
     *
     * @return the response, as an XML DOM
     */
    getResponseXml: function() {
        return this.xmlHttpRequest ? this.xmlHttpRequest.responseXML : null;
    },
    
    /**
     * Event listener for <code>readystatechange</code> events received from
     * the <code>XMLHttpRequest</code>.
     */
    processReadyStateChange: function() {
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
    }
});

// _______________________
// Object EchoModalManager

/**
 * Manages the modal state of the application.
 */
EchoModalManager = {
    
    /**
     * Element id of current modal object, or null if no object is currently 
     * modal.
     */
    modalElementId: null,
    
    /**
     * Determines if a particular element lies within the modal context.
     *
     * @param element the element or id of the element to analyze
     * @return true if the specified <code>element</code> is within the modal
     *         context
     */
    isElementInModalContext: function(element) {
        if (typeof element == "string") {
            element = document.getElementById(element);
        }
        if (EchoModalManager.modalElementId) {
            var modalElement = document.getElementById(EchoModalManager.modalElementId);
            return EchoDomUtil.isAncestorOf(modalElement, element);
        } else {
            return true;
        }
    }
};

// _______________________________
// Object EchoScriptLibraryManager

/**
 * Static object/namespace to manage dynamic loading of client libraries.
 */
EchoScriptLibraryManager = {

    /**
     * Library load-state constant indicating a library has been requested from 
     * the server.
     */
    STATE_REQUESTED: 1,
    
    /**
     * Library load-state constant indicating a library has been successfully
     * retrieved from the server.
     */
    STATE_LOADED: 2,
    
    /**
     * Library load-state constant indicating a library has been successfully
     * installed.
     */
    STATE_INSTALLED: 3,
    
    /**
     * Associative array mapping library service ids to library source code.
     */
    librarySourceMap: { },
    
    /**
     * Associative array mapping library service ids to load-states.
     */
    libraryLoadStateMap: { },
    
    /**
     * Queries the state of the specified libary.
     *
     * @param serviceId the server service identifier of the library
     * @return the load-state of the library, either
     *         <code>STATE_REQUESTED</code> or <code>STATE_LOADED</code>
     */
    getState: function(serviceId) {
        return EchoScriptLibraryManager.libraryLoadStateMap[serviceId];
    },
    
    /**
     * Installs a previously loaded JavaScript library.
     *
     * @param serviceId the server service identifier of the library
     */
    installLibrary: function(serviceId) {
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
    },
    
    /**
     * Loads a JavaScript library and stores it for execution.
     *
     * @param serviceId the server service identifier of the library
     */
    loadLibrary: function(serviceId) {
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
    },
    
    /**
     * Processes a valid response from the server.
     *
     * @param conn the EchoHttpConnection containing the response information.
     */
    responseHandler: function(conn) {
        EchoScriptLibraryManager.librarySourceMap[conn.serviceId] = conn.getResponseText();
        EchoScriptLibraryManager.libraryLoadStateMap[conn.serviceId] = EchoScriptLibraryManager.STATE_LOADED;
        EchoClientEngine.incrementLoadStatus();
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
 *   invoke EchoClientEngine.verifyInput() before accepting input.</li>
 *  <li>Displays a "wait" (hourglass) mouse cursor.</li>
 *  <li>Displays a "please wait" message after a time interval has elapsed
 *   during a longer-than-normal server transaction.</li>
 * </ul>
 */
EchoServerDelayMessage = {
    
    /**
     * MessagePartProcessor implementation for EchoServerDelayMessage.
     */
    MessageProcessor: {
        
        /**
         * MessagePartProcessor process() method implementation.
         */
        process: function(messagePartElement) {
            for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
                if (messagePartElement.childNodes[i].nodeType == 1) {
                    if (messagePartElement.childNodes[i].tagName == "set-message") {
                        EchoServerDelayMessage.MessageProcessor.processSetDelayMessage(messagePartElement.childNodes[i]);
                    }
                }
            }
        },
        
        /**
         * ServerMessage parser to handle requests to set delay message text.
         */
        processSetDelayMessage: function(setDirectiveElement) {
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
        }
    },
    
    MESSAGE_ELEMENT_ID: "serverDelayMessage",
    MESSAGE_ELEMENT_LONG_ID: "serverDelayMessageLong",
    
    /** 
     * Id of HTML element providing "long-running" delay message.  
     * This element is shown/hidden as necessary when long-running delays
     * occur or are completed.
     */
    delayMessageTimeoutId: null,
    
    /** Timeout (in ms) before "long-running" delay message is displayed. */ 
    timeout: 750,
    
    /**
     * Activates/shows the server delay message pane.
     */
    activate: function() {
        var serverDelayMessage = document.getElementById(EchoServerDelayMessage.MESSAGE_ELEMENT_ID);
        if (!serverDelayMessage) {
            return;
        }
        serverDelayMessage.style.visibility = "visible";
        EchoServerDelayMessage.delayMessageTimeoutId = window.setTimeout("EchoServerDelayMessage.activateDelayMessage()", 
                EchoServerDelayMessage.timeout);
    },
    
    /**
     * Activates the "long-running" delay message (requires server delay message
     * to have been previously activated).
     */
    activateDelayMessage: function() {
        EchoServerDelayMessage.cancelDelayMessageTimeout();
        var longMessage = document.getElementById(EchoServerDelayMessage.MESSAGE_ELEMENT_LONG_ID);
        if (!longMessage) {
            return;
        }
        longMessage.style.visibility = "visible";
    },
    
    /**
     * Cancels timeout object that will raise delay message when server
     * transaction is determined to be "long running".
     */
    cancelDelayMessageTimeout: function() {
        if (EchoServerDelayMessage.delayMessageTimeoutId) {
            window.clearTimeout(EchoServerDelayMessage.delayMessageTimeoutId);
            EchoServerDelayMessage.delayMessageTimeoutId = null;
        }
    },
    
    /**
     * Deactives/hides the server delay message.
     */
    deactivate: function() {
        EchoServerDelayMessage.cancelDelayMessageTimeout();
        var serverDelayMessage = document.getElementById(EchoServerDelayMessage.MESSAGE_ELEMENT_ID);
        var longMessage = document.getElementById(EchoServerDelayMessage.MESSAGE_ELEMENT_LONG_ID);
        if (serverDelayMessage) {
            serverDelayMessage.style.visibility = "hidden";
        }
        if (longMessage) {
            longMessage.style.visibility = "hidden";
        }
    }
};

// ________________________
// Object EchoServerMessage

/**
 * Static object to process synchronization messages received from server.
 */
EchoServerMessage = {
        
    STATUS_INITIALIZED: 0,
    STATUS_PROCESSING: 1,
    STATUS_PROCESSING_COMPLETE: 2,
    STATUS_PROCESSING_FAILED: 3,
    
    /**
     * Count of successfully processed server messages.
     */
    transactionCount: 0,
    
    /**
     * DOM of the ServerMessage.
     */
    messageDocument: null,
    
    /**
     * Identifier returned from setInterval() for asynchronously monitoring 
     * loading of JavaScript libraries.
     */
    backgroundIntervalId: null,
    
    /**
     * Flag indicating whether attribute values must be fixed to accommodate bugs in Safari browser.
     */
    enableFixSafariAttrs: false,
    
    /**
     * Callback to invoke when ServerMessage processing has completed.
     * Set via init() method.
     */
    processingCompleteListener: null,
    
    /**
     * Map of temporarily stored properties in the server message.
     * These properties are reset after each transaction.
     */
    temporaryProperties: null,
    
    /**
     * ServerMessage processing status, one of the following constants:
     * <ul>
     *  <li><code>EchoServerMessage.STATUS_INITIALIZED</code></li>
     *  <li><code>EchoServerMessage.STATUS_PROCESSING</code></li>
     *  <li><code>EchoServerMessage.STATUS_PROCESSING_COMPLETE</code></li>
     *  <li><code>EchoServerMessage.STATUS_PROCESSING_FAILED</code></li>
     * </ul>
     */
    status: 0, // EchoServerMessage.STATUS_INITIALIZED
    
    /**
     * Retrieves the value of a temporary property.
     * 
     * @param key the property key
     * @return the property value
     */
    getTemporaryProperty: function(key) {
        if (!EchoServerMessage.temporaryProperties) {
            return undefined;
        } else {
            return EchoServerMessage.temporaryProperties[key];
        }
    },
    
    /**
     * Initializes the state of the ServerMessage processor.
     *
     * @param messageDocument the ServerMessage XML document to be processed
     * @param processingCompleteListener a callback to be invoked when processing
     *        has been completed
     */
    init: function(messageDocument, processingCompleteListener) {
        EchoServerMessage.temporaryProperties = null;
        EchoServerMessage.messageDocument = messageDocument;
        EchoServerMessage.backgroundIntervalId = null;
        EchoServerMessage.processingCompleteListener = processingCompleteListener;
        EchoDebugManager.updateServerMessage();
    },
    
    /**
     * Determines if all required JavaScript libraries have been loaded.
     *
     * @return true if all libraries have been loaded.
     */
    isLibraryLoadComplete: function() {
        // 'complete' flag is used to set status to 'failed' in the event of an exception.
        var complete = false;
        try {
            var librariesElement = EchoServerMessage.messageDocument.getElementsByTagName("libraries").item(0);
            var libraryElements = librariesElement.getElementsByTagName("library");
            
            var returnValue = true;
            for (var i = 0; i < libraryElements.length; ++i) {
                var serviceId = libraryElements.item(i).getAttribute("service-id");
                var libraryState = EchoScriptLibraryManager.getState(serviceId);
                if (libraryState != EchoScriptLibraryManager.STATE_LOADED &&
                        libraryState != EchoScriptLibraryManager.STATE_INSTALLED) {
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
    },
    
    /**
     * Parses 'libraries' element of ServerMessage and dynamically loads
     * external JavaScript resources.
     * The libraries are loaded asynchronously--invocation of this
     * method will return before the actual libraries have been loaded.
     */
    loadLibraries: function() {
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
    },
    
    /**
     * Installs the dynamically retrieved JavaScript libraries.
     */
    installLibraries: function() {
        var librariesElement = EchoServerMessage.messageDocument.getElementsByTagName("libraries").item(0);
        var headElement = document.getElementsByTagName("head").item(0);
        if (!librariesElement) {
            return;
        }
        var libraryElements = librariesElement.getElementsByTagName("library");
        for (var i = 0; i < libraryElements.length; ++i) {
            var serviceId = libraryElements.item(i).getAttribute("service-id");
            EchoScriptLibraryManager.installLibrary(serviceId);
            EchoClientEngine.incrementLoadStatus();
        }
    },
    
    /**
     * Processes the ServerMessage.
     * The processing is performed asynchronously--this method will return before
     * the processing has been completed.
     */
    process: function() {
        EchoServerMessage.prepare();
        EchoServerMessage.processPhase1();
    },
    
    /**
     * Prepares the ServerMessage for prcocessing.
     */
    prepare: function() {
        // Obtain transaction id from server message.
        EchoClientEngine.updateTransactionId(EchoServerMessage.messageDocument.documentElement.getAttribute("trans-id"));
        
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
    },
    
    /**
     * Configures the asynchronous server polling system by retrieving
     * the "asyncinterval" attribute from the ServerMessage and starting
     * the asynchronous monitor if required.
     */
    processAsyncConfig: function() {
        var timeInterval = parseInt(EchoServerMessage.messageDocument.documentElement.getAttribute("async-interval"), 10);
        if (!isNaN(timeInterval)) {
            EchoAsyncMonitor.timeInterval = timeInterval;
            EchoAsyncMonitor.start();
        }
    },
    
    /**
     * Configures various application level properties, including:
     * <ul>
     *  <li>Modal context</li>
     *  <li>Layout direction (LTR/RTL)</li>
     * </ul>
     */
    processApplicationProperties: function() {
        EchoModalManager.modalElementId = EchoServerMessage.messageDocument.documentElement.getAttribute("modal-id");
        if (EchoServerMessage.messageDocument.documentElement.getAttribute("root-layout-direction")) {
            document.documentElement.style.direction =
                    EchoServerMessage.messageDocument.documentElement.getAttribute("root-layout-direction");
        }
    },
    
    /**
     * Processes all of the 'message-part' directives contained in the ServerMessage.
     */
    processMessageParts: function() {
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
            EchoServerMessage.status = complete ? EchoServerMessage.STATUS_PROCESSING_COMPLETE :
                    EchoServerMessage.STATUS_PROCESSING_FAILED;
        }
    },
    
    /**
     * Performs FIRST phase of synchronization response processing.
     * During this phase required JavaScript libraries are loaded.
     * Library loading is performed asynchronously, so a callback
     * interval is established to wait for the libraries to load
     * before the second phase of processing is invoked.
     */
    processPhase1: function() {
        try {
            EchoServerMessage.status = EchoServerMessage.STATUS_PROCESSING;
            EchoServerMessage.loadLibraries();
            if (EchoServerMessage.isLibraryLoadComplete()) {
                EchoServerMessage.processPhase2();
            } else {
                EchoServerMessage.backgroundIntervalId = window.setInterval("EchoServerMessage.waitForLibraries();", 20);
            }
        } catch (ex) {
            EchoServerMessage.temporaryProperties = null;
            EchoClientEngine.processClientError("Cannot process ServerMessage (Phase 1)", ex);
            throw ex;
        }
    },
    
    /**
     * Performs SECOND phase of synchronization response processing.
     * This phase may only be performed after all required JavaScript
     * libraries have been loaded.   During this phase, directives provided
     * in the ServerMessage are processed.  Additionally, the client modal 
     * state and asynchoronous serevr polling configurations are established.
     */
    processPhase2: function() {
        try {
            // Install retrieved libraries.
            EchoServerMessage.installLibraries();
            
            // Disable load status indicator (only relevant on initialization).
            EchoClientEngine.disableLoadStatus();
            
            // Process message parts contained in server message.
            EchoServerMessage.processMessageParts();
            
            EchoServerMessage.processApplicationProperties();
            if (EchoServerMessage.processingCompleteListener) {
                EchoServerMessage.processingCompleteListener();
            }
            EchoServerMessage.processAsyncConfig();
        } catch (ex) {
            EchoClientEngine.processClientError("Cannot process ServerMessage (Phase 2)", ex);
            throw ex;
        } finally {
            EchoServerMessage.temporaryProperties = null;
        }
        
        EchoVirtualPosition.redraw();
    
        ++EchoServerMessage.transactionCount;
        
        var completionTime = new Date().getTime();
        var interactionTime = completionTime - EchoServerTransaction.timer;
        EchoServerTransaction.timer = null;
        EchoDebugManager.consoleWrite("Interaction time: " + interactionTime + "ms");
    },
    
    /**
      Sets the value of a temporary property.
     * 
     * @param key the property key
     * @param value the property value
     */
    setTemporaryProperty: function(key, value) {
        if (!EchoServerMessage.temporaryProperties) {
            EchoServerMessage.temporaryProperties = { };
        }
        EchoServerMessage.temporaryProperties[key] = value;
    },
    
    /**
     * Executed at intervals (via setInterval) to determine if all libraries
     * have been loaded.  In the event that all libraries have been loaded,
     * the interval operation is cleared and message part processing is started.
     */
    waitForLibraries: function() {
        if (EchoServerMessage.isLibraryLoadComplete()) {
            window.clearInterval(EchoServerMessage.backgroundIntervalId);
            EchoServerMessage.backgroundIntervalId = null;
            EchoServerMessage.processPhase2();
        }
    }
};

// ____________________________
// Object EchoServerTransaction

/**
 * Static object/namespace to manage HTTP synchronization connections to 
 * server.
 */
EchoServerTransaction = {
    
    /**
     * Timer used for measuring performance.
     */
    timer: null,
    
    /**
     * Flag specifying whether a server transaction is active.
     * This flag should be queried by event handlers before
     * performing operations that are not compatible with
     * an open server transaction.
     */
    active: false,
    
    /**
     * Servlet query for synchronize service.
     */
    synchronizeServiceRequest: "?serviceId=Echo.Synchronize",
    
    /**
     * Initiates a client-server transaction my making a request to the server.
     * This operation is asynchronous; this method will return before the server
     * issues a response.
     * 
     * @return false if a connection is already in progress, true if connection was
     *         successfully initiated
     */
    connect: function() {
        if (EchoServerTransaction.active) {
            return false;
        }
        
        EchoServerDelayMessage.activate();
        EchoAsyncMonitor.stop();
        if (EchoClientProperties.get("quirkSafariUnescapedXHR")) {
            EchoDomUtil.fixSafariEscaping(EchoClientMessage.messageDocument);
        }
        var conn = new EchoHttpConnection(EchoClientEngine.baseServerUri + EchoServerTransaction.synchronizeServiceRequest, 
                "POST", EchoClientMessage.messageDocument, "text/xml");
        conn.responseHandler = EchoServerTransaction.responseHandler;
        conn.invalidResponseHandler = EchoServerTransaction.invalidResponseHandler;
        EchoServerTransaction.active = true;
        conn.connect();
        
        // Reset client message.
        EchoClientMessage.reset();
        
        return true;
    },
    
    /**
     * Processes an invalid HTTP response to the synchronize request.
     *
     * @param conn the EchoHttpConnection containing the response information. 
     */
    invalidResponseHandler: function(conn) {
        EchoServerTransaction.postProcess();
        if (conn.xmlHttpRequest.status == 500) {
            EchoClientEngine.processServerError();
        } else if (conn.xmlHttpRequest.status == 400) {
            EchoClientEngine.processSessionExpiration();
        } else {
            alert("Invalid/unknown response from server: " + conn.getResponseText());
        }
    },
    
    /**
     * Handles post processing cleanup tasks, i.e., disabling server message delay
     * pane and setting active flag state to false.
     */
    postProcess: function() {
        EchoServerDelayMessage.deactivate();
        EchoServerTransaction.active = false;
    },
    
    /**
     * Processes a (valid) HTTP response to the synchronize request.
     *
     * @param conn the EchoHttpConnection containing the response information.
     */
    responseHandler: function(conn) {
        EchoServerTransaction.timer = new Date().getTime();
        EchoServerMessage.init(conn.getResponseXml(), EchoServerTransaction.postProcess);
        EchoServerMessage.process();
    }
};    

// ____________________
// Object EchoStringUtil

/**
* Static object/namespace for performing string-operations that are not
* provided by the JavaScript specification.
*/
EchoStringUtil = {

    /**
     * Trims leading and trailing whitespace from a string.
     *
     * @param s the string to trim
    */
    trim: function(s) {
       var result = s.replace(/^\s+/g, "");  // strip leading
       return result.replace(/\s+$/g, "");   // strip trailing
    }
};

// __________________________
// Object EchoVirtualPosition

/**
 * Static object/namespace which provides cross-platform CSS positioning 
 * capabilities.  Internet Explorer 6 is ordinarily handicapped by its lack
 * of support for setting 'left' and 'right' or 'top' and 'bottom' positions
 * simultaneously on a single document element.
 *
 * To use the virtual positioning system, you must first register any elements
 * that have should be drawn using it.  To do this, invoke the register() method
 * with the id of the element. 
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
EchoVirtualPosition = {
    
    /** Array containing ids of elements registered with the virtual positioning system. */
    elementIdList: [],
    
    /** Set of element ids being redrawn using virtual positioning. */
    elementIdSet: new EchoCollectionsMap(),
    
    /** Flag indicating whether virtual positioning is required/enabled. */
    enabled: false,
    
    /** Flag indicating whether virtual positioning list is sorted); */
    elementIdListSorted: true,
    
    /** 
     * Adjusts the style.height and style.height attributes of an element to 
     * simulate its specified top, bottom, left, and right CSS position settings
     * The calculation makes allowances for padding, margin, and border width.
     *
     * @param element the element whose height setting is to be calculated
     *
     * This method is for internal use only by EchoVirtualPosition.
     */
    adjust: function(element) {
        var parentWidth, parentHeight, leftPixels, rightPixels, topPixels, bottomPixels,
            paddingPixels, marginPixels, borderPixels, calculatedHeight, calculatedWidth;
    
        // Adjust 'height' property if 'top' and 'bottom' properties are set, 
        // and if all padding/margin/borders are 0 or set in pixel units .
        if (EchoVirtualPosition.verifyPixelValue(element.style.top) &&
                EchoVirtualPosition.verifyPixelValue(element.style.bottom) &&
                EchoVirtualPosition.verifyPixelOrUndefinedValue(element.style.paddingTop) &&
                EchoVirtualPosition.verifyPixelOrUndefinedValue(element.style.paddingBottom) &&
                EchoVirtualPosition.verifyPixelOrUndefinedValue(element.style.marginTop) &&
                EchoVirtualPosition.verifyPixelOrUndefinedValue(element.style.marginBottom) &&
                EchoVirtualPosition.verifyPixelOrUndefinedValue(element.style.borderTopWidth) &&
                EchoVirtualPosition.verifyPixelOrUndefinedValue(element.style.borderBottomWidth)) {
            parentHeight = element.parentNode.offsetHeight;
            topPixels = parseInt(element.style.top, 10);
            bottomPixels = parseInt(element.style.bottom, 10);
            paddingPixels = EchoVirtualPosition.toInteger(element.style.paddingTop) + 
                    EchoVirtualPosition.toInteger(element.style.paddingBottom);
            marginPixels = EchoVirtualPosition.toInteger(element.style.marginTop) +
                    EchoVirtualPosition.toInteger(element.style.marginBottom);
            borderPixels = EchoVirtualPosition.toInteger(element.style.borderTopWidth) +
                    EchoVirtualPosition.toInteger(element.style.borderBottomWidth);
            calculatedHeight = parentHeight - topPixels - bottomPixels - paddingPixels - marginPixels - borderPixels;
            if (calculatedHeight <= 0) {
                element.style.height = 0;
            } else {
                if (element.style.height != calculatedHeight + "px") {
                    element.style.height = calculatedHeight + "px";
                }
            }
        }
        
        // Adjust 'width' property if 'left' and 'right' properties are set, 
        // and if all padding/margin/borders are 0 or set in pixel units .
        if (EchoVirtualPosition.verifyPixelValue(element.style.left) &&
                EchoVirtualPosition.verifyPixelValue(element.style.right) &&
                EchoVirtualPosition.verifyPixelOrUndefinedValue(element.style.paddingLeft) &&
                EchoVirtualPosition.verifyPixelOrUndefinedValue(element.style.paddingRight) &&
                EchoVirtualPosition.verifyPixelOrUndefinedValue(element.style.marginLeft) &&
                EchoVirtualPosition.verifyPixelOrUndefinedValue(element.style.marginRight) &&
                EchoVirtualPosition.verifyPixelOrUndefinedValue(element.style.borderLeftWidth) &&
                EchoVirtualPosition.verifyPixelOrUndefinedValue(element.style.borderRightWidth)) {
            parentWidth = element.parentNode.offsetWidth;
            leftPixels = parseInt(element.style.left, 10);
            rightPixels = parseInt(element.style.right, 10);
            paddingPixels = EchoVirtualPosition.toInteger(element.style.paddingLeft) + 
                    EchoVirtualPosition.toInteger(element.style.paddingRight);
            marginPixels = EchoVirtualPosition.toInteger(element.style.marginLeft) +
                    EchoVirtualPosition.toInteger(element.style.marginRight);
            borderPixels = EchoVirtualPosition.toInteger(element.style.borderLeftWidth) +
                    EchoVirtualPosition.toInteger(element.style.borderRightWidth);
            calculatedWidth = parentWidth - leftPixels - rightPixels - paddingPixels - marginPixels - borderPixels;
            if (calculatedWidth <= 0) {
                element.style.width = 0;
            } else {
                if (element.style.width != calculatedWidth + "px") {
                    element.style.width = calculatedWidth + "px";
                }
            }
        }
    },
    
    /**
     * Enables and initializes the virtual positioning system.
     */
    init: function() {
        EchoVirtualPosition.enabled = true;
        EchoDomUtil.addEventListener(window, "resize", EchoVirtualPosition.resizeListener, false);
    },
    
    /**
     * Redraws elements registered with the virtual positioning system.
     *
     * @param element (optional) the element to redraw; if unspecified, 
     *        all elements will be redrawn.
     */
    redraw: function(element) {
        if (!EchoVirtualPosition.enabled) {
            return;
        }
        
        var i, removedIds = false;
        
        if (element) {
            EchoVirtualPosition.adjust(element);
        } else {
            if (!EchoVirtualPosition.elementIdListSorted) {
                EchoVirtualPosition.sort();
            }
            
            for (i = 0; i < EchoVirtualPosition.elementIdList.length; ++i) {
                element = document.getElementById(EchoVirtualPosition.elementIdList[i]);
                if (element) {
                    EchoVirtualPosition.adjust(element);
                } else {
                    // Element no longer exists.  Replace id in elementIdList with null,
                    // and set 'removedIds' flag to true such that elementIdList will
                    // be pruned for nulls once redrawing has been completed.
                    EchoVirtualPosition.elementIdSet.remove(EchoVirtualPosition.elementIdList[i]);
                    EchoVirtualPosition.elementIdList[i] = null;
                    removedIds = true;
                }
            }
            
            // Prune removed ids from list if necessary.
            if (removedIds) {
                var updatedIdList = [];
                for (i = 0; i < EchoVirtualPosition.elementIdList.length; ++i) {
                    if (EchoVirtualPosition.elementIdList[i]) {
                        updatedIdList.push(EchoVirtualPosition.elementIdList[i]);
                    }
                }
                EchoVirtualPosition.elementIdList = updatedIdList;
            }
        }
    },
    
    /**
     * Registers an element to be drawn using the virtual positioning system.
     * The element must meet the following criteria:
     * <ul>
     *  <li>Margins and paddings, if set, must be set in pixel units.
     *  <li>Top, bottom, left, and right coordinates, if set, must be set in pixel 
     *   units.</li>
     * </ul>
     *
     * @param elementId the elementId to register
     */
    register: function(elementId) {
        if (!EchoVirtualPosition.enabled) {
            return;
        }
        EchoVirtualPosition.elementIdListSorted = false;
        EchoVirtualPosition.elementIdList.push(elementId);
        EchoVirtualPosition.elementIdSet.put(elementId, 1);
    },
    
    /**
     * Lisetener to receive "resize" events from containing browser window.
     * 
     * @param e the DOM2 resize event
     */
    resizeListener: function(e) {
        e = e ? e : window.event;
        EchoVirtualPosition.redraw();
    },
    
    /**
     * Sorts the array of virtually positioned element ids based on the order in 
     * which they appear top-to-bottom in the hierarchy.  This is necessary in order
     * that their positions will be adjusted starting at the highest level in the 
     * hierarchy.  This method delegates the real work to a recursive 
     * implementation.
     */
    sort: function() {
        var sortedList = [];
        EchoVirtualPosition.sortImpl(document.documentElement, sortedList); 
        EchoVirtualPosition.elementIdList = sortedList;
        EchoVirtualPosition.elementIdListSorted = true;
    },
    
    /**
     * Recursive work method to support <code>sort()</code>.
     * 
     * @param element the current element of the hierarchy being analyzed.
     * @param sortedList an array to which element ids will be appended in the
     *        orde rthe appear in the hierarchy
     */
    sortImpl: function(element, sortedList) {
        // If element has id and element is in set of virtually positioned elements
        if (element.id && EchoVirtualPosition.elementIdSet.get(element.id)) {
            sortedList.push(element.id);
        }
        
        for (var child = element.firstChild; child; child = child.nextSibling) {
            if (child.nodeType == 1) {
                EchoVirtualPosition.sortImpl(child, sortedList);
            }
        }
    },
    
    /**
     * Parses the specified value as an integer, returning 0 in the event the
     * specified value cannot be expressed as a number.
     *
     * @param value the value to parse, e.g., "20px"
     * @return the value as a integer, e.g., '20'
     */
    toInteger: function(value) {
        value = parseInt(value, 10);
        return isNaN(value) ? 0 : value;
    },
    
    /** 
     * Determines if the specified value contains a pixel dimension, e.g., "20px"
     * Returns false if the value is null/whitespace/undefined.
     *
     * @param value the value to evaluate
     * @return true if the value is a pixel dimension, false if it is not
     */
    verifyPixelValue: function(value) {
        if (value === null || value === "" || value === undefined) {
            return false;
        }
        var valueString = value.toString();
        return valueString == "0" || valueString.indexOf("px") != -1;
    },
    
    /** 
     * Determines if the specified value contains a pixel dimension, e.g., "20px"
     * Returns true if the value is null/whitespace/undefined.
     *
     * @param value the value to evaluate
     * @return true if the value is null or a pixel dimension, false if it is not
     */
    verifyPixelOrUndefinedValue: function(value) {
        if (value === null || value === "" || value === undefined) {
            return true;
        }
        var valueString = value.toString();
        return valueString == "0" || valueString.indexOf("px") != -1;
    },
    
    /**
     * Static object/namespace for EchoVirtualPosition MessageProcessor 
     * implementation.  Provides capability to register elements with
     * the Virtual Positioning System.
     */
    MessageProcessor: {

        /**
         * MessageProcessor process() implementation 
         * (invoked by ServerMessage processor).
         *
         * @param messagePartElement the <code>message-part</code> element to process
         */
        process: function(messagePartElement) {
            for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
                if (messagePartElement.childNodes[i].nodeType == 1) {
                    if (messagePartElement.childNodes[i].tagName == "register") {
                        EchoVirtualPosition.MessageProcessor.processRegister(messagePartElement.childNodes[i]);
                    }
                }
            }
        },
        
        /**
         * Processes a <code>register</code> message register an elmeent with the
         * <code>EchoVirtualPosition</code> system.
         *
         * @param registerElement the <code>register</code> element to process
         */
        processRegister: function(registerElement) {
            var items = registerElement.getElementsByTagName("item");
            for (var i = 0; i < items.length; ++i) {
                var elementId = items[i].getAttribute("eid");
                EchoVirtualPosition.register(elementId);
            }
        }
    }
};    

// _______________________
// Object EchoWindowUpdate

/**
 * MessageProcessor implementation to perform browser window-related updates,
 * such as setting the window title.
 */
EchoWindowUpdate = {
    
    /**
     * The DOM element to be focused (used by delayed focusing which is required on some browsers). 
     */
    focusPendingElement: null,
    
    delayFocus: function() {
        if (EchoWindowUpdate.focusPendingElement && EchoWindowUpdate.focusPendingElement.focus) {
            try {
                EchoWindowUpdate.focusPendingElement.focus();
            } catch (ex) { }
            EchoWindowUpdate.focusPendingElement = null;
        }
    },
    
    /**
     * MessageProcessor process() implementation.
     */
    process: function(messagePartElement) {
        for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
            if (messagePartElement.childNodes[i].nodeType == 1) {
                switch (messagePartElement.childNodes[i].tagName) {
                case "reload":
                    EchoWindowUpdate.processReload(messagePartElement.childNodes[i]);
                    break;
                case "set-focus":
                    EchoWindowUpdate.processSetFocus(messagePartElement.childNodes[i]);
                    break;
                case "set-title":
                    EchoWindowUpdate.processSetTitle(messagePartElement.childNodes[i]);
                    break;
                }
            }
        }
    },
    
    /**
     * Processes a server directive to reload the entire window
     * (due to an out-of-sync application state).
     * 
     * @param reloadElement the "reload" directive element
     */
    processReload: function(reloadElement) {
        var message = EchoClientConfiguration.propertyMap["defaultOutOfSyncErrorMessage"];
        if (message) {
            alert(message);
        }
        window.location.reload();
    },
    
    /**
     * Processes a server directive to set the focused component.
     *
     * @param setFocusElement the "set-focus" directive element
     */
    processSetFocus: function(setFocusElement) {
        EchoVirtualPosition.redraw();
        var elementId = setFocusElement.getAttribute("element-id");
        var element = document.getElementById(elementId);
        if (EchoClientProperties.get("quirkDelayedFocusRequired")) {
            this.focusPendingElement = element;
            window.setTimeout(EchoWindowUpdate.delayFocus, 0);
        } else {   
            if (element && element.focus) {
                element.focus();
            }
        }
    },
    
    /**
     * Processes a server directive to set the window title.
     *
     * @param setTitleElement the "set-title" directive element
     */
    processSetTitle: function(setTitleElement) {
        document.title = setTitleElement.getAttribute("title");
    }
};    

// _____________________
// Static Initialization

EchoClientMessage.reset();
