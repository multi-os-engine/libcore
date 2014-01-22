package org.apache.harmony.tests.java.io;

import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ComputeSerialVersionUIDTest extends junit.framework.TestCase {


    private String path = "/serialization/org/apache/harmony/tests/java/io/";

    public void testComputeSUIDClass() throws Exception {
        ObjectInputStream ois = new ObjectInputStream(getClass()
                .getResourceAsStream(path + "testComputeSUIDClass.ser"));
        SerializationTestClass.TestClassName1 o1 = (SerializationTestClass.TestClassName1) ois
                .readObject();
        SerializationTestClass.TestClassName2T_T$T o2 = (SerializationTestClass.TestClassName2T_T$T) ois
                .readObject();
        SerializationTestClass.TestClassModifierPublic o6 = (SerializationTestClass.TestClassModifierPublic) ois
                .readObject();
        SerializationTestClass.TestClassModifierAbstract o3 = (SerializationTestClass.TestClassModifierAbstract) ois
                .readObject();
        SerializationTestClass.TestClassModifierFinal o4 = (SerializationTestClass.TestClassModifierFinal) ois
                .readObject();
        SerializationTestClass.TestClassModifierInterface o5 = (SerializationTestClass.TestClassModifierInterface) ois
                .readObject();
        ois.close();
    }

    public void testComputeSUIDInterfaces() throws Exception {
        ObjectInputStream ois = new ObjectInputStream(getClass()
                .getResourceAsStream(path + "testComputeSUIDInterfaces.ser"));
        SerializationTestClass.TestIntefaces o1 = (SerializationTestClass.TestIntefaces) ois
                .readObject();
        SerializationTestClass.TestIntefacesA o2 = (SerializationTestClass.TestIntefacesA) ois
                .readObject();
        SerializationTestClass.TestIntefacesAB o3 = (SerializationTestClass.TestIntefacesAB) ois
                .readObject();
        SerializationTestClass.TestIntefacesBA o4 = (SerializationTestClass.TestIntefacesBA) ois
                .readObject();
        SerializationTestClass.TestIntefacesC o5 = (SerializationTestClass.TestIntefacesC) ois
                .readObject();
        SerializationTestClass.TestIntefacesAC o6 = (SerializationTestClass.TestIntefacesAC) ois
                .readObject();
        SerializationTestClass.TestIntefacesCA o7 = (SerializationTestClass.TestIntefacesCA) ois
                .readObject();
        SerializationTestClass.TestIntefacesABC o8 = (SerializationTestClass.TestIntefacesABC) ois
                .readObject();
        SerializationTestClass.TestIntefacesACB o9 = (SerializationTestClass.TestIntefacesACB) ois
                .readObject();
        SerializationTestClass.TestIntefacesBAC o10 = (SerializationTestClass.TestIntefacesBAC) ois
                .readObject();
        SerializationTestClass.TestIntefacesBCA o11 = (SerializationTestClass.TestIntefacesBCA) ois
                .readObject();
        SerializationTestClass.TestIntefacesCAB o12 = (SerializationTestClass.TestIntefacesCAB) ois
                .readObject();
        SerializationTestClass.TestIntefacesCBA o13 = (SerializationTestClass.TestIntefacesCBA) ois
                .readObject();
        ois.close();
    }

    public void testComputeSUIDFields() throws Exception {
        ObjectInputStream ois = new ObjectInputStream(getClass()
                .getResourceAsStream(path + "testComputeSUIDFields.ser"));
        SerializationTestClass.TestFieldsNone o1 = (SerializationTestClass.TestFieldsNone) ois
                .readObject();
        SerializationTestClass.TestFieldsOneFinal o2 = (SerializationTestClass.TestFieldsOneFinal) ois
                .readObject();
        SerializationTestClass.TestFieldsTwoFinal o3 = (SerializationTestClass.TestFieldsTwoFinal) ois
                .readObject();
        SerializationTestClass.TestFieldsOnePrivate o4 = (SerializationTestClass.TestFieldsOnePrivate) ois
                .readObject();
        SerializationTestClass.TestFieldsTwoPrivate o5 = (SerializationTestClass.TestFieldsTwoPrivate) ois
                .readObject();
        SerializationTestClass.TestFieldsOneProtected o6 = (SerializationTestClass.TestFieldsOneProtected) ois
                .readObject();
        SerializationTestClass.TestFieldsTwoProtected o7 = (SerializationTestClass.TestFieldsTwoProtected) ois
                .readObject();
        SerializationTestClass.TestFieldsOnePublic o8 = (SerializationTestClass.TestFieldsOnePublic) ois
                .readObject();
        SerializationTestClass.TestFieldsTwoPublic o9 = (SerializationTestClass.TestFieldsTwoPublic) ois
                .readObject();
        SerializationTestClass.TestFieldsOneStatic o10 = (SerializationTestClass.TestFieldsOneStatic) ois
                .readObject();
        SerializationTestClass.TestFieldsTwoStatic o11 = (SerializationTestClass.TestFieldsTwoStatic) ois
                .readObject();
        SerializationTestClass.TestFieldsOneTransient o12 = (SerializationTestClass.TestFieldsOneTransient) ois
                .readObject();
        SerializationTestClass.TestFieldsTwoTransient o13 = (SerializationTestClass.TestFieldsTwoTransient) ois
                .readObject();
        SerializationTestClass.TestFieldsOneVolatile o14 = (SerializationTestClass.TestFieldsOneVolatile) ois
                .readObject();
        SerializationTestClass.TestFieldsTwoVolatile o15 = (SerializationTestClass.TestFieldsTwoVolatile) ois
                .readObject();
        SerializationTestClass.TestFieldSignatures o16 = (SerializationTestClass.TestFieldSignatures) ois
                .readObject();
        ois.close();
    }

    public void testComputeSUIDConstructors() throws Exception {
        ObjectInputStream ois = new ObjectInputStream(getClass()
                .getResourceAsStream(path + "testComputeSUIDConstructors.ser"));
        SerializationTestClass.TestConstructorNone o1 = (SerializationTestClass.TestConstructorNone) ois
                .readObject();
        SerializationTestClass.TestConstructorOne o2 = (SerializationTestClass.TestConstructorOne) ois
                .readObject();
        SerializationTestClass.TestConstructorPrivate o3 = (SerializationTestClass.TestConstructorPrivate) ois
                .readObject();
        SerializationTestClass.TestConstructorProtected o4 = (SerializationTestClass.TestConstructorProtected) ois
                .readObject();
        SerializationTestClass.TestConstructorPublic o5 = (SerializationTestClass.TestConstructorPublic) ois
                .readObject();
        SerializationTestClass.TestConstructorSignature o6 = (SerializationTestClass.TestConstructorSignature) ois
                .readObject();
        SerializationTestClass.TestConstructorTwo o7 = (SerializationTestClass.TestConstructorTwo) ois
                .readObject();
        SerializationTestClass.TestConstructorTwoReverse o8 = (SerializationTestClass.TestConstructorTwoReverse) ois
                .readObject();
        ois.close();
    }

    public void testComputeSUIDMethods() throws Exception {
        ObjectInputStream ois = new ObjectInputStream(getClass()
                .getResourceAsStream(path + "testComputeSUIDMethods.ser"));
        SerializationTestClass.TestMehodPrivate o1 = (SerializationTestClass.TestMehodPrivate) ois
                .readObject();
        SerializationTestClass.TestMethodAbstract o2 = (SerializationTestClass.TestMethodAbstract) ois
                .readObject();
        SerializationTestClass.TestMethodFinal o3 = (SerializationTestClass.TestMethodFinal) ois
                .readObject();
        SerializationTestClass.TestMethodNative o4 = (SerializationTestClass.TestMethodNative) ois
                .readObject();
        SerializationTestClass.TestMethodProtected o5 = (SerializationTestClass.TestMethodProtected) ois
                .readObject();
        SerializationTestClass.TestMethodPublic o6 = (SerializationTestClass.TestMethodPublic) ois
                .readObject();
        SerializationTestClass.TestMethodStatic o7 = (SerializationTestClass.TestMethodStatic) ois
                .readObject();
        SerializationTestClass.TestMethodSignature o9 = (SerializationTestClass.TestMethodSignature) ois
                .readObject();
        SerializationTestClass.TestMethodReturnSignature o10 = (SerializationTestClass.TestMethodReturnSignature) ois
                .readObject();
        SerializationTestClass.TestMethodSynchronized o8 = (SerializationTestClass.TestMethodSynchronized) ois
                .readObject();
        ois.close();
    }

    private void generateGoldenFiles(String path) throws Exception {
         ObjectOutputStream out = new ObjectOutputStream(
                 new FileOutputStream(path + "/testComputeSUIDClass.ser"));
         out.writeObject(new SerializationTestClass.TestClassName1());
         out.writeObject(new SerializationTestClass.TestClassName2T_T$T());
         out.writeObject(new SerializationTestClass.TestClassModifierPublic());
         out.writeObject(new SerializationTestClass.TestClassModifierAbstract());
         out.writeObject(new SerializationTestClass.TestClassModifierFinal());
         out.writeObject(new SerializationTestClass.TestClassModifierInterface());
         out.close();

         out = new ObjectOutputStream(new FileOutputStream(path + "/testComputeSUIDInterfaces.ser"));
         out.writeObject(new SerializationTestClass.TestIntefaces());
         out.writeObject(new SerializationTestClass.TestIntefacesA());
         out.writeObject(new SerializationTestClass.TestIntefacesAB());
         out.writeObject(new SerializationTestClass.TestIntefacesBA());
         out.writeObject(new SerializationTestClass.TestIntefacesC());
         out.writeObject(new SerializationTestClass.TestIntefacesAC());
         out.writeObject(new SerializationTestClass.TestIntefacesCA());
         out.writeObject(new SerializationTestClass.TestIntefacesABC());
         out.writeObject(new SerializationTestClass.TestIntefacesACB());
         out.writeObject(new SerializationTestClass.TestIntefacesBAC());
         out.writeObject(new SerializationTestClass.TestIntefacesBCA());
         out.writeObject(new SerializationTestClass.TestIntefacesCAB());
         out.writeObject(new SerializationTestClass.TestIntefacesCBA());
         out.close();

         out = new ObjectOutputStream(new FileOutputStream(path + "/testComputeSUIDFields.ser"));
         out.writeObject(new SerializationTestClass.TestFieldsNone());
         out.writeObject(new SerializationTestClass.TestFieldsOneFinal());
         out.writeObject(new SerializationTestClass.TestFieldsTwoFinal());
         out.writeObject(new SerializationTestClass.TestFieldsOnePrivate());
         out.writeObject(new SerializationTestClass.TestFieldsTwoPrivate());
         out.writeObject(new SerializationTestClass.TestFieldsOneProtected());
         out.writeObject(new SerializationTestClass.TestFieldsTwoProtected());
         out.writeObject(new SerializationTestClass.TestFieldsOnePublic());
         out.writeObject(new SerializationTestClass.TestFieldsTwoPublic());
         out.writeObject(new SerializationTestClass.TestFieldsOneStatic());
         out.writeObject(new SerializationTestClass.TestFieldsTwoStatic());
         out.writeObject(new SerializationTestClass.TestFieldsOneTransient());
         out.writeObject(new SerializationTestClass.TestFieldsTwoTransient());
         out.writeObject(new SerializationTestClass.TestFieldsOneVolatile());
         out.writeObject(new SerializationTestClass.TestFieldsTwoVolatile());
         out.writeObject(new SerializationTestClass.TestFieldSignatures());
         out.close();

         out = new ObjectOutputStream(new FileOutputStream(path + "/testComputeSUIDConstructors.ser"));
         out.writeObject(new SerializationTestClass.TestConstructorNone());
         out.writeObject(new SerializationTestClass.TestConstructorOne());
         out.writeObject(new SerializationTestClass.TestConstructorPrivate(0));
         out.writeObject(new SerializationTestClass.TestConstructorProtected());
         out.writeObject(new SerializationTestClass.TestConstructorPublic());
         out.writeObject(new SerializationTestClass.TestConstructorSignature(false, (byte) 0, '0',
                 (short) 0, 0, 0.0f, 0.0, new Object()));
         out.writeObject(new SerializationTestClass.TestConstructorTwo('0'));
         out.writeObject(new SerializationTestClass.TestConstructorTwoReverse((byte) 0));
         out.close();

         out = new ObjectOutputStream(new FileOutputStream(path + "/testComputeSUIDMethods.ser"));
         out.writeObject(new SerializationTestClass.TestMehodPrivate());
         out.writeObject(new SerializationTestClass.TestMethodAbstract());
         out.writeObject(new SerializationTestClass.TestMethodFinal());
         out.writeObject(new SerializationTestClass.TestMethodNative());
         out.writeObject(new SerializationTestClass.TestMethodProtected());
         out.writeObject(new SerializationTestClass.TestMethodPublic());
         out.writeObject(new SerializationTestClass.TestMethodStatic());
         out.writeObject(new SerializationTestClass.TestMethodSignature());
         out.writeObject(new SerializationTestClass.TestMethodReturnSignature());
         out.writeObject(new SerializationTestClass.TestMethodSynchronized());
         out.close();
     }
}
