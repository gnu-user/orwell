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
package com.orwell.csprng;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.strippedcastle.crypto.DataLengthException;
import org.strippedcastle.crypto.StreamCipher;
import org.strippedcastle.crypto.engines.ISAACEngine;
import org.strippedcastle.crypto.params.KeyParameter;
import org.strippedcastle.crypto.prng.RandomGenerator;

/**
 * ISAAC random number generator that generates random numbers using the
 * ISAAC stream cipher. Currently only ISAAC is supported, ISAAC+ support
 * is planned for future releases.
 */
public class ISAACRandomGenerator implements RandomGenerator
{
	private ISAACEngine engine;
	private byte[] seed;
	
	/**
	 * Default constructor, at the moment only ISAAC engine is supported, 
	 * but in future ISAAC+ will also be supported.
	 * 
	 * @param engine The ISAAC engine, currently ISAAC+ is NOT supported
	 * 
	 * @throws IllegalArgumentException if the stream cipher is not ISAAC
	 */
	public ISAACRandomGenerator(StreamCipher engine)
			throws IllegalArgumentException
	{
		if (! (engine instanceof ISAACEngine))
		{
			throw new IllegalArgumentException("Invalid stream engine, at the moment ONLY ISAACEngine is supported!");
		}
		
		this.engine = (ISAACEngine)engine;
	}
	
	/**
	 * Initialize the ISAAC random generator with an initial seed value. The seed 
	 * value must be specified, future versions will enforce a minimum level of 
	 * entropy, week seeds will throw exceptions!
	 * 
	 * @param seed The seed value, must be specified
	 * 
     * @throws DataLengthException if the seed is not specified or has weak entropy
	 */
	public void init(byte[] seed)
			throws DataLengthException
	{
		/*
		 * TODO Add a check to throw an exception if the level of entropy does not 
		 * meet minimum requirements
		 */
		if (seed.length == 0)
		{
			throw new DataLengthException("Seed cannot be empty, you must specify a high entropy seed!");
		}
		
		this.seed = seed;
		
		// Initialize the stream cipher engine
		engine.init(true, new KeyParameter(seed));
	}
	
	/* 
	 * Add more seed material to the generator, this re-initializes the stream cipher
	 * with the new seed data to ensure maximum randomness
	 * 
	 * @param seed The additional seed data to add
	 * @throws DataLengthException if the seed is not specified or has weak entropy
	 */
	public void addSeedMaterial(byte[] seed)
			throws DataLengthException
	{
		/*
		 * TODO Add a check to throw an exception if the level of entropy does not 
		 * meet minimum requirements
		 */
		if (seed.length == 0)
		{
			throw new DataLengthException("Seed cannot be empty, you must specify a high entropy seed!");
		}
		
		/*
		 * Resize the current array of bytes for the seed and add the additional seed data
		 * to the current seed value, then re-initialize the stream cipher with the new
		 * seed data
		 */
		byte[] newSeed = new byte[this.seed.length + seed.length];
		System.arraycopy(this.seed, 0, newSeed, 0, this.seed.length);
		System.arraycopy(seed, 0, newSeed, this.seed.length, seed.length);
		
		this.seed = newSeed;
		this.engine.reset();
		this.engine.init(true, new KeyParameter(newSeed));
	}
	
	/* 
	 * Add more seed material to the generator, this re-initializes the stream cipher
	 * with the new seed data to ensure maximum randomness
	 * 
	 * @param seed The additional seed data to add
	 * @throws DataLengthException if the seed is not specified or has weak entropy
	 */
	public void addSeedMaterial(long seed)
			throws DataLengthException
	{
		/*
		 * TODO Add a check to throw an exception if the level of entropy does not 
		 * meet minimum requirements
		 */
		if (seed == 0)
		{
			throw new DataLengthException("Seed cannot be zero, you must specify a high entropy seed!");
		}
		
		/*
		 * Convert the seed from long to bytes and add the additional seed data
		 * to the current seed value, then re-initialize the stream cipher with the
		 * new seed data
		 */
		byte[] convSeed = new byte[Long.SIZE / 8];	// Long is 8 bytes
		byte[] newSeed = new byte[this.seed.length + convSeed.length];
		
		ByteBuffer buf = ByteBuffer.wrap(convSeed);
		buf.putLong(seed);
		
		System.arraycopy(this.seed, 0, newSeed, 0, this.seed.length);
		System.arraycopy(convSeed, 0, newSeed, this.seed.length, convSeed.length);
		
		this.seed = newSeed;
		this.engine.reset();
		this.engine.init(true, new KeyParameter(newSeed));
	}
	
	/**
	 * Generates a random data using ISAAC stream cipher and populates
	 * the byte array provided with the random number.
	 * 
	 * @note For maximum entropy it is recommended to frequently add additional
	 * random seed data, other random or even hardware random sources are ideal.
	 * 
	 * @param bytes The output byte array to populate with the random number
	 */
	public void nextBytes(byte[] bytes)
	{
		byte[] in = new byte[bytes.length];
		
		// Generate the random number and store in output
		this.engine.processBytes(in, 0, in.length, bytes, 0);
	}
	
	/**
	 * Generates a random data using ISAAC stream cipher and populates
	 * the byte array provided with the random number.
	 * 
	 * @note For maximum entropy it is recommended to frequently add additional
	 * random seed data, other random or even hardware random sources are ideal.
	 * 
	 * @param bytes The output byte array to populate with the random number
	 * @param start The index to start filling at
	 * @param len The length of the segment to fill
	 */
	public void nextBytes(byte[] bytes, int start, int len)
	{
		byte[] in = new byte[len];
		
		// Generate the random number and store in output
		this.engine.processBytes(in, 0, len, bytes, start);
	}
	
	/**
	 * Since most PRNG are used for NUMBERS it seems prudent to add an additional
	 * function to return randomly generated data as BigInteger instead of having to
	 * always convert manually from bytes.
	 * 
	 * @return BigInteger A large randomly generated number
	 */
	public BigInteger nextBigInteger()
	{
		// 256-bit number, this is much larger than long
		byte[] in = new byte[32];
		byte[] out = new byte[32];
		
		// Generate the random number and return the converted BigInteger number'
		this.engine.processBytes(in, 0, in.length, out, 0);
		
		return new BigInteger(out);
	}
		
	/**
	 * Since most PRNG are used for NUMBERS it seems prudent to add an additional
	 * function to return randomly generated data as int instead of having to
	 * always convert manually from bytes.
	 * 
	 * @return int A randomly generated number
	 */
	public int nextInt()
	{
		// 256-bit number, this is much larger than long
		byte[] in = new byte[32];
		byte[] out = new byte[32];
		
		// Generate the random number and return the converted BigInteger number'
		this.engine.processBytes(in, 0, in.length, out, 0);
		
		return new BigInteger(out).intValue();
	}
		
	/**
	 * Resets the CSPRNG, after resetting the CSPRNG you must either re-initialize
	 * it or add additional seed material.
	 */
	public void reset()
	{
	    this.engine.reset();
	}
}
