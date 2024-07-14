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
 * This class is used to programmatically run a GSH template. This class can run templates on groups, stems, or in the GSH templates page 
 * in Miscellaneous. 
 * <p>
 * Run Gsh template in misellaneous>gsh templates. Add desired input values. Store each of the error, info, and success message outputs as lists.
 * <blockquote> 
 * <pre>
 *    GrouperUiBrowserTemplateRun grouperUiBrowserTemplateRun = new GrouperUiBrowserTemplateRun(
 *      grouperPage).assignGshTemplateConfigId("validateGrouper").
 *      assignSecondsToWait(20).addInputValue("gsh_input_expectedVersion", "1.2.3").
 *      addInputValue("gsh_input_textarea", "textAreaInput").
 *      addInputValue("gsh_input_dropdown", "first").
 *      addInputValue("gsh_input_password", "passwordInput").browse();
 *    List<String> messageErrors = rouperUiBrowserTemplateRun.getGrouperUiBrowserDaemonErrors();
 *    List<String> messageInfos = rouperUiBrowserTemplateRun.getGrouperUiBrowserDaemonErrors();
 *    List<String> messageSuccesses = rouperUiBrowserTemplateRun.getGrouperUiBrowserDaemonErrors();
 * </pre>
 * </blockquote>
 * </p>
 */
public class GrouperUiBrowserTemplateRun extends GrouperUiBrowser {

    
    public GrouperUiBrowserTemplateRun(GrouperPage grouperPage) {
    super(grouperPage);
  }
    private String groupToExecuteInName;

    /**
     * Id Path in UI
     * @param groupToExecuteInName
     * @return this object
     */
    public GrouperUiBrowserTemplateRun assignGroupToExecuteInName(
        String groupToExecuteInName) {

      this.groupToExecuteInName = groupToExecuteInName;
      return this;
    }

    /**
     * Uuid of the group
     * @param groupToExecuteInName
     * @return this object
     */
    public GrouperUiBrowserTemplateRun assignGroupToExecuteInId(
        String groupToExecuteInId) {
      Group group = GroupFinder.findByUuid(groupToExecuteInId, true);
      this.groupToExecuteInName = group.getName();
      return this;
    }

    /**
     * Pass in a group object
     * @param groupToExecuteInName
     * @return this object
     */
    public GrouperUiBrowserTemplateRun assignGroupToExecuteIn(
        Group group) {
      this.groupToExecuteInName = group.getName();
      return this;
    }
    
    private String stemToExecuteInName;


    /**
     * Folder in UI
     * @param stemToExecuteInName
     * @return this object
     */
    public GrouperUiBrowserTemplateRun assignStemToExecuteInName(
        String stemToExecuteInName) {

      this.stemToExecuteInName = stemToExecuteInName;
      return this;
    }
    
    private String gshTemplateConfigId;
    
    
    public GrouperUiBrowserTemplateRun assignGshTemplateConfigId(
        String gshTemplateConfigId) {

      this.gshTemplateConfigId = gshTemplateConfigId;
      return this;
    }

    
    
    /**
     * Pass in a stem object
     * @param groupToExecuteInName
     * @return this object
     */
    public GrouperUiBrowserTemplateRun assignStemToExecuteIn(
        Stem stem) {
      this.stemToExecuteInName = stem.getName();
      return this;
    }

    
    /**
     * For the template inputs
     * @param inputName
     * @param value
     * @return
     */
    private Map<String, String> gshTemplateInputValueMap = new LinkedTreeMap<>();
    
    /**
     * For the template inputs
     * @param inputName
     * @param value
     * @return
     */
    public GrouperUiBrowserTemplateRun addInputValue(String inputName, String value) {
      gshTemplateInputValueMap.put(inputName, value);
      return this;
    }  
    
    /**
     * Default amount of seconds to wait for the template to run. We found no other way to verify this.
     */
    private int secondsToWait = 40;    
    
    /**
     * Default amount of seconds to wait for the template to run. We found no other way to verify this.
     */
    public GrouperUiBrowserTemplateRun assignSecondsToWait(
        int secondsToWait) {
      this.secondsToWait = secondsToWait;
      return this;
    }
    
    /**
     * The success messages (green). The user can check each sorted messge contents to view desired outputs.
     * @return
     */
    private List<String> messageContentSuccesses = new ArrayList<>();
    
    /**
     * The success messages (green). The user can check each sorted messge contents to view desired outputs.
     * @return
     */
    private List<String> messageContentInfos = new ArrayList<>();
    
    /**
     * The success messages (green). The user can check each sorted messge contents to view desired outputs.
     * @return
     */
    private List<String> messageContentErrors = new ArrayList<>();

    /**
     * The success messages (green). The user can check each sorted messge contents to view desired outputs.
     * @return
     */
    public List<String> getMessageContentSuccesses() {
      return messageContentSuccesses;
    }

    /**
     * The info messages (blue). The user can check each sorted messge contents to view desired outputs.
     * @return
     */
    public List<String> getMessageContentInfos() {
      return messageContentInfos;
    }

    /**
     * The error messages. The user can check each sorted messge contents to view desried outputs.
     * @return
     */
    public List<String> getMessageContentErrors() {
      return messageContentErrors;
    }

    /**
     * This method starts off by running the correct template in the correct place (stem, group, misc>gsh templates). 
     * It then fills in the previously mapped inputs, and then clicks submit and waits the desired time with a default of 40 seconds.
     * After this, it sorts the messages into lists based on success, info, and error.
     * @return
     */
    public GrouperUiBrowserTemplateRun browse()  {
      if (stemToExecuteInName != null && groupToExecuteInName != null) {
        throw new RuntimeException("Stem and group to execute in are mutually exclusive. "
            + "You have passed both a stem: " + stemToExecuteInName + ", and a group: " + groupToExecuteInName);
      }
      else if (stemToExecuteInName == null && groupToExecuteInName == null) {
        this.navigateToGrouperHome();
        this.getGrouperPage().getPage().locator("#leftMenuMiscellaneousLink").click();
        this.waitForJspToLoad("miscellaneous");
        this.getGrouperPage().getPage().locator("#miscGshTemplatesLink").click();
        this.waitForJspToLoad("gshTemplateConfigs");
        this.getGrouperPage().getPage().locator("#actions_" + gshTemplateConfigId + "_id").click();
        GrouperUtil.sleep(200);
        this.getGrouperPage().getPage().locator("#stemTemplateActionsRunTemplateButton").click();
      }
      else if (groupToExecuteInName != null) {
        this.navigateToGroup(groupToExecuteInName);
        this.getGrouperPage().getPage().locator("#more-action-button").click();
        GrouperUtil.sleep(200);
        this.getGrouperPage().getPage().locator("#groupMoreActionsRunTemplateButton").click();
        this.waitForJspToLoad("newTemplate");
        this.getGrouperPage().getPage().locator("#templateTypeId").selectOption(gshTemplateConfigId);
        this.getGrouperPage().getPage().locator("#filterSubmitId").click();
      }
      else if (stemToExecuteInName != null) {
        this.navigateToStem(stemToExecuteInName);
        this.getGrouperPage().getPage().locator("#moreActionsButton").click();
        GrouperUtil.sleep(200);
        this.getGrouperPage().getPage().locator("#stemMoreActionsRuntTemplateButton").click();
        this.waitForJspToLoad("newTemplate");
        this.getGrouperPage().getPage().locator("#templateTypeId").selectOption(gshTemplateConfigId);
        this.getGrouperPage().getPage().locator("#filterSubmitId").click();
      }
      
      this.waitForJspToLoad("newTemplate");
      
      for (String inputId : gshTemplateInputValueMap.keySet()) {
        String value = gshTemplateInputValueMap.get(inputId);
        Locator inputLocator = this.getGrouperPage().getPage().locator("#config_" + inputId + "_id");
        String htmlTagName = inputLocator.getAttribute("data-gr-input-type");
        
        if (StringUtils.equals("password", htmlTagName) || StringUtils.equals("text", htmlTagName) ||
              StringUtils.equals("textarea", htmlTagName)) {
          inputLocator.fill(value);
        }
        else if (StringUtils.equals("select", htmlTagName)) {
          inputLocator.selectOption(value);
          this.waitForJspToLoad("newTemplate");
        }
      }
      this.getGrouperPage().getPage().locator("#filterSubmitId").click();

      // wait for template to run
      int millisToWait = secondsToWait * 1000;
      GrouperUtil.sleep(millisToWait);

      for (Locator alert : this.getGrouperPage().getPage().locator("#messaging")
          .locator("[role=\"alert\"]").all()) {
        String messageTextHtml = alert.locator(".messageText").innerHTML();
        String[] messageTexts = GrouperUtil.splitTrim(messageTextHtml, "<br>");
        for (String messageText : messageTexts) {
          if ((alert.getAttribute("class")).equals("alert alert-success")) {
            messageContentSuccesses.add(messageText);
          }
          if ((alert.getAttribute("class")).equals("alert alert-info")) {
            messageContentInfos.add(messageText);
          }
          if ((alert.getAttribute("class")).equals("alert alert-error")) {
            messageContentErrors.add(messageText);
          }
        }
      }
      return this;

    }
  }
