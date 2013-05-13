import java.lang.reflect.*;
import java.util.*;
import java.util.zip.*;

public class ApiDump {
  private final ArrayList<String> uninterestingPackages = new ArrayList<String>();
  private final ArrayList<String> interestingPackages = new ArrayList<String>();

  private static final Comparator<Class> CLASS_SORTER = new Comparator<Class>() {
    public int compare(Class lhs, Class rhs) {
      return lhs.toString().compareTo(rhs.toString());
    }
  };

  private static final Comparator<Constructor> CONSTRUCTOR_SORTER = new Comparator<Constructor>() {
    public int compare(Constructor lhs, Constructor rhs) {
      return ApiDump.toString(lhs).compareTo(ApiDump.toString(rhs));
    }
  };

  private static final Comparator<Field> FIELD_SORTER = new Comparator<Field>() {
    public int compare(Field lhs, Field rhs) {
      return ApiDump.toString(lhs).compareTo(ApiDump.toString(rhs));
    }
  };

  private static final Comparator<Method> METHOD_SORTER = new Comparator<Method>() {
    public int compare(Method lhs, Method rhs) {
      return ApiDump.toString(lhs).compareTo(ApiDump.toString(rhs));
    }
  };

  private boolean isInteresting(String className) {
    for (String packageName : uninterestingPackages) {
      if (className.startsWith(packageName)) {
        return false;
      }
    }

    for (String packageName : interestingPackages) {
      if (className.startsWith(packageName)) {
        return true;
      }
    }
    return false;
  }

  public ApiDump(String[] args) throws Exception {
    ZipFile jarFile = new ZipFile(args[0]);
    for (int i = 1; i < args.length; ++i) {
      this.interestingPackages.add(args[i]);
    }

    this.uninterestingPackages.add("java.beans.beancontext.");
    this.uninterestingPackages.add("java.lang.instrument.");
    this.uninterestingPackages.add("java.lang.invoke.");
    this.uninterestingPackages.add("java.lang.management.");
    this.uninterestingPackages.add("java.nio.file.");

    TreeMap<String, Class<?>> classes = new TreeMap<String, Class<?>>();

    for (ZipEntry entry : Collections.list(jarFile.entries())) {
      String name = entry.getName();
      if (!name.endsWith(".class")) {
        continue;
      }
      if (name.matches(".*\\$\\d+.*")) {
        continue;
      }
      name = name.replaceAll("\\.class$", "");
      name = name.replace('/', '.');
      if (!isInteresting(name)) {
        continue;
      }
      Class<?> c = Class.forName(name, false, this.getClass().getClassLoader());
      if (Modifier.isPublic(c.getModifiers())) {
        classes.put(name, c);
      }
    }

    for (String className : classes.keySet()) {
      Class<?> c = classes.get(className);

      String classDescription = Modifier.toString(c.getModifiers()) + " " + c.getName();
      if (c.getSuperclass() != null) {
        classDescription += " extends " + c.getSuperclass().getName();
      }
      Class[] interfaces = c.getInterfaces();
      Arrays.sort(interfaces, CLASS_SORTER);
      if (interfaces.length > 0) {
        classDescription += " implements ";
        for (int i = 0; i < interfaces.length; ++i) {
          if (i != 0) classDescription += ", ";
          classDescription += interfaces[i].getName();
        }
      }
      System.out.println(classDescription);

      Field[] fields = c.getFields(); //c.getDeclaredFields();
      Arrays.sort(fields, FIELD_SORTER);
      for (Field field : fields) {
        if (!isAccessible(field)) {
          continue;
        }
        System.out.println("\t" + toString(field));
      }

      Constructor[] constructors = c.getConstructors();
      Arrays.sort(constructors, CONSTRUCTOR_SORTER);
      for (Constructor constructor : constructors) {
        if (!isAccessible(constructor)) {
          continue;
        }
        System.out.println("\t" + toString(constructor));
      }

      Method[] methods = c.getMethods(); //c.getDeclaredMethods();
      Arrays.sort(methods, METHOD_SORTER);
      for (Method method : methods) {
        if (!isAccessible(method)) {
          continue;
        }
        System.out.println("\t" + toString(method));
      }

      //System.out.println();
    }
  }

  public static String toString(Field f) {
    return Modifier.toString(f.getModifiers()) + " " + f.getType() + " " + f.getName();
  }

  public static String toString(Constructor c) {
    String result;

    // 'native' and 'synchronized' changes are not API changes.
    int modifiers = c.getModifiers();
    modifiers &= ~(Modifier.NATIVE | Modifier.SYNCHRONIZED);

    result = Modifier.toString(modifiers);
    result += " " + c.getName();
    result += "(" + toString(c.getParameterTypes()) + ")";

    // TODO: need to ignore unchecked exceptions
    if (false) {
      Class<?>[] exceptions = c.getExceptionTypes();
      Arrays.sort(exceptions, CLASS_SORTER);
      if (exceptions.length > 0) {
        result += " throws " + toString(exceptions);
      }
    }

    return result;
  }

  public static String toString(Method m) {
    String result;

    // 'native' and 'synchronized' changes are not API changes.
    int modifiers = m.getModifiers();
    modifiers &= ~(Modifier.NATIVE | Modifier.SYNCHRONIZED);

    // Everything in StrictMath is 'strictfp'.
    if ((modifiers & Modifier.STRICT) != 0) {
      if (m.getDeclaringClass().getSimpleName().equals("StrictMath")) {
        modifiers &= ~Modifier.STRICT;
      }
    }

    // 'static' methods can't be overridden, so 'final' is just noise.
    if ((modifiers & Modifier.STATIC) != 0) {
      modifiers &= ~Modifier.FINAL;
    }

    result = Modifier.toString(modifiers);
    result += " " + m.getReturnType() + " " + m.getName();
    result += "(" + toString(m.getParameterTypes()) + ")";

    // TODO: need to ignore unchecked exceptions
    if (false) {
      Class<?>[] exceptions = m.getExceptionTypes();
      Arrays.sort(exceptions, CLASS_SORTER);
      if (exceptions.length > 0) {
        result += " throws " + toString(exceptions);
      }
    }

    return result;
  }

  private static String toString(Class<?>[] types) {
    StringBuilder result = new StringBuilder();
    if (types.length != 0) {
      appendTypeName(result, types[0]);
      for (int i = 1; i < types.length; i++) {
        result.append(',');
        appendTypeName(result, types[i]);
      }
    }
    return result.toString();
  }

  private static void appendTypeName(StringBuilder out, Class<?> c) {
    int dimensions = 0;
    while (c.isArray()) {
      c = c.getComponentType();
      dimensions++;
    }
    out.append(c.getName());
    for (int d = 0; d < dimensions; d++) {
      out.append("[]");
    }
  }

  private static boolean isAccessible(Member m) {
    return Modifier.isPublic(m.getModifiers()) || Modifier.isProtected(m.getModifiers());
  }

  public static void main(String[] args) throws Exception {
    new ApiDump(args);
  }
}
