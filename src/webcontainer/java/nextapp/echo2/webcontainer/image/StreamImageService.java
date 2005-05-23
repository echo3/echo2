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

package nextapp.echo2.webcontainer.image;

import java.io.IOException;

import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.StreamImageReference;
import nextapp.echo2.webrender.server.Connection;
import nextapp.echo2.webrender.server.ContentType;
import nextapp.echo2.webrender.server.WebRenderServlet;

/**
 * Renders a <code>StreamImageReference</code> to the client.
 */
public class StreamImageService extends AbstractImageService {

    private static final String SERVICE_ID = "Echo.StreamImage"; 
    
    public static final StreamImageService INSTANCE = new StreamImageService();

    static {
        WebRenderServlet.getServiceRegistry().add(INSTANCE);
    }
    
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
        return 0; // Enable caching.
    }

    /**
     * @see nextapp.echo2.webcontainer.image.AbstractImageService#renderImage(
     *      nextapp.echo2.webrender.server.Connection, nextapp.echo2.app.ImageReference)
     */
    public void renderImage(Connection conn, ImageReference imageReference) 
    throws IOException {
        if (!(imageReference instanceof StreamImageReference)) {
            throw new IOException("Image is not a StreamImageReference.");
        }
        StreamImageReference streamImageReference = (StreamImageReference) imageReference;
        conn.setContentType(new ContentType(streamImageReference.getContentType(), true));
        streamImageReference.render(conn.getOutputStream());
    }
}
