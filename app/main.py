from fastapi import FastAPI

from app.api.routes import counting

app = FastAPI(title="Shrimp Counter API", version="0.1.0")
app.include_router(counting.router)

# uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

@app.get("/health", tags=["meta"])
def health():
    return {"status": "ok"}