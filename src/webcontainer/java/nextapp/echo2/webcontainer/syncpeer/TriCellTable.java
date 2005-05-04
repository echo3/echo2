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

package nextapp.echo2.webcontainer.syncpeer;

import nextapp.echo2.app.Extent;
import nextapp.echo2.webcontainer.propertyrender.ExtentRender;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Renders an HTML table that has two or three &quot;container&quot; cells and
 * independently settable margins between them.  These tables are useful for
 * rendering buttons that have two or three elements (images, text labels, and 
 * state indicators).  This class supports all possible permutations for
 * placement of each of the two or three contained components.
 */
public class TriCellTable {

    static final int INVERTED = 1;
    static final int VERTICAL = 2;
    
    public static final int LEFT_RIGHT = 0;
    public static final int RIGHT_LEFT = INVERTED;
    public static final int TOP_BOTTOM = VERTICAL;
    public static final int BOTTOM_TOP = INVERTED | VERTICAL;

    private Element[] tdElements;
    private Element[] marginTdElements = null;
    private Element tableElement;
    private Element tbodyElement;
    private Document document;
    
    /**
     * This constructor is called by the non-private constructors to set up 
     * common properties.
     * 
     * @param id The id the id upon which the inner elements will be based.
     *        the id of the returned table element is not set and must be
     *        set appropriately by the caller.
     */
    private TriCellTable(Document document, String id) {
        super();
        this.document = document;
        tableElement = document.createElement("table");
        tbodyElement = document.createElement("tbody");
        tbodyElement.setAttribute("id", id + "_tbody");
        tableElement.appendChild(tbodyElement);
    }

    /**
     * Creates a two-celled <code>TriCellTable</code>.
     * 
     * @param document the outgoing XML document
     * @param id the id of the root element
     * @param orientation0_1 The orientation of Element 0 with respect to 
     *        Element 1, one of the following values:
     *        <ul>
     *        <li>LEFT_RIGHT (element 0 is to the left of element 1)</li>
     *        <li>RIGHT_LEFT (element 1 is to the left of element 0)</li>
     *        <li>TOP_BOTTOM (element 0 is above element 1)</li>
     *        <li>BOTTOM_TOP (element 1 is above element 0)</li>
     *        </ul>
     * @param margin0_1 The margin size between element 0 and element 1.
     */
    TriCellTable(Document document, String id, int orientation0_1, Extent margin0_1) {
        this(document, id);
        
        marginTdElements = new Element[1];
        tdElements = new Element[2];
        tdElements[0] = document.createElement("td");
        tdElements[0].setAttribute("id", id + "_td_0");
        tdElements[1] = document.createElement("td");
        tdElements[0].setAttribute("id", id + "_td_1");
        
        if (margin0_1 != null && margin0_1.getValue() > 0) {
            marginTdElements[0] = document.createElement("td");
            marginTdElements[0].setAttribute("id", id + "_tdmargin_0_1");
            if ((orientation0_1 & VERTICAL) == 0) {
                marginTdElements[0].setAttribute("style", "width:" + ExtentRender.renderCssAttributeValue(margin0_1));
            } else {
                marginTdElements[0].setAttribute("style", "height:" + ExtentRender.renderCssAttributeValue(margin0_1));
            }
        }
                
        if ((orientation0_1 & VERTICAL) == 0) {
            // horizontally oriented
            Element trElement = document.createElement("tr");
            trElement.setAttribute("id", id + "_tr_0_1");
            if ((orientation0_1 & INVERTED) == 0) {
                // normal (left to right)
                addColumn(trElement, tdElements[0]);
                addColumn(trElement, marginTdElements[0]);
                addColumn(trElement, tdElements[1]);
            } else {
                // inverted (right to left)
                addColumn(trElement, tdElements[1]);
                addColumn(trElement, marginTdElements[0]);
                addColumn(trElement, tdElements[0]);
            }
            tbodyElement.appendChild(trElement);
        } else {
            // vertically oriented
            if ((orientation0_1 & INVERTED) == 0) {
                // normal (top to bottom)
                addRow(tdElements[0], id + "_tr_0");
                addRow(marginTdElements[0], id + "_trmargin_0_1");
                addRow(tdElements[1], id + "_tr_1");
            } else {
                // inverted (bottom to top)
                addRow(tdElements[1], id + "_tr_1");
                addRow(marginTdElements[0], id + "_trmargin_0_1");
                addRow(tdElements[0], id + "_tr_0");
            }
        }
    }
    
    /**
     * Creates a three-celled <code>TriCellTable</code>.
     * 
     * @param document the outgoing XML document
     * @param id the id of the root element
     * @param orientation0_1 The orientation of Element 0 with respect to 
     *        Element 1, one of the following values:
     *        <ul>
     *        <li>LEFT_RIGHT (element 0 is to the left of element 1)</li>
     *        <li>RIGHT_LEFT (element 1 is to the left of element 0)</li>
     *        <li>TOP_BOTTOM (element 0 is above element 1)</li>
     *        <li>BOTTOM_TOP (element 1 is above element 0)</li>
     *        </ul>
     * @param margin0_1 The margin size between element 0 and element 1.
     * @param orientation01_2 The orientation of Elements 0 and 1 with 
     *        respect to Element 2, one of the following values:
     *        <ul>
     *        <li>LEFT_RIGHT (elements 0 and 1 are to the left of 
     *        element 1)</li>
     *        <li>RIGHT_LEFT (element 2 is to the left of elements 0 and 1)</li>
     *        <li>TOP_BOTTOM (elements 0 and 1 are above element 2)</li>
     *        <li>BOTTOM_TOP (element 2 is above elements 0 and 1)</li>
     *        </ul>
     * @param margin01_2 The margin size between the combination
     *        of elements 0 and 1 and element 2.
     */
    TriCellTable(Document document, String id, int orientation0_1, Extent margin0_1, int orientation01_2, Extent margin01_2) {
        this(document, id);
        
        Element trElement;
        
        marginTdElements = new Element[2];
        tdElements = new Element[3];
        tdElements[0] = document.createElement("td");
        tdElements[0].setAttribute("id", id + "_td_0");
        tdElements[1] = document.createElement("td");
        tdElements[1].setAttribute("id", id + "_td_1");
        tdElements[2] = document.createElement("td");
        tdElements[2].setAttribute("id", id + "_td_2");

        // Create margin cells
        if (margin0_1 != null || margin01_2 != null) {
            if (margin0_1 != null && margin0_1.getValue() > 0) {
                marginTdElements[0] = document.createElement("td");
                marginTdElements[0].setAttribute("id", id + "_tdmargin_0_1");
                if ((orientation0_1 & VERTICAL) == 0) {
                    marginTdElements[0].setAttribute("style", "width:" + ExtentRender.renderCssAttributeValue(margin0_1));
                } else {
                    marginTdElements[0].setAttribute("style", "height:" + ExtentRender.renderCssAttributeValue(margin0_1));
                }
            }
            if (margin01_2 != null && margin01_2.getValue() > 0) {
                marginTdElements[1] = document.createElement("td");
                marginTdElements[1].setAttribute("id", id + "_tdmargin_01_2");
                if ((orientation01_2 & VERTICAL) == 0) {
                    marginTdElements[1].setAttribute("style", "width:" + ExtentRender.renderCssAttributeValue(margin01_2));
                } else {
                    marginTdElements[1].setAttribute("style", "height:" + ExtentRender.renderCssAttributeValue(margin01_2));
                }
            }
        }
        
        if ((orientation0_1 & VERTICAL) == 0) {
            // horizontally oriented 0/1
            if ((orientation01_2 & VERTICAL) == 0) {
                // horizontally orientated 01/2
                trElement = document.createElement("tr");
                trElement.setAttribute("id", id + "_tr_0");
                if ((orientation01_2 & INVERTED) != 0) {
                    // 2 before 01: render #2 and margin at beginning of TR.
                    addColumn(trElement, tdElements[2]);
                    addColumn(trElement, marginTdElements[1]);
                }
                
                // Render 01
                if ((orientation0_1 & INVERTED) == 0) {
                    // normal (left to right)
                    addColumn(trElement, tdElements[0]);
                    addColumn(trElement, marginTdElements[0]);
                    addColumn(trElement, tdElements[1]);
                } else {
                    // inverted (right to left)
                    addColumn(trElement, tdElements[1]);
                    addColumn(trElement, marginTdElements[0]);
                    addColumn(trElement, tdElements[0]);
                }
                
                if ((orientation01_2 & INVERTED) == 0) {
                    addColumn(trElement, marginTdElements[1]);
                    addColumn(trElement, tdElements[2]);
                }
                
                tbodyElement.appendChild(trElement);
            } else {
                // vertically oriented 01/2

                // determine and apply column span based on presence of margin between 0 and 1
                int columns = (margin0_1 != null && margin0_1.getValue() > 0) ? 3 : 2;
                tdElements[2].setAttribute("colspan", Integer.toString(columns));
                if (marginTdElements[1] != null) {
                    marginTdElements[1].setAttribute("colspan", Integer.toString(columns));
                }
                
                if ((orientation01_2 & INVERTED) != 0) {
                    // 2 before 01: render #2 and margin at beginning of TR.
                    addRow(tdElements[2], id + "_tr_2");
                    addRow(marginTdElements[1], id + "_trmargin_01_2");
                }
                
                // Render 01
                trElement = document.createElement("tr");
                trElement.setAttribute("id", "tr_" + id);
                if ((orientation0_1 & INVERTED) == 0) {
                    // normal (left to right)
                    addColumn(trElement, tdElements[0]);
                    addColumn(trElement, marginTdElements[0]);
                    addColumn(trElement, tdElements[1]);
                } else {
                    // inverted (right to left)
                    addColumn(trElement, tdElements[1]);
                    addColumn(trElement, marginTdElements[0]);
                    addColumn(trElement, tdElements[0]);
                }
                tbodyElement.appendChild(trElement);
                
                if ((orientation01_2 & INVERTED) == 0) {
                    // 01 before 2: render margin and #2 at end of TR.
                    addRow(marginTdElements[1], id + "_trmargin_01_2");
                    addRow(tdElements[2], id + "_tr_2");
                }
            }
        } else {
            // vertically oriented 0/1
            if ((orientation01_2 & VERTICAL) == 0) {
                // horizontally orientated 01/2

                // determine and apply row span based on presence of margin between 0 and 1
                int rows = (margin0_1 != null && margin0_1.getValue() > 0) ? 3 : 2;
                tdElements[2].setAttribute("rowspan", Integer.toString(rows));
                if (marginTdElements[1] != null) {
                    marginTdElements[1].setAttribute("rowspan", Integer.toString(rows));
                }
                
                trElement = document.createElement("tr");
                trElement.setAttribute("id", id + "_tr_0");
                if ((orientation01_2 & INVERTED) != 0) {
                    addColumn(trElement, tdElements[2]);
                    addColumn(trElement, marginTdElements[1]);
                    if ((orientation0_1 & INVERTED) == 0) {
                        addColumn(trElement, tdElements[0]);
                    } else {
                        addColumn(trElement, tdElements[1]);
                    }
                } else {
                    if ((orientation0_1 & INVERTED) == 0) {
                        addColumn(trElement, tdElements[0]);
                    } else {
                        addColumn(trElement, tdElements[1]);
                    }
                    addColumn(trElement, marginTdElements[1]);
                    addColumn(trElement, tdElements[2]);
                }
                tbodyElement.appendChild(trElement);
                addRow(marginTdElements[0], id + "_trmargin_0_1");
                if ((orientation0_1 & INVERTED) == 0) {
                    addRow(tdElements[1], id + "_tr_1");
                } else {
                    addRow(tdElements[0], id + "_tr_0");
                }
            } else {
                // vertically oriented 01/2
                if ((orientation01_2 & INVERTED) != 0) {
                    // 2 before 01: render #2 and margin at beginning of TABLE.
                    addRow(tdElements[2], id + "_tr_2");
                    addRow(marginTdElements[1], id + "_trmargin_01_2");
                }
                
                // Render 01
                if ((orientation0_1 & INVERTED) == 0) {
                    // normal (top to bottom)
                    addRow(tdElements[0], id + "_tr_0");
                    addRow(marginTdElements[0], id + "_trmargin_0_1");
                    addRow(tdElements[1], id + "_tr_1");
                } else {
                    // inverted (bottom to top)
                    addRow(tdElements[1], id + "_tr_1");
                    addRow(marginTdElements[0], id + "_trmargin_1_0");
                    addRow(tdElements[0], id + "_tr_0");
                }
                
                if ((orientation01_2 & INVERTED) == 0) {
                    // 01 before 2: render margin and #2 at end of TABLE.
                    addRow(marginTdElements[1], id + "_trmargin_01_2");
                    addRow(tdElements[2], id + "_tr_2");
                }
            }
        }
    }
    
    /**
     * Adds an cell element to a table row.  The element will not be added
     * if null is provided for the value of <code>td</code>.
     * 
     * @param tr The table row to which the cell element is to be added.
     * @param td The cell element to be added (if not null).
     */
    private void addColumn(Element tr, Element td) {
        if (td != null) {
            tr.appendChild(td);
        }
    }
    
    /**
     * Adds an attribute to every table cell in the rendered table.
     *
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     */
    public void addGlobalAttribute(String name, String value) {
        for (int index = 0; index < tdElements.length; ++index) {
            tdElements[index].setAttribute(name, value);
        }
        if (marginTdElements != null) {
            for (int index = 0; index < marginTdElements.length; ++index) {
                if (marginTdElements[index] != null) {
                    marginTdElements[index].setAttribute(name, value);
                }
            }
        }
    }
    
    /**
     * Adds a row containing a single column element to a table.
     * The row will not be added if <code>td</code> is null.
     *
     * @param tdElement The row element to be added.
     * @param trElementId the id to assign to the row element.
     */
    private void addRow(Element tdElement, String trElementId) {
        if (tdElement != null) {
            Element trElement = document.createElement("tr");
            trElement.setAttribute("id", trElementId);
            trElement.appendChild(tdElement);
            tbodyElement.appendChild(trElement);
        }
    }
    
    /**
     * Returns the created table element.
     * 
     * @return the table element
     */
    Element getTableElement() {
        return tableElement;
    }

    /**
     * Returns the specified container element.
     *
     * @param index The index of the table element to return.  For two-celled tables,
     *        legitimate values are 0 and 1.  For three-celled tables, 
     *        legitimate values are 0, 1, and 2.
     * @return The specified container element.
     */
    Element getTdElement(int index) {
        return tdElements[index];
    }
    
    /**
     * Returns the specified margin element.
     *
     * @param index The index of the table element to return.  Index 0 is the margin
     *        element between container cells 0 and 1.  Index 1 is the margin
     *        element between container cells 0/1 and 2.
     * @return The specified margin element.  Returns null if the margin is zero
     *         pixels.
     */
    Element getMarginTdElement(int index) {
        return marginTdElements[index];
    }
}
