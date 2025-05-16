# Change Log

All notable changes to the "flutter-intl" extension will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 1.19.3 - 2023-08-04

- Add support for locale formats based on ISO 639-2/3 languages and UN-M49 regions

## 1.19.2 - 2023-05-12

- Support projects that rely on Dart 3

## 1.19.1 - 2022-11-24

- Fix reading config when dev_dependencies section is omitted

## 1.19.0 - 2022-11-22

- Fix black frames caused by async initialization of localization messages

- Use the intl_utils version provided in the list of dependencies

## 1.18.2 - 2022-03-30

- Update links

## 1.18.1 - 2022-01-17

- Improve error handling for invalid config files

## 1.18.0 - 2021-12-27

- Add custom date-time format option

## 1.17.0 - 2021-11-09

- Add number and date-time format options

- Add support for json strings

- Ignore unnecessary_string_interpolations and unnecessary_string_escapes lint rules in generated files

## 1.16.0 - 2021-07-14

- Add support for tagging uploaded string keys to Localizely

- Add support for download tagged string keys from Localizely

- Fix issue with translations that contain tags

## 1.15.0 - 2021-04-29

- Add support for compound messages

- Format generated files

- Add missing return types in generated files

- Ignore avoid_escaping_inner_quotes lint rule in generated files

- Fix escaping special chars

## 1.14.0 - 2021-03-09

- Support null-safety

## 1.13.0 - 2020-10-19

- Make generated directory path configurable

- Extend configuration with deferred loading parameter

- Ignore common lint warnings for the `l10n.dart` file

## 1.12.1 - 2020-10-15

- Fix unnecessary generation within workspaces with multiple projects

## 1.12.0 - 2020-10-09

- Extend Localizely configuration with the download_empty_as parameter used for setting a fallback for empty translations during download

## 1.11.0 - 2020-10-03

- Make ARB directory path configurable

## 1.10.1 - 2020-09-18

- Fix unzipping issues during download

## 1.10.0 - 2020-09-03

- Extend Localizely configuration with the branch parameter

## 1.9.1 - 2020-06-17

- Improve performance

- Add useful error message for invalid ARB files

## 1.9.0 - 2020-06-08

- Reference the key without passing the context

- Provide main translation as Dart doc on getters in `l10n.dart` file

- Suppress lint warnings for getters that do not follow the lowerCamelCase style within `l10n.dart` file

## 1.8.0 - 2020-05-11

- Add support for the Localizely SDK

- Fix lint warnings for the `l10n.dart` file

## 1.7.0 - 2020-05-04

- Add integration with Localizely

## 1.6.0 - 2020-04-27

- Add code action for extracting string keys to ARB files

## 1.5.0 - 2020-04-23

- Support select messages
- Make order of supported locales consistent

## 1.4.2 - 2020-04-13

- Make generated files consistent

## 1.4.1 - 2020-03-30

- Update order of supported locales
- Replace `dynamic` with concrete type for generated Dart methods
- Handle empty plural and gender forms
- Update `l10n.dart` file template

## 1.4.0 - 2020-03-16

- Add support for locales with script code
- Fix locale loading issue when country code is not provided

## 1.3.0 - 2020-02-04

- Make main locale configurable

## 1.2.1 - 2020-01-21

- Add curly braces around placeholders when they are followed by alphanumeric or underscore character

## 1.2.0 - 2019-12-28

- Add option to configure generated localization class name

## 1.1.0 - 2019-12-26

- Add support for gender messages
- Regenerate localization files on .arb file rename

## 1.0.0 - 2019-12-16

- Initial release
