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

package nextapp.echo2.testapp.auth;

import java.security.Principal;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Border;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Window;
import nextapp.echo2.testapp.interactive.InteractiveApp;
import nextapp.echo2.webcontainer.ContainerContext;

public class AuthApp extends ApplicationInstance {

    /**
     * @see nextapp.echo2.app.ApplicationInstance#init()
     */
    public Window init() {
        if (InteractiveApp.LIVE_DEMO_SERVER) {
            throw new RuntimeException("Authentication test disabled on live demo server.");
        }
        
        Window mainWindow = new Window();
        mainWindow.setTitle("NextApp Echo2 Authentication Test Application");
        
        ContentPane content = new ContentPane();
        mainWindow.setContent(content);
        
        Column mainColumn = new Column();
        mainColumn.setBorder(new Border(new Extent(4), Color.BLUE, Border.STYLE_OUTSET));
        mainColumn.setInsets(new Insets(40));
        mainColumn.setCellSpacing(new Extent(20));
        content.add(mainColumn);
        
        ContainerContext containerContext = (ContainerContext) getContextProperty(ContainerContext.CONTEXT_PROPERTY_NAME);
        
        Principal principal = containerContext.getUserPrincipal();
        mainColumn.add(new Label("getUserPrincipal(): " + (principal == null ? "null" : principal.getName())));
        mainColumn.add(new Label("isUserInRole(\"role1\"): " + containerContext.isUserInRole("role1")));
        
        return mainWindow;
    }
}
