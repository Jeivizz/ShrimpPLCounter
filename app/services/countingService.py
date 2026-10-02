import base64

import cv2
import numpy as np

from app.config.settings import Settings, settings as default_settings
from app.vision.preprocessing import preprocess_image, resize, estimate_scale
from app.vision.segmentation import segment_image
from app.vision.detection import detect_components
from app.vision.filtering import filter_components
from app.vision.visualization import draw_detections


class InvalidImageError(ValueError):
    """Bytes recebidos não são uma imagem válida."""


def decode_image(data: bytes) -> np.ndarray:

    image = cv2.imdecode(np.frombuffer(data, np.uint8), cv2.IMREAD_COLOR)
    if image is None:
        raise InvalidImageError("Arquivo não é uma imagem válida")
    return image


def _to_original_coords(detections: list[dict], total_scale: float) -> list[dict]:

    f = 1.0 / total_scale
    return [
        {
            "x": int(round(d["x"] * f)),
            "y": int(round(d["y"] * f)),
            "width": int(round(d["width"] * f)),
            "height": int(round(d["height"] * f)),
            "area": int(round(d["area"] * f * f)),
            "center_x": float(d["center_x"] * f),
            "center_y": float(d["center_y"] * f),
        }
        for d in detections
    ]


def count_shrimp(
    image: np.ndarray,
    include_image: bool = False,
    settings: Settings = default_settings,
) -> dict:
    orig_h, orig_w = image.shape[:2]


    pre_scale = min(1.0, settings.max_input_side / max(orig_h, orig_w))
    work = resize(image, pre_scale)

    scale = estimate_scale(work, segment_image, detect_components)
    work = resize(work, scale)

    mask = segment_image(preprocess_image(work))
    components = detect_components(mask)
    detections = filter_components(
        components,
        min_area=settings.min_area,
        max_area=settings.max_area,
        max_gap=settings.max_gap,
    )
    total_scale = pre_scale * scale
    detections_orig = _to_original_coords(detections, total_scale)

    annotated_b64 = None
    if include_image:
        annotated = draw_detections(image, detections_orig)
        ok, buf = cv2.imencode(
            ".jpg", annotated, [cv2.IMWRITE_JPEG_QUALITY, settings.jpeg_quality]
        )
        if ok:
            annotated_b64 = base64.b64encode(buf.tobytes()).decode("ascii")

    return {
        "count": len(detections_orig),
        "image_width": orig_w,
        "image_height": orig_h,
        "scale": round(total_scale, 3),
        "detections": detections_orig,
        "annotated_image_base64": annotated_b64,
    }


def count_from_bytes(data: bytes, include_image: bool = False) -> dict:
    return count_shrimp(decode_image(data), include_image=include_image)