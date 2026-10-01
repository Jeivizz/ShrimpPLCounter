import cv2
import os

from app.vision.preprocessing import preprocess_image, resize, estimate_scale
from app.vision.segmentation import segment_image
from app.vision.detection import detect_components
from app.vision.filtering import filter_components
from app.vision.visualization import draw_detections

os.environ["QT_QPA_FONTDIR"] = "/usr/share/fonts/truetype/dejavu"

os.environ.pop("XDG_SESSION_TYPE", None)

RSZ = 0.5 # zoom só da exibição
IMAGE_PATH = "./data/test.png"


def main():

    image = cv2.imread(IMAGE_PATH)

    if image is None:
        raise FileNotFoundError(
            f"Não foi possível carregar: {IMAGE_PATH}"
        )

    # Normaliza a escala: a imagem é redimensionada para o tamanho de PL
    # para o qual blur, threshold e filtros foram calibrados.
    scale = estimate_scale(image, segment_image, detect_components)
    image = resize(image, scale)

    processed = preprocess_image(image)

    mask = segment_image(processed)

    components = detect_components(mask)

    detections = filter_components(
        components,
        min_area=30,
        max_area=500,
        max_gap=15,
    )

    # Desenha sobre a imagem já normalizada, para as caixas coincidirem
    result = draw_detections(image, detections)

    print(f"Escala aplicada: {scale:.2f}")
    print(f"Componentes encontrados: {len(components)}")
    print(f"PLs detectadas: {len(detections)}")

    cv2.imshow("1 - Original (normalizada)", cv2.resize(image, (0, 0), fx=RSZ, fy=RSZ))
    cv2.imshow("2 - Pre-processamento", cv2.resize(processed, (0, 0), fx=RSZ, fy=RSZ))
    cv2.imshow("3 - Segmentacao", cv2.resize(mask, (0, 0), fx=RSZ, fy=RSZ))
    cv2.imshow("4 - Resultado das deteccoes", cv2.resize(result, (0, 0), fx=RSZ, fy=RSZ))

    cv2.waitKey(0)

    cv2.destroyAllWindows()


if __name__ == "__main__":
    main()