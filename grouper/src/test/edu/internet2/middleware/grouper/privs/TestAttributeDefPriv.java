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

package edu.internet2.middleware.grouper.privs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Test use of the CREATE {@link NamingPrivilege}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestPrivCREATE.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 */
public class TestAttributeDefPriv extends GrouperTest {

  //** logger */
  //private static final Log LOG = GrouperUtil.getLog(TestAttributeDefPriv.class);
  
  /** grouper session */
  private GrouperSession grouperSession;

  /** subject0 can read a different attribute def */
  private Subject subject0;

  /** attribute def is the main attribute def of the test */
  private AttributeDef attributeDef;

  /** attribute def another is a superfluous attribute def */
  private AttributeDef attributeDefAnother;

  /** subject1 can read the attributeDef */
  private Subject subject1;

  /** subject2 can effectively view the attributeDef */
  private Subject subject2;

  /** groupSubject2 holds subject2 and can immediately view the attributeDef */
  private Group groupSubject2;

  /** subject3 can immediately and effectively admin the attribute def */
  private Subject subject3;

  /** groupSubject3 holds subject3 and can immediately admin the attributeDef */
  private Group groupSubject3;

  /** subject4 can view and read the attribute def */
  private Subject subject4;

  /** subject5 can effectively read the attribute def by 2 paths */
  private Subject subject5;

  /** groupSubject5a holds subject5 and can immediately read the attributeDef */
  private Group groupSubject5a;

  /** groupSubject5b holds subject5 and can immediately read the attributeDef */
  private Group groupSubject5b;

  /** subject6 can admin the attributeDef */
  private Subject subject6; 

  /** all groups in order of id */
  private Map<Group, PrivilegeContainer> allGroupsMap;

  /** all groups in order of id */
  private List<Group> allGroups;
  
  /**
   * 
   * @param name
   */
  public TestAttributeDefPriv(String name) {
    super(name);
  }

  /**
   * 
   */
  @Override
  protected void setUp() {
    super.setUp();

    Map<Group, PrivilegeContainer> theGroupMap = new HashMap<Group, PrivilegeContainer>();
    this.allGroups = new ArrayList<Group>();

    this.grouperSession = GrouperSession.startRootSession();
    
    //subject0 can read a different attribute def
    this.subject0 = SubjectTestHelper.SUBJ0;
    
    //attribute def is the main attribute def of the test
    this.attributeDef = new AttributeDefSave(this.grouperSession)
      .assignAttributeDefNameToEdit("aStem:attributeDef")
      .assignCreateParentStemsIfNotExist(true).save();
    
    //attribute def another is a superfluous attribute def
    this.attributeDefAnother = new AttributeDefSave(this.grouperSession)
      .assignAttributeDefNameToEdit("aStem:attributeDefAnother")
      .assignCreateParentStemsIfNotExist(true).save();
    
    this.attributeDefAnother.getPrivilegeDelegate().grantPriv(this.subject0, AttributeDefPrivilege.ATTR_READ, true);
    
    //subject1 can read the attributeDef
    this.subject1 = SubjectTestHelper.SUBJ1;

    this.attributeDef.getPrivilegeDelegate().grantPriv(this.subject1, AttributeDefPrivilege.ATTR_READ, true);
    
    //subject2 can effectively view the attributeDef
    this.subject2 = SubjectTestHelper.SUBJ2;
    
    //groupSubject2 holds subject2 and can immediately view the attributeDef
    this.groupSubject2 = new GroupSave(this.grouperSession)
      .assignCreateParentStemsIfNotExist(true)
      .assignName("aStem:subject2Group").save();

    this.allGroups.add(this.groupSubject2);
    theGroupMap.put(this.groupSubject2, new PrivilegeContainerImpl(AttributeDefPrivilege.ATTR_VIEW, PrivilegeAssignType.IMMEDIATE));

    this.groupSubject2.addMember(this.subject2);
    
    this.attributeDef.getPrivilegeDelegate().grantPriv(this.groupSubject2.toSubject(), AttributeDefPrivilege.ATTR_VIEW, true);

    //subject3 can immediately and effectively admin the attribute def
    this.subject3 = SubjectTestHelper.SUBJ3;

    //groupSubject3 holds subject3 and can immediately admin the attributeDef
    this.groupSubject3 = new GroupSave(this.grouperSession)
      .assignCreateParentStemsIfNotExist(true)
      .assignName("aStem:subject3Group").save();

    this.allGroups.add(this.groupSubject3);
    theGroupMap.put(this.groupSubject3, new PrivilegeContainerImpl(AttributeDefPrivilege.ATTR_ADMIN, PrivilegeAssignType.IMMEDIATE));

    this.groupSubject3.addMember(this.subject3);
    
    this.attributeDef.getPrivilegeDelegate().grantPriv(this.subject3, AttributeDefPrivilege.ATTR_ADMIN, true);
    this.attributeDef.getPrivilegeDelegate().grantPriv(this.groupSubject3.toSubject(), AttributeDefPrivilege.ATTR_ADMIN, true);
    
    //subject4 can view and read the attribute def
    this.subject4 = SubjectTestHelper.SUBJ4;

    this.attributeDef.getPrivilegeDelegate().grantPriv(this.subject4, AttributeDefPrivilege.ATTR_VIEW, true);
    this.attributeDef.getPrivilegeDelegate().grantPriv(this.subject4, AttributeDefPrivilege.ATTR_READ, true);
    
    //subject5 can effectively read the attribute def by 2 paths
    this.subject5 = SubjectTestHelper.SUBJ5;

    //groupSubject5a holds subject5 and can immediately read the attributeDef
    this.groupSubject5a = new GroupSave(this.grouperSession)
      .assignCreateParentStemsIfNotExist(true)
      .assignName("aStem:subject5GroupA").save();

    this.allGroups.add(this.groupSubject5a);
    theGroupMap.put(this.groupSubject5a, new PrivilegeContainerImpl(AttributeDefPrivilege.ATTR_READ, PrivilegeAssignType.IMMEDIATE));

    this.groupSubject5a.addMember(this.subject5);

    this.attributeDef.getPrivilegeDelegate().grantPriv(this.groupSubject5a.toSubject(), AttributeDefPrivilege.ATTR_READ, true);

    //groupSubject5b holds subject5 and can immediately read the attributeDef
    groupSubject5b = new GroupSave(this.grouperSession)
      .assignCreateParentStemsIfNotExist(true)
      .assignName("aStem:subject5GroupB").save();

    this.allGroups.add(this.groupSubject5b);
    theGroupMap.put(this.groupSubject5b, new PrivilegeContainerImpl(AttributeDefPrivilege.ATTR_READ, PrivilegeAssignType.IMMEDIATE));

    this.groupSubject5b.addMember(this.subject5);
  
    this.attributeDef.getPrivilegeDelegate().grantPriv(this.groupSubject5b.toSubject(), AttributeDefPrivilege.ATTR_READ, true);

    // subject6 can admin the attributeDef 
    this.subject6 = SubjectTestHelper.SUBJ6;

    this.attributeDef.getPrivilegeDelegate().grantPriv(this.subject6, AttributeDefPrivilege.ATTR_ADMIN, true);
    
    //sort by uuid
    Collections.sort(this.allGroups, new Comparator<Group>() {

      public int compare(Group o1, Group o2) {
        return o1.getUuid().compareTo(o2.getUuid());
      }
      
    });
    
    this.allGroupsMap = new LinkedHashMap<Group, PrivilegeContainer>();
    //these need to go in in order
    for (Group group : this.allGroups) {
      this.allGroupsMap.put(group, theGroupMap.get(group));
    }
    
  }

  /**
   * Method main.
   * @param args String[]
   */
  public static void main(String[] args) {
    TestRunner.run(new TestAttributeDefPriv("testRetrieveAll"));
    //TestRunner.run(TestAttributeDefPrivRetrieve.class);
  }

  /**
   * 
   */
  public void testRetrieveAll() {


    //ok, lets get all as admin
    List<PrivilegeSubjectContainer> subjectPrivilegeContainers = new ArrayList<PrivilegeSubjectContainer>(this.grouperSession
      .getAttributeDefResolver().retrievePrivileges(this.attributeDef, null, null, null, null));
    
    //should be 9, subject 1,2,3,4,5,6, grouperSystem, groupSubject2, groupSubject3, groupSubject5a, groupSubject5b
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), 11, GrouperUtil.length(subjectPrivilegeContainers));
    PrivilegeSubjectContainer privilegeSubjectContainer = null;
    //first should be first group
    for (int i=0; i<this.allGroups.size(); i++) {
      privilegeSubjectContainer = subjectPrivilegeContainers.get(i);
      Group group = this.allGroups.get(i);
      assertEquals(i + ", " + GrouperUtil.collectionToString(subjectPrivilegeContainers), group.getId(), 
          privilegeSubjectContainer.getSubject().getId());
      assertEquals(i + ", " + GrouperUtil.collectionToString(subjectPrivilegeContainers), 1, 
          privilegeSubjectContainer.getPrivilegeContainers().size());
      assertEquals(i + ", " + GrouperUtil.collectionToString(subjectPrivilegeContainers), 
          this.allGroupsMap.get(group).getPrivilege().getName(), privilegeSubjectContainer.getPrivilegeContainers().keySet().iterator().next());
      assertEquals(i + ", " + GrouperUtil.collectionToString(subjectPrivilegeContainers), 
          this.allGroupsMap.get(group).getPrivilegeAssignType(), privilegeSubjectContainer.getPrivilegeContainers().get(this.allGroupsMap.get(group).getPrivilege().getName()).getPrivilegeAssignType());
    }
    
    //fifth should be grouperSystem with admin immediate
    privilegeSubjectContainer = subjectPrivilegeContainers.get(4);
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), SubjectFinder.findRootSubject().getId(), privilegeSubjectContainer.getSubject().getId());
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), 1, privilegeSubjectContainer.getPrivilegeContainers().size());
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), AttributeDefPrivilege.ATTR_ADMIN.getName(), privilegeSubjectContainer.getPrivilegeContainers().keySet().iterator().next());
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), PrivilegeAssignType.IMMEDIATE, privilegeSubjectContainer.getPrivilegeContainers().get(AttributeDefPrivilege.ATTR_ADMIN.getName()).getPrivilegeAssignType());

    //subject1 can read the attributeDef
    privilegeSubjectContainer = subjectPrivilegeContainers.get(5);
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), this.subject1.getId(), privilegeSubjectContainer.getSubject().getId());
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), 1, privilegeSubjectContainer.getPrivilegeContainers().size());
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), AttributeDefPrivilege.ATTR_READ.getName(), privilegeSubjectContainer.getPrivilegeContainers().keySet().iterator().next());
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), PrivilegeAssignType.IMMEDIATE, privilegeSubjectContainer.getPrivilegeContainers().get(AttributeDefPrivilege.ATTR_READ.getName()).getPrivilegeAssignType());
    
    //subject2 can effectively view the attributeDef
    privilegeSubjectContainer = subjectPrivilegeContainers.get(6);
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), this.subject2.getId(), privilegeSubjectContainer.getSubject().getId());
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), 1, privilegeSubjectContainer.getPrivilegeContainers().size());
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), AttributeDefPrivilege.ATTR_VIEW.getName(), privilegeSubjectContainer.getPrivilegeContainers().keySet().iterator().next());
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), PrivilegeAssignType.EFFECTIVE, privilegeSubjectContainer.getPrivilegeContainers().get(AttributeDefPrivilege.ATTR_VIEW.getName()).getPrivilegeAssignType());
    
    //subject3 can immediately and effectively admin the attribute def
    privilegeSubjectContainer = subjectPrivilegeContainers.get(7);
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), this.subject3.getId(), privilegeSubjectContainer.getSubject().getId());
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), 1, privilegeSubjectContainer.getPrivilegeContainers().size());
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), AttributeDefPrivilege.ATTR_ADMIN.getName(), privilegeSubjectContainer.getPrivilegeContainers().keySet().iterator().next());
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), PrivilegeAssignType.IMMEDIATE_AND_EFFECTIVE, privilegeSubjectContainer.getPrivilegeContainers().get(AttributeDefPrivilege.ATTR_ADMIN.getName()).getPrivilegeAssignType());
    
    //subject4 can view and read the attribute def
    privilegeSubjectContainer = subjectPrivilegeContainers.get(8);
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), this.subject4.getId(), privilegeSubjectContainer.getSubject().getId());
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), 2, privilegeSubjectContainer.getPrivilegeContainers().size());
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), AttributeDefPrivilege.ATTR_READ.getName(), privilegeSubjectContainer.getPrivilegeContainers().keySet().iterator().next());
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), PrivilegeAssignType.IMMEDIATE, privilegeSubjectContainer.getPrivilegeContainers().get(AttributeDefPrivilege.ATTR_READ.getName()).getPrivilegeAssignType());
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), AttributeDefPrivilege.ATTR_VIEW.getName(), GrouperUtil.get(privilegeSubjectContainer.getPrivilegeContainers().keySet(), 1));
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), PrivilegeAssignType.IMMEDIATE, privilegeSubjectContainer.getPrivilegeContainers().get(AttributeDefPrivilege.ATTR_VIEW.getName()).getPrivilegeAssignType());
    
    //subject5 can effectively read the attribute def by 2 paths
    privilegeSubjectContainer = subjectPrivilegeContainers.get(9);
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), this.subject5.getId(), privilegeSubjectContainer.getSubject().getId());
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), 1, privilegeSubjectContainer.getPrivilegeContainers().size());
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), AttributeDefPrivilege.ATTR_READ.getName(), privilegeSubjectContainer.getPrivilegeContainers().keySet().iterator().next());
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), PrivilegeAssignType.EFFECTIVE, privilegeSubjectContainer.getPrivilegeContainers().get(AttributeDefPrivilege.ATTR_READ.getName()).getPrivilegeAssignType());
    
    //subject6 can admin the attributeDef 
    privilegeSubjectContainer = subjectPrivilegeContainers.get(10);
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), this.subject6.getId(), privilegeSubjectContainer.getSubject().getId());
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), 1, privilegeSubjectContainer.getPrivilegeContainers().size());
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), AttributeDefPrivilege.ATTR_ADMIN.getName(), privilegeSubjectContainer.getPrivilegeContainers().keySet().iterator().next());
    assertEquals(GrouperUtil.collectionToString(subjectPrivilegeContainers), PrivilegeAssignType.IMMEDIATE, privilegeSubjectContainer.getPrivilegeContainers().get(AttributeDefPrivilege.ATTR_ADMIN.getName()).getPrivilegeAssignType());

    
  }

} 

