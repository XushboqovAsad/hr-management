package uz.hrms.other.dto;

public record StoredFileDescriptor(
        String storageKey,
        String fileName,
        String contentType,
        long sizeBytes
) {
}
