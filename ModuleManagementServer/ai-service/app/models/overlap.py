from pydantic import BaseModel
from typing import List

class SimilarModule(BaseModel):
    moduleId: str
    title: str
    similarity: float

class SimilarityResponse(BaseModel):
    similarModules: List[SimilarModule]

class ModuleAddedResponse(BaseModel):
    message: str