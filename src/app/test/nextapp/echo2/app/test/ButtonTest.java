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

package nextapp.echo2.app.test;

import junit.framework.TestCase;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.IllegalChildException;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.button.ButtonModel;
import nextapp.echo2.app.button.ToggleButtonModel;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.ChangeEvent;
import nextapp.echo2.app.event.ChangeListener;

/**
 * Unit test(s) for the <code>nextapp.echo2.app.AbstractButton</code>-based 
 * components.
 */
public class ButtonTest extends TestCase {
    
    /**
     * <code>ActionListener</code> that retains last fired event and counts
     * events received.
     */
    private static class ActionHandler 
    implements ActionListener {
        
        int eventCount = 0;
        ActionEvent lastEvent;
        
        /**
         * @see nextapp.echo2.app.event.ActionListener#actionPerformed(nextapp.echo2.app.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            lastEvent = e;
            ++eventCount;
        }
    }
    
    /**
     * <code>ChangeListener</code> that retains last fired event and counts
     * events received.
     */
    private static class ChangeHandler 
    implements ChangeListener {
        
        int eventCount = 0;
        ChangeEvent lastEvent;
        
        /**
         * @see nextapp.echo2.app.event.ChangeListener#stateChanged(nextapp.echo2.app.event.ChangeEvent)
         */
        public void stateChanged(ChangeEvent e) {
            lastEvent = e;
            ++eventCount;
        }
    }
    
    /**
     * Test behavior of <code>ActionListener</code>s.
     */
    public void testActionListener() {
        ActionHandler buttonActionListener = new ActionHandler();
        ActionHandler modelActionListener = new ActionHandler();
        Button button = new Button("Test");
        ButtonModel model = button.getModel();
        button.addActionListener(buttonActionListener);
        model.addActionListener(modelActionListener);
        assertEquals(0, buttonActionListener.eventCount);
        assertEquals(0, modelActionListener.eventCount);
        button.doAction();
        assertEquals(1, buttonActionListener.eventCount);
        assertEquals(1, modelActionListener.eventCount);
        assertEquals(button, buttonActionListener.lastEvent.getSource());
        assertEquals(model, modelActionListener.lastEvent.getSource());

        buttonActionListener.lastEvent = null;
        modelActionListener.lastEvent = null;
        assertEquals(null, buttonActionListener.lastEvent);
        assertEquals(null, modelActionListener.lastEvent);

        model.doAction();
        
        assertEquals(2, buttonActionListener.eventCount);
        assertEquals(2, modelActionListener.eventCount);
        assertEquals(button, buttonActionListener.lastEvent.getSource());
        assertEquals(model, modelActionListener.lastEvent.getSource());
    }
    
    /**
     * Test behavior of <code>ChangeListener</code>s.
     */
    public void testChangeListener() {
        ChangeHandler buttonChangeListener = new ChangeHandler();
        ChangeHandler modelChangeListener = new ChangeHandler();
        CheckBox checkBox = new CheckBox("Test");
        ToggleButtonModel model = (ToggleButtonModel) checkBox.getModel();
        checkBox.addChangeListener(buttonChangeListener);
        model.addChangeListener(modelChangeListener);
        assertEquals(0, buttonChangeListener.eventCount);
        assertEquals(0, modelChangeListener.eventCount);
        checkBox.setSelected(true);
        assertEquals(1, buttonChangeListener.eventCount);
        assertEquals(1, modelChangeListener.eventCount);
        assertEquals(checkBox, buttonChangeListener.lastEvent.getSource());
        assertEquals(model, modelChangeListener.lastEvent.getSource());

        buttonChangeListener.lastEvent = null;
        modelChangeListener.lastEvent = null;
        assertEquals(null, buttonChangeListener.lastEvent);
        assertEquals(null, modelChangeListener.lastEvent);

        model.setSelected(false);
        
        assertEquals(2, buttonChangeListener.eventCount);
        assertEquals(2, modelChangeListener.eventCount);
        assertEquals(checkBox, buttonChangeListener.lastEvent.getSource());
        assertEquals(model, modelChangeListener.lastEvent.getSource());
    }
    
    /**
     * Attempt to illegally add children, test for failure.
     */
    public void testIllegalChildren() {
        Button button = new Button();
        boolean exceptionThrown = false;
        try {
            button.add(new Label("you can't add children to this component, right?"));
        } catch (IllegalChildException ex) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }
    
    /**
     * Ensure setting model to null fails.
     */
    public void testNullModelException() {
        boolean exceptionThrown = false;
        Button button = new Button();
        try {
            button.setModel(null);
        } catch (IllegalArgumentException ex) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }
    
    /**
     * Test property accessors and mutators.
     */
    public void testProperties() {
        Button button = new Button();

        button.setRolloverBackground(Color.RED);
        button.setRolloverForeground(Color.BLUE);
        button.setRolloverFont(TestConstants.MONOSPACE_12);
        
        assertEquals(Color.RED, button.getRolloverBackground());
        assertEquals(Color.BLUE, button.getRolloverForeground());
        assertEquals(TestConstants.MONOSPACE_12, button.getRolloverFont());

        button.setPressedBackground(Color.GREEN);
        button.setPressedForeground(Color.YELLOW);
        button.setPressedFont(TestConstants.TIMES_72);
        
        assertEquals(Color.GREEN, button.getPressedBackground());
        assertEquals(Color.YELLOW, button.getPressedForeground());
        assertEquals(TestConstants.TIMES_72, button.getPressedFont());

        button.setRolloverEnabled(true);
        assertTrue(button.isRolloverEnabled());
        button.setRolloverEnabled(false);
        assertFalse(button.isRolloverEnabled());

        button.setPressedEnabled(true);
        assertTrue(button.isPressedEnabled());
        button.setPressedEnabled(false);
        assertFalse(button.isPressedEnabled());
        
        button.setText("Alpha");
        assertEquals("Alpha", button.getText());
        
        button.setIcon(TestConstants.ICON);
        assertEquals(TestConstants.ICON, button.getIcon());
        
        button.setRolloverIcon(TestConstants.ROLLOVER_ICON);
        assertEquals(TestConstants.ROLLOVER_ICON, button.getRolloverIcon());
        
        button.setPressedIcon(TestConstants.PRESSED_ICON);
        assertEquals(TestConstants.PRESSED_ICON, button.getPressedIcon());

        button.setBorder(TestConstants.BORDER_THIN_YELLOW);
        assertEquals(TestConstants.BORDER_THIN_YELLOW, button.getBorder());

        button.setRolloverBorder(TestConstants.BORDER_THICK_ORANGE);
        assertEquals(TestConstants.BORDER_THICK_ORANGE, button.getRolloverBorder());
        assertEquals(TestConstants.BORDER_THIN_YELLOW, button.getBorder());
    }
}
