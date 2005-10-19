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
  
//_________________
// Object EchoTable

/**
 * Static object/namespace for Table support.
 * This object/namespace should not be used externally.
 */
EchoTable = function() { };

/**
 * Static object/namespace for Table MessageProcessor 
 * implementation.
 */
EchoTable.MessageProcessor = function() { };

/**
 * MessageProcessor process() implementation 
 * (invoked by ServerMessage processor).
 *
 * @param messagePartElement the <code>message-part</code> element to process.
 */
EchoTable.MessageProcessor.process = function(messagePartElement) {
    for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
        if (messagePartElement.childNodes[i].nodeType == 1) {
            switch (messagePartElement.childNodes[i].tagName) {
            case "init":
                EchoTable.MessageProcessor.processInit(messagePartElement.childNodes[i]);
                break;
            case "dispose":
                EchoTable.MessageProcessor.processDispose(messagePartElement.childNodes[i]);
                break;
            }
        }
    }
};

/**
 * Processes a <code>dispose</code> message to finalize the state of a
 * Table component that is being removed.
 *
 * @param disposeMessageElement the <code>dispose</code> element to process
 */
EchoTable.MessageProcessor.processDispose = function(disposeMessageElement) {
    for (var item = disposeMessageElement.firstChild; item; item = item.nextSibling) {
        var tableElementId = item.getAttribute("eid");
        EchoTable.disposeCellListeners(tableElementId);
    }
};

/**
 * Processes an <code>init</code> message to initialize the state of a 
 * Table component that is being added.
 *
 * @param initMessageElement the <code>init</code> element to process
 */
EchoTable.MessageProcessor.processInit = function(initMessageElement) {
    var rolloverStyle = initMessageElement.getAttribute("rollover-style");
    var selectionStyle = initMessageElement.getAttribute("selection-style");

    for (var item = initMessageElement.firstChild; item; item = item.nextSibling) {
        var tableElementId = item.getAttribute("eid");
        var selectionEnabled = item.getAttribute("selection-enabled") == "true";
        if (selectionEnabled) {
            EchoDomPropertyStore.setPropertyValue(tableElementId, "selectionStyle", selectionStyle);
            EchoDomPropertyStore.setPropertyValue(tableElementId, "selectionEnabled", "true");
            EchoDomPropertyStore.setPropertyValue(tableElementId, "selectionMode", item.getAttribute("selection-mode"));
            if (item.getAttribute("server-notify")) {
                EchoDomPropertyStore.setPropertyValue(tableElementId, "serverNotify", item.getAttribute("server-notify"));
            }
        }

        if (rolloverStyle) {
            EchoDomPropertyStore.setPropertyValue(tableElementId, "rolloverStyle", rolloverStyle);
        }
        
        var rowElements = item.getElementsByTagName("row");
        for (var rowIndex = 0; rowIndex < rowElements.length; ++rowIndex) {
            var trElement = document.getElementById(tableElementId + "_tr_" + rowElements[rowIndex].getAttribute("index"));
            EchoTable.setSelected(trElement, true);
        }
        
        if (item.getAttribute("enabled") == "false") {
            EchoDomPropertyStore.setPropertyValue(tableElementId, "EchoClientEngine.inputDisabled", true);
        }

        if (selectionEnabled || rolloverStyle) {
            // Enable listeners only if table features selection or rollover effects.
            EchoTable.initCellListeners(tableElementId);
        }
    }
};

/**
 * Deselects all selected rows in a Table.
 *
 * @param tableElement the Table element
 */
EchoTable.clearSelected = function(tableElement) {
    for (var i = 0; i < tableElement.rows.length; ++i) {
        if (EchoTable.isSelected(tableElement.rows[i])) {
            EchoTable.setSelected(tableElement.rows[i], false);
        }
    }
};

/**
 * Removes rollover/selection listeners from a Table row.
 *
 * @param tableElementId the id of the Table element
 */
EchoTable.disposeCellListeners = function(tableElementId) {
    var tableElement = document.getElementById(tableElementId);
    if (!tableElement) {
        return;
    }
    for (var rowIndex = 0; rowIndex < tableElement.rows.length; ++rowIndex) {
        var trElement = tableElement.rows[rowIndex];
        EchoEventProcessor.removeHandler(trElement.id, "click");
        EchoEventProcessor.removeHandler(trElement.id, "mouseover");
        EchoEventProcessor.removeHandler(trElement.id, "mouseout");
    }
};

/**
 * Redraws a row in the appropriate style (i.e., selected or deselected).
 *
 * @param trElement the row <code>tr</code> element to redraw
 */
EchoTable.drawRowStyle = function(trElement) {
    var selected = EchoTable.isSelected(trElement);
    var tableElementId = EchoDomUtil.getComponentId(trElement.id);
    var selectionStyle = EchoDomPropertyStore.getPropertyValue(tableElementId, "selectionStyle");

    for (var i = 0; i < trElement.cells.length; ++i) {
        if (selected) {
            EchoCssUtil.restoreOriginalStyle(trElement.cells[i]);
            EchoCssUtil.applyTemporaryStyle(trElement.cells[i], selectionStyle);
        } else {
            EchoCssUtil.restoreOriginalStyle(trElement.cells[i]);
        }
    }
};

/**
 * Adds rollover/selection listeners to a Table row.
 *
 * @param tableElementId the id of the Table element
 */
EchoTable.initCellListeners = function(tableElementId) {
    var tableElement = document.getElementById(tableElementId);
    for (var rowIndex = 0; rowIndex < tableElement.rows.length; ++rowIndex) {
        var trElement = tableElement.rows[rowIndex];
        EchoEventProcessor.addHandler(trElement.id, "click", "EchoTable.processClick");
        EchoEventProcessor.addHandler(trElement.id, "mouseover", "EchoTable.processRolloverEnter");
        EchoEventProcessor.addHandler(trElement.id, "mouseout", "EchoTable.processRolloverExit");
    }
};

/**
 * Determines the selection state of a table row.
 *
 * @param trElement the row TR element
 * @return the selection state (as a boolean value)
 */
EchoTable.isSelected = function(trElement) {
    return EchoDomPropertyStore.getPropertyValue(trElement.id, "selected") == "true";
};

/**
 * Processes a row selection (click) event.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoTable.processClick = function(echoEvent) {
    var trElement = echoEvent.registeredTarget;
    var tableElementId = EchoDomUtil.getComponentId(trElement.id);
    if (!EchoClientEngine.verifyInput(tableElementId)) {
        return;
    }

    if (trElement.id.indexOf("_header") != -1) {
        return;
    }

    if (EchoDomPropertyStore.getPropertyValue(tableElementId, "selectionEnabled") != "true") {
        return;
    }
    
    EchoDomUtil.preventEventDefault(echoEvent);

    var tableElement = document.getElementById(tableElementId);
    if (EchoDomPropertyStore.getPropertyValue(tableElementId, "selectionMode") != "multiple") {
        EchoTable.clearSelected(tableElement);
    }

    EchoTable.setSelected(trElement, !EchoTable.isSelected(trElement));
    
    // Update ClientMessage.
    EchoTable.updateClientMessage(tableElement);
    
    // Notify server if required.
    if ("true" == EchoDomPropertyStore.getPropertyValue(tableElement.id, "serverNotify")) {
        EchoClientMessage.setActionValue(tableElement.id, "action");
        EchoServerTransaction.connect();
    }
};

/**
 * Processes a row mouse over event.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoTable.processRolloverEnter = function(echoEvent) {
    var trElement = echoEvent.registeredTarget;
    var tableElementId = EchoDomUtil.getComponentId(trElement.id);
    if (!EchoClientEngine.verifyInput(tableElementId)) {
        return;
    }
    
    if (trElement.id.indexOf("_header") != -1) {
        return;
    }
    
    var rolloverStyle = EchoDomPropertyStore.getPropertyValue(tableElementId, "rolloverStyle");
    if (rolloverStyle) {
        for (var i = 0; i < trElement.cells.length; ++i) {
            EchoCssUtil.applyTemporaryStyle(trElement.cells[i], rolloverStyle);
        }
    }
};

/**
 * Processes a row mouse out event.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoTable.processRolloverExit = function(echoEvent) {
    var trElement = echoEvent.registeredTarget;
    var tableElementId = EchoDomUtil.getComponentId(trElement.id);
    if (!EchoClientEngine.verifyInput(tableElementId)) {
        return;
    }

    if (trElement.id.indexOf("_header") != -1) {
        return;
    }

    EchoTable.drawRowStyle(trElement);
};

/**
 * Sets the selection state of a table row.
 *
 * @param trElement the row TR element
 * @param newValue the new selection state (a boolean value)
 */
EchoTable.setSelected = function(trElement, newValue) {
    // Set state flag.
    EchoDomPropertyStore.setPropertyValue(trElement.id, "selected", newValue ? "true" : "false");
    
    // Redraw.
    EchoTable.drawRowStyle(trElement);
};

/**
 * Updates the selection state in the outgoing <code>ClientMessage</code>.
 * If any server-side <code>ActionListener</code>s are registered, an action
 * will be set in the ClientMessage and a client-server connection initiated.
 *
 * @param tableElement the table element whose state is to be updated
 */
EchoTable.updateClientMessage = function(tableElement) {
    var propertyElement = EchoClientMessage.createPropertyElement(tableElement.id, "selection");

    // remove previous values
    while(propertyElement.hasChildNodes()){
        propertyElement.removeChild(propertyElement.firstChild);
    }
    
    var hasHeaderRow = tableElement.rows.length > 0 && tableElement.rows[0].id.indexOf("_head") != -1;
    
    for (var i = 0; i < tableElement.rows.length; ++i) {
        if (EchoTable.isSelected(tableElement.rows[i])) {
            var rowElement = EchoClientMessage.messageDocument.createElement("row");
            rowElement.setAttribute("index", hasHeaderRow ? i - 1 : i);
            propertyElement.appendChild(rowElement);
        }
    }

    EchoDebugManager.updateClientMessage();
};
