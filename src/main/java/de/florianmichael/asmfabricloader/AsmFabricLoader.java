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

package de.florianmichael.asmfabricloader;

import de.florianmichael.asmfabricloader.api.EarlyRiser;
import de.florianmichael.asmfabricloader.api.event.PrePrePreLaunchEntrypoint;
import de.florianmichael.asmfabricloader.loader.classloading.AFLConstants;
import de.florianmichael.asmfabricloader.loader.feature.JarBooter;
import de.florianmichael.asmfabricloader.loader.feature.Unmixer;
import de.florianmichael.asmfabricloader.loader.feature.ClassTransform;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.Collection;
import java.util.List;

public class AsmFabricLoader {

    private static AsmFabricLoader loader;

    private final ClassTransform classTransform;
    private final Unmixer unmixer;
    private final JarBooter jarBooter;

    public static void install() {
        if (loader != null) {
            throw new IllegalStateException("AsmFabricLoader is already installed");
        }
        AFLConstants.LOGGER.error("no good? no, this man is definitely up to evil.");
        loader = new AsmFabricLoader();
    }

    public AsmFabricLoader() {
        EarlyRiser.invokeEntrypoints(PrePrePreLaunchEntrypoint.getEntrypointName(),
                PrePrePreLaunchEntrypoint.class, PrePrePreLaunchEntrypoint::onLanguageAdapterLaunch);

        final Collection<ModContainer> mods = FabricLoader.getInstance().getAllMods();

        classTransform = new ClassTransform(mods);
        unmixer = new Unmixer(mods);
        jarBooter = new JarBooter(mods);
    }

    /**
     * Returns all mods that are AsmFabricLoader mods (have the afl: namespace)
     *
     * @return All AsmFabricLoader mods
     */
    public List<ModContainer> getAflMods() {
        return FabricLoader.getInstance().getAllMods().stream().filter(this::isAflMod).toList();
    }

    /**
     * Returns if a mod is an AsmFabricLoader mod (has the afl: namespace)
     *
     * @param mod The mod to check
     * @return If the mod is an AsmFabricLoader mod
     */
    public boolean isAflMod(final ModContainer mod) {
        return mod.getMetadata().getCustomValues().keySet().stream().anyMatch(s -> s.startsWith("afl:"));
    }

    public ClassTransform getClassTransform() {
        return classTransform;
    }

    public Unmixer getUnmixer() {
        return unmixer;
    }

    public JarBooter getJarBooter() {
        return jarBooter;
    }

    public static AsmFabricLoader getLoader() {
        return loader;
    }

}
