<div align="center">
<p><img src="https://raw.githubusercontent.com/attila-fazekas/kolibrium/main/assets/kolibrium_logo.png" alt="kolibrium_logo.png"></p>
<h1>Modern testing toolkit for Kotlin</h1>
<p><a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg" alt="License"></a>
<a href="https://github.com/attila-fazekas/kolibrium/actions/workflows/gradle.yml"><img src="https://github.com/attila-fazekas/kolibrium/actions/workflows/gradle.yml/badge.svg" alt="Build"></a>
<a href="https://github.com/attila-fazekas/kolibrium/actions/workflows/codeql.yml"><img src="https://github.com/attila-fazekas/kolibrium/actions/workflows/codeql.yml/badge.svg" alt="CodeQL"></a>
<a href="https://central.sonatype.com/search?namespace=dev.kolibrium"><img src="https://img.shields.io/maven-central/v/dev.kolibrium/kolibrium-selenium.svg" alt="Maven Central"></a>
<a href="https://javadoc.io/doc/dev.kolibrium/kolibrium-selenium"><img src="https://img.shields.io/badge/API_reference-KDoc-blue" alt="KDoc"></a>  
<a href="https://img.shields.io/badge/Project%20status-Experimental-red.svg"><img src="https://img.shields.io/badge/Project%20status-Experimental-red.svg" alt="Project status"></a></p>
</div>

Build simple and maintainable test automation faster with Kolibrium — a comprehensive, type-safe testing toolkit for modern Kotlin.

Kolibrium provides type-safe HTTP clients with integrated test harness generation for API testing, alongside declarative browser automation. Write idiomatic Kotlin tests with minimal boilerplate and compile-time safety.

Kolibrium is divided into several subprojects (modules), each of which can be used either independently or in conjunction with others.

- `api`: provides a code generation tool that uses [Kotlin Symbol Processing (KSP)](https://kotlinlang.org/docs/ksp-overview.html) to automatically generate type-safe HTTP client code and test harness functions from annotated request models. 
- `selenium`: provides core WebDriver functionality including Page object base class, browserTest harness, element locator delegates, and extensible decorator framework

# Documentation

The documentation is available at [https://kolibrium.dev](https://kolibrium.dev).

# Contributing

Please read [CONTRIBUTING](docs/CONTRIBUTING.md) before submitting your pull requests.

# Project status

This project serves as a playground to explore new ideas and push the boundaries of what can be done when Kotlin's language features and tools are combined with Selenium. Although it’s experimental, the goal is to make it production-ready under version 1.0.0 once the project's APIs and Kotlin's [context parameters](https://github.com/Kotlin/KEEP/blob/context-parameters/proposals/context-parameters.md) are stabilized.  
