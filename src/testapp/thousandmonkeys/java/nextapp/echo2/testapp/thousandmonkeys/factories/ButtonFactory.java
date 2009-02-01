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

package nextapp.echo2.testapp.thousandmonkeys.factories;

import nextapp.echo2.app.Border;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Insets;
import nextapp.echo2.testapp.thousandmonkeys.ComponentFactory;

/**
 * <code>ComponentFactory</code> for <code>Button</code>s
 */
public class ButtonFactory 
implements ComponentFactory{

    private static final Insets INSETS_10_5 = new Insets(10, 5);
    
    private int count = 0;
    
    /**
     * @see nextapp.echo2.testapp.thousandmonkeys.ComponentFactory#newInstance()
     */
    public Component newInstance() {
        Button button = new Button();
        switch ((int) (Math.random() * 3)) {
        case 0:
            button.setText("Button #" + ++count);
            button.setBackground(new Color((int) (16777216 * Math.random())));
            break;
        case 1:
            button.setText("This is an example button."); 
            button.setForeground(new Color((int) (16777216 * Math.random()) & 0x7f7f7f));
            button.setBackground(new Color((int) (16777216 * Math.random()) | 0xb0b0b0));
            button.setBorder(new Border(1, new Color((int) (16777216 * Math.random()) | 0xb3b3b3), Border.STYLE_OUTSET));
            button.setInsets(INSETS_10_5);
            break;
        case 2:
            button.setText("Initially Plain Button");
            break;
        }
        return button;
    }
}
