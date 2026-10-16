package club.gusim;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** The same authorization/data contract runs against both DAO implementations. */
abstract class ApiContract {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;
  @Autowired club.gusim.repository.MemberRepository members;
  @Autowired club.gusim.service.MemberService memberService;
  static final AtomicInteger numbers = new AtomicInteger(20261000);
  static final String PASSWORD = "test-password-123";
  static final Path UPLOADS;

  static {
    try {
      UPLOADS = Files.createTempDirectory("gusim-test-uploads");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @DynamicPropertySource
  static void settings(DynamicPropertyRegistry registry) {
    registry.add("app.bootstrap.student-number", () -> "20260001");
    registry.add("app.bootstrap.password", () -> PASSWORD);
    registry.add("app.upload-dir", () -> UPLOADS.toString());
  }

  String json(Object data) throws Exception {
    return mapper.writeValueAsString(data);
  }

  MockHttpSession login(String number) throws Exception {
    return (MockHttpSession)
        mvc.perform(
                post("/api/auth/login")
                    .with(csrf())
                    .contentType("application/json")
                    .content(
                        json(
                            Map.of(
                                "studentNumber",
                                number,
                                "password",
                                number.equals("20260001") ? PASSWORD : "gusim1234"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.passwordHash").doesNotExist())
            .andExpect(jsonPath("$.sessionVersion").doesNotExist())
            .andReturn()
            .getRequest()
            .getSession();
  }

  JsonNode create(MockHttpSession admin, String role) throws Exception {
    String number = String.valueOf(numbers.incrementAndGet());
    var response =
        mvc.perform(
                post("/api/admin/members")
                    .session(admin)
                    .with(csrf())
                    .contentType("application/json")
                    .content(
                        json(
                            Map.of(
                                "name",
                                "테스트 부원",
                                "studentNumber",
                                number,
                                "password",
                                PASSWORD,
                                "role",
                                role))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.passwordHash").doesNotExist())
            .andReturn()
            .getResponse();
    return mapper.readTree(response.getContentAsString());
  }

  @Test
  void publicRegistrationRemovedAndPermissionsEnforced() throws Exception {
    mvc.perform(
            post("/api/auth/register").with(csrf()).contentType("application/json").content("{}"))
        .andExpect(status().isNotFound());
    mvc.perform(get("/api/admin/members")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/pages/ot")).andExpect(status().isUnauthorized());
    mvc.perform(post("/api/auth/login").contentType("application/json").content("{}"))
        .andExpect(status().isForbidden());
    var admin = login("20260001");
    var user = create(admin, "USER");
    var session = login(user.get("studentNumber").asText());
    mvc.perform(get("/api/admin/members").session(session)).andExpect(status().isForbidden());
    mvc.perform(
            post("/api/admin/members")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content(
                    json(
                        Map.of(
                            "name",
                            "침입",
                            "studentNumber",
                            "99999999",
                            "password",
                            PASSWORD,
                            "role",
                            "ADMIN"))))
        .andExpect(status().isForbidden());
    mvc.perform(
            put("/api/admin/pages/ot")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content(json(Map.of("title", "변경", "content", "불가"))))
        .andExpect(status().isForbidden());
    mvc.perform(
            post("/api/admin/officers")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content(
                    json(
                        Map.of(
                            "memberId",
                            user.get("id").asLong(),
                            "position",
                            "회장",
                            "bio",
                            "",
                            "photo",
                            "",
                            "sortOrder",
                            0))))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/pages/ot").session(session)).andExpect(status().isOk());
    mvc.perform(
            post("/api/posts")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content(json(Map.of("title", "일반 게시글", "content", "본문", "category", "BOARD"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.authorId").value(user.get("id").asLong()));
    mvc.perform(get("/api/posts?category=BOARD")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/posts")).andExpect(status().isOk());
  }

  @Test
  void promotionDeactivationAndAdminSelfProtection() throws Exception {
    var admin = login("20260001");
    var user = create(admin, "USER");
    String number = user.get("studentNumber").asText();
    long id = user.get("id").asLong();
    var oldSession = login(number);
    mvc.perform(
            put("/api/admin/members/" + id)
                .session(admin)
                .with(csrf())
                .contentType("application/json")
                .content(
                    json(
                        Map.of(
                            "name",
                            "추가 관리자",
                            "studentNumber",
                            number,
                            "role",
                            "ADMIN",
                            "active",
                            true))))
        .andExpect(status().isOk());
    mvc.perform(get("/api/me").session(oldSession)).andExpect(status().isUnauthorized());
    var promoted = login(number);
    mvc.perform(get("/api/admin/members").session(promoted)).andExpect(status().isOk());
    mvc.perform(
            put("/api/admin/members/" + id)
                .session(promoted)
                .with(csrf())
                .contentType("application/json")
                .content(
                    json(
                        Map.of(
                            "name",
                            "관리자",
                            "studentNumber",
                            number,
                            "role",
                            "USER",
                            "active",
                            true))))
        .andExpect(status().isConflict());
    mvc.perform(
            put("/api/admin/members/" + id)
                .session(admin)
                .with(csrf())
                .contentType("application/json")
                .content(
                    json(
                        Map.of(
                            "name",
                            "휴면 부원",
                            "studentNumber",
                            number,
                            "role",
                            "USER",
                            "active",
                            false))))
        .andExpect(status().isOk());
    mvc.perform(get("/api/me").session(promoted)).andExpect(status().isUnauthorized());
    mvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType("application/json")
                .content(json(Map.of("studentNumber", number, "password", PASSWORD))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void duplicateValidationAndPasswordReset() throws Exception {
    var admin = login("20260001");
    var user = create(admin, "USER");
    String number = user.get("studentNumber").asText();
    var oldSession = login(number);
    mvc.perform(
            post("/api/admin/members")
                .session(admin)
                .with(csrf())
                .contentType("application/json")
                .content(
                    json(
                        Map.of(
                            "name",
                            "중복",
                            "studentNumber",
                            number,
                            "password",
                            PASSWORD,
                            "role",
                            "USER"))))
        .andExpect(status().isConflict());
    mvc.perform(
            post("/api/admin/members")
                .session(admin)
                .with(csrf())
                .contentType("application/json")
                .content("{}"))
        .andExpect(status().isBadRequest());
    mvc.perform(
            put("/api/admin/members/" + user.get("id").asLong() + "/password")
                .session(admin)
                .with(csrf())
                .contentType("application/json")
                .content(json(Map.of("password", "changed-password-456"))))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/me").session(oldSession)).andExpect(status().isUnauthorized());
    mvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType("application/json")
                .content(json(Map.of("studentNumber", number, "password", PASSWORD))))
        .andExpect(status().isUnauthorized());
    mvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType("application/json")
                .content(json(Map.of("studentNumber", number, "password", "changed-password-456"))))
        .andExpect(status().isOk());
  }

  @Test
  void editableOtAndOfficerPortrait() throws Exception {
    var admin = login("20260001");
    var candidate = create(admin, "USER");
    long memberId = candidate.get("id").asLong();
    var oldSession = login(candidate.get("studentNumber").asText());
    mvc.perform(
            put("/api/admin/pages/ot")
                .session(admin)
                .with(csrf())
                .contentType("application/json")
                .content(json(Map.of("title", "환영합니다", "content", "첫 줄\n둘째 줄"))))
        .andExpect(status().isOk());
    mvc.perform(get("/api/pages/ot").session(admin))
        .andExpect(jsonPath("$.title").value("환영합니다"))
        .andExpect(jsonPath("$.content").value("첫 줄\n둘째 줄"));
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    ImageIO.write(new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB), "png", bytes);
    var uploadResult =
        mvc.perform(
                multipart("/api/uploads")
                    .file(
                        new MockMultipartFile(
                            "file", "portrait.png", "image/png", bytes.toByteArray()))
                    .session(admin)
                    .with(csrf()))
            .andExpect(status().isCreated())
            .andReturn();
    String url =
        mapper.readTree(uploadResult.getResponse().getContentAsString()).get("url").asText();
    mvc.perform(get(url)).andExpect(status().isOk());
    var officerResult =
        mvc.perform(
                post("/api/admin/officers")
                    .session(admin)
                    .with(csrf())
                    .contentType("application/json")
                    .content(
                        json(
                            Map.of(
                                "memberId",
                                memberId,
                                "position",
                                "회장",
                                "bio",
                                "안녕하세요",
                                "photo",
                                url,
                                "sortOrder",
                                1))))
            .andExpect(status().isCreated())
            .andReturn();
    long id = mapper.readTree(officerResult.getResponse().getContentAsString()).get("id").asLong();
    mvc.perform(get("/api/me").session(oldSession)).andExpect(status().isUnauthorized());
    var officerSession = login(candidate.get("studentNumber").asText());
    mvc.perform(get("/api/me").session(officerSession))
        .andExpect(jsonPath("$.role").value("ADMIN"))
        .andExpect(jsonPath("$.tags[0]").value("회장"))
        .andExpect(jsonPath("$.tags.length()").value(1));
    mvc.perform(get("/api/admin/members").session(officerSession)).andExpect(status().isOk());
    mvc.perform(
            post("/api/posts")
                .session(officerSession)
                .with(csrf())
                .contentType("application/json")
                .content(
                    json(
                        Map.of(
                            "title",
                            "크리틱 사진",
                            "content",
                            "촬영 의도",
                            "category",
                            "CRITIQUE",
                            "image",
                            url))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.image").value(url))
        .andExpect(jsonPath("$.authorTags[0]").value("회장"));
    mvc.perform(
            post("/api/posts")
                .session(officerSession)
                .with(csrf())
                .contentType("application/json")
                .content(
                    json(Map.of("title", "사진 없는 크리틱", "content", "본문", "category", "CRITIQUE"))))
        .andExpect(status().isBadRequest());
    mvc.perform(
            put("/api/admin/officers/" + id)
                .session(admin)
                .with(csrf())
                .contentType("application/json")
                .content(
                    json(
                        Map.of(
                            "memberId",
                            memberId,
                            "position",
                            "부회장",
                            "bio",
                            "소개 수정",
                            "photo",
                            url,
                            "sortOrder",
                            2))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.photo").value(url));
    mvc.perform(get("/api/officers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("테스트 부원"));
    mvc.perform(get("/api/me").session(officerSession))
        .andExpect(jsonPath("$.tags[0]").value("부회장"));
    mvc.perform(get("/api/posts?category=CRITIQUE").session(officerSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].image").value(url))
        .andExpect(jsonPath("$[0].authorTags[0]").value("부회장"));
    mvc.perform(delete("/api/admin/officers/" + id).session(admin).with(csrf()))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/officers")).andExpect(jsonPath("$.length()").value(0));
    mvc.perform(get("/api/me").session(officerSession))
        .andExpect(jsonPath("$.tags.length()").value(1))
        .andExpect(jsonPath("$.role").value("ADMIN"));
    mvc.perform(
            multipart("/api/uploads")
                .file(
                    new MockMultipartFile(
                        "file", "bad.jpg", "image/jpeg", "not-an-image".getBytes()))
                .session(admin)
                .with(csrf()))
        .andExpect(status().isBadRequest());
  }

  @Test
  void equipmentOwnershipAndRealCsrfHeader() throws Exception {
    var admin = login("20260001");
    var owner = create(admin, "USER");
    var other = create(admin, "USER");
    var ownerSession = login(owner.get("studentNumber").asText());
    var otherSession = login(other.get("studentNumber").asText());
    var result =
        mvc.perform(
                post("/api/me/equipment")
                    .session(ownerSession)
                    .with(csrf())
                    .contentType("application/json")
                    .content(json(Map.of("type", "카메라", "maker", "Sony", "model", "A7"))))
            .andExpect(status().isCreated())
            .andReturn();
    long id = mapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    mvc.perform(delete("/api/me/equipment/" + id).session(otherSession).with(csrf()))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/me").session(ownerSession))
        .andExpect(jsonPath("$.equipment.length()").value(1));
    mvc.perform(delete("/api/me/equipment/" + id).session(ownerSession).with(csrf()))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/me").session(ownerSession))
        .andExpect(jsonPath("$.equipment.length()").value(0));
    var tokenResult = mvc.perform(get("/api/csrf")).andExpect(status().isOk()).andReturn();
    var token = mapper.readTree(tokenResult.getResponse().getContentAsString());
    var session = (MockHttpSession) tokenResult.getRequest().getSession();
    mvc.perform(
            post("/api/auth/login")
                .session(session)
                .header(token.get("headerName").asText(), token.get("token").asText())
                .contentType("application/json")
                .content(json(Map.of("studentNumber", "20260001", "password", PASSWORD))))
        .andExpect(status().isOk());
    mvc.perform(post("/api/auth/logout").session(session).with(csrf()))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void staleProfileEditCannotRestoreDeactivatedAccount() throws Exception {
    var admin = login("20260001");
    var user = create(admin, "USER");
    long id = user.get("id").asLong();
    var stale = members.find(id).orElseThrow();
    mvc.perform(
            put("/api/admin/members/" + id)
                .session(admin)
                .with(csrf())
                .contentType("application/json")
                .content(
                    json(
                        Map.of(
                            "name",
                            "비활성 회원",
                            "studentNumber",
                            user.get("studentNumber").asText(),
                            "role",
                            "USER",
                            "active",
                            false))))
        .andExpect(status().isOk());
    memberService.update(stale, new club.gusim.dto.Requests.Profile("프로필 수정", "소개", ""));
    var stored = members.find(id).orElseThrow();
    assertFalse(stored.active);
    assertEquals(1, stored.sessionVersion);
    assertEquals(stale.passwordHash, stored.passwordHash);
  }

  @Test
  void officerAssignmentRequiresUniqueActiveMember() throws Exception {
    var admin = login("20260001");
    var user = create(admin, "USER");
    long memberId = user.get("id").asLong();
    var input =
        Map.of("memberId", memberId, "position", "총무", "bio", "", "photo", "", "sortOrder", 0);
    mvc.perform(
            post("/api/admin/officers")
                .session(admin)
                .with(csrf())
                .contentType("application/json")
                .content(
                    json(
                        Map.of(
                            "memberId",
                            999999999,
                            "position",
                            "총무",
                            "bio",
                            "",
                            "photo",
                            "",
                            "sortOrder",
                            0))))
        .andExpect(status().isNotFound());
    var result =
        mvc.perform(
                post("/api/admin/officers")
                    .session(admin)
                    .with(csrf())
                    .contentType("application/json")
                    .content(json(input)))
            .andExpect(status().isCreated())
            .andReturn();
    long officerId = mapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    mvc.perform(
            post("/api/admin/officers")
                .session(admin)
                .with(csrf())
                .contentType("application/json")
                .content(json(input)))
        .andExpect(status().isConflict());
    mvc.perform(
            put("/api/admin/members/" + memberId)
                .session(admin)
                .with(csrf())
                .contentType("application/json")
                .content(
                    json(
                        Map.of(
                            "name",
                            "테스트",
                            "studentNumber",
                            user.get("studentNumber").asText(),
                            "role",
                            "USER",
                            "active",
                            true))))
        .andExpect(status().isConflict());
    mvc.perform(delete("/api/admin/officers/" + officerId).session(admin).with(csrf()))
        .andExpect(status().isNoContent());
    mvc.perform(
            put("/api/admin/members/" + memberId)
                .session(admin)
                .with(csrf())
                .contentType("application/json")
                .content(
                    json(
                        Map.of(
                            "name",
                            "테스트",
                            "studentNumber",
                            user.get("studentNumber").asText(),
                            "role",
                            "USER",
                            "active",
                            false))))
        .andExpect(status().isOk());
    mvc.perform(
            post("/api/admin/officers")
                .session(admin)
                .with(csrf())
                .contentType("application/json")
                .content(json(input)))
        .andExpect(status().isConflict());
  }

  @Test
  void detailVisibilityAndOwnerDeletion() throws Exception {
    var admin = login("20260001");
    var first = create(admin, "USER");
    var second = create(admin, "USER");
    var owner = login(first.get("studentNumber").asText());
    var other = login(second.get("studentNumber").asText());
    var result =
        mvc.perform(
                post("/api/posts")
                    .session(owner)
                    .with(csrf())
                    .contentType("application/json")
                    .content(
                        json(Map.of("title", "내 게시글", "content", "전체 본문", "category", "BOARD"))))
            .andExpect(status().isCreated())
            .andReturn();
    long id = mapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    mvc.perform(get("/api/posts/" + id)).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/posts/" + id).session(other))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("전체 본문"));
    mvc.perform(delete("/api/posts/" + id).session(other).with(csrf()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("본인이 작성한 글만 삭제할 수 있습니다."));
    mvc.perform(delete("/api/posts/" + id).session(admin).with(csrf()))
        .andExpect(status().isForbidden());
    mvc.perform(delete("/api/posts/" + id).session(owner).with(csrf()))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/posts/" + id).session(owner)).andExpect(status().isNotFound());
  }

  @Test
  void defaultPasswordAndOwnPasswordChange() throws Exception {
    var admin = login("20260001");
    var user = create(admin, "USER");
    String number = user.get("studentNumber").asText();
    var session = login(number);
    mvc.perform(get("/api/me").session(session)).andExpect(jsonPath("$.tags[0]").value("일반 회원"));
    mvc.perform(
            put("/api/me/password")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content(json(Map.of("currentPassword", "wrong", "newPassword", "newpass1"))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("현재 비밀번호가 일치하지 않습니다."));
    mvc.perform(
            put("/api/me/password")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content(json(Map.of("currentPassword", "gusim1234", "newPassword", "short"))))
        .andExpect(status().isBadRequest());
    mvc.perform(
            put("/api/me/password")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content(json(Map.of("currentPassword", "gusim1234", "newPassword", "newpass1"))))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    mvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType("application/json")
                .content(json(Map.of("studentNumber", number, "password", "gusim1234"))))
        .andExpect(status().isUnauthorized());
    mvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType("application/json")
                .content(json(Map.of("studentNumber", number, "password", "newpass1"))))
        .andExpect(status().isOk());
  }

  @Test
  void equipmentDescriptionAndFiftyMegabyteLimit() throws Exception {
    var admin = login("20260001");
    mvc.perform(
            post("/api/me/equipment")
                .session(admin)
                .with(csrf())
                .contentType("application/json")
                .content(
                    json(
                        Map.of(
                            "type",
                            "카메라",
                            "maker",
                            "Canon",
                            "model",
                            "R6",
                            "description",
                            "출사용 바디"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.description").value("출사용 바디"));
    mvc.perform(get("/api/me").session(admin))
        .andExpect(jsonPath("$.equipment[?(@.description == '출사용 바디')]").isNotEmpty());
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    ImageIO.write(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB), "png", bytes);
    MockMultipartFile boundary =
        new MockMultipartFile("file", "image.png", "image/png", bytes.toByteArray()) {
          @Override
          public long getSize() {
            return 50L * 1024 * 1024;
          }
        };
    mvc.perform(multipart("/api/uploads").file(boundary).session(admin).with(csrf()))
        .andExpect(status().isCreated());
    MockMultipartFile oversized =
        new MockMultipartFile("file", "image.png", "image/png", bytes.toByteArray()) {
          @Override
          public long getSize() {
            return 50L * 1024 * 1024 + 1;
          }
        };
    mvc.perform(multipart("/api/uploads").file(oversized).session(admin).with(csrf()))
        .andExpect(status().isBadRequest());
  }

  private MockMultipartFile imageFile(String name, long size) throws Exception {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    ImageIO.write(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB), "png", bytes);
    return new MockMultipartFile("files", name, "image/png", bytes.toByteArray()) {
      @Override
      public long getSize() {
        return size;
      }
    };
  }

  private java.util.List<String> batch(
      MockHttpSession session, String category, MockMultipartFile... files) throws Exception {
    var request = multipart("/api/uploads/batch");
    request.param("category", category);
    for (var file : files) request.file(file);
    var result =
        mvc.perform(request.session(session).with(csrf()))
            .andExpect(status().isCreated())
            .andReturn();
    var urls = mapper.readTree(result.getResponse().getContentAsString()).get("urls");
    java.util.List<String> values = new java.util.ArrayList<>();
    urls.forEach(url -> values.add(url.asText()));
    return values;
  }

  @Test
  void multipleBoardImagesCategoriesAndDeletion() throws Exception {
    var admin = login("20260001");
    var images =
        batch(
            admin,
            "BOARD",
            imageFile("one.png", 100),
            imageFile("two.png", 200),
            imageFile("three.png", 300));
    var result =
        mvc.perform(
                post("/api/posts")
                    .session(admin)
                    .with(csrf())
                    .contentType("application/json")
                    .content(
                        json(
                            Map.of(
                                "title",
                                "여러 사진 정보",
                                "content",
                                "본문",
                                "category",
                                "BOARD",
                                "boardCategory",
                                "INFO",
                                "images",
                                images))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.images.length()").value(3))
            .andExpect(jsonPath("$.image").value(images.get(0)))
            .andReturn();
    long id = mapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    mvc.perform(get("/api/posts/" + id).session(admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.images[2]").value(images.get(2)))
        .andExpect(jsonPath("$.boardCategory").value("INFO"));
    mvc.perform(get("/api/posts?category=BOARD&boardCategory=INFO").session(admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.id == " + id + ")]").isNotEmpty());
    mvc.perform(get("/api/posts?category=BOARD&boardCategory=CHAT").session(admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.id == " + id + ")]").isEmpty());
    mvc.perform(get("/api/posts?category=BOARD&boardCategory=INVALID").session(admin))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/posts")
                .session(admin)
                .with(csrf())
                .contentType("application/json")
                .content(
                    json(
                        Map.of(
                            "title",
                            "잘못된 분류",
                            "category",
                            "BOARD",
                            "boardCategory",
                            "OTHER",
                            "images",
                            images))))
        .andExpect(status().isBadRequest());
    mvc.perform(delete("/api/posts/" + id).session(admin).with(csrf()))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/posts/" + id).session(admin)).andExpect(status().isNotFound());
  }

  @Test
  void critiqueTwoImagesAndThreeImageRejection() throws Exception {
    var admin = login("20260001");
    var images = batch(admin, "CRITIQUE", imageFile("one.png", 100), imageFile("two.png", 100));
    mvc.perform(
            post("/api/posts")
                .session(admin)
                .with(csrf())
                .contentType("application/json")
                .content(json(Map.of("title", "비교 크리틱", "category", "CRITIQUE", "images", images))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.images.length()").value(2));
    mvc.perform(
            multipart("/api/uploads/batch")
                .file(imageFile("one.png", 100))
                .file(imageFile("two.png", 100))
                .file(imageFile("three.png", 100))
                .param("category", "CRITIQUE")
                .session(admin)
                .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("크리틱에는 사진을 1~2장 첨부해주세요."));
    var three =
        batch(
            admin,
            "BOARD",
            imageFile("one.png", 100),
            imageFile("two.png", 100),
            imageFile("three.png", 100));
    mvc.perform(
            post("/api/posts")
                .session(admin)
                .with(csrf())
                .contentType("application/json")
                .content(json(Map.of("title", "초과 크리틱", "category", "CRITIQUE", "images", three))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void aggregateLimitIsValidatedAtUploadAndPostCreation() throws Exception {
    var admin = login("20260001");
    long fifty = 50L * 1024 * 1024;
    mvc.perform(
            multipart("/api/uploads/batch")
                .file(imageFile("one.png", fifty))
                .file(imageFile("two.png", fifty))
                .file(imageFile("three.png", 1))
                .param("category", "BOARD")
                .session(admin)
                .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("첨부 사진의 합계는 100MB 이하여야 합니다."));
    var images = batch(admin, "BOARD", imageFile("one.png", fifty), imageFile("two.png", fifty));
    mvc.perform(
            post("/api/posts")
                .session(admin)
                .with(csrf())
                .contentType("application/json")
                .content(
                    json(
                        Map.of(
                            "title",
                            "정확히 100MB",
                            "category",
                            "BOARD",
                            "boardCategory",
                            "QUESTION",
                            "images",
                            images))))
        .andExpect(status().isCreated());
    var extra = batch(admin, "BOARD", imageFile("extra.png", 1));
    java.util.List<String> combined = new java.util.ArrayList<>(images);
    combined.addAll(extra);
    mvc.perform(
            post("/api/posts")
                .session(admin)
                .with(csrf())
                .contentType("application/json")
                .content(
                    json(Map.of("title", "합계 초과 우회", "category", "BOARD", "images", combined))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void ownerCanEditWithoutChangingAuthorOrCreationDate() throws Exception {
    var admin = login("20260001");
    var user = create(admin, "USER");
    var other = create(admin, "USER");
    var owner = login(user.get("studentNumber").asText());
    var outsider = login(other.get("studentNumber").asText());
    var result =
        mvc.perform(
                post("/api/posts")
                    .session(owner)
                    .with(csrf())
                    .contentType("application/json")
                    .content(
                        json(Map.of("title", "원래 제목", "content", "원래 내용", "category", "BOARD"))))
            .andExpect(status().isCreated())
            .andReturn();
    var original = mapper.readTree(result.getResponse().getContentAsString());
    long id = original.get("id").asLong();
    assertEquals(0, java.time.Instant.parse(original.get("date").asText()).getNano() % 1000);
    mvc.perform(get("/api/posts/" + id).session(owner))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.date").value(original.get("date").asText()));
    String body =
        json(
            Map.of(
                "title",
                "수정 제목",
                "content",
                "수정 내용",
                "category",
                "BOARD",
                "boardCategory",
                "QUESTION",
                "images",
                java.util.List.of()));
    mvc.perform(
            put("/api/posts/" + id)
                .session(outsider)
                .with(csrf())
                .contentType("application/json")
                .content(body))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("본인이 작성한 글만 수정할 수 있습니다."));
    mvc.perform(
            put("/api/posts/" + id)
                .session(admin)
                .with(csrf())
                .contentType("application/json")
                .content(body))
        .andExpect(status().isForbidden());
    mvc.perform(
            put("/api/posts/" + id)
                .session(owner)
                .with(csrf())
                .contentType("application/json")
                .content(json(Map.of("title", "이동 시도", "category", "PHOTO"))))
        .andExpect(status().isBadRequest());
    mvc.perform(
            put("/api/posts/" + id)
                .session(owner)
                .with(csrf())
                .contentType("application/json")
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.title").value("수정 제목"))
        .andExpect(jsonPath("$.boardCategory").value("QUESTION"))
        .andExpect(jsonPath("$.authorId").value(user.get("id").asLong()))
        .andExpect(jsonPath("$.date").value(original.get("date").asText()));
    mvc.perform(get("/api/posts/" + id).session(owner))
        .andExpect(jsonPath("$.content").value("수정 내용"));
    mvc.perform(delete("/api/posts/" + id).session(owner).with(csrf()))
        .andExpect(status().isNoContent());
    mvc.perform(
            put("/api/posts/" + id)
                .session(owner)
                .with(csrf())
                .contentType("application/json")
                .content(body))
        .andExpect(status().isNotFound());
  }

  @Test
  void editRetainsRemovesAndAddsAttachmentsWithLimits() throws Exception {
    var admin = login("20260001");
    var images =
        batch(
            admin,
            "BOARD",
            imageFile("one.png", 100),
            imageFile("two.png", 200),
            imageFile("three.png", 300));
    var result =
        mvc.perform(
                post("/api/posts")
                    .session(admin)
                    .with(csrf())
                    .contentType("application/json")
                    .content(json(Map.of("title", "사진 글", "category", "BOARD", "images", images))))
            .andExpect(status().isCreated())
            .andReturn();
    long id = mapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    mvc.perform(get("/api/posts/" + id).session(admin))
        .andExpect(jsonPath("$.imageSizes[0]").value(100))
        .andExpect(jsonPath("$.imageSizes[2]").value(300));
    var extra = batch(admin, "BOARD", imageFile("new.png", 400));
    var updated = java.util.List.of(images.get(1), extra.get(0));
    mvc.perform(
            put("/api/posts/" + id)
                .session(admin)
                .with(csrf())
                .contentType("application/json")
                .content(
                    json(
                        Map.of(
                            "title",
                            "사진 교체",
                            "category",
                            "BOARD",
                            "boardCategory",
                            "INFO",
                            "images",
                            updated))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.images.length()").value(2))
        .andExpect(jsonPath("$.images[0]").value(images.get(1)))
        .andExpect(jsonPath("$.images[1]").value(extra.get(0)))
        .andExpect(jsonPath("$.imageSizes[1]").value(400));
    mvc.perform(get("/api/posts/" + id).session(admin))
        .andExpect(jsonPath("$.image").value(images.get(1)));
    var critique =
        mvc.perform(
                post("/api/posts")
                    .session(admin)
                    .with(csrf())
                    .contentType("application/json")
                    .content(
                        json(Map.of("title", "크리틱", "category", "CRITIQUE", "images", updated))))
            .andExpect(status().isCreated())
            .andReturn();
    long critiqueId =
        mapper.readTree(critique.getResponse().getContentAsString()).get("id").asLong();
    mvc.perform(
            put("/api/posts/" + critiqueId)
                .session(admin)
                .with(csrf())
                .contentType("application/json")
                .content(json(Map.of("title", "초과", "category", "CRITIQUE", "images", images))))
        .andExpect(status().isBadRequest());
    mvc.perform(
            put("/api/posts/" + critiqueId)
                .session(admin)
                .with(csrf())
                .contentType("application/json")
                .content(
                    json(
                        Map.of(
                            "title",
                            "사진 없음",
                            "category",
                            "CRITIQUE",
                            "images",
                            java.util.List.of()))))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/posts/" + critiqueId).session(admin))
        .andExpect(jsonPath("$.title").value("크리틱"))
        .andExpect(jsonPath("$.images.length()").value(2));
  }

  @Test
  void directoryGroupsOfficersAndSortsKoreanNames() throws Exception {
    var admin = login("20260001");
    var park = create(admin, "USER");
    var kim = create(admin, "ADMIN");
    var na = create(admin, "USER");
    java.util.Map<Long, String> names =
        Map.of(
            park.get("id").asLong(),
            "박소연",
            kim.get("id").asLong(),
            "김가영",
            na.get("id").asLong(),
            "나도현");
    for (var entry : names.entrySet()) {
      var member = members.find(entry.getKey()).orElseThrow();
      member.name = entry.getValue();
      members.save(member);
    }
    var officerResult =
        mvc.perform(
                post("/api/admin/officers")
                    .session(admin)
                    .with(csrf())
                    .contentType("application/json")
                    .content(
                        json(
                            Map.of(
                                "memberId",
                                na.get("id").asLong(),
                                "position",
                                "회장",
                                "bio",
                                "소개",
                                "photo",
                                "",
                                "sortOrder",
                                0))))
            .andExpect(status().isCreated())
            .andReturn();
    long officerId =
        mapper.readTree(officerResult.getResponse().getContentAsString()).get("id").asLong();
    mvc.perform(get("/api/members")).andExpect(status().isUnauthorized());
    var result =
        mvc.perform(get("/api/members").session(admin))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.officers[?(@.id == " + na.get("id").asLong() + ")].position")
                    .value(org.hamcrest.Matchers.hasItem("회장")))
            .andExpect(jsonPath("$.officers[*].studentNumber").isEmpty())
            .andExpect(jsonPath("$.members[*].role").isEmpty())
            .andReturn();
    var directory = mapper.readTree(result.getResponse().getContentAsString());
    java.util.List<String> ordered = new java.util.ArrayList<>();
    for (var member : directory.get("members"))
      if (names.containsKey(member.get("id").asLong())) ordered.add(member.get("name").asText());
    assertEquals(java.util.List.of("김가영", "박소연"), ordered); // A standalone ADMIN is not an officer.
    var inactive = members.find(park.get("id").asLong()).orElseThrow();
    inactive.active = false;
    members.save(inactive);
    mvc.perform(get("/api/members").session(admin))
        .andExpect(jsonPath("$.members[?(@.id == " + inactive.id + ")]").isEmpty());
    mvc.perform(get("/api/members/" + inactive.id).session(admin)).andExpect(status().isNotFound());
    mvc.perform(delete("/api/admin/officers/" + officerId).session(admin).with(csrf()))
        .andExpect(status().isNoContent());
  }

  @Test
  void memberProfileIsReadOnlyAndExcludesPrivateAccountData() throws Exception {
    var admin = login("20260001");
    var target = create(admin, "USER");
    var targetSession = login(target.get("studentNumber").asText());
    mvc.perform(get("/api/members/" + target.get("id").asLong()))
        .andExpect(status().isUnauthorized());
    mvc.perform(
            post("/api/me/equipment")
                .session(targetSession)
                .with(csrf())
                .contentType("application/json")
                .content(
                    json(
                        Map.of(
                            "type",
                            "카메라",
                            "maker",
                            "Canon",
                            "model",
                            "R6",
                            "description",
                            "장비 소개"))))
        .andExpect(status().isCreated());
    var images = batch(targetSession, "PHOTO", imageFile("profile-photo.png", 100));
    mvc.perform(
            post("/api/posts")
                .session(targetSession)
                .with(csrf())
                .contentType("application/json")
                .content(json(Map.of("title", "공유 사진", "category", "PHOTO", "images", images))))
        .andExpect(status().isCreated());
    mvc.perform(
            post("/api/posts")
                .session(targetSession)
                .with(csrf())
                .contentType("application/json")
                .content(json(Map.of("title", "회원 게시글", "category", "BOARD", "content", "본문"))))
        .andExpect(status().isCreated());
    mvc.perform(get("/api/members/" + target.get("id").asLong()).session(admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.studentNumber").doesNotExist())
        .andExpect(jsonPath("$.role").doesNotExist())
        .andExpect(jsonPath("$.passwordHash").doesNotExist())
        .andExpect(jsonPath("$.sessionVersion").doesNotExist())
        .andExpect(jsonPath("$.equipment[0].description").value("장비 소개"))
        .andExpect(jsonPath("$.photos.length()").value(1))
        .andExpect(jsonPath("$.photos[0].title").value("공유 사진"));
    mvc.perform(
            put("/api/members/" + target.get("id").asLong())
                .session(admin)
                .with(csrf())
                .contentType("application/json")
                .content("{}"))
        .andExpect(status().isMethodNotAllowed());
  }

  @Test
  void postAuthorsUseCurrentNameAndProfilePhoto() throws Exception {
    var admin = login("20260001");
    var user = create(admin, "USER");
    var session = login(user.get("studentNumber").asText());
    var images = batch(session, "PHOTO", imageFile("author.png", 100));
    var result =
        mvc.perform(
                post("/api/posts")
                    .session(session)
                    .with(csrf())
                    .contentType("application/json")
                    .content(
                        json(Map.of("title", "작성자 확인", "category", "PHOTO", "images", images))))
            .andExpect(status().isCreated())
            .andReturn();
    long id = mapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    mvc.perform(
            put("/api/me")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content(json(Map.of("name", "변경된 이름", "bio", "소개", "avatar", images.get(0)))))
        .andExpect(status().isOk());
    mvc.perform(get("/api/posts/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.author").value("변경된 이름"))
        .andExpect(jsonPath("$.authorAvatar").value(images.get(0)));
    mvc.perform(get("/api/posts"))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$[?(@.id == " + id + ")].authorAvatar")
                .value(org.hamcrest.Matchers.hasItem(images.get(0))));
  }
}
