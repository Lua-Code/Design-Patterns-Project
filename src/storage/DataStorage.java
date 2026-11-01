
package storage;

import java.io.*;
import java.nio.file.*;

public class DataStorage {

    private static final Path DATA_FILE =
            Path.of("data", "organization-data.dat");

    // Save application records
    public static void save(AppData data) throws IOException {

        Path directory = DATA_FILE.getParent();

        if (directory != null) {
            Files.createDirectories(directory);
        }

        Path temporaryFile = Files.createTempFile(
                directory,
                "organization-",
                ".tmp"
        );

        try {

            // Write the complete snapshot to a temporary file
            try (ObjectOutputStream output =
                         new ObjectOutputStream(
                                 Files.newOutputStream(temporaryFile))) {

                output.writeObject(data);
            }

            // Replace the save file after writing succeeds
            try {

                Files.move(
                        temporaryFile,
                        DATA_FILE,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );

            } catch (AtomicMoveNotSupportedException ex) {

                Files.move(
                        temporaryFile,
                        DATA_FILE,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

        } finally {

            // Clean up a temporary file if it still exists
            Files.deleteIfExists(temporaryFile);
        }
    }

    // Load previously saved records
    public static AppData load()
            throws IOException, ClassNotFoundException {

        if (!Files.exists(DATA_FILE)) {
            return new AppData();
        }

        try (ObjectInputStream input =
                     new ObjectInputStream(
                             Files.newInputStream(DATA_FILE))) {

            Object object = input.readObject();

            if (!(object instanceof AppData)) {
                throw new IOException(
                        "Invalid application data file."
                );
            }

            return (AppData) object;
        }
    }
}
