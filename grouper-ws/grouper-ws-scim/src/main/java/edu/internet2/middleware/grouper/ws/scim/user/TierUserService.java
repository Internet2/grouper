package edu.internet2.middleware.grouper.ws.scim.user;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.ws.scim.TierFilter;
import edu.internet2.middleware.grouper.ws.scim.TierMetaExtension;
import edu.internet2.middleware.subject.Subject;
import edu.psu.swe.scim.server.exception.UnableToCreateResourceException;
import edu.psu.swe.scim.server.exception.UnableToDeleteResourceException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveExtensionsException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveResourceException;
import edu.psu.swe.scim.server.exception.UnableToUpdateResourceException;
import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.spec.exception.InvalidExtensionException;
import edu.psu.swe.scim.spec.protocol.filter.AttributeComparisonExpression;
import edu.psu.swe.scim.spec.protocol.filter.CompareOperator;
import edu.psu.swe.scim.spec.protocol.filter.FilterExpression;
import edu.psu.swe.scim.spec.protocol.filter.FilterResponse;
import edu.psu.swe.scim.spec.protocol.search.Filter;
import edu.psu.swe.scim.spec.protocol.search.PageRequest;
import edu.psu.swe.scim.spec.protocol.search.SortRequest;
import edu.psu.swe.scim.spec.resources.ScimUser;

public class TierUserService implements Provider<ScimUser> {
  
  private static final Log LOG = LogFactory.getLog(TierUserService.class);

  @Override
  public ScimUser create(ScimUser resource) throws UnableToCreateResourceException {
    throw new UnableToCreateResourceException(BAD_REQUEST, "Method not supported");
  }

  @Override
  public ScimUser update(String id, ScimUser resource) throws UnableToUpdateResourceException {
    throw new UnableToUpdateResourceException(BAD_REQUEST, "Method not supported");
  }

  @Override
  public ScimUser get(String id) throws UnableToRetrieveResourceException {
    GrouperSession grouperSession = null;
    ScimUser scimUser = null;
    try {
      Subject authenticatedSubject = TierFilter.retrieveSubjectFromRemoteUser();
      grouperSession = GrouperSession.start(authenticatedSubject);
      Subject subject = SubjectFinder.findByIdOrIdentifier(id, false);
      if (subject == null) {
        throw new UnableToRetrieveResourceException(Status.NOT_FOUND, "User with id "+id+" does not exist.");
      }
      scimUser = new ScimUser();
      scimUser.setDisplayName(subject.getName());
      scimUser.setId(subject.getId());
      TierMetaExtension tierMetaExtension = new TierMetaExtension();
      tierMetaExtension.setResultCode("SUCCESS");
      scimUser.addExtension(tierMetaExtension);
    } catch(InvalidExtensionException ie) {
      throw new UnableToRetrieveResourceException(Status.INTERNAL_SERVER_ERROR, "Invalid extension");
    } catch(RuntimeException e) {
      LOG.error("Unable to get a subject "+ id, e);
      throw new UnableToRetrieveResourceException(Status.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.");
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    return scimUser;
  }

  
  private Subject findSubject(String attributeName, String attributeValue) {
    if (attributeName.equalsIgnoreCase("id")) {
      return SubjectFinder.findById(attributeValue, false);
    } else if (attributeName.equalsIgnoreCase("identifier")) {
      return SubjectFinder.findByIdentifier(attributeValue, false);
    } else {
      throw new IllegalArgumentException("only id and identifier attribute names are allowed");
    }
  }

  
  @Override
  public FilterResponse<ScimUser> find(Filter filter, PageRequest pageRequest,
      SortRequest sortRequest) throws UnableToRetrieveResourceException {
    
    GrouperSession grouperSession = null;
    FilterResponse<ScimUser> response = new FilterResponse<>();
    Subject resultSubject =  null;
    List<ScimUser> scimUserList = new ArrayList<>();
    try {
      Subject subject = TierFilter.retrieveSubjectFromRemoteUser();
      grouperSession = GrouperSession.start(subject);
      if (filter == null) {
        throw new UnableToRetrieveResourceException(Status.BAD_REQUEST, "filter cannot be blank or null");
      }
      FilterExpression filterExpression = filter.getExpression();
      if (filterExpression instanceof AttributeComparisonExpression) {
        
        AttributeComparisonExpression ace = (AttributeComparisonExpression) filterExpression;
        String attributeName = ace.getAttributePath().getFullAttributeName();
        
        CompareOperator operation = ace.getOperation();
        if (operation == CompareOperator.EQ) {
          resultSubject = findSubject(attributeName, ace.getCompareValue().toString());
          if (resultSubject != null) {
            ScimUser scimUser = new ScimUser();
            scimUser.setDisplayName(resultSubject.getName());
            scimUser.setId(resultSubject.getId());
            scimUserList.add(scimUser);
          }
        } else {
          throw new UnableToRetrieveResourceException(Status.BAD_REQUEST, "only eq comparison operator is allowed without grouping.");
        }        
      } else {
        throw new UnableToRetrieveResourceException(Status.BAD_REQUEST, "only eq comparison operator is allowed without grouping.");
      }
    } catch(IllegalArgumentException e) {
      throw new UnableToRetrieveResourceException(Status.BAD_REQUEST, e.getMessage());
    } catch(RuntimeException e) {
      LOG.error("Unable to find subjects ", e);
      throw new UnableToRetrieveResourceException(Status.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.");
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    response.setResources(scimUserList);
    PageRequest pr = new PageRequest();
    pr.setCount(scimUserList.size());
    pr.setStartIndex(0);
    response.setPageRequest(pr);
    response.setTotalResults(scimUserList.size());
    return response;
  }

  @Override
  public void delete(String id) throws UnableToDeleteResourceException {
   throw new UnableToDeleteResourceException(Status.BAD_REQUEST, "Method not supported");
    
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public List getExtensionList() throws UnableToRetrieveExtensionsException {
    return Arrays.asList(TierMetaExtension.class);
  }

}
