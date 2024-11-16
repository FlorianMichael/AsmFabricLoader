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

import com.google.gson.Gson;
import de.florianmichael.asmfabricloader.api.EarlyRiser;
import de.florianmichael.asmfabricloader.api.event.InstrumentationEntrypoint;
import de.florianmichael.asmfabricloader.loader.AFLFeature;
import de.florianmichael.asmfabricloader.loader.classloading.AFLConstants;
import de.florianmichael.asmfabricloader.loader.classloading.MixinClassLoaderConstants;
import de.florianmichael.asmfabricloader.loader.feature.classtransform.ClassTransformJson;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.lenni0451.classtransform.TransformerManager;
import net.lenni0451.classtransform.utils.tree.BasicClassProvider;
import net.lenni0451.reflect.Agents;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.util.*;

public final class ClassTransform {
    private final Gson GSON = new Gson();

    private final Map<ModContainer, List<ClassTransformJson>> modsToTransformerJsons = new HashMap<>();
    private final TransformerManager javaTransformerManager;

    public ClassTransform(final Collection<ModContainer> modContainers) {
        AFLFeature.applyForMods(modContainers, "classtransform", (modContainer, value) -> {
            try {
                for (CustomValue transformerValue : value.getAsArray()) {
                    parseTransformer(modContainer, transformerValue.getAsString());
                }
            } catch (Exception e) {
                parseTransformer(modContainer, value.getAsString());
            }
        });
        if (AFLConstants.isDebugEnabled()) {
            AFLConstants.LOGGER.info("Loaded {} transformer config {}", modsToTransformerJsons.size(), modsToTransformerJsons.size() != 1 ? "s" : "");
        }

        javaTransformerManager = new TransformerManager(new BasicClassProvider(), MixinClassLoaderConstants.MAPPINGS);
        for (List<ClassTransformJson> value : modsToTransformerJsons.values()) {
            for (ClassTransformJson config : value) {
                for (String transformer : config.javaTransformers()) {
                    javaTransformerManager.addTransformer(config.packageName() + "." + transformer);
                }
            }
        }

        try {
            final Instrumentation instrumentation = Agents.getInstrumentation();
            EarlyRiser.invokeEntrypoints(InstrumentationEntrypoint.getEntrypointName(), InstrumentationEntrypoint.class,
                    entrypoint -> entrypoint.onGetInstrumentation(instrumentation));

            javaTransformerManager.hookInstrumentation(instrumentation);
            AFLConstants.LOGGER.error("KnotClassLoader, you fool! You fell victim to one of the classic blunders!");
        } catch (Exception e) {
            AFLConstants.LOGGER.error("Failed to hook instrumentation", e);
        }
    }

    private void parseTransformer(final ModContainer provider, final String filePath) {
        if (!filePath.endsWith(".json")) {
            AFLConstants.LOGGER.error("Transformer config file {} from {} is not a json file", filePath, provider.getMetadata().getId());
            return;
        }

        provider.findPath(filePath).ifPresentOrElse(path -> {
            try {
                final String content = Files.readString(path);
                final ClassTransformJson config = GSON.fromJson(content, ClassTransformJson.class);
                modsToTransformerJsons.computeIfAbsent(provider, modContainer -> new ArrayList<>()).add(config);

                for (String mixinTransformer : config.mixinTransformers()) {
                    MixinClassLoaderConstants.MIXING_TRANSFORMERS.add(config.packageName() + "." + mixinTransformer);
                }
            } catch (IOException e) {
                AFLConstants.LOGGER.error("Failed to read transformer config file {} from {}", filePath, provider.getMetadata().getId(), e);
            }
        }, () -> AFLConstants.LOGGER.error("Transformer config file {} from {} does not exist", filePath, provider.getMetadata().getId()));
    }


    /**
     * Registers a java transformer
     *
     * @param folder The folder of the transformer
     * @param paths  The paths to the transformer
     */
    public void registerJavaTransformer(final String folder, final String... paths) {
        for (String path : paths) {
            javaTransformerManager.addTransformer(folder + "." + path);
        }
    }

    /**
     * Registers a java transformer
     *
     * @param path The path to the transformer
     */
    public void registerJavaTransformer(final String path) {
        javaTransformerManager.addTransformer(path);
    }

    /**
     * Returns true if the mod is transforming classes
     *
     * @param mod The mod to check
     * @return If the mod is transforming classes
     */
    public boolean isTransforming(final ModContainer mod) {
        return modsToTransformerJsons.containsKey(mod);
    }

    /**
     * Returns the transformer config for a mod
     *
     * @param mod The mod to get the config for
     * @return The transformer config for the mod or null if the mod is not transforming classes
     */
    public List<ClassTransformJson> getTransformers(final ModContainer mod) {
        if (!modsToTransformerJsons.containsKey(mod)) {
            return null;
        }
        return modsToTransformerJsons.get(mod);
    }

    /**
     * Returns the amount of transformers for a mod
     *
     * @param mod   The mod to get the transformer count for
     * @param java  If java transformers should be counted
     * @param mixin If mixin transformers should be counted
     * @return The amount of transformers for the mod
     */
    public int getTransformerCount(final ModContainer mod, final boolean java, final boolean mixin) {
        if (!modsToTransformerJsons.containsKey(mod)) return 0;
        int sum = 0;
        for (ClassTransformJson classTransformJson : modsToTransformerJsons.get(mod)) {
            if (java) sum += classTransformJson.javaTransformers().size();
            if (mixin) sum += classTransformJson.mixinTransformers().size();
        }
        return sum;
    }

    /**
     * Returns the amount of transformers for all mods
     *
     * @param java  If java transformers should be counted
     * @param mixin If mixin transformers should be counted
     * @return The amount of transformers for all mods
     */
    public int getAllTransformerCount(final boolean java, final boolean mixin) {
        return getAllTransformers(java, mixin).size();
    }

    /**
     * Returns all transformers for all mods
     *
     * @param java  If java transformers should be returned
     * @param mixin If mixin transformers should be returned
     * @return All transformers for all mods
     */
    public List<String> getAllTransformers(final boolean java, final boolean mixin) {
        final List<String> transformers = new ArrayList<>();
        for (List<ClassTransformJson> value : modsToTransformerJsons.values()) {
            for (ClassTransformJson classTransformJson : value) {
                if (java) transformers.addAll(classTransformJson.javaTransformers());
                if (mixin) transformers.addAll(classTransformJson.mixinTransformers());
            }
        }
        return transformers;
    }

    public TransformerManager getJavaTransformerManager() {
        return javaTransformerManager;
    }

}
