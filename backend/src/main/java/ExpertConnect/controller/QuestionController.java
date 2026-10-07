package ExpertConnect.controller;

import ExpertConnect.dto.CreateQuestionRequest;
import ExpertConnect.dto.QuestionResponse;
import ExpertConnect.dto.UpdateQuestionRequest;
import ExpertConnect.entity.Category;
import ExpertConnect.security.AuthUser;
import ExpertConnect.service.QuestionService;
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
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> createQuestion(
            @Valid @RequestBody CreateQuestionRequest request,
            @AuthenticationPrincipal AuthUser authUser) {
        QuestionResponse created = questionService.create(request, authUser.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<QuestionResponse> getAllQuestions() {
        return questionService.findAll();
    }

    @GetMapping("/{id}")
    public QuestionResponse getQuestionById(@PathVariable Long id) {
        return questionService.findById(id);
    }

    @GetMapping("/category/{category}")
    public List<QuestionResponse> getQuestionsByCategory(@PathVariable Category category) {
        return questionService.findByCategory(category);
    }

    @GetMapping("/user/{userId}")
    public List<QuestionResponse> getQuestionsByUser(@PathVariable Long userId) {
        return questionService.findByUser(userId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody UpdateQuestionRequest request,
            @AuthenticationPrincipal AuthUser authUser) {
        QuestionResponse updated = questionService.update(id, request, authUser.id());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthUser authUser) {
        questionService.delete(id, authUser.id());
        return ResponseEntity.noContent().build();
    }
}
