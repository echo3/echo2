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
    var defaultStyle = initMessageElement.getAttribute("defaultstyle");
    var rolloverStyle = initMessageElement.getAttribute("rolloverstyle");
    var selectionStyle = initMessageElement.getAttribute("selectionstyle");

    for (var item = initMessageElement.firstChild; item; item = item.nextSibling) {
        var tableElementId = item.getAttribute("eid");

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

EchoTable.disposeCellListeners = function(tableElementId) {
    var tableElement = document.getElementById(tableElementId);
    for (var rowIndex = 0; rowIndex < tableElement.rows.length; ++rowIndex) {
        for (var cellIndex = 0; cellIndex < tableElement.rows[rowIndex].cells.length; ++cellIndex) {
            var tdElement = tableElement.rows[rowIndex].cells[cellIndex];
            EchoEventProcessor.removeHandler(tdElement.id, "mouseover");
            EchoEventProcessor.removeHandler(tdElement.id, "mouseout");
        }
    }
};

EchoTable.initCellListeners = function(tableElementId) {
    var tableElement = document.getElementById(tableElementId);
    for (var rowIndex = 0; rowIndex < tableElement.rows.length; ++rowIndex) {
        for (var cellIndex = 0; cellIndex < tableElement.rows[rowIndex].cells.length; ++cellIndex) {
            var tdElement = tableElement.rows[rowIndex].cells[cellIndex];
            EchoEventProcessor.addHandler(tdElement.id, "mouseover", "EchoTable.processRolloverEnter");
            EchoEventProcessor.addHandler(tdElement.id, "mouseout", "EchoTable.processRolloverExit");
        }
    }
};

EchoTable.processRolloverEnter = function(echoEvent) {
    var tdElement = echoEvent.registeredTarget;
    var trElement = tdElement.parentNode;
    EchoDebugManager.consoleWrite("rolloverEnter:" + trElement.id);
};

EchoTable.processRolloverExit = function(echoEvent) {
    var tdElement = echoEvent.registeredTarget;
    var trElement = tdElement.parentNode;
    EchoDebugManager.consoleWrite("rolloverExit:" + trElement.id);
};