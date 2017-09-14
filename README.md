# Google Sheets Cordova Plugin

## Description
This plugin has not been extensively tested, use it at your own risk.

## Supported platforms
This plugin, at it's current state, brings support only for the android platform, iOS might be supported in the near future.

## Installation instructions
Change directory to the root of your cordova project and run:
```bash
cordova plugin add https://github.com/kvaldivia/cordova-plugin-google-sheets.git#master
```

## Usage
Unline most other plugins, ther is no Typescript wrapper for this one, at least
not yet. Anyway, the plugin can be still accessed using the javascript fashion:

```javascript
cordova.require('cordova-plugin-google-sheets.GoogleSheets');
```
The previous code listing returns an instance of the GoogleSheets javascript interface.

Documentation is still under work, feel free to browse the code of the
[SheetsApiTest app](https://github.com/kvaldivia/SheetsApiPluginTest) for further reference.
