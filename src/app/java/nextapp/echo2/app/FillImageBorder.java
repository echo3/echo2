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
//BUGBUG? Restructure API for borders...this is BAD.
package nextapp.echo2.app;

public class FillImageBorder {

    private Insets contentInsets, borderInsets;
    private Color color;
    private FillImage northWest, north, northEast, west, east, southWest, south, southEast;

    public FillImageBorder() {
    }
    
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

    public FillImage getEast() {
        return east;
    }

    public FillImage getNorth() {
        return north;
    }

    public FillImage getNorthEast() {
        return northEast;
    }

    public FillImage getNorthWest() {
        return northWest;
    }

    public FillImage getSouth() {
        return south;
    }

    public FillImage getSouthEast() {
        return southEast;
    }

    public FillImage getSouthWest() {
        return southWest;
    }

    public FillImage getWest() {
        return west;
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

    public void setEast(FillImage east) {
        this.east = east;
    }

    public void setNorth(FillImage north) {
        this.north = north;
    }

    public void setNorthEast(FillImage northEast) {
        this.northEast = northEast;
    }

    public void setNorthWest(FillImage northWest) {
        this.northWest = northWest;
    }

    public void setSouth(FillImage south) {
        this.south = south;
    }

    public void setSouthEast(FillImage southEast) {
        this.southEast = southEast;
    }

    public void setSouthWest(FillImage southWest) {
        this.southWest = southWest;
    }

    public void setWest(FillImage west) {
        this.west = west;
    }
}