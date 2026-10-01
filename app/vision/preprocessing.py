# preprocessing.py
import cv2
import numpy as np

BLUR_K = 25
SIGMAX = 0
TARGET_MEDIAN_AREA = 130   # área mediana de blob na escala "calibrada"
SCALE_MIN, SCALE_MAX = 0.5, 2.0

def resize(image: np.ndarray, scale: float) -> np.ndarray:
    if scale == 1.0:
        return image
    interp = cv2.INTER_CUBIC if scale > 1 else cv2.INTER_AREA
    return cv2.resize(image, None, fx=scale, fy=scale, interpolation=interp)

def gray_blur(image: np.ndarray) -> np.ndarray:
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    return cv2.GaussianBlur(gray, (BLUR_K, BLUR_K), SIGMAX)

def estimate_scale(image, segment_fn, detect_fn, min_area=40, max_area=500) -> float:
    comps = detect_fn(segment_fn(gray_blur(image)))
    areas = [c["area"] for c in comps if min_area <= c["area"] <= max_area]
    if len(areas) < 5:
        return 1.0
    s = float(np.sqrt(TARGET_MEDIAN_AREA / np.median(areas)))
    return float(np.clip(s, SCALE_MIN, SCALE_MAX))

def preprocess_image(image: np.ndarray) -> np.ndarray:
    if image is None:
        raise ValueError("Image cannot be None")
    return gray_blur(image)