package com.example.demo.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService {

  @Value("${file.image-root-location}")
  private String rootLocationValue;
  private Path rootLocation;

  @PostConstruct
  public void postConstruct() {
    if (!StringUtils.hasText(rootLocationValue)) {
      throw new IllegalArgumentException("Image root path not specified");
    }
    rootLocation = Paths.get(rootLocationValue);
  }

  public void store(MultipartFile file, String filename) {
    try {
      if (file.isEmpty()) {
        throw new RuntimeException("Failed to store empty file.");
      }

      Path destinationFile = this.rootLocation.resolve(Paths.get(filename))
          .normalize().toAbsolutePath();

      if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
        // This is a security check
        throw new RuntimeException("Cannot store file outside current directory.");
      }

      try (InputStream inputStream = file.getInputStream()) {
        Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to store file.", e);
    }
  }

  public Stream<Path> loadAll() {
    try {
      return Files.walk(this.rootLocation, 1)
          .filter(path -> !path.equals(this.rootLocation))
          .map(this.rootLocation::relativize);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read stored files", e);
    }

  }

  public Path load(String filename) {
    return rootLocation.resolve(filename);
  }

  public Resource loadAsResource(String filename) {
    try {
      Path file = load(filename);
      Resource resource = new UrlResource(file.toUri());
      if (resource.exists() || resource.isReadable()) {
        return resource;
      } else {
        throw new RuntimeException("Could not read file: " + filename);
      }
    } catch (MalformedURLException e) {
      throw new RuntimeException("Could not read file: " + filename, e);
    }
  }

  public void deleteAll() {
    FileSystemUtils.deleteRecursively(rootLocation.toFile());
  }

  public void delete(String filename) throws IOException {
    Files.deleteIfExists(rootLocation.resolve(filename));
  }

  public void init() {
    try {
      Files.createDirectories(rootLocation);
    } catch (IOException e) {
      throw new RuntimeException("Could not initialize storage", e);
    }
  }

  public String generateFilename(String extension) {
    return UUID.randomUUID().toString() + "." + extension;
  }

  public String getSupportedImageExtension(MultipartFile file) {
    switch (Objects.requireNonNull(file.getContentType())) {
      case "image/jpeg" :
      case "image/jpg" :
        return "jpg";
      case "image/png" :
        return "png";
    }

    throw new IllegalArgumentException("Image type not supported");
  }
}
