from typing import List
from fastapi import APIRouter, HTTPException
from app.models.completionModels import ModuleInfo
from app.models.overlapModels import SimilarModule, SimilarityResponse, ModuleAddedResponse
from app.services.overlap_service import OverlapService

router = APIRouter(prefix="/overlap-detection", tags=["overlap-detection"])
overlap_service = OverlapService()

@router.post("/check-similarity", response_model=List[SimilarModule])
async def check_module_similarity(module_info: ModuleInfo) -> List[SimilarModule]:
    try:
        similar_modules = overlap_service.find_similar_modules(module_info)
        return similar_modules
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/add-module", response_model=ModuleAddedResponse)
async def add_module(module_info: ModuleInfo) -> ModuleAddedResponse:
    try:
        overlap_service.add_module(module_info)
        return ModuleAddedResponse(message="Module added successfully")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
