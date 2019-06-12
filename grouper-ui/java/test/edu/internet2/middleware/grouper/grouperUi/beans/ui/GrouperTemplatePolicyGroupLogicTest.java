/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.List;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.grouperUi.serviceLogic.UiV2Template;
import edu.internet2.middleware.grouper.helper.GrouperTest;


/**
 *
 */
public class GrouperTemplatePolicyGroupLogicTest extends GrouperTest {

  /**
   * @param name
   */
  public GrouperTemplatePolicyGroupLogicTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperTemplatePolicyGroupLogicTest("testNewPolicy"));
  }
  
  /**
   * 
   */
  public void testNewPolicy() {
    String templateType = "policyGroup";
    String stemName = "aStem";
    String templateKey = "policyGroup1";
    String serviceDescription = "Policy Group 1 is the policy";
    String serviceFriendlyName = "Policy Group 1";
    boolean createSubFolder = true;
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignName(stemName).save();

    GrouperRequestContainer.assignUseStaticRequestContainer(true);
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    StemTemplateContainer stemTemplateContainer = grouperRequestContainer.getStemTemplateContainer();
    GrouperTemplateLogicBase templateLogic = UiV2Template.getTemplateLogic(templateType, stemTemplateContainer);
    templateLogic.setStemId(stem.getUuid());
    stemTemplateContainer.setCreateNoSubfolder(!createSubFolder);
    stemTemplateContainer.setTemplateKey(templateKey);
    stemTemplateContainer.setTemplateDescription(serviceDescription);
    stemTemplateContainer.setTemplateFriendlyName(serviceFriendlyName);
    List<ServiceAction> allServiceActions = templateLogic.getServiceActions();
    // if (templateLogic.validate(allServiceActions)) { throw new RuntimeException("Not valid"); } // dont include this line for some reason
    for (ServiceAction serviceAction: allServiceActions) { serviceAction.getServiceActionType().createTemplateItem(serviceAction); }
  }
  
}
