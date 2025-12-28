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

package de.florianmichael.asmfabricloader.transformer;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.launch.knot.Knot;
import net.lenni0451.classtransform.InjectionCallback;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CInject;
import org.objectweb.asm.tree.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@CTransformer(name = "org.spongepowered.asm.mixin.transformer.MixinInfo")
public class MixinInfoTransformer {

    private static final Method afl$transformMethod;

    static {
        try {
            ClassLoader targetLoader = Knot.getLauncher().getTargetClassLoader(); // Get the target class loader
            if (targetLoader.getClass().getSimpleName().equals("KnotCompatibilityClassLoader")) {
                // In dev environment, we have to use the Fabric loader class loader
                targetLoader = FabricLoader.class.getClassLoader();
            }
            // Get the class loaded in the target class loader to avoid class loader issues
            final Class<?> mixinTransformerBootstrapClass = Class.forName("de.florianmichael.asmfabricloader.loader.feature.classtransform.MixinTransformerBootstrap", true, targetLoader);

            // Get the method
            afl$transformMethod = mixinTransformerBootstrapClass.getDeclaredMethod("transform", String.class, ClassNode.class);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @CInject(method = "loadMixinClass", target = @CTarget("RETURN"), cancellable = true)
    public void transformMixins(String mixinClassName, InjectionCallback callback) throws InvocationTargetException, IllegalAccessException {
        callback.setReturnValue(afl$transformMethod.invoke(null, mixinClassName, callback.getReturnValue()));
    }

}
