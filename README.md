# Mindustry Auto Export / Mirror Sync Mod
Automatically exports and imports your Mindustry save data between devices using a user‑defined sync folder.
This mod creates a self‑contained autosync system:
Mindustry exports its full game‑data ZIP into a public folder, and on the next launch it imports that ZIP if it is newer (“newest wins”).
Perfect for setting up automatic phone ↔ PC save synchronization using tools like FolderSync, OneDrive, Syncthing, Dropbox, etc.

# ✨ Features
## ✔ Auto‑export game data
Exports Mindustry’s official game‑data ZIP at a configurable interval (default 5 minutes) into any folder you choose (Downloads, Documents, etc.).
Exports use Mindustry’s built‑in exportData() function from the settings menu, ensuring full compatibility with campaign progress, maps, schematics, etc. [github.com]
## ✔ Auto‑import newest save on launch
When Mindustry reaches the main menu, the mod checks the chosen ZIP file.
If it is newer than the last import, the mod imports it using the same routine as Settings → Game Data → Import Data. [github.com]
## ✔ "Newest Wins" protection
The mod never overwrites newer data:

- Export is skipped if the mirror ZIP is newer
- Import happens only when the ZIP timestamp is newer than the last import
- Prevents sync loops and data loss

## ✔ User‑choosable sync location
Mindustry’s native file chooser (the same used by the export/import UI) allows picking any writeable public folder.
Path is stored in Core.settings using Mindustry's settings system. [mindustryg....github.io]
## ✔ Safe import (menu‑only)
Imports only in the main menu, avoiding autosave overwrites during gameplay.
This is consistent with the Mindustry developer note that importing during play will be overwritten by the active autosave. [reddit.com]

## 🧩 How it works

# Export flow

1. Every N minutes (default: 5)
2. Check if the mirror ZIP exists
3. If the mirror ZIP is newer, skip export (“newest wins”)
4. Else export using Mindustry’s built‑in exporter
5. Update internal timestamp

# Import flow

On main menu
1. Check if mirror ZIP exists
2. If mirror ZIP timestamp > last imported timestamp → import
3. Import using Mindustry’s official importData()
4. Mindustry resets game state and restarts (same as manual import)


# 📦 Installation

## From Mod Browser (recommended)
Search for:
Mindustry Auto Export
Then press Install.
## Manual install

1. Download the .jar from the GitHub Releases page
2. Put it in:

- Windows: %AppData%/Mindustry/mods/
- Linux: ~/.local/share/Mindustry/mods/
- Android: /storage/emulated/0/Android/data/io.anuke.mindustry/files/mods/


Restart Mindustry


## ⚙️ First‑time setup
After installing the mod:

1. Open Settings → Mirror Sync
2. Tap Choose mirror zip…
3. Pick a location you want your ZIP saved to (e.g. Documents/MindustrySync/mindustry-data.zip)
4. Enable:

	- Auto Import
	- Auto Export
	- Set interval (default 5 min)

Now you can sync the chosen folder using:

- FolderSync
- Syncthing
- OneDrive
- Dropbox
- Google Drive (via FolderSync)
- PC cloud clients

📁 Platform support

✔ Desktop (Windows / Linux / Mac)
✔ Android (11+ supported via system file picker)
✔ Cross‑device sync ready


📝 Changelog
v1.0.0

Initial release
Auto export / auto import
Newest‑wins sync logic
SAF file chooser
Menu‑safe importing
Customizable export interval


❤️ Credits
Created by MrGabrielko
Built using the official Mindustry Java Mod Template.

🐞 Issues & Suggestions
Submit bug reports or feature requests at:
https://github.com/MrGabrielko/MindustryAutoExport/issues