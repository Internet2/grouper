/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* ========================================================================
 * Copyright (c) 2009-2011 The University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================================
 */

/*
 * Ldap subject adapter
 * @author fox
*/

package edu.internet2.middleware.subject.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.ldap.LdapPEMSocketFactory;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.morphString.Morph;
import edu.internet2.middleware.subject.SearchPageResult;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectCaseInsensitiveMapImpl;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.SubjectUtils;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapConfig;
import edu.vt.middleware.ldap.SearchFilter;
import edu.vt.middleware.ldap.pool.CompareLdapValidator;
import edu.vt.middleware.ldap.pool.ConnectLdapValidator;
import edu.vt.middleware.ldap.pool.DefaultLdapFactory;
import edu.vt.middleware.ldap.pool.LdapPool;
import edu.vt.middleware.ldap.pool.LdapPoolConfig;
import edu.vt.middleware.ldap.pool.LdapValidator;
import edu.vt.middleware.ldap.pool.SoftLimitLdapPool;

/**
 * Ldap source adapter.  
 */

public class LdapSourceAdapterLegacy extends BaseSourceAdapter {
    
  public static void main(String[] args) {
    System.out.println("abc123ABC_-".matches("[a-zA-Z0-9_-]+"));
    System.out.println("abc1 23ABC_-".matches("[a-zA-Z0-9_-]+"));
    System.out.println("abc1)23ABC_-".matches("[a-zA-Z0-9_-]+"));
    
  }
  
    private static Log log = LogFactory.getLog(LdapSourceAdapterLegacy.class);
    
    private Properties props;
    private String nameAttributeName = null;
    private String subjectIDAttributeName = null;
    private boolean subjectIDFormatToLowerCase = false;
    private String descriptionAttributeName = null;
    // private String subjectTypeString = null;
    private String localDomain = null;
    private String propertiesFile = null;
    private SoftLimitLdapPool ldapPool;
    private boolean initialized = false;

    private boolean multipleResults = false;

    private String[] allAttributeNames;

    private boolean throwErrorOnFindAllFailure;

    /** if there is a limit to the number of results */
    private Integer maxPage;

    public LdapSourceAdapterLegacy() {
        super();
    }
    
    public LdapSourceAdapterLegacy(String id, String name) {
        super(id, name);
    }
    
    /**
     * {@inheritDoc}
     */
    public void init() {
        log.debug("ldap source init");
        props = initParams();

	nameAttributeName = getNeededProperty(props,"Name_AttributeType");
	subjectIDAttributeName = getNeededProperty(props,"SubjectID_AttributeType");
	descriptionAttributeName = getNeededProperty(props,"Description_AttributeType");

	subjectIDFormatToLowerCase = SubjectUtils.booleanValue(getNeededProperty(props,"SubjectID_formatToLowerCase"), false);
	
        String mr = props.getProperty("Multiple_Results");
        if (mr!=null && (mr.equalsIgnoreCase("yes")||mr.equalsIgnoreCase("true"))) multipleResults = true;

        Set<?> attributeNameSet = this.getAttributes();
        allAttributeNames = new String[3+attributeNameSet.size()];
        allAttributeNames[0] = nameAttributeName;
        allAttributeNames[1] = subjectIDAttributeName;
        allAttributeNames[2] = descriptionAttributeName;
        int i = 0;
        for (Iterator<?> it = attributeNameSet.iterator(); it.hasNext(); allAttributeNames[3+i++]= (String) it.next());

        Map<String, String> virtualAttributes = SubjectUtils.nonNull(SubjectImpl.virtualAttributesForSource(this));
        
        // GRP-1669: grouper sends virtual attribute names to ldap
        //take out dupes and virtuals
        Set<String> attributeSet = SubjectUtils.toSet(allAttributeNames);
        attributeSet.removeAll(virtualAttributes.keySet());
        allAttributeNames = toArray(attributeSet, String.class);
        
        initializeLdap();
  
        String throwErrorOnFindAllFailureString = this.getInitParam("throwErrorOnFindAllFailure");
        throwErrorOnFindAllFailure = SubjectUtils.booleanValue(throwErrorOnFindAllFailureString, true);
        
        
        {
          String maxPageString = props.getProperty("maxPageSize");
          if (!StringUtils.isBlank(maxPageString)) {
            try {
              this.maxPage = Integer.parseInt(maxPageString);
            } catch (NumberFormatException nfe) {
              throw new SourceUnavailableException("Cant parse maxPage: " + maxPageString, nfe);
            }
          }
        }

   }

    /**
     * convert a list into an array of type of theClass
     * @param <T> is the type of the array
     * @param collection list to convert
     * @param theClass type of array to return
     * @return array of type theClass[] filled with the objects from list
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Collection collection, Class<T> theClass) {
      if (collection == null || collection.size() == 0) {
        return null;
      }

      return (T[])collection.toArray((Object[]) Array.newInstance(theClass,
          collection.size()));

    }

   private void initializeLdap() {
   
      log.debug("ldap initializeLdap");
      LdapConfig ldapConfig = null;
      LdapPoolConfig ldapPoolConfig = null;
      String cafile = (String)props.get("pemCaFile");
      String certfile = (String)props.get("pemCertFile");
      String keyfile = (String)props.get("pemKeyFile");
        
      // ldap properties can be in a separate properties file or specified in subject.properties

      propertiesFile = props.getProperty("ldapProperties_file");
      if (propertiesFile!=null) {
          
         try {
            // load ldap config from the ldap properties file
            if (log.isDebugEnabled()) {
              log.debug("reading properties file " + propertiesFile);
            }
         
            URL rsrcLocation;
            // Try opening the properties file from the file system
            File theFile = new File(propertiesFile);

            if (theFile.exists()) {
              rsrcLocation = theFile.toURI().toURL();
              if (log.isDebugEnabled()) {
                log.debug("properties file " + propertiesFile + " was found on the filesystem, location " + rsrcLocation.getFile());
              }
            } else {
              // otherwise, get it from the classpath
              rsrcLocation = SubjectUtils.computeUrl(propertiesFile, true);
              if (log.isDebugEnabled()) {
                log.debug("properties file " + propertiesFile + " was found on the classpath, location uri " + rsrcLocation);
              }
            }

            // If not successful, throw runtime exception.
            if (rsrcLocation == null) {
        	 log.error("Unable to open properties file '" + propertiesFile + "'");
        	 throw new IllegalArgumentException("Unable to open properties file '" + propertiesFile + "'");
            }

            // Create the ldap configuration from the properties file
            ldapConfig = LdapConfig.createFromProperties(rsrcLocation.openStream());
            if (log.isDebugEnabled()) {
               log.debug("from properties file " + propertiesFile + " got " + ldapConfig);
            }
            
            // Create a properties object from the properties file
            Properties properties = new Properties();
            properties.load(rsrcLocation.openStream());
            
            // Get the bindCredential property
            String bindCredential = properties.getProperty("edu.vt.middleware.ldap.bindCredential");                            
            // If bindCredential is blank, try the older serviceCredential property
            if (StringUtils.isBlank(bindCredential)) {
            	bindCredential = properties.getProperty("edu.vt.middleware.ldap.serviceCredential");
            }
            
            // The password might be encrypted
            if (!StringUtils.isBlank(bindCredential)) {
              bindCredential = Morph.decryptIfFile(bindCredential);
            }
            
            // Override the credential in case it was encrypted
            ldapConfig.setBindCredential(bindCredential);
            
            // get our cert config from the properties file as well
            Map<String, Object> props = ldapConfig.getEnvironmentProperties();
       
            Set<String> ps = props.keySet();
            if (log.isDebugEnabled()) {
              for (Iterator<String> it = ps.iterator(); it.hasNext(); log.debug(".. key = " + it.next()));
            }
         
            cafile = (String)props.get("pemCaFile");
            certfile = (String)props.get("pemCertFile");
            keyfile = (String)props.get("pemKeyFile");
            
            //GRP-1151
            ldapPoolConfig = new LdapPoolConfig();
            
            //lets fix the boolean properties
            //GRP-1196 - vtldap doesnt set pooling properties
            Set<String> propertyNames = new LinkedHashSet<String>((Set<String>)(Object)properties.keySet());
            for (String propertyName : propertyNames) {
              if (StringUtils.equals(propertyName, "edu.vt.middleware.ldap.pool.validatePeriodically")) {
                String value = properties.getProperty(propertyName);
                ldapPoolConfig.setValidatePeriodically(SubjectUtils.booleanValue(value));
                properties.remove(propertyName);
              }
              if (StringUtils.equals(propertyName, "edu.vt.middleware.ldap.pool.validateOnCheckIn")) {
                String value = properties.getProperty(propertyName);
                ldapPoolConfig.setValidateOnCheckIn(SubjectUtils.booleanValue(value));
                properties.remove(propertyName);
              }
              if (StringUtils.equals(propertyName, "edu.vt.middleware.ldap.pool.validateOnCheckOut")) {
                String value = properties.getProperty(propertyName);
                ldapPoolConfig.setValidateOnCheckOut(SubjectUtils.booleanValue(value));
                properties.remove(propertyName);
              }
            }
            
            ldapPoolConfig.setEnvironmentProperties(properties);
         } catch (FileNotFoundException e) {
            log.error("ldap properties not found: " + e, e);
            throw new IllegalArgumentException("Unable to open properties file '" + propertiesFile + "' not found!");
         } catch (IOException e) {
        	 log.error("Unable to load properties from file: " + e, e);
             throw new IllegalArgumentException("Unable to load properties from file: " + e, e);
         }

      } else {

            // load ldap properties from subject.properties

            String url = getNeededProperty(props, "PROVIDER_URL");
            ldapConfig = new LdapConfig(url);
            
            String authtype = props.getProperty("SECURITY_AUTHENTICATION");
            if (authtype==null) authtype = "simple";
            ldapConfig.setAuthtype(authtype);
 
            String principal = props.getProperty("SECURITY_PRINCIPAL");
            if (principal==null) principal = "-missing-";
            ldapConfig.setBindDn(principal);
 
            String creds = props.getProperty("SECURITY_CREDENTIALS");
            if (creds==null) {
              creds = "-missing-";
            } else {
              //maybe its encrypted
              if (!StringUtils.isBlank(creds)) {
                creds = Morph.decryptIfFile(creds);
              }
            }
            ldapConfig.setBindCredential(creds);
 
            String proto = props.getProperty("SECURITY_PROTOCOL");
            if (proto!=null && proto.equals("ssl")) ldapConfig.setSsl(true);
            if (proto!=null && proto.equals("tls")) ldapConfig.setTls(true);
            
      }

      if (cafile!=null && certfile!=null && keyfile!=null) {
         if (log.isDebugEnabled()) {
         log.debug("using the PEM socketfactory: ca=" + cafile + ", cert=" + certfile + ", key=" + keyfile);
         }
         LdapPEMSocketFactory sf = new LdapPEMSocketFactory(cafile, certfile, keyfile);
         SSLSocketFactory ldapSocketFactory = sf.getSocketFactory();
         ldapConfig.setSslSocketFactory(ldapSocketFactory);
      } else {
         log.debug("using the default socketfactory");
      }

      DefaultLdapFactory factory = new DefaultLdapFactory(ldapConfig);
     
      try {

        String ldapValidator = props.getProperty("VTLDAP_VALIDATOR");

        //if we are validating, we need a validator
        // https://code.google.com/p/vt-middleware/wiki/vtldapPooling#Validation
        if (StringUtils.equalsIgnoreCase(ldapValidator, CompareLdapValidator.class.getSimpleName())) {
          
          String validationDn = props.getProperty("VTLDAP_VALIDATOR_COMPARE_DN");
          String validationSearchFilterString = props.getProperty("VTLDAP_VALIDATOR_COMPARE_SEARCH_FILTER_STRING");
          factory.setLdapValidator(
              new CompareLdapValidator(
                validationDn, new SearchFilter(validationSearchFilterString))); // perform a simple compare
          
        } else if (StringUtils.equalsIgnoreCase(ldapValidator, ConnectLdapValidator.class.getSimpleName())) {
          //this is the default, why not
          factory.setLdapValidator(
              new ConnectLdapValidator()); // perform a simple connect

        } else if (!StringUtils.isBlank(ldapValidator)) {
          //get the class
          Class<LdapValidator<Ldap>> validatorClass = SubjectUtils.forName(ldapValidator);
          LdapValidator<Ldap> validator = SubjectUtils.newInstance(validatorClass);
          factory.setLdapValidator(validator);
        }
        
        ldapPool = new SoftLimitLdapPool(ldapPoolConfig != null ? ldapPoolConfig : new LdapPoolConfig(), factory);
         
         
         ldapPool.initialize();
         initialized = true;
      } catch (Exception e) {
         log.error("Error creating ldappool = " + e, e);
      }
      log.debug("ldap initialize done");
   }
    


    /**
     * {@inheritDoc}
     */
    @Override
    public Subject getSubject(String id, boolean exceptionIfNull)
            throws SubjectNotFoundException,SubjectNotUniqueException {
        Subject subject = null;
        Search search = getSearch("searchSubject");
        if (search == null) {
            log.error("searchType: \"searchSubject\" not defined.");
            return subject;
        }
        try {
           Attributes attributes = getLdapUnique( search, id, allAttributeNames   );
           subject = createSubject(attributes);
           if (subject == null && exceptionIfNull) {
               throw new SubjectNotFoundException("Subject " + id + " not found.");
           }
        } catch (SubjectNotFoundException e) {
          if (exceptionIfNull) throw e;
        }
        return subject;
    }
    
    /**
     * {@inheritDoc}
     * @deprecated
     */
    @Deprecated
    @Override
    public Subject getSubject(String id) throws SubjectNotFoundException, SubjectNotUniqueException {
        return this.getSubject(id, true); 
    }


  /**
   * {@inheritDoc}
   */
  @Override
  public Subject getSubjectByIdentifier(String id, boolean exceptionIfNull)
      throws SubjectNotFoundException, SubjectNotUniqueException {
    Map<String, Object> debugLog = null;
    try {
      if (log.isDebugEnabled()) {
        debugLog = new LinkedHashMap<String, Object>();
        debugLog.put("method", "getSubjectByIdentifier");
        debugLog.put("id", id);
        debugLog.put("exceptionIfNull", exceptionIfNull);
      }
      Subject subject = null;
      Search search = getSearch("searchSubjectByIdentifier");
      if (debugLog != null) {
        debugLog.put("search", search);
      }
      
      if (search == null) {
        log.error("searchType: \"searchSubjectByIdentifier\" not defined.");
        return subject;
      }
      if (localDomain != null) {
        int atpos;
        if ((atpos = id.indexOf("@" + localDomain)) > 0) {
          if (log.isDebugEnabled()) {
            log.debug("looking at id=" + id);
          }
          id = id.substring(0, atpos);
          if (log.isDebugEnabled()) {
            log.debug("converted to id=" + id);
          }
          if (debugLog != null) {
            debugLog.put("convertedToId", id);
          }
        }
      }
      try {
        Attributes attributes = getLdapUnique(search, id, allAttributeNames);
        subject = createSubject(attributes);
        if (debugLog != null) {
          debugLog.put("foundSubject", subject != null);
        }
        if (subject == null && exceptionIfNull) {
          throw new SubjectNotFoundException("Subject " + id + " not found.");
        }
      } catch (SubjectNotFoundException e) {
        if (exceptionIfNull) {
          throw e;
        }
        return null;
      }
      search = getSearch("searchSubjectByIdentifierAttributes");
      if (debugLog != null) {
        debugLog.put("searchSubjectByIdentifierAttributesNotNull", search!=null);
      }
      if (search == null)
        ((LdapSubject) subject).setAttributesGotten(true);
      return subject;

    } finally {
      if (log.isDebugEnabled()) {
        log.debug(SubjectUtils.mapToString(debugLog));
      }
    }

  }
    
    /**
     * {@inheritDoc}
     * @deprecated
     */
    @Deprecated
    @Override
    public Subject getSubjectByIdentifier(String id) throws SubjectNotFoundException, SubjectNotUniqueException {
      return this.getSubjectByIdentifier(id, true);
    } 

    /**
     * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#searchPage(java.lang.String)
     */
    @Override
    public SearchPageResult searchPage(String searchValue) {
      return searchHelper(searchValue, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Subject> search(String searchValue) {
      return searchHelper(searchValue, false).getResults();
    }

    /**
     * @param searchValue 
     * @return the set
     */
   private SearchPageResult searchHelper(String searchValue, boolean firstPageOnly) {

     boolean tooManyResults = false;
        Comparator cp = new LdapComparator();
        TreeSet result = new TreeSet(cp);
        Search search = getSearch("search");
        if (search == null) {
            log.error("searchType: \"search\" not defined.");
            return new SearchPageResult(tooManyResults, result);
        }
        Search searchA = getSearch("searchAttributes");
        boolean noAttrSearch = true;
        if (searchA!=null) noAttrSearch = false;

        try {
           Iterator<SearchResult> ldapResults = getLdapResultsHelper(search,searchValue, allAttributeNames, firstPageOnly);
           if (ldapResults == null) {
             return new SearchPageResult(tooManyResults, result);
           }
           while ( ldapResults.hasNext()) {
               //if we are at the end of the page
               if (firstPageOnly && this.maxPage != null && result.size() >= this.maxPage) {
                 tooManyResults = true;
                 break;
               }

               SearchResult si = (SearchResult) ldapResults.next();
               Attributes attributes = si.getAttributes();
               Subject subject = createSubject(attributes);
               
               if (subject != null) {
                 if (noAttrSearch) ((LdapSubject)subject).setAttributesGotten(true);
                 result.add(subject);
               } else {
                log.error("Failed to create subject with attributes: " + attributes);  
               }

               
           }

           if (log.isDebugEnabled()) {
              log.debug("set has " + result.size() + " subjects");
              if (result.size()>0) log.debug("first is " + ((Subject)result.first()).getName());
           }

        } catch (Exception ex) {

           if (throwErrorOnFindAllFailure) {
              throw new SourceUnavailableException(ex.getMessage() + ", source: " + this.getId() + ", sql: " + search.getParam("sql"), ex);
           } else {
              log.error("LDAP Naming Except: " + ex.getMessage() + ", " + this.id + ", " + searchValue, ex);
           }
        }

        return new SearchPageResult(tooManyResults, result);
    }
    
    /**
     * @param attributes
     */
    private Subject createSubject( Attributes attributes) {
        String subjectID = "";

        if (attributes==null) {
           log.error("Ldap createSubject called with null attributes.");
           return (null);
        }
        try {
            Attribute attribute = attributes.get(subjectIDAttributeName);
            if (attribute == null) {
                log.error("No value for LDAP attribute \"" + subjectIDAttributeName + "\". It is Grouper attribute \"SubjectID\".\".  Subject's problematic attributes : " + attributes);
                return null;
            }
            subjectID   = (String)attribute.get();
            if (this.subjectIDFormatToLowerCase) {
            	subjectID = subjectID.toLowerCase();
            }
        } catch (NamingException ex) {
            log.error("LDAP Naming Except: " + ex.getMessage(), ex);
        }
        LdapSubject subject = new LdapSubject(subjectID, null, null, this.getSubjectType().getName(), this.getId(), nameAttributeName, descriptionAttributeName);
 
        // add the attributes

        Map<String, Set<String>> myAttributes = new  SubjectCaseInsensitiveMapImpl<String, Set<String>>();
        try {
            for (NamingEnumeration<?> e = attributes.getAll(); e.hasMore();) {
                Attribute attr = (Attribute) e.next();
                String attrName = attr.getID();
                // skip the basic ones
                //if (attrName.equals(nameAttributeName)) continue;
                //if (attrName.equals(subjectIDAttributeName)) continue;
                //if (attrName.equals(descriptionAttributeName)) continue;
                Set<String> values = new HashSet<String>();
                for (NamingEnumeration<?> en = attr.getAll(); en.hasMore(); ) {
                    Object value =  en.next();
                    values.add(value.toString());
                }
                myAttributes.put(attrName, values);
            }
            subject.setAttributes(myAttributes);
        } catch (NamingException e ) {
            log.error("Naming error: " + e);
        }

        return subject;
    }
    
    protected String getNeededProperty(Properties props, String prop) {
        String value = props.getProperty(prop);
        if (value==null) {
            log.error("Property '" + prop + "' is not defined!");
        }
        return (value);
    }

    
    /**
     * Try to get more attributes for the subject.
     * @param subject
     */
    protected Map<String, Set<String>> getAllAttributes(LdapSubject subject) {
    	Map<String, Set<String>> attributes = new  SubjectCaseInsensitiveMapImpl<String, Set<String>>();
        if (log.isDebugEnabled()) {
          log.debug("getAllAttributes for " + subject.getName());
        }
        Search search = getSearch("searchSubjectAttributes");
        if (search == null) {
            log.debug("searchType: \"searchSubjectAttributes\" not defined.");
            return attributes;
        }

        try {
            Attributes ldapAttributes = getLdapUnique(search,subject.getName(), allAttributeNames);
            for (NamingEnumeration<?> e = ldapAttributes.getAll(); e.hasMore();) {
                Attribute attr = (Attribute) e.next();
                String attrName = attr.getID();

                // special case the basic ones
                //if (attrName.equals(subjectIDAttributeName)) continue;  // already have
                //if (attrName.equals(nameAttributeName)) continue; // already have
                /*if (attrName.equals(descriptionAttributeName)) {
                   subject.setDescription((String)attr.get());
                   //continue;
                }*/
                   
                Set<String> values = new HashSet<String>();
                for (NamingEnumeration<?> en = attr.getAll(); en.hasMore(); ) {
                    Object value =  en.next();
                    values.add(value.toString());
                }
                attributes.put(attrName, values);
            }
            subject.setAttributes(attributes);
        } catch (SubjectNotFoundException ex ) {
            log.error("SubjectNotFound: "+ subject.getId() +" " + ex.getMessage(), ex);
        } catch (SubjectNotUniqueException ex ) {
            log.error("SubjectNotUnique: "+ subject.getId() +" " + ex.getMessage(), ex);
        } catch (NamingException ex ) {
            log.error("LDAP Naming Except: " + ex.getMessage(), ex);
        }
        return attributes;
    }
    
   protected Iterator<SearchResult> getLdapResults(Search search, String searchValue, String[] attributeNames) {
      return getLdapResultsHelper(search, searchValue, attributeNames, false); 
    }
   
    private Iterator<SearchResult> getLdapResultsHelper(Search search, String searchValue, String[] attributeNames, boolean firstPageOnly ) {

      SubjectStatusResult subjectStatusResult = null;
      
      //if this is a search and not by id or identifier
      boolean subjectStatusQuery = StringUtils.equals("search", search.getSearchType());
      if (subjectStatusQuery) {
        //see if we are doing status
        SubjectStatusProcessor subjectStatusProcessor = new SubjectStatusProcessor(searchValue, this.getSubjectStatusConfig());
        subjectStatusResult = subjectStatusProcessor.processSearch();
  
        //strip out status parts
        searchValue = subjectStatusResult.getStrippedQuery();
      }      
      
        Ldap ldap = null;
        String filter = null;
        Iterator<SearchResult> results = null;
        int cp;
        String aff = null;

        if (!initialized) initializeLdap();

        // build a filter
        if ((cp=searchValue.indexOf(',')) >0 ) {
            int lb, rb;
            if ( (lb=searchValue.indexOf('['))>cp && (rb=searchValue.indexOf(']'))>lb ) {
               aff = searchValue.substring(lb+1, rb);
               searchValue = searchValue.substring(0, lb);
               // log.debug("first, last [" + aff + "] search: " + searchValue);
               filter = search.getParam("affiliationfilter");
            } else {
               // log.debug("first, last search: " + searchValue);
               filter = search.getParam("firstlastfilter");
            }
            if (filter==null) filter = search.getParam("filter");  // fall back
            if (filter==null) {
                log.error("Search filter not found for search type:  " + search.getSearchType());
                return null;
            }
            String last = searchValue.substring(0, cp);
            String first = searchValue.substring(cp+1);
            if (last!=null) filter = GrouperClientUtils.replace(filter, "%LAST%", escapeSearchFilter(last));
            if (first!=null) filter = GrouperClientUtils.replace(filter, "%FIRST%", escapeSearchFilter(first));
            if (aff!=null) filter = GrouperClientUtils.replace(filter, "%AFFILIATION%", escapeSearchFilter(aff));
         } else {
            // simple search
            filter = search.getParam("filter");
            if (filter==null) {
                log.error("Search filter not found for search type:  " + search.getSearchType());
                return results;
            }
            filter = GrouperClientUtils.replace(filter, "%TERM%", escapeSearchFilter(searchValue));
        }
        
        String preStatusFilter = filter;
        if (subjectStatusQuery && !subjectStatusResult.isAll() && !StringUtils.isBlank(subjectStatusResult.getDatastoreFieldName())) {
          
          //validate the status value
          if (!subjectStatusResult.getDatastoreValue().matches("[a-zA-Z0-9_-]+")) {
            throw new RuntimeException("Invalid status value: " + subjectStatusResult.getDatastoreValue());
          }
          
          //wrap the query in a status part
          filter = "(&" + filter + "(" + (subjectStatusResult.isEquals()?"":" ! ( ") + subjectStatusResult.getDatastoreFieldName() + "=" 
            + subjectStatusResult.getDatastoreValue() + (subjectStatusResult.isEquals()?"":" ) ") + "))";

        }

        if (!StringUtils.equals(preStatusFilter, filter)) {
          if (log.isDebugEnabled()) {
            log.debug("searchType: " + search.getSearchType() + ", preStatusFilter: " + preStatusFilter + ", filter: " + filter);
          }

        } else {
          if (log.isDebugEnabled()) {
            log.debug("searchType: " + search.getSearchType() + ", filter: " + filter);
          }
          
        }
        
        try  {
            ldap =  (Ldap) ldapPool.checkOut();

            SearchControls searchControls = new SearchControls();
            searchControls.setReturningAttributes(attributeNames);
            
            //if we are at the end of the page
            if (firstPageOnly && this.maxPage != null) {
              searchControls.setCountLimit(this.maxPage+1);
            }
            
            // get params
            String base = search.getParam("base");
            if (base == null) {
            	base = ldap.getLdapConfig().getBaseDn();
            }
                                    
            String scope = search.getParam("scope");                                    
            if (scope!=null) {
               if (scope.equals("OBJECT_SCOPE")) searchControls.setSearchScope(LdapConfig.SearchScope.OBJECT.scope());
               if (scope.equals("ONELEVEL_SCOPE")) searchControls.setSearchScope(LdapConfig.SearchScope.ONELEVEL.scope());
               if (scope.equals("SUBTREE_SCOPE")) searchControls.setSearchScope(LdapConfig.SearchScope.SUBTREE.scope());
            } else {
            	searchControls.setSearchScope(ldap.getLdapConfig().getSearchScope().scope());
            }
                                               
            results = ldap.search(base, new SearchFilter(filter), searchControls);
        } catch (NamingException ex) {
            log.error("Ldap NamingException: " + ex.getMessage(), ex);
            throw new SourceUnavailableException("Ldap NamingException: " + ex.getMessage(), ex);
        } catch (Exception ex) {  // don't know if there are others
            log.error("Ldap Exception: " + ex.getMessage(), ex);
            throw new SourceUnavailableException("Ldap Exception: " + ex.getMessage(), ex);
        } finally {
            if (ldap != null) {
                try {
                    ldapPool.checkIn(ldap);
                } catch (Exception e) {
                    log.error("Could not return Ldap object back to pool", e);
                }
            }
        }
        return results;
    }
    
    
    protected Attributes getLdapUnique( Search search, String searchValue, String[] attributeNames)
                 throws SubjectNotFoundException,SubjectNotUniqueException, SourceUnavailableException  {
      
      
      Map<String, Object> debugLog = null;
      try {
        if (log.isDebugEnabled()) {
          debugLog = new LinkedHashMap<String, Object>();
          debugLog.put("method", "getLdapUnique");
          debugLog.put("search", search);
          debugLog.put("searchValue", searchValue);
          debugLog.put("attributeNames", SubjectUtils.toStringForLog(attributeNames, 200));
        }

        Attributes attributes = null;
        Iterator<SearchResult> results = getLdapResults(search, searchValue, attributeNames);

        if (results == null || !results.hasNext()) {
            String errMsg = "No results: " + search.getSearchType() + " filter:" + search.getParam("filter") + " searchValue: " + searchValue;
            throw new SubjectNotFoundException( errMsg);
        }
            
        SearchResult si = ( SearchResult )results.next( );
        attributes = si.getAttributes();
        
        if (debugLog!=null) {
          debugLog.put("dn", si.getName());
        }

        // Add the DN to the returned attributes.
        attributes.put(new BasicAttribute("dn", si.getName()));
        
        if ( results.hasNext()) {
            si = (SearchResult) results.next();
            if (debugLog!=null) {
              debugLog.put("dn2", si.getName());
            }
            if (!multipleResults) {
              if (debugLog!=null) {
                debugLog.put("searchIsNotUnique", true);
              }
               String errMsg ="Search is not unique:" + si.getName() + "\n";
               throw new SubjectNotUniqueException( errMsg );
            }
            Attributes attr = si.getAttributes();
            NamingEnumeration<? extends Attribute> n = attr.getAll();
          try {
            while (n.hasMore()) {
               Attribute a = n.next();
               if (log.isDebugEnabled()) {
               log.debug("checking attribute " + a.getID());
               }
               if (attributes.get(a.getID())==null) {
                  if (log.isDebugEnabled()) {
                  log.debug("adding " + a.getID());
                  }
                  attributes.put(a);
               }
            }
          } catch (NamingException e) {
              log.error("ldap excp: " + e);
              throw new SourceUnavailableException("Ldap Exception: " + e.getMessage(), e);
          }
          
          // Add the DN to the returned attributes.
          attributes.get("dn").add(si.getName());
        }
        return attributes;

      } finally {
        if (log.isDebugEnabled()) {
          log.debug(SubjectUtils.mapToString(debugLog));
        }
      }

      
    }
    
    /**
     * Escape a search filter to prevent LDAP injection.
     * From http://www.owasp.org/index.php/Preventing_LDAP_Injection_in_Java
     * 
     * @param filter
     * @return escaped filter
     */
    protected String escapeSearchFilter(String filter) {
        //From RFC 2254
        String escapedStr = new String(filter);
        // note, these are regexes, so thats why there is an extra slash
        escapedStr = escapedStr.replaceAll("\\\\","\\\\5c");
        // We want people to be able to use wildcards.
        // escapedStr = escapedStr.replaceAll("\\*","\\\\2a");
        escapedStr = escapedStr.replaceAll("\\(","\\\\28");
        escapedStr = escapedStr.replaceAll("\\)","\\\\29");
        escapedStr = escapedStr.replaceAll("\\"+Character.toString('\u0000'), "\\\\00");
        return escapedStr;
    }
    /**
     * @see edu.internet2.middleware.subject.Source#checkConfig()
     */
    public void checkConfig() {
    }
    /**
     * @see edu.internet2.middleware.subject.Source#printConfig()
     */
    public String printConfig() {
     StringBuilder message = new StringBuilder("subject.properties ldap source id:   ").append(this.getId()).append(": ");
      if (propertiesFile != null) {
        message.append(propertiesFile);
      } else {

        String principal = props.getProperty("SECURITY_PRINCIPAL");
        if (!StringUtils.isBlank(principal)) {
          message.append(principal).append("@");
        }
        message.append(getNeededProperty(props, "PROVIDER_URL"));

      }
      
      return message.toString();
    }
    
	/**
	 * Return the underlying {@link LdapPool}.
	 * 
	 * @return the ldap pool
	 */
	public LdapPool<Ldap> getLdapPool() {
		return ldapPool;
	}

	/**
	 * Set whether or not multiple results are allowed. Primarily for tests.
	 * 
	 * @param multipleResults
	 */
	public void setMultipleResults(boolean multipleResults) {
		this.multipleResults = multipleResults;
	}

    /**
     * max Page size
     * @return the maxPage
     */
    public Integer getMaxPage() {
      return this.maxPage;
    }
}
