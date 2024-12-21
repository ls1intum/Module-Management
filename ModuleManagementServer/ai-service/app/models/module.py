from enum import Enum
from typing import Optional
from pydantic import BaseModel


class Language(str, Enum):
    ENGLISH = "English"
    GERMAN = "German"


class ModuleInfo(BaseModel):
    bulletPoints: str
    titleEng: Optional[str] = None
    levelEng: Optional[str] = None
    languageEng: Optional[Language] = None
    frequencyEng: Optional[str] = None
    credits: Optional[int] = None
    hoursTotal: Optional[int] = None
    hoursSelfStudy: Optional[int] = None
    hoursPresence: Optional[int] = None
    examinationAchievementsEng: Optional[str] = None
    repetitionEng: Optional[str] = None
    recommendedPrerequisitesEng: Optional[str] = None
    contentEng: Optional[str] = None
    learningOutcomesEng: Optional[str] = None
    teachingMethodsEng: Optional[str] = None
    mediaEng: Optional[str] = None
    literatureEng: Optional[str] = None
    responsiblesEng: Optional[str] = None
    lvSwsLecturerEng: Optional[str] = None

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