package club.gusim.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "officers")
public class Officer {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  // Nullable only for migration of historical, unlinked cards. New requests require a member.
  @Column(unique = true)
  public Long memberId;

  @Column(nullable = false, length = 50)
  public String name;

  @Column(nullable = false, length = 50)
  public String position;

  @Column(nullable = false, length = 500)
  public String bio = "";

  @Column(nullable = false, length = 2048)
  public String photo = "";

  @Column(nullable = false)
  public int sortOrder;
}
