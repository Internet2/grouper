/**
 * Copyright 2012 Internet2
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
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import org.apache.commons.lang.StringUtils;

import bsh.CallStack;
import bsh.Interpreter;

/**
 * Display usage information.
 * <p/>
 * @author  blair christensen.
 * @version $Id: help.java,v 1.20 2009-08-12 12:44:45 shilen Exp $
 * @since   0.0.1
 */
public class help {

  /**
   * get help on a specific command
   * @param interpreter
   * @param callStack
   * @param helpOn
   */
  public static void invoke(Interpreter interpreter, CallStack callStack,
      String helpOn) {
    if (StringUtils.equalsIgnoreCase(helpOn, "XmlExport") ) {
      interpreter.println("XmlExport help:\n"
          + "There is an object: XmlExport which has various chaining methods, \n" +
          		"which should be ended with an exportTo() method.  You can export to file or string.\n\n"
          + "XmlExport xmlExport.stem(stem)  The stem to export. Defaults to the ROOT stem.\n"
          + "XmlExport xmlExport.group(group)  The group to export\n"
          + "XmlExport xmlExport.relative(boolean)  If group or stem specified do not export parent Stems.\n"
          + "XmlExport xmlExport.includeParent(boolean)  If group specified, export from the parent stem\n"
          + "XmlExport xmlExport.userProperties(file)  Properties file for extra settings for import\n"
          + "XmlExport xmlExport.grouperSession(grouperSession)  Operate " +
          		"\n   within a certain grouper session (defaults to root session)\n"
          + "void xmlExport.exportToFile(file)  Export to an XML file\n"
          + "void xmlExport.exportToString(string)  Export to an XML string\n"
          + "\n"
          + " Examples:\n"
          + "\n"
          + "gsh 1% new XmlExport().exportToFile(new File(\"c:\\\\temp\\\\export.xml\"))\n"
          + "\n\n\n"
          + "gsh 1% grouperSession = GrouperSession.start(SubjectFinder.findById(\"mchyzer\"));\n"
          + "\n"
          + "gsh 2% stem = StemFinder.findByName(grouperSession, \"aStem\");\n"
          + "\n"
          + "gsh 3% new XmlExport().stem(stem).relative(true).userProperties(\n" +
          		"new File(\"C:\\temp\\some.properties\")).grouperSession(grouperSession)\n" +
          		".exportToFile(new File(\"c:\\\\temp\\\\export.xml\"));\n"
          + "\n"
          + " -or- (without chaining)\n"
          + "\n"
          + "gsh 3% xmlExport = new XmlExport();\n"
          + "\n"
          + "gsh 4% xmlExport.stem(stem);\n"
          + "\n"
          + "gsh 5% xmlExport.grouperSession(grouperSession);\n"
          + "\n"
          + "gsh 6% xmlExport.exportToFile(new File(\"c:\\\\temp\\\\export.xml\"))\n"
          );
      return;
    }
    if (StringUtils.equalsIgnoreCase(helpOn, "GroupCopy") ) {
      interpreter.println("GroupCopy help:\n"
          + "There is an object: GroupCopy which has various chaining methods, \n" +
              "which should be ended with a save() method.\n\n"
          + "GroupCopy groupCopy = new GroupCopy(group, stem)  Create a new instance.\n"
          + "GroupCopy groupCopy.copyPrivilegesOfGroup(boolean)  Whether to copy privileges of the group.  Default is true.\n"
          + "GroupCopy groupCopy.copyGroupAsPrivilege(boolean)  Whether to copy privileges where this group is a member.  Default is true.\n"
          + "GroupCopy groupCopy.copyListMembersOfGroup(boolean)  Whether to copy the list memberships of the group.  Default is true.\n"
          + "GroupCopy groupCopy.copyListGroupAsMember(boolean)  Whether to copy list memberships where this group is a member.  Default is true.\n"
          + "GroupCopy groupCopy.copyAttributes(boolean)  Whether to copy attributes.  Default is true.\n"
          + "Group groupCopy.save()  Copies the group.\n"
          + "\n"
          + " Examples:\n"
          + "\n"
          + "gsh 1% new GroupCopy(group, stem).copyAttributes(false).save()\n"
          + "\n"
          + " -or- (without chaining)\n"
          + "\n"
          + "gsh 2% groupCopy = new GroupCopy(group, stem);\n"
          + "\n"
          + "gsh 3% groupCopy.copyAttributes(false);\n"
          + "\n"
          + "gsh 4% groupCopy.save();\n"
          + "\n"
          + " -or- (if you want to use the default options)\n"
          + "\n"
          + "gsh 5% group.copy(stem);\n"
          );
      return;
    }
    if (StringUtils.equalsIgnoreCase(helpOn, "GroupMove") ) {
      interpreter.println("GroupMove help:\n"
          + "There is an object: GroupMove which has various chaining methods, \n" +
              "which should be ended with a save() method.\n\n"
          + "GroupMove groupMove = new GroupMove(group, stem)  Create a new instance.\n"
          + "GroupMove groupMove.assignAlternateName(boolean)  Whether to add the current name of the group to the group's alternate names list.  Default is true.\n"
          + "void groupMove.save()  Moves the group.\n"
          + "\n"
          + " Examples:\n"
          + "\n"
          + "gsh 1% new GroupMove(group, stem).assignAlternateName(false).save()\n"
          + "\n"
          + " -or- (without chaining)\n"
          + "\n"
          + "gsh 2% groupMove = new GroupMove(group, stem);\n"
          + "\n"
          + "gsh 3% groupMove.assignAlternateName(false);\n"
          + "\n"
          + "gsh 4% groupMove.save();\n"
          + "\n"
          + " -or- (if you want to use the default options)\n"
          + "\n"
          + "gsh 5% group.move(stem);\n"
          );
      return;
    }
    if (StringUtils.equalsIgnoreCase(helpOn, "StemCopy") ) {
      interpreter.println("StemCopy help:\n"
          + "There is an object: StemCopy which has various chaining methods, \n" +
              "which should be ended with a save() method.\n\n"
          + "StemCopy stemCopy = new StemCopy(stemToCopy, destinationStem)  Create a new instance.\n"
          + "StemCopy stemCopy.copyPrivilegesOfStem(boolean)  Whether to copy privileges of stems.  Default is true.\n"
          + "StemCopy stemCopy.copyPrivilegesOfGroup(boolean)  Whether to copy privileges of groups.  Default is true.\n"
          + "StemCopy stemCopy.copyGroupAsPrivilege(boolean)  Whether to copy privileges where groups are a member.  Default is true.\n"
          + "StemCopy stemCopy.copyListMembersOfGroup(boolean)  Whether to copy the list memberships of groups.  Default is true.\n"
          + "StemCopy stemCopy.copyListGroupAsMember(boolean)  Whether to copy list memberships where groups are a member.  Default is true.\n"
          + "StemCopy stemCopy.copyAttributes(boolean)  Whether to copy attributes.  Default is true.\n"
          + "Stem stemCopy.save()  Copies the stem.\n"
          + "\n"
          + " Examples:\n"
          + "\n"
          + "gsh 1% new StemCopy(stemToCopy, destinationStem).copyAttributes(false).save()\n"
          + "\n"
          + " -or- (without chaining)\n"
          + "\n"
          + "gsh 2% stemCopy = new StemCopy(stemToCopy, destinationStem);\n"
          + "\n"
          + "gsh 3% stemCopy.copyAttributes(false);\n"
          + "\n"
          + "gsh 4% stemCopy.save();\n"
          + "\n"
          + " -or- (if you want to use the default options)\n"
          + "\n"
          + "gsh 5% stem.copy(destinationStem);\n"
          );
      return;
    }
    if (StringUtils.equalsIgnoreCase(helpOn, "StemMove") ) {
      interpreter.println("StemMove help:\n"
          + "There is an object: StemMove which has various chaining methods, \n" +
              "which should be ended with a save() method.\n\n"
          + "StemMove stemMove = new StemMove(stemToMove, destinationStem)  Create a new instance.\n"
          + "StemMove stemMove.assignAlternateName(boolean)  Whether to add the current names of the affected stems and groups to their alternate name lists.  Default is true.\n"
          + "void stemMove.save()  Moves the stem.\n"
          + "\n"
          + " Examples:\n"
          + "\n"
          + "gsh 1% new StemMove(stemToMove, destinationStem).assignAlternateName(false).save()\n"
          + "\n"
          + " -or- (without chaining)\n"
          + "\n"
          + "gsh 2% stemMove = new StemMove(stemToMove, destinationStem);\n"
          + "\n"
          + "gsh 3% stemMove.assignAlternateName(false);\n"
          + "\n"
          + "gsh 4% stemMove.save();\n"
          + "\n"
          + " -or- (if you want to use the default options)\n"
          + "\n"
          + "gsh 5% stem.move(destinationStem);\n"
          );
      return;
    }
    if (StringUtils.equalsIgnoreCase(helpOn, "XmlImport") ) {
      interpreter.println("XmlImport help:\n"
          + "There is an object: XmlImport which has various chaining methods,\n"
          + "which should be ended with an importFrom() method.  You can import from file, string, or url.\n\n"
          + "XmlImport xmlImport.stem(stem)  The Stem into which\n" +
          		"   data will be imported. Defaults to the ROOT stem.\n"
          + "XmlImport xmlImport.updateList(boolean)   XML contains\n" +
          		"   a flat list of Stems or Groups which may be updated.\n"
          + "Missing Stems and Groups are not created.\n"
          + "XmlImport xmlImport.userProperties(file)\n"
          + "  Properties file for extra settings for import\n"
          + "XmlImport xmlImport.grouperSession(grouperSession)\n"
          + "  Operate within a certain grouper session (defaults to root session)\n"
          + "void xmlImport.importFromFile(file)  Import from an XML file\n"
          + "void xmlImport.importFromString(string)  Import from an XML string\n"
          + "void xmlImport.importFromUrl(url)  Import XML from a URL\n"
          + "\n"
          + " Examples:\n"
          + " \n"
          + "gsh 1% new XmlImport().importFromFile(new File(\"c:\\\\temp\\\\export.xml\"))\n"
          + "\n\n\n"
          + "gsh 1% grouperSession = GrouperSession.start(SubjectFinder.findById(\"mchyzer\"));\n"
          + "\n"
          + "gsh 2% stem = StemFinder.findByName(grouperSession, \"aStem\");\n"
          + "\n"
          + "gsh 3% new XmlImport().stem(stem).updateList(true).userProperties(\n"
          + "    new File(\"C:\\\\temp\\\\some.properties\")).grouperSession(grouperSession)\n"
          + "    .importFromUrl(new URL(\"http://whatever.xml\"));\n"
          + "    \n"
          + " -or- (without chaining)\n"
          + "\n"
          + "gsh 3% xmlImport = new XmlImport();\n"
          + "\n"
          + "gsh 4% xmlImport.stem(stem);\n"
          + "\n"
          + "gsh 5% xmlImport.grouperSession(grouperSession);\n"
          + "\n"
          + "gsh 6% xmlImport.importFromFile(new File(\"c:\\\\temp\\\\export.xml\"))\n"
          + "\n");
      return;
    }
    if (StringUtils.equalsIgnoreCase(helpOn, "transaction") || StringUtils.equalsIgnoreCase(helpOn, "transactions")) {
      interpreter.println("transaction help:\n"
        + "- help(\"transaction\")       print help information\n"
        + "\nTransactions facilitate all commands succeeding or failing together, and perhaps some level of " +
        		"repeatable reads of the DB (depending on the DB).  If there is an open transaction and " +
        		"an exception is thrown in a command, GSH will shut down so that subsequent commands will " +
        		"not execute outside of a transaction.\n\n"        
        + "- transactionStatus()         print the list of nested transactions\n"
        + "- transactionStart(\"<GrouperTransactionType>\")         start a transaction, or make sure one is already started\n"
        + "    Can use: \"READONLY_OR_USE_EXISTING\", \"NONE\", \"READONLY_NEW\",\n"
        + "      \"READ_WRITE_OR_USE_EXISTING\", \"READ_WRITE_NEW\"\n"
        + "- transactionCommit(\"<GrouperCommitType>\")         commit a transaction\n"
        + "    Can use: \"COMMIT_NOW\", \"COMMIT_IF_NEW_TRANSACTION\n"
        + "- transactionRollback(\"<GrouperRollbackType>\")         rollback a transaction\n"
        + "    Can use: \"ROLLBACK_NOW\", \"ROLLBACK_IF_NEW_TRANSACTION\n"
        + "- transactionEnd()         end a transaction.\n"
        + "    Note if it was read/write, and not committed or rolled back, this will commit and end\n");
      return;
    } 
    if (StringUtils.equalsIgnoreCase(helpOn, "usdu")) {
      interpreter.println("usdu help: unresolvable subject deletion utility\n"
          + "- usdu finds which memberships are with subjects which cannot be found in a subject source, \n"
          +		"and prints them on the screen\n"
          + "- if the usdu.DELETE option is passed in, then the memberships will be deleted\n"
          + "- a grouper session must be open when this command is run.\n"
          + "- here is an example is a complete usdu run (a couple of commands):\n\n"
          + "subject=SubjectFinder.findById(\"GrouperSystem\")\n"
          + "session=GrouperSession.start(subject)\n"
          + "usdu()\n\n"
          + "- you can pass in that you want to delete memberships in the usdu call:\n\n"
          + "usdu(usdu.DELETE)\n"
          + "- you can work only in a specific subject source, pass in the sourceId from sources.xml:\n"
          + "usduBySource(\"schoolperson\")\n"
          + "- you can work in a specific source and delete membeships:\n"
          + "usduBySource(\"schoolperson\", usdu.DELETE)\n"
          + "- you can work only with a specific member\n\n"
          + "subject=SubjectFinder.findById(\"GrouperSystem\")\n"
          + "session=GrouperSession.start(subject)\n"
          + "memberSubject=SubjectFinder.findById(\"1234567\")\n"
          + "member = MemberFinder.findBySubject(session, memberSubject)\n"
          + "usduByMember(member)\n"
          + "- usdu by member, and delete memberships:\n"
          + "usduByMember(member, usdu.DELETE)\n");
      
      return;
    }
    if (StringUtils.equalsIgnoreCase(helpOn, "findBadMemberships")) {
      interpreter.println("findBadMemberships help: \n"
          + "- this command will find membership records in the database which are invalid \n"
          +   "and print them on the screen.\n"
          + "- if bad memberships are found, a GSH script called findbadmemberships.gsh will be created.\n"
          + "- to fix your bad membership, do the following:\n"
          + "  1.  Review the GSH script before applying any changes to your database.\n"
          + "  2.  Run the GSH script.\n"
          + "  3.  Re-run the bad membership finder utility to verify that bad memberships have been fixed.\n"
          + "- here is an example is a complete findBadMemberships run:\n\n"
          + "findBadMemberships()\n");
      
      return;
    }
    interpreter.println("cant find help on command: " + helpOn);
    
  }
  
  /**
   * Display usage information.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @since   0.0.1
   */
  public static void invoke(Interpreter i, CallStack stack) {
    GrouperShell.setOurCommand(i, true);
    i.println("Visit the wiki for more GSH documentation:");
    i.println("  https://wiki.internet2.edu/confluence/display/GrouperWG/GrouperShell+(gsh)");
    i.println("Also see the Java BeanShell docs: http://www.beanshell.org/manual/contents.html");
    i.println("# COMMANDS"                                            );
    i.println("* var = method(args)    Invoke any Grouper API method" );
    i.println("* addComposite(group, type, left group, right group)"  );
    i.println("* addGroup(parent, extension, displayExtension)"       );
    i.println("* addMember(group, subject id)"                        );
    i.println("* addMember(group, subject id, Field)"                 );
    i.println("* addRootStem(extension, displayExtension)"            );
    i.println("* addStem(parent, extension, displayExtension)"        );
    i.println("* addSubject(id, type, name)"                          );
    i.println("* delComposite(group)"                                 );
    i.println("* delGroup(name)"                                      );
    i.println("* delMember(group, subject id)"                        );
    i.println("* delMember(group, subject id, Field)"                 );
    i.println("* delStem(name)"                                       );
    i.println("* exit"                                                );
    i.println("* findBadMemberships: type   help(\"findBadMemberships\")   for info on finding bad memberships");
    i.println("* findSubject(id)"                                     );
    i.println("* findSubject(id, type)"                               );
    i.println("* findSubject(id, type, source)"                       );
    i.println("* getGroupAttr(stem, attr)"                            );
    i.println("* getGroups(name)"                                     );
    i.println("* GroupCopy: type   help(\"GroupCopy\")   for info on copying a group");
    i.println("* GroupMove: type   help(\"GroupMove\")   for info on moving a group");
    i.println("* GroupFinder.findByAlternateName(grouperSession, name, exceptionIfNotFound)");
    i.println("* GroupFinder.findByCurrentName(grouperSession, name, exceptionIfNotFound)");
    i.println("* GroupFinder.findByName(grouperSession, name, exceptionIfNotFound)");
    i.println("* GroupFinder.findByUuid(grouperSession, uuid)");
    i.println("* getMembers(group)"                                   );
    i.println("* getSources()"                                        );
    i.println("* getStemAttr(stem, attr)"                             );
    i.println("* getStems(name)"                                      );
    i.println("* group.addAlternateName(name)");
    i.println("* group.getAlternateNames()");
    i.println("* group.deleteAlternateName(name)");
    i.println("* group.manageIncludesExcludes(grouperSession, isIncludeExclude)");
    i.println("* group.manageIncludesExcludes(grouperSession, isIncludeExclude, groupThatMembersMustAlsoBeIn)");
    i.println("* group.manageIncludesExcludes(grouperSession, isIncludeExclude, setOfGroupsThatMembersMustAlsoBeIn)");
    i.println("* StemCopy: type   help(\"StemCopy\")   for info on copying a stem");
    i.println("* StemMove: type   help(\"StemMove\")   for info on moving a stem");
    i.println("* stem = StemFinder.findByAlternateName(grouperSession, name, exceptionOnNotFound, queryOptions)");
    i.println("* stem = StemFinder.findByCurrentName(grouperSession, name, exceptionOnNotFound, queryOptions)");
    i.println("* stem = StemFinder.findByName(grouperSession, name)");
    i.println("* stem = StemFinder.findByUuid(grouperSession, uuid)");
    i.println("* grantPriv(name, subject id, Privilege)"              );
    i.println("* hasMember(group, subject id)"                        );
    i.println("* hasMember(group, subject id, Field)"                 );
    i.println("* hasPriv(name, subject id, Privilege)"                );
    i.println("* help()"                                              );
    i.println("* history()"                                           );
    i.println("* history(n)"                                          );
    i.println("* last()"                                              );
    i.println("* last(n)"                                             );
    i.println("* loaderRunOneJob(loaderGroup);");
    i.println("* loaderRunOneJob(\"MAINTENANCE_cleanLogs\");");
    i.println("");
    i.println("* grouperSession = GrouperSession.startRootSession();");
    i.println("* oldSubject = findSubject(\"10021368\");");
    i.println("* member = MemberFinder.findBySubject(grouperSession, oldSubject);");
    i.println("* newSubject = findSubject(\"10021366\");");
    i.println("* member.changeSubject(newSubject);");
    i.println("* member.changeSubject(newSubject, !Member.DELETE_OLD_MEMBER);");
    i.println("* member.changeSubjectReport(newSubject, Member.DELETE_OLD_MEMBER);");
    i.println("");
    i.println("* p(command)"                                          );
    i.println("* quit"                                                );
    i.println("* resetRegistry()"                                     );
    i.println("* registryInstall()     will insert default Grouper data if not there, e.g. root stem");
    i.println("* registryInitializeSchema()     will generate schema DDL for the DB, and wont drop before creating, will not run script.  Note: gsh -registry is probably better");
    i.println("* registryInitializeSchema(registryInitializeSchema.DROP_THEN_CREATE)  generate DDL for the DB, dropping existing tables, will not run script.  Note: gsh -registry is probably better");
    i.println("* registryInitializeSchema(registryInitializeSchema.WRITE_AND_RUN_SCRIPT)  generate DDL for the DB, not dropping, but will run the script after writing it to file.  Note: gsh -registry is probably better");
    i.println("* registryInitializeSchema(registryInitializeSchema.DROP_THEN_CREATE | registryInitializeSchema.WRITE_AND_RUN_SCRIPT)  generate DDL for the DB, drop existing grouper tables, and run the script after writing it to file.  Note: gsh -registry is probably better");
    i.println("* revokePriv(name, subject id, Privilege)"             );
    i.println("* setGroupAttr(stem, attr, value)"                     );
    i.println("* setStemAttr(stem, attr, value)"                      );
    i.println("* sqlRun(new File(\"C:\\\\temp\\\\grouperDdl_20081024_01_46_46_296.sql\"));");
    i.println("* sqlRun(string)"                                      );
    i.println("* transaction: type  help(\"transaction\")  for more info");
    i.println("* typeAdd(name)"                                       );
    i.println("* typeAddAttr(type, name)"                             );
    i.println("* typeAddList(type, name, read, write)"                );
    i.println("* typeDel(name)"                                       );
    i.println("* typeDelField(type, name)"                            );
    i.println("* typeFind(name)"                                      );
    i.println("* typeGetFields(name)"                                 );
    i.println("* usdu: type  help(\"usdu\")  for more info on unresolvable subject deletion utility");
    i.println("* XmlExport: type  help(\"XmlExport\")  for more info on xml export");
    i.println("* XmlImport: type  help(\"XmlImport\")  for more info on xml import");
    i.println("* version()"                                           );
    i.println("* GrouperReport: new GrouperReport().findBadMemberships(boolean).findUnresolvables(boolean).runReport();");
    i.println(""                                                      );
    i.println("# VARIABLES");
    i.println("* GSH_DEBUG=true");
    i.println("* GSH_DEVEL=true");
    i.println("* GSH_TIMER=true");
    i.println("Note: you cannot encrypt passwords with GSH since the passwords end up in the GSH history.  To encrypt passwords, issue the command:");
    i.println("  C:\\mchyzer\\isc\\dev\\grouper-qs-1.2.0\\grouper>java -jar lib\\grouper\\morphString.jar");
    i.println("  Enter the location of morphString.properties: conf/morphString.properties");

  } // public static void invoke(i, stack)

} // public class help

