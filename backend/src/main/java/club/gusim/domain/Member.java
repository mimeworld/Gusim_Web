package club.gusim.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class Member {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, length = 50)
  public String name;

  @Column(nullable = false, unique = true, length = 20)
  public String studentNumber;

  @JsonIgnore
  @Column(nullable = false)
  public String passwordHash;

  @Column(nullable = false, length = 20)
  public String role = "USER";

  @Column(length = 500)
  public String bio = "";

  @Column(length = 2048)
  public String avatar = "";

  @Column(length = 50)
  public String position = "";

  @Column(nullable = false)
  public boolean active = true;

  @Column(nullable = false)
  public int sessionVersion = 0;

  public Member() {}
}
