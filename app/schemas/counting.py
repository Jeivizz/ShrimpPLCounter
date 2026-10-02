from pydantic import BaseModel, Field


class Detection(BaseModel):
    x: int
    y: int
    width: int
    height: int
    area: int
    center_x: float
    center_y: float


class CountingResponse(BaseModel):
    count: int = Field(description="Total de PLs detectadas")
    image_width: int = Field(description="Largura da imagem original (px)")
    image_height: int = Field(description="Altura da imagem original (px)")
    scale: float = Field(description="Escala total aplicada internamente (diagnóstico)")
    detections: list[Detection]
    annotated_image_base64: str | None = Field(
        default=None, description="JPEG anotado em base64 (só se include_image=true)"
    )