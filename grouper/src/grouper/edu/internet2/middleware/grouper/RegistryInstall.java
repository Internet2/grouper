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


import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.logging.*;


/** 
 * Install the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: RegistryInstall.java,v 1.19 2006-03-28 20:18:55 blair Exp $    
 */
public class RegistryInstall {

  // Private Class Constants
  private static final String ERR_IS  = "unable to install schema: ";
  private static final String ERR_ISG = "unable to install base stems and groups: ";
  private static final Log    LOG     = LogFactory.getLog(RegistryInstall.class);


  // Public Class Methods

  public static void main(String[] args) {
    // Install group types, fields and privileges
    _installFieldsAndTypes();
    _installGroupsAndStems();
  } // public static void main(args)


  // Private Class Methods
  private static void _installFieldsAndTypes() {
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

    // TODO Why don't I use createType()?
    GroupType base    = new GroupType("base", base_f);
    base.setAssignable(false);
    base.setInternal(false);
    types.add(base);

    GroupType naming  = new GroupType("naming", naming_f);
    naming.setAssignable(false);
    naming.setInternal(true);
    types.add(naming);

    GroupType hasFactor = new GroupType("hasFactor" , new HashSet());
    hasFactor.setAssignable(false);
    hasFactor.setInternal(true);
    types.add(hasFactor);
    
    GroupType isFactor  = new GroupType("isFactor"  , new HashSet());
    isFactor.setAssignable(false);
    isFactor.setInternal(true);
    types.add(isFactor);
  
    fields.addAll(base_f);
    fields.addAll(naming_f);

    try {
      Session hs = HibernateHelper.getSession();
      Set objects = new LinkedHashSet();
      objects.addAll(types);
      HibernateHelper.save(objects);
      hs.close();
      LOG.info("group types installed: " + types.size());
      LOG.info("fields installed     : " + fields.size());
    }
    catch (HibernateException eH) {
      String err = ERR_IS + eH.getMessage();
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
  } // private static void _installFieldsAndTypes()

  private static void _installGroupsAndStems() {
    try {
      GrouperSession s = GrouperSession.start(
        SubjectFinder.findById(GrouperConfig.ROOT, GrouperConfig.IST)
      );
      Stem  root    = Stem.addRootStem(s);
      LOG.info("root stem installed");
    }
    catch (Exception e) { 
      String err = ERR_ISG + e.getMessage();
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
  } // private static void _installGroupsAndStems()

}

