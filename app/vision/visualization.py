import cv2
import numpy as np


def draw_detections(
        image: np.ndarray,
        detections: list[dict]
) -> np.ndarray:
    result = image.copy()

    count = 0
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

        count += 1
    s = "PL: " + str(count)
    cv2.putText(result, s, (20, 20),
                cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 0, 0), 1)

    return result