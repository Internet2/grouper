/*--
  $Id: SubjectNotFoundException.java,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.subject;

/**
Used to indicate that a requested Subject is not found in the data source.
 */
public class SubjectNotFoundException extends Exception
{
	public SubjectNotFoundException(String msg)
	{
		super(msg);
	}

	public SubjectNotFoundException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
