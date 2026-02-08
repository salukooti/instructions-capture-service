package com.example.instructions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class InstructionsCaptureApplication {
  public static void main(String[] args) {
    SpringApplication.run(InstructionsCaptureApplication.class, args);
    log.info("InstructionsCaptureApplication is up and running!!!");
  }
}
