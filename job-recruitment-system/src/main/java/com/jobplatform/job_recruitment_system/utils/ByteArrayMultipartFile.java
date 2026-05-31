package com.jobplatform.job_recruitment_system.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ByteArrayMultipartFile implements MultipartFile {
    private final byte[] fileContent;
    private final String name;
    private final String originalFileName;
    private final String contentType;

    public ByteArrayMultipartFile(byte[] fileContent, String name, String originalFileName, String contentType) {
        this.fileContent = fileContent;
        this.name = name;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
    }

    @Override
    public String getName() { return name; }

    @Override
    public String getOriginalFilename() { return originalFileName; }

    @Override
    public String getContentType() { return contentType; }

    @Override
    public boolean isEmpty() { return fileContent == null || fileContent.length == 0; }

    @Override
    public long getSize() { return fileContent.length; }

    @Override
    public byte[] getBytes() throws IOException { return fileContent; }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(fileContent);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(fileContent);
        }
    }
}
