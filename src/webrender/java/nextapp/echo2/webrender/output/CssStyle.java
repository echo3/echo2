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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A representation of a single CSS style.
 */
public class CssStyle {

    private Map styleData = new HashMap();
    
    /**
     * Retrieves a style attribute value.
     * 
     * @param attributeName the name of the attribute
     * @return the value of the attribute (null if it is not set)
     */
    public String getAttribute(String attributeName) {
        return (String) styleData.get(attributeName);
    }
    
    /**
     * Determines if any attributes are set.
     * 
     * @return true if any attributes are set.
     */
    public boolean hasAttributes() {
        return styleData.size() > 0;
    }
    
    /**
     * Sets a style attribute value.
     * 
     * @param attributeName the name of the attribute.
     * @param attributeValue the value of the attribute.
     */
    public void setAttribute(String attributeName, String attributeValue) {
        styleData.put(attributeName, attributeValue);
    }
    
    /**
     * Renders the style inline.  The returned value is suitable as the value
     * of the "style" attribute of an HTML element.
     * 
     * @return the inline representation
     */
    public String renderInline() {
        Iterator it = styleData.keySet().iterator();
        StringBuffer out = new StringBuffer();
        while (it.hasNext()) {
            String attributeName = (String) it.next();
            String attributeValue = (String) styleData.get(attributeName);
            out.append(attributeName);
            out.append(":");
            out.append(attributeValue);
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
