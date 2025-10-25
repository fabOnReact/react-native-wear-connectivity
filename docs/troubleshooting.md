# Troubleshooting

Common issues when connecting mobile and wearable apps.

## Wearable app not installed on mobile device

The mobile device must have the Google Play Wear app installed to pair with the WearOS device.

## WearOS device too far for Bluetooth connection

Logcat may show `Device is too far for bluetooth connection` when the watch is out of range of the phone.

## Failed to deliver message to AppKey

Ensure the WearOS and mobile apps share the same package name, application ID, and signing key. Messages cannot be delivered if these values differ.
