package edu.internet2.middleware.grouper.app.browser;
import java.sql.Timestamp;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.playwright.Page;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * This class is used to verify the version of the ui.
 * <p>
 * Get the current ui version
 * <blockquote> 
 * <pre>
 *    GrouperUiBrowserGeneralVerifyVersion grouperUiBrowserGeneralVerifyVersion = new GrouperUiBrowserGeneralVerifyVersion(page).browse();
 *    String uiVersion = grouperUiBrowserGeneralVerifyVersion.getUiVersion().toString();
 * </pre>
 * </blockquote>
 * </p>
 * Confirm the current ui version
 * <blockquote> 
 * <pre>
 *    new GrouperUiBrowserGeneralVerifyVersion(page).assignExpectedVersion("4.0.0").browse();
 * </pre>
 * </blockquote>
 * </p>
 */
public class GrouperUiBrowserGeneralVerifyVersion
    extends GrouperUiBrowser {

  public GrouperUiBrowserGeneralVerifyVersion(GrouperPage grouperPage) {
    super(grouperPage);
  }

  /**
   * This is the expected version of the ui.
   */
  private GrouperVersion expectedVersion;

  /**
   * Getter method for the expected version.
   * @return expectedVersion
   */
  public GrouperVersion getExpectedVersion() {
    return expectedVersion;
  }

  /**
   * Assigner method for expected version
   * @param expectedVersion1 is the expected version to be assigned
   * @return this object
   */
  public GrouperUiBrowserGeneralVerifyVersion assignExpectedVersion(
      String expectedVersion1) {

    this.expectedVersion = GrouperVersion.valueOfIgnoreCase(expectedVersion1, false);
    return this;
  }

  /**
   * Assigner method for expected version
   * @param expectedVersion1 is the expected version to be assigned
   * @return this object
   */
  public GrouperUiBrowserGeneralVerifyVersion assignExpectedVersion(
      GrouperVersion expectedVersion1) {

    this.expectedVersion = expectedVersion1;
    return this;
  }
  
  /**
   * getter method for the uiVersion
   * @return
   */
  public GrouperVersion getUiVersion() {
    return uiVersion;
  }

  /**
   * field that represents the UI version.
   */
  private GrouperVersion uiVersion;

  /**
  * Method used to verify that the browser starts with the correct version.
  */
  public GrouperUiBrowserGeneralVerifyVersion browse() {
    this.navigateToGrouperHome();
    this.getGrouperPage().getPage().locator("#leftMenuMiscellaneousLink").click();
    
    this.waitForJspToLoad("miscellaneous");
    this.getGrouperPage().getPage().locator("#miscConfigureLink").click();
    this.waitForJspToLoad("configureIndex");
 

    // These versions are in hidden spans in the .jsp
    String uiVersionString = GrouperUtil.defaultIfBlank(
        StringUtils.trimToNull(
            this.getGrouperPage().getPage().locator("#configureHeaderVersionContainer").textContent()),
        StringUtils.trimToNull(
            this.getGrouperPage().getPage().locator("#configureHeaderVersionGrouper").textContent()));

    this.uiVersion = new GrouperVersion(uiVersionString);

    if (expectedVersion != null && !expectedVersion.equals(uiVersion)) {
      throw new RuntimeException("Expected version: " + expectedVersion
          + " is not the same as Ui version: " + uiVersion);
    }
    uiVersion = this.getUiVersion();
    return this;
  }
  
}