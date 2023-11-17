import codechicken.diffpatch.cli.DiffOperation;
import codechicken.diffpatch.util.archiver.ArchiveFormat;
import utils.TimeStamp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class GeneratePatches {
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: GeneratePatches <base.jar> <output.zip> <patched.jar>");
            return;
        }

        long startTime = System.currentTimeMillis();

        Path basePath = new File(args[0]).toPath();
        Path outputPath = new File(args[1]).toPath();
        Path patchedPath = new File(args[2]).toPath();

        DiffOperation.Builder builder = DiffOperation.builder()
                .aPrefix("a/")
                .bPrefix("b/")
                .summary(true)
                .outputPath(outputPath, ArchiveFormat.ZIP)
                .aPath(basePath)
                .bPath(patchedPath);

        var result = builder.build().operate();
        result.summary.print(System.out, true);

        int exit = result.exit;
        if (exit != 0 && exit != 1) {
            throw new RuntimeException("DiffPatch failed with exit code: " + exit);
        }

        TimeStamp time = TimeStamp.fromNow(startTime);
        System.out.printf("Finished in: %s", time);
    }
}
