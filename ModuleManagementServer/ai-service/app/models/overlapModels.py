from pydantic import BaseModel
from typing import List

class SimilarModule(BaseModel):
    moduleId: str
    titleEng: str
    levelEng: str
    languageEng: str
    frequencyEng: str
    credits: int
    duration: str
    hoursTotal: int
    hoursSelfStudy: int
    hoursPresence: int
    examinationAchievementsEng: str
    examinationAchievementsPromptEng: str
    repetitionEng: str
    recommendedPrerequisitesEng: str
    contentEng: str
    contentPromptEng: str
    learningOutcomesEng: str
    learningOutcomesPromptEng: str
    teachingMethodsEng: str
    teachingMethodsPromptEng: str
    mediaEng: str
    literatureEng: str
    responsiblesEng: str
    lvSwsLecturerEng: str
    similarity: float

class SimilarityResponse(BaseModel):
    similarModules: List[SimilarModule]

class ModuleAddedResponse(BaseModel):
    message: str