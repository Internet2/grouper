/**
 * Copyright 2018 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.xml.export;

import java.io.File;
import java.io.IOException;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * test exporting a gsh script for one or multiple stems
 */
public class XmlExportGshScriptTest extends GrouperTest {

  /** grouperSession */
  private GrouperSession grouperSession;

  /** file the script is written to */
  private File gshScriptFile;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new XmlExportGshScriptTest("testExportMultipleStems"));
  }

  /**
   * @param name
   */
  public XmlExportGshScriptTest(String name) {
    super(name);
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();

    this.grouperSession = GrouperSession.startRootSession();

    try {
      this.gshScriptFile = File.createTempFile("grouperGshScriptExport", ".gsh");
    } catch (IOException ioe) {
      throw new RuntimeException("Cant create temp file", ioe);
    }
    //the file must not exist, XmlExportGshScript creates it
    this.gshScriptFile.delete();
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {

    if (this.gshScriptFile != null) {
      this.gshScriptFile.delete();
    }

    GrouperSession.stopQuietly(this.grouperSession);

    super.tearDown();
  }

  /**
   * all objects under each added stem should be in one script, and nothing else
   */
  public void testExportMultipleStems() {

    new GroupSave(this.grouperSession).assignName("gshScriptTest1:groupInStemOne")
      .assignCreateParentStemsIfNotExist(true).save();

    new GroupSave(this.grouperSession).assignName("gshScriptTest2:groupInStemTwo")
      .assignCreateParentStemsIfNotExist(true).save();

    new GroupSave(this.grouperSession).assignName("gshScriptTest3:groupInStemThree")
      .assignCreateParentStemsIfNotExist(true).save();

    new XmlExportGshScript().addStemName("gshScriptTest1").addStemName("gshScriptTest2")
      .assignFileNameToWriteTo(GrouperUtil.fileCanonicalPath(this.gshScriptFile)).exportGsh();

    String script = GrouperUtil.readFileIntoString(this.gshScriptFile);

    assertTrue(script.contains("gshScriptTest1:groupInStemOne"));
    assertTrue(script.contains("gshScriptTest2:groupInStemTwo"));
    assertFalse(script.contains("gshScriptTest3:groupInStemThree"));

  }

  /**
   * assignStemName replaces stems which were added before it
   */
  public void testAssignStemNameReplacesStems() {

    new GroupSave(this.grouperSession).assignName("gshScriptTest1:groupInStemOne")
      .assignCreateParentStemsIfNotExist(true).save();

    new GroupSave(this.grouperSession).assignName("gshScriptTest2:groupInStemTwo")
      .assignCreateParentStemsIfNotExist(true).save();

    new XmlExportGshScript().addStemName("gshScriptTest1").assignStemName("gshScriptTest2")
      .assignFileNameToWriteTo(GrouperUtil.fileCanonicalPath(this.gshScriptFile)).exportGsh();

    String script = GrouperUtil.readFileIntoString(this.gshScriptFile);

    assertFalse(script.contains("gshScriptTest1:groupInStemOne"));
    assertTrue(script.contains("gshScriptTest2:groupInStemTwo"));

  }

  /**
   * a stem which does not exist yet should still be exported by name
   */
  public void testExportStemNameNotFound() {

    new GroupSave(this.grouperSession).assignName("gshScriptTest1:groupInStemOne")
      .assignCreateParentStemsIfNotExist(true).save();

    new XmlExportGshScript().addStemName("gshScriptTest1").addStemName("gshScriptTestNotThere")
      .assignFileNameToWriteTo(GrouperUtil.fileCanonicalPath(this.gshScriptFile)).exportGsh();

    String script = GrouperUtil.readFileIntoString(this.gshScriptFile);

    assertTrue(script.contains("gshScriptTest1:groupInStemOne"));

  }

  /**
   * if the root stem is one of the stems then everything is exported
   */
  public void testExportRootStemWithOtherStems() {

    new GroupSave(this.grouperSession).assignName("gshScriptTest1:groupInStemOne")
      .assignCreateParentStemsIfNotExist(true).save();

    new GroupSave(this.grouperSession).assignName("gshScriptTest3:groupInStemThree")
      .assignCreateParentStemsIfNotExist(true).save();

    new XmlExportGshScript().addStemName("gshScriptTest1").addStemName(":")
      .assignFileNameToWriteTo(GrouperUtil.fileCanonicalPath(this.gshScriptFile)).exportGsh();

    String script = GrouperUtil.readFileIntoString(this.gshScriptFile);

    assertTrue(script.contains("gshScriptTest1:groupInStemOne"));
    assertTrue(script.contains("gshScriptTest3:groupInStemThree"));

  }

  /**
   * a stem or object name is required
   */
  public void testNoStemOrObjectName() {

    try {
      new XmlExportGshScript()
        .assignFileNameToWriteTo(GrouperUtil.fileCanonicalPath(this.gshScriptFile)).exportGsh();
      fail("should not get here");
    } catch (RuntimeException re) {
      assertTrue(re.getMessage(), re.getMessage().contains("A stem or objectName is required"));
    }

  }

}
