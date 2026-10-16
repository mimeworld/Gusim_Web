package club.gusim.service;

import static org.springframework.http.HttpStatus.*;

import club.gusim.domain.*;
import club.gusim.dto.Responses;
import club.gusim.repository.*;
import java.text.Collator;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MemberDirectoryService {
  private final MemberRepository members;
  private final ContentRepository content;
  private final CommunityRepository community;
  private final CommunityService posts;
  private final MemberTagsService tags;

  public MemberDirectoryService(
      MemberRepository members,
      ContentRepository content,
      CommunityRepository community,
      CommunityService posts,
      MemberTagsService tags) {
    this.members = members;
    this.content = content;
    this.community = community;
    this.posts = posts;
    this.tags = tags;
  }

  public Responses.MemberDirectory directory() {
    Map<Long, String> positions = new HashMap<>();
    for (Officer officer : content.officers())
      if (officer.memberId != null) positions.put(officer.memberId, officer.position);
    Collator korean = Collator.getInstance(Locale.KOREAN);
    Comparator<Member> order =
        Comparator.comparing((Member member) -> member.name, korean)
            .thenComparing(member -> member.id);
    var sorted = members.findAll().stream().filter(member -> member.active).sorted(order).toList();
    List<Responses.DirectoryEntry> officers = new ArrayList<>(), regular = new ArrayList<>();
    for (Member member : sorted) {
      String position = positions.get(member.id);
      var entry =
          new Responses.DirectoryEntry(member.id, member.name, member.avatar, member.bio, position);
      (position == null ? regular : officers).add(entry);
    }
    return new Responses.MemberDirectory(officers, regular);
  }

  public Responses.MemberProfile profile(Long id) {
    Member member =
        members
            .find(id)
            .filter(m -> m.active)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "회원을 찾을 수 없습니다."));
    return new Responses.MemberProfile(
        member.id,
        member.name,
        member.avatar,
        member.bio,
        tags.tags(member.id),
        community.equipment(id).stream().map(Responses.EquipmentView::from).toList(),
        posts.photosByAuthor(id));
  }
}
