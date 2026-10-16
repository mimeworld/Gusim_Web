package club.gusim.service;

import club.gusim.domain.Member;
import club.gusim.dto.*;
import club.gusim.repository.*;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
  private final MemberRepository members;
  private final CommunityRepository community;
  private final UploadService uploads;
  private final MemberTagsService tags;

  public MemberService(
      MemberRepository members,
      CommunityRepository community,
      UploadService uploads,
      MemberTagsService tags) {
    this.members = members;
    this.community = community;
    this.uploads = uploads;
    this.tags = tags;
  }

  public Responses.Profile profile(Member member) {
    return Responses.Profile.from(member, community.equipment(member.id), tags.tags(member.id));
  }

  public Responses.Profile update(Member member, Requests.Profile input) {
    uploads.validateUrl(input.avatar());
    // Update only profile columns: an in-flight request must not restore old permissions/passwords.
    return profile(
        members.updateProfile(
            member.id,
            input.name(),
            input.bio() == null ? "" : input.bio(),
            input.avatar() == null ? "" : input.avatar()));
  }
}
