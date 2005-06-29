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

//__________________________
// Object EchoBrowserCommand

/**
 * Static object/namespace for sending general-purpose stateless commands to 
 * the client browser.
 * This object/namespace should not be used externally.
 */
EchoBrowserCommand = function() { };

/**
 * Static object/namespace for browser command MessageProcessor 
 * implementation.
 */
EchoBrowserCommand.MessageProcessor = function() { };

/**
 * MessageProcessor process() implementation 
 * (invoked by ServerMessage processor).
 *
 * @param messagePartElement the <code>message-part</code> element to process.
 */
EchoBrowserCommand.MessageProcessor.process = function(messagePartElement) {
    for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
        if (messagePartElement.childNodes[i].nodeType == 1) {
            switch (messagePartElement.childNodes[i].tagName) {
            case "redirect":
                EchoBrowserCommand.MessageProcessor.processRedirect(messagePartElement.childNodes[i]);
                break;
            case "open-window":
                EchoBrowserCommand.MessageProcessor.processOpenWindow(messagePartElement.childNodes[i]);
                break;
            }
        }
    }
};

/**
 * Processes an <code>open-window</code> command message to display a URI in a
 * specific or new browser window.
 *
 * @param openWindowElement the <code>open-window</code> element to process
 */
EchoBrowserCommand.MessageProcessor.processOpenWindow = function(openWindowElement) {
    var uri = openWindowElement.getAttribute("uri");
    var name = openWindowElement.getAttribute("name");
    var features = openWindowElement.getAttribute("features");
    var replace = openWindowElement.getAttribute("replace") == "true";
    window.open(uri, name, features, replace);
};


/**
 * Processes an <code>redirect</code> to redirect the window containing the
 * Echo application a new URI.
 *
 * @param redirectElement the <code>redirect</code> element to process
 */
EchoBrowserCommand.MessageProcessor.processRedirect = function(redirectElement) {
    var uri = redirectElement.getAttribute("uri");
    window.location = uri;
};
