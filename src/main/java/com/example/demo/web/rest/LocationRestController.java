package com.example.demo.web.rest;

import com.example.demo.entity.Location;
import com.example.demo.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationRestController {

  private final LocationRepository locationRepository;

  @GetMapping
  public ResponseEntity<Page<Location>> read(Pageable pageable) {
    return ResponseEntity.ok(locationRepository.findAll(pageable)
        .map(entity -> {
          entity.setImage("/static/" + entity.getImage());
          entity.setContent("");
          return entity;
        }));
  }

  @GetMapping("/{id}")
  public ResponseEntity<Location> readOne(@PathVariable Integer id) {
    return ResponseEntity.ok(locationRepository.findById(id)
        .map(entity -> {
          entity.setImage("/static/" + entity.getImage());
          return entity;
        })
        .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "Record not found")));
  }
}
