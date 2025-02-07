from langchain_openai import ChatOpenAI
from langchain.chat_models.base import BaseChatModel
from app.settings import settings

class LLMService:
    model: BaseChatModel

    def __init__(self):
        self.model = ChatOpenAI(
            temperature=0.7,
            model="gpt-4o-mini",
            api_key=settings.OPENAI_API_KEY
        )

    async def generate_content(self, prompt: str) -> str:
        response = await self.model.ainvoke(prompt)
        return response.content

llm_service = LLMService()