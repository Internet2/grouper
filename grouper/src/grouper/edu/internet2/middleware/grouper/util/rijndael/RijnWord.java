/*
 * $Id: RijnWord.java,v 1.1 2008-08-17 15:33:02 mchyzer Exp $
 * 
 * Copyright University of Pennsylvania 2004
 */
package edu.internet2.middleware.grouper.util.rijndael;

//this class represents a word
//it holds an array containing 4 RijnByte objects
/**
 */
class RijnWord {

  //again this array is public so it's contents can be
  //accessed without accessor methods.

  /**
   * $fieldName$ A public field of $objectType$
   */
  public RijnByte[] theRinjWord;

  //returns the bits of each byte
  //starting with the MSByte
  /**
   * Method toString.
   * @return String
   */
  public String toString() {
    String temp = "";

    for (int i = 3; i >= 0; i--) {
      if (this.theRinjWord[i] != null) {
        temp = temp + "  " + this.theRinjWord[i].toString();
      }
    }

    return temp;
  }

  //returns the decimal value of each byte
  //starting with the Most Significant byte
  /**
   * Method toStringInt.
   * @return String
   */
  public String toStringInt() {
    String temp = "";

    for (int i = 3; i >= 0; i--) {
      if (this.theRinjWord[i] != null) {
        temp = temp + "  " + this.theRinjWord[i].getVal();
      }
    }

    return temp;
  }

  //an simple constructor that merely initializes the array
  /**
   * Constructor for RijnWord.
   */
  public RijnWord() {
    this.theRinjWord = new RijnByte[4];
  }

  //a constructor that takes 4 RijnByte objects
  //the first one is the most significant
  /**
   * Constructor for RijnWord.
   * @param a RijnByte
   * @param b RijnByte
   * @param c RijnByte
   * @param d RijnByte
   */
  public RijnWord(RijnByte a, RijnByte b, RijnByte c, RijnByte d) {
    this.theRinjWord = new RijnByte[4];
    this.theRinjWord[3] = a;
    this.theRinjWord[2] = b;
    this.theRinjWord[1] = c;
    this.theRinjWord[0] = d;
  }

  //a method which adds 4 RijnByte elements into the word array
  //the first byte is the most significant
  /**
   * Method addWord.
   * @param a RijnByte
   * @param b RijnByte
   * @param c RijnByte
   * @param d RijnByte
   */
  public void addWord(RijnByte a, RijnByte b, RijnByte c, RijnByte d) {
    this.theRinjWord[3] = a;
    this.theRinjWord[2] = b;
    this.theRinjWord[1] = c;
    this.theRinjWord[0] = d;
  }

  //a method which adds 4 RijnByte objects to the word. It takes in the
  //RijnByte objects as an array of RijnByte objects
  /**
   * Method addWord.
   * @param m RijnByte[]
   */
  public void addWord(RijnByte[] m) {
    for (int i = 0; i < 4; i++) {
      this.theRinjWord[i] = m[i];
    }
  }

  //a construtor that takes in an array of myBytes
  /**
   * Constructor for RijnWord.
   * @param b RijnByte[]
   */
  public RijnWord(RijnByte[] b) {
    this.theRinjWord = b;
  }

  //a method which returns the result of
  //XORing a word taken in with the current word
  //it returns the result as a new word without affecting the
  //current word.
  /**
   * Method XOR.
   * @param b RijnWord
   * @return RijnWord
   */
  public RijnWord XOR(RijnWord b) {
    RijnWord temp = new RijnWord();
    temp.addWord(this.theRinjWord[3].XOR(b.theRinjWord[3]), this.theRinjWord[2].XOR(b.theRinjWord[2]), this.theRinjWord[1]
        .XOR(b.theRinjWord[1]), this.theRinjWord[0].XOR(b.theRinjWord[0]));

    return temp;
  }
}