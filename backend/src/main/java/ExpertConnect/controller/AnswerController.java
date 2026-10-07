package ExpertConnect.controller;

import ExpertConnect.dto.AnswerRequest;
import ExpertConnect.dto.AnswerResponse;
import ExpertConnect.security.AuthUser;
import ExpertConnect.service.AnswerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AnswerController {

    private final AnswerService answerService;

    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @PostMapping("/questions/{questionId}/answers")
    public ResponseEntity<AnswerResponse> createAnswer(
            @PathVariable Long questionId,
            @Valid @RequestBody AnswerRequest request,
            @AuthenticationPrincipal AuthUser authUser) {
        AnswerResponse created = answerService.create(questionId, request, authUser.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/questions/{questionId}/answers")
    public List<AnswerResponse> getAnswersForQuestion(@PathVariable Long questionId) {
        return answerService.findByQuestion(questionId);
    }

    @PutMapping("/answers/{answerId}")
    public AnswerResponse updateAnswer(
            @PathVariable Long answerId,
            @Valid @RequestBody AnswerRequest request,
            @AuthenticationPrincipal AuthUser authUser) {
        return answerService.update(answerId, request, authUser.id());
    }

    @DeleteMapping("/answers/{answerId}")
    public ResponseEntity<Void> deleteAnswer(
            @PathVariable Long answerId,
            @AuthenticationPrincipal AuthUser authUser) {
        answerService.delete(answerId, authUser.id());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/questions/{questionId}/answers/{answerId}/accept")
    public AnswerResponse acceptAnswer(
            @PathVariable Long questionId,
            @PathVariable Long answerId,
            @AuthenticationPrincipal AuthUser authUser) {
        return answerService.accept(questionId, answerId, authUser.id());
    }
}
