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

package nextapp.echo2.webrender;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Representation of a client/server-interaction delay message.
 * Client/server-interaction delay messages are presented to the user
 * while the client is communicating synchronously with the server. 
 */
public abstract class ServerDelayMessage {

    /**
     * The element id of the "main delay message" element which will be made
     * visible when the any client/server interaction occurs.
     */
    public static final String ELEMENT_ID_MESSAGE = "serverDelayMessage";
    
    /**
     * The element id of the "long delay message" element which will be made 
     * visible when the delay pane has been displayed for a set amount
     * of time. 
     */
    public static final String ELEMENT_ID_LONG_MESSAGE = "serverDelayMessageLong";

    /**
     * Creates a new concrete <code>ServerDelayMessage</code> implementation 
     * based on the HTML fragment contained in the specified 
     * <code>CLASSPATH</code> resource
     * 
     * @param resourceName the <code>CLASSPATH</code> resource containing the
     *        HTML fragment
     * @return the created <code>ServerDelayMessage</code>
     * @throws IOException if the resource cannot be found or parsed
     */
    public static ServerDelayMessage createFromResource(String resourceName) 
    throws IOException {
        Document document;
        InputStream in = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            in = ServerDelayMessage.class.getResourceAsStream(resourceName); 
            if (in == null) {
                throw new IOException("Resource not found: " + resourceName + ".");
            }
            document = builder.parse(in);
        } catch (ParserConfigurationException ex) {
            throw new IOException("Failed to parse InputStream.");
        } catch (SAXException ex) {
            throw new IOException("Failed to parse InputStream.");
        } finally {
            if (in != null) {
                try { in.close(); } catch (IOException ex) { } 
            }
        }
        final Node messageNode = document.getDocumentElement();
        return new ServerDelayMessage() {
            public Node getMessage() {
                return messageNode;
            }
        };
    }
    
    /**
     * Returns the message content as an HTML fragment
     * 
     * @return the HTML fragment
     */
    public abstract Node getMessage();
 }
