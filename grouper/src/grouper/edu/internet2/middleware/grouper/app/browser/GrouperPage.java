package edu.internet2.middleware.grouper.app.browser;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.TimeoutError;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class GrouperPage {

  /**
   * millis to wait after each click, default 0
   */
  private int millisWaitAfterClick = 0;
  
  /**
   * millis to wait after each click, default 0
   * @return
   */
  public int getMillisWaitAfterClick() {
    return millisWaitAfterClick;
  }

  /**
   * millis to wait after each click, default 0
   * @param millisWaitAfterClick
   */
  public void setMillisWaitAfterClick(int millisWaitAfterClick) {
    this.millisWaitAfterClick = millisWaitAfterClick;
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperPage.class);

  /**
   * The jsp is expected to have the grouper broswer page custom tag. 
   * There is an example in miscellaneous.jsp. 
   * @param jspName this is the jsp name without the .jsp, or if null just wait for any click
   */
  public void waitForJspToLoad(String jspName) {
    // this.getPage().locator("#grouperAjaxDone_miscellaneous").textContent();
    long currentMillis = System.currentTimeMillis() - 190;
    LOG.debug(jspName + ": " + new Timestamp(currentMillis) + ", " + currentMillis);
    OUTER: for (int i = 0; i < 600; i++) {
      List<Locator> grouperJspClasses = this.getPage()
          .locator(".grouperJspClass").all();
      if (GrouperUtil.length(grouperJspClasses) > 0) {
        for (Locator locator : grouperJspClasses) {
          String grouperJspPrefix = null;
          try {
            grouperJspPrefix = locator.textContent();
          } catch (TimeoutError te) {
            // Text content will time out after 30 seconds, so if i > 2,
            // that means we have tried for 90 seconds.
            if (i > 2) {
              break OUTER;
            }
            // ignore this exception and continue loop
            continue;
          }

          if (jspName != null && !StringUtils.equals(grouperJspPrefix, jspName)) {
            continue;
          }
          String timestampString = locator.getAttribute("data-gr-page-loadtime");
          Timestamp timestamp = GrouperUtil
              .timestampIsoUtcMicrosConvertFromString(timestampString);
          LOG.debug(jspName + ": " + timestampString + ", " + timestamp.getTime());
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
    List<String> messagingContents = this.getPage().locator("#messaging")
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

  
  /**
   * 
   * @param externalizedTextKeyToFind
   * @param throwExceptionOnError
   * @return true if found the message
   */
  public boolean findLiteralTextInMessages(String literalText,
      boolean throwExceptionOnError) {
    List<String> messagingContents = this.getPage().locator("#messaging")
        .allTextContents();
    for (String message : GrouperUtil.nonNull(messagingContents)) {
      if (message.contains(literalText)) {
        return true;
      }
    }
    if (throwExceptionOnError) {
      throw new RuntimeException("Cannot find literal text, expecting:" + literalText
          + ", received: " + messagingContents);
    }
    return false;
  }
  
  /**
   * This method is used to navigate to the main group page. This is going to search for the group in the upper right 
   * search and will find it in the list of results.
   * @param groupName the subclass of the browser class should input various identifiers of the group 
   * or the group itself.
   */
  public void navigateToGroup(String groupName) {
    GrouperUtil.assertion(StringUtils.isNotBlank(groupName),
        "You must pass in a group to find");
    this.navigateToGrouperHome();
    this.getPage().locator("#mainPageSearchInput").fill(groupName);
    this.getPage().keyboard().press("Enter");
    this.waitForJspToLoad("search");
    GrouperUtil.sleep(this.getMillisWaitAfterClick());

    // Looping through the pages 1000 times, breaking when the desired group is found
    int timeToLive = 1000;
    OUTER: while (true) {
      GrouperUtil.assertion(timeToLive-- > 0, "Endless loop while paging");

      // Looping through each of the result lines on one page
      for (Locator locator : this.getPage().locator("#searchResultsId")
          .locator("[data-gr-browse-group-name]").all()) {

        if (StringUtils.equals(locator.getAttribute("data-gr-browse-group-name"),
            groupName)) {
          locator.click();
          break OUTER;
        }
      }

      // See if there is a next page link
      if (this.getPage().locator("#searchResultsId")
          .locator("#pagingNextLink").all().isEmpty()) {

        // No next link means the last page has been reached
        throw new RuntimeException("Group not found: '" + groupName + "'");
      } else {
        this.getPage().locator("#searchResultsId")
            .locator("#pagingNextLink").click();
        this.waitForJspToLoad("search");
        GrouperUtil.sleep(this.getMillisWaitAfterClick());
      }

    }
    this.waitForJspToLoad("viewGroup");
    GrouperUtil.sleep(this.getMillisWaitAfterClick());
  }
  
  
  
  /**
   * This method is used to navigate to the main stem page. This is going to search for the stem in the upper right 
   * search and will find it in the list of results.
   * @param stemName the subclass of the browser class should input various identifiers of the group 
   * or the group itself.
   */
  public void navigateToStem(String stemName) {
    GrouperUtil.assertion(StringUtils.isNotBlank(stemName),
        "You must pass in a stem to find");
    this.navigateToGrouperHome();
    this.getPage().locator("#mainPageSearchInput").fill(stemName);
    this.getPage().keyboard().press("Enter");
    this.waitForJspToLoad("search");
    GrouperUtil.sleep(this.getMillisWaitAfterClick());

    // Looping through the pages 1000 times, breaking when the desired group is found
    int timeToLive = 1000;
    OUTER: while (true) {
      GrouperUtil.assertion(timeToLive-- > 0, "Endless loop while paging");

      // Looping through each of the result lines on one page
      for (Locator locator : this.getPage().locator("#searchResultsId")
          .locator("[data-gr-browse-folder-name]").all()) {

        if (StringUtils.equals(locator.getAttribute("data-gr-browse-folder-name"),
            stemName)) {
          locator.click();
          break OUTER;
        }
      }

      // See if there is a next page link
      if (this.getPage().locator("#searchResultsId")
          .locator("#pagingNextLink").all().isEmpty()) {

        // No next link means the last page has been reached
        throw new RuntimeException("Folder not found: '" + stemName + "'");
      } else {
        this.getPage().locator("#searchResultsId")
            .locator("#pagingNextLink").click();
        this.waitForJspToLoad("search");
        GrouperUtil.sleep(this.getMillisWaitAfterClick());
      }

    }
    this.waitForJspToLoad("viewStem");
    GrouperUtil.sleep(this.getMillisWaitAfterClick());
  }

  
  public void findMembership(String groupName, Subject subject) {

    this.navigateToGroup(groupName);
    // Looping through the pages 1000 times, breaking when the desired group is found
    int timeToLive = 1000;
    boolean membershipFound = false;
    while (!membershipFound) {
      GrouperUtil.assertion(timeToLive-- > 0, "Endless loop while paging");
      for (Locator locator : this.getPage()
          .locator("#membersToDeleteFormId")
          .locator("[data-gr-member-checkbox]").all()) {

        if (StringUtils.equals(locator.getAttribute("data-gr-member-checkbox"),
            subject.getSourceId() + "||" + subject.getId())) {
          membershipFound = true;
          locator.click();
          GrouperUtil.sleep(this.getMillisWaitAfterClick());
          break;
        }
      }
      if (membershipFound) {
        break;
      }

      // See if there is a next page link
      if (this.getPage().locator("#groupFilterResultsId")
          .locator("#pagingNextLink").all().isEmpty()) {

        // No next link means the last page has been reached
        throw new RuntimeException("Membership not found: '" + subject.getName() + "'");
      } else {
        this.getPage().locator("#groupFilterResultsId")
            .locator("#pagingNextLink").click();
        this.waitForJspToLoad("viewGroup");
        GrouperUtil.sleep(this.getMillisWaitAfterClick());
      }

    }
  }

  /**
   * This method is called at the beginning of each programmatic browsing method to ensure that 
   * every method begins from the home page.
   */
  public void navigateToGrouperHome() {
    // http://localhost:8080/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Main.indexMain
    // matching the context which in this case is 'grouper'
//    Pattern pattern = Pattern.compile("^[^/]+//[^/]+/([^/]+)/.+$");
//    String url = this.getPage().url();
//    Matcher matcher = pattern.matcher(url);
//    GrouperUtil.assertion(matcher.matches(), "Can't find context in url: " + this.getPage().url());
//    String context = matcher.group(1);
    
    String grouperUiUrl = this.retrieveBaseUrl(); 
    
    this.getPage().navigate(grouperUiUrl
      +  "/grouperUi/app/UiV2Main.index?operation=UiV2Main.indexMain");
    this.waitForJspToLoad("indexMain");
  }


  private Page page;
  private Browser browser;
  private BrowserContext context;
  
  /**
   * e.g. https://grouperdemo.internet2.edu/grouper_v4/
   */
  private String baseUrl = null;
  
  /**
   * 
   * @param browserType e.g. firefox, chromium, webkit
   * @return
   */
  public GrouperPage initializePage(String browserType) {
    Playwright playwright = Playwright.create();
    BrowserType chromium = null;
    if (StringUtils.equalsIgnoreCase("firefox", browserType)) {
      chromium = playwright.firefox();
    } else if (StringUtils.equalsIgnoreCase("chromium", browserType)) {
      chromium = playwright.chromium();
    } else if (StringUtils.equalsIgnoreCase("webkit", browserType)) {
      chromium = playwright.webkit();
    } else {
      throw new RuntimeException("Invalid browserType, expecting: firefox, chromium, webkit");
    }
    this.browser = chromium.launch(new BrowserType.LaunchOptions().setHeadless(true));
    this.context = browser.newContext();
    this.page = context.newPage();
    return this;
  }
  
  /**
   * e.g. https://grouperdemo.internet2.edu/grouper_v4/
   */
  public void setBaseUrl(String theBaseUrl) {
    this.baseUrl = theBaseUrl;
  }
  
  /**
   * get the base url with no trailing slash
   * @return
   */
  public String retrieveBaseUrl() {
    if (this.baseUrl == null) {
      this.baseUrl = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.ui.url");
    }
    if (StringUtils.defaultString(this.baseUrl).endsWith("/")) {
      this.baseUrl = GrouperUtil.stripLastSlashIfExists(this.baseUrl);
    }
    return this.baseUrl;
  }
  
  public BrowserContext getContext() {
    return context;
  }

  
  public void setContext(BrowserContext context) {
    this.context = context;
  }

  public void close() {
    if (this.page != null) {
      try {
        this.page.close();
      } catch (Exception e) {
        // ignore
      }
    }
    if (this.browser != null) {
      try {
        this.browser.close();
      } catch (Exception e) {
        // ignore
      }
    }
  }
  
  public Browser getBrowser() {
    return browser;
  }


  
  public void setBrowser(Browser browser) {
    this.browser = browser;
  }


  public Page getPage() {
    return page;
  }

  
  public void setPage(Page page) {
    this.page = page;
  }
  
}
