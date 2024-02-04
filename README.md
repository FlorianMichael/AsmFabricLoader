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
    * [Get a Java instrumentation](#get-a-java-instrumentation)
    * [Mappings API](#mappings-api)
    * [Unmixer](#unmixer)
    * [Jar Booter](#jar-booter)
    * [Class Transform](#class-transform)
      * [Transformer config](#transformer-config)
      * [Example transformer](#example-transformer)
      * [Registering transformers](#registering-transformers)
    * [Environments](#environments)
    * [Debug options](#debug-options)
<!-- TOC -->

## Why?

There are many use cases for direct ASM injections and the power to re-transform every loaded class.

This is basically a cleanup rewrite of [GrossFabricHacks](https://github.com/Devan-Kerman/GrossFabricHacks/tree/master)
using
the [ClassTransform](https://www.github.com/Lenni0451/ClassTransform)
and [Reflect](https://www.github.com/Lenni0451/Reflect) libraries.

It also provides the same functionality as [MixinSquared](https://github.com/Bawnorton/MixinSquared).

## Contact

If you encounter any issues, please report them on the
[issue tracker](https://github.com/FlorianMichael/AsmFabricLoader/issues).  
If you just want to talk or need help with AsmFabricLoader feel free to join my
[Discord](https://discord.gg/BwWhCHUKDf).

## How to add this to your project

### Gradle/Maven

To use BaseProject with Gradle/Maven you can
use [Maven Central](https://mvnrepository.com/artifact/de.florianmichael/AsmFabricLoader), [Lenni0451 server](https://maven.lenni0451.net/#/releases/de/florianmichael/AsmFabricLoader)
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

### Get a Java instrumentation

AsmFabricLoader allows you to get a Java instrumentation instance which you can use to transform classes directly.

You can use the `InstrumentationEntrypoint` to get the instrumentation instance. The name of the entry point is
`afl:instrumentation`.

### Mappings API

FabricLoader 0.15.0+ removed the `tiny-mappings-parser` library and therefore most of the mappings API. AsmFabricLoader
re-adds the mappings API and allows you to use it in your mods.

```java
final MappingsResolver mappings = AsmFabricLoader.getLauncher().getMappingsResolver();

if (mappings.areMappingsLoaded()) { // This will be true in production environments
    // Now you get the mapping path you want to translate to, for example:
    final String className=mappings.named().getClassName("net/minecraft/class_7833");
    // this field should now be net/minecraft/util/math/RotationAxis

    // You can also use the getClassDef method to get a ClassDef object from the tiny-mappings-parser library
    final ClassDef classDef = mappings.named().getClassDef("net/minecraft/class_7833");
}
```

### Unmixer

AsmFabricLoader adds a new API which allows you to unload Mixin classes meaning they aren't injected anymore. This is
useful if another mod is breaking your Mixins or if you want to unload Mixins after they have been applied.

```java
final Unmixer unmixer = AsmFabricLoader.getLauncher().getUnmixer();

// This method accepts the mixin classes of all loaded mods including the Fabric internals
unmixer.unloadMixinClass("net/fabricmc/api/mixin/v1/MixinEnvironment");
```

You can also use the Unmixer via the `fabric.mod.json` file:

```json
{
  "custom": {
    "afl:unmixer": [
      "net/fabricmc/api/mixin/v1/MixinEnvironment"
    ]
  }
}
```

You can also group the packages together:

```json
{
  "custom": {
    "afl:unmixer": {
      "net/fabricmc/api/mixin/v1": [
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

### Class Transform

AsmFabricLoader bootstraps the [ClassTransform](https://www.github.com/Lenni0451/ClassTransform) library which allows
you to transform classes directly without using Mixins.

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
