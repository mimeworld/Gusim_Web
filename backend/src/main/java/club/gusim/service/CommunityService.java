package club.gusim.service;

import static org.springframework.http.HttpStatus.*;

import club.gusim.domain.*;
import club.gusim.dto.*;
import club.gusim.repository.CommunityRepository;
import club.gusim.repository.MemberRepository;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommunityService {
  private final CommunityRepository repository;
  private final UploadService uploads;
  private final MemberTagsService tags;
  private final MemberRepository members;

  public CommunityService(
      CommunityRepository repository,
      UploadService uploads,
      MemberTagsService tags,
      MemberRepository members) {
    this.repository = repository;
    this.uploads = uploads;
    this.tags = tags;
    this.members = members;
  }

  public void validateCategory(String category) {
    if (!Set.of("PHOTO", "CRITIQUE", "BOARD").contains(category))
      throw new ResponseStatusException(BAD_REQUEST, "지원하지 않는 게시판입니다.");
  }

  public List<Responses.PostView> posts(String category, String boardCategory) {
    validateCategory(category);
    if (boardCategory != null
        && (!category.equals("BOARD")
            || !Set.of("CHAT", "QUESTION", "INFO").contains(boardCategory)))
      throw new ResponseStatusException(BAD_REQUEST, "게시판 분류는 잡담, 질문, 정보 중 선택해주세요.");
    return repository.posts(category, boardCategory).stream().map(p -> view(p, List.of())).toList();
  }

  private void applyInput(Post post, Requests.Post input) {
    validateCategory(input.category());
    List<String> images =
        input.images() == null
            ? (input.image() == null || input.image().isBlank()
                ? List.of()
                : List.of(input.image()))
            : List.copyOf(input.images());
    uploads.validateAttachments(input.category(), images);
    if (!input.category().equals("BOARD") && input.boardCategory() != null)
      throw new ResponseStatusException(BAD_REQUEST, "분류는 일반 게시판에서만 지정할 수 있습니다.");
    post.title = input.title();
    post.content = input.content();
    post.images = new ArrayList<>(images);
    post.image = images.isEmpty() ? "" : images.get(0);
    post.boardCategory =
        input.category().equals("BOARD")
            ? (input.boardCategory() == null ? "CHAT" : input.boardCategory())
            : null;
  }

  public Responses.PostView createPost(Member author, Requests.Post input) {
    Post post = new Post();
    post.authorId = author.id;
    post.author = author.name;
    post.category = input.category();
    applyInput(post, input);
    return detail(repository.save(post));
  }

  public Responses.PostView updatePost(Member author, Long id, Requests.Post input) {
    Post post = findPost(id);
    if (!post.authorId.equals(author.id))
      throw new ResponseStatusException(FORBIDDEN, "본인이 작성한 글만 수정할 수 있습니다.");
    if (!post.category.equals(input.category()))
      throw new ResponseStatusException(BAD_REQUEST, "글 수정 시 게시판 종류를 변경할 수 없습니다.");
    applyInput(post, input);
    Post saved =
        repository
            .update(post, author.id)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "게시글을 찾을 수 없습니다."));
    return detail(saved);
  }

  public Post findPost(Long id) {
    return repository
        .post(id)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "게시글을 찾을 수 없습니다."));
  }

  public Responses.PostView detail(Post post) {
    var images =
        post.images == null || post.images.isEmpty()
            ? (post.image == null || post.image.isBlank() ? List.<String>of() : List.of(post.image))
            : post.images;
    return view(post, images.stream().map(uploads::sizeForView).toList());
  }

  private Responses.PostView view(Post post, List<Long> sizes) {
    var author = members.find(post.authorId);
    return Responses.PostView.from(
        post,
        tags.tags(post.authorId),
        sizes,
        author.map(member -> member.name).orElse(post.author),
        author.map(member -> member.avatar).orElse(""));
  }

  public List<Responses.PostView> photosByAuthor(Long authorId) {
    return repository.photosByAuthor(authorId).stream().map(p -> view(p, List.of())).toList();
  }

  public void deletePost(Member author, Long id) {
    Post post = findPost(id);
    if (!post.authorId.equals(author.id))
      throw new ResponseStatusException(FORBIDDEN, "본인이 작성한 글만 삭제할 수 있습니다.");
    if (!repository.deletePost(id, author.id))
      throw new ResponseStatusException(NOT_FOUND, "게시글을 찾을 수 없습니다.");
  }

  public Responses.EquipmentView addEquipment(Member owner, Requests.Equipment input) {
    Equipment e = new Equipment();
    e.userId = owner.id;
    e.type = input.type();
    e.maker = input.maker();
    e.model = input.model();
    e.description = input.description() == null ? "" : input.description();
    return Responses.EquipmentView.from(repository.save(e));
  }

  public void deleteEquipment(Member owner, Long id) {
    repository.deleteEquipment(id, owner.id);
  }
}
