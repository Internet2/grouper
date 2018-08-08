package edu.internet2.middleware.grouper.pspng;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.RDN;
import com.unboundid.ldap.sdk.persist.LDAPObject;
import edu.internet2.middleware.morphString.Morph;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ldaptive.*;
import org.ldaptive.ad.handler.RangeEntryHandler;
import org.ldaptive.control.util.PagedResultsClient;
import org.ldaptive.io.LdifReader;
import org.ldaptive.pool.BlockingConnectionPool;
import org.ldaptive.pool.PoolConfig;
import org.ldaptive.pool.PoolException;
import org.ldaptive.pool.SearchValidator;
import org.ldaptive.props.BindConnectionInitializerPropertySource;
import org.ldaptive.props.ConnectionConfigPropertySource;
import org.ldaptive.props.DefaultConnectionFactoryPropertySource;
import org.ldaptive.props.PoolConfigPropertySource;
import org.ldaptive.props.SearchRequestPropertySource;
import org.ldaptive.sasl.GssApiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * This class encapsulates an LDAP system configured by a collection of
 * properties defined withing grouper-loader.properties
 * @author bert
 *
 */
public class LdapSystem {
  private static final Logger LOG = LoggerFactory.getLogger(LdapSystem.class);

  // What ldaptive properties will be decrypted if their values are Morph files?
  // (We don't decrypt all properties because that would prevent the use of slashes in the property values)
  public static final String ENCRYPTABLE_LDAPTIVE_PROPERTIES[]
          = new String[]{"org.ldaptive.bindCredential"};

  public final String ldapSystemName;
  protected Properties _ldaptiveProperties = new Properties();
  
  private final boolean isActiveDirectory;
  private BlockingConnectionPool ldapPool;

  protected boolean searchResultPagingEnabled_defaultValue = true;
  protected int searchResultPagingSize_default_value = 100;

  
  public static boolean attributeHasNoValues(final LdapAttribute attribute) {
    if ( attribute == null ) {
      return true;
    }

    Collection<String> values = attribute.getStringValues();

    return values.size() == 0  || values.iterator().next().length() == 0;
  }


  public LdapSystem(String ldapSystemName, boolean isActiveDirectory) {
    this.ldapSystemName = ldapSystemName;
    this.isActiveDirectory = isActiveDirectory;
    getLdaptiveProperties();
  }

  
  private BlockingConnectionPool buildLdapConnectionPool() throws PspException {
    BlockingConnectionPool result;
  
    LOG.info("{}: Creating LDAP Pool", ldapSystemName);
    Properties ldaptiveProperties = getLdaptiveProperties();
    
    LOG.info("Setting up LDAP Connection with properties: {}", ldaptiveProperties);
    // Setup ldaptive ConnectionConfig
    ConnectionConfig connConfig = new ConnectionConfig();
    ConnectionConfigPropertySource ccpSource = new ConnectionConfigPropertySource(connConfig, ldaptiveProperties);
    ccpSource.initialize();
  
    //GrouperLoaderLdapServer grouperLoaderLdapProperties 
    //  = GrouperLoaderConfig.retrieveLdapProfile(ldapSystemName);
    
    /////////////
    // Binding
    BindConnectionInitializer binder = new BindConnectionInitializer();
  
    BindConnectionInitializerPropertySource bcip = new BindConnectionInitializerPropertySource(binder, ldaptiveProperties);
    bcip.initialize();
  
    // I'm not sure if SaslRealm and/or SaslAuthorizationId can be used independently
    // Therefore, we'll initialize gssApiConfig when either one of them is used.
    // And, then, we'll attach the gssApiConfig to the binder if there is a gssApiConfig
    GssApiConfig gssApiConfig = null;
    String val = (String) ldaptiveProperties.get("org.ldaptive.saslRealm");
    if (!StringUtils.isBlank(val)) {
      LOG.info("Processing saslRealm");
      if ( gssApiConfig == null )
        gssApiConfig = new GssApiConfig();
      gssApiConfig.setRealm(val);
      
    }
    
    val = (String) ldaptiveProperties.get("org.ldaptive.saslAuthorizationId");
    if (!StringUtils.isBlank(val)) {
      LOG.info("Processing saslAuthorizationId");
      if ( gssApiConfig == null )
        gssApiConfig = new GssApiConfig();
      gssApiConfig.setAuthorizationId(val);
    }
  
    // If there was a sasl/gssapi attribute, then save the gssApiConfig
    if ( gssApiConfig != null ) {
      LOG.info("Setting gssApiConfig");
      binder.setBindSaslConfig(gssApiConfig);
    }
    
    DefaultConnectionFactory connectionFactory = new DefaultConnectionFactory();
    DefaultConnectionFactoryPropertySource dcfSource = new DefaultConnectionFactoryPropertySource(connectionFactory, ldaptiveProperties);
    dcfSource.initialize();

    // Test the ConnectionFactory before error messages are buried behind the pool
    Connection conn = connectionFactory.getConnection();
    performTestLdapRead(conn);
    
    /////////////
    // PoolConfig
    
    PoolConfig ldapPoolConfig = new PoolConfig();
    PoolConfigPropertySource pcps = new PoolConfigPropertySource(ldapPoolConfig, ldaptiveProperties);
    pcps.initialize();

    // Make sure some kind of validation is turned on
    if ( !ldapPoolConfig.isValidateOnCheckIn() &&
         !ldapPoolConfig.isValidateOnCheckOut() &&
         !ldapPoolConfig.isValidatePeriodically() ) {
      LOG.debug("{}: Using default onCheckOut ldap-connection validation", ldapSystemName);
      ldapPoolConfig.setValidateOnCheckOut(true);
    }
      
    result = new BlockingConnectionPool(ldapPoolConfig, connectionFactory);
    result.setValidator(new SearchValidator());
    result.initialize();
    
    ////////////
    // Test the connection obtained from pool
    try {
      conn = result.getConnection();
      performTestLdapRead(conn);
    } catch (LdapException e) {
      LOG.error("Problem while testing ldap pool", e);
      throw new PspException("Problem testing ldap pool: %s", e.getMessage());
    }
    
    
    return result;
  }


  protected void performTestLdapRead(Connection conn) throws PspException {
    LOG.info("Performing test read of directory root");
    SearchExecutor searchExecutor = new SearchExecutor();
    SearchRequestPropertySource srSource = new SearchRequestPropertySource(searchExecutor, getLdaptiveProperties());
    srSource.initialize();

    SearchRequest read = new SearchRequest("", "objectclass=*");
    read.setSearchScope(SearchScope.OBJECT);

    // Turn on attribute-value paging if this is an active directory target
    if ( isActiveDirectory() )
      read.setSearchEntryHandlers(new RangeEntryHandler());
 
    try {
      conn.open();
      SearchOperation searchOp = new SearchOperation(conn);
      
      Response<SearchResult> response = searchOp.execute(read);
      SearchResult searchResult = response.getResult();
    
      LdapEntry searchResultEntry = searchResult.getEntry();
      LOG.info("Search success: " + searchResultEntry.getAttributes());
    }
    catch (LdapException e) {
      LOG.error("Ldap problem",e);
      throw new PspException("Problem testing ldap connection: %s", e.getMessage());
    }
    finally {
      conn.close();
    }
  }

  
  
  public BlockingConnectionPool getLdapPool() throws PspException {
    if ( ldapPool != null )
      return ldapPool;
    
    // We don't have a pool setup yet. Synchronize so we're sure we only make one pool.
    synchronized(this) {
      // Check if another thread has created our pool while we were waiting for the semaphore
      if ( ldapPool != null )
        return ldapPool;
      
     ldapPool = buildLdapConnectionPool();
    }
  
    return ldapPool;
  }

  
  
  public boolean isActiveDirectory() {
    return isActiveDirectory;
  }

  
  
  public Properties getLdaptiveProperties() {
    if ( _ldaptiveProperties.size() == 0 ) {
      String ldapPropertyPrefix = "ldap." + ldapSystemName.toLowerCase() + ".";
      
      for (String propName : GrouperLoaderConfig.retrieveConfig().propertyNames()) {
        if ( propName.toLowerCase().startsWith(ldapPropertyPrefix) ) {
          String propValue = GrouperLoaderConfig.retrieveConfig().propertyValueString(propName, "");
          
          // Get the part of the property after ldapPropertyPrefix 'ldap.person.'
          String propNameTail = propName.substring(ldapPropertyPrefix.length());
          _ldaptiveProperties.put("org.ldaptive." + propNameTail, propValue);
          
          // Some compatibility between old vtldap properties and ldaptive versions
          // url (vtldap) ==> ldapUrl
          if ( propNameTail.equalsIgnoreCase("url") ) {
            LOG.info("Setting org.ldaptive.ldapUrl for compatibility with vt-ldap");
            _ldaptiveProperties.put("org.ldaptive.ldapUrl", propValue);
          }
          // tls (vtldap) ==> useStartTls
          if ( propNameTail.equalsIgnoreCase("tls") ) {
            LOG.info("Setting org.ldaptive.useStartTLS for compatibility with vt-ldap");
            _ldaptiveProperties.put("org.ldaptive.useStartTLS", propValue);
          }
          // user (vtldap) ==> bindDn
          if ( propNameTail.equalsIgnoreCase("user") )
          {
            LOG.info("Setting org.ldaptive.bindDn for compatibility with vt-ldap");
            _ldaptiveProperties.put("org.ldaptive.bindDn", propValue);
          }
          // pass (vtldap) ==> bindCredential
          if ( propNameTail.equalsIgnoreCase("pass") )
          {
            LOG.info("Setting org.ldaptive.bindCredential for compatibility with vt-ldap");
            _ldaptiveProperties.put("org.ldaptive.bindCredential", propValue);
          }
        }
      }
    }

    // Go through the properties that can be encrypted and decrypt them if they're Morph files
    for (String encryptablePropertyKey : ENCRYPTABLE_LDAPTIVE_PROPERTIES) {
      String value = _ldaptiveProperties.getProperty(encryptablePropertyKey);
      value = Morph.decryptIfFile(value);
      _ldaptiveProperties.put(encryptablePropertyKey, value);
    }
    return _ldaptiveProperties;
  }

  
  
  public int getSearchResultPagingSize() {
    Object searchResultPagingSize = getLdaptiveProperties().get("org.ldaptive.searchResultPagingSize");
    
    return GrouperUtil.intValue(searchResultPagingSize, searchResultPagingSize_default_value);
  }

  
  
  public boolean isSearchResultPagingEnabled() {
    Object searchResultPagingEnabled = getLdaptiveProperties().get("org.ldaptive.searchResultPagingEnabled");
    
    return GrouperUtil.booleanValue(searchResultPagingEnabled, searchResultPagingEnabled_defaultValue);
  }

  
  
  protected Connection getLdapConnection() throws PspException {
    BlockingConnectionPool pool = getLdapPool();
    try {
      Connection conn = pool.getConnection();
      return conn;
    } catch (PoolException e) {
      LOG.error("LDAP Pool Exception", e);
      throw new PspException("Problem connecting to ldap server %s: %s",pool, e.getMessage());
    }
  }
  
  

  /**
   * Returns ldaptive search executor configured according to properties
   * @return
   */
  public SearchExecutor getSearchExecutor() {
    SearchExecutor searchExecutor = new SearchExecutor();
    SearchRequestPropertySource srSource = new SearchRequestPropertySource(searchExecutor, getLdaptiveProperties());
    srSource.initialize();
    
    return searchExecutor;
  }

  
  
  protected void performLdapAdd(LdapEntry entryToAdd) throws PspException {
    LOG.info("{}: Creating LDAP object: {}", ldapSystemName, entryToAdd.getDn());
  
    Connection conn = getLdapConnection();
    try {
      // Actually ADD the account
      conn.open();
      conn.getProviderConnection().add(new AddRequest(entryToAdd.getDn(), entryToAdd.getAttributes()));
    } catch (LdapException e) {
      LOG.error("Problem while creating new ldap object: {}", entryToAdd, e);
      throw new PspException("LDAP problem creating object: %s", e.getMessage());
    }
    finally {
      conn.close();
    }
  
  }
  
  

  protected void performLdapDelete(String dnToDelete) throws PspException {
    LOG.info("{}: Deleting LDAP object: {}", ldapSystemName, dnToDelete);
    
    Connection conn = getLdapConnection();
    try {
      // Actually DELETE the account
      conn.open();
      conn.getProviderConnection().delete(new DeleteRequest(dnToDelete));
    } catch (LdapException e) {
      LOG.error("Problem while deleting ldap object: {}", dnToDelete, e);
      throw new PspException("LDAP problem deleting object: %s", e.getMessage());
    }
    finally {
      conn.close();
    }
  
  }

  public void performLdapModify(ModifyRequest mod) throws PspException {
    performLdapModify(mod, true);
  }

  /**
   * This performs a modification and optionally retries it by comparing attributeValues
   * being added/removed to those already on the ldap server
   * @param mod
   * @param retryIfFails Should the Modify be retried if something goes wrong. This retry
   *                     will do attributeValue-by-attributeValue comparison to
   *                     make the retry as safe as possible
   * @throws PspException
   */
  public void performLdapModify(ModifyRequest mod, boolean retryIfFails) throws PspException {
    LOG.info("{}: Performing Ldap modification: {}", ldapSystemName, mod);

    Connection conn = getLdapConnection();
    try {
      conn.open();
      conn.getProviderConnection().modify(mod);
    } catch (LdapException e) {

      // Abort with Exception if retries are disabled
      if ( !retryIfFails ) {
        throw new PspException("%s: (probably repeated) LDAP problem modifying ldap object: %s %s",
                ldapSystemName, mod, e.getMessage());
      }

      // First case: a single attribute being modified with a single value
      //   Perform a quick ldap comparison and heck to see if the object
      //   already matches the modification
      //
      //   If the object doesn't already match, then it was a real ldap failure
      if ( mod.getAttributeModifications().length == 1 &&
           mod.getAttributeModifications()[0].getAttribute().getStringValues().size() == 1 ) {
        AttributeModification modification = mod.getAttributeModifications()[0];

        boolean attributeMatches = performLdapComparison(mod.getDn(), modification.getAttribute());

        if ( attributeMatches && modification.getAttributeModificationType() == AttributeModificationType.ADD ) {
          LOG.info("{}: Change not necessary: System already had attribute value", ldapSystemName);
          return;
        }
        else if ( !attributeMatches && modification.getAttributeModificationType() == AttributeModificationType.REMOVE ) {
          LOG.info("{}: Change not necessary: System already had attribute value removed", ldapSystemName);
          return;
        }
        else {
          LOG.error("{}: Ldap modification failed", ldapSystemName, e);
          throw new PspException("LDAP Modification Failed");
        }
      }

      // This wasn't a single-attribute change, or multiple values were being changed.
      // Therefore: Read what is in the LDAP server and implement the differences

      LOG.warn("{}: Problem while modifying ldap system based on grouper expectations. Starting to perform adaptive modifications based on data already on server: {}: {}",
              new Object[]{ldapSystemName, mod, e.getResultCode()});


      // Gather up the attributes that were modified so we can read them from server
      Set<String> attributeNames = new HashSet<>();
      for ( AttributeModification attributeMod : mod.getAttributeModifications()) {
        attributeNames.add(attributeMod.getAttribute().getName());
      }

      // Read the current values of those attributes
      LOG.info("{}: Modification retrying... reading object to know what needs to change: {}",
        ldapSystemName, mod.getDn());

      LdapObject currentLdapObject = performLdapRead(mod.getDn(), attributeNames);

      // Go back through the requested mods and see if they are redundant
      for ( AttributeModification attributeMod : mod.getAttributeModifications()) {
        LOG.info("{}: Comparing modification to what is already in LDAP: {}", ldapSystemName, attributeMod);

        String attributeName = attributeMod.getAttribute().getName();

        Collection<String> currentValues = currentLdapObject.getStringValues(attributeName);
        Collection<String> modifyValues  = attributeMod.getAttribute().getStringValues();

        switch (attributeMod.getAttributeModificationType()) {
          case ADD:
            // See if any modifyValues are missing from currentValues
            //
            // Subtract currentValues from modifyValues (case-insensitively)
            Set<String> valuesNotAlreadyOnServer = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            valuesNotAlreadyOnServer.addAll(modifyValues);
            valuesNotAlreadyOnServer.removeAll(currentValues);

            LOG.info("{}: {} {} values still need to be ADDed",
                    new Object[] {ldapSystemName, valuesNotAlreadyOnServer.size(), attributeName});

            for ( String valueToChange : valuesNotAlreadyOnServer ) {
              performLdapModify( new ModifyRequest( mod.getDn(),
                      new AttributeModification(AttributeModificationType.ADD,
                              new LdapAttribute(attributeName, valueToChange))),
                      false);
            }
            break;

          case REMOVE:
            // For Mod.REMOVE, not specifying any values means to remove them all
            if ( modifyValues.size() == 0 ) {
              modifyValues.addAll(currentValues);
            }

            // See if any modifyValues are still in currentValues
            //
            // Intersect modifyValues and currentValues
            Set<String> valuesStillOnServer = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            valuesStillOnServer.addAll(modifyValues);
            valuesStillOnServer.retainAll(currentValues);
            LOG.info("{}: {} {} values still need to be REMOVEd",
                    new Object[] {ldapSystemName, valuesStillOnServer.size(), attributeName});

            for (String valueToChange : valuesStillOnServer) {
              performLdapModify(new ModifyRequest(mod.getDn(),
                              new AttributeModification(AttributeModificationType.REMOVE,
                                      new LdapAttribute(attributeName, valueToChange))),
                      false);
            }
            break;

          case REPLACE:
            // See if any differences between modifyValues and currentValues
            // (Subtract in both directions)
            Set<String> extraValuesOnServer = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            extraValuesOnServer.addAll(currentValues);
            extraValuesOnServer.retainAll(modifyValues);
            LOG.info("{}: REPLACE: {} {} values still need to be REMOVEd",
                    new Object[] {ldapSystemName, extraValuesOnServer.size(), attributeName});

            for (String valueToChange : extraValuesOnServer) {
              performLdapModify(new ModifyRequest(mod.getDn(),
                              new AttributeModification(AttributeModificationType.REMOVE,
                                      new LdapAttribute(attributeName, valueToChange))),
                      false);
            }

            Set<String> missingValuesOnServer = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            missingValuesOnServer.addAll(modifyValues);
            missingValuesOnServer.retainAll(currentValues);
            LOG.info("{}: REPLACE: {} {} values still need to be ADDed",
                    new Object[] {ldapSystemName, missingValuesOnServer.size(), attributeName});

            for ( String valueToChange : missingValuesOnServer ) {
              performLdapModify( new ModifyRequest( mod.getDn(),
                              new AttributeModification(AttributeModificationType.ADD,
                                      new LdapAttribute(attributeName, valueToChange))),
                      false);
            }
        }
      }
    }
    finally {
      conn.close();
    }
  }

  private boolean performLdapComparison(String dn, LdapAttribute attribute) throws PspException {
    LOG.info("{}: Performaing Ldap comparison operation: {}", ldapSystemName, attribute);

    Connection conn = getLdapConnection();
    try {
      try {
        conn.open();
        CompareOperation compare = new CompareOperation(conn);

        boolean result = compare.execute(new CompareRequest(dn, attribute)).getResult();
        return result;

      } catch (LdapException ldapException) {
        ResultCode resultCode = ldapException.getResultCode();

        // A couple errors mean that object does not match attribute values
        if (resultCode == ResultCode.NO_SUCH_OBJECT || resultCode == ResultCode.NO_SUCH_ATTRIBUTE) {
          return false;
        } else {
          LOG.error("{}: Error performing compare operation: {}",
                  new Object[]{ldapSystemName, attribute, ldapException});

          throw new PspException("LDAP problem performing ldap comparison: %s", ldapException.getMessage());
        }
      }
    }
    finally {
      conn.close();
    }

  }


  void performLdapModifyDn(ModifyDnRequest mod) throws PspException {
    LOG.info("{}: Performing Ldap mod-dn operation: {}", ldapSystemName, mod);

    Connection conn = getLdapConnection();
    try {
      conn.open();
      conn.getProviderConnection().modifyDn(mod);
    } catch (LdapException e) {
      LOG.error("Problem while modifying dn of ldap object: {}", mod, e);
      throw new PspException("LDAP problem modifying dn of ldap object: %s", e.getMessage());
    }
    finally {
      conn.close();
    }
  }




  protected LdapObject performLdapRead(DN dn, String... attributes) throws PspException {
    return performLdapRead(dn.toMinimallyEncodedString(), attributes);
  }
  
  protected LdapObject performLdapRead(String dn, Collection<String> attributes) throws PspException {
    return performLdapRead(dn, attributes.toArray(new String[0]));
  }

  protected LdapObject performLdapRead(String dn, String... attributes) throws PspException {
    LOG.debug("Doing ldap read: {} attributes {}", dn, Arrays.toString(attributes));
    
    Connection conn = getLdapConnection();
    try {
      conn.open();
  
      SearchRequest read = new SearchRequest(dn, "objectclass=*");
      read.setSearchScope(SearchScope.OBJECT);
      read.setReturnAttributes(attributes);
      
      // Turn on attribute-value paging if this is an active directory target
      if ( isActiveDirectory() )
        read.setSearchEntryHandlers(new RangeEntryHandler());
  
      SearchOperation searchOp = new SearchOperation(conn);
      
      Response<SearchResult> response = searchOp.execute(read);
      SearchResult searchResult = response.getResult();
      
      LdapEntry result = searchResult.getEntry();
      
      if ( result == null ) {
        LOG.debug("{}: Object does not exist: {}", ldapSystemName, dn);
        return null;
      } else {
        LOG.debug("{}: Object does exist: {}", ldapSystemName, dn);
        return new LdapObject(result, attributes);
      }
    }
    catch (LdapException e) {
      if ( e.getResultCode() == ResultCode.NO_SUCH_OBJECT ) {
        LOG.warn("{}: Ldap object does not exist: '{}'", ldapSystemName, dn);
        return null;
      }
      
      LOG.error("Problem during ldap read {}", dn, e);
      throw new PspException("Problem during LDAP read: %s", e.getMessage());
    }
    finally {
      if ( conn != null )
        conn.close();
    }
  }

  /**
   * 
   * @param request
   *
   * @return
   * @throws LdapException
   */
  protected List<LdapObject> performLdapSearchRequest(SearchRequest request) throws PspException {
    LOG.debug("Doing ldap search: {} / {} / {}", 
        new Object[] {request.getSearchFilter(), request.getBaseDn(), Arrays.toString(request.getReturnAttributes())});
    List<LdapObject> result = new ArrayList<LdapObject>();
    
    Connection conn = getLdapConnection();
    try {
      conn.open();
      
      // Turn on attribute-value paging if this is an active directory target
      if ( isActiveDirectory() ) {
        LOG.debug("Using attribute-value paging");
        request.setSearchEntryHandlers(new RangeEntryHandler());
      }
      
      Response<SearchResult> response;
      
      // Perform search. This is slightly different if paging is enabled or not. 
      if ( isSearchResultPagingEnabled() ) {
        PagedResultsClient client = new PagedResultsClient(conn, getSearchResultPagingSize());
        LOG.debug("Using ldap search-result paging");
        response = client.executeToCompletion(request);
      }
      else {
        LOG.debug("Not using ldap search-result paging");
        SearchOperation searchOp = new SearchOperation(conn);
        response = searchOp.execute(request);
      }
      
      SearchResult searchResult = response.getResult();
      for (LdapEntry entry : searchResult.getEntries()) {
        result.add(new LdapObject(entry, request.getReturnAttributes()));
      }

      LOG.info("LDAP search returned {} entries", result.size());

      if ( LOG.isDebugEnabled() ) {
        int i=0;
        for (LdapObject ldapObject : result ) {
          i++;
          LOG.debug("...ldap-search result {} of {}: {}", new Object[]{i, result.size(), ldapObject.getMap()});
        }
      }
      return result;
    }
    catch (LdapException e) {
      if ( e.getResultCode() == ResultCode.NO_SUCH_OBJECT ) {
        LOG.warn("Search base does not exist: {} (No such object ldap error)", request.getBaseDn());
        return Collections.EMPTY_LIST;
      }
      
      LOG.error("Problem during ldap search {}", request, e);
      throw new PspException("LDAP problem while searching: " + e.getMessage());
    }
    catch (RuntimeException e) {
      LOG.error("Runtime problem during ldap search {}", request, e);
      throw e;
    }
    finally {
      if ( conn != null )
        conn.close();
    }
  }

  
  
  protected List<LdapObject> performLdapSearchRequest(String searchBaseDn, SearchScope scope, Collection<String> attributesToReturn, String filterTemplate, Object... filterParams) 
  throws PspException {
    SearchFilter filter = new SearchFilter(filterTemplate);
    LOG.debug("Running ldap search: <{}>/{}: {} << {}", 
        new Object[]{searchBaseDn, scope, filterTemplate, Arrays.toString(filterParams)});
    
    for (int i=0; i<filterParams.length; i++) {
      filter.setParameter(i, filterParams[i]);
    }
    
    SearchRequest request = new SearchRequest(searchBaseDn, filter, attributesToReturn.toArray(new String[0]));
    request.setSearchScope(scope);
    return performLdapSearchRequest(request);
  }



  public boolean makeLdapObjectCorrect(LdapEntry correctEntry,
                                         LdapEntry existingEntry)
          throws PspException
  {
    boolean changed = false;

    changed = makeLdapDnCorrect(correctEntry, existingEntry);
    changed = makeLdapDataCorrect(correctEntry, existingEntry) || changed;

    return changed;

/*
    if ( changed ) {
      return fetchTargetSystemGroup(grouperGroupInfo);
    }
    else {
      return existingGroup;
    }
*/
  }

  protected boolean makeLdapDataCorrect(LdapEntry correctEntry,
                                        LdapEntry existingEntry)
        throws PspException
  {
    boolean changed = false ;
    for ( String attributeName : correctEntry.getAttributeNames() ) {
      LdapAttribute correctAttribute = correctEntry.getAttribute(attributeName);
      if ( attributeHasNoValues(correctAttribute) ) {
        correctAttribute = null;
      }

      LdapAttribute existingAttribute= existingEntry.getAttribute(attributeName);

      // If there should not be any values for this attribute, delete any existing values
      if ( correctAttribute == null ) {
        if ( existingAttribute != null ) {
          changed = true;
          AttributeModification mod = new AttributeModification(AttributeModificationType.REMOVE, existingAttribute);
          ModifyRequest modRequest = new ModifyRequest(correctEntry.getDn(), mod);
          performLdapModify(modRequest);
        }
      }
      else if ( !correctAttribute.equals(existingAttribute) ) {
        // Attribute is different. Update existing one
        changed = true;

        AttributeModification mod = new AttributeModification(AttributeModificationType.REPLACE, correctAttribute);
        ModifyRequest modRequest = new ModifyRequest(correctEntry.getDn(), mod);
        performLdapModify(modRequest);
      }
    }
    return changed;
  }

  /**
   * Moves the ldap object if necessary. It does require the OU to already exist because
   * OU templates and OU caching would make OU-creation here too intertwined with
   * the provisioning objects
   *
   * @param correctEntry
   * @param existingEntry
   * @return
   * @throws PspException
   */
  protected boolean makeLdapDnCorrect(LdapEntry correctEntry, LdapEntry existingEntry) throws PspException {
    // Compare DNs
    String correctDn = correctEntry.getDn();
    String existingDn= existingEntry.getDn();

    // TODO: This should do case-sensitive comparisons of the first RDN and case-insensitive comparisons of the rest
    if ( !correctDn.equalsIgnoreCase(existingDn) ) {
      // The DNs do not match. Existing object needs to be moved

      // Now modify the DN
      ModifyDnRequest moddn = new ModifyDnRequest(existingDn, correctDn);
      moddn.setDeleteOldRDn(true);

      performLdapModifyDn(moddn);
      return true;
    }
    return false;
  }


  public boolean test() {
    String ldapUrlString = (String) getLdaptiveProperties().get("org.ldaptive.ldapUrl");
    if ( ldapUrlString == null ) {
      LOG.error("Could not find LDAP URL");
      return false;
    }
    
    LOG.info("LDAP Url: " + ldapUrlString);
    
    if ( !ldapUrlString.startsWith("ldaps") ) {
      LOG.warn("Not an SSL ldap url");
    }
    else {        
      LOG.info("Testing SSL before the LDAP test");
      try {
        // ldaps://host[:port]...
        Pattern urlPattern = Pattern.compile("ldaps://([^:]*)(:[0-9]+)?.*");
        Matcher m = urlPattern.matcher(ldapUrlString);
        if ( !m.matches() ) {
          LOG.error("Unable to parse ldap url: " + ldapUrlString);
          return false;
        }
        
        String host = m.group(1);
        String portString = m.group(2);
        int port;
        if ( portString == null || portString.length() == 0 ) {
          port=636;
        }
        else {
          port=Integer.parseInt(portString.substring(1));
        }
        
        LOG.info("  Making SSL connection to {}:{}", host, port);
        
        SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(host, port);

        InputStream in = sslsocket.getInputStream();
        OutputStream out = sslsocket.getOutputStream();

        // Write a test byte to get a reaction :)
        out.write(1);

        while (in.available() > 0) {
            System.out.print(in.read());
        }
        LOG.info("Successfully connected");

      } catch (Exception exception) {
          exception.printStackTrace();
      }
    }
    
    try {
      BlockingConnectionPool pool = buildLdapConnectionPool();
      LOG.info("Success: Ldap pool built");

      performTestLdapRead(pool.getConnection());
      LOG.info("Success: Test ldap read");
      return true;
    }
    catch (LdapException e) {
      LOG.error("LDAP Failure",e);
      return false;
    }
    catch (PspException e) {
      LOG.error("LDAP Failure",e);
      return false;
    }
  }
  
  public static void main(String[] args) {
    if ( args.length != 1 ) {
      LOG.error("USAGE: <ldap-pool-name from grouper-loader.properties>");
      System.exit(1);
    }
   
    LOG.info("Starting LDAP-connection test");
    LdapSystem system = new LdapSystem(args[0], false);
    system.test();
  }
}
