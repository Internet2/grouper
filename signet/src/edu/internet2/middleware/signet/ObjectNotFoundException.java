/*--
$Id: ObjectNotFoundException.java,v 1.2 2004-12-24 04:15:46 acohen Exp $
$Date: 2004-12-24 04:15:46 $

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
  // TODO Auto-generated constructor stub
}

/**
 * @param message
 */
public ObjectNotFoundException(String message)
{
  super(message);
  // TODO Auto-generated constructor stub
}

/**
 * @param message
 * @param cause
 */
public ObjectNotFoundException(String message, Throwable cause)
{
  super(message, cause);
  // TODO Auto-generated constructor stub
}

/**
 * @param cause
 */
public ObjectNotFoundException(Throwable cause)
{
  super(cause);
  // TODO Auto-generated constructor stub
}

}
