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

package nextapp.echo2.webrender.servermessage;

import nextapp.echo2.webrender.ServerMessage;

import org.w3c.dom.Element;

/**
 * A utility class to add <code>EchoEventProcessor</code> message parts to the 
 * <code>ServerMessage</code>.  <code>EchoEventProcessor</code> message parts
 * are used to register/unregister client-side event listeners.
 */
public class EventProcessor {

    private static final String MESSAGE_PART_NAME = "EchoEventProcessor.MessageProcessor";    
    private static final String[] EVENT_ADD_KEYS = new String[]{"type", "handler"};
    private static final String[] EVENT_REMOVE_KEYS = new String[]{"type"};
    
    /**
     * Creates an <code>event-add</code> operation to register a client event
     * listener of a particular type on an HTML element.
     * 
     * @param serverMessage the outgoing <code>ServerMessage</code>
     * @param eventType the type of event (the "on" prefix should be
     *        omitted, e.g., "onmousedown" would be expressed as
     *        "mousedown")
     * @param elementId the id of the listened-to DOM element
     * @param eventHandler the name of the handler method to be invoked 
     *        when the event occurs, e.g., "EchoButton.processAction"
     */
    public static void renderEventAdd(ServerMessage serverMessage, String eventType,
            String elementId, String eventHandler) {
        Element itemizedUpdateElement = serverMessage.getItemizedDirective(ServerMessage.GROUP_ID_POSTUPDATE, 
                MESSAGE_PART_NAME, "event-add", EVENT_ADD_KEYS, new String[]{eventType, eventHandler});
        Element itemElement = serverMessage.getDocument().createElement("item");
        itemElement.setAttribute("eid", elementId);
        itemizedUpdateElement.appendChild(itemElement);
    }
    
    /**
     * Creates an <code>event-remove</code> operation to unregister a client 
     * event listener of a particular type on an HTML element.
     * 
     * @param serverMessage the outgoing <code>ServerMessage</code>
     * @param eventType the type of event (the "on" prefix should be
     *        omitted, e.g., "onmousedown" would be expressed as
     *        "mousedown")
     * @param elementId the id of the listened-to DOM element
     */
    public static void renderEventRemove(ServerMessage serverMessage, String eventType,
            String elementId) {
        Element itemizedUpdateElement = serverMessage.getItemizedDirective(ServerMessage.GROUP_ID_PREREMOVE, 
                MESSAGE_PART_NAME, "event-remove", EVENT_REMOVE_KEYS, new String[]{eventType});
        Element itemElement = serverMessage.getDocument().createElement("item");
        itemElement.setAttribute("eid", elementId);
        itemizedUpdateElement.appendChild(itemElement);
    }
}
