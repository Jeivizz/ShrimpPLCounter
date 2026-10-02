import os
from dataclasses import dataclass


def _int(name: str, default: int) -> int:
    return int(os.getenv(name, default))


@dataclass(frozen=True)
class Settings:
    # Upload
    max_upload_mb: int = _int("MAX_UPLOAD_MB", 15)
    max_input_side: int = _int("MAX_INPUT_SIDE", 1600)
    min_area: int = _int("MIN_AREA", 30)
    max_area: int = _int("MAX_AREA", 500)
    max_gap: int = _int("MAX_GAP", 15)
    jpeg_quality: int = _int("JPEG_QUALITY", 85)

    @property
    def max_upload_bytes(self) -> int:
        return self.max_upload_mb * 1024 * 1024


settings = Settings()