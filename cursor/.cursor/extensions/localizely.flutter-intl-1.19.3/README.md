# Flutter Intl

VS Code extension to create a binding between your translations from .arb files and your Flutter app. It generates boilerplate code for official Dart [Intl](https://pub.dev/packages/intl) library and adds auto-complete for keys in Dart code.

This plugin is also available for [IntelliJ / Android Studio](https://plugins.jetbrains.com/plugin/13666-flutter-intl).

CLI tool [intl_utils](https://pub.dev/packages/intl_utils) can be used for manual invocation or in CI/CD systems.

[Star us on GitHub](https://github.com/localizely/flutter-intl-vscode 'Star us on GitHub'), and [follow us on Twitter](https://twitter.com/localizely 'Follow us on Twitter')

## Getting started

### 1. Install the VS Code extension

[Install from the Visual Studio Code Marketplace](https://marketplace.visualstudio.com/items?itemName=localizely.flutter-intl) or by [searching within VS Code](https://code.visualstudio.com/docs/editor/extension-gallery#_search-for-an-extension).

### 2. Initialize extension for your project

Open your project, open the command palette and find the [`Flutter Intl: Initialize`](#flutter-intl-initialize) command.

By default `en` locale is added by auto-creating a file `lib/l10n/intl_en.arb`.

*NOTE*: By default the extension generates and maintains files inside `lib/generated/` folder which you should not edit manually. But you should keep these files in your project repository.

### 3. Setup your app

Add dependency needed for localization:

<pre>
dependencies:
    // Other dependencies...
    <b>flutter_localizations:
        sdk: flutter</b>
</pre>

*NOTE:* The extension under the hood uses a globally activated `intl_utils` package for generating localization files. As of version `1.19.0`, the extension checks whether the project uses the `intl_utils` package as a dependency (or dev dependency), and if it does, it uses an identical version during generation. If not, the plugin will either use the globally activated version (if it matches the project configuration) or activate the correct version that aligns with your project's setup.

Setup your `localizationsDelegates` and your `supportedLocales` which will allow to access the strings.

<pre>
import 'package:flutter_localizations/flutter_localizations.dart';
<b>import 'generated/l10n.dart';</b>

class MyApp extends StatelessWidget {
    @override
    Widget build(BuildContext context) {
        return new MaterialApp(
            <b>localizationsDelegates: [
                S.delegate,</b>
                GlobalMaterialLocalizations.delegate,
                GlobalWidgetsLocalizations.delegate,
                GlobalCupertinoLocalizations.delegate,
            <b>],
            supportedLocales: S.delegate.supportedLocales,</b>
            title: 'Flutter Demo',
            home: new MyHomePage(title: 'Flutter Demo Home Page'),
        );
    }
}
</pre>

Additional set up for iOS apps:

To enable localization for iOS apps, extend `ios/Runner/Info.plist` file with supported locales configuration.
This list should be consistent with the locales listed in the `lib/l10n` folder.

    <key>CFBundleLocalizations</key>
    <array>
        <string>en</string>
        <string>de_DE</string>
        ...
    </array>

Current locale can be changed programmatically using `S.load` method and passing the locale:

    S.load(Locale('de', 'DE'));

Access to the current locale:

    Intl.getCurrentLocale()

### 4. Add other locales

You probably want to localize you Flutter app into more than one locale.

You can add more locales with command [`Flutter Intl: Add locale`](#flutter-intl-add-locale).

### 5. Add keys to main ARB file

[ARB files](https://localizely.com/flutter-arb) extension stands for Application Resource Bundle and is used by the Dart [Intl](https://pub.dev/packages/intl) package. It's supported by Google and is official localization library for Dart.

File content example:

    {
        "pageHomeConfirm": "Confirm",
        "pageHomeWelcome": "Welcome {name}",
        "pageHomeWelcomeGender": "{gender, select, male {Hi man!} female {Hi woman!} other {Hi there!}}",
        "pageHomeWelcomeRole": "{role, select, admin {Hi admin!} manager {Hi manager!} other {Hi visitor!}}",
        "pageNotificationsCount": "{howMany, plural, one{1 message} other{{howMany} messages}}"
    }

In the example above we have 5 keys:

* `pageHomeConfirm` - A very simple key
* `pageHomeWelcome` - A key which translation contains a placeholder `name`
* `pageHomeWelcomeGender` - A gender key whose translation depends on a placeholder `gender`. Genders are formatted in [ICU format](https://devpal.co/icu-message-editor/).
* `pageHomeWelcomeRole` - A select key whose translation depends on a placeholder `role`. Selects are formatted in [ICU format](https://devpal.co/icu-message-editor/).
* `pageNotificationsCount` - A plural key whose translation depends on a placeholder `howMany`. Plurals are formatted in [ICU format](https://devpal.co/icu-message-editor/).

Your main ARB file by default is `lib/l10n/intl_en.arb`. When you add new key-value pairs in it and save the file, those keys will be automatically available for auto-complete in your Dart code.

### 6. Reference the keys in Dart code

The ARB file's keys now correspond to methods from the `S` class. For example:

<pre>
Widget build(BuildContext context) {
    return Column(children: [
        Text(
            <b>S.of(context).pageHomeConfirm,</b>
        ),
        Text(
            <b>S.current.pageHomeConfirm,</b>// If you don't have `context` to pass
        ),
    ]);
}
</pre>

In case keys have defined placeholders, or their translation is in Plural, Gender or Select ICU format, you can reference them in your code in a following way:

    S.of(context).pageHomeWelcome("John")
    S.of(context).pageHomeWelcomeGender("male")
    S.of(context).pageHomeWelcomeRole("admin")
    S.of(context).pageNotificationsCount(5)

By default the extension recognizes placeholders order by how they are defined in the main ARB file for that key. If you want to redefine the order of placeholders for usage from Dart code, you can do that by adding meta info for the key:

    "pageHomeWelcomeFullName": "Welcome {lastName} {firstName}",
    "@pageHomeWelcomeFullName": {
        "placeholders": {
            "firstName": {},
            "lastName": {}
        }
    }

In this case you will use given key in your Dart code by passing first name as the first parameter, and then last name as a second parameter:

    S.of(context).pageHomeWelcomeFullName("John", "Doe")

To format a number or date & time, you need to add necessary meta info to the key:

    "commonTotalAmount": "Total: {total}",
    "@commonTotalAmount": {
        "placeholders": {
            "total": {
                "type": "double",
                "format": "currency",
                "optionalParameters": {
                    "symbol": "€",
                    "decimalDigits": 2
                }
            }
        }
    },
    "commonCurrentDateTime": "Date: {date} Time: {time}",
    "@commonCurrentDateTime": {
        "placeholders": {
            "date": {
                "type": "DateTime",
                "format": "yMd"
            },
            "time": {
                "type": "DateTime",
                "format": "Hm"
            }
        }
    },
    "commonCustomDateFormat": "Custom date format: {date}",
    "@commonCustomDateFormat": {
        "placeholders": {
            "date": {
                "type": "DateTime",
                "format": "EEE, M/d/y",
                "isCustomDateFormat": "true"
            }
        }
    }

Reference the keys that contain formatted values from the code:

    S.of(context).commonTotalAmount(125750.00)
    S.of(context).commonCurrentDateTime(DateTime.now(), DateTime.now())
    S.of(context).commonCustomDateFormat(DateTime.now())

Alternatively, you can format the necessary values in Dart code and pass them as formatted strings to the key getter.

## Commands

The extension provides several commands for working with Flutter localization.
You can access all of the below commands from the Command Palette (`Cmd+Shift+P` or `Ctrl+Shift+P`).

### `Flutter Intl: Initialize`

Creates the inital binding between translations from .arb files and your Flutter app.
The command will create a default locale `en`, the locale that will be used for auto-complete support for keys in Dart code.

The following file will be created:

    lib/l10n/intl_en.arb

Additionally this command will update `pubspec.yaml` file with plugin config which you can change manually later:

    flutter_intl:
        enabled: true

### `Flutter Intl: Add locale`

Adds another locale to your Flutter app.
The command will ask you for a new locale.

The following file will be created:

    lib/l10n/<new-locale>.arb

### `Flutter Intl: Remove locale`

Removes an existing locale from your Flutter app.
The command will ask you to pick a locale from the list of existing locales.

The following file will be removed:

    lib/l10n/<existing-locale>.arb

## Editor actions

### `Extract to ARB files`

Extract string to ARB files using [code actions](https://code.visualstudio.com/docs/editor/refactoring#_code-actions-quick-fixes-and-refactorings). The dialog will ask you to enter the string key, which will be extracted to ARB files together with the selected content. [Check demo](https://twitter.com/localizely/status/1255175275454881793)

## Configuration options

Plugin reads its configuration from project's `pubspec.yaml` file.
Here is a full configuration for the plugin:

<pre>
flutter_intl:
  <b>enabled: true</b> # Required. Must be set to true to activate the plugin. Default: false
  class_name: S # Optional. Sets the name for the generated localization class. Default: S
  main_locale: en # Optional. Sets the main locale used for generating localization files. Provided value should consist of language code and optional script and country codes separated with underscore (e.g. 'en', 'en_GB', 'zh_Hans', 'zh_Hans_CN'). Default: en
  arb_dir: lib/l10n # Optional. Sets the directory of your ARB resource files. Provided value should be a valid path on your system. Default: lib/l10n
  output_dir: lib/generated # Optional. Sets the directory of generated localization files. Provided value should be a valid path on your system. Default: lib/generated
  use_deferred_loading: false # Optional. Must be set to true to generate localization code that is loaded with deferred loading. Default: false

  localizely: # Optional settings if you use Localizely platform. Read more: https://localizely.com/blog/flutter-localization-step-by-step/?tab=automated-using-flutter-intl
    project_id: # Configured manually or through 'Connect to Localizely' command. Get it from the https://app.localizely.com/projects page.
    branch: # Configured manually. Get it from the “Branches” page on the Localizely platform, in case branching is enabled and you want to use a non-main branch.
    upload_overwrite: # Set to true if you want to overwrite translations with upload. Default: false
    upload_as_reviewed: # Set to true if you want to mark uploaded translations as reviewed. Default: false
    upload_tag_added: # Optional list of tags to add to new translations with upload (e.g. ['new', 'New translation']). Default: []
    upload_tag_updated: # Optional list of tags to add to updated translations with upload (e.g. ['updated', 'Updated translation']). Default: []
    upload_tag_removed: # Optional list of tags to add to removed translations with upload (e.g. ['removed', 'Removed translation']). Default: []
    download_empty_as: # Set to empty|main|skip, to configure how empty translations should be exported from the Localizely platform. Default: empty
    download_include_tags: # Optional list of tags to be downloaded (e.g. ['include', 'Include key']). If not set, all string keys will be considered for download. Default: []
    download_exclude_tags: # Optional list of tags to be excluded from download (e.g. ['exclude', 'Exclude key']). If not set, all string keys will be considered for download. Default: []
    ota_enabled: # Set to true if you want to use Localizely Over-the-air functionality. Default: false
</pre>

## Example

The complete Flutter project example can be found [here](https://github.com/localizely/flutter-intl-plugin-sample-app).

## Reporting Issues

Issues should be reported in the [Flutter Intl issue tracker](https://github.com/localizely/flutter-intl-vscode/issues).
