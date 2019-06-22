The contents of this folder are interpreted as follows:

Any folder is assumed to be the name of a faction. Each folder should contain unit sprites for that faction.
Each faction directory may also contain a text file named "basis.txt", which may contain one line: the name of
another faction folder to use as a backup if the current folder is missing any unit images.
If "basis.txt" is missing, then "Thorn" will be used as the default.

Any PNG images will be parsed as a faction color palette. The name of the image will be used as the in-game
name for that color. Each image should be seven pixels by two pixels; the first 6 pixels of the first row are used
to recolor unit sprites in-game. The first 6 pixels of the second row are used to recolor building sprites in-game.
The last pixel on the first row is a representative color for the palette - normally one of the middle palette colors.
The final pixel on the second row is used to order the palettes in-game, via the RGB values. When determining order,
Red has the most weight, then Green, then Blue. For example, (1, 0, 0) will come after (0, 255, 255), but before (2, 0, 0).