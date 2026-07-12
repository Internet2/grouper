---
title: "Example of customizing the subject API source adapter"
space: Grouper
pageId: 28544024
version: 7
lastUpdated: 2026-07-01T05:48:57.910Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544024/Example+of+customizing+the+subject+API+source+adapter
---

- Download the latest Grouper 1.4 branch
  
  - cvs -d:pserver:anoncvs@anoncvs.internet2.edu:/home/cvs/i2mi login
  - cvs -d:pserver:anoncvs@anoncvs.internet2.edu:/home/cvs/i2mi export -r GROUPER_1_4_BRANCH grouper
- In grouper dir, run: ant dist
- Start the database (hsql in this case, could be mysql/postgres/oracle also):
  
  - F:\temp\grouper1.4\bin>java -cp ..\lib\jdbcSamples\hsqldb.jar org.hsqldb.Server -database.0 [file:grouper](file:///grouper) -dbname.0 grouper
- Add tables: (unix) bin> gsh.sh -registry -runscript (windows): bin>gsh -registry -runscript
  
  - (note: this is using hsql, you can do a similar thing on mysql or postgres or oracle)
  - You should now be able to browse the DB with hsqlManager or whatever tool you use to admin the db
  - (hsql) bin>java -cp ..\lib\jdbcSamples\hsqldb.jar org.hsqldb.util.DatabaseManager -url jdbc:hsqldb:hsql://localhost/grouper
- 
- Check tables: bin> gsh.sh -registry -check
  
  - Should output: NOTE: database table/object structure (ddl) is up to date
- Start gsh and add a subject: bin> gsh.sh  
  gsh 10% addSubject("mchyzer", "person", "Chris Hyzer")  
  hibernatesubject: id='mchyzer' type='person' name='Chris Hyzer'  
  gsh 11% SubjectFinder.findById("mchyzer", "person").getAttributes()
  ```
   java.util.HashMap: {LFNAME=[null], LOGINID=[null]}
  ```
  
  gsh 12% exit
- Make a directory under src/grouper called "poc" (proof of concept)
- Add a java source file of this name "src/grouper/poc/MyGrouperJdbcSourceAdapter.java" with these contents:
  ```
  /*
   * @author mchyzer
   * $Id$
   */
  package poc;
  
  import java.util.Set;
  
  import org.apache.commons.lang3.StringUtils;
  
  import edu.internet2.middleware.grouper.subj.GrouperJdbcSourceAdapter;
  import edu.internet2.middleware.grouper.util.GrouperUtil;
  import edu.internet2.middleware.subject.Subject;
  import edu.internet2.middleware.subject.SubjectNotFoundException;
  import edu.internet2.middleware.subject.SubjectNotUniqueException;
  
  /**
   *
   */
  public class MyGrouperJdbcSourceAdapter extends GrouperJdbcSourceAdapter {
  
    /**
     * as an example, concatenate two attributes together, store as a third attribute
     * @param subject
     */
    private void decorateSubject(Subject subject) {
      if (subject != null) {
        String idNameLoginid = subject.getId() + " " + subject.getName() + " "
          + StringUtils.defaultString(subject.getAttributeValue("LOGINID"));
        subject.getAttributes().put("idNameLoginid", idNameLoginid);
      }
    }
  
    @Override
    public Subject getSubject(String id) throws SubjectNotFoundException,
        SubjectNotUniqueException {
      Subject subject = super.getSubject(id);
      decorateSubject(subject);
      return subject;
    }
  
    @Override
    public Subject getSubjectByIdentifier(String id)
        throws SubjectNotFoundException, SubjectNotUniqueException {
      Subject subject = super.getSubjectByIdentifier(id);
      decorateSubject(subject);
      return subject;
    }
  
    @Override
    public Set<Subject> search(String searchValue) {
      Set<Subject> subjectSet = super.search(searchValue);
      for (Subject subject : GrouperUtil.nonNull(subjectSet)) {
        decorateSubject(subject);
      }
      return subjectSet;
    }
  
  }
  
  
  ```
  
  * Edit the sources.xml, and instead of:  
   <source adapterClass="edu.internet2.middleware.grouper.subj.GrouperJdbcSourceAdapter">  
   <id>jdbc</id>  
  Change it to:  
   <source adapterClass="poc.MyGrouperJdbcSourceAdapter">  
   <id>jdbc</id>
- Do another "ant dist" from GROUPER_HOME
- Start gsh from the bin dir, search for the subject again, see the new attribute: idNameLoginid
  ```
  gsh 0% SubjectFinder.findById("mchyzer", "person").getAttributes()
  java.util.HashMap: {LFNAME=[null], idNameLoginid=mchyzer Chris Hyzer , LOGINID=[null]}
  gsh 1%
  
  ```
- Note, after major upgrades of Grouper versions, you should test this to make sure it is still accurate...
