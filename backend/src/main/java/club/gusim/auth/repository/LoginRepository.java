package club.gusim.auth.repository;

import club.gusim.dao.CommunityDao;
import club.gusim.domain.Member;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Authentication lookups share the same user storage as member management. */
@Repository
public class LoginRepository {
  private final CommunityDao dao;

  public LoginRepository(CommunityDao dao) {
    this.dao = dao;
  }

  public Optional<Member> findByStudentNumber(String number) {
    return dao.byStudentNumber(number);
  }
}
