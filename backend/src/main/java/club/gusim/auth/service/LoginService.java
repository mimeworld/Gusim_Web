package club.gusim.auth.service;

import static org.springframework.http.HttpStatus.*;

import club.gusim.auth.repository.LoginRepository;
import club.gusim.domain.Member;
import club.gusim.dto.Requests;
import club.gusim.repository.MemberRepository;
import jakarta.servlet.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LoginService {
  private final LoginRepository loginRepository;
  private final MemberRepository members;
  private final BCryptPasswordEncoder encoder;
  private final String dummyHash;

  public LoginService(
      LoginRepository loginRepository, MemberRepository members, BCryptPasswordEncoder encoder) {
    this.loginRepository = loginRepository;
    this.members = members;
    this.encoder = encoder;
    this.dummyHash = encoder.encode("invalid-account-placeholder");
  }

  public Member login(Requests.Login input, HttpServletRequest request) {
    Member member = loginRepository.findByStudentNumber(input.studentNumber()).orElse(null);
    boolean matches =
        encoder.matches(input.password(), member == null ? dummyHash : member.passwordHash);
    if (member == null || !matches || !member.active)
      throw new ResponseStatusException(UNAUTHORIZED, "학번 또는 비밀번호를 확인해주세요.");
    request.getSession();
    request.changeSessionId();
    request.getSession().setAttribute("memberId", member.id);
    request.getSession().setAttribute("sessionVersion", member.sessionVersion);
    return member;
  }

  public Member current(HttpSession session) {
    if (session == null || !(session.getAttribute("memberId") instanceof Long id))
      throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
    Member member =
        members
            .find(id)
            .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다."));
    if (!member.active
        || !Integer.valueOf(member.sessionVersion).equals(session.getAttribute("sessionVersion"))) {
      session.invalidate();
      throw new ResponseStatusException(UNAUTHORIZED, "다시 로그인해주세요.");
    }
    return member;
  }

  public Member requireAdmin(HttpSession session) {
    Member member = current(session);
    if (!member.role.equals("ADMIN"))
      throw new ResponseStatusException(FORBIDDEN, "관리자 권한이 필요합니다.");
    return member;
  }
}
