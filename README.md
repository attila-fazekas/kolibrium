<div align="center">
<p><img src="https://raw.githubusercontent.com/attila-fazekas/kolibrium/main/assets/kolibrium_logo.png" alt="kolibrium_logo.png"></p>
<h1>Kotlin library for Selenium tests</h1>
<p><a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg" alt="License"></a>
<a href="https://github.com/attila-fazekas/kolibrium/actions/workflows/gradle.yml"><img src="https://github.com/attila-fazekas/kolibrium/actions/workflows/gradle.yml/badge.svg" alt="Build"></a>
<a href="https://github.com/attila-fazekas/kolibrium/actions/workflows/codeql.yml"><img src="https://github.com/attila-fazekas/kolibrium/actions/workflows/codeql.yml/badge.svg" alt="CodeQL"></a>
<a href="https://central.sonatype.com/search?namespace=dev.kolibrium"><img src="https://img.shields.io/maven-central/v/dev.kolibrium/kolibrium-selenium.svg" alt="Maven Central"></a>
<a href="https://javadoc.io/doc/dev.kolibrium/kolibrium-selenium"><img src="https://javadoc.io/badge2/dev.kolibrium/kolibrium-selenium/javadoc.svg" alt="Javadocs"></a>
<a href="https://img.shields.io/badge/Project%20status-Experimental-red.svg"><img src="https://img.shields.io/badge/Project%20status-Experimental-red.svg" alt="Project status"></a></p>
</div>

Build simple and maintainable automation faster with Kolibrium.

Kolibrium is a declarative Kotlin library designed to reduce boilerplate code and find better abstractions to express Selenium tests in a compact way.  

Quickly bring your test automation efforts to life with less code and easy-to-read APIs.  

Kolibrium is divided into several subprojects (modules), each of which can be used either independently or in conjunction with others.

- `selenium`: offers a range of delegate functions for locating elements
- `ksp`: offers code generation with [Kotlin Symbol Processing (KSP)](https://kotlinlang.org/docs/ksp-overview.html) for part of the Page Object classes
- `dsl`: offers [Domain-Specific Languages (DSLs)](https://kotlinlang.org/docs/type-safe-builders.html) functions for creating, configuring, and interacting with driver instances
- `junit`: offers an extension to write JUnit tests without boilerplate

You can decide to go all-in on Kolibrium by opting for all four modules or choosing just one or two. For example, you could use the Selenium library in conjunction with DSL and JUnit, or you could use Selenium with KSP.  

# Documentation

The documentation is available at [https://kolibrium.dev](https://kolibrium.dev).

# Project status

This project serves as a playground to explore new ideas and push the boundaries of what can be done when Kotlin's language features and tools are combined with Selenium. Although it’s experimental, the goal is to make it production-ready under version 1.0.0 once the project's APIs and Kotlin's [context parameters](https://github.com/Kotlin/KEEP/blob/context-parameters/proposals/context-parameters.md) are stabilized.  