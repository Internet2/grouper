/*--
$Id: Proxy.java,v 1.1 2005-08-25 20:31:35 acohen Exp $
$Date: 2005-08-25 20:31:35 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Date;
import java.util.Set;

import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.SignetAuthorityException;

/**
* 
* A Proxy represents some granting authority which has been delegated to a 
* {@link PrivilegedSubject} (often a person). Unlike an (@link Assignment},
* which invests a <code>PrivilegedSubject</code> with some independent authority
* of its own, a Proxy allows a PrivilegedSubject to act "in the name of" its
* <code>Proxy</code>-grantor. For example, an administrative assistant who
* understands how to use the Signet UI may act as a Proxy for a high-ranking
* official who never actually logs into Signet.
* <p>
* Furthermore, this proxying ability applies only within Signet; That is, if
* B is a proxy for A, then B can grant any privilege that A can grant, but
* B cannot exercise any privilege that A can exercise (unless A explicitly
* grants that privilege to B). Someday, additional Proxy-types may be added
* to allow the exercise of a Proxy in enterprise information systems.
* <p>
* The granularity of a Proxy is always {@link Subsystem}; that is, a
* <code>Subsystem</code> is the smallest unit of authority which can be
* delegated.
* <p>
* Proxies are not transitive.
* <p>
* An existing Proxy may be modified. To save the modified Proxy,
* call Proxy.save().
* 
* @see PrivilegedSubject
* @see Function
* 
*/

public interface Proxy
extends Grantable
{
  
  /**
   * Gets the <code>Subsystem</code> associated with this Proxy.
   * 
   * @return the <code>Subsystem</code> associated with this Proxy.
   */
  public Subsystem getSubsystem();
  
 /**
  * Indicates whether or not this Proxy can be extended to a third party.
  * 
  * @param canExtend When <code>true</code>, means that the Proxy grantee, when
  *        acting as a Proxy for the Proxy grantor, can grant the grantor's
  *        Proxy to some third <code>PrivilegedSubject</code>. Note, however,
  *        that that third <code>PrivilegedSubject</code> is never allowed to
  *        further grant that second-hand Proxy.
  */
  public boolean canExtend();
  
  /**
   * Changes the extensibility of an existing Proxy. To save this change
   * to the database, call <code>Proxy.save()</code>.
   * 
   * @param editor the <code>PrivilegedSubject</code> who is responsible for
   * this change.
   *
   * @param canExtend <code>true</code> if this Proxy should be grantable
   * to others by its current grantee, and <code>false</code> otherwise.
   * 
   * @throws SignetAuthorityException
   */
  public void setCanExtend(PrivilegedSubject editor, boolean canExtend)
  throws SignetAuthorityException;

  /**
   * Indicates whether or not this Assignment can be used directly
   * by its current grantee, or can only be granted to others.
   * 
   * @return <code>false</code> if this Assignment can only be granted to others
   * by its current grantee, and not used directly by its current grantee.
   */
  public boolean canUse();
  
  /**
   * Changes the direct usability of an existing Assignment. To save this change
   * to the database, call <code>Assignment.save()</code>;
   * 
   * @param editor the <code>PrivilegedSubject</code> who is responsible for
   * this change.
   * 
   * @param isGrantOnly <code>false</code> if this Assignment should only be
   * granted to others (and not directly used) by its current grantee, and
   * <code>true</code> otherwise.
   * 
   * @throws SignetAuthorityException
   */
  public void setCanUse(PrivilegedSubject editor, boolean canUse)
  throws SignetAuthorityException;
}