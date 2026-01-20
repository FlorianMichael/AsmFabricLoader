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

package de.florianmichael.asmfabricloader.loader.feature.classtransform;

import de.florianmichael.asmfabricloader.loader.classloading.AFLConstants;
import de.florianmichael.asmfabricloader.loader.classloading.MixinClassLoaderConstants;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.launch.knot.Knot;
import net.lenni0451.classtransform.InjectionCallback;
import net.lenni0451.classtransform.TransformerManager;
import net.lenni0451.classtransform.mappings.AMapper;
import net.lenni0451.classtransform.utils.tree.BasicClassProvider;
import net.lenni0451.classtransform.utils.tree.IClassProvider;
import net.lenni0451.reflect.ClassLoaders;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

/**
 * Internal class for the mixins bootstrap, do not use
 */
public class MixinTransformerBootstrap {

    private static final List<TransformerManager> TRANSFORMER = new ArrayList<>();

    static {
        // We use the knot class loader as it is the only one that is able to load the classes we need
        final IClassProvider classProvider = new BasicClassProvider(Knot.getLauncher().getTargetClassLoader());
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) { // Copy classes to the system class loader for non-dev environments
            try {
                ClassLoaders.defineClass(ClassLoader.getSystemClassLoader(), InjectionCallback.class.getName(), classProvider.getClass(InjectionCallback.class.getName()));
            } catch (ClassNotFoundException e) {
                AFLConstants.LOGGER.error("Unable to load InjectionCallback class", e);
            }
        }

        for (final ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            final List<String> mixingTransformers = MixinClassLoaderConstants.MIXING_TRANSFORMERS.get(mod);
            if (mixingTransformers == null) {
                continue;
            }

            final AMapper mapper = MixinClassLoaderConstants.getMapper(mod);
            final TransformerManager manager = new TransformerManager(classProvider, mapper);
            for (final String transformer : mixingTransformers) {
                manager.addTransformer(transformer);
            }
            TRANSFORMER.add(manager);
        }
        MixinClassLoaderConstants.MIXING_TRANSFORMERS.clear();
    }

    /*
    ! THIS METHOD IS CALLED BY RAW BYTECODE - DO NOT CHANGE THE SIGNATURE NOR MOVE THIS METHOD !
     */

    public static ClassNode transform(final String mixinClassName, final ClassNode mixin) {
        System.out.println(mixinClassName);
        final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        mixin.accept(writer);

        byte[] current = writer.toByteArray();
        for (final TransformerManager manager : TRANSFORMER) {
            final byte[] transformed = manager.transform(mixinClassName, current);
            if (transformed != null) {
                current = transformed;
            }
        }

        final ClassNode classNode = new ClassNode();
        final ClassReader classReader = new ClassReader(current);
        classReader.accept(classNode, 0);
        return classNode;
    }

}
