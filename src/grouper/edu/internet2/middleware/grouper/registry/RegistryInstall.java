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
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperRuntimeException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;

/** 
 * Install the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: RegistryInstall.java,v 1.1 2008-07-21 04:43:58 mchyzer Exp $    
 */
public class RegistryInstall {

  // PUBLIC CLASS METHODS //

  public static void main(String[] args) {
    // Install group types, fields and privileges
    try {
      GrouperSession s = GrouperSession.start( SubjectFinder.findRootSubject(), false );
      
      GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          try {
            _installFieldsAndTypes(grouperSession);
            _installGroupsAndStems(grouperSession);
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
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, throwable);
    }
  }

  /** logger */
  private static final Log LOG = LogFactory.getLog(RegistryInstall.class);


  // PRIVATE CLASS METHODS //
  private static void _installFieldsAndTypes(GrouperSession s) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    //note, no need for GrouperSession inverse of control
    GroupType base    = GroupType.internal_createType(s, "base", false, false);
    // base attributes
    base.internal_addField( s, "description",      FieldType.ATTRIBUTE, AccessPrivilege.READ, AccessPrivilege.ADMIN,  false );
    base.internal_addField( s, "displayExtension", FieldType.ATTRIBUTE, AccessPrivilege.VIEW, AccessPrivilege.ADMIN,  true  );
    base.internal_addField( s, "displayName",      FieldType.ATTRIBUTE, AccessPrivilege.VIEW, AccessPrivilege.SYSTEM, true  );
    base.internal_addField( s, "extension",        FieldType.ATTRIBUTE, AccessPrivilege.VIEW, AccessPrivilege.ADMIN,  true  );
    base.internal_addField( s, "name",             FieldType.ATTRIBUTE, AccessPrivilege.VIEW, AccessPrivilege.SYSTEM, true  );
    // base lists
    base.internal_addField( s, "members", FieldType.LIST, AccessPrivilege.READ, AccessPrivilege.UPDATE, false );
    // reserve access privs
    base.internal_addField( s, "admins",   FieldType.ACCESS, AccessPrivilege.ADMIN,  AccessPrivilege.ADMIN,  false );
    base.internal_addField( s, "optouts",  FieldType.ACCESS, AccessPrivilege.UPDATE, AccessPrivilege.UPDATE, false );
    base.internal_addField( s, "optins",   FieldType.ACCESS, AccessPrivilege.UPDATE, AccessPrivilege.UPDATE, false );
    base.internal_addField( s, "readers",  FieldType.ACCESS, AccessPrivilege.ADMIN,  AccessPrivilege.ADMIN,  false );
    base.internal_addField( s, "updaters", FieldType.ACCESS, AccessPrivilege.ADMIN,  AccessPrivilege.ADMIN,  false );
    base.internal_addField( s, "viewers",  FieldType.ACCESS, AccessPrivilege.ADMIN,  AccessPrivilege.ADMIN,  false );

    GroupType naming  = GroupType.internal_createType(s, "naming", false, true);
    // reserve naming privs
    naming.internal_addField( s, "creators", FieldType.NAMING, NamingPrivilege.STEM, NamingPrivilege.STEM, false);
    naming.internal_addField( s, "stemmers", FieldType.NAMING, NamingPrivilege.STEM, NamingPrivilege.STEM, false);

  } // private static void _installFieldsAndTypes(s)

  private static void _installGroupsAndStems(GrouperSession s) 
    throws  GrouperRuntimeException
  {
    //note, no need for GrouperSession inverse of control
    Stem.internal_addRootStem(s);
  } // private static void _installGroupsAndStems(s)

}

