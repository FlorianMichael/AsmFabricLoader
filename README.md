# AsmFabricLoader

A series of cursed Fabric hacks and utilities which break everything.

<!-- TOC -->
* [AsmFabricLoader](#asmfabricloader)
  * [Why?](#why)
  * [Contact](#contact)
  * [How to add this to your project](#how-to-add-this-to-your-project)
    * [Gradle/Maven](#gradlemaven)
    * [Jar File](#jar-file)
  * [How to use](#how-to-use)
    * [PreLaunch Entry Points](#prelaunch-entry-points)
    * [Get a Java instrumentation (Requires Java 9+)](#get-a-java-instrumentation-requires-java-9)
    * [Unmixer](#unmixer)
    * [Jar Booter](#jar-booter)
    * [Early riser](#early-riser)
    * [Class Transform (Requires Java 9+)](#class-transform-requires-java-9)
      * [Setting up mappings](#setting-up-mappings)
      * [Transformer config](#transformer-config)
      * [Example transformer](#example-transformer)
      * [Registering transformers](#registering-transformers)
    * [Environments](#environments)
    * [Debug options](#debug-options)
    * [Testing](#testing)
<!-- TOC -->

## Why?

There are many use cases for direct ASM injections and the power to re-transform every loaded class.

This is basically a cleanup rewrite of [GrossFabricHacks](https://github.com/Devan-Kerman/GrossFabricHacks/tree/master)
using the [ClassTransform](https://www.github.com/Lenni0451/ClassTransform)
and [Reflect](https://www.github.com/Lenni0451/Reflect) libraries.

It also provides the same functionality as [MixinSquared](https://github.com/Bawnorton/MixinSquared).

## Contact

If you encounter any issues, please report them on
the [issue tracker](https://github.com/FlorianMichael/AsmFabricLoader/issues).  
If you just want to talk or need help with AsmFabricLoader feel free to join
my [Discord](https://florianmichael.de/discord).

## How to add this to your project

### Gradle/Maven

To use BaseProject with Gradle/Maven you can
use [Maven Central](https://mvnrepository.com/artifact/de.florianmichael/AsmFabricLoader), [Lenni0451's Maven](https://maven.lenni0451.net/#/releases/de/florianmichael/AsmFabricLoader)
or [Jitpack](https://jitpack.io/#FlorianMichael/AsmFabricLoader).  
You can also find instructions how to implement it into your build script there.

### Jar File

If you just want the latest jar file you can download it from the
GitHub [Actions](https://github.com/FlorianMichael/AsmFabricLoader/actions) or use
the [Release](https://github.com/FlorianMichael/AsmFabricLoader/releases).

## How to use

AsmFabricLoader allows you to access a series of utilities and hacks to make your life easier.

### PreLaunch Entry Points

AsmFabricLoader adds an even more early entry point than FabricMC's `PreLaunchEntrypoint`.

You can use the `PrePreLaunchEntrypoint`, which will be called when Fabric bootstraps the mixin service. The name
of the entry point is `afl:prePreLaunch`.

The `PrePrePreLaunchEntrypoint` is the earliest entry point possible. It is getting bootstrapped before the loader
even starts. The name of the entry point is `afl:prePrePreLaunch`.

You can use both just like the `PreLaunchEntrypoint` from FabricMC.

### Get a Java instrumentation (Requires Java 9+)

AsmFabricLoader allows you to get a Java instrumentation instance which you can use to transform classes directly.

You can use the `InstrumentationEntrypoint` to get the instrumentation instance. The name of the entry point is
`afl:instrumentation`.

### Unmixer

AsmFabricLoader adds a new API which allows you to unload Mixin classes meaning they aren't injected anymore. This is
useful if another mod is breaking your Mixins or if you want to unload Mixins after they have been applied.

```java
final Unmixer unmixer = AsmFabricLoader.getLoader().getUnmixer();

// This method accepts the mixin classes of all loaded mods including the Fabric internals
unmixer.unloadMixinClass("net/fabricmc/api/mixin/v1/MixinEnvironment");
```

You can also use the Unmixer via the `fabric.mod.json` file:

```json
{
  "custom": {
    "afl:unmixer": [
      "net.fabricmc.api.mixin.v1.MixinEnvironment"
    ]
  }
}
```

You can also group the packages together:

```json
{
  "custom": {
    "afl:unmixer": {
      "net.fabricmc.api.mixin.v1": [
        "MixinEnvironment"
      ]
    }
  }
}
```

### Jar Booter

AsmFabricLoader allows you to define a list of folders where it will load all jar files from to the front of the
classpath.
This can be useful if you want to allow the user to replace a library jar file you are using with a different version.

```json
{
  "custom": {
    "afl:jarbooter": [
      "libs"
    ]
  }
}
```

This will load all jar files from the run directory/libs folder to the front of the classpath.

### Early riser

Utility to create and invoke mod entrypoints before Fabric has finished loading entrypoints. See the `EarlyRiser` class

### Class Transform (Requires Java 9+)

AsmFabricLoader bootstraps the [ClassTransform](https://www.github.com/Lenni0451/ClassTransform) library which allows
you to transform classes directly without using Mixins.

#### Setting up mappings

AsmFabricLoader uses tiny mappings internally to remap Minecraft classes for transformers.

AsmFabricLoader itself **does not** ship these mappings. Your mod has to provide the Tiny mappings file so that AFL can
load it at runtime.

AsmFabricLoader looks for a resource named:

- `/afl_mappings.tiny`

You can override this path per mod via `fabric.mod.json` using `afl:mappings_path`. The value must be a string
and is resolved as a resource path inside the mod jar (a leading / is added automatically if missing), for example:

```json
{
  "custom": {
    "afl:mappings_path": "custom/afl_mappings.tiny"
  }
}
```

Example for **Gradle Kotlin DSL** (`build.gradle.kts`) in your mod project:

```kotlin
tasks {
    jar {
        dependsOn(configurations["mappings"])
        val mappingsJar = configurations["mappings"].resolvedConfiguration.resolvedArtifacts.firstOrNull {
            it.name.contains("mappings") || it.name.contains("yarn")
        }?.file

        if (mappingsJar != null && mappingsJar.exists()) {
            val mappingsFile = zipTree(mappingsJar).matching {
                include("mappings/mappings.tiny")
            }.singleFile

            from(mappingsFile) {
                rename { "afl_mappings.tiny" }
            }
        }
    }
}
```

Example for **Gradle Groovy DSL** (`build.gradle`) in your mod project:

```groovy
tasks {
    jar {
        dependsOn(configurations.mappings)
        def mappingsJar = configurations.mappings
            .resolvedConfiguration
            .resolvedArtifacts
            .find { it.name.contains("mappings") || it.name.contains("yarn") }
            ?.file

        if (mappingsJar != null && mappingsJar.exists()) {
            def mappingsFile = zipTree(mappingsJar).matching {
                include "mappings/mappings.tiny"
            }.singleFile

            from(mappingsFile) {
                rename { "afl_mappings.tiny" }
            }
        }
    }
}
```

#### Transformer config

You just have to create a json file called `<modid>.classtransform.json` in your resources folder and add the following

```json
{
  "package": "net.example.injection.transformer",
  "java": [
  ],
  "mixins": [
  ]
}
```

The `package` field is the package where your transformers are located.

Now AsmFabricLoader provides two ways to add transformers to the ClassTransform library:

The `java` field will apply all the transformers using a runtime injected java agent after Mixins and the loader
has been initialized. Meaning you can transform all classes in your runtime, even Java standard library classes.

The `mixins` field will apply all the transformers before Mixins are getting applied, therefore you can only add
transformers targeting other mods mixin classes. These transformers will be applied before those Mixins are getting
applied to the game code, this is basically a replacement for [MixinSquared](https://github.com/Bawnorton/MixinSquared).

#### Example transformer

```java
@CTransformer(String.class)
public class Test {

    @CInline
    @CInject(method = "equals", target = @CTarget("HEAD"))
    public void printInput(Object anObject, InjectionCallback callback) {
        System.out.println("Input: " + anObject);
    }

}
```

#### Registering transformers

To register your json file, you have to add the following to your `fabric.mod.json` file:

```json
{
  "custom": {
    "afl:classtransform": "<modid>.classtransform.json"
  }
}
```

### Environments

AsmFabricLoader allows you to limit the execution of all the features to specific environments. This can be useful if
you want to use the same mod jar file for multiple environments.

This snippet will only apply the unmixer if the mod is loaded on the client:

```json
{
  "custom": {
    "afl:client:unmixer": [
    ]
  }
}
```

### Debug options

There are a few system properties you can enable to debug AsmFabricLoader:

```-DAsmFabricLoader.debug=true``` will print a lot of useful debug information including loading states

```-Dclasstransform.dumpClasses=``` will dump all classes transformed by ClassTransform
to `run directory` / `classtransform`

### Testing

The `TestMod` submodule contains a Fabric mod to test various features of AsmFabricLoader. You can run the tests by
building both root project and the test mod project and then running them in a production environment.

A test running successfully will be indicated by the following message in the console:

```
[TestMod] <NameOfTheTest> passed!
```

All tests have been passed if you see the following message in the console:

```
[TestMod] All tests passed!
```
