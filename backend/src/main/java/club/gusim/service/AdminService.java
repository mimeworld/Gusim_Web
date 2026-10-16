package club.gusim.service;

import static org.springframework.http.HttpStatus.*;

import club.gusim.auth.repository.LoginRepository;
import club.gusim.domain.Member;
import club.gusim.domain.Officer;
import club.gusim.dto.*;
import club.gusim.repository.ContentRepository;
import club.gusim.repository.MemberRepository;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminService {
  private final MemberRepository members;
  private final LoginRepository login;
  private final BCryptPasswordEncoder encoder;
  private final ContentRepository content;
  private final MemberTagsService tags;

  public AdminService(
      MemberRepository members,
      LoginRepository login,
      BCryptPasswordEncoder encoder,
      ContentRepository content,
      MemberTagsService tags) {
    this.members = members;
    this.login = login;
    this.encoder = encoder;
    this.content = content;
    this.tags = tags;
  }

  public List<Responses.MemberView> list() {
    return members.findAll().stream()
        .map(m -> Responses.MemberView.from(m, tags.tags(m.id)))
        .toList();
  }

  private Member find(Long id) {
    return members
        .find(id)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "회원을 찾을 수 없습니다."));
  }

  private void unique(String number, Long id) {
    if (login.findByStudentNumber(number).filter(m -> !Objects.equals(m.id, id)).isPresent())
      throw new ResponseStatusException(CONFLICT, "이미 등록된 학번입니다.");
  }

  private String hash(String password) {
    if (password.length() < 8 || password.length() > 60)
      throw new ResponseStatusException(BAD_REQUEST, "비밀번호는 8자 이상 60자 이하로 입력해주세요.");
    if (password.getBytes(StandardCharsets.UTF_8).length > 72)
      throw new ResponseStatusException(BAD_REQUEST, "비밀번호는 UTF-8 기준 72바이트 이하로 입력해주세요.");
    return encoder.encode(password);
  }

  public synchronized Responses.MemberView create(Requests.CreateMember input) {
    return createAccount(input.name(), input.studentNumber(), "gusim1234", input.role());
  }

  public synchronized Responses.MemberView update(
      Long actorId, Long id, Requests.UpdateMember input) {
    Member m = find(id);
    unique(input.studentNumber(), id);
    if (content.officerForMember(id).isPresent()
        && (!input.role().equals("ADMIN") || !input.active()))
      throw new ResponseStatusException(CONFLICT, "임원 지정을 먼저 해제한 뒤 권한 또는 활성 상태를 변경해주세요.");
    if (actorId.equals(id) && (!input.active() || !input.role().equals("ADMIN")))
      throw new ResponseStatusException(CONFLICT, "본인 관리자 권한과 활성 상태는 해제할 수 없습니다.");
    if (m.role.equals("ADMIN")
        && m.active
        && (!input.role().equals("ADMIN") || !input.active())
        && members.findAll().stream().filter(u -> u.active && u.role.equals("ADMIN")).count() <= 1)
      throw new ResponseStatusException(CONFLICT, "활성 관리자는 최소 한 명 필요합니다.");
    if (m.active != input.active() || !m.role.equals(input.role())) m.sessionVersion++;
    m.name = input.name();
    m.studentNumber = input.studentNumber();
    m.role = input.role();
    m.active = input.active();
    return Responses.MemberView.from(members.save(m), tags.tags(m.id));
  }

  public synchronized void resetPassword(Long id, Requests.Password input) {
    Member m = find(id);
    m.passwordHash = hash(input.password());
    m.sessionVersion++;
    members.save(m);
  }

  public synchronized Officer assignOfficer(Officer officer) {
    Member member = find(officer.memberId);
    if (!member.active) throw new ResponseStatusException(CONFLICT, "비활성 회원은 임원으로 지정할 수 없습니다.");
    if (content
        .officerForMember(member.id)
        .filter(other -> !Objects.equals(other.id, officer.id))
        .isPresent()) throw new ResponseStatusException(CONFLICT, "이미 임원으로 지정된 회원입니다.");
    officer.name = member.name;
    return content.save(officer);
  }

  public synchronized void removeOfficer(Long id) {
    if (content.officer(id).isEmpty())
      throw new ResponseStatusException(NOT_FOUND, "임원진을 찾을 수 없습니다.");
    content.deleteOfficer(id);
  }

  private Responses.MemberView createAccount(
      String name, String number, String password, String role) {
    unique(number, null);
    Member member = new Member();
    member.name = name;
    member.studentNumber = number;
    member.passwordHash = hash(password);
    member.role = role;
    Member saved = members.save(member);
    return Responses.MemberView.from(saved, tags.tags(saved.id));
  }

  public synchronized void bootstrap(String name, String number, String password) {
    createAccount(name, number, password, "ADMIN");
  }

  public synchronized void changeOwnPassword(Long id, Requests.ChangePassword input) {
    Member member = find(id);
    if (!encoder.matches(input.currentPassword(), member.passwordHash))
      throw new ResponseStatusException(BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다.");
    member.passwordHash = hash(input.newPassword());
    member.sessionVersion++;
    members.save(member);
  }
}
