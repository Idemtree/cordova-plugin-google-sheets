# Google Sheets Cordova Plugin

## Description
This plugin has not been extensively tested, use it at your own risk.

## Supported platforms
This plugin, at it's current state, brings support only for the android platform.

## Installation instructions
Change directory to the root of your cordova project and run:
```bash
cordova plugin add cordova-plugin-google-sheets
```
Or if you are using [Ionic Framework®](https://ionicframework.com).
```bash
ionic cordova plugin add cordova-plugin-google-sheets
```

## Usage
If you are writing your app with [Ionic Framework®](https://ionicframework.com). There is a typescript wrapper available at 
[NPM](https://www.npmjs.com/package/@sumaq-plugins/google-sheets). Just change directory into your project's root and type.
```bash
npm install @sumaq-plugins/google-sheets --save
```
Then just use the import statement like this:
```typescript
import { GoogleSheets } from '@sumaq-plugins/google-sheets'
```

If you want to stick with javascript, you can access the plugin using cordova's require:
```javascript
cordova.require('cordova-plugin-google-sheets.GoogleSheets');
```
The previous code listing returns an instance of the GoogleSheets javascript interface.

Documentation is still under work, feel free to browse the code of the
[SheetsApiTest app](https://github.com/kvaldivia/SheetsApiPluginTest) for further reference.
