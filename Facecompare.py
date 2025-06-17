import cv2
import requests
import os
import glob
import numpy as np
import json

API_KEY = 'xQLsTmMyqp1L2MIt7M3l0h-cQiy0Dwhl'
API_SECRET = 'TyBSGw8NBEP9Tbhv_JbQM18mIlorY6-D'
FACE_COMPARE_URL = 'https://api-us.faceplusplus.com/facepp/v3/compare'

LIVE_IMAGE_PATH = "live_face.jpg"
KNOWN_IMAGES_DIR = "images/"
MAX_WIDTH = 600

def resize_and_encode(image_path):
    img = cv2.imread(image_path)
    if img is None:
        return None
    h, w = img.shape[:2]
    if w > MAX_WIDTH:
        scale = MAX_WIDTH / float(w)
        img = cv2.resize(img, (MAX_WIDTH, int(h * scale)), interpolation=cv2.INTER_AREA)
    success, encoded = cv2.imencode(".jpg", img, [int(cv2.IMWRITE_JPEG_QUALITY), 85])
    return encoded.tobytes() if success else None

def capture_image():
    cam = cv2.VideoCapture(0)
    if not cam.isOpened():
        return None
    while True:
        ret, frame = cam.read()
        if not ret:
            break
        cv2.imshow("Webcam", frame)
        key = cv2.waitKey(1)
        if key == 27:  # ESC
            break
        elif key == 32:  # SPACE
            cv2.imwrite(LIVE_IMAGE_PATH, frame)
            break
    cam.release()
    cv2.destroyAllWindows()
    return LIVE_IMAGE_PATH if os.path.exists(LIVE_IMAGE_PATH) else None

def compare_faces_bytes(image_bytes1, image_bytes2):
    files = {
        'api_key': (None, API_KEY),
        'api_secret': (None, API_SECRET),
        'image_file1': ('img1.jpg', image_bytes1, 'image/jpeg'),
        'image_file2': ('img2.jpg', image_bytes2, 'image/jpeg')
    }
    response = requests.post(FACE_COMPARE_URL, files=files)
    try:
        data = response.json()
        return {'confidence': data.get('confidence', -1)}
    except:
        return {'confidence': -1, 'error': 'Invalid JSON response'}

if __name__ == '__main__':
    live_path = capture_image()
    if not live_path:
        print(json.dumps({"status": "capture_failed"}))
        exit()

    live_bytes = resize_and_encode(live_path)
    if not live_bytes:
        print(json.dumps({"status": "encode_failed"}))
        exit()

    best_match = None
    highest_confidence = 0
    known_files = glob.glob(KNOWN_IMAGES_DIR + "*.jpg")

    for known_path in known_files:
        known_bytes = resize_and_encode(known_path)
        if not known_bytes:
            continue
        result = compare_faces_bytes(live_bytes, known_bytes)
        confidence = result.get("confidence", 0)
        if confidence > highest_confidence:
            highest_confidence = confidence
            best_match = known_path

    if best_match and highest_confidence > 80:
        result = {"status": "authorized", "match": best_match, "confidence": highest_confidence}
    elif best_match:
        result = {"status": "low_confidence", "match": best_match, "confidence": highest_confidence}
    else:
        result = {"status": "no_match", "confidence": 0}

    print(json.dumps(result))
