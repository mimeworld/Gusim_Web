package club.gusim.controller;

import club.gusim.dto.Responses;
import java.util.Map;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SystemController {
  @GetMapping("/health")
  public Map<String, String> health() {
    return Map.of("status", "ok");
  }

  @GetMapping("/csrf")
  public Responses.Csrf csrf(CsrfToken token) {
    return new Responses.Csrf(token.getToken(), token.getHeaderName());
  }
}
