package club.gusim.repository;

import club.gusim.dao.CommunityDao;
import club.gusim.domain.Member;
import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class MemberRepository {
  private final CommunityDao dao;

  public MemberRepository(CommunityDao dao) {
    this.dao = dao;
  }

  public Optional<Member> find(Long id) {
    return dao.member(id);
  }

  public List<Member> findAll() {
    return dao.members();
  }

  public Member save(Member member) {
    return dao.saveMember(member);
  }

  public Member updateProfile(Long id, String name, String bio, String avatar) {
    return dao.updateProfile(id, name, bio, avatar);
  }
}
