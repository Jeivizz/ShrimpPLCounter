import cv2
import numpy as np


def detect_components(mask: np.ndarray) -> list[dict]:
    num_labels, labels, stats, centroids = cv2.connectedComponentsWithStats(
        mask,
        connectivity=8,
    )

    components = []

    for label in range(1, num_labels):
        x = int(stats[label, cv2.CC_STAT_LEFT])
        y = int(stats[label, cv2.CC_STAT_TOP])
        w = int(stats[label, cv2.CC_STAT_WIDTH])
        h = int(stats[label, cv2.CC_STAT_HEIGHT])
        a = int(stats[label, cv2.CC_STAT_AREA])

        center_x = float(centroids[label][0])
        center_y = float(centroids[label][1])

        components.append({
            "label": label,
            "x": x,
            "y": y,
            "width": w,
            "height": h,
            "area": a,
            "center_x": center_x,
            "center_y": center_y
        })

    return components