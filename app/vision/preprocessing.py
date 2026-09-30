from email.mime import image

import cv2
import numpy as np

BLUR_K = 25
SIGMAX = 0

def preprocess_image(image: np.ndarray) -> np.ndarray:

    if image is None:
        raise ValueError("Image cannot be None")

    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    blurred = cv2.GaussianBlur(gray, (BLUR_K, BLUR_K), SIGMAX)

    return blurred