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
import  java.io.Serializable;
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.*;

/*
 * Find {@link HibernateSubject}s.
 * @author  blair christensen.
 * @version $Id: HibernateSubjectFinder.java,v 1.2 2006-06-15 00:07:02 blair Exp $
 * @since   1.0
 */
class HibernateSubjectFinder implements Serializable {

  // PROTECTED CLASS METHODS //

  // @since 1.0
  protected static HibernateSubject find(String id, String type)
    throws  HibernateException,
            SubjectNotFoundException
  {
    Session hs  = HibernateHelper.getSession();
    Query   qry = hs.createQuery(
      "from HibernateSubject as hs where  " 
      + "     hs.subjectId      = :id     "
      + " and hs.subjectTypeId  = :type   "
    );
    qry.setCacheable(false);  // TODO
    qry.setString(  "id"    , id    );
    qry.setString(  "type"  , type  );
    List    l   = qry.list();
    if (l.size() == 1) {
      HibernateSubject subj = (HibernateSubject) l.get(0);
      return subj; 
    } 
    hs.close();
    throw new SubjectNotFoundException("could not find subject: " + id + "/" + type);
  } // protected static HibernateSubject find(id, type)

}

