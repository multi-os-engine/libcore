/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.lang;

import java.lang.Math;
import java.util.Random;

import junit.framework.TestCase;

public class StrictMathTest extends TestCase {
  Random r = new Random();
  double x;
  double y;
  
  public void test_ceil() {
    for(int i = 0; i < 20; ++i) {
      x = 100*r.nextDouble();
      /* System.out.println("Native ceil is: " + Math.ceil(x) 
          + " and Java ceil is "
          + StrictMath.ceil(x)); */
      assertTrue(Math.ceil(x) == StrictMath.ceil(x));
    }
    assertTrue(StrictMath.ceil(+0.0) == 0.0);
    assertTrue(StrictMath.ceil(-0.0) == -0.0);
    assertTrue(StrictMath.ceil(-0.5) == -0.0);
    assertTrue(StrictMath.ceil(Double.POSITIVE_INFINITY)
        == Double.POSITIVE_INFINITY);
    assertTrue(StrictMath.ceil(Double.NEGATIVE_INFINITY) 
        == Double.NEGATIVE_INFINITY);
    assertTrue(Double.isNaN(StrictMath.ceil(Double.NaN)));
  }
  
  public void test_floor() {
    for(int i = 0; i < 20; ++i) {
      x = 100*r.nextDouble();
      /* System.out.println("Native floor is: " + Math.floor(x) 
          + " and Java floor is "
          + StrictMath.floor(x)); */
      assertTrue(Math.floor(x) == StrictMath.floor(x));
    }
    assertTrue(StrictMath.floor(+0.0) == 0.0);
    assertTrue(StrictMath.floor(-0.0) == -0.0);
    assertTrue(StrictMath.floor(Double.POSITIVE_INFINITY)
        == Double.POSITIVE_INFINITY);
    assertTrue(StrictMath.floor(Double.NEGATIVE_INFINITY) 
        == Double.NEGATIVE_INFINITY);
    assertTrue(Double.isNaN(StrictMath.floor(Double.NaN)));
  }
  
  public void test_hypot() {
    for(int i = 0; i < 20; ++i) {
      x = r.nextDouble();
      y = r.nextDouble();
      /*System.out.println("Native hypot is: " + Math.hypot(x,y)
          + " and Java hypot is "
          + StrictMath.hypot(x,y));*/
      assertTrue(Math.hypot(x, y) == StrictMath.hypot(x, y));
    }
    System.out.println();
    assertTrue(StrictMath.hypot(Double.POSITIVE_INFINITY, r.nextDouble()) 
        == Double.POSITIVE_INFINITY);
    assertTrue(StrictMath.hypot(Double.NEGATIVE_INFINITY, r.nextDouble()) 
        == Double.POSITIVE_INFINITY);
    assertTrue(StrictMath.hypot(r.nextDouble(), Double.POSITIVE_INFINITY) 
        == Double.POSITIVE_INFINITY);
    assertTrue(StrictMath.hypot(r.nextDouble(), Double.NEGATIVE_INFINITY) 
        == Double.POSITIVE_INFINITY);
    assertTrue(Double.isNaN(StrictMath.hypot(Double.NaN, Double.NaN)));
  }
  
  public void test_IEEEremainder() {
    for(int i = 0; i < 20; ++i) {
       x = r.nextDouble();
       y = 10*r.nextDouble();
       /* System.out.println("Native remainder is: " 
           + Math.IEEEremainder(x,y) 
           + " and Java remainder is "
           + StrictMath.IEEEremainder(x,y)); */
       assertTrue(Math.IEEEremainder(x,y) 
           == StrictMath.IEEEremainder(x,y));
     }
     assertTrue(Double.isNaN(StrictMath.IEEEremainder(100*r.nextDouble(), 0)));
     assertTrue(Double.isNaN(StrictMath.IEEEremainder(-100*r.nextDouble(), 0)));
     assertTrue(Double.isNaN(StrictMath.IEEEremainder(Double.POSITIVE_INFINITY, 
                                                      100*r.nextDouble())));
     assertTrue(Double.isNaN(StrictMath.IEEEremainder(Double.NEGATIVE_INFINITY, 
                                                      100*r.nextDouble())));
     assertTrue(Double.isNaN(StrictMath.IEEEremainder(Double.NaN, 
                                                      100*r.nextDouble())));
     assertTrue(Double.isNaN(StrictMath.IEEEremainder(100*r.nextDouble(), 
                                                      Double.NaN)));
     x = r.nextDouble();
     assertTrue(StrictMath.IEEEremainder(x, Double.POSITIVE_INFINITY) == x);
     assertTrue(StrictMath.IEEEremainder(x, Double.NEGATIVE_INFINITY) == x);
   }

  public void test_rint() {
    for(int i = 0; i < 20; ++i) {
      x = r.nextDouble();
      y = r.nextDouble();
      /*System.out.println("Native rint is: " + Math.rint(x)
          + " and Java rint is " + StrictMath.rint(x));*/
      assertTrue(Math.rint(x) == StrictMath.rint(x));
    }
    assertTrue(StrictMath.rint(+0.0) == 0.0);
    assertTrue(StrictMath.rint(-0.0) == -0.0);
    assertTrue(StrictMath.rint(Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY);
    assertTrue(StrictMath.rint(Double.NEGATIVE_INFINITY) == Double.NEGATIVE_INFINITY);
    assertTrue(Double.isNaN(StrictMath.rint(Double.NaN)));
  } 
  
  public void test_nextafter() {
    for(int i = 0; i < 20; ++i) {
      x = r.nextDouble();
      y = r.nextDouble();
      /* System.out.println("Native nextafter is: " + Math.nextAfter(x, y)
          + " and Java nextafter is " + StrictMath.nextafter(x,y)); */
      assertTrue(Math.nextAfter(x,y) == StrictMath.nextAfter(x,y));
    }
    assertTrue(Double.isNaN(StrictMath.nextAfter(Double.NaN, y)));
    assertTrue(Double.isNaN(StrictMath.nextAfter(x, Double.NaN)));
    double z = x;
    assertTrue(StrictMath.nextAfter(x, z) == x);
  }
  
  /* public void test_pow() {
    for(int i = 0; i < 20; ++i) {
      x = r.nextDouble();
      y = r.nextDouble();
      System.out.println("Native pow is: " + Math.pow(x, y)
          + " and StrictMath pow is " + StrictMath.pow(x,y));
      assertTrue(Math.pow(x,y) == StrictMath.pow(x,y));
    }
    assertTrue(StrictMath.pow(10*z, Double.POSITIVE_INFINITY)
               == Double.POSITIVE_INFINITY);
    assertTrue(StrictMath.pow(-10*z, Double.POSITIVE_INFINITY)
               == Double.POSITIVE_INFINITY);
    assertTrue(StrictMath.pow(10*z, Double.NEGATIVE_INFINITY)
               == 0.0);
    assertTrue(StrictMath.pow(-10*z, Double.NEGATIVE_INFINITY)
               == 0.0);
    assertTrue(StrictMath.pow(r.nextDouble(), Double.POSITIVE_INFINITY)
               == 0.0);
    assertTrue(StrictMath.pow(-1*r.nextDouble(), Double.POSITIVE_INFINITY)
               == 0.0);
    assertTrue(StrictMath.pow(r.nextDouble(), Double.NEGATIVE_INFINITY)
               == Double.POSITIVE_INFINITY);
    assertTrue(StrictMath.pow(-1*r.nextDouble(), Double.NEGATIVE_INFINITY)
               == Double.POSITIVE_INFINITY);
    assertTrue(Double.isNaN(StrictMath.pow(1.0, Double.POSITIVE_INFINITY)));
    assertTrue(Double.isNaN(StrictMath.pow(-1.0, 
                                               Double.POSITIVE_INFINITY)));
    assertTrue(Double.isNaN(StrictMath.pow(1.0, Double.NEGATIVE_INFINITY)));
    assertTrue(Double.isNaN(StrictMath.pow(-1.0, 
                                               Double.NEGATIVE_INFINITY)));
    assertTrue(StrictMath.pow(0.0, r.nextDouble()) == 0.0);
    assertTrue(StrictMath.pow(-0.0, r.nextDouble()) == 0.0);
    assertTrue(StrictMath.pow(-0.0, 3.0) == 0.0);
    assertTrue(StrictMath.pow(0.0, -1*r.nextDouble())
               == Double.POSITIVE_INFINITY);
    assertTrue(StrictMath.pow(-0.0, -1*r.nextDouble()) 
               == Double.POSITIVE_INFINITY);
    x = r.nextDouble();
    y = r.nextInt();
    assertTrue(StrictMath.pow(-0.0, 3) == -1*StrictMath.pow(0, 3));
    assertTrue(StrictMath.pow(Double.POSITIVE_INFINITY, x) 
               == Double.POSITIVE_INFINITY);
    assertTrue(StrictMath.pow(Double.POSITIVE_INFINITY, -1*x) == 0.0);
    assertTrue(StrictMath.pow(Double.NEGATIVE_INFINITY, x) 
               == Double.POSITIVE_INFINITY);
    assertTrue(StrictMath.pow(0.0, -1*x) == Double.POSITIVE_INFINITY);
    assertTrue(StrictMath.pow(-1*x, y) 
               == (StrictMath.pow(-1, y) * StrictMath.pow(x, y)));
    assertTrue(Double.isNaN(StrictMath.pow(-1*r.nextDouble(), Double.NaN)));
  } */
  
  /* public void test_sqrt() {
    for(int i = 0; i < 20; ++i) {
       x = r.nextDouble();
       y = r.nextDouble();
       /*System.out.println("Native sqrt is: " + Math.sqrt(x) 
           + " and StrictMath sqrt is "
           + StrictMath.sqrt(x)); 
       // assertTrue(Math.sqrt(x) == StrictMath.sqrt(x));
     }
     assertTrue(StrictMath.sqrt(+0.0) == 0.0);
     assertTrue(StrictMath.sqrt(-0.0) == -0.0);
     assertTrue(Double.isNaN(StrictMath.sqrt(-1)));
     assertTrue(StrictMath.sqrt(Double.POSITIVE_INFINITY) 
         == Double.POSITIVE_INFINITY);
     assertTrue(Double.isNaN(StrictMath.sqrt(Double.NaN)));
   }*/
}
