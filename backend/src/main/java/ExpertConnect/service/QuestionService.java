package ExpertConnect.service;

import ExpertConnect.dto.CreateQuestionRequest;
import ExpertConnect.dto.QuestionResponse;
import ExpertConnect.dto.UpdateQuestionRequest;
import ExpertConnect.entity.Category;
import ExpertConnect.entity.Question;
import ExpertConnect.entity.User;
import ExpertConnect.exception.ForbiddenOperationException;
import ExpertConnect.exception.ResourceNotFoundException;
import ExpertConnect.repository.QuestionRepository;
import ExpertConnect.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    public QuestionService(QuestionRepository questionRepository, UserRepository userRepository) {
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    public QuestionResponse create(CreateQuestionRequest request, Long askerId) {
        User asker = userRepository.findById(askerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", askerId));

        Question question = new Question();
        question.setAsker(asker);
        question.setTitle(request.title().trim());
        question.setDescription(request.description().trim());
        question.setCategory(request.category());

        Question saved = questionRepository.save(question);
        return toResponse(saved);
    }

    public List<QuestionResponse> findAll() {
        return questionRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public QuestionResponse findById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public List<QuestionResponse> findByCategory(Category category) {
        return questionRepository.findByCategory(category).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<QuestionResponse> findByUser(Long userId) {
        return questionRepository.findByAskerId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public QuestionResponse update(Long id, UpdateQuestionRequest request, Long requestingUserId) {
        Question question = findOrThrow(id);
        checkOwnership(question, requestingUserId);

        question.setTitle(request.title().trim());
        question.setDescription(request.description().trim());
        question.setCategory(request.category());

        Question saved = questionRepository.save(question);
        return toResponse(saved);
    }

    public void delete(Long id, Long requestingUserId) {
        Question question = findOrThrow(id);
        checkOwnership(question, requestingUserId);

        questionRepository.delete(question);
    }

    private Question findOrThrow(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", id));
    }

    // Temporary ownership check until Part 4 replaces requestingUserId with the
    // authenticated user from Spring Security. Keep this strict: the caller must
    // be the user who asked the question.
    private void checkOwnership(Question question, Long requestingUserId) {
        if (requestingUserId == null || !requestingUserId.equals(question.getAsker().getId())) {
            throw new ForbiddenOperationException("Only the owner of this question can modify it");
        }
    }

    private QuestionResponse toResponse(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getAsker().getId(),
                question.getAsker().getName(),
                question.getTitle(),
                question.getDescription(),
                question.getCategory(),
                question.getCreatedAt(),
                question.getUpdatedAt()
        );
    }
}
