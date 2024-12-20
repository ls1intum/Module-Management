package modulemanagement.ls1.dtos.ai;

import lombok.Data;

@Data
public class CompletionServiceResponseDTO {
    private String responseData;

    public CompletionServiceResponseDTO(String data) {
        this.responseData = data;
    }
}
