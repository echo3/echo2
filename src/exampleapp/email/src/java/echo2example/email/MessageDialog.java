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
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.WindowPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

/**
 * A generic modal dialog that displays a message.
 */
public class MessageDialog extends WindowPane {

    public static final int TYPE_ERROR = 1;    
    
    public static final int CONTROLS_OK = 1;
    public static final int CONTROLS_OK_CANCEL = 2;
    
    public static final String COMMAND_OK = "ok";
    public static final String COMMAND_CANCEL = "cancel";
    
    private ActionListener actionProcessor = new ActionListener() {

        /**
         * @see nextapp.echo2.app.event.ActionListener#actionPerformed(nextapp.echo2.app.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            getParent().remove(MessageDialog.this);
        }
    };
    
    public MessageDialog(String title, String message, int type, int controlConfiguration) {
        super(title, new Extent(320), new Extent(240));
        setStyleName("Default");
        setModal(true);

        SplitPane splitPane = new SplitPane(SplitPane.ORIENTATION_VERTICAL, new Extent(-32));
        add(splitPane);
        
        Label contentLabel = new Label(message);
        contentLabel.setStyleName("MessageDialog.ContentLabel");
        splitPane.add(contentLabel);
        
        Row controlsRow = new Row();
        controlsRow.setStyleName("MessageDialog.ControlsRow");
        splitPane.add(controlsRow);

        Button button;
        switch (controlConfiguration) {
        case CONTROLS_OK:
            button = new Button(Messages.getString("Generic.Ok"));
            button.addActionListener(actionProcessor);
            controlsRow.add(button);
            break;
        case CONTROLS_OK_CANCEL:
            button = new Button(Messages.getString("Generic.Ok"));
            button.addActionListener(actionProcessor);
            controlsRow.add(button);
            button = new Button(Messages.getString("Generic.Cancel"));
            button.addActionListener(actionProcessor);
            controlsRow.add(button);
            break;
        }
        
        setModal(true);
    }
}
