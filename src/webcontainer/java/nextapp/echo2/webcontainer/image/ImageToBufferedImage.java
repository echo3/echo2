/* 
 * This file is part of the Echo Web Application Framework (hereinafter "Echo").
 * Copyright (C) 2002-2009 NextApp, Inc.
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

package nextapp.echo2.webcontainer.image;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

/**
 * Utility class for converting <code>Image</code>s to <code>BufferedImage</code>s.
 */
class ImageToBufferedImage {

    /**
     * Converts an <code>Image</code> to a <code>BufferedImage</code>.
     * If the image is already a <code>BufferedImage</code>, the original is returned.
     * 
     * @param image the image to convert
     * @return the image as a <code>BufferedImage</code>
     */
    static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            // Return image unchanged if it is already a BufferedImage.
            return (BufferedImage) image;
        }
        
        // Ensure image is loaded.
        image = new ImageIcon(image).getImage();        
        
        int type = hasAlpha(image) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        Graphics g = bufferedImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        
        return bufferedImage;
    }
    
    /**
     * Determines if an image has an alpha channel.
     * 
     * @param image the <code>Image</code>
     * @return true if the image has an alpha channel
     */
    static boolean hasAlpha(Image image) {
        PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
        try {
            pg.grabPixels();
        } catch (InterruptedException ex) { }
        return pg.getColorModel().hasAlpha();
    }

    /**
     * Non-instantiable class.
     */
    private ImageToBufferedImage() { }
}
