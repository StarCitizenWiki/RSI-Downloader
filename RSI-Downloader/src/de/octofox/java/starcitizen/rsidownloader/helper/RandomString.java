package de.octofox.java.starcitizen.rsidownloader.helper;

import java.security.SecureRandom;

public class RandomString {

	private static final String ALNUM_SEQ = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	
	/**
	 * generates a random alphanumeric string with a given length
	 * @param len length of the string
	 * @return random alphanumeric substring with given length
	 */
	public static String getAlNum( int len ) {

		SecureRandom rnd = new SecureRandom();
		StringBuilder sb = new StringBuilder( len );
        
		for ( int i = 0; i < len; i++ ) {
        	sb.append( RandomString.ALNUM_SEQ.charAt( rnd.nextInt(RandomString.ALNUM_SEQ.length()) ) );
        }
        return sb.toString();
    }
}
