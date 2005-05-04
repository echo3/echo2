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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.TaskQueue;
import nextapp.echo2.app.Window;
import nextapp.echo2.app.button.AbstractButton;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.layout.SplitPaneLayoutData;
import nextapp.echo2.testapp.interactive.InteractiveApp;
import nextapp.echo2.testapp.interactive.Styles;
import nextapp.echo2.webcontainer.ContainerContext;

/**
 * 
 */
public class PushGhostTest extends Column {
    
    private static final boolean LIVE_DEMO_SERVER;
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
    

    private class RandomClickTask 
    implements Runnable {
        
        private boolean indefinite;
        private long stopTime;
        
        public RandomClickTask() {
            indefinite = true;
        }
        
        public RandomClickTask(long runTime) {
            stopTime = System.currentTimeMillis() + runTime;
        }
        
        public void clickRandomButton() {
            Window window = ApplicationInstance.getActive().getWindows()[0];
            List buttonList = new ArrayList();
            findButtons(buttonList, window);
            AbstractButton button = (AbstractButton) buttonList.get((int) (buttonList.size() * Math.random()));
            button.doAction();
        }
        
        public void findButtons(Collection foundButtons, Component component) {
            if (component instanceof AbstractButton) {
                foundButtons.add(component);
            }
            Component[] children = component.getComponents();
            for (int i = 0; i < children.length; ++i) {
                findButtons(foundButtons, children[i]);
            }
        }
        
        /**
         * @see nextapp.echo2.app.event.MessageListener#messageReceived(nextapp.echo2.app.event.MessageEvent)
         */
        public void run() {
            clickRandomButton();
            InteractiveApp app = (InteractiveApp) ApplicationInstance.getActive(); 
            if (indefinite || System.currentTimeMillis() < stopTime) {
                app.enqueueTask(taskQueue, RandomClickTask.this);
            } else {
                // Test complete.
                app.ghostTestRunning = false;
                app.removeTaskQueue(taskQueue);
            }
        }
    }
    
    private TaskQueue taskQueue;
    
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
        
        if (LIVE_DEMO_SERVER) {
            label = new Label("Because you are visiting this application on the nextapp.com server, we have disabled "
                    + "the options to run this test indefinitely and/or with a 0ms callback interval.  If you install this "
                    + "application on your own server, you will be given the option of running the more extreme version "
                    + "of this test.  A binary Web Archive (WAR file) version of this application is provided by the "
                    + "standard Echo2 download.");
            add(label);
        }
        
        Button oneMinuteStartButton = new Button("Start Ghost Click Test (Runtime: 20s, Callback interval: 100ms)");
        oneMinuteStartButton.setStyleName(Styles.DEFAULT_STYLE_NAME);
        oneMinuteStartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                InteractiveApp app = (InteractiveApp)getApplicationInstance();
                if (!app.ghostTestRunning) {
                    app.ghostTestRunning = true;
                    taskQueue = app.createTaskQueue();
                    ContainerContext containerContext = 
                            (ContainerContext) app.getContextProperty(ContainerContext.CONTEXT_PROPERTY_NAME);
                    containerContext.setTaskQueueCallbackInterval(taskQueue, 100);
                    app.enqueueTask(taskQueue, new RandomClickTask(20000));
                }
            }
        });
        add(oneMinuteStartButton);
        
        if (!LIVE_DEMO_SERVER) {
            Button oneMinuteFastStartButton = new Button("Start Ghost Click Test (Runtime: 20s, Callback interval: 0ms)");
            oneMinuteFastStartButton.setStyleName(Styles.DEFAULT_STYLE_NAME);
            oneMinuteFastStartButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    InteractiveApp app = (InteractiveApp)getApplicationInstance();
                    if (!app.ghostTestRunning) {
                        app.ghostTestRunning = true;
                        taskQueue = app.createTaskQueue();
                        ContainerContext containerContext = 
                                (ContainerContext) app.getContextProperty(ContainerContext.CONTEXT_PROPERTY_NAME);
                        containerContext.setTaskQueueCallbackInterval(taskQueue, 0);
                        app.enqueueTask(taskQueue, new RandomClickTask(20000));
                    }
                }
            });
            add(oneMinuteFastStartButton);
            
            Button indefiniteStartButton = new Button("Start Ghost Click Test (Runtime: Indefinite, Callback interval: 0ms)");
            indefiniteStartButton.setStyleName(Styles.DEFAULT_STYLE_NAME);
            indefiniteStartButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    InteractiveApp app = (InteractiveApp)getApplicationInstance();
                    if (!app.ghostTestRunning) {
                        app.ghostTestRunning = true;
                        taskQueue = app.createTaskQueue();
                        ContainerContext containerContext = 
                                (ContainerContext) app.getContextProperty(ContainerContext.CONTEXT_PROPERTY_NAME);
                        containerContext.setTaskQueueCallbackInterval(taskQueue, 0);
                        app.enqueueTask(taskQueue, new RandomClickTask());
                    }
                }
            });
            add(indefiniteStartButton);
        }
    }
}