package edu.internet2.middleware.grouper.authentication;

import org.junit.Assert;

import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

public class GrouperTrustedJwtTest extends GrouperTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperTrustedJwtTest("testDecode"));    
  }
  
  public GrouperTrustedJwtTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public GrouperTrustedJwtTest(String name) {
    super(name);
  }
  
  
  public void testDecode() {
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.jwt.trusted.testConfigId.enabled").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.jwt.trusted.testConfigId.key.0.publicKey").value("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnzyis1ZjfNB0bBgKFMSv\nvkTtwlvBsaJq7S5wA+kzeVOVpVWwkWdVha4s38XM/pa/yr47av7+z3VTmvDRyAHc\naT92whREFpLv9cj5lTeJSibyr/Mrm/YtjCZVWgaOYIhwrXwKLqPr/11inWsAkfIy\ntvHWTxZYEcXLgAXFuUuaS3uF9gEiNQwzGTU1v0FqkqTBr4B8nW3HCN47XUu0t8Y0\ne+lf4s4OxQawWD79J9/5d3Ry0vbV3Am1FtGJiJvOwRsIfVChDpYStTcHTCMqtvWb\nV6L11BWkpzGXSW4Hv43qa+GSYOD2QU68Mb59oSk2OB+BtOLpJofmbGEGgvmwyCI9\nMwIDAQAB").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.jwt.trusted.testConfigId.key.0.encryptionType").value("RS-256").store();
    
    
    GrouperTrustedJwt grouperTrustedJwt = new GrouperTrustedJwt().assignBearerTokenHeader("Bearer jwtTrusted_testConfigId_eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwic3ViamVjdElkIjoiR3JvdXBlclN5c3RlbSIsImFkbWluIjp0cnVlLCJpYXQiOjE1MTYyMzkwMjJ9.bT0dn9j7yyA4NQAb_190P1Rydpiw1m48tGMUhGyfTJMcmovgwHbSxbEqXd9t9vtbN3YLB1MTrD6Q4aZ9GbfPhDFdwtPHnGsv5A9L7WVuYARR85kHnFTQ724dRvIiZz9U8mYyStg405--qMfDDFKgDC2j3cWxSEwULSaBcMZCvS7RU4d0uNUKAdwkTJdfoWsrojp6W65ApfPZ-AWIHw93aIOcA6ttwT6zaKmhlQJMN8z1a5WWB7lfFozu-Lc-XW5dp64_tTdTqSGSmZGEUyOsF5oq0cz9EGSXP8DvXm-PqfFO3Qcok5NyMOYTdSpVgb1qwkYoxHOI35VI9ukPJr15cg");
    
    // when
    Subject subject = grouperTrustedJwt.decode();
    
    // then
    Assert.assertTrue(subject != null);
    Assert.assertEquals("GrouperSysAdmin", subject.getName());
    
    // jwt was issued long before so with only 1 second expirationSeconds, decode should return null
    GrouperTrustedJwtConfig.clearCache();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.jwt.trusted.testConfigId.expirationSeconds").value("1").store();
    
    grouperTrustedJwt = new GrouperTrustedJwt().assignBearerTokenHeader("Bearer jwtTrusted_testConfigId_eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwic3ViamVjdElkIjoiR3JvdXBlclN5c3RlbSIsImFkbWluIjp0cnVlLCJpYXQiOjE1MTYyMzkwMjJ9.bT0dn9j7yyA4NQAb_190P1Rydpiw1m48tGMUhGyfTJMcmovgwHbSxbEqXd9t9vtbN3YLB1MTrD6Q4aZ9GbfPhDFdwtPHnGsv5A9L7WVuYARR85kHnFTQ724dRvIiZz9U8mYyStg405--qMfDDFKgDC2j3cWxSEwULSaBcMZCvS7RU4d0uNUKAdwkTJdfoWsrojp6W65ApfPZ-AWIHw93aIOcA6ttwT6zaKmhlQJMN8z1a5WWB7lfFozu-Lc-XW5dp64_tTdTqSGSmZGEUyOsF5oq0cz9EGSXP8DvXm-PqfFO3Qcok5NyMOYTdSpVgb1qwkYoxHOI35VI9ukPJr15cg");
    
    // when
    subject = grouperTrustedJwt.decode();
    
    // then
    Assert.assertTrue(subject == null);
    
    GrouperTrustedJwtConfig.clearCache();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.jwt.trusted.testConfigId.expirationSeconds").value("-1").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.jwt.trusted.testConfigId.subjectSourceIds").value("g:isa").store();
    
    grouperTrustedJwt = new GrouperTrustedJwt().assignBearerTokenHeader("Bearer jwtTrusted_testConfigId_eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwic3ViamVjdElkIjoiR3JvdXBlclN5c3RlbSIsImFkbWluIjp0cnVlLCJpYXQiOjE1MTYyMzkwMjJ9.bT0dn9j7yyA4NQAb_190P1Rydpiw1m48tGMUhGyfTJMcmovgwHbSxbEqXd9t9vtbN3YLB1MTrD6Q4aZ9GbfPhDFdwtPHnGsv5A9L7WVuYARR85kHnFTQ724dRvIiZz9U8mYyStg405--qMfDDFKgDC2j3cWxSEwULSaBcMZCvS7RU4d0uNUKAdwkTJdfoWsrojp6W65ApfPZ-AWIHw93aIOcA6ttwT6zaKmhlQJMN8z1a5WWB7lfFozu-Lc-XW5dp64_tTdTqSGSmZGEUyOsF5oq0cz9EGSXP8DvXm-PqfFO3Qcok5NyMOYTdSpVgb1qwkYoxHOI35VI9ukPJr15cg");
    
    // when
    subject = grouperTrustedJwt.decode();
    
    // then
    Assert.assertTrue(subject != null);
    Assert.assertEquals("GrouperSysAdmin", subject.getName());
    
  }

}
