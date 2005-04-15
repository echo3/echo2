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

package nextapp.echo2.webrender.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * A representation of a connection to the server by the client, encapsulating
 * the servlet request and response objects, and providing access to the
 * relevant application instance.
 */
public class Connection {
    
    private static final String USER_INSTANCE_SESSION_KEY = "Echo2UserInstance";
    
    private HttpServletRequest request;
    private HttpServletResponse response;
    private WebRenderServlet servlet;
    private UserInstance userInstance;
    
    /**
     * Creates a <code>connection</code> object that will handle the given 
     * request and response.  The InstancePeer will be pulled from the session 
     * if one exists.  A session will NOT be created if one does not exist.
     *
     * @param servlet The <code>WebRenderServlet</code> generating the connection.
     * @param request The HTTP request object that was passed to the servlet.
     * @param response The HTTP response object that was passed to the servlet.
     */
    Connection(WebRenderServlet servlet, HttpServletRequest request, HttpServletResponse response) 
    throws IOException, ServletException {
        super();
        
        this.servlet = servlet;
        this.request = request;
        this.response = response;

        // Configure connection for Multipart Request if required.
        String contentType = request.getContentType();
        if (contentType != null && contentType.startsWith(ContentType.MULTIPART_FORM_DATA.getMimeType())) {
            if (WebRenderServlet.getMultipartRequestWrapper() == null) {
                throw new WebRenderServletException(
                        "MultipartRequestWrapper was never set and client made an HTTP request "
                        + "encoded as multipart/form-data.");
            }
            this.request = WebRenderServlet.getMultipartRequestWrapper().getWrappedRequest(request);
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            userInstance = (UserInstance) session.getAttribute(USER_INSTANCE_SESSION_KEY);
        }
    }
    
    /**
     * Returns the <code>OutputStream</code> object that may be used to 
     * generate a response.  This method may be called once.  If it is called, 
     * the getPrintWriter() method may not be called.  This method wraps a 
     * call to <code>HttpServletResponse.getOutputStream()</code>.  The 
     * <code>OutputStream</code> will be closed by the servlet container.
     *
     * @return the <code>OutputStream</code> object that may be used to 
     *         generate a response to the client.
     */
    public OutputStream getOutputStream() {
        try {
            return response.getOutputStream();
        } catch (IOException ex) {
            throw new WebRenderServletException("Unable to get PrintWriter.", ex);
        }
    }
    
    /**
     * Returns the <code>HttpServletRequest</code> wrapped by this 
     * <code>Connection</code>.
     *
     * @return The <code>HttpServletRequest</code> wrapped by this 
     *         <code>Connection</code>.
     */
    public HttpServletRequest getRequest() {
        return request;
    }
    
    /**
     * Returns the <code>HttpServletResponse</code> wrapped by this 
     * <code>Connection</code>.
     *
     * @return The <code>HttpServletResponse</code> wrapped by this 
     *         <code>Connection</code>.
     */
    public HttpServletResponse getResponse() {
        return response;
    }
    
    /**
     * Returns the <code>WebRenderServlet</code> wrapped by this 
     * <code>Connection</code>.
     *
     * @return The <code>WebRenderServlet</code> wrapped by this 
     *         <code>Connection</code>.
     */
    public WebRenderServlet getServlet() {
        return servlet;
    }

    /**
     * Returns the <code>UserInstance</code> associated with 
     * this connection.  If the session has not been initialized, null is 
     * returned.
     *
     * @return The <code>UserInstance</code> associated with  
     *         this connection.
     */
    public UserInstance getUserInstance() {
        return userInstance;
    }
    
    /**
     * Returns the <code>PrintWriter</code> object that may be used to 
     * generate a response.  This method may be called once.  If it is called, 
     * the getOuputStream() method may not be called.  This method wraps a 
     * call to <code>HttpServletResponse.getWriter()</code>.  The 
     * <code>PrintWriter</code> will be closed by the servlet container.
     *
     * @return the <code>PrintWriter</code> object that may be used to 
     *         generate a response to the client.
     */
    public PrintWriter getWriter() {
        try {
            return response.getWriter();
        } catch (IOException ex) {
            throw new WebRenderServletException("Unable to get PrintWriter.", ex);
        }
    }
    
    public void removeUserInstance() {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(USER_INSTANCE_SESSION_KEY);
        }
    }
    
    /**
     * Sets the content type of the response.
     * This method will automatically append a character encoding to
     * non-binary content types.
     * 
     * @param contentType The content type of the response.
     */
    public void setContentType(ContentType contentType) {
        UserInstance userInstance = getUserInstance();
        if (contentType.isBinary() || userInstance == null) {
            response.setContentType(contentType.getMimeType());
        } else {
            response.setContentType(contentType.getMimeType() + "; charset=" + userInstance.getCharacterEncoding());
        }
    }
    
    //BUGBUG. doc.
    public void setUserInstance(UserInstance userInstance) {
        this.userInstance = userInstance;
        userInstance.setApplicationUri(request.getRequestURI());
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_INSTANCE_SESSION_KEY, userInstance);
    }
}
