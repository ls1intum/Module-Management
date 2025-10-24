import logging
import traceback
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

# NEW: import FastAPI/Starlette exception types & default handlers
from fastapi.exceptions import RequestValidationError, ResponseValidationError
from starlette.exceptions import HTTPException as StarletteHTTPException
from fastapi.exception_handlers import (
    http_exception_handler,
    request_validation_exception_handler,
)


# Configure logging once, early in your app
logging.basicConfig(
    level=logging.INFO,                      # minimum level
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
)

from fastapi import FastAPI
from app.routers.completions import router as completions_router
from app.routers.overlap_detection import router as overlap_detection_router

logger = logging.getLogger(__name__)

app = FastAPI(
    title="Module Description Completion Service API",
    description="API documentation for the Module Description Completion Service.",
    version="0.0.1"
)

logger.info("Initialized FastAPI application")

@app.middleware("http")
async def error_tracing_middleware(request: Request, call_next):
    try:
        return await call_next(request)
    except Exception as e:
        tb = traceback.format_exc()
        logging.error("Unhandled exception: %s\n%s", e, tb)
        return JSONResponse(
            status_code=500,
            content={"error": str(e), "traceback": tb.splitlines()[-10:]},  # last lines only
        )
        
@app.exception_handler(StarletteHTTPException)
async def http_ex_handler(request: Request, exc: StarletteHTTPException):
    logging.exception("HTTPException on %s %s", request.method, request.url.path)
    # delegate to default handler (keeps normal behavior)
    return await http_exception_handler(request, exc)

@app.exception_handler(RequestValidationError)
async def req_val_ex_handler(request: Request, exc: RequestValidationError):
    logging.exception("RequestValidationError on %s %s", request.method, request.url.path)
    # default JSON body, but we already logged full traceback
    return await request_validation_exception_handler(request, exc)

@app.exception_handler(ResponseValidationError)
async def resp_val_ex_handler(request: Request, exc: ResponseValidationError):
    # This is the one that often causes silent 500s when response_model != returned value.
    logging.exception("ResponseValidationError on %s %s", request.method, request.url.path)
    # Provide a short payload so you can see *why* it failed
    return JSONResponse(
        status_code=500,
        content={
            "error": "Response validation failed",
            "detail": exc.errors(),  # shows which field/validator failed
        },
    )

@app.exception_handler(Exception)
async def catch_all_ex_handler(request: Request, exc: Exception):
    logging.exception("Unhandled Exception on %s %s", request.method, request.url.path)
    return JSONResponse(status_code=500, content={"error": str(exc)})

logger.info("error tracing_middleware set up")

app.include_router(completions_router)
app.include_router(overlap_detection_router)
