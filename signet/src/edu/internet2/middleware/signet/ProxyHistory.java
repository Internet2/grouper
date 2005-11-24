/*--
$Id: ProxyHistory.java,v 1.3 2005-11-24 00:02:53 acohen Exp $
$Date: 2005-11-24 00:02:53 $

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
class ProxyHistory extends History
{
  protected Proxy proxy;
  
  private Subsystem         subsystem;
  private boolean           canExtend;
  private boolean           canUse;
  
  /**
   * Hibernate requires the presence of a default constructor.
   */
  public ProxyHistory()
  {
    super();
  }
  
  ProxyHistory(ProxyImpl proxy)
  {
    // Most information is just copied from the Proxy object to the
    // AssignmentHistory object.
    super(proxy);
    this.setProxy(proxy);
    this.setSubsystem(proxy.getSubsystem());
    this.setCanExtend(proxy.canExtend());
    this.setCanUse(proxy.canUse());
  }
  
  Proxy getProxy()
  {
    return this.proxy;
  }
  
  // This method is only for use by Hibernate.
  protected void setProxy(Proxy proxy)
  {
    this.proxy = proxy;
  }
  
  Subsystem getSubsystem()
  {
    return this.subsystem;
  }
  
  void setSubsystem(Subsystem subsystem)
  {
    this.subsystem = subsystem;
  }
  
  boolean canExtend()
  {
    return this.canExtend;
  }
  
  // This method is only for use by Hibernate.
  protected boolean getCanExtend()
  {
    return this.canExtend;
  }
  
  /* This method is for use only by Hibernate. */
  protected void setCanExtend(boolean canExtend)
  {
    this.canExtend = canExtend;
  }
  
  boolean canUse()
  {
    return this.canUse;
  }
  
  // This method is only for use by Hibernate.
  protected boolean getCanUse()
  {
    return this.canUse;
  }
  
  /* This method is only for use by Hibernate. */
  protected void setCanUse(boolean canUse)
  {
    this.canUse = canUse;
  }
  
  public String toString()
  {
    return
      "[proxy="
      + this.getProxy()
      + ", instanceNumber="
      + this.getInstanceNumber()
      + "]";
  }
}
