/*
 * @author mchyzer
 * $Id: GrouperJdbcSourceAdapter.java,v 1.2 2008-08-18 06:15:58 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.subj;

import edu.internet2.middleware.subject.provider.HelperGrouperJdbcSourceAdapter;



/**
 * Grouper's jdbc source adapter.  Has c3p0 pooling (eventually), 
 * shares pool with grouper (evenutally), and decrypts passwords.
 */
public class GrouperJdbcSourceAdapter extends HelperGrouperJdbcSourceAdapter {

  /**
   * 
   */
  public GrouperJdbcSourceAdapter() {
  }

  /**
   * @param id
   * @param name
   */
  public GrouperJdbcSourceAdapter(String id, String name) {
    super(id, name);
  }

}
