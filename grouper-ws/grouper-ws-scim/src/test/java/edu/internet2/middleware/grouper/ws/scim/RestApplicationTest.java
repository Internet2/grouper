package edu.internet2.middleware.grouper.ws.scim;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.Test;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;

public class RestApplicationTest {
  
  @Test
  public void classesAreLoadedWhenGrouperWsScimIsSetToTrueInGrouperHibernateProperties() {
    // given
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.is.scim", "true");
    RestApplication restApplication = new RestApplication();
    
    // when
    Set<Class<?>> classes = restApplication.getClasses();
    
    // then
    assertThat(classes.size(), Matchers.greaterThan(0));
  } 
  
  @Test
  public void noClassesAreLoadedWhenGrouperWsScimIsSetToFalseInGrouperHibernateProperties() {
    // given
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.is.scim", "false");
    RestApplication restApplication = new RestApplication();
    
    // when
    Set<Class<?>> classes = restApplication.getClasses();
    
    // then
    assertThat(classes.size(), Matchers.equalTo(0));
  } 

}
