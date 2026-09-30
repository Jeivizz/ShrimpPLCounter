import cv2
import numpy as np
import math


def has_body_evidence(
    image: np.ndarray,
    component: dict,
    search_radius: int = 60,
    body_threshold: int = 150,
    min_body_pixels: int = 8
) -> bool:

    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    center_x = int(component["center_x"])
    center_y = int(component["center_y"])

    height, width = gray.shape

    directions = 16

    for i in range(directions):
        angle = (2 * math.pi * i) / directions

        pixels_found = 0

        for distance in range(5, search_radius):
            x = int(center_x + math.cos(angle) * distance)
            y = int(center_y + math.sin(angle) * distance)

            if x < 0 or x >= width or y < 0 or y >= height:
                break

            # Pequena região ao redor da linha
            neighborhood = gray[
                max(0, y - 1):min(height, y + 2),
                max(0, x - 1):min(width, x + 2)
            ]

            if np.mean(neighborhood) < body_threshold:
                pixels_found += 1

        if pixels_found >= min_body_pixels:
            return True

    return False