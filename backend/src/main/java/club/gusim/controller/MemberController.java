package club.gusim.controller;

import club.gusim.auth.service.LoginService;
import club.gusim.dto.*;
import club.gusim.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
public class MemberController {
  private final LoginService login;
  private final MemberService members;
  private final CommunityService community;
  private final AdminService admin;

  public MemberController(
      LoginService login, MemberService members, CommunityService community, AdminService admin) {
    this.login = login;
    this.members = members;
    this.community = community;
    this.admin = admin;
  }

  @GetMapping
  public Responses.Profile me(HttpSession session) {
    return members.profile(login.current(session));
  }

  @PutMapping
  public Responses.Profile update(@Valid @RequestBody Requests.Profile input, HttpSession session) {
    return members.update(login.current(session), input);
  }

  @PutMapping("/password")
  @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  public void password(@Valid @RequestBody Requests.ChangePassword input, HttpSession session) {
    admin.changeOwnPassword(login.current(session).id, input);
    session.invalidate();
  }

  @PostMapping("/equipment")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Responses.EquipmentView equipment(
      @Valid @RequestBody Requests.Equipment input, HttpSession session) {
    return community.addEquipment(login.current(session), input);
  }

  @DeleteMapping("/equipment/{id}")
  @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  public void deleteEquipment(@PathVariable Long id, HttpSession session) {
    community.deleteEquipment(login.current(session), id);
  }
}
