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

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.CollationKey;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.strippedcastle.crypto.DataLengthException;
import org.strippedcastle.crypto.digests.SHA256Digest;
import org.strippedcastle.crypto.engines.ISAACEngine;

import com.orwell.csprng.ISAACRandomGenerator;
import com.orwell.csprng.SDFGenerator;
import com.orwell.params.SDFParameters;
import com.orwell.stego.Steganography;
import com.orwell.util.DictionaryUtil;

public class SteganographyTest
{
    /* Number of elements */
    private static final int NUMBER_ELEMENTS = 100000;
    
    /* Minimum size of the dictionary */
    private static final int MIN_DICT_SIZE = 65537; 
    
    /* Collator for sorting words */
    private static Collator strictCollator;
    
    /* Original dictionary used as the source for the steganography */
    private static String[] masterDictionary;
    private String[] uniqueDictionary = new String[MIN_DICT_SIZE];
    private String[] inverseDictString = new String[MIN_DICT_SIZE];
    CollationKey[] inverseDictionary = new CollationKey[MIN_DICT_SIZE];
    
    /* CSPRNG for the dictionary */
    private static byte[] shared_seed = new byte[32];
    private static ISAACRandomGenerator isaac;
    
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
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        /*
         *  Inititialize the seed derivative function parameters with the
         *  parameters S1 & S2
         */
        SDFParameters paramSDF = new SDFParameters("test1", "test2");
        
        /* Generate the seed using SHA256 digest */
        SDFGenerator generatorSDF = new SDFGenerator(new SHA256Digest());
        generatorSDF.init(paramSDF);
        generatorSDF.generateBytes(shared_seed, 0, 0);
        
        /*
         * Simulate two separate users using a shared seed and the ISAAC 
         * stream cipher as the PRNG
         */
         isaac = new ISAACRandomGenerator(new ISAACEngine());
         isaac.init(shared_seed);
        
         
        /*
         * Read a list of unsorted words, to test the string sorting capabilities
         */
        masterDictionary = readLines("unsorted_wordlist.txt");
         
         /*  
          * Set the collator decomposition parameters and comparison strength
          * For more detail on the different decompositions and comparison strengths
          * 
          * @see http://docs.oracle.com/javase/1.5.0/docs/api/java/text/Collator.html
          */
         strictCollator = Collator.getInstance(Locale.US);
         strictCollator.setDecomposition(Collator.FULL_DECOMPOSITION);
         strictCollator.setStrength(Collator.IDENTICAL);
    }

    /**
     * Test that the Steganography class properly generates the unique dictionary and
     * inverse dictionary given the master dictionary.
     * Test method for {@link com.orwell.stego.Steganography#generateDict(java.lang.String[], java.text.CollationKey[], java.lang.String[], java.util.Comparator, com.orwell.csprng.ISAACRandomGenerator)}.
     * @throws Exception 
     * @throws IllegalArgumentException 
     * @throws DataLengthException 
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testGenerateDict() throws DataLengthException, IllegalArgumentException, Exception
    {
        byte[] value;
        int keyIndex = 0;
        boolean dictionaryMatch = true;
        
        long startTime = System.currentTimeMillis();
        Steganography.generateDict(uniqueDictionary, inverseDictionary, masterDictionary, (Comparator)strictCollator, isaac);
        long endTime = System.currentTimeMillis();
        
        /* Display results */
        for (int i  = 0; i < uniqueDictionary.length; ++i)
        {
            System.out.println(uniqueDictionary[i]);

        }
        
        for (int i = 0; i < inverseDictionary.length; ++i)
        {
            System.out.println(inverseDictionary[i].getSourceString());
            
            // Convert the inverseDictionary from collationkey to string array
            inverseDictString[i] = inverseDictionary[i].getSourceString();
        }
        System.out.println(uniqueDictionary.length + ", " + inverseDictionary.length);
        System.out.println(uniqueDictionary[0] + ", " + uniqueDictionary[uniqueDictionary.length - 1]);
        System.out.println(inverseDictString[0] + ", " + inverseDictString[inverseDictString.length - 1]);
        
        
        /* Verify that the unique and inverse dictionary map to each other correctly */
        for (int i  = 0; i < uniqueDictionary.length - 1; ++i)
        {
            System.out.println("dictionary " + uniqueDictionary[i] + ":" + i + " to inverse " 
                    + inverseDictString[i] + ":" + keyIndex);
            
            /* Get the inverse key mapped to the current word */
            value = DictionaryUtil.getInverseValue(uniqueDictionary[i], inverseDictString, strictCollator);
            ByteBuffer buffer = ByteBuffer.wrap(value);
            buffer.order(ByteOrder.BIG_ENDIAN);
            
            /* Get the key as an unsigned short index to lookup in the dictionary */
            keyIndex = buffer.getShort() & 0xFFFF;
            
            /* Verify that the dictionary index and the inverse dictionary map to each other */
            if (i != keyIndex)
            {
                System.out.println("The dictionary and inverse dictionary do not map at: " 
                        + uniqueDictionary[i] + ":" + i + " to inverse: " + keyIndex);
                dictionaryMatch = false;
                break;
            }
        }
        
        if (dictionaryMatch)
        {
            System.out.println("The dictionaries are correctly mapped!");
        }
        else
        {
            Assert.fail("THE DICTIONARIES ARE NOT CORRECTLY MAPPED!");
        }
   
        System.out.println("Total time to generate the unique dictionary and inverse dictionary " 
                + (endTime-startTime) + " milliseconds");
    }

    /**
     * Test method for {@link com.orwell.stego.Steganography#obfuscate(byte[], java.lang.String[])}.
     */
    @Test
    public void testObfuscate()
    {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link com.orwell.stego.Steganography#deObfuscate(java.lang.String, java.lang.String[], java.text.Collator)}.
     */
    @Test
    public void testDeObfuscate()
    {
        fail("Not yet implemented"); // TODO
    }
}
