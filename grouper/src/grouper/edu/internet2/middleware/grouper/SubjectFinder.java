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
import  org.apache.commons.logging.LogFactory;

/**
 * Find I2MI subjects.
 * <p />
 * @author  blair christensen.
 * @version $Id: SubjectFinder.java,v 1.1.2.7 2005-11-07 16:22:36 blair Exp $
 */
public class SubjectFinder implements Serializable {
  // TODO Add caching?

  // Private Class Variables
  private static Log            log = LogFactory.getLog(SubjectFinder.class);
  private static SourceManager  mgr = null;


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
    SubjectFinder._init();
    List subjects  = SubjectFinder._findById(
      id, mgr.getSources().iterator()
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
    SubjectFinder._init();
    List subjects  = SubjectFinder._findById(
      id, mgr.getSources(SubjectTypeEnum.valueOf(type)).iterator()
    );
    if (subjects.size() == 1) {
      return (Subject) subjects.get(0);
    }
    throw new SubjectNotFoundException("subject not found: " + id);
  }

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
    SubjectFinder._init();
    List subjects  = SubjectFinder._findByIdentifier(
      id, mgr.getSources().iterator()
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
    SubjectFinder._init();
    List subjects  = SubjectFinder._findByIdentifier(
      id, mgr.getSources(SubjectTypeEnum.valueOf(type)).iterator()
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
   * Set subjects = SubjectFinder.find(query);
   * </pre>
   * @param   query     Subject query string.
   * @return  A {@link Set} of {@link Subject} objects.
   */
  public static Set find(String query) {
    throw new RuntimeException("Not implemented");
  }


  // Private class methods
 
  // Find subjects by id 
  private static List _findById(String id, Iterator iter) {
    Subject subj      = null;
    List    subjects  = new ArrayList();
    while (iter.hasNext()) {
      Source sa = (Source) iter.next();
      try {
        subj = sa.getSubject(id);
        log.debug("Found subject in " + sa.getId() + ": " + id);
        subjects.add(subj);
      }
      catch (SubjectNotFoundException e) {
        log.debug("Subject not found in " + sa.getId() + ": " + id);
      }
    }
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
        log.debug("Found subject in " + sa.getId() + ": " + id);
        subjects.add(subj);
      }
      catch (SubjectNotFoundException e) {
        log.debug("Subject not found in " + sa.getId() + ": " + id);
      }
    }
    return subjects;
  } // private static List _findByIdentifier(id, iter) 

  // Initialize the Source Manager
  private static void _init() {
    if (mgr == null) {
      log.debug("Initializing source manager");
      try {
        mgr = SourceManager.getInstance();
        log.debug("Source manager initialized: " + mgr);
        // Add in internal source adapter
        BaseSourceAdapter isa = new InternalSourceAdapter("isa", "internal source adapter"); 
        mgr.loadSource(isa);
        log.debug("Added source: " + isa.getId());
        // Add in group source adapter
        BaseSourceAdapter gsa = new GrouperSourceAdapter(
          "grouperAdapter", "grouper source adapter"
        );
        mgr.loadSource(gsa);
        log.debug("Added source: " + gsa.getId());
        log.info("Subject finder initialized");
      } 
      catch (Exception e) {
        // TODO Is there something more appropriate to do here?
        throw new RuntimeException(e.getMessage()); 
      }
    }
  } // private static void _init()

}

