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

import nextapp.echo2.app.TaskQueueHandle;

/**
 * Note to developers who might use this class as an example:
 * Don't.  This is a *very unusual* use of asynchronous tasks.
 * See the documentation for examples of how asynchronous tasks
 * might normally be used.
 */
public class GhostTask 
implements Runnable {
    
    /**
     * Creates and starts a new <code>GhostTask</code>.
     * 
     * @param app the application to test
     * @param taskQueue the <code>TaskQueueHandle</code> to which tasks will be
     *        added 
     * @param runTime the number of milliseconds the test should run (specify 0
     *        for an indefinite amount of time)
     * @param clicksPerIteration the number of button clicks to perform in a 
     *        single iteration.
     */
    static void start(InteractiveApp app, TaskQueueHandle taskQueue, long runTime, int clicksPerIteration) {
        app.enqueueTask(taskQueue, new GhostTask(app, taskQueue, runTime, clicksPerIteration));
    }
    
    private int iteration = 0;
    private boolean indefinite;
    private long stopTime;
    private int clicksPerIteration;
    private TaskQueueHandle taskQueue;
    private InteractiveApp app;
    
    /**
     * Creates a new <code>GhostTask</code>.
     * 
     * @param app the application to test
     * @param taskQueue the <code>TaskQueueHandle</code> to which tasks will be 
     *        added
     * @param runTime the number of milliseconds the test should run (specify 0
     *        for an indefinite amount of time)
     * @param clicksPerIteration the number of button clicks to perform in a 
     *        single iteration.
     */
    private GhostTask(InteractiveApp app, TaskQueueHandle taskQueue, long runTime, int clicksPerIteration) {
        this.taskQueue = taskQueue;
        this.app = app;
        this.clicksPerIteration = clicksPerIteration;
        if (InteractiveApp.LIVE_DEMO_SERVER || runTime > 0) {
            stopTime = System.currentTimeMillis() + runTime;
        } else {
            indefinite = true;
        }
    }
    
    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        for (int i = 0; i < clicksPerIteration; ++i) {
            RandomClick.clickRandomButton();
        }
        if (indefinite || System.currentTimeMillis() < stopTime) {
            ++iteration;
            app.setGhostIterationWindowTitle(iteration);
            app.enqueueTask(taskQueue, this);
        } else {
            app.setGhostIterationWindowTitle(-1);
            // Test complete.
            app.stopGhostTest();
        }
    }
}
