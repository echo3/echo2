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

package echo2example.email;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

/**
 * 
 */
public class MailScreen extends ContentPane {
    
    public MailScreen() {
        super();
        
        SplitPane mainSplitPane = new SplitPane(SplitPane.ORIENTATION_HORIZONTAL, new Extent(175));
        add(mainSplitPane);
        
        SplitPane titleMenuSplitPane = new SplitPane(SplitPane.ORIENTATION_VERTICAL, new Extent(70));
        mainSplitPane.add(titleMenuSplitPane);
        
        Column titleColumn = new Column();
        titleColumn.setStyleName("MailScreen.TitleColumn");
        titleMenuSplitPane.add(titleColumn);
        Label label;
        
        label = new Label(Messages.getString("Title.Main"));
        titleColumn.add(label);
        
        label = new Label(Messages.getString("Title.Sub"));
        titleColumn.add(label);
        
        titleMenuSplitPane.add(createMenu());
        
        SplitPane mailSplitPane = new SplitPane(SplitPane.ORIENTATION_VERTICAL, new Extent(320));
        mainSplitPane.add(mailSplitPane);
        
        SplitPane messageListSplitPane = new SplitPane(SplitPane.ORIENTATION_VERTICAL, new Extent(30));
        mailSplitPane.add(messageListSplitPane);
        
        messageListSplitPane.add(new PageNavigator());
    }
    
    private Component createMenu() {
        Button button;
        Label label;

        Column menuColumn = new Column();
        menuColumn.setStyleName("MailScreen.MenuColumn");
        
        Column folderSelectColumn = new Column();
        menuColumn.add(folderSelectColumn);
        
        label = new Label(Messages.getString("MailScreen.PromptFolderSelect"));
        folderSelectColumn.add(label);
        
        SelectField folderSelect = new SelectField();
        folderSelectColumn.add(folderSelect);
        
        Column optionsColumn = new Column();
        menuColumn.add(optionsColumn);
        
        button = new Button(Messages.getString("MailScreen.ButtonNewMessage"));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
        optionsColumn.add(button);
        
        button = new Button(Messages.getString("MailScreen.ButtonReplyTo"));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
        optionsColumn.add(button);
        
        button = new Button(Messages.getString("MailScreen.ButtonLogOut"));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ((EmailApp) getApplicationInstance()).disconnect();
            }
        });
        menuColumn.add(button);
        
        return menuColumn;
    }
}
