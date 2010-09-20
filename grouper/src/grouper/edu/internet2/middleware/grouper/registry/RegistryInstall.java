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

import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cache.GrouperCacheUtils;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
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
      LOG.fatal(msg, throwable);
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
    GroupType base    = GroupType.internal_createType(s, "base", false, false, false, changedArray, null);
    changed = changed || changedArray[0];
    // base lists
    base.internal_addField( s, "members", FieldType.LIST, AccessPrivilege.READ, 
        AccessPrivilege.UPDATE, false, false, false , changedArray, null);
    changed = changed || changedArray[0];
    // reserve access privs
    base.internal_addField( s, "admins",   FieldType.ACCESS, AccessPrivilege.ADMIN,  
        AccessPrivilege.ADMIN,  false, false, false , changedArray, null);
    changed = changed || changedArray[0];
    base.internal_addField( s, "optouts",  FieldType.ACCESS, AccessPrivilege.UPDATE,
        AccessPrivilege.UPDATE, false, false, false , changedArray, null);
    changed = changed || changedArray[0];
    base.internal_addField( s, "optins",   FieldType.ACCESS, AccessPrivilege.UPDATE, 
        AccessPrivilege.UPDATE, false, false, false , changedArray, null);
    changed = changed || changedArray[0];
    base.internal_addField( s, "readers",  FieldType.ACCESS, AccessPrivilege.ADMIN,  
        AccessPrivilege.ADMIN,  false, false, false , changedArray, null);
    changed = changed || changedArray[0];
    base.internal_addField( s, "updaters", FieldType.ACCESS, AccessPrivilege.ADMIN,  
        AccessPrivilege.ADMIN,  false, false, false, changedArray , null);
    changed = changed || changedArray[0];
    base.internal_addField( s, "viewers",  FieldType.ACCESS, AccessPrivilege.ADMIN,  
        AccessPrivilege.ADMIN,  false, false, false, changedArray, null );
    changed = changed || changedArray[0];

    // reserve attributeDef privs
    GroupType attributeDefType  = GroupType.internal_createType(s, "attributeDef", false, true, false, changedArray, null);
    attributeDefType.internal_addField( s, "attrAdmins",   FieldType.ATTRIBUTE_DEF, AttributeDefPrivilege.ATTR_ADMIN,  
        AttributeDefPrivilege.ATTR_ADMIN,  false, false, false , changedArray, null);
    changed = changed || changedArray[0];
    attributeDefType.internal_addField( s, "attrOptouts",  FieldType.ATTRIBUTE_DEF, AttributeDefPrivilege.ATTR_UPDATE,
        AttributeDefPrivilege.ATTR_UPDATE, false, false, false , changedArray, null);
    changed = changed || changedArray[0];
    attributeDefType.internal_addField( s, "attrOptins",   FieldType.ATTRIBUTE_DEF, AttributeDefPrivilege.ATTR_UPDATE, 
        AttributeDefPrivilege.ATTR_UPDATE, false, false, false , changedArray, null);
    changed = changed || changedArray[0];
    attributeDefType.internal_addField( s, "attrReaders",  FieldType.ATTRIBUTE_DEF, AttributeDefPrivilege.ATTR_ADMIN,  
        AttributeDefPrivilege.ATTR_ADMIN,  false, false, false , changedArray, null);
    changed = changed || changedArray[0];
    attributeDefType.internal_addField( s, "attrUpdaters", FieldType.ATTRIBUTE_DEF, AttributeDefPrivilege.ATTR_ADMIN,  
        AttributeDefPrivilege.ATTR_ADMIN,  false, false, false, changedArray , null);
    changed = changed || changedArray[0];
    attributeDefType.internal_addField( s, "attrViewers",  FieldType.ATTRIBUTE_DEF, AttributeDefPrivilege.ATTR_ADMIN,  
        AttributeDefPrivilege.ATTR_ADMIN,  false, false, false, changedArray, null );
    changed = changed || changedArray[0];

    GroupType naming  = GroupType.internal_createType(s, "naming", false, true, false, changedArray, null);
    changed = changed || changedArray[0];
    // reserve naming privs
    naming.internal_addField( s, "creators", FieldType.NAMING, NamingPrivilege.STEM, 
        NamingPrivilege.STEM, false, false, false, changedArray, null);
    changed = changed || changedArray[0];
    naming.internal_addField( s, "stemmers", FieldType.NAMING, 
        NamingPrivilege.STEM, NamingPrivilege.STEM, false, false, false, changedArray, null);
    changed = changed || changedArray[0];
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

