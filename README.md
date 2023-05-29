# TeamPing

## Introduction

Provides in-world location markers (pings) to all party members via party chat.

## Requirements

  * Minecraft 1.8.9

## Installation


## Development Environment

```sh
./gradlew.bat setupDecompWorkspace
```

For eclipse:
```sh
gradlew eclipse
```

import the gradle project in Eclipse.


If at any point you are missing libraries in your IDE, or you've run into
problems you can run "gradlew --refresh-dependencies" to refresh the local
cache. "gradlew clean" to reset everything {this does not effect your code} and
then start the processs again.

Add the following to your program arguments:
```
--tweakClass org.spongepowered.asm.launch.MixinTweaker --mixin mixins.teamping.json
```

## Usage

### Keybindings:
* F - ping blocks
* U - clear the pings
* ; - open Party Menu

### Commands:
* /teamping - shows all commands
* /teamping join \<partyid\> - join the party with an id whose length is in [3, 32] range
* /teamping leave - leave the party
* /teamping list - shows all players in the party
* /teamping kick \<playername\> - kicks a player from the party
* /teamping ban \<playername\> - bans a player from the party
* /teamping promote \<playername\> - promotes a player in a party
* /teamping reconnect - reconnects to a TeamPing server
* /teamping status - shows status of connection to the TeamPing server
* /teamping genInvText - puts invitation link to the chatbox

## Acknowledgements

* Original code and design by [https://github.com/Ivan-Khar](Ivan Kh)
