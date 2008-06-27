/*
 * @author mchyzer
 * $Id: HooksMemberPreUpdateBean.java,v 1.1 2008-06-27 19:04:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Member;


/**
 * pre insert bean
 */
public class HooksMemberPreUpdateBean extends HooksMemberBean {

  /**
   * @param theMember
   */
  public HooksMemberPreUpdateBean(Member theMember) {
    super(theMember);
  }

}
