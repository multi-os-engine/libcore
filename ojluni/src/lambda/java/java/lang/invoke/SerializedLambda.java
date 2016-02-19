/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package java.lang.invoke;

import java.io.Serializable;

/**
 * Serialized form of a lambda expression.  The properties of this class
 * represent the information that is present at the lambda factory site, including
 * static metafactory arguments such as the identity of the primary functional
 * interface method and the identity of the implementation method, as well as
 * dynamic metafactory arguments such as values captured from the lexical scope
 * at the time of lambda capture.
 *
 * <p>Implementors of serializable lambdas, such as compilers or language
 * runtime libraries, are expected to ensure that instances deserialize properly.
 * One means to do so is to ensure that the {@code writeReplace} method returns
 * an instance of {@code SerializedLambda}, rather than allowing default
 * serialization to proceed.
 *
 * <p>{@code SerializedLambda} has a {@code readResolve} method that looks for
 * a (possibly private) static method called
 * {@code $deserializeLambda$(SerializedLambda)} in the capturing class, invokes
 * that with itself as the first argument, and returns the result.  Lambda classes
 * implementing {@code $deserializeLambda$} are responsible for validating
 * that the properties of the {@code SerializedLambda} are consistent with a
 * lambda actually captured by that class.
 */
public final class SerializedLambda implements Serializable {
    /**
     * Create a {@code SerializedLambda} from the low-level information present
     * at the lambda factory site.
     *
     * @param capturingClass The class in which the lambda expression appears
     * @param functionalInterfaceClass Name, in slash-delimited form, of static
     *                                 type of the returned lambda object
     * @param functionalInterfaceMethodName Name of the functional interface
     *                                      method for the present at the
     *                                      lambda factory site
     * @param functionalInterfaceMethodSignature Signature of the functional
     *                                           interface method present at
     *                                           the lambda factory site
     * @param implMethodKind Method handle kind for the implementation method
     * @param implClass Name, in slash-delimited form, for the class holding
     *                  the implementation method
     * @param implMethodName Name of the implementation method
     * @param implMethodSignature Signature of the implementation method
     * @param instantiatedMethodType The signature of the primary functional
     *                               interface method after type variables
     *                               are substituted with their instantiation
     *                               from the capture site
     * @param capturedArgs The dynamic arguments to the lambda factory site,
     *                     which represent variables captured by
     *                     the lambda
     */
    public SerializedLambda(Class<?> capturingClass,
                            String functionalInterfaceClass,
                            String functionalInterfaceMethodName,
                            String functionalInterfaceMethodSignature,
                            int implMethodKind,
                            String implClass,
                            String implMethodName,
                            String implMethodSignature,
                            String instantiatedMethodType,
                            Object[] capturedArgs) {
    }

    /**
     * Get the name of the class that captured this lambda.
     * @return the name of the class that captured this lambda
     */
    public String getCapturingClass() {
      return "";
    }

    /**
     * Get the name of the invoked type to which this
     * lambda has been converted
     * @return the name of the functional interface class to which
     * this lambda has been converted
     */
    public String getFunctionalInterfaceClass() {
      return "";
    }

    /**
     * Get the name of the primary method for the functional interface
     * to which this lambda has been converted.
     * @return the name of the primary methods of the functional interface
     */
    public String getFunctionalInterfaceMethodName() {
      return "";
    }

    /**
     * Get the signature of the primary method for the functional
     * interface to which this lambda has been converted.
     * @return the signature of the primary method of the functional
     * interface
     */
    public String getFunctionalInterfaceMethodSignature() {
      return "";
    }

    /**
     * Get the name of the class containing the implementation
     * method.
     * @return the name of the class containing the implementation
     * method
     */
    public String getImplClass() {
      return "";
    }

    /**
     * Get the name of the implementation method.
     * @return the name of the implementation method
     */
    public String getImplMethodName() {
      return "";
    }

    /**
     * Get the signature of the implementation method.
     * @return the signature of the implementation method
     */
    public String getImplMethodSignature() {
      return "";
    }

    /**
     * Get the method handle kind (see {@link MethodHandleInfo}) of
     * the implementation method.
     * @return the method handle kind of the implementation method
     */
    public int getImplMethodKind() {
      return -1;
    }

    /**
     * Get the signature of the primary functional interface method
     * after type variables are substituted with their instantiation
     * from the capture site.
     * @return the signature of the primary functional interface method
     * after type variable processing
     */
    public final String getInstantiatedMethodType() {
      return "";
    }

    /**
     * Get the count of dynamic arguments to the lambda capture site.
     * @return the count of dynamic arguments to the lambda capture site
     */
    public int getCapturedArgCount() {
      return -1;
    }

    /**
     * Get a dynamic argument to the lambda capture site.
     * @param i the argument to capture
     * @return a dynamic argument to the lambda capture site
     */
    public Object getCapturedArg(int i) {
      return null;
    }

    private Object readResolve() throws ReflectiveOperationException {
      return null;
    }

    @Override
    public String toString() {
      return null;
    }
}
