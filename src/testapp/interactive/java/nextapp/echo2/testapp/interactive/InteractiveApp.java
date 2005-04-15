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

public class InteractiveApp extends ApplicationInstance {
    
    public static final String ACTION_WINDOW_PANE_TEST = "windowPaneTest";
    
    public static InteractiveApp getApp() {
        return (InteractiveApp) ApplicationInstance.getActive();
    }

    private Window mainWindow;
    
    public void addDialogWindowPane(WindowPane windowPane) {
        mainWindow.getContent().add(windowPane);
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
        mainWindow.setTitle("NextApp Echo2 Test Application [EARLY ACCESS/EXPERIMENTAL]");
        mainWindow.setContent(new WelcomePane());
        return mainWindow;
    }

    public Window getMainWindow() {
        return mainWindow;
    }

    public void removeDialogWindowPane(WindowPane windowPane) {
        mainWindow.getContent().remove(windowPane);
    }
}
