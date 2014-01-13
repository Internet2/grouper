/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * gui wrapper around a membership subject container
 * @author mchyzer
 *
 */
public class GuiMembershipSubjectContainer {

  /**
   * get the gui group
   * @return gui gruop
   */
  public GuiGroup getGuiGroup() {
    return this.guiGroup;
  }
  
  /**
   * convert membership subject containers to gui membership subject containers
   * @param membershipSubjectContainers
   * @return the converted set
   */
  public static Set<GuiMembershipSubjectContainer> convertFromMembershipSubjectContainers(
      Set<MembershipSubjectContainer> membershipSubjectContainers) {
    
    Set<GuiMembershipSubjectContainer> results = new LinkedHashSet<GuiMembershipSubjectContainer>();

    for (MembershipSubjectContainer membershipSubjectContainer : GrouperUtil.nonNull(membershipSubjectContainers)) {
      GuiMembershipSubjectContainer guiMembershipSubjectContainer = new GuiMembershipSubjectContainer(membershipSubjectContainer);
      results.add(guiMembershipSubjectContainer);
    }
    return results;
  }

  /**
   * gui subject
   */
  private GuiSubject guiSubject;
  
  /**
   * gui subject
   * @return gui subject
   */
  public GuiSubject getGuiSubject() {
    return this.guiSubject;
  }
  
  /**
   * gui member
   */
  private GuiMember guiMember;
  
  /**
   * gui member
   * @return gui member
   */
  public GuiMember getGuiMember() {
    return this.guiMember;
  }

  /**
   * gui group
   */
  private GuiGroup guiGroup;
  
  /**
   * construct
   * @param membershipSubjectContainer1
   */
  public GuiMembershipSubjectContainer(
      MembershipSubjectContainer membershipSubjectContainer1) {
    super();
    this.membershipSubjectContainer = membershipSubjectContainer1;
    this.guiMember = new GuiMember(membershipSubjectContainer1.getMember());
    this.guiSubject = new GuiSubject(membershipSubjectContainer1.getSubject());
    this.guiMembershipContainers = new LinkedHashMap<String, GuiMembershipContainer>();
    if (membershipSubjectContainer1.getGroupOwner() != null) {
      this.guiGroup = new GuiGroup(membershipSubjectContainer1.getGroupOwner());
    }
    for (String fieldName : GrouperUtil.nonNull(membershipSubjectContainer1.getMembershipContainers()).keySet()) {
      this.guiMembershipContainers.put(fieldName, new GuiMembershipContainer(
          membershipSubjectContainer1.getMembershipContainers().get(fieldName)));
    }
    
  }

  /**
   * gui membership containers
   */
  private Map<String, GuiMembershipContainer> guiMembershipContainers;
  
  /**
   * gui membership containers
   * @return gui membership containers
   */
  public Map<String, GuiMembershipContainer> getGuiMembershipContainers() {
    return this.guiMembershipContainers;
  }

  /**
   * membership subject container
   */
  private MembershipSubjectContainer membershipSubjectContainer;

  /**
   * membership subject container
   * @return bean
   */
  public MembershipSubjectContainer getMembershipSubjectContainer() {
    return this.membershipSubjectContainer;
  }

  
  
}
