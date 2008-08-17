/*
 * $Id: RijnByte.java,v 1.1 2008-08-17 15:33:02 mchyzer Exp $
 * 
 * Copyright University of Pennsylvania 2004
 */
package edu.internet2.middleware.grouper.util.rijndael;

/**
 */
class RijnByte {

  //this class represents my version of a byte
  //It contains an array of 8 booleans and provides
  //methods as required
  //bytes[0] is the LSB, bytes[7] is the MSB

  /**
   * $fieldName$ A public field of $objectType$
   */
  public boolean[] rinjBytesBoolean;

  //this array holds the boolean values
  //it is public so that they can be accessed
  //directly without accessor methods.
  //an simple constructor which initialises the array
  /**
   * Constructor for RijnByte.
   */
  public RijnByte() {
    this.rinjBytesBoolean = new boolean[8];
  }

  // a constructor that takes in 8 boolean values. the first value is the MSB
  // etc.
  /**
   * Constructor for RijnByte.
   * @param a boolean
   * @param b boolean
   * @param c boolean
   * @param d boolean
   * @param e boolean
   * @param f boolean
   * @param g boolean
   * @param h boolean
   */
  public RijnByte(boolean a, boolean b, boolean c, boolean d, boolean e, boolean f,
      boolean g, boolean h) {
    this.rinjBytesBoolean = new boolean[8];
    this.rinjBytesBoolean[7] = a;
    this.rinjBytesBoolean[6] = b;
    this.rinjBytesBoolean[5] = c;
    this.rinjBytesBoolean[4] = d;
    this.rinjBytesBoolean[3] = e;
    this.rinjBytesBoolean[2] = f;
    this.rinjBytesBoolean[1] = g;
    this.rinjBytesBoolean[0] = h;
  }

  //a constructor that takes an int and converts it to a RijnByte object by
  // filling
  //the array as required with 1 or 0
  /**
   * Constructor for RijnByte.
   * @param a int
   */
  public RijnByte(int a) {
    this.rinjBytesBoolean = new boolean[8];

    int temp = a;

    for (int i = 0; i < 8; i++) {
      if ((temp % 2) == 1) {
        this.rinjBytesBoolean[i] = true;
      } else {
        this.rinjBytesBoolean[i] = false;
      }

      temp = temp / 2;
    }
  }

  //this methods takes in a RijnByte object and returns the result
  //of it being XORed with the current RijnByte object
  //it uses a private method called intXOR. It doesn't actually effect
  //the current RijnByte object
  /**
   * Method XOR.
   * @param b RijnByte
   * @return RijnByte
   */
  public RijnByte XOR(RijnByte b) {
    RijnByte temp = new RijnByte();

    for (int i = 0; i < 8; i++) {
      temp.rinjBytesBoolean[i] = intXOR(this.rinjBytesBoolean[i], b.rinjBytesBoolean[i]);
    }

    return temp;
  }

  //this private method is used by XOR to return
  //the results of the XOR operation on a single bit
  /**
   * Method intXOR.
   * @param a boolean
   * @param b boolean
   * @return boolean
   */
  private boolean intXOR(boolean a, boolean b) {
    boolean temp = false;

    if (((a == true) && (b == false)) || ((b == true) && (a == false))) {
      temp = true;
    }

    return temp;
  }

  //this constructor takes in a string containing 1's and 0's
  //the first bit in the string is the MSB
  /**
   * Constructor for RijnByte.
   * @param a String
   */
  public RijnByte(String a) {
    this.rinjBytesBoolean = new boolean[8];

    String s;
    Character c;

    for (int i = 0; i < 8; i++) {
      c = new Character(a.charAt(i));
      s = c.toString();

      if (s.equals("1")) {
        this.rinjBytesBoolean[7 - i] = true;
      } else {
        this.rinjBytesBoolean[7 - i] = false;
      }
    }
  }

  //a constructor that takes in a boolean array
  /**
   * Constructor for RijnByte.
   * @param b boolean[]
   */
  public RijnByte(boolean[] b) {
    this.rinjBytesBoolean = b;
  }

  //this method returns an decimal value (int) of the array
  /**
   * Method getVal.
   * @return int
   */
  public int getVal() {
    int temp = 0;

    for (int i = 0; i < 8; i++) {
      if (this.rinjBytesBoolean[i] == true) {
        temp = temp + (int) java.lang.Math.pow(2, i);
      }
    }

    return temp;
  }
}