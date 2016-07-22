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
 * $Id: GuiSubject.java,v 1.2 2009-10-11 22:04:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.entity.EntitySourceAdapter;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.subj.GrouperSubject;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.subj.UnresolvableSubject;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperEmailUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;


/**
 * subject for gui has all attributes etc, and getter to be accessed from screen
 */
@SuppressWarnings("serial")
public class GuiSubject extends GuiObjectBase implements Serializable {

  /**
   * return source id two pipes and subject id
   * @return the source id two pipes and subject id
   */
  public String getSourceIdSubjectId() {
    if (this.subject == null) {
      return null;
    }
    return this.subject.getSourceId() + "||" + this.subject.getId();
  }
  
  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GuiSubject)) {
      return false;
    }
    return SubjectHelper.eq(this.subject, ((GuiSubject)other).subject);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return SubjectHelper.hashcode(this.subject);
  }

  /**
   * get the member id of the subject or null if not there
   * @return the member id if exists or null if not
   */
  public String getMemberId() {
    
    if (this.subject != null) {
      GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
      
      //when converting json this is null, so dont do a query if just doing json beans
      if (grouperSession != null) {
        Member member = MemberFinder.findBySubject(grouperSession, this.getSubject(), false);
        if (member != null) {
          return member.getId();
        }
      }
    }
    return null;
  }
  
  /**
   * if the gui subject has an email address
   * @return true if the subject has email
   */
  public boolean isHasEmailAttributeInSource() {
    if (this.subject == null) {
      return false;
    }
    //if there is an email attribute in the source, then this is true
    return !StringUtils.isBlank(GrouperEmailUtils.emailAttributeNameForSource(this.subject.getSourceId()));
  }

  /**
   * get the email attribute value
   * @return the email or null or blank if not there
   */
  public String getEmail() {
    if (this.subject == null) {
      return null;
    }
    String emailAttributeName = GrouperEmailUtils.emailAttributeNameForSource(this.subject.getSourceId());
    if (StringUtils.isBlank(emailAttributeName)) {
      return null;
    }
    return this.subject.getAttributeValue(emailAttributeName);
  }
  
  /**
   * 
   * @param subjects
   * @return subjects
   */
  public static Set<GuiSubject> convertFromSubjects(Set<Subject> subjects) {
    return convertFromSubjects(subjects, null, -1);
  }


  /**
   * 
   * @param subjects
   * @return gui subjects
   */
  public static Set<GuiSubject> convertFromSubjects(Set<Subject> subjects, String configMax, int defaultMax) {

    Set<GuiSubject> tempSubjects = new LinkedHashSet<GuiSubject>();
    
    Integer max = null;
    
    if (!StringUtils.isBlank(configMax)) {
      max = GrouperUiConfig.retrieveConfig().propertyValueInt(configMax, defaultMax);
    }
    
    int count = 0;
    for (Subject subject : GrouperUtil.nonNull(subjects)) {
      tempSubjects.add(new GuiSubject(subject));
      if (max != null && ++count >= max) {
        break;
      }
    }
    
    return tempSubjects;
    
  }

  /**
   * see if group or not
   * @return if group
   */
  public boolean isGroup() {
    if (this.subject == null) {
      return false;
    }
    return StringUtils.equals(SubjectFinder.internal_getGSA().getId(), this.subject.getSourceId());
  }
  
  /** subject */
  private Subject subject;

  /**
   * e.g. &lt;a href="#"&gt;John Smith&lt;/a&gt;
   * @return short link
   */
  public String getShortLink() {
    this.initScreenLabels();
    return this.screenLabelShort2html;
  }
  
  /**
   * e.g. &lt;a href="#"&gt;John Smith&lt;/a&gt;
   * @return short link
   */
  public String getShortLinkWithIcon() {
    this.initScreenLabels();
    return this.screenLabelShort2htmlWithIcon;
  }

  /**
   * e.g. &lt;a href="#"&gt;John Smith&lt;/a&gt;
   * @return short link
   */
  public String getScreenLabelShort2noLink() {
    this.initScreenLabels();
    return this.screenLabelShort2noLink;
  }

  /**
   * some source Id that isnt a normal grouper one
   */
  private static String sourceId = null;
  
  /**
   * 
   * @return a source id
   */
  public static String someSourceId() {
    if (sourceId == null) {
      synchronized (GuiSubject.class) {
        if (sourceId == null) {
          //pick one at random?
          String theSourceId = "g:gsa";
          
          for (Source source : SourceManager.getInstance().getSources()) {
            if (!"g:gsa".equals(source.getId())
                && !"grouperEntities".equals(source.getId())
                && !"grouperExternal".equals(source.getId())
                && !"g:isa".equals(source.getId())) {
              theSourceId = source.getId();
              break;
            }
          }
          sourceId = theSourceId;
          
        }
      }
    }
    return sourceId;
  }
  
  /**
   * init screen labels
   */
  private void initScreenLabels() {
    
    if (this.subject == null) {

      //unresolvable
      this.subject = new UnresolvableSubject("", null, someSourceId());
      
    }
    
    if (this.screenLabelLong == null && this.screenLabelShort == null) {
      
      boolean convertToUnresolvableSubject = false;
      String unresolvableSubjectString = TextContainer.retrieveFromRequest().getText().get("guiUnresolvableSubject");
      
      if (this.subject instanceof UnresolvableSubject) {
        if (!StringUtils.equals(unresolvableSubjectString, ((UnresolvableSubject)this.subject).getUnresolvableString())) {
          //we want to use the unresolvable string from the externalized text file in the UI
          convertToUnresolvableSubject = true;
        }
        
        //convert from lazy subject error to an unresolvable
      } else if (this.subject != null && this.subject.getName() != null && this.subject.getName().contains(" entity not found")) {
        convertToUnresolvableSubject = true;
      }

      if (convertToUnresolvableSubject) {
        
        this.subject = new UnresolvableSubject(this.subject.getId(), this.subject.getTypeName(), this.subject.getSourceId(), unresolvableSubjectString);

      }

      boolean isUnresolvableGroup = false;
      
      
      if (this.subject instanceof UnresolvableSubject) {
        if (StringUtils.equals(GrouperSourceAdapter.groupSourceId(), this.subject.getSourceId())
            || StringUtils.equals(EntitySourceAdapter.entitySourceId(), this.subject.getSourceId())) {
          
          isUnresolvableGroup = true;
          
          GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setSubjectId(this.subject.getId());
          String extensionName = null;
          if (StringUtils.equals(GrouperSourceAdapter.groupSourceId(), this.subject.getSourceId())) {
            extensionName = TextContainer.retrieveFromRequest().getText().get("guiGroupCantView");
          } else {
            extensionName = TextContainer.retrieveFromRequest().getText().get("guiEntityCantView");
          }
          GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setSubjectId(null);

          //the user does not have view on this group or entity!
          Map<String, Set<String>> subjectAttributes = this.subject.getAttributes();
          subjectAttributes.put("displayExtension", GrouperUtil.toSet(StringUtils.abbreviate(extensionName, 33)));
          subjectAttributes.put("displayName", GrouperUtil.toSet(extensionName));
          ((UnresolvableSubject) this.subject).setDescription(extensionName);
          ((UnresolvableSubject) this.subject).setAttributes(subjectAttributes);
        }
      }
      
      String screenLabel = GrouperUiUtils.convertSubjectToLabelLong(this.subject);
            
      this.screenLabelLong = screenLabel;
      
      screenLabel = GrouperUiUtils.convertSubjectToLabel(this.subject);
      
      int maxWidth = GrouperUiConfig.retrieveConfig().propertyValueInt("subject.maxChars", 100);
      if (maxWidth == -1) {
        this.screenLabelShort = screenLabel;
      } else {
        this.screenLabelShort = StringUtils.abbreviate(screenLabel, maxWidth);
      }

      screenLabel = GrouperUiUtils.convertSubjectToLabelHtmlConfigured2(this.subject);
      
      maxWidth = GrouperUiConfig.retrieveConfig().propertyValueInt("subject2.maxChars", 40);
      if (maxWidth == -1) {
        this.screenLabelShort2 = screenLabel;
      } else {
        this.screenLabelShort2 = StringUtils.abbreviate(screenLabel, maxWidth);
      }
      boolean hasTooltip = this.subject != null 
        && !StringUtils.isBlank(this.subject.getDescription()) && !StringUtils.equals(this.subject.getName(), this.subject.getDescription());
      
      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowTooltip(hasTooltip);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiSubject(this);
      
      try {
        
        //"<a href=\"view-subject.html\" rel=\"tooltip\" data-html=\"true\" data-delay-show=\"200\" data-placement=\"right\" title=\"" + GrouperUtil.xmlEscape(subject.getDescription(), true)  + "\">" + this.screenLabelShort2 + "</a>";
        //"<a href=\"view-subject.html\" data-html=\"true\" data-delay-show=\"200\" data-placement=\"right\">" + this.screenLabelShort2 + "</a>";
        Group group = null;
        
        if (this.isGroup()) {
          if (this.subject instanceof GrouperSubject) {
            group = ((GrouperSubject)this.subject).internal_getGroup();
          }
          if (group == null) {
            group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), this.subject.getId(), false);
          }
          if (group == null) {
            group = new Group();
            group.setId(this.subject.getId());
            String cantFindGroupName = this.subject.getAttributeValue("displayName");
            String cantFindGroupExtension = this.subject.getAttributeValue("displayExtension");
            group.setNameDb(cantFindGroupName);
            group.setDisplayNameDb(cantFindGroupName);
            group.setExtensionDb(cantFindGroupExtension);
            group.setDisplayExtensionDb(cantFindGroupExtension);
            group.setDescriptionDb(cantFindGroupName);
          }
        }
        
        if (group != null) {
          GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiGroup(new GuiGroup(group));
          this.screenLabelShort2html = TextContainer.retrieveFromRequest().getText().get("guiGroupShortLink");
        } else {
          
          this.screenLabelShort2html = TextContainer.retrieveFromRequest().getText().get("guiSubjectShortLink");
        }
        
        GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowIcon(true);

        if (group != null) {
          this.screenLabelLongWithIcon = TextContainer.retrieveFromRequest().getText().get("guiGroupLongLinkWithIcon");
        } else {
          this.screenLabelLongWithIcon = TextContainer.retrieveFromRequest().getText().get("guiSubjectLongLinkWithIcon");
        }
        

        if (group != null) {
          this.screenLabelShort2htmlWithIcon = TextContainer.retrieveFromRequest().getText().get("guiGroupShortLink");
        } else {
          this.screenLabelShort2htmlWithIcon = TextContainer.retrieveFromRequest().getText().get("guiSubjectShortLink");
        }
        
        if (group != null) {
          this.screenLabelShort2noLinkWithIcon = TextContainer.retrieveFromRequest().getText().get("guiGroupShort");
        } else {
          this.screenLabelShort2noLinkWithIcon = TextContainer.retrieveFromRequest().getText().get("guiSubjectShort");
        }
        
        GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowIcon(false);

        if (group != null) {
          this.screenLabelShort2noLink = TextContainer.retrieveFromRequest().getText().get("guiGroupShort");
        } else {
          this.screenLabelShort2noLink = TextContainer.retrieveFromRequest().getText().get("guiSubjectShort");
        }

      } finally {

        GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiSubject(null);
        GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowTooltip(false);
        GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowIcon(false);

      }

    }
  }

  /**
   * short screen label for ui v2
   * @return label
   */
  public String getScreenLabelShort2() {
    this.initScreenLabels();
    return this.screenLabelShort2;
  }

  /**
   * construct with subject
   * @param subject1
   */
  public GuiSubject(Subject subject1) {
    this.subject = subject1;
  }
  
  /**
   * get screen label
   * @return screen label
   */
  public String getScreenLabel() {
    this.initScreenLabels();
    return this.screenLabelShort;
  }
  
  /**
   * get screen label
   * @return screen label
   */
  public String getScreenLabelLong() {
    this.initScreenLabels();
    return this.screenLabelLong;
  }
  
  /** attributes in string - string format */
  private Map<String, String> attributes = null;
  /**
   * long screen label
   */
  private String screenLabelLong = null;
  /**
   * long screen label with icon
   */
  private String screenLabelLongWithIcon = null;

  /**
   * long screen label with icon
   * @return screen label
   */
  public String getScreenLabelLongWithIcon() {
    this.initScreenLabels();
    return this.screenLabelLongWithIcon;
  }

  /**
   * short screen label
   */
  private String screenLabelShort = null;

  /**
   * new short label in v2
   */
  private String screenLabelShort2 = null;
  
  /**
   * new short label in v2 with html tooltip and link
   */
  private String screenLabelShort2html = null;
  
  /**
   * new short label in v2 with html tooltip and link and icon
   */
  private String screenLabelShort2htmlWithIcon = null;
  
  /**
   * new short label in v2 with no link, but with icon
   */
  private String screenLabelShort2noLinkWithIcon = null;

  /**
   * new short label in v2 with no link or icon
   */
  private String screenLabelShort2noLink = null;

  
  /**
   * subject
   * @return the subject
   */
  public Subject getSubject() {
    return this.subject;
  }
  
  /**
   * attribute names for this subject
   * @return the attribute names for this subject
   */
  public Set<String> getAttributeNamesNonInternal() {
    Set<String> attributeNames = new LinkedHashSet<String>();
    if (this.subject != null) {
      String emailAttributeName = GrouperEmailUtils.emailAttributeNameForSource(this.subject.getSourceId());
  
      for (String attributeName : GrouperUtil.nonNull(this.getAttributes().keySet())) {
        if (!StringUtils.equalsIgnoreCase("name", attributeName)
            && !StringUtils.equalsIgnoreCase("description", attributeName)
            && !StringUtils.equalsIgnoreCase("subjectId", attributeName)
            && !StringUtils.equalsIgnoreCase(emailAttributeName, attributeName)) {
          attributeNames.add(attributeName);
        }
      }
    }
    return attributeNames;
  }
  
  /**
   * attribute names for this subject to show in the expanded view
   * @return the attribute names for this subject
   */
  public Set<String> getAttributeNamesExpandedView() {
    Set<String> attributeNames = new LinkedHashSet<String>();
    if (this.subject != null) {
      String orderCommaSeparated = GrouperUiConfig.retrieveConfig().propertyValueString("subject2.attributes.order.expanded." + this.subject.getSourceId());
      if (GrouperUtil.isBlank(orderCommaSeparated)) {
        orderCommaSeparated = GrouperUiConfig.retrieveConfig().propertyValueString("subject2.attributes.order.expanded.default");
      }
      
      // still empty, return them all to preserve previous behavior
      if (GrouperUtil.isBlank(orderCommaSeparated)) {
        return getAttributeNamesNonInternal();
      }
      
      attributeNames.addAll(GrouperUtil.splitTrimToSet(orderCommaSeparated, ","));
    }
    return attributeNames;
  }
  
  
  /**
   * attribute names for this subject to show in the non-expanded view
   * @return the attribute names for this subject
   */
  public Set<String> getAttributeNamesNonExpandedView() {
    Set<String> attributeNames = new LinkedHashSet<String>();
    if (this.subject != null) {
      String orderCommaSeparated = GrouperUiConfig.retrieveConfig().propertyValueString("subject2.attributes.order.nonexpanded." + this.subject.getSourceId());
      if (GrouperUtil.isBlank(orderCommaSeparated)) {
        orderCommaSeparated = GrouperUiConfig.retrieveConfig().propertyValueString("subject2.attributes.order.nonexpanded.default");
      }
      
      if (!GrouperUtil.isBlank(orderCommaSeparated)) {
        attributeNames.addAll(GrouperUtil.splitTrimToSet(orderCommaSeparated, ","));
      }
    }
    return attributeNames;
  }

  /**
   * dynamic map of attribute name to attribute label
   */
  private Map<String, String> attributeLabelMap = new HashMap<String, String>() {

    /**
     * @see java.util.HashMap#get(java.lang.Object)
     */
    @Override
    public String get(Object attributeName) {
      
      String sourceId = GuiSubject.this.getSubject().getSourceId();
      String sourceTextId = GrouperUiUtils.convertSourceIdToTextId(sourceId);
      
      String emailAttribute = GrouperEmailUtils.emailAttributeNameForSource(GuiSubject.this.subject.getSourceId());
      
      // subjectViewLabel__sourceTextId__attributeName
      String key = "subjectViewLabel__" + sourceTextId + "__" + attributeName;
      
      if ("sourceId".equals(attributeName)) {
        key = "subjectViewLabelSourceId";
      } else if ("sourceName".equals(attributeName)) {
        key = "subjectViewLabelSourceName";
      } else if ("memberId".equals(attributeName)) {
        key = "subjectViewLabelMemberId";
      } else if ("subjectId".equals(attributeName)) {
        key = "subjectViewLabelId";
      } else if ("name".equals(attributeName)) {
        key = "subjectViewLabelName";
      } else if ("description".equals(attributeName)) {
        key = "subjectViewLabelDescription";
      } else if (!GrouperUtil.isBlank(emailAttribute) && emailAttribute.equals(attributeName)) {
        key = "subjectViewLabelEmail";
      }
      
      String value = TextContainer.textOrNull(key);
      
      if (StringUtils.isBlank(value)) {
        return ((String)attributeName) + ":";
      }
      return value;
      
    }

  };

  
  /**
   * attribute label for this attribute if configured
   * first get the text id for the source, then look in the externalized text
   * for a label for the attribute, if not there, just use the attribute name
   * @return the attribute label for this attribute
   */
  public Map<String, String> getAttributeLabel() {
    
    return this.attributeLabelMap;
    
  }
  
  /**
   * Gets a map attribute names and value. The map's key
   * contains the attribute name and the map's value
   * contains a Set of attribute value(s).  Note, this only does single valued attributes
   * @return the map of attributes
   */
  @SuppressWarnings({ "cast", "unchecked" })
  public Map<String, String> getAttributes() {
    if (this.attributes == null) {
      Map<String, String> result = new LinkedHashMap<String, String>();
      
      if (this.subject != null) {
        for (String key : (Set<String>)(Object)GrouperUtil.nonNull(this.subject.getAttributes()).keySet()) {
          Object value = this.subject.getAttributes().get(key);
          if (value instanceof String) {
            //if a string
            result.put(key, (String)value);
          } else if (value instanceof Set) {
            //if set of one string, then add it
            if (((Set<?>)value).size() == 1) {
              result.put(key, (String)((Set<?>)value).iterator().next());
            } else if (((Set<?>)value).size() > 1) {
              //put commas in between?  not sure what else to do here
              result.put(key, GrouperUtil.setToString((Set<?>)value));
            }
          }
        }
      }
      
      result.put("memberId", getMemberId());
      result.put("sourceId", this.subject.getSourceId());
      result.put("sourceName", this.subject.getSource().getName());
      result.put("subjectId", this.subject.getId());
      result.put("name", this.subject.getName());
      result.put("description", this.subject.getDescription());
      
      this.attributes = result;
    }
    return this.attributes;
  }

  /**
   * 
   * @return long label if different than the short one
   */
  public String getScreenLabelLongIfDifferent() {
    this.initScreenLabels();
    if (this.isNeedsTooltip()) {
      return this.screenLabelLong;
    }
    return null;
  }

  /**
   * get short screen label 
   * @return short screen label
   */
  public boolean isNeedsTooltip() {
    this.initScreenLabels();
    return !StringUtils.equals(this.screenLabelLong, this.screenLabelShort);
  }


  /**
   * e.g. &lt;a href="#"&gt;John Smith&lt;/a&gt;
   * @return short link
   */
  public String getScreenLabelShort2noLinkWithIcon() {
    this.initScreenLabels();
    return this.screenLabelShort2noLinkWithIcon;
  }


  /**
   * 
   * @param subject
   * @param attrName
   * @return the value
   */
  public static String attributeValue(Subject subject, String attrName) {
    if (StringUtils.equalsIgnoreCase("screenLabel", attrName)) {
      return GrouperUiUtils.convertSubjectToLabel(subject);
    }
    if (subject == null) { 
      return null;
    }
    if (StringUtils.equalsIgnoreCase("subjectId", attrName)) {
      return subject.getId();
    }
    if (StringUtils.equalsIgnoreCase("name", attrName)) {
      return subject.getName();
    }
    if (StringUtils.equalsIgnoreCase("description", attrName)) {
      return subject.getDescription();
    }
    if (StringUtils.equalsIgnoreCase("typeName", attrName)) {
      return subject.getType().getName();
    }
    if (StringUtils.equalsIgnoreCase("sourceId", attrName)) {
      return subject.getSource().getId();
    }
    if (StringUtils.equalsIgnoreCase("sourceName", attrName)) {
      return subject.getSource().getName();
    }
    //TODO switch this to attribute values comma separated
    return subject.getAttributeValue(attrName);
  }

  /**
   * cant get grouper object
   */
  @Override
  public GrouperObject getGrouperObject() {
    return null;
  }

  /**
   * if this is a subject
   */
  @Override
  public boolean isSubjectType() {
    return true;
  }

  /**
   * path colon separated not applicable
   */
  @Override
  public String getPathColonSpaceSeparated() {
    return "";
  }

  /**
   * not applicable
   */
  @Override
  public String getNameColonSpaceSeparated() {
    return this.getScreenLabelShort2noLink();
  }

  /**
   * not applicable
   */
  @Override
  public String getTitle() {
    return "";
  }
  
}
