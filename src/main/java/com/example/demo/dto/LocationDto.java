package com.example.demo.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class LocationDto {

  private String title;
  private String brief;
  private String content;
  private MultipartFile image;
}
