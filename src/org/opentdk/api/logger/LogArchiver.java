package org.opentdk.api.logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class to handle log file archiving. This takes the log file size, the log keep age in days
 * and the number of archived log files into account.
 */
public final class LogArchiver {

    public static void archiveIfNecessary(Path logFile, long logSize, int keepAge, int archiveSize) throws IOException {
        if (!Files.exists(logFile)) {
            throw new IOException("No log file present to archive");
        }

        // Get file properties
        BasicFileAttributes attributes = Files.readAttributes(logFile, BasicFileAttributes.class);
        long fileSize = attributes.size();
        Instant lastModified = attributes.lastModifiedTime().toInstant();
        Instant now = Instant.now();

        // Check if archiving is required
        if (fileSize > logSize && lastModified.isBefore(now.minus(keepAge, ChronoUnit.DAYS))) {
            archiveLog(logFile, logFile.getParent().getParent(), archiveSize);
        }
    }

    private static void archiveLog(Path logFile, Path archiveDir, int archiveSize) throws IOException {
        if (!Files.exists(archiveDir)) {
            Files.createDirectories(archiveDir);
        }

        // Generate archive name
        String timestamp = LocalDateTime.now().toString().replace(":", "-");
        Path archiveFile = archiveDir.resolve(logFile.getFileName() + "_" + timestamp);

        // Move log file
        Files.move(logFile, archiveFile, StandardCopyOption.REPLACE_EXISTING);

        // Create new log file
        Files.createFile(logFile);

        // Remove old files when the number exceeds the archive size
        cleanupOldArchives(archiveDir, archiveSize);
    }

    private static void cleanupOldArchives(Path archiveDir, int maxArchives) throws IOException {
        try (Stream<Path> files = Files.list(archiveDir)) {
            List<Path> archiveFiles = files.filter(Files::isRegularFile)
                    .sorted(Comparator.comparingLong(path -> path.toFile().lastModified()))
                    .collect(Collectors.toList());

            while (archiveFiles.size() > maxArchives) {
                Path oldestFile = archiveFiles.remove(0);
                Files.delete(oldestFile);
            }
        }
    }

}