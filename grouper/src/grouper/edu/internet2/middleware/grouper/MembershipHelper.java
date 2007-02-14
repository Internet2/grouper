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
 * {@link Membership} utility helper class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: MembershipHelper.java,v 1.15 2007-02-14 17:06:28 blair Exp $
 */
class MembershipHelper {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Owner getOwner(Membership ms) 
    throws  IllegalStateException
  {
    // TODO 20070130 what should this do with sessions?
    String uuid = ms.getDTO().getOwnerUuid();
    try {
      Group g = new Group();
      g.setDTO( HibernateGroupDAO.findByUuid(uuid) );
      return g;
    }
    catch (GroupNotFoundException eGNF) {
      try {
        Stem ns = new Stem();
        ns.setDTO( HibernateStemDAO.findByUuid(uuid) );
        return ns;
      }
      catch (StemNotFoundException eNSNF) {
        // ignore
      }
    }
    throw new IllegalStateException("unable to find membership owner " + uuid);
  } // protected static Owner getOwner()

} // class MembershipHelper
 
