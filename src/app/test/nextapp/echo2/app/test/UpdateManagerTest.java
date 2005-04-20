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

import java.util.Arrays;
import java.util.List;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.layout.RowLayoutData;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.app.update.UpdateManager;
import junit.framework.TestCase;

/**
 * Unit test(s) for update management subsystem.
 */
public class UpdateManagerTest extends TestCase  {
    
    private UpdateManager manager;
    private RowApp rowApp;
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() {
        rowApp = new RowApp();
        rowApp.doInit();
        manager = rowApp.getUpdateManager();
    }
    
    /**
     * Ensure updates are returned sorted by component depth.
     */
    public void testUpdateSorting1() {
        ServerComponentUpdate[] componentUpdates;
        manager.purge();

        rowApp.getLabel().setBackground(Color.BLUE);
        rowApp.getContentPane().setBackground(Color.RED);
        rowApp.getRow().setBackground(Color.GREEN);
        componentUpdates = manager.getServerComponentUpdates();

        assertEquals(3, componentUpdates.length);
        assertEquals(rowApp.getContentPane(), componentUpdates[0].getParent());
        assertEquals(rowApp.getRow(), componentUpdates[1].getParent());
        assertEquals(rowApp.getLabel(), componentUpdates[2].getParent());
    }
    
    /**
     * Ensure updates are returned sorted by component depth.
     */
    public void testUpdateSorting2() {
        ServerComponentUpdate[] componentUpdates;
        Row row2 = new Row();
        rowApp.getRow().add(row2);
        Label label2 = new Label();
        row2.add(label2);
        manager.purge();

        rowApp.getLabel().setBackground(Color.BLUE);
        rowApp.getContentPane().setBackground(Color.RED);
        rowApp.getRow().setBackground(Color.GREEN);
        label2.setBackground(Color.YELLOW);
        row2.setBackground(Color.ORANGE);

        componentUpdates = manager.getServerComponentUpdates();
        assertEquals(5, componentUpdates.length);
        assertEquals(rowApp.getContentPane(), componentUpdates[0].getParent());
        assertEquals(rowApp.getRow(), componentUpdates[1].getParent());
        assertTrue(rowApp.getLabel().equals(componentUpdates[2].getParent()) 
                || rowApp.getLabel().equals(componentUpdates[3].getParent()));
        assertTrue(row2.equals(componentUpdates[2].getParent()) 
                || row2.equals(componentUpdates[3].getParent()));
        assertEquals(label2, componentUpdates[4].getParent());
    }
    
    public void testAddChlidToAddedParent() {
        ServerComponentUpdate[] componentUpdates;
        Component[] addedChildren;
        manager.purge();

        // Add a row.
        Row row1 = new Row();
        rowApp.getRow().add(row1);
        
        // Ensure only 1 update w/ 1 child added.
        componentUpdates = manager.getServerComponentUpdates();
        assertEquals(1, componentUpdates.length);
        addedChildren = componentUpdates[0].getAddedChildren();
        assertEquals(1, addedChildren.length);
        assertEquals(0, componentUpdates[0].getRemovedChildren().length);
        assertEquals(row1, addedChildren[0]);
        
        // Add a label to row.
        row1.add(new Label("A"));

        // Ensure only 1 update w/ 1 child added.
        componentUpdates = manager.getServerComponentUpdates();
        assertEquals(1, componentUpdates.length);
        addedChildren = componentUpdates[0].getAddedChildren();
        assertEquals(1, addedChildren.length);
        assertEquals(0, componentUpdates[0].getRemovedChildren().length);
        assertEquals(row1, addedChildren[0]);

        // Add another row.
        Row row2 = new Row();
        Label labelB = new Label("B");
        row2.add(labelB);
        rowApp.getRow().add(row2);
        
        // Ensure only 1 update w/ 2 children added.
        componentUpdates = manager.getServerComponentUpdates();
        assertEquals(1, componentUpdates.length);
        addedChildren = componentUpdates[0].getAddedChildren();
        assertEquals(2, addedChildren.length);
        assertEquals(0, componentUpdates[0].getRemovedChildren().length);
        
        List addedChildrenList = Arrays.asList(addedChildren);
        assertTrue(addedChildrenList.contains(row1));
        assertTrue(addedChildrenList.contains(row2));
        assertFalse(addedChildrenList.contains(labelB));
    }
    
    public void testLayoutDataUpdate() {
        ServerComponentUpdate[] componentUpdates;
        RowLayoutData rowLayoutData;
 
        manager.purge();
        
        // Setup.
        Row row = new Row();
        rowApp.getRow().add(row);
        Label label1 = new Label("Label1");
        row.add(label1);
        Label label2 = new Label("Label2");
        row.add(label2);
        label2.setLayoutData(new RowLayoutData());

        componentUpdates = manager.getServerComponentUpdates();
        assertEquals(1, componentUpdates.length);
        assertEquals(false, componentUpdates[0].hasUpdatedLayoutDataChildren());
        
        manager.purge();
        
        rowLayoutData = new RowLayoutData();
        rowLayoutData.setAlignment(new Alignment(Alignment.CENTER, Alignment.DEFAULT));
        label1.setLayoutData(rowLayoutData);
        componentUpdates = manager.getServerComponentUpdates();
        assertEquals(1, componentUpdates.length);
        assertEquals(row, componentUpdates[0].getParent());
        assertFalse(componentUpdates[0].hasAddedChildren());
        assertFalse(componentUpdates[0].hasRemovedChildren());
        assertFalse(componentUpdates[0].hasUpdatedProperties());
        assertTrue(componentUpdates[0].hasUpdatedLayoutDataChildren());
        
        Component[] components = componentUpdates[0].getUpdatedLayoutDataChildren();
        assertEquals(1, components.length);
        assertEquals(label1, components[0]);
    }
    
    public void testPropertyUpdateCancellation1() {
        TextField textField = new TextField();
        rowApp.getRow().add(textField);
        
        manager.purge();
        manager.addClientPropertyUpdate(textField, TextField.TEXT_CHANGED_PROPERTY, "a user typed this.");
        manager.processClientUpdates();
        
        ServerComponentUpdate[] componentUpdates = manager.getServerComponentUpdates();
        assertEquals(0, componentUpdates.length);
    }
    
    public void testPropertyUpdateCancellation2() {
        TextField textField = new TextField();
        rowApp.getRow().add(textField);
        
        manager.purge();
        textField.setBackground(Color.BLUE);
        manager.addClientPropertyUpdate(textField, TextField.TEXT_CHANGED_PROPERTY, "a user typed this.");
        manager.processClientUpdates();
        
        ServerComponentUpdate[] componentUpdates = manager.getServerComponentUpdates();
        assertEquals(1, componentUpdates.length);
        ServerComponentUpdate.PropertyUpdate backgroundUpdate = 
                  componentUpdates[0].getUpdatedProperty(TextField.PROPERTY_BACKGROUND);
        assertNotNull(backgroundUpdate);
        assertEquals(Color.BLUE, backgroundUpdate.getNewValue());
        assertNull(componentUpdates[0].getUpdatedProperty(TextField.TEXT_CHANGED_PROPERTY));
    }
    
    public void testPropertyUpdateCancellation3() {
        TextField textField = new TextField();
        rowApp.getRow().add(textField);
        
        manager.purge();
        textField.setText("first the application set it to this.");
        manager.addClientPropertyUpdate(textField, TextField.TEXT_CHANGED_PROPERTY, "a user typed this.");
        manager.processClientUpdates();
        
        ServerComponentUpdate[] componentUpdates = manager.getServerComponentUpdates();
        assertEquals(1, componentUpdates.length);
        assertFalse(componentUpdates[0].hasUpdatedProperties());
    }
    
    
    public void testPropertyUpdate() {
        ServerComponentUpdate[] componentUpdates;
        // Remove previous updates.
        manager.purge();
        
        // Update text property of label and verify.
        rowApp.getLabel().setText("Hi there");
        componentUpdates = manager.getServerComponentUpdates();
        assertEquals(rowApp.getLabel(), componentUpdates[0].getParent());
        assertEquals(1, componentUpdates.length);
        assertEquals(0, componentUpdates[0].getAddedChildren().length);
        assertEquals(0, componentUpdates[0].getRemovedChildren().length);
        
        String[] updatedPropertyNames = componentUpdates[0].getUpdatedPropertyNames(); 
        assertEquals(1, updatedPropertyNames.length);
        assertEquals(Label.PROPERTY_TEXT, updatedPropertyNames[0]);
        ServerComponentUpdate.PropertyUpdate propertyUpdate = componentUpdates[0].getUpdatedProperty(Label.PROPERTY_TEXT);
        assertEquals("Label", propertyUpdate.getOldValue());
        assertEquals("Hi there", propertyUpdate.getNewValue());
        
        // Remove label entirely and ensure property update disappears.
        rowApp.getRow().remove(rowApp.getLabel());
        componentUpdates = manager.getServerComponentUpdates();
        assertEquals(1, componentUpdates.length);
        assertEquals(0, componentUpdates[0].getUpdatedPropertyNames().length);
        assertEquals(0, componentUpdates[0].getAddedChildren().length);
    }
    
    public void testPurge() {
        assertFalse(manager.getServerComponentUpdates().length == 0);
        manager.purge();
        assertTrue(manager.getServerComponentUpdates().length == 0);
    }

    public void testRemove1() {
        Row row = new Row();
        Label label = new Label();
        row.add(label);
        rowApp.getRow().add(row);
        manager.purge();
        
        rowApp.getRow().remove(row);
        
        ServerComponentUpdate[] componentUpdates = manager.getServerComponentUpdates();
        assertEquals(1, componentUpdates.length);
        
        assertEquals(true, componentUpdates[0].hasRemovedChildren());
        assertEquals(true, componentUpdates[0].hasRemovedDescendants());
        
        Component[] removedChildren = componentUpdates[0].getRemovedChildren();
        assertEquals(1, removedChildren.length);
        assertEquals(row, removedChildren[0]);
        
        Component[] removedDescendants = componentUpdates[0].getRemovedDescendants();
        assertEquals(1, removedDescendants.length);
        assertEquals(label, removedDescendants[0]);
    }

    public void testRemove2() {
        Row row1 = new Row();
        Row row2 = new Row();
        row1.add(row2);
        Label label = new Label();
        row2.add(label);
        rowApp.getRow().add(row1);
        manager.purge();
        
        row1.remove(row2);
        rowApp.getRow().remove(row1);
        
        ServerComponentUpdate[] componentUpdates = manager.getServerComponentUpdates();
        assertEquals(1, componentUpdates.length);
        
        assertEquals(true, componentUpdates[0].hasRemovedChildren());
        assertEquals(true, componentUpdates[0].hasRemovedDescendants());
        
        Component[] removedChildren = componentUpdates[0].getRemovedChildren();
        assertEquals(1, removedChildren.length);
        
        Component[] removedDescendants = componentUpdates[0].getRemovedDescendants();
        assertEquals(1, removedDescendants.length);
        assertEquals(label, removedDescendants[0]);
    }
}
