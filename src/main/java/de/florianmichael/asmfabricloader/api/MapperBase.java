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

import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.FieldDef;
import net.fabricmc.mapping.tree.MethodDef;
import net.fabricmc.mapping.tree.TinyTree;

import java.util.List;

/**
 * Wrapper for the {@link TinyTree} mappings. This class is used to convert between named and intermediary mappings.
 */
public class MapperBase {

    private final TinyTree mappings;
    private final String from;
    private final String to;

    public MapperBase(final TinyTree mappings, final String from, final String to) {
        this.mappings = mappings;
        this.from = from;
        this.to = to;
    }

    /**
     * @param from The name of the class in the "from" mapping
     * @return The name of the class in the "to" mapping
     */
    public String getClassName(final String from) {
        final ClassDef classDef = getClassDef(from);
        if (classDef == null) {
            return from;
        }

        return classDef.getName(to);
    }

    /**
     * @param name The name of the class in the "from" mapping
     * @return The class definition of the class
     */
    public ClassDef getClassDef(final String name) {
        for (ClassDef classDef : mappings.getClasses()) {
            if (classDef.getName("intermediary").equals(name) || classDef.getName("named").equals(name)) {
                return classDef;
            }
        }
        return null;
    }

    /**
     * @param owner The owner of the method
     * @param from  The name of the method in the "from" mapping
     * @return The name of the method in the "to" mapping
     */
    public String getMethodName(final Class<?> owner, final String from) {
        final MethodDef methodDef = getMethodDef(owner, from, true);
        if (methodDef == null) {
            return from;
        }

        return methodDef.getName(to);
    }

    /**
     * @param owner        The owner of the method
     * @param name         The name of the method in the "from" mapping
     * @param superclasses Whether to search in superclasses of the owner
     * @return The method definition of the method
     */
    public MethodDef getMethodDef(final Class<?> owner, final String name, final boolean superclasses) {
        final List<String> classNames = superclasses ? AsmUtil.getClassStruct(owner) : List.of(AsmUtil.normalizeDescriptor(owner.getName()));
        for (String className : classNames) {
            final ClassDef classDef = getClassDef(className);
            if (classDef == null) {
                continue;
            }

            for (MethodDef method : classDef.getMethods()) {
                if (method.getName(from).equals(name)) {
                    return method;
                }
            }
        }

        return null;
    }

    /**
     * @param owner The owner of the field
     * @param from  The name of the field in the "from" mapping
     * @return The name of the field in the "to" mapping
     */
    public String getFieldName(final Class<?> owner, final String from) {
        final FieldDef fieldDef = getFieldDef(owner, from, true);
        if (fieldDef == null) {
            return from;
        }

        return fieldDef.getName(to);
    }

    /**
     * @param owner        The owner of the field
     * @param name         The name of the field in the "from" mapping
     * @param superclasses Whether to search in superclasses of the owner
     * @return The field definition of the field
     */
    public FieldDef getFieldDef(final Class<?> owner, final String name, final boolean superclasses) {
        final List<String> classNames = superclasses ? AsmUtil.getClassStruct(owner) : List.of(AsmUtil.normalizeDescriptor(owner.getName()));
        for (String className : classNames) {
            final ClassDef classDef = getClassDef(className);
            if (classDef == null) {
                continue;
            }

            for (FieldDef field : classDef.getFields()) {
                if (field.getName(from).equals(name)) {
                    return field;
                }
            }
        }
        return null;
    }

}
