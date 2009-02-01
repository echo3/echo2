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

package nextapp.echo2.webcontainer.syncpeer;

import nextapp.echo2.app.Extent;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
import nextapp.echo2.webrender.Service;
import nextapp.echo2.webrender.WebRenderServlet;
import nextapp.echo2.webrender.service.StaticBinaryService;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Renders an HTML table that has two or three &quot;container&quot; cells and
 * independently configurable margins between them.  These tables are useful for
 * rendering buttons that have two or three elements (images, text labels, and 
 * state indicators).  This class supports all possible permutations for
 * placement of each of the two or three contained components.
 * <p>
 * This class should not be extended or used by classes outside of the
 * Echo framework.
 */
class TriCellTable {

    private static final Service TRANSPARENT_SPACER_IMAGE_SERVICE = StaticBinaryService.forResource(
            "Echo.TriCellTable.TransparentImage", "image/gif", "/nextapp/echo2/webcontainer/resource/image/Transparent.gif");
    
    static {
        WebRenderServlet.getServiceRegistry().add(TRANSPARENT_SPACER_IMAGE_SERVICE);
    }
    
    static final int INVERTED = 1;
    static final int VERTICAL = 2;
    
    public static final int LEADING_TRAILING = 0;
    public static final int TRAILING_LEADING = INVERTED;
    public static final int TOP_BOTTOM = VERTICAL;
    public static final int BOTTOM_TOP = INVERTED | VERTICAL;

    private Element[] tdElements;
    private Element[] marginTdElements = null;
    private Element tableElement;
    private Element tbodyElement;
    private Document document;
    private RenderContext rc;
    
    /**
     * This constructor is called by the non-private constructors to set up 
     * common properties.
     * 
     * @param rc the relevant <code>RenderContext</code>
     * @param id The id the id upon which the inner elements will be based.
     *        the id of the returned table element is not set and must be
     *        set appropriately by the caller.
     */
    private TriCellTable(RenderContext rc, Document document, String id) {
        super();
        this.rc = rc;
        this.document = document;
        tableElement = document.createElement("table");
        tbodyElement = document.createElement("tbody");
        tbodyElement.setAttribute("id", id + "_tbody");
        tableElement.appendChild(tbodyElement);
    }
    
    /**
     * Creates a two-celled <code>TriCellTable</code>.
     * 
     * @param rc the relevant <code>RenderContext</code>
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
    TriCellTable(RenderContext rc, Document document, String id, int orientation0_1, Extent margin0_1) {
        this(rc, document, id);
        
        marginTdElements = new Element[1];
        tdElements = new Element[2];
        tdElements[0] = document.createElement("td");
        tdElements[0].setAttribute("id", id + "_td_0");
        tdElements[1] = document.createElement("td");
        tdElements[1].setAttribute("id", id + "_td_1");
        
        if (margin0_1 != null && margin0_1.getValue() > 0) {
            marginTdElements[0] = document.createElement("td");
            marginTdElements[0].setAttribute("id", id + "_tdmargin_0_1");
            int size = ExtentRender.toPixels(margin0_1, 1);
            if ((orientation0_1 & VERTICAL) == 0) {
                marginTdElements[0].setAttribute("style", "width:" +  size + "px;");
                addSpacer(marginTdElements[0], size, false);
            } else {
                marginTdElements[0].setAttribute("style", "height:" + size + "px;");
                addSpacer(marginTdElements[0], size, true);
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
     * @param rc the relevant <code>RenderContext</code>
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
    TriCellTable(RenderContext rc, Document document, String id, int orientation0_1, Extent margin0_1, int orientation01_2,
            Extent margin01_2) {
        this(rc, document, id);
        
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
                
                int size = ExtentRender.toPixels(margin0_1, 1);
                if ((orientation0_1 & VERTICAL) == 0) {
                    marginTdElements[0].setAttribute("style", "width:" +  size + "px;");
                    addSpacer(marginTdElements[0], size, false);
                } else {
                    marginTdElements[0].setAttribute("style", "height:" + size + "px;");
                    addSpacer(marginTdElements[0], size, true);
                }
            }
            if (margin01_2 != null && margin01_2.getValue() > 0) {
                marginTdElements[1] = document.createElement("td");
                marginTdElements[1].setAttribute("id", id + "_tdmargin_01_2");

                int size = ExtentRender.toPixels(margin01_2, 1);
                if ((orientation01_2 & VERTICAL) == 0) {
                    marginTdElements[1].setAttribute("style", "width:" +  size + "px;");
                    addSpacer(marginTdElements[1], size, false);
                } else {
                    marginTdElements[1].setAttribute("style", "height:" + size + "px;");
                    addSpacer(marginTdElements[1], size, true);
                }
            }
        }
        
        if ((orientation0_1 & VERTICAL) == 0) {
            // horizontally oriented 0/1
            if ((orientation01_2 & VERTICAL) == 0) {
                // horizontally oriented 01/2
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
                // horizontally oriented 01/2

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
     * Appends CSS text to the 'style' attribute of each table cell 'td' 
     * element.
     * 
     * @param cssText the CSS text to add 
     */
    void addCellCssText(String cssText) {
        for (int i = 0; i < tdElements.length; ++i) {
            if (tdElements[i].hasAttribute("style")) {
                tdElements[i].setAttribute("style", tdElements[i].getAttribute("style") + cssText);
            } else {
                tdElements[i].setAttribute("style", cssText);
            }
        }
        if (marginTdElements != null) {
            for (int i = 0; i < marginTdElements.length; ++i) {
                if (marginTdElements[i] != null) {
                    if (marginTdElements[i].hasAttribute("style")) {
                        marginTdElements[i].setAttribute("style", marginTdElements[i].getAttribute("style") + cssText);
                    } else {
                        marginTdElements[i].setAttribute("style", cssText);
                    }
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
     * Adds a spacer element containing a transparent GIF image.
     * These are unfortunately necessary to prevent spacer cells
     * from collapsing in circumstances where horizontal real-estate
     * is not available.
     * 
     * @param parentElement the <code>Element</code> to which the spacer image
     *        is to be appended
     * @param size the size of the spacer cell, in pixels
     * @param vertical a flag indicating the direction; specify 
     *        <code>true</code> for a vertical spacer or <code>false</code> for
     *        a horizontal spacer.
     */
    private void addSpacer(Element parentElement, int size, boolean vertical) {
        Element imgElement = document.createElement("img");
        imgElement.setAttribute("src", rc.getContainerInstance().getServiceUri(TRANSPARENT_SPACER_IMAGE_SERVICE));
        imgElement.setAttribute("alt", "");
        imgElement.setAttribute("width", vertical ? "1" : Integer.toString(size));
        imgElement.setAttribute("height", vertical ? Integer.toString(size) : "1");
        parentElement.appendChild(imgElement);
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
