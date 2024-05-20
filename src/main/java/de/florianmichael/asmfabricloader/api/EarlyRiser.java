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

package de.florianmichael.asmfabricloader.api;

import de.florianmichael.asmfabricloader.loader.classloading.AFLConstants;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.LanguageAdapterException;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.metadata.EntrypointMetadata;
import net.fabricmc.loader.impl.metadata.LoaderModMetadata;
import net.fabricmc.loader.impl.util.DefaultLanguageAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Utility to run and create entrypoints before mod metadata is loaded.
 */
public class EarlyRiser {

    public static <T> void invokeEntrypoints(final String name, final Class<T> type, final Consumer<T> consumer) {
        getEarlyEntrypoints(name, type).forEach(consumer);
    }

    public static <T> List<T> getEarlyEntrypoints(final String name, final Class<T> type) {
        final List<T> entrypoints = new ArrayList<>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            if (mod.getMetadata() instanceof LoaderModMetadata modMetadata) {
                final List<EntrypointMetadata> entrypointMetadata = modMetadata.getEntrypoints(name);
                for (EntrypointMetadata metadata : entrypointMetadata) {
                    final T entrypoint = createEntrypoint(mod, metadata.getValue(), type);
                    if (entrypoint != null) {
                        entrypoints.add(entrypoint);
                    }
                }
            }
        }
        return entrypoints;
    }

    public static <T> T createEntrypoint(final ModContainer mod, final String value, final Class<T> type) {
        try {
            return DefaultLanguageAdapter.INSTANCE.create(mod, value, type);
        } catch (LanguageAdapterException e) {
            AFLConstants.LOGGER.error("Failed to load early entrypoint {} for mod {}", value, mod.getMetadata().getId(), e);
            return null;
        }
    }

}
