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
import  java.io.Serializable;
import  java.util.*;
import  org.apache.commons.logging.*;


/**
 * Find I2MI subjects.
 * <p />
 * @author  blair christensen.
 * @version $Id: SubjectFinder.java,v 1.11 2005-12-12 04:54:09 blair Exp $
 */
public class SubjectFinder implements Serializable {

  // Private Class Constants
  private static final String         ERR_IAS   = "unable to initialize ALL subject: ";
  private static final String         ERR_INIT  = "failed to initialize source manager: ";
  private static final Log            LOG       = LogFactory.getLog(SubjectFinder.class);
  private static final SourceManager  MGR;
  private static final Subject        ALL; 


  static {
    LOG.debug("Initializing source manager");
    try {
      MGR = SourceManager.getInstance();
      LOG.debug("Source manager initialized: " + MGR);
      // Add in internal source adapter
      BaseSourceAdapter isa = new InternalSourceAdapter(
        InternalSourceAdapter.ID, InternalSourceAdapter.NAME
      ); 
      MGR.loadSource(isa);
      LOG.debug("Added source: " + isa.getId());
      // Add in group source adapter
      BaseSourceAdapter gsa = new GrouperSourceAdapter(
        GrouperSourceAdapter.ID, GrouperSourceAdapter.NAME
      );
      MGR.loadSource(gsa);
      LOG.debug("Added source: " + gsa.getId());
      LOG.info("Subject finder initialized");
      try {
        ALL = SubjectFinder.findById(GrouperConfig.ALL, GrouperConfig.IST);
        LOG.info("ALL subject initialized");
      }
      catch (SubjectNotFoundException eSNF) {
        String err = ERR_IAS + eSNF.getMessage();
        LOG.fatal(err);
        throw new RuntimeException(err);
      }
    } 
    catch (Exception e) {
      String err = ERR_INIT + e.getMessage();
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
  } // static


  // Public Class Methods

  /**
   * Get a subject by id.
   * <pre class="eg">
   * // Find the subject - within all sources - with the specified id.
   * try {
   *   Subject subj = SubjectFinder.findById(subjectID);
   * }
   * catch (SubjectNotFoundException e) {
   *   // Subject not found
   * }
   *  </pre>
   * @param   id      Subject ID
   * @return  A {@link Subject} object
   * @throws SubjectNotFoundException
   */
  public static Subject findById(String id) 
    throws SubjectNotFoundException
  {
    List subjects  = SubjectFinder._findById(
      id, MGR.getSources().iterator()
    );
    if (subjects.size() == 1) {
      return (Subject) subjects.get(0);
    }
    throw new SubjectNotFoundException("subject not found: " + id);
  }

  /**
   * Get a subject by id and the specified type.
   * <pre class="eg">
   * // Find the subject - within all sources - with the specified id
   * // and type.
   * try {
   *   Subject subj = SubjectFinder.findById(subjectID, type);
   * }
   * catch (SubjectNotFoundException e) {
   *   // Subject not found
   * }
   *  </pre>
   * @param   id      Subject ID
   * @param   type    Subject type.
   * @return  A {@link Subject} object
   * @throws SubjectNotFoundException
   */
  public static Subject findById(String id, String type) 
    throws SubjectNotFoundException
  {
    String msg = "findById '" + id + "'/'" + type + "'";
    LOG.debug(msg);
    List subjects  = SubjectFinder._findById(
      id, MGR.getSources(SubjectTypeEnum.valueOf(type)).iterator()
    );
    LOG.debug(msg + " found: " + subjects.size());
    if (subjects.size() == 1) {
      return (Subject) subjects.get(0);
    }
    String err = msg + " subject not found";
    LOG.debug(err);
    throw new SubjectNotFoundException(err);
  } // public static Subject findById(id, type)

  /**
   * Get a subject by a well-known identifier.
   * <pre class="eg">
   * // Find the subject - within all sources - with the well-known
   * // identifier.
   * try {
   *   Subject subj = SubjectFinder.findByIdentifier(identifier);
   * }
   * catch (SubjectNotFoundException e) {
   *   // Subject not found
   * }
   *  </pre>
   * @param   id      Subject identifier.
   * @return  A {@link Subject} object
   * @throws SubjectNotFoundException
   */
  public static Subject findByIdentifier(String id) 
    throws SubjectNotFoundException
  {
    List subjects  = SubjectFinder._findByIdentifier(
      id, MGR.getSources().iterator()
    );
    if (subjects.size() == 1) {
      return (Subject) subjects.get(0);
    }
    throw new SubjectNotFoundException("subject not found: " + id);
  }

  /**
   * Get a subject by a well-known identifier and the specified type.
   * <pre class="eg">
   * // Find the subject - within all sources - with the specified id
   * // and type.
   * try {
   *   Subject subj = SubjectFinder.findByIdentifier(identifier, type);
   * }
   * catch (SubjectNotFoundException e) {
   *   // Subject not found
   * }
   *  </pre>
   * @param   id      Subject identifier.
   * @param   type    Subject type.
   * @return  A {@link Subject} object
   * @throws SubjectNotFoundException
   */
  public static Subject findByIdentifier(String id, String type) 
    throws SubjectNotFoundException
  {
    List subjects  = SubjectFinder._findByIdentifier(
      id, MGR.getSources(SubjectTypeEnum.valueOf(type)).iterator()
    );
    if (subjects.size() == 1) {
      return (Subject) subjects.get(0);
    }
    throw new SubjectNotFoundException("subject not found: " + id);
  }

  /**
   * Find all subjects matching the query.
   * <p>
   * The query string specification is currently unique to each subject
   * source adapter.  Queries may not work or may lead to erratic
   * results across different source adapters.  Consult the
   * documentation for each source adapter for more information on the
   * query language supported by each adapter.
   * </p>
   * <pre class="eg">
   * // Find all subjects matching the given query string.
   * Set subjects = SubjectFinder.findAll(query);
   * </pre>
   * @param   query     Subject query string.
   * @return  A {@link Set} of {@link Subject} objects.
   */
  public static Set findAll(String query) {
    Set       subjects  = new LinkedHashSet();
    Iterator  iter      = MGR.getSources().iterator();
    while (iter.hasNext()) {
      Source sa = (Source) iter.next();
      Set found = sa.search(query);
      LOG.debug("Found subjects in " + sa.getId() + ": " + found.size());
      subjects.addAll(found);
    }
    return subjects;
  } // public static Set findAll(query)

  /**
   * Get <i>GrouperAll</i> subject.
   * <pre class="eg">
   * Subject all = SubjectFinder.findAllSubject();
   *  </pre>
   * @return  The <i>GrouperAll</i> {@link Subject} 
   */
  public static Subject findAllSubject() {
    return ALL;
  } // public static Subject findAllSubject()


  // Private class methods
 
  // Find subjects by id 
  private static List _findById(String id, Iterator iter) {
    String msg = "_findById '" + id + "'";
    LOG.debug(msg);
    Subject subj      = null;
    List    subjects  = new ArrayList();
    while (iter.hasNext()) {
      Source  sa    = (Source) iter.next();
      String  _msg  = msg + " searching '" + sa.getId() + "'";
      try {
        subj = sa.getSubject(id);
        LOG.debug(_msg + " found: " + subj);
        subjects.add(subj);
      }
      catch (SubjectNotFoundException e) {
        LOG.debug(_msg + " not found");
      }
    }
    LOG.debug(msg + " found: " + subjects.size());
    return subjects;
  } // private static List _findById(id, iter) 

  // Find subjects by identifier
  private static List _findByIdentifier(String id, Iterator iter) {
    Subject subj      = null;
    List    subjects  = new ArrayList();
    while (iter.hasNext()) {
      Source sa = (Source) iter.next();
      try {
        subj = sa.getSubjectByIdentifier(id);
        LOG.debug("Found subject in " + sa.getId() + ": " + id);
        subjects.add(subj);
      }
      catch (SubjectNotFoundException e) {
        LOG.debug("Subject not found in " + sa.getId() + ": " + id);
      }
    }
    return subjects;
  } // private static List _findByIdentifier(id, iter) 

}

