/*--
$Id: ChoiceSetNotFoundException.java,v 1.1 2005-01-12 23:49:24 mnguyen Exp $
$Date: 2005-01-12 23:49:24 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/

package edu.internet2.middleware.signet.choice;

/**
Used to indicate that a requested ChoiceSet is not found in the data
source.
*/
public class ChoiceSetNotFoundException extends Exception
{
	/**
	 * Default constructor.
	 */
	public ChoiceSetNotFoundException() {
	  super();
	}

	public ChoiceSetNotFoundException(Throwable cause) {
	  super(cause);
	}

	public ChoiceSetNotFoundException(String msg) {
		super(msg);
	}

	public ChoiceSetNotFoundException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
