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

package edu.internet2.middleware.grouper.registry;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cache.GrouperCacheUtils;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Perform low-level operations on the Groups Registry.
 * <p>
 * <strong>WARNING:</strong> Do <strong>not</strong> run the methods
 * expose by this class against your Grouper installation unless you
 * know what you are doing.  It <strong>will</strong> delete data.
 * </p>
 * @author  blair christensen.
 * @version $Id: RegistryReset.java,v 1.13 2009-10-23 15:28:31 tzeller Exp $
 */
public class RegistryReset {

  // PRIVATE CLASS CONSTANTS //
  private static final String SUBJ_TYPE = "person"; 


  // CONSTRUCTORS //
  private RegistryReset() {
    super();
  } // private RegistryReset()



  // PUBLIC CLASS METHODS //
 
  /**
   * Reset the Groups Registry.
   * <p>
   * <strong>WARNING:</strong> This is a destructive act and will
   * delete all groups, stems, members, memberships and subjects from
   * your Groups Registry.  Do <strong>not</strong> run this unless
   * that is what you want.
   * </p>
   * <pre class="eg">
   * % java edu.internet2.middleware.grouper.RegistryReset
   * </pre>
   * @param args 
   */
  public static void main(String[] args) {
    if (args != null && args.length == 1 && StringUtils.equals("addSubjects", args[0])) {
      RegistryReset.internal_resetRegistryAndAddTestSubjects();
    } else {
      RegistryReset.reset();
    }
    System.exit(0);
  } // public static void main(args)

  /**
   * Attempt to reset the Groups Registry to a pristine state.
   */
  public static void reset() {
    reset(true, true);
  }
  /**
   * Attempt to reset the Groups Registry to a pristine state.
   * @param promptUser 
   * @param includeTypesAndFields
   */
  public static void reset(boolean promptUser, boolean includeTypesAndFields) {
    
    //make sure it is ok to change db
    if(promptUser) {
      GrouperUtil.promptUserAboutDbChanges(GrouperUtil.PROMPT_KEY_RESET_DATA, true);
    }
    
    GrouperStartup.startup();
    
    RegistryReset rr = new RegistryReset();
    try {
      MemberFinder.clearInternalMembers();
      GrouperDAOFactory.internal_resetFactory();  // as it is static and cached
      rr._emptyTables(includeTypesAndFields);
    }
    catch (Exception e) {
      e.printStackTrace();
      rr._abort(e.getMessage());
    }
  } 


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static void internal_addTestSubjects() { 
    RegistryReset rr = new RegistryReset();
    try {
      rr._addSubjects();
    }
    catch (Exception e) {
      e.printStackTrace();
      rr._abort(e.getMessage());
    }
  }

  /**
   * 
   */
  public static void internal_resetRegistryAndAddTestSubjects() { 
    internal_resetRegistryAndAddTestSubjects(true);
  }
  
  /**
   * 
   * @param includeTypesAndFields
   */
  public static void internal_resetRegistryAndAddTestSubjects(boolean includeTypesAndFields) { 
    RegistryReset rr = new RegistryReset();
    try {
      MemberFinder.clearInternalMembers();
      rr._emptyTables(includeTypesAndFields);
      rr._addSubjects();
    }
    catch (Exception e) {
      rr._abort(ExceptionUtils.getFullStackTrace(e));
    }
    
    GrouperCacheUtils.clearAllCaches();

  } 


  // PRIVATE INSTANCE METHODS //
  public void _addSubjects()   
    throws  GrouperException
  {
    _addSubjects(0, 10);
  }
  
  public static void _addSubjects(int start, int end)   
  throws  GrouperException
  {
    for (int i=start; i<end; i++) {
      String id   = "test.subject." + i;
      String name = "my name is " + id;
      RegistrySubject registrySubject = new RegistrySubject();
      registrySubject.setId(id);
      registrySubject.setName(name);
      registrySubject.setTypeString(SUBJ_TYPE);
      
      registrySubject.getAttributes().put("name", GrouperUtil.toSet("name." + id));
      registrySubject.getAttributes().put("loginid", GrouperUtil.toSet("id." + id));
      registrySubject.getAttributes().put("description", GrouperUtil.toSet("description." + id));
      registrySubject.getAttributes().put("email", GrouperUtil.toSet(id + "@somewhere.someSchool.edu"));
      
      GrouperDAOFactory.getFactory().getRegistrySubject().create(registrySubject);
    }
  } 

  private void _abort(String msg) 
    throws  GrouperException
  {
    LOG.error(msg);
    throw new GrouperException(msg);
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(RegistryReset.class);

  /**
   * 
   * @param includeTypesAndFields
   * @throws GrouperException
   */
  private void _emptyTables(boolean includeTypesAndFields) 
    throws  GrouperException
  {
    GrouperDAOFactory.getFactory().getRegistry().reset(includeTypesAndFields);
    // Now update the cached types + fields
    GroupTypeFinder.internal_updateKnownTypes();
    FieldFinder.internal_updateKnownFields();
    SubjectFinder.reset(); 
  } 

} 

