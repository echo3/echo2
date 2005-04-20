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

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.async.DefaultMessageProcessor;
import nextapp.echo2.app.async.Message;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.MessageEvent;
import nextapp.echo2.app.event.MessageListener;
import nextapp.echo2.app.layout.SplitPaneLayoutData;
import nextapp.echo2.testapp.interactive.Styles;

/**
 * Test for asynchronous (server push) operations.
 */
public class AsynchronousTest extends Row {
    
    private class TaskMessage implements Message {
        
        private int percentComplete;
        
        private TaskMessage(int percentComplete) {
            this.percentComplete = percentComplete;
        }
    }
    
    /**
     * Thread to simulate long-running operation on server.
     * Note that threading is not allowed by the J2EE containers
     * conforming to the 1.3 (or earlier) J2EE specification.
     * If you plan on deploying an Echo2 user interface to such
     * a container, please refrain from using this class as 
     * an example.
     */
    private class SimulatedTask 
    implements Runnable {
        
        private int percentComplete = 0;
        
        public void run() {
            while (percentComplete < 100) {
                percentComplete += (Math.random() * 20);
                if (percentComplete > 100) {
                    percentComplete = 100;
                }
                ApplicationInstance app = getApplicationInstance();
                if (app != null) {
                    messageProcessor.getMessageQueue().enqueueMessage(new TaskMessage(percentComplete));
                    try {
                        Thread.sleep((long) (Math.random() * 1000));
                    } catch (InterruptedException ex) { }
                }
            }
        }
    }
    
    private MessageListener messageListener = new MessageListener() {
        
        /**
         * @see nextapp.echo2.app.event.MessageListener#messageReceived(nextapp.echo2.app.event.MessageEvent)
         */
        public void messageReceived(MessageEvent e) {
            if (e.getMessage() instanceof TaskMessage) {
                TaskMessage taskMessage = (TaskMessage) e.getMessage();
                if (taskMessage.percentComplete < 100) {
                    statusLabel.setText("Asynchronous operation in progress; " + taskMessage.percentComplete 
                            + "% complete.");
                } else {
                    statusLabel.setText("Asynchronous operation complete.");
                    simulatedTask = null;
                    messageProcessor.setEnabled(false);
                }
            }
        }
    };
    
    private SimulatedTask simulatedTask;
    private Label statusLabel;
    private DefaultMessageProcessor messageProcessor;
    
    public AsynchronousTest() {
        super();
        messageProcessor = new DefaultMessageProcessor(null, null, false);
        messageProcessor.getMessageRegistry().addMessageListener(messageListener);
        
        SplitPaneLayoutData splitPaneLayoutData = new SplitPaneLayoutData();
        splitPaneLayoutData.setInsets(new Insets(10));
        setLayoutData(splitPaneLayoutData);
        
        setCellSpacing(new Extent(20));
        
        statusLabel = new Label("Asynchronous operation not active.");
        add(statusLabel);
        
        Button startButton = new Button("Start Asynchronous (Server Push) Operation");
        startButton.setStyleName(Styles.DEFAULT_STYLE_NAME);
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (simulatedTask == null) {
                    messageProcessor.setEnabled(true);
                    simulatedTask = new SimulatedTask();
                    Thread t = new Thread(simulatedTask);
                    t.start();
                }
            }
        });
        add(startButton);
    }
    
    /**
     * @see nextapp.echo2.app.Component#dispose()
     */
    public void dispose() {
        getApplicationInstance().removeMessageProcessor("AsyncTest");
        super.dispose();
    }
    
    /**
     * @see nextapp.echo2.app.Component#init()
     */
    public void init() {
        super.init();
        getApplicationInstance().addMessageProcessor("AsyncTest", messageProcessor);
    }
    
}
