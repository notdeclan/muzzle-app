# Muzzle

A fully-fledged text messaging app with support for fully 256-bit encrypted messages to provide seamless secure text messages between other users of the app. Created for module CMP309 - Software Development for Mobile Devices at the University of Abertay Dundee. 

[Logo](logo.PNG)

## Features
- 256 bit Encrypted Messages over SMS
- Support for Light and Dark Mode
- Notifications
- Incognito Keyboard

### Encryption
 - Initial Encryption requests are made using ECDH
 - EC Public Keys are sent to each user to derive a AES 256 GCM Key
 
### Android API Features
- Notifications
- Telephony
- SMS
- Custom Broadcasts
- Services

### Storage
- SQLite
- Shared Preferences

### Android X
- Room
- Life Cycle
- Data Binding
- Live Data


## Existing Issues
- Race Condition
  - Issues with corrupted messages and sessions
- Messages
  - Arriving in the wrong order
  - Longer messages failing to decrypt
- MMS Support
  - Currently no support for images, videos, or audio messages

