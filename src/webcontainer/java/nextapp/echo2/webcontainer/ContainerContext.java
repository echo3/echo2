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

import nextapp.echo2.app.TaskQueue;
import nextapp.echo2.webrender.server.ClientProperties;

/**
 * Contextual information about the application container provided to an
 * application instance.  The <code>ContainerContext</code> will be stored
 * as a context property of an application's <code>ApplicationInstance</code>,
 * under the key constant <code>CONTEXT_PROPERTY_NAME</code>.
 */
public interface ContainerContext {
    
    /**
     * Property name by which a <code>ContainerContext</code> may be retrived
     * from an <code>ApplicationInstance</code>'s context properties.
     * 
     * @see nextapp.echo2.app.ApplicationInstance#getContextProperty(java.lang.String)
     */
    public static final String CONTEXT_PROPERTY_NAME = ContainerContext.class.getName();

    /**
     * Returns the <code>ClientProperties</code> describing the user's
     * client web browser environment.
     * 
     * @return the <code>ClientProperties</code> object
     */
    public ClientProperties getClientProperties();
    
    /**
     * Sets the interval between asynchronous callbacks from the client to check
     * for queued tasks for a given <code>TaskQueue</code>.  If multiple 
     * <code>TaskQueue</code>s are active, the smallest specified interval should
     * be used.  The default interval is 500ms.
     * 
     * @param taskQueue the <code>TaskQueue</code>
     * @param ms the number of milleseconds between asynchronous client 
     *        callbacks
     */
    public void setTaskQueueCallbackInterval(TaskQueue taskQueue, int ms);
}
