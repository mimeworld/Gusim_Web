package club.gusim.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "equipment_tags")
public class Equipment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false)
  public Long userId;

  @Column(nullable = false, length = 30)
  public String type;

  @Column(nullable = false, length = 100)
  public String maker;

  @Column(nullable = false, length = 150)
  public String model;

  @Column(length = 1000)
  public String description = "";

  public Equipment() {}
}
