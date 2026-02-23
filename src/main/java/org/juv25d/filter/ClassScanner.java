package org.juv25d.filter;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * Utility class for scanning and loading all classes within a given base package.
 *
 * This scanner uses the context class loader to locate resources corresponding
 * to the provided package name and recursively scans directories for compiled
 * {@code .class} files.
 *
 * Currently, only file-based classpath resources are supported. JAR scanning
 * is not implemented.
 */
public class ClassScanner {

    /**
     * Finds all classes within the specified base package.
     *
     * @param basePackage the root package to scan (e.g. {@code org.example})
     * @return a set of all discovered classes within the package and its subpackages
     * @throws RuntimeException if scanning fails due to IO or URI issues
     */
    public static Set<Class<?>> findClasses(String basePackage) {
        Set<Class<?>> classes = new HashSet<>();
        try {
            String path = basePackage.replace('.', '/');
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = cl.getResources(path);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();

                // Only supports file system resources (not JARs)
                if (resource.getProtocol().equals("file")) {
                    File dir = new File(resource.toURI());
                    scanDir(dir, basePackage, classes, cl);
                } else {
                    // Log a warning for skipped non-file resources
                    java.util.logging.Logger.getLogger(ClassScanner.class.getName())
                        .warning(String.format(
                            "ClassScanner: skipping non-file resource (protocol=%s, url=%s). " +
                                "scanDir/findClasses cannot load classes from JARs; JAR scanning not implemented.",
                            resource.getProtocol(), resource
                        ));
                }
            }

            return classes;

        } catch (Exception e) {
            throw new RuntimeException("Failed to scan package: " + basePackage, e);
        }
    }

    /**
     * Recursively scans a directory for {@code .class} files and loads them.
     *
     * @param dir the directory to scan
     * @param pkg the current package name
     * @param classes the result set where discovered classes are added
     * @throws ClassNotFoundException if a class cannot be loaded
     */
    private static void scanDir(File dir, String pkg, Set<Class<?>> classes, ClassLoader cl) {

        if (!dir.exists()) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDir(file, pkg + "." + file.getName(), classes, cl);
            }
            else if (file.getName().endsWith(".class")) {
                String className = pkg + "." + file.getName().replace(".class", "");
                try {
                    classes.add(Class.forName(className, true, cl));
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    // Skip classes that cannot be loaded (e.g. missing dependencies)
                }
            }
        }
    }
}
