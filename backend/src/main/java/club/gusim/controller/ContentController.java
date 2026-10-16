package club.gusim.controller;

import club.gusim.auth.service.LoginService;
import club.gusim.dto.Responses;
import club.gusim.service.ContentService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ContentController {
  private final ContentService content;
  private final LoginService login;

  public ContentController(ContentService content, LoginService login) {
    this.content = content;
    this.login = login;
  }

  @GetMapping("/officers")
  public List<Responses.OfficerView> officers() {
    return content.officers();
  }

  @GetMapping("/pages/ot")
  public Responses.PageView ot(HttpSession session) {
    login.current(session);
    return content.ot();
  }
}
