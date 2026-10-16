package club.gusim.dao.memory;

import club.gusim.dao.CommunityDao;
import club.gusim.domain.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

/** Detached copies prevent changes from leaking into storage before an explicit save. */
@Repository
@Profile("memory")
public class MemoryCommunityDao implements CommunityDao {
  private final Map<Long, Member> members = new ConcurrentHashMap<>();
  private final Map<Long, Post> posts = new ConcurrentHashMap<>();
  private final Map<Long, Equipment> gear = new ConcurrentHashMap<>();
  private final Map<Long, Officer> officers = new ConcurrentHashMap<>();
  private final Map<String, SitePage> pages = new ConcurrentHashMap<>();
  private final AtomicLong ids = new AtomicLong();
  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

  private <T> T copy(T value, Class<T> type) {
    return value == null ? null : mapper.convertValue(value, type);
  }

  // Password is ignored by JSON intentionally, so copy members explicitly.
  private Member copyMember(Member m) {
    if (m == null) return null;
    Member n = new Member();
    n.id = m.id;
    n.name = m.name;
    n.studentNumber = m.studentNumber;
    n.passwordHash = m.passwordHash;
    n.role = m.role;
    n.bio = m.bio;
    n.avatar = m.avatar;
    n.position = m.position;
    n.active = m.active;
    n.sessionVersion = m.sessionVersion;
    return n;
  }

  public Optional<Member> member(Long id) {
    return Optional.ofNullable(copyMember(members.get(id)));
  }

  public Optional<Member> byStudentNumber(String number) {
    return members.values().stream()
        .filter(m -> m.studentNumber.equals(number))
        .findFirst()
        .map(this::copyMember);
  }

  public List<Member> members() {
    return members.values().stream()
        .sorted(Comparator.comparing(m -> m.id))
        .map(this::copyMember)
        .toList();
  }

  public synchronized Member saveMember(Member m) {
    if (members.values().stream()
        .anyMatch(
            other ->
                other.studentNumber.equals(m.studentNumber) && !Objects.equals(other.id, m.id)))
      throw new DuplicateKeyException("학번 중복");
    if (m.id == null) m.id = ids.incrementAndGet();
    members.put(m.id, copyMember(m));
    return copyMember(m);
  }

  public synchronized Member updateProfile(Long id, String name, String bio, String avatar) {
    Member latest = copyMember(members.get(id));
    if (latest == null) throw new IllegalStateException("Member no longer exists");
    latest.name = name;
    latest.bio = bio;
    latest.avatar = avatar;
    members.put(id, latest);
    return copyMember(latest);
  }

  public List<Post> posts(String category, String boardCategory) {
    return posts.values().stream()
        .filter(p -> p.category.equals(category))
        .filter(
            p ->
                boardCategory == null
                    || boardCategory.equals(p.boardCategory == null ? "CHAT" : p.boardCategory))
        .sorted(Comparator.comparing((Post p) -> p.date).reversed())
        .limit(200)
        .map(p -> copy(p, Post.class))
        .toList();
  }

  public List<Post> photosByAuthor(Long authorId) {
    return posts.values().stream()
        .filter(p -> p.category.equals("PHOTO") && p.authorId.equals(authorId))
        .sorted(Comparator.comparing((Post p) -> p.date).reversed())
        .limit(200)
        .map(p -> copy(p, Post.class))
        .toList();
  }

  public Post savePost(Post p) {
    if (p.id == null) {
      p.normalizeCreationTime();
      p.id = ids.incrementAndGet();
    }
    posts.put(p.id, copy(p, Post.class));
    return copy(p, Post.class);
  }

  public synchronized Optional<Post> updatePost(Post changes, Long authorId) {
    Post current = posts.get(changes.id);
    if (current == null || !current.authorId.equals(authorId)) return Optional.empty();
    Post updated = copy(current, Post.class);
    updated.title = changes.title;
    updated.content = changes.content;
    updated.images = new ArrayList<>(changes.images);
    updated.image = changes.image;
    updated.boardCategory = changes.boardCategory;
    posts.put(updated.id, copy(updated, Post.class));
    return Optional.of(copy(updated, Post.class));
  }

  public Optional<Post> post(Long id) {
    return Optional.ofNullable(copy(posts.get(id), Post.class));
  }

  public synchronized boolean deletePost(Long id, Long authorId) {
    Post post = posts.get(id);
    if (post == null || !post.authorId.equals(authorId)) return false;
    posts.remove(id);
    return true;
  }

  public List<Equipment> equipment(Long id) {
    return gear.values().stream()
        .filter(e -> e.userId.equals(id))
        .sorted(Comparator.comparing(e -> e.id))
        .map(e -> copy(e, Equipment.class))
        .toList();
  }

  public Equipment saveEquipment(Equipment e) {
    if (e.id == null) e.id = ids.incrementAndGet();
    gear.put(e.id, copy(e, Equipment.class));
    return copy(e, Equipment.class);
  }

  public void deleteEquipment(Long id, Long userId) {
    gear.computeIfPresent(id, (key, e) -> e.userId.equals(userId) ? null : e);
  }

  public List<Officer> officers() {
    return officers.values().stream()
        .sorted(Comparator.comparingInt((Officer o) -> o.sortOrder).thenComparing(o -> o.id))
        .map(o -> copy(o, Officer.class))
        .toList();
  }

  public Optional<Officer> officer(Long id) {
    return Optional.ofNullable(copy(officers.get(id), Officer.class));
  }

  public Optional<Officer> officerForMember(Long memberId) {
    return officers.values().stream()
        .filter(o -> Objects.equals(o.memberId, memberId))
        .findFirst()
        .map(o -> copy(o, Officer.class));
  }

  public synchronized Officer saveOfficer(Officer o) {
    Member member = copyMember(members.get(o.memberId));
    if (member == null || !member.active)
      throw new org.springframework.dao.DataIntegrityViolationException("활성 회원이 필요합니다.");
    if (officers.values().stream()
        .anyMatch(
            other -> Objects.equals(other.memberId, o.memberId) && !Objects.equals(other.id, o.id)))
      throw new DuplicateKeyException("이미 지정된 임원입니다.");
    // Promotion and card persistence are one atomic operation in both DAO implementations.
    if (!member.role.equals("ADMIN")) {
      member.role = "ADMIN";
      member.sessionVersion++;
    }
    if (o.id == null) o.id = ids.incrementAndGet();
    officers.put(o.id, copy(o, Officer.class));
    members.put(member.id, member);
    return copy(o, Officer.class);
  }

  public void deleteOfficer(Long id) {
    officers.remove(id);
  }

  public Optional<SitePage> page(String slug) {
    return Optional.ofNullable(copy(pages.get(slug), SitePage.class));
  }

  public SitePage savePage(SitePage p) {
    pages.put(p.slug, copy(p, SitePage.class));
    return copy(p, SitePage.class);
  }
}
