package modulemanagement.ls1.dtos.ai;


import com.fasterxml.jackson.annotation.JsonProperty;
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

