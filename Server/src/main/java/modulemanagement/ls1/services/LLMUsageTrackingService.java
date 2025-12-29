package modulemanagement.ls1.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LLMUsageTrackingService {

    private static final Logger log = LoggerFactory.getLogger(LLMUsageTrackingService.class);

    public UsageInfo extractUsage(ChatResponse chatResponse) {
        try {
            var metadata = chatResponse.getMetadata();

            String model = metadata.getModel() != null
                    ? metadata.getModel()
                    : "unknown";

            var usage = metadata.getUsage();
            Integer inputTokens = null;
            Integer outputTokens = null;
            Integer totalTokens = null;

            if (usage != null) {
                inputTokens = usage.getPromptTokens();
                outputTokens = usage.getCompletionTokens();
                totalTokens = usage.getTotalTokens();
            }

            // Fallback: calculate total if not provided
            if (totalTokens == null && (inputTokens != null || outputTokens != null)) {
                totalTokens = (inputTokens != null ? inputTokens : 0) +
                        (outputTokens != null ? outputTokens : 0);
            }

            return new UsageInfo(model, inputTokens, outputTokens, totalTokens);
        } catch (Exception e) {
            log.debug("Could not extract usage information: {}", e.getMessage());
            return new UsageInfo(null, null, null, null);
        }
    }

    public double estimateCostUsd(String modelName, Integer inputTokens, Integer outputTokens) {
        if (modelName == null || modelName.isEmpty()) {
            return 0.0;
        }

        int inTok = inputTokens != null ? inputTokens : 0;
        int outTok = outputTokens != null ? outputTokens : 0;

        String model = modelName.toLowerCase().trim();

        // Pricing per 1M tokens (USD)
        double inPerTok;
        double outPerTok;

        if (model.contains("gpt-5-nano")) {
            inPerTok = 0.050 / 1_000_000.0;
            outPerTok = 0.400 / 1_000_000.0;
        } else if (model.contains("gpt-5-mini")) {
            inPerTok = 0.250 / 1_000_000.0;
            outPerTok = 2.000 / 1_000_000.0;
        } else if (model.startsWith("gpt-5")) {
            inPerTok = 1.250 / 1_000_000.0;
            outPerTok = 10.000 / 1_000_000.0;
        } else if (model.contains("gpt-4")) {
            // GPT-4 pricing (approximate)
            inPerTok = 30.0 / 1_000_000.0;
            outPerTok = 60.0 / 1_000_000.0;
        } else if (model.contains("gpt-3.5")) {
            // GPT-3.5 pricing
            inPerTok = 0.5 / 1_000_000.0;
            outPerTok = 1.5 / 1_000_000.0;
        } else {
            // Unknown model or local LLM - no cost
            inPerTok = 0.0;
            outPerTok = 0.0;
        }

        return (inTok * inPerTok) + (outTok * outPerTok);
    }

    public void logUsage(UsageInfo usage, long duration) {
        double costUsd = estimateCostUsd(usage.model, usage.inputTokens, usage.outputTokens);
        log.info("LLM usage | model={} | input={} | output={} | total={} | duration={}ms | est_cost=${}",
                usage.model != null ? usage.model : "unknown",
                usage.inputTokens != null ? usage.inputTokens : "N/A",
                usage.outputTokens != null ? usage.outputTokens : "N/A",
                usage.totalTokens != null ? usage.totalTokens : "N/A",
                duration,
                String.format("%.6f", costUsd));
    }

    public void extractAndLogUsage(ChatResponse chatResponse, long duration) {
        UsageInfo usage = extractUsage(chatResponse);
        logUsage(usage, duration);
    }

    public static class UsageInfo {
        final String model;
        final Integer inputTokens;
        final Integer outputTokens;
        final Integer totalTokens;

        public UsageInfo(String model, Integer inputTokens, Integer outputTokens, Integer totalTokens) {
            this.model = model;
            this.inputTokens = inputTokens;
            this.outputTokens = outputTokens;
            this.totalTokens = totalTokens;
        }
    }
}
