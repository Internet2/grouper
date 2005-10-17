package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.io.Serializable;
import  java.util.*;
import  org.apache.commons.logging.Log;
import  org.apache.commons.logging.LogFactory;

public class SubjectFinder implements Serializable {

  /*
   * PRIVATE CLASS VARIABLES
   */
/*
  private static SimpleCache    cacheGS   = null;
  private static SimpleCache    cacheGSID = null;
  private static Log            log       = LogFactory.getLog(SubjectFinder.class);
  private static SourceManager  mgr       = null;
  private static List           sources   = new ArrayList();
  private static Map            types     = null;  
*/


  /*
   * PUBLIC CLASS METHODS 
   */

  /**
   * Gets a subject by its id and using the default subject type.
   * <p />
   * TODO Why doesn't this search all sources?
   * <pre class="eg">
   * // Retrieve a subject of type _Grouper.DEF_SUBJ_TYPE_, the default 
   * // subject type.
   * Subject subj = SubjectFinder.findById(subjectID);
   *  </pre>
   * @param   id      Subject ID
   * @return  A {@link Subject} object
   * @throws SubjectNotFoundException
   */
/*
  public static Subject findById(String id)
    throws SubjectNotFoundException 
  {
    return SubjectFinder.getSubject(id, Grouper.DEF_SUBJ_TYPE);
  }
*/

  /**
   * Gets a subject by its ID.
   * <p />
   * <pre class="eg">
   * // Retrieve a subject of type _group_ for group _g_.
   * Subject subj = SubjectFinder.getSubject(g.id(), "group");
   *  </pre>
   * @param   id      subject identifier
   * @param   type    subject type
   * @return  a {@link Subject}
   * @throws SubjectNotFoundException
   */
/*
  public static Subject getSubject(String id, String type) 
    throws SubjectNotFoundException 
  {
    SubjectFinder.init();
    boolean cached  = false;
    Subject subj    = null;
    log.debug("Getting subject " + id + "/" + type);
    if (types.containsKey(type)) {
      try {
        subj = (Subject) cacheGS.get(id, type);
        cached = true;
        log.debug("Found cached subject " + id + "/" + type);
      } catch (CacheNotFoundException e) {
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
             // Don't worry about not finding subjects in particular adapters.
            continue;
          }
        }
      }
    } else {
      log.debug("Unknown subject type: " + type);
    }
    if (subj != null) {
      if (cached == false) {
        cacheGS.put(id, type, (Object) subj);
        log.debug("Caching subject " + id + "/" + type);
      }
    } else {
      // TODO Do I want a negative cache?
      log.debug("Unable to find subject " + id + "/" + type);
      throw new SubjectNotFoundException(
        "Could not get " + id + "/" + type
      );
    }
    return subj;
  }
*/
 
  /**
   * Gets a Subject by other well-known identifiers.
   * <p />
   * @param   id  identifier
   * @return  a {@link Subject}
   * @throws  SubjectNotFoundException
   */
/*
  public static Subject getSubjectByIdentifier(String id) 
    throws SubjectNotFoundException
  {
    return SubjectFinder.getSubjectByIdentifier(id, Grouper.DEF_SUBJ_TYPE);
  }
*/

  /**
   * Gets a Subject by other well-known identifiers.
   * <p />
   * @param   id    identifier
   * @param   type  subject type
   * @return  a {@link Subject}
   * @throws  SubjectNotFoundException
   */
/*
  public static Subject getSubjectByIdentifier(String id, String type) 
    throws SubjectNotFoundException
  {
    // TODO A lot of duplication with _getSubject()_
    SubjectFinder.init();
    boolean cached  = false;
    Subject subj    = null;
    if (types.containsKey(type)) {
      try {
        subj = (Subject) cacheGSID.get(id, type);
        cached = true;
        log.debug("Found cached subject " + id + "/" + type);
      } catch (CacheNotFoundException e) {
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
             // Don't worry about not finding subjects in particular adapters.
            continue;
          }
        }
      }
    } else {
      log.info("Unknown subject type: " + type);
    }
    if (subj != null) {
      if (cached == false) {
        cacheGSID.put(id, type, (Object) subj);
        log.debug("Caching subject " + id + "/" + type);
      }
    } else {
      // TODO Do I want a negative cache?
      log.debug("Unable to find subject " + id + "/" + type);
      throw new SubjectNotFoundException(
        "Could not get " + id + "/" + type
      );
    }
    return subj;
  }
*/

  /**
   * @return true if subject type is known.
   */
/*
  public static boolean hasType(String type) {
    SubjectFinder.init();
    if (types.containsKey(type)) {
      return true;
    }
    return false;
  }
*/

  /**
   * Unstructured search for Subjects.
   * <p />
   * @param searchValue Source adapter specific query string.
   * @return  Set of found subjects.
   */
  // TODO cache?
/*
  public static Set search(String searchValue) {
    SubjectFinder.init();
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
*/

  /**
   * @return known subject types.
   */
/*
  public static Set types() {
    SubjectFinder.init();
    return new HashSet( types.values() );
  }
*/
 
 
  /*
   * PRIVATE CLASS METHODS
   */

  /*
   * Initialize the Subject API source manager
   * TODO I *really* hate this method and how I use it.
   */
/*
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
        log.info("Initializing caches");
        cacheGS   = new SimpleCache();
        cacheGSID = new SimpleCache();
        SubjectFinder.loadTypes(); 
        log.info("Subject factory initialized");
      } catch (Exception e) {
        throw new RuntimeException(e); // TODO ???
      } 
    }
  }
*/
   
  /*
   * Load the known subject types.
   */ 
/*
  private static void loadTypes() {
    if (types == null) {
      log.debug("Loading types");
      types = new HashMap();
      SubjectFinder.init();
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
*/
 
}

