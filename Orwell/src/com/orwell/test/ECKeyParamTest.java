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

import java.security.InvalidParameterException;

import org.junit.Before;
import org.junit.Test;

import org.strippedcastle.jce.ECNamedCurveTable;
import org.strippedcastle.jce.spec.ECParameterSpec;

import com.orwell.params.ECKeyParam;

public class ECKeyParamTest
{
	private ECParameterSpec expectedCurve;
	private ECKeyParam keyParam;
	
	@Before
	public void setUp() throws Exception
	{
		expectedCurve = ECNamedCurveTable.getParameterSpec("secp256r1");
	}

	/**
	 * Test that the correct curve is being created by the default
	 * constructor.
	 * 
	 * NOTE: If you changed the defNamedCurve constant in the class then you must
	 * update the setUp() for this test to match the named curve.
	 */
	@Test
	public void defNamedCurve()
	{
		keyParam = new ECKeyParam();
		assertTrue(expectedCurve.getCurve().equals(keyParam.getCurve()));
		assertTrue(expectedCurve.getG().equals(keyParam.getG()));
		assertTrue(expectedCurve.getN().equals(keyParam.getN()));
	}
	
	/**
	 * Test that an exception is thrown if an invalid curve name is provided
	 */
	@Test(expected=InvalidParameterException.class)
	public void invalidNamedCurve()
	{
		keyParam = new ECKeyParam("derp");
	}
}