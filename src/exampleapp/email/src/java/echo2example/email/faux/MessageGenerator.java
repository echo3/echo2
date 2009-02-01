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

package echo2example.email.faux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * Generates random fake content for the fake JavaMail <code>Message</code> 
 * objects.
 */
public class MessageGenerator {
    
    private static final String DOMAIN = "nextapp.com";
    private static final Address YOUR_ADDRESS;
    static {
        try {
            YOUR_ADDRESS = new InternetAddress("Joe Smith <joe.smith@nextapp.com>");
        } catch (AddressException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private static final int MINIMUM_DATE_DELTA = -365;
    private static final int MAXIMUM_DATE_DELTA = 0;

    /**
     * Generates a text document from random artificial text.
     * 
     * @param sentenceArray the source array from which strings are randomly 
     *        retrieved
     * @param minimumSentences the minimum number of sentences each paragraph 
     *        may contain
     * @param maximumSentences the maximum number of sentences each paragraph 
     *        may contain
     * @param minimumParagraphs the minimum number of paragraphs the document
     *        may contain
     * @param maximumParagraphs the maximum number of paragraphs the document
     *        may contain
     * @return the document
     */
    private static String generateText(String[] sentenceArray, int minimumSentences, int maximumSentences,
            int minimumParagraphs, int maximumParagraphs) {
    
        int paragraphCount = randomInteger(minimumParagraphs, maximumParagraphs);
        StringBuffer text = new StringBuffer();
        for (int paragraphIndex = 0; paragraphIndex < paragraphCount; ++paragraphIndex) {
            int sentenceCount = randomInteger(minimumSentences, maximumSentences);
            for (int sentenceIndex = 0; sentenceIndex < sentenceCount; ++sentenceIndex) {
                text.append(sentenceArray[randomInteger(sentenceArray.length)]);
                text.append(".");
                if (sentenceIndex < sentenceCount - 1) {
                    text.append("  ");
                }
            }
            if (paragraphIndex < paragraphCount - 1) {
                text.append("\n\n");
            }
        }
        return text.toString();
    }

    /**
     * Returns a text resource as an array of strings.
     * 
     * @param resourceName the name of resource available from the 
     *        <code>CLASSPATH</code>
     * @return the resource as an array of strings
     */
    private static String[] loadStrings(String resourceName) {
        InputStream in = null;
        List strings = new ArrayList();
        try {
            in = MessageGenerator.class.getResourceAsStream(resourceName);
            if (in == null) {
                throw new IllegalArgumentException("Resource does not exist: \"" + resourceName + "\"");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    strings.add(Character.toUpperCase(line.charAt(0)) + line.substring(1).toLowerCase());
                }
            }
            return (String[]) strings.toArray(new String[strings.size()]);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Cannot get resource: \"" + resourceName + "\": " + ex);
        } finally {
            if (in != null) { try { in.close(); } catch (IOException ex) { } } 
        }
    }

    /**
     * Returns a random boolean value.
     * 
     * @param truePercent The percentage of 'true' values to be returned.
     * @return The random boolean value.
     */
    private static boolean randomBoolean(int truePercent) {
        int number = (int) (Math.random() * 100);
        return number < truePercent;
    }

    /**
     * Returns a random integer between the specified bounds.
     * 
     * @param minimum the minimum possible value
     * @param maximum the maximum possible value
     * @return the random integer
     */
    private static int randomInteger(int minimum, int maximum) {
        return minimum + (int) (Math.random() * (maximum - minimum + 1));
    }

    /**
     * Returns a random integer between 0 and <code>size</code> - 1.
     * 
     * @param size the number of possible random values
     * @return a random integer between 0 and <code>size</code> - 1
     */
    private static int randomInteger(int size) {
        return (int) (Math.random() * size);
    }

    /**
     * Returns a version of a sentence with every word capitalized.
     * 
     * @param title The title sentence to capitalize.
     * @return A version of the sentence with every word capitalized.
     */
    private static String titleCapitalize(String title) {
        StringTokenizer st = new StringTokenizer(title);
        StringBuffer sb = new StringBuffer();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            sb.append(Character.toUpperCase(token.charAt(0)));
            sb.append(token.substring(1));
            if (st.hasMoreTokens()) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private String[] lastNames = loadStrings("resource/LastNames.txt");
    private String[] maleFirstNames = loadStrings("resource/MaleFirstNames.txt");
    private String[] femaleFirstNames = loadStrings("resource/FemaleFirstNames.txt");
    private String[] latinSentences = loadStrings("resource/LatinSentences.txt");
    
    private Address createAddress() 
    throws MessagingException {
        // Create Name.
        String firstName;
        
        boolean male = randomBoolean(50);
        if (male) {
            int firstNameIndex = randomInteger(maleFirstNames.length);
            firstName = maleFirstNames[firstNameIndex];
        } else {
            int firstNameIndex = randomInteger(femaleFirstNames.length);
            firstName  = femaleFirstNames[firstNameIndex];
        }
        String lastName = lastNames[randomInteger(lastNames.length)];
        String email = firstName + "." + lastName + "@" + DOMAIN;
        InternetAddress address = new InternetAddress(firstName + " " + lastName + " <" + email + ">");
        return address;
    }
    
    public Date randomDate() {
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.DAY_OF_MONTH, randomInteger(MINIMUM_DATE_DELTA, MAXIMUM_DATE_DELTA));
        cal.add(Calendar.SECOND, -randomInteger(86400));
        return cal.getTime();
    }
    
    public FauxMessage generateMessage() 
    throws MessagingException {
        
        Date receivedDate = randomDate();
        Address from = createAddress();

        Address[] to = null;
        Address[] cc = null;
        Address[] bcc = null;
        switch (randomInteger(0, 3)) {
        case 0:
            to = new Address[]{YOUR_ADDRESS};
            break;
        case 1:
            to = new Address[]{createAddress(), createAddress()};
            cc = new Address[]{createAddress(), createAddress(), createAddress()};
            bcc = new Address[]{YOUR_ADDRESS};
            break;
        case 2:
            to = new Address[]{YOUR_ADDRESS};
            cc = new Address[]{createAddress(), createAddress()};
            break;
        case 3:
            to = new Address[]{createAddress()};
            cc = new Address[]{createAddress(), YOUR_ADDRESS, createAddress()};
            break;
        }
        // Create Article Title
        String subject = latinSentences[randomInteger(latinSentences.length)];
        if (subject.length() > 40) {
            subject = subject.substring(0, subject.lastIndexOf(" ", 40));
        }
        if (subject.endsWith(",")) {
            subject = subject.substring(0, subject.length() - 1);
        }
        subject = titleCapitalize(subject);

        String content = generateText(latinSentences, 2, 8, 6, 26);

        return new FauxMessage(from, receivedDate, to, cc, bcc, subject, content);
        
    }
}
