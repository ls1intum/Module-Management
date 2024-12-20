from fastapi import FastAPI
from .routers.completions import router as completions_router

app = FastAPI(
    title="Module Description Completion Service API",
    description="API documentation for the Module Description Completion Service.",
    version="0.0.1"
)

app.include_router(completions_router)