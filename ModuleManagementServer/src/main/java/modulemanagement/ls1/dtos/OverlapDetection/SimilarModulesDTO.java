package modulemanagement.ls1.dtos.OverlapDetection;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimilarModulesDTO {
    public record SimilarModule(
            String moduleId,
            String title,
            double similarity
    ) {}

    private List<SimilarModule> similarModules = new ArrayList<>();
}

