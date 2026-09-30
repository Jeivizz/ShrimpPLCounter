import cv2

from app.vision.preprocessing import preprocess_image
from app.vision.segmentation import segment_image
from app.vision.detection import detect_components
from app.vision.filtering import filter_components
from app.vision.visualization import draw_detections

RSZ = 0.5
IMAGE_PATH = "./data/test.png"


def main():
    # 1. Carregar imagem
    image = cv2.imread(IMAGE_PATH)

    if image is None:
        raise FileNotFoundError(
            f"Não foi possível carregar: {IMAGE_PATH}"
        )

    processed = preprocess_image(image)

    mask = segment_image(processed)

    components = detect_components(mask)

    detections = filter_components(components)

    result = draw_detections(image, detections)

    print(f"Componentes encontrados: {len(components)}")
    print(f"PLs detectadas: {len(detections)}")

    cv2.imshow("1 - Original", cv2.resize(image, (0, 0), fx=RSZ, fy=RSZ))
    cv2.imshow("2 - Pre-processamento", cv2.resize(processed, (0, 0), fx=RSZ, fy=RSZ))
    cv2.imshow("3 - Segmentacao", cv2.resize(mask, (0, 0), fx=RSZ, fy=RSZ))
    cv2.imshow("4 - Resultado das deteccoes", cv2.resize(result, (0, 0), fx=RSZ, fy=RSZ))

    cv2.waitKey(0)

    cv2.destroyAllWindows()


if __name__ == "__main__":
    main()