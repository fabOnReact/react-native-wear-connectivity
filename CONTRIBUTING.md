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
```

Build the project with `yarn android`, open the [react native dev menu][23] and change the bundle location to `your-ip:8082` (for ex. `192.168.18.2:8082`).
Now you can build the WearOS app with the command:

```bash
yarn android
```

[20]: https://reactnative.dev/docs/next/signed-apk-android
[21]: https://developer.android.com/training/wearables/get-started/connect-phone
[22]: https://gist.github.com/assets/24992535/f6cb9f84-dc50-492b-963d-6d9e9396f451 'wear os large round'
[23]: https://reactnative.dev/docs/debugging

### Sending a pull request

> **Working on your first pull request?** You can learn how from this _free_ series: [How to Contribute to an Open Source Project on GitHub](https://app.egghead.io/playlists/how-to-contribute-to-an-open-source-project-on-github).

When you're sending a pull request:

- Prefer small pull requests focused on one change.
- Verify that linters and tests are passing.
- Review the documentation to make sure it looks good.
- Follow the pull request template when opening a pull request.
- For pull requests that change the API or implementation, discuss with maintainers first by opening an issue.

### Linting and tests

[ESLint](https://eslint.org/), [Prettier](https://prettier.io/), [TypeScript](https://www.typescriptlang.org/)

We use [TypeScript](https://www.typescriptlang.org/) for type checking, [ESLint](https://eslint.org/) with [Prettier](https://prettier.io/) for linting and formatting the code, and [Jest](https://jestjs.io/) for testing.

Our pre-commit hooks verify that the linter and tests pass when committing.

### Publishing to npm

We use [release-it](https://github.com/release-it/release-it) to make it easier to publish new versions. It handles common tasks like bumping version based on semver, creating tags and releases etc.

To publish new versions, run the following:

```sh
yarn release
```

### Scripts

The `package.json` file contains various scripts for common tasks:

- `yarn`: setup project by installing dependencies.
- `yarn typecheck`: type-check files with TypeScript.
- `yarn lint`: lint files with ESLint.
- `yarn test`: run unit tests with Jest.
