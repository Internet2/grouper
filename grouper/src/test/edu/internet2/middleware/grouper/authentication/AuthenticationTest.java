package edu.internet2.middleware.grouper.authentication;

import java.util.Base64;

import org.junit.Assert;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.j2ee.Authentication;
import junit.textui.TestRunner;

public class AuthenticationTest extends GrouperTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AuthenticationTest("testAuthentication"));    
  }
  
  public AuthenticationTest() {
    super();
  }
  
  /**
   * 
   * @param name
   */
  public AuthenticationTest(String name) {
    super(name);
  }
  
  public void testAuthentication() {
    
    GrouperSession grouperSession = SessionHelper.getRootSession();
    
    GrouperPasswordSave grouperPasswordSave = new GrouperPasswordSave();
    grouperPasswordSave.assignUsername("GrouperSystem").assignPassword("admin123").assignEntityType("username");
    grouperPasswordSave.assignApplication(GrouperPassword.Application.UI).save();

    
    String base64Encoded = Base64.getEncoder().encodeToString("GrouperSystem:admin123".getBytes());
    String authHeader = "Basic "+base64Encoded;
    
    //when
    boolean isValid = new Authentication().authenticate(authHeader, GrouperPassword.Application.UI, "127.0.0.1");
    
    //then
    Assert.assertTrue(isValid);
    
  }

}
