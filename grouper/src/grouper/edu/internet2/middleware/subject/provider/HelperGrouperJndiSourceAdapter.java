/*--
$Id: HelperGrouperJndiSourceAdapter.java,v 1.2 2008-09-14 04:54:00 mchyzer Exp $
$Date: 2008-09-14 04:54:00 $

Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
See doc/license.txt in this distribution.
 */
/*
 * JNDISourceAdapter.java
 *
 * Created on March 6, 2006
 *
 * Author Ellen Sluss
 */
package edu.internet2.middleware.subject.provider;

/**
 * JNDI Source 
 *
 */
public class HelperGrouperJndiSourceAdapter
        extends LdapSourceAdapter {

  /**
   * 
   */
  public HelperGrouperJndiSourceAdapter() {
    super();
  }

  /**
   * @param arg0
   * @param arg1
   */
  public HelperGrouperJndiSourceAdapter(String arg0, String arg1) {
    super(arg0, arg1);
  }
    
}
