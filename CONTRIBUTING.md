# Contributing

Contributions are always welcome, no matter how large or small!

We want this community to be friendly and respectful to each other. Please follow it in all your interactions with the project. Before contributing, please read the [code of conduct](./CODE_OF_CONDUCT.md).

https://github.com/user-attachments/assets/100ee026-550f-4d84-b180-58874ae2b395

### How to run the React Native Mobile example

You need to clone the `react-native-wear-connectivity` project, build and run the mobile app example.

```
git clone https://github.com/fabOnReact/react-native-wear-connectivity
cd react-native-wear-connectivity 
yarn
cd example
yarn
yarn android
```

### How to run the Jetpack Compose WearOS example

1) Clone the WearOS Jetpack Compose [example](https://github.com/fabOnReact/wearos-communication-with-rn)

```
git clone https://github.com/fabOnReact/wearos-communication-with-rn
```

2) Open the project with android studio, build and run it on an [Android WearOS emulator](https://github-production-user-asset-6210df.s3.amazonaws.com/24992535/303911079-f6cb9f84-dc50-492b-963d-6d9e9396f451.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAVCODYLSA53PQK4ZA%2F20250125%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20250125T110158Z&X-Amz-Expires=300&X-Amz-Signature=4bd2be95943124fe34fb13e6a54e9a2fe8a9c06d1eb8afdf005ce02cf43c90d1&X-Amz-SignedHeaders=host).

3) Now you can pair the WearOS emulator with the Android Mobile Emulator as explained in these [instructions](https://developer.android.com/training/wearables/get-started/connect-phone).

### Detailed explanation on how WearOS (Jetpack Compose) communicates with React Native (Android Mobile)

**Sending messages from Jetpack Compose WearOS to React Native Mobile Device**

[sendMessageToClient](https://github.com/fabOnReact/wearos-communication-with-rn/blob/371e6c5862d49ccbff08ab951a26284a216daf97/app/src/main/java/com/wearconnectivityexample/presentation/MainActivity.kt#L75-L87) is implemented on Jetpack Compose WearOS to send messages to the React Native Mobile App. `sendMessageToClient` is triggered on WearOS when [clicking](https://github.com/fabOnReact/wearos-communication-with-rn/blob/371e6c5862d49ccbff08ab951a26284a216daf97/app/src/main/java/com/wearconnectivityexample/presentation/WearApp.kt#L31) on the watch Button Component.

```kotlin
fun sendMessageToClient(node: Node) {
    val jsonObject = JSONObject().apply {
        put("event", "message")
        put("text", "hello")
    }
    try {
        val sendTask = Wearable.getMessageClient(applicationContext).sendMessage(
            node.getId(), jsonObject.toString(), null
        )
    } catch (e: Exception) {
        Log.w("WearOS: ", "e $e")
    }
}
```

The WearOS `sendMessageToClient` function retrieves the devices connected via bluetooth to the WearOS device, and sends a JSON payload to those devices. 

The payload is:

```javascript
{
   event: "message",
   text: "this is the message parameter",
}
```

The React Native Mobile App uses `watchEvents.on(eventName, callback)` to listen to the `message` event and to increase the number displayed in the React Native Mobile App. The implementation in the React Native Mobile example is in [CounterScreen/index.android.tsx](https://github.com/fabOnReact/react-native-wear-connectivity/blob/2f936622422e197c22bef228b44eb24b46c878ae/example/src/CounterScreen/index.android.tsx#L14-L16).

```javascript
useEffect(() => {
  const unsubscribe = watchEvents.on('message', () => {
    setCount((prevCount) => prevCount + 1);
  });


  return () => {
    unsubscribe();
  };
}, []);
```

**Sending messages from React Native Mobile Device to Jetpack Compose WearOS**

The React Native Mobile App Example sends messages to the WearOS Jetpack Compose example with [sendMessage](https://github.com/fabOnReact/react-native-wear-connectivity/blob/2f936622422e197c22bef228b44eb24b46c878ae/example/src/CounterScreen/index.android.tsx#L29-L33).

```javascript
const sendMessageToWear = () => {
  setDisabled(true);
  const json = { text: 'hello' };
  sendMessage(json, onSuccess, onError);
};
```

The Jetpack Compose WearOS app implements [onMessageReceived](https://github.com/fabOnReact/wearos-communication-with-rn/blob/371e6c5862d49ccbff08ab951a26284a216daf97/app/src/main/java/com/wearconnectivityexample/presentation/MainActivity.kt#L89-L95) and updates the Counter number on the screen when the message is received:

```kotlin
override fun onMessageReceived(messageEvent: MessageEvent) {
    val jsonObject = JSONObject(messageEvent.path)
    val event = jsonObject.getString("event")
    if (event.equals("message")) {
        count = count + 1;
    }
}
```

onMessageReceived modifies the [count state variable](https://github.com/fabOnReact/wearos-communication-with-rn/blob/371e6c5862d49ccbff08ab951a26284a216daf97/app/src/main/java/com/wearconnectivityexample/presentation/MainActivity.kt#L31) and re-renders the Counter component with a new [text](https://github.com/fabOnReact/wearos-communication-with-rn/blob/371e6c5862d49ccbff08ab951a26284a216daf97/app/src/main/java/com/wearconnectivityexample/presentation/WearApp.kt#L46).

You can copy the [implementation](https://github.com/fabOnReact/wearos-communication-with-rn/tree/main/app/src/main/java/com/wearconnectivityexample/presentation) from the example, or follow the [instructions above](https://github.com/fabOnReact/wearos-communication-with-rn?tab=readme-ov-file#both-apps-share-the-same-package-name-and-applicationid) to rename package name, application id and change the signing key to pair that example with your React Native App.


### Sending a pull request

> **Working on your first pull request?** You can learn how from this _free_ series: [How to Contribute to an Open Source Project on GitHub](https://app.egghead.io/playlists/how-to-contribute-to-an-open-source-project-on-github).

When you're sending a pull request:

- Prefer small pull requests focused on one change.
- Review the documentation to make sure it looks good.
- Follow the pull request template when opening a pull request.
- For pull requests that change the API or implementation, discuss with maintainers first by opening an issue.

### Publishing to npm

We use [release-it](https://github.com/release-it/release-it) to make it easier to publish new versions. It handles common tasks like bumping version based on semver, creating tags and releases etc.

To publish new versions, run the following:

```sh
yarn release
```
