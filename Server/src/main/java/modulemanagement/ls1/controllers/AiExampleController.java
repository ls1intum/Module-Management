package modulemanagement.ls1.controllers;

import modulemanagement.ls1.dtos.PromptRequestDTO;
import modulemanagement.ls1.dtos.PromptResponseDTO;
import modulemanagement.ls1.services.LLMGenerationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-example")
public class AiExampleController {

    private final LLMGenerationService llmGenerationService;

    public AiExampleController(LLMGenerationService llmGenerationService) {
        this.llmGenerationService = llmGenerationService;
    }

    @PostMapping("/prompt")
    public ResponseEntity<PromptResponseDTO> generateResponse(
            @Valid @RequestBody PromptRequestDTO request) {

        String response = llmGenerationService.generate(request.getPrompt(), "Test");

        return ResponseEntity.ok(new PromptResponseDTO(response));
    }
}
