package club.gusim.repository;

import club.gusim.dao.CommunityDao;
import club.gusim.domain.*;
import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class ContentRepository {
  private final CommunityDao dao;

  public ContentRepository(CommunityDao dao) {
    this.dao = dao;
  }

  public List<Officer> officers() {
    return dao.officers();
  }

  public Optional<Officer> officer(Long id) {
    return dao.officer(id);
  }

  public Optional<Officer> officerForMember(Long memberId) {
    return dao.officerForMember(memberId);
  }

  public Officer save(Officer officer) {
    return dao.saveOfficer(officer);
  }

  public void deleteOfficer(Long id) {
    dao.deleteOfficer(id);
  }

  public Optional<SitePage> page(String slug) {
    return dao.page(slug);
  }

  public SitePage save(SitePage page) {
    return dao.savePage(page);
  }
}
