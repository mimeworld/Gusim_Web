package club.gusim.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UploadService {
  private final Path root;

  public UploadService(@Value("${app.upload-dir}") String directory) {
    root = Path.of(directory).toAbsolutePath();
  }

  public void validateUrl(String url) {
    if (url == null || url.isBlank()) return;
    if (!url.matches("/uploads/[a-f0-9-]+\\.jpg")
        || !Files.isRegularFile(root.resolve(url.substring("/uploads/".length()))))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "먼저 사진 파일을 업로드해주세요.");
  }

  public String store(MultipartFile file) {
    if (file.isEmpty() || file.getSize() > 50 * 1024 * 1024)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "50MB 이하 사진을 선택해주세요.");
    try {
      BufferedImage image;
      try (var stream = ImageIO.createImageInputStream(file.getInputStream())) {
        var readers = ImageIO.getImageReaders(stream);
        if (!readers.hasNext())
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JPEG 또는 PNG 사진을 선택해주세요.");
        var reader = readers.next();
        try {
          reader.setInput(stream);
          String format = reader.getFormatName();
          if (!(format.equalsIgnoreCase("JPEG") || format.equalsIgnoreCase("PNG")))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JPEG 또는 PNG 사진을 선택해주세요.");
          image = reader.read(0);
        } finally {
          reader.dispose();
        }
      }
      BufferedImage clean =
          new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
      var graphics = clean.createGraphics();
      graphics.setColor(java.awt.Color.WHITE);
      graphics.fillRect(0, 0, clean.getWidth(), clean.getHeight());
      graphics.drawImage(image, 0, 0, null);
      graphics.dispose();
      Files.createDirectories(root);
      String key = UUID.randomUUID() + ".jpg";
      ImageIO.write(clean, "jpg", root.resolve(key).toFile());
      Files.writeString(root.resolve(key + ".size"), Long.toString(file.getSize()));
      return "/uploads/" + key;
    } catch (IOException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일을 읽거나 저장할 수 없습니다.");
    }
  }

  public void validateAttachments(String category, List<String> images) {
    validateCount(category, images.size());
    if (new HashSet<>(images).size() != images.size())
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "같은 사진을 중복 첨부할 수 없습니다.");
    long total = 0;
    for (String url : images) {
      validateUrl(url);
      total += originalSize(url);
      if (total > 100L * 1024 * 1024)
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "첨부 사진의 합계는 100MB 이하여야 합니다.");
    }
  }

  private void validateCount(String category, int count) {
    if (!Set.of("PHOTO", "CRITIQUE", "BOARD").contains(category))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 게시판입니다.");
    if (category.equals("PHOTO") && count != 1)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사진 공유에는 사진 한 장을 첨부해주세요.");
    if (category.equals("CRITIQUE") && (count < 1 || count > 2))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "크리틱에는 사진을 1~2장 첨부해주세요.");
  }

  public long sizeForView(String url) {
    try {
      return originalSize(url);
    } catch (ResponseStatusException exception) {
      return 0L;
    }
  }

  private long originalSize(String url) {
    Path image = root.resolve(url.substring("/uploads/".length()));
    try {
      Path metadata = image.resolveSibling(image.getFileName() + ".size");
      // Historical files have no original-size metadata; use their stored bytes.
      return Files.exists(metadata)
          ? Long.parseLong(Files.readString(metadata))
          : Files.size(image);
    } catch (IOException | NumberFormatException exception) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "사진의 용량 정보를 읽을 수 없습니다. 다시 업로드해주세요.");
    }
  }

  public List<String> storeBatch(String category, List<MultipartFile> files) {
    validateCount(category, files.size());
    long total = 0;
    for (MultipartFile file : files) {
      if (file.isEmpty() || file.getSize() > 50L * 1024 * 1024)
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사진 한 장은 50MB 이하여야 합니다.");
      total += file.getSize();
      if (total > 100L * 1024 * 1024)
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "첨부 사진의 합계는 100MB 이하여야 합니다.");
    }
    List<String> urls = new ArrayList<>();
    try {
      for (MultipartFile file : files) urls.add(store(file));
      return urls;
    } catch (RuntimeException exception) {
      for (String url : urls) {
        try {
          Path image = root.resolve(url.substring("/uploads/".length()));
          Files.deleteIfExists(image);
          Files.deleteIfExists(image.resolveSibling(image.getFileName() + ".size"));
        } catch (IOException cleanup) {
          exception.addSuppressed(cleanup);
        }
      }
      throw exception;
    }
  }
}
