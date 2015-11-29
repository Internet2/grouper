/**
 * @author mchyzer
 * $Id: LazySource.java,v 1.1 2009-10-31 16:27:09 mchyzer Exp $
 */
package edu.internet2.middleware.subject;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import edu.internet2.middleware.subject.provider.SourceManager;


/**
 * if the id or sources might not be known, dont resolve until you need it
 */
public class LazySource implements Source {

  /** id of underlying source */
  private String underlyingSourceId = null;
  
  /** underlying source */
  private transient Source source = null;
  
  /** default constructor */
  public LazySource() {}
  
  /**
   * construct with source id
   * @param theSourceId
   */
  public LazySource(String theSourceId) {
    this.underlyingSourceId = theSourceId;
  }
  
  
  /**
   * id of underlying source
   * @return the sourceId
   */
  public String getUnderlyingSourceId() {
    return this.underlyingSourceId;
  }

  
  /**
   * id of underlying source
   * @param sourceId1 the sourceId to set
   */
  public void setUnderlyingSourceId(String sourceId1) {
    this.underlyingSourceId = sourceId1;
    this.source = null;
  }

  /**
   * 
   * @return source
   */
  private Source getSource() {
    if (this.source == null) {
      this.source = SourceManager.getInstance().getSource(this.underlyingSourceId);
    }
    return this.source;
  }

  /**
   * @see edu.internet2.middleware.subject.Source#checkConfig()
   */
  public void checkConfig() {
    this.getSource().checkConfig();
  }

  /**
   * @see edu.internet2.middleware.subject.Source#getId()
   */
  public String getId() {
    return this.getSource().getId();
  }

  /**
   * @see edu.internet2.middleware.subject.Source#getName()
   */
  public String getName() {
    return this.getSource().getName();
  }

  /**
   * @param id
   * @return subject
   * @throws SubjectNotFoundException
   * @throws SubjectNotUniqueException
   */
  @SuppressWarnings("deprecation")
  public Subject getSubject(String id) throws SubjectNotFoundException,
      SubjectNotUniqueException {
    return this.getSource().getSubject(id);
  }

  /**
   * @see edu.internet2.middleware.subject.Source#getSubject(java.lang.String, boolean)
   */
  public Subject getSubject(String id, boolean exceptionIfNull)
      throws SubjectNotFoundException, SubjectNotUniqueException {
    return this.getSource().getSubject(id, exceptionIfNull);
  }

  /**
   * @see edu.internet2.middleware.subject.Source#getSubjectByIdOrIdentifier(java.lang.String, boolean)
   */
  public Subject getSubjectByIdOrIdentifier(String idOrIdentifier, boolean exceptionIfNull)
      throws SubjectNotFoundException, SubjectNotUniqueException {
    return this.getSource().getSubjectByIdOrIdentifier(idOrIdentifier, exceptionIfNull);
  }

  /**
   * @param id
   * @return subject
   * @throws SubjectNotFoundException
   * @throws SubjectNotUniqueException
   */
  @SuppressWarnings("deprecation")
  public Subject getSubjectByIdentifier(String id) throws SubjectNotFoundException,
      SubjectNotUniqueException {
    return this.getSource().getSubjectByIdentifier(id);
  }

  /**
   * @see edu.internet2.middleware.subject.Source#getSubjectByIdentifier(java.lang.String, boolean)
   */
  public Subject getSubjectByIdentifier(String id, boolean exceptionIfNull)
      throws SubjectNotFoundException, SubjectNotUniqueException {
    return this.getSource().getSubjectByIdentifier(id, exceptionIfNull);
  }

  /**
   * @see edu.internet2.middleware.subject.Source#getSubjectTypes()
   */
  public Set<SubjectType> getSubjectTypes() {
    return this.getSource().getSubjectTypes();
  }

  /**
   * @see edu.internet2.middleware.subject.Source#getSubjectsByIdentifiers(java.util.Collection)
   */
  public Map<String, Subject> getSubjectsByIdentifiers(Collection<String> identifiers) {
    return this.getSource().getSubjectsByIdentifiers(identifiers);
  }

  /**
   * @see edu.internet2.middleware.subject.Source#getSubjectsByIds(java.util.Collection)
   */
  public Map<String, Subject> getSubjectsByIds(Collection<String> ids) {
    return this.getSource().getSubjectsByIds(ids);
  }

  /**
   * @see edu.internet2.middleware.subject.Source#getSubjectsByIdsOrIdentifiers(java.util.Collection)
   */
  public Map<String, Subject> getSubjectsByIdsOrIdentifiers(
      Collection<String> idsOrIdentifiers) {
    return this.getSource().getSubjectsByIdsOrIdentifiers(idsOrIdentifiers);
  }

  /**
   * @see edu.internet2.middleware.subject.Source#init()
   */
  public void init() throws SourceUnavailableException {
    this.getSource().init();
  }

  /**
   * @see edu.internet2.middleware.subject.Source#printConfig()
   */
  public String printConfig() {
    return this.getSource().printConfig();
  }

  /**
   * @see edu.internet2.middleware.subject.Source#search(java.lang.String)
   */
  public Set<Subject> search(String searchValue) {
    return this.getSource().search(searchValue);
  }

  /**
   * @see edu.internet2.middleware.subject.Source#setId(java.lang.String)
   */
  public void setId(String id) {
    this.getSource().setId(id);
  }

  /**
   * @see edu.internet2.middleware.subject.Source#setName(java.lang.String)
   */
  public void setName(String name) {
    this.getSource().setName(name);
  }

  /**
   * @see edu.internet2.middleware.subject.Source#getInitParam(java.lang.String)
   */
  public String getInitParam(String name1) {
    return this.getSource().getInitParam(name1);
  }

  /**
   * @see edu.internet2.middleware.subject.Source#initParams()
   */
  public Properties initParams() {
    return this.getSource().initParams();
  }

}
