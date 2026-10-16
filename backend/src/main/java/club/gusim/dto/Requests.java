package club.gusim.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public final class Requests {
  private Requests() {}

  public record Login(@NotBlank String studentNumber, @NotBlank String password) {}

  public record CreateMember(
      @NotBlank @Size(max = 50) String name,
      @NotNull @Pattern(regexp = "[0-9]{4,20}") String studentNumber,
      @NotNull @Pattern(regexp = "USER|ADMIN") String role) {}

  public record UpdateMember(
      @NotBlank @Size(max = 50) String name,
      @NotNull @Pattern(regexp = "[0-9]{4,20}") String studentNumber,
      @NotNull @Pattern(regexp = "USER|ADMIN") String role,
      @NotNull Boolean active) {}

  public record Password(@NotBlank @Size(min = 8, max = 60) String password) {}

  public record ChangePassword(
      @NotBlank String currentPassword, @NotBlank @Size(min = 8, max = 60) String newPassword) {}

  public record Profile(
      @NotBlank @Size(max = 50) String name,
      @Size(max = 500) String bio,
      @Size(max = 2048) String avatar) {}

  public record Post(
      @NotBlank @Size(max = 200) String title,
      @Size(max = 20000) String content,
      @NotNull @Pattern(regexp = "PHOTO|CRITIQUE|BOARD") String category,
      @Size(max = 2048) String image,
      List<@NotBlank @Size(max = 2048) String> images,
      @Pattern(regexp = "CHAT|QUESTION|INFO") String boardCategory) {}

  public record Equipment(
      @NotBlank @Size(max = 30) String type,
      @NotBlank @Size(max = 100) String maker,
      @NotBlank @Size(max = 150) String model,
      @Size(max = 1000) String description) {}

  public record Officer(
      @NotNull @Positive Long memberId,
      @NotBlank @Size(max = 50) String position,
      @NotNull @Size(max = 500) String bio,
      @NotNull @Size(max = 2048) String photo,
      @Min(0) @Max(999) int sortOrder) {}

  public record Page(
      @NotBlank @Size(max = 200) String title, @NotNull @Size(max = 20000) String content) {}
}
