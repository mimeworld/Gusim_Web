package club.gusim.controller;

import club.gusim.dto.Responses;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Responses.Error> status(ResponseStatusException e) {
    return ResponseEntity.status(e.getStatusCode())
        .body(
            new Responses.Error(
                "HTTP_" + e.getStatusCode().value(),
                e.getReason() == null ? "요청을 처리할 수 없습니다." : e.getReason()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Responses.Error> validation(MethodArgumentNotValidException e) {
    return ResponseEntity.badRequest()
        .body(new Responses.Error("VALIDATION_ERROR", "입력 형식과 필수 항목을 확인해주세요."));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Responses.Error> duplicate(DataIntegrityViolationException e) {
    return ResponseEntity.status(409).body(new Responses.Error("CONFLICT", "중복되거나 참조 중인 데이터입니다."));
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<Responses.Error> size(MaxUploadSizeExceededException e) {
    return ResponseEntity.status(413)
        .body(new Responses.Error("FILE_TOO_LARGE", "사진은 50MB 이하로 업로드해주세요."));
  }
}
