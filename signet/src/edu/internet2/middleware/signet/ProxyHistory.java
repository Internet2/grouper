/*--
 $Id: ProxyHistory.java,v 1.4 2005-12-02 18:36:53 acohen Exp $
 $Date: 2005-12-02 18:36:53 $

 Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
 Licensed under the Signet License, Version 1,
 see doc/license.txt in this distribution.
 */
package edu.internet2.middleware.signet;

/**
 * @author Andy Cohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface ProxyHistory extends History
{
  /**
   * Returns the extensibility of the {@link Proxy} described by this
   * historical record, at the time this record was created.
   * 
   * @return the extensibility of the {@link Proxy} described by this
   * historical record, at the time this record was created.
   */
  public boolean canExtend();

  
  /**
   * Returns the useability of the {@link Proxy} described by this
   * historical record, at the time this record was created.
   * 
   * @return the useability of the {@link Proxy} described by this
   * historical record, at the time this record was created.
   */
  public boolean canUse();
}