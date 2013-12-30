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
import org.strippedcastle.crypto.DataLengthException;

import com.orwell.crypto.APrioriInfo;

public class APrioriInfoTest
{
	private APrioriInfo priorInfo;
	private String expectedS1;
	private String expectedS2;
	
	@Before
	public void setUp() throws Exception
	{
		/* Set the default S1 and S2 as defined by the ECG protocol */
		expectedS1 = "initiator";
		expectedS2 = "recipient";
	}

	/**
	 *  Test that the APrioriInfo object created with the default S1 & S2 shared info
	 */
	@Test
	public void defaultSharedInfo()
	{
		priorInfo = new APrioriInfo("initiator", "recipient");
		assertTrue(expectedS1.equals(new String(priorInfo.getS1())));
		assertTrue(expectedS2.equals(new String(priorInfo.getS2())));
	}
	
	/**
	 * Test that it throws an exception if no shared info S1 & S2 provided
	 */
	@Test(expected=DataLengthException.class)
	public void noSharedInfo()
	{
		priorInfo = new APrioriInfo("", "");
	}
	
	/**
	 * Test that it throws an exception if only one shared info S1/S2 provided
	 */
	@Test(expected=DataLengthException.class)
	public void oneSharedInfo()
	{
		priorInfo = new APrioriInfo("initiator", "");
	}
}
