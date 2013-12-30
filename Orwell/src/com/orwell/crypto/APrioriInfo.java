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
package com.orwell.crypto;

import org.strippedcastle.crypto.DataLengthException;

 /**
 * Information communicated by both parties involved in the key exchange before
 * the actual key exchange takes place (a priori), contains the a priori shared 
 * information S1 & S2 that users have agreed upon before initiating the exchange
 */
public final class APrioriInfo
{
    private final byte[] S1;
    private final byte[] S2;
	
	/**
	 * Defines the two parameters for constructor which are the a priori
	 * shared information S1 & S2, the values cannot be empty
	 * 
	 * @param S1 Shared information S1
	 * @param S2 Shared information S2
	 * 
	 * @throws DataLengthException if shared information is empty
	 */
	public APrioriInfo(String S1, String S2)
			throws DataLengthException
	{
		if (S1.length() == 0 || S2.length() == 0)
		{
		    throw new DataLengthException("You must specify a value for the shared information!");
		}
		
		this.S1 = S1.getBytes();
		this.S2 = S2.getBytes();
	}

	/**
	 * Gets the a priori shared information S1
	 * @return shared information, S1
	 */
    public byte[] getS1()
    {
        return S1;
    }
    
	/**
	 * Gets the a priori shared information S2
	 * @return shared information, S2
	 */
    public byte[] getS2()
    {
        return S2;
    }
}
