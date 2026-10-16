package club.gusim.config;

import club.gusim.repository.MemberRepository;
import club.gusim.service.AdminService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {
  private final MemberRepository members;
  private final AdminService admin;
  private final String name, number, password;

  public AdminBootstrap(
      MemberRepository members,
      AdminService admin,
      @Value("${app.bootstrap.name:동아리 관리자}") String name,
      @Value("${app.bootstrap.student-number:}") String number,
      @Value("${app.bootstrap.password:}") String password) {
    this.members = members;
    this.admin = admin;
    this.name = name;
    this.number = number;
    this.password = password;
  }

  public void run(ApplicationArguments args) {
    if (members.findAll().stream().anyMatch(m -> m.active && m.role.equals("ADMIN"))) return;
    if (!number.matches("[0-9]{4,20}")
        || password.length() < 8
        || password.length() > 60
        || name.isBlank()
        || name.length() > 50)
      throw new IllegalStateException(
          "최초 관리자 생성을 위해 BOOTSTRAP_ADMIN_STUDENT_NUMBER, BOOTSTRAP_ADMIN_PASSWORD(8~60자)를 설정하세요.");
    admin.bootstrap(name, number, password);
  }
}
