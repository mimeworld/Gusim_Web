package club.gusim.service;

import club.gusim.repository.ContentRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MemberTagsService {
  private final ContentRepository content;

  public MemberTagsService(ContentRepository content) {
    this.content = content;
  }

  public List<String> tags(Long memberId) {
    return content
        .officerForMember(memberId)
        .map(o -> List.of(o.position))
        .orElse(List.of("일반 회원"));
  }
}
