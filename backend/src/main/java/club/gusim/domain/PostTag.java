package club.gusim.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "post_tags", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class PostTag {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, length = 50)
  public String name;
}
