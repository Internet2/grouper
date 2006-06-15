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


/** 
 * @author  blair christensen.
 * @version $Id: StemValidator.java,v 1.4 2006-06-15 03:53:01 blair Exp $
 * @since   1.0
 */
class StemValidator {

  // PROTECTED CLASS METHODS //

  // @since 1.0
  protected static void canWriteField(
    GrouperSession s, Stem ns, Subject subj, Field f, FieldType type
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    // FIXME Can I remove s?
    // Validate the field type
    if (!f.getType().equals(type)) {
      throw new SchemaException(E.FIELD_TYPE + f.getType());
    }  
    // FIXME Should this be internalized?
    PrivilegeResolver.getInstance().canPrivDispatch(
      s, ns, subj, f.getWritePriv()
    );
  } // protected static void canWriteField(s, ns, subj, f, type)

}

