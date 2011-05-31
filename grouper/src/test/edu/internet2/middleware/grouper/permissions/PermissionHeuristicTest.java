/**
 * 
 */
package edu.internet2.middleware.grouper.permissions;

import java.util.List;

import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.permissions.PermissionHeuristic.PermissionHeuristicType;
import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 * @author mchyzer
 *
 */
public class PermissionHeuristicTest extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(PermissionHeuristicTest.class);
    //TestRunner.run(new PermissionHeuristicTest("testNumbers"));
  }
  
  /**
   * @param name
   */
  public PermissionHeuristicTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testMaxScore() {
    PermissionHeuristic.maxDepth();
    int oldDepth = PermissionHeuristic.maxDepth;
    try {
      PermissionHeuristic.maxDepth = 30;
      //    * Max depth resource: 30 (configurable in grouper.properties)

      //    * (1) allow
      assertEquals(1, PermissionHeuristicType.allow.maxScore());
      
      List<PermissionHeuristic> permissionHeuristics = PermissionHeuristic.computeHeuristics(1).getPermissionHeuristicList();
      assertEquals(1, permissionHeuristics.size());
      assertEquals(PermissionHeuristicType.allow, permissionHeuristics.get(0).getPermissionHeuristicType());
      
      //    *
      //    * (60) direct action assignment (2*30)
      assertEquals(60, PermissionHeuristicType.action.maxScore());

      permissionHeuristics = PermissionHeuristic.computeHeuristics(60).getPermissionHeuristicList();
      assertEquals(1, permissionHeuristics.size());
      assertEquals(PermissionHeuristicType.action, permissionHeuristics.get(0).getPermissionHeuristicType());
      assertEquals(0, permissionHeuristics.get(0).getDepth());
      
      //    *
      //    * (3600) direct resource assignment (120*30)
      assertEquals(3600, PermissionHeuristicType.resource.maxScore());

      permissionHeuristics = PermissionHeuristic.computeHeuristics(3600).getPermissionHeuristicList();
      assertEquals(1, permissionHeuristics.size());
      assertEquals(PermissionHeuristicType.resource, permissionHeuristics.get(0).getPermissionHeuristicType());
      assertEquals(0, permissionHeuristics.get(0).getDepth());
      
      //    * 
      //    * (7200) assignment to user as opposed to group
      assertEquals(7200, PermissionHeuristicType.assignedToUserNotGroup.maxScore());

      permissionHeuristics = PermissionHeuristic.computeHeuristics(7200).getPermissionHeuristicList();
      assertEquals(1, permissionHeuristics.size());
      assertEquals(PermissionHeuristicType.assignedToUserNotGroup, permissionHeuristics.get(0).getPermissionHeuristicType());
      
      //    * 
      //    * (432000) direct role assignment (14400 * 30)
      assertEquals(432000, PermissionHeuristicType.role.maxScore());

      permissionHeuristics = PermissionHeuristic.computeHeuristics(432000).getPermissionHeuristicList();
      assertEquals(PermissionHeuristic.collectionToString(permissionHeuristics), 1, permissionHeuristics.size());
      assertEquals(PermissionHeuristicType.role, permissionHeuristics.get(0).getPermissionHeuristicType());
      assertEquals(0, permissionHeuristics.get(0).getDepth());

      //    * (864000) person/role assignment
      assertEquals(864000, PermissionHeuristicType.personRole.maxScore());

      permissionHeuristics = PermissionHeuristic.computeHeuristics(864000).getPermissionHeuristicList();
      assertEquals(1, permissionHeuristics.size());
      assertEquals(PermissionHeuristicType.personRole, permissionHeuristics.get(0).getPermissionHeuristicType());
  
    } finally {
      PermissionHeuristic.maxDepth = oldDepth;
    }
  }
  
  /**
   * 
   */
  public void testNumbers() {
    //    * Max depth resource: 30 (configurable in grouper.properties)
    //    * 
    //    * (864000) person/role assignment
    //    * 
    //    * (432000) direct role assignment (14400 * 30)
    //    * 
    //    * (14400) role assignment with role depth 29
    //    * 
    //    * (7200) assignment to user as opposed to group
    //    *
    //    * (3600) direct resource assignment (120*30)
    //    * 
    //    * (120) indirect direct resource assignment depth 29
    //    *
    //    * (60) direct action assignment (2*30)
    //    *
    //    * (58) action depth 1
    //    *
    //    * (56) action depth 2
    //    *
    //    * (54) action depth 3
    //    *
    //    * (2) action depth 29
    //    *
    //    * (1) allow
    
    //let sjust do an allow, make sure the depth is set to 30
    PermissionHeuristic.maxDepth();
    int oldDepth = PermissionHeuristic.maxDepth;
    try {
      PermissionHeuristic.maxDepth = 30;
      
      List<PermissionHeuristic> permissionHeuristicsList = null;
      
      //nothing...
      {
        PermissionEntry permissionEntry = new PermissionEntry();
        permissionEntry.setPermissionTypeDb(PermissionType.role.name());
        permissionEntry.setAttributeAssignActionSetDepth(30);
        permissionEntry.setAttributeDefNameSetDepth(30);
        permissionEntry.setRoleSetDepth(30);
        permissionEntry.setAllowed(false);
        permissionEntry.setEnabled(true);
        long score = PermissionHeuristic.computePermissionHeuristic(permissionEntry);
        assertEquals(0, score);
        
        permissionHeuristicsList = PermissionHeuristic.computeHeuristics(0).getPermissionHeuristicList();
        assertEquals(PermissionHeuristic.collectionToString(permissionHeuristicsList), 0, permissionHeuristicsList.size());
        
      } 
      
      //only allowed
      {
        PermissionEntry permissionEntry = new PermissionEntry();
        permissionEntry.setPermissionTypeDb(PermissionType.role.name());
        permissionEntry.setRoleSetDepth(30);
        permissionEntry.setAttributeDefNameSetDepth(30);
        permissionEntry.setAttributeAssignActionSetDepth(30);
        permissionEntry.setAllowed(true);
        permissionEntry.setEnabled(true);
        long score = PermissionHeuristic.computePermissionHeuristic(permissionEntry);
        assertEquals(1, score);
        
        permissionHeuristicsList = PermissionHeuristic.computeHeuristics(1).getPermissionHeuristicList();
        assertEquals(PermissionHeuristic.collectionToString(permissionHeuristicsList), 1, permissionHeuristicsList.size());
        assertEquals(PermissionHeuristicType.allow, permissionHeuristicsList.get(0).getPermissionHeuristicType());
        
      } 
      
      //action set of n-1
      {
        PermissionEntry permissionEntry = new PermissionEntry();
        permissionEntry.setPermissionTypeDb(PermissionType.role.name());
        permissionEntry.setRoleSetDepth(30);
        permissionEntry.setAttributeDefNameSetDepth(30);
        permissionEntry.setAttributeAssignActionSetDepth(29);
        permissionEntry.setAllowed(true);
        permissionEntry.setEnabled(true);
        long score = PermissionHeuristic.computePermissionHeuristic(permissionEntry);
        assertEquals(3, score);

        permissionHeuristicsList = PermissionHeuristic.computeHeuristics(3).getPermissionHeuristicList();
        assertEquals(PermissionHeuristic.collectionToString(permissionHeuristicsList), 2, permissionHeuristicsList.size());
        assertEquals(PermissionHeuristicType.action, permissionHeuristicsList.get(0).getPermissionHeuristicType());
        assertEquals(29, permissionHeuristicsList.get(0).getDepth());
        assertEquals(PermissionHeuristicType.allow, permissionHeuristicsList.get(1).getPermissionHeuristicType());
      
      } 
      
      //action set of n-2
      {
        PermissionEntry permissionEntry = new PermissionEntry();
        permissionEntry.setPermissionTypeDb(PermissionType.role.name());
        permissionEntry.setRoleSetDepth(30);
        permissionEntry.setAttributeDefNameSetDepth(30);
        permissionEntry.setAttributeAssignActionSetDepth(28);
        permissionEntry.setAllowed(true);
        permissionEntry.setEnabled(true);
        long score = PermissionHeuristic.computePermissionHeuristic(permissionEntry);
        assertEquals(5, score);
        
        permissionHeuristicsList = PermissionHeuristic.computeHeuristics(5).getPermissionHeuristicList();
        assertEquals(PermissionHeuristic.collectionToString(permissionHeuristicsList), 2, permissionHeuristicsList.size());
        assertEquals(PermissionHeuristicType.action, permissionHeuristicsList.get(0).getPermissionHeuristicType());
        assertEquals(28, permissionHeuristicsList.get(0).getDepth());
        assertEquals(PermissionHeuristicType.allow, permissionHeuristicsList.get(1).getPermissionHeuristicType());

        
      } 
      
      //action set of 0
      {
        PermissionEntry permissionEntry = new PermissionEntry();
        permissionEntry.setPermissionTypeDb(PermissionType.role.name());
        permissionEntry.setRoleSetDepth(30);
        permissionEntry.setAttributeDefNameSetDepth(30);
        permissionEntry.setAttributeAssignActionSetDepth(0);
        permissionEntry.setAllowed(true);
        permissionEntry.setEnabled(true);
        long score = PermissionHeuristic.computePermissionHeuristic(permissionEntry);
        assertEquals(61, score);

        permissionHeuristicsList = PermissionHeuristic.computeHeuristics(61).getPermissionHeuristicList();
        assertEquals(PermissionHeuristic.collectionToString(permissionHeuristicsList), 2, permissionHeuristicsList.size());
        assertEquals(PermissionHeuristicType.action, permissionHeuristicsList.get(0).getPermissionHeuristicType());
        assertEquals(0, permissionHeuristicsList.get(0).getDepth());
        assertEquals(PermissionHeuristicType.allow, permissionHeuristicsList.get(1).getPermissionHeuristicType());

      } 
      
      //action set of 0 deny
      {
        PermissionEntry permissionEntry = new PermissionEntry();
        permissionEntry.setPermissionTypeDb(PermissionType.role.name());
        permissionEntry.setRoleSetDepth(30);
        permissionEntry.setAttributeDefNameSetDepth(30);
        permissionEntry.setAttributeAssignActionSetDepth(0);
        permissionEntry.setAllowed(false);
        permissionEntry.setEnabled(true);
        long score = PermissionHeuristic.computePermissionHeuristic(permissionEntry);
        assertEquals(60, score);

        permissionHeuristicsList = PermissionHeuristic.computeHeuristics(60).getPermissionHeuristicList();
        assertEquals(PermissionHeuristic.collectionToString(permissionHeuristicsList), 1, permissionHeuristicsList.size());
        assertEquals(PermissionHeuristicType.action, permissionHeuristicsList.get(0).getPermissionHeuristicType());
        assertEquals(0, permissionHeuristicsList.get(0).getDepth());
      } 
      
      //resource assignment set of 29 deny
      {
        PermissionEntry permissionEntry = new PermissionEntry();
        permissionEntry.setPermissionTypeDb(PermissionType.role.name());
        permissionEntry.setRoleSetDepth(30);
        permissionEntry.setAttributeDefNameSetDepth(29);
        permissionEntry.setAttributeAssignActionSetDepth(30);
        permissionEntry.setAllowed(false);
        permissionEntry.setEnabled(true);
        long score = PermissionHeuristic.computePermissionHeuristic(permissionEntry);
        assertEquals(120, score);

        permissionHeuristicsList = PermissionHeuristic.computeHeuristics(120).getPermissionHeuristicList();
        assertEquals(PermissionHeuristic.collectionToString(permissionHeuristicsList), 1, permissionHeuristicsList.size());
        assertEquals(PermissionHeuristicType.resource, permissionHeuristicsList.get(0).getPermissionHeuristicType());
        assertEquals(29, permissionHeuristicsList.get(0).getDepth());

      } 
      
      //resource assignment set of 29 allow
      {
        PermissionEntry permissionEntry = new PermissionEntry();
        permissionEntry.setPermissionTypeDb(PermissionType.role.name());
        permissionEntry.setRoleSetDepth(30);
        permissionEntry.setAttributeDefNameSetDepth(29);
        permissionEntry.setAttributeAssignActionSetDepth(30);
        permissionEntry.setAllowed(true);
        permissionEntry.setEnabled(true);
        long score = PermissionHeuristic.computePermissionHeuristic(permissionEntry);
        assertEquals(121, score);

        permissionHeuristicsList = PermissionHeuristic.computeHeuristics(121).getPermissionHeuristicList();
        assertEquals(PermissionHeuristic.collectionToString(permissionHeuristicsList), 2, permissionHeuristicsList.size());
        assertEquals(PermissionHeuristicType.resource, permissionHeuristicsList.get(0).getPermissionHeuristicType());
        assertEquals(29, permissionHeuristicsList.get(0).getDepth());
        assertEquals(PermissionHeuristicType.allow, permissionHeuristicsList.get(1).getPermissionHeuristicType());
      } 
      
      //resource assignment set of 0 allow
      {
        PermissionEntry permissionEntry = new PermissionEntry();
        permissionEntry.setPermissionTypeDb(PermissionType.role.name());
        permissionEntry.setRoleSetDepth(30);
        permissionEntry.setAttributeDefNameSetDepth(0);
        permissionEntry.setAttributeAssignActionSetDepth(30);
        permissionEntry.setAllowed(true);
        permissionEntry.setEnabled(true);
        long score = PermissionHeuristic.computePermissionHeuristic(permissionEntry);
        assertEquals(3601, score);
        
        permissionHeuristicsList = PermissionHeuristic.computeHeuristics(3601).getPermissionHeuristicList();
        assertEquals(PermissionHeuristic.collectionToString(permissionHeuristicsList), 2, permissionHeuristicsList.size());
        assertEquals(PermissionHeuristicType.resource, permissionHeuristicsList.get(0).getPermissionHeuristicType());
        assertEquals(0, permissionHeuristicsList.get(0).getDepth());
        assertEquals(PermissionHeuristicType.allow, permissionHeuristicsList.get(1).getPermissionHeuristicType());

      } 
      
      //resource assignment set of 0 deny
      {
        PermissionEntry permissionEntry = new PermissionEntry();
        permissionEntry.setPermissionTypeDb(PermissionType.role.name());
        permissionEntry.setRoleSetDepth(30);
        permissionEntry.setAttributeDefNameSetDepth(0);
        permissionEntry.setAttributeAssignActionSetDepth(30);
        permissionEntry.setAllowed(false);
        permissionEntry.setEnabled(true);
        long score = PermissionHeuristic.computePermissionHeuristic(permissionEntry);
        assertEquals(3600, score);

        permissionHeuristicsList = PermissionHeuristic.computeHeuristics(3600).getPermissionHeuristicList();
        assertEquals(PermissionHeuristic.collectionToString(permissionHeuristicsList), 1, permissionHeuristicsList.size());
        assertEquals(PermissionHeuristicType.resource, permissionHeuristicsList.get(0).getPermissionHeuristicType());
        assertEquals(0, permissionHeuristicsList.get(0).getDepth());
      } 
      
      //resource assignment set of 0 deny plus action 29
      {
        PermissionEntry permissionEntry = new PermissionEntry();
        permissionEntry.setPermissionTypeDb(PermissionType.role.name());
        permissionEntry.setRoleSetDepth(30);
        permissionEntry.setAttributeDefNameSetDepth(0);
        permissionEntry.setAttributeAssignActionSetDepth(29);
        permissionEntry.setAllowed(false);
        permissionEntry.setEnabled(true);
        long score = PermissionHeuristic.computePermissionHeuristic(permissionEntry);
        assertEquals(3602, score);

        permissionHeuristicsList = PermissionHeuristic.computeHeuristics(3602).getPermissionHeuristicList();
        assertEquals(PermissionHeuristic.collectionToString(permissionHeuristicsList), 2, permissionHeuristicsList.size());
        assertEquals(PermissionHeuristicType.resource, permissionHeuristicsList.get(0).getPermissionHeuristicType());
        assertEquals(0, permissionHeuristicsList.get(0).getDepth());
        assertEquals(PermissionHeuristicType.action, permissionHeuristicsList.get(1).getPermissionHeuristicType());
        assertEquals(29, permissionHeuristicsList.get(1).getDepth());

      } 
      
      //role assignment set of 29 deny 
      {
        PermissionEntry permissionEntry = new PermissionEntry();
        permissionEntry.setPermissionTypeDb(PermissionType.role.name());
        permissionEntry.setRoleSetDepth(29);
        permissionEntry.setAttributeDefNameSetDepth(30);
        permissionEntry.setAttributeAssignActionSetDepth(30);
        permissionEntry.setAllowed(false);
        permissionEntry.setEnabled(true);
        long score = PermissionHeuristic.computePermissionHeuristic(permissionEntry);
        assertEquals(14400, score);

        permissionHeuristicsList = PermissionHeuristic.computeHeuristics(14400).getPermissionHeuristicList();
        assertEquals(PermissionHeuristic.collectionToString(permissionHeuristicsList), 1, permissionHeuristicsList.size());
        assertEquals(PermissionHeuristicType.role, permissionHeuristicsList.get(0).getPermissionHeuristicType());
        assertEquals(29, permissionHeuristicsList.get(0).getDepth());

      } 
      
      //role assignment set of 29 allow 
      {
        PermissionEntry permissionEntry = new PermissionEntry();
        permissionEntry.setPermissionTypeDb(PermissionType.role.name());
        permissionEntry.setRoleSetDepth(29);
        permissionEntry.setAttributeDefNameSetDepth(30);
        permissionEntry.setAttributeAssignActionSetDepth(30);
        permissionEntry.setAllowed(true);
        permissionEntry.setEnabled(true);
        long score = PermissionHeuristic.computePermissionHeuristic(permissionEntry);
        assertEquals(14401, score);

        permissionHeuristicsList = PermissionHeuristic.computeHeuristics(14401).getPermissionHeuristicList();
        assertEquals(PermissionHeuristic.collectionToString(permissionHeuristicsList), 2, permissionHeuristicsList.size());
        assertEquals(PermissionHeuristicType.role, permissionHeuristicsList.get(0).getPermissionHeuristicType());
        assertEquals(29, permissionHeuristicsList.get(0).getDepth());
        assertEquals(PermissionHeuristicType.allow, permissionHeuristicsList.get(1).getPermissionHeuristicType());

      } 
      
      //role assignment set of 29 allow action 29 resource 29
      {
        PermissionEntry permissionEntry = new PermissionEntry();
        permissionEntry.setPermissionTypeDb(PermissionType.role.name());
        permissionEntry.setRoleSetDepth(29);
        permissionEntry.setAttributeDefNameSetDepth(29);
        permissionEntry.setAttributeAssignActionSetDepth(29);
        permissionEntry.setAllowed(true);
        permissionEntry.setEnabled(true);
        long score = PermissionHeuristic.computePermissionHeuristic(permissionEntry);
        assertEquals(14523, score);

        permissionHeuristicsList = PermissionHeuristic.computeHeuristics(14523).getPermissionHeuristicList();
        assertEquals(PermissionHeuristic.collectionToString(permissionHeuristicsList), 4, permissionHeuristicsList.size());
        assertEquals(PermissionHeuristicType.role, permissionHeuristicsList.get(0).getPermissionHeuristicType());
        assertEquals(29, permissionHeuristicsList.get(0).getDepth());
        assertEquals(PermissionHeuristicType.resource, permissionHeuristicsList.get(1).getPermissionHeuristicType());
        assertEquals(29, permissionHeuristicsList.get(1).getDepth());
        assertEquals(PermissionHeuristicType.action, permissionHeuristicsList.get(2).getPermissionHeuristicType());
        assertEquals(29, permissionHeuristicsList.get(2).getDepth());
        assertEquals(PermissionHeuristicType.allow, permissionHeuristicsList.get(3).getPermissionHeuristicType());
      
      } 
      
      //role assignment set of 0 allow action 30 resource 30
      {
        PermissionEntry permissionEntry = new PermissionEntry();
        permissionEntry.setPermissionTypeDb(PermissionType.role.name());
        permissionEntry.setRoleSetDepth(0);
        permissionEntry.setAttributeDefNameSetDepth(30);
        permissionEntry.setAttributeAssignActionSetDepth(30);
        permissionEntry.setAllowed(true);
        permissionEntry.setEnabled(true);
        long score = PermissionHeuristic.computePermissionHeuristic(permissionEntry);
        assertEquals(432001, score);

        permissionHeuristicsList = PermissionHeuristic.computeHeuristics(432001).getPermissionHeuristicList();
        assertEquals(PermissionHeuristic.collectionToString(permissionHeuristicsList), 2, permissionHeuristicsList.size());
        assertEquals(PermissionHeuristicType.role, permissionHeuristicsList.get(0).getPermissionHeuristicType());
        assertEquals(0, permissionHeuristicsList.get(0).getDepth());
        assertEquals(PermissionHeuristicType.allow, permissionHeuristicsList.get(1).getPermissionHeuristicType());

      } 
      
      //person assignment set of 0 allow action 30 resource 30
      {
        PermissionEntry permissionEntry = new PermissionEntry();
        permissionEntry.setPermissionTypeDb(PermissionType.role_subject.name());
        permissionEntry.setRoleSetDepth(0);
        permissionEntry.setAttributeDefNameSetDepth(0);
        permissionEntry.setAttributeAssignActionSetDepth(0);
        permissionEntry.setAllowed(true);
        permissionEntry.setEnabled(true);
        long score = PermissionHeuristic.computePermissionHeuristic(permissionEntry);
        assertEquals(867661, score);

        permissionHeuristicsList = PermissionHeuristic.computeHeuristics(867661).getPermissionHeuristicList();
        assertEquals(PermissionHeuristic.collectionToString(permissionHeuristicsList), 4, permissionHeuristicsList.size());
        assertEquals(PermissionHeuristicType.personRole, permissionHeuristicsList.get(0).getPermissionHeuristicType());
        assertEquals(PermissionHeuristicType.resource, permissionHeuristicsList.get(1).getPermissionHeuristicType());
        assertEquals(0, permissionHeuristicsList.get(1).getDepth());
        assertEquals(PermissionHeuristicType.action, permissionHeuristicsList.get(2).getPermissionHeuristicType());
        assertEquals(0, permissionHeuristicsList.get(2).getDepth());
        assertEquals(PermissionHeuristicType.allow, permissionHeuristicsList.get(3).getPermissionHeuristicType());
      
      } 
      
      //person assignment allow action 0 resource 0
      {
        PermissionEntry permissionEntry = new PermissionEntry();
        permissionEntry.setPermissionTypeDb(PermissionType.role_subject.name());
        permissionEntry.setRoleSetDepth(0);
        permissionEntry.setAttributeDefNameSetDepth(0);
        permissionEntry.setAttributeAssignActionSetDepth(0);
        permissionEntry.setAllowed(false);
        permissionEntry.setEnabled(true);
        long score = PermissionHeuristic.computePermissionHeuristic(permissionEntry);
        assertEquals(867660, score);

        permissionHeuristicsList = PermissionHeuristic.computeHeuristics(867660).getPermissionHeuristicList();
        assertEquals(PermissionHeuristic.collectionToString(permissionHeuristicsList), 3, permissionHeuristicsList.size());
        assertEquals(PermissionHeuristicType.personRole, permissionHeuristicsList.get(0).getPermissionHeuristicType());
        assertEquals(PermissionHeuristicType.resource, permissionHeuristicsList.get(1).getPermissionHeuristicType());
        assertEquals(0, permissionHeuristicsList.get(1).getDepth());
        assertEquals(PermissionHeuristicType.action, permissionHeuristicsList.get(2).getPermissionHeuristicType());
        assertEquals(0, permissionHeuristicsList.get(2).getDepth());

      } 

      {

        PermissionHeuristics permissionHeuristics = PermissionHeuristic.computeHeuristics(867661);
        PermissionHeuristics permissionHeuristics2 = PermissionHeuristic.computeHeuristics(867660);
        
        PermissionHeuristicBetter permissionHeuristicBetter = permissionHeuristics2.whyBetterThanArg(permissionHeuristics);
        assertNull(permissionHeuristicBetter);
        
        permissionHeuristicBetter = permissionHeuristics.whyBetterThanArg(permissionHeuristics);
        assertNull(permissionHeuristicBetter);
        
        permissionHeuristicBetter = permissionHeuristics.whyBetterThanArg(permissionHeuristics2);
        assertEquals(PermissionHeuristicType.allow, permissionHeuristicBetter.getThisPermissionHeuristic().getPermissionHeuristicType());
        assertNull(permissionHeuristicBetter.getOtherPermissionHeuristic());
        

        permissionHeuristics = PermissionHeuristic.computeHeuristics(432001);
        permissionHeuristics2 = PermissionHeuristic.computeHeuristics(14523);
        
        permissionHeuristicBetter = permissionHeuristics.whyBetterThanArg(permissionHeuristics2);
        assertEquals(PermissionHeuristicType.role, permissionHeuristicBetter.getThisPermissionHeuristic().getPermissionHeuristicType());
        assertEquals(PermissionHeuristicType.role, permissionHeuristicBetter.getOtherPermissionHeuristic().getPermissionHeuristicType());
        
      
      }
      
    } finally {
      PermissionHeuristic.maxDepth = oldDepth;
    }
    
  }
  
}
