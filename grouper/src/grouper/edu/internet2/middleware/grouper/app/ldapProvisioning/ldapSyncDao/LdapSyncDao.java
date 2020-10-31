package edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapConfiguration;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapModificationAttributeError;
import edu.internet2.middleware.grouper.ldap.LdapModificationItem;
import edu.internet2.middleware.grouper.ldap.LdapModificationResult;
import edu.internet2.middleware.grouper.ldap.LdapModificationType;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * access LDAP or dry run or testing
 * @author mchyzer
 *
 */
public abstract class LdapSyncDao {

  /**
   * logger
   */
  private static final Log LOG = GrouperUtil.getLog(LdapSyncDao.class);
      
  /**
   * do a filter search
   * @param ldapPoolName
   * @param baseDn
   * @param filter
   * @param ldapSearchScope
   * @param attributeNames are optional attribute names to get from the ldap object
   * @return the data
   */
  public abstract List<LdapEntry> search(String ldapPoolName, String baseDn, String filter, LdapSearchScope ldapSearchScope, List<String> attributeNames );
  
  /**
   * find objects by dn's
   * @param ldapPoolName
   * @param baseDn
   * @param dnList
   * @param attributeNames are optional attribute names to get from the ldap object
   * @return the data
   */
  public abstract List<LdapEntry> read(String ldapPoolName, String baseDn, List<String> dnList, List<String> attributeNames);
  
  /**
   * delete an object by dn
   * @param ldapPoolName
   * @param dn
   */
  public abstract void delete(String ldapPoolName, String dn);

  /**
   * create an object
   * @param ldapPoolName
   * @param ldapEntry
   * @return true if created, false if existed and updated
   */
  public abstract boolean create(String ldapPoolName, LdapEntry ldapEntry);

  /**
   * Move an object to a new dn.  Assuming this would only be called if it's expected to work.
   * i.e. If the ldap server doesn't allow this, the caller should avoid calling this and instead
   * do a delete/re-create as appropriate.
   * @param ldapPoolName
   * @param oldDn
   * @param newDn
   * @return true if moved, false if newDn exists and oldDn doesn't exist so no update
   */
  public abstract boolean move(String ldapPoolName, String oldDn, String newDn);

  /**
   * Modify attributes for an object.  this should be done in bulk, and if there is an error, should be done individually.
   * @param ldapPoolName
   * @param dn
   * @param ldapModificationItems
   * @return the result
   */
  public final LdapModificationResult modify(String ldapPoolName, String dn, List<LdapModificationItem> ldapModificationItems) {
    // sort - want to make sure the list is in an expected way (sort attributes together and have deletes before adds)
    List<LdapModificationItem> ldapModificationItemsSorted = new ArrayList<LdapModificationItem>();
    Set<String> ldapModificationItemsUniqueAttributes = new LinkedHashSet<String>();
    for (LdapModificationItem ldapModificationItem : ldapModificationItems) {
      ldapModificationItemsUniqueAttributes.add(ldapModificationItem.getAttribute().getName());
    }
    for (String attributeName : ldapModificationItemsUniqueAttributes) {
      LdapAttribute addAttribute = null;
      LdapAttribute removeAttribute = null;
      LdapAttribute replaceAttribute = null;
      for (LdapModificationItem ldapModificationItem : ldapModificationItems) {
        if (ldapModificationItem.getAttribute().getName().equals(attributeName)) {
          if (ldapModificationItem.getLdapModificationType() == LdapModificationType.ADD_ATTRIBUTE) {
            if (replaceAttribute != null) {
              throw new RuntimeException("Not expecting to replace values and add values for the same attribute: " + attributeName);
            }
            
            if (addAttribute == null) {
              addAttribute = new LdapAttribute(attributeName);
            }
            addAttribute.addBinaryValues(ldapModificationItem.getAttribute().getBinaryValues());
            addAttribute.addStringValues(ldapModificationItem.getAttribute().getStringValues());
          } else if (ldapModificationItem.getLdapModificationType() == LdapModificationType.REMOVE_ATTRIBUTE) {
            if (replaceAttribute != null) {
              throw new RuntimeException("Not expecting to replace values and remove values for the same attribute: " + attributeName);
            }
            
            if (removeAttribute != null) {
              if ((removeAttribute.getBinaryValues().size() + removeAttribute.getStringValues().size()) == 0 ||
                  (ldapModificationItem.getAttribute().getBinaryValues().size() + ldapModificationItem.getAttribute().getStringValues().size()) == 0) {
                throw new RuntimeException("Not expecting multiple removes if one is removing all values for the same attribute: " + attributeName);
              }
            }
            
            if (removeAttribute == null) {
              removeAttribute = new LdapAttribute(attributeName);
            }
            removeAttribute.addBinaryValues(ldapModificationItem.getAttribute().getBinaryValues());
            removeAttribute.addStringValues(ldapModificationItem.getAttribute().getStringValues());
          } else if (ldapModificationItem.getLdapModificationType() == LdapModificationType.REPLACE_ATTRIBUTE) {
            if (addAttribute != null || removeAttribute != null) {
              throw new RuntimeException("Not expecting to replace values and add/delete values for the same attribute: " + attributeName);
            }
            
            if (replaceAttribute != null) {
              throw new RuntimeException("Not expecting multiple replaces for the same attribute: " + attributeName);
            }
            
            if (replaceAttribute == null) {
              replaceAttribute = new LdapAttribute(attributeName);
            }
            replaceAttribute.addBinaryValues(ldapModificationItem.getAttribute().getBinaryValues());
            replaceAttribute.addStringValues(ldapModificationItem.getAttribute().getStringValues());
          } else {
            throw new RuntimeException("Unexpected: " + ldapModificationItem.getLdapModificationType());
          }
        }
      }
      
      if (replaceAttribute != null) {
        ldapModificationItemsSorted.add(new LdapModificationItem(LdapModificationType.REPLACE_ATTRIBUTE, replaceAttribute));
      }
      
      if (removeAttribute != null) {
        ldapModificationItemsSorted.add(new LdapModificationItem(LdapModificationType.REMOVE_ATTRIBUTE, removeAttribute));
      }
      
      if (addAttribute != null) {
        ldapModificationItemsSorted.add(new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, addAttribute));
      }
    }
    
    LdapModificationResult result = new LdapModificationResult();
    result.setSuccess(true);  // assume true
    
    // do the bulk with internal_modifyHelper (in batches based on ldap setting)
    List<LdapModificationItem> ldapModificationItemsBatch = new ArrayList<LdapModificationItem>();
    int currentBatchSize[] = { 0 }; // count each value as 1?  count attribute with no values as 1 too?
    int batchSize = LdapConfiguration.getConfig(ldapPoolName).getUpdateBatchSize();
    for (int i = 0; i < ldapModificationItemsSorted.size(); i++) {
      LdapModificationItem ldapModificationItem = ldapModificationItemsSorted.get(i);
      if (ldapModificationItem.getLdapModificationType() == LdapModificationType.ADD_ATTRIBUTE && ldapModificationItem.getAttribute().getValues().size() == 0) {
        continue;
      }
      
      LdapModificationItem ldapModificationItemForBatch = new LdapModificationItem(ldapModificationItem.getLdapModificationType(), new LdapAttribute(ldapModificationItem.getAttribute().getName()));
      ldapModificationItemsBatch.add(ldapModificationItemForBatch);
      if (ldapModificationItem.getAttribute().getValues().size() == 0) {
        currentBatchSize[0]++;
        if (currentBatchSize[0] >= batchSize) {
          processBatch(ldapPoolName, dn, ldapModificationItemsBatch, false, currentBatchSize, result);
        }
      } else if (ldapModificationItem.getLdapModificationType() == LdapModificationType.REPLACE_ATTRIBUTE) {
        // this all has to happen together
        ldapModificationItemForBatch.getAttribute().addValues(ldapModificationItem.getAttribute().getValues());
        currentBatchSize[0] += ldapModificationItem.getAttribute().getValues().size();
        if (currentBatchSize[0] >= batchSize) {
          processBatch(ldapPoolName, dn, ldapModificationItemsBatch, false, currentBatchSize, result);
        }
      } else {
        Iterator<Object> valueIterator = ldapModificationItem.getAttribute().getValues().iterator();
        while (valueIterator.hasNext()) {
          Object value = valueIterator.next();
          ldapModificationItemForBatch.getAttribute().addValue(value);
          currentBatchSize[0]++;
          if (currentBatchSize[0] >= batchSize) {
            // if we are deleting values, we want to make sure we don't end up with zero values if more will be added since some ldaps don't support that
            if (ldapModificationItem.getLdapModificationType() == LdapModificationType.REMOVE_ATTRIBUTE && 
                !valueIterator.hasNext() && ldapModificationItemsSorted.size() > (i+1) && ldapModificationItemsSorted.get(i+1).getLdapModificationType() == LdapModificationType.ADD_ATTRIBUTE &&
                ldapModificationItemsSorted.get(i+1).getAttribute().getName().equals(ldapModificationItem.getAttribute().getName())) {
              LdapModificationItem ldapModificationItemForBatchAdd = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute(ldapModificationItem.getAttribute().getName()));
              Iterator<Object> addValuesIterator = ldapModificationItemsSorted.get(i+1).getAttribute().getValues().iterator();
              Object addValue = addValuesIterator.next();
              addValuesIterator.remove();
              ldapModificationItemForBatchAdd.getAttribute().addValue(addValue);
              ldapModificationItemsBatch.add(ldapModificationItemForBatchAdd);
            }
            processBatch(ldapPoolName, dn, ldapModificationItemsBatch, valueIterator.hasNext(), currentBatchSize, result);
          }
        }
      }
    }
    
    // finish remaining
    if (ldapModificationItemsBatch.size() > 0) {
      processBatch(ldapPoolName, dn, ldapModificationItemsBatch, false, currentBatchSize, result);
    }
            
    return result;
  }
  
  private void processBatch(String ldapPoolName, String dn, List<LdapModificationItem> ldapModificationItemsBatch, boolean attributeHasMoreValues, int[] currentBatchSize, LdapModificationResult result) {
    
    try {
      this.internal_modifyHelper(ldapPoolName, dn, ldapModificationItemsBatch);
    } catch (Exception e) {
      LOG.info("Error during batch update.  Going to try each update individually", e);
      
      // try individually
      Set<String> attributeNames = new LinkedHashSet<String>();
      for (LdapModificationItem item : ldapModificationItemsBatch) {
        if (item.getLdapModificationType() == LdapModificationType.REPLACE_ATTRIBUTE ||
            (item.getLdapModificationType() == LdapModificationType.REMOVE_ATTRIBUTE && item.getAttribute().getValues().size() == 0)) {
          // no need to get the attribute in this case since we wouldn't need to check existing values
        } else {
          attributeNames.add(item.getAttribute().getName());
        }
      }
      
      List<LdapEntry> ldapEntries = this.search(ldapPoolName, dn, "(objectclass=*)", LdapSearchScope.OBJECT_SCOPE, new ArrayList<String>(attributeNames));
      if (ldapEntries.size() == 0) {
        // throw original exception
        throw e;
      }
      
      LdapEntry ldapEntry = ldapEntries.get(0);
      
      for (LdapModificationItem item : ldapModificationItemsBatch) {
        String attributeName = item.getAttribute().getName();
        if (item.getLdapModificationType() == LdapModificationType.REPLACE_ATTRIBUTE ||
            (item.getLdapModificationType() == LdapModificationType.REMOVE_ATTRIBUTE && item.getAttribute().getValues().size() == 0)) {
          // this has to happen together
          try {
            this.internal_modifyHelper(ldapPoolName, dn, Collections.singletonList(item));
          } catch (Exception e2) {
            result.setSuccess(false);
            
            LdapModificationAttributeError attributeError = new LdapModificationAttributeError();
            attributeError.setError(e2);
            attributeError.setLdapModificationItem(item);
            result.addAttributeError(attributeError);
          }
        } else {
          for (Object value : item.getAttribute().getValues()) {
            if ((item.getLdapModificationType() == LdapModificationType.ADD_ATTRIBUTE && !ldapEntry.getAttribute(attributeName).getValues().contains(value)) ||
                (item.getLdapModificationType() == LdapModificationType.REMOVE_ATTRIBUTE && ldapEntry.getAttribute(attributeName).getValues().contains(value))) {
              LdapAttribute newAttributeWithOneValue = new LdapAttribute(attributeName, value);
              LdapModificationItem newLdapModificationItem = new LdapModificationItem(item.getLdapModificationType(), newAttributeWithOneValue);
              try {
                this.internal_modifyHelper(ldapPoolName, dn, Collections.singletonList(newLdapModificationItem));
              } catch (Exception e2) {
                result.setSuccess(false);
                
                LdapModificationAttributeError attributeError = new LdapModificationAttributeError();
                attributeError.setError(e2);
                attributeError.setLdapModificationItem(newLdapModificationItem);
                result.addAttributeError(attributeError);
              }
            }
          }   
        }
      }
    }
    
    currentBatchSize[0] = 0;
    
    if (attributeHasMoreValues) {
      // remove everything but the last.  the last attribute should have values cleared.
      if (ldapModificationItemsBatch.size() > 1) {
        ldapModificationItemsBatch.subList(0, ldapModificationItemsBatch.size() - 2).clear();
      }
      ldapModificationItemsBatch.get(0).getAttribute().clearValues();
    } else {
      ldapModificationItemsBatch.clear();
    }
  }

  /**
   * modify attributes for an object.  this should be done in bulk, and if there is an error, throw it
   * @param ldapPoolName
   * @param dn
   * @param ldapModificationItems
   * @throws Exception if problem
   */
  public abstract void internal_modifyHelper(String ldapPoolName, String dn, List<LdapModificationItem> ldapModificationItems);
}
