/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperVoot.restLogic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouperVoot.VootRestHttpMethod;
import edu.internet2.middleware.grouperVoot.beans.VootGroup;
import edu.internet2.middleware.grouperVoot.messages.VootGetGroupsResponse;


/**
 *
 */
public class VootWsRestTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new VootWsRestTest("testMemberInOneGroup"));
  }
  
  /**
   * 
   */
  public VootWsRestTest() {
    super();
    
  }

  /**
   * @param name
   */
  public VootWsRestTest(String name) {
    super(name);
  }

  /**
   * note: running this will delete all data in the registry!
   * /groups/test.subject.0
   */
  public void testMemberInOneGroup() {

    //setup data as root user
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    //group where the user is a member and they can read it (only one returned)
    Group group = new GroupSave(grouperSession).assignName("aStem:aGroup")
        .assignDescription("some description").assignCreateParentStemsIfNotExist(true).save();
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    group.addMember(SubjectTestHelper.SUBJ0, false);

    //group where user isnt a member and cant read
    group = new GroupSave(grouperSession).assignName("aStem:aGroup2")
        .assignDescription("some description2").assignCreateParentStemsIfNotExist(true).save();
    group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ, false);
    group.addMember(SubjectTestHelper.SUBJ1, false);

    //group where user is a member and cant read
    group = new GroupSave(grouperSession).assignName("aStem:aGroup3")
        .assignDescription("some description3").assignCreateParentStemsIfNotExist(true).save();
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    group.addMember(SubjectTestHelper.SUBJ1, false);
    
    //group where user isnt a member and can read
    group = new GroupSave(grouperSession).assignName("aStem:aGroup4")
        .assignDescription("some description4").assignCreateParentStemsIfNotExist(true).save();
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    group.addMember(SubjectTestHelper.SUBJ1, false);
    
    GrouperSession.stopQuietly(grouperSession);

    
    //start session as logged in user to web service
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    // URL: /groups/test.subject.0
    List<String> urlStrings = GrouperUtil.toList("groups", "test.subject.0");

    String resource = GrouperServiceUtils.popUrlString(urlStrings);
    VootRestHttpMethod vootRestHttpMethod = VootRestHttpMethod.valueOfIgnoreCase("GET", true);

    // validate and get the operation
    VootWsRest vootWsRest = VootWsRest.valueOfIgnoreCase(resource, false);

    Map<String, String[]> urlParamMap = new HashMap<String, String[]>();
    
    //add params here
    
    // main business logic method
    Object resultObject = vootWsRest.service(urlStrings, vootRestHttpMethod, urlParamMap);

    //analyze the result
    assertNotNull(resultObject);
    
    assertTrue(resultObject.getClass().toString(), resultObject instanceof VootGetGroupsResponse);
    
    VootGetGroupsResponse vootGetGroupsResponse = (VootGetGroupsResponse)resultObject;
    
    assertEquals(1, vootGetGroupsResponse.getTotalResults().intValue());

    assertEquals(1, GrouperUtil.length(vootGetGroupsResponse.getEntry()));
    
    VootGroup vootGroup = vootGetGroupsResponse.getEntry()[0];
    
    assertEquals("aStem:aGroup", vootGroup.getId());
    assertEquals("aStem:aGroup", vootGroup.getName());
    assertEquals("some description", vootGroup.getDescription());
    assertEquals("member", vootGroup.getVoot_membership_role());


    GrouperSession.stopQuietly(grouperSession);
    
  }
  
}
