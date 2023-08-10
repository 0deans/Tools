import codechicken.diffpatch.cli.PatchOperation;
import codechicken.diffpatch.util.PatchMode;
import codechicken.diffpatch.util.archiver.ArchiveFormat;
import utils.TimeStamp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class ApplyPatches {
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: ApplyPatches <base.jar> <output_folder> <patches\\joined>");
            return;
        }

        long startTime = System.currentTimeMillis();

        Path basePath = new File(args[0]).toPath();
        Path outputPath = new File(args[1]).toPath();
        Path patchesPath = new File(args[2]).toPath();
        Path rejectsPath = new File("reject").toPath();

        PatchOperation.Builder builder = PatchOperation.builder()
                .basePath(basePath)
                .patchesPath(patchesPath)
                .outputPath(outputPath, ArchiveFormat.findFormat("jar"))
                .rejectsPath(rejectsPath, ArchiveFormat.findFormat("zip"))
                .verbose(false)
                .summary(true)
                .mode(PatchMode.EXACT)
                .aPrefix("a/")
                .bPrefix("b/")
                .patchesPrefix("");

        var result = builder.build().operate();

        int exit = result.exit;
        if (exit != 0 && exit != 1) {
            throw new RuntimeException("DiffPatch failed with exit code: " + exit);
        }

        TimeStamp time = TimeStamp.fromNow(startTime);
        System.out.printf("Finished in: %s", time);
    }
}
