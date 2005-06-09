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
  
//BUGBUG. remove all defaultStyle stuff, no longer used.  
  
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
    var defaultStyle = initMessageElement.getAttribute("defaultstyle");
    var rolloverStyle = initMessageElement.getAttribute("rolloverstyle");
    var selectionStyle = initMessageElement.getAttribute("selectionstyle");

    for (var item = initMessageElement.firstChild; item; item = item.nextSibling) {
        var tableElementId = item.getAttribute("eid");
        
        EchoDomPropertyStore.setPropertyValue(tableElementId, "selectionMode", item.getAttribute("selectionMode"));
        
        if (defaultStyle) {
            EchoDomPropertyStore.setPropertyValue(tableElementId, "defaultStyle", defaultStyle);
        }
        if (rolloverStyle) {
            EchoDomPropertyStore.setPropertyValue(tableElementId, "rolloverStyle", rolloverStyle);
        }
        if (selectionStyle) {
            EchoDomPropertyStore.setPropertyValue(tableElementId, "selectionStyle", selectionStyle);
        }
        if (item.getAttribute("servernotify")) {
            EchoDomPropertyStore.setPropertyValue(tableElementId, "serverNotify", item.getAttribute("servernotify"));
        }
        
        EchoTable.initCellListeners(tableElementId);
    }
};

EchoTable.applyTemporaryStyle = function(element, cssStyle) {
    // Set original style if not already set.
    if (!EchoDomPropertyStore.getPropertyValue(element.id, "originalStyle")) {
        if (element.style.cssText) {
            EchoDomPropertyStore.setPropertyValue(element.id, "originalStyle", element.style.cssText);
        } else {
            EchoDomPropertyStore.setPropertyValue(element.id, "originalStyle", "-");
        }
    }
    EchoDomUtil.applyStyle(element, cssStyle);
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
            EchoTable.restoreOriginalStyle(trElement.cells[i]);
            EchoTable.applyTemporaryStyle(trElement.cells[i], selectionStyle);
        } else {
            EchoTable.restoreOriginalStyle(trElement.cells[i]);
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
    var trElement = sourceTdElement.parentNode;
    EchoTable.setSelected(trElement, !EchoTable.isSelected(trElement));
};

EchoTable.processRolloverEnter = function(echoEvent) {
    var sourceTdElement = echoEvent.registeredTarget;
    var trElement = sourceTdElement.parentNode;
    var tableElementId = EchoDomUtil.getComponentId(sourceTdElement.id);
    var rolloverStyle = EchoDomPropertyStore.getPropertyValue(tableElementId, "rolloverStyle");
    if (rolloverStyle) {
        for (var i = 0; i < trElement.cells.length; ++i) {
            EchoTable.applyTemporaryStyle(trElement.cells[i], rolloverStyle);
        }
    }
};

EchoTable.processRolloverExit = function(echoEvent) {
    var sourceTdElement = echoEvent.registeredTarget;
    var trElement = sourceTdElement.parentNode;
    EchoTable.drawRowStyle(trElement);
};

EchoTable.restoreOriginalStyle = function(element) {
    var originalStyle = EchoDomPropertyStore.getPropertyValue(element.id, "originalStyle");
    if (!originalStyle) {
        return;
    }
    element.style.cssText = originalStyle == "-" ? "" : originalStyle;
};

/**
 * Sets the selection state of a table row.
 *
 * @param trElement the row TR element
 * @param newValue the new selection state (a boolean value)
 */
EchoTable.setSelected = function(trElement, newValue) {
    var tableElement = document.getElementById(EchoDomUtil.getComponentId(trElement.id));

    // Set state flag.
    EchoDomPropertyStore.setPropertyValue(trElement.id, "selected", newValue ? "true" : "false");
    
    // Redraw.
    EchoTable.drawRowStyle(trElement);
    
    // Update ClientMessage.
    EchoTable.updateClientMessage(tableElement);
    
    // Notify server if required.
    if ("true" == EchoDomPropertyStore.getPropertyValue(tableElement.id, "serverNotify")) {
        EchoClientMessage.setActionValue(tableElement.id, "action");
        EchoServerTransaction.connect();
    }
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
            rowElement.setAttribute("id", tableElement.rows[i].id);
            propertyElement.appendChild(rowElement);
        }
    }

    EchoDebugManager.updateClientMessage();
};