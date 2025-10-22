from langchain_openai import AzureChatOpenAI
from langchain.chat_models.base import BaseChatModel
from app.settings import settings

class LLMService:
    model: BaseChatModel

    def __init__(self):
        self.model = AzureChatOpenAI(
            temperature=0.7,
            api_key=settings.AZURE_API_KEY,
            azure_endpoint=settings.AZURE_ENDPOINT,
            azure_deployment=settings.AZURE_DEPLOYMENT_NAME,
            api_version=settings.AZURE_API_VERSION,
        )

    async def generate_content(self, prompt: str) -> str:
        response = await self.model.ainvoke(prompt)
        return response.content

llm_service = LLMService()
