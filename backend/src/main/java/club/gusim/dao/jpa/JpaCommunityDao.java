package club.gusim.dao.jpa;

import club.gusim.dao.CommunityDao;
import club.gusim.domain.*;
import jakarta.persistence.*;
import java.util.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Profile("db")
@Transactional
public class JpaCommunityDao implements CommunityDao {
  @PersistenceContext private EntityManager em;

  public Optional<Member> member(Long id) {
    return Optional.ofNullable(em.find(Member.class, id));
  }

  public Optional<Member> byStudentNumber(String number) {
    return em.createQuery("from Member where studentNumber=:number", Member.class)
        .setParameter("number", number)
        .getResultStream()
        .findFirst();
  }

  public List<Member> members() {
    return em.createQuery("from Member order by id", Member.class).getResultList();
  }

  public Member saveMember(Member m) {
    if (m.id == null) {
      em.persist(m);
      return m;
    }
    return em.merge(m);
  }

  public Member updateProfile(Long id, String name, String bio, String avatar) {
    em.createQuery("update Member set name=:name, bio=:bio, avatar=:avatar where id=:id")
        .setParameter("name", name)
        .setParameter("bio", bio)
        .setParameter("avatar", avatar)
        .setParameter("id", id)
        .executeUpdate();
    return em.find(Member.class, id);
  }

  public List<Post> posts(String category, String boardCategory) {
    var query =
        em.createQuery(
                "from Post where category=:category"
                    + (boardCategory == null
                        ? ""
                        : " and coalesce(boardCategory,'CHAT')=:boardCategory")
                    + " order by date desc",
                Post.class)
            .setParameter("category", category);
    if (boardCategory != null) query.setParameter("boardCategory", boardCategory);
    return query.setMaxResults(200).getResultList();
  }

  public List<Post> photosByAuthor(Long authorId) {
    return em.createQuery(
            "from Post where category='PHOTO' and authorId=:id order by date desc", Post.class)
        .setParameter("id", authorId)
        .setMaxResults(200)
        .getResultList();
  }

  public Post savePost(Post p) {
    if (p.id == null) {
      em.persist(p);
      return p;
    }
    return em.merge(p);
  }

  public Optional<Post> updatePost(Post changes, Long authorId) {
    Post current = em.find(Post.class, changes.id, LockModeType.PESSIMISTIC_WRITE);
    if (current == null || !current.authorId.equals(authorId)) return Optional.empty();
    current.title = changes.title;
    current.content = changes.content;
    current.image = changes.image;
    current.boardCategory = changes.boardCategory;
    current.images.clear();
    current.images.addAll(changes.images);
    em.flush();
    return Optional.of(current);
  }

  public Optional<Post> post(Long id) {
    return Optional.ofNullable(em.find(Post.class, id));
  }

  public boolean deletePost(Long id, Long authorId) {
    Post post = em.find(Post.class, id, LockModeType.PESSIMISTIC_WRITE);
    if (post == null || !post.authorId.equals(authorId)) return false;
    em.createQuery("delete from PostTagLink where post.id=:id")
        .setParameter("id", id)
        .executeUpdate();
    em.remove(post); // Remove the ordered image collection in the same transaction.
    em.flush();
    return true;
  }

  public List<Equipment> equipment(Long id) {
    return em.createQuery("from Equipment where userId=:id order by id", Equipment.class)
        .setParameter("id", id)
        .getResultList();
  }

  public Equipment saveEquipment(Equipment e) {
    if (e.id == null) {
      em.persist(e);
      return e;
    }
    return em.merge(e);
  }

  public void deleteEquipment(Long id, Long userId) {
    em.createQuery("delete from Equipment where id=:id and userId=:user")
        .setParameter("id", id)
        .setParameter("user", userId)
        .executeUpdate();
  }

  public List<Officer> officers() {
    return em.createQuery("from Officer order by sortOrder,id", Officer.class).getResultList();
  }

  public Optional<Officer> officer(Long id) {
    return Optional.ofNullable(em.find(Officer.class, id));
  }

  public Optional<Officer> officerForMember(Long memberId) {
    return em.createQuery("from Officer where memberId=:id", Officer.class)
        .setParameter("id", memberId)
        .getResultStream()
        .findFirst();
  }

  public Officer saveOfficer(Officer o) {
    Member member = em.find(Member.class, o.memberId, LockModeType.PESSIMISTIC_WRITE);
    if (member == null || !member.active)
      throw new org.springframework.dao.DataIntegrityViolationException("활성 회원이 필요합니다.");
    Officer saved;
    if (o.id == null) {
      em.persist(o);
      saved = o;
    } else {
      saved = em.merge(o);
    }
    em.flush(); // Validate the unique member/card link before granting authority.
    if (!member.role.equals("ADMIN")) {
      member.role = "ADMIN";
      member.sessionVersion++;
    }
    return saved;
  }

  public void deleteOfficer(Long id) {
    em.createQuery("delete from Officer where id=:id").setParameter("id", id).executeUpdate();
  }

  public Optional<SitePage> page(String slug) {
    return Optional.ofNullable(em.find(SitePage.class, slug));
  }

  public SitePage savePage(SitePage p) {
    return em.merge(p);
  }
}
