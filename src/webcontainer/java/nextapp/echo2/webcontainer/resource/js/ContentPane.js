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

//_______________________
// Object EchoContentPane

/**
 * Static object/namespace for ContentPane support.
 * This object/namespace should not be used externally.
 */
EchoContentPane = function() { };

/**
 * Static object/namespace for ContentPane MessageProcessor 
 * implementation.
 */
EchoContentPane.MessageProcessor = function() { };

/**
 * MessageProcessor process() implementation 
 * (invoked by ServerMessage processor).
 *
 * @param messagePartElement the <code>message-part</code> element to process.
 */
EchoContentPane.MessageProcessor.process = function(messagePartElement) {
    for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
        if (messagePartElement.childNodes[i].nodeType == 1) {
            switch (messagePartElement.childNodes[i].tagName) {
            case "init":
                EchoContentPane.MessageProcessor.processInit(messagePartElement.childNodes[i]);
                break;
            case "dispose":
                EchoContentPane.MessageProcessor.processDispose(messagePartElement.childNodes[i]);
                break;
            case "scroll-horizontal":
                EchoContentPane.MessageProcessor.processScroll(messagePartElement.childNodes[i]);
                break;
            case "scroll-vertical":
                EchoContentPane.MessageProcessor.processScroll(messagePartElement.childNodes[i]);
                break;
            }
        }
    }
};

/**
 * Processes a <code>dispose</code> message to finalize the state of a
 * selection component that is being removed.
 *
 * @param disposeMessageElement the <code>dispose</code> element to process
 */
EchoContentPane.MessageProcessor.processDispose = function(disposeMessageElement) {
    for (var item = disposeMessageElement.firstChild; item; item = item.nextSibling) {
        var elementId = item.getAttribute("eid");
        EchoEventProcessor.removeHandler(elementId, "scroll");
    }
};

/**
 * Processes an <code>init</code> message to initialize the state of a 
 * selection component that is being added.
 *
 * @param initMessageElement the <code>init</code> element to process
 */
EchoContentPane.MessageProcessor.processInit = function(initMessageElement) {
    for (var item = initMessageElement.firstChild; item; item = item.nextSibling) {
        var elementId = item.getAttribute("eid");
        var divElement = document.getElementById(elementId);

        var horizontalScroll = item.getAttribute("horizontal-scroll");
        var verticalScroll = item.getAttribute("vertical-scroll");

        EchoEventProcessor.addHandler(divElement, "scroll", "EchoContentPane.processScroll");
        
        if (horizontalScroll) {
            divElement.scrollLeft = parseInt(horizontalScroll);
        }
        if (verticalScroll) {
            divElement.scrollTop = parseInt(verticalScroll);
        }
    }
};

/**
 * Processes a <code>scroll</code> directive to update the scrollbar positions
 * of the component.
 *
 * @param scrollMessageElement the <code>scroll</code> element to process
 */
EchoContentPane.MessageProcessor.processScroll = function(scrollMessageElement) {
    var elementId = scrollMessageElement.getAttribute("eid");
    var position = parseInt(scrollMessageElement.getAttribute("position"));

    var divElement = document.getElementById(elementId);
    
    if (position < 0) {
        position = 1000000;
    }
    
    if (scrollMessageElement.nodeName == "scroll-horizontal") {
        divElement.scrollLeft = position;
    } else if (scrollMessageElement.nodeName == "scroll-vertical") {
        divElement.scrollTop = position;
    }
};

/**
 * Processes a scrollbar adjustment event.
 *
 * @param echoEvent the event, preprocessed by the 
 *        <code>EchoEventProcessor</code>
 */
EchoContentPane.processScroll = function(echoEvent) {
    if (!EchoClientEngine.verifyInput(echoEvent.registeredTarget)) {
        return;
    }
    EchoClientMessage.setPropertyValue(echoEvent.registeredTarget.id, "horizontalScroll",  
            echoEvent.registeredTarget.scrollLeft + "px");
    EchoClientMessage.setPropertyValue(echoEvent.registeredTarget.id, "verticalScroll",  
            echoEvent.registeredTarget.scrollTop + "px");
};
