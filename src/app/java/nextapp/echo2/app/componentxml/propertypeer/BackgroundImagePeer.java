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

package nextapp.echo2.app.componentxml.propertypeer;

import org.w3c.dom.Element;

import nextapp.echo2.app.BackgroundImage;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.componentxml.InvalidPropertyException;
import nextapp.echo2.app.componentxml.PropertyLoader;
import nextapp.echo2.app.componentxml.PropertyXmlPeer;
import nextapp.echo2.app.util.DomUtil;

/**
 * <code>PropertyXmlPeer</code> implementation for 
 * <code>nextapp.echo2.BackgroundImage</code> properties.
 */
public class BackgroundImagePeer 
implements PropertyXmlPeer {

    /**
     * @see nextapp.echo2.app.componentxml.PropertyXmlPeer#getValue(java.lang.ClassLoader, 
     *      java.lang.Class, org.w3c.dom.Element)
     */
    public Object getValue(ClassLoader classLoader, Class objectClass, Element propertyElement)
    throws InvalidPropertyException {
        Element backgroundImageElement = DomUtil.getChildElementByTagName(propertyElement, "backgroundimage");
        if (backgroundImageElement == null) {
            throw new InvalidPropertyException("Invalid BackgroundImage property.", null);
        }
        
        int attachment = "fixed".equals(backgroundImageElement.getAttribute("attachment")) 
                ? BackgroundImage.ATTACHMENT_FIXED : BackgroundImage.ATTACHMENT_SCROLL;
        Extent offsetX = backgroundImageElement.hasAttribute("horizontal")
                ? ExtentPeer.toExtent(backgroundImageElement.getAttribute("horizontal")) : null;
        Extent offsetY = backgroundImageElement.hasAttribute("vertical")
                ? ExtentPeer.toExtent(backgroundImageElement.getAttribute("vertical")) : null;
        
        int repeat;
        String repeatString = backgroundImageElement.getAttribute("repeat");
        if ("horizontal".equals(repeatString)) {
            repeat = BackgroundImage.REPEAT_HORIZONTAL;
        } else if ("vertical".equals(repeatString)) {
            repeat = BackgroundImage.REPEAT_VERTICAL;
        } else if ("none".equals(repeatString)) {
            repeat = BackgroundImage.NO_REPEAT;
        } else {
            repeat = BackgroundImage.REPEAT;
        }
        
        Element imageElement = DomUtil.getChildElementByTagName(backgroundImageElement, "image");
        if (imageElement == null) {
            throw new InvalidPropertyException("Invalid BackgroundImage property.", null);
        }
        String imageType = imageElement.getAttribute("type");
        PropertyLoader propertyLoader = PropertyLoader.forClassLoader(classLoader);
        
        Class propertyClass;
        try {
            propertyClass = Class.forName(imageType, true, classLoader);
        } catch (ClassNotFoundException ex) {
            throw new InvalidPropertyException("Invalid BackgroundImage property (type \"" + imageType + "\" not found.", ex);
        }
        
        Object imagePropertyValue = propertyLoader.getPropertyValue(BackgroundImage.class, propertyClass, imageElement);
        if (!(imagePropertyValue instanceof ImageReference)) {
            throw new InvalidPropertyException("Invalid BackgroundImage proeprty (type \"" + imageType 
                    + "\" is not an ImageReference.", null);
        }

        ImageReference imageReference = (ImageReference) imagePropertyValue;
        BackgroundImage backgroundImage = new BackgroundImage(imageReference, offsetX, offsetY, repeat, attachment);
        
        return backgroundImage;
    }
}
