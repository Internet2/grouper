/*--
$Id: Category.java,v 1.4 2005-10-31 18:31:44 acohen Exp $
$Date: 2005-10-31 18:31:44 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Set;

import edu.internet2.middleware.subject.Subject;

/**
* Category organizes a group of {@link Function}s. Each <code>Function</code> is
* intended to correspond to a business-level task that a {@link Subject}
* must perform in order to accomplish some business operation.
* 
*/

public interface Category
extends SubsystemPart, Name, Comparable
{
  /**
   * Gets the ID of this entity.
   * 
   * @return Returns a short mnemonic id which will appear in XML
   *    documents and other documents used by analysts.
   */
  public String getId();
  
  /**
   * Gets the {@link Function}s associated with this Category.
   * 
   * @return Returns the functions associated with this Category.
   */
  public Set getFunctions();

  /**
   * Gets the Subsystem associated with this Category.
   * 
   * @return the Subsystem associated with this Category.
   */
  public Subsystem getSubsystem();
}
