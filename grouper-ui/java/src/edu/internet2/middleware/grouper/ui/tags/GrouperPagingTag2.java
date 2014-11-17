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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * paging tag for ui v2
 * @author mchyzer
 *
 */
public class GrouperPagingTag2 extends SimpleTagSupport {

  /**
   * process a request from a paging tag
   * @param request
   * @param queryOptions
   * @param guiPaging
   */
  public static void processRequest(HttpServletRequest request, GuiPaging guiPaging, QueryOptions queryOptions) {
    processRequest(request, guiPaging, queryOptions, null);
  }

  /**
   * page size from http request from paging gui object
   * @param request
   * @return the page size
   */
  public static int pageSize(HttpServletRequest request) {
    String pageSizeString = request.getParameter("pagingTagPageSize");
    int pageSize = GrouperUtil.intValue(pageSizeString);
    return pageSize;
  }

  /**
   * process a request from a paging tag
   * @param request
   * @param guiPaging
   * @param queryOptions 
   * @param pagesizeDefaultProperty
   */
  public static void processRequest(HttpServletRequest request, GuiPaging guiPaging, 
      QueryOptions queryOptions, String pagesizeDefaultProperty) {

    //how many per page
    String pageSizeString = request.getParameter("pagingTagPageSize");
    int pageSize = -1;
    if (!StringUtils.isBlank(pageSizeString)) {
      pageSize = GrouperUtil.intValue(pageSizeString);
    } else {
      pagesizeDefaultProperty = StringUtils.defaultIfEmpty(pagesizeDefaultProperty, "pager.pagesize.default");
      pageSize = GrouperUiConfig.retrieveConfig().propertyValueInt(pagesizeDefaultProperty, 50);
    }
    guiPaging.setPageSize(pageSize);
    
    //1 indexed
    String pageNumberString = request.getParameter("pagingTagPageNumber");
    
    int pageNumber = 1;
    if (!StringUtils.isBlank(pageNumberString)) {
      pageNumber = GrouperUtil.intValue(pageNumberString);
    }
    
    guiPaging.setPageNumber(pageNumber);

    if (queryOptions != null) {
      queryOptions.paging(pageSize, pageNumber, true);
    }
  }
  
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
    
    if (this.guiPaging == null) {
      throw new NullPointerException("guiPaging cannot be null, does it exist not null in the objec model?");
    }
    
    StringBuilder result = new StringBuilder();
    
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
    //  <form style="display:none;">
    //    <input type="hidden" name="pagingTagPageNumber" value="" />
    //  </form>

    QueryPaging queryPaging = this.guiPaging.queryPaging();
    
    result.append("  <div class=\"pull-right\">" + GrouperUiUtils.message("paging2.showing") + " " + (queryPaging.getFirstIndexOnPage()+1) 
        + "-" + (queryPaging.getLastIndexOnPage()+1) + " " + GrouperUiUtils.message("paging2.of") + " " + this.guiPaging.getTotalRecordCount() + " &middot; ");

    String javascriptEventPrefix = "ajax('";
    String javascriptEventSuffix = "', {formIds: '" + this.formName + "Id" + (StringUtils.isBlank(this.ajaxFormIds) ? "" : ("," + this.ajaxFormIds)) + "'}); return false;";

    if (queryPaging.isFirstPage()) {
      result.append(GrouperUiUtils.message("paging2.first") + " | " + GrouperUiUtils.message("paging2.prev"));
    } else {
      result.append("<a href=\"#\" onclick=\"" + javascriptEventPrefix + this.refreshOperation + javascriptEventSuffix + "\">" 
          + GrouperUiUtils.message("paging2.first") + "</a> | ");
      result.append("<a href=\"#\" onclick=\"" + javascriptEventPrefix 
          + appendToUrl(this.refreshOperation, "pagingTagPageNumber=" + (queryPaging.getPageNumber()-1) ) + javascriptEventSuffix + "\">" 
          + GrouperUiUtils.message("paging2.prev") + "</a>");
    }
    result.append(" | ");
    if (queryPaging.isLastPage()) {
      result.append(GrouperUiUtils.message("paging2.next") + " | " + GrouperUiUtils.message("paging2.last"));
    } else {
      result.append("<a href=\"#\" onclick=\"" + javascriptEventPrefix 
          + appendToUrl(this.refreshOperation, "pagingTagPageNumber=" + (queryPaging.getPageNumber()+1) ) + javascriptEventSuffix + "\">" + GrouperUiUtils.message("paging2.next") + "</a>");
      result.append(" | ");
      result.append("<a href=\"#\" onclick=\"" + javascriptEventPrefix 
          + appendToUrl(this.refreshOperation, "pagingTagPageNumber=" + queryPaging.getNumberOfPages() ) + javascriptEventSuffix + "\">" + GrouperUiUtils.message("paging2.last") + "</a>");
    }
    result.append("</div>\n");
    result.append("  <form class=\"form-inline form-small\" name=\"" + this.formName + "\" id=\"" + this.formName + "Id\">\n");
    result.append("    <label for=\"show-entries\">" + GrouperUiUtils.message("paging2.show") + "&nbsp;</label>\n");
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
    //this is a form if something needs to send the current page number to the server...
    result.append("  <form style=\"display:none;\" name=\"" + this.formName + "PageNumber\" id=\"" + this.formName + "PageNumberId\">\n");
    result.append("    <input type=\"hidden\" name=\"pagingTagPageNumber\" value=\"" + queryPaging.getPageNumber() + "\" />\n");
    result.append("  </form>\n");
    
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
