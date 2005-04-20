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

package nextapp.echo2.app.async;

import java.util.EventListener;

import nextapp.echo2.app.event.EventListenerList;
import nextapp.echo2.app.event.MessageEvent;
import nextapp.echo2.app.event.MessageListener;

/**
 * Default <code>MessageRegistry</code> implementation.
 */
public class DefaultMessageRegistry 
implements MessageRegistry {
    
    private EventListenerList listenerList;
    
    /**
     * @see nextapp.echo2.app.async.MessageRegistry#addMessageListener(nextapp.echo2.app.event.MessageListener)
     */
    public void addMessageListener(MessageListener l) {
        if (listenerList == null) {
            listenerList = new EventListenerList();
        }
        listenerList.addListener(MessageListener.class, l);
    }

    /**
     * @see nextapp.echo2.app.async.MessageRegistry#fireMessageEvent(nextapp.echo2.app.event.MessageEvent)
     */
    public void fireMessageEvent(MessageEvent e) {
        EventListener[] listeners = listenerList.getListeners(MessageListener.class);
        for (int j = 0; j < listeners.length; ++j) {
            MessageListener messageListener = (MessageListener) listeners[j];
            messageListener.messageReceived(e);
        }
    }

    /**
     * @see nextapp.echo2.app.async.MessageRegistry#hasMessageListeners()
     */
    public boolean hasMessageListeners() {
        return listenerList != null && listenerList.getListenerCount(MessageListener.class) != 0;
    }
    
    /**
     * @see nextapp.echo2.app.async.MessageRegistry#removeMessageListener(nextapp.echo2.app.event.MessageListener)
     */
    public void removeMessageListener(MessageListener l) {
        if (listenerList != null) {
            listenerList.removeListener(MessageListener.class, l);
        }
    }
}
