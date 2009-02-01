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

package echo2example.email;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/**
 * Utilities for rendering e-mail content.
 */
public class MessageUtil {

    /**
     * Remove control characters from SPAM messages.
     * 
     * @param source the source text
     * @param wordLengthLimit the maximum length of a word (words longer
     *        than this length will have spaces inserted; negative values
     *        indicate unlimited length)
     * @param lengthLimit Maximum length of text (text longer than this
     *        length will be cut; negative values indicate unlimited length)
     * @return the translated string
     */
    public static final String clean(String source, int wordLengthLimit, int lengthLimit) {
        if (source == null) {
            return null;
        }
        StringBuffer out = new StringBuffer();
        CharacterIterator ci = new StringCharacterIterator(source);
        int wordLength = 0;
        int textLength = 0;
        char ch = ci.first();
        while (ch != CharacterIterator.DONE) {
            if (lengthLimit > 0) {
                if (textLength > lengthLimit) {
                    out.append("...");
                    break;
                }
                ++textLength;
            }
            if (wordLengthLimit > 0) {
                if (Character.isWhitespace(ch)) {
                    wordLength = 0;
                } else {
                    ++wordLength;
                }
                if (wordLength > wordLengthLimit) {
                    wordLength = 0;
                    out.append(" ");
                }
            }
            if (ch >= 0x20) {
                out.append(ch);
            }
            ch = ci.next();
        }
        return out.toString();
    }

    /** Non-instantiable class. */
    private MessageUtil() { }
}
