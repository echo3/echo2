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

package echo2example.chatserver;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Chat Server Servlet
 */
public class ChatServerServlet extends HttpServlet {
    
    private static final String LOCALHOST = "127.0.0.1";
    private static final Server server = new Server();
    
    /**
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, IOException {
        
        if (! LOCALHOST.equals(request.getRemoteHost())) {
            throw new ServletException("Request from invalid host.");
        }
        
        Document inputDocument = loadInputDocument(request);
        Document outputDocument = createOutputDocument();
        processUserAdd(inputDocument, outputDocument);
        processPostMessage(inputDocument, outputDocument);
        processGetMessages(inputDocument, outputDocument);
        renderOutputDocument(response, outputDocument);
    }
    
    private Document createOutputDocument() 
    throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            document.appendChild(document.createElement("chat-server-response"));
            return document;
        } catch (ParserConfigurationException ex) {
            throw new IOException("Cannot create document: " + ex);
        }
    }
    
    private Document loadInputDocument(HttpServletRequest request) 
    throws IOException {
        InputStream in = null;
        try {
            in = request.getInputStream();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(in);
        } catch (ParserConfigurationException ex) {
            throw new IOException("Provided InputStream cannot be parsed: " + ex);
        } catch (SAXException ex) {
            throw new IOException("Provided InputStream cannot be parsed: " + ex);
        } catch (IOException ex) {
            throw new IOException("Provided InputStream cannot be parsed: " + ex);
        } finally {
            if (in != null) { try { in.close(); } catch (IOException ex) { } }
        }
    }
    
    private void processGetMessages(Document inputDocument, Document outputDocument) {
        String lastRetrievedIdString = inputDocument.getDocumentElement().getAttribute("last-retrieved-id");
        Message[] messages;
        if (lastRetrievedIdString == null || lastRetrievedIdString.trim().length() == 0) {
            messages = server.getRecentMessages();
        } else {
            long lastRetrievedId = Long.parseLong(lastRetrievedIdString);
            messages = server.getMessages(lastRetrievedId);
        }
        for (int i = 0; i < messages.length; ++i) {
            Element messageElement = outputDocument.createElement("message");
            messageElement.setAttribute("id", Long.toString(messages[i].getId()));
            messageElement.setAttribute("user-name", messages[i].getUserName());
            messageElement.setAttribute("time", Long.toString(messages[i].getPostTime()));
            messageElement.appendChild(outputDocument.createTextNode(messages[i].getContent()));
            outputDocument.getDocumentElement().appendChild(messageElement);
        }
    }
    
    private void processPostMessage(Document inputDocument, Document outputDocument) {
        NodeList postMessageNodes = inputDocument.getDocumentElement().getElementsByTagName("post-message");
        if (postMessageNodes.getLength() == 0) {
            return;
        }
        
        Element postMessageElement = (Element) postMessageNodes.item(0);
        String userName = postMessageElement.getAttribute("user-name");
        String authToken = postMessageElement.getAttribute("auth-token");
        NodeList postMessageContentNodes = postMessageElement.getChildNodes();
        int length = postMessageContentNodes.getLength();
        for (int i = 0; i < length; ++i) {
            if (postMessageContentNodes.item(i) instanceof Text) {
                server.postMessage(userName, authToken, ((Text) postMessageContentNodes.item(i)).getNodeValue());
            }
        }
    }
    
    private void processUserAdd(Document inputDocument, Document outputDocument) {
        NodeList userAddNodes = inputDocument.getDocumentElement().getElementsByTagName("user-add");
        if (userAddNodes.getLength() == 0) {
            return;
        }
        
        // Attempt to authenticate.
        Element userAddElement = (Element) userAddNodes.item(0);
        String userName = userAddElement.getAttribute("name");
        String authToken = server.addUser(userName);
        
        Element userAuthElement = outputDocument.createElement("user-auth");
        if (authToken == null) {
            userAuthElement.setAttribute("failed", "true");
        } else {
            userAuthElement.setAttribute("auth-token", authToken);
        }
        outputDocument.getDocumentElement().appendChild(userAuthElement);
    }
    
    private void renderOutputDocument(HttpServletResponse response, Document outputDocument) 
    throws IOException {
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource source = new DOMSource(outputDocument);
            StreamResult result = new StreamResult(response.getWriter());
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            throw new IOException("Unable to write document to OutputStream: " + ex.toString());
        }
    }
}
