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

package edu.internet2.middleware.grouper.registry;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cache.GrouperCacheUtils;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/** 
 * Install the Groups Registry.  When there is a newly created (or truncated database),
 * this will put the base records that grouper needs to operate (e.g. root stem)
 * <p/>
 * @author  blair christensen.
 * @version $Id: RegistryInstall.java,v 1.12 2009-09-21 06:14:27 mchyzer Exp $    
 */
public class RegistryInstall {


  /**
   * @param args
   */
  public static void main(String[] args) {
    install();
  }

  /**
   * install the registry if it is not already installeds
   */
  public static void install() {
    // Install group types, fields and privileges
    try {
      GrouperSession s = GrouperSession.start( SubjectFinder.findRootSubject(), false );
      
      GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          //make sure the ALL subject is created before the change log happens
          MemberFinder.findBySubject(grouperSession, SubjectFinder.findAllSubject(), true);
          boolean changed = false;
          try {
            changed = changed | _installFieldsAndTypes(grouperSession);
            changed = changed | _installGroupsAndStems(grouperSession);

            if (changed) {
              ChangeLogTempToEntity.convertRecords();
            }

            if (changed && LOG.isWarnEnabled()) {
              LOG.warn("Registry was initted (default fields, types, stem, etc inserted)");
            }
          } catch (Exception e) {
            throw new GrouperSessionException(e);
          }
          return null;
        }
        
      });
      
      s.stop();
    }
    catch (Throwable throwable) {
      //unwrap exception
      if (throwable instanceof GrouperSessionException && throwable.getCause() != null) {
        throwable = throwable.getCause();
      }
      String msg = "unable to initialize registry: " + throwable.getMessage();
      if (GrouperStartup.logErrorStatic) {
        LOG.fatal(msg, throwable);
      } else {
        LOG.debug(msg, throwable);
      }
      throw new GrouperException(msg, throwable);
    }
    GrouperCacheUtils.clearAllCaches();

  }
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(RegistryInstall.class);


  /**
   * 
   * @param s
   * @return true if any changes were made
   * @throws InsufficientPrivilegeException
   * @throws SchemaException
   */
  private static boolean _installFieldsAndTypes(GrouperSession s) 
    throws  InsufficientPrivilegeException,
            SchemaException {
    //note, no need for GrouperSession inverse of control
    boolean changed = false;
    boolean[] changedArray = {false};

    // base lists
    Field.internal_addField( s, "members", FieldType.LIST, AccessPrivilege.READ, 
        AccessPrivilege.UPDATE, false, false , changedArray, null);
    changed = changed || changedArray[0];
    // reserve access privs
    Field.internal_addField( s, Field.FIELD_NAME_ADMINS,   FieldType.ACCESS, AccessPrivilege.ADMIN,  
        AccessPrivilege.ADMIN,  false, false , changedArray, null);
    changed = changed || changedArray[0];
    Field.internal_addField( s, Field.FIELD_NAME_OPTOUTS,  FieldType.ACCESS, AccessPrivilege.UPDATE,
        AccessPrivilege.UPDATE, false, false , changedArray, null);
    changed = changed || changedArray[0];
    Field.internal_addField( s, Field.FIELD_NAME_OPTINS,   FieldType.ACCESS, AccessPrivilege.UPDATE, 
        AccessPrivilege.UPDATE, false, false , changedArray, null);
    changed = changed || changedArray[0];
    Field.internal_addField( s, Field.FIELD_NAME_READERS,  FieldType.ACCESS, AccessPrivilege.ADMIN,  
        AccessPrivilege.ADMIN,  false, false , changedArray, null);
    changed = changed || changedArray[0];
    Field.internal_addField( s, Field.FIELD_NAME_UPDATERS, FieldType.ACCESS, AccessPrivilege.ADMIN,  
        AccessPrivilege.ADMIN,  false, false, changedArray , null);
    changed = changed || changedArray[0];
    Field.internal_addField( s, Field.FIELD_NAME_VIEWERS,  FieldType.ACCESS, AccessPrivilege.ADMIN,  
        AccessPrivilege.ADMIN,  false, false, changedArray, null );
    changed = changed || changedArray[0];
    Field.internal_addField( s, Field.FIELD_NAME_GROUP_ATTR_READERS,  FieldType.ACCESS, AccessPrivilege.ADMIN,  
        AccessPrivilege.ADMIN,  false, false, changedArray, null );
    changed = changed || changedArray[0];
    Field.internal_addField( s, Field.FIELD_NAME_GROUP_ATTR_UPDATERS,  FieldType.ACCESS, AccessPrivilege.ADMIN,  
        AccessPrivilege.ADMIN,  false, false, changedArray, null );
    changed = changed || changedArray[0];

    // reserve attributeDef privs
    Field.internal_addField( s, "attrAdmins",   FieldType.ATTRIBUTE_DEF, AttributeDefPrivilege.ATTR_ADMIN,  
        AttributeDefPrivilege.ATTR_ADMIN,  false, false , changedArray, null);
    changed = changed || changedArray[0];
    Field.internal_addField( s, "attrOptouts",  FieldType.ATTRIBUTE_DEF, AttributeDefPrivilege.ATTR_UPDATE,
        AttributeDefPrivilege.ATTR_UPDATE, false, false , changedArray, null);
    changed = changed || changedArray[0];
    Field.internal_addField( s, "attrOptins",   FieldType.ATTRIBUTE_DEF, AttributeDefPrivilege.ATTR_UPDATE, 
        AttributeDefPrivilege.ATTR_UPDATE, false, false , changedArray, null);
    changed = changed || changedArray[0];
    Field.internal_addField( s, "attrReaders",  FieldType.ATTRIBUTE_DEF, AttributeDefPrivilege.ATTR_ADMIN,  
        AttributeDefPrivilege.ATTR_ADMIN,  false, false , changedArray, null);
    changed = changed || changedArray[0];
    Field.internal_addField( s, "attrUpdaters", FieldType.ATTRIBUTE_DEF, AttributeDefPrivilege.ATTR_ADMIN,  
        AttributeDefPrivilege.ATTR_ADMIN,  false, false, changedArray , null);
    changed = changed || changedArray[0];
    Field.internal_addField( s, "attrViewers",  FieldType.ATTRIBUTE_DEF, AttributeDefPrivilege.ATTR_ADMIN,  
        AttributeDefPrivilege.ATTR_ADMIN,  false, false, changedArray, null );
    changed = changed || changedArray[0];
    Field.internal_addField( s, "attrDefAttrReaders",  FieldType.ATTRIBUTE_DEF, AttributeDefPrivilege.ATTR_ADMIN,  
        AttributeDefPrivilege.ATTR_ADMIN,  false, false, changedArray, null );
    changed = changed || changedArray[0];
    Field.internal_addField( s, "attrDefAttrUpdaters",  FieldType.ATTRIBUTE_DEF, AttributeDefPrivilege.ATTR_ADMIN,  
        AttributeDefPrivilege.ATTR_ADMIN,  false, false, changedArray, null );
    changed = changed || changedArray[0];

    // reserve naming privs
    Field.internal_addField( s, Field.FIELD_NAME_CREATORS, FieldType.NAMING, NamingPrivilege.STEM_ADMIN, 
        NamingPrivilege.STEM_ADMIN, false, false, changedArray, null);
    changed = changed || changedArray[0];
    Field.internal_addField( s, Field.FIELD_NAME_STEM_ADMINS, FieldType.NAMING, 
        NamingPrivilege.STEM_ADMIN, NamingPrivilege.STEM_ADMIN, false, false, changedArray, null);
    changed = changed || changedArray[0];
    Field.internal_addField( s, Field.FIELD_NAME_STEM_ATTR_READERS, FieldType.NAMING, 
        NamingPrivilege.STEM_ADMIN, NamingPrivilege.STEM_ADMIN, false, false, changedArray, null);
    changed = changed || changedArray[0];
    Field.internal_addField( s, Field.FIELD_NAME_STEM_ATTR_UPDATERS, FieldType.NAMING, 
        NamingPrivilege.STEM_ADMIN, NamingPrivilege.STEM_ADMIN, false, false, changedArray, null);
    changed = changed || changedArray[0];
    FieldFinder.clearCache();
    GroupTypeFinder.clearCache();
    return changed;
  } // private static void _installFieldsAndTypes(s)

  /**
   * 
   * @param s
   * @return true if there were changes, false if not
   * @throws GrouperException
   */
  private static boolean _installGroupsAndStems(GrouperSession s) 
    throws  GrouperException
  {
    //note, no need for GrouperSession inverse of control
    boolean[] changed = {false};
    Stem.internal_addRootStem(s, changed);
    return changed[0];
  } // private static void _installGroupsAndStems(s)

}

