# filtering.py
import numpy as np

def _gap(a, b):
    dx = max(a["x"] - (b["x"] + b["width"]), b["x"] - (a["x"] + a["width"]), 0)
    dy = max(a["y"] - (b["y"] + b["height"]), b["y"] - (a["y"] + a["height"]), 0)
    return (dx * dx + dy * dy) ** 0.5

def filter_components(components, min_area=28, max_area=500,
                      frag_ratio=0.5, max_gap=15):
    valid = [c for c in components if min_area <= c["area"] <= max_area]
    if not valid:
        return []
    median = float(np.median([c["area"] for c in valid]))

    result = []
    for c in valid:
        is_small = c["area"] < frag_ratio * median
        near_larger = any(
            o is not c and o["area"] > c["area"] and _gap(c, o) <= max_gap
            for o in valid
        )
        if not (is_small and near_larger):
            result.append(c)
    return result