from app.models.moduleModels import ModuleInfo
from pydantic import BaseModel

class GenerationModuleInfo(ModuleInfo):
    bulletPoints: str

class ContentGenerationResponse(BaseModel):
    responseData: str

class ExaminationAchievementsGenerationResponse(BaseModel):
    responseData: str

class LearningOutcomesGenerationResponse(BaseModel):
    responseData: str

class TeachingMethodsGenerationResponse(BaseModel):
    responseData: str

class MediaGenerationResponse(BaseModel):
    responseData: str