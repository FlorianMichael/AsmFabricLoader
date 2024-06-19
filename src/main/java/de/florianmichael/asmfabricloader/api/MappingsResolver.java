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
import net.fabricmc.mapping.tree.TinyMappingFactory;
import net.fabricmc.mapping.tree.TinyTree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all mappings related stuff. This class is used to convert between named and intermediary mappings.
 */
@Deprecated
public class MappingsResolver {

    private MapperBase intermediaryToNamed;
    private MapperBase namedToIntermediary;

    public MappingsResolver() {
        try {
            if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
                loadMappings();
            } else if (AFLConstants.isDebugEnabled()) {
                AFLConstants.LOGGER.warn("Skipping mapping loading in development environment!");
            }
        } catch (IOException e) {
            AFLConstants.LOGGER.error("Unable to load mappings!", e);
        }
    }

    /**
     * Loads the mappings from the mappings.tiny file
     *
     * @throws IOException           If the file could not be read
     */
    public void loadMappings() throws IOException, IllegalStateException {
        if (namedToIntermediary != null && intermediaryToNamed != null) {
            return;
        }
        final InputStream mappingFile = AFLConstants.getMappingsFile();
        if (mappingFile == null) throw new RuntimeException("Unable to load mappings!");

        final TinyTree mappings = TinyMappingFactory.load(new BufferedReader(new InputStreamReader(mappingFile, StandardCharsets.UTF_8)));
        namedToIntermediary = new MapperBase(mappings, "named", "intermediary");
        intermediaryToNamed = new MapperBase(mappings, "intermediary", "named");
    }

    /**
     * @return The mapping environment for intermediary to named mappings
     */
    public MapperBase named() {
        return intermediaryToNamed;
    }

    /**
     * @return The mapping environment for named to intermediary mappings
     */
    public MapperBase intermediary() {
        return namedToIntermediary;
    }

    /**
     * @return Whether the mappings are loaded or not (Usually only false in dev environment, except if {@link #loadMappings()} was called manually)
     */
    public boolean areMappingsLoaded() {
        return intermediaryToNamed != null && namedToIntermediary != null;
    }

}
