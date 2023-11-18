package edu.internet2.middleware.grouper.misc;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;


public class GrouperVersionTest extends GrouperTest {

  public static void main(String[] args) {
    TestRunner.run(new GrouperVersionTest("testGrouperVersionTest"));
  }
  
  public GrouperVersionTest(String name) {
    super(name);
  }

  public void testGrouperVersionTest() {
    GrouperVersion grouperVersion = new GrouperVersion("1.2.3");
    assertEquals(1, grouperVersion.getMajor());
    assertEquals(2, grouperVersion.getMinor());
    assertEquals(3, grouperVersion.getBuild());
    assertNull(grouperVersion.getRc());

    grouperVersion = new GrouperVersion("1.2.3.4");
    assertEquals(1, grouperVersion.getMajor());
    assertEquals(2, grouperVersion.getMinor());
    assertEquals(3, grouperVersion.getBuild());
    assertEquals(4, grouperVersion.getPatch().intValue());

    grouperVersion = new GrouperVersion("1.2.3rc4");
    assertEquals(1, grouperVersion.getMajor());
    assertEquals(2, grouperVersion.getMinor());
    assertEquals(3, grouperVersion.getBuild());
    assertEquals(4, grouperVersion.getPatch().intValue());

    grouperVersion = new GrouperVersion("1.2.3-SNAPSHOT");
    assertEquals(1, grouperVersion.getMajor());
    assertEquals(2, grouperVersion.getMinor());
    assertEquals(3, grouperVersion.getBuild());
    assertEquals(-1, grouperVersion.getPatch().intValue());

  }
}
