package uz.hrms;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public final class StorageModule {
    private StorageModule() {
    }
}

record StoredFileDescriptor(
    String storageKey,
    String fileName,
    String contentType,
    long sizeBytes
) {
}

@Service
class LocalFileStorageService {

    private final Path root;

    LocalFileStorageService(Environment environment) {
        String configuredRoot = environment.getProperty("app.storage.root");
        String storageRoot = StringUtils.hasText(configuredRoot) ? configuredRoot : "storage";
        this.root = Path.of(storageRoot).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.root);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to initialize storage root", exception);
        }
    }

    StoredFileDescriptor store(String folder, MultipartFile file) {
        String originalName = StringUtils.hasText(file.getOriginalFilename()) ? Path.of(file.getOriginalFilename()).getFileName().toString() : "file.bin";
        String contentType = StringUtils.hasText(file.getContentType()) ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        String storageKey = folder + "/" + UUID.randomUUID() + "-" + originalName;
        Path target = root.resolve(storageKey).normalize();
        try {
            Files.createDirectories(target.getParent());
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to store file", exception);
        }
        return new StoredFileDescriptor(storageKey, originalName, contentType, file.getSize());
    }

    StoredFileDescriptor storeBytes(String folder, String fileName, byte[] content, String contentType) {
        String safeName = StringUtils.hasText(fileName) ? Path.of(fileName).getFileName().toString() : "generated-" + UUID.randomUUID() + ".bin";
        String storageContentType = StringUtils.hasText(contentType) ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        String storageKey = folder + "/" + UUID.randomUUID() + "-" + safeName;
        Path target = root.resolve(storageKey).normalize();
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to store generated content", exception);
        }
        return new StoredFileDescriptor(storageKey, safeName, storageContentType, content.length);
    }

    StoredFileDescriptor storeText(String folder, String fileName, String content, String contentType) {
        return storeBytes(folder, fileName, content == null ? new byte[0] : content.getBytes(StandardCharsets.UTF_8), contentType);
    }

    Resource load(String storageKey) {
        Path target = root.resolve(storageKey).normalize();
        if (Files.exists(target) == false) {
            throw new IllegalArgumentException("File not found");
        }
        return new FileSystemResource(target);
    }
}
