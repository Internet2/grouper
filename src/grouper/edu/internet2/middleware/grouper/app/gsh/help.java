/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import org.apache.commons.lang.StringUtils;

import  bsh.*;

/**
 * Display usage information.
 * <p/>
 * @author  blair christensen.
 * @version $Id: help.java,v 1.4 2008-09-22 15:06:40 mchyzer Exp $
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
    if (StringUtils.equals(helpOn, "transaction") || StringUtils.equals(helpOn, "transactions")) {
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
    i.println("* addRootStem(extension, displayExtension)"            );
    i.println("* addStem(parent, extension, displayExtension)"        );
    i.println("* addSubject(id, type, name)"                          );
    i.println("* delComposite(group)"                                 );
    i.println("* delGroup(name)"                                      );
    i.println("* delMember(group, subject id)"                        );
    i.println("* delStem(name)"                                       );
    i.println("* exit"                                                );
    i.println("* findSubject(id)"                                     );
    i.println("* findSubject(id, type)"                               );
    i.println("* findSubject(id, type, source)"                       );
    i.println("* getGroupAttr(stem, attr)"                            );
    i.println("* getGroups(name)"                                     );
    i.println("* getMembers(group)"                                   );
    i.println("* getSources()"                                        );
    i.println("* getStemAttr(stem, attr)"                             );
    i.println("* getStems(name)"                                      );
    i.println("* grantPriv(name, subject id, Privilege)"              );
    i.println("* hasMember(group, subject id)"                        );
    i.println("* hasPriv(name, subject id, Privilege)"                );
    i.println("* help()"                                              );
    i.println("* history()"                                           );
    i.println("* history(n)"                                          );
    i.println("* last()"                                              );
    i.println("* last(n)"                                             );
    i.println("* p(command)"                                          );
    i.println("* quit"                                                );
    i.println("* resetRegistry()"                                     );
    i.println("* registryInstall()     will insert default Grouper data if not there, e.g. root stem");
    i.println("* revokePriv(name, subject id, Privilege)"             );
    i.println("* setGroupAttr(stem, attr, value)"                     );
    i.println("* setStemAttr(stem, attr, value)"                      );
    i.println("* transaction: type  help(\"transaction\")  for more info");
    i.println("* typeAdd(name)"                                       );
    i.println("* typeAddAttr(type, name, read, write, req)"           );
    i.println("* typeAddList(type, name, read, write)"                );
    i.println("* typeDel(name)"                                       );
    i.println("* typeDelField(type, name)"                            );
    i.println("* typeFind(name)"                                      );
    i.println("* typeGetFields(name)"                                 );
    i.println("* version()"                                           );
    i.println(""                                                      );
    i.println("# VARIABLES"                                           );
    i.println("* GSH_DEBUG"                                           );
    i.println("* GSH_DEVEL"                                           );
    i.println("* GSH_TIMER"                                           );
    i.println("Note: you cannot encrypt passwords with GSH since the passwords end up in the GSH history.  To encrypt passwords, issue the command:");
    i.println("  C:\\mchyzer\\isc\\dev\\grouper-qs-1.2.0\\grouper>java -jar lib\\morphString.jar");
    i.println("  Enter the location of morphString.properties: conf/morphString.properties");

  } // public static void invoke(i, stack)

} // public class help

