import logging, httpx, os
import traceback
import time
from langchain_core.messages import AIMessage
from typing import Any, Dict, Optional

from langchain_openai import AzureChatOpenAI
from app.settings import settings

httpx_logger = logging.getLogger("httpx")
httpx_logger.setLevel(logging.DEBUG)

os.environ["LANGCHAIN_DEBUG"] = "true"
os.environ["LANGCHAIN_TRACING_V2"] = "false"  # or true if you use LangSmith
os.environ["OPENAI_LOG"] = "debug"            # SDK debug logs

logger = logging.getLogger(__name__)

class LLMService:
    model: AzureChatOpenAI

    def __init__(self):
        logger.info("Init AzureChatOpenAI with model %s and endpoint %s ", settings.AZURE_DEPLOYMENT_NAME, settings.AZURE_ENDPOINT)
        self.model = AzureChatOpenAI(
            api_key = settings.AZURE_API_KEY,
            azure_endpoint = settings.AZURE_ENDPOINT,
            azure_deployment = settings.AZURE_DEPLOYMENT_NAME,
            api_version = settings.AZURE_API_VERSION,
            reasoning = {"effort": "minimal"},
            use_responses_api = True,
        )

    async def generate_content(self, prompt: str) -> str:
        logger.debug("Generating content from LLM for prompt: %s", prompt)
        try:
            start_time = time.perf_counter()
            resp: AIMessage = await self.model.ainvoke(prompt)
            
            text = as_text(resp)
            usage = self._extract_usage(resp)
            duration = time.perf_counter() - start_time
            # compute + log cost
            cost_usd = self._estimate_cost_usd(
                usage.get("model"),
                usage.get("input_tokens"),
                usage.get("output_tokens"),
            )

            logger.info(
                "LLM usage | model=%s | input=%s | output=%s | total=%s | duration=%.2fs | est_cost=$%.6f",
                usage.get("model"),
                usage.get("input_tokens"),
                usage.get("output_tokens"),
                usage.get("total_tokens"),
                duration,
                cost_usd,
            )
            
            # Log safe introspection
            logger.info("LLM Answer: %s", text)
            if isinstance(resp, AIMessage):
                logger.debug("AIMessage.content: %r", text)
                logger.debug("AIMessage: %s", resp)
                logger.info("AIMessage.response_metadata: %s", resp.response_metadata)
                logger.debug("AIMessage.tool_calls: %s", getattr(resp, "tool_calls", None))
                logger.debug("AIMessage.additional_kwargs keys: %s", list(resp.additional_kwargs.keys()))
            else:
                logger.info("Raw response repr: %r", resp)

            return text
        except Exception as e:
            logger.error("LLM call failed: %s\n%s", e, traceback.format_exc())
            raise
    
    @staticmethod
    def _extract_usage(resp) -> Dict[str, Any]:
        """
        Prefer LangChain's parsed usage on AIMessage.usage_metadata.
        Fall back to response_metadata. Normalize keys.
        """
        usage = getattr(resp, "usage_metadata", None) or {}
        if not usage:
            meta = getattr(resp, "response_metadata", {}) or {}
            usage = meta.get("usage") or meta.get("token_usage") or {}

        in_tok  = usage.get("input_tokens")  or usage.get("prompt_tokens")
        out_tok = usage.get("output_tokens") or usage.get("completion_tokens")
        total   = usage.get("total_tokens")
        if total is None and (in_tok is not None or out_tok is not None):
            total = (in_tok or 0) + (out_tok or 0)

        # capture model from either usage or response metadata
        meta = getattr(resp, "response_metadata", {}) or {}
        model = (
            usage.get("model")
            or usage.get("model_name")
            or meta.get("model")
            or meta.get("model_name")
        )

        return {
            "input_tokens": in_tok,
            "output_tokens": out_tok,
            "total_tokens": total,
            "model": model,
            "input_token_details": usage.get("input_token_details"),
            "output_token_details": usage.get("output_token_details"),
        }
    
    @staticmethod
    def _estimate_cost_usd(model_name: Optional[str], input_tokens: Optional[int], output_tokens: Optional[int]) -> float:
        """
        Hard-coded prices (USD per 1M tokens):
          - GPT-5:      in $1.250, out $10.000
          - GPT-5 mini: in $0.250, out $2.000
          - GPT-5 nano: in $0.050, out $0.400
        Unknown models → $0.
        """
        itok = input_tokens or 0
        otok = output_tokens or 0

        if not model_name:
            return 0.0

        m = model_name.lower().strip()
        # map common variants
        if "gpt-5-nano" in m:
            in_per_tok = 0.050 / 1_000_000
            out_per_tok = 0.400 / 1_000_000
        elif "gpt-5-mini" in m:
            in_per_tok = 0.250 / 1_000_000
            out_per_tok = 2.000 / 1_000_000
        elif m.startswith("gpt-5"):
            in_per_tok = 1.250 / 1_000_000
            out_per_tok = 10.000 / 1_000_000
        else:
            in_per_tok = out_per_tok = 0.0

        return itok * in_per_tok + otok * out_per_tok

def as_text(ai_msg: AIMessage) -> str:
    c = getattr(ai_msg, "content", "")
    if isinstance(c, str):
        return c
    if isinstance(c, list):
        # Concatenate only text blocks; ignore 'reasoning' etc.
        return "".join(part.get("text", "") for part in c if part.get("type") == "text")
    return str(c)

llm_service = LLMService()
