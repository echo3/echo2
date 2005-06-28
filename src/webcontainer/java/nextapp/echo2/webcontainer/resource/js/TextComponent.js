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
// Object EchoTextComponent

EchoTextComponent = function() { };

EchoTextComponent.MessageProcessor = function() { };

EchoTextComponent.MessageProcessor.process = function(messagePartElement) {
    for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
        if (messagePartElement.childNodes[i].nodeType == 1) {
            switch (messagePartElement.childNodes[i].tagName) {
            case "init":
                EchoTextComponent.MessageProcessor.processInit(messagePartElement.childNodes[i]);
                break;
            case "dispose":
                EchoTextComponent.MessageProcessor.processDispose(messagePartElement.childNodes[i]);
                break;
            }
        }
    }
};

EchoTextComponent.MessageProcessor.processDispose = function(disposeMessageElement) {
    for (var item = disposeMessageElement.firstChild; item; item = item.nextSibling) {
        var elementId = item.getAttribute("eid");
        EchoEventProcessor.removeHandler(elementId, "blur");
        EchoEventProcessor.removeHandler(elementId, "focus");
        EchoEventProcessor.removeHandler(elementId, "keypress");
        EchoEventProcessor.removeHandler(elementId, "keyup");
    }
};

EchoTextComponent.MessageProcessor.processInit = function(initMessageElement) {
    for (var item = initMessageElement.firstChild; item; item = item.nextSibling) {
        var elementId = item.getAttribute("eid");
        var textComponent = document.getElementById(elementId);
        
        if (item.getAttribute("enabled") == "false") {
            textComponent.readOnly = true;
            EchoDomPropertyStore.setPropertyValue(textComponent.id, "EchoClientEngine.inputDisabled", true);
        }
	    if (item.getAttribute("text")) {
            textComponent.value = item.getAttribute("text");
	    }
        if (item.getAttribute("server-notify")) {
            EchoDomPropertyStore.setPropertyValue(textComponent.id, "serverNotify", true);
        }
        if (item.getAttribute("horizontal-scroll")) {
            textComponent.scrollLeft = parseInt(item.getAttribute("horizontal-scroll"));
        }
        if (item.getAttribute("vertical-scroll")) {
            if (EchoClientProperties.get("quirkIERepaint")) {
	            var originalWidth = textComponent.style.width;
	            var temporaryWidth = parseInt(textComponent.clientWidth) - 1;
	            textComponent.style.width = temporaryWidth + "px";
	            textComponent.style.width = originalWidth;
            }
            textComponent.scrollTop = parseInt(item.getAttribute("vertical-scroll"));
        }
        EchoEventProcessor.addHandler(elementId, "blur", "EchoTextComponent.processBlur");
        EchoEventProcessor.addHandler(elementId, "focus", "EchoTextComponent.processFocus");
        EchoEventProcessor.addHandler(elementId, "keyup", "EchoTextComponent.processKeyUp");
        EchoEventProcessor.addHandler(elementId, "keypress", "EchoTextComponent.processKeyPress");
    }
};

EchoTextComponent.doAction = function(textComponent) {
    if (!EchoDomPropertyStore.getPropertyValue(textComponent.id, "serverNotify")) {
        return;
    }
    EchoClientMessage.setActionValue(textComponent.id, "action");
    EchoServerTransaction.connect();
};

EchoTextComponent.doUpdate = function(textComponent) {
    var textPropertyElement = EchoClientMessage.createPropertyElement(textComponent.id, "text");
    if (textPropertyElement.firstChild) {
        textPropertyElement.firstChild.nodeValue = textComponent.value;
    } else {
        textPropertyElement.appendChild(EchoClientMessage.messageDocument.createTextNode(textComponent.value));
    }
    
    EchoClientMessage.setPropertyValue(textComponent.id, "horizontalScroll", textComponent.scrollLeft);
    EchoClientMessage.setPropertyValue(textComponent.id, "verticalScroll", textComponent.scrollTop);
};

EchoTextComponent.processBlur = function(echoEvent) {
    var textComponent = echoEvent.registeredTarget;
    if (!EchoClientEngine.verifyInput(textComponent.id)) {
        return;
    }
    EchoTextComponent.doUpdate(textComponent);
    EchoFocusManager.setFocusedState(textComponent.id, false);
};

EchoTextComponent.processFocus = function(echoEvent) {
    var textComponent = echoEvent.registeredTarget;
    if (!EchoClientEngine.verifyInput(textComponent.id)) {
        return;
    }
    EchoFocusManager.setFocusedState(textComponent.id, true);
};

EchoTextComponent.processKeyPress = function(echoEvent) {
    var textComponent = echoEvent.registeredTarget;
    if (!EchoClientEngine.verifyInput(textComponent.id)) {
        EchoDomUtil.preventEventDefault(echoEvent);
    }
    if (echoEvent.keyCode == 13 || echoEvent.keyCode == 32) {
        EchoTextComponent.doAction(textComponent);
    }
};

EchoTextComponent.processKeyUp = function(echoEvent) {
    var textComponent = echoEvent.registeredTarget;
    if (!EchoClientEngine.verifyInput(textComponent.id)) {
        EchoDomUtil.preventEventDefault(echoEvent);
        return;
    }
    EchoTextComponent.doUpdate(textComponent);
};
