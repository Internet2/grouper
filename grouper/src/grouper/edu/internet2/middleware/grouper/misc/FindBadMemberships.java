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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.logging.Log;

import org.hibernate.HibernateException;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.CompositeNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperRuntimeException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/** 
 * Find bad memberships in the Grouper memberships table.
 *
 * This script is used to find bad memberships in Grouper.  It will go through all types of 
 * memberships including composites, access privileges and naming privileges.  If a bad
 * membership is found for a group or stem, a GSH script will be created to delete the immediate 
 * membership and recreate the membership.
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

  // GSH script to fix membership data
  public static StringWriter gshScript = null;

  // File name for GSH script
  private static final String gshScriptFilename = "findbadmemberships.gsh";

  // Whether to print memberships errors to standard out.
  private static boolean printErrorsToSTOUT = false;

  // map list names to corresponding privileges, a better way probably exists
  private static Map<String, String> list2priv = new HashMap<String, String>();
  static {
    list2priv.put("admins", "AccessPrivilege.ADMIN");
    list2priv.put("optins", "AccessPrivilege.OPTIN");
    list2priv.put("optouts", "AccessPrivilege.OPTOUT");
    list2priv.put("readers", "AccessPrivilege.READ");
    list2priv.put("updaters", "AccessPrivilege.UPDATE");
    list2priv.put("viewers", "AccessPrivilege.VIEW");
    list2priv.put("creators", "NamingPrivilege.CREATE");
    list2priv.put("stemmers", "NamingPrivilege.STEM");
  }

  /**
   * call this before finding bad memberships
   */
  public static void clearResults() {
    gshScript = null;
  }
  
  /**
   * @since   1.3.1
   */
  public static void main(String[] args) {
    Options options = new Options();
    OptionGroup optionGroup = new OptionGroup();
    optionGroup.addOption(new Option("all", false, 
      "Find bad list memberships, access privileges, and naming privileges owned by all groups and stems."));
    optionGroup.addOption(new Option("group", true, 
      "Find bad list memberships and access privileges for a specific group."));
    optionGroup.addOption(new Option("stem", true, "Find bad naming privileges for a specific stem."));
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
    
    try {
      if (line.hasOption("all")) {
        checkAll(out);
      } else if (line.hasOption("group")) {
        checkGroup(line.getOptionValue("group"));
      } else if (line.hasOption("stem")) {
        checkStem(line.getOptionValue("stem"));
      } else {
        printUsage(options);
        System.exit(0);
      }

    } catch (Exception e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }

    out.println();
    out.println();
    if (gshScript == null) {
      out.println("No membership errors found.");
    } else {
      out.println("Membership errors have been found.  Do the following to resolve the errors:");
      writeFile(gshScript, gshScriptFilename);
      out.println(" - Review the GSH script before applying any changes to your database.");
      out.println(" - Execute the GSH Script " + gshScriptFilename);
      out.println(" - Re-run the bad membership finder utility for all groups and stems since additional membership errors for other groups and stems may be revealed after the current groups and stems are fixed.");
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
      out.println();
      out.println("PHASE 1: Find memberships with invalid owners.");
      checkMembershipsWithInvalidOwners();
  
      out.println();
      out.println("PHASE 2: Check list and access memberships for groups.");
      checkGroups();
  
      out.println();
      out.println("PHASE 3: Check naming memberships for stems.");
      checkStems();
    } finally {
      out = oldPrintStream;
    }
  }

  /**
   * Set whether to print errors to STDOUT.
   */
  public static void printErrorsToSTOUT(boolean v) {
    printErrorsToSTOUT = v;
  }

  /**
   * Find all membership entries that have invalid owner uuids.
   * 
   * @return number of errors found
   */
  public static int checkMembershipsWithInvalidOwners() {
    //out.println("Querying all memberships with invalid owners");
    List<Membership> invalid = GrouperDAOFactory.getFactory().getMembership().findAllMembershipsWithInvalidOwners();
    Iterator<Membership> invalidIterator = invalid.iterator();
    while (invalidIterator.hasNext()) {
      Membership ms = invalidIterator.next();
      foundError(ms);
    }

    return invalid.size();
  } 

  /**
   * Find bad memberships for all stems.  This will check all naming privileges.
   * 
   * @return number of errors found.
   * @throws SessionException
   */
  private static int checkStems() throws SessionException {
    out.println("Querying all stems");
    GrouperSession s = GrouperSession.start(SubjectFinder.findRootSubject());
    Set<Stem> allStems = GrouperDAOFactory.getFactory().getStem().getAllStems();
    out.println("Found " + allStems.size() + " stems.  Verifying stems now....");
    Iterator<Stem> stemIterator = allStems.iterator();

    int badCount = 0;
    while (stemIterator.hasNext()) {
      Stem stem = stemIterator.next();

      try {
        boolean result = checkStem(stem);
        if (result == false) {
          badCount++;
        }
      } catch (MemberNotFoundException e) {
        out.println("Error checking stem " + stem.getName() + ": " + e.getMessage());
      } catch (SchemaException e) {
        out.println("Error checking stem " + stem.getName() + ": " + e.getMessage());
      } catch (GrouperRuntimeException e) {
        out.println("Error checking stem " + stem.getName() + ": " + e.getMessage());
      } catch (IllegalStateException e) {
        out.println("Error checking stem " + stem.getName() + ": " + e.getMessage());
      }
    }

    return badCount;
  }

  /**
   * Check for bad naming privileges for the given stem.
   *
   * @param stemName to check
   * @return true if there are no errors with this stem.
   * @throws SessionException
   * @throws StemNotFoundException
   * @throws MemberNotFoundException
   * @throws SchemaException
   */
  protected static boolean checkStem(String stemName) throws SessionException, StemNotFoundException, 
    MemberNotFoundException, SchemaException {
    GrouperSession s = GrouperSession.start(SubjectFinder.findRootSubject());
    Stem stem = GrouperDAOFactory.getFactory().getStem().findByName(stemName);

    out.println("Checking stem: " + stem.getName());
    return checkStem(stem);
  } 

  /**
   * Check for bad naming privileges for the given stem.
   *
   * @param stem to check
   * @return true if there are no errors with this stem.
   * @throws MemberNotFoundException
   * @throws SchemaException
   */
  public static boolean checkStem(Stem stem) throws MemberNotFoundException, SchemaException {
    //out.println("Checking stem: " + stem.getName() + " - " + stem.getUuid());
    String ownerUUID = stem.getUuid();

    // get all memberships for this stem.
    List<Membership> current = GrouperDAOFactory.getFactory().getMembership().findAllByOwnerAsList(ownerUUID);

    // we'll store all the effective memberships for the stem here.
    Set<Membership> currentEffective = new LinkedHashSet<Membership>();

    // we need to map current membership uuid values with the membership uuid values that we'll be creating.
    Map<String,String> uuidMap = new HashMap<String,String>();
  
    // these are the memberships that *should* exist.
    Set<Membership> should = new LinkedHashSet<Membership>();

    Iterator<Membership> currentIterator = current.iterator();
    while (currentIterator.hasNext()) {
      Membership currentMembership = currentIterator.next();
      if (!currentMembership.getType().equals(Membership.IMMEDIATE)) {
        if (currentMembership.getType().equals(Membership.EFFECTIVE)) {
          boolean result = currentEffective.add(currentMembership);

          // if this is false, we have a duplicate membership.
          if (!result) {
            foundError(stem, current);
            return false;
          }
        }
        continue;
      }
      Member currentMember = GrouperDAOFactory.getFactory().getMember().findByUuid(currentMembership.getMemberUuid());

      // add the immediate membership and get back the membership objects that are created.
      DefaultMemberOf mof = new DefaultMemberOf();

      try {
        // if this throws a HibernateException, we might have bad data in the database.
        mof.addImmediateWithoutValidation(GrouperSession.staticGrouperSession(), stem, FieldFinder.find(currentMembership.getListName()), currentMember);
      } catch (HibernateException e) {
        return false;
      }

      Set<GrouperAPI> shouldBeforeFilter = mof.getSaves();
 
      Iterator<GrouperAPI> shouldBeforeFilterIterator = shouldBeforeFilter.iterator();
      while (shouldBeforeFilterIterator.hasNext()) {
        Membership shouldMembership = (Membership)shouldBeforeFilterIterator.next();
        if (shouldMembership.getOwnerUuid().equals(ownerUUID) && shouldMembership.getType().equals(Membership.EFFECTIVE)) {
          should.add(shouldMembership);
        }
        if (shouldMembership.getOwnerUuid().equals(ownerUUID) && shouldMembership.getType().equals(Membership.IMMEDIATE)) {
          uuidMap.put(shouldMembership.getUuid(), currentMembership.getUuid());
        }
      }
    }

    boolean result = checkEquality(should, currentEffective, uuidMap);
    if (!result) {
      foundError(stem, current);
      return false;
    }

    return true;
  }

  /**
   * Retrieves a composite.
   *
   * @param group owner of composite
   * @return the composite or null if the group is not the owner of a composite.
   */
  private static Composite getComposite(Group group) {
    try {
      Composite c = group.getComposite();
      return c;
    } catch (CompositeNotFoundException e) {
      return null;
    }
  }

  /**
   * Find bad memberships for all groups.  This will check all list memberships and access privileges.
   * 
   * @return number of errors found.
   * @throws SessionException
   */
  private static int checkGroups() throws SessionException {
    out.println("Querying all groups");
    GrouperSession s = GrouperSession.start(SubjectFinder.findRootSubject());
    Set<Group> allGroups = GrouperDAOFactory.getFactory().getGroup().getAllGroups();
    out.println("Found " + allGroups.size() + " groups.  Verifying groups now....");
    Iterator<Group> groupIterator = allGroups.iterator();

    int badCount = 0;
    while (groupIterator.hasNext()) {
      Group group = groupIterator.next();
 
      try {
        boolean result = checkGroup(group);
        if (result == false) {
          badCount++;
        }
      } catch (MemberNotFoundException e) {
        out.println("Error checking group " + group.getName() + ": " + e.getMessage());
      } catch (GroupNotFoundException e) {
        out.println("Error checking group " + group.getName() + ": " + e.getMessage());
      } catch (SchemaException e) {
        out.println("Error checking group " + group.getName() + ": " + e.getMessage());
      } catch (GrouperRuntimeException e) {
        out.println("Error checking group " + group.getName() + ": " + e.getMessage());
      } catch (IllegalStateException e) {
        out.println("Error checking group " + group.getName() + ": " + e.getMessage());
      }
    }

    return badCount;
  }

  /**
   * Check the composite memberships of a group.
   *
   * @param group to check
   * @param composite to check
   * @param current composite memberships that exist for this group.
   * @return true if the memberships are good.
   */
  private static boolean checkCompositeMemberships(Group group, Composite c, Set<Membership> current) {
    String ownerUUID = group.getUuid();

    Set<Membership> should = new LinkedHashSet<Membership>();

    DefaultMemberOf mof = new DefaultMemberOf();

    try {
      // if this throws a HibernateException, we might have bad data in the database.
      mof.addComposite(GrouperSession.staticGrouperSession(), group, c);
    } catch (HibernateException e) {
      return false;
    }

    Set<GrouperAPI> shouldBeforeFilter = mof.getEffectiveSaves();
    Iterator<GrouperAPI> shouldBeforeFilterIterator = shouldBeforeFilter.iterator();
    while (shouldBeforeFilterIterator.hasNext()) {
      Membership shouldMembership = (Membership)shouldBeforeFilterIterator.next();
      if (shouldMembership.getOwnerUuid().equals(ownerUUID) && 
        shouldMembership.getType().equals(Membership.COMPOSITE)) {
        should.add(shouldMembership);
      }
    }

    if (should.size() != current.size()) {
      return false;
    }

    should.removeAll(current);
    if (should.size() != 0) {
      return false;
    }

    return true;
  }

  /**
   * Check for bad list memberships and access privileges for the given group.
   *
   * @param groupName to check
   * @return true if there are no errors with this group.
   * @throws SessionException
   * @throws GroupNotFoundException
   * @throws MemberNotFoundException
   * @throws SchemaException
   */
  protected static boolean checkGroup(String groupName) throws SessionException, GroupNotFoundException,
    MemberNotFoundException, SchemaException {
    GrouperSession s = GrouperSession.start(SubjectFinder.findRootSubject());
    Group group = GrouperDAOFactory.getFactory().getGroup().findByName(groupName);

    out.println("Checking group: " + group.getName());
    return checkGroup(group);
  }

  /**
   * Check for bad list memberships and access privileges for the given group.
   *
   * @param group to check
   * @return true if there are no errors with this group.
   * @throws MemberNotFoundException
   * @throws GroupNotFoundException
   * @throws SchemaException
   */
  public static boolean checkGroup(Group group) throws MemberNotFoundException, GroupNotFoundException, SchemaException {
    //out.println("Checking group: " + group.getName() + " - " + group.getUuid());
    String ownerUUID = group.getUuid();

    // get all memberships for this group.
    List<Membership> current = GrouperDAOFactory.getFactory().getMembership().findAllByOwnerAsList(ownerUUID);

    // we'll store all the effective memberships for the group here.
    Set<Membership> currentEffective = new LinkedHashSet<Membership>();

    // we'll store all the composite memberships for the group here.
    Set<Membership> currentComposites = new LinkedHashSet<Membership>();

    // we need to map current membership uuid values with the membership uuid values that we'll be creating.
    Map<String,String> uuidMap = new HashMap<String,String>();

    // is this a composite group?
    Composite c = getComposite(group);
  
    // these are the memberships that *should* exist.
    Set<Membership> should = new LinkedHashSet<Membership>();

    Iterator<Membership> currentIterator = current.iterator();
    while (currentIterator.hasNext()) {
      Membership currentMembership = currentIterator.next();
      if (currentMembership.getType().equals(Membership.EFFECTIVE)) {
        boolean result = currentEffective.add(currentMembership);
        if (!result) {
          // we have a duplicate effective membership.
          foundError(group, c, current);
          return false;
        }
        continue;
      } else if (currentMembership.getType().equals(Membership.COMPOSITE)) {
        if (c == null) {
          // we have a composite membership but this isn't a composite group!
          foundError(group, c, current);
          return false;
        }

        boolean result = currentComposites.add(currentMembership);
        if (!result) {
          // we have a duplicate composite membership.
          foundError(group, c, current);
          return false;
        }
        continue;
      }
      Member currentMember = GrouperDAOFactory.getFactory().getMember().findByUuid(currentMembership.getMemberUuid());

      // add the immediate membership and lets see what effective memberships are created.
      DefaultMemberOf mof = new DefaultMemberOf();

      try {
        // if this throws a HibernateException, we might have bad data in the database.
        mof.addImmediateWithoutValidation(GrouperSession.staticGrouperSession(), group, FieldFinder.find(currentMembership.getListName()), currentMember);
      } catch (HibernateException e) {
        return false;
      }

      Set<GrouperAPI> shouldBeforeFilter = mof.getSaves();

      Iterator<GrouperAPI> shouldBeforeFilterIterator = shouldBeforeFilter.iterator();
      while (shouldBeforeFilterIterator.hasNext()) {
        Membership shouldMembership = (Membership)shouldBeforeFilterIterator.next();
        if (shouldMembership.getOwnerUuid().equals(ownerUUID) && shouldMembership.getType().equals(Membership.EFFECTIVE) &&
          shouldMembership.getListName().equals(currentMembership.getListName())) {
          should.add(shouldMembership);
        }
        if (shouldMembership.getOwnerUuid().equals(ownerUUID) && shouldMembership.getType().equals(Membership.IMMEDIATE)) {
          uuidMap.put(shouldMembership.getUuid(), currentMembership.getUuid());
        }
      } 
    }

    // checking the effective memberships...
    boolean equalTest = checkEquality(should, currentEffective, uuidMap);
    if (!equalTest) {
      foundError(group, c, current);
      return false;
    }

    if (c != null) {
      // if this is a composite group, lets also check the composite memberships.
      boolean result = checkCompositeMemberships(group, c, currentComposites);
      if (!result) {
        foundError(group, c, current);
        return false;
      }
    }

    return true;
  }

  /**
   * The purpose of this method is to simply check if two sets contain the same memberships.  The first set
   * contains the memberships that should exist based on re-adding immediate memberships through the API.
   * The second set contains the memberships that currently exist.  But this gets more complicated then it should
   * be because the "should" and "current" memberships will have different membership uuids and different
   * parent uuids.  There's probably a better way of doing this, but this seems to work....
   *
   * @param should The effective memberships that should exist for this group.
   * @param current The effective memberships that actually exist right now.
   * @param uuidMap maps the membership uuids for the immediate memberships between the "should" and "current" memberships.
   * @return true if the memberships are the same.
   */
  private static boolean checkEquality(Set<Membership> should, Set<Membership> current, Map<String,String> uuidMap) {
    // if the size of the two sets are different, we can just return false.
    if (should.size() != current.size()) {
      return false;
    }

    // if the sets have nothing, then return true
    if (should.size() == 0 && current.size() == 0) {
      return true;
    }

    // this will map a parent uuid with its children Memberships for the "should" memberships.
    Map<String,Set> childrenOfShouldMemberships = new HashMap<String, Set>();

    // given a parent uuid and a member uuid, we should get back at most 1 Membership.
    MultiKeyMap currentMembershipByParentAndMember = new MultiKeyMap();

    // populate the childrenOfShouldMemberships Map
    Iterator<Membership> shouldIterator = should.iterator();
    while (shouldIterator.hasNext()) {
      Membership m = shouldIterator.next();
      String parentUUID = m.getParentUuid();
      Set<Membership> children = childrenOfShouldMemberships.get(parentUUID);
      if (children == null) {
        children = new HashSet<Membership>();
      }
      
      children.add(m);
      childrenOfShouldMemberships.put(parentUUID, children);
    }

    // populate the currentMembershipByParentAndMember Map.
    Iterator<Membership> currentIterator = current.iterator();
    while (currentIterator.hasNext()) {
      Membership m = currentIterator.next();
      String parentUUID = m.getParentUuid();
      String memberUUID = m.getMemberUuid();
      currentMembershipByParentAndMember.put(parentUUID, memberUUID, m);
    }

    // initially the uuidsToProcess are the uuids of all immediate memberships.
    Set<String> uuidsToProcess = new HashSet<String>();
    Iterator<String> initialUuidsToProcessIterator  = uuidMap.keySet().iterator();
    while (initialUuidsToProcessIterator.hasNext()) {
      uuidsToProcess.add(initialUuidsToProcessIterator.next());
    }
    

    // keep looping until we've reached all child memberships.
    while (true) {

      // these are the membership uuids of the child memberships that we're currently looking at.
      Set<String> newUuidsToProcess = new HashSet<String>();

      Iterator<String> uuidsIterator = uuidsToProcess.iterator();

      while (uuidsIterator.hasNext()) {
        // the uuid for the "should" membership
        String shouldMembershipParentUUID = uuidsIterator.next();

        // the uuid for the "current" membership
        String currentMembershipParentUUID = uuidMap.get(shouldMembershipParentUUID);

        // the children of the uuid in the "should" set.
        Set<Membership> childMemberships = childrenOfShouldMemberships.get(shouldMembershipParentUUID);
        if (childMemberships == null) {
          continue;
        }

        // Go through each "should" membership and get the "current" membership.  See if they are equal...
        Iterator<Membership> childMembershipsIterator = childMemberships.iterator();
        while (childMembershipsIterator.hasNext()) {
          Membership shouldMembership = childMembershipsIterator.next();
          String shouldMembershipUUID = shouldMembership.getUuid();

          Membership currentMembership = (Membership)currentMembershipByParentAndMember.
            get(currentMembershipParentUUID, shouldMembership.getMemberUuid());
          if (currentMembership == null) {
            return false;
          }

          String currentMembershipUUID = currentMembership.getUuid();
          currentMembership.setUuid(shouldMembershipUUID);
          currentMembership.setParentUuid(shouldMembershipParentUUID);
          if (!currentMembership.equals(shouldMembership)) {
            return false; 
          }

          // Now that we know the "should" and "current" membership uuids for this membership,
          // add them to the uuidMap so we can find this membership's children.
          uuidMap.put(shouldMembershipUUID, currentMembershipUUID);
          newUuidsToProcess.add(shouldMembershipUUID);
        }
      }

      if (newUuidsToProcess.size() == 0) {
        break;
      }
      uuidsToProcess = newUuidsToProcess;
    }

    return true;
  }

  /**
   * Print usage.
   */
  private static void printUsage(Options options) {

    out.println();
 
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(FindBadMemberships.class.getSimpleName(), options, true);
 
    out.println();
    out.print("This script will find bad effective and composite memberships in your Grouper database.  ");
    out.print("It will not make any modifications to the Grouper database.  ");
    out.println("If bad memberships are found, this script will create a GSH script that will delete and re-add memberships.");
    out.println();
    out.println("To fix your memberships, complete these steps in the order listed:");
    out.println();
    out.println("1.  Review the GSH script before applying any changes to your database.");
    out.println("2.  Run the GSH script.");
    out.println("3.  Re-run the bad membership finder utility for all groups and stems since additional membership errors for other groups and stems may be revealed after the current groups and stems are fixed.");
    out.println();
  }

  /**
   * Map fields to privileges names used in GSH.
   *
   * @param field
   * @return the privilege matching the given field or null
   */
  private static String getPrivilegeString(Field field) {
    return list2priv.get(field.getName());
  }

  /**
   * We have found an error with a group membership.  Write a GSH script to delete and re-add the immediate memberships.
   *
   * @param group where an error was found
   * @param composite if this is a composite group, otherwise null.
   * @param current memberships for the group
   * @throws GroupNotFoundException
   */
  private static void foundError(Group group, Composite c, List<Membership> current) throws GroupNotFoundException {
    if (printErrorsToSTOUT) {
      out.println("FOUND BAD MEMBERSHIP: Bad membership in group with uuid=" + group.getUuid() + " and name=" + group.getName() + ".");
    }

    StringWriter deletes = new StringWriter();
    StringWriter adds = new StringWriter();

    Iterator<Membership> currentIterator = current.iterator();
    while (currentIterator.hasNext()) {
      Membership ms = currentIterator.next();
      if (ms.getType().equals(Membership.IMMEDIATE)) {
        try { 
          Field f = FieldFinder.find(ms.getListName());
          Member m = GrouperDAOFactory.getFactory().getMember().findByUuid(ms.getMemberUuid());
          String subjectId = m.getSubjectId();
          if (f.equals(Group.getDefaultList())) {
            deletes.write("delMember(\"" + group.getName() + "\", \"" + subjectId + "\")\n");
            adds.write("addMember(\"" + group.getName() + "\", \"" + subjectId + "\")\n");
          } else if (FieldType.LIST.equals(f.getType())) {
            String fieldString = "FieldFinder.find(\"" + ms.getListName() + "\")";
            deletes.write("delMember(\"" + group.getName() + "\", \"" + subjectId + "\", " + fieldString + ")\n");
            adds.write("addMember(\"" + group.getName() + "\", \"" + subjectId + "\", " + fieldString + ")\n");
          } else {
            String privilegeString = getPrivilegeString(f);
            deletes.write("revokePriv(\"" + group.getName() + "\", \"" + subjectId + "\", " + privilegeString + ")\n");
            adds.write("grantPriv(\"" + group.getName() + "\", \"" + subjectId + "\", " + privilegeString + ")\n");
          }
        } catch (SchemaException e) {
          // this shouldn't happen...
          throw new IllegalStateException(e.getMessage(), e);
        } catch (MemberNotFoundException e) {
          throw new IllegalStateException("Unable to find member object for this bad membership: " + e.getMessage(), e);
        }
      }
    }

    if (c != null) {
      String leftGroupName = c.getLeftGroup().getName();
      String rightGroupName = c.getRightGroup().getName();
      String compositeType = "";
      if (c.getType().equals(CompositeType.UNION)) {
        compositeType = "CompositeType.UNION";
      } else if (c.getType().equals(CompositeType.COMPLEMENT)) {
        compositeType = "CompositeType.COMPLEMENT";
      } else if (c.getType().equals(CompositeType.INTERSECTION)) {
        compositeType = "CompositeType.INTERSECTION";
      }

      deletes.write("delComposite(\"" + group.getName() + "\")\n");
      adds.write("addComposite(\"" + group.getName() + "\", " + compositeType + ", \"" + leftGroupName + "\", \"" + rightGroupName + "\")\n");
    }

    logGshScript(deletes.toString());
    logGshScript("sqlRun(\"delete from grouper_memberships where owner_id='" + group.getUuid() + "' and mship_type='effective'\")\n");
    logGshScript("sqlRun(\"delete from grouper_memberships where owner_id='" + group.getUuid() + "' and mship_type='composite'\")\n");
    logGshScript(adds.toString());
  }

  /**
   * We have found an error with a stem membership.  Write a GSH script to delete and re-add the immediate memberships.
   *
   * @param stem where an error was found
   * @param current memberships for the stem
   */
  private static void foundError(Stem stem, List<Membership> current) {
    if (printErrorsToSTOUT) {
      out.println("FOUND BAD MEMBERSHIP: Bad membership in stem with uuid=" + stem.getUuid() + " and name=" + stem.getName() + ".");
    }

    StringWriter deletes = new StringWriter();
    StringWriter adds = new StringWriter();

    Iterator<Membership> currentIterator = current.iterator();
    while (currentIterator.hasNext()) {
      Membership ms = currentIterator.next();
      if (ms.getType().equals(Membership.IMMEDIATE)) {
        try {
          Field f = FieldFinder.find(ms.getListName());
          Member m = GrouperDAOFactory.getFactory().getMember().findByUuid(ms.getMemberUuid());
          String subjectId = m.getSubjectId();
          String privilegeString = getPrivilegeString(f);
          deletes.write("revokePriv(\"" + stem.getName() + "\", \"" + subjectId + "\", " + privilegeString + ")\n");
          adds.write("grantPriv(\"" + stem.getName() + "\", \"" + subjectId + "\", " + privilegeString + ")\n");
        } catch (SchemaException e) {
          // this shouldn't happen...
          throw new IllegalStateException(e.getMessage(), e);
        } catch (MemberNotFoundException e) {
          throw new IllegalStateException("Unable to find member object for this bad membership: " + e.getMessage(), e);
        }
      }
    }

    logGshScript(deletes.toString());
    logGshScript("sqlRun(\"delete from grouper_memberships where owner_id='" + stem.getUuid() + "' and mship_type='effective'\")\n");
    logGshScript("sqlRun(\"delete from grouper_memberships where owner_id='" + stem.getUuid() + "' and mship_type='composite'\")\n");
    logGshScript(adds.toString());
  }

  /**
   * We have found an error with a membership that does not have an owner.  Write a GSH script to delete the membership.
   *
   * @param ms is the bad membership
   */
  private static void foundError(Membership ms) {
    if (printErrorsToSTOUT) {
      out.println("FOUND BAD MEMBERSHIP: Membership with uuid=" + ms.getUuid() + " has invalid owner with uuid=" + ms.getOwnerUuid() + ".");
    }
    logGshScript("sqlRun(\"delete from grouper_memberships where id='" + ms.getUuid() + "'\")\n");
  }

  /**
   * Keep the GSH script in memory until we're done with this utility.
   *
   * @param script to log
   */
  private static void logGshScript(String script) {
    if (gshScript == null) {
      gshScript = new StringWriter();
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
}

