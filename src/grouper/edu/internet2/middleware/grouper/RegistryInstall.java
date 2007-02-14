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

package edu.internet2.middleware.grouper;

/** 
 * Install the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: RegistryInstall.java,v 1.34 2007-02-14 17:06:28 blair Exp $    
 */
public class RegistryInstall {

  // PUBLIC CLASS METHODS //

  public static void main(String[] args) {
    // Install group types, fields and privileges
    try {
      GrouperSession s = GrouperSession.start( SubjectFinder.findRootSubject() );
      _installFieldsAndTypes(s);
      _installGroupsAndStems(s);
      s.stop();
    }
    catch (Exception e) {
      String msg = "unable to initialize registry: " + e.getMessage();
      ErrorLog.fatal(RegistryInstall.class, msg);
      throw new GrouperRuntimeException(msg, e);
    }
  } // public static void main(args)


  // PRIVATE CLASS METHODS //
  private static void _installFieldsAndTypes(GrouperSession s) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
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

    // TODO 20070207 what should i do with `Settings`?
    SettingsDTO settings = new SettingsDTO();
    settings.setSchemaVersion( Settings.internal_getCurrentSchemaVersion() );
    HibernateSettingsDAO.create(settings);
  } // private static void _installFieldsAndTypes(s)

  private static void _installGroupsAndStems(GrouperSession s) 
    throws  GrouperRuntimeException
  {
    Stem.internal_addRootStem(s);
  } // private static void _installGroupsAndStems(s)

}

