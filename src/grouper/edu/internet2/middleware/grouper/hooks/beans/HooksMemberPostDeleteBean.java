/*
 * @author mchyzer
 * $Id: HooksMemberPostDeleteBean.java,v 1.1 2008-06-27 19:04:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Member;


/**
 * pre insert bean
 */
public class HooksMemberPostDeleteBean extends HooksMemberBean {

  /**
   * @param theMember
   */
  public HooksMemberPostDeleteBean(Member theMember) {
    super(theMember);
  }

}
