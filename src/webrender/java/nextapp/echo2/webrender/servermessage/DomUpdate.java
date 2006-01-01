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

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A utility class to add <code>EchoDomUpdate</code> message parts to the 
 * <code>ServerMessage</code>.  <code>EchoDomUpdate</code> message parts
 * are used to directly update the client DOM with HTML code generated on
 * the server.
 */
public class DomUpdate {
    
    private static final String MESSAGE_PART_NAME = "EchoDomUpdate.MessageProcessor";
    private static final String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";

    /**
     * Creates a <code>attribute-update</code> operation to update an 
     * element attribute of the element identified by <code>targetId</code>
     * in the client DOM.
     * 
     * @param serverMessage the outgoing <code>ServerMessage</code>
     * @param targetId the id of the element whose attribute is to be updated
     * @param attributeName the name of the attribute to update
     * @param attributeValue the new value of the attribute
     */
    public static void renderAttributeUpdate(ServerMessage serverMessage, String targetId, String attributeName, 
            String attributeValue) {
        //BUGBUG. support nulls for deletion.
        Element element = serverMessage.appendPartDirective(ServerMessage.GROUP_ID_UPDATE, 
                MESSAGE_PART_NAME, "attribute-update");
        element.setAttribute("target-id", targetId);
        element.setAttribute("name", attributeName);
        element.setAttribute("value", attributeValue);
    }
    
    /**
     * Prepares a <code>dom-add</code> operation by immediately appending an 
     * empty <code>dom-add</code> element to the end of the 
     * <code>ServerMessage</code>'s 'update' group.
     * Content is added to the <code>dom-add</code> element by invoking
     * <code>renderElementAddContent()</code>.
     * 
     * @param serverMessage the <code>ServerMessage</code>
     * @return the created <code>dom-add</code> <code>Element</code>.
     */
    public static Element renderElementAdd(ServerMessage serverMessage) {
        Element domAddElement = serverMessage.appendPartDirective(ServerMessage.GROUP_ID_UPDATE, 
                MESSAGE_PART_NAME, "dom-add");
        return domAddElement;
    }

    /**
     * Creates a <code>dom-add</code> operation to append HTML content to the 
     * end of the element identified by <code>parentId</code>.
     * 
     * @param serverMessage the outgoing <code>ServerMessage</code>
     * @param parentId the id of the element the HTML code will be appended to
     * @param htmlFragment the HTML fragment to add to the DOM
     * @deprecated use of this method can result in DOM modifications
     *             being performed in improper order 
     *             (instead use <code>renderElementAdd(ServerMessage)</code> followed by
     *             <code>renderElementAddContent()</code>)
     */
    public static void renderElementAdd(ServerMessage serverMessage, String parentId, DocumentFragment htmlFragment) {
        renderElementAdd(serverMessage, parentId, null, htmlFragment);
    }

    /**
     * Creates a <code>dom-add</code> operation to insert HTML content in the 
     * element identified by <code>parentId</code>.
     * 
     * @param serverMessage the outgoing <code>ServerMessage</code>
     * @param parentId the id of the element into which the HTML code will be 
     *        inserted
     * @param siblingId The id of the element which the content will be inserted
     *        <strong>before</strong> (this element must be an immediate child
     *        of the element specified by <code>parentId</code>)
     * @param htmlFragment the HTML fragment to add to the DOM
     * @deprecated use of this method can result in DOM modifications
     *             being performed in improper order 
     *             (instead use <code>renderElementAdd(ServerMessage)</code> followed by
     *             <code>renderElementAddContent()</code>)
     */
    public static void renderElementAdd(ServerMessage serverMessage, String parentId, String siblingId, 
            DocumentFragment htmlFragment) {
        setContentNamespace(htmlFragment);
        Element domAddElement = serverMessage.appendPartDirective(ServerMessage.GROUP_ID_UPDATE, 
                MESSAGE_PART_NAME, "dom-add");
        Element contentElement = domAddElement.getOwnerDocument().createElement("content");
        contentElement.setAttribute("parent-id", parentId);
        if (siblingId != null) {
            contentElement.setAttribute("sibling-id", siblingId);
        }
        domAddElement.appendChild(contentElement);
        contentElement.appendChild(htmlFragment);
    }
    
    /**
     * Adds content to be added to an existing <code>dom-add</code> operation.
     * The content will be appended to the end of the DOM element identified by
     * <code>parentId</code>
     * 
     * @param serverMessage the <code>ServerMessage</code>
     * @param domAddElement the <code>dom-add</code> element created by a 
     *        previous invocation of <code>renderAdd(ServerMessage)</code>
     * @param parentId the id of the element the HTML code will be appended to
     * @param htmlFragment the HTML fragment to add to the DOM
     */
    public static void renderElementAddContent(ServerMessage serverMessage, Element domAddElement, String parentId,
            DocumentFragment htmlFragment) {
        renderElementAddContent(serverMessage, domAddElement, parentId, null, htmlFragment);
    }
    
    /**
     * Adds content to be added to an existing <code>dom-add</code> operation.
     * The content will be inserted into the DOM element identified by
     * <code>parentId</code> before the specified <code>siblingId</code>.
     * 
     * @param serverMessage the <code>ServerMessage</code>
     * @param domAddElement the <code>dom-add</code> element created by a 
     *        previous invocation of <code>renderAdd(ServerMessage)</code>
     * @param parentId the id of the element the HTML code will be appended to
     * @param siblingId The id of the element which the content will be inserted
     *        <strong>before</strong> (this element must be an immediate child
     *        of the element specified by <code>parentId</code>)
     * @param htmlFragment the HTML fragment to add to the DOM
     */
    public static void renderElementAddContent(ServerMessage serverMessage, Element domAddElement, String parentId, 
            String siblingId, DocumentFragment htmlFragment) {
        setContentNamespace(htmlFragment);
        Element contentElement = domAddElement.getOwnerDocument().createElement("content");
        contentElement.setAttribute("parent-id", parentId);
        if (siblingId != null) {
            contentElement.setAttribute("sibling-id", siblingId);
        }
        domAddElement.appendChild(contentElement);
        contentElement.appendChild(htmlFragment);
    }
    
    /**
     * Creates a <code>dom-remove</code> operation to remove the HTML element 
     * identified by <code>targetId</code> from the client DOM.
     * 
     * @param serverMessage the outgoing <code>ServerMessage</code>
     * @param targetId the id of the element to remove
     */
    public static void renderElementRemove(ServerMessage serverMessage, String targetId) {
        Element domRemoveElement = serverMessage.appendPartDirective(ServerMessage.GROUP_ID_REMOVE, 
                MESSAGE_PART_NAME, "dom-remove");
        domRemoveElement.setAttribute("target-id", targetId);
    }

    /**
     * Creates a <code>dom-remove</code> operation to remove all child elements
     * of the element identified by <code>targetId</code> from the client DOM.
     * 
     * @param serverMessage the outgoing <code>ServerMessage</code>
     * @param targetId the id of the element whose children ware to be removed
     */
    public static void renderElementRemoveChildren(ServerMessage serverMessage, String targetId) {
        Element domRemoveElement = serverMessage.appendPartDirective(ServerMessage.GROUP_ID_REMOVE, 
                MESSAGE_PART_NAME, "dom-remove-children");
        domRemoveElement.setAttribute("target-id", targetId);
    }
    
    /**
     * Creates a <code>style-update</code> operation to update a CSS style 
     * attribute of the element identified by <code>targetId</code> in the
     * client DOM.
     * 
     * @param serverMessage the outgoing <code>ServerMessage</code>
     * @param targetId the id of the element whose <strong>style</strong> 
     *        attribute is to be updated
     * @param attributeName the name of the <strong>style</strong> attribute
     * @param attributeValue the new value of the <strong>style</strong> 
     *        attribute
     */
    public static void renderStyleUpdate(ServerMessage serverMessage, String targetId, String attributeName, 
            String attributeValue) {
        //BUGBUG. support nulls for deletion.
        Element element = serverMessage.appendPartDirective(ServerMessage.GROUP_ID_UPDATE, MESSAGE_PART_NAME, "style-update");
        element.setAttribute("target-id", targetId);
        element.setAttribute("name", attributeName);
        element.setAttribute("value", attributeValue);
    }
    
    /**
     * Configures all child elements of a "content" element to use the XHTML
     * namespace. 
     * 
     * @param htmlFragment an HTML document fragment
     */
    private static void setContentNamespace(DocumentFragment htmlFragment) {
        Node childNode = htmlFragment.getFirstChild();
        while (childNode != null) {
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                ((Element) childNode).setAttribute("xmlns", XHTML_NAMESPACE);
            }
            childNode = childNode.getNextSibling();
        }
    }
}
