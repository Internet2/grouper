/*
 * @author mchyzer
 * $Id: GrouperJndiSourceAdapter.java,v 1.1 2008-08-18 06:15:58 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.subj;

import edu.internet2.middleware.subject.provider.HelperGrouperJndiSourceAdapter;


/**
 * grouper version of jndi source adapter
 */
public class GrouperJndiSourceAdapter extends HelperGrouperJndiSourceAdapter {

  /**
   * 
   */
  public GrouperJndiSourceAdapter() {
  }

  /**
   * @param id
   * @param name
   */
  public GrouperJndiSourceAdapter(String id, String name) {
    super(id, name);
  }

}
