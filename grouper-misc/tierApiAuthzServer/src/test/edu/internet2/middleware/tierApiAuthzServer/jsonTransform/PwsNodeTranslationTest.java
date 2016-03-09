/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.tierApiAuthzServer.jsonTransform;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import edu.internet2.middleware.tierApiAuthzServer.jsonTransform.PwsNode.PwsNodeType;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;


/**
 *
 */
public class PwsNodeTranslationTest extends TestCase {

  /**
   * 
   */
  public PwsNodeTranslationTest() {
    super();
    
  }

  /**
   * @param name
   */
  public PwsNodeTranslationTest(String name) {
    super(name);

  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new PwsNodeTranslationTest("testTranslateAttributeValueSelectorObjectToSelectorElTertiary"));
    //TestRunner.run(PwsNodeTranslationTest.class);
  }

  /**
   * someField.another = someField.another
   */
  public void testTranslateDrillDownAssignment() {
    
    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"someField\":{\"another\":56}}");

    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "someField.another = someField.another");

    assertEquals(new Long(56), newNode.retrieveField("someField").retrieveField("another").getInteger());
    assertEquals(new Long(56), dataNode.retrieveField("someField").retrieveField("another").getInteger());
    
    assertEquals("{\"someField\":{\"another\":56}}", dataNode.toJson());
    assertEquals("{\"someField\":{\"another\":56}}", newNode.toJson());
    
  }
  
  /**
   * someField.another[2].something = someField2.another2[2].something2
   */
  public void testTranslateArrayAssignment() {

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"someField2\":{\"another2\":[{\"something2\":\"a\"},{\"something2\":\"b\"},{\"something2\":\"c\"},{\"something2\":\"d\"}]}}");

    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "someField.another[2].something = someField2.another2[2].something2");

    assertEquals("c", dataNode.retrieveField("someField2").retrieveField("another2").retrieveArrayItem(2).retrieveField("something2").getString());
    assertEquals("c", newNode.retrieveField("someField").retrieveField("another").retrieveArrayItem(2).retrieveField("something").getString());

    assertEquals("{\"someField2\":{\"another2\":[{\"something2\":\"a\"},{\"something2\":\"b\"},{\"something2\":\"c\"},{\"something2\":\"d\"}]}}", dataNode.toJson());
    assertEquals("{\"someField\":{\"another\":[{},{},{\"something\":\"c\"}]}}", newNode.toJson());

  }

  /**
   * scalar
   * someField.another[1] = someField2.another2[2]
   */
  public void testTranslateArrayScalarAssignment() {

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"someField2\":{\"another2\":[\"a\", \"b\", \"c\", \"d\",\"e\"]}}");

    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "someField.another[2] = someField2.another2[3]");

    assertEquals("d", dataNode.retrieveField("someField2").retrieveField("another2").retrieveArrayItem(3).getString());
    assertEquals("d", newNode.retrieveField("someField").retrieveField("another").retrieveArrayItem(2).getString());

    assertEquals("{\"someField2\":{\"another2\":[\"a\",\"b\",\"c\",\"d\",\"e\"]}}", dataNode.toJson());
    assertEquals("{\"someField\":{\"another\":[null,null,\"d\"]}}", newNode.toJson());

  }

  /**
   * scalar
   * someField.another[2] = someField2.another2  (scalar)
   */
  public void testTranslateArrayScalarFromScalarAssignment() {

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"someField2\":{\"another2\":3.45}}");

    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "someField.another[2] = someField2.another2");

    assertEquals(new Double(3.45), dataNode.retrieveField("someField2").retrieveField("another2").getFloating());
    assertEquals(new Double(3.45), newNode.retrieveField("someField").retrieveField("another").retrieveArrayItem(2).getFloating());

    assertEquals("{\"someField2\":{\"another2\":3.45}}", dataNode.toJson());
    assertEquals("{\"someField\":{\"another\":[null,null,3.45]}}", newNode.toJson());

  }

  /**
   * someField.another = someField2.another2   (object)
   */
  public void testTranslateObject() {

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"someField2\":{\"another3\":3.45,\"another2\":{\"arraySub\":[{\"subInteger\":37,\"subString\":\"sub string\"},{\"subInteger\":37,\"subString\":\"sub string\"}]}}}");
    
    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "someField.another = someField2.another2");

    
    assertEquals(new Long(37), dataNode.retrieveField("someField2").retrieveField("another2").retrieveField("arraySub").retrieveArrayItem(0).retrieveField("subInteger").getInteger());
    assertEquals(new Long(37), newNode.retrieveField("someField").retrieveField("another").retrieveField("arraySub").retrieveArrayItem(0).retrieveField("subInteger").getInteger());

    assertEquals(dataNode.toJson(), "{\"someField2\":{\"another3\":3.45,\"another2\":{\"arraySub\":[{\"subInteger\":37,\"subString\":\"sub string\"},{\"subInteger\":37,\"subString\":\"sub string\"}]}}}", dataNode.toJson());
    assertEquals(newNode.toJson(), "{\"someField\":{\"another\":{\"arraySub\":[{\"subInteger\":37,\"subString\":\"sub string\"},{\"subInteger\":37,\"subString\":\"sub string\"}]}}}", newNode.toJson());
  }


  /**
   * someField.another = someField2.another2   (array of scalars)
   */
  public void testTranslateArrayOfScalars() {

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"someField2\":{\"another3\":3.45,\"another2\":{\"arraySub\":[23,45]}}}");
    
    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "someField.another = someField2.another2.arraySub");

    
    assertEquals(new Long(23), dataNode.retrieveField("someField2").retrieveField("another2").retrieveField("arraySub").retrieveArrayItem(0).getInteger());
    assertEquals(new Long(23), newNode.retrieveField("someField").retrieveField("another").retrieveArrayItem(0).getInteger());

    assertEquals(dataNode.toJson(), "{\"someField2\":{\"another3\":3.45,\"another2\":{\"arraySub\":[23,45]}}}", dataNode.toJson());
    assertEquals(newNode.toJson(), "{\"someField\":{\"another\":[23,45]}}", newNode.toJson());
  }

  /**
   * someField.another = someField2.another2   (array of objects)
   */
  public void testTranslateObjectArray() {

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"someField2\":{\"another3\":3.45,\"another2\":{\"arraySub\":[{\"subInteger\":37,\"subString\":\"sub string\"},{\"subInteger\":37,\"subString\":\"sub string\"}]}}}");
    
    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "someField.another = someField2.another2.arraySub");

    
    assertEquals(new Long(37), dataNode.retrieveField("someField2").retrieveField("another2").retrieveField("arraySub").retrieveArrayItem(0).retrieveField("subInteger").getInteger());
    assertEquals(new Long(37), newNode.retrieveField("someField").retrieveField("another").retrieveArrayItem(0).retrieveField("subInteger").getInteger());

    assertEquals(dataNode.toJson(), "{\"someField2\":{\"another3\":3.45,\"another2\":{\"arraySub\":[{\"subInteger\":37,\"subString\":\"sub string\"},{\"subInteger\":37,\"subString\":\"sub string\"}]}}}", dataNode.toJson());
    assertEquals(newNode.toJson(), "{\"someField\":{\"another\":[{\"subInteger\":37,\"subString\":\"sub string\"},{\"subInteger\":37,\"subString\":\"sub string\"}]}}", newNode.toJson());
  }

  /**
   * 
   * "someField:complicate.whatever"."someField:complicate.another"[2] = "someField2:complicate2.whatever2"."someField2:complicate.another2"[3]
   */
  public void testTranslateArrayScalarAssignmentQuotedFields() {

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"someField2:complicate2.whatever2\":{\"someField2:complicate.another2\":[\"a\",\"b\",\"c\",\"d\",\"e\"]}}");

    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "\"someField:complicate.whatever\".\"someField:complicate.another\"[2] "
        + "= \"someField2:complicate2.whatever2\".\"someField2:complicate.another2\"[3]");

    assertEquals("d", dataNode.retrieveField("someField2:complicate2.whatever2").retrieveField("someField2:complicate.another2").retrieveArrayItem(3).getString());
    assertEquals("d", newNode.retrieveField("someField:complicate.whatever").retrieveField("someField:complicate.another").retrieveArrayItem(2).getString());

    assertEquals("{\"someField2:complicate2.whatever2\":{\"someField2:complicate.another2\":[\"a\",\"b\",\"c\",\"d\",\"e\"]}}", dataNode.toJson());
    assertEquals("{\"someField:complicate.whatever\":{\"someField:complicate.another\":[null,null,\"d\"]}}", newNode.toJson());

  }

  /**
   * "some\"Field:compl[icate.whate=ver"."some\"Field:complic[ate.an=other"[2] = "some\"Field2:complic[ate2.wha=tever2"."some\"Field2:co[mplic=ate.another2"[3]
   */
  public void testTranslateArrayScalarAssignmentQuotedEqualsFields() {

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"some\\\"Field2:complic[ate2.wha=tever2\":{\"some\\\"Field2:co[mplic=ate.another2\":[\"a\",\"b\",\"c\",\"d\",\"e\"]}}");

    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "\"some\\\"Field:compl[icate.whate=ver\".\"some\\\"Field:complic[ate.an=other\"[2] "
        + "= \"some\\\"Field2:complic[ate2.wha=tever2\".\"some\\\"Field2:co[mplic=ate.another2\"[3]");

    assertEquals("d", dataNode.retrieveField("some\"Field2:complic[ate2.wha=tever2").retrieveField("some\"Field2:co[mplic=ate.another2").retrieveArrayItem(3).getString());
    assertEquals("d", newNode.retrieveField("some\"Field:compl[icate.whate=ver").retrieveField("some\"Field:complic[ate.an=other").retrieveArrayItem(2).getString());

    assertEquals("{\"some\\\"Field2:complic[ate2.wha=tever2\":{\"some\\\"Field2:co[mplic=ate.another2\":[\"a\",\"b\",\"c\",\"d\",\"e\"]}}", dataNode.toJson());
    assertEquals("{\"some\\\"Field:compl[icate.whate=ver\":{\"some\\\"Field:complic[ate.an=other\":[null,null,\"d\"]}}", newNode.toJson());

  }

  /**
   * someField.another = someField2.another2[@lang='fr']
   */
  public void testTranslateAttributeValueSelectorObject() {

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"someField2\":{\"another2\":[{\"lang\":\"fr\",\"aField2\":\"theVal\"},{\"lang\":\"en\",\"aField2\":\"theVal2\"}]}}");

    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "someField.another = someField2.another2[@lang='fr']");
    
    assertEquals("theVal", dataNode.retrieveField("someField2").retrieveField("another2").retrieveArrayItemByAttributeValue("lang", "fr").retrieveField("aField2").getString());
    assertEquals("theVal", newNode.retrieveField("someField").retrieveField("another").retrieveField("aField2").getString());

    assertEquals("{\"someField2\":{\"another2\":[{\"lang\":\"fr\",\"aField2\":\"theVal\"},{\"lang\":\"en\",\"aField2\":\"theVal2\"}]}}", dataNode.toJson());
    assertEquals("{\"someField\":{\"another\":{\"lang\":\"fr\",\"aField2\":\"theVal\"}}}", newNode.toJson());

  }

  
  
  /**
   * someField.another[@lang.something='en'] = someField2.another2[@lang.whatever='fr']
   */
  public void testTranslateAttributeValueSelectorObjectToSelector() {

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"someField2\":{\"another2\":[{\"lang\":{\"whatever\":\"fr\"},\"aField2\":\"theVal\"},{\"lang\":{\"whatever\":\"en\"},\"aField2\":\"theVal2\"}]}}");

    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "someField.another[@lang.something='en'] = someField2.another2[@lang.whatever='fr']");

    assertEquals("theVal", dataNode.retrieveField("someField2").retrieveField("another2").retrieveArrayItemByAttributeValue("lang.whatever", "fr").retrieveField("aField2").getString());
    assertEquals("theVal", newNode.retrieveField("someField").retrieveField("another").retrieveArrayItemByAttributeValue("lang.whatever", "fr").retrieveField("aField2").getString());

    assertEquals("{\"someField2\":{\"another2\":[{\"lang\":{\"whatever\":\"fr\"},\"aField2\":\"theVal\"},{\"lang\":{\"whatever\":\"en\"},\"aField2\":\"theVal2\"}]}}", dataNode.toJson());
    assertEquals("{\"someField\":{\"another\":[{\"lang\":{\"whatever\":\"fr\"},\"aField2\":\"theVal\"}]}}", newNode.toJson());

  }

  /**
   * someField.another[@lang.something='en'].yo = someField2.another2[@lang.whatever='fr'].aField2
   */
  public void testTranslateAttributeValueSelectorObjectToSelectorField() {

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"someField2\":{\"another2\":[{\"lang\":{\"whatever\":\"fr\"},\"aField2\":\"theVal\"},{\"lang\":{\"whatever\":\"en\"},\"aField2\":\"theVal2\"}]}}");

    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    //{"someField":{"another":[{"lang":{},"yo":"theVal"}]}}
    
    PwsNodeTranslation.assign(newNode, dataNode, "someField.another[@lang.something='en'].yo = someField2.another2[@lang.whatever='fr'].aField2");
    
    assertEquals("theVal", dataNode.retrieveField("someField2").retrieveField("another2").retrieveArrayItemByAttributeValue("lang.whatever", "fr").retrieveField("aField2").getString());
    assertEquals("theVal", newNode.retrieveField("someField").retrieveField("another").retrieveArrayItemByAttributeValue("lang.something", "en").retrieveField("yo").getString());

    assertEquals("{\"someField2\":{\"another2\":[{\"lang\":{\"whatever\":\"fr\"},\"aField2\":\"theVal\"},{\"lang\":{\"whatever\":\"en\"},\"aField2\":\"theVal2\"}]}}", dataNode.toJson());
    assertEquals("{\"someField\":{\"another\":[{\"lang\":{\"something\":\"en\"},\"yo\":\"theVal\"}]}}", newNode.toJson());

  }

  /**
   * someField.another[@lang.something='en'].field = (object)${null}
   */
  public void testTranslateAttributeValueSelectorObjectToSelectorFieldField() {

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"someField2\":{\"another2\":[{\"lang\":{\"whatever\":\"fr\"},\"aField2\":\"theVal\"},{\"lang\":{\"whatever\":\"en\"},\"aField2\":\"theVal2\"}]}}");

    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "someField.another[@lang.something='en'].field = (object)${null}");

    assertEquals("theVal", dataNode.retrieveField("someField2").retrieveField("another2").retrieveArrayItemByAttributeValue("lang.whatever", "fr").retrieveField("aField2").getString());
    
    // {"someField":{"another":[{"lang":{"something":"en"},"field":null}]}}
    
    assertEquals(0, StandardApiServerUtils.length(newNode.retrieveField("someField").retrieveField("another")
        .retrieveArrayItemByAttributeValue("lang.something", "en").retrieveField("field").getFieldNames()));

    assertEquals("{\"someField2\":{\"another2\":[{\"lang\":{\"whatever\":\"fr\"},\"aField2\":\"theVal\"},{\"lang\":{\"whatever\":\"en\"},\"aField2\":\"theVal2\"}]}}", dataNode.toJson());
    assertEquals("{\"someField\":{\"another\":[{\"lang\":{\"something\":\"en\"},\"field\":null}]}}", newNode.toJson());

  }


  /**
   * someField.another[@lang.something='en'].field = (floating)${'3.4'}${'5'}
   */
  public void testTranslateAttributeValueSelectorObjectToSelectorEl() {

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"someField2\":{\"another2\":[{\"lang\":{\"whatever\":\"fr\"},\"aField2\":\"theVal\"},{\"lang\":{\"whatever\":\"en\"},\"aField2\":\"theVal2\"}]}}");

    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "someField.another[@lang.something='en'].field = (floating)${'3.4'}${'5'}");

    assertEquals("theVal", dataNode.retrieveField("someField2").retrieveField("another2")
        .retrieveArrayItemByAttributeValue("lang.whatever", "fr").retrieveField("aField2").getString());
    
    // {"someField":{"another":[{"lang":{"something":"en"},"field":null}]}}
    
    assertEquals(new Double(3.45), newNode.retrieveField("someField").retrieveField("another")
        .retrieveArrayItemByAttributeValue("lang.something", "en").retrieveField("field").getFloating());

    assertEquals("{\"someField2\":{\"another2\":[{\"lang\":{\"whatever\":\"fr\"},\"aField2\":\"theVal\"},{\"lang\":{\"whatever\":\"en\"},\"aField2\":\"theVal2\"}]}}", dataNode.toJson());
    assertEquals("{\"someField\":{\"another\":[{\"lang\":{\"something\":\"en\"},\"field\":3.45}]}}", newNode.toJson());

  }

  /**
   * someField.another[@lang.something='en'].field = ${(3.4 > 3) ? 2 : 3}${'5}whatever'}
   */
  public void testTranslateAttributeValueSelectorObjectToSelectorElTertiary() {

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"someField2\":{\"another2\":[{\"lang\":{\"whatever\":\"fr\"},\"aField2\":\"theVal\"},{\"lang\":{\"whatever\":\"en\"},\"aField2\":\"theVal2\"}]}}");

    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "someField.another[@lang.something='en'].field = ${(3.4 > 3) ? 2 : 3} ${'5}whatever'}");

    assertEquals("theVal", dataNode.retrieveField("someField2").retrieveField("another2")
        .retrieveArrayItemByAttributeValue("lang.whatever", "fr").retrieveField("aField2").getString());
    
    // {"someField":{"another":[{"lang":{"something":"en"},"field":null}]}}
    
    String result = newNode.retrieveField("someField").retrieveField("another")
    .retrieveArrayItemByAttributeValue("lang.something", "en").retrieveField("field").getString();
    assertEquals(result,
        "2 5}whatever", result);

    assertEquals("{\"someField2\":{\"another2\":[{\"lang\":{\"whatever\":\"fr\"},\"aField2\":\"theVal\"},{\"lang\":{\"whatever\":\"en\"},\"aField2\":\"theVal2\"}]}}", dataNode.toJson());
    assertEquals("{\"someField\":{\"another\":[{\"lang\":{\"something\":\"en\"},\"field\":\"2 5}whatever\"}]}}", newNode.toJson());

  }

  /**
   * someField.another[@lang.something='en'].field = ${node.fields["someField2"].retrieveField("another2").retrieveArrayItemByAttributeValue("lang.whatever", "fr").fields["aField2"].string} ${node.retrieveField("someField2").retrieveField("another2").array[1].retrieveField("aField2").getString()}
   */
  public void testTranslateAttributeValueSelectorElTraverse() {

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"someField2\":{\"another2\":[{\"lang\":{\"whatever\":\"fr\"},\"aField2\":\"theVal\"},{\"lang\":{\"whatever\":\"en\"},\"aField2\":\"theVal2\"}]}}");

    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "someField.another[@lang.something='en'].field = ${node.fields[\"someField2\"].retrieveField(\"another2\").retrieveArrayItemByAttributeValue('lang.whatever', \"fr\").fields[\"aField2\"].string} ${node.retrieveField(\"someField2\").retrieveField(\"another2\").array[1].retrieveField(\"aField2\").getString()}");

    assertEquals("theVal", dataNode.retrieveField("someField2").retrieveField("another2")
        .retrieveArrayItemByAttributeValue("lang.whatever", "fr").retrieveField("aField2").getString());
    
    // {"someField":{"another":[{"lang":{"something":"en"},"field":null}]}}
    
    assertEquals("theVal theVal2", newNode.retrieveField("someField").retrieveField("another")
        .retrieveArrayItemByAttributeValue("lang.something", "en").retrieveField("field").getString());

    assertEquals("{\"someField2\":{\"another2\":[{\"lang\":{\"whatever\":\"fr\"},\"aField2\":\"theVal\"},{\"lang\":{\"whatever\":\"en\"},\"aField2\":\"theVal2\"}]}}", dataNode.toJson());
    assertEquals("{\"someField\":{\"another\":[{\"lang\":{\"something\":\"en\"},\"field\":\"theVal theVal2\"}]}}", newNode.toJson());

  }

  /**
   * someField = someField
   */
  public void testTranslateSimpleAssignment() {

    PwsNode dataNode = new PwsNode();
    dataNode.setPwsNodeType(PwsNodeType.object);
    dataNode.assignField("someField", new PwsNode("someValue"));

    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "someField = someField");

    assertEquals("someValue", newNode.retrieveField("someField").getString());
    assertEquals("someValue", dataNode.retrieveField("someField").getString());

    assertEquals("{\"someField\":\"someValue\"}", newNode.toJson());
    assertEquals("{\"someField\":\"someValue\"}", dataNode.toJson());

  }


  
}
