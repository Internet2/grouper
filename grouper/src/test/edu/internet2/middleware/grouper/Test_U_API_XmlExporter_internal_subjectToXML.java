/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;
import  edu.internet2.middleware.subject.Subject;
import  java.util.regex.Pattern;

/**
 * @author  blair christensen.
 * @version $Id: Test_U_API_XmlExporter_internal_subjectToXML.java,v 1.1 2007-05-21 17:25:02 blair Exp $
 * @since   1.2.0
 */
public class Test_U_API_XmlExporter_internal_subjectToXML extends GrouperTest {

  // PRIVATE CLASS VARIABLES //
  private Group           child;
  private Subject         childAsSubject;
  private GrouperSession  s;
  private Stem            parent;
  private XmlExporter     export;


  // TESTING INFRASTRUCTURE //

  public void setUp() {
    super.setUp();
    try {
      // TODO 20070521 this *really* cries out for an object mother     
      s               = GrouperSession.start( SubjectFinder.findRootSubject() );
      parent          = StemFinder.findRootStem(s).addChildStem("parent", "parent");
      child           = parent.addChildGroup("parent > child", "parent > child");
      childAsSubject  = child.toSubject();
      export          = new XmlExporter( s, new java.util.Properties() );
    }
    catch (Exception eShouldNotHappen) {
      throw new GrouperRuntimeException( eShouldNotHappen.getMessage(), eShouldNotHappen );
    }
  }

  public void tearDown() {
    try {
      s.stop();
    }
    catch (Exception eShouldNotHappen) {
      throw new GrouperRuntimeException( eShouldNotHappen.getMessage(), eShouldNotHappen );
    }
    super.tearDown();
  }


  // TESTS //

  /**
   * Verify <i>name</i> is escaped in output.
   * @since   1.2.0
   */
  public void test_internal_subjectToXML_escapeIdentifier() {
    String xml = export.internal_subjectToXML( childAsSubject, GrouperConfig.EMPTY_STRING );
    String pat = "^(?s).*\\sidentifier='parent:parent &gt; child'.*$";
    assertTrue( "identifier escaped", Pattern.matches(pat, xml) );
  }
    
} 

