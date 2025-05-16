package org.xresource.core.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.xresource.core.annotations.XCronResource;
import org.xresource.core.annotations.XResource;
import org.xresource.core.annotations.XResourceExposeAsEmbeddedResource;
import org.xresource.core.annotations.XResourceIgnore;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@RequiredArgsConstructor
public class XResourceRepositoryScanner {

    @Value("${xresource.metadata.autoScanEnabled:true}")
    private boolean autoScanEnabled;

    public Set<Class<?>> scanRepositoriesWithXResource(String basePackage) {
        Set<Class<?>> result = new HashSet<>();
        String path = basePackage.replace('.', '/');

        try {
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();

                if ("file".equals(protocol)) {
                    scanDirectory(resource.getPath(), basePackage, result);
                } else if ("jar".equals(protocol)) {
                    scanJar(resource, path, basePackage, result);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan package: " + basePackage, e);
        }

        return result;
    }

    private void scanDirectory(String directoryPath, String packageName, Set<Class<?>> result) {
        var dir = new java.io.File(directoryPath);
        if (!dir.exists())
            return;

        for (var file : dir.listFiles()) {
            if (file.isDirectory()) {
                scanDirectory(file.getPath(), packageName + "." + file.getName(), result);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().replace(".class", "");
                try {
                    Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
                    if (autoScanEnabled) {
                        if (clazz.isAnnotationPresent(Repository.class)) {
                            if (!clazz.isAnnotationPresent(XResourceIgnore.class))
                                result.add(clazz);
                            else if (clazz.isAnnotationPresent(XResourceIgnore.class)
                                    && clazz.isAnnotationPresent(XResourceExposeAsEmbeddedResource.class))
                                result.add(clazz);
                            else if (clazz.isAnnotationPresent(XResourceIgnore.class)
                                    && clazz.isAnnotationPresent(XCronResource.class))
                                result.add(clazz);

                        }
                    } else {
                        if (clazz.isAnnotationPresent(Repository.class) && clazz.isAnnotationPresent(XResource.class)) {
                            result.add(clazz);
                        }
                    }

                } catch (Throwable ignored) {
                }
            }
        }
    }

    private void scanJar(URL resource, String path, String basePackage, Set<Class<?>> result) {
        try {
            JarURLConnection jarConn = (JarURLConnection) resource.openConnection();
            JarFile jarFile = jarConn.getJarFile();
            for (JarEntry entry : java.util.Collections.list(jarFile.entries())) {
                String name = entry.getName();
                if (name.startsWith(path) && name.endsWith(".class") && !entry.isDirectory()) {
                    String className = name.replace('/', '.').replace(".class", "");
                    try {
                        Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
                        if (autoScanEnabled) {
                            if (clazz.isAnnotationPresent(Repository.class)) {
                                if (!clazz.isAnnotationPresent(XResourceIgnore.class))
                                    result.add(clazz);
                                else if (clazz.isAnnotationPresent(XResourceIgnore.class)
                                        && clazz.isAnnotationPresent(XResourceExposeAsEmbeddedResource.class))
                                    result.add(clazz);

                            }
                        } else {
                            if (clazz.isAnnotationPresent(Repository.class)
                                    && clazz.isAnnotationPresent(XResource.class)) {
                                result.add(clazz);
                            }
                        }

                    } catch (Throwable ignored) {
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan JAR for package: " + basePackage, e);
        }
    }

    public boolean isAutoScanEnabled() {
        return autoScanEnabled;
    }
}
