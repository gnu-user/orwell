/**
 * Orwell -- A security library for the pathologically paranoid
 *
 * Copyright (C) 2013, Jonathan Gillett, All rights reserved.
 * 
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see <http://www.gnu.org/licenses/>.
 */

import org.strippedcastle.crypto.digests.SHA256Digest;
import org.strippedcastle.crypto.engines.ISAACEngine;
import org.strippedcastle.crypto.params.KeyParameter;
import org.strippedcastle.crypto.prng.RandomGenerator;
import org.strippedcastle.crypto.StreamCipher;
import org.strippedcastle.jce.provider.BouncyCastleProvider;
import org.strippedcastle.util.encoders.Hex;

import com.orwell.csprng.ISAACRandomGenerator;
import com.orwell.csprng.SDFGenerator;
import com.orwell.params.SDFParameters;
import com.orwell.stego.Steganography;
import com.orwell.util.FastQuickSort;
import com.orwell.util.HashTable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;


/**
 * 
 * @title	Steganography Research and Proof of Concept
 * 
 * @author	GNU USER
 * 
 */
public class SteganographyActivity
{
	static {
		// Set bouncy castle as the most preferred security provider
	    Security.insertProviderAt(new BouncyCastleProvider(), 1);
	}
	
	// Number of random elements
	private static final int NUMBER_RANDOM_ELEMENTS = 10000;
	
	// Minimum size of the dictionary
	private static final int MIN_DICT_SIZE = 65537; 
	
	/**
	 * are_same A function which checks if two array of bytes are identical
	 * 
	 * @param a first array of bytes
	 * @param b first array of bytes
	 * 
	 * @return boolean, true if identical
	 */
	public boolean are_same(
	        byte[]  a,
	        byte[]  b)
	    {
	        if (a.length != b.length)
	        {
	            return false;
	        }

	        for (int i = 0; i != a.length; i++)
	        {
	            if (a[i] != b[i])
	            {
	                return false;
	            }
	        }

	        return true;
	    }
			

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
	 * Simple function which returns a hexadecimal string with a minimum fixed
	 * width given a value to convert to hex.
	 */
	public static String fixedWidthHex(int value, int minWidth)
	{
		String leadingZeros = "";
		
		for (int i = Integer.toHexString(value).length(); i < minWidth; ++i)
		{
			leadingZeros += "0";
		}
		
		return leadingZeros + Integer.toHexString(value).toUpperCase();
	}
    
	

	
	
	/**
	 * @param args
	 * @throws Exception 
	 */ 
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception
	{
		byte[] seed = new byte[32];
		byte[] random1 = new byte[32];
		byte[] random2 = new byte[32];
	
		int aliceKey;
		int bobKey;
		
		int length;
		boolean sequenceMatch = true;
		
		byte[] contentBefore = new byte[64];
		byte[] contentAfter = new byte[64];
		String stegotext;
		
		// Steganography dictionaries
		String[] uniqueDictionary = new String[MIN_DICT_SIZE];
		String[] inverseDictString = new String[MIN_DICT_SIZE];
		CollationKey[] inverseDictionary = new CollationKey[MIN_DICT_SIZE];
		
		
		// Calculate the time it takes to parse the file
		long startTime = System.currentTimeMillis();
		
		String[] masterDictionary = readLines("src/master_dictionary2.txt");
		
		
		
		long endTime = System.currentTimeMillis();
		
		System.out.println("Total time to store file in string array: " + (endTime-startTime) + " milliseconds");
		
		CollationKey[] unsortedKeys = new CollationKey[masterDictionary.length];
		
		
		/*
		 *  Inititialize the seed derivative function parameters with the
		 *  parameters S1 & S2
		 */
		SDFParameters paramSDF = new SDFParameters("test1", "test2");
		
		// Generate the seed using SHA256 digest
		SDFGenerator generatorSDF = new SDFGenerator(new SHA256Digest());
		generatorSDF.init(paramSDF);
		length = generatorSDF.generateBytes(seed, 0, 0);
		
		System.out.println("LENGTH: " + seed.length);
		System.out.println("SEED: " + new String(Hex.encode(seed)));
		System.out.println("HEX LENGTH: " + new String(Hex.encode(seed)).length());
		
		ISAACRandomGenerator isaac1 = new ISAACRandomGenerator(new ISAACEngine());
		ISAACRandomGenerator isaac2 = new ISAACRandomGenerator(new ISAACEngine());
		
		isaac1.init(seed);
		isaac2.init(seed);
		
		//byte[] test = Hex.encode(seed);
		
		byte[] subTest = new byte[2];
		
		System.arraycopy(seed, 0, subTest, 0, 2);
		
		ByteBuffer buffer = ByteBuffer.wrap(subTest);
		buffer.order(ByteOrder.BIG_ENDIAN);  // if you want little-endian
		int result = buffer.getShort() & 0xFFFF;
		
		System.out.println(result);
		//System.out.println(new String(subTest));
		//System.out.println(new BigInteger(subTest).intValue());		
		
		/*
		 * Quick test of the hashtable functions and miller-rabin primality tests
		 */
		 
		
		for (int i = 0; i < 30; ++i)
		{
			aliceKey = isaac1.nextInt();
			bobKey = isaac2.nextInt();
			
			System.out.println("Alice probe: " + HashTable.getProbe(aliceKey, 190366));
			System.out.println("Alice double hash interval: " + HashTable.getInterval(aliceKey));
			
			System.out.println("Bob probe: " + HashTable.getProbe(bobKey, 190366));
			System.out.println("Bog double hash interval: " + HashTable.getInterval(bobKey));

		}
		System.out.println("Co-prime bucketSize " + HashTable.getCoPrime(190366));
		
		
		/*
		 * Test that the Steganography class properly generates the unique dictionary and
		 * inverse dictionary given the master dictionary
		 */
		startTime = System.currentTimeMillis();
		Steganography.generateDict(uniqueDictionary, inverseDictionary, masterDictionary, (Comparator)strictCollator, isaac1);
		endTime = System.currentTimeMillis();
		
		// Display results
		for (int i  = 0; i < uniqueDictionary.length; ++i)
		{
			System.out.println(uniqueDictionary[i]);

		}
		
		for (int i = 0; i < inverseDictionary.length; ++i)
		{
			System.out.println(inverseDictionary[i].getSourceString());
		}
		
		System.out.println("Total time to generate the unique dictionary and inverse dictionary " + (endTime-startTime) + " milliseconds");
		
		
		// Convert the inverseDictionary from collationkey to string array as that is how it will be stored on disk
		for (int i =0; i < inverseDictionary.length; ++i)
		{
			inverseDictString[i] = inverseDictionary[i].getSourceString();
		}
		
		/*
		 * Test the steganography obfuscate and deobfuscate, as well as the average length of messages given
		 * a simulated encrypted content using random generated input
		 * 
		 *  Use the seed value to generate a sequence of random bytes as a GOOD test to simulate the encrypted
		 *  content, concatenate two random numbers to get a 64byte string as a test (since we will support
		 *  messages of at least 60 chars which is 64 bytes when using block cipher
		 */

		for (int i = 0; i < 10; ++i)
		{
			// Generate a random "simulated" encrypted message, this is the initial content
			isaac1.nextBytes(contentBefore);
		
			// Test string, breaks because of daemons conflict
			//contentBefore = Hex.decode("b904caa129f7a41c28300087d662e00c56cb2f121deac2f9226aea29ec5d4e972e83d41a3377438996b6e97602e0c226f1df6c6266b28cfb218b2040552b33b0");
			
			stegotext = Steganography.obfuscate(contentBefore, uniqueDictionary);
			contentAfter = Steganography.deObfuscate(stegotext, inverseDictString, strictCollator);
			
			// Verify that the content is THE EXACT SAME after it has had steganography applied (obfuscated) and then has
			// been de-obfuscated by the recipient, if they are not the same then the steganography has a SERIOUS FLAW!
			if (strictCollator.compare(new String(Hex.encode(contentBefore)), new String(Hex.encode(contentAfter))) != 0)
			{
				System.out.println("STEGANOGRAPHY FAILED, ORIGINAL CONTENT: " + new String(Hex.encode(contentBefore)) +
						" AND CONTENT AFTER DE-OBFUSCATE DO NOT MATCH: " + new String(Hex.encode((contentAfter))));
				sequenceMatch = false;
			}

			 System.out.println("Encrypted message before OBFUSCATE: " + new String(Hex.encode(contentBefore)));
			 System.out.println("STEGOTEXT: " + stegotext.length() + " : " + stegotext);
			 System.out.println("Encrypted message after DE-OBFUSCATE: " + new String(Hex.encode(contentAfter)));
		}
		if (sequenceMatch)
		{
			System.out.println("STEGANOGRAPHY SUCCEEDED!");
		}
		
	}
}