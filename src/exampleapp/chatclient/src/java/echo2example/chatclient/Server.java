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

package echo2example.chatclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class Server {
    
    public static class Message {
        
        private String content;
        private Date date;
        private String userName;
        
        private Message(String userName, Date date, String content) {
            super();
            this.userName = userName;
            this.date = date;
            this.content = content;
        }
        
        public String getContent() {
            return content;
        }
        
        public Date getDate() {
            return date;
        }

        public String getUserName() {
            return userName;
        }
    }
    
    public static Server forUserName(String userName) 
    throws IOException {
        Server server = new Server(userName);
        return server.authToken == null ? null : server;
    }

    private String lastRetrievedId;
    private String authToken;
    private String userName;
    private List newMessages = new ArrayList();
    private String serviceUrl = "http://localhost:8080/ChatServer/app"; 
    
    private Server(String userName) 
    throws IOException {
        super();
        this.userName = userName;
        
        Document requestDocument = createRequestDocument();
        
        Element userAddElement = requestDocument.createElement("user-add");
        userAddElement.setAttribute("name", userName);
        requestDocument.getDocumentElement().appendChild(userAddElement);
        
        Document responseDocument = XmlHttpConnection.send(serviceUrl, requestDocument);
        NodeList userAuthNodes = responseDocument.getElementsByTagName("user-auth");
        if (userAuthNodes.getLength() != 1) {
            throw new IOException("Unexpected response.");
        }
        Element userAuthElement = (Element) userAuthNodes.item(0);
        if ("true".equals(userAuthElement.getAttribute("failed"))) {
        } else {
            authToken = userAuthElement.getAttribute("auth-token");
        }
    }

    private Document createRequestDocument() 
    throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element rootElement = document.createElement("chat-server-request");
            if (lastRetrievedId != null) {
                rootElement.setAttribute("last-retrieved-id", lastRetrievedId);
            }
            document.appendChild(rootElement);
            return document;
        } catch (ParserConfigurationException ex) {
            throw new IOException("Cannot create document: " + ex);
        }
    }
    
    public Message[] getNewMessages() {
        Message[] messages = (Message[]) newMessages.toArray(new Message[newMessages.size()]);
        newMessages.clear();
        return messages;
    }
    
    public boolean hasNewMessages() {
        return newMessages.size() != 0;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void pollServer() 
    throws IOException {
        Document requestDocument = createRequestDocument();
        Document responseDocument = XmlHttpConnection.send(serviceUrl, requestDocument);
        updateLocalMessages(responseDocument);
    }
    
    public void postMessage(String content) 
    throws IOException {
        Document requestDocument = createRequestDocument();
        
        Element postMessageElement = requestDocument.createElement("post-message");
        postMessageElement.setAttribute("user-name", userName);
        postMessageElement.setAttribute("auth-token", authToken);
        postMessageElement.appendChild(requestDocument.createTextNode(content));
        requestDocument.getDocumentElement().appendChild(postMessageElement);
        
        Document responseDocument = XmlHttpConnection.send(serviceUrl, requestDocument);
        updateLocalMessages(responseDocument);
    }
    
    public void updateLocalMessages(Document responseDocument) {
        NodeList newMessageElements = responseDocument.getDocumentElement().getElementsByTagName("message");
        int length = newMessageElements.getLength();
        for (int i = 0; i < length; ++i) {
            Element messageElement = (Element) newMessageElements.item(i);
            lastRetrievedId = messageElement.getAttribute("id");
            String userName = messageElement.getAttribute("user-name");
            NodeList childNodes = messageElement.getChildNodes();
            String content = null;
            for (int j = 0; j < childNodes.getLength(); ++j) {
                if (childNodes.item(j) instanceof Text) {
                    content = childNodes.item(j).getNodeValue();
                    break;
                }
            }
            long timeMs = Long.parseLong(messageElement.getAttribute("time"));
            Message message = new Message(userName, new Date(timeMs), content);
            newMessages.add(message);
        }
    }
}
