package club.gusim.controller;

import club.gusim.auth.service.LoginService;
import club.gusim.dto.Responses;
import club.gusim.service.MemberDirectoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
public class MemberDirectoryController {
  private final LoginService login;
  private final MemberDirectoryService members;

  public MemberDirectoryController(LoginService login, MemberDirectoryService members) {
    this.login = login;
    this.members = members;
  }

  @GetMapping
  public Responses.MemberDirectory directory(HttpSession session) {
    login.current(session);
    return members.directory();
  }

  @GetMapping("/{id}")
  public Responses.MemberProfile profile(@PathVariable Long id, HttpSession session) {
    login.current(session);
    return members.profile(id);
  }
}
