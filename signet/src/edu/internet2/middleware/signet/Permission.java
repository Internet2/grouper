/*--
$Id: Permission.java,v 1.2 2004-12-24 04:15:46 acohen Exp $
$Date: 2004-12-24 04:15:46 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import edu.internet2.middleware.subject.Subject;

/**
* Permission describes an application-level action that a {@link Subject} may
* be allowed to perform.
* 
*/

public interface Permission
extends SubsystemPart
{  
/**
 * Gets the Functions associated with this Permission.
 * 
 * @return Returns the Functions associated with this Permission.
 */
public Function[] getFunctionsArray();

/**
 * Sets the Functions associated with this Permission.
 * 
 * @param functions The Functions associated with this Permission.
 */
void setFunctionsArray(Function[] functions);

/**
 * Adds a Function to the set of Functions associated with this Permission.
 * 
 * @param function
 */
void addFunction(Function function);
}
