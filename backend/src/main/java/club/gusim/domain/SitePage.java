package club.gusim.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "site_pages")
public class SitePage {
  @Id
  @Column(length = 30)
  public String slug;

  @Column(nullable = false, length = 200)
  public String title;

  @Lob
  @Column(nullable = false)
  public String content;

  public SitePage() {}

  public SitePage(String slug, String title, String content) {
    this.slug = slug;
    this.title = title;
    this.content = content;
  }
}
