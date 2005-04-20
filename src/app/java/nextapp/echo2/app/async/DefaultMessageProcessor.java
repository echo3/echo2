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

import nextapp.echo2.app.event.MessageEvent;

/**
 * Default <code>MessageProcessor</code> implementation.
 */
public class DefaultMessageProcessor 
implements MessageProcessor {
    
    private MessageQueue messageQueue;
    private MessageRegistry messageRegistry;
    private boolean enabled = true;
    
    /**
     * Creates a new <code>DefaultMessageProcessor</code>.
     * A <code>DefaultMessageQueue</code> and 
     * <code>DefaultMessageRegistry</code> will automatically be created
     * to support queueing and event handling respectively.
     * The initial enabled state of the <code>MessageQueue</code> will be
     * true.
     */
    public DefaultMessageProcessor() {
        this(null, null, true);
    }
    
    /**
     * Creates a new <code>DefaultMessageProcessor</code> using the 
     * specified <code>MessageQueue</code> and <code>MessageRegistry</code>.
     * 
     * @param messageQueue the <code>MessageQueue</code> (if null, a 
     *        <code>DefaultMessageQueue</code> will automatically be created)
     * @param messageRegistry the <code>MessageRegistry</code> (if null, a
     *        <code>DefaultMessageRegistry</code> will automatically be created)
     * @param enabled the initial enabled state of the message processor
     */
    public DefaultMessageProcessor(MessageQueue messageQueue, MessageRegistry messageRegistry, boolean enabled) {
        super();
        this.messageQueue = messageQueue == null ? new DefaultMessageQueue() : messageQueue;
        this.messageRegistry = messageRegistry == null ? new DefaultMessageRegistry() : messageRegistry;
        this.enabled = enabled;
    }
    
    /**
     * @see nextapp.echo2.app.async.MessageProcessor#getMessageQueue()
     */
    public MessageQueue getMessageQueue() {
        return messageQueue;
    }
    
    /**
     * @see nextapp.echo2.app.async.MessageProcessor#getMessageRegistry()
     */
    public MessageRegistry getMessageRegistry() {
        return messageRegistry;
    }
    
    /**
     * @see nextapp.echo2.app.async.MessageProcessor#isEnabled()
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * @see nextapp.echo2.app.async.OldMessageQueue#processMessages()
     */
    public void processMessages() {
        if (!enabled) {
            throw new IllegalStateException("Attempt to process messages in a non-enabled MessageProcessor.");
        }
        Message[] messages = messageQueue.dequeueMessages();
        for (int i = 0; i < messages.length; ++i) {
            MessageEvent e = new MessageEvent(this, messages[i]);
            messageRegistry.fireMessageEvent(e);
        }
    }
    
    /**
     * Sets the enabled state of the <code>MessageProcessor</code>.
     * 
     * @param newValue true to enable this <code>MessageProcessor</code>
     *        (initial state is true)
     */
    public void setEnabled(boolean newValue) {
        enabled = newValue;
    }
}
