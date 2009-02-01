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

package nextapp.echo2.testapp.interactive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import nextapp.echo2.app.util.Uid;
import nextapp.echo2.webcontainer.ContainerContext;
import nextapp.echo2.webrender.ClientProperties;
import nextapp.echo2.webrender.Connection;
import nextapp.echo2.webrender.ContentType;
import nextapp.echo2.webrender.ServerDelayMessage;
import nextapp.echo2.webrender.Service;
import nextapp.echo2.webrender.WebRenderServlet;
import nextapp.echo2.webrender.output.HtmlDocument;
import nextapp.echo2.webrender.output.XmlDocument;

/**
 * An example of a more creative <code>ServerDelayMessage</code>.
 * <p>
 * Creating a custom <code>ServerDelayMessage</code>
 * is a fairly advanced activity that requires working with the Echo
 * Web Render Engine API.
 */
public class CoolDelayMessage extends ServerDelayMessage {

    private static int BUFFER_SIZE = 4096;
    private static String IMAGE_RESOURCE_NAME = Styles.IMAGE_PATH + "ShadowOverlay.png";
    
    private static Service IMAGE_SERVICE = new ImageService();
    static {
        WebRenderServlet.getServiceRegistry().add(IMAGE_SERVICE);
    }
    
    private static class ImageService implements Service {

        private static final String SERVICE_ID = Uid.generateUidString();
        
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
            return 0;
        }

        /**
         * @see nextapp.echo2.webrender.Service#service(nextapp.echo2.webrender.Connection)
         */
        public void service(Connection conn) throws IOException {
            conn.setContentType(ContentType.IMAGE_PNG);
            OutputStream out = conn.getOutputStream();

            InputStream in = null;
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = 0;
            
            try {
                in = CoolDelayMessage.class.getResourceAsStream(IMAGE_RESOURCE_NAME);
                if (in == null) {
                    throw new IllegalArgumentException("Specified resource does not exist: " + IMAGE_RESOURCE_NAME + ".");
                }
                do {
                    bytesRead = in.read(buffer);
                    if (bytesRead > 0) {
                        out.write(buffer, 0, bytesRead);
                    }
                } while (bytesRead > 0);
            } finally {
                if (in != null) { try { in.close(); } catch (IOException ex) { } } 
            }
        }
    }
    
    private Element messageElement;
    
    public CoolDelayMessage(ContainerContext containerContext, String messageText) {
        XmlDocument xmlDocument = new XmlDocument("div", null, null, HtmlDocument.XHTML_1_0_NAMESPACE_URI);
        Document document = xmlDocument.getDocument();
        Element divElement = document.getDocumentElement();
        divElement.setAttribute("id", ELEMENT_ID_MESSAGE);
        divElement.setAttribute("style", "position:absolute;top:0px;left:0px;width:100%;height:100%;cursor:wait;"
                + "margin:0px;padding:0px;visibility:hidden;z-index:10000;");

        Element tableElement = document.createElement("table");
        tableElement.setAttribute("style", "width:100%;height:100%;border:0px;padding:0px;");
        divElement.appendChild(tableElement);
        
        Element tbodyElement = document.createElement("tbody");
        tableElement.appendChild(tbodyElement);
        
        Element trElement = document.createElement("tr");
        tbodyElement.appendChild(trElement);
        
        Element tdElement = document.createElement("td");
        tdElement.setAttribute("style", "width:100%;height:100%;");
        tdElement.setAttribute("valign", "middle");
        tdElement.setAttribute("align", "center");
        trElement.appendChild(tdElement);
        
        Element longDivElement = document.createElement("div");
        longDivElement.setAttribute("id", ELEMENT_ID_LONG_MESSAGE);
        String longDivStyleText = "color:#4f4f4f;width:277px;padding-top:120px;height:156px;"
                + "font-family:verdana,arial,helvetica,sans-serif;font-size:14pt;font-weight:bold;font-style:italic;"
                + "text-align:center;";
        if (containerContext.getClientProperties().getBoolean(ClientProperties.PROPRIETARY_IE_PNG_ALPHA_FILTER_REQUIRED)) {
            // Use Internet Explorer PNG filter hack to achieve transparency.
            longDivStyleText += "filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" 
                    + containerContext.getServiceUri(IMAGE_SERVICE) + "', sizingMethod='scale')";
        } else {
            longDivStyleText += "background-image:url(" + containerContext.getServiceUri(IMAGE_SERVICE) + ");";
        }
        longDivElement.setAttribute("style", longDivStyleText);

        longDivElement.appendChild(document.createTextNode(messageText));
        tdElement.appendChild(longDivElement);
        
        messageElement = divElement;
    }
    
    /**
     * @see nextapp.echo2.webrender.ServerDelayMessage#getMessage()
     */
    public Element getMessage() {
        return messageElement;
    }
}
