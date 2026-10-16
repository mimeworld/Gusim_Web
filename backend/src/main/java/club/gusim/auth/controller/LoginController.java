package club.gusim.auth.controller;

import club.gusim.auth.service.LoginService;
import club.gusim.dto.*;
import club.gusim.service.MemberService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class LoginController {
  private final LoginService login;
  private final MemberService members;

  public LoginController(LoginService login, MemberService members) {
    this.login = login;
    this.members = members;
  }

  @PostMapping("/login")
  public Responses.Profile login(
      @Valid @RequestBody Requests.Login input, HttpServletRequest request) {
    return members.profile(login.login(input, request));
  }

  @PostMapping("/logout")
  @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  public void logout(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) session.invalidate();
  }
}
