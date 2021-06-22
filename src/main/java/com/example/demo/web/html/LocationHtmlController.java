package com.example.demo.web.html;

import com.example.demo.dto.LocationDto;
import com.example.demo.entity.Location;
import com.example.demo.repository.LocationRepository;
import com.example.demo.service.FileSystemStorageService;
import java.io.IOException;
import java.nio.file.FileStore;
import java.util.NoSuchElementException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpClientErrorException.BadRequest;

@Controller
@RequestMapping("/locations")
@RequiredArgsConstructor
public class LocationHtmlController {

  private final LocationRepository locationRepository;
  private final FileSystemStorageService fileSystemStorageService;

  @GetMapping
  public String getIndexPage(Model model) {
    model.addAttribute("locations", locationRepository.findAll());

    return "location/index";
  }

  @GetMapping("/{id}")
  public String getOne(@PathVariable Integer id, Model model) {
    model.addAttribute("location",
        locationRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Record not found")));

    return "location/view";
  }

  @GetMapping("/create")
  public String getCreatePage() {
    return "location/create";
  }

  @PostMapping("/create")
  @Transactional
  public String create(@ModelAttribute LocationDto dto) {
    Location entity = new Location();

    if (Objects.isNull(dto.getImage()) || dto.getImage().isEmpty()) {
      throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Image file not provided");
    }

    final String filename = fileSystemStorageService
        .generateFilename(fileSystemStorageService.getSupportedImageExtension(dto.getImage()));

    entity.setTitle(dto.getTitle());
    entity.setBrief(dto.getBrief());
    entity.setContent(dto.getContent());
    entity.setImage(filename);

    entity = locationRepository.save(entity);

    fileSystemStorageService.store(dto.getImage(), filename);

    return "redirect:/locations/" + entity.getId();
  }

  @GetMapping("/update/{id}")
  public String getUpdatePage(@PathVariable Integer id, Model model) {
    Location entity = locationRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Record not found"));

    model.addAttribute("location", entity);

    return "location/update";
  }

  @PostMapping("/update/{id}")
  public String update(@PathVariable Integer id, @ModelAttribute LocationDto dto) {
    Location entity = locationRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Record not found"));

    String filename = entity.getImage();
    String oldFilename = entity.getImage();
    if (Objects.nonNull(dto.getImage()) && !dto.getImage().isEmpty()) {
      filename = fileSystemStorageService
          .generateFilename(fileSystemStorageService.getSupportedImageExtension(dto.getImage()));
      entity.setImage(filename);
    }

    entity.setTitle(dto.getTitle());
    entity.setBrief(dto.getBrief());
    entity.setContent(dto.getContent());

    entity = locationRepository.save(entity);

    if (Objects.nonNull(dto.getImage()) && !dto.getImage().isEmpty()) {
      fileSystemStorageService.store(dto.getImage(), filename);
      try {
        fileSystemStorageService.delete(oldFilename);
      } catch (IOException exception) {
      }
    }

    return "redirect:/locations/" + entity.getId();
  }

  @GetMapping("/delete/{id}")
  public String delete(@PathVariable Integer id) {
    locationRepository.deleteById(id);
    return "redirect:/locations";
  }
}
