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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.PasswordField;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.DomUpdateSupport;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webrender.clientupdate.EventUpdate;
import nextapp.echo2.webrender.clientupdate.ServerMessage;
import nextapp.echo2.webrender.output.CssStyle;

import org.w3c.dom.Element;

/**
 * Synchronization peer for <code>nextapp.echo2.app.TextField</code> components.
 */
public class TextFieldPeer extends TextComponentPeer
implements DomUpdateSupport {

    /**
     * @see nextapp.echo2.webcontainer.DomUpdateSupport#renderHtml(nextapp.echo2.webcontainer.RenderContext, 
     *      nextapp.echo2.app.update.ServerComponentUpdate, org.w3c.dom.Element, nextapp.echo2.app.Component)
     */
    public void renderHtml(RenderContext rc, ServerComponentUpdate addUpdate,
            Element parent, Component component) {
        TextField textField = (TextField) component;
        String elementId = ContainerInstance.getElementId(textField);

        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(TEXT_COMPONENT_SERVICE.getId(), true);
        
        Element inputElement = parent.getOwnerDocument().createElement("input");
        inputElement.setAttribute("id", elementId);
        if (textField instanceof PasswordField) {
            inputElement.setAttribute("type", "password");
        } else {
            inputElement.setAttribute("type", "text");
        }
        String value = textField.getDocument().getText();
        if (value != null) {
            inputElement.setAttribute("value", value);
        }
        
        CssStyle cssStyle = createBaseCssStyle(rc, textField);
        if (cssStyle.hasAttributes()) {
            inputElement.setAttribute("style", cssStyle.renderInline());
        }
        
        parent.appendChild(inputElement);
        //BUGBUG. Mozilla not responding to blurs correctly...using keyup as well.
        EventUpdate.createEventAdd(rc.getServerMessage(), "keyup", elementId, "EchoTextComponent.processUpdate");
        EventUpdate.createEventAdd(rc.getServerMessage(), "blur", elementId, "EchoTextComponent.processUpdate");
    }
}
