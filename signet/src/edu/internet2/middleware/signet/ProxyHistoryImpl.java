/*--
$Id: ProxyHistoryImpl.java,v 1.2 2006-02-09 10:24:05 lmcrae Exp $
$Date: 2006-02-09 10:24:05 $

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
 * @author Andy Cohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class ProxyHistoryImpl extends HistoryImpl implements ProxyHistory
{
  protected Proxy proxy;
  
  private Subsystem         subsystem;
  private boolean           canExtend;
  private boolean           canUse;
  
  /**
   * Hibernate requires the presence of a default constructor.
   */
  public ProxyHistoryImpl()
  {
    super();
  }
  
  ProxyHistoryImpl(ProxyImpl proxy)
  {
    // Most information is just copied from the Proxy object to the
    // AssignmentHistoryImpl object.
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
  
  public boolean canExtend()
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
  
  public boolean canUse()
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
