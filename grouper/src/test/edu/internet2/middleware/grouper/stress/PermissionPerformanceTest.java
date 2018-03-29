/**
 * Copyright 2016 Internet2
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
package edu.internet2.middleware.grouper.stress;

import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.internal.dao.RegistrySubjectDAO;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.PermissionAllowed;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.permissions.PermissionFinder;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.subject.Subject;

/**
 * @author shilen
 */
public class PermissionPerformanceTest extends GrouperTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new PermissionPerformanceTest("testPerformance"));
  }

  /**
   * 
   */
  public PermissionPerformanceTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public PermissionPerformanceTest(String name) {
    super(name);
  }
  
  /**
   * 
   */
  public void testPerformance() {
    
    System.out.println("Starting permission performance testing: " + new Date());

    try {
      GrouperSession grouperSession = GrouperSession.start(SubjectFinder.findRootSubject());
      Stem root = StemFinder.findRootStem(grouperSession);
      Stem top = root.addChildStem("top", "top display name");
      Role group = top.addChildRole("group", "group");
            
      Set<String> subjectIds = new LinkedHashSet<String>();
      RegistrySubjectDAO dao = GrouperDAOFactory.getFactory().getRegistrySubject();
      for (int i = 0; i < 10000; i++) {
        String id = "id-" + i;
        String name = "name-" + i;
        RegistrySubject subject = new RegistrySubject();
        subject.setId(id);
        subject.setName(name);
        subject.setTypeString("person");
        dao.create(subject);
        subjectIds.add(id);
      }

      System.out.println("Done loading registry subjects: " + new Date());

      Set<Subject> subjects = new LinkedHashSet<Subject>(SubjectFinder.findByIds(subjectIds).values());
      assertEquals(10000, subjects.size());
      
      for (Subject subject : subjects) {
        group.addMember(subject, true);
      }

      System.out.println("Done adding members: " + new Date());

      AttributeDef permissionDef = top.addChildAttributeDef("attribute def", AttributeDefType.perm);
      permissionDef.setAssignToGroup(true);
      permissionDef.store();
      
      permissionDef.getAttributeDefActionDelegate().addAction("read");
      permissionDef.getAttributeDefActionDelegate().addAction("write");
      permissionDef.getAttributeDefActionDelegate().addAction("own");
      
      for (int i = 0; i < 5000; i++) {
        top.addChildAttributeDefName(permissionDef, "attr-" + i, "attr-" + i);
      }

      System.out.println("Done loading attribute def names: " + new Date());

      AttributeDefName parentAttributeDefName = top.addChildAttributeDefName(permissionDef, "parent", "parent");
      for (int i = 0; i < 1000; i++) {
        AttributeDefName childAttributeDefName = top.addChildAttributeDefName(permissionDef, "child-" + i, "child-" + i);
        parentAttributeDefName.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(childAttributeDefName);
      }

      System.out.println("Done loading child attribute def names: " + new Date());

      group.getPermissionRoleDelegate().assignRolePermission("read", parentAttributeDefName, PermissionAllowed.ALLOWED).getAttributeAssign(); 

      System.out.println("Done adding role permissions: " + new Date());

      ChangeLogTempToEntity.convertRecords();
      
      System.out.println("Done populating data.  Now testing: " + new Date());

      long start = System.currentTimeMillis();
      Set<PermissionEntry> permissions = new PermissionFinder()
        .assignPermissionType(PermissionType.role_subject)
        .addPermissionName("top:child-0")
        .assignPointInTimeFrom(new Timestamp(System.currentTimeMillis() - 10000000))
        .assignEnabled(true)
        .findPermissions();
      long end = System.currentTimeMillis();
      long diff = end - start;
      
      assertEquals(10000, permissions.size());
      System.out.println("Time (ms) to get all permissions in point in time with permission name top:child-0: " + diff);
      assertTrue("Query should take less than 90 seconds, actual time in ms=" + diff, diff < 90000);
      
      start = System.currentTimeMillis();
      permissions = new PermissionFinder()
        .assignPermissionType(PermissionType.role_subject)
        .addPermissionName("top:child-0")
        .assignEnabled(true)
        .findPermissions();
      end = System.currentTimeMillis();
      diff = end - start;
      
      assertEquals(10000, permissions.size());
      System.out.println("Time (ms) to get all permissions with permission name top:child-0: " + diff);
      assertTrue("Query should take less than 90 seconds, actual time in ms=" + diff, diff < 90000);
      
      start = System.currentTimeMillis();
      permissions = new PermissionFinder()
        .assignPermissionType(PermissionType.role_subject)
        .addMemberId(MemberFinder.findBySubject(grouperSession, subjects.iterator().next(), false).getUuid())
        .assignPointInTimeFrom(new Timestamp(System.currentTimeMillis() - 10000000))
        .assignEnabled(true)
        .findPermissions();
      end = System.currentTimeMillis();
      diff = end - start;
      
      assertEquals(1001, permissions.size());
      System.out.println("Time (ms) to get all permissions in point in time for an individual subject: " + diff);
      assertTrue("Query should take less than 90 seconds, actual time in ms=" + diff, diff < 90000);
      
      start = System.currentTimeMillis();
      permissions = new PermissionFinder()
        .assignPermissionType(PermissionType.role_subject)
        .addMemberId(MemberFinder.findBySubject(grouperSession, subjects.iterator().next(), false).getUuid())
        .assignEnabled(true)
        .findPermissions();
      end = System.currentTimeMillis();
      diff = end - start;
      
      assertEquals(1001, permissions.size());
      System.out.println("Time (ms) to get all permissions for an individual subject: " + diff);
      assertTrue("Query should take less than 90 seconds, actual time in ms=" + diff, diff < 90000);
      
      
      start = System.currentTimeMillis();
      permissions = new PermissionFinder()
        .assignPermissionType(PermissionType.role)
        .addPermissionName("top:child-0")
        .assignEnabled(true)
        .findPermissions();
      end = System.currentTimeMillis();
      diff = end - start;
      
      assertEquals(1, permissions.size());
      System.out.println("Time (ms) to get a single role assignment: " + diff);
      assertTrue("Query should take less than 90 seconds, actual time in ms=" + diff, diff < 90000);      
    } finally {
      System.out.println("Done permission performance testing");
    }
  }
}
