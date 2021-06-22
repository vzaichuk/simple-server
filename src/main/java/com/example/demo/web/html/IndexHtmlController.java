package com.example.demo.web.html;

import com.example.demo.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class IndexHtmlController {

  private final LocationRepository locationRepository;

  @GetMapping
  public String getIndexPage() {
    return "index";
  }
}
