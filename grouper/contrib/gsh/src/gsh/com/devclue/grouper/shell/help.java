/*
 * Copyright (C) 2006 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.*;

/**
 * Display usage information.
 * <p/>
 * @author  blair christensen.
 * @version $Id: help.java,v 1.2 2006-08-08 17:56:10 blair Exp $
 * @since   0.0.1
 */
public class help {

  // PUBLIC CLASS METHODS //

  /**
   * Display usage information.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @since   0.0.1
   */
  public static void invoke(Interpreter i, CallStack stack) {
    GrouperShell.setOurCommand(i, true);
    i.println("# COMMANDS"                                            );
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
    i.println("* revokePriv(name, subject id, Privilege)"             );
    i.println("* setGroupAttr(stem, attr, value)"                     );
    i.println("* setStemAttr(stem, attr, value)"                      );
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
  } // public static void invoke(i, stack)

} // public class help

