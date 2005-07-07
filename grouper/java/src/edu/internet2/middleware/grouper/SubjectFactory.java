/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Chicago
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Chicago nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Chicago, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Chicago, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.internet2.middleware.grouper;


import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  org.apache.commons.logging.Log;
import  org.apache.commons.logging.LogFactory;


/** 
 * Class for performing I2MI {@link Subject} lookups.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: SubjectFactory.java,v 1.11 2005-07-07 03:14:03 blair Exp $
 */
public class SubjectFactory {

  /*
   * PRIVATE CLASS VARIABLES
   */
  private static List           sources = new ArrayList();
  private static Log            log     = LogFactory.getLog(SubjectFactory.class);
  private static Map            types   = null;  
  private static SourceManager  mgr     = null;
  private static SubjectCache   cache   = null;


  /*
   * PUBLIC CLASS METHODS 
   */

  /**
   * Gets a subject by its id and using hte default subject type.
   * <p />
   * @param   id      Subject ID
   * @return  A {@link SubjectFactory} object
   * @throws SubjectNotFoundException
   */
  public static Subject getSubject(String id) 
    throws SubjectNotFoundException 
  {
    return SubjectFactory.getSubject(id, Grouper.DEF_SUBJ_TYPE);
  }

  /**
   * Gets a subject by its ID.
   * <p />
   * @param   id      subject identifier
   * @param   type    subject type
   * @return  a {@link Subject}
   * @throws SubjectNotFoundException
   */
  public static Subject getSubject(String id, String type) 
    throws SubjectNotFoundException 
  {
    SubjectFactory.init();
    boolean cached  = false;
    Subject subj    = null;
    log.debug("Getting subject " + id + "/" + type);
    try {
      subj = cache.get(id, type);
      cached = true;
      log.debug("Found cached subject " + id + "/" + type);
    } catch (SubjectNotFoundException e0) {
      if (types.containsKey(type)) {
        Iterator iter = mgr.getSources(SubjectTypeEnum.valueOf(type)).iterator();
        while (iter.hasNext()) {
          Source sa = (Source) iter.next();
          // FIXME Actually, I should probably gather a list.  If 
          //       one entry found, return it.  Otherwise, throw an
          //       exception.
          try {
            subj = sa.getSubject(id);
            log.debug("Found subject " + id + "/" + type + " in " + sa.getName());
            break;
          } catch (SubjectNotFoundException e1) {
            log.debug("Did not find subject " + id + "/" + type + " in " + sa.getName());
            /*
             * Don't worry about not finding subjects in 
             * particular adapters.
             */
            continue;
          }
        }
      } else {
        log.debug("No adapters for type " + type);
      }
      if (subj != null) {
        if (cached == false) {
          cache.put(id, type, subj);
          log.debug("Caching subject " + id + "/" + type);
        }
      } else {
        // TODO Do I want a negative cache?
        log.debug("Unable to find subject " + id + "/" + type);
        throw new SubjectNotFoundException(
          "Could not get " + id + "/" + type
        );
      }
    }
    return subj;
  }
 
  /**
   * Gets a Subject by other well-known identifiers, aside from the
   * subject ID, e.g. login ID.
   * <p />
   * @param   id  identifier
   * @return  a {@link Subject}
   * @throws  SubjectNotFoundException
   */
  public static Subject getSubjectByIdentifier(String id) 
    throws SubjectNotFoundException
  {
    return SubjectFactory.getSubjectByIdentifier(id, Grouper.DEF_SUBJ_TYPE);
  }

  /**
   * Gets a Subject by other well-known identifiers, aside from the
   * subject ID, e.g. login ID.
   * <p />
   * @param   id    identifier
   * @param   type  subject type
   * @return  a {@link Subject}
   * @throws  SubjectNotFoundException
   */
  public static Subject getSubjectByIdentifier(String id, String type) 
    throws SubjectNotFoundException
  {
    // TODO Cache
    // TODO A lot of duplication with _getSubject()_
    SubjectFactory.init();
    Subject subj = null;
    if (types.containsKey(type)) {
      Iterator iter = mgr.getSources(SubjectTypeEnum.valueOf(type)).iterator();
      while (iter.hasNext()) {
        Source sa = (Source) iter.next();
        // FIXME Actually, I should probably gather a list.  If 
        //       one entry found, return it.  Otherwise, throw an
        //       exception.
        try {
          subj = sa.getSubjectByIdentifier(id);
          log.debug("Found subject " + id + "/" + type + " in " + sa.getName());
          break;
        } catch (SubjectNotFoundException e1) {
          log.debug("Did not find subject " + id + "/" + type + " in " + sa.getName());
          /*
           * Don't worry about not finding subjects in 
           * particular adapters.
           */
          continue;
        }
      }
    }
    if (subj == null) {
      log.debug("Unable to find subject " + id + "/" + type);
      throw new SubjectNotFoundException(
        "Could not get " + id + "/" + type
      );
    }
    return subj;
  }

  /**
   * @return true if subject type is known.
   */
  public static boolean hasType(String type) {
    SubjectFactory.init();
    if (types.containsKey(type)) {
      return true;
    }
    return false;
  }

  /**
   * Unstructured search for Subjects.
   * <p />
   * @param searchValue Source adapter specific query string.
   * @return  Set of found subjects.
   */
  // TODO cache?
  public static Set search(String searchValue) {
    SubjectFactory.init();
    Set vals = new HashSet();
    Iterator iter = sources.iterator();
    while (iter.hasNext()) {
      Source sa = (Source) iter.next();
      Set s = sa.search(searchValue);
      if (s != null) {  // TODO This _should not_ be necessary
        vals.addAll(s); // TODO Does search() have the same flaw?
      }
    }
    return vals;
  }

  /**
   * @return known subject types.
   */
  public static Set types() {
    SubjectFactory.init();
    return new HashSet( types.values() );
  }
 
 
  /*
   * PRIVATE CLASS METHODS
   */

  /*
   * Initialize the Subject API source manager
   * TODO I *really* hate this method and how I use it.
   */
  private static void init() {
    if (mgr == null) {
      log.debug("Initializing source manager");
      try { 
        mgr = SourceManager.getInstance();
        log.debug("Initialized source manager: " + mgr);
        Iterator iter = mgr.getSources().iterator();
        while (iter.hasNext()) {
          Source sa = (Source) iter.next();
          sources.add(sa);
          log.debug("Added source: " + sa);
        } 
        cache = new SubjectCache();
        SubjectFactory.loadTypes(); 
        log.info("Subject factory initialized");
      } catch (Exception e) {
        throw new RuntimeException(e); // TODO ???
      } 
    }
  }
   
  /*
   * Load the known subject types.
   */ 
  private static void loadTypes() {
    if (types == null) {
      log.debug("Loading types");
      types = new HashMap();
      SubjectFactory.init();
      Iterator iter = sources.iterator();
      while (iter.hasNext()) {
        Source sa = (Source) iter.next();
        log.debug("Loading types from " + sa);
        Iterator typeIter = sa.getSubjectTypes().iterator();
        while (typeIter.hasNext()) {
          SubjectType type = (SubjectType) typeIter.next();
          types.put( type.getName(), type );
          log.debug("Loaded type " + type.getName());
        }
      }
    }
  }
 
}

