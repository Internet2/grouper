/*--
 
Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
See doc/license.txt in this distribution.
 */
/*
 * JNDISourceAdapter.java
 *
 * Created on March 6, 2006
 *
 * Author Ellen Sluss
 */
package edu.internet2.middleware.subject.provider;

/**
 *
 * @author esluss
 */

import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;

import javax.naming.*;
import javax.naming.directory.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.SubjectType;


/**
 * JNDI Source  * <br>
 *
 */
public class JNDISourceAdapter
        extends BaseSourceAdapter {
    
    private static Log log = LogFactory.getLog(JNDISourceAdapter.class);
    
    Hashtable environment = new Hashtable(11);
    
    String nameAttributeName = null;
    String subjectIDAttributeName = null;
    String descriptionAttributeName = null;
    String subjectTypeString = null;
    
    
    // Return scope for searching as a int - associate the string with the int
    protected static HashMap scopeStrings = new HashMap();
    static {
        scopeStrings.put("OBJECT_SCOPE", Integer.valueOf(SearchControls.OBJECT_SCOPE));
        scopeStrings.put("ONELEVEL_SCOPE", Integer.valueOf(SearchControls.ONELEVEL_SCOPE));
        scopeStrings.put("SUBTREE_SCOPE", Integer.valueOf(SearchControls.SUBTREE_SCOPE));
        
    }
    protected static int getScope(String scope ) {
        Integer s = (Integer) scopeStrings.get(scope.toUpperCase());
        if (s==null) {
            return -1;
        }
        return ((Integer) scopeStrings.get(scope)).intValue();
    }
    /**
     * Allocates new JNDISourceAdapter;
     */
    public JNDISourceAdapter() {
        super();
    }
    
    /**
     * Allocates new JNDISourceAdapter;
     * @param id
     * @param name
     */
    public JNDISourceAdapter(String id, String name) {
        super(id, name);
    }
    
    /**
     * {@inheritDoc}
     */
    public Subject getSubject(String id)
    throws SubjectNotFoundException {
        Subject subject = null;
        Search search = getSearch("searchSubject");
        if (search == null) {
            log.error("searchType: \"searchSubject\" not defined.");
            return subject;
        }
        String[] attributeNames = {nameAttributeName, descriptionAttributeName};
        Attributes attributes = getLdapUnique( search, id, attributeNames   );
        try {
            String name = (String) attributes.get(nameAttributeName).get();
            String description = (String) attributes.get(descriptionAttributeName).get();
            subject = new JNDISubject(id, name, description, this.getSubjectType(), this);
        } catch (NamingException ex) {
            log.error("LDAP Naming Except: " + ex.getMessage(), ex);
        }
        if (subject == null) {
            throw new SubjectNotFoundException("Subject " + id + " not found.");
        }
        
        return subject;
    }
    
    /**
     * {@inheritDoc}
     */
    public Subject getSubjectByIdentifier(String id)
    throws SubjectNotFoundException {
        Subject subject = null;
        Search search = getSearch("searchSubjectByIdentifier");
        if (search == null) {
            log.error("searchType: \"searchSubjectByIdentifier\" not defined.");
            return subject;
        }
        String[] attributeNames = {nameAttributeName, subjectIDAttributeName, descriptionAttributeName};
        Attributes attributes = getLdapUnique( search, id, attributeNames );
        try {
            String name = (String) attributes.get(nameAttributeName).get();
            String subjectID = (String)attributes.get(subjectIDAttributeName).get();
            String description = (String)attributes.get(descriptionAttributeName).get();
            subject = new JNDISubject(subjectID, name, description, this.getSubjectType(), this);
        } catch (NamingException ex) {
            log.error("LDAP Naming Except: " + ex.getMessage(), ex);
        }
        
        if (subject == null) {
            throw new SubjectNotFoundException("Subject " + id + " not found.");
        }
        return subject;
    }
    
    /**
     * {@inheritDoc}
     */
    public Set search(String searchValue) {
        Set result = new HashSet();
        Search search = getSearch("search");
        if (search == null) {
            log.error("searchType: \"search\" not defined.");
            return result;
        }
        String[] attributeNames = {nameAttributeName, subjectIDAttributeName,descriptionAttributeName} ;
        NamingEnumeration ldapResults = getLdapResults(search,searchValue, attributeNames);
        if (ldapResults == null) {
            return result;
        }
        try {
            while ( ldapResults.hasMore()) {
                SearchResult si = (SearchResult) ldapResults.next();
                Attributes attributes = si.getAttributes();
                String name = (String) attributes.get(nameAttributeName).get();
                String subjectID   = (String) attributes.get(subjectIDAttributeName).get();
                String description   = (String) attributes.get(descriptionAttributeName).get();
                SubjectType type = this.getSubjectType();
                Subject subject = new JNDISubject(subjectID,  name, description, type, this);
                result.add(subject);
            }
        } catch (NamingException ex) {
            log.error("LDAP Naming Except: " + ex.getMessage(), ex);
        }
        
        
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    public void init() throws SourceUnavailableException {
        try {
            Properties props = getInitParams();
            setupEnvironment(props);
        } catch (Exception ex) {
            throw new SourceUnavailableException(
                    "Unable to init JNDI source", ex);
        }
    }
    
    
    /**
     * Setup environment.
     * @throws SourceUnavailableException
     */
    protected void setupEnvironment(Properties props)
    throws SourceUnavailableException {
        
        environment.put(Context.INITIAL_CONTEXT_FACTORY,  props.getProperty("INITIAL_CONTEXT_FACTORY") );
        environment.put(Context.PROVIDER_URL, props.getProperty("PROVIDER_URL") );
        environment.put(Context.SECURITY_AUTHENTICATION, props.getProperty("SECURITY_AUTHENTICATION"));
        environment.put(Context.SECURITY_PRINCIPAL, props.getProperty("SECURITY_PRINCIPAL") );
        environment.put(Context.SECURITY_CREDENTIALS,props.getProperty("SECURITY_CREDENTIALS"));
        if (props.getProperty("SECURITY_PROTOCOL")!= null) {
            environment.put(Context.SECURITY_PROTOCOL, "ssl");
        }
        Context context = null;
        try {
            log.debug("Creating Directory Context");
            context = new InitialDirContext(environment);
        } catch (AuthenticationException ex) {
            log.error("Error with Authentication " + ex.getMessage(), ex);
            throw new SourceUnavailableException("Error with Authentication ", ex);
        } catch (NamingException ex) {
            log.error("Naming Error " + ex.getMessage(), ex);
            throw new SourceUnavailableException("Naming Error", ex);
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException ne) {
                    // squelch, since it is already closed
                }
            }
        }
        log.info("Success in connecting to LDAP");
        
        nameAttributeName = props.getProperty("Name_AttributeType");
        if (nameAttributeName==null) {
            log.error("Name_AttributeType not defined");
        }
        subjectIDAttributeName = props.getProperty("SubjectID_AttributeType");
        if (subjectIDAttributeName==null) {
            log.error("SubjectID_AttributeType not defined");
        }
        descriptionAttributeName = props.getProperty("Description_AttributeType");
        if (descriptionAttributeName==null) {
            log.error("Description_AttributeType not defined");
        }
        
    }
    
    
    /**
     * Loads attributes for the argument subject.
     */
    protected Map loadAttributes(JNDISubject subject) {
        Map attributes = new HashMap();
        Search search = getSearch("searchSubject");
        if (search == null) {
            log.error("searchType: search not defined.");
            return attributes;
        }
        //setting attributeNames to null will cause all attributes for a subject to be returned
        String[] attributeNames = null;
        try {
            Attributes ldapAttributes = getLdapUnique(search,subject.getId(), attributeNames);
            for (NamingEnumeration e = ldapAttributes.getAll(); e.hasMore();) {
                Attribute attr = (Attribute) e.next();
                String name = attr.getID();
                Set values = new HashSet();
                for (NamingEnumeration en = attr.getAll(); en.hasMore(); ) {
                    String value = (String) en.next();
                    values.add(value);
                }
                attributes.put(name, values);
            }
            subject.setAttributes(attributes);
        } catch (SubjectNotFoundException ex ) {
            log.error("SubjectNotFound: "+ subject.getId() +" " + ex.getMessage(), ex);
        } catch (NamingException ex ) {
            log.error("LDAP Naming Except: " + ex.getMessage(), ex);
        }
        return attributes;
    }
    
    protected NamingEnumeration getLdapResults(Search search, String searchValue, String[] attributeNames) {
        DirContext context = null;
        NamingEnumeration results = null;
        String filter = search.getParam("filter");
        if (filter==null) {
            log.error("Search filter not found for search type:  " + search.getSearchType());
            return results;
        }
        filter = filter.replaceAll("%TERM%", searchValue);
        String base = search.getParam("base");
        if (base==null){
            base = "";
            log.error("Search base not found for:  " + search.getSearchType()+  ". Using base \"\" ");
            
        }
        int scopeNum = -1;
        String scope = search.getParam("scope");
        if (scope!=null) {
            scopeNum = getScope(scope);
        }
        if (scopeNum == -1) {
            scopeNum = SearchControls.SUBTREE_SCOPE;
            log.error("Search scope not found for: " + search.getSearchType() + ". Using scope SUBTREE_SCOPE." );
        }
        log.debug("searchType: " + search.getSearchType() + " filter: " + filter + " base: " + base + " scope: "+ scope);
        try  {
            context = new InitialDirContext(environment);
            SearchControls constraints = new SearchControls( );
            constraints.setSearchScope( scopeNum );
            constraints.setReturningAttributes(attributeNames);
            results = context.search( base, filter, constraints );
        } catch ( AuthenticationException ex ) {
            log.error("Ldap Authentication Exception: " + ex.getMessage(), ex);
        } catch (NamingException ex) {
            log.error("Ldap NamingException: " + ex.getMessage(), ex);
            
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException ne) {
                    // squelch, since it is already closed
                }
            }
        }
        return results;
        
    }
    
    
    protected Attributes getLdapUnique( Search search, String searchValue, String[] attributeNames) throws SubjectNotFoundException  {
        Attributes attributes = null;
        NamingEnumeration results = getLdapResults(search, searchValue, attributeNames);
        
        try  {
            if (results == null || !results.hasMore()) {
                String errMsg = "No results: " + search.getSearchType() + " filter:" + search.getParam("filter")
                + " searchValue: " + searchValue;
                throw new SubjectNotFoundException( errMsg);
            }
            
            SearchResult si = ( SearchResult )results.next( );
            attributes = si.getAttributes();
            if ( results.hasMore()) {
                si = (SearchResult) results.next();
                String errMsg ="Search is not unique:" + si.getName() + "\n";
                throw new SubjectNotFoundException( errMsg );
            }
        } catch (NamingException ex) {
            log.error("Ldap NamingException: " + ex.getMessage(), ex);
        }
        return attributes;
    }
    public static void main(String[] args) {
        
    }
    
}
