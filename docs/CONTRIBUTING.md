# Contributing

Are you considering helping out on Kolibrium? Awesome! 🎉

Please take a moment to review this document **before submitting a Pull Request**.

# Your First Contribution

There are numerous ways to make contributions to Kolibrium, but if you are just starting with contributions, we suggest focusing on the following areas:

- Try out the [Get started](https://github.com/attila-fazekas/kolibrium#get-started) guide and provide feedback by [starting a new discussion](https://github.com/attila-fazekas/kolibrium/discussions/new?category=general). Your feedback helps us to iron out any confusion and is very much appreciated. ❤️
- Review a [Pull Request](https://github.com/attila-fazekas/kolibrium/pulls?q=is%3Apr+is%3Aopen+label%3A%22good+first+issue%22)
- Pick an [issue](https://github.com/attila-fazekas/kolibrium/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22) and fix it
- Update the [documentation](https://github.com/attila-fazekas/kolibrium/blob/main/README.md)

# Filing bug reports

Have you found a bug in code or a typo in documentation? Please [open a Bug Report](https://github.com/attila-fazekas/kolibrium/issues/new?assignees=attila-fazekas&labels=bug&projects=&template=%F0%9F%90%9B-bug-report.md&title=%5B%F0%9F%90%9B+Bug%5D%3A+).

# Proposing new features

Since the project is still in the early stages, this is the perfect time to influence its APIs. If you happen to have a cool idea about DSL design or any other module, or wish to have a Selenium feature implemented in Kolibrium, please [start a new discussion](https://github.com/attila-fazekas/kolibrium/discussions/new?category=ideas).

# Setting up your environment

## Step 1: Fork the repository

1. Make a fork of the repository [on GitHub](https://github.com/attila-fazekas/kolibrium/fork)
2. Open Terminal and clone your fork to create a local copy on your computer.
   ```shell
   git clone git@github.com:your_username/kolibrium.git
   ```
3. Add the base repository as a remote "upstream," so you can pull new changes from the base repository into your fork.
   ```shell
   cd kolibrium
   git remote add upstream git://github.com/attila-fazekas/kolibrium
   ```

## Step 2: Building the project

Open the project in IntelliJ and run `./gradlew testClasses` to compile both `main` and `test` source sets.

# Implementing new features

## API development guidelines

Kolibrium is built with API development guidelines in mind, available at [Kotlin's website](https://kotlinlang.org/docs/jvm-api-guidelines-introduction.html), and it is recommended to get familiar with it before designing new APIs.

### API visibility and backward compatibility

We place great emphasis on [backward compatibility](https://kotlinlang.org/docs/jvm-api-guidelines-backward-compatibility.html) and, to achieve that, we run the [binary compatibility validator](https://kotlinlang.org/docs/jvm-api-guidelines-backward-compatibility.html#binary-compatibility-validator) with every build and have [Explicit API mode](https://kotlinlang.org/docs/jvm-api-guidelines-backward-compatibility.html#explicit-api-mode) enabled across the whole project.

If you don't feel like reading, you can watch [this](https://www.youtube.com/watch?v=6dN8qLvu_BQ&t=359s) awesome presentation from Márton Braun.

### Static analysis

The project also heavily relies on static analysis tools, such as [konsist](https://github.com/LemonAppDev/konsist). The `konsistTest` module enforces project-wide consistency checks, and module-specific consistency checks are implemented within the `konsistTest` source sets under their respective modules.

For linting source code, [ktlint](https://github.com/pinterest/ktlint), for code smell analysis, [detekt](https://github.com/detekt/detekt) is used.

## Testing you changes

When applicable to the module, we write tests, though writing good tests is not always an easy task, especially when the library being developed is itself a testing tool. For instance, to test if the DSL functions construct the requested driver, we check the driver logs. In other cases, such as KSP tests, it's more straightforward to cover functionalities with compile testing.

As of now, writing tests for new features is optional but highly encouraged. If you decide to write tests, please use functions from [kotest](https://kotest.io/docs/assertions/assertions.html)'s rich APIs.

## Making sure your changes compile

Once you are done with the implementation and have written tests, run  `./gradlew clean build` to build the project and run the tests.

Depending on the scope of your change, you might see errors indicating that some static analysis checks were violated. But don't worry, it's easy to fix them.

- `API check failed for project $moduleName.`: This error occurs when a public API's signature has changed or a non-public API became public. To solve this issue, run the `apiDump` task in the module where the change is implemented, as suggested in the build report: `You can run :$moduleName:apiDump task to overwrite API declarations.` This will generate a new `.api` file in the module and should be checked in as part of your Pull Request. Changes in public APIs are always carefully reviewed.
- `> Task :$moduleName:konsistTest FAILED`: Code consistency has broken and needs to be fixed after studying the failing konsist test(s).
- `> Task :$moduleName:detekt FAILED`: Code smell found by detekt, and the detailed analysis points to the exact line that needs to be fixed.

# Get in touch

If you have a question or comment, please [start a new discussion](https://github.com/attila-fazekas/kolibrium/discussions/new?category=q-a).
