/*
 * This file is part of AsmFabricLoader - https://github.com/FlorianMichael/AsmFabricLoader
 * Copyright (C) 2023-2024 FlorianMichael/EnZaXD <florian.michael07@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.florianmichael.asmfabricloader.loader.feature;

import de.florianmichael.asmfabricloader.loader.AFLFeature;
import de.florianmichael.asmfabricloader.loader.classloading.AFLConstants;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.lenni0451.reflect.ClassLoaders;
import net.lenni0451.reflect.stream.RStream;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;

public class JarBooter {

    public JarBooter(final Collection<ModContainer> modContainers) {
        AFLFeature.applyForMods(modContainers, "jarbooter", (modContainer, value) -> {
            if (value.getType() == CustomValue.CvType.ARRAY) {
                for (CustomValue customValue : value.getAsArray()) {
                    final File target = new File(customValue.getAsString());

                    replaceJar0(target);
                }
            } else if (value.getType() == CustomValue.CvType.STRING) {
                final File target = new File(value.getAsString());

                replaceJar0(target);
            }
        });
    }

    /**
     * Recursively iterates over the given file and loads all jar files to the front of the classpath
     *
     * @param file The file to iterate over
     */
    public void replaceJar0(final File file) {
        try {
            replaceJar(file);
        } catch (MalformedURLException e) {
            AFLConstants.LOGGER.error("Failed to load jar file " + file.getName() + " to the front of the classpath", e);
        }
    }

    /**
     * Recursively iterates over the given file and loads all jar files to the front of the classpath
     *
     * @param file The file to iterate over
     * @throws MalformedURLException If the file is not a valid URL
     */
    public void replaceJar(final File file) throws MalformedURLException {
        for (File listFile : file.listFiles()) {
            final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
            try {
                final ClassLoader actualLoader = RStream.of(oldLoader).fields().by("urlLoader").get();
                Thread.currentThread().setContextClassLoader(actualLoader);
                if (listFile.getName().endsWith(".jar")) {
                    ClassLoaders.loadToFront(listFile.toURI().toURL());

                    if (AFLConstants.isDebugEnabled()) {
                        AFLConstants.LOGGER.info("Loaded jar file " + listFile.getName() + " to the front of the classpath");
                    }
                } else if (listFile.isDirectory()) {
                    replaceJar(listFile);
                }
            } finally {
                Thread.currentThread().setContextClassLoader(oldLoader);
            }
        }
    }

}
