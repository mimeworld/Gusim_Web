package club.gusim.controller;

import club.gusim.auth.service.LoginService;
import club.gusim.dto.*;
import club.gusim.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
  private final LoginService login;
  private final AdminService admin;
  private final ContentService content;

  public AdminController(LoginService login, AdminService admin, ContentService content) {
    this.login = login;
    this.admin = admin;
    this.content = content;
  }

  @GetMapping("/members")
  public List<Responses.MemberView> members(HttpSession session) {
    login.requireAdmin(session);
    return admin.list();
  }

  @PostMapping("/members")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Responses.MemberView create(
      @Valid @RequestBody Requests.CreateMember input, HttpSession session) {
    login.requireAdmin(session);
    return admin.create(input);
  }

  @PutMapping("/members/{id}")
  public Responses.MemberView update(
      @PathVariable Long id, @Valid @RequestBody Requests.UpdateMember input, HttpSession session) {
    return admin.update(login.requireAdmin(session).id, id, input);
  }

  @PutMapping("/members/{id}/password")
  @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  public void password(
      @PathVariable Long id, @Valid @RequestBody Requests.Password input, HttpSession session) {
    login.requireAdmin(session);
    admin.resetPassword(id, input);
  }

  @PostMapping("/officers")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Responses.OfficerView createOfficer(
      @Valid @RequestBody Requests.Officer input, HttpSession session) {
    login.requireAdmin(session);
    return content.saveOfficer(null, input);
  }

  @PutMapping("/officers/{id}")
  public Responses.OfficerView updateOfficer(
      @PathVariable Long id, @Valid @RequestBody Requests.Officer input, HttpSession session) {
    login.requireAdmin(session);
    return content.saveOfficer(id, input);
  }

  @DeleteMapping("/officers/{id}")
  @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  public void deleteOfficer(@PathVariable Long id, HttpSession session) {
    login.requireAdmin(session);
    content.deleteOfficer(id);
  }

  @PutMapping("/pages/ot")
  public Responses.PageView ot(@Valid @RequestBody Requests.Page input, HttpSession session) {
    login.requireAdmin(session);
    return content.updateOt(input);
  }
}
