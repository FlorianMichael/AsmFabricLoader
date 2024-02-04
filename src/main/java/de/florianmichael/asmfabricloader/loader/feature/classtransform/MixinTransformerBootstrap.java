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

package de.florianmichael.asmfabricloader.loader.feature.classtransform;

import de.florianmichael.asmfabricloader.loader.classloading.MixinClassLoaderConstants;
import de.florianmichael.asmfabricloader.loader.classloading.AFLConstants;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.launch.knot.Knot;
import net.lenni0451.classtransform.InjectionCallback;
import net.lenni0451.classtransform.TransformerManager;
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

    private final static TransformerManager TRANSFORMER;

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

        TRANSFORMER = new TransformerManager(classProvider, MixinClassLoaderConstants.MAPPINGS);
        for (String transformer : MixinClassLoaderConstants.MIXING_TRANSFORMERS) {
            TRANSFORMER.addTransformer(transformer);
        }
        MixinClassLoaderConstants.MIXING_TRANSFORMERS.clear();
    }

    /*
    ! THIS METHOD IS CALLED BY RAW ASM - DO NOT CHANGE THE SIGNATURE NOR MOVE THIS METHOD !
     */

    public static ClassNode transform(final String mixinClassName, final ClassNode mixin) {
        final var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        mixin.accept(writer);

        final var newBytes = TRANSFORMER.transform(mixinClassName, writer.toByteArray());
        if (newBytes == null) {
            return mixin;
        }

        final var classNode = new ClassNode();
        final var classReader = new ClassReader(newBytes);

        classReader.accept(classNode, 0);
        return classNode;
    }

}
