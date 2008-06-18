/*
 * @author mchyzer
 * $Id: HooksGroupPreInsertBean.java,v 1.1.2.3 2008-06-18 09:22:22 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.internal.dto.GroupDTO;


/**
 * pre insert bean
 */
public class HooksGroupPreInsertBean extends HooksBean {

  /** object being inserted */
  private GroupDTO groupDto = null;
  
  /**
   * @param theHooksContext
   * @param theGroupDto 
   */
  public HooksGroupPreInsertBean(HooksContext theHooksContext, GroupDTO theGroupDto) {
    super(theHooksContext);
    this.groupDto = theGroupDto;
  }
  
  /**
   * object being inserted
   * @return the GroupDTO
   */
  public GroupDTO getGroupDto() {
    return this.groupDto;
  }
}
