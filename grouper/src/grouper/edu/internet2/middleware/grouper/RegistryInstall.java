/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
import  java.util.*;
import  net.sf.hibernate.*;

/** 
 * Install the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: RegistryInstall.java,v 1.26 2006-08-30 18:35:38 blair Exp $    
 */
public class RegistryInstall {

  // PUBLIC CLASS METHODS //

  public static void main(String[] args) {
    // Install group types, fields and privileges
    _installFieldsAndTypes();
    _installGroupsAndStems();
  } // public static void main(args)


  // PRIVATE CLASS METHODS //
  // FIXME Refactor
  private static void _installFieldsAndTypes() 
    throws  GrouperRuntimeException
  {
    Set base_f    = new LinkedHashSet();
    Set fields    = new LinkedHashSet();
    Set naming_f  = new LinkedHashSet();
    Set types     = new LinkedHashSet();
   
    // Base Attributes
    base_f.add(
      new Field(
        "description"       , FieldType.ATTRIBUTE,
        AccessPrivilege.READ, AccessPrivilege.ADMIN,
        true
      )
    );
    base_f.add(
      new Field(
        "displayName"       , FieldType.ATTRIBUTE,
        AccessPrivilege.VIEW, AccessPrivilege.SYSTEM,
        false
      )
    );
    base_f.add(
      new Field(
        "displayExtension"  , FieldType.ATTRIBUTE,
        AccessPrivilege.VIEW, AccessPrivilege.ADMIN,
        false
      )
    );
    base_f.add(
      new Field(
        "extension"         , FieldType.ATTRIBUTE,
        AccessPrivilege.VIEW, AccessPrivilege.ADMIN,
        false
      )
    );
    base_f.add(
      new Field(
        "name"              , FieldType.ATTRIBUTE,
        AccessPrivilege.VIEW, AccessPrivilege.SYSTEM,
        false
      )
    );
    // Base Access Privileges
    base_f.add(
      new Field(
        "admins"                , FieldType.ACCESS,
        AccessPrivilege.ADMIN   , AccessPrivilege.ADMIN,
        true
      )
    );
    base_f.add(
      new Field(
        GrouperConfig.LIST      , FieldType.LIST,
        AccessPrivilege.READ    , AccessPrivilege.UPDATE,
        true
      )
    );
    base_f.add(
      new Field(
        "optins"                , FieldType.ACCESS,
        AccessPrivilege.UPDATE  , AccessPrivilege.UPDATE,
        true
      )
    );
    base_f.add(
      new Field(
        "optouts"               , FieldType.ACCESS,
        AccessPrivilege.UPDATE  , AccessPrivilege.UPDATE,
        true
      )
    );
    base_f.add(
      new Field(
        "readers"               , FieldType.ACCESS,
        AccessPrivilege.ADMIN   , AccessPrivilege.ADMIN,
        true
      )
    );
    base_f.add(
      new Field(
        "updaters"              , FieldType.ACCESS,
        AccessPrivilege.ADMIN   , AccessPrivilege.ADMIN,
        true
      )
    );
    base_f.add(
      new Field(
        "viewers"               , FieldType.ACCESS,
        AccessPrivilege.ADMIN   , AccessPrivilege.ADMIN,
        true
      )
    );
    // Naming Privileges
    naming_f.add(
      new Field(
        "creators"              , FieldType.NAMING,
        NamingPrivilege.STEM    , NamingPrivilege.STEM,
        true
      )
    );
    naming_f.add(
      new Field(
        "stemmers"              , FieldType.NAMING,
        NamingPrivilege.STEM    , NamingPrivilege.STEM,
        true
      )
    );

    GroupType base    = new GroupType("base", base_f, false, false);
    types.add(base);
    GroupType naming  = new GroupType("naming", naming_f, false, true);
    types.add(naming);
  
    fields.addAll(base_f);
    fields.addAll(naming_f);

    try {
      Session   hs        = HibernateHelper.getSession();
      Set       objects   = new LinkedHashSet();
      Settings  settings  = new Settings(Settings.getCurrentSchemaVersion());
      objects.addAll(types);
      objects.add(settings);
      HibernateHelper.save(objects);
      hs.close();
      EventLog.info("set schema version   : " + settings.getSchemaVersion()  );
      EventLog.info("group types installed: " + types.size()                 );
      EventLog.info("fields installed     : " + fields.size()                );
    }
    catch (HibernateException eH) {
      String msg = E.RI_IS + eH.getMessage();
      ErrorLog.fatal(RegistryInstall.class, msg);
      throw new GrouperRuntimeException(msg, eH);
    }
  } // private static void _installFieldsAndTypes()

  private static void _installGroupsAndStems() 
    throws  GrouperRuntimeException
  {
    try {
      GrouperSession s = GrouperSession.start(
        SubjectFinder.findById(
          GrouperConfig.ROOT, GrouperConfig.IST, InternalSourceAdapter.ID
        )
      );
      Stem.addRootStem(s);
      EventLog.info(s, M.STEM_ROOTINSTALL);
    }
    catch (Exception e) { 
      String msg = E.RI_ISG + e.getMessage();
      ErrorLog.fatal(RegistryInstall.class, msg);
      throw new GrouperRuntimeException(msg, e);
    }
  } // private static void _installGroupsAndStems()

}

