package modulemanagement.ls1.services;

import modulemanagement.ls1.dtos.CreateSchoolDTO;
import modulemanagement.ls1.dtos.PageResponseDTO;
import modulemanagement.ls1.models.School;
import modulemanagement.ls1.repositories.SchoolRepository;
import modulemanagement.ls1.shared.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SchoolService {

    private final SchoolRepository schoolRepository;

    public SchoolService(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
    }

    public PageResponseDTO<School> getSchoolsPage(Pageable pageable, String search) {
        Page<School> page = search != null && !search.isBlank()
                ? schoolRepository.findBySearch(search.trim(), pageable)
                : schoolRepository.findAllNonDeleted(pageable);
        return PageResponseDTO.from(page);
    }

    public School create(CreateSchoolDTO dto) {
        School school = new School();
        school.setName(dto.getName().trim());
        school.setCreatedAt(LocalDateTime.now());
        return schoolRepository.save(school);
    }

    public void delete(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found: " + schoolId));
        school.setDeletedAt(LocalDateTime.now());
        schoolRepository.save(school);
    }
}
