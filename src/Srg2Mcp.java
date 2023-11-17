import com.google.common.io.Files;
import de.siegmar.fastcsv.reader.NamedCsvReader;
import utils.TimeStamp;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Srg2Mcp {
    private static final Pattern SRG_FINDER = Pattern.compile("(f|m|p)_\\d+?_");

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: Srg2Mcp <mappings.zip> <src_folder>");
            return;
        }

        long startTime = System.currentTimeMillis();

        File mappings = new File(args[0]);
        File srcFolder = new File(args[1]);

        if (!mappings.exists() || !mappings.isFile()) {
            System.out.println("Error: Mappings file not found.");
            return;
        }

        if (!srcFolder.exists() || !srcFolder.isDirectory()) {
            System.out.println("Error: SRC folder not found.");
            return;
        }

        Map<String, String> names = loadMappings(mappings);
        processFolder(srcFolder, names);

        TimeStamp time = TimeStamp.fromNow(startTime);
        System.out.printf("Finished in: %s", time);
    }

    private static Map<String, String> loadMappings(File mappings) throws IOException {
        Map<String, String> names = new HashMap<>();
        try (ZipFile zip = new ZipFile(mappings)) {
            zip.stream().filter(entry -> entry.getName().endsWith(".csv")).forEach(entry -> {
                try {
                    var inputStreamReader = new InputStreamReader(zip.getInputStream(entry));
                    var reader = NamedCsvReader.builder().build(inputStreamReader);
                    reader.forEach(row -> names.put(row.getField("searge"), row.getField("name")));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        return names;
    }

    private static String rename(String line, Map<String, String> names) {
        Matcher matcher = SRG_FINDER.matcher(line);

        while (matcher.find()) {
            String searge = matcher.group();
            if (names.containsKey(searge)) {
                String name = names.get(searge);
                line = line.replace(searge, name);
            }
        }

        return line;
    }

    private static void processFolder(File folder, Map<String, String> names) throws IOException {
        if (!folder.isDirectory()) return;

        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                processFolder(file, names);
            } else if (file.getName().endsWith(".java")) {
                processFile(file, names);
            }
        }
    }

    private static void processFile(File file, Map<String, String> names) throws IOException {
        List<String> lines = Files.readLines(file, StandardCharsets.UTF_8);

        try (FileWriter writer = new FileWriter(file)) {
            for (String line : lines) {
                String mod = rename(line, names);
                writer.write(mod + System.lineSeparator());
            }
        }
    }
}