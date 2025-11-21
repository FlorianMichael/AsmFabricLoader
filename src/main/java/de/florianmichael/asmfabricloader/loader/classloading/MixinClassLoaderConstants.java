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

package de.florianmichael.asmfabricloader.loader.classloading;

import de.florianmichael.asmfabricloader.loader.AFLFeature;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.lenni0451.classtransform.mappings.AMapper;
import net.lenni0451.classtransform.mappings.MapperConfig;
import net.lenni0451.classtransform.mappings.impl.TinyV2Mapper;
import net.lenni0451.classtransform.mappings.impl.VoidMapper;

public class MixinClassLoaderConstants {

    // We can't use AFLConstants here, because we don't have the system class loader libraries on the class path.
    public static final Map<ModContainer, List<String>> MIXING_TRANSFORMERS = new ConcurrentHashMap<>();

    private static final Map<ModContainer, AMapper> MOD_MAPPINGS = new ConcurrentHashMap<>();

    private static final String DEFAULT_MAPPINGS_PATH = "/afl_mappings.tiny";

    public static AMapper getMapper(final ModContainer mod) {
        return MOD_MAPPINGS.computeIfAbsent(mod, id -> {
            if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                return new VoidMapper(); // In dev environment we don't want to remap
            }

            final Optional<Path> mappingsPath = mod.findPath(getConfiguredMappingsPath(mod));
            if (mappingsPath.isEmpty()) {
                return new VoidMapper();
            }

            final String modId = mod.getMetadata().getId();
            final Path sourcePath = mappingsPath.get();
            try {
                final Path tempFile = Files.createTempFile("afl_mappings_" + modId + "_", ".tiny");
                try (final InputStream in = Files.newInputStream(sourcePath)) {
                    Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
                }
                tempFile.toFile().deleteOnExit();

                AFLConstants.LOGGER.info("Loaded AFL mappings for mod {} from {}", modId, sourcePath);
                return new TinyV2Mapper(MapperConfig.create().fillSuperMappings(true).remapTransformer(true), tempFile.toFile(), "named", "intermediary");
            } catch (final IOException e) {
                AFLConstants.LOGGER.error("I/O error while loading afl mappings for mod {} from {}", modId, sourcePath, e);
                return new VoidMapper();
            } catch (final Throwable t) {
                AFLConstants.LOGGER.error("Failed to load afl mappings for mod {} from {}", modId, sourcePath, t);
                return new VoidMapper();
            }
        });
    }

    private static String getConfiguredMappingsPath(final ModContainer mod) {
        final CustomValue value = AFLFeature.getAflFeature(mod, "mappings_path");
        if (value == null) {
            return DEFAULT_MAPPINGS_PATH;
        }

        try {
            final String path = value.getAsString().trim();
            return path.startsWith("/") ? path : "/" + path;
        } catch (final Exception e) {
            AFLConstants.LOGGER.warn("Invalid custom.asmfabricloader:mappings on mod {}: {}", mod.getMetadata().getId(), e.getMessage());
            return DEFAULT_MAPPINGS_PATH;
        }
    }

}
