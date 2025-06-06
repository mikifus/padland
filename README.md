# Padland
Padland is a tool for Android to manage collaborative documents based on the Etherpad or CryptPad technology.

<p align="center" style="text-align:center;">
  <a href="https://f-droid.org/repository/browse/?fdid=com.mikifus.padland" alt='Download from F-Droid' target="_blank">
    <img src="https://f-droid.org/badge/get-it-on.png" height="90"/>
  </a>

  <a href='https://play.google.com/store/apps/details?id=com.mikifus.padland'>
    <img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png'  height="90" />
  </a>
</p>

<p align="center" style="text-align:center;">
  <img alt='Padland Logo' src='./metadata/en-US/images/icon.png'  height="90" />
</p>

<p align="center" style="text-align:center;">
  <picture>
    <source media="(prefers-color-scheme: light)" srcset="./metadata/en-US/images/phoneScreenshots/1.png">
    <source media="(prefers-color-scheme: dark)" srcset="./metadata/en-US/images/phoneScreenshots/2.png">
    <img alt="Screenshot 1" src="./metadata/en-US/images/phoneScreenshots/1.png" width="150">
  </picture>

  <picture>
    <source media="(prefers-color-scheme: light)" srcset="./metadata/en-US/images/phoneScreenshots/3.png">
    <source media="(prefers-color-scheme: dark)" srcset="./metadata/en-US/images/phoneScreenshots/4.png">
    <img alt="Screenshot 2" src="./metadata/en-US/images/phoneScreenshots/3.png" width="150">
  </picture>
</p>

## Translations
Thanks to all the volunteers that already contributed!
More translations are still needed as well as spell checking and keeping the current ones up to date.

## Custom servers

This features allows the user to add a custom server like "pad.myserver.com". It doesn't replace the default servers, it adds customization.

The feature is found in the settings menu as "Server list". The New server dialog will promp when the button is pressed. The server name is just an arbitrary name for the user.
The url must be something like "https://examplehost.com". No more and no less info than the protocol and the host.

To try your new server create a new pad and choose it. You can make it the default option in the settings menu.

**Note:** The server hosts the user adds and their subdomains are now considered part of the _whitelist_. This means that http connections to these domains are allowed.
buildTypes
## Current version
3.3

### v3.3
- Merge chinese translation

### v3.2
- Minor bug fixes.
- Minor QoL improvements.
- Added CryptPad support.

### v3.1
- Minor bug fixes.

### v3.0
- Migrated database to Room.
- Better implementation of Material Design.
- Extreme refactoring keeping the same features and adding some minor ones.
- Export and import data.

### v2.0
- Migrated code to Kotlin.
- Implemented AndroidX and Material Design.
- Theme dark/light follows device.

### v1.9
- Fixed a vulnerability.

### v1.7
- Dropped support for PrimaryPad.
- Removed old workarounds and hacks to correctly display Etherpad-based documents.

### v1.6
- Dropped support for the following server (you can add them manually):
  - Etherpad.net
  - Titanpad.com
  - Meetingwords.com
  - Piratenpad.de

### v1.5
- More SQL injection fixes.
- Added pad URL copy function
- Dropped support for piratepad.net

### v1.4.1
- Fixed an unnoticed SQL injection security issue 

### v1.4
- Fixed a bug for API < 23
- Full url can be now pasted for pad name and gets automatically parsed
- Added option to edit pads
- Allows to assign a local name for pads
- Allows better NodeJS interaction for compatibility with MyPads

### v1.3.7
- Fixed a major bug that didn't allow to create new pads
- Fixed a bug that didn't allow to select a recently created server as default
- CompileSdkVersion is now 26
- MinSdkVersion is now 14
- Now Etherpad Lite usage is selected by default

### v1.3.6
- Updated french translation.

### v1.3.5
- Removed a bug when adding custom servers if a trailing slash was set by the user

### v1.3.4
- SSL error message includes a link to learn how to manage certificates
- Now pad names can be any valid URL

### v1.3.3
- Screen rotation bug fixed
- External links are now opened in default browser
- Added SSL error management
- Minor bug fixes

### v1.3.2
- Added a fancy introduction to understand the app
- Color picker on the preferences option
- Custom servers feature: add your own servers
- HTTP login: Access a pad behind an http auth
- Added support for URLs with complex parameters
- All known bugs

### v1.1.4
- Added Malayalam translation
- Added Piratenpad server

### v1.1.3
- Added French translation
- Fixed a bug that caused the app to crash in some devices

### v1.1.2
- Removed unused permissions
- Added Etherpad.net server

### v1.1.1
- Added German translation
- Added Japanese translation

### v1.1
- Added spanish and catalan translation
- Ready for a beta release

### v1.0
- Now documents can be classified in groups
- Pads will by default appear in the "Unclassified" group
- Groups can be added and deleted
- Pads can be moved to groups in bulk
- Design improvements
- Security: Protected from undesired hosts to run java methods from javascript (disables some tracking too).
- A hosts whitelist was added. Supports "*" wildcard for subdomains.
- Improved compatibility with older Android versions (not lower than SDK 14)

### v0.3
- Added a parameter to count the times a pad was accessed
- Added a loading animation
- Improved stability issues
- Landscape orientation is not forced anymore
- Pad names can't be free strings now
- Fixed minor bugs

### v0.2
- Added a view with pad data
- The "last used date" parameter is updated correctly

### v0.1
- Added multiple-server support
- Just commit

### License
----
Apache