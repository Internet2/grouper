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
package edu.internet2.middleware.grouper.ui.tags;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * paging tag for ui v2
 * @author mchyzer
 *
 */
public class GrouperPagingTag2 extends SimpleTagSupport {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperPagingTag2.class);
  
  /**
   * unique name for form
   */
  private String formName;
  
  /**
   * gui paging object
   */
  private GuiPaging guiPaging;
  
  /**
   * url for ajax to go to
   */
  private String refreshOperation;
  
  /**
   * unique name for form
   * @return the form name
   */
  public String getFormName() {
    return this.formName;
  }

  /**
   * unique name for form
   * @param formName1
   */
  public void setFormName(String formName1) {
    this.formName = formName1;
  }

  /**
   * gui paging object
   * @return the gui paging object
   */
  public GuiPaging getGuiPaging() {
    return this.guiPaging;
  }

  /**
   * gui paging object
   * @param guiPaging1
   */
  public void setGuiPaging(GuiPaging guiPaging1) {
    this.guiPaging = guiPaging1;
  }

  /**
   * url for ajax to go to
   * @return url for ajax
   */
  public String getRefreshOperation() {
    return this.refreshOperation;
  }

  /**
   * url for ajax to go to
   * @param ajaxUrl1
   */
  public void setRefreshOperation(String ajaxUrl1) {
    this.refreshOperation = ajaxUrl1;
  }

  /**
   * if there are more ajax form ids to submit, put them here
   */
  private String ajaxFormIds;
  
  /**
   * if there are more ajax form ids to submit, put them here
   * @return ajax form ids
   */
  public String getAjaxFormIds() {
    return this.ajaxFormIds;
  }
  
  /**
   * if there are more ajax form ids to submit, put them here
   * @param ajaxFormIds1
   */
  public void setAjaxFormIds(String ajaxFormIds1) {
    this.ajaxFormIds = ajaxFormIds1;
  }

  /**
   * 
   */
  public GrouperPagingTag2() {
  }

  /**
   * tag logic
   */
  @Override
  public void doTag() throws JspException, IOException {
    
    StringBuilder result = new StringBuilder();
    
    //<div class="data-table-bottom gradient-background">
    //  <div class="pull-right">Showing 1-10 of 25 &middot; <a href="#">First</a> | <a href="#">Prev</a> | <a href="#">Next</a> | <a href="#">Last</a></div>
    //  <form class="form-inline form-small">
    //    <label for="show-entries">Show:&nbsp;</label>
    //    <select id="show-entries" class="span2">
    //      <option>10</option>
    //      <option>25</option>
    //      <option>50</option>
    //      <option>100</option>
    //    </select>
    //  </form>
    //</div>

    result.append("<div class=\"data-table-bottom gradient-background\">\n");
    
    QueryPaging queryPaging = this.guiPaging.queryPaging();
    
    result.append("  <div class=\"pull-right\">Showing " + (queryPaging.getFirstIndexOnPage()+1) 
        + "-" + (queryPaging.getLastIndexOnPage()+1) + " of " + this.guiPaging.getTotalRecordCount() + " &middot; ");

    String javascriptEventPrefix = "ajax('";
    String javascriptEventSuffix = "', {formIds: '" + this.formName + "Id" + (StringUtils.isBlank(this.ajaxFormIds) ? "" : ("," + this.ajaxFormIds)) + "'}); return false;";

    if (queryPaging.isFirstPage()) {
      result.append("First | Prev");
    } else {
      result.append("<a href=\"#\" onclick=\"" + javascriptEventPrefix + this.refreshOperation + javascriptEventSuffix + "\">First</a> | ");
      result.append("<a href=\"#\" onclick=\"" + javascriptEventPrefix 
          + appendToUrl(this.refreshOperation, "pagingTagPageNumber=" + (queryPaging.getPageNumber()-1) ) + javascriptEventSuffix + "\">Prev</a>");
    }
    result.append(" | ");
    if (queryPaging.isLastPage()) {
      result.append("Next | Last");
    } else {
      result.append("<a href=\"#\" onclick=\"" + javascriptEventPrefix 
          + appendToUrl(this.refreshOperation, "pagingTagPageNumber=" + (queryPaging.getPageNumber()+1) ) + javascriptEventSuffix + "\">Next</a>");
      result.append(" | ");
      result.append("<a href=\"#\" onclick=\"" + javascriptEventPrefix 
          + appendToUrl(this.refreshOperation, "pagingTagPageNumber=" + queryPaging.getNumberOfPages() ) + javascriptEventSuffix + "\">Last</a>");
    }
    result.append("</div>\n");
    result.append("  <form class=\"form-inline form-small\" name=\"" + this.formName + "\" id=\"" + this.formName + "Id\">\n");
    result.append("    <label for=\"show-entries\">Show:&nbsp;</label>\n");
    result.append("    <select name=\"pagingTagPageSize\" id=\"show-entries\" class=\"span2\" onchange=\"" + javascriptEventPrefix + this.refreshOperation + javascriptEventSuffix + "\">\n");

    this.guiPaging.getPageSize();

    String pageSizesString = GrouperUiConfig.retrieveConfig().propertyValueString("pager.pagesize.selection", "10 25 50 100");

    List<String> pageSizesList = GrouperUtil.splitTrimToList(pageSizesString, " ");
    int defaultPageSize = GrouperUiConfig.retrieveConfig().propertyValueInt("pager.pagesize.default", 50);

    if (!pageSizesList.contains(defaultPageSize + "")) {
      LOG.error("Why is default page size not in page size list? " + defaultPageSize + ", " + pageSizesString);
    }
    
    for (String pageSizeString : pageSizesList) {

      boolean selected = StringUtils.equals(pageSizeString, Integer.toString(this.guiPaging.getPageSize()));

      //result.append("      <option>25</option>\n");
      result.append("      <option" + (selected ? " selected=\"selected\" " : "") + ">" + pageSizeString + "</option>\n");

    }

    result.append("    </select>\n");
    result.append("  </form>\n");
    result.append("</div>\n");

    
    this.getJspContext().getOut().print(result.toString());
  }

  /**
   * append to url with question of ampersand
   * @param url
   * @param append
   * @return the url
   */
  private static String appendToUrl(String url, String append) {
    if (url.contains("?") ) {
      return url + "&" + append;
    }
    return url + "?" + append;
  }
}
