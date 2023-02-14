# NPS Browser
NPS Client app for android written in kotlin.

## Building
The application targets android 10 (API 29) with a minimum version of android 6 (API 23).

1. Create a `keystore` using android studio: `Build > Generate Signed Bundle / APK > Next > Create new`
2. Create a `keystore.properties` file in the root directory of the repo.
3. Specify the keystore location, alias and keys in `keystore.properties`\
For example:
```
storePassword=password
keyPassword=password2
keyAlias=alias1
storeFile=keyStore.jks
```
4. Move your newly generated `keyStore.jks` file to `app/`

Note: keyStore is relative to the `app` directory. So it's very **recommended** to save it as `app/keyStore.jks`

## Contributing
Contributions are very welcome, just open a PR if you have a piece of code to contribute and we'll iron out the details when it's time.

## License
This repository is licensed under the GPLv2 license.

## Disclaimer
No mentions of any links or piracy related content will be tolerated on this repository, and any persons asking for help with such things will be ignored, not receive any sort of help or potentially get banned.
