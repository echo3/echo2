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

/*
 * This file contains utility objects used by the Echo2's built-in 
 * library of components.
 */
  
//__________________________
// Object EchoCoreProperties

/**
 * Static object/namespace containing namespaces for client-side 
 * representations of key nextapp.echo2.app properties.
 */
EchoCoreProperties = { };

/**
 * Object representing a nextapp.echo2.app.FillImgaeBorder property value.
 */
EchoCoreProperties.FillImageBorder = Core.extend({

    $static: {
        
        // Constants for border image positions.
        IMAGE_TL: 0,
        IMAGE_T: 1,
        IMAGE_TR: 2,
        IMAGE_L: 3,
        IMAGE_R: 4,
        IMAGE_BL: 5,
        IMAGE_B: 6,
        IMAGE_BR: 7
    },

    /**
     * Creates a new FillImageBorder instance.
     *
     * @param color the color as a hexadecimal string (e.g., "#123abc")
     * @param borderInsets an EchoCoreProperties.Insets object representing
     *        the border insets
     * @param contentInsts an EchoCoreProperties.Insets object representing
     *        the content insets
     * @param fillImages an (optional) array of eight strings containing CSS
     *        style data to represent the border
     */
    $construct: function(color, borderInsets, contentInsets, fillImages) {
        this.borderInsets = borderInsets;
        this.contentInsets = contentInsets;
        this.color = color;
        if (fillImages) {
            if (fillImages.length != 8) {
                throw new Error("Image array must contain eight images.");
            }
            this.fillImages = fillImages;
        } else {
            this.fillImages = new Array(8);
        }
    }
});

/**
 * Object representing a nextapp.echo2.app.Insets property value.
 */
EchoCoreProperties.Insets = Core.extend({

    /**
     * Creates a new Insets instance.
     * This object is currently limited to dealing only with pixel based-insets.
     * Behavior is undefined for non-pixel-based insets.
     */
    $construct: function() {
        this.top = 0;
        this.right = 0;
        this.bottom = 0;
        this.left = 0;
        
        if (arguments.length == 1) {
            this.loadValuesFromString(arguments[0]);
        } else if (arguments.length == 2) {
            this.top = this.bottom = arguments[0];
            this.right = this.left = arguments[1];
        } else if (arguments.length == 4) {
            this.top = arguments[0];
            this.right = arguments[1];
            this.bottom = arguments[2];
            this.left = arguments[3];
        }
    },
    
    loadValuesFromString: function(insetsString) {
        insetsString = new String(insetsString);
        var elements = insetsString.split(" ");
        switch (elements.length) {
        case 1:
            this.top = this.left = this.right = this.bottom = parseInt(elements[0]);
            break;
        case 2:
            this.top = this.bottom = parseInt(elements[0]);
            this.right = this.left = parseInt(elements[1]);
            break;
        case 3:
            this.top = parseInt(elements[0]);
            this.right = this.left = parseInt(elements[1]);
            this.bottom = parseInt(elements[2]);
            break;
        case 4:
            this.top = parseInt(elements[0]);
            this.right = parseInt(elements[1]);
            this.bottom = parseInt(elements[2]);
            this.left = parseInt(elements[3]);
            break;
        default:
            throw "Illegal inset value: " + insetsString;
        }
    },
    
    toString: function(insetsString) {
        if (this.top == this.bottom && this.right == this.left) {
            if (this.top == this.right) {
                return this.top + "px"
            } else {
                return this.top + "px " + this.right + "px";
            }
        } else {
            return this.top + "px " + this.right + "px " + this.bottom + "px " + this.left + "px";
        }
    }
});