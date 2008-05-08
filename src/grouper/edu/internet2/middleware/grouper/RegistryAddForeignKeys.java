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

import  java.io.FileWriter;
import  java.io.IOException;

import  edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import  edu.internet2.middleware.grouper.internal.dao.RegistryDAO;

/** 
 * Adds foreign keys to the Groups Registry.
 * <p/>
 * @author  Shilen Patel.
 * @since   1.3.0
 */
public class RegistryAddForeignKeys {

  // PUBLIC CLASS METHODS //

  /**
   * @since   1.3.0
   */
  public static void main(String[] args) {

    FileWriter fw = null;

    try {
      fw = new FileWriter(GrouperConfig.getBuildProperty("schemaexport.out"), true);
      ( (RegistryDAO) GrouperDAOFactory.getFactory().getRegistry() ).addForeignKeys(fw);
    }
    catch (IOException e) {
      throw new RuntimeException( e.getMessage() );
    }
    catch (GrouperDAOException eDAO) {
      throw new RuntimeException( eDAO.getMessage() );
    }
    finally {
      if (fw != null) {
        try {
          fw.close();
        }
        catch (IOException e) { }
      }
    }
  } 

}

