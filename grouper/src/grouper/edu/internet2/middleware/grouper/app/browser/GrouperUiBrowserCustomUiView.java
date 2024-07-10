package edu.internet2.middleware.grouper.app.browser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.internal.LinkedTreeMap;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * This class is used to programmatically run a custom Ui.
 * <p>
 * Run a custom Ui. 
 * <blockquote> 
 * <pre>
 *    GrouperUiBrowserCustomUiView grouperUiBrowserCustomUiView = new GrouperUiBrowserCustomUiView(page).
 *      assignConfigId("myCustomUiConfigId").browse();
 * </pre>
 * </blockquote>
 * </p>
 */
public class GrouperUiBrowserCustomUiView extends GrouperUiBrowser {

  public GrouperUiBrowserCustomUiView(GrouperPage grouperPage) {
    super(grouperPage);
  }

  
  private String customUiConfigId;
  
  public GrouperUiBrowserCustomUiView assignCustomUiConfigId(
      String customUiConfigId) {

    this.customUiConfigId = customUiConfigId;
    return this;
  }
  
  public GrouperUiBrowserCustomUiView browse() {
    this.navigateToGrouperHome();
    this.getGrouperPage().getPage().locator("#leftMenuMiscellaneousLink").click();
    this.waitForJspToLoad("miscellaneous");
    this.getGrouperPage().getPage().locator("#miscCustomUiLink").click();
    this.waitForJspToLoad("customUiConfigs");
    this.getGrouperPage().getPage().locator("#actions_" + customUiConfigId + "_id").click();
    GrouperUtil.sleep(300);
    this.getGrouperPage().getPage().locator("#run_" + customUiConfigId + "_id").click();
    this.waitForJspToLoad("indexCustomUi");
    return this;
  }

}
