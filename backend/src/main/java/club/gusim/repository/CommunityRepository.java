package club.gusim.repository;

import club.gusim.dao.CommunityDao;
import club.gusim.domain.*;
import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class CommunityRepository {
  private final CommunityDao dao;

  public CommunityRepository(CommunityDao dao) {
    this.dao = dao;
  }

  public List<Post> posts(String category, String boardCategory) {
    return dao.posts(category, boardCategory);
  }

  public List<Post> photosByAuthor(Long authorId) {
    return dao.photosByAuthor(authorId);
  }

  public Post save(Post post) {
    return dao.savePost(post);
  }

  public Optional<Post> update(Post changes, Long authorId) {
    return dao.updatePost(changes, authorId);
  }

  public java.util.Optional<Post> post(Long id) {
    return dao.post(id);
  }

  public boolean deletePost(Long id, Long authorId) {
    return dao.deletePost(id, authorId);
  }

  public List<Equipment> equipment(Long userId) {
    return dao.equipment(userId);
  }

  public Equipment save(Equipment equipment) {
    return dao.saveEquipment(equipment);
  }

  public void deleteEquipment(Long id, Long userId) {
    dao.deleteEquipment(id, userId);
  }
}
