/* GENERATED SOURCE. DO NOT MODIFY. GENERATED BY nfuller @ Mon Oct 05 15:20:14 BST 2015 */
/*
******************************************************************************
* Copyright (C) 2007, International Business Machines Corporation and   *
* others. All Rights Reserved.                                               *
******************************************************************************
*/

package android.icu.impl.duration.impl;

interface RecordReader {
  boolean open(String title);
  boolean close();

  boolean bool(String name);
  boolean[] boolArray(String name);
  char character(String name);
  char[] characterArray(String name);
  byte namedIndex(String name, String[] names);
  byte[] namedIndexArray(String name, String[] names);
  String string(String name);
  String[] stringArray(String name);
  String[][] stringTable(String name);
}
