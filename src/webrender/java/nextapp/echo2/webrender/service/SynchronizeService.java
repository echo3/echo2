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

package nextapp.echo2.webrender.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import nextapp.echo2.webrender.ClientAnalyzerProcessor;
import nextapp.echo2.webrender.Connection;
import nextapp.echo2.webrender.ContentType;
import nextapp.echo2.webrender.ServerMessage;
import nextapp.echo2.webrender.Service;
import nextapp.echo2.webrender.UserInstance;
import nextapp.echo2.webrender.UserInstanceUpdateManager;
import nextapp.echo2.webrender.servermessage.ClientConfigurationUpdate;
import nextapp.echo2.webrender.servermessage.ClientPropertiesStore;
import nextapp.echo2.webrender.servermessage.ServerDelayMessageUpdate;
import nextapp.echo2.webrender.util.DomUtil;

/**
 * A service which synchronizes the state of the client with that of the server.
 * Requests made to this service are in the form of "ClientMessage" XML
 * documents which describe the user's actions since the last synchronization,
 * e.g., the input typed into text fields and the action taken (e.g., a button
 * press) which caused the server interaction. The service parses this XML input
 * from the client and performs updates to the server state of the application.
 * Once the input has been processed by the server application, an output
 * "ServerMessage" containing instructions to update the client state is
 * generated as a response.
 */
public abstract class SynchronizeService 
implements Service {
    
    /**
     * An interface describing a ClientMessage MessagePart Processor.
     * Implementations registered with the
     * <code>registerClientMessagePartProcessor()</code> method will have
     * their <code>process()</code> methods invoked when a matching
     * message part is provided in a ClientMessage.
     */
    public static interface ClientMessagePartProcessor {
        
        /**
         * Returns the name of the <code>ClientMessagePartProcessor</code>.
         * The processor will be invoked when a message part with its name
         * is found within the ClientMessage.
         * 
         * @return the name of the processor
         */
        public String getName();
        
        /**
         * Processes a MessagePart of a ClientMessage
         * 
         * @param userInstance the relevant <code>UserInstance</code>
         * @param messagePartElement the <code>message part</code> element
         *        to process
         */
        public void process(UserInstance userInstance, Element messagePartElement);
    }

    /**
     * <code>Service</code> identifier.
     */
    public static final String SERVICE_ID = "Echo.Synchronize";

    /**
     * Map containing registered <code>ClientMessagePartProcessor</code>s.
     */
    private Map clientMessagePartProcessorMap = new HashMap(); 
    
    /**
     * Creates a new <code>SynchronizeService</code>.
     */
    public SynchronizeService() {
        super();
        registerClientMessagePartProcessor(new ClientAnalyzerProcessor());
    }
    
    /**
     * Trims an XML <code>InputStream</code> to work around the issue 
     * of the XML parser crashing on trailing whitespace.   This issue is present 
     * with requests from Konqueror/KHTML browsers. 
     * 
     * @param in the <code>InputStream</code>
     * @param characterEncoding the character encoding of the stream 
     * @return a cleaned version of the stream, as a 
     *         <code>ByteArrayInputStream</code>.
     */
    private InputStream cleanXmlInputStream(InputStream in, String characterEncoding) 
    throws IOException{
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        
        byte[] buffer = new byte[4096];
        int bytesRead = 0;
        
        try {
            do {
                bytesRead = in.read(buffer);
                if (bytesRead > 0) {
                    byteOut.write(buffer, 0, bytesRead);
                }
            } while (bytesRead > 0);
        } finally {
            if (in != null) { try { in.close(); } catch (IOException ex) { } } 
        }
        
        in.close();
        
        byte[] data = byteOut.toByteArray();
        data = new String(data, characterEncoding).trim().getBytes(characterEncoding);
        
        return new ByteArrayInputStream(data);
    }
    
    /**
     * @see nextapp.echo2.webrender.Service#getId()
     */
    public String getId() {
        return SERVICE_ID;
    }
    
    /**
     * @see nextapp.echo2.webrender.Service#getVersion()
     */
    public int getVersion() {
        return DO_NOT_CACHE;
    }
    
    /**
     * Generates a DOM representation of the XML input POSTed to this service.
     * 
     * @param conn the relevant <code>Connection</code>
     * @return a DOM representation of the POSTed XML input
     * @throws IOException if the input is invalid
     */
    private Document parseRequestDocument(Connection conn) 
    throws IOException {
        HttpServletRequest request = conn.getRequest();
        InputStream in = null;
        try {
            String userAgent = conn.getRequest().getHeader("user-agent");
            if (userAgent != null && userAgent.indexOf("onqueror") != -1) {
                // Invoke XML 'cleaner', but only for  user agents that contain the string "onqueror",
                // such as Konqueror, for example.
                in = cleanXmlInputStream(request.getInputStream(), conn.getUserInstance().getCharacterEncoding());
            } else {
                in = request.getInputStream();
            }
            return DomUtil.getDocumentBuilder().parse(in);
        } catch (SAXException ex) {
            throw new IOException("Provided InputStream cannot be parsed: " + ex);
        } catch (IOException ex) {
            throw new IOException("Provided InputStream cannot be parsed: " + ex);
        } finally {
            if (in != null) { try { in.close(); } catch (IOException ex) { } }
        }
    }

    /**
     * Processes a "ClientMessage" XML document containing application UI state 
     * change information from the client.  This method will parse the
     * message parts of the ClientMessage and invoke the
     * <code>ClientMessagePartProcessor</code>s registered to process them.
     * 
     * @param conn the relevant <code>Connection</code> 
     * @param clientMessageDocument the ClientMessage XML document to process
     * @see ClientMessagePartProcessor
     */
    protected void processClientMessage(Connection conn, Document clientMessageDocument) {
        UserInstance userInstance = conn.getUserInstance();
        Element[] messageParts = DomUtil.getChildElementsByTagName(clientMessageDocument.getDocumentElement(), 
                "message-part");
        for (int i = 0; i < messageParts.length; ++i) {
            ClientMessagePartProcessor processor = 
                    (ClientMessagePartProcessor) clientMessagePartProcessorMap.get(messageParts[i].getAttribute("processor"));
            if (processor == null) {
                throw new RuntimeException("Invalid processor name \"" + messageParts[i].getAttribute("processor") + "\".");
            }
            processor.process(userInstance, messageParts[i]);
        }
    }
    
    /**
     * Registers a <code>ClientMessagePartProcessor</code> to handle a
     * specific type of message part.
     * 
     * @param processor the <code>ClientMessagePartProcessor</code> to 
     *        register
     * @throws IllegalStateException if a processor with the same name is 
     *         already registered
     */
    protected void registerClientMessagePartProcessor(ClientMessagePartProcessor processor) {
        if (clientMessagePartProcessorMap.containsKey(processor.getName())) {
            throw new IllegalStateException("Processor already registered with name \"" + processor.getName() + "\".");
        }
        clientMessagePartProcessorMap.put(processor.getName(), processor);
    }
    
    /**
     * Renders a <code>ServerMessage</code> in response to the initial
     * synchronization.
     * 
     * @param conn the relevant <code>Connection</code>
     * @param clientMessageDocument the ClientMessage XML document
     * @return the generated <code>ServerMessage</code>
     */
    protected abstract ServerMessage renderInit(Connection conn, Document clientMessageDocument);
    
    /**
     * Renders a <code>ServerMessage</code> in response to a synchronization
     * other than the initial synchronization.
     * 
     * @param conn the relevant <code>Connection</code>
     * @param clientMessageDocument the ClientMessage XML document
     * @return the generated <code>ServerMessage</code>
     */
    protected abstract ServerMessage renderUpdate(Connection conn, Document clientMessageDocument);
    
    /**
     * @see nextapp.echo2.webrender.Service#service(nextapp.echo2.webrender.Connection)
     */
    public void service(Connection conn) 
    throws IOException {
        UserInstance userInstance = conn.getUserInstance();
        synchronized(userInstance) {
            Document clientMessageDocument = parseRequestDocument(conn);
            String messageType = clientMessageDocument.getDocumentElement().getAttribute("type");
            ServerMessage serverMessage;
            
            if ("initialize".equals(messageType)) {
                serverMessage = renderInit(conn, clientMessageDocument);
                ClientPropertiesStore.renderStoreDirective(serverMessage, userInstance.getClientProperties());
                ClientConfigurationUpdate.renderUpdateDirective(serverMessage, userInstance.getClientConfiguration());
                ServerDelayMessageUpdate.renderUpdateDirective(serverMessage, userInstance.getServerDelayMessage());
                
                // Add "test attribute" used by ClientEngine to determine if browser is correctly (un)escaping
                // attribute values.  Safari does not do this correctly and a workaround is thus employed if such
                // bugs are detected.
                serverMessage.getDocument().getDocumentElement().setAttribute("xml-attr-test", "x&y");
            } else {
                serverMessage = renderUpdate(conn, clientMessageDocument);
                processUserInstanceUpdates(userInstance, serverMessage);
            }
            serverMessage.setTransactionId(userInstance.getNextTransactionId());
            conn.setContentType(ContentType.TEXT_XML);
            serverMessage.render(conn.getWriter());
        }
    }
    
    /**
     * Renders updates to <code>UserInstance</code> properties.
     * 
     * @param userInstance the relevant <code>UserInstance</code>
     * @param serverMessage the <code>ServerMessage</code> containing the updates
     */
    private void processUserInstanceUpdates(UserInstance userInstance, ServerMessage serverMessage) {
        UserInstanceUpdateManager updateManager = userInstance.getUserInstanceUpdateManager();
        String[] updatedPropertyNames = updateManager.getPropertyUpdateNames();
        for (int i = 0; i < updatedPropertyNames.length; ++i) {
            if (UserInstance.PROPERTY_CLIENT_CONFIGURATION.equals(updatedPropertyNames[i])) {
                ClientConfigurationUpdate.renderUpdateDirective(serverMessage, userInstance.getClientConfiguration());
            } else if (UserInstance.PROPERTY_SERVER_DELAY_MESSAGE.equals(updatedPropertyNames[i])) {
                ServerDelayMessageUpdate.renderUpdateDirective(serverMessage, userInstance.getServerDelayMessage());
            }
        }
        updateManager.purge();
    }
}
