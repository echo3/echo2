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

package echo2tutorial.numberguess;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.ResourceImageReference;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.Window;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

/**
 * Guess-a-number Tutorial Application.
 */
public class NumberGuessApp extends ApplicationInstance {

    private Window mainWindow;
    
    /**
     * @see nextapp.echo2.app.ApplicationInstance#init()
     */
    public Window init() {
        mainWindow = new Window();
        mainWindow.setTitle("Echo2 Guess-A-Number");
        startNewGame();
        return mainWindow;
    }

    /**
     * Starts a new game.
     */
    void startNewGame() {
        // Set the content to be a new GamePane, so the 
        mainWindow.setContent(new GamePane());
    }
    
    /**
     * Displays a congratulatory message to the user when s/he has guessed
     * the correct number.
     * 
     * @param numberOfTries the number of tries it took the user to guess the
     *        correct answer.
     */
    void congratulate(int numberOfTries) {
        mainWindow.setContent(new CongratulationsPane(numberOfTries));
    }
}

/**
 * A <code>ContentPane</code> which generates a random number and provides the
 * user opportunities to guess it.
 */
class GamePane extends ContentPane 
implements ActionListener {

    private int randomNumber 
            = ((int) Math.floor(Math.random() * 100)) + 1;
    private int lowerBound = 1;
    private int upperBound = 100;
    private int numberOfTries = 0;
    private TextField guessEntryField;
    private Label statusLabel = new Label();
    private Label countLabel = new Label("You have made no guesses.");
    private Label promptLabel= new Label("Guess a number between 1 and 100: ");
    private int guess;
    
    /**
     * Creates a new <code>GamePane</code>.
     */
    GamePane() {
        super();
        
        Row layoutRow = new Row();
        layoutRow.setCellSpacing(new Extent(10));
        add(layoutRow);
        
        layoutRow.add(new Label(new ResourceImageReference("/echo2tutorial/numberguess/TitleBanner.png")));
        layoutRow.add(statusLabel);
        layoutRow.add(countLabel);
        layoutRow.add(promptLabel);
        
        guessEntryField = new TextField();
        
        guessEntryField.setForeground(Color.WHITE);
        guessEntryField.setBackground(Color.BLUE);
        layoutRow.add(guessEntryField);
        
        Button submitButton = new Button("Submit Your Guess");
        submitButton.setActionCommand("submit guess");
        submitButton.setForeground(Color.BLACK);
        submitButton.setBackground(Color.GREEN);
        submitButton.setWidth(new Extent(200));
        submitButton.addActionListener(this);
        layoutRow.add(submitButton);
        
        Button newGameButton  = new Button("Start a New Game");
        newGameButton.setActionCommand("new game");
        newGameButton.setForeground(Color.WHITE);
        newGameButton.setBackground(Color.RED);
        newGameButton.setWidth(new Extent(200));
        newGameButton.addActionListener(this);
        layoutRow.add(newGameButton);
    }
    
    /**
     * @see nextapp.echo2.app.event.ActionListener#actionPerformed(nextapp.echo2.app.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("new game")) {
            ((NumberGuessApp) ApplicationInstance.getActive()).startNewGame();
        } else if (e.getActionCommand().equals("submit guess")) {
            ++numberOfTries;
            
            if (numberOfTries == 1) {
                countLabel.setText("You have made 1 guess.");
            } else {
                countLabel.setText("You have made " + numberOfTries + " guesses.");
            }

            try {
                guess = Integer.parseInt(guessEntryField.getDocument().getText());
            } catch (NumberFormatException ex) {
                statusLabel.setText("Your guess was not valid.");
                return;
            }

            if (guess == randomNumber) {
                ((NumberGuessApp) ApplicationInstance.getActive()).congratulate(numberOfTries);
            } else if (guess < 1 || guess > 100) {
                statusLabel.setText("Your guess, " + guess  + " was not between 1 and 100.");
            } else if (guess < randomNumber) {
                if (guess >= lowerBound) {
                    lowerBound = guess + 1;
                }
                statusLabel.setText("Your guess, " + guess + " was too low.  Try again:");
            } else if (guess > randomNumber) {
                statusLabel.setText("Your guess, " + guess + " was too high.  Try again:");
                if (guess <= upperBound) {
                    upperBound = guess - 1;
                }
            }
            
            promptLabel.setText("Guess a number between " + lowerBound + " and " + upperBound + ": ");
        }
    }
}

/**
 * A <code>ContentPane</code> which presents a congratulatory message to the
 * player when the correct number has been guessed.
 */
class CongratulationsPane extends ContentPane
implements ActionListener {

    /**
     * Creates a new <code>CongratulationsPane</code>.
     * 
     * @param numberOfTries the number of tries it took the user to guess the
     *        correct answer.
     */
    CongratulationsPane(int numberOfTries) {
        Row layoutRow = new Row();
        layoutRow.setCellSpacing(new Extent(30));
        add(layoutRow);
        
        layoutRow.add(new Label(new ResourceImageReference("/echo2tutorial/numberguess/CongratulationsBanner.png")));
        layoutRow.add(new Label("You got the correct answer in " + numberOfTries + (numberOfTries == 1 ? " try." : " tries.")));

        Button button = new Button("Play Again");
        button.setForeground(Color.WHITE);
        button.setBackground(Color.RED);
        button.setWidth(new Extent(200));
        button.addActionListener(this);
        layoutRow.add(button);
    }

    /**
     * @see nextapp.echo2.app.event.ActionListener#actionPerformed(nextapp.echo2.app.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        ((NumberGuessApp) ApplicationInstance.getActive()).startNewGame();
    }
}
