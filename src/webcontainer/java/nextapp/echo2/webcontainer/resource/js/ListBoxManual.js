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

//_________________________
// Object EchoManualListBox

   	EchoManualListBox = function() { };
  
    EchoManualListBox.mouseOverItem = function(e) {
    	EchoDomUtil.preventEventDefault(e);
        var target = EchoDomUtil.getEventTarget(e);
        if (target.selectedState == null) {
        	EchoManualListBox.applySelectedIndices(target.parentNode.id);
        }
        
        if (!target.selectedState){
	    	var style = EchoDomPropertyStore.getPropertyValue(target.parentNode.id, "mouseoverStyle");
		    EchoManualListBox.applyStyle(target,style);
	    }
    }
    
    EchoManualListBox.mouseOutItem = function(e) {
    	EchoDomUtil.preventEventDefault(e);
        var target = EchoDomUtil.getEventTarget(e);
        if (!target.selectedState) {
        	var style = EchoDomPropertyStore.getPropertyValue(target.parentNode.id, "unselectedStyle");
		    EchoManualListBox.applyStyle(target,style);
		}
    }
    
    
    EchoManualListBox.deselectItem = function(elementId) {
		var target = document.getElementById(elementId);
		target.selectedState = false;
		var style = EchoDomPropertyStore.getPropertyValue(target.parentNode.id, "unselectedStyle");
		
        EchoManualListBox.applyStyle(target,style);
        
	}
	
	EchoManualListBox.selectItem = function(elementId) {
		var target = document.getElementById(elementId);
		var style = EchoDomPropertyStore.getPropertyValue(target.parentNode.id, "selectedStyle");
		var singleSelect = EchoDomPropertyStore.getPropertyValue(target.parentNode.id, "singleSelect");

		if (singleSelect) {
			EchoManualListBox.clearSelectedValues(target.parentNode.id);
		}
		target.selectedState = true;
		
        EchoManualListBox.applyStyle(target,style);
	}
	
	EchoManualListBox.processUpdates = function(elementId) {
		var element = document.getElementById(elementId).parentNode;
		var propertyElement  = EchoClientMessage.createPropertyElement(element.id, "selectedObjects");
		
		EchoManualListBox.createUpdates(element.id,propertyElement);

	    EchoDebugManager.updateClientMessage();
	}
	
	EchoManualListBox.createUpdates = function(elementId,propertyElement){
		var element = document.getElementById(elementId);
		
		// remove previous values
		while(propertyElement.hasChildNodes()){
			var removed = propertyElement.removeChild(propertyElement.firstChild);
		}
		
        var optionDivElements = element.getElementsByTagName("div");

		// add new values		
		for (var i=0; i<optionDivElements.length; i++){
			if (optionDivElements[i].selectedState) {
				var optionId = optionDivElements[i].id;
				var optionElement = EchoClientMessage.messageDocument.createElement("option");
				optionElement.setAttribute("id",optionId);
				// EchoDebugManager.consoleWrite("added " + optionId);
				propertyElement.appendChild(optionElement);
			}
		}
	}
	
    EchoManualListBox.processSelection = function(e) {
        EchoDomUtil.preventEventDefault(e);
        
        // BUGBUG: Move into something common
        if (document.selection && document.selection.empty) {
        	document.selection.empty();
    	}
    	
        var target = EchoDomUtil.getEventTarget(e);

		// check to see if it's the first time this control has been activated
		// by inspecting a custom attribute on the element.
        if (target.selectedState == null){
        	EchoManualListBox.applySelectedIndices(target.parentNode.id);
        }
        
        if (target.selectedState) {
            EchoManualListBox.deselectItem(target.id);
            
        } else {
			EchoManualListBox.selectItem(target.id);
        }
        EchoManualListBox.processUpdates(target.id);
    }
	
	EchoManualListBox.clearSelectedValues = function(elementId) {
		var element = document.getElementById(elementId);
        var optionDivElements = element.getElementsByTagName("div");
        for (var i = 0; i < optionDivElements.length; ++i) {
        	var style = EchoDomPropertyStore.getPropertyValue(elementId, "unselectedStyle");
			EchoManualListBox.applyStyle(optionDivElements[i],style);
			optionDivElements[i].selectedState = false;
        }
	}
	
	EchoManualListBox.applySelectedIndices = function(elementId) {
		var element = document.getElementById(elementId);
        var optionDivElements = element.getElementsByTagName("div");
        for (var i = 0; i < optionDivElements.length; ++i) {
        	var selected = EchoDomPropertyStore.getPropertyValue(optionDivElements[i].id, "selectedState");
        	if (selected) {
        		optionDivElements[i].selectedState = true;
        	} else {
        		optionDivElements[i].selectedState = false;
        	}
        }
	}
	
	// BUGBUG Copied verbatim from Button.js
	EchoManualListBox.applyStyle = function(element, cssText) {
	    if (!cssText) {
	        //BUGBUG. Temporary fix to prevent exceptions for child element (image) issue.
	        return;
	    }
	    var styleProperties = cssText.split(";");
	    var styleData = new Array();
	    for (var i = 0; i < styleProperties.length; ++i) {
	        var separatorIndex = styleProperties[i].indexOf(":");
	        if (separatorIndex == -1) {
	            continue;
	        }
	        var attributeName = styleProperties[i].substring(0, separatorIndex);
	        var propertyName = EchoDomUtil.cssAttributeNameToPropertyName(attributeName);
	        var propertyValue = styleProperties[i].substring(separatorIndex + 1);
	        element.style[propertyName] = propertyValue;
    }
};
	
