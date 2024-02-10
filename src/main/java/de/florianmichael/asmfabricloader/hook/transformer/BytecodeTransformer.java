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

package de.florianmichael.asmfabricloader.hook.transformer;

import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.COverride;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Map;

// Improves loading speed by caching the opcode names instead of using reflection every time
@CTransformer(name = "org.spongepowered.asm.util.Bytecode")
public class BytecodeTransformer {

    private static final Map<String, Integer> afl$OP_CODE_CACHE = new HashMap<>();

    static {
        for (java.lang.reflect.Field f : Opcodes.class.getDeclaredFields()) {
            if (f.getType() == Integer.TYPE) {
                try {
                    afl$OP_CODE_CACHE.put(f.getName(), f.getInt(null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @COverride
    private static String getOpcodeName(int opcode, String start, int min) {
        if (opcode >= min && afl$OP_CODE_CACHE.containsKey(start) && afl$OP_CODE_CACHE.get(start) == opcode) {
            return start;
        } else {
            return opcode >= 0 ? String.valueOf(opcode) : "UNKNOWN";
        }
    }

}
