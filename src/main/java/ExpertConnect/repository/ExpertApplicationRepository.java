package ExpertConnect.repository;

import ExpertConnect.entity.ExpertApplication;
import ExpertConnect.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpertApplicationRepository extends JpaRepository<ExpertApplication, Long> {

    List<ExpertApplication> findByVerificationStatus(VerificationStatus verificationStatus);

    List<ExpertApplication> findByUserId(Long userId);

    Optional<ExpertApplication> findFirstByUserIdAndVerificationStatus(Long userId, VerificationStatus verificationStatus);

    boolean existsByUserIdAndVerificationStatus(Long userId, VerificationStatus verificationStatus);
}
