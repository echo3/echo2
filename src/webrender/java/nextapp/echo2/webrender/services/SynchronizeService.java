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

package nextapp.echo2.webrender.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import nextapp.echo2.webrender.clientupdate.ServerMessage;
import nextapp.echo2.webrender.server.ClientPropertiesLoader;
import nextapp.echo2.webrender.server.Connection;
import nextapp.echo2.webrender.server.ContentType;
import nextapp.echo2.webrender.server.Service;
import nextapp.echo2.webrender.server.UserInstance;
import nextapp.echo2.webrender.util.DomUtil;

/**
 * A service which synchronizes the state of the client with that of the
 * server.  Requests made to this service are in the form of "ClientMessage"
 * XML documents which describe the users actions since the last 
 * synchronization, e.g., input typed into text fields and the action taken
 * (e.g., a button press) which caused the server interaction.
 * The service then communicates these changes to the server-side application,
 * and then generates an output "ServerMessage" containing instructions to 
 * update the client-side state of the application to the updated server-side
 * state.
 */
public abstract class SynchronizeService 
implements Service {

    
    public static interface ClientMessagePartProcessor {
        
        public void process(UserInstance userInstance, Element messagePartElement);
    }

    public static final String SERVICE_ID = "Echo.Synchronize";

    private Map clientMessagePartProcessors = new HashMap(); 
    
    public SynchronizeService() {
        super();
        addClientMessagePartProcessor("EchoClientProperties", new ClientPropertiesLoader());
    }
    
    public void addClientMessagePartProcessor(String name, ClientMessagePartProcessor processor) {
        clientMessagePartProcessors.put(name, processor);
    }
    
    /**
     * @see nextapp.echo2.webrender.server.Service#getId()
     */
    public String getId() {
        return SERVICE_ID;
    }
    
    /**
     * @see nextapp.echo2.webrender.server.Service#getVersion()
     */
    public int getVersion() {
        return DO_NOT_CACHE;
    }

    private Document parseRequestDocument(Connection conn) 
    throws IOException {
        HttpServletRequest request =  conn.getRequest();
        if (!request.getContentType().equals("text/xml")) {
            throw new IOException("Invalid content type.");
        }
        InputStream in = null;
        try {
            in = request.getInputStream();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(in);
        } catch (ParserConfigurationException ex) {
            throw new IOException("Provided InputStream cannot be parsed: " + ex.toString());
        } catch (SAXException ex) {
            throw new IOException("Provided InputStream cannot be parsed: " + ex.toString());
        } catch (IOException ex) {
            throw new IOException("Provided InputStream cannot be parsed: " + ex.toString());
        } finally {
            if (in != null) { try { in.close(); } catch (IOException ex) { } }
        }
    }
    
    protected abstract ServerMessage renderInit(Connection conn, Document clientMessageDocument);
    
    protected abstract ServerMessage renderUpdate(Connection conn, Document clientMessageDocument);
    
    protected void processClientMessage(Connection conn, Document clientMessageDocument) {
        UserInstance userInstance = conn.getUserInstance();
        Element[] messageParts = DomUtil.getChildElementsByTagName(clientMessageDocument.getDocumentElement(), 
                "messagepart");
        for (int i = 0; i < messageParts.length; ++i) {
            ClientMessagePartProcessor processor = 
                    (ClientMessagePartProcessor) clientMessagePartProcessors.get(messageParts[i].getAttribute("processor"));
            if (processor == null) {
                //BUGBUG. Possibly have a syncserviceexception
                throw new RuntimeException("Invalid processor name: " + messageParts[i].getAttribute("processor"));
            }
            processor.process(userInstance, messageParts[i]);
        }
    }
    
    /**
     * @see nextapp.echo2.webrender.server.Service#service(nextapp.echo2.webrender.server.Connection)
     */
    public void service(Connection conn) throws IOException {
        Document clientMessageDocument = parseRequestDocument(conn);
        String messageType = clientMessageDocument.getDocumentElement().getAttribute("type");
        
        ServerMessage serverMessage;
        
        if ("initialize".equals(messageType)) {
            serverMessage = renderInit(conn, clientMessageDocument);
        } else {
            serverMessage = renderUpdate(conn, clientMessageDocument);
        }
        conn.setContentType(ContentType.TEXT_XML);
        serverMessage.render(conn.getWriter());       
    }
}
