from enum import Enum
from typing import Optional
from pydantic import BaseModel


class Language(str, Enum):
    ENGLISH = "English"
    GERMAN = "German"


class ModuleInfo(BaseModel):
    bullet_points: str
    title_eng: Optional[str] = None
    level_eng: Optional[str] = None
    language_eng: Optional[Language] = None
    frequency_eng: Optional[str] = None
    credits: Optional[int] = None
    hours_total: Optional[int] = None
    hours_self_study: Optional[int] = None
    hours_presence: Optional[int] = None
    examination_achievements_eng: Optional[str] = None
    repetition_eng: Optional[str] = None
    recommended_prerequisites_eng: Optional[str] = None
    content_eng: Optional[str] = None
    learning_outcomes_eng: Optional[str] = None
    teaching_methods_eng: Optional[str] = None
    media_eng: Optional[str] = None
    literature_eng: Optional[str] = None
    responsibles_eng: Optional[str] = None
    lv_sws_lecturer_eng: Optional[str] = None


class ContentGenerationResponse(BaseModel):
    content: str


class ExaminationAchievementsGenerationResponse(BaseModel):
    examination_achievement: str

class LearningOutcomesGenerationResponse(BaseModel):
    learning_outcomes: str

class TeachingMethodsGenerationResponse(BaseModel):
    teaching_methods: str

class MediaGenerationResponse(BaseModel):
    media: str