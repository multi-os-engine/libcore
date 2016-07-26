/*
 * Copyright (C) 2016 The Android Open Source Project
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
 * limitations under the License
 */

package libcore.java.lang.invoke;

import junit.framework.TestCase;

import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.List;

public class MethodTypeTest extends TestCase {
    private static final Class<?>[] LARGE_PARAMETER_ARRAY;

    static {
        LARGE_PARAMETER_ARRAY = new Class<?>[254];
        for (int i = 0; i < 254; ++i) {
            LARGE_PARAMETER_ARRAY[i] = Object.class;
        }
    }

    public void test_methodType_basicTestsReturnTypeAndParameterClassArray() {
        MethodType mt = MethodType.methodType(int.class,
                new Class<?>[] { String.class, long.class});

        assertEquals(int.class, mt.returnType());
        assertEquals(2, mt.parameterCount());
        assertEquals(String.class, mt.parameterType(0));
        assertEquals(long.class, mt.parameterType(1));

        try {
            MethodType.methodType(null, new Class<?>[] { String.class });
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            MethodType.methodType(int.class, (Class<?>[]) null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            MethodType.methodType(int.class, new Class<?>[] {void.class});
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void test_methodType_basicTestsReturnTypeAndParameterClassList() {
        MethodType mt = MethodType.methodType(int.class, Arrays.asList(String.class, long.class));

        assertEquals(int.class, mt.returnType());
        assertEquals(2, mt.parameterCount());
        assertEquals(String.class, mt.parameterType(0));
        assertEquals(long.class, mt.parameterType(1));

        try {
            MethodType.methodType(null, Arrays.asList(String.class));
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            MethodType.methodType(int.class, (List<Class<?>>) null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            MethodType.methodType(int.class, Arrays.asList(void.class));
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void test_methodType_basicTestsReturnTypeAndVarargsParameters() {
        MethodType mt = MethodType.methodType(int.class, String.class, long.class);

        assertEquals(int.class, mt.returnType());
        assertEquals(2, mt.parameterCount());
        assertEquals(String.class, mt.parameterType(0));
        assertEquals(long.class, mt.parameterType(1));

        try {
            MethodType.methodType(null, String.class);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            MethodType.methodType(int.class, String.class, null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            MethodType.methodType(int.class, void.class, String.class);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void test_methodType_basicTestsReturnTypeOnly() {
        MethodType mt = MethodType.methodType(int.class);

        assertEquals(int.class, mt.returnType());
        assertEquals(0, mt.parameterCount());

        try {
            MethodType.methodType(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void test_methodType_basicTestsReturnTypeAndSingleParameter() {
        MethodType mt = MethodType.methodType(int.class, long.class);

        assertEquals(int.class, mt.returnType());
        assertEquals(1, mt.parameterCount());
        assertEquals(long.class, mt.parameterType(0));

        try {
            MethodType.methodType(null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            MethodType.methodType(null, String.class);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            MethodType.methodType(int.class, (Class<?>) null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            MethodType.methodType(int.class, void.class);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testGenericMethodType() {
        MethodType mt = MethodType.genericMethodType(0);
        assertEquals(0, mt.parameterCount());
        assertEquals(Object.class, mt.returnType());

        mt = MethodType.genericMethodType(3);
        assertEquals(Object.class, mt.returnType());

        assertEquals(3, mt.parameterCount());
        assertEquals(Object.class, mt.parameterType(0));
        assertEquals(Object.class, mt.parameterType(1));
        assertEquals(Object.class, mt.parameterType(2));

        Class<?>[] params = mt.parameterArray();
        assertEquals(3, params.length);
        assertEquals(Object.class, params[0]);
        assertEquals(Object.class, params[1]);
        assertEquals(Object.class, params[2]);

        List<Class<?>> paramsList = mt.parameterList();
        assertEquals(3, paramsList.size());
        assertEquals(Object.class, paramsList.get(0));
        assertEquals(Object.class, paramsList.get(1));
        assertEquals(Object.class, paramsList.get(2));

        try {
            MethodType.genericMethodType(-1);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            MethodType.genericMethodType(256);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testGenericMethodTypeWithTrailingArray() {
        MethodType mt = MethodType.genericMethodType(3, false /* finalArray */);
        assertEquals(Object.class, mt.returnType());

        assertEquals(3, mt.parameterCount());
        assertEquals(Object.class, mt.parameterType(0));
        assertEquals(Object.class, mt.parameterType(1));
        assertEquals(Object.class, mt.parameterType(2));

        mt = MethodType.genericMethodType(0, true /* finalArray */);
        assertEquals(Object.class, mt.returnType());

        assertEquals(1, mt.parameterCount());
        assertEquals(Object[].class, mt.parameterType(0));

        mt = MethodType.genericMethodType(2, true /* finalArray */);
        assertEquals(Object.class, mt.returnType());

        assertEquals(3, mt.parameterCount());
        assertEquals(Object.class, mt.parameterType(0));
        assertEquals(Object.class, mt.parameterType(1));
        assertEquals(Object[].class, mt.parameterType(2));

        Class<?>[] params = mt.parameterArray();
        assertEquals(3, params.length);
        assertEquals(Object.class, params[0]);
        assertEquals(Object.class, params[1]);
        assertEquals(Object[].class, params[2]);

        List<Class<?>> paramsList = mt.parameterList();
        assertEquals(3, paramsList.size());
        assertEquals(Object.class, paramsList.get(0));
        assertEquals(Object.class, paramsList.get(1));
        assertEquals(Object[].class, paramsList.get(2));

        try {
            MethodType.genericMethodType(-1, true);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            MethodType.genericMethodType(255, true);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testChangeParameterType() {
        // int method(String, Object, List);
        MethodType mt = MethodType.methodType(int.class, String.class, Object.class, List.class);
        assertEquals(Object.class, mt.parameterType(1));

        MethodType changed = mt.changeParameterType(1, String.class);
        assertEquals(String.class, changed.parameterType(1));

        // Assert that the return types and the other parameter types haven't changed.
        assertEquals(mt.parameterCount(), changed.parameterCount());
        assertEquals(mt.returnType(), changed.returnType());
        assertEquals(mt.parameterType(0), changed.parameterType(0));
        assertEquals(mt.parameterType(2), changed.parameterType(2));

        try {
            mt.changeParameterType(-1, String.class);
            fail();
        } catch (ArrayIndexOutOfBoundsException expected) {
        }

        try {
            mt.changeParameterType(3, String.class);
            fail();
        } catch (ArrayIndexOutOfBoundsException expected) {
        }

        try {
            mt.changeParameterType(1, void.class);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            mt.changeParameterType(1, null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testInsertParameterTypes_varargs() {
        MethodType mt = MethodType.methodType(int.class, String.class, Object.class);

        MethodType insert0 = mt.insertParameterTypes(0, Integer.class, Long.class);
        assertEquals(int.class, insert0.returnType());
        assertEquals(4, insert0.parameterCount());
        assertEquals(Integer.class, insert0.parameterType(0));
        assertEquals(Long.class, insert0.parameterType(1));
        assertEquals(String.class, insert0.parameterType(2));
        assertEquals(Object.class, insert0.parameterType(3));

        MethodType insert1 = mt.insertParameterTypes(1, Integer.class, Long.class);
        assertEquals(4, insert0.parameterCount());
        assertEquals(String.class, insert1.parameterType(0));
        assertEquals(Integer.class, insert1.parameterType(1));
        assertEquals(Long.class, insert1.parameterType(2));
        assertEquals(Object.class, insert1.parameterType(3));

        MethodType insert2 = mt.insertParameterTypes(2, Integer.class, Long.class);
        assertEquals(String.class, insert2.parameterType(0));
        assertEquals(Object.class, insert2.parameterType(1));
        assertEquals(Integer.class, insert2.parameterType(2));
        assertEquals(Long.class, insert2.parameterType(3));

        try {
            mt.insertParameterTypes(1, LARGE_PARAMETER_ARRAY);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            mt.insertParameterTypes(1, void.class);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            mt.insertParameterTypes(1, (Class<?>) null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            mt.insertParameterTypes(-1, String.class);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }

        try {
            mt.insertParameterTypes(3, String.class);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
    }

    public void testInsertParameterTypes_list() {
        MethodType mt = MethodType.methodType(int.class, String.class, Object.class);

        MethodType insert0 = mt.insertParameterTypes(0, Arrays.asList(Integer.class, Long.class));
        assertEquals(int.class, insert0.returnType());
        assertEquals(4, insert0.parameterCount());
        assertEquals(Integer.class, insert0.parameterType(0));
        assertEquals(Long.class, insert0.parameterType(1));
        assertEquals(String.class, insert0.parameterType(2));
        assertEquals(Object.class, insert0.parameterType(3));

        MethodType insert1 = mt.insertParameterTypes(1, Arrays.asList(Integer.class, Long.class));
        assertEquals(4, insert0.parameterCount());
        assertEquals(String.class, insert1.parameterType(0));
        assertEquals(Integer.class, insert1.parameterType(1));
        assertEquals(Long.class, insert1.parameterType(2));
        assertEquals(Object.class, insert1.parameterType(3));

        MethodType insert2 = mt.insertParameterTypes(2, Arrays.asList(Integer.class, Long.class));
        assertEquals(4, insert2.parameterCount());
        assertEquals(String.class, insert2.parameterType(0));
        assertEquals(Object.class, insert2.parameterType(1));
        assertEquals(Integer.class, insert2.parameterType(2));
        assertEquals(Long.class, insert2.parameterType(3));

        try {
            mt.insertParameterTypes(1, Arrays.asList(LARGE_PARAMETER_ARRAY));
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            mt.insertParameterTypes(1, Arrays.asList(void.class));
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            mt.insertParameterTypes(1, (List<Class<?>>) null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            mt.insertParameterTypes(1, Arrays.asList(null));
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            mt.insertParameterTypes(-1, Arrays.asList(String.class));
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }

        try {
            mt.insertParameterTypes(3, Arrays.asList(String.class));
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
    }

    public void testAppendParameterTypes_varargs() {
        MethodType mt = MethodType.methodType(int.class, String.class, String.class);

        MethodType appended = mt.appendParameterTypes(List.class, Integer.class);
        assertEquals(int.class, appended.returnType());

        assertEquals(4, appended.parameterCount());
        assertEquals(String.class, appended.parameterType(0));
        assertEquals(String.class, appended.parameterType(1));
        assertEquals(List.class, appended.parameterType(2));
        assertEquals(Integer.class, appended.parameterType(3));

        try {
            mt.appendParameterTypes(LARGE_PARAMETER_ARRAY);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            mt.appendParameterTypes(void.class);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            mt.appendParameterTypes((Class<?>) null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testAppendParameterTypes_list() {
        MethodType mt = MethodType.methodType(int.class, String.class, String.class);

        MethodType appended = mt.appendParameterTypes(Arrays.asList(List.class, Integer.class));
        assertEquals(int.class, appended.returnType());

        assertEquals(4, appended.parameterCount());
        assertEquals(String.class, appended.parameterType(0));
        assertEquals(String.class, appended.parameterType(1));
        assertEquals(List.class, appended.parameterType(2));
        assertEquals(Integer.class, appended.parameterType(3));

        try {
            mt.appendParameterTypes(Arrays.asList(LARGE_PARAMETER_ARRAY));
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            mt.appendParameterTypes(Arrays.asList(void.class));
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            mt.appendParameterTypes((List<Class<?>>) null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            mt.appendParameterTypes(Arrays.asList(null));
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testDropParameterTypes() {
        MethodType mt = MethodType.methodType(int.class, String.class, List.class, Object.class);

        MethodType dropNone = mt.dropParameterTypes(0, 0);
        assertEquals(3, dropNone.parameterCount());
        assertEquals(int.class, dropNone.returnType());
        assertEquals(String.class, dropNone.parameterType(0));
        assertEquals(List.class, dropNone.parameterType(1));
        assertEquals(Object.class, dropNone.parameterType(2));

        MethodType dropFirst = mt.dropParameterTypes(0, 1);
        assertEquals(2, dropFirst.parameterCount());
        assertEquals(int.class, dropFirst.returnType());
        assertEquals(List.class, dropFirst.parameterType(0));
        assertEquals(Object.class, dropFirst.parameterType(1));

        MethodType dropAll = mt.dropParameterTypes(0, 3);
        assertEquals(0, dropAll.parameterCount());
        assertEquals(int.class, dropAll.returnType());

        try {
            mt.dropParameterTypes(-1, 1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }

        try {
            mt.dropParameterTypes(1, 4);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }

        try {
            mt.dropParameterTypes(2, 1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
    }

    public void testChangeReturnType() {
        MethodType mt = MethodType.methodType(int.class, String.class);

        MethodType changed = mt.changeReturnType(long.class);
        assertEquals(long.class, changed.returnType());
        assertEquals(1, changed.parameterCount());
        assertEquals(String.class, changed.parameterType(0));

        try {
            mt.changeReturnType(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testHasPrimitives() {
        MethodType mt = MethodType.methodType(Integer.class, Object.class, String.class);
        assertFalse(mt.hasPrimitives());

        mt = MethodType.methodType(int.class, Object.class);
        assertTrue(mt.hasPrimitives());

        mt = MethodType.methodType(Integer.class, long.class);
        assertTrue(mt.hasPrimitives());

        mt = MethodType.methodType(Integer.class, int[].class);
        assertFalse(mt.hasPrimitives());

        mt = MethodType.methodType(void.class);
        assertTrue(mt.hasPrimitives());
    }

    public void testHasWrappers() {
        MethodType mt = MethodType.methodType(Integer.class);
        assertTrue(mt.hasWrappers());

        mt = MethodType.methodType(String.class, Integer.class);
        assertTrue(mt.hasWrappers());

        mt = MethodType.methodType(int.class, long.class);
        assertFalse(mt.hasWrappers());
    }

    public void testErase() {
        // String mt(int, String, Object) should be erased to Object mt(int, Object, Object);
        MethodType mt = MethodType.methodType(String.class, int.class, String.class, Object.class);

        MethodType erased = mt.erase();
        assertEquals(Object.class, erased.returnType());

        assertEquals(3, erased.parameterCount());
        assertEquals(int.class, erased.parameterType(0));
        assertEquals(Object.class, erased.parameterType(1));
        assertEquals(Object.class, erased.parameterType(2));

        // Void returns must be left alone.
        mt = MethodType.methodType(void.class, int.class);
        erased = mt.erase();
        assertEquals(mt, erased);
    }

    public void testGeneric() {
        // String mt(int, String, Object) should be generified to Object mt(Object, Object, Object).
        // In other words, it must be equal to genericMethodType(3 /* parameterCount */);
        MethodType mt = MethodType.methodType(String.class, int.class, String.class, Object.class);

        MethodType generic = mt.generic();

        assertEquals(generic, MethodType.genericMethodType(mt.parameterCount()));
        assertEquals(generic, mt.wrap().erase());

        assertEquals(3, generic.parameterCount());
        assertEquals(Object.class, generic.returnType());
        assertEquals(Object.class, generic.parameterType(0));
        assertEquals(Object.class, generic.parameterType(1));
        assertEquals(Object.class, generic.parameterType(2));

        // Primitive return types must also become Object.
        generic = MethodType.methodType(int.class).generic();
        assertEquals(Object.class, generic.returnType());

        // void returns get converted to object returns (the same as wrap).
        generic = MethodType.methodType(void.class).generic();
        assertEquals(Object.class, generic.returnType());
    }

    public void testWrap() {
        // int mt(String, int, long, float, double, short, char, byte) should be wrapped to
        // Integer mt(String, Integer, Long, Float, Double, Short, Character, Byte);
        MethodType mt = MethodType.methodType(int.class, String.class, int.class, long.class,
                float.class, double.class, short.class, char.class, byte.class);

        MethodType wrapped = mt.wrap();
        assertFalse(wrapped.hasPrimitives());
        assertTrue(wrapped.hasWrappers());

        assertEquals(Integer.class, wrapped.returnType());

        assertEquals(8, wrapped.parameterCount());
        assertEquals(String.class, wrapped.parameterType(0));
        assertEquals(Integer.class, wrapped.parameterType(1));
        assertEquals(Long.class, wrapped.parameterType(2));
        assertEquals(Float.class, wrapped.parameterType(3));
        assertEquals(Double.class, wrapped.parameterType(4));
        assertEquals(Short.class, wrapped.parameterType(5));
        assertEquals(Character.class, wrapped.parameterType(6));
        assertEquals(Byte.class, wrapped.parameterType(7));

        // (semi) special case - void return types get wrapped to Void.
        wrapped = MethodType.methodType(void.class, int.class).wrap();
        assertEquals(Void.class, wrapped.returnType());
    }

    public void testUnwrap() {
        // Integer mt(String, Integer, Long, Float, Double, Short, Character, Byte);
        // should be unwrapped to :
        // int mt(String, int, long, float, double, short, char, byte).
        MethodType mt = MethodType.methodType(int.class, String.class, Integer.class, Long.class,
                Float.class, Double.class, Short.class, Character.class, Byte.class);

        MethodType unwrapped = mt.unwrap();
        assertTrue(unwrapped.hasPrimitives());
        assertFalse(unwrapped.hasWrappers());

        assertEquals(8, unwrapped.parameterCount());
        assertEquals(String.class, unwrapped.parameterType(0));
        assertEquals(int.class, unwrapped.parameterType(1));
        assertEquals(long.class, unwrapped.parameterType(2));
        assertEquals(float.class, unwrapped.parameterType(3));
        assertEquals(double.class, unwrapped.parameterType(4));
        assertEquals(short.class, unwrapped.parameterType(5));
        assertEquals(char.class, unwrapped.parameterType(6));
        assertEquals(byte.class, unwrapped.parameterType(7));

        // (semi) special case - void return types get wrapped to Void.
        unwrapped = MethodType.methodType(Void.class, int.class).unwrap();
        assertEquals(void.class, unwrapped.returnType());
    }

    public void testParameterListAndArray() {
        MethodType mt = MethodType.methodType(String.class, int.class, String.class, Object.class);

        List<Class<?>> paramsList = mt.parameterList();
        Class<?>[] paramsArray = mt.parameterArray();

        assertEquals(3, mt.parameterCount());

        for (int i = 0; i < 3; ++i) {
            Class<?> param = mt.parameterType(i);
            assertEquals(param, paramsList.get(i));
            assertEquals(param, paramsArray[i]);
        }

        mt = MethodType.methodType(int.class);
        assertEquals(0, mt.parameterCount());

        paramsList = mt.parameterList();
        paramsArray = mt.parameterArray();

        assertEquals(0, paramsList.size());
        assertEquals(0, paramsArray.length);
    }

    public void testEquals() {
        MethodType mt = MethodType.methodType(int.class, String.class);
        MethodType mt2 = MethodType.methodType(int.class, String.class);

        assertEquals(mt, mt2);
        assertEquals(mt, mt);

        assertFalse(mt.equals(null));
        assertFalse(mt.equals(MethodType.methodType(Integer.class, String.class)));
    }

    public void testHashCode() {
        MethodType mt = MethodType.methodType(int.class, String.class, Object.class);
        int hashCode = mt.hashCode();

        // The hash code should change if we change the return type or any of the parameters,
        // or if we add or remove parameters from the list.
        assertFalse(hashCode == mt.changeReturnType(long.class).hashCode());
        assertFalse(hashCode == mt.changeParameterType(0, Object.class).hashCode());
        assertFalse(hashCode == mt.appendParameterTypes(List.class).hashCode());
        assertFalse(hashCode == mt.dropParameterTypes(0, 1).hashCode());
    }

    public void testToString() {
        assertEquals("(String,Object)int",
                MethodType.methodType(int.class, String.class, Object.class).toString());
        assertEquals("()int", MethodType.methodType(int.class).toString());
        assertEquals("()void", MethodType.methodType(void.class).toString());
        assertEquals("()int[]", MethodType.methodType(int[].class).toString());
    }

    public void testFromMethodDescriptorString() {
        assertEquals(
                MethodType.methodType(int.class, String.class, Object.class),
                MethodType.fromMethodDescriptorString("(Ljava/lang/String;Ljava/lang/Object;)I", null));

        assertEquals(MethodType.fromMethodDescriptorString("()I", null),
                MethodType.methodType(int.class));
        assertEquals(MethodType.fromMethodDescriptorString("()[I", null),
                MethodType.methodType(int[].class));
        assertEquals(MethodType.fromMethodDescriptorString("([I)V", null),
                MethodType.methodType(void.class, int[].class));
    }

    public void testToMethodDescriptorString() {
        assertEquals("(Ljava/lang/String;Ljava/lang/Object;)I", MethodType.methodType(
                int.class, String.class, Object.class).toMethodDescriptorString());

        assertEquals("()I", MethodType.methodType(int.class).toMethodDescriptorString());
        assertEquals("()[I", MethodType.methodType(int[].class).toMethodDescriptorString());

        assertEquals("([I)V", MethodType.methodType(void.class, int[].class)
                .toMethodDescriptorString());
    }
}
