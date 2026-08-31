---
title: "Grouper programmatic JSON"
space: Grouper
pageId: 28547952
version: 7
lastUpdated: 2026-07-01T05:45:41.188Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547952/Grouper+programmatic+JSON
---

This document is for Grouper v4.9.4+ When programming with JSON in Grouper (e.g. for a GSH template web service) here are some tips and tricks

Jackson should be used and there are helper methods in the GrouperUtil class

## Generating JSON from a Map

A map will be converted to JSON, and will use the Jackson standard conversion.

- Note: the indenting is for troubleshooting purposes only
- Note: arrays or collections will be translated to json arrays

```
    Map<String, Object> theMap = new LinkedHashMap<String, Object>();
    theMap.put("first", "value1");
    theMap.put("second", 123);
    theMap.put("third", true);
    theMap.put("hey1", new String[] {"abc", "123"});
    theMap.put("hey2", GrouperUtil.toList("abcd", "1234"));
    
    String json = GrouperUtil.jsonConvertTo(theMap);
    json = GrouperUtil.indent(json, true);
    System.out.println(json);

```

Prints

```
{
  "first":"value1",
  "second":123,
  "third":true,
  "hey1":[
    "abc",
    "123"
  ]
  ,
  "hey2":[
    "abcd",
    "1234"
  ]
}
```

## Generating a Map from JSON

If translating JSON into generic Java (e.g. not to a Javabean), a map is used to represent the JSON. Small whole numbers will be translated to Integer. Large will go to Long. Floating point will be double. And arrays will be lists.

This code

```
    String json2 = """
      {
        "first":"value1",
        "second":123,
        "third":true,
        "hey1":[
          "abc",
          "123"
        ]
        ,
        "hey2":[
          "abcd",
          "1234"
        ]
      }
        """;
    Map<String, Object> theMap2 = GrouperUtil.jsonConvertFrom(json2, Map.class);
    

```

Translates to this map

## Sample Javabean

Note, for the next few examples, this is the Javabean

```
  public static class SomeBean {
    private String first;
    private int second;
    private boolean third;
    private String[] hey1;
    private List<String> hey2;
    
    public String getFirst() {
      return first;
    }
    
    public void setFirst(String first) {
      this.first = first;
    }
    
    public int getSecond() {
      return second;
    }
    
    public void setSecond(int second) {
      this.second = second;
    }
    
    public boolean isThird() {
      return third;
    }
    
    public void setThird(boolean third) {
      this.third = third;
    }
    
    public String[] getHey1() {
      return hey1;
    }
    
    public void setHey1(String[] hey1) {
      this.hey1 = hey1;
    }
    
    public List<String> getHey2() {
      return hey2;
    }
    
    public void setHey2(List<String> hey2) {
      this.hey2 = hey2;
    }
    
  }

```

## Converting JSON to a Javabean

To make things easier you can convert from a Javabean to JSON. Note you need to have getters and setters just like any Javabean. Note you can have beans that are fields of the bean etc.

This code:

```
    String json2 = """
      {
        "first":"value1",
        "second":123,
        "third":true,
        "hey1":[
          "abc",
          "123"
        ]
        ,
        "hey2":[
          "abcd",
          "1234"
        ]
      }
        """;

    SomeBean someBean = GrouperUtil.jsonConvertFrom(json2, SomeBean.class);

```

Translates to this bean:

## Converting a Javabean to JSON

You can convert a Javabean to JSON

This code

```
    SomeBean someBean2 = new SomeBean();
    someBean2.setFirst("value1");
    someBean2.setSecond(123);
    someBean2.setThird(true);
    someBean2.setHey1(new String[] {"abc", "123"});
    someBean2.setHey2(GrouperUtil.toList("abcd", "1234"));
    
    String json4 = GrouperUtil.jsonConvertTo(someBean2, false);
    json4 = GrouperUtil.indent(json4, true);
    System.out.println(json4);

```

Generates this JSON

```
{
  "first":"value1",
  "second":123,
  "third":true,
  "hey1":[
    "abc",
    "123"
  ]
  ,
  "hey2":[
    "abcd",
    "1234"
  ]
}

```

## Converting a Map to a Javabean

When processing the inbound of a GSH template, the JSON is marshaled to a Map. If you want to convert that to a Javabean, you can do this

This code:

```
    Map<String, Object> theMap = new LinkedHashMap<String, Object>();
    theMap.put("first", "value1");
    theMap.put("second", 123);
    theMap.put("third", true);
    theMap.put("hey1", new String[] {"abc", "123"});
    theMap.put("hey2", GrouperUtil.toList("abcd", "1234"));
    
    SomeBean someBean3 = GrouperUtil.jsonConvertFromMap(theMap, SomeBean.class);

```

Translates to this bean
