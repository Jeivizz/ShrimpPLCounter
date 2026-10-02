from fastapi import APIRouter, File, HTTPException, Query, UploadFile

from app.config.settings import settings
from app.schemas.counting import CountingResponse
from app.services.countingService import InvalidImageError, count_from_bytes

router = APIRouter(prefix="/api/v1", tags=["counting"])


@router.post("/count", response_model=CountingResponse)
def count(
    file: UploadFile = File(..., description="Foto (JPEG/PNG)"),
    include_image: bool = Query(False, description="Devolver imagem anotada em base64"),
):
    data = file.file.read(settings.max_upload_bytes + 1)
    if len(data) > settings.max_upload_bytes:
        raise HTTPException(413, f"Arquivo maior que {settings.max_upload_mb} MB")
    if not data:
        raise HTTPException(400, "Arquivo vazio")

    try:
        return count_from_bytes(data, include_image=include_image)
    except InvalidImageError as e:
        raise HTTPException(400, str(e))