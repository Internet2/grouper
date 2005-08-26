/*--
$Id: Reason.java,v 1.2 2005-08-26 19:50:24 acohen Exp $
$Date: 2005-08-26 19:50:24 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

/**
 * This is a typesafe enumeration that identifies the various reasons a
 * Signet operation may be disallowed.
 *  
 */
public class Reason extends TypeSafeEnumeration
{
  /**
   * Constructor is private to prevent instantiation except during
   * class loading.
   * 
   * @param name
   *          the external name of the Reason value.
   * @param description
   *          the human readable description of the Reason value, by
   *          which it may be presented in a log file or a debug console.
   */
  private Reason(String name, String description)
  {
    super(name, description);
  }

  /**
   * The instance that describes an attempt to modify one's own
   * {@link Assignment}.
   */
  public static final Reason SELF
    = new Reason
        ("self",
         "It is illegal to grant an assignment to oneself, or to modify or"
         + " revoke an assignment which is granted to oneself.");

  /**
   * The instance that describes an attempt to modify an {@link Assignment}
   * beyond the reach of one's own {@link Limit}s.
   */
  public static final Reason LIMIT
    = new Reason
        ("limit",
         "This assignment includes a Limit-value which this PrivilegedSubject"
         + " does not have sufficient privileges to work with.");

  /**
   * The instance that describes an attempt to modify an {@link Assignment}
   * which involves a {@link Function} that one has no grantable
   * <code>Assignment</code>s for.
   */
  public static final Reason FUNCTION
    = new Reason
        ("function",
         "The PrivilegedSubject has no grantable privileges in regard to this"
         + " function.");

  /**
   * The instance that describes an attempt to modify an {@link Assignment}
   * which involves a scope that one has no grantable <code>Assignment</code>s
   * over for the specified {@link Function}.
   */
  public static final Reason SCOPE
    = new Reason
        ("scope",
         "The scope of this Assignment lies outside the scope within which"
         + " this PrivilegedSubject has the ability to grant the specified"
         + " Function.");
}
