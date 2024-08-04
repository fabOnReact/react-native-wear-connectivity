# Contributing

Contributions are always welcome, no matter how large or small!

We want this community to be friendly and respectful to each other. Please follow it in all your interactions with the project. Before contributing, please read the [code of conduct](./CODE_OF_CONDUCT.md).

## Development workflow

This project is a monorepo managed using [Yarn workspaces](https://yarnpkg.com/features/workspaces). It contains the following packages:

- The library package in the root directory.
- The mobile android app is in the `example/` directory.
- The Wear OS android app is in the `watch-example/` directory.

To get started with the project, run `yarn` in the root directory to install the required dependencies for each package:

```sh
yarn
```

### Pair the Android and WearOS emulators

- Create a new emulator of type [WearOS Large round][22].
- Pair the Android emulator with the Wear OS emulator. Follow this [instructions][21].

### Build the Mobile App

The first step is building the react-native Android mobile app.

```bash
cd example
yarn install
yarn start
# Now build android
yarn android
```

### Build the WearOS App

```bash
cd watch-example
yarn install
yarn start --port=8082
# Now build android
yarn android
```

[20]: https://reactnative.dev/docs/next/signed-apk-android
[21]: https://developer.android.com/training/wearables/get-started/connect-phone
[22]: https://gist.github.com/assets/24992535/f6cb9f84-dc50-492b-963d-6d9e9396f451 'wear os large round'
[23]: https://reactnative.dev/docs/debugging
