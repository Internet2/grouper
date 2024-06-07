package edu.internet2.middleware.grouper.app.browser;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.playwright.Page;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * This is the programmatic browser class.
 * @param <T>
 */
public abstract class GrouperUiBrowser {

  /**
   * Field that represents which page the programmatic browser is interacting with.
   */
  private Page page;

  /**
   * Field that represents which page the programmatic browser is interacting with.
   * @return the page
   */
  public Page getPage() {
    return page;
  }

  /**
   * This method is called at the beginning of each programmatic browsing method to ensure that 
   * every method begins from the home page.
   */
  public void navigateToGrouperHome() {
    this.getPage().navigate(
        "http://localhost:8080/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Main.indexMain");
    
    // Wait for the ajax to complete
    this.getPage().locator("#grouperAjaxDone_indexMain").textContent();
    //<span id="grouperJspId" style="display:none">grouperMainPage</span>
    GrouperUtil.assertion(StringUtils.equals(this.getPage().locator("#grouperJspId").textContent(), "grouperMainPage"), 
        "expected to be on the main page.");
  }

  /**
   * Field that represents which page the programmatic browser is interacting with.
   * @param page is the page to be interacted with
   */
  public GrouperUiBrowser(Page page) {
    super();
    this.page = page;
  }
}
