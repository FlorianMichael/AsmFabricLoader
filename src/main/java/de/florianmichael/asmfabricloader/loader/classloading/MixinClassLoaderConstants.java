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

package de.florianmichael.asmfabricloader.loader.classloading;

import net.fabricmc.loader.api.FabricLoader;
import net.lenni0451.classtransform.mappings.AMapper;
import net.lenni0451.classtransform.mappings.MapperConfig;
import net.lenni0451.classtransform.mappings.impl.TinyV2Mapper;
import net.lenni0451.classtransform.mappings.impl.VoidMapper;

import java.util.ArrayList;
import java.util.List;

public class MixinClassLoaderConstants {

    // We can't use AFLConstants here, because we don't have the system class loader libraries on the class path.
    public static final List<String> MIXING_TRANSFORMERS = new ArrayList<>();

    public static AMapper MAPPINGS;

    static {
        // We have to map intermediary <-> named in production environment so our injections work
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            // For dev environments, we use the void mapper
            MAPPINGS = new VoidMapper();
        } else {
            // If we are in prod, we copy the mappings from the jar to a temp file and use that
            MAPPINGS = new TinyV2Mapper(MapperConfig.create().fillSuperMappings(true), AFLConstants.class.getResourceAsStream("/mappings/mappings.tiny"), "named", "intermediary");
        }
    }

}
