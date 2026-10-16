package club.gusim.controller;

import club.gusim.auth.service.LoginService;
import club.gusim.dto.*;
import club.gusim.service.CommunityService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class CommunityController {
  private final LoginService login;
  private final CommunityService community;

  public CommunityController(LoginService login, CommunityService community) {
    this.login = login;
    this.community = community;
  }

  @GetMapping
  public List<Responses.PostView> posts(
      @RequestParam(defaultValue = "PHOTO") String category,
      @RequestParam(required = false) String boardCategory,
      HttpSession session) {
    community.validateCategory(category);
    if (!category.equals("PHOTO")) login.current(session);
    return community.posts(category, boardCategory);
  }

  @GetMapping("/{id}")
  public Responses.PostView detail(@PathVariable Long id, HttpSession session) {
    var post = community.findPost(id);
    if (!post.category.equals("PHOTO")) login.current(session);
    return community.detail(post);
  }

  @PutMapping("/{id}")
  public Responses.PostView update(
      @PathVariable Long id, @Valid @RequestBody Requests.Post input, HttpSession session) {
    return community.updatePost(login.current(session), id, input);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id, HttpSession session) {
    community.deletePost(login.current(session), id);
  }

  @PostMapping
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Responses.PostView create(@Valid @RequestBody Requests.Post input, HttpSession session) {
    return community.createPost(login.current(session), input);
  }
}
