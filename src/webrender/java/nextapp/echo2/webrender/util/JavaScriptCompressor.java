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

package nextapp.echo2.webrender.util;

/**
 * Compresses the size of JavaScript resource files by removing comments and 
 * white space.
 */
public class JavaScriptCompressor {
    
    //BUGBUG. Disabled JS Compressor until URL mangling fixed.
    private static final boolean DISABLE_JAVASCRIPT_COMPRESSOR = true;
    
    private static final char LINE_FEED = '\n';
    private static final char CARRIAGE_RETURN = '\r';    
    private static final char SPACE = ' ';
    
    /**
     * Compresses a JavaScript file.
     *
     * @param script The contents of the JavaScript file as  a 
     *        <code>String</code>.
     * @return A compressed version of the JavaScript file, with its comments
     *         and white space removed.
     */
    public static String compress(String script) {
        if (DISABLE_JAVASCRIPT_COMPRESSOR) {
            return script;
        }
        
        StringBuffer scriptBuffer = new StringBuffer(replaceCRWithLF(script));
        replaceCtrlWithSpaces(scriptBuffer);
        removeLineComments(scriptBuffer);
        removeBoxComments(scriptBuffer);
        removeMultipleCharacters(scriptBuffer, SPACE);
        removeCharacterAfterAnother(scriptBuffer, SPACE, LINE_FEED);
        removeCharacterBeforeAnother(scriptBuffer, SPACE, LINE_FEED);
        removeMultipleCharacters(scriptBuffer, LINE_FEED);        
        return scriptBuffer.toString();
    }
    
    /**
     * Replaces all Carriage Return characters with a Line Feeds.
     */
    private static String replaceCRWithLF(String string) {
        return string.replace(CARRIAGE_RETURN, LINE_FEED);
    }
    
    /**
     * Replaces all Control characters except Linefeeds with Spaces.
     */
    private static void replaceCtrlWithSpaces(StringBuffer buffer) {
        for (int i = 0; i < buffer.length(); i++) {
            char testChar = buffer.charAt(i);
            if (Character.isISOControl(testChar) && (testChar != LINE_FEED)) {
                buffer.setCharAt(i, SPACE);
            }
        }
    }
    
    /**
     * Removes line comments.
     */
    private static void removeLineComments(StringBuffer buffer) {
//BUGBUG! Mangles URLS. (http://)        
        String string = buffer.toString();
        while (string.indexOf("//") != -1) {
            int commentStart = string.indexOf("//");
            int commentEnd = string.indexOf(LINE_FEED, commentStart);
            if (commentEnd == -1) {
                commentEnd = buffer.length() - 1;
            }
            buffer.delete(commentStart, commentEnd);
            string = buffer.toString();
        }
    }
    
    /**
     * Remove Box comments.
     */
    private static void removeBoxComments(StringBuffer buffer) {
        String string = buffer.toString();
        while (string.indexOf("/*") != -1) {
            int commentStart = string.indexOf("/*");
            int commentEnd = string.indexOf("*/", commentStart);
            if (commentEnd != -1) {
                commentEnd = commentEnd + 2;
            } else {
                commentEnd = buffer.length() - 1;
            }
            buffer.delete(commentStart, commentEnd);
            string = buffer.toString();
        }
    }
    
    /**
     * Removes duplicated characters.
     */
    private static void removeMultipleCharacters(StringBuffer buffer, char charToRemove) {
        for (int i = buffer.length() - 1; i > 0; i--) {
            char currentChar = buffer.charAt(i);
            char previousChar = buffer.charAt(i - 1);
            if ((currentChar == previousChar) && (currentChar == charToRemove)) {
                buffer.deleteCharAt(i);
            }
        }
    }
    
    /**
     * Removes one character before another.
     */
    private static void removeCharacterBeforeAnother(StringBuffer buffer, char charToRemove, char charAfter) {
        StringBuffer searchBuffer = new StringBuffer(2);
        searchBuffer.append(charToRemove);
        searchBuffer.append(charAfter);
        String searchString = searchBuffer.toString();

        String string = buffer.toString();
        int deleteIndex = string.indexOf(searchString);
        while (deleteIndex != -1) {
            buffer.deleteCharAt(deleteIndex);
            string = buffer.toString();
            deleteIndex = string.indexOf(searchString);
        }
    }
    
    /**
     * Removes a character after another.
     */
    private static void removeCharacterAfterAnother(StringBuffer buffer, char charToRemove, char charBefore) {
        StringBuffer searchBuffer = new StringBuffer(2);
        searchBuffer.append(charBefore);
        searchBuffer.append(charToRemove);
        String searchString = searchBuffer.toString();
        
        String string = buffer.toString();
        int deleteIndex = string.indexOf(searchString);
        while (deleteIndex != -1) {
            buffer.deleteCharAt(deleteIndex + 1);
            string = buffer.toString();
            deleteIndex = string.indexOf(searchString);
        }
    }
    
    /** Non-instantiable class. */
    private JavaScriptCompressor() { }
}
