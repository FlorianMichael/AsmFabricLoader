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

package de.florianmichael.asmfabricloader.test;

import de.florianmichael.asmfabricloader.api.event.InstrumentationEntrypoint;
import de.florianmichael.asmfabricloader.api.event.PrePreLaunchEntrypoint;
import de.florianmichael.asmfabricloader.api.event.PrePrePreLaunchEntrypoint;

import java.lang.instrument.Instrumentation;

public class TestMod implements InstrumentationEntrypoint, PrePreLaunchEntrypoint, PrePrePreLaunchEntrypoint {

    public static final int TEST_COUNT = 5;
    private static int passedTestCount;

    @Override
    public void onGetInstrumentation(Instrumentation instrumentation) {
        passTest("onGetInstrumentation");
    }

    @Override
    public void onMixinPluginLaunch() {
        passTest("onMixinPluginLaunch");

        // Use system property to bypass class loading issues, counterpart in JavaTransformerTest
        if (System.getProperty("asmfabricloader.test.java") != null) {
            passTest("JavaTransformerTest.testInject()");
        }
    }

    @Override
    public void onLanguageAdapterLaunch() {
        System.setProperty("asmfabricloader.debug", "true");
        passTest("onLanguageAdapterLaunch");
    }

    public static void passTest(final String test) {
        log("Passed test: " + test);
        passedTestCount++;
        if (passedTestCount == TEST_COUNT) {
            log("All tests passed!");
        }
    }

    public static void log(final String message) { // Can't use logger since code execution is too early
        System.out.println("[TestMod] " + message);
    }

}
