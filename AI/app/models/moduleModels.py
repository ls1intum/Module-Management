
from enum import Enum
from typing import Optional
from pydantic import BaseModel

class Language(str, Enum):
    ENGLISH = "English"
    GERMAN = "German"

class ModuleInfo(BaseModel):
    contextPrompt: Optional[str] = None
    titleEng: Optional[str] = None
    levelEng: Optional[str] = None
    languageEng: Optional[Language] = None
    frequencyEng: Optional[str] = None
    credits: Optional[int] = None
    durationEng: Optional[str] = None
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
