/**
 * Orwell -- A security library for the pathologically paranoid
 *
 * Copyright (C) 2013, Jonathan Gillett
 * All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.orwell.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.CollationKey;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.orwell.util.FastQuickSort;

public class FastQuickSortTest
{
    /* Number of elements */
    private static final int NUMBER_ELEMENTS = 100000;
    
    /* Collator for sorting words */
    private static Collator strictCollator;
    
    /* Data used for the tests */
    private static Integer[] unsortedNumbers;
    private static String[] unsortedStrings;
    private static CollationKey[] unsortedStringsKey;
    
    /**
     * Function which quickly reads the lines from the file into an 
     * array of strings
     * 
     * @param filename The path and name of the file to open
     * @return A string array containing each line of the file
     */
    public static String[] readLines(String filename)
            throws IOException
    {
        /*
         * Quickly get the number of lines in the file for creating the string array
         */
        LineNumberReader  lnr = new LineNumberReader(new FileReader(filename));
        lnr.skip(Long.MAX_VALUE);
        String[] fileLines = new String[lnr.getLineNumber()];
        String line = null;
        lnr.close();        
        
        /*
         * Open the file for reading as well as a buffered reader
         */
        FileReader fileReader = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        
        for(int i = 0;; ++i)
        {
            // Reached the end of the file
            if ((line = bufferedReader.readLine()) == null)
            {
                break;
            }

            fileLines[i] = line;
        }
        
        bufferedReader.close();
        return fileLines;
    }
    
    @Before
    public void setUp() throws IOException
    {
        /*
         * Simulate a sequence of random numbers to sort with FastQuickSort 
         */
        unsortedNumbers = new Integer[NUMBER_ELEMENTS];
        Random random = new Random();
        
        for (int i = 0; i < NUMBER_ELEMENTS; ++i)
        {
            unsortedNumbers[i] = Integer.valueOf(random.nextInt(NUMBER_ELEMENTS));
        }
        
        
        /*
         * Read a list of unsorted words, to test the string sorting capabilities
         */
         unsortedStrings = readLines("unsorted_wordlist.txt");
         
         /*  
          * Set the collator decomposition parameters and comparison strength
          * For more detail on the different decompositions and comparison strengths
          * 
          * @see http://docs.oracle.com/javase/1.5.0/docs/api/java/text/Collator.html
          */
         strictCollator = Collator.getInstance(Locale.US);
         strictCollator.setDecomposition(Collator.FULL_DECOMPOSITION);
         strictCollator.setStrength(Collator.IDENTICAL);
         
         
         /*
          * Create an unsorted collection of Collation keys for the strings to be sorted
          */
         unsortedStringsKey = new CollationKey[unsortedStrings.length]; 
         for (int i = 0; i < unsortedStrings.length; ++i)
         {
             unsortedStringsKey[i] = strictCollator.getCollationKey(unsortedStrings[i]);
         }
    }  
    
    /**
     * Tests the FastQuickSort implementation by verifying that it properly 
     * sorts a collection of random numbers.
     * 
     * Test method for {@link com.orwell.util.FastQuickSort#sort(T[])}.
     */
    @Test
    public void randomNumSortTest() throws Exception
    {        
        long startTime = 0;
        long endTime = 0;
        
        boolean sequenceMatch = true;
        
        /* Calculate the time it takes to sort the numerical data */
        startTime = System.currentTimeMillis();
        
        FastQuickSort.sort(unsortedNumbers);
        
        endTime = System.currentTimeMillis();
        
        
        /*
         * Verify that the sequence is actually ordered with a simple linear comparison
         */
        for (int i  = 0; i < unsortedNumbers.length - 1; ++i)
        {
            /*
             * If the current value in the list is greater than the next number, then
             * sequence of numbers is not sorted correctly
             */
            if (unsortedNumbers[i].compareTo(unsortedNumbers[i+1]) > 0)
            {
                System.out.println("The next value in the sequence: " + unsortedNumbers[i+1] + 
                        " is less than the current value: " + unsortedNumbers[i]);
                
                sequenceMatch = false;
                break;
            }
        }
        
        if (sequenceMatch)
        {
            System.out.println("THE NUMBER SEQUENCE IS CORRECTLY SORTED!");
        }
        else
        {
            Assert.fail("The number sequence is NOT SORTED CORRECTLY!");
        }

        System.out.println("Total time to sort numerical data: " + (endTime-startTime) + " milliseconds");
    }
    
    /**
     * Function which tests the FastQuickSort implementation by verifying that
     * it properly sorts a collection of strings.
     * 
     * Test method for {@link com.orwell.util.FastQuickSort#sort(T[], java.util.Comparator)}.
     */
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void randomStringSortTest() throws Exception
    {
        boolean sequenceMatch = true;
        
        // Used for calculating execution times
        long startTime = 0;
        long endTime = 0;
        
        // Calculate the time it takes to sort the string data
        startTime = System.currentTimeMillis();
        
        // Sort the strings with quicksort
        FastQuickSort.sort(unsortedStrings, (Comparator)strictCollator);
        
        endTime = System.currentTimeMillis();  
        
        /*
         * Verify that the string sequence is actually ordered with a simple linear comparison
         */
        for (int i  = 0; i < unsortedStrings.length - 1; ++i)
        {
            /*
             * If the current value in the list is greater than the next number, then
             * sequence of numbers is not sorted correctly
             */
            if (strictCollator.compare(unsortedStrings[i], unsortedStrings[i+1]) > 0)
            {
                System.out.println("The next value in the string sequence: " + unsortedStrings[i+1] + 
                        " is less than the current value: " + unsortedStrings[i]);
                sequenceMatch = false;
                break;
            }
        }
        
        if (sequenceMatch)
        {
            System.out.println("THE STRING SEQUENCE IS CORRECTLY SORTED!");
        }
        else
        {
            Assert.fail("The string sequence is NOT SORTED PROPERLY!");
        }
        
        System.out.println("Total time to sort STRING data: " + (endTime-startTime) + " milliseconds");
    }  
    
    /**
     * Function which tests the FastQuickSort implementation by verifying that
     * it properly sorts a collection of strings stored as CollationKeys.
     * 
     * Test method for {@link com.orwell.util.FastQuickSort#sort(T[], java.util.Comparator)}.
     */
    @Test
    public void randomKeysSortTest() throws Exception
    {
        boolean sequenceMatch = true;
        
        // Used for calculating execution times
        long startTime = 0;
        long endTime = 0;
        
        
        // Calculate the time it takes to sort the collation keys for the string data
        startTime = System.currentTimeMillis();
        
        // Sort the numbers with quicksort
        FastQuickSort.sort(unsortedStringsKey);
        
        endTime = System.currentTimeMillis();
        
        
        /*
         * Verify that the collation keys sequence is actually ordered with a simple linear comparison
         */
        for (int i  = 0; i < unsortedStringsKey.length - 1; ++i)
        {
            /*
             * If the current value in the list is greater than the next number, then
             * sequence of numbers is not sorted correctly
             */
            if (unsortedStringsKey[i].compareTo(unsortedStringsKey[i+1]) > 0)
            {
                System.out.println("The next value in the collation key sequence: " + unsortedStringsKey[i+1] + 
                        " is less than the current value: " + unsortedStringsKey[i]);
                sequenceMatch = false;
                break;
            }
        }
        
        if (sequenceMatch)
        {
            System.out.println("THE COLLATION KEY SEQUENCE IS CORRECTLY SORTED!");
        }
        else
        {
            Assert.fail("The COLLATION KEY sequence is NOT SORTED PROPERLY!");
        }
        
        System.out.println("Total time to sort COLLATION KEY data: " + (endTime-startTime) + " milliseconds");
    }
}
