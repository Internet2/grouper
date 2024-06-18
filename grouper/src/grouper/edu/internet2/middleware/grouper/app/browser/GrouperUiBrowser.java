package edu.internet2.middleware.grouper.app.browser;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * This is the programmatic browser class.
 * @param <T>
 */
public abstract class GrouperUiBrowser {

  /**
   * Field that represents which page the programmatic browser is interacting with.
   */
  private GrouperPage grouperPage;

  /**
   * Field that represents which page the programmatic browser is interacting with.
   * @return the page
   */
  public GrouperPage getGrouperPage() {
    return grouperPage;
  }

  /**
   * This method is called at the beginning of each programmatic browsing method to ensure that 
   * every method begins from the home page.
   */
  public void navigateToGrouperHome() {
    this.grouperPage.getPage().navigate(
        "http://localhost:8080/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Main.indexMain");

    this.waitForJspToLoad("indexMain");
  }

  /**
   * Field that represents which page the programmatic browser is interacting with.
   * @param page is the page to be interacted with
   */
  public GrouperUiBrowser(GrouperPage grouperPage) {
    super();
    this.grouperPage = grouperPage;
  }

  /**
   * The jsp is expected to have the grouper broswer page custom tag. 
   * There is an example in miscellaneous.jsp. 
   * @param jspName this is the jsp name without the .jsp, or if null just wait for any click
   */
  public void waitForJspToLoad(String jspName) {
    // this.getPage().locator("#grouperAjaxDone_miscellaneous").textContent();
    long currentMillis = System.currentTimeMillis() - 190;
    for (int i = 0; i < 600; i++) {
      List<Locator> grouperJspClasses = this.grouperPage.getPage()
          .locator(".grouperJspClass").all();
      if (GrouperUtil.length(grouperJspClasses) > 0) {
        for (Locator locator : grouperJspClasses) {
          String grouperJspPrefix = locator.textContent();
          if (jspName != null && !StringUtils.equals(grouperJspPrefix, jspName)) {
            continue;
          }
          String timestampString = locator.getAttribute("data-gr-page-loadtime");
          Timestamp timestamp = GrouperUtil
              .timestampIsoUtcMicrosConvertFromString(timestampString);
          if (timestamp != null && timestamp.getTime() > currentMillis) {
            if (i == 0) {
              GrouperUtil.sleep(200);
            } else if (i == 1) {
              GrouperUtil.sleep(100);
            }
            return;
          }
        }

      }
      GrouperUtil.sleep(100);
    }
    throw new RuntimeException(
        "Could not find the recent html tag for the jsp: '" + jspName + "'");
  }

  /**
   * 
   * @param externalizedTextKeyToFind
   * @param throwExceptionOnError
   * @return true if found the message
   */
  public boolean findMessageInMessages(String externalizedTextKeyToFind,
      boolean throwExceptionOnError) {
    List<String> messagingContents = this.getGrouperPage().getPage().locator("#messaging")
        .allTextContents();
    String successMessage = GrouperTextContainer.textOrNull(externalizedTextKeyToFind);
    for (String message : GrouperUtil.nonNull(messagingContents)) {
      if (message.contains(successMessage)) {
        return true;
      }
    }
    if (throwExceptionOnError) {
      throw new RuntimeException("Cannot find message, expecting:" + successMessage
          + ", received: " + messagingContents);
    }
    return false;
  }
}
