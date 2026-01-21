/*
 * This file is part of AsmFabricLoader - https://github.com/FlorianMichael/AsmFabricLoader
 * Copyright (C) 2023-2026 FlorianMichael/EnZaXD <git@florianmichael.de>
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

package de.florianmichael.asmfabricloader.api;

import de.florianmichael.asmfabricloader.loader.classloading.AFLConstants;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.metadata.EntrypointMetadata;
import net.fabricmc.loader.impl.metadata.LoaderModMetadata;

import java.util.List;
import java.util.function.Consumer;

/**
 * Utility to manually create entrypoints from mods before fabric loader is doing it.
 * This can be useful if you want to invoke entrypoints in language adapter loading stage.
 */
public class EarlyRiser {

    /**
     * Invokes a given consumer for all early entrypoints with a specific name and type.
     *
     * @param name     the entrypoint name
     * @param type     the entrypoint type
     * @param consumer the consumer to invoke
     */
    public static <T> void invokeEntrypoints(final String name, final Class<T> type, final Consumer<T> consumer) {
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            if (mod.getMetadata() instanceof LoaderModMetadata) {
                final LoaderModMetadata modMetadata = (LoaderModMetadata) mod.getMetadata();
                final List<EntrypointMetadata> entrypointMetadata = modMetadata.getEntrypoints(name);
                for (EntrypointMetadata metadata : entrypointMetadata) {
                    final T instance = createInstance(metadata.getValue(), type);
                    if (instance != null) {
                        consumer.accept(instance);
                    }
                }
            }
        }
    }

    private static <T> T createInstance(final String className, final Class<T> type) {
        try {
            return type.cast(Class.forName(className).getConstructor().newInstance());
        } catch (Exception e) {
            AFLConstants.LOGGER.error("Failed to load early entrypoint " + className, e);
            return null;
        }
    }

}
