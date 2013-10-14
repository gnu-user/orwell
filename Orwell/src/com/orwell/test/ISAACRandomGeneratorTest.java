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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.engines.ISAACEngine;

import com.orwell.csprng.ISAACRandomGenerator;
import com.orwell.csprng.SDFGenerator;
import com.orwell.csprng.SDFParameters;


public class ISAACRandomGeneratorTest
{
    /* Number of elements */
    private static final int NUMBER_ELEMENTS = 10000;
 
    /* Seed for the CSPRNG */
    private static byte[] shared_seed = new byte[32];
    private static byte[] shared_seed2 = new byte[32];
    
    private static ISAACRandomGenerator isaac1;
    private static ISAACRandomGenerator isaac2;
    
    
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
         isaac1 = new ISAACRandomGenerator(new ISAACEngine());
         isaac2 = new ISAACRandomGenerator(new ISAACEngine());
         isaac1.init(shared_seed);
         isaac2.init(shared_seed);
    }

    /**
     * Simple function to test that the random number generators generate the
     * same sequence of values give a shared seed from the SDF
     */
    @Test
    public void randomSequenceTest()
    {   
        List<BigInteger> randomSequence1 = new ArrayList<BigInteger>();
        List<BigInteger> randomSequence2 = new ArrayList<BigInteger>();
        

        /* Generate two separate sequences of random data and verify they are identical */
        for (int i = 0; i < NUMBER_ELEMENTS; ++i)
        {
            randomSequence1.add(isaac1.nextBigInteger());
        }
        
        for (int j = 0; j < NUMBER_ELEMENTS; ++j)
        {
            randomSequence2.add(isaac2.nextBigInteger());
        }

        
        /* Verify that the two sequences are equal */
        Assert.assertEquals(randomSequence1, randomSequence2);
    }
}
