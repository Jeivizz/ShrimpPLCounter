import io

import cv2
import numpy as np
from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def _synthetic_png(n_blobs: int = 12, size=(600, 800)) -> bytes:

    img = np.full((*size, 3), 235, np.uint8)
    rng = np.random.default_rng(0)
    cols = 4
    for i in range(n_blobs):
        cx = 100 + (i % cols) * 180 + int(rng.integers(-10, 10))
        cy = 100 + (i // cols) * 180 + int(rng.integers(-10, 10))
        cv2.ellipse(img, (cx, cy), (14, 9), 30, 0, 360, (40, 50, 60), -1)
    ok, buf = cv2.imencode(".png", img)
    assert ok
    return buf.tobytes()


def test_health():
    assert client.get("/health").json() == {"status": "ok"}


def test_count_returns_schema_and_detections():
    r = client.post("/api/v1/count", files={"file": ("a.png", _synthetic_png(), "image/png")})
    assert r.status_code == 200
    body = r.json()
    assert body["count"] == len(body["detections"]) > 0
    assert body["image_width"] == 800 and body["image_height"] == 600
    assert body["annotated_image_base64"] is None
    d = body["detections"][0]
    assert 0 <= d["center_x"] <= 800 and 0 <= d["center_y"] <= 600


def test_include_image_returns_decodable_jpeg():
    import base64
    r = client.post("/api/v1/count?include_image=true",
                    files={"file": ("a.png", _synthetic_png(), "image/png")})
    assert r.status_code == 200
    raw = base64.b64decode(r.json()["annotated_image_base64"])
    assert cv2.imdecode(np.frombuffer(raw, np.uint8), cv2.IMREAD_COLOR) is not None


def test_invalid_image_is_400():
    r = client.post("/api/v1/count", files={"file": ("a.png", b"nao sou imagem", "image/png")})
    assert r.status_code == 400


def test_empty_file_is_400():
    r = client.post("/api/v1/count", files={"file": ("a.png", b"", "image/png")})
    assert r.status_code == 400


def test_missing_file_is_422():
    assert client.post("/api/v1/count").status_code == 422