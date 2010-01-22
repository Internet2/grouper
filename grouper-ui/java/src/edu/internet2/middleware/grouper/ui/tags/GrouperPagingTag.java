/**
 * @author mchyzer
 * $Id: GrouperPagingTag.java,v 1.1 2009-10-11 22:04:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ui.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * does paging google style
 */
public class GrouperPagingTag extends SimpleTagSupport  {

  /**
   * 
   * @param result
   * @param text 
   * @param pageNumber
   */
  private void appendButton(StringBuilder result, String text, int pageNumber) {
    result.append("<a href=\"#\" onclick=\"return guiGoToPage('").append(this.pagingName).append("',");
    result.append(pageNumber).append(",'").append(this.refreshOperation);
    result.append("');\">").append(text).append("</a>");

  }
  
  /**
   * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
   */
  @Override
  public void doTag() throws JspException, IOException {
    
    StringBuilder result = new StringBuilder();

    //get the bean, but exist
    GuiPaging guiPaging = GuiPaging.retrievePaging(this.pagingName, true);
    
    QueryPaging queryPaging = guiPaging.queryPaging();
    
    
    //Previous 1 2 3 4 5 6 7 8 9 10 11 Next
    //Previous 1 2 3 ... 5 6 7 8 9 ... 11 12 13 Next
    //Previous 1 2 3 4 5 6 ... 11 12 13 Next
    //Previous 1 2 3 ... 8 9 10 11 12 13 Next
    //Previous 1 2 Next
    
    if (this.showSummaryOrButtons) {
      
      result.append(queryPaging.getPageStartIndex());
      result.append("-");
      result.append(queryPaging.getPageEndIndex());
      result.append(" of ");
      result.append(queryPaging.getTotalRecordCount());
      result.append(" &nbsp;&nbsp; ");
      result.append(GrouperUiUtils.escapeHtml(GrouperUiUtils.message("page.size"), true));
      result.append(" ");
      String id = GrouperUtil.uniqueId();
      
      
      
      
      String pageSizeOptionsString = TagUtils.mediaResourceString("pager.pagesize.selection");
      String[] pageSizeOptionsStringArray = GrouperUtil.splitTrim(pageSizeOptionsString, " ");
      
      result.append("<select class=\"pagingDropdown\" id=\"").append(id).append("\" onchange=\"return guiPageSize('").append(this.pagingName)
        .append("', guiInt(guiFieldValue(document.getElementById('").append(id).append("'))), '")
        .append(this.refreshOperation).append("');\">\n");
      int currentPageSize = queryPaging.getPageSize(); 
      boolean foundOne = false;
      for (String pageSizeOption : pageSizeOptionsStringArray) {
        result.append("<option ");
        if (Integer.toString(currentPageSize).equals(pageSizeOption)) {
          result.append(" selected=\"selected\"");
          foundOne = true;
        }
        result.append(">").append(pageSizeOption).append("</option>");
      }
      
      //this is bad
      if (!foundOne) {
        
        throw new RuntimeException("Cant find paging value: '" + currentPageSize + "' in list: " + pageSizeOptionsString);
        
      }
      result.append("</select>");
    } else {
      
      //see if there should be a previous tag
      if (!queryPaging.isFirstPage()) {
        //allObjects.appState.pagers.")
        //.append(this.pagingName).append(".pageNumber = ").append( "; return false;\
        this.appendButton(result, "Previous", queryPaging.getPageNumber()-1);
      } else {
        //just show a label (nothing)
        //result.append("Previous");
      }
      result.append(" ");

      //we are either doing 
      // Previous 1 2 3 4 5 6 7 8 9 10 11 Next
      // Previous 1 2 3 ... 5 6 7 8 9 ... 11 12 13 Next
      // Previous 1 2 3 4 5 6 ... 11 12 13 Next
      // Previous 1 2 3 ... 8 9 10 11 12 13 Next
      // Previous 1 2 Next
      int i = 1;
      while (i <= queryPaging.getNumberOfPages()) {

        if (showLabel(queryPaging, i)) {
          result.append(i).append(" ");
        } else if (showButton(queryPaging, i)) {
          this.appendButton(result, "" + i, i);
          result.append(" ");
        } else if (showEllipses(queryPaging, i)) {
          result.append("... ");
        }
        i = queryPaging.nextPageNeeded(i);
      }

      //see if there should be a next tag
      if (!queryPaging.isLastPage()) {
        
        this.appendButton(result, "Next", queryPaging.getPageNumber()+1);
      }

      
    }
    
    this.getJspContext().getOut().print(result.toString());
  }

  /** pagingName */
  private String pagingName;

  
  /**
   * pagingName
   * @return the groupName
   */
  public String getPagingName() {
    return this.pagingName;
  }

  /** show summary or buttons */
  private boolean showSummaryOrButtons;


  
  /**
   * @return the showSummaryOrButtons
   */
  public boolean isShowSummaryOrButtons() {
    return this.showSummaryOrButtons;
  }

  
  /**
   * show summary or buttons
   * @param showSummaryOrButtons1 the showSummaryOrButtons to set
   */
  public void setShowSummaryOrButtons(boolean showSummaryOrButtons1) {
    this.showSummaryOrButtons = showSummaryOrButtons1;
  }

  
  /**
   * @param pagingName1 the pagingName to set
   */
  public void setPagingName(String pagingName1) {
    this.pagingName = pagingName1;
  }
  
  /** operation to call when refreshing */
  private String refreshOperation;


  
  /**
   * operation to call when refreshing
   * @return the refreshOperation
   */
  public String getRefreshOperation() {
    return this.refreshOperation;
  }


  
  /**
   * operation to call when refreshing
   * @param refreshOperation1 the refreshOperation to set
   */
  public void setRefreshOperation(String refreshOperation1) {
    this.refreshOperation = refreshOperation1;
  }

  /**
   * @param queryPaging
   * @param pageNumber
   * @return true if ellipses should appear for this button
   */
  public boolean showEllipses(QueryPaging queryPaging, int pageNumber) {
  
    //first of all, no for labels and buttons
    if (showButton(queryPaging, pageNumber)) {
      return false;
    }
  
    if (showLabel(queryPaging, pageNumber)) {
      return false;
    }
  
    //show ellipses on the transitions of border cases
    //must be over 7 so that there is only one set of ellipses if nothing in the middle
    if (pageNumber == 4
        || (queryPaging.getPageNumber() != 1 && queryPaging.getNumberOfPages() - pageNumber == 3)) {
      return true;
    }
  
    return false;
  }

  /**
   * @param queryPaging
   * @param pageNumber
   * @return true if a label should display for this page number
   */
  public boolean showLabel(QueryPaging queryPaging, int pageNumber) {
  
    //first of all see if it is a label
    return queryPaging.getPageNumber() == pageNumber;
  
  }

  /**
   * @param queryPaging
   * @param pageNumber
   * @return true if a button should display for this page number
   */
  public boolean showButton(QueryPaging queryPaging, int pageNumber) {
  
    //first of all see if it is a label
    if (showLabel(queryPaging, pageNumber)) {
      return false;
    }
  
    //if total is less than 11, then yes
    if (queryPaging.getNumberOfPages() <= 11) {
      return true;
    }
  
    //if we are within 2 of either side or the current page
    if (pageNumber <= 3 || Math.abs(pageNumber - queryPaging.getPageNumber()) <= 2
        || queryPaging.getNumberOfPages() - pageNumber <= 2) {
      return true;
    }
  
    return false;
  }
  
}


