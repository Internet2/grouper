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
/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.CompositeFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GroupContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver;
import edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolverFactory;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * Result of one group being retrieved since a user is a member of it.  The number of
 * groups will equal the number of groups the user is a member of (provided the filter matches)
 * 
 * @author mchyzer
 */
@SuppressWarnings("serial")
public class GuiGroup extends GuiObjectBase implements Serializable {

  /**
   * if the logged in user can invite external users to this group
   * @return true if the logged in user can invite external users for this group
   */
  public boolean isCanInviteExternalUsers() {

    boolean canInviteOthers = false;

    UIGroupPrivilegeResolver resolver = 
      UIGroupPrivilegeResolverFactory.getInstance(GrouperSession.staticGrouperSession(), 
          GrouperUiFilter.retrieveSessionMediaResourceBundle(), 
          this.group, GrouperSession.staticGrouperSession().getSubject());
    canInviteOthers = resolver.canInviteExternalPeople();
    
    //see if we can invite
    if (canInviteOthers && GrouperUiConfig.retrieveConfig().propertyValueBoolean(
        "inviteExternalPeople.link-from-lite-ui", false)) {

      return true;
    
    }

    return false;
  }
  
  /**
   * get a composite from the underlying group if there is one there
   * this is needed since there is a group.isComposite, so it conflicts
   * with the javabean group.getComposite()
   * @return the composite
   */
  public Composite getComposite() {
    Composite composite = this.group.getComposite(false);
    return composite;
  }
  
  /**
   * get lines that show if this group is a composite factor of other groups
   * @return the text
   */
  public String getCompositeFactorOfOtherGroupsText() {
    
    //get the composites
    Set<Composite> composites = CompositeFinder.findAsFactor(this.group);
    
    if (GrouperUtil.length(composites) == 0) {
      return TextContainer.retrieveFromRequest().getText().get("groupLabelThisGroupNotCompositeFactor");
    }

    StringBuilder result = new StringBuilder();
    boolean firstLine = true;
    for (Composite composite : composites) {
      
      try {
        if (!firstLine) {
          result.append("<br />\n");
        }
        
        Group theGroup = composite.getOwnerGroup();

        GuiGroup guiGroup = new GuiGroup(theGroup);
        result.append(guiGroup.getCompositeOwnerText());
        
      } catch (GroupNotFoundException gnfe) {
        return TextContainer.retrieveFromRequest().getText().get("groupLabelNotAllowedToViewAllGroups");
      }
      
      firstLine = false;
      
    }
    return result.toString();
  }
  
  /**
   * text about this group being a composite owner
   * @return text about this group being a composite owner
   */
  public String getCompositeOwnerText() {
    
    if (this.group.hasComposite()) {
      
      boolean allowed = true;

      final Subject subject = GrouperSession.staticGrouperSession().getSubject();
      
      allowed = allowed && this.group.canHavePrivilege(subject, AccessPrivilege.READ.getName(), false);      

      Composite composite = this.group.getComposite(false);
      
      Group leftGroup = null;
      Group rightGroup = null;
      
      if (composite == null || !allowed) {
        //cannot view
        allowed = false;
      } else {
      
        try {
          leftGroup = composite.getLeftGroup();
          
          rightGroup = composite.getRightGroup();
        } catch (GroupNotFoundException gnfe) {
          allowed = false;
        }
        
      }
      
      if (!allowed) {
        return TextContainer.retrieveFromRequest().getText().get("groupLabelNotAllowedToViewAllGroups");
      }
      
      //setup object model for text
      GroupContainer groupContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer();
      groupContainer.setCompositeOwnerGuiGroup(this);
      groupContainer.setCompositeLeftFactorGuiGroup(new GuiGroup(leftGroup));
      groupContainer.setCompositeRightFactorGuiGroup(new GuiGroup(rightGroup));

      switch(composite.getType()) {
        case COMPLEMENT:
          return TextContainer.retrieveFromRequest().getText().get("groupLabelCompositeMinus");
        case INTERSECTION:
          return TextContainer.retrieveFromRequest().getText().get("groupLabelCompositeIntersection");
        case UNION:
          return TextContainer.retrieveFromRequest().getText().get("groupLabelCompositeUnion");
          
      }
      
    } 
    return TextContainer.retrieveFromRequest().getText().get("groupLabelThisGroupNotComposite");
    
  }
  
  /**
   * comma separated privilege labels allowed by grouper all
   * @return the labels
   */
  public String getPrivilegeLabelsAllowedByGrouperAll() {
    StringBuilder results = new StringBuilder();
    boolean foundOne = false;
    
    if (this.isGrantAllAdmin()) {
      if (foundOne) {
        results.append(", ");
      }
      foundOne = true;
      results.append(TextContainer.retrieveFromRequest().getText().get("priv.adminUpper"));
    }
    if (this.isGrantAllUpdate()) {
      if (foundOne) {
        results.append(", ");
      }
      foundOne = true;
      results.append(TextContainer.retrieveFromRequest().getText().get("priv.updateUpper"));
    }
    if (this.isGrantAllRead()) {
      if (foundOne) {
        results.append(", ");
      }
      foundOne = true;
      results.append(TextContainer.retrieveFromRequest().getText().get("priv.readUpper"));
    }
    if (this.isGrantAllView()) {
      if (foundOne) {
        results.append(", ");
      }
      foundOne = true;
      results.append(TextContainer.retrieveFromRequest().getText().get("priv.viewUpper"));
    }
    if (this.isGrantAllOptin()) {
      if (foundOne) {
        results.append(", ");
      }
      foundOne = true;
      results.append(TextContainer.retrieveFromRequest().getText().get("priv.optinUpper"));
    }
    if (this.isGrantAllOptout()) {
      if (foundOne) {
        results.append(", ");
      }
      foundOne = true;
      results.append(TextContainer.retrieveFromRequest().getText().get("priv.optoutUpper"));
    }
    if (this.isGrantAllAttrUpdate()) {
      if (foundOne) {
        results.append(", ");
      }
      foundOne = true;
      results.append(TextContainer.retrieveFromRequest().getText().get("priv.groupAttrUpdateUpper"));
    }
    if (this.isGrantAllAttrRead()) {
      if (foundOne) {
        results.append(", ");
      }
      foundOne = true;
      results.append(TextContainer.retrieveFromRequest().getText().get("priv.groupAttrReadUpper"));
    }
    return results.toString();
  }
  
  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GuiGroup)) {
      return false;
    }
    return new EqualsBuilder()
      .append( this.group, ( (GuiGroup) other ).group )
      .isEquals();
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.group )
      .toHashCode();
  }


  /**
   * if the underlying group has a composite
   * @return true if has composite
   */
  public boolean isHasComposite() {
    return this.group.hasComposite();
  }
  
  /**
   * 
   * @param groups
   * @return groups
   */
  public static Set<GuiGroup> convertFromGroups(Set<Group> groups) {
    return convertFromGroups(groups, null, -1);
  }

  /**
   * 
   * @param groups
   * @param configMax
   * @param defaultMax
   * @return groups
   */
  public static Set<GuiGroup> convertFromGroups(Set<Group> groups, String configMax, int defaultMax) {
    Set<GuiGroup> tempGroups = new LinkedHashSet<GuiGroup>();
    
    Integer max = null;
    
    if (!StringUtils.isBlank(configMax)) {
      max = GrouperUiConfig.retrieveConfig().propertyValueInt(configMax, defaultMax);
    }
    
    int count = 0;
    for (Group group : GrouperUtil.nonNull(groups)) {
      tempGroups.add(new GuiGroup(group));
      if (max != null && ++count >= max) {
        break;
      }
    }
    
    return tempGroups;
    
  }

  
  /**
   * &lt;a href="#" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&amp;lt;strong&amp;gt;FOLDER:&amp;lt;/strong&amp;gt;&amp;lt;br /&amp;gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.">Editors</a>
   * @return short link
   */
  public String getShortLink() {
    
    return shortLinkHelper(false, false);
  }
  
  /**
   * display short link with image next to it in li
   * &lt;a href="#" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&amp;lt;strong&amp;gt;FOLDER:&amp;lt;/strong&amp;gt;&amp;lt;br /&amp;gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.">Editors</a>
   * @return short link
   */
  public String getShortLinkWithIcon() {
    
    return shortLinkHelper(true, false);
  }

  /**
   * display short link with image next to it in li and the path info below it
   * &lt;a href="#" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&amp;lt;strong&amp;gt;FOLDER:&amp;lt;/strong&amp;gt;&amp;lt;br /&amp;gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.">Editors</a>
   * @return short link
   */
  public String getShortLinkWithIconAndPath() {
    
    return shortLinkHelper(true, true);
  }

  /**
   * 
   * @param showIcon
   * @param showPath 
   * @return the link
   */
  private String shortLinkHelper(boolean showIcon, boolean showPath) {
    
    if (this.group == null) {
      //TODO put icon here?
      return TextContainer.retrieveFromRequest().getText().get("guiObjectUnknown");
    }
    
    GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiGroup(this);
    GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowIcon(showIcon);
    GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowPath(showPath);
    
    try {
      
      String result = TextContainer.retrieveFromRequest().getText().get("guiGroupShortLink");
      return result;
      
    } finally {

      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiGroup(null);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowIcon(false);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowPath(false);

    }

  }

  /**
   * &lt;a href="#" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&amp;lt;strong&amp;gt;FOLDER:&amp;lt;/strong&amp;gt;&amp;lt;br /&amp;gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.">Editors</a>
   * @return short link
   */
  public String getLink() {
    
    return linkHelper(false);
  }
  
  /**
   * display short link with image next to it in li
   * &lt;a href="#" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&amp;lt;strong&amp;gt;FOLDER:&amp;lt;/strong&amp;gt;&amp;lt;br /&amp;gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.">Editors</a>
   * @return short link
   */
  public String getLinkWithIcon() {
    
    return linkHelper(true);
  }

  /**
   * 
   * @param showIcon
   * @return the link
   */
  private String linkHelper(boolean showIcon) {
    
    if (this.group == null) {
      //TODO put icon here?
      return TextContainer.retrieveFromRequest().getText().get("guiObjectUnknown");
    }
    
    GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiGroup(this);
    GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowIcon(showIcon);
    
    try {
      
      String result = TextContainer.retrieveFromRequest().getText().get("guiGroupLink");
      return result;
      
    } finally {

      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setGuiGroup(null);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setShowIcon(false);

    }

  }

  
  /** group */
  private Group group;
  
  /** if there is an external config url */
  private String configUrl;
  
  /** see if has a config url */
  private Boolean hasMembershipConfigUrl = null;
  
  /**
   * config url if set
   * @return the config url if set
   */
  public String getMembershipConfigUrl() {
    
    if (this.hasMembershipConfigUrl == null) {
      
      if (this.group == null) {

        //sidestep
        return null;
      }

      //default to false
      this.hasMembershipConfigUrl = false;
      
      if (GrouperUiConfig.retrieveConfig().propertyValueBoolean("simpleMembershipUpdate.allowExternalUrlProperties", false)) {
        
        final GroupType groupType = GroupTypeFinder.find("grouperGroupMembershipSettings", false);
        
        if (groupType != null && GuiGroup.this.group.hasType(groupType)) {
          this.configUrl = GuiGroup.this.group.getAttributeOrFieldValue("grouperGroupMshipSettingsUrl", false, false);
          this.hasMembershipConfigUrl = !StringUtils.isBlank(this.configUrl);
        }
      }
    }
    
    return this.hasMembershipConfigUrl ? this.configUrl : null;
  }

  /**
   * return the group
   * @return the group
   */
  public Group getGroup() {
    return this.group;
  }

  /**
   * 
   */
  public GuiGroup() {
    
  }
  
  /**
   * 
   * @param theGroup
   */
  public GuiGroup(Group theGroup) {
    this.group = theGroup;
  }
  
  /**
   * the export subject ids file name
   * @return the export subject ids file name
   */
  public String getExportSubjectIdsFileName() {
    return getExportSubjectIdsFileNameStatic(this.group);
  }
  
  /**
   * static logic
   * @param group
   * @return the file name
   */
  public static String getExportSubjectIdsFileNameStatic(Group group) {
    String groupExtensionFileName = group.getDisplayExtension();
    
    groupExtensionFileName = GrouperUiUtils.stripNonFilenameChars(groupExtensionFileName);
    
    return "groupExportSubjectIds_" + groupExtensionFileName + ".csv";
 
  }
  
  /**
   * the export all file name
   * @return the export subject ids file name
   */
  public String getExportAllFileName() {
    return getExportAllFileNameStatic(this.group);
  }
  
  /**
   * static logic
   * @param group
   * @return the file name
   */
  public static String getExportAllFileNameStatic(Group group) {
    String groupExtensionFileName = group.getDisplayExtension();
    
    groupExtensionFileName = GrouperUiUtils.stripNonFilenameChars(groupExtensionFileName);
    
    return "groupExportAll_" + groupExtensionFileName + ".csv";
 
  }
  
  /**
   * @see GuiObjectBase#getGrouperObject()
   */
  @Override
  public GrouperObject getGrouperObject() {
    return this.group;
  }

  /**
   * if the group has admin granted to all
   * @return true
   */
  public boolean isGrantAllAdmin() {
    return this.group.hasAdmin(SubjectFinder.findAllSubject());
  }


  /**
   * if the logged in user can update (or inherit), dont check security
   * @return true
   */
  public boolean isCanUpdate() {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    return this.group.canHavePrivilege(loggedInSubject, AccessPrivilege.UPDATE.getName(), false);

  }

  /**
   * if the logged in user has update
   * @return true
   * @deprecated use isCanUpdate instead probably
   */
  @Deprecated
  public boolean isHasUpdate() {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    return this.group.hasUpdate(loggedInSubject);

  }


  /**
   * if the group has update granted to all
   * @return true
   */
  public boolean isGrantAllUpdate() {
    return this.group.hasUpdate(SubjectFinder.findAllSubject());
  }

  /**
   * if the group has read granted to all
   * @return true
   */
  public boolean isGrantAllRead() {
    return this.group.hasRead(SubjectFinder.findAllSubject());
  }

  /**
   * if the group has view granted to all
   * @return true
   */
  public boolean isGrantAllView() {
    return this.group.hasView(SubjectFinder.findAllSubject());
  }

  /**
   * if the group has optin granted to all
   * @return true
   */
  public boolean isGrantAllOptin() {
    return this.group.hasOptin(SubjectFinder.findAllSubject());
  }


  /**
   * if the group has optout granted to all
   * @return true
   */
  public boolean isGrantAllOptout() {
    return this.group.hasOptout(SubjectFinder.findAllSubject());
  }


  /**
   * if the group has attr read granted to all
   * @return true
   */
  public boolean isGrantAllAttrRead() {
    return this.group.hasGroupAttrRead(SubjectFinder.findAllSubject());
  }


  /**
   * if the group has attr update granted to all
   * @return true
   */
  public boolean isGrantAllAttrUpdate() {
    return this.group.hasGroupAttrUpdate(SubjectFinder.findAllSubject());
  }

  /**
   * test if an attribute GrouperLoaderLdap is assigned to this group
   * @return true if an attribute GrouperLoaderLdap is assigned.
   * return false if not
   */
  public boolean isHasAttrDefNameGrouperLoaderLdap() {

    boolean hasAttrLdap = false;
    
    //first, get the attribute def name
    AttributeDefName grouperLoaderLdapName = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure(LoaderLdapUtils.grouperLoaderLdapName(), false);
    
    //check if the attribute def name is assigned to this group
    if (grouperLoaderLdapName != null) {
      hasAttrLdap = this.group.getAttributeDelegate().hasAttribute(grouperLoaderLdapName);
    }
    
    return hasAttrLdap;
  }

  /**
   * test if an attribute GrouperLoader SQL is assigned to this group
   * @return true if an attribute GrouperLoader SQL is assigned.
   * return false if not
   */
  public boolean isHasAttrDefNameGrouperLoader() {
    boolean hasAttrLdap = false;

    //first, get the attribute def name
    AttributeDefName grouperLoader = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure(GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":legacy:attribute:legacyGroupType_grouperLoader", false);
    
    //check if the attribute def name is assigned to this group
    if (grouperLoader != null) {
      hasAttrLdap = this.group.getAttributeDelegate().hasAttribute(grouperLoader);
    }
    
    return hasAttrLdap;
  }
  
  public boolean isTypeRole() {
    return this.group.getTypeOfGroup() == TypeOfGroup.role;
  }
}