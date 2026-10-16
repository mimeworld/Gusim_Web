package club.gusim.dao;

import club.gusim.domain.*;
import java.util.*;

public interface CommunityDao {
  Optional<Member> member(Long id);

  Optional<Member> byStudentNumber(String number);

  List<Member> members();

  Member saveMember(Member member);

  Member updateProfile(Long id, String name, String bio, String avatar);

  List<Post> posts(String category, String boardCategory);

  List<Post> photosByAuthor(Long authorId);

  Post savePost(Post post);

  Optional<Post> updatePost(Post post, Long authorId);

  Optional<Post> post(Long id);

  boolean deletePost(Long id, Long authorId);

  List<Equipment> equipment(Long userId);

  Equipment saveEquipment(Equipment equipment);

  void deleteEquipment(Long id, Long userId);

  List<Officer> officers();

  Optional<Officer> officer(Long id);

  Optional<Officer> officerForMember(Long memberId);

  Officer saveOfficer(Officer officer);

  void deleteOfficer(Long id);

  Optional<SitePage> page(String slug);

  SitePage savePage(SitePage page);
}
