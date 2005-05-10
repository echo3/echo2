/*
 * This file is part of the Echo Web Application Framework (hereinafter "Echo").
 * Copyright (C) 2002-2005 NextApp, Inc.
 * 
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or the
 * GNU Lesser General Public License Version 2.1 or later (the "LGPL"), in which
 * case the provisions of the GPL or the LGPL are applicable instead of those
 * above. If you wish to allow use of your version of this file only under the
 * terms of either the GPL or the LGPL, and not to allow others to use your
 * version of this file under the terms of the MPL, indicate your decision by
 * deleting the provisions above and replace them with the notice and other
 * provisions required by the GPL or the LGPL. If you do not delete the
 * provisions above, a recipient may use your version of this file under the
 * terms of any one of the MPL, the GPL or the LGPL.
 */

//BUGBUG. Doc.
//BUGBUG. needs equals.

package nextapp.echo2.app;

/**
 * A represntation of a graphical border drawn using a series of 
 * <code>FillImage</code>s.
 */
public class FillImageBorder {

    public static final int TOP_LEFT = 0;
    public static final int TOP = 1;
    public static final int TOP_RIGHT= 2;
    public static final int LEFT = 3;
    public static final int RIGHT = 4;
    public static final int BOTTOM_LEFT= 5;
    public static final int BOTTOM = 6;
    public static final int BOTTOM_RIGHT = 7;
    
    private Insets contentInsets, borderInsets;
    private Color color;
    private FillImage[] fillImages;

    /**
     * Creates a new <code>FillImageBorder</code>.
     */
    public FillImageBorder() {
        super();
    }
    
    /**
     * Creates a new <code>FillImageBorder</code> with the specified color,
     * border insets, and content insets.
     */
    public FillImageBorder(Color color, Insets borderInsets, Insets contentInsets) {
        super();
        this.color = color;
        this.borderInsets = borderInsets;
        this.contentInsets = contentInsets;
    }

    public Insets getBorderInsets() {
        return borderInsets;
    }
    
    public Color getColor() {
        return color;
    }

    public Insets getContentInsets() {
        return contentInsets;
    }

    /**
     * Retrieves the <code>FillImage</code> at the specified position.
     * 
     * @param position the position, one of the following values:
     *        <ul>
     *         <li><code>TOP_LEFT</code> the top left corner image</li>
     *         <li><code>TOP</code> the top side image</li>
     *         <li><code>TOP_RIGHT</code> the top right corner image</li>
     *         <li><code>LEFT</code> the left side image</li>
     *         <li><code>RIGHT</code> the right side image</li>
     *         <li><code>BOTTOM_LFET</code> the bottom left corner image</li>
     *         <li><code>BOTTOM</code> the bottom side image</li>
     *         <li><code>BOTTOM_RIGHT</code> the bottom right corner image</li>
     *        </ul>
     * @return the <code>FillImage</code>
     */
    public FillImage getFillImage(int position) {
        if (fillImages == null) {
            return null;
        } else {
            return fillImages[position];
        }
    }
    
    public void setBorderInsets(Insets borderInsets) {
        this.borderInsets = borderInsets;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }

    public void setContentInsets(Insets contentInsets) {
        this.contentInsets = contentInsets;
    }

    /**
     * Sets the <code>FillImage</code> at the specified position.
     * 
     * @param position the position, one of the following values:
     *        <ul>
     *         <li><code>TOP_LEFT</code> the top left corner image</li>
     *         <li><code>TOP</code> the top side image</li>
     *         <li><code>TOP_RIGHT</code> the top right corner image</li>
     *         <li><code>LEFT</code> the left side image</li>
     *         <li><code>RIGHT</code> the right side image</li>
     *         <li><code>BOTTOM_LFET</code> the bottom left corner image</li>
     *         <li><code>BOTTOM</code> the bottom side image</li>
     *         <li><code>BOTTOM_RIGHT</code> the bottom right corner image</li>
     *        </ul>
     * @param fillImage the new <code>FillIamge</code>
     */
    public void setFillImage(int position, FillImage fillImage) {
        if (fillImages == null) {
            if (fillImage == null) {
                return;
            }
            fillImages = new FillImage[8];
        }
        fillImages[position] = fillImage;
    }
}