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

package nextapp.echo2.webcontainer.syncpeer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.TextArea;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webrender.ClientProperties;
import nextapp.echo2.webrender.ServerMessage;
import nextapp.echo2.webrender.output.CssStyle;
import nextapp.echo2.webrender.servermessage.EventUpdate;

/**
 * Synchronization peer for <code>nextapp.echo2.app.TextArea</code> components.
 * <p>
 * This class should not be extended or used by classes outside of the
 * Echo framework.
 */
public class TextAreaPeer extends TextComponentPeer {

    /**
     * @see nextapp.echo2.webcontainer.DomUpdateSupport#renderHtml(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, org.w3c.dom.Node, nextapp.echo2.app.Component)
     */
    public void renderHtml(RenderContext rc, ServerComponentUpdate addUpdate, Node parentNode, Component component) {
        TextArea textArea = (TextArea) component;
        String elementId = ContainerInstance.getElementId(component);
        
        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(TEXT_COMPONENT_SERVICE.getId(), true);
        
        Element textAreaElement = parentNode.getOwnerDocument().createElement("textarea");
        textAreaElement.setAttribute("id", elementId);
        
        String value = textArea.getText();
        if (value != null) {
            if (rc.getContainerInstance().getClientProperties().getBoolean(
                    ClientProperties.QUIRK_TEXTAREA_NEWLINE_OBLITERATION)) {
                // This implementation relies on the fact that our IE-specific 
                // document.importNode implementation will assign HTML attributes
                // using JavaScript.
                textAreaElement.setAttribute("value", value);
            } else {
                textAreaElement.appendChild(rc.getServerMessage().getDocument().createTextNode(value));
            }
        }

        CssStyle cssStyle = createBaseCssStyle(rc, textArea);
        if (cssStyle.hasAttributes()) {
            textAreaElement.setAttribute("style", cssStyle.renderInline());
        }
        
        parentNode.appendChild(textAreaElement);
        EventUpdate.renderEventAdd(rc.getServerMessage(), "keyup", elementId, "EchoTextComponent.processUpdate");
        EventUpdate.renderEventAdd(rc.getServerMessage(), "blur", elementId, "EchoTextComponent.processUpdate");
    }
}
