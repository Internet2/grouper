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
import  edu.internet2.middleware.subject.SubjectNotFoundException;
import  java.util.regex.Pattern;

/**
 * @author  blair christensen.
 * @version $Id: Test_U_API_XmlExporter_internal_membershipToXML.java,v 1.2 2007-05-31 19:01:11 blair Exp $
 * @since   1.2.0
 */
public class Test_U_API_XmlExporter_internal_membershipToXML extends GrouperTest {

  // PRIVATE CLASS VARIABLES //
  private Subject         all;
  private Group           child;
  private Field           cList;
  private GroupType       cType;
  private XmlExporter     export;
  private Membership      ms;
  private Stem            parent;
  private GrouperSession  s;


  // TESTING INFRASTRUCTURE //

  public void setUp() {
    super.setUp();
    try {    
      s       = GrouperSession.start( SubjectFinder.findRootSubject() );
      parent  = StemFinder.findRootStem(s).addChildStem("parent", "parent");
      child   = parent.addChildGroup("parent > child", "parent > child");
      cType   = GroupType.createType(s, "custom type");
      child.addType(cType);
      cList   = cType.addList( s, "custom type > custom list", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN ); 
      all     = SubjectFinder.findAllSubject();
      child.addMember(all, cList);
      ms      = MembershipFinder.findImmediateMembership( s, child, all, cList );
      export  = new XmlExporter( s, new java.util.Properties() );
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
   * Verify list <i>name</i> is escaped in output.
   * @since   1.2.0
   */
  public void test_internal_membershipToXML_escapeName() {
    try {
      String xml = export.internal_membershipToXML(ms);
      String pat = "^(?s).*<listName>custom type &gt; custom list</listName>.*$";
      assertTrue( "list name escaped", Pattern.matches(pat, xml) );
    }
    catch (GroupNotFoundException eGNF) {
      fail( eGNF.getMessage() );
    }
    catch (MemberNotFoundException eMNF) {
      fail( eMNF.getMessage() );
    }
    catch (SubjectNotFoundException eSNF) {
      fail( eSNF.getMessage() );
    }
  }
    
} 

