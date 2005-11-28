/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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


/** 
 * Install the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: RegistryInstall.java,v 1.6 2005-11-28 17:53:06 blair Exp $    
 */
public class RegistryInstall {

  // Public Class Methods

  public static void main(String[] args) {
    // Install group types, fields and privileges
    _installFieldsAndTypes();
    _installRootStem();
  } // public static void main(args)


  // Private Class Methods
  private static void _installFieldsAndTypes() {
    Set fields  = new HashSet();
    Set types   = new HashSet();
   
    // TODO GroupType base    = new GroupType("base");

    Field description = new Field(
      "description"       , FieldType.ATTRIBUTE,
      AccessPrivilege.READ, AccessPrivilege.ADMIN
    );
    // TODO Remove?
    Field displayName = new Field(
      "displayName"       , FieldType.ATTRIBUTE,
      AccessPrivilege.VIEW, AccessPrivilege.SYSTEM
    );
    Field displayExtn = new Field(
      "displayExtension"  , FieldType.ATTRIBUTE,
      AccessPrivilege.VIEW, AccessPrivilege.ADMIN
    );
    Field extension   = new Field(
      "extension"         , FieldType.ATTRIBUTE,
      AccessPrivilege.VIEW, AccessPrivilege.ADMIN
    );
    // TODO Remove?
    Field name        = new Field(
      "name"              , FieldType.ATTRIBUTE,
      AccessPrivilege.VIEW, AccessPrivilege.SYSTEM
    );

    fields.add(description);
    fields.add(displayName);
    fields.add(displayExtn);
    fields.add(extension);
    fields.add(name);

    Field admins    = new Field(
      "admins"                , FieldType.ACCESS,
      AccessPrivilege.ADMIN   , AccessPrivilege.ADMIN
    );
    // TODO Not needed?  Or maybe just reserve it?
    Field creators  = new Field(
      "creators"              , FieldType.NAMING,
      NamingPrivilege.STEM    , NamingPrivilege.STEM
    );
    Field members   = new Field(
      "members"               , FieldType.LIST,
      AccessPrivilege.READ    , AccessPrivilege.UPDATE
    );
    Field optins    = new Field(
      "optins"                , FieldType.ACCESS,
      AccessPrivilege.UPDATE  , AccessPrivilege.UPDATE
    );
    Field optouts   = new Field(
      "optouts"               , FieldType.ACCESS,
      AccessPrivilege.UPDATE  , AccessPrivilege.UPDATE
    );
    Field readers   = new Field(
      "readers"               , FieldType.ACCESS,
      AccessPrivilege.ADMIN   , AccessPrivilege.ADMIN
    );
    // TODO Not needed?  Or maybe just reserve it?
    Field stemmers  = new Field(
      "stemmers"              , FieldType.NAMING,
      NamingPrivilege.STEM    , NamingPrivilege.STEM
    );
    Field updaters  = new Field(
      "updaters"              , FieldType.ACCESS,
      AccessPrivilege.ADMIN   , AccessPrivilege.ADMIN
    );
    Field viewers   = new Field(
      "viewers"               , FieldType.ACCESS,
      AccessPrivilege.ADMIN   , AccessPrivilege.ADMIN
    );

    fields.add(admins);
    fields.add(creators);
    fields.add(members);
    fields.add(optins);
    fields.add(optouts);
    fields.add(readers);
    fields.add(stemmers);
    fields.add(updaters);
    fields.add(viewers); 

/* TODO
    types.add(base);

    Iterator iter = fields.iterator();
    while (iter.hasNext()) {
      Field f = (Field) iter.next();
      f.getGroupTypes().add(base);
      base.getFields().add(f);
    }
*/

    try {
      Session hs = HibernateHelper.getSession();
      Set objects = new HashSet();
      objects.addAll(fields);
      // TODO objects.addAll(types);
      HibernateHelper.save(objects);
      hs.close();
      System.err.println("fields installed: " + fields.size());
      System.err.println("types installed : " + types.size());
    }
    catch (HibernateException eH) {
      throw new RuntimeException(
        "error installing schema: " + eH.getMessage()
      );
    }
  } // private static void _installFieldsAndTypes()

  private static void _installRootStem() {
    try {
      GrouperSession s = GrouperSession.startSession(
        SubjectFinder.findById(
          "GrouperSystem", "application"
        )
      );
      Stem.addRootStem(s);
      System.err.println("root stem installed");
    }
    catch (Exception e) {
      throw new RuntimeException(
        "unable to install root stem: " + e.getMessage()
      );
    }
  } // private static void _installRootStem()

}

