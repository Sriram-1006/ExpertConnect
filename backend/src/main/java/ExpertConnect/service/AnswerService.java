package ExpertConnect.service;

import ExpertConnect.dto.AnswerRequest;
import ExpertConnect.dto.AnswerResponse;
import ExpertConnect.entity.Answer;
import ExpertConnect.entity.Question;
import ExpertConnect.entity.Role;
import ExpertConnect.entity.User;
import ExpertConnect.entity.VerificationStatus;
import ExpertConnect.exception.ForbiddenOperationException;
import ExpertConnect.exception.ResourceNotFoundException;
import ExpertConnect.repository.AnswerRepository;
import ExpertConnect.repository.ExpertApplicationRepository;
import ExpertConnect.repository.QuestionRepository;
import ExpertConnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final ExpertApplicationRepository expertApplicationRepository;

    public AnswerService(AnswerRepository answerRepository,
                         QuestionRepository questionRepository,
                         UserRepository userRepository,
                         ExpertApplicationRepository expertApplicationRepository) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.expertApplicationRepository = expertApplicationRepository;
    }

    @Transactional
    public AnswerResponse create(Long questionId, AnswerRequest request, Long userId) {
        Question question = findQuestionOrThrow(questionId);
        User expert = findUserOrThrow(userId);

        requireVerifiedExpert(expert);

        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setExpert(expert);
        answer.setContent(request.content().trim());

        return toResponse(answerRepository.save(answer));
    }

    @Transactional(readOnly = true)
    public List<AnswerResponse> findByQuestion(Long questionId) {
        // Ensure the question exists so an unknown id returns 404 rather than [].
        findQuestionOrThrow(questionId);
        return answerRepository.findByQuestionIdOrderByCreatedAtAsc(questionId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AnswerResponse update(Long answerId, AnswerRequest request, Long userId) {
        Answer answer = findAnswerOrThrow(answerId);
        requireOwnership(answer, userId);

        answer.setContent(request.content().trim());
        return toResponse(answerRepository.save(answer));
    }

    @Transactional
    public void delete(Long answerId, Long userId) {
        Answer answer = findAnswerOrThrow(answerId);
        requireOwnership(answer, userId);

        answerRepository.delete(answer);
    }

    @Transactional
    public AnswerResponse accept(Long questionId, Long answerId, Long userId) {
        Question question = findQuestionOrThrow(questionId);

        if (!question.getAsker().getId().equals(userId)) {
            throw new ForbiddenOperationException("Only the owner of this question can accept an answer");
        }

        // The answer must actually belong to this question.
        Answer answer = answerRepository.findByIdAndQuestionId(answerId, questionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Answer", "id", answerId));

        // Only one answer may be accepted: unaccept any previous one first.
        answerRepository.findFirstByQuestionIdAndAcceptedTrue(questionId)
                .filter(previous -> !previous.getId().equals(answer.getId()))
                .ifPresent(previous -> {
                    previous.setAccepted(false);
                    answerRepository.save(previous);
                });

        answer.setAccepted(true);
        return toResponse(answerRepository.save(answer));
    }

    private void requireVerifiedExpert(User user) {
        boolean verified = user.getRole() == Role.EXPERT
                && expertApplicationRepository.existsByUserIdAndVerificationStatus(
                        user.getId(), VerificationStatus.VERIFIED);
        if (!verified) {
            throw new ForbiddenOperationException("Only verified experts can answer questions");
        }
    }

    private void requireOwnership(Answer answer, Long userId) {
        if (userId == null || !userId.equals(answer.getExpert().getId())) {
            throw new ForbiddenOperationException("Only the author of this answer can modify it");
        }
    }

    private Question findQuestionOrThrow(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", questionId));
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private Answer findAnswerOrThrow(Long answerId) {
        return answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer", "id", answerId));
    }

    private AnswerResponse toResponse(Answer answer) {
        return new AnswerResponse(
                answer.getId(),
                answer.getQuestion().getId(),
                answer.getExpert().getId(),
                answer.getExpert().getName(),
                answer.getContent(),
                answer.isAccepted(),
                answer.getCreatedAt(),
                answer.getUpdatedAt()
        );
    }
}
