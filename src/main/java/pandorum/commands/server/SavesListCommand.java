package pandorum.commands.server;

import arc.files.Fi;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.io.SaveIO;

import static mindustry.Vars.saveDirectory;

public class SavesListCommand {
    public static void run(final String[] args) {
        Seq<Fi> savesList = Seq.with(saveDirectory.list()).filter(file -> SaveIO.isSaveValid(file));
        if (savesList.isEmpty()) {
            Log.info("На сервере нет ни одного сохранения.");
        } else {
            Log.info("Сохранения сервера: (@)", savesList.size);
            savesList.each(save -> Log.info("  '@'", save.nameWithoutExtension()));
        }
        Log.info("Все сохранения находятся здесь: &fi@", saveDirectory.absolutePath());
    }
}
