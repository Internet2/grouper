package edu.internet2.middleware.grouper.app.browser;

import java.util.List;

import edu.internet2.middleware.grouper.ui.util.GrouperUiConfigInApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * This class is used to view daemon errors. Clicks on miscellaneous page, then daemon jobs, then filters by errors. 
 * This is only going to return the maximum number of errors that can fit on one page, which is 100 by default.
 * <p>
 * Get the daemon errors
 * <blockquote> 
 * <pre>
 *    GrouperUiBrowserDaemonViewErrors grouperUiBrowserDaemonViewErrors = new GrouperUiBrowserDaemonViewErrors(page).browse();
 *    List<String> jobNamesWithErrors = grouperUiBrowserDaemonViewErrors.getGrouperUiBrowserDaemonErrors();
 * </pre>
 * </blockquote>
 * </p>
 */
public class GrouperUiBrowserDaemonViewErrors
extends GrouperUiBrowser {

  public GrouperUiBrowserDaemonViewErrors(GrouperPage grouperPage) {
    super(grouperPage);
  }

  private List<String> grouperUiBrowserDaemonErrors;

  /**
   * An array list containing strings of the daemon errors. 
   * This is only going to return the maximum number of errors that can fit on one page, which is 100 by default.
   * @return grouperUiBrowserDaemonErrors
   */
  public List<String> getGrouperUiBrowserDaemonErrors() {
    return grouperUiBrowserDaemonErrors;
  }

  /**
   * Method used to view daemon errors
   * @throws InterruptedException 
   */
  public GrouperUiBrowserDaemonViewErrors browse() {
    this.navigateToGrouperHome();
    this.getGrouperPage().getPage().locator("#leftMenuMiscellaneousLink").click();

    this.waitForJspToLoad("miscellaneous");

    this.getGrouperPage().getPage().locator("#miscDaemonJobsLink").click();
    this.waitForJspToLoad("adminDaemonJobs");
    this.getGrouperPage().getPage().locator("#daemonJobsFilterShowOnlyErrorsId").check();
    this.getGrouperPage().getPage().locator("#applyfilterdaemonjobs").click();
    this.waitForJspToLoad("adminDaemonJobsContents");
    String pageOptions = GrouperUiConfigInApi.retrieveConfig()
        .propertyValueString("pager.pagesize.selection");
    List<String> pageOptionsList = GrouperUtil.splitTrimToList(pageOptions, " ");
    String highestOption = pageOptionsList.get(pageOptionsList.size() - 1);
    this.getGrouperPage().getPage().locator("#show-entries").selectOption(highestOption);
    this.waitForJspToLoad("adminDaemonJobsContents");

    grouperUiBrowserDaemonErrors = this.getGrouperPage().getPage().locator("#daemontable")
        .locator(".adminDaemonJobNameLink").allTextContents();

    return this;
  }

}