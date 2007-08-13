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
import  edu.internet2.middleware.grouper.internal.dao.RegistrySubjectDAO;
import  edu.internet2.middleware.grouper.internal.dto.RegistrySubjectDTO;
import  edu.internet2.middleware.grouper.internal.util.U;
import  edu.internet2.middleware.subject.*;
import  java.util.HashMap;
import  java.util.Map;
import  org.apache.commons.logging.*;

/**
 * <a href="http://www.martinfowler.com/bliki/ObjectMother.html">ObjectMother</a> for Grouper testing.
 * <p/>
 * @author  blair christensen.
 * @version $Id: R.java,v 1.19 2007-08-13 16:07:04 blair Exp $
 * @since   1.2.0
 */
public class R {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(R.class);

  
  // PROTECTED INSTANCE VARIABLES //
  protected GrouperSession  rs    = null;
  protected Stem            root  = null;
  protected Stem            ns    = null;


  // PRIVATE INSTANCE VARIABLES //
  private Map<String, Group>  groups    = new HashMap<String, Group>();
  private GrouperSession      s;
  private Map<String, Stem>   stems     = new HashMap();
  private Map                 subjects  = new HashMap<String, Stem>();


  // CONSTRUCTORS //

  /**
   * <a href="http://www.martinfowler.com/bliki/ObjectMother.html">ObjectMother</a> for Grouper testing.
   * <p/>
   * @since   1.2.0
   */
  public R() {
    super();
  } // R()


  /**
   * Add test subjects to registry.
   * <p/>
   * @since   @HEAD@
   */
  protected void addSubjects(int number) {
    RegistrySubject     subj;
    RegistrySubjectDAO  dao   = GrouperDAOFactory.getFactory().getRegistrySubject();
    RegistrySubjectDTO  _subj;
    for (int i=0; i<number; i++) {
      String  id    = _getSuffix(i);
      String  name  = "subject " + id;
      _subj = new RegistrySubjectDTO()
                .setId(id)
                .setName(name)
                .setType("person")
                ;
      dao.create(_subj);
      subj = new RegistrySubject();
      subj.setDTO(_subj);
      this.subjects.put(id, subj);  
      LOG.debug("created subject: " + subj);
    }
  }

  /**
   * Initializes and returns a pre-defined context.
   * <p/>
   * @throws  GrouperRuntimeException
   * @throws  IllegalStateException
   * @since   1.2.0
   */
  public static R getContext(String ctx) 
    throws  GrouperRuntimeException,
            IllegalStateException
  {
    if      ( ctx.equals("grouper") ) { 
      return _getContextGrouper();
    }
    else if ( ctx.equals("i2mi") )    {
      return _getContextI2MI();
    }
    else {
      throw new IllegalStateException("unknown context: " + ctx);
    }
  } // public static R getContext(ctx)


  // PUBLIC INSTANCE METHODS //

  /**
   * Adds a child {@link Group} beneath <i>parent</i>.
   * <p/>
   * @return  Child {@link Group}. 
   * @throws  GrouperRuntimeException
   * @since   1.2.0
   */
  public Group addGroup(Stem parent, String extn, String displayExtn) 
    throws  GrouperRuntimeException
  {
    try {
      Group child = parent.addChildGroup(extn, displayExtn);
      this.groups.put( child.getName(), child );
      return child;
    }
    catch (GroupAddException eGA) {
      throw new GrouperRuntimeException( eGA.getMessage(), eGA );
    }
    catch (InsufficientPrivilegeException eIP) {
      throw new GrouperRuntimeException( eIP.getMessage(), eIP );
    }
  } // public Group addGroup(parent, extn, displayExtn)

  /**
   * Adds a child {@link Stem} beneath the root stem.
   * <p/>
   * @return  Child {@link Stem}.
   * @throws  GrouperRuntimeException
   * @since   1.2.0
   */
  public Stem addStem(String extn, String displayExtn) 
    throws  GrouperRuntimeException
  {
    return addStem( this.findRootStem(), extn, displayExtn );
  } // public Stem addStem(extn)

  /**
   * Adds a child {@link Stem} beneath <i>parent</i>.
   * <p/>
   * @return  Child {@link Stem}.
   * @throws  GrouperRuntimeException
   * @since   1.2.0
   */
  public Stem addStem(Stem parent, String extn, String displayExtn) 
    throws  GrouperRuntimeException 
  {
    try {
      Stem child = parent.addChildStem(extn, displayExtn);
      this.stems.put( child.getName(), child );
      return child;
    }
    catch (InsufficientPrivilegeException eIP) {
      throw new GrouperRuntimeException( eIP.getMessage(), eIP );
    }
    catch (StemAddException eNSA) {
      throw new GrouperRuntimeException( eNSA.getMessage(), eNSA );
    }
  } // public Stem addStem(parent, extn, displayExtn)

  /**
   * @return  The root {@link Stem} of the Groups Registry.
   * @since   1.2.0
   */
  public Stem findRootStem() {
    if (this.root == null) {
      this.root = StemFinder.findRootStem( this.getSession() );
    }
    return this.root;
  } // public Stem findRootStem()

  /**
   * Starts, associates and returns a {@link GrouperSession} running as <i>GrouperSystem</i>.
   * <p/>
   * @return  Return {@link GrouperSession} running as <i>GrouperSystem</i>.
   * @throws  GrouperRuntimeException
   * @since   1.2.0
   */
  public GrouperSession startSession() 
    throws  GrouperRuntimeException
  {
    return this.startRootSession();
  } // public GrouperSession startSession()

  /**
   * Starts, associates and returns a {@link GrouperSession} running as <i>subj</i>.
   * <p/>
   * @return  Return {@link GrouperSession} running as <i>subj</i>.
   * @throws  GrouperRuntimeException
   * @since   1.2.0 
   */
  public GrouperSession startSession(Subject subj) 
    throws  GrouperRuntimeException
  {
    try {
      this.setSession( GrouperSession.start(subj) );
      return this.getSession();
    }
    catch (SessionException eS) {
      throw new GrouperRuntimeException( eS.getMessage(), eS );
    }
  } // public GrouperSession startSession(subj)

  /**
   * Starts, associates and returns a {@link GrouperSession} running as <i>GrouperAll</i>.
   * <p/>
   * @return  Return {@link GrouperSession} running as <i>GrouperSystem</i>.
   * @throws  GrouperRuntimeException
   * @since   1.2.0
   */
  public GrouperSession startAllSession() 
    throws  GrouperRuntimeException
  {
    return this.startSession( SubjectFinder.findAllSubject() );
  } // public GrouperSession startAllSession()

  /**
   * Starts, associates and returns a {@link GrouperSession} running as <i>GrouperSystem</i>.
   * <p/>
   * @return  Return {@link GrouperSession} running as <i>GrouperSystem</i>.
   * @throws  GrouperRuntimeException
   * @since   1.2.0
   */
  public GrouperSession startRootSession() 
    throws  GrouperRuntimeException
  {
    return this.startSession( SubjectFinder.findRootSubject() );
  } // public GrouperSession startRootSession()


  // PRIVATE CLASS METHODS //

  // @since   1.2.0 
  private static R _getContextGrouper() 
    throws  GrouperRuntimeException
  {
    R     r       = getContext("i2mi");
    Stem  i2mi    = r.getStem("i2mi");
    Stem  grouper = r.addStem(i2mi, "grouper", "grouper");
    r.addGroup( grouper, "grouper-dev", "grouper development" );
    r.addGroup( grouper, "grouper-users", "grouper users" );
    return r;
  } // private static R _getContextGrouper()

  // @since   1.2.0
  private static R _getContextI2MI() {
    R r = new R();
    r.addStem("i2mi", "internet2 middleware initiative");
    r.addSubjects(2);
    return r;
  } // private static R _getContextI2MI()
  

  // GETTERS //

  /**
   * Returns {@link GrouperSession} associated with this {@link R}.  
   * <p>If no session is associated, a root session will be created and associated..</p>
   * @return  {@link GrouperSession} 
   * @since   1.2.0
   */
  public GrouperSession getSession() {
    if (this.s == null) {
      this.s = this.startSession();
    }
    return this.s;
  } // public GrouperSession getSession()

  /**
   * Returns a cached {@link Stem}.
   * <p/>
   * @return  Cached {@link Stem} if it exists.
   * @throws  GrouperRuntimeException
   * @since   1.2.0
   */
  public Stem getStem(String stem) 
    throws  GrouperRuntimeException
  {
    if (this.stems.containsKey(stem)) {
      return (Stem) this.stems.get(stem);
    }
    throw new GrouperRuntimeException("stem not found: " + stem);
  } // public Stem getStem(stem)


  // SETTERS //

  /**
   * Set {@link GrouperSession} associated with this {@link R}.
   * <p/>
   * @since   1.2.0
   */
  public void setSession(GrouperSession s) {
    this.s = s;
  } // public void setSession(s)


  // PROTECTED CLASS METHODS //
  protected static R populateRegistry(int nStems, int nGroups, int nSubjects) 
    throws  Exception
  {
    LOG.info("populateRegistry");   
    R r  = new R();
    r.rs    = SessionHelper.getRootSession();
    r.root  = StemFinder.findRootStem(r.rs);
    r.ns    = r.root.addChildStem("i2", "internet2");
    for (int i=0; i<nStems; i++) {
      String  nsExtn  = _getSuffix(i);
      Stem    ns      = r.ns.addChildStem(nsExtn, "stem " + nsExtn);
      LOG.debug("created stem: " + ns);
      r.stems.put(nsExtn, ns);
      for (int j=0; j<nGroups; j++) {
        String  gExtn = _getSuffix(j);
        String  key   = nsExtn + ":" + gExtn;
        Group   g     = ns.addChildGroup(gExtn, "group " + gExtn);
        LOG.debug("created group: " + g);
        r.groups.put(key, g);
      }
    }
    r.addSubjects(nSubjects);

    return r;
  } // protected static R populateRegistry(nStems, nGroups, nSubjects)

  // PROTECTED INSTANCE METHODS //
  protected Group getGroup(String stem, String group) 
    throws  Exception
  {
    String key = U.constructName(stem, group);
    if (this.groups.containsKey(key)) {
      return (Group) this.groups.get(key);
    }
    throw new Exception("group not found: " + key);
  } // protected Group getGroup(stem, group)

  protected Subject getSubject(String id) 
    throws  Exception
  {
    // Bah.  We stash RegistrySubjects but we need Subjects.  
    if (this.subjects.containsKey(id)) {
      return SubjectFinder.findById(id, "person");
    }
    throw new Exception("subject not found: " + id);
  } // protected Subject getSubject(id)


  // PRIVATE CLASS METHODS //

  private static String _getSuffix(int i) {
    int     base  = 97;
    return  new Character( (char) (i + base) ).toString();
  } // private static String _getSuffix(i)

} // public class R

