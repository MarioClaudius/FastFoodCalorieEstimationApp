import base64
from io import BytesIO

import numpy as np
import uvicorn
from PIL import Image
from fastapi import FastAPI, File, UploadFile

import server_model

app = FastAPI()

@app.get('/')
def test():
    return "Hello World"

@app.post("/predict")
async def predict_image(file: UploadFile = File(...)):
    ext = file.filename.split(".")[-1]
    extension = file.filename.split(".")[-1] in ("jpg", "jpeg", "png")
    print(extension)
    if not extension:
        return "Image must be jpg or png format!"

    image_data = await file.read()
    image = server_model.read_imagefile(image_data)
    result1 = server_model.predict(image)
    result = result1[0]
    masked_image = server_model.get_masked_image(image, result['rois'], result['masks'], result['class_ids'], ['BG', 'Ayam Goreng', 'French Fries', 'Hamburger', 'Nugget', 'Kartu', 'Others'], result['scores'])
    print(type(masked_image))
    class_ids = result['class_ids']
    masks = result['masks']
    scores = result['scores']
    class_names = ['BG', 'Ayam Goreng', 'French Fries', 'Hamburger', 'Nugget', 'Kartu', 'Others']
    class_energy = [0.0, 1.874, 3.12, 2.63, 3.26, 0.0, 0.0]     # in kal/g
    class_density = [0.0, 0.68, 0.24, 1.15, 0.59, 0.0, 0.0]     # in g/cm3
    class_height = [0.0, 4.56, 0.7, 4.1, 1.4, 0.0, 0.0]         # in cm, notes: untuk height ayam maybe diganti
    # ambil mask kartu dengan confidence score tertinggi
    list_index_kartu = np.where(class_ids == 5)
    print(list_index_kartu)
    print(type(list_index_kartu))
    card_is_not_found = (list_index_kartu[0].size == 0)
    fast_food_class = ""
    calorie = 0.0
    if card_is_not_found:
        return {
            "error": True,
            "error_message": "No Object Reference Detected"
        }
    else:
        max = 0
        maxIndexKartu = -1
        for index in list_index_kartu[0]:
            if scores[index] > max:
                max = scores[index]
                maxIndexKartu = index

        max_kartu_mask = masks[:,:, maxIndexKartu]
        bool_max_kartu, counts_max_kartu = np.unique(max_kartu_mask, return_counts=True)
        dict_max_kartu = dict(zip(bool_max_kartu, counts_max_kartu))
        pixel_kartu = dict_max_kartu[True]
        area_kartu_in_cm = 46.20688     # ukuran : 8.56 cm x 5.398 cm
        # print('MAX KARTU: %r' % dict_max_kartu)
        for class_id in class_ids:
            if class_names[class_id] != 'Others' and class_names[class_id] != 'Kartu':
                class_mask = masks[:,:, class_ids == class_id]
                bool_class, counts_class = np.unique(class_mask, return_counts=True)
                dict_class = dict(zip(bool_class, counts_class))
                print('%r : %r' % (class_names[class_id], dict_class))
                fast_food_class += ' ' + class_names[class_id] + ','
                pixel_class = dict_class[True]
                area_class_in_cm = (pixel_class / pixel_kartu) * area_kartu_in_cm
                volume_class = area_class_in_cm * class_height[class_id]
                mass_class = volume_class * class_density[class_id]
                calorie += mass_class * class_energy[class_id]
                # print('area class: %r' % area_class_in_cm)
                # print('volume_class: %r' % volume_class)
                # print('mass: %r' % mass_class)
                # print('calorie: %r' % calorie)

    masked_image_pil = Image.fromarray(masked_image.astype(np.uint8))
    pil_bytes = BytesIO()
    masked_image_pil.save(pil_bytes, format='JPEG')
    pil_bytes = pil_bytes.getvalue()
    print(masked_image.dtype)

    masked_image_byte_encoded = base64.b64encode(pil_bytes).decode("utf-8")
    print(type(masked_image_byte_encoded))
    return {
        "img_byte_enc": masked_image_byte_encoded,
        "type": fast_food_class[:-1],
        "calorie": calorie,
        "error": False,
        "error_message": "No Error"
    }

# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    # 192.168.100.11 buat lan
    # 192.168.100.6 buat wifi
    uvicorn.run(app, host="192.168.100.11")

# See PyCharm help at https://www.jetbrains.com/help/pycharm/
