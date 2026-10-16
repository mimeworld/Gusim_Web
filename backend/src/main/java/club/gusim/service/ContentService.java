package club.gusim.service;

import static org.springframework.http.HttpStatus.*;

import club.gusim.domain.*;
import club.gusim.dto.*;
import club.gusim.repository.ContentRepository;
import club.gusim.repository.MemberRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ContentService {
  private final ContentRepository repository;
  private final UploadService uploads;
  private final AdminService admin;
  private final MemberRepository members;

  public ContentService(
      ContentRepository repository,
      UploadService uploads,
      AdminService admin,
      MemberRepository members) {
    this.repository = repository;
    this.uploads = uploads;
    this.admin = admin;
    this.members = members;
  }

  public List<Responses.OfficerView> officers() {
    return repository.officers().stream().map(this::view).toList();
  }

  public Responses.OfficerView saveOfficer(Long id, Requests.Officer input) {
    uploads.validateUrl(input.photo());
    Officer o =
        id == null
            ? new Officer()
            : repository
                .officer(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "임원진을 찾을 수 없습니다."));
    o.memberId = input.memberId();
    o.position = input.position();
    o.bio = input.bio();
    o.photo = input.photo();
    o.sortOrder = input.sortOrder();
    return view(admin.assignOfficer(o));
  }

  private Responses.OfficerView view(Officer officer) {
    String name =
        officer.memberId == null
            ? officer.name
            : members.find(officer.memberId).map(m -> m.name).orElse(officer.name);
    return new Responses.OfficerView(
        officer.id,
        officer.memberId,
        name,
        officer.position,
        officer.bio,
        officer.photo,
        officer.sortOrder);
  }

  public void deleteOfficer(Long id) {
    admin.removeOfficer(id);
  }

  public Responses.PageView ot() {
    return Responses.PageView.from(
        repository.page("ot").orElse(new SitePage("ot", "신입 부원 OT", "아직 등록된 OT 안내가 없습니다.")));
  }

  public Responses.PageView updateOt(Requests.Page input) {
    return Responses.PageView.from(
        repository.save(new SitePage("ot", input.title(), input.content())));
  }
}
