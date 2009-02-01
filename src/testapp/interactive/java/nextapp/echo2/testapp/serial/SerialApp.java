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

package nextapp.echo2.testapp.serial;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;

import javax.servlet.http.HttpSession;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Border;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.ListBox;
import nextapp.echo2.app.Window;
import nextapp.echo2.app.WindowPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.list.DefaultListModel;
import nextapp.echo2.testapp.interactive.InteractiveApp;
import nextapp.echo2.webcontainer.ContainerContext;
import nextapp.echo2.webrender.UserInstance;

public class SerialApp extends ApplicationInstance {
    
    private ListBox listBox;
    private Window mainWindow;
    
    private void doLoad() {
        try {
            String fileName = "session.data";
            FileInputStream fis = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            String sessionKey = (String) ois.readObject();
            UserInstance userInstance = (UserInstance) ois.readObject();
            getSession().setAttribute(sessionKey, userInstance);
            ois.close();
            fis.close();
            showDialog(false, sessionKey + " loaded successfully.");
        } catch (ClassNotFoundException ex) {
            showDialog(true, "Exception occurred: " + ex);
            ex.printStackTrace();
        } catch (IOException ex) {
            showDialog(true, "Exception occurred: " + ex);
            ex.printStackTrace();
        }
    }

    private void doRefresh() {
        DefaultListModel listModel = (DefaultListModel) listBox.getModel();
        listModel.removeAll();
        Enumeration enumeration = getSession().getAttributeNames();
        while (enumeration.hasMoreElements()) {
            String sessionKey = (String) enumeration.nextElement();
            listModel.add(sessionKey);
        }
    }

    private void doStore() {
        String sessionKey = (String) listBox.getSelectedValue();
        UserInstance userInstance = (UserInstance) getSession().getAttribute(sessionKey);
        if (userInstance == null) {
            showDialog(true, "No instance selected.");
            return;
        }
        try {
            String fileName = "session.data";
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(sessionKey);
            oos.writeObject(userInstance);
            oos.flush();
            oos.close();
            fos.close();
            showDialog(false, sessionKey + " serialized successfully.");
        } catch (IOException ex) {
            showDialog(true, "Exception occurred: " + ex);
            ex.printStackTrace();
        }
        
    }
    
    private HttpSession getSession() {
        ContainerContext containerContext = (ContainerContext) getContextProperty(ContainerContext.CONTEXT_PROPERTY_NAME);
        return containerContext.getSession();
    }

    /**
     * @see nextapp.echo2.app.ApplicationInstance#init()
     */
    public Window init() {
        if (InteractiveApp.LIVE_DEMO_SERVER) {
            throw new RuntimeException("Serialization test disabled on live demo server.");
        }
        
        mainWindow = new Window();
        mainWindow.setTitle("NextApp Echo2 Serialization Test Application");
        
        ContentPane content = new ContentPane();
        mainWindow.setContent(content);
        
        Column mainColumn = new Column();
        mainColumn.setBorder(new Border(new Extent(4), Color.BLUE, Border.STYLE_OUTSET));
        mainColumn.setInsets(new Insets(40));
        mainColumn.setCellSpacing(new Extent(20));
        content.add(mainColumn);
        
        Column serializeColumn = new Column();
        mainColumn.add(serializeColumn);
        
        Button button;

        serializeColumn.add(new Label("Available Applications:"));
        
        listBox = new ListBox();
        listBox.setWidth(new Extent(100, Extent.PERCENT));
        serializeColumn.add(listBox);
        
        button = new Button("[ Refresh ]");
        button.addActionListener(new ActionListener() {
            
            /**
             * @see nextapp.echo2.app.event.ActionListener#actionPerformed(nextapp.echo2.app.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e) {
                doRefresh();
            }
        });
        serializeColumn.add(button);
        
        button = new Button("[ Serialize ]");
        button.addActionListener(new ActionListener() {
            
            /**
             * @see nextapp.echo2.app.event.ActionListener#actionPerformed(nextapp.echo2.app.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e) {
                doStore();
            }
        });
        serializeColumn.add(button);
        
        button = new Button("[ Load Serialized Application ]");
        button.addActionListener(new ActionListener() {
            
            /**
             * @see nextapp.echo2.app.event.ActionListener#actionPerformed(nextapp.echo2.app.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e) {
                doLoad();
            }
        });
        mainColumn.add(button);
        
        return mainWindow;
    }

    private void showDialog(boolean error, String message) {
         WindowPane windowPane = new WindowPane();
         windowPane.setModal(true);
         windowPane.setTitle(error ? "Error" : "Status");
         windowPane.setTitleBackground(error ? Color.RED : Color.GREEN);
         windowPane.setInsets(new Insets(20));
         windowPane.add(new Label(message));
         mainWindow.getContent().add(windowPane);
    }
}
