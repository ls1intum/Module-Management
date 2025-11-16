import os
import logging
from pathlib import Path
from pydantic_settings import BaseSettings, SettingsConfigDict

_DEFAULT_ENV = Path(__file__).resolve().parent.parent.parent / ".env"
_ENV_FILE = os.getenv("ENV_FILE") or (str(_DEFAULT_ENV) if _DEFAULT_ENV.exists() else None)

logger = logging.getLogger(__name__)

class Settings(BaseSettings):
    # Azure OpenAI settings
    AZURE_API_KEY: str = ""
    AZURE_ENDPOINT: str = ""
    AZURE_DEPLOYMENT_NAME: str = ""
    AZURE_API_VERSION: str = ""
    
    # Local LLM settings
    USE_LOCAL_LLM: bool = False
    LOCAL_LLM_BASE_URL: str = ""  
    LOCAL_LLM_MODEL: str = "" 
    
    logger.info("Loading settings from env file: %s", _ENV_FILE or "No env file, using environment variables only")

    model_config = SettingsConfigDict(
        env_file=_ENV_FILE,          # None in prod unless ENV_FILE is set
        env_file_encoding="utf-8",
        extra="ignore",
    )


settings = Settings()
