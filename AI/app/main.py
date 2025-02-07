from fastapi import FastAPI
from app.routers.completions import router as completions_router
from app.routers.overlap_detection import router as overlap_detection_router

app = FastAPI(
    title="Module Description Completion Service API",
    description="API documentation for the Module Description Completion Service.",
    version="0.0.1"
)

app.include_router(completions_router)
app.include_router(overlap_detection_router)