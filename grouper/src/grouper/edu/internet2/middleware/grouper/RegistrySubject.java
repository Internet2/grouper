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
import  edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import  edu.internet2.middleware.grouper.internal.dto.RegistrySubjectDTO;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.SubjectTypeEnum;
import  java.util.HashMap;
import  java.util.LinkedHashSet;
import  java.util.Map;
import  java.util.Set;

/** 
 * A {@link Subject} local to the Groups Registry.
 * <p/>
 * <p><b>NOTE: THIS CLASS IS NOT CONSIDERED STABLE AND MAY CHANGE IN FUTURE RELEASES.</b></p>
 * @author  blair christensen.
 * @version $Id: RegistrySubject.java,v 1.9 2008-06-21 04:16:12 mchyzer Exp $
 * @since   1.2.0
 */
public class RegistrySubject extends GrouperAPI implements Subject {

  // PUBLIC CLASS METHODS //

  /**
   * Add a {@link Subject} to a {@link Source} within the Groups Registry.
   * <p>Subjects may only be added within a root-like session.</p>
   * <pre class="eg">
   * try {
   *   RegistrySubject subj = RegistrySubject.add(s, "subject id", "person", "name");
   * }
   * catch (GrouperException eG) {
   *   // unable to add subject
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // not privileged to add subject
   * }
   * </pre>
   * @param   s     Create subject within this session context.
   * @param   id    The subject id to assign to the subject.
   * @param   type  The subject type to assign to the subject.
   * @param   name  The name to assign to the subject.
   * @return  The created {@link RegistrySubject}.
   * @throws  GrouperException
   * @throws  InsufficientPrivilegeException
   * @since   1.2.0
   */
  public static RegistrySubject add(GrouperSession s, String id, String type, String name)
      throws  GrouperException,
              InsufficientPrivilegeException
  {
    if ( !PrivilegeHelper.isRoot(s) ) {
      throw new InsufficientPrivilegeException(E.ROOTLIKE_TO_ADD_HSUBJ);
    }    
    try {
      GrouperDAOFactory.getFactory().getRegistrySubject().find(id, type);
      throw new GrouperException(E.SUBJ_ALREADY_EXISTS + id + "/" + type + "/" + name);
    }
    catch (SubjectNotFoundException eSNF) {
      RegistrySubject     subj  = new RegistrySubject();
      RegistrySubjectDTO  _subj = new RegistrySubjectDTO()
        .setId(id)
        .setName(name)
        .setType(type);
      GrouperDAOFactory.getFactory().getRegistrySubject().create(_subj);
      subj.setDTO( _subj);
      return subj;
    }
  } // public static RegistrySubject add(s, id, type, name)



  // PUBLIC INSTANCE METHODS //

  /**
   * Delete existing {@link RegistrySubject}.
   * <pre>
   * try {
   *   rSubj.delete(s);
   * }
   * catch (GrouperException eG) {
   *   // failed to delete this RegistrySubject
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // not privileged to delete this RegistrySubject
   * }
   * </pre>
   * @param   s   Delete <i>RegistrySubject</i> within this <i>GrouperSession</i> context.
   * @throws  GrouperException  if <i>RegistrySubject</i> cannot be deleted.
   * @throws  IllegalStateException if <i>GrouperSession</i> is null.
   * @throws  InsufficientPrivilegeException if not privileged to delete <i>RegistrySubject</i>s.
   * @since   1.2.0
   */
  public void delete(GrouperSession s) 
    throws  GrouperException,
            IllegalStateException,
            InsufficientPrivilegeException
  {
    if (s == null) {
      throw new IllegalStateException("null session");
    }
    if ( !PrivilegeHelper.isRoot(s) ) {
      throw new InsufficientPrivilegeException("must be root-like to delete RegistrySubjects");
    }    
    try {
      GrouperDAOFactory.getFactory().getRegistrySubject().delete( this._getDTO() );
    }
    catch (GrouperDAOException eDAO) {
      throw new GrouperException( eDAO.getMessage(), eDAO );
    }
  }
  /**
   * Return the subject's attribute.
   *  <p/>
   * <p><b>NOTE:</b> This is not currently implemented and will always return an empty map.</p>
   * @since   1.2.0
   */
  public Map getAttributes() {
    return new HashMap();
  } // public Map getAttributes()

  /**
   * Return the value of the specified attribute.
   * <p/>
   * <p><b>NOTE:</b> This is not currently implemented and will always return an empty string.</p>
   * @since   1.2.0
   */
  public String getAttributeValue(String name) {
    return GrouperConfig.EMPTY_STRING;
  } // public String getAttributevalue(name)

  /**
   * Return the values for the specified attribute.
   * <p/>
   * <p><b>NOTE:</b> This is not currently implemented and will always return an empty set.</p>
   * @since   1.2.0
   */
  public Set getAttributeValues(String name) {
    return new LinkedHashSet();
  } // public Set getAttributeValues(name)

  /**
   * Return this subject's description.
   * <p/>
   * <p><b>NOTE:</b> This is not currently implemented and will always return an empty string.</p>
   * @since   1.2.0
   */
  public String getDescription() {
    return GrouperConfig.EMPTY_STRING;
  } // public String getDescription()

  /**
   * Return the subject id.
   * <p/>
   * @since   1.2.0
   */
  public String getId() {
    return this._getDTO().getId();
  } // public String getId()

  /**
   * Return the subject's name.
   * <p/>
   * @since   1.2.0
   */
  public String getName() {
    return this._getDTO().getName();
  } // public String getName()

  /**
   * Return the source.
   * <p/>
   * <p><b>NOTE:</b> The current implementation is very crude and inefficient.  It
   * attempts to query for the subject to identify the source.</p>
   * @throws  IllegalStateException if source cannot be returned.
   * @since   1.2.0
   */
  public Source getSource() 
    throws  IllegalStateException
  {
    try {
      return SubjectFinder.findById( this._getDTO().getId(), this._getDTO().getType()).getSource();
    }
    catch (SubjectNotFoundException eSNF)   {
      throw new IllegalStateException( eSNF.getMessage(), eSNF );
    }
    catch (SubjectNotUniqueException eSNU)  {
      throw new IllegalStateException( eSNU.getMessage(), eSNU );
    }
  } // public Source getSource()

  /**
   * Return this subject's {@link SubjectType}.
   * <p/>
   * @since   1.2.0
   */
  public SubjectType getType() {
    return SubjectTypeEnum.valueOf( this._getDTO().getType() );
  } // public SubjectType getType()


  // PRIVATE INSTANCE METHODS //

  // @since   1.2.0
  private RegistrySubjectDTO _getDTO() {
    return (RegistrySubjectDTO) super.getDTO();
  } 
  
}

