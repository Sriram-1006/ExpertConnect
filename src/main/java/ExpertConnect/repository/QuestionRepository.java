package ExpertConnect.repository;

import ExpertConnect.entity.Category;
import ExpertConnect.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByCategory(Category category);

    List<Question> findByAskerId(Long askerId);
}
