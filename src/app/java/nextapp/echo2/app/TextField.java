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

package nextapp.echo2.app;

import java.util.EventListener;

import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.text.Document;
import nextapp.echo2.app.text.StringDocument;
import nextapp.echo2.app.text.TextComponent;

/**
 * A single-line text input field.
 */
public class TextField extends TextComponent {

    public static final String INPUT_ENTER = "input_enter";

    public static final String PROPERTY_ACTION_COMMAND = "actionCommand";
    
    /**
     * Creates a new <code>TextField</code> with an empty 
     * <code>StringDocument</code> as its model, and default width
     * setting.
     */
    public TextField() {
        this(new StringDocument(), null, 20);
    }
    
    /**
     * Creates a new <code>TextField</code> with the specified
     * <code>Document</code> model, initial text, and column width.
     * 
     * @param document the document
     * @param text the initial text (may be null)
     * @param columns the number of columns to display
     */
    public TextField(Document document, String text, int columns) {
        super(document);
        if (text != null) {
            document.setText(text);
        }
        setWidth(new Extent(columns, Extent.EX));
    }
    
    /**
     * Adds an <code>ActionListener</code> to the <code>TextField</code>.
     * The <code>ActionListener</code> will be invoked when the user
     * presses the ENTER key in the field.
     * 
     * @param l the <code>ActionListener</code> to add
     */
    public void addActionListener(ActionListener l) {
        getEventListenerList().addListener(ActionListener.class, l);
    }

    /**
     * Fires an action event to all listeners.
     */
    private void fireActionEvent() {
        EventListener[] listeners = getEventListenerList().getListeners(ActionListener.class);
        ActionEvent e = null;
        for (int i = 0; i < listeners.length; ++i) {
            if (e == null) {
                e = new ActionEvent(this, (String) getRenderProperty(PROPERTY_ACTION_COMMAND));
            } 
            ((ActionListener) listeners[i]).actionPerformed(e);
        }
    }
    
    /**
     * Returns the action command which will be provided in 
     * <code>ActionEvent</code>s fired by this <code>TextField</code>.
     * 
     * @return the action command
     */
    public String getActionCommand() {
        return (String) getProperty(PROPERTY_ACTION_COMMAND);
    }
    
    /**
     * @see nextapp.echo2.app.Component#processInput(java.lang.String, java.lang.Object)
     */
    public void processInput(String inputName, Object inputValue) {
        super.processInput(inputName, inputValue);
        
        if (INPUT_ENTER.equals(inputName)) {
            fireActionEvent();
        }
    }
    
    /**
     * Removes an <code>ActionListener</code> from the <code>TextField</code>.
     * 
     * @param l the <code>ActionListener</code> to remove
     */
    public void removeActionListener(ActionListener l) {
        getEventListenerList().removeListener(ActionListener.class, l);
    }
    
    /**
     * Sets the action command which will be provided in
     * <code>ActionEvent</code>s fired by this <code>TextField</code>.
     * 
     * @param newValue the new action command
     */
    public void setActionCommand(String newValue) {
        setProperty(PROPERTY_ACTION_COMMAND, newValue);
    }
}
