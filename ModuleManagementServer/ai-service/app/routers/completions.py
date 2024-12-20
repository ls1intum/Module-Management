from fastapi import APIRouter

router = APIRouter(prefix="/completions", tags=["completions"])

@router.get("/")
def hello():
    return {"message": "Hello Space"}