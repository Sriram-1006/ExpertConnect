package ExpertConnect.controller;

import ExpertConnect.dto.ExpertApplicationRequest;
import ExpertConnect.dto.ExpertApplicationResponse;
import ExpertConnect.entity.VerificationStatus;
import ExpertConnect.security.AuthUser;
import ExpertConnect.service.ExpertService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/experts")
public class ExpertController {

    private final ExpertService expertService;

    public ExpertController(ExpertService expertService) {
        this.expertService = expertService;
    }

    @PostMapping("/apply")
    public ResponseEntity<ExpertApplicationResponse> apply(
            @Valid @RequestBody ExpertApplicationRequest request,
            @AuthenticationPrincipal AuthUser authUser) {
        ExpertApplicationResponse created = expertService.apply(request, authUser.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/me")
    public List<ExpertApplicationResponse> getMyApplications(
            @AuthenticationPrincipal AuthUser authUser) {
        return expertService.findByUser(authUser.id());
    }

    @GetMapping
    public List<ExpertApplicationResponse> getAll() {
        return expertService.findAll();
    }

    @GetMapping("/{id}")
    public ExpertApplicationResponse getById(@PathVariable Long id) {
        return expertService.findById(id);
    }

    @GetMapping("/status/{status}")
    public List<ExpertApplicationResponse> getByStatus(@PathVariable VerificationStatus status) {
        return expertService.findByStatus(status);
    }

    @PutMapping("/{id}/approve")
    public ExpertApplicationResponse approve(@PathVariable Long id,
                                             @AuthenticationPrincipal AuthUser authUser) {
        return expertService.approve(id, authUser.id());
    }

    @PutMapping("/{id}/reject")
    public ExpertApplicationResponse reject(@PathVariable Long id,
                                            @AuthenticationPrincipal AuthUser authUser) {
        return expertService.reject(id, authUser.id());
    }
}
