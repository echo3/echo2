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

package nextapp.echo2.webrender;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import nextapp.echo2.webrender.output.XmlDocument;

/**
 * The outgoing XML message which synchronizes the state of the client to that
 * of the server.
 */
public class ServerMessage extends XmlDocument {
    
    /**
     * Constant for the use with <code>setRootLayoutDirection()</code> 
     * indicating a left-to-right layout direction.
     */
    public static final int LEFT_TO_RIGHT = 0;

    /**
     * Constant for the use with <code>setRootLayoutDirection()</code> 
     * indicating a right-to-left layout direction.
     */
    public static final int RIGHT_TO_LEFT = 1;

    /**
     * A hash table associating <code>ItemizedDirectiveLookupKey</code>s to
     * corresponding <code>Element</code>s in the message.
     */
    private Map itemizedDirectivesMap = new HashMap();

    /**
     * Representation of the information required to look up a suitable Itemized
     * Directive <code>Element</code>. Instances are used as keys in the
     * <code>itemizedDirectivesMap</code>. This class provides a
     * <code>getHashCode()</code> implementation for efficient lookups.
     */
    private class ItemizedDirectiveLookupKey {

        String groupId;
        String processor;
        String directiveName;
        String[] keyAttributeNames;
        String[] keyAttributeValues;
        int hashCode;

        /**
         * Creates an <code>ItemizedDirectiveLookupKey</code> based on the
         * provided description.
         * 
         * @param groupId the identifier of the target message part group
         * @param processor the name of the client-side processor object which
         *        will process the message part containing the directive, e.g.,
         *        "EchoEventUpdate", or "EchoDomUpdate"
         * @param directiveName the name of the directive, e.g., "event-add" or
         *        "dom-remove".
         * @param keyAttributeNames
         * @param keyAttributeValues
         */
        private ItemizedDirectiveLookupKey(String groupId, String processor, String directiveName, String[] keyAttributeNames,
                String[] keyAttributeValues) {
            super();
            this.groupId = groupId;
            this.processor = processor;
            this.directiveName = directiveName;
            this.keyAttributeNames = keyAttributeNames;
            this.keyAttributeValues = keyAttributeValues;
            this.hashCode = groupId.hashCode() ^ processor.hashCode() ^ directiveName.hashCode();
            for (int i = 0; i < keyAttributeNames.length; ++i) {
                this.hashCode ^= keyAttributeNames[i].hashCode() ^ keyAttributeValues[i].hashCode();
            }
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object o) {
            if (!(o instanceof ItemizedDirectiveLookupKey)) {
                return false;
            }
            ItemizedDirectiveLookupKey that = (ItemizedDirectiveLookupKey) o;

            if (!this.groupId.equals(that.groupId)) {
                return false;
            }
            if (!this.processor.equals(that.processor)) {
                return false;
            }
            if (!this.directiveName.equals(that.directiveName)) {
                return false;
            }
            if (this.keyAttributeNames.length != that.keyAttributeNames.length) {
                return false;
            }
            for (int i = 0; i < keyAttributeValues.length; ++i) {
                if (!(this.keyAttributeValues[i].equals(that.keyAttributeValues[i]))) {
                    return false;
                }
            }
            for (int i = 0; i < keyAttributeNames.length; ++i) {
                if (!(this.keyAttributeNames[i].equals(that.keyAttributeNames[i]))) {
                    return false;
                }
            }

            return true;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return hashCode;
        }
    }

    /**
     * Constant for the "init" message part group. Message parts in this group are
     * processed before the "preremove", "remove" "update", and "postupdate"
     * groups.
     */
    public static final String GROUP_ID_INIT = "init";

    /**
     * Constant for the "preremove" message part group. Message parts in this group
     * are processed after the "init" group. Message parts in this group are
     * processed before the "remove", "update" and "postupdate" groups.
     */
    public static final String GROUP_ID_PREREMOVE = "preremove";

    /**
     * Constant for the "remove" message part group. Message parts in this group
     * are processed after the "init" and "preremove" groups. Message parts in
     * this group are processed before the "update" and "postupdate" groups.
     * This group is used for removing elements from the DOM.
     */
    public static final String GROUP_ID_REMOVE = "remove";

    /**
     * Constant for the "update" message part group. Message parts in this group
     * are processed after the "init", "preremove" and "remove" groups.
     * Message parts in this group are processed before the "postupdate" group.
     * This group is used for adding elements to the DOM.
     */
    public static final String GROUP_ID_UPDATE = "update";

    /**
     * Constant for the "postupdate" message part group. Message parts in this
     * group are processed after the "init", "preremove", "remove" and "update"
     * groups.
     */
    public static final String GROUP_ID_POSTUPDATE = "postupdate";

    /** Set of added script libraries. */
    private Set addedLibraries;

    /**
     * DOM <code>libraries</code> Element to which <code>library</code>
     * elements are added to represent individual dynamically loaded JavaScript
     * libraries.
     */
    private Element librariesElement;

    /** Root DOM <code>server-message</code> element. */
    private Element serverMessageElement;

    /**
     * Creates a new <code>ServerMessage</code>.
     */
    public ServerMessage() {
        super("server-message", null, null, "http://www.nextapp.com/products/echo2/svrmsg/servermessage");
        Document document = getDocument();
        serverMessageElement = document.getDocumentElement();
        librariesElement = document.createElement("libraries");
        serverMessageElement.appendChild(librariesElement);

        // Add basic part groups.
        addPartGroup(GROUP_ID_INIT);
        addPartGroup(GROUP_ID_PREREMOVE);
        addPartGroup(GROUP_ID_REMOVE);
        addPartGroup(GROUP_ID_UPDATE);
        addPartGroup(GROUP_ID_POSTUPDATE);
    }

    /**
     * Adds a JavaScript library service to be dynamically loaded.
     * 
     * @param serviceId the id of the service to load (the service must return
     *        JavaScript code with content-type "text/javascript")
     */
    public void addLibrary(String serviceId) {
        if (addedLibraries == null) {
            addedLibraries = new HashSet();
        }
        if (addedLibraries.contains(serviceId)) {
            return;
        }
        Element libraryElement = getDocument().createElement("library");
        libraryElement.setAttribute("service-id", serviceId);
        librariesElement.appendChild(libraryElement);
        addedLibraries.add(serviceId);
    }

    /**
     * Adds a "group" to the document. Part groups enable certain groups of
     * operations, e.g., remove operations, to be performed before others, e.g.,
     * add operations.
     * 
     * @param groupId the identifier of the group
     * @return the created "message-part-group" element.
     */
    public Element addPartGroup(String groupId) {
        Element messagePartGroupElement = getDocument().createElement("message-part-group");
        messagePartGroupElement.setAttribute("id", groupId);
        serverMessageElement.appendChild(messagePartGroupElement);
        return messagePartGroupElement;
    }

    /**
     * Retrieves the "message-part-group" element pertaining to a specific group.
     * 
     * @param groupId the id of the group
     * @return the "message-part-group" element
     */
    public Element getPartGroup(String groupId) {
        NodeList groupList = serverMessageElement.getElementsByTagName("message-part-group");
        int length = groupList.getLength();
        for (int i = 0; i < length; ++i) {
            Element groupElement = (Element) groupList.item(i);
            if (groupId.equals(groupElement.getAttribute("id"))) {
                return groupElement;
            }
        }
        return null;
    }

    /**
     * Adds a "message-part" to the document that will be processed by the
     * specified client-side processor object.
     * 
     * @param groupId the id of the group to which the "message-part" element
     *        should be added
     * @param processor the name of the client-side processor object which will
     *        process the message part, e.g., "EchoEventUpdate", or
     *        "EchoDomUpdate"
     * @return the created "message-part" element
     */
    public Element addPart(String groupId, String processor) {
        Element messagePartGroupElement = getPartGroup(groupId);
        Element messagePartElement = getDocument().createElement("message-part");
        messagePartElement.setAttribute("processor", processor);
        messagePartGroupElement.appendChild(messagePartElement);
        return messagePartElement;
    }

    /**
     * Creates and appends a directive element beneath to a message part.
     * Attempts to append the directive to an existing message part if the last
     * message part in the specified group happens to have the same processor as
     * is specified by the <code>processor</code> argument. If this is not
     * possible, a new "message-part" element is created and the directive is
     * added to it.
     * 
     * @param groupId
     * @param processor the name of the client-side processor object which will
     *        process the message part, e.g., "EchoEventUpdate", or
     *        "EchoDomUpdate"
     * @param directiveName the name of the directive, e.g., "event-add" or
     *        "dom-remove".
     * @return the directive element
     */
    public Element appendPartDirective(String groupId, String processor, String directiveName) {
        Element messagePartElement = null;
        Element groupElement = getPartGroup(groupId);

        Element lastChild = (Element) groupElement.getLastChild();
        if (lastChild != null && processor.equals(lastChild.getAttribute("processor"))) {
            messagePartElement = lastChild;
        } else {
            messagePartElement = addPart(groupId, processor);
        }
        
        Element directiveElement = getDocument().createElement(directiveName);
        messagePartElement.appendChild(directiveElement);
        return directiveElement;
    }

    /**
     * Creates or retrieves a suitable "Itemized Directive" element. Itemized
     * Directives may be used to create more bandwidth-efficient ServerMessage
     * output in cases where a particular operation may need to be performed on
     * a significant number of targets. In the case that the directive must be
     * created, it will be added to the message. Repeated invocations of this
     * method with equivalent values of all parameters will result in the same
     * directive being returned each time.
     * 
     * This method should only be used for adding directives to the 
     * <code>GROUP_ID_PREREMOVE</code> and <code>GROUP_ID_POSTUPDATE</code>
     * groups as itemized directives will not be executed in-order, which
     * will cause problems if they are used for to manipulate the DOM.
     * 
     * @param groupId the identifier of the target message part group,
     *        either <code>GROUP_ID_PREREMOVE</code> or 
     *        <code>GROUP_ID_POSTUPDATE</code>
     * @param processor the name of the client-side processor object which will
     *        process the message part containing the directive, e.g.,
     *        "EchoEventUpdate", or "EchoDomUpdate"
     * @param directiveName the name of the directive, e.g., "event-add" or
     *        "dom-remove"
     * @param keyAttributeNames the names of the key attributes
     * @param keyAttributeValues the values of the key attributes
     * @return the created/retrieved directive element
     */
    public Element getItemizedDirective(String groupId, String processor, String directiveName, String[] keyAttributeNames,
            String[] keyAttributeValues) {
        ItemizedDirectiveLookupKey itemizedDirectiveLookupKey = new ItemizedDirectiveLookupKey(groupId, processor, directiveName,
                keyAttributeNames, keyAttributeValues);
        Element element = (Element) itemizedDirectivesMap.get(itemizedDirectiveLookupKey);
        if (element == null) {
            element = appendPartDirective(groupId, processor, directiveName);
            for (int i = 0; i < keyAttributeNames.length; ++i) {
                element.setAttribute(keyAttributeNames[i], keyAttributeValues[i]);
            }
            itemizedDirectivesMap.put(itemizedDirectiveLookupKey, element);
        }
        return element;
    }
    
    /**
     * Sets the interval between asynchronous requests to the server to check
     * for server-pushed updates.
     * 
     * @param newValue the new interval in milliseconds (a negative value will
     *        disable asynchronous requests)
     */
    public void setAsynchronousMonitorInterval(int newValue) {
        if (newValue < 0) {
            serverMessageElement.setAttribute("async-interval", "disable");
        } else {
            serverMessageElement.setAttribute("async-interval", Integer.toString(newValue));
        }
    }

    /**
     * Sets the element id of the root of the modal context. Only elements
     * within the modal context will be enabled. A <code>id</code> value of
     * null will disable the modal context, thus allowing ALL elements to be
     * enabled.
     * 
     * @param id the root element id of the modal context
     */
    public void setModalContextRootId(String id) {
        if (id == null) {
            serverMessageElement.setAttribute("modal-id", "");
        } else {
            serverMessageElement.setAttribute("modal-id", id);
        }
    }

    /**
     * Sets the root layout direction of the application, i.e., either
     * <code>LEFT_TO_RIGHT</code> or <code>RIGHT_TO_LEFT</code>.
     * 
     * @param layoutDirection the new layout direction
     */
    public void setRootLayoutDirection(int layoutDirection) {
        switch (layoutDirection) {
        case LEFT_TO_RIGHT:
            serverMessageElement.setAttribute("root-layout-direction", "ltr");
            break;
        case RIGHT_TO_LEFT:
            serverMessageElement.setAttribute("root-layout-direction", "rtl");
            break;
        default:
            throw new IllegalArgumentException("Illegal layout direction.");
        }
    }
    
    /**
     * Sets the numeric identifier for this transaction, which will be returned
     * in next client message.
     * 
     * @param transactionId the transaction identifier
     */
    public void setTransactionId(long transactionId) {
        serverMessageElement.setAttribute("trans-id", Long.toString(transactionId));
    }
}