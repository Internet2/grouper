/*
 * Copyright 2006-2007 The University Of Chicago Copyright 2006-2007 University
 * Corporation for Advanced Internet Development, Inc. Copyright 2006-2007 EDUCAUSE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.ldappc.synchronize;

import java.util.Iterator;
import java.util.Vector;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapContext;

import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.ldappc.util.SubjectCache;

/**
 * This defines the common functionality needed by all synchronizers.
 */
public abstract class Synchronizer {

  /**
   * Ldap context.
   */
  private LdapContext context;

  /**
   * Cache for subjects that have already been retrieved.
   */
  private SubjectCache subjectCache;

  /**
   * Constructs a <code>Synchronizer</code>.
   * 
   * @param ctx
   *          Ldap context to be used for synchronizing
   * @param subjectCache
   *          Subject cache to speed subject retrieval
   */
  public Synchronizer(LdapContext ctx, SubjectCache subjectCache) {
    setContext(ctx);
    setSubjectCache(subjectCache);
  }

  /**
   * Get the Ldap context.
   * 
   * @return the LDAP context.
   */
  public LdapContext getContext() {
    return context;
  }

  /**
   * Set the Ldap context.
   * 
   * @param context
   *          Ldap context
   */
  protected void setContext(LdapContext context) {
    this.context = context;
  }

  /**
   * Return the subject cache.
   * 
   * @return the subjectIDTables.
   */
  public SubjectCache getSubjectCache() {
    return subjectCache;
  }

  /**
   * Set the subject cache.
   * 
   * @param subjectCache
   *          the subjectCache to set
   */
  public void setSubjectCache(SubjectCache subjectCache) {
    this.subjectCache = subjectCache;
  }

  /**
   * This determines if the identified attribute is required. If <code>objectClass</code>
   * is provided (i.e, not null), then the attribute is required if
   * <code>attributeName</code> identifies a <b>MUST</b> attribute for
   * <code>objectClass</code>. If <code>objectClass</code> is <code>null</code>, then the
   * attribute is required if <code>attributeName</code> indentifies a <b>MUST</b>
   * attribute for any of the current object classes defined for <code>dn</code>.
   * 
   * @param ctx
   *          Ldap Context
   * @param dn
   *          DN of the entry the attribute will be populate on
   * @param objectClass
   *          Name of the object class that supports attrName (this may be null)
   * @param attributeName
   *          Name of the attribute
   * @return <code>true</code> if the attribute is required, and <code>false</code>
   *         otherwise
   * 
   * @throws OperationNotSupportedException
   *           thrown if the schema can not be accessed
   * @throws NamingException
   *           thrown if a Naming error occurs
   */
  protected boolean isAttributeRequired(LdapContext ctx, Name dn, String objectClass,
      String attributeName) throws OperationNotSupportedException, NamingException {
    //
    // Build the list of object classes examine based on whether or not
    // a specific objectClass was passed (i.e.,objectClass is not null)
    //
    Vector objectClasses = new Vector();
    if (objectClass != null) {
      objectClasses.add(objectClass);
    } else {
      //
      // Get the objectClass list for dn
      //
      Attributes attributes = ctx.getAttributes(dn,
          new String[] { LdapUtil.OBJECT_CLASS_ATTRIBUTE });
      Attribute attribute = attributes.get(LdapUtil.OBJECT_CLASS_ATTRIBUTE);

      //
      // Add the object class list to objectClasses
      //
      NamingEnumeration values = attribute.getAll();
      while (values.hasMore()) {
        objectClasses.add(values.next());
      }
    }

    //
    // Build query filter to determine if the attribute is required (i.e.,
    // is a MUST attribute)
    //
    String filter = "(&(MUST=" + attributeName + ")";
    if (objectClasses.size() > 1) {
      filter += "(|";
    }

    Iterator objClassIter = objectClasses.iterator();
    while (objClassIter.hasNext()) {
      filter += "(NAME=" + objClassIter.next() + ")";
    }

    if (objectClasses.size() > 1) {
      filter += ")";
    }

    filter += ")";

    //
    // Build the search controls
    // - only need to return a single result
    // - only need to search one level
    //
    SearchControls controls = new SearchControls();
    controls.setCountLimit(1);
    controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);

    //
    // Get the schema from dn
    //
    DirContext schema = ctx.getSchema(dn);

    //
    // Search the "ClassDefinition"'s
    //
    NamingEnumeration results = schema.search("ClassDefinition", filter, controls);

    //
    // If a result was found, it IS required
    //
    boolean isRequired = results.hasMore();

    return isRequired;
  }

  /**
   * This determines if the identified attribute is required. This calls
   * {@link #isAttributeRequired(LdapContext, Name, String, String)} to determine if the
   * attribute is required. If the schema cannot be accessed, the <code>isRequired</code>
   * value is returned rather than throwing an exception.
   * 
   * @param ctx
   *          Ldap Context
   * @param dn
   *          DN of the entry the attribute will be populate on
   * @param objectClass
   *          Name of the object class that supports attrName (this may be null)
   * @param attributeName
   *          Name of the attribute
   * @param isRequired
   *          value returned if the schema cannot be accessed.
   * @return If the schema can be accessed then <code>true</code> if the attribute is
   *         required, and <code>false</code> otherwise. Otherwise <code>isRequired</code>
   *         is returned.
   * @throws NamingException
   *           thrown if a Naming error occurs
   */
  protected boolean isAttributeRequired(LdapContext ctx, Name dn, String objectClass,
      String attributeName, boolean isRequired) throws NamingException {
    //
    // Init the return value to be the default value
    //
    boolean required = isRequired;

    //
    // Try to determine whether or not the attribute is required
    //
    try {
      required = isAttributeRequired(ctx, dn, objectClass, attributeName);
    } catch (OperationNotSupportedException onse) {
      //
      // Log the exception
      //
      String msg = "Schema for " + dn + "cannot be accessed. It is assumed that the "
          + attributeName + " attribute in " + objectClass + " is "
          + (isRequired ? "" : "not") + " a required attribute.";
      ErrorLog.warn(getClass(), msg);

      //
      // Make sure return value is set to default value
      //
      required = isRequired;
    }

    return required;
  }
}
