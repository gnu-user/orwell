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
import org.strippedcastle.crypto.Digest;
import org.strippedcastle.crypto.macs.HMac;
import org.strippedcastle.crypto.params.KeyParameter;

/**
 * The Elliptic Curve Gillett (ECG) Exchange, provides support for signing
 * and verifying the keys exchanged as part of an ECC/IES encryption scheme
 * by using shared information S1 and S2 that users have agreed upon before 
 * initiating the exchange.
 * 
 * In the event that the users initiate a key exchange without any shared
 * information the default shared information S1 = "initiator" and 
 * S2 = "recipient" are used.
 */
public abstract class ECGKeyExchange
{
    /** 
     * A method which takes an ASN.1 encoded public key Q and signs the 
     * public key using an HMAC with the shared information S1 + S2 if 
     * initiating the key exchange or S2 + S1 if responding to a key exchange.
     * 
     * @param digest The digest function to use for signing the key such as SHA256
     * @param encodedPubkey A byte array of the ASN.1 encoded public key Q
     * @param sharedInfo The shared information, S1 and S2
     * @param isInitiator True if initiating a key exchange, false if responding to a key exchange
     * 
     * @return A byte array containing the public key concatenated with the HMAC
     * 
     * @throws DataLengthException if shared information is empty
     */
    public static byte[] signPubKey(Digest digest, 
    								byte[] encodedPubKey, 
									APrioriInfo sharedInfo, 
									boolean isInitiator)
	throws DataLengthException
    {
    	if (sharedInfo.getS1().length == 0 || sharedInfo.getS2().length == 0)
    	{
    		throw new DataLengthException("The shared information S1 and S2 cannot be null/empty!");
    	}
    	
        /* The shared information to use for signing the public key */
        byte[] S = new byte[sharedInfo.getS1().length + sharedInfo.getS2().length];
    	HMac hmac = new HMac(digest);
        byte[] signedPubKey = new byte[encodedPubKey.length + hmac.getMacSize()];
        System.arraycopy(encodedPubKey, 0, signedPubKey, 0, encodedPubKey.length);
    	
    	/*
    	 * Set the shared information as S1 + S2 if initiating key exchange, S2 + S1 if
    	 * responding to key exchange
    	 */
    	if (isInitiator)
    	{
    		System.arraycopy(sharedInfo.getS1(), 0, S, 0, sharedInfo.getS1().length);
    		System.arraycopy(sharedInfo.getS2(), 0, S, sharedInfo.getS1().length, sharedInfo.getS2().length);
		}
    	else
    	{
    		System.arraycopy(sharedInfo.getS2(), 0, S, 0, sharedInfo.getS2().length);
    		System.arraycopy(sharedInfo.getS1(), 0, S, sharedInfo.getS2().length, sharedInfo.getS1().length);
		}

        /* Initializes and generate the signature for the public key */
        hmac.init(new KeyParameter(S));
        hmac.update(encodedPubKey, 0, encodedPubKey.length);
        hmac.doFinal(signedPubKey, encodedPubKey.length);

		return signedPubKey;
    }
    
    /** 
     * A method which takes a signed public key and verifies the public key 
     * using an HMAC with the shared information S1 + S2 if initiating the key 
     * exchange or S2 + S1 if responding to a key exchange.
     * 
     * @param digest the digest function to use for signing the key such as SHA256
     * @param signedPubKey byte array containing public key concatenated with the hash of the public key 
     * @param sharedInfo The shared information, S1 and S2
     * @param isInitiator True if initiating a key exchange, false if responding to a key exchange
     * 
     * @return true if the public key is verified to be valid
     * 
     * @throws DataLengthException if shared information is empty
     */
    public static boolean verifyPubKey(Digest digest, 
    								   byte[] signedPubKey, 
    								   APrioriInfo sharedInfo,
    								   boolean isInitiator)
	throws DataLengthException
    {
    	if (sharedInfo.getS1().length == 0 || sharedInfo.getS2().length == 0)
    	{
    		throw new DataLengthException("The shared information S1 and S2 cannot be null/empty!");
    	}
    	
    	/* Check that the signed public key is large enough to contain the signature */
    	if (signedPubKey.length <= digest.getDigestSize())
    	{
    	    throw new DataLengthException("Invalid signed public key, it's smaller than signature!");
	    }
    	
        /* The shared information to use for signing the public key */
        byte[] S = new byte[sharedInfo.getS1().length + sharedInfo.getS2().length];
        HMac hmac = new HMac(digest);
    	byte[] digestInput = new byte[signedPubKey.length - hmac.getMacSize()];
    	byte[] origSignature = new byte[hmac.getMacSize()];
    	byte[] calcSignature = new byte[hmac.getMacSize()];
    	
    	/* Copy the contents of the public key and the original signature */
    	System.arraycopy(signedPubKey, 0, digestInput, 0, signedPubKey.length - hmac.getMacSize());
    	System.arraycopy(signedPubKey, signedPubKey.length - hmac.getMacSize(), 
    			origSignature, 0, hmac.getMacSize());
    	
        /*
         * Set the shared information as S1 + S2 if initiating key exchange, S2 + S1 if
         * responding to key exchange
         */
    	if (isInitiator)
    	{
    		System.arraycopy(sharedInfo.getS2(), 0, S, 0, sharedInfo.getS2().length);
    		System.arraycopy(sharedInfo.getS1(), 0, S, sharedInfo.getS2().length, sharedInfo.getS1().length);
		}
    	else
    	{
    		System.arraycopy(sharedInfo.getS1(), 0, S, 0, sharedInfo.getS1().length);
    		System.arraycopy(sharedInfo.getS2(), 0, S, sharedInfo.getS1().length, sharedInfo.getS2().length);
		}   	    	
	
		/* Calculated the signature of the public key */
        hmac.init(new KeyParameter(S));
        hmac.update(digestInput, 0, digestInput.length);
        hmac.doFinal(calcSignature, 0);
		
		/*
		 * Verify that the calculated signature matches the signature of the
		 * signed public key received, returns true if the signatures match
		 */
		return signatureEquals(calcSignature, origSignature);
    }

	/**
	 * Verifies if two signatures are exactly equal using a byte-level
	 * comparison of the values. Usually used to verify the signature 
	 * of a key that has been exchanged.
	 * 
	 * @param sig1 The first signature to compare
	 * @param sig2 The second signature to compare
	 * 
	 * @return boolean, true if the signatures are identical
	 */
    private static boolean signatureEquals(byte[] sig1, byte[] sig2)
    {
        if (sig1.length != sig2.length)
        {
            return false;
        }

        for (int i = 0; i != sig1.length; ++i)
        {
            if (sig1[i] != sig2[i])
            {
                return false;
            }
        }

        return true;
    }
}
