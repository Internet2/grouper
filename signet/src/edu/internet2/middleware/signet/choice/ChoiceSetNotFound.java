/*--
$Id: ChoiceSetNotFound.java,v 1.1 2005-03-07 18:55:44 acohen Exp $
$Date: 2005-03-07 18:55:44 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/

package edu.internet2.middleware.signet.choice;

/**
Used to indicate that a requested ChoiceSet is not found in the data
source.
*/
public class ChoiceSetNotFound extends Exception
{
	/**
	 * Default constructor.
	 */
	public ChoiceSetNotFound() {
	  super();
	}

	public ChoiceSetNotFound(Throwable cause) {
	  super(cause);
	}

	public ChoiceSetNotFound(String msg) {
		super(msg);
	}

	public ChoiceSetNotFound(String msg, Throwable cause) {
		super(msg, cause);
	}
}
