package modulemanagement.ls1.controllers;

import modulemanagement.ls1.dtos.PromptRequestDTO;
import modulemanagement.ls1.dtos.PromptResponseDTO;
import modulemanagement.ls1.services.SpringAiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-example")
public class AiExampleController {

    private final SpringAiService springAiService;

    public AiExampleController(SpringAiService springAiService) {
        this.springAiService = springAiService;
    }

    @PostMapping("/prompt")
    public ResponseEntity<PromptResponseDTO> generateResponse(
            @Valid @RequestBody PromptRequestDTO request) {

        String response = springAiService.generateResponse(request.getPrompt());

        return ResponseEntity.ok(new PromptResponseDTO(response));
    }
}
