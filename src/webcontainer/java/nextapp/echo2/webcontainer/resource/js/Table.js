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

EchoTable = function() { };

/**
 * ServerMessage processor.
 */
EchoTable.MessageProcessor = function() { };

/**
 * ServerMessage process() implementation.
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

EchoTable.MessageProcessor.processDispose = function(disposeMessageElement) {
    for (var item = disposeMessageElement.firstChild; item; item = item.nextSibling) {
        var tableElementId = item.getAttribute("eid");
        
        EchoTable.disposeCellListeners(tableElementId);
    }
};

EchoTable.MessageProcessor.processInit = function(initMessageElement) {
    var rolloverStyle = initMessageElement.getAttribute("rollover-style");
    var selectionStyle = initMessageElement.getAttribute("selection-style");

    for (var item = initMessageElement.firstChild; item; item = item.nextSibling) {
        var tableElementId = item.getAttribute("eid");
        var selectionEnabled = item.getAttribute("selection-enabled") == "true";

        if (selectionEnabled) {
            EchoDomPropertyStore.setPropertyValue(tableElementId, "selectionStyle", selectionStyle);
            EchoDomPropertyStore.setPropertyValue(tableElementId, "selectionEnabled", "true");
            EchoDomPropertyStore.setPropertyValue(tableElementId, "selectionMode", item.getAttribute("selectionmode"));
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
        
        EchoTable.initCellListeners(tableElementId);
    }
};

EchoTable.clearSelected = function(tableElement) {
    for (var i = 0; i < tableElement.rows.length; ++i) {
        if (EchoTable.isSelected(tableElement.rows[i])) {
            EchoTable.setSelected(tableElement.rows[i], false);
        }
    }
};

EchoTable.disposeCellListeners = function(tableElementId) {
    var tableElement = document.getElementById(tableElementId);
    for (var rowIndex = 0; rowIndex < tableElement.rows.length; ++rowIndex) {
        for (var cellIndex = 0; cellIndex < tableElement.rows[rowIndex].cells.length; ++cellIndex) {
            var tdElement = tableElement.rows[rowIndex].cells[cellIndex];
            EchoEventProcessor.removeHandler(tdElement.id, "click");
            EchoEventProcessor.removeHandler(tdElement.id, "mouseover");
            EchoEventProcessor.removeHandler(tdElement.id, "mouseout");
        }
    }
};

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

EchoTable.initCellListeners = function(tableElementId) {
    var tableElement = document.getElementById(tableElementId);
    for (var rowIndex = 0; rowIndex < tableElement.rows.length; ++rowIndex) {
        for (var cellIndex = 0; cellIndex < tableElement.rows[rowIndex].cells.length; ++cellIndex) {
            var tdElement = tableElement.rows[rowIndex].cells[cellIndex];
            EchoEventProcessor.addHandler(tdElement.id, "click", "EchoTable.processClick");
            EchoEventProcessor.addHandler(tdElement.id, "mouseover", "EchoTable.processRolloverEnter");
            EchoEventProcessor.addHandler(tdElement.id, "mouseout", "EchoTable.processRolloverExit");
        }
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

EchoTable.processClick = function(echoEvent) {
    var sourceTdElement = echoEvent.registeredTarget;
    if (!EchoClientEngine.verifyInput(sourceTdElement)) {
        return;
    }

    var trElement = sourceTdElement.parentNode;
    if (trElement.id.indexOf("_header") != -1) {
        return;
    }

    var tableElement = document.getElementById(EchoDomUtil.getComponentId(trElement.id));
    
    if (EchoDomPropertyStore.getPropertyValue(tableElement.id, "selectionEnabled") != "true") {
        return;
    }
    
    EchoDomUtil.preventEventDefault(echoEvent);

    if (EchoDomPropertyStore.getPropertyValue(tableElement.id, "selectionMode") != "multiple") {
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

EchoTable.processRolloverEnter = function(echoEvent) {
    var sourceTdElement = echoEvent.registeredTarget;
    if (!EchoClientEngine.verifyInput(sourceTdElement)) {
        return;
    }
    
    var trElement = sourceTdElement.parentNode;
    if (trElement.id.indexOf("_header") != -1) {
        return;
    }
    
    var tableElementId = EchoDomUtil.getComponentId(sourceTdElement.id);
    var rolloverStyle = EchoDomPropertyStore.getPropertyValue(tableElementId, "rolloverStyle");
    if (rolloverStyle) {
        for (var i = 0; i < trElement.cells.length; ++i) {
            EchoCssUtil.applyTemporaryStyle(trElement.cells[i], rolloverStyle);
        }
    }
};

EchoTable.processRolloverExit = function(echoEvent) {
    var sourceTdElement = echoEvent.registeredTarget;
    if (!EchoClientEngine.verifyInput(sourceTdElement)) {
        return;
    }

    var trElement = sourceTdElement.parentNode;
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

EchoTable.updateClientMessage = function(tableElement) {
    var propertyElement = EchoClientMessage.createPropertyElement(tableElement.id, "selection");

    // remove previous values
    while(propertyElement.hasChildNodes()){
        propertyElement.removeChild(propertyElement.firstChild);
    }
    
    for (var i = 0; i < tableElement.rows.length; ++i) {
        if (EchoTable.isSelected(tableElement.rows[i])) {
            var rowElement = EchoClientMessage.messageDocument.createElement("row");
            rowElement.setAttribute("index", i - 1);
            propertyElement.appendChild(rowElement);
        }
    }

    EchoDebugManager.updateClientMessage();
};