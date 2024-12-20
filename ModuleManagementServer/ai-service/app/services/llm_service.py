from langchain_openai import ChatOpenAI
from ..settings import settings

class LLMService:
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