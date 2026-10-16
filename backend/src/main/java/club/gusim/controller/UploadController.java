package club.gusim.controller;

import club.gusim.auth.service.LoginService;
import club.gusim.dto.Responses;
import club.gusim.service.UploadService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {
  private final LoginService login;
  private final UploadService uploads;

  public UploadController(LoginService login, UploadService uploads) {
    this.login = login;
    this.uploads = uploads;
  }

  @PostMapping("/batch")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Responses.UploadBatch batch(
      @RequestParam String category, @RequestParam List<MultipartFile> files, HttpSession session) {
    login.current(session);
    return new Responses.UploadBatch(uploads.storeBatch(category, files));
  }

  @PostMapping
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Responses.Upload upload(@RequestParam MultipartFile file, HttpSession session) {
    login.current(session);
    return new Responses.Upload(uploads.store(file));
  }
}
