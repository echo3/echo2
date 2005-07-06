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

package nextapp.echo2.testapp.thousandmonkeys;

import java.util.ArrayList;
import java.util.List;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.IllegalChildException;
import nextapp.echo2.app.ListBox;
import nextapp.echo2.app.PasswordField;
import nextapp.echo2.app.RadioButton;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.TextArea;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.WindowPane;
import nextapp.echo2.testapp.thousandmonkeys.factories.ButtonFactory;
import nextapp.echo2.testapp.thousandmonkeys.factories.GenericFactory;
import nextapp.echo2.testapp.thousandmonkeys.factories.LabelFactory;

/**
 * A "Monkey" randomly adds, updates, and removes <code>Component</code>s from
 * the application.
 * Note: yes, there is only one monkey per application (you'll need 1000 
 * browser clients to truly achieve 1000 monkeys).
 */
public class Monkey {
    
    private static final ComponentFactory[] componentFactories = new ComponentFactory[]{
        new ButtonFactory(),
        new GenericFactory(CheckBox.class),
        new GenericFactory(Column.class),
        new GenericFactory(ContentPane.class),
        new GenericFactory(Grid.class),
        new LabelFactory(),
        new GenericFactory(ListBox.class),
        new GenericFactory(PasswordField.class),
        new GenericFactory(RadioButton.class),
        new GenericFactory(Row.class),
        new GenericFactory(SelectField.class),
        new GenericFactory(SplitPane.class),
        new GenericFactory(TextField.class),
        new GenericFactory(TextArea.class),
        new GenericFactory(WindowPane.class)
    };
    
    /**
     * The maximum number of children to add to the root 
     * <code>ContentPane</code>.
     */
    private static final int MAX_CONTENT_PANE_CHILDREN = 5;

    /**
     * The minimum number of add operations to perform per iteration.
     */
    private static final int MIN_ADDS = 0;

    /**
     * The maximum number of add operations to perform per iteration.
     */
    private static final int MAX_ADDS = 20;

    /**
     * The minimum number of remove operations to perform per iteration.
     */
    private static final int MIN_REMOVES = 0;

    /**
     * The maximum number of remove operations to perform per iteration.
     */
    private static final int MAX_REMOVES = 12;

    /**
     * The root <code>ContentPane</code> to which children should be added
     * (will never be removed).
     */
    private ContentPane contentPane;
    
    /**
     * List of all potential parent <code>Component</code>s. 
     */
    private List components = new ArrayList();
    
    /**
     * Creates a new <code>Monkey</code>.
     * 
     * @param contentPane the root <code>ContentPane</code> to which children
     *        should be added (will never be removed)
     */
    public Monkey(ContentPane contentPane) {
        this.contentPane = contentPane;
    }
    
    /**
     * Creates a random <code>Component</code> and adds it beneath a random 
     * parent.
     */
    private void doAdd() {
        int addParentIndex;
        if (components.size() < MAX_CONTENT_PANE_CHILDREN) {
            addParentIndex = ((int) Math.random() * (components.size() + 1)) - 1;
        } else {
            addParentIndex = ((int) Math.random() * components.size());
        }
        ComponentFactory componentFactory = componentFactories[((int) (Math.random() * componentFactories.length))];
        Component child = componentFactory.newInstance();
        
        for (int i = 0; i < 5; ++i) {
            Component parent = addParentIndex == -1 ? contentPane : (Component) components.get(addParentIndex);
            try {
                parent.add(child);
                components.add(child);
                return;
            } catch (IllegalChildException ex) { }
        }
    }
    
    /**
     * Removes a random <code>Component</code> from the hierarchy.
     */
    private void doRemove() {
        if (components.size() == 0) {
            // No components to remove.
            return;
        }
        Component child = (Component) components.get((int) (Math.random() * components.size()));
        child.getParent().remove(child);
        recursiveRemoveComponentFromList(child);
    }
    
    /**
     * Performs a single iteration, potentially adding, removing, and/or 
     * updating several <code>Component</code>s.
     */
    public void iterate() {
        int removes = ((int) (Math.random() * (1 + MAX_REMOVES - MIN_REMOVES))) + MIN_REMOVES;
        for (int i = 0; i < removes; ++i) {
            doRemove();
        }

        int adds = ((int) (Math.random() * (1 + MAX_ADDS - MIN_ADDS))) + MIN_ADDS;
        for (int i = 0; i < adds; ++i) {
            doAdd();
        }
        System.err.println(components.size());
    }
    
    /**
     * Recursively removes a <code>Component</code> and its descendants from 
     * the list of eligible parents.  This operation is performed due to the
     * <code>Component</code> having been deleted.
     * 
     * @param component the deleted <code>Component</code>
     */
    private void recursiveRemoveComponentFromList(Component component) {
        components.remove(component);
        Component[] children = component.getComponents();
        for (int i = 0; i < children.length; ++i) {
            recursiveRemoveComponentFromList(children[i]);
        }
    }
}
