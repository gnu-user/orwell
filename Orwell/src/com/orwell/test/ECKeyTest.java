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
package com.orwell.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.strippedcastle.crypto.params.ECPrivateKeyParameters;
import org.strippedcastle.crypto.params.ECPublicKeyParameters;

import com.orwell.crypto.ECKey;
import com.orwell.params.ECKeyParam;

public class ECKeyTest
{
	private ECKeyParam param;
	private ECKey expKey;
	private ECPublicKeyParameters expPubKey;
	private ECPrivateKeyParameters expPriKey;
	
	@Before
	public void setUp() throws Exception
	{
		/* Create an instance of the ECKeyParam object with default curve */
		param = new ECKeyParam();
		
		/* Create an instance of the expected key and initialize it */
		expKey = new ECKey(param.getECDomainParam());
		expKey.init();
		
		/* Set the expected public/private keys */
		expPubKey = (ECPublicKeyParameters) expKey.getPublic();
		expPriKey = (ECPrivateKeyParameters) expKey.getPrivate();
		
	}
	
	/**
	 * Test that multiple subsequent elliptic curve keypairs created using the
	 * same domain parameters have unique private and public keys and are therefore
	 * random
	 * 
	 * TODO: This test is pretty poor at testing the actual cryptographic security...
	 */
	@Test
	public void uniqueECKeys()
	{
		ECKey key;
		ECPublicKeyParameters pubKey;
		ECPrivateKeyParameters priKey;
		
		/* Keep creating new keypairs and assure that there are no cycles and that the
		 * keys are uniformly random... this test needs to be improved and done properly...
		 */
		for (int i = 0; i < 100; ++i)
		{
			key = new ECKey(param.getECDomainParam());
			key.init();
			pubKey = (ECPublicKeyParameters) key.getPublic();
			priKey = (ECPrivateKeyParameters) key.getPrivate();
			
			/* Assert that the X and Y parameters of public keys are different, thus unique */
			assertFalse(expPubKey.getQ().getX().toBigInteger().equals(pubKey.getQ().getX().toBigInteger()));
			assertFalse(expPubKey.getQ().getY().toBigInteger().equals(pubKey.getQ().getY().toBigInteger()));
			
			/* Assert that the private keys are different, thus unique */
			assertFalse(expPriKey.getD().equals(priKey.getD()));
		}
	}
}
