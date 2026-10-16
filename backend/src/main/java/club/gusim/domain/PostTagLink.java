package club.gusim.domain;

import jakarta.persistence.*;

@Entity
@Table(
    name = "post_tag_links",
    uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "tag_id"}))
public class PostTagLink {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "post_id")
  public Post post;

  @ManyToOne(optional = false)
  @JoinColumn(name = "tag_id")
  public PostTag tag;
}
