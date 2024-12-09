# Contributing

Are you considering helping out with Kolibrium?? Awesome! 🎉

Please take a moment to review this document **before submitting a Pull Request**.

## Your first contribution

There are numerous ways to make contributions to Kolibrium, but if you are just starting with contributions, we suggest focusing on the following areas:

1. Follow our [Get Started](https://kolibrium.dev/docs/get-started) guide and provide feedback through a [new discussion](https://github.com/attila-fazekas/kolibrium/discussions/new?category=general). Your feedback helps us to iron out any confusion and is very much appreciated. ❤️
2. Review a [Pull Request](https://github.com/attila-fazekas/kolibrium/pulls?q=is%3Apr+is%3Aopen+label%3A%22good+first+issue%22).
3. Address an open [issue](https://github.com/attila-fazekas/kolibrium/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22) labeled as "good first issue".
4. Improve our [documentation](https://kolibrium.dev/docs/what-is-kolibrium).

## Reporting issues

### Bug reports
Have you found a bug in the code or a typo in the documentation? Please [open a Bug Report](https://github.com/attila-fazekas/kolibrium/issues/new?assignees=attila-fazekas&labels=bug&projects=&template=%F0%9F%90%9B-bug-report.md&title=%5B%F0%9F%90%9B+Bug%5D%3A+).

### Feature proposals
Since Kolibrium is in its early stages, this is an ideal time to influence its APIs. If you have ideas about Domain-Specific Language (DSL) design, other modules, or wish to have a Selenium feature implemented in Kolibrium, please [start a new discussion](https://github.com/attila-fazekas/kolibrium/discussions/new?category=ideas).

## Development setup

### Prerequisites
- Git
- JDK (minimum version 17)
- IntelliJ IDEA (recommended) or another Java IDE
- Gradle (wrapper included in repository)

### Step 1: Fork the repository

1. Create a fork of the repository [on GitHub](https://github.com/attila-fazekas/kolibrium/fork).
2. Open Terminal and clone your fork to create a local copy on your computer:
   ```shell
   git clone git@github.com:your_username/kolibrium.git
   ```
3. Add the base repository as a remote "upstream", so you can pull new changes from the base repository into your fork:
   ```shell
   cd kolibrium
   git remote add upstream git://github.com/attila-fazekas/kolibrium
   ```

### Step 2: Building the project

1. Open the project in your IDE.
2. Run `./gradlew testClasses` to compile both `main` and `test` source sets.

## Development guidelines

### API development

Kolibrium follows API development guidelines, available at [Kotlin's website](https://kotlinlang.org/docs/jvm-api-guidelines-introduction.html), and we recommend getting familiar with it before designing new APIs.

#### API compatibility

We place great emphasis on backward compatibility and, to achieve that, we run the [Binary Compatibility Validator](https://kotlinlang.org/docs/api-guidelines-backward-compatibility.html#use-the-binary-compatibility-validator) with every build and have [Explicit API Mode](https://kotlinlang.org/docs/api-guidelines-backward-compatibility.html#specify-return-types-explicitly) enabled across all modules.

For a comprehensive overview, watch Márton Braun's awesome [presentation on API design and compatibility](https://www.youtube.com/watch?v=6dN8qLvu_BQ&t=359s).

### Code quality tools

We employ several static analysis tools:
- [Konsist](https://github.com/LemonAppDev/konsist): Enforces project-wide and module-specific consistency
- [ktlint](https://github.com/pinterest/ktlint): Handles code formatting
- [detekt](https://github.com/detekt/detekt): Performs code smell analysis

### Testing guidelines

When applicable to the module, we write tests. Writing good tests is not always an easy task, especially when the library being developed is itself a testing tool.
For instance, to test whether the DSL functions construct the requested driver, we check the driver logs. In other cases, such as KSP tests, it's more straightforward to cover functionalities with compile testing.

As of now, writing tests for new features is optional but highly encouraged. If you decide to write tests, please use [kotest](https://kotest.io/docs/assertions/assertions.html) for assertions when implementing tests.

### Build verification

Once you are done with the implementation and have written tests, run `./gradlew clean build` to build the project and run the tests.

#### Common build issues and solutions

1. API check failures:
   ```
   API check failed for project $moduleName
   ```
   Meaning: This error occurs when a public API's signature has changed or a non-public API became public.  
   Resolution: Run `./gradlew :$moduleName:apiDump` to update API declarations. This will generate a new `.api` file in the module and should be checked in as part of your Pull Request. We always carefully review changes in public APIs.

2. Consistency check failures:
   ```
   Task :$moduleName:konsistTest FAILED
   ```
   Meaning: Code consistency has broken and needs to be fixed.  
   Resolution: Review and fix failing konsist test(s).

3. Code quality issues:
   ```
   Task :$moduleName:detekt FAILED
   ```
   Meaning: Code smell found by detekt.  
   Resolution: Address the specific code smell issues identified in the report.

## Get in touch

If you have a question or comment, please [start a new discussion](https://github.com/attila-fazekas/kolibrium/discussions/new?category=q-a).