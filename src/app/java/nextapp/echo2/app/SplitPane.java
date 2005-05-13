package nextapp.echo2.app;

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

/**
 * A container which displays two components horizontally or vertically
 * adjacent to one another.
 * <p>
 * <strong>Child LayoutData</code>: Children of this component may provide
 * layout information using the 
 * <code>nextapp.echo2.app.layout.SplitPaneLayoutData</code> layout data object.
 * 
 * @see nextapp.echo2.app.layout.SplitPaneLayoutData
 */
public class SplitPane extends Component {
    
    public static final int ORIENTATION_HORIZONTAL_LEADING_TRAILING = 1;
    public static final int ORIENTATION_HORIZONTAL_TRAILING_LEADING = 2;
    public static final int ORIENTATION_HORIZONTAL_LEFT_RIGHT = 3;
    public static final int ORIENTATION_HORIZONTAL_RIGHT_LEFT = 4;
    public static final int ORIENTATION_VERTICAL_TOP_BOTTOM = 5;
    public static final int ORIENTATION_VERTICAL_BOTTOM_TOP = 6;
    
    public static final int ORIENTATION_HORIZONTAL = ORIENTATION_HORIZONTAL_LEADING_TRAILING;
    public static final int ORIENTATION_VERTICAL = ORIENTATION_VERTICAL_TOP_BOTTOM;
    
    public static final String PROPERTY_ORIENTATION = "orientation";
    public static final String PROPERTY_RESIZABLE = "resizable";
    public static final String PROPERTY_SEPARATOR_COLOR = "separatorColor";
    public static final String PROPERTY_SEPARATOR_BACKGROUND_IMAGE = "separatorBackgroundImage";
    public static final String PROPERTY_SEPARATOR_POSITION = "separatorPosition";
    public static final String PROPERTY_SEPARATOR_SIZE = "separatorSize";
    
    /**
     * Creates a new <code>SplitPane</code> with default (horizontal) 
     * orientation.
     */
    public SplitPane() {
        this(ORIENTATION_HORIZONTAL, null);
    }
    
    //BUGBUG. update docs for new orientation values.
    /**
     * Creates a new <code>SplitPane</code> with the specified orientation and
     * separator position.
     *
     * @param orientation a constant representing the orientation, one of the 
     *        following values:
     *        <ul>
     *         <li><code>ORIENTATION_HORIZONTAL</code></li>
     *         <li><code>ORIENTATION_VERTICAL</code></li>
     *        </ul>
     * @param separatorPosition the initial position of the separator
     */
    public SplitPane(int orientation, Extent separatorPosition) {
        super();
        setOrientation(orientation);
        if (separatorPosition != null) {
            setSeparatorPosition(separatorPosition);
        }
    }
    
    /**
     * Returns the orientation of the <code>SplitPane</code>.
     * 
     * @return a constant representing the orientation, one of the following 
     *         values:
     *         <ul>
     *          <li><code>ORIENTATION_HORIZONTAL</code></li>
     *          <li><code>ORIENTATION_VERTICAL</code></li>
     *         </ul>
     */
    public int getOrienation() {
        Integer orientation = (Integer) getProperty(PROPERTY_ORIENTATION); 
        return orientation == null ? ORIENTATION_VERTICAL : orientation.intValue();
    }
    
    /**
     * Returns the background image of the pane separator.
     * 
     * @return the background image
     */
    public FillImage getSeparatorBackgroundImage() {
        return (FillImage) getProperty(PROPERTY_SEPARATOR_BACKGROUND_IMAGE);
    }
    
    /**
     * Returns the color of the pane separator.
     * 
     * @return the color
     */
    public Color getSeparatorColor() {
        return (Color) getProperty(PROPERTY_SEPARATOR_COLOR);
    }
    
    /**
     * Returns the position of the pane separator.
     * 
     * @return the separator position
     */
    public Extent getSeparatorPosition() {
        return (Extent) getProperty(PROPERTY_SEPARATOR_POSITION);
    }
    
    /**
     * Returns the size of the pane separator.
     * 
     * @return the separator size
     */
    public Extent getSeparatorSize() {
        return (Extent) getProperty(PROPERTY_SEPARATOR_SIZE);
    }
    
    /**
     * Determines if the <code>SplitPane</code> is resizable.
     * 
     * @return true if the <code>SplitPane</code> is resizable
     */
    public boolean isResizable() {
        Boolean value = (Boolean) getProperty(PROPERTY_RESIZABLE);
        return value == null ? false : value.booleanValue();
    }
    
    /**
     * No more than two children may be added.
     * 
     * @see nextapp.echo2.app.Component#isValidChild(nextapp.echo2.app.Component)
     */
    public boolean isValidChild(Component component) {
        return getComponentCount() <= 1;
    }
    
    /**
     * @see nextapp.echo2.app.Component#processInput(java.lang.String, java.lang.Object)
     */
    public void processInput(String inputName, Object inputValue) {
        if (PROPERTY_SEPARATOR_POSITION.equals(inputName)) {
            setSeparatorPosition((Extent) inputValue);
        }
    }

    /**
     * Sets the orientation of the <code>SplitPane</code>.
     * 
     * @param newValue a constant representing the orientation, one of the 
     *        following values:
     *        <ul>
     *         <li><code>ORIENTATION_HORIZONTAL</code></li>
     *         <li><code>ORIENTATION_VERTICAL</code></li>
     *        </ul>
     */
    public void setOrientation(int newValue) {
        setProperty(PROPERTY_ORIENTATION, new Integer(newValue));
    }
    
    /**
     * Sets whether the <code>SplitPane</code> is resizable.
     * 
     * @param newValue true if the <code>SplitPane</code> should allow the
     *        resizing of panes by dragging the separator, false if it should
     *        not
     */
    public void setResizable(boolean newValue) {
        setProperty(PROPERTY_RESIZABLE, new Boolean(newValue));
    }
    
    /**
     * Sets the background image of the pane separator.
     * 
     * @param newValue the new background image
     */
    public void setSeparatorBackgroundImage(FillImage newValue) {
        setProperty(PROPERTY_SEPARATOR_BACKGROUND_IMAGE, newValue);
    }
    
    /**
     * Sets the color of the pane separator.
     * 
     * @param newValue the new color
     */
    public void setSeparatorColor(Color newValue) {
        setProperty(PROPERTY_SEPARATOR_COLOR, newValue);
    }
    
    /**
     * Sets the position of the pane separator.
     * 
     * @param newValue the new position
     */
    public void setSeparatorPosition(Extent newValue) {
        setProperty(PROPERTY_SEPARATOR_POSITION, newValue);
    }
    
    /**
     * Sets the size of the pane separator
     * 
     * @param newValue the new size
     */
    public void setSeparatorSize(Extent newValue) {
        setProperty(PROPERTY_SEPARATOR_SIZE, newValue);
    }
}
