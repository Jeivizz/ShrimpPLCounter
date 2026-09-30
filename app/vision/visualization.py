import cv2
import numpy as np


def draw_detections(
        image: np.ndarray,
        detections: list[dict]
) -> np.ndarray:
    result = image.copy()

    for detection in detections:
        x = detection["x"]
        y = detection["y"]
        width = detection["width"]
        height = detection["height"]

        cv2.rectangle(
            result,
            (x, y),
            (x + width, y + height),
            (0, 255, 0),
            2
        )

    return result