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

package nextapp.echo2.testapp.interactive;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Window;
import nextapp.echo2.app.WindowPane;
import nextapp.echo2.webcontainer.ContainerContext;

/**
 * Interactive Test Application Instance.
 * <p>
 * <b>WARNING TO DEVELOPERS:</b>
 * <p>
 * Beware that the Interactive Test Application is not a good example of an
 * Echo application.  The intent of this application is to serve as a running
 * testbed for the Echo framework.  As such, the requirements of this 
 * application are dramatically different from a production internet 
 * application.  There is stuff in this code that is downright absurd.  
 * Please do not look to this application to see good design
 * practices for building your own Echo apps--you will not find them here.
 */
public class InteractiveApp extends ApplicationInstance {

    public static final boolean LIVE_DEMO_SERVER;
    static {
        boolean liveDemoServer;
        try {
            if ("true".equals(System.getProperties().getProperty("nextapp.echo2.demoserver"))) {
                liveDemoServer = true;
            } else {
                liveDemoServer = false;
            }
        } catch (SecurityException ex) {
            liveDemoServer = false;
        }
        LIVE_DEMO_SERVER = liveDemoServer;
    }
    
    // BUGBUG. This needs to be done more cleanly...e.g., have the app itself start/stop/manage the ghost test.
    boolean ghostTestRunning = false;
    
    public static final String ACTION_WINDOW_PANE_TEST = "windowPaneTest";
    
    public static InteractiveApp getApp() {
        return (InteractiveApp) ApplicationInstance.getActive();
    }

    private Window mainWindow;
    private ConsoleWindowPane console;
    
    public void addDialogWindowPane(WindowPane windowPane) {
        mainWindow.getContent().add(windowPane);
    }
    
    public void consoleWrite(String message) {
        //BUGBUG. clean this up.
        if (console == null) {
            console = new ConsoleWindowPane();
            getDefaultWindow().getContent().add(console);
        } else if (console.getParent() == null) {
            getDefaultWindow().getContent().add(console);
        }
        console.writeMessage(message);
    }
    
    public void displayTestPane() {
        mainWindow.setContent(new TestPane());
    }
    
    public void displayWelcomePane() {
        mainWindow.setContent(new WelcomePane());
    }

    /**
     * @see nextapp.echo2.app.ApplicationInstance#init()
     */
    public Window init() {
        setStyleSheet(Styles.DEFAULT_STYLE_SHEET);
        mainWindow = new Window();
        mainWindow.setTitle("NextApp Echo2 Test Application");
        mainWindow.setContent(new WelcomePane());
        
        ContainerContext cc = (ContainerContext) getContextProperty(ContainerContext.CONTEXT_PROPERTY_NAME);
        if (!LIVE_DEMO_SERVER && cc.getInitialParameterMap().containsKey("ghost")) {
            startGhostTask(0, 0);
        }
        
        return mainWindow;
    }

    public Window getMainWindow() {
        return mainWindow;
    }

    public void removeDialogWindowPane(WindowPane windowPane) {
        mainWindow.getContent().remove(windowPane);
    }
    
    public void startGhostTask(int interval, long runTime) {
        if (ghostTestRunning) {
            return;
        }
        GhostTask.start(this, interval, runTime);
    }
}
