/*--
$Id: NullSourceAdapter.java,v 1.1 2006-04-28 17:43:36 blair Exp $
$Date: 2006-04-28 17:43:36 $

Copyright (C) 2006 Internet2 and The University Of Chicago.  
All Rights Reserved.
See doc/license.txt in this distribution.
*/
package edu.internet2.middleware.subject.provider;

import edu.internet2.middleware.subject.*;
import java.util.*;
import org.apache.commons.logging.*;

/**
 * Null {@link Source} which will never return any {@link Subject}s.
 * @author  blair christensen.
 * @version $Id: NullSourceAdapter.java,v 1.1 2006-04-28 17:43:36 blair Exp $
 */
public class NullSourceAdapter extends BaseSourceAdapter {

  // Private Class Constants //
  private static final Log LOG    = LogFactory.getLog(NullSourceAdapter.class);
  private static final Set TYPES  = new HashSet();

  static {
    TYPES.add( SubjectTypeEnum.valueOf( "application" ) );
    TYPES.add( SubjectTypeEnum.valueOf( "group"       ) );
    TYPES.add( SubjectTypeEnum.valueOf( "person"      ) );
  } // static

	
  // Constructors //	

  /**
   * Allocates new {@link NullSourceAdapter}.
   */
  public NullSourceAdapter() {
    super();
  } // public NullSourceAdapter()
	
  /**
   * Allocates new {@link NullSourceAdapter}.
   * @param id    The source id for the new adapter.
   * @param name  The source name for the new adapter.
   */
  public NullSourceAdapter(String id, String name) {
    super(id, name);
  } // public NullSourceAdapter(id, name)


  // Public Instance Methods //

  /**
   * {@inheritDoc}
   */
  public Subject getSubject(String id)
    throws SubjectNotFoundException 
  {
    throw new SubjectNotFoundException("Subject " + id + " not found.");
  } // public Subject getSubject(id)

  /**
   * {@inheritDoc}
   */
  public Subject getSubjectByIdentifier(String id)
    throws SubjectNotFoundException 
  {
    throw new SubjectNotFoundException("Subject " + id + " not found.");
  } // public Subject getSubjectByIdentifier(id)

  /**
   * {@inheritDoc}
   */	
  public Set getSubjectTypes() {
    return TYPES;
  } // public Set getSubjectTypes()

  /**
   * {@inheritDoc}
   */
  public void init()
    throws SourceUnavailableException 
  {
    // Nothing
  } // public void init()

  /**
   * {@inheritDoc}
   */
  public Set search(String searchValue) {
    return new HashSet();
  } // public Set search()

}

