# BGM Player

This mod fully overwrite the game bgm system.

## Features

- More continuous playing. (Will not interrupt by join save/pause screen/etc.)
- MP3 format support.
- In-game music selection screen. (Visit via modmenu or sound options screen.)

## Configure

You can use resource reloading to reload all configurations.

Game will load all music and icon into memory when reloading resources, which may cause a loading lag.

### Root configuration

This config will automatically create in `.minecraft/config/bgm-player/config.json` after game launch.

```json5
{
  //Allow: playlist, single_looping, random
  "mode": "random",
  //The ordinal in music list screen. If one of music is not here, it will append at last.
  "playlist": [
    //Fill the json file name of music config without file extension.
    //Game will automatically play the first music at launch, you can define the first one at here.
  ]
}
```

### Music configuration

All music config save in `.minecraft/config/bgm-player/music/`.

```json5
{
  "name": "Name display in the music list",
  //Relative path of .minecraft/config/bgm-player/resource/
  "path": "The music path, support wav, ogg and mp3",
  "author": "(Optional) Music author",
  "album": "(Optional) Music album",
  //Relative path of .minecraft/config/bgm-player/resource/
  "icon": "(Optional) The icon path, support png and jpg"
}
```