/**
 * Copyright 2014 Internet2
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

package edu.internet2.middleware.grouper.misc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/** 
 * Find bad memberships in the Grouper memberships table.
 *
 * This script is used to find bad memberships in Grouper.  If a bad membership is found, a GSH script will be created to 
 * resolve the issue.
 *
 * @since   1.3.1
 */
public class FindBadMemberships {

  /**
   * 
   */
  public static PrintStream out = System.out;
  
  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(FindBadMemberships.class);

  /** GSH script to fix membership data */
  public static StringWriter gshScript = null;

  /** File name for GSH script */
  private static final String gshScriptFilename = "findbadmemberships.gsh";

  /** Whether to print memberships errors to standard out. */
  private static boolean printErrorsToSTOUT = false;
  
  /**
   * call this before finding bad memberships
   */
  public static void clearResults() {
    gshScript = null;
  }
  
  /**
   * @param args 
   * @since   1.3.1
   */
  public static void main(String[] args) {
    Options options = new Options();
    OptionGroup optionGroup = new OptionGroup();
    optionGroup.addOption(new Option("all", false, 
      "Find bad memberships."));
    optionGroup.setRequired(true);
    options.addOptionGroup(optionGroup);

    if (args.length == 0) {
      printUsage(options);
      System.exit(0);
    }

    CommandLineParser parser = new GnuParser();
    CommandLine line = null;
    try {
      line = parser.parse(options, args);
    } catch (ParseException e) {
      System.err.println(e.getMessage());
      printUsage(options);
      System.exit(1);
    }
    clearResults();
    // maybe this should go to a log file instead?
    printErrorsToSTOUT = true;
    
    final CommandLine LINE = line;
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        try {

          if (LINE.hasOption("all")) {
            checkAll(out);
          } else {
            printUsage(options);
            System.exit(0);
          }

        } catch (Exception e) {
          System.err.println(e.getMessage());
          System.exit(1);
        }
        return null;
      }
    });

    

    out.println();
    out.println();
    if (gshScript == null) {
      out.println("No membership errors found.");
    } else {
      out.println("Membership errors have been found.  Do the following to resolve the errors:");
      writeFile(gshScript, gshScriptFilename);
      out.println(" - Review the GSH script before applying any changes to your database.");
      out.println(" - Execute the GSH Script " + gshScriptFilename);
      out.println(" - Re-run the bad membership finder utility to verify that bad memberships have been fixed.");
    }
    System.exit(0);
  }

  /**
   * @param printStream 
   * @throws SessionException
   */
  public static void checkAll(PrintStream printStream) throws SessionException {
    PrintStream oldPrintStream = out;
    out = printStream;
    try {  
      checkAll();
    } finally {
      out = oldPrintStream;
    }
  }
  
  /**
   * @return count of bad and missing memberships
   */
  public static long checkAll() {
    long errors = 0;
    
    if (printErrorsToSTOUT) {
      out.println();
      out.println("Checking Composite Memberships");
    }
    
    errors += checkComposites();
    
    if (errors == 0) {
      // only check group sets if composites are okay
      if (printErrorsToSTOUT) {
        out.println("Checking Group Sets");
      }
      errors += checkGroupSets();
    } else {
      if (printErrorsToSTOUT) {
        out.println("*** Skipping group set check since there were composite issues. Re-run after fixing composites. ***\n");
      }
    }
    
    if (printErrorsToSTOUT) {
      out.println("Checking memberships where member is a deleted group");
    }
    errors += checkDeletedGroupAsMember();
    
    
    if (printErrorsToSTOUT) {
      out.println("Checking GrouperAll");
    }
    errors += checkGrouperAll();
    
    return errors;
  }
  
  /**
   * Set whether to print errors to STDOUT.
   * @param v 
   */
  public static void printErrorsToSTOUT(boolean v) {
    printErrorsToSTOUT = v;
  }

  /**
   * Print usage.
   * @param options 
   */
  private static void printUsage(Options options) {

    out.println();
 
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(FindBadMemberships.class.getSimpleName(), options, true);
 
    out.println();
    out.print("This script will find bad memberships in your Grouper database.  ");
    out.print("It will not make any modifications to the Grouper database.  ");
    out.println("If bad memberships are found, this script will create a GSH script that will correct memberships.");
    out.println();
    out.println("To fix your memberships, complete these steps in the order listed:");
    out.println();
    out.println("1.  Review the GSH script before applying any changes to your database.");
    out.println("2.  Run the GSH script.");
    out.println("3.  Re-run the bad membership finder utility to verify that bad memberships have been fixed.");
    out.println();
  }
  
  /**
   * Note that this isn't a complete check of the group set table..
   * @return count of bad and missing group sets
   */
  public static long checkGroupSets() {
    Set<GroupSet> badType = GrouperDAOFactory.getFactory().getGroupSet().findTypeMismatch();

    for (GroupSet gs : badType) {
      if (gs.getDepth() != 0) {
        throw new RuntimeException("Unexpected depth of " + gs.getDepth());
      }
      
      Group g = GrouperDAOFactory.getFactory().getGroup().findByUuid(gs.getOwnerGroupId(), true);
      Composite c = GrouperDAOFactory.getFactory().getComposite().findAsOwner(g, false);
      String currentType = gs.getType();
      String newType = c == null ? "immediate" : "composite";
      if (printErrorsToSTOUT) {
        out.println("Bad group set type: owner groupId=" + g.getUuid() + ", owner group name=" + g.getName() + ", currentType=" + currentType + ", newType=" + newType + ".");
      }
      
      logGshScript("sqlRun(\"update grouper_group_set set mship_type='" + newType + "' where id='" + gs.getId() + "'\");\n");
    }
    
    Set<GroupSet> badCompositeGroupSets = GrouperDAOFactory.getFactory().getGroupSet().findBadGroupSetsForCompositeGroups();
    for (GroupSet gs : badCompositeGroupSets) {
      Group ownerGroup = GrouperDAOFactory.getFactory().getGroup().findByUuid(gs.getOwnerGroupId(), true);
      Group memberGroup = GrouperDAOFactory.getFactory().getGroup().findByUuid(gs.getMemberGroupId(), true);

      if (printErrorsToSTOUT) {
        out.println("Bad group set for composite: owner groupId=" + ownerGroup.getUuid() + ", owner group name=" + ownerGroup.getName() + ", member groupId=" + memberGroup.getUuid() + ", member group name=" + memberGroup.getName() + ".");
      }
      
      logGshScript("GrouperDAOFactory.getFactory().getGroupSet().findById(\"" + gs.getId() + "\").delete(true);\n");
    }
    
    Set<GroupSet> immediateGroupSetsWithMissingEffective = getImmediateGroupSetsWithMissingEffective(GrouperDAOFactory.getFactory().getGroupSet().findMissingEffectiveGroupSets());
    for (GroupSet gs : immediateGroupSetsWithMissingEffective) {
      if (gs.getOwnerGroupId() == null || gs.getMemberGroupId() == null || gs.getDepth() != 1 || !gs.getFieldId().equals(Group.getDefaultList().getUuid())) {
        throw new RuntimeException("Excepted an immediate group set with an ownerGroup, memberGroup, and member field.  id=" + gs.getId());
      }
      
      Group ownerGroup = GrouperDAOFactory.getFactory().getGroup().findByUuid(gs.getOwnerGroupId(), true);
      Group memberGroup = GrouperDAOFactory.getFactory().getGroup().findByUuid(gs.getMemberGroupId(), true);
      
      if (printErrorsToSTOUT) {
        out.println("Incomplete group set hierarchy (GSH script will attempt to delete and recreate it): owner groupId=" + ownerGroup.getUuid() + ", owner group name=" + ownerGroup.getName() + ", member groupId=" + memberGroup.getUuid() + ", member group name=" + memberGroup.getName() + ".");
      }
      
      // TODO: this should be improved to fix group sets directly without having to delete and recreate the membership
      // simplifying this right now to avoid timing issues with point in time.
      logGshScript("delMember(\"" + ownerGroup.getName() + "\", \"" + memberGroup.getId() + "\");\n");      
      logGshScript("addMember(\"" + ownerGroup.getName() + "\", \"" + memberGroup.getId() + "\");\n");      
    }
    
    List<GroupSet> badEffectiveGroupSets = getBadEffectiveGroupSets(new ArrayList<GroupSet>(GrouperDAOFactory.getFactory().getGroupSet().findBadEffectiveGroupSets()));
    for (GroupSet gs : badEffectiveGroupSets) {

      if (printErrorsToSTOUT) {
        out.println("Bad effective group set: group set id=" + gs.getId() + ", depth=" + gs.getDepth() + ", owner id=" + gs.getOwnerId() + ", member id = " + gs.getMemberId() + ".");
      }

      logGshScript("sqlRun(\"delete from grouper_group_set where id='" + gs.getId() + "'\");\n");
    }
    
    // note this doesn't handle if in a set of duplicates, multiple group sets have foreign keys - that seems unlikely.
    // this also doesn't fix PIT if needed.  should run pit sync after this.
    Set<GroupSet> duplicates = GrouperDAOFactory.getFactory().getGroupSet().findDuplicateSelfGroupSets();

    for (GroupSet gs : duplicates) {
      if (gs.getDepth() != 0) {
        throw new RuntimeException("Unexpected depth of " + gs.getDepth());
      }

      if (printErrorsToSTOUT) {
        out.println("Duplicate group set: owner=" + gs.getOwnerId() + ", member=" + gs.getMemberId() + ", field=" + gs.getFieldId() + ".");
      }
      
      logGshScript("sqlRun(\"update grouper_group_set set parent_id=null where id='" + gs.getId() + "'\");\n");
      logGshScript("sqlRun(\"delete from grouper_group_set where id='" + gs.getId() + "'\");\n");
    }
    
    return duplicates.size() + badType.size() + badCompositeGroupSets.size() + immediateGroupSetsWithMissingEffective.size() + badEffectiveGroupSets.size();
  }
  
  /**
   * GrouperAll shouldn't have manage privileges or be a member of a group
   * @return count of bad memberships
   */
  public static long checkGrouperAll() {
    
    Member grouperAll = GrouperDAOFactory.getFactory().getMember().findBySubject(SubjectFinder.findAllSubject().getId(), SubjectFinder.findAllSubject().getSourceId(), SubjectFinder.findAllSubject().getTypeName(), false);
    if (grouperAll == null) {
      return 0;
    }
    
    Set<Membership> badMemberships = new LinkedHashSet<Membership>();
    
    for (Privilege privilege : AccessPrivilege.MANAGE_PRIVILEGES) {
      Field field = privilege.getField();
      badMemberships.addAll(GrouperDAOFactory.getFactory().getMembership().findAllImmediateByMemberAndField(grouperAll.getId(), field, false));
    }
    
    badMemberships.addAll(GrouperDAOFactory.getFactory().getMembership().findAllImmediateByMemberAndField(grouperAll.getId(), Group.getDefaultList(), false));
    
    for (Membership ms : badMemberships) {
      if (printErrorsToSTOUT) {
        out.println("Bad GrouperAll membership: groupId=" + ms.getOwnerGroupId() + ", group name=" + ms.getOwnerGroup().getName() + ", field=" + ms.getField().getName() + ".");
      }
      
      logGshScript("GrouperDAOFactory.getFactory().getMembership().findByImmediateUuid(\"" + ms.getImmediateMembershipId() + "\", true).delete();\n");
    }
    
    return badMemberships.size();
  }
  
  /**
   * GrouperAll shouldn't have manage privileges or be a member of a group.  Use this to force a cleanup without outputting a GSH script.
   */
  public static void checkAndFixGrouperAll() {
    
    Member grouperAll = GrouperDAOFactory.getFactory().getMember().findBySubject(SubjectFinder.findAllSubject().getId(), SubjectFinder.findAllSubject().getSourceId(), SubjectFinder.findAllSubject().getTypeName(), false);
    if (grouperAll == null) {
      System.out.println("Finished running successfully.  0 changes made.");
      return;
    }
    
    Set<Membership> badMemberships = new LinkedHashSet<Membership>();
    
    for (Privilege privilege : AccessPrivilege.MANAGE_PRIVILEGES) {
      Field field = privilege.getField();
      badMemberships.addAll(GrouperDAOFactory.getFactory().getMembership().findAllImmediateByMemberAndField(grouperAll.getId(), field, false));
    }
    
    badMemberships.addAll(GrouperDAOFactory.getFactory().getMembership().findAllImmediateByMemberAndField(grouperAll.getId(), Group.getDefaultList(), false));
    
    for (Membership ms : badMemberships) {
      System.out.println("Removing GrouperAll membership: groupId=" + ms.getOwnerGroupId() + ", group name=" + ms.getOwnerGroup().getName() + ", field=" + ms.getField().getName() + ".");
      
      ms.delete();
    }
    
    System.out.println("Finished running successfully.  " + badMemberships.size() + " changes made.");
  }

  /**
   * @return count of bad and missing memberships
   */
  public static long checkComposites() {

    // batch these up
    int batchSize = GrouperConfig.retrieveConfig().propertyValueInt("findBadMemberships.batchSize", 1);
    // get list of composites
    List<Object[]> compositeIdAndTypes = new GcDbAccess().sql("select id, type from grouper_composites").selectList(Object[].class);
    
    List<String> compositeIds = new ArrayList<String>();
    List<String> unionCompositeIds = new ArrayList<String>();
    List<String> complementCompositeIds = new ArrayList<String>();
    List<String> intersectionCompositeIds = new ArrayList<String>();
    
    for (Object[] compositeIdAndType : GrouperUtil.nonNull(compositeIdAndTypes)) {
      String compositeId = (String) compositeIdAndType[0];
      String type = (String) compositeIdAndType[1];
      
      compositeIds.add(compositeId);
      if (StringUtils.equals("union", type)) {
        
        unionCompositeIds.add(compositeId);

      } else if (StringUtils.equals("intersection", type)) {

        intersectionCompositeIds.add(compositeId);

      } else if (StringUtils.equals("complement", type)) {

        complementCompositeIds.add(compositeId);

      } else {
        throw new RuntimeException("Invalid type: '" + type + "', expecting: union, intersection, or complement");
      }
    }
    

    Set<Membership> badMemberships = new LinkedHashSet<Membership>();
    badMemberships.addAll(GrouperDAOFactory.getFactory().getMembership().findBadMembershipsOnCompositeGroup());
    badMemberships.addAll(GrouperDAOFactory.getFactory().getMembership().findBadCompositeMembershipsOnNonCompositeGroup());
    
    Set<Object[]> potentialMissingMemberships = new LinkedHashSet<Object[]>();
    
    {
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(unionCompositeIds, batchSize, false);
      for (int i=0;i<numberOfBatches;i++) {
        List<String> currentBatchOfCompositeIds = GrouperUtil.batchList(unionCompositeIds, batchSize, i);
        badMemberships.addAll(GrouperDAOFactory.getFactory().getMembership().findBadUnionMemberships(currentBatchOfCompositeIds));
        potentialMissingMemberships.addAll(GrouperDAOFactory.getFactory().getMembership().findMissingUnionMemberships(currentBatchOfCompositeIds));
      }
    }
    
    {
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(intersectionCompositeIds, batchSize, false);
      for (int i=0;i<numberOfBatches;i++) {
        List<String> currentBatchOfCompositeIds = GrouperUtil.batchList(intersectionCompositeIds, batchSize, i);
        badMemberships.addAll(GrouperDAOFactory.getFactory().getMembership().findBadIntersectionMemberships(currentBatchOfCompositeIds));
        potentialMissingMemberships.addAll(GrouperDAOFactory.getFactory().getMembership().findMissingIntersectionMemberships(currentBatchOfCompositeIds));
      }
    }
    
    {
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(complementCompositeIds, batchSize, false);
      for (int i=0;i<numberOfBatches;i++) {
        List<String> currentBatchOfCompositeIds = GrouperUtil.batchList(complementCompositeIds, batchSize, i);
        badMemberships.addAll(GrouperDAOFactory.getFactory().getMembership().findBadComplementMemberships(currentBatchOfCompositeIds));
        potentialMissingMemberships.addAll(GrouperDAOFactory.getFactory().getMembership().findMissingComplementMemberships(currentBatchOfCompositeIds));
      }
    }
    


    for (Membership ms : badMemberships) {
      if (printErrorsToSTOUT) {
        out.println("Bad composite membership: groupId=" + ms.getOwnerGroupId() + ", group name=" + ms.getOwnerGroup().getName() + ", subjectId=" + ms.getMember().getSubjectId() + ".");
      }
      
      logGshScript("GrouperDAOFactory.getFactory().getMembership().findByImmediateUuid(\"" + ms.getImmediateMembershipId() + "\", true).delete();\n");
    }
    
    int missingMembershipsCount = 0;
    
    for (Object[] ownerAndCompositeAndMember : potentialMissingMemberships) {
      String ownerGroupId = (String)ownerAndCompositeAndMember[0];
      String compositeId = (String)ownerAndCompositeAndMember[1];
      String memberId = (String)ownerAndCompositeAndMember[2];
      Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(ownerGroupId, true);

      if (group.isEnabled()) {
        missingMembershipsCount++;
        
        if (printErrorsToSTOUT) {
          Member member = GrouperDAOFactory.getFactory().getMember().findByUuid(memberId, true);
          out.println("Missing composite membership: groupId=" + group.getId() + ", group name=" + group.getName() + ", subjectId=" + member.getSubjectId() + ".");
        }
        
        logGshScript("GrouperDAOFactory.getFactory().getMembership().save(Composite.internal_createNewCompositeMembershipObject(\"" + ownerGroupId + "\", \"" + memberId + "\", \"" + compositeId + "\"));\n");
      }
    }
    
    return badMemberships.size() + missingMembershipsCount;
  }
  
  /**
   * @return count of bad memberships
   */
  public static long checkDeletedGroupAsMember() {
    
    Set<Membership> badMemberships = GrouperDAOFactory.getFactory().getMembership().findBadMembershipsDeletedGroupAsMember();

    for (Membership ms : badMemberships) {
      if (printErrorsToSTOUT) {
        out.println("Bad membership where member is a deleted group: groupId=" + ms.getOwnerGroupId() + ", group name=" + ms.getOwnerGroup().getName() + ", subjectId=" + ms.getMember().getSubjectId() + ".");
      }
      
      logGshScript("sqlRun(\"delete from grouper_memberships where id='" + ms.getImmediateMembershipId() + "'\");\n");
    }
    
    return badMemberships.size();
  }
  
  /**
   * @param parentAndImmediateSet
   * @return immediates that need to be recreated
   */
  private static Set<GroupSet> getImmediateGroupSetsWithMissingEffective(Set<Object[]> parentAndImmediateSet) {
    Set<GroupSet> missing = new LinkedHashSet<GroupSet>();
    
    for (Object[] parentAndImmediate : parentAndImmediateSet) {
      GroupSet parent = (GroupSet)parentAndImmediate[0];
      GroupSet immediate = (GroupSet)parentAndImmediate[1];
      
      // note that these objects aren't actually saved, we're just creating them for convenience..
      GroupSet groupSet = new GroupSet();
      groupSet.setId(GrouperUuid.getUuid());
      groupSet.setDepth(parent.getDepth() + 1);
      groupSet.setParentId(parent.getId());
      groupSet.setFieldId(parent.getFieldId());
      groupSet.setMemberGroupId(immediate.getMemberId());
      groupSet.setOwnerGroupId(parent.getOwnerGroupId());
      groupSet.setOwnerAttrDefId(parent.getOwnerAttrDefId());
      groupSet.setOwnerStemId(parent.getOwnerStemId());
      groupSet.setType(MembershipType.EFFECTIVE.getTypeString());
      
      if (!immediate.internal_isCircular(groupSet, parent)) {
        missing.add(immediate);
      }
    }
    
    return missing;
  }
  
  /**
   * Keep the GSH script in memory until we're done with this utility.
   *
   * @param script to log
   */
  private static void logGshScript(String script) {
    if (gshScript == null) {
      gshScript = new StringWriter();
      gshScript.write("GrouperSession.startRootSession();\n");
    }

    gshScript.write(script);
  }

  /**
   * Write GSH script to file.
   */
  public static void writeGshScriptToFile() {
    if (gshScript != null) {
      writeFile(gshScript, gshScriptFilename);
    }
  }

  /**
   * Write data to a file.
   *
   * @param data to write
   * @param filename to write data to.
   */
  private static void writeFile(StringWriter data, String filename) {
    FileWriter fw = null;
    try {
      out.println("Writing file: " + GrouperUtil.fileCanonicalPath(new File(filename)));
      fw = new FileWriter(filename, false);
      fw.write(data.toString());
    } catch (IOException e) {
      out.println("Exception while writing out to file " + filename + ": " + e.toString());
      out.println("Writing data out here instead: ");
      out.println(data.toString());
    } finally {
      try {
        if (fw != null) {
          fw.close();
        }
      } catch (IOException e) {
        // do nothing
      }
    }
  }
  
  private static List<GroupSet> getBadEffectiveGroupSets(List<GroupSet> groupSets) {
    List<GroupSet> groupSetsToReturn = new ArrayList<GroupSet>();
    for (GroupSet groupSet : groupSets) {
      // get children
      List<GroupSet> childGroupSets = new ArrayList<GroupSet>(GrouperDAOFactory.getFactory().getGroupSet().findAllByParentId(groupSet.getId()));
      
      groupSetsToReturn.addAll(getBadEffectiveGroupSets(childGroupSets));
      groupSetsToReturn.add(groupSet);
    }
    
    return groupSetsToReturn;
  }
}

