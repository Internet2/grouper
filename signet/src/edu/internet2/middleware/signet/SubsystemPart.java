/*--
$Id: SubsystemPart.java,v 1.6 2005-11-01 00:07:33 acohen Exp $
$Date: 2005-11-01 00:07:33 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

/**
* Every sub-part of the Signet Subsystem implements this interface, which
* ensures that each of those parts has its full complement of common
* attributes.
* 
*/
interface SubsystemPart extends NonGrantable
{
 /**
  * Gets the <code>Subsystem</code> associated with this entity.
  * 
  * @return Returns the <code>Subsystem</code>.
  */
 public Subsystem getSubsystem();
}
