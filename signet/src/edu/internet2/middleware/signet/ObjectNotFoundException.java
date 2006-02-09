/*--
$Id: ObjectNotFoundException.java,v 1.4 2006-02-09 10:22:34 lmcrae Exp $
$Date: 2006-02-09 10:22:34 $

Copyright 2006 Internet2, Stanford University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
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
