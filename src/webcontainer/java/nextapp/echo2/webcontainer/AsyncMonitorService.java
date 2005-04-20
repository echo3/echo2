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
import nextapp.echo2.webrender.server.Connection;
import nextapp.echo2.webrender.server.ContentType;
import nextapp.echo2.webrender.server.Service;

/**
 * A service which handles server poll requests to determine if any 
 * asynchronous operations have completed since the last server interaction,
 * such that the client might resynchronize with the server.
 */
public class AsyncMonitorService 
implements Service {
    
    
    public static final Service INSTANCE = new AsyncMonitorService();

    public static final String SERVICE_ID = "Echo.AsyncMonitor";
    
    /**
     * @see nextapp.echo2.webrender.server.Service#getId()
     */
    public String getId() {
        return SERVICE_ID;
    }
    
    /**
     * @see nextapp.echo2.webrender.server.Service#getVersion()
     */
    public int getVersion() {
        return DO_NOT_CACHE;
    }
    
    /**
     * @see nextapp.echo2.webrender.server.Service#service(nextapp.echo2.webrender.server.Connection)
     */
    public void service(Connection conn) throws IOException {
        ContainerInstance ci = (ContainerInstance) conn.getUserInstance();
        ApplicationInstance app = ci.getApplicationInstance();
        conn.setContentType(ContentType.TEXT_XML);
        if (app.hasMessagesQueued()) {
            conn.getWriter().write("<asyncmonitor requestsync=\"true\"/>");
        } else {
            conn.getWriter().write("<asyncmonitor requestsync=\"false\"/>");
        }
    }
}
