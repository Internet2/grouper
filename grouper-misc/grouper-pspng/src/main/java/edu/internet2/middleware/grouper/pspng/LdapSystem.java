package edu.internet2.middleware.grouper.pspng;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.unboundid.ldap.sdk.DN;
import edu.internet2.middleware.morphString.Morph;
import org.apache.commons.lang.StringUtils;
import org.ldaptive.*;
import org.ldaptive.ad.handler.RangeEntryHandler;
import org.ldaptive.control.util.PagedResultsClient;
import org.ldaptive.handler.HandlerResult;
import org.ldaptive.handler.SearchEntryHandler;
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
import static edu.internet2.middleware.grouper.pspng.PspUtils.*;

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

    Properties loggableProperties = new Properties();
    loggableProperties.putAll(ldaptiveProperties);

    for ( String propertyToMask : ENCRYPTABLE_LDAPTIVE_PROPERTIES )
    {
      if ( loggableProperties.containsKey(propertyToMask) )
      {
        loggableProperties.put(propertyToMask, "**masked**");
      }
    }
    
    LOG.info("Setting up LDAP Connection with properties: {}", loggableProperties);

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

  public void log(LdapEntry ldapEntry, String ldapEntryDescriptionFormat, Object... ldapEntryDescriptionArgs)  {
    String ldapEntryDescription;
    if (LOG.isInfoEnabled() || LOG.isDebugEnabled()) {
      ldapEntryDescription = String.format(ldapEntryDescriptionFormat, ldapEntryDescriptionArgs);
    } else {
      return;
    }

    // INFO log is a count of each attribute's values
    if ( LOG.isInfoEnabled() ) {
      StringBuilder sb = new StringBuilder();
      sb.append(String.format("dn=%s|", ldapEntry.getDn()));

      for (LdapAttribute attribute : ldapEntry.getAttributes()) {
        sb.append(String.format("%d %s values|", attribute.size(), attribute.getName()));
      }
      LOG.info("{}: {} Entry Summary: {}", ldapSystemName, ldapEntryDescription, sb.toString());
    }

    LOG.debug("{}: {} Entry Details: {}", ldapSystemName, ldapEntryDescription, ldapEntry);
  }

  public void log( ModifyRequest modifyRequest, String descriptionFormat, Object... descriptionArgs)  {
    String ldapEntryDescription;
    if (LOG.isInfoEnabled() || LOG.isDebugEnabled()) {
      ldapEntryDescription = String.format(descriptionFormat, descriptionArgs);
    } else {
      return;
    }

    // INFO log is a count of each attribute's values
    if ( LOG.isInfoEnabled() ) {
      StringBuilder sb = new StringBuilder();
      sb.append(String.format("dn=%s|", modifyRequest.getDn()));

      for (AttributeModification mod : modifyRequest.getAttributeModifications()) {
        sb.append(String.format("%s %d %s values|",
                mod.getAttributeModificationType(), mod.getAttribute().size(), mod.getAttribute().getName()));
      }
      LOG.info("{}: {} Mod Summary: {}", ldapSystemName, ldapEntryDescription, sb.toString());
    }

    LOG.debug("{}: {} Mod Details: {}", ldapSystemName, ldapEntryDescription, modifyRequest);
  }


  protected void performTestLdapRead(Connection conn) throws PspException {
    LOG.info("{}: Performing test read of directory root", ldapSystemName);
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
      log(searchResultEntry, "Ldap test success");
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
            LOG.info("Setting org.ldaptive.ldapUrl");
            _ldaptiveProperties.put("org.ldaptive.ldapUrl", propValue);
          }
          // tls (vtldap) ==> useStartTls
          if ( propNameTail.equalsIgnoreCase("tls") ) {
            LOG.info("Setting org.ldaptive.useStartTLS");
            _ldaptiveProperties.put("org.ldaptive.useStartTLS", propValue);
          }
          // user (vtldap) ==> bindDn
          if ( propNameTail.equalsIgnoreCase("user") )
          {
            LOG.info("Setting org.ldaptive.bindDn");
            _ldaptiveProperties.put("org.ldaptive.bindDn", propValue);
          }
          // pass (vtldap) ==> bindCredential
          if ( propNameTail.equalsIgnoreCase("pass") )
          {
            LOG.info("Setting org.ldaptive.bindCredential");
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
    log(entryToAdd, "Creating LDAP object");

    Connection conn = getLdapConnection();
    try {
      // Actually ADD the object
      conn.open();
      conn.getProviderConnection().add(new AddRequest(entryToAdd.getDn(), entryToAdd.getAttributes()));
    } catch (LdapException e) {
      if ( e.getResultCode() == ResultCode.ENTRY_ALREADY_EXISTS ) {
        LOG.warn("{}: Skipping LDAP ADD because object already existed: {}", ldapSystemName, entryToAdd.getDn());
      } else {
        LOG.error("{}: Problem while creating new ldap object: {}",
                new Object[] {ldapSystemName, entryToAdd, e});

        throw new PspException("LDAP problem creating object: %s", e.getMessage());
      }
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

  public void performLdapModify(ModifyRequest mod, boolean valuesAreCaseSensitive) throws PspException {
    performLdapModify(mod, valuesAreCaseSensitive,true);
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
  public void performLdapModify(ModifyRequest mod, boolean valuesAreCaseSensitive, boolean retryIfFails) throws PspException {
    log(mod, "Performing ldap mod (%s retry)", retryIfFails ? "with" : "without");

    Connection conn = getLdapConnection();
    try {
      conn.open();
      conn.getProviderConnection().modify(mod);
    } catch (LdapException e) {

      // Abort with Exception if retries are disabled
      if ( !retryIfFails ) {
        throw new PspException("%s: Unrecoverable problem modifying ldap object: %s %s",
                ldapSystemName, mod, e.getMessage());
      }


      LOG.warn("{}: Problem while modifying ldap system based on grouper expectations. Starting to perform adaptive modifications based on data already on server: {}: {}",
              new Object[]{ldapSystemName, mod.getDn(), e.getResultCode()});

      // First case: a single attribute being modified with a single value
      //   Perform a quick ldap comparison and check to see if the object
      //   already matches the modification
      //
      //   If the object doesn't already match, then it was a real ldap failure... there
      //   is no way to simplify it or otherwise retry it
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
          LOG.error("{}: Single-attribute-value Ldap mod-{} failed when Ldap server {} already have {}={}. Mod that failed: {}",
                  ldapSystemName, modification.getAttributeModificationType().toString().toLowerCase(),
                  attributeMatches ? "does" : "does not",
                  modification.getAttribute().getName(), modification.getAttribute().getStringValue(),
                  mod,
                  e);
          throw new PspException("LDAP Modification Failed");
        }
      }

      // This wasn't a single-attribute change, or multiple values were being changed.
      // Therefore: Read what is in the LDAP server and implement the differences


      // Gather up the attributes that were modified so we can read them from server
      Set<String> attributeNames = new HashSet<>();
      for ( AttributeModification attributeMod : mod.getAttributeModifications()) {
        attributeNames.add(attributeMod.getAttribute().getName());
      }

      // Read the current values of those attributes
      LOG.info("{}: Modification retrying... reading object to know what needs to change: {}",
        ldapSystemName, mod.getDn());

      LdapObject currentLdapObject = performLdapRead(mod.getDn(), attributeNames);
      log(currentLdapObject.ldapEntry, "Data already on ldap server");

      // Go back through the requested mods and see if they are redundant
      for ( AttributeModification attributeMod : mod.getAttributeModifications()) {
        String attributeName = attributeMod.getAttribute().getName();

        LOG.info("{}: Summary: Comparing modification of {} to what is already in LDAP: {}/{} Values",
                ldapSystemName,
                attributeName,
                attributeMod.getAttributeModificationType(),
                attributeMod.getAttribute().size());
        LOG.debug("{}: Details: Comparing modification of {} to what is already in LDAP: {}/{}",
                ldapSystemName,
                attributeName,
                attributeMod.getAttributeModificationType(),
                attributeMod.getAttribute());

        Collection<String> currentValues = currentLdapObject.getStringValues(attributeName);
        Collection<String> modifyValues  = attributeMod.getAttribute().getStringValues();

        LOG.info("{}: Comparing Attribute {}. #Values on server already {}. #Values in mod/{}: {}",
          ldapSystemName, attributeName, currentValues.size(), attributeMod.getAttributeModificationType(), modifyValues.size());

        switch (attributeMod.getAttributeModificationType()) {
          case ADD:
            // See if any modifyValues are missing from currentValues
            //
            // Subtract currentValues from modifyValues (case-insensitively)
            Set<String> valuesNotAlreadyOnServer =
                    subtractStringCollections(valuesAreCaseSensitive, modifyValues, currentValues);

            LOG.debug("{}: {}: Values on server: {}",
                    ldapSystemName, attributeName, currentValues);
            LOG.debug("{}: {}: Modify/Add values: {}",
                    ldapSystemName, attributeName, modifyValues);

            LOG.info("{}: {}: Need to add {} values",
                    ldapSystemName, attributeName, valuesNotAlreadyOnServer.size());

            for ( String valueToChange : valuesNotAlreadyOnServer ) {
              performLdapModify( new ModifyRequest( mod.getDn(),
                      new AttributeModification(AttributeModificationType.ADD,
                              new LdapAttribute(attributeName, valueToChange))),
                      valuesAreCaseSensitive,false);
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
            Set<String> valuesStillOnServer
                    = intersectStringCollections(valuesAreCaseSensitive, modifyValues, currentValues);
            LOG.debug("{}: {}: Values on server: {}",
                    ldapSystemName, attributeName, currentValues);
            LOG.debug("{}: {}: Modify/Delete values: {}",
                    ldapSystemName, attributeName, modifyValues);

            LOG.info("{}: {}: {} values need to be REMOVEd",
                    ldapSystemName, attributeName, valuesStillOnServer.size());

            for (String valueToChange : valuesStillOnServer) {
              performLdapModify(new ModifyRequest(mod.getDn(),
                              new AttributeModification(AttributeModificationType.REMOVE,
                                      new LdapAttribute(attributeName, valueToChange))),
                      valuesAreCaseSensitive,false);
            }
            break;

          case REPLACE:
            // See if any differences between modifyValues and currentValues
            // (Subtract in both directions)

            LOG.debug("{}: {}: Values on server: {}",
                    ldapSystemName, attributeName, currentValues);
            LOG.debug("{}: {}: Modify/Replace values: {}",
                    ldapSystemName, attributeName, modifyValues);

            Set<String> extraValuesOnServer =
                    subtractStringCollections(valuesAreCaseSensitive, currentValues, modifyValues);
            LOG.info("{}: REPLACE: {}: {} values still need to be REMOVEd",
                    ldapSystemName, attributeNames, extraValuesOnServer.size());

            for (String valueToChange : extraValuesOnServer) {
              performLdapModify(new ModifyRequest(mod.getDn(),
                              new AttributeModification(AttributeModificationType.REMOVE,
                                      new LdapAttribute(attributeName, valueToChange))),
                      valuesAreCaseSensitive,false);
            }

            Set<String> missingValuesOnServer =
                    subtractStringCollections(valuesAreCaseSensitive, modifyValues, currentValues);

            LOG.info("{}: REPLACE: {}: {} values need to be ADDed",
                    ldapSystemName, attributeName, missingValuesOnServer.size());

            for ( String valueToChange : missingValuesOnServer ) {
              performLdapModify( new ModifyRequest( mod.getDn(),
                              new AttributeModification(AttributeModificationType.ADD,
                                      new LdapAttribute(attributeName, valueToChange))),
                      valuesAreCaseSensitive, false);
            }
        }
      }
    }
    finally {
      conn.close();
    }
  }

  private boolean performLdapComparison(String dn, LdapAttribute attribute) throws PspException {
    LOG.info("{}: Performaing Ldap comparison operation: {} on {}",
            new Object[]{ldapSystemName, attribute, LdapObject.getDnSummary(dn,2)});

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
      if ( isActiveDirectory() ) {
        LOG.info("Active Directory: Searching with Ldap RangeEntryHandler");
        read.setSearchEntryHandlers(new RangeEntryHandler());
      }

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
  protected void performLdapSearchRequest(int approximateNumResultsExpected, SearchRequest request, SearchEntryHandler callback) throws PspException {
    LOG.debug("Doing ldap search: {} / {} / {}", 
        new Object[] {request.getSearchFilter(), request.getBaseDn(), Arrays.toString(request.getReturnAttributes())});
    List<LdapObject> result = new ArrayList<LdapObject>();
    
    Connection conn = getLdapConnection();
    try {
      conn.open();
      
      // Turn on attribute-value paging if this is an active directory target
      if ( isActiveDirectory() ) {
        LOG.debug("Using attribute-value paging");
        request.setSearchEntryHandlers(
                new RangeEntryHandler(),
                new LdapSearchProgressHandler(approximateNumResultsExpected, LOG, "Performing ldap search"),
                callback);
      }
      else {
        LOG.debug("Not using attribute-value paging");
        request.setSearchEntryHandlers(
                new LdapSearchProgressHandler(approximateNumResultsExpected, LOG, "Performing ldap search"),
                callback);
      }
      
      // Perform search. This is slightly different if paging is enabled or not.
      if ( isSearchResultPagingEnabled() ) {
        PagedResultsClient client = new PagedResultsClient(conn, getSearchResultPagingSize());
        LOG.debug("Using ldap search-result paging");
        client.executeToCompletion(request);
      }
      else {
        LOG.debug("Not using ldap search-result paging");
        SearchOperation searchOp = new SearchOperation(conn);
        searchOp.execute(request);
      }
      
    }
    catch (LdapException e) {
      if ( e.getResultCode() == ResultCode.NO_SUCH_OBJECT ) {
        LOG.warn("Search base does not exist: {} (No such object ldap error)", request.getBaseDn());
        return;
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



  public List<LdapObject> performLdapSearchRequest(int approximateNumResultsExpected, String searchBaseDn, SearchScope scope, Collection<String> attributesToReturn, String filterTemplate, Object... filterParams)
  throws PspException {
    SearchFilter filter = new SearchFilter(filterTemplate);

    for (int i=0; i<filterParams.length; i++) {
      filter.setParameter(i, filterParams[i]);
    }

    return performLdapSearchRequest(approximateNumResultsExpected, searchBaseDn, scope, attributesToReturn, filter);
  }


  public List<LdapObject> performLdapSearchRequest(int approximateNumResultsExpected, String searchBaseDn, SearchScope scope, Collection<String> attributesToReturn, SearchFilter filter)
          throws PspException {
    LOG.debug("Running ldap search: <{}>/{}: {} << {}",
            searchBaseDn, scope, filter.getFilter(), filter.getParameters());

    final SearchRequest request = new SearchRequest(searchBaseDn, filter, attributesToReturn.toArray(new String[0]));
    request.setSearchScope(scope);


    final List<LdapObject> result = new ArrayList<>();
    SearchEntryHandler searchCallback = new SearchEntryHandler() {
      @Override
      public HandlerResult<SearchEntry> handle(Connection connection, SearchRequest searchRequest, SearchEntry searchEntry) throws LdapException {
        LOG.debug("Ldap result: {}", searchEntry.getDn());
        result.add(new LdapObject(searchEntry, request.getReturnAttributes()));
        return null;
      }

      @Override
      public void initializeRequest(SearchRequest searchRequest) {

      }
    };
    performLdapSearchRequest(approximateNumResultsExpected, request, searchCallback);

    LOG.info("LDAP search returned {} entries", result.size());

    if ( LOG.isTraceEnabled() ) {
      int i=0;
      for (LdapObject ldapObject : result ) {
        i++;
        LOG.trace("...ldap-search result {} of {}: {}", new Object[]{i, result.size(), ldapObject.getMap()});
      }
    }
    return result;

  }


  public Set<String> performLdapSearchRequest_returningValuesOfAnAttribute(int approximateNumResultsExpected, String searchBaseDn, SearchScope scope, final String attributeToReturn, String filterTemplate, Object... filterParams)
          throws PspException {
    SearchFilter filter = new SearchFilter(filterTemplate);
    LOG.debug("Running ldap search: <{}>/{}: {} << {}",
            new Object[]{searchBaseDn, scope, filterTemplate, Arrays.toString(filterParams)});

    for (int i=0; i<filterParams.length; i++) {
      filter.setParameter(i, filterParams[i]);
    }

    final SearchRequest request = new SearchRequest(searchBaseDn, filter, new String[]{attributeToReturn});
    request.setSearchScope(scope);


    // Create a place to hold the String-only results and a handler to put them into it
    final Set<String> result = new HashSet<>();
    SearchEntryHandler searchCallback = new SearchEntryHandler() {
      @Override
      public HandlerResult<SearchEntry> handle(Connection connection, SearchRequest searchRequest, SearchEntry searchEntry) throws LdapException {

        if ( attributeToReturn.equalsIgnoreCase("dn") || attributeToReturn.equalsIgnoreCase("distinguishedName") ) {
          result.add(searchEntry.getDn().toLowerCase());
        } else {
          LdapAttribute attribute = searchEntry.getAttribute(attributeToReturn);
          if (attribute != null)
            result.addAll(attribute.getStringValues());
        }
        return null;
      }

      @Override
      public void initializeRequest(SearchRequest searchRequest) {

      }
    };

    performLdapSearchRequest(approximateNumResultsExpected, request, searchCallback);

    LOG.info("LDAP search returned {} entries", result.size());

    if ( LOG.isTraceEnabled() ) {
      int i=0;
      for (String attributeValue : result ) {
        i++;
        LOG.trace("...ldap-search result {} of {}: {}", i, result.size(), attributeValue);
      }
    }
    return result;

  }


  public boolean makeLdapObjectCorrect(LdapEntry correctEntry,
                                         LdapEntry existingEntry,
                                       boolean valuesAreCaseSensitive)
          throws PspException
  {
    boolean changedDn = false, changedAttributes = false;

    changedDn = makeLdapDnCorrect(correctEntry, existingEntry);
    if ( changedDn ) {
      LOG.info("{}: Rereading entry after changing DN", ldapSystemName, correctEntry.getDn());

      LdapObject rereadLdapObject = performLdapRead(correctEntry.getDn(), getAttributeNames(existingEntry));

      // this should always be found, but checking just in case
      if ( rereadLdapObject!= null ) {
        existingEntry = rereadLdapObject.ldapEntry;
      }
    }

    changedAttributes = makeLdapDataCorrect(correctEntry, existingEntry, valuesAreCaseSensitive);

    return changedDn || changedAttributes;

/*
    if ( changed ) {
      return fetchTargetSystemGroup(grouperGroupInfo);
    }
    else {
      return existingGroup;
    }
*/
  }


  /**
   * Read a fresh copy of an ldapEntry, using the dn and attribute list from the provided
   * entry.
   *
   * @param ldapEntry Source of DN and attributes that should be read.
   * @return
   * @throws PspException
   */

  public LdapEntry rereadEntry(LdapEntry ldapEntry) throws PspException {
    Collection<String> attributeNames = getAttributeNames(ldapEntry);

    try {
      LOG.debug("{}: Rereading entry {}", ldapSystemName, ldapEntry.getDn());
      LdapObject result = performLdapRead(ldapEntry.getDn(), attributeNames);
      return result.ldapEntry;
    } catch (PspException e) {
      LOG.error("{} Unable to reread ldap object {}", ldapSystemName, ldapEntry.getDn(), e);
      throw e;
    }
  }

  /**
   * Get the names of the attributes present in a given LdapEntry
   * @param ldapEntry
   * @return
   */
  private Collection<String> getAttributeNames(LdapEntry ldapEntry) {
    Collection<String> attributeNames = new HashSet<>();

    for (LdapAttribute attribute : ldapEntry.getAttributes() ) {
      attributeNames.add(attribute.getName());
    }
    return attributeNames;
  }

  protected boolean makeLdapDataCorrect(LdapEntry correctEntry,
                                        LdapEntry existingEntry,
                                        boolean valuesAreCaseSensitive)
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
          LOG.info("{}: Attribute {} is incorrect: {} current values, Correct values: none",
                  correctEntry.getDn(), attributeName,
                  (existingAttribute != null ? existingAttribute.size() : "<none>"));

          AttributeModification mod = new AttributeModification(AttributeModificationType.REMOVE, existingAttribute);
          ModifyRequest modRequest = new ModifyRequest(correctEntry.getDn(), mod);
          performLdapModify(modRequest, valuesAreCaseSensitive);
        }
      }
      else if ( !correctAttribute.equals(existingAttribute) ) {
        // Attribute is different. Update existing one
        changed = true;
        LOG.info("{}: Attribute {} is incorrect: {} Current values, {} Correct values",
                correctEntry.getDn(),
                attributeName,
                (existingAttribute != null ? existingAttribute.size() : "<none>"),
                (correctAttribute  != null ? correctAttribute.size() : "<none>" ));

        AttributeModification mod = new AttributeModification(AttributeModificationType.REPLACE, correctAttribute);
        ModifyRequest modRequest = new ModifyRequest(correctEntry.getDn(), mod);
        performLdapModify(modRequest, valuesAreCaseSensitive);
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
      LOG.debug("{}: DN needs to change to {}", existingDn, correctDn);

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
