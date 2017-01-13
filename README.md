# Padland
Padland is a tool to manage, share, remember and read collaborative documents based on the Etherpad technology in Android.

<a href="https://f-droid.org/repository/browse/?fdid=com.mikifus.padland" target="_blank">
<img src="https://f-droid.org/badge/get-it-on.png" height="90"/></a>

![Introduction screenshot 1](docs/Screenshot_1.png)
![Introduction screenshot 2](docs/Screenshot_2.png)
![Introduction screenshot 4](docs/Screenshot_4.png)
![Introduction screenshot 5](docs/Screenshot_5.png)

## Translations
Thanks to all the volunteers that already contributed!
More translations are still needed as well as spell checking and keeping the current ones up to date.

## Custom servers

This features allows the user to add a custom server like "something.titanpad.com" or "pad.myserver.com". It doesn't replace the default servers, it adds customization.

The feature is found in the settings menu as "Server list". The New server dialog will promp when the button is pressed. The server name is just an arbitrary name for the user.
The url must be something like "https://examplehost.com". No more and no less info than the protocol and the host.
The user will be requested to choose whether the server runs Etherpad Lite or not. Most servers do, but titanpad.com doesn't, for example. Use the Advanced Options combined with the Etherpad Lite checkbox to understand them.

To try your new server create a new pad and choose it. You can make it the default option in the settings menu.

**Note:** The server hosts the user adds and their subdomains are now considered part of the _whitelist_. This means that http connections to these domains are allowed.

## Opening a URL

The user can now share an URL to the app in order to open it. Keep in mind that the host server must be previously added, otherwise the URL will be considered unsafe. 

## Current version
1.3.3

## Changelog
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

## Technology
Padland depends on:
- jQuery
- etherpad-lite-jquery-plugin


### License
----
Apache



[Etherpad]:http://etherpad.org/
[etherpad-lite-jquery-plugin]:https://github.com/ether/etherpad-lite-jquery-plugin
