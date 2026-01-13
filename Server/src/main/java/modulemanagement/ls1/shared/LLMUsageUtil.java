package modulemanagement.ls1.shared;

import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;

@Slf4j
public class LLMUsageUtil {

    public static void extractAndLogUsage(ChatResponse chatResponse, long duration) {
        UsageInfo usage = extractUsage(chatResponse);
        double costUsd = estimateCostUsd(usage.model(), usage.inputTokens(), usage.outputTokens());

        log.info("LLM usage | model={} | input={} | output={} | total={} | duration={}ms | est_cost=${}",
                usage.model(),
                usage.inputTokens(),
                usage.outputTokens(),
                usage.totalTokens(),
                duration,
                String.format("%.6f", costUsd));
    }

    public static UsageInfo extractUsage(ChatResponse chatResponse) {
        try {
            var metadata = chatResponse.getMetadata();
            String model = metadata.getModel();
            Usage usage = metadata.getUsage();
            Integer inputTokens = usage.getPromptTokens();
            Integer outputTokens = usage.getCompletionTokens();
            Integer totalTokens = usage.getTotalTokens();

            return new UsageInfo(model, inputTokens, outputTokens, totalTokens);
        } catch (Exception e) {
            log.debug("Could not extract usage information: {}", e.getMessage());
            return new UsageInfo(null, null, null, null);
        }
    }

    public static double estimateCostUsd(String modelName, Integer inputTokens, Integer outputTokens) {
        if (modelName == null || modelName.isBlank()) {
            return 0.0;
        }

        PricingRates rates = getPricingRates(modelName.toLowerCase().trim());
        int inTok = inputTokens != null ? inputTokens : 0;
        int outTok = outputTokens != null ? outputTokens : 0;

        return (inTok * rates.inputPerToken()) + (outTok * rates.outputPerToken());
    }

    private static PricingRates getPricingRates(String model) {
        if (model.contains("gpt-5-nano")) {
            return PricingRates.perMillion(0.050, 0.400);
        } else if (model.contains("gpt-5-mini")) {
            return PricingRates.perMillion(0.250, 2.000);
        } else if (model.startsWith("gpt-5")) {
            return PricingRates.perMillion(1.250, 10.000);
        } else if (model.contains("gpt-4")) {
            return PricingRates.perMillion(30.0, 60.0);
        } else if (model.contains("gpt-3.5")) {
            return PricingRates.perMillion(0.5, 1.5);
        } else {
            return PricingRates.perMillion(0.0, 0.0); // Unknown or local LLM
        }
    }

    public record UsageInfo(
            String model,
            Integer inputTokens,
            Integer outputTokens,
            Integer totalTokens) {
    }

    private record PricingRates(double inputPerToken, double outputPerToken) {
        static PricingRates perMillion(double input, double output) {
            return new PricingRates(input / 1_000_000, output / 1_000_000);
        }
    }
}
