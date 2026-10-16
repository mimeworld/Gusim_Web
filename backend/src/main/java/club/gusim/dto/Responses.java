package club.gusim.dto;

import club.gusim.domain.*;
import java.time.Instant;
import java.util.List;

public final class Responses {
  private Responses() {}

  public record MemberView(
      Long id, String name, String studentNumber, String role, boolean active, List<String> tags) {
    public static MemberView from(Member m, List<String> tags) {
      return new MemberView(m.id, m.name, m.studentNumber, m.role, m.active, tags);
    }
  }

  public record EquipmentView(
      Long id, String type, String maker, String model, String description) {
    public static EquipmentView from(Equipment e) {
      return new EquipmentView(e.id, e.type, e.maker, e.model, e.description);
    }
  }

  public record Profile(
      Long id,
      String name,
      String studentNumber,
      String role,
      String bio,
      String avatar,
      List<EquipmentView> equipment,
      List<String> tags) {
    public static Profile from(Member m, List<Equipment> gear, List<String> tags) {
      return new Profile(
          m.id,
          m.name,
          m.studentNumber,
          m.role,
          m.bio,
          m.avatar,
          gear.stream().map(EquipmentView::from).toList(),
          tags);
    }
  }

  public record PostView(
      Long id,
      Long authorId,
      String author,
      String title,
      String content,
      String category,
      String image,
      Instant date,
      List<String> authorTags,
      List<String> images,
      String boardCategory,
      List<Long> imageSizes,
      String authorAvatar) {
    public static PostView from(Post p, List<String> authorTags) {
      return from(p, authorTags, List.of());
    }

    public static PostView from(Post p, List<String> authorTags, List<Long> imageSizes) {
      return from(p, authorTags, imageSizes, p.author, "");
    }

    public static PostView from(
        Post p,
        List<String> authorTags,
        List<Long> imageSizes,
        String authorName,
        String authorAvatar) {
      return new PostView(
          p.id,
          p.authorId,
          authorName,
          p.title,
          p.content,
          p.category,
          p.image,
          p.date,
          authorTags,
          p.images == null || p.images.isEmpty()
              ? (p.image == null || p.image.isBlank() ? List.of() : List.of(p.image))
              : List.copyOf(p.images),
          p.category.equals("BOARD") ? (p.boardCategory == null ? "CHAT" : p.boardCategory) : null,
          imageSizes,
          authorAvatar);
    }
  }

  public record DirectoryEntry(Long id, String name, String avatar, String bio, String position) {}

  public record MemberDirectory(List<DirectoryEntry> officers, List<DirectoryEntry> members) {}

  public record MemberProfile(
      Long id,
      String name,
      String avatar,
      String bio,
      List<String> tags,
      List<EquipmentView> equipment,
      List<PostView> photos) {}

  public record OfficerView(
      Long id,
      Long memberId,
      String name,
      String position,
      String bio,
      String photo,
      int sortOrder) {
    public static OfficerView from(Officer o) {
      return new OfficerView(o.id, o.memberId, o.name, o.position, o.bio, o.photo, o.sortOrder);
    }
  }

  public record PageView(String slug, String title, String content) {
    public static PageView from(SitePage p) {
      return new PageView(p.slug, p.title, p.content);
    }
  }

  public record Error(String code, String message) {}

  public record Upload(String url) {}

  public record UploadBatch(List<String> urls) {}

  public record Csrf(String token, String headerName) {}
}
