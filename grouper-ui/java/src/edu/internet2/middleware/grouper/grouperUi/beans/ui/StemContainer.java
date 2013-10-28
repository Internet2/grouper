package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDef;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiObjectBase;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;

/**
 * stem container in new ui
 * @author mchyzer
 *
 */
public class StemContainer {

  /**
   * filter text for the stem contents
   */
  private String filterText = null;

  /**
   * filter text
   * @return filter text
   */
  public String getFilterText() {
    return this.filterText;
  }

  /**
   * filter text
   * @param filterText1
   */
  public void setFilterText(String filterText1) {
    this.filterText = filterText1;
  }

  /**
   * groups, stems, etc in this stem which are children, only in the current page
   */
  private Set<GuiObjectBase> childGuiObjectsAbbreviated;

  /**
   * groups, stems, etc in this stem which are children, only in the current page
   * @return gui groups, stems, etc
   */
  public Set<GuiObjectBase> getChildGuiObjectsAbbreviated() {
    if (this.childGuiObjectsAbbreviated == null) {

      Stem stem = this.getGuiStem().getStem();

      Set<GuiObjectBase> tempObjects = new LinkedHashSet<GuiObjectBase>(); 
      
      {
        StemFinder stemFinder = new StemFinder()
          .assignQueryOptions(QueryOptions.create("extension", true, 1, 10))
          .assignPrivileges(AttributeDefPrivilege.VIEW_PRIVILEGES)
          .assignSubject(GrouperSession.staticGrouperSession().getSubject())
          .assignParentStemId(stem.getId()).assignStemScope(Scope.ONE);
        if (!StringUtils.isBlank(this.filterText)) {
          stemFinder.assignScope("%" + this.filterText);
          stemFinder.assignSplitScope(true);
        }
        Set<Stem> stems = stemFinder.findStems();
        tempObjects.addAll(GuiStem.convertFromStems(stems));
        
      }
      
      {
        GroupFinder groupFinder = new GroupFinder()
          .assignQueryOptions(QueryOptions.create("extension", true, 1, 10))
          .assignPrivileges(AttributeDefPrivilege.VIEW_PRIVILEGES)
          .assignSubject(GrouperSession.staticGrouperSession().getSubject())
          .assignParentStemId(stem.getId()).assignStemScope(Scope.ONE);
        if (!StringUtils.isBlank(this.filterText)) {
          groupFinder.assignScope("%" + this.filterText);
          groupFinder.assignSplitScope(true);
        }
        Set<Group> groups = groupFinder.findGroups();
        tempObjects.addAll(GuiGroup.convertFromGroups(groups));
      }
      
      {
        AttributeDefFinder attributeDefFinder = new AttributeDefFinder()
          .assignQueryOptions(QueryOptions.create("extension", true, 1, 10))
          .assignPrivileges(AttributeDefPrivilege.VIEW_PRIVILEGES)
          .assignSubject(GrouperSession.staticGrouperSession().getSubject())
          .assignParentStemId(stem.getId()).assignStemScope(Scope.ONE);
        
        if (!StringUtils.isBlank(this.filterText)) {
          attributeDefFinder.assignScope("%" + this.filterText);
          attributeDefFinder.assignSplitScope(true);
        }
        
        Set<AttributeDef> attributeDefSet = attributeDefFinder.findAttributes();
        
        tempObjects.addAll(GuiAttributeDef.convertFromAttributeDefs(attributeDefSet));
      }

      {
        AttributeDefNameFinder attributeDefNameFinder = new AttributeDefNameFinder()
          .assignQueryOptions(QueryOptions.create("displayExtension", true, 1, 10))
          .assignPrivileges(AttributeDefPrivilege.VIEW_PRIVILEGES)
          .assignSubject(GrouperSession.staticGrouperSession().getSubject())
          .assignStemScope(Scope.ONE)
          .assignParentStemId(stem.getId());

        if (!StringUtils.isBlank(this.filterText)) {
          attributeDefNameFinder.assignScope("%" + this.filterText);
          attributeDefNameFinder.assignSplitScope(true);
        }

        Set<AttributeDefName> attributeDefNameSet = attributeDefNameFinder.findAttributeNames();
        
        tempObjects.addAll(GuiAttributeDefName.convertFromAttributeDefNames(attributeDefNameSet));
        
      }
      this.childGuiObjectsAbbreviated = tempObjects;
    }
    return this.childGuiObjectsAbbreviated;
  }

  /**
   * gui stem shown on screen
   */
  private GuiStem guiStem;

  /**
   * gui stem shown on screen
   * @return stem
   */
  public GuiStem getGuiStem() {
    return this.guiStem;
  }

  /**
   * gui stem shown on screen
   * @param guiStem1
   */
  public void setGuiStem(GuiStem guiStem1) {
    this.guiStem = guiStem1;
  }

  
  
}
