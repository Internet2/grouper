package edu.internet2.middleware.grouper.ws.versioning;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.versioning.v1.BeanA;
import edu.internet2.middleware.grouper.ws.versioning.v1.BeanB;

/**
 * 
 * @author mchyzer
 *
 */
public class VersioningPocTest extends TestCase {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(VersioningPocTest.class);

  /**
   * 
   */
  public VersioningPocTest() {
    super();
  }
  
  /**
   * 
   * @param name
   */
  public VersioningPocTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
   TestRunner.run(new VersioningPocTest("testVersioning"));
    
    
  }
  
  /**
   * test versioning
   */
  public void testVersioning() {
    BeanA beanA = null;
    
    String v2package = "edu.internet2.middleware.grouper.ws.versioning.v2";

    assertNull(changeToVersion(beanA, v2package));
    
    beanA = new BeanA();
    
    edu.internet2.middleware.grouper.ws.versioning.v2.BeanA v2BeanA = 
      (edu.internet2.middleware.grouper.ws.versioning.v2.BeanA)changeToVersion(beanA, v2package);
    
    assertNotNull(v2BeanA);
    
    assertNull(v2BeanA.getField1());
    assertNull(v2BeanA.getField1b());
    assertNull(v2BeanA.getField2());
    assertNull(v2BeanA.getField3());
    assertNull(v2BeanA.getField4());

    beanA.setField1("field1");
    beanA.setField1a("field1a");
    beanA.setField2(new String[]{"a", "b"});
    {
      BeanB beanB = new BeanB();
      beanB.setFieldB1("beanB");
      beanB.setFieldB2(new String[]{"beanBa", "beanBb"});
      beanA.setField3(beanB);
    }
    BeanB beanB1 = new BeanB();
    beanB1.setFieldB1("beanB1");
    beanB1.setFieldB2(new String[]{"beanBa1", "beanBb1"});
    
    BeanB beanB2 = new BeanB();
    beanB2.setFieldB1("beanB2");
    beanB2.setFieldB2(new String[]{"beanBa2", "beanBb2"});

    beanA.setField4(new BeanB[]{beanB1, beanB2});
    
    v2BeanA = (edu.internet2.middleware.grouper.ws.versioning.v2.BeanA)changeToVersion(beanA, v2package);
    
    assertEquals("field1", v2BeanA.getField1());
    assertNull(v2BeanA.getField1b());
    assertEquals(2, GrouperUtil.length(v2BeanA.getField2()));
    assertEquals("a", v2BeanA.getField2()[0]);
    assertEquals("b", v2BeanA.getField2()[1]);
    
    assertNotNull(v2BeanA.getField3());
    assertEquals("beanB", v2BeanA.getField3().getFieldB1());
    assertNull(v2BeanA.getField3().getFieldB1a());
    assertEquals(2, GrouperUtil.length(v2BeanA.getField3().getFieldB2()));
    assertEquals("beanBa", v2BeanA.getField3().getFieldB2()[0]);
    assertEquals("beanBb", v2BeanA.getField3().getFieldB2()[1]);

    assertEquals(2, GrouperUtil.length(v2BeanA.getField4()));
    assertEquals("beanBa1", v2BeanA.getField4()[0].getFieldB1());
    assertNull(v2BeanA.getField4()[0].getFieldB1a());
    assertEquals("beanBa2", v2BeanA.getField4()[0].getFieldB2());
    
    assertEquals("beanBb1", v2BeanA.getField4()[1].getFieldB1());
    assertNull(v2BeanA.getField4()[1].getFieldB1a());
    assertEquals("beanBb2", v2BeanA.getField4()[1].getFieldB2());
    
  }
  
  /**
   * convert an object from one version to another
   * @param input input object
   * @param newPackage is the package where the other version of things are
   * @return the object in the new version or null if input null or new version not found
   */
  public static Object changeToVersion(Object input, String newPackage) {
    return changeToVersionHelper(input, newPackage, 100);
  }
  
  /**
   * convert an object from one version to another
   * @param input input object
   * @param newPackage is the package where the other version of things are
   * @param timeToLive avoid circular references
   * @return the object in the new version or null if input null or new version not found
   */
  public static Object changeToVersionHelper(Object input, String newPackage, int timeToLive) {
    
    
    if (input == null) {
      LOG.debug("input is null");
      return null;
    }

    //if we are a string, just return it
    if (input instanceof String) {
      return input;
    }
    
    //lets get the input class
    Class inputClass = input.getClass();
    
    int interestingLogFields = 0;
    
    //if we are an array of strings, clone and return it
    int inputArrayLength = inputClass.isArray() ? GrouperUtil.length(input) : -1;
    if (inputClass.isArray() && String.class.equals(inputClass.getComponentType())) {
      //lets clone
      String[] result = new String[inputArrayLength];
      System.arraycopy(input, 0, result, 0, result.length);
      return result;
    }
    
    StringBuilder logMessage = LOG.isDebugEnabled() ? new StringBuilder() : null;
    
    if (logMessage != null) {
      logMessage.append("class: ").append(inputClass.getSimpleName()).append(", ");
    }
    
    if (timeToLive-- < 0) {
      throw new RuntimeException("Circular reference!");
    }
    
    //new class
    try {

      Object result = null;

      //if we are an array of objects, do that
      if (inputClass.isArray()) {
        Class<?> componentClass = inputClass.getComponentType();
        Object array = Array.newInstance(componentClass, inputArrayLength);
        for (int i=0;i<inputArrayLength;i++) {
          
          Object inputElement = Array.get(array, i);
          Object outputElement = changeToVersionHelper(inputElement, newPackage, timeToLive);
          Array.set(array, i, outputElement);
          
        }
        return array;
      }
      
      Class<?> outputClass = null;
      String outputClassName = newPackage + "." + inputClass.getSimpleName();
      try {
        outputClass = GrouperUtil.forName(outputClassName);
      } catch (RuntimeException re) {
        if (re.getCause() instanceof ClassNotFoundException) {
          if (logMessage != null) {
            logMessage.append("output classNotFound: ").append(outputClassName);
            LOG.debug(logMessage.toString());
          }
          return null;
        }
        //let this be handled below
        throw re;
      }


      
      //get instance
      result = GrouperUtil.newInstance(outputClass);

      //get all fields in the input
      Set<Field> inputFields = GrouperUtil.fields(inputClass, Object.class, null, false, false, false, null, false);
      Set<Field> outputFields = GrouperUtil.fields(outputClass, Object.class, null, false, false, false, null, false);
      
      Map<String, Field> inputFieldMap = new HashMap<String, Field>();
      
      for (Field field : GrouperUtil.nonNull(inputFields)) {
        inputFieldMap.put(field.getName(), field);
      }
      
      //see which ones match
      for (Field outputField : GrouperUtil.nonNull(outputFields)) {
        
        Field inputField = inputFieldMap.get(outputField.getName());
        
        if (inputField == null) {
          if (logMessage != null) {
            interestingLogFields++;
            logMessage.append("field not found input: ").append(outputField.getName()).append(", ");
          }
          continue;
        }
        //take it out of the map so we know which ones are left
        inputFieldMap.remove(inputField.getName());
        
        Object inputFieldObject = GrouperUtil.fieldValue(inputField, input);
        
        //lets convert that field
        Object outputFieldObject = changeToVersionHelper(inputFieldObject, newPackage, timeToLive);
        
        //this is ok
        if (outputFieldObject == null) {
          continue;
        }
        
        try {
          GrouperUtil.assignField(outputField, result, outputFieldObject, true, false);
        } catch (RuntimeException re) {
          if (logMessage != null) {
            logMessage.append("problem with field: ").append(inputField.getName()).append(", ").append(ExceptionUtils.getFullStackTrace(re));
            interestingLogFields++;
          }
        }
      }
      if (logMessage != null) {
        for (String inputFieldName : GrouperUtil.nonNull(inputFieldMap.keySet())) {
          logMessage.append("field not found output: ").append(inputFieldName).append(", ");
          interestingLogFields++;
        }
        
        if (interestingLogFields > 0) {
          LOG.debug(logMessage.toString());
        }
      }    
      
      return result;
    } catch (RuntimeException re) {
      if (logMessage != null) {
        logMessage.append("Problem with class: ").append(re.getClass()).append(", ").append(re.getMessage());
        LOG.debug(logMessage.toString(), re);
      }
      throw re;
    }
    
  }
  
}
