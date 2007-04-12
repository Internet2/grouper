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
 * {@link Composite} utility code.
 * <p/>
 * @since   1.2.0
 */
class CompositeHelper {

  // PROTECTED CLASS METHODS //

  // FIXME 20070412 why aren't these in "Composite"?
  
  // @since   1.2.0
  protected static String getLeftName(Composite c) {
    return _getName( c, ( (CompositeDTO) c.getDTO() ).getLeftFactorUuid(), E.COMP_NULL_LEFT_GROUP );
  } 

  // @since   1.2.0
  protected static String getOwnerName(Composite c) {
    return _getName( c, ( (CompositeDTO) c.getDTO() ).getFactorOwnerUuid(), E.COMP_NULL_OWNER_GROUP );
  }

  // @since   1.2.0
  protected static String getRightName(Composite c) {
    return _getName( c, ( (CompositeDTO) c.getDTO() ).getRightFactorUuid(), E.COMP_NULL_RIGHT_GROUP );
  }


  // PRIVATE CLASS METHODS //

  // @since   1.2.0
  private static String _getName(Composite c, String uuid, String msg) {
    try {
      Group g = new Group();
      g.setDTO( GrouperDAOFactory.getFactory().getGroup().findByUuid(uuid) );
      return g.getName();
    }
    catch (GroupNotFoundException eGNF) {
      ErrorLog.error( CompositeHelper.class, msg + U.internal_q( c.getUuid() ) + ": " + eGNF.getMessage() );
      return GrouperConfig.EMPTY_STRING;
    }
  } 

} // class CompositeHelper

