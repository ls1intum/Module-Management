package modulemanagement.ls1.services;

import modulemanagement.ls1.dtos.ReviewerPreSubmissionGuidelineDto;
import modulemanagement.ls1.dtos.ReviewerPreSubmissionGuidelineWriteDto;
import modulemanagement.ls1.enums.UserRole;
import modulemanagement.ls1.models.ReviewerPreSubmissionGuideline;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.repositories.ReviewerPreSubmissionGuidelineRepository;
import modulemanagement.ls1.shared.ModuleVersionGuidelineFieldKeys;
import modulemanagement.ls1.shared.ReviewerRoles;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class ReviewerPreSubmissionGuidelineService {

    private final ReviewerPreSubmissionGuidelineRepository guidelineRepository;

    public ReviewerPreSubmissionGuidelineService(ReviewerPreSubmissionGuidelineRepository guidelineRepository) {
        this.guidelineRepository = guidelineRepository;
    }

    @Transactional(readOnly = true)
    public List<ReviewerPreSubmissionGuidelineDto> listForUser(User user) {
        List<ReviewerPreSubmissionGuideline> rows = guidelineRepository.findByAuthor_UserId(user.getUserId());
        rows.sort(Comparator
                .comparing(ReviewerPreSubmissionGuideline::getReviewerRole)
                .thenComparingInt(ReviewerPreSubmissionGuideline::getSortOrder)
                .thenComparing(ReviewerPreSubmissionGuideline::getTitle, String.CASE_INSENSITIVE_ORDER));
        return rows.stream().map(ReviewerPreSubmissionGuidelineDto::from).toList();
    }

    @Transactional
    public ReviewerPreSubmissionGuidelineDto create(User user, ReviewerPreSubmissionGuidelineWriteDto body) {
        assertCanWriteForRole(user, body.getReviewerRole());
        LocalDateTime now = LocalDateTime.now();
        ReviewerPreSubmissionGuideline g = new ReviewerPreSubmissionGuideline();
        copyWriteDtoToEntity(body, g);
        g.setSortOrder(body.getSortOrder() != null ? body.getSortOrder() : 0);
        g.setActive(body.getActive() == null || body.getActive());
        g.setAuthor(user);
        g.setCreatedAt(now);
        g.setUpdatedAt(now);
        return ReviewerPreSubmissionGuidelineDto.from(guidelineRepository.save(g));
    }

    @Transactional
    public ReviewerPreSubmissionGuidelineDto update(User user, long guidelineId,
            ReviewerPreSubmissionGuidelineWriteDto body) {
        ReviewerPreSubmissionGuideline g = guidelineRepository.findById(guidelineId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Guideline not found"));
        assertCanModifyExisting(user, g);
        assertCanWriteForRole(user, body.getReviewerRole());
        copyWriteDtoToEntity(body, g);
        if (body.getSortOrder() != null) {
            g.setSortOrder(body.getSortOrder());
        }
        if (body.getActive() != null) {
            g.setActive(body.getActive());
        }
        g.setUpdatedAt(LocalDateTime.now());
        return ReviewerPreSubmissionGuidelineDto.from(guidelineRepository.save(g));
    }

    @Transactional
    public void delete(User user, long guidelineId) {
        ReviewerPreSubmissionGuideline g = guidelineRepository.findById(guidelineId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Guideline not found"));
        assertCanModifyExisting(user, g);
        guidelineRepository.delete(g);
    }

    private void assertCanWriteForRole(User user, UserRole reviewerRole) {
        if (!ReviewerRoles.isReviewerRole(reviewerRole)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reviewer role for guidelines.");
        }
        if (ReviewerRoles.userIsAdmin(user.getRoles())) {
            return;
        }
        if (user.getRoles() == null || !user.getRoles().contains(reviewerRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot create guidelines for this role.");
        }
    }

    private void assertCanModifyExisting(User user, ReviewerPreSubmissionGuideline g) {
        if (g.getAuthor() == null || !g.getAuthor().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this guideline.");
        }
    }

    private static void copyWriteDtoToEntity(ReviewerPreSubmissionGuidelineWriteDto body,
            ReviewerPreSubmissionGuideline g) {
        g.setReviewerRole(body.getReviewerRole());
        g.setTitle(body.getTitle().trim());
        g.setContent(body.getContent() == null ? "" : body.getContent());
        g.setGoodExample(StringUtils.hasText(body.getGoodExample()) ? body.getGoodExample() : null);
        g.setBadExample(StringUtils.hasText(body.getBadExample()) ? body.getBadExample() : null);
        g.setRelatedModuleFieldKey(normalizeRelatedModuleFieldKey(body.getRelatedModuleFieldKey()));
    }

    /**
     * Blank → null; otherwise trimmed key must be allowed by
     * {@link ModuleVersionGuidelineFieldKeys}.
     */
    private static String normalizeRelatedModuleFieldKey(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }
        String t = key.trim();
        if (!ModuleVersionGuidelineFieldKeys.isAllowed(t)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid related module field key.");
        }
        return t;
    }
}
