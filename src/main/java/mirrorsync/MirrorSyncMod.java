
package mirrorsync;

import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.util.Log;
import arc.util.Timer;

import mindustry.mod.Mod;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.ui.dialogs.SettingsMenuDialog;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustry.core.GameState;

import static mindustry.Vars.*;

public class MirrorSyncMod extends Mod {

    private static final String K_PATH = "mirrorsync.mirrorZipPath";
    private static final String K_AUTO_EXPORT = "mirrorsync.autoExport";
    private static final String K_AUTO_IMPORT = "mirrorsync.autoImport";
    private static final String K_INTERVAL_MIN = "mirrorsync.intervalMin";
    private static final String K_LAST_SEEN_MTIME = "mirrorsync.lastSeenRemoteMtime";

    private Timer.Task exportTask;
    private Timer.Task importPollTask;

    public MirrorSyncMod() {
        Events.on(ClientLoadEvent.class, e -> {
            addSettingsCategory();
            reschedule();
        });
    }

    private void addSettingsCategory() {
        ui.settings.addCategory("Mirror Sync", (SettingsMenuDialog.SettingsTable t) -> {
            t.checkPref(K_AUTO_IMPORT, true);
            t.checkPref(K_AUTO_EXPORT, true);
            t.sliderPref(K_INTERVAL_MIN, 5, 1, 60, i -> i + " min");

            t.row();
            t.button("Choose mirror zip…", Icon.folder, Styles.flatt, this::chooseMirrorZip).size(300f, 54f).row();
            t.button("Import now (menu only)", Icon.download, Styles.flatt, () -> importIfNewer(true)).size(300f, 54f).row();
            t.button("Export now", Icon.upload, Styles.flatt, () -> exportIfSafe(true)).size(300f, 54f).row();
            t.button("Reschedule", Styles.flatt, this::reschedule).size(300f, 54f).row();

            t.row();
            t.label(() -> "Target: " + Core.settings.getString(K_PATH, "<not set>"))
             .wrap().width(520f).left().padTop(8f);
        });
    }

    private void chooseMirrorZip() {
        platform.showFileChooser(false, "zip", file -> {
            if (file == null) return;
            Core.settings.put(K_PATH, file.absolutePath());
            Core.settings.manualSave();
            ui.announce("Mirror Sync: target set");
        });
    }

    private Fi mirrorZip() {
        String p = Core.settings.getString(K_PATH, "");
        if (p == null || p.trim().isEmpty()) return null;
        return new Fi(p);
    }

    private long lastSeenMtime() {
        return Core.settings.getLong(K_LAST_SEEN_MTIME, 0L);
    }

    private void setLastSeenMtime(long value) {
        Core.settings.put(K_LAST_SEEN_MTIME, value);
        Core.settings.manualSave();
    }

    private void reschedule() {
        if (exportTask != null) exportTask.cancel();
        if (importPollTask != null) importPollTask.cancel();

        int mins = Core.settings.getInt(K_INTERVAL_MIN, 5);

        if (Core.settings.getBool(K_AUTO_EXPORT, true)) {
            exportTask = Timer.schedule(() -> exportIfSafe(false), 30f, mins * 60f);
        }
        if (Core.settings.getBool(K_AUTO_IMPORT, true)) {
            importPollTask = Timer.schedule(() -> importIfNewer(false), 5f, 10f);
        }
        ui.announce("Mirror Sync: tasks scheduled");
    }

    private void exportIfSafe(boolean manual) {
        Fi zip = mirrorZip();
        if (zip == null) {
            if (manual) ui.announce("Mirror Sync: choose a target zip first");
            return;
        }

        zip.parent().mkdirs();

        // NEWEST-WINS: don't overwrite a newer remote file.
        if (zip.exists() && zip.lastModified() > lastSeenMtime()) {
            if (manual) ui.announce("Mirror Sync: newer mirror zip detected — import first");
            return;
        }

        try {
            // Use Mindustry's official exporter (same as Settings → Game Data → Export). [2](https://mindustrygame.github.io/docs/mindustry/core/GameState.html)
            ui.settings.exportData(zip);

            long mtime = zip.lastModified();
            setLastSeenMtime(mtime);

            if (manual) ui.announce("Mirror Sync: exported");
        } catch (Throwable t) {
            Log.err("Mirror Sync export failed", t);
            if (manual) ui.announce("Mirror Sync: export failed (see logs)");
        }
    }

    private void importIfNewer(boolean manual) {
        if (!state.isMenu()) {
            if (manual) ui.announce("Mirror Sync: go to main menu before importing");
            return;
        }

        Fi zip = mirrorZip();
        if (zip == null || !zip.exists()) {
            if (manual) ui.announce("Mirror Sync: no mirror zip found");
            return;
        }

        long remoteMtime = zip.lastModified();
        if (!manual && remoteMtime <= lastSeenMtime()) return;

        try {
            ui.settings.importData(zip);

            control.saves.resetSave();
            state = new GameState();
            setLastSeenMtime(remoteMtime);

            Core.app.exit();
        } catch (IllegalArgumentException bad) {
            if (manual) ui.announce("Mirror Sync: invalid zip");
        } catch (Throwable t) {
            Log.err("Mirror Sync import failed", t);
            if (manual) ui.announce("Mirror Sync: import failed (see logs)");
        }
    }
}
