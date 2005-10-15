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

import java.io.IOException;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.webrender.BaseHtmlDocument;
import nextapp.echo2.webrender.Connection;
import nextapp.echo2.webrender.ContentType;
import nextapp.echo2.webrender.Service;
import nextapp.echo2.webrender.WebRenderServlet;
import nextapp.echo2.webrender.output.CssStyle;
import nextapp.echo2.webrender.service.CoreServices;

/**
 * Completely re-renders a browser window.
 * This is the default service invoked when the user visits an application.
 */
public class WindowHtmlService 
implements Service {
    
    public static final WindowHtmlService INSTANCE = new WindowHtmlService();

    /**
     * Root element identifier.
     */
    public static final String ROOT_ID = "c_root";
    
    /**
     * @see nextapp.echo2.webrender.Service#getId()
     */
    public String getId() {
        return WebRenderServlet.SERVICE_ID_DEFAULT;
    }

    /**
     * @see nextapp.echo2.webrender.Service#getVersion()
     */
    public int getVersion() {
        return DO_NOT_CACHE;
    }

    /**
     * @see nextapp.echo2.webrender.Service#service(nextapp.echo2.webrender.Connection)
     */
    public void service(Connection conn) throws IOException {
        ContainerInstance ci = (ContainerInstance) conn.getUserInstance();
        conn.setContentType(ContentType.TEXT_HTML);
        
        boolean debug = !("false".equals(conn.getServlet().getInitParameter("echo2.debug")));

        BaseHtmlDocument baseDoc = new BaseHtmlDocument(ROOT_ID);
        baseDoc.setGenarator(ApplicationInstance.ID_STRING);
        baseDoc.addJavaScriptInclude(ci.getServiceUri(CoreServices.CLIENT_ENGINE));

        // Add initialization directive.
        baseDoc.getBodyElement().setAttribute("onload", "EchoClientEngine.init('" + ci.getServletUri() + "', " 
                + debug + ");");
        
        // Set body element CSS style.
        CssStyle cssStyle = new CssStyle();
        cssStyle.setAttribute("position", "absolute");
        cssStyle.setAttribute("font-family", "verdana, arial, helvetica, sans-serif");
        cssStyle.setAttribute("font-size", "10pt");
        cssStyle.setAttribute("height", "100%");
        cssStyle.setAttribute("width", "100%");
        cssStyle.setAttribute("padding", "0px");
        cssStyle.setAttribute("margin", "0px");
        cssStyle.setAttribute("overflow", "hidden");
        baseDoc.getBodyElement().setAttribute("style", cssStyle.renderInline());
        
        // Render.
        baseDoc.render(conn.getWriter());
    }
}
