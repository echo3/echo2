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
 * 
 * Creates a new ListComponent
 */
EchoListComponent = function(elementId, containerElementId) { 
    this.elementId = elementId;
    this.containerElementId = containerElementId;
    this.values = null;
    this.styles = null;
    this.selectedIndices = null;
    this.rolloverIndex = -1;
};

EchoListComponent.DHTML_SELECTION_STYLE = "background-color:#0a246a;color:#ffffff;";

EchoListComponent.prototype.create = function() {
    if (this.renderAsListBox) {
        this.renderAsDhtml = EchoClientProperties.get("quirkIESelectListDomUpdate") ? true : false;
        if (this.renderAsDhtml) {
            this.createDhtml();
        } else {
            this.createSelect();
        }
    } else {
        this.createSelect();
    }
    this.loadSelection();
};

EchoListComponent.prototype.createDhtml = function() {
    var containerElement = document.getElementById(this.containerElementId);
    
    this.selectElement = document.createElement("div");
    this.selectElement.id = this.elementId;
    this.selectElement.selectedIndex = -1;
    
    EchoDomUtil.setCssText(this.selectElement, this.style);
    
    if (this.toolTip) {
        this.selectElement.setAttribute("title", this.toolTip);
    }
    if (this.tabIndex) {
        this.selectElement.setAttribute("tabindex", this.tabIndex);
    }

    this.selectElement.style.cursor = "default";
    this.selectElement.style.overflow = "auto";
    
    if (!this.selectElement.style.height) {
        this.selectElement.style.height = "6em";
    }
    if (!this.selectElement.style.border) {
        this.selectElement.style.border = "1px inset #cfcfcf";
    }
    
    if (this.values) {
        for (var i = 0; i < this.values.length; ++i) {
            var optionElement = document.createElement("div");
            optionElement.setAttribute("id", this.elementId + "_item_" + i);
            optionElement.appendChild(document.createTextNode(this.values[i]));
            if (this.styles && this.styles[i]) {
                optionElement.setAttribute("style", this.styles[i]);
            }
            this.selectElement.appendChild(optionElement);
        }
    }

    containerElement.appendChild(this.selectElement);
    
    EchoEventProcessor.addHandler(this.selectElement, "click", "EchoListComponent.processClickDhtml");
    if (this.rolloverStyle) {
        EchoEventProcessor.addHandler(this.selectElement, "mouseover", "EchoListComponent.processRolloverEnter");
        EchoEventProcessor.addHandler(this.selectElement, "mouseout", "EchoListComponent.processRolloverExit");
    }
    if (EchoClientProperties.get("browserInternetExplorer")) {
        EchoEventProcessor.addHandler(this.selectElement, "selectstart", "EchoListComponent.processSelectStart");
    }
    
    EchoDomPropertyStore.setPropertyValue(this.selectElement, "component", this);
};

EchoListComponent.prototype.createSelect = function() {
    var containerElement = document.getElementById(this.containerElementId);
    
    this.selectElement = document.createElement("select");
    this.selectElement.id = this.elementId;
    
    if (!this.enabled) {
        this.selectElement.disabled = true;
    }

    EchoDomUtil.setCssText(this.selectElement, this.style);
    
    if (this.toolTip) {
        this.selectElement.setAttribute("title", this.toolTip);
    }
    if (this.tabIndex) {
        this.selectElement.setAttribute("tabindex", this.tabIndex);
    }
    
    if (this.renderAsListBox) {
        this.selectElement.setAttribute("size", "5");
        if (this.multipleSelection) {
            this.selectElement.setAttribute("multiple", "multiple");
        }
    }

    if (this.values) {
        for (var i = 0; i < this.values.length; ++i) {
            var optionElement = document.createElement("option");
            optionElement.setAttribute("id", this.elementId + "_item_" + i);
            optionElement.appendChild(document.createTextNode(this.values[i]));
            if (this.styles && this.styles[i]) {
                optionElement.setAttribute("style", this.styles[i]);
            }
            this.selectElement.appendChild(optionElement);
        }
    }
    
    containerElement.appendChild(this.selectElement);
    
    EchoEventProcessor.addHandler(this.selectElement, "change", "EchoListComponent.processSelection");
    if (this.rolloverStyle) {
        EchoEventProcessor.addHandler(this.selectElement, "mouseover", "EchoListComponent.processRolloverEnter");
        EchoEventProcessor.addHandler(this.selectElement, "mouseout", "EchoListComponent.processRolloverExit");
    }
    
    EchoDomPropertyStore.setPropertyValue(this.selectElement, "component", this);
};

/**
 * Disposes of an <code>EchoListComponent</code> instance, releasing resources
 * and unregistering event handlers.
 */
EchoListComponent.prototype.dispose = function() {
    this.values = null;
    this.styles = null;

    if (this.renderAsDhtml) {
        EchoEventProcessor.removeHandler(this.selectElement, "click");
        if (EchoClientProperties.get("browserInternetExplorer")) {
            EchoEventProcessor.removeHandler(this.selectElement, "selectstart");
        }
    } else {
        EchoEventProcessor.removeHandler(this.selectElement, "change");
    }

    if (this.rolloverStyle) {
        EchoEventProcessor.removeHandler(this.selectElement, "mouseover");
        EchoEventProcessor.removeHandler(this.selectElement, "mouseout");
    }
    
    EchoDomPropertyStore.dispose(this.selectElement);
    this.selectElement = null;
};

EchoListComponent.prototype.ensureSelectionVisibleDhtml = function() {
    //TODO finish
    if (!this.selectedIndices) {
        return;
    }
    var scrollTop = this.selectElement.scrollTop;
    var scrollBottom = scrollTop + this.selectElement.scrollHeight;
    for (var i = 0; i < this.selectedIndices.length; ++i) {
        var itemBounds = new EchoCssUtil.Bounds(this.selectElement.childNodes[i]);
    }
};

EchoListComponent.prototype.getNodeIndex = function(node) {
    var itemPrefix = this.elementId + "_item_";
    while (node != this.selectElement) {
        if (node.nodeType == 1) {
            var id = node.getAttribute("id");
            if (id && id.indexOf(itemPrefix) == 0) {
                return parseInt(id.substring(itemPrefix.length));
            }
        }
        node = node.parentNode;
    }
    return -1;
};

EchoListComponent.prototype.addNullOption = function() {
    if (EchoClientProperties.get("quirkSelectRequiresNullOption") 
            && !this.renderAsListBox && !this.nullOptionActive) {
        // Add null option.
        var nullOptionElement = document.createElement("option");
        if (this.selectElement.childNodes.length > 0) {
            this.selectElement.insertBefore(nullOptionElement, this.selectElement.options[0]);
        } else {
            this.selectElement.appendChild(nullOptionElement);
        }
        this.nullOptionActive = true;
    }
};

EchoListComponent.prototype.removeNullOption = function() {
    if (this.nullOptionActive) {
        // Remove null option.
        this.selectElement.removeChild(this.selectElement.options[0]);
        this.nullOptionActive = false;
    }
};

EchoListComponent.prototype.loadSelection = function() {
    if (this.renderAsDhtml) {
        this.loadSelectionDhtml();
    } else {
        this.selectElement.selectedIndex = -1;
        if (this.selectElement.options.length > 0) {
            this.selectElement.options[0].selected = false;
        }
        
        if (!this.selectedIndices || this.selectedIndices.length == 0) {
            this.addNullOption();
        } else {
            this.removeNullOption();
            var selectionSet;
            for (var i = 0; i < this.selectedIndices.length; ++i) {
                if (this.selectedIndices[i] < this.selectElement.options.length) {
                    selectionSet = true;
                    this.selectElement.options[this.selectedIndices[i]].selected = true;
                }
            }
            if (!selectionSet) {
                this.addNullOption();
            }
        }
    }
};

EchoListComponent.prototype.loadSelectionDhtml = function() {
    for (var i = 0; i < this.selectElement.childNodes.length; ++i) {
        if (this.styles && this.styles[i]) {
            EchoDomUtil.setCssText(this.selectElement.childNodes[i], this.styles[i]);
        } else {
            EchoDomUtil.setCssText(this.selectElement.childNodes[i], "");
        }
    }
    if (!this.selectedIndices) {
        return;
    }
    
    for (var i = 0; i < this.selectedIndices.length; ++i) {
        if (this.selectedIndices[i] < this.selectElement.childNodes.length) {
            EchoCssUtil.applyStyle(this.selectElement.childNodes[this.selectedIndices[i]], 
                    EchoListComponent.DHTML_SELECTION_STYLE);
        }
    }
};

EchoListComponent.prototype.processClickDhtml = function(echoEvent) {
    EchoDomUtil.preventEventDefault(echoEvent);
    if (!this.enabled || !EchoClientEngine.verifyInput(this.selectElement)) {
        return;
    }
    var index = this.getNodeIndex(echoEvent.target);
    if (index == -1) {
        return;
    }
    if (this.multipleSelection && echoEvent.ctrlKey && this.selectedIndices) {
        var oldState = false;
        for (var i = 0; i < this.selectedIndices.length; ++i) {
            if (this.selectedIndices[i] == index) {
                // Remove item from selection if found.
                this.selectedIndices[i] = this.selectedIndices.pop();
                oldState = true;
                break;
            }
        }
        if (!oldState) {
            // Item was not found in selection: add it to selection.
            this.selectedIndices.push(index);
        }
    } else {
        this.selectedIndices = new Array();
        this.selectedIndices.push(index);
    }
    this.loadSelectionDhtml();
    this.updateClientMessage();
};

EchoListComponent.prototype.processRolloverEnter = function(echoEvent) {
    EchoDomUtil.preventEventDefault(echoEvent);
    if (!this.enabled || !EchoClientEngine.verifyInput(this.selectElement)) {
        return;
    }
    var index = this.getNodeIndex(echoEvent.target);
    if (index == -1) {
        return;
    }
    this.setRolloverIndex(index);
};

EchoListComponent.prototype.processRolloverExit = function(echoEvent) {
    EchoDomUtil.preventEventDefault(echoEvent);
    if (!this.enabled || !EchoClientEngine.verifyInput(this.selectElement)) {
        return;
    }
    var index = this.getNodeIndex(echoEvent.target);
    if (index == -1) {
        return;
    }
    this.setRolloverIndex(-1);
};

/**
 * Processes an item selection (change) event.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoListComponent.prototype.processSelection = function(echoEvent) {    
    if (!this.enabled || !EchoClientEngine.verifyInput(this.selectElement)) {
        this.loadSelection();
        return;
    }
    this.removeNullOption();
    this.storeSelection();
    this.updateClientMessage();
};

EchoListComponent.prototype.setRolloverIndex = function(rolloverIndex) {
    if (this.rolloverIndex != -1) {
        var oldElement;
        var oldStyle = null;
        if (this.renderAsDhtml) {
            oldElement = this.selectElement.childNodes[this.rolloverIndex];
            // Determine if element was selected and if so load selection style as "old style".
            if (this.selectedIndices) {
                for (var i = 0; i < this.selectedIndices.length; ++i) {
                    if (this.selectedIndices[i] == this.rolloverIndex) {
                        oldStyle = EchoListComponent.DHTML_SELECTION_STYLE;
                        break;
                    }
                }
            }
        } else {
            oldElement = this.selectElement.options[this.rolloverIndex];
        }
        if (!oldStyle) {
            oldStyle = this.styles ? this.styles[this.rolloverIndex] : "";
        }
        EchoDomUtil.setCssText(oldElement, oldStyle);
    }
    this.rolloverIndex = rolloverIndex;
    if (this.rolloverIndex != -1) {
        var newElement = this.renderAsDhtml 
                ? this.selectElement.childNodes[this.rolloverIndex] : this.selectElement.options[this.rolloverIndex];
        EchoCssUtil.applyStyle(newElement, this.rolloverStyle);
    }
};

EchoListComponent.prototype.storeSelection = function() {
    this.selectedIndices = new Array();
    for (var i = 0; i < this.selectElement.options.length; ++i) {
        if (this.selectElement.options[i].selected) {
            this.selectedIndices.push(i);
        }
    }
};

/**
 * Updates the selection state in the outgoing <code>ClientMessage</code>.
 * If any server-side <code>ActionListener</code>s are registered, an action
 * will be set in the ClientMessage and a client-server connection initiated.
 */
EchoListComponent.prototype.updateClientMessage = function() {
    var propertyElement = EchoClientMessage.createPropertyElement(this.elementId, "selection");

    // remove previous values
    while(propertyElement.hasChildNodes()){
        propertyElement.removeChild(propertyElement.firstChild);
    }

    if (!this.selectedIndices) {
        return;
    }
    
    // add new values        
    for (var i = 0; i < this.selectedIndices.length; ++i) {
        var clientMessageItemElement = EchoClientMessage.messageDocument.createElement("item");
        clientMessageItemElement.setAttribute("index", this.selectedIndices[i]);
        propertyElement.appendChild(clientMessageItemElement);
    }

    EchoDebugManager.updateClientMessage();
    
    if (this.serverNotify) {
        EchoClientMessage.setActionValue(this.elementId, "action");
        EchoServerTransaction.connect();
    }
};

/**
 * Returns the ListComponent data object instance based on the root element
 * of the ListComponent.
 *
 * @param element the root element or element id of the ListComponent
 * @return the relevant ListComponent instance
 */
EchoListComponent.getComponent = function(element) {
    return EchoDomPropertyStore.getPropertyValue(element, "component");
};

EchoListComponent.processClickDhtml = function(echoEvent) {
    var componentId = EchoDomUtil.getComponentId(echoEvent.registeredTarget.id);
    var listComponent = EchoListComponent.getComponent(componentId);
    listComponent.processClickDhtml(echoEvent);
};

EchoListComponent.processRolloverEnter = function(echoEvent) {
    var componentId = EchoDomUtil.getComponentId(echoEvent.registeredTarget.id);
    var listComponent = EchoListComponent.getComponent(componentId);
    listComponent.processRolloverEnter(echoEvent);
};

EchoListComponent.processRolloverExit = function(echoEvent) {
    var componentId = EchoDomUtil.getComponentId(echoEvent.registeredTarget.id);
    var listComponent = EchoListComponent.getComponent(componentId);
    listComponent.processRolloverExit(echoEvent);
};

EchoListComponent.processSelection = function(echoEvent) {
    var componentId = EchoDomUtil.getComponentId(echoEvent.registeredTarget.id);
    var listComponent = EchoListComponent.getComponent(componentId);
    listComponent.processSelection(echoEvent);
};

EchoListComponent.processSelectStart = function(e) {
    EchoDomUtil.preventEventDefault(e);
};

/**
 * Static object/namespace for SelectField/ListBox MessageProcessor 
 * implementation.
 */
EchoListComponent.MessageProcessor = function() { };

/**
 * MessageProcessor process() implementation 
 * (invoked by ServerMessage processor).
 *
 * @param messagePartElement the <code>message-part</code> element to process.
 */
EchoListComponent.MessageProcessor.process = function(messagePartElement) {
    for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
        if (messagePartElement.childNodes[i].nodeType == 1) {
            switch (messagePartElement.childNodes[i].tagName) {
            case "dispose":
                EchoListComponent.MessageProcessor.processDispose(messagePartElement.childNodes[i]);
                break;
            case "load-content":
                EchoListComponent.MessageProcessor.processLoadContent(messagePartElement.childNodes[i]);
                break;
            case "init":
                EchoListComponent.MessageProcessor.processInit(messagePartElement.childNodes[i]);
                break;
            }
        }
    }
};

/**
 * Processes a <code>dispose</code> message to finalize the state of a
 * selection component that is being removed.
 *
 * @param disposeElement the <code>dispose</code> element to process
 */
EchoListComponent.MessageProcessor.processDispose = function(disposeElement) {
    var elementId = disposeElement.getAttribute("eid");
    var listComponent = EchoListComponent.getComponent(elementId);
    if (listComponent) {
        listComponent.dispose();
    }
};

/**
 * Processes an <code>init</code> message to initialize the state of a 
 * selection component that is being added.
 *
 * @param initElement the <code>init</code> element to process
 */
EchoListComponent.MessageProcessor.processInit = function(initElement) {
    var elementId = initElement.getAttribute("eid");
    var containerElementId = initElement.getAttribute("container-eid");

    var listComponent = new EchoListComponent(elementId, containerElementId);
    listComponent.enabled = initElement.getAttribute("enabled") != "false";

    listComponent.serverNotify = initElement.getAttribute("server-notify") == "true";

    listComponent.style = initElement.getAttribute("style");
    listComponent.tabIndex = initElement.getAttribute("tab-index");
    listComponent.toolTip = initElement.getAttribute("tool-tip");
    
    var contentId = initElement.getAttribute("content-id");
    listComponent.values = EchoServerMessage.getTemporaryProperty("EchoListComponent.Values." + contentId);
    listComponent.styles = EchoServerMessage.getTemporaryProperty("EchoListComponent.Styles." + contentId);
    
    if (initElement.getAttribute("type") == "list-box") {
        listComponent.rolloverStyle = initElement.getAttribute("rollover-style");
        listComponent.renderAsListBox = true;
        if (initElement.getAttribute("multiple") == "true") {
            listComponent.multipleSelection = true;
        }
    }

    // Retrieve selection information.
    listComponent.selectedIndices = new Array();
    if (listComponent.multipleSelection) {
        var selectionElements = initElement.getElementsByTagName("selection");
        if (selectionElements.length == 1) {
            var itemElements = selectionElements[0].getElementsByTagName("item");
            for (var i = 0; i < itemElements.length; ++i) {
                listComponent.selectedIndices.push(parseInt(itemElements[i].getAttribute("index")));
            }
        }
    } else {
        var selectedIndex = parseInt(initElement.getAttribute("selection-index"));
        if (!isNaN(selectedIndex)) {
            listComponent.selectedIndices.push(selectedIndex);
        }
    }

    listComponent.create();
};

/**
 * Processes a <code>load-content</code> message to store model/content
 * information in the EchoServerMessage's temporary property store.
 * These model values will be later used during processInit() 
 * (possibly for multiple individual list components).
 *
 * @param loadContentElement the <code>loadContent</code> element to process
 */
EchoListComponent.MessageProcessor.processLoadContent = function(loadContentElement) {
    var contentId = loadContentElement.getAttribute("content-id");
    
    var valueArray = new Array();
    for (var item = loadContentElement.firstChild; item; item = item.nextSibling) {
        var value = item.getAttribute("value");
        valueArray.push(value);
    }
    EchoServerMessage.setTemporaryProperty("EchoListComponent.Values." + contentId, valueArray);

    if (loadContentElement.getAttribute("styled") == "true") {
        var styleArray = new Array();
        for (var item = loadContentElement.firstChild; item; item = item.nextSibling) {
            var value = item.getAttribute("style");
            styleArray.push(value);
        }
        EchoServerMessage.setTemporaryProperty("EchoListComponent.Styles." + contentId, styleArray);
    }
};
