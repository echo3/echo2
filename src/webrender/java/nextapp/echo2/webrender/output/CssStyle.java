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

package nextapp.echo2.webrender.output;

/**
 * A renderable representation of a single CSS style.
 */
public class CssStyle {
    
    // Note that this class uses a proprietary associative array implementation
    // in the interest of performance/memory allocation during rendering.  The
    // implementation is tuned for very low numbers of keys/value which will be
    // the typical case in describing a CSS style.
    
    private static final int GROW_RATE = 5 * 2;  // Must be a multiple of 2.
    private static final String[] EMPTY = new String[0];
    
    private String[] data = EMPTY;
    int length = 0; // Number of items * 2;
    
    /**
     * Retrieves a style attribute value.
     * 
     * @param attributeName the name of the attribute
     * @return the value of the attribute (null if it is not set)
     */
    public String getAttribute(String attributeName) {
        for (int i = 0; i < length; i += 2) {
            if (data[i].equals(attributeName)) {
                return data[i + 1];
            }
        }
        return null;
    }
    
    /**
     * Determines if any attributes are set.
     * 
     * @return true if any attributes are set.
     */
    public boolean hasAttributes() {
        return length > 0;
    }
    
    /**
     * Sets a style attribute value.
     * 
     * @param attributeName the name of the attribute.
     * @param attributeValue the value of the attribute.
     */
    public void setAttribute(String attributeName, String attributeValue) {
        if (data == EMPTY) {
            data = new String[GROW_RATE];
        }

        int propertyNameHashCode = attributeName.hashCode();
        for (int i = 0; i < data.length; i += 2) {
            if (data[i] == null) {
                // Property is not set, space remains to set property.
                // Add property at end.
                data[i] = attributeName;
                data[i + 1] = attributeValue;
                length += 2;
                return;
            }
            if (propertyNameHashCode == data[i].hashCode() && attributeName.equals(data[i])) {
                // Found property, overwrite.
                data[i + 1] = attributeValue;
                return;
            }
        }
        
        // Array is full: grow array.
        String[] newData = new String[data.length + GROW_RATE];
        System.arraycopy(data, 0, newData, 0, data.length);
        
        newData[data.length] = attributeName;
        newData[data.length + 1] = attributeValue;
        length += 2;
        data = newData;
    }
    
    /**
     * Renders the style inline.  The returned value is suitable as the value
     * of the "style" attribute of an HTML element.
     * 
     * @return the inline representation
     */
    public String renderInline() {
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < length; i += 2) {
            out.append(data[i]);
            out.append(":");
            out.append(data[i + 1]);
            out.append(";");
        }
        return out.toString();
    }
    
    /**
     * Renders a debug representation of the object.
     *  
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return CssStyle.class.getName() + " {" + renderInline() + "}";
    }
}
