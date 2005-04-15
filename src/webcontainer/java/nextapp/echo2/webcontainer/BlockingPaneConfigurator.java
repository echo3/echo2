package nextapp.echo2.webcontainer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import nextapp.echo2.webrender.clientupdate.BlockingPane;
import nextapp.echo2.webrender.clientupdate.ServerMessage;
import nextapp.echo2.webrender.output.CssStyle;

/**
 * 
 */
public class BlockingPaneConfigurator {
    
    public static void configureDefault(RenderContext rc) {
        ServerMessage serverMessage = rc.getServerMessage();
        Document document = serverMessage.getDocument();
        
        Element setDelayMessageElement = BlockingPane.createSetDelayMessage(serverMessage);
        
        Element tableElement = document.createElement("table");
        CssStyle tableCssStyle = new CssStyle();
        tableCssStyle.setAttribute("width", "100%");
        tableCssStyle.setAttribute("height", "100%");
        tableCssStyle.setAttribute("border", "0px");
        tableCssStyle.setAttribute("padding", "0px");
        tableElement.setAttribute("style", tableCssStyle.renderInline());
        setDelayMessageElement.appendChild(tableElement);
        
        Element tbodyElement = document.createElement("tbody");
        tableElement.appendChild(tbodyElement);
        
        Element trElement = document.createElement("tr");
        tbodyElement.appendChild(trElement);
        
        Element tdElement = document.createElement("td");
        trElement.appendChild(tdElement);
        
        Element divElement = document.createElement("div");
        CssStyle divCssStyle = new CssStyle();
        divCssStyle.setAttribute("margin-top", "40px");
        divCssStyle.setAttribute("margin-left", "auto");
        divCssStyle.setAttribute("margin-right", "auto");
        divCssStyle.setAttribute("background-color", "#afafbf");
        divCssStyle.setAttribute("color", "#000000");
        divCssStyle.setAttribute("padding", "40px");
        divCssStyle.setAttribute("width", "200px");
        divCssStyle.setAttribute("border", "groove 2px #bfbfcf");
        divCssStyle.setAttribute("font-family", "verdana, arial, helvetica, sans-serif");
        divCssStyle.setAttribute("font-size", "10pt");
        divCssStyle.setAttribute("text-align", "center");
        divElement.setAttribute("style", divCssStyle.renderInline());
        divElement.setAttribute("id", BlockingPane.ELEMENT_ID_DELAY_MESSAGE);
        divElement.appendChild(document.createTextNode("Please wait..."));
        tdElement.appendChild(divElement);
    }
}