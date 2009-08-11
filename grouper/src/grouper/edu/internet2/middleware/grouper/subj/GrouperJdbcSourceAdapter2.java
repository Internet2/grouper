/*
 * @author mchyzer
 * $Id: GrouperJdbcSourceAdapter2.java,v 1.2 2009-08-11 20:18:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.subj;

import edu.internet2.middleware.subject.provider.HelperGrouperJdbcSourceAdapter2;



/**
 * Grouper's jdbc source adapter.  Has c3p0 pooling (eventually), 
 * shares pool with grouper (evenutally), and decrypts passwords.
 */
public class GrouperJdbcSourceAdapter2 extends HelperGrouperJdbcSourceAdapter2 {

  /**
   * 
   */
  public GrouperJdbcSourceAdapter2() {
  }

  /**
   * @param id
   * @param name
   */
  public GrouperJdbcSourceAdapter2(String id, String name) {
    super(id, name);
  }

}
