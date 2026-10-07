package ExpertConnect.repository;

import ExpertConnect.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByQuestionIdOrderByCreatedAtAsc(Long questionId);

    Optional<Answer> findByIdAndQuestionId(Long id, Long questionId);

    Optional<Answer> findFirstByQuestionIdAndAcceptedTrue(Long questionId);

    List<Answer> findByExpertId(Long expertId);
}
