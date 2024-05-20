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

package de.florianmichael.asmfabricloader.test.transformer;

import de.florianmichael.asmfabricloader.test.TestMod;
import net.fabricmc.loader.api.FabricLoader;
import net.lenni0451.classtransform.annotations.CInline;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CInject;

@CTransformer(FabricLoader.class)
public class JavaTransformerTest {

    @CInline
    @CInject(method = "getInstance", target = @CTarget("HEAD"))
    public static void testInject() {
        if (System.getProperty("asmfabricloader.test.java") == null) {
            System.setProperty("asmfabricloader.test.java", "true");
            TestMod.passTest("JavaTransformerTest.testInject()");
        }
    }

}
