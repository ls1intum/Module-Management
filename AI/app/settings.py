from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    AZURE_API_KEY: str = ""
    AZURE_ENDPOINT: str = ""
    AZURE_DEPLOYMENT_NAME: str = ""
    AZURE_API_VERSION: str = ""

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")


settings = Settings()
