/* 
 * This file is part of the Echo Web Application Framework (hereinafter "Echo").
 * Copyright (C) 2002-2009 NextApp, Inc.
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import nextapp.echo2.webrender.ClientConfiguration;
import nextapp.echo2.webrender.ServerMessage;

/**
 * Renders <code>ServerMessage</code> directives to store
 * <code>ClientConfiguration</code> information on the client.
 */
public class ClientConfigurationUpdate {

    /**
     * Renders a <code>ServerMessage</code> directive directing the 
     * client to store the provided <code>ClientConfiguration</code> information.
     *
     * @param serverMessage the outgoing <code>ServerMessage</code>
     * @param clientConfiguration the <code>ClientConfiguration</code> information
     */
    public static void renderUpdateDirective(ServerMessage serverMessage, ClientConfiguration clientConfiguration) {
        if (clientConfiguration == null) {
            return;
        }
        Document document = serverMessage.getDocument();
        Element messagePartElement = serverMessage.addPart(ServerMessage.GROUP_ID_INIT, "EchoClientConfiguration.MessageProcessor");
        Element storeElement = document.createElement("store");
        messagePartElement.appendChild(storeElement);

        String[] propertyNames = clientConfiguration.getPropertyNames();
        for (int i = 0; i < propertyNames.length; ++i) {
            Element propertyElement = document.createElement("property");
            storeElement.appendChild(propertyElement);
            propertyElement.setAttribute("name", propertyNames[i]);
            propertyElement.setAttribute("value", clientConfiguration.getProperty(propertyNames[i]));
        }
    }
    
    /** Non-instantiable class. */
    private ClientConfigurationUpdate() { }
}
