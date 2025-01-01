/*
 * This file is part of AsmFabricLoader - https://github.com/FlorianMichael/AsmFabricLoader
 * Copyright (C) 2023-2025 FlorianMichael/EnZaXD <florian.michael07@gmail.com>
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

package de.florianmichael.asmfabricloader.loader;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;

import java.util.Collection;
import java.util.function.BiConsumer;

public class AFLFeature {

    /**
     * Applies the given function to all mods that have the given feature
     *
     * @param modContainers The mods
     * @param name          The name of the feature
     * @param impl          The function to apply
     */
    public static void applyForMods(final Collection<ModContainer> modContainers, final String name, final BiConsumer<ModContainer, CustomValue> impl) {
        for (ModContainer modContainer : modContainers) {
            final CustomValue feature = getAflFeature(modContainer, name);
            if (feature == null) {
                // Skipping mods which don't depend on AsmFabricLoader
                continue;
            }
            impl.accept(modContainer, feature);
        }
    }

    /**
     * Checks if the mod has the afl: namespace or the afl:environment: namespace (e.g. afl:client: or afl:server:)
     * and returns the feature if it exists or null if it doesn't exist or the environment doesn't match
     *
     * @param modContainer The mod container
     * @param name         The name of the feature
     * @return True if the mod has the feature
     */
    public static CustomValue getAflFeature(final ModContainer modContainer, final String name) {
        final var env = FabricLoader.getInstance().getEnvironmentType().name().toLowerCase();
        final var meta = modContainer.getMetadata();

        if (meta.containsCustomValue("afl:" + name)) {
            return meta.getCustomValue("afl:" + name);
        } else if (meta.containsCustomValue("afl:" + env + ":" + name)) {
            return meta.getCustomValue("afl:" + env + ":" + name);
        } else {
            return null;
        }
    }

}
