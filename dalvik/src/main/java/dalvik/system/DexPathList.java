/*
 * Copyright (C) 2011 The Android Open Source Project
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

package dalvik.system;

import android.system.ErrnoException;
import android.system.StructStat;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import libcore.io.IoUtils;
import libcore.io.Libcore;
import libcore.io.ClassPathURLStreamHandler;

import static android.system.OsConstants.S_ISDIR;

/**
 * A pair of lists of entries, associated with a {@code ClassLoader}.
 * One of the lists is a dex/resource path &mdash; typically referred
 * to as a "class path" &mdash; list, and the other names directories
 * containing native code libraries. Class path entries may be any of:
 * a {@code .jar} or {@code .zip} file containing an optional
 * top-level {@code classes.dex} file as well as arbitrary resources,
 * or a plain {@code .dex} file (with no possibility of associated
 * resources).
 *
 * <p>This class also contains methods to use these lists to look up
 * classes and resources.</p>
 */
/*package*/ final class DexPathList {
    private static final String DEX_SUFFIX = ".dex";
    private static final String zipSeparator = "!/";

    /** class definition context */
    private final ClassLoader definingContext;

    /**
     * List of dex/resource (class path) elements.
     * Should be called pathElements, but the Facebook app uses reflection
     * to modify 'dexElements' (http://b/7726934).
     */
    private final Element[] dexElements;

    /** List of native library path elements. */
    private final Element[] nativeLibraryPathElements;

    /** List of application native library directories. */
    private final List<File> nativeLibraryDirectories;

    /** List of system native library directories. */
    private final List<File> systemNativeLibraryDirectories;

    /**
     * Exceptions thrown during creation of the dexElements list.
     */
    private final IOException[] dexElementsSuppressedExceptions;

    /**
     * Constructs an instance.
     *
     * @param definingContext the context in which any as-yet unresolved
     * classes should be defined
     * @param dexPath list of dex/resource path elements, separated by
     * {@code File.pathSeparator}
     * @param libraryPath list of native library directory path elements,
     * separated by {@code File.pathSeparator}
     * @param optimizedDirectory directory where optimized {@code .dex} files
     * should be found and written to, or {@code null} to use the default
     * system directory for same
     */
    public DexPathList(ClassLoader definingContext, String dexPath,
            String libraryPath, File optimizedDirectory) {

        if (definingContext == null) {
            throw new NullPointerException("definingContext == null");
        }

        if (dexPath == null) {
            throw new NullPointerException("dexPath == null");
        }

        if (optimizedDirectory != null) {
            if (!optimizedDirectory.exists())  {
                throw new IllegalArgumentException(
                        "optimizedDirectory doesn't exist: "
                        + optimizedDirectory);
            }

            if (!(optimizedDirectory.canRead()
                            && optimizedDirectory.canWrite())) {
                throw new IllegalArgumentException(
                        "optimizedDirectory not readable/writable: "
                        + optimizedDirectory);
            }
        }

        this.definingContext = definingContext;

        ArrayList<IOException> suppressedExceptions = new ArrayList<IOException>();
        // save dexPath for BaseDexClassLoader
        this.dexElements = makeDexElements(splitDexPath(dexPath), optimizedDirectory,
                                           suppressedExceptions, definingContext);

        // Native libraries may exist in both the system and
        // application library paths, and we use this search order:
        //
        //   1. This class loader's library path for application libraries (libraryPath):
        //   1.1. Native library directories
        //   1.2. Path to libraries in apk-files
        //   2. The VM's library path from the system property for system libraries
        //      also known as java.library.path
        //
        // This order was reversed prior to Gingerbread; see http://b/2933456.
        this.nativeLibraryDirectories = splitPaths(libraryPath, false);
        this.systemNativeLibraryDirectories =
                splitPaths(System.getProperty("java.library.path"), true);
        List<File> allNativeLibraryDirectories = new ArrayList<>(nativeLibraryDirectories);
        allNativeLibraryDirectories.addAll(systemNativeLibraryDirectories);

        this.nativeLibraryPathElements = makePathElements(allNativeLibraryDirectories,
                                                          suppressedExceptions,
                                                          definingContext);

        if (suppressedExceptions.size() > 0) {
            this.dexElementsSuppressedExceptions =
                suppressedExceptions.toArray(new IOException[suppressedExceptions.size()]);
        } else {
            dexElementsSuppressedExceptions = null;
        }
    }

    @Override public String toString() {
        List<File> allNativeLibraryDirectories = new ArrayList<>(nativeLibraryDirectories);
        allNativeLibraryDirectories.addAll(systemNativeLibraryDirectories);

        File[] nativeLibraryDirectoriesArray =
                allNativeLibraryDirectories.toArray(
                    new File[allNativeLibraryDirectories.size()]);

        return "DexPathList[" + Arrays.toString(dexElements) +
            ",nativeLibraryDirectories=" + Arrays.toString(nativeLibraryDirectoriesArray) + "]";
    }

    /**
     * For BaseDexClassLoader.getLdLibraryPath.
     */
    public List<File> getNativeLibraryDirectories() {
        return nativeLibraryDirectories;
    }

    /**
     * Splits the given dex path string into elements using the path
     * separator, pruning out any elements that do not refer to existing
     * and readable files.
     */
    private static List<File> splitDexPath(String path) {
        return splitPaths(path, false);
    }

    /**
     * Splits the given path strings into file elements using the path
     * separator, combining the results and filtering out elements
     * that don't exist, aren't readable, or aren't either a regular
     * file or a directory (as specified). Either string may be empty
     * or {@code null}, in which case it is ignored. If both strings
     * are empty or {@code null}, or all elements get pruned out, then
     * this returns a zero-element list.
     */
    private static List<File> splitPaths(String searchPath, boolean directoriesOnly) {
        List<File> result = new ArrayList<>();

        if (searchPath != null) {
            for (String path : searchPath.split(File.pathSeparator)) {
                if (directoriesOnly) {
                    try {
                        StructStat sb = Libcore.os.stat(path);
                        if (!S_ISDIR(sb.st_mode)) {
                            continue;
                        }
                    } catch (ErrnoException ignored) {
                        continue;
                    }
                }
                result.add(new File(path));
            }
        }

        return result;
    }

    /**
     * Makes an array of dex/resource path elements, one per element of
     * the given array.
     */
    private static Element[] makeDexElements(List<File> files, File optimizedDirectory,
                                             List<IOException> suppressedExceptions,
                                             ClassLoader loader) {
        return makeElements(files, optimizedDirectory, suppressedExceptions, false, loader);
    }

    /**
     * Makes an array of directory/zip path elements, one per element of the given array.
     */
    private static Element[] makePathElements(List<File> files,
                                              List<IOException> suppressedExceptions,
                                              ClassLoader loader) {
        return makeElements(files, null, suppressedExceptions, true, loader);
    }

    /*
     * TODO (dimitry): Revert after GMS core stops relying on the existence of this
     * method (see b/21957414 for details)
     */
    private static Element[] makePathElements(List<File> files, File optimizedDirectory,
                                              List<IOException> suppressedExceptions) {
        return makeElements(files, null, suppressedExceptions, true, null);
    }

    private static Element[] makeElements(List<File> files, File optimizedDirectory,
                                          List<IOException> suppressedExceptions,
                                          boolean ignoreDexFiles,
                                          ClassLoader loader) {
        List<Element> elements = new ArrayList<>();
        /*
         * Open all files and load the (direct or contained) dex files
         * up front.
         */
        for (File file : files) {
            File zip = null;
            File dir = new File("");
            DexFile dex = null;
            String path = file.getPath();
            String name = file.getName();

            if (path.contains(zipSeparator)) {
                String split[] = path.split(zipSeparator, 2);
                zip = new File(split[0]);
                dir = new File(split[1]);
            } else if (file.isDirectory()) {
                // We support directories for looking up resources and native libraries.
                // Looking up resources in directories is useful for running libcore tests.
                elements.add(new Element(file, true, null, null));
            } else if (file.isFile()) {
                if (!ignoreDexFiles && name.endsWith(DEX_SUFFIX)) {
                    // Raw dex file (not inside a zip/jar).
                    try {
                        dex = loadDexFile(file, optimizedDirectory, loader);
                    } catch (IOException ex) {
                        System.logE("Unable to load dex file: " + file, ex);
                    }
                } else {
                    zip = file;

                    if (!ignoreDexFiles) {
                        try {
                            dex = loadDexFile(file, optimizedDirectory, loader);
                        } catch (IOException suppressed) {
                            /*
                             * IOException might get thrown "legitimately" by the DexFile constructor if
                             * the zip file turns out to be resource-only (that is, no classes.dex file
                             * in it).
                             * Let dex == null and hang on to the exception to add to the tea-leaves for
                             * when findClass returns null.
                             */
                            suppressedExceptions.add(suppressed);
                        }
                    }
                }
            } else {
                System.logW("ClassLoader referenced unknown path: " + file);
            }

            if ((zip != null) || (dex != null)) {
                elements.add(new Element(dir, false, zip, dex));
            }
        }

        return elements.toArray(new Element[elements.size()]);
    }

    /**
     * Constructs a {@code DexFile} instance, as appropriate depending
     * on whether {@code optimizedDirectory} is {@code null}.
     */
    private static DexFile loadDexFile(File file, File optimizedDirectory, ClassLoader loader)
            throws IOException {
        if (optimizedDirectory == null) {
            return new DexFile(file, loader);
        } else {
            String optimizedPath = optimizedPathFor(file, optimizedDirectory);
            return DexFile.loadDex(file.getPath(), optimizedPath, 0, loader);
        }
    }

    /**
     * Converts a dex/jar file path and an output directory to an
     * output file path for an associated optimized dex file.
     */
    private static String optimizedPathFor(File path,
            File optimizedDirectory) {
        /*
         * Get the filename component of the path, and replace the
         * suffix with ".dex" if that's not already the suffix.
         *
         * We don't want to use ".odex", because the build system uses
         * that for files that are paired with resource-only jar
         * files. If the VM can assume that there's no classes.dex in
         * the matching jar, it doesn't need to open the jar to check
         * for updated dependencies, providing a slight performance
         * boost at startup. The use of ".dex" here matches the use on
         * files in /data/dalvik-cache.
         */
        String fileName = path.getName();
        if (!fileName.endsWith(DEX_SUFFIX)) {
            int lastDot = fileName.lastIndexOf(".");
            if (lastDot < 0) {
                fileName += DEX_SUFFIX;
            } else {
                StringBuilder sb = new StringBuilder(lastDot + 4);
                sb.append(fileName, 0, lastDot);
                sb.append(DEX_SUFFIX);
                fileName = sb.toString();
            }
        }

        File result = new File(optimizedDirectory, fileName);
        return result.getPath();
    }

    /**
     * Finds the named class in one of the dex files pointed at by
     * this instance. This will find the one in the earliest listed
     * path element. If the class is found but has not yet been
     * defined, then this method will define it in the defining
     * context that this instance was constructed with.
     *
     * @param name of class to find
     * @param suppressed exceptions encountered whilst finding the class
     * @return the named class or {@code null} if the class is not
     * found in any of the dex files
     */
    public Class findClass(String name, List<Throwable> suppressed) {
        for (Element element : dexElements) {
            DexFile dex = element.dexFile;

            if (dex != null) {
                Class clazz = dex.loadClassBinaryName(name, definingContext, suppressed);
                if (clazz != null) {
                    return clazz;
                }
            }
        }
        if (dexElementsSuppressedExceptions != null) {
            suppressed.addAll(Arrays.asList(dexElementsSuppressedExceptions));
        }
        return null;
    }

    /**
     * Finds the named resource in one of the zip/jar files pointed at
     * by this instance. This will find the one in the earliest listed
     * path element.
     *
     * @return a URL to the named resource or {@code null} if the
     * resource is not found in any of the zip/jar files
     */
    public URL findResource(String name) {
        for (Element element : dexElements) {
            URL url = element.findResource(name);
            if (url != null) {
                return url;
            }
        }

        return null;
    }

    /**
     * Finds all the resources with the given name, returning an
     * enumeration of them. If there are no resources with the given
     * name, then this method returns an empty enumeration.
     */
    public Enumeration<URL> findResources(String name) {
        ArrayList<URL> result = new ArrayList<URL>();

        for (Element element : dexElements) {
            URL url = element.findResource(name);
            if (url != null) {
                result.add(url);
            }
        }

        return Collections.enumeration(result);
    }

    /**
     * Finds the named native code library on any of the library
     * directories pointed at by this instance. This will find the
     * one in the earliest listed directory, ignoring any that are not
     * readable regular files.
     *
     * @return the complete path to the library or {@code null} if no
     * library was found
     */
    public String findLibrary(String libraryName) {
        String fileName = System.mapLibraryName(libraryName);

        for (Element element : nativeLibraryPathElements) {
            String path = element.findNativeLibrary(fileName);

            if (path != null) {
                return path;
            }
        }

        return null;
    }

    /**
     * Element of the dex/resource/native library path
     */
    /*package*/ static class Element {
        private final File dir;
        private final boolean isDirectory;
        private final File zip;
        private final DexFile dexFile;

        private ClassPathURLStreamHandler urlHandler;
        private boolean initialized;

        public Element(File dir, boolean isDirectory, File zip, DexFile dexFile) {
            this.dir = dir;
            this.isDirectory = isDirectory;
            this.zip = zip;
            this.dexFile = dexFile;
        }

        @Override public String toString() {
            if (isDirectory) {
                return "directory \"" + dir + "\"";
            } else if (zip != null) {
                return "zip file \"" + zip + "\"" +
                       (dir != null && !dir.getPath().isEmpty() ? ", dir \"" + dir + "\"" : "");
            } else {
                return "dex file \"" + dexFile + "\"";
            }
        }

        public synchronized void maybeInit() {
            if (initialized) {
                return;
            }

            initialized = true;

            if (isDirectory || zip == null) {
                return;
            }

            try {
                urlHandler = new ClassPathURLStreamHandler(zip.getPath());
            } catch (IOException ioe) {
                /*
                 * Note: ZipException (a subclass of IOException)
                 * might get thrown by the ZipFile constructor
                 * (e.g. if the file isn't actually a zip/jar
                 * file).
                 */
                System.logE("Unable to open zip file: " + zip, ioe);
                urlHandler = null;
            }
        }

        public String findNativeLibrary(String name) {
            maybeInit();

            if (isDirectory) {
                String path = new File(dir, name).getPath();
                if (IoUtils.canOpenReadOnly(path)) {
                    return path;
                }
            } else if (urlHandler != null) {
                // Having a urlHandler means the element has a zip file.
                // In this case Android supports loading the library iff
                // it is stored in the zip uncompressed.

                String entryName = new File(dir, name).getPath();
                if (urlHandler.isEntryStored(entryName)) {
                  return zip.getPath() + zipSeparator + entryName;
                }
            }

            return null;
        }

        public URL findResource(String name) {
            maybeInit();

            // We support directories so we can run tests and/or legacy code
            // that uses Class.getResource.
            if (isDirectory) {
                File resourceFile = new File(dir, name);
                if (resourceFile.exists()) {
                    try {
                        return resourceFile.toURI().toURL();
                    } catch (MalformedURLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }

            if (urlHandler == null) {
                /* This element has no zip/jar file.
                 */
                return null;
            }
            return urlHandler.getEntryUrlOrNull(name);
        }
    }
}
