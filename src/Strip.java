import utils.TimeStamp;

import java.io.*;
import java.util.zip.*;

public class Strip {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: Strip <input.jar> <output.jar>");
            return;
        }

        String inputFilePath = args[0];
        String outputFilePath = args[1];

        TimeStamp time = TimeStamp.fromNow(System.currentTimeMillis());

        try (ZipFile inputZip = new ZipFile(inputFilePath);
             ZipOutputStream outputZip = new ZipOutputStream(new FileOutputStream(outputFilePath))) {

            inputZip.stream()
                    .filter(entry -> entry.getName().endsWith(".class"))
                    .forEach(entry -> {
                        try (InputStream inputStream = inputZip.getInputStream(entry)) {
                            outputZip.putNextEntry(entry);
                            inputStream.transferTo(outputZip);
                            outputZip.closeEntry();
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                    });

            System.out.println("Extraction complete.");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        System.out.printf("Finished in: %s", time);
    }
}
