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

package nextapp.echo2.app.text;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.FillImage;
import nextapp.echo2.app.Border;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.event.DocumentEvent;
import nextapp.echo2.app.event.DocumentListener;

/**
 * Abstract base class for text-entry components.
 */
public class TextComponent 
extends Component {
    
    public static final String PROPERTY_ALIGNMENT = "alignment";
    public static final String PROPERTY_BACKGROUND_IMAGE = "backgroundImage";
    public static final String PROPERTY_BORDER = "border";
    public static final String PROPERTY_HEIGHT = "height";
    public static final String PROPERTY_INSETS = "insets";
    public static final String PROPERTY_WIDTH = "width";
    
    public static final String DOCUMENT_CHANGED_PROPERTY = "document";
    public static final String TEXT_CHANGED_PROPERTY = "text";
    
    private Document document;
    
    /**
     * Local listener to monitor changes to document.
     */
    private DocumentListener documentListener = new DocumentListener() {

        /**
         * @see nextapp.echo2.app.event.DocumentListener#documentUpdate(nextapp.echo2.app.event.DocumentEvent)
         */
        public void documentUpdate(DocumentEvent e) {
            firePropertyChange(TEXT_CHANGED_PROPERTY, null, ((Document) e.getSource()).getText());
        }
    };
    
    /**
     * Creates a new <code>TextComponent</code> with the specified
     * <code>Document</code> as its model.
     * 
     * @param document the desired model
     */
    public TextComponent(Document document) {
        super();
        setDocument(document);
    }
    
    /**
     * Returns the alignment of the text component.
     * 
     * @return the alignment
     */
    public Alignment getAlignment() {
        return (Alignment) getProperty(PROPERTY_ALIGNMENT);
    }
    
    /**
     * Returns the default background image of the text component.
     * 
     * @return the background image
     */
    public FillImage getBackgroundImage() {
        return (FillImage) getProperty(PROPERTY_BACKGROUND_IMAGE);
    }
    
    /**
     * Returns the border of the text component.
     * 
     * @return the border
     */
    public Border getBorder() {
        return (Border) getProperty(PROPERTY_BORDER);
    }
    
    /**
     * Returns the model associated with this <code>TextComponent</code>.
     * 
     * @return the model
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Returns the height of the text component.
     * 
     * @return the height
     */
    public Extent getHeight() {
        return (Extent) getProperty(PROPERTY_HEIGHT);
    }
    
    /**
     * Returns the insets of the text component.
     * 
     * @return the insets
     */
    public Insets getInsets() {
        return (Insets) getProperty(PROPERTY_INSETS);
    }
    
    /**
     * Returns the text contained in the <code>Document</code> model of
     * this text component.
     * 
     * @return the text contained in the document
     */
    public String getText() {
        return document.getText();
    }
    
    /**
     * Returns the width of the text component.
     * 
     * @return the width
     */
    public Extent getWidth() {
        return (Extent) getProperty(PROPERTY_WIDTH);
    }
    
    /**
     * This component does not support children.
     * 
     * @see nextapp.echo2.app.Component#isValidChild(nextapp.echo2.app.Component)
     */
    public boolean isValidChild(Component component) {
        return false;
    }
    
    /**
     * @see nextapp.echo2.app.Component#processInput(java.lang.String, java.lang.Object)
     */
    public void processInput(String inputName, Object inputValue) {
        super.processInput(inputName, inputValue);
        
        if (TEXT_CHANGED_PROPERTY.equals(inputName)) {
            setText((String) inputValue);
        }
    }
    
    /**
     * Sets the alignment of the text component.
     * 
     * @param newValue the new alignment
     */
    public void setAlignment(Alignment newValue) {
        setProperty(PROPERTY_ALIGNMENT, newValue);
    }
    
    /**
     * Sets the default background image of the text component.
     * 
     * @param newValue the new background image
     */
    public void setBackgroundImage(FillImage newValue) {
        setProperty(PROPERTY_BACKGROUND_IMAGE, newValue);
    }
    
    /**
     * Sets the border of the text component.
     * 
     * @param newValue the new border
     */
    public void setBorder(Border newValue) {
        setProperty(PROPERTY_BORDER, newValue);
    }

    /**
     * Sets the model associated with this <code>TextComponent</code>.
     * 
     * @param newValue the new model (may not be null)
     */
    public void setDocument(Document newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException("Document may not be null.");
        }
        Document oldValue = getDocument();
        if (oldValue != null) {
            oldValue.removeDocumentListener(documentListener);
        }
        newValue.addDocumentListener(documentListener);
        document = newValue;
    }
    
    /**
     * Sets the height of the text component.
     * 
     * @param newValue the new height
     */
    public void setHeight(Extent newValue) {
        setProperty(PROPERTY_HEIGHT, newValue);
    }
    
    /**
     * Sets the insets of the text component.
     * 
     * @param newValue the new insets
     */
    public void setInsets(Insets newValue) {
        setProperty(PROPERTY_INSETS, newValue);
    }
    
    /**
     * Sets the text of document model of this text component.
     * 
     * @param newValue the new text
     */
    public void setText(String newValue) {
        getDocument().setText(newValue);
    }
    
    /**
     * Sets the width of the text component.
     * 
     * @param newValue the new width
     */
    public void setWidth(Extent newValue) {
        setProperty(PROPERTY_WIDTH, newValue);
    }
}
