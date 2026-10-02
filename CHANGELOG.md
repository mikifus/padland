# Padland changelog

## v4.0
- General bugfixes and improvements in stability
- Added offline mode support
- Added sorting options
- Added a 'select all' button in the list
- Long press the new pad button to add from clipboard
- Added debug options, includes error popup and logs
- Imports don't duplicate data anymore

## v3.6
- Minor fixes for Android 16 compatibility

## v3.5
- Minor fixes and library updates

## v3.4
- Fix bug #76 that arised in API 35 (Android 15)
- Fix styles in AppIntro

## v3.3
- Merge chinese translation

## v3.2
- Minor bug fixes.
- Minor QoL improvements.
- Added CryptPad support.

## v3.1
- Minor bug fixes.

## v3.0
- Migrated database to Room.
- Better implementation of Material Design.
- Extreme refactoring keeping the same features and adding some minor ones.
- Export and import data.

## v2.0
- Migrated code to Kotlin.
- Implemented AndroidX and Material Design.
- Theme dark/light follows device.

## v1.9
- Fixed a vulnerability.

## v1.7
- Dropped support for PrimaryPad.
- Removed old workarounds and hacks to correctly display Etherpad-based documents.

## v1.6
- Dropped support for the following server (you can add them manually):
  - Etherpad.net
  - Titanpad.com
  - Meetingwords.com
  - Piratenpad.de

## v1.5
- More SQL injection fixes.
- Added pad URL copy function
- Dropped support for piratepad.net

## v1.4.1
- Fixed an unnoticed SQL injection security issue

## v1.4
- Fixed a bug for API < 23
- Full url can be now pasted for pad name and gets automatically parsed
- Added option to edit pads
- Allows to assign a local name for pads
- Allows better NodeJS interaction for compatibility with MyPads

## v1.3.7
- Fixed a major bug that didn't allow to create new pads
- Fixed a bug that didn't allow to select a recently created server as default
- CompileSdkVersion is now 26
- MinSdkVersion is now 14
- Now Etherpad Lite usage is selected by default

## v1.3.6
- Updated french translation.

## v1.3.5
- Removed a bug when adding custom servers if a trailing slash was set by the user

## v1.3.4
- SSL error message includes a link to learn how to manage certificates
- Now pad names can be any valid URL

## v1.3.3
- Screen rotation bug fixed
- External links are now opened in default browser
- Added SSL error management
- Minor bug fixes

## v1.3.2
- Added a fancy introduction to understand the app
- Color picker on the preferences option
- Custom servers feature: add your own servers
- HTTP login: Access a pad behind an http auth
- Added support for URLs with complex parameters
- All known bugs

## v1.1.4
- Added Malayalam translation
- Added Piratenpad server

## v1.1.3
- Added French translation
- Fixed a bug that caused the app to crash in some devices

## v1.1.2
- Removed unused permissions
- Added Etherpad.net server

## v1.1.1
- Added German translation
- Added Japanese translation

## v1.1
- Added spanish and catalan translation
- Ready for a beta release

## v1.0
- Now documents can be classified in groups
- Pads will by default appear in the "Unclassified" group
- Groups can be added and deleted
- Pads can be moved to groups in bulk
- Design improvements
- Security: Protected from undesired hosts to run java methods from javascript (disables some tracking too).
- A hosts whitelist was added. Supports "*" wildcard for subdomains.
- Improved compatibility with older Android versions (not lower than SDK 14)

## v0.3
- Added a parameter to count the times a pad was accessed
- Added a loading animation
- Improved stability issues
- Landscape orientation is not forced anymore
- Pad names can't be free strings now
- Fixed minor bugs

## v0.2
- Added a view with pad data
- The "last used date" parameter is updated correctly

## v0.1
- Added multiple-server support
- Just commit

