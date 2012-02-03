/*--
$Id: JNDISourceAdapter.java,v 1.16 2009-10-23 04:04:22 mchyzer Exp $
$Date: 2009-10-23 04:04:22 $

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
 * JNDI Source.  This is the legacy classname which will use the new code in LdapSourceAdapter.
 * If you want to use the legacy code, configure for edu.internet2.middleware.subject.provider.JNDISourceAdapterLegacy
 *
 */
public class JNDISourceAdapter extends LdapSourceAdapter {

  /**
   * 
   */
  public JNDISourceAdapter() {
    super();
    
  }

  /**
   * @param id
   * @param name
   */
  public JNDISourceAdapter(String id, String name) {
    super(id, name);
    
  }


}
