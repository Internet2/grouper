/*--
$Id: SubsystemPart.java,v 1.2 2004-12-24 04:15:46 acohen Exp $
$Date: 2004-12-24 04:15:46 $

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
interface SubsystemPart extends Entity
{
 /**
  * Gets the Subsystem associated with this entity.
  * 
  * @return Returns the subsystem.
  */
 public Subsystem getSubsystem()
 throws ObjectNotFoundException;
 
 /**
  * Sets the subsystem associated with this entity.
  * 
  * @param subsystem The subsystem to set.
  */
 void setSubsystem(Subsystem subsystem);
}
