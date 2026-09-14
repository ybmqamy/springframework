package com.example.test;

import com.example.Annotation.GetMapping;
  import com.example.Annotation.ResponseBody;
  import com.example.Annotation.RestController;

  @RestController
  public class HelloController {
      @GetMapping("/api/hello")
      @ResponseBody
      public String hello() {
          return "Hello WebMvc";
      }
  }