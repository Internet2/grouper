/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: GuiMember.java,v 1.3 2009-11-02 08:50:40 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.grouperUi.beans.simpleMembershipUpdate.SimpleMembershipUpdateContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.SubjectNotFoundException;



/**
 * member bean wraps grouper class with useful methods for UIs
 */
@SuppressWarnings("serial")
public class GuiMember implements Serializable {

  /**
   * 
   * @param members
   * @param configMax
   * @param max
   * @return
   */
  public static Set<GuiMember> convertFromMembers(Set<Member> members) {
    return convertFromMembers(members, null, -1);
  }

  /**
   * 
   * @param members
   * @param configMax
   * @param max
   * @return
   */
  public static Set<GuiMember> convertFromMembers(Set<Member> members, String configMax, int defaultMax) {
    Set<GuiMember> tempMembers = new LinkedHashSet<GuiMember>();
    
    Integer max = null;
    
    if (!StringUtils.isBlank(configMax)) {
      max = GrouperUiConfig.retrieveConfig().propertyValueInt(configMax, defaultMax);
    }
    
    int count = 0;
    for (Member member : GrouperUtil.nonNull(members)) {
      tempMembers.add(new GuiMember(member));
      if (max != null && ++count >= max) {
        break;
      }
    }
    
    return tempMembers;
    
  }

  /**
   * default constructor
   */
  public GuiMember() {
    
  }
  
  
  /**
   * get membership if here
   * @return the membership
   */
  public Membership getMembership() {
    return this.membership;
  }

  /** member */
  private Member member;
  
  /** immediate membership */
  private Membership membership;
  
  /**
   * construct from member
   * @param member1
   */
  public GuiMember(Member member1) {
    try {
      this.guiSubject = new GuiSubject(member1.getSubject());
    } catch (SubjectNotFoundException snfe) {
      this.guiSubject = new GuiSubject(new SubjectWrapper(member1));
    }
    this.setGuiSubject(this.guiSubject);
    this.member = member1;
  }
  
  /**
   * 
   * @param membership1
   */
  public void setMembership(Membership membership1) {
    this.membership = membership1;
  }
  
  /**
   * format on screen of config for milestone: yyyy/MM/dd (not hh:mm aa)
   */
  public static final String TIMESTAMP_FORMAT = "yyyy/MM/dd";

  /**
   * <pre> format: yyyy/MM/dd HH:mm:ss.SSS synchronize code that uses this standard formatter for timestamps </pre>
   */
  final static SimpleDateFormat timestampFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);

  /**
   * 
   * @return the disabled date
   */
  public boolean isHasDisabledString() {
    return this.membership != null && this.membership.getDisabledTime() != null;
  }
  
  /**
   * 
   * @return the short link for a webpage for this member
   */
  public String getShortLink() {
    return shortLinkHelper(false);
  }
  
  /**
   * 
   * @return the short link for a webpage for this member
   */
  public String getShortLinkWithIcon() {
    return shortLinkHelper(true);
  }
  
  /**
   * 
   * @return the short link for a webpage for this member
   */
  private String shortLinkHelper(boolean showIcon) {
    Member theMember = this.getMember();

    GuiSubject guiSubject = null;

    if (theMember != null) {
      guiSubject = this.getGuiSubject();
    }
    
    if (guiSubject == null) { 
      return TextContainer.retrieveFromRequest().getText().get("guiObjectUnknown");
    }
    if (showIcon) {
      return guiSubject.getShortLinkWithIcon();
    }
    return guiSubject.getShortLink();
    
  }

  /**
   * 
   * @return the disabled date
   */
  public String getDisabledDateString() {
    String format = this.getDisabledDate();
    if (format == null) {
      return null;
    }
    SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = SimpleMembershipUpdateContainer.retrieveFromSession();
    return "("+ simpleMembershipUpdateContainer.getText().getDisabledPrefix() 
      + ": " + format + ")";
  }

  /**
   * 
   * @return the disabled date
   */
  public String getDisabledDate() {
    if (this.membership == null || this.membership.getDisabledTime() == null) {
      return null;
    }
    return formatEnabledDisabled(this.membership.getDisabledTime());
  }

  /**
   * 
   * @return the disabled date
   */
  public String getEnabledDate() {
    if (this.membership == null || this.membership.getEnabledTime() == null) {
      return null;
    }
    return formatEnabledDisabled(this.membership.getEnabledTime());
  }

  /**
   * @param timestamp 
   * @return the string format
   */
  public synchronized static String formatEnabledDisabled(Timestamp timestamp) {
    return timestampFormat.format(timestamp);
  }
  
  /**
   * return the member
   * @return the member
   */
  public Member getMember() {
    return this.member;
  }
  
  /** the subject for this member */
  private GuiSubject guiSubject;
  
  /** if this subject is deletable (has an immediate membership) */
  private boolean deletable;

  /**
   * 
   * @return the subject
   */
  public GuiSubject getGuiSubject() {
    return this.guiSubject;
  }

  /**
   * subject
   * @param subject1
   */
  public void setGuiSubject(GuiSubject subject1) {
    this.guiSubject = subject1;
  }

  /**
   * if there is an immediate membership which can be deleted
   * @return if deletable
   */
  public boolean isDeletable() {
    return this.deletable;
  }

  /**
   * if this subject has an immediate membership
   * @param deletable1
   */
  public void setDeletable(boolean deletable1) {
    this.deletable = deletable1;
  }
  
}
