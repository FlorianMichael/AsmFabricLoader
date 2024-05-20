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

import de.florianmichael.asmfabricloader.api.AsmUtil;
import de.florianmichael.asmfabricloader.loader.AFLFeature;
import de.florianmichael.asmfabricloader.loader.classloading.AFLConstants;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.lenni0451.reflect.stream.RStream;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class Unmixer {

    public static final String class_MIXINCONFIG = "org.spongepowered.asm.mixin.transformer.MixinConfig";
    public static final String field_MIXINCONFIG_globalMixinList = "globalMixinList";

    public Unmixer(final Collection<ModContainer> modContainers) {
        AFLFeature.applyForMods(modContainers, "unmixer", (modContainer, value) -> {
            if (value.getType() == CustomValue.CvType.ARRAY) {
                for (CustomValue customValue : value.getAsArray()) {
                    unloadMixinClass(customValue.getAsString());
                }
            } else {
                for (Map.Entry<String, CustomValue> entry : value.getAsObject()) {
                    if (entry.getValue().getType() != CustomValue.CvType.ARRAY) {
                        continue;
                    }
                    final String packageName = entry.getKey();
                    for (CustomValue customValue : entry.getValue().getAsArray()) {
                        unloadMixinClass(packageName + "." + customValue.getAsString());
                    }
                }
            }
        });
    }

    public void unloadMixinClass(final String folder, final String... mixins) {
        for (String mixin : mixins) {
            unloadMixinClass(folder + "." + mixin);
        }
    }

    /**
     * Unloads a mixin class from the global mixin list
     *
     * @param path The path of the mixin class
     */
    public void unloadMixinClass(final String path) {
        final Set<String> mixins = RStream.of(class_MIXINCONFIG).fields().by(field_MIXINCONFIG_globalMixinList).get();
        mixins.add(AsmUtil.normalizeClassName(path));

        if (AFLConstants.isDebugEnabled()) {
            AFLConstants.LOGGER.warn("Unloaded mixin class {}", AsmUtil.normalizeClassName(path));
        }
    }

}
