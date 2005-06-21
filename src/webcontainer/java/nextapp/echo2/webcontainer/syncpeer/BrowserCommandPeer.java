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

import nextapp.echo2.app.Command;
import nextapp.echo2.webcontainer.CommandSynchronizePeer;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.command.BrowserOpenWindowCommand;
import nextapp.echo2.webcontainer.command.BrowserRedirectCommand;
import nextapp.echo2.webrender.ServerMessage;
import nextapp.echo2.webrender.Service;
import nextapp.echo2.webrender.WebRenderServlet;
import nextapp.echo2.webrender.service.JavaScriptService;

/**
 * A <code>CommandSynchronizePeer</code> implementation for 
 * browser control commands.
 */
public class BrowserCommandPeer 
implements CommandSynchronizePeer {

    /**
     * Service to provide supporting JavaScript library.
     */
    private static final Service BROWSER_COMMAND_SERVICE = JavaScriptService.forResource("Echo.BrowserCommand", 
            "/nextapp/echo2/webcontainer/resource/js/BrowserCommand.js");

    static {
        WebRenderServlet.getServiceRegistry().add(BROWSER_COMMAND_SERVICE);
    }
    
    /**
     * @see nextapp.echo2.webcontainer.CommandSynchronizePeer#render(
     *      nextapp.echo2.webcontainer.RenderContext, nextapp.echo2.app.Command)
     */
    public void render(RenderContext rc, Command command) {
        if (command instanceof BrowserOpenWindowCommand) {
            renderOpenWindow(rc, (BrowserOpenWindowCommand) command);
        } else if (command instanceof BrowserRedirectCommand) {
            renderRedirect(rc, (BrowserRedirectCommand) command);
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * Renders a <code>BrowserOpenWindowCommand</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param command the command
     */
    private void renderOpenWindow(RenderContext rc, BrowserOpenWindowCommand command) {
        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(BROWSER_COMMAND_SERVICE.getId());
        Element openWindowElement = serverMessage.appendPartDirective(ServerMessage.GROUP_ID_POSTUPDATE, 
                "EchoBrowserCommand", "open-window");
        openWindowElement.setAttribute("uri", command.getUri());
        if (command.getName() != null) {
            openWindowElement.setAttribute("name", command.getName());
        }
        if (command.getFeatures() != null) {
            openWindowElement.setAttribute("features", command.getFeatures());
        }
        openWindowElement.setAttribute("replace", command.isReplace() ? "true" : "false");
    }

    /**
     * Renders a <code>BrowserRedirectCommand</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param command the command
     */
    private void renderRedirect(RenderContext rc, BrowserRedirectCommand command) {
        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(BROWSER_COMMAND_SERVICE.getId());
        Element redirectElement = serverMessage.appendPartDirective(ServerMessage.GROUP_ID_POSTUPDATE, 
                "EchoBrowserCommand", "redirect");
        redirectElement.setAttribute("uri", command.getUri());
    }
}
