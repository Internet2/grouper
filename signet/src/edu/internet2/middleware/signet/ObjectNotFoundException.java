/*--
$Id: ObjectNotFoundException.java,v 1.3 2005-01-11 20:38:44 acohen Exp $
$Date: 2005-01-11 20:38:44 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

/**
* This exception is thrown whenever an object required for the successful
* completion of a Signet operation is missing from the database.
* 
*/
public class ObjectNotFoundException extends Exception
{

/**
 * 
 */
public ObjectNotFoundException()
{
  super();
}

/**
 * @param message
 */
public ObjectNotFoundException(String message)
{
  super(message);
}

/**
 * @param message
 * @param cause
 */
public ObjectNotFoundException(String message, Throwable cause)
{
  super(message, cause);
}

/**
 * @param cause
 */
public ObjectNotFoundException(Throwable cause)
{
  super(cause);
}

}
