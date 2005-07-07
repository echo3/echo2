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

package nextapp.echo2.testapp.interactive.testscreen;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.layout.SplitPaneLayoutData;
import nextapp.echo2.testapp.interactive.InteractiveApp;

/**
 * Test to initiate a client-server push "loop" that autonomously clicks random
 * on-screen buttons.
 */
public class PushGhostTest extends Column {
    
    /**
     * Default constructor.
     */
    public PushGhostTest() {
        SplitPaneLayoutData splitPaneLayoutData = new SplitPaneLayoutData();
        splitPaneLayoutData.setInsets(new Insets(10));
        setLayoutData(splitPaneLayoutData);
        
        setCellSpacing(new Extent(20));
        
        Label label; 
        label = new Label("This test will cause the application to continuously push asynchronous updates "
                + "to the client by clicking buttons randomly on the screen.  Once started, the test cannot be "
                + "stopped until your application session is destroyed, i.e., by exiting your browser or "
                + "manually clearing the session cookie.");
        add(label);
        
        if (InteractiveApp.LIVE_DEMO_SERVER) {
            label = new Label("Because you are visiting this application on the nextapp.com server, we have disabled "
                    + "the options to run this test indefinitely and/or with a 0ms callback interval.  If you install this "
                    + "application on your own server, you will be given the option of running the more extreme version "
                    + "of this test.  A binary Web Archive (WAR file) version of this application is provided by the "
                    + "standard Echo2 download.");
            add(label);
        }
        
        Button oneMinuteStartButton = new Button("Start Ghost Click Test (Runtime: 20s, Callback interval: 500ms)");
        oneMinuteStartButton.setStyleName("Default");
        oneMinuteStartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                InteractiveApp app = (InteractiveApp)getApplicationInstance();
                app.startGhostTask(500, 20000);
            }
        });
        add(oneMinuteStartButton);
        
        if (!InteractiveApp.LIVE_DEMO_SERVER) {
            Button oneMinuteFastStartButton = new Button("Start Ghost Click Test (Runtime: 20s, Callback interval: 0ms)");
            oneMinuteFastStartButton.setStyleName("Default");
            oneMinuteFastStartButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    InteractiveApp app = (InteractiveApp)getApplicationInstance();
                    app.startGhostTask(0, 20000);
                }
            });
            add(oneMinuteFastStartButton);
            
            Button indefiniteStartButton = new Button("Start Ghost Click Test (Runtime: Indefinite, Callback interval: 0ms)");
            indefiniteStartButton.setStyleName("Default");
            indefiniteStartButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    InteractiveApp app = (InteractiveApp)getApplicationInstance();
                    app.startGhostTask(0, 0);
                }
            });
            add(indefiniteStartButton);
        }
    }
}