package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiObjectBase;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperObjectFinder;
import edu.internet2.middleware.grouper.misc.GrouperObjectFinder.ObjectPrivilege;

/**
 * stem container in new ui
 * @author mchyzer
 *
 */
public class StemContainer {

  /**
   * keep track of the paging on the stem screen
   */
  private GuiPaging guiPaging = null;
  
  /**
   * keep track of the paging on the stem screen
   * @return the paging object, init if not there...
   */
  public GuiPaging getGuiPaging() {
    if (this.guiPaging == null) {
      this.guiPaging = new GuiPaging();
    }
    return this.guiPaging;
  }

  
  public void setGuiPaging(GuiPaging guiPaging) {
    this.guiPaging = guiPaging;
  }

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
      int pageSize = this.getGuiPaging().getPageSize();
      int pageNumber = this.getGuiPaging().getPageNumber();
      QueryOptions queryOptions = QueryOptions.create("displayExtension", true, pageNumber, pageSize);
      
      GrouperObjectFinder grouperObjectFinder = new GrouperObjectFinder()
        .assignObjectPrivilege(ObjectPrivilege.view)
        .assignParentStemId(stem.getId())
        .assignQueryOptions(queryOptions)
        .assignSplitScope(true).assignStemScope(Scope.ONE)
        .assignSubject(GrouperSession.staticGrouperSession().getSubject());
 
      if (!StringUtils.isBlank(this.filterText)) {
        grouperObjectFinder.assignFilterText(this.filterText);
      }

      Set<GrouperObject> results = grouperObjectFinder.findGrouperObjects();
      
      this.childGuiObjectsAbbreviated = GuiObjectBase.convertFromGrouperObjects(results);
      
      this.getGuiPaging().setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
      
      
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
