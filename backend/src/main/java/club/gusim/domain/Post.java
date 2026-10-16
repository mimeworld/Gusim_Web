package club.gusim.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Entity
@Table(name = "posts")
public class Post {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false)
  public Long authorId;

  @Column(nullable = false, length = 50)
  public String author;

  @Column(nullable = false, length = 200)
  public String title;

  @Lob public String content;

  @Column(nullable = false, length = 20)
  public String category;

  @Column(length = 2048)
  public String image;

  @Column(length = 20)
  public String boardCategory;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "post_images", joinColumns = @JoinColumn(name = "post_id"))
  @OrderColumn(name = "sort_order")
  @Column(name = "image_url", nullable = false, length = 2048)
  public List<String> images = new ArrayList<>();

  // Match MySQL TIMESTAMP(6) before the first API response is serialized.
  @Column(nullable = false, updatable = false)
  public Instant date = Instant.now().truncatedTo(ChronoUnit.MICROS);

  @PrePersist
  public void normalizeCreationTime() {
    date = (date == null ? Instant.now() : date).truncatedTo(ChronoUnit.MICROS);
  }

  public Post() {}
}
