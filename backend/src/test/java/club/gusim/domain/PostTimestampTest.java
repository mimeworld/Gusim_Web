package club.gusim.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class PostTimestampTest {
  @Test
  void creationTimeTruncatesNanosecondsBeforePersistence() {
    Post post = new Post();
    post.date = Instant.parse("2026-10-16T11:45:57.565767804Z");
    post.normalizeCreationTime();
    assertEquals(Instant.parse("2026-10-16T11:45:57.565767Z"), post.date);
    Instant normalized = post.date;
    post.normalizeCreationTime();
    assertEquals(normalized, post.date);
  }

  @Test
  void constructorAndMissingTimestampUseMicroseconds() {
    Post post = new Post();
    assertEquals(0, post.date.getNano() % 1000);
    post.date = null;
    post.normalizeCreationTime();
    assertNotNull(post.date);
    assertEquals(0, post.date.getNano() % 1000);
  }
}
