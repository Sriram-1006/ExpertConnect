package ExpertConnect.service;

import ExpertConnect.dto.ExpertApplicationRequest;
import ExpertConnect.dto.ExpertApplicationResponse;
import ExpertConnect.entity.ExpertApplication;
import ExpertConnect.entity.Role;
import ExpertConnect.entity.User;
import ExpertConnect.entity.VerificationStatus;
import ExpertConnect.exception.ForbiddenOperationException;
import ExpertConnect.exception.ResourceNotFoundException;
import ExpertConnect.repository.ExpertApplicationRepository;
import ExpertConnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExpertService {

    private final ExpertApplicationRepository expertApplicationRepository;
    private final UserRepository userRepository;

    public ExpertService(ExpertApplicationRepository expertApplicationRepository,
                         UserRepository userRepository) {
        this.expertApplicationRepository = expertApplicationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ExpertApplicationResponse apply(ExpertApplicationRequest request, Long userId) {
        User user = findUserOrThrow(userId);

        ExpertApplication application = new ExpertApplication();
        application.setUser(user);
        application.setExpertise(request.expertise());
        application.setBio(request.bio().trim());
        application.setExperience(request.experience());
        application.setVerificationStatus(VerificationStatus.PENDING);

        ExpertApplication saved = expertApplicationRepository.save(application);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ExpertApplicationResponse findById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ExpertApplicationResponse> findAll() {
        return expertApplicationRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExpertApplicationResponse> findByStatus(VerificationStatus status) {
        return expertApplicationRepository.findByVerificationStatus(status).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExpertApplicationResponse> findByUser(Long userId) {
        return expertApplicationRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ExpertApplicationResponse approve(Long id, Long requestingUserId) {
        requireAdmin(requestingUserId);
        ExpertApplication application = findOrThrow(id);

        application.setVerificationStatus(VerificationStatus.VERIFIED);
        User user = application.getUser();
        user.setRole(Role.EXPERT);
        userRepository.save(user);

        return toResponse(expertApplicationRepository.save(application));
    }

    @Transactional
    public ExpertApplicationResponse reject(Long id, Long requestingUserId) {
        requireAdmin(requestingUserId);
        ExpertApplication application = findOrThrow(id);

        application.setVerificationStatus(VerificationStatus.REJECTED);
        return toResponse(expertApplicationRepository.save(application));
    }

    private void requireAdmin(Long userId) {
        User user = findUserOrThrow(userId);
        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenOperationException("Admin privileges are required for this operation");
        }
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private ExpertApplication findOrThrow(Long id) {
        return expertApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expert application", "id", id));
    }

    private ExpertApplicationResponse toResponse(ExpertApplication application) {
        return new ExpertApplicationResponse(
                application.getId(),
                application.getUser().getId(),
                application.getUser().getName(),
                application.getExpertise(),
                application.getBio(),
                application.getExperience(),
                application.getVerificationStatus(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }
}
