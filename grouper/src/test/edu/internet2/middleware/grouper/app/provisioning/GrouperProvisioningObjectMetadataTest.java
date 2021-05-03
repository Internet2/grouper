package edu.internet2.middleware.grouper.app.provisioning;

import junit.framework.TestCase;


public class GrouperProvisioningObjectMetadataTest extends TestCase {

  public GrouperProvisioningObjectMetadataTest(String name) {
    super(name);
  }
  
  public void testGroupNameMatchesRegex() {
    
    assertTrue(GrouperProvisioningObjectMetadata.groupNameMatchesRegex("abc:def:ghi", "groupName matches ^ab.*hi$"));
    assertFalse(GrouperProvisioningObjectMetadata.groupNameMatchesRegex("abc:def:ghi", "groupName matches ^ab.*hj$"));
    assertTrue(GrouperProvisioningObjectMetadata.groupNameMatchesRegex("abc:def:ghi", "groupName not matches ^ab.*hj$"));
    assertFalse(GrouperProvisioningObjectMetadata.groupNameMatchesRegex("abc:def:ghi", "groupName not matches ^ab.*hi$"));

    assertTrue(GrouperProvisioningObjectMetadata.groupNameMatchesRegex("abc:def:ghi", "groupExtension matches ^g.*i$"));
    assertFalse(GrouperProvisioningObjectMetadata.groupNameMatchesRegex("abc:def:ghi", "groupExtension matches ^g.*j$"));
    assertTrue(GrouperProvisioningObjectMetadata.groupNameMatchesRegex("abc:def:ghi", "groupExtension not matches ^g.*j$"));
    assertFalse(GrouperProvisioningObjectMetadata.groupNameMatchesRegex("abc:def:ghi", "groupExtension not matches ^g.*i$"));

    assertTrue(GrouperProvisioningObjectMetadata.groupNameMatchesRegex("abc:def:ghi", "folderExtension matches ^d.*f$"));
    assertFalse(GrouperProvisioningObjectMetadata.groupNameMatchesRegex("abc:def:ghi", "folderExtension matches ^d.*g$"));
    assertTrue(GrouperProvisioningObjectMetadata.groupNameMatchesRegex("abc:def:ghi", "folderExtension not matches ^d.*g$"));
    assertFalse(GrouperProvisioningObjectMetadata.groupNameMatchesRegex("abc:def:ghi", "folderExtension not matches ^d.*f$"));

    assertTrue(GrouperProvisioningObjectMetadata.groupNameMatchesRegex("abc:def:ghi", "folderName matches ^a.*f$"));
    assertFalse(GrouperProvisioningObjectMetadata.groupNameMatchesRegex("abc:def:ghi", "folderName matches ^a.*g$"));
    assertTrue(GrouperProvisioningObjectMetadata.groupNameMatchesRegex("abc:def:ghi", "folderName not matches ^a.*g$"));
    assertFalse(GrouperProvisioningObjectMetadata.groupNameMatchesRegex("abc:def:ghi", "folderName not matches ^a.*f$"));

    try {
      GrouperProvisioningObjectMetadata.groupNameMatchesRegex("abc:def:ghi", "groupName2 matches ^ab.*hi$");
      fail("This should not be valid");
    } catch (Exception e) {
      // good
    }
    try {
      GrouperProvisioningObjectMetadata.groupNameMatchesRegex("abc:def:ghi", "groupName m2atches ^ab.*hi$");
      fail("This should not be valid");
    } catch (Exception e) {
      // good
    }
    try {
      GrouperProvisioningObjectMetadata.groupNameMatchesRegex("abc:def:ghi", "groupName matches [");
      fail("This should not be valid");
    } catch (Exception e) {
      // good
    }
    
  }

}
