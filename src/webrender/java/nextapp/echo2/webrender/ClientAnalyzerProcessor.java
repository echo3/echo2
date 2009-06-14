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

package nextapp.echo2.webrender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import nextapp.echo2.webrender.service.SynchronizeService;
import nextapp.echo2.webrender.util.DomUtil;

import org.w3c.dom.Element;

/**
 * <code>SynchronizeService.ClientMessagePartProcessor</code> which creates
 * a <code>ClientProperties</code> object based on the client script's
 * analysis of its environment.
 */
public class ClientAnalyzerProcessor 
implements SynchronizeService.ClientMessagePartProcessor {

    /**
     * <code>Set</code> containing valid properties which may be received from
     * the client. Property settings received from the client that are not in
     * this set are discarded.
     */
    private static final Set VALID_PROPERTIES;
    static {
        Set set = new HashSet();
        set.add(ClientProperties.NAVIGATOR_APP_CODE_NAME);
        set.add(ClientProperties.NAVIGATOR_APP_NAME);
        set.add(ClientProperties.NAVIGATOR_APP_VERSION);
        set.add(ClientProperties.NAVIGATOR_COOKIE_ENABLED);
        set.add(ClientProperties.NAVIGATOR_JAVA_ENABLED);
        set.add(ClientProperties.NAVIGATOR_LANGUAGE);
        set.add(ClientProperties.NAVIGATOR_PLATFORM);
        set.add(ClientProperties.NAVIGATOR_USER_AGENT);
        set.add(ClientProperties.SCREEN_WIDTH);
        set.add(ClientProperties.SCREEN_HEIGHT);
        set.add(ClientProperties.SCREEN_COLOR_DEPTH);
        set.add(ClientProperties.UTC_OFFSET);
        set.add(ClientProperties.UNESCAPED_XHR_TEST);
        VALID_PROPERTIES = Collections.unmodifiableSet(set);
    }
    
    /**
     * Analyzes the state of <code>ClientProperties</code> and adds additional
     * inferred data, such as quirk attributes based on browser type.
     * 
     * @param clientProperties the <code>ClientProperties</code> to analyze
     *        and update
     */
    private void analyze(ClientProperties clientProperties) {
        Connection conn = WebRenderServlet.getActiveConnection();
        
        Enumeration localeEnum = conn.getRequest().getLocales();
        List localeList = new ArrayList();
        while (localeEnum.hasMoreElements()) {
            localeList.add(localeEnum.nextElement());
        }
        clientProperties.setProperty(ClientProperties.LOCALES, localeList.toArray(new Locale[localeList.size()]));
        
        clientProperties.setProperty(ClientProperties.REMOTE_HOST, conn.getRequest().getRemoteHost());
    
        String userAgent = clientProperties.getString(ClientProperties.NAVIGATOR_USER_AGENT).toLowerCase();
        
        boolean browserOpera = userAgent.indexOf("opera") != -1;
        boolean browserSafari = userAgent.indexOf("safari") != -1;
        boolean browserKonqueror = userAgent.indexOf("konqueror") != -1;
        
        // Note deceptive user agent fields:
        // - Konqueror and Safari UA fields contain "like Gecko"
        // - Opera UA field typically contains "MSIE"
        boolean deceptiveUserAgent = browserOpera || browserSafari || browserKonqueror;
        
        boolean browserMozilla = !deceptiveUserAgent && userAgent.indexOf("gecko") != -1;
        boolean browserFireFox = userAgent.indexOf("firefox") != -1;
        boolean browserInternetExplorer = !deceptiveUserAgent && userAgent.indexOf("msie") != -1;
        
        int majorVersion = -1, minorVersion = -1;
        
        // Store browser information.
        if (browserOpera) {
            clientProperties.setProperty(ClientProperties.BROWSER_OPERA, Boolean.TRUE);
            if (userAgent.indexOf("opera/9") != -1 || userAgent.indexOf("opera 9") != -1) {
                majorVersion = 9;
            } else if (userAgent.indexOf("opera/8") != -1 || userAgent.indexOf("opera 8") != -1) {
                majorVersion = 8;
            } else {
                // Assume future version with Opera 9 compatibility.
                // (Versions prior to 8 do not provide minimum requirements, i.e., XMLHTTPRequest.)
                majorVersion = 9;
            }
        } else if (browserKonqueror) {
            clientProperties.setProperty(ClientProperties.BROWSER_KONQUEROR, Boolean.TRUE);
        } else if (browserSafari) {
            clientProperties.setProperty(ClientProperties.BROWSER_SAFARI, Boolean.TRUE);
        } else if (browserMozilla) {
            clientProperties.setProperty(ClientProperties.BROWSER_MOZILLA, Boolean.TRUE);
            if (browserFireFox) {
                clientProperties.setProperty(ClientProperties.BROWSER_MOZILLA_FIREFOX, Boolean.TRUE);
                if (userAgent.indexOf("firefox/3.0") != -1) {
                    majorVersion = 3;
                    minorVersion = 0;
                }
            }
        } else if (browserInternetExplorer) {
            clientProperties.setProperty(ClientProperties.BROWSER_INTERNET_EXPLORER, Boolean.TRUE);
            if (userAgent.indexOf("msie 6.") != -1) {
                majorVersion = 6;
            } else if (userAgent.indexOf("msie 7.") != -1) {
                majorVersion = 7;
            } else if (userAgent.indexOf("msie 8.") != -1) {
                majorVersion = 8;
            }
        }
        
        if (majorVersion != -1) {
            clientProperties.setProperty(ClientProperties.BROWSER_VERSION_MAJOR, Integer.toString(majorVersion));
        }
        
        if (minorVersion != -1) {
            clientProperties.setProperty(ClientProperties.BROWSER_VERSION_MINOR, Integer.toString(minorVersion));
        }
        
        // Set quirk flags.
        if (browserInternetExplorer) {
            // IE6-7-8+ Issues 
            clientProperties.setProperty(ClientProperties.QUIRK_IE_REPAINT, Boolean.TRUE);
            clientProperties.setProperty(ClientProperties.QUIRK_TEXTAREA_CONTENT, Boolean.TRUE);
            clientProperties.setProperty(ClientProperties.QUIRK_IE_TEXTAREA_NEWLINE_OBLITERATION, Boolean.TRUE);
            clientProperties.setProperty(ClientProperties.QUIRK_IE_SELECT_LIST_DOM_UPDATE, Boolean.TRUE);
            clientProperties.setProperty(ClientProperties.QUIRK_CSS_BORDER_COLLAPSE_INSIDE, Boolean.TRUE);
            clientProperties.setProperty(ClientProperties.QUIRK_CSS_BORDER_COLLAPSE_FOR_0_PADDING, Boolean.TRUE);
            clientProperties.setProperty(ClientProperties.QUIRK_DELAYED_FOCUS_REQUIRED, Boolean.TRUE);
            clientProperties.setProperty(ClientProperties.PROPRIETARY_EVENT_MOUSE_ENTER_LEAVE_SUPPORTED, Boolean.TRUE);
            clientProperties.setProperty(ClientProperties.QUIRK_IE_TABLE_PERCENT_WIDTH_SCROLLBAR_ERROR, Boolean.TRUE);
            clientProperties.setProperty(ClientProperties.QUIRK_IE_SELECT_PERCENT_WIDTH, Boolean.TRUE);
            if (majorVersion < 8) {
                // IE6-7 Issues 
                clientProperties.setProperty(ClientProperties.PROPRIETARY_IE_CSS_EXPRESSIONS_SUPPORTED, Boolean.TRUE);
                clientProperties.setProperty(ClientProperties.NOT_SUPPORTED_CSS_OPACITY, Boolean.TRUE);
                clientProperties.setProperty(ClientProperties.PROPRIETARY_IE_OPACITY_FILTER_REQUIRED, Boolean.TRUE);
                
                if (majorVersion < 7) {
                    // IE6 Issues
                    clientProperties.setProperty(ClientProperties.PROPRIETARY_IE_PNG_ALPHA_FILTER_REQUIRED, Boolean.TRUE);
                    clientProperties.setProperty(ClientProperties.QUIRK_CSS_POSITIONING_ONE_SIDE_ONLY, Boolean.TRUE);
                    clientProperties.setProperty(ClientProperties.QUIRK_CSS_BACKGROUND_ATTACHMENT_USE_FIXED,  Boolean.TRUE);
                    clientProperties.setProperty(ClientProperties.QUIRK_IE_SELECT_Z_INDEX, Boolean.TRUE);
                }
            }
        }
        if (browserOpera) {
            clientProperties.setProperty(ClientProperties.QUIRK_TEXTAREA_CONTENT, Boolean.TRUE);
            clientProperties.setProperty(ClientProperties.QUIRK_OPERA_NO_CSS_TEXT, Boolean.TRUE);
            clientProperties.setProperty(ClientProperties.QUIRK_SELECT_REQUIRES_NULL_OPTION, Boolean.TRUE);            
            clientProperties.setProperty(ClientProperties.QUIRK_IE_SELECT_PERCENT_WIDTH, Boolean.TRUE);
            if (majorVersion < 9) {
                clientProperties.setProperty(ClientProperties.NOT_SUPPORTED_CSS_OPACITY, Boolean.TRUE);
            }
        }
        if (browserMozilla) {
            clientProperties.setProperty(ClientProperties.QUIRK_SELECT_REQUIRES_NULL_OPTION, Boolean.TRUE);
            clientProperties.setProperty(ClientProperties.QUIRK_MOZILLA_TEXT_INPUT_REPAINT, Boolean.TRUE);
            clientProperties.setProperty(ClientProperties.QUIRK_MOZILLA_PERFORMANCE_LARGE_DOM_REMOVE, Boolean.TRUE);
        }
        
        if (browserKonqueror) {
            clientProperties.setProperty(ClientProperties.QUIRK_SELECT_REQUIRES_NULL_OPTION, Boolean.TRUE);            
            clientProperties.setProperty(ClientProperties.QUIRK_IE_SELECT_PERCENT_WIDTH, Boolean.TRUE);
            clientProperties.setProperty(ClientProperties.NOT_SUPPORTED_CSS_MANIPULATION, Boolean.TRUE);
        }
        
        if (browserSafari) {
            clientProperties.setProperty(ClientProperties.QUIRK_SELECT_REQUIRES_NULL_OPTION, Boolean.TRUE);
            clientProperties.setProperty(ClientProperties.NOT_SUPPORTED_CSS_MANIPULATION, Boolean.TRUE);
            String unescapeTest = clientProperties.getString(ClientProperties.UNESCAPED_XHR_TEST);
            if (unescapeTest.equals("&")) {
                clientProperties.setProperty(ClientProperties.QUIRK_SAFARI_UNESCAPED_XHR, Boolean.TRUE);
            }
        }
    }
    
    /**
     * @see nextapp.echo2.webrender.service.SynchronizeService.ClientMessagePartProcessor#getName()
     */
    public String getName() {
        return "EchoClientAnalyzer";
    }
    
    /**
     * @see nextapp.echo2.webrender.service.SynchronizeService.ClientMessagePartProcessor#process(
     *      nextapp.echo2.webrender.UserInstance, org.w3c.dom.Element)
     */
    public void process(UserInstance userInstance, Element messagePartElement) {
        ClientProperties clientProperties = new ClientProperties();
        Element[] propertyElements = DomUtil.getChildElementsByTagName(messagePartElement, "property");
        for (int i = 0; i < propertyElements.length; ++i) {
            String propertyName = propertyElements[i].getAttribute("name");
            if (VALID_PROPERTIES.contains(propertyName)) {
                String type = propertyElements[i].getAttribute("type");
                if ("text".equals(type)) {
                    clientProperties.setProperty(propertyName, propertyElements[i].getAttribute("value"));
                } else if ("integer".equals(type)) {
                    try {
                        clientProperties.setProperty(propertyName, 
                                new Integer(propertyElements[i].getAttribute("value")));
                    } catch (NumberFormatException ex) { }
                } else if ("boolean".equals(type)) {
                    clientProperties.setProperty(propertyName, 
                            new Boolean("true".equals(propertyElements[i].getAttribute("value"))));
                }
            }
        }
        userInstance.setClientProperties(clientProperties);
        analyze(clientProperties);
    }
}    
