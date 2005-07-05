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
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.ResourceImageReference;
import nextapp.echo2.app.button.DefaultButtonModel;
import nextapp.echo2.app.button.ToggleButtonModel;
import nextapp.echo2.app.event.ChangeEvent;
import nextapp.echo2.app.event.ChangeListener;

/**
 * Unit tests for the <code>nextapp.echo2.app.button.ToggleButton</code>-based 
 * components.
 */
public class ToggleButtonTest extends TestCase {
    
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
     * Test default property values.
     */
    public void testDefaults() {
        CheckBox checkBox = new CheckBox();
        assertFalse(checkBox.isSelected());
    }

    /**
     * Ensure that <code>ToggleButtonModel</code> requirement is being 
     * enforced. 
     */
    public void testInvalidModelException() {
        CheckBox checkBox = new CheckBox();
        try {
            checkBox.setModel(new DefaultButtonModel());
            fail();
        } catch (IllegalArgumentException ex) {
            // Expected.
        }
    }
    
    /**
     * Test property accessors and mutators.
     */
    public void testProperties() {
        CheckBox checkBox = new CheckBox();
        
        checkBox.setSelectedStateIcon(new ResourceImageReference("SelectedState.png"));
        checkBox.setStateIcon(new ResourceImageReference("State.png"));
        checkBox.setPressedSelectedStateIcon(new ResourceImageReference("PressedSelectedState.png"));
        checkBox.setPressedStateIcon(new ResourceImageReference("PressedState.png"));
        checkBox.setRolloverSelectedStateIcon(new ResourceImageReference("RolloverSelectedState.png"));
        checkBox.setRolloverStateIcon(new ResourceImageReference("RolloverState.png"));
        checkBox.setStateAlignment(new Alignment(Alignment.RIGHT, Alignment.BOTTOM));
        checkBox.setStatePosition(new Alignment(Alignment.TRAILING, Alignment.DEFAULT));
        checkBox.setStateMargin(TestConstants.EXTENT_100_PX);
        
        assertEquals(new ResourceImageReference("SelectedState.png"), checkBox.getSelectedStateIcon());
        assertEquals(new ResourceImageReference("State.png"), checkBox.getStateIcon());
        assertEquals(new ResourceImageReference("PressedSelectedState.png"), checkBox.getPressedSelectedStateIcon());
        assertEquals(new ResourceImageReference("PressedState.png"), checkBox.getPressedStateIcon());
        assertEquals(new ResourceImageReference("RolloverSelectedState.png"), checkBox.getRolloverSelectedStateIcon());
        assertEquals(new ResourceImageReference("RolloverState.png"), checkBox.getRolloverStateIcon());
        assertEquals(new Alignment(Alignment.RIGHT, Alignment.BOTTOM), checkBox.getStateAlignment());
        assertEquals(new Alignment(Alignment.TRAILING, Alignment.DEFAULT), checkBox.getStatePosition());
        assertEquals(TestConstants.EXTENT_100_PX, checkBox.getStateMargin());
        
        checkBox.setSelected(true);
        assertTrue(checkBox.isSelected());
        checkBox.setSelected(false);
        assertFalse(checkBox.isSelected());
    }
}
