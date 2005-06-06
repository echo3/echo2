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

package nextapp.echo2.webcontainer;

import java.util.HashMap;
import java.util.Map;

import nextapp.echo2.app.update.ServerComponentUpdate;

/**
 * A utility class for rendering a collection of property updates to an
 * existing HTML representation of a component on the client browser.
 */
public class PartialUpdateManager {
    
    //BUGBUG? May wish to add ConditionalPropertyRender objects that report back
    //whether a specific property update is possible based on prop. name &value
    //instead of simply assuming the update is possible based on name alone.
    
    private Map registry = null;

    /**
     * Adds a <code>PartialUpdateParticipant</code> to handle a given property.
     * 
     * @param propertyName the name of the property
     * @param propertyRender the renderer
     */
    public void add(String propertyName, PartialUpdateParticipant propertyRender) {
        if (registry == null) {
            registry = new HashMap();
        }
        registry.put(propertyName, propertyRender);
    }
    
    /**
     * Determines if this <code>PartialUpdateManager</code> has renderers
     * to update all changed properties specified in <code>update</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update
     * @return true if this registry is capable of performing all the 
     *         described property updates
     */
    public boolean canProcess(RenderContext rc, ServerComponentUpdate update) {
        if (registry == null) {
            return false;
        }
        String[] propertyNames = update.getUpdatedPropertyNames();
        for (int i = 0; i < propertyNames.length; ++i) {
            PartialUpdateParticipant propertyRender = (PartialUpdateParticipant) registry.get(propertyNames[i]);
            if (propertyRender == null) {
                return false;
            } else if (!propertyRender.canRenderProperty(rc, update)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Renders updates to all properties in the provided <code>update</code>.
     * If the update contains a property for which a renderer does not exist in
     * this registry, the given property is skipped.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param update the update to process
     */
    public void process(RenderContext rc, ServerComponentUpdate update) {
        String[] propertyNames = update.getUpdatedPropertyNames();
        for (int i = 0; i < propertyNames.length; ++i) {
            PartialUpdateParticipant propertyRender = (PartialUpdateParticipant) registry.get(propertyNames[i]);
            if (propertyRender == null) {
                // If no renderer exists, discard the property update.
                continue;
            }
            propertyRender.renderProperty(rc, update);
        }
    }
}
