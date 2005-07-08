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

package nextapp.echo2.webcontainer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import nextapp.echo2.webrender.ServerDelayMessage;
import nextapp.echo2.webrender.output.CssStyle;
import nextapp.echo2.webrender.output.XmlDocument;

/**
 * The default Web Container <code>ServerDelayMessage</code>.
 */
public class DefaultServerDelayMessage 
extends ServerDelayMessage {
    
    /**
     * Singleton instance.
     */
    public static final ServerDelayMessage INSTANCE = new DefaultServerDelayMessage();

    /**
     * Message content.
     */
    private Node messageNode;
    
    /**
     * Creates the <code>DefaultServerDelayMessage</code>
     */
    private DefaultServerDelayMessage() {
        XmlDocument xmlDocument = new XmlDocument("table", null, null, null);
        Document document = xmlDocument.getDocument();
        Element tableElement = document.getDocumentElement();
        CssStyle tableCssStyle = new CssStyle();
        tableCssStyle.setAttribute("width", "100%");
        tableCssStyle.setAttribute("height", "100%");
        tableCssStyle.setAttribute("border", "0px");
        tableCssStyle.setAttribute("padding", "0px");
        tableElement.setAttribute("style", tableCssStyle.renderInline());
        
        Element tbodyElement = document.createElement("tbody");
        tableElement.appendChild(tbodyElement);
        
        Element trElement = document.createElement("tr");
        tbodyElement.appendChild(trElement);
        
        Element tdElement = document.createElement("td");
        trElement.appendChild(tdElement);
        
        Element divElement = document.createElement("div");
        CssStyle divCssStyle = new CssStyle();
        divCssStyle.setAttribute("margin-top", "40px");
        divCssStyle.setAttribute("margin-left", "auto");
        divCssStyle.setAttribute("margin-right", "auto");
        divCssStyle.setAttribute("background-color", "#afafbf");
        divCssStyle.setAttribute("color", "#000000");
        divCssStyle.setAttribute("padding", "40px");
        divCssStyle.setAttribute("width", "200px");
        divCssStyle.setAttribute("border", "groove 2px #bfbfcf");
        divCssStyle.setAttribute("font-family", "verdana, arial, helvetica, sans-serif");
        divCssStyle.setAttribute("font-size", "10pt");
        divCssStyle.setAttribute("text-align", "center");
        divElement.setAttribute("style", divCssStyle.renderInline());
        divElement.setAttribute("id", ELEMENT_ID_LONG_MESSAGE);
        divElement.appendChild(document.createTextNode("Please wait..."));
        tdElement.appendChild(divElement);
        
        messageNode = tableElement;
    }
    
    /**
     * @see nextapp.echo2.webrender.ServerDelayMessage#getMessage()
     */
    public Node getMessage() {
        return messageNode;
    }
}