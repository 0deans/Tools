import com.google.common.io.Files;
import de.siegmar.fastcsv.reader.NamedCsvReader;
import utils.TimeStamp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

public class Srg2Mcp {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: Srg2Mcp <mappings.zip> <src_folder>");
            return;
        }

        String mappingsFilePath = args[0];
        String srcFolderPath = args[1];

        long startTime = System.currentTimeMillis();

        File mappings = new File(mappingsFilePath);
        File srcFolder = new File(srcFolderPath);

        if (!mappings.exists() || !mappings.isFile()) {
            System.out.println("Error: Mappings file not found.");
            return;
        }

        if (!srcFolder.exists() || !srcFolder.isDirectory()) {
            System.out.println("Error: SRC folder not found.");
            return;
        }

        Map<String, String> names = loadMappings(mappings);
        processFilesInFolder(srcFolder, names);

        TimeStamp time = TimeStamp.fromNow(startTime);
        System.out.printf("Finished in: %s", time);
    }

    private static Map<String, String> loadMappings(File mappings) throws IOException {
        Map<String, String> names = new HashMap<>();
        try (ZipFile zip = new ZipFile(mappings)) {
            zip.stream().filter(e -> e.getName().equals("fields.csv") || e.getName().equals("methods.csv") ||
                    e.getName().equals("params.csv")).forEach(e -> {
                try (NamedCsvReader reader = NamedCsvReader.builder().build(new InputStreamReader(zip.getInputStream(e)))) {
                    reader.forEach(row -> names.put(row.getField("searge"), row.getField("name")));
                } catch (IOException e1) {
                    throw new RuntimeException(e1);
                }
            });
        }
        return names;
    }

    private static String replaceMappings(String line, Map<String, String> names) {
        Matcher matcher = Pattern.compile("(f|m|p)_\\d+?_").matcher(line);

        while (matcher.find()) {
            String searge = matcher.group();
            if (names.containsKey(searge)) {
                String name = names.get(searge);
                line = line.replace(searge, name);
            }
        }

        return line;
    }

    private static void processFilesInFolder(File folder, Map<String, String> names) throws IOException {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        processFilesInFolder(file, names);
                    } else {
                        processFile(file, names);
                    }
                }
            }
        }
    }

    private static void processFile(File file, Map<String, String> names) throws IOException {
        if (file.getName().endsWith(".java")) {
            List<String> lines = Files.readLines(file, StandardCharsets.UTF_8);
            List<String> modifiedLines = new ArrayList<>();

            for (String line : lines) {
                modifiedLines.add(replaceMappings(line, names));
            }

            try (FileWriter writer = new FileWriter(file)) {
                for (String line : modifiedLines) {
                    writer.write(line + System.lineSeparator());
                }
            }
        }
    }
}