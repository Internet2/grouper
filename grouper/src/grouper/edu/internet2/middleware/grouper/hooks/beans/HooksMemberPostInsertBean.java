/*
 * @author mchyzer
 * $Id: HooksMemberPostInsertBean.java,v 1.1 2008-06-27 19:04:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Member;


/**
 * pre insert bean
 */
public class HooksMemberPostInsertBean extends HooksMemberBean {

  /**
   * @param theMember
   */
  public HooksMemberPostInsertBean(Member theMember) {
    super(theMember);
  }

}
