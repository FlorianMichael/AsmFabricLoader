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

package de.florianmichael.asmfabricloader.loader.bootstrap;

import de.florianmichael.asmfabricloader.AsmFabricLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.LanguageAdapterException;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.launch.knot.Knot;
import net.lenni0451.reflect.stream.RStream;

public class AFLLanguageAdapter implements LanguageAdapter {

    @Override
    public native <T> T create(ModContainer mod, String value, Class<T> type) throws LanguageAdapterException;

    // Forces the fabric.debug.disableClassPathIsolation system property to be true which disables the classpath isolation
    // This is needed because we need to access classes loaded in the system classloader
    private static void unlockSystemClassloader() {
        ((FabricLoaderImpl) FabricLoader.getInstance()).getGameProvider().unlockClassPath(Knot.getLauncher());
        RStream.of("net.fabricmc.loader.impl.launch.knot.KnotClassDelegate").fields().by("DISABLE_ISOLATION").set(true);
    }

    static {
        unlockSystemClassloader();
        AsmFabricLoader.install();
    }

}
