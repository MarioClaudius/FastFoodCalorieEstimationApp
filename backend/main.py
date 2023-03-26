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
    # print(result)
    masked_image = server_model.get_masked_image(image, result['rois'], result['masks'], result['class_ids'], ['BG', 'Dada', 'Nugget', 'Kartu'], result['scores'])
    print(type(masked_image))
    masked_image_pil = Image.fromarray(masked_image.astype(np.uint8))
    pil_bytes = BytesIO()
    masked_image_pil.save(pil_bytes, format='JPEG')
    pil_bytes = pil_bytes.getvalue()
    print(masked_image.dtype)

    masked_image_byte_encoded = base64.b64encode(pil_bytes).decode("utf-8")
    # masked_image_byte_encoded = base64.b64encode(masked_image_byte_compressed).decode("utf-8")
    print(type(masked_image_byte_encoded))
    # mediatype = "image/" + ext
    return {
        "img_byte_enc": masked_image_byte_encoded
    }
    # return {
    #     "predicted_image_byte": masked_image_byte
    # }

@app.post("/predict1")
async def predict_image1(file: UploadFile = File(...)):
    ext = file.filename.split(".")[-1]
    extension = file.filename.split(".")[-1] in ("jpg", "jpeg", "png")
    print(extension)
    if not extension:
        return "Image must be jpg or png format!"
    # try:
    #     image = server_model.read_imagefile(await file.read())
    #     result = server_model.predict(image)
    #     print("hasil: " + result)
    #     return result
    # except Exception as e:
    #     print(e)
    #     return e
    # image = server_model.read_imagefile(await file.read())
    # print(type(image))
    # IMAGE_PATH = 'C:\Kuliah\KP & Skripsi\Dataset Testing\Ayam Dada\dada1_1.jpg'
    # image1 = skimage.io.imread(IMAGE_PATH)
    # print(type(image1))
    # image_byte = image.tobytes()
    image_data = await file.read()
    image = server_model.read_imagefile(image_data)
    print(type(image))
    # image_data = await file.read()
    print(type(image_data))
    image_array = np.array(image_data)
    print(image_array.dtype)
    image_array_pil = Image.fromarray(image_array)
    image_array_pil.show()
    # image_array_change = image_array.astype(np.uint8)
    # print(type(image_array))
    image_bytes = image_array.tobytes()
    # print(type(image_bytes))
    # buf = io.BytesIO()
    # image_bytes.save(buf, format='JPEG')
    # byte_im = buf.getvalue()
    # # print("SEBELUM COMPRESS\n")
    # # masked_image_byte_compressed = gzip.compress(image_data)
    # # print("SETELAH COMPRESS\n")
    if type(image_data) == type(image_bytes):
        print("TIPENYA SAMA")
    if image_bytes == image_data:
        print("SAMA")
    image_byte_encoded = base64.b64encode(np.array(image_data).tobytes()).decode("utf-8")
    # image_byte_encoded = base64.b64encode(masked_image_byte_compressed).decode("utf-8")
    # mediatype = "image/" + ext
    return {
        "img_byte_enc": image_byte_encoded
    }
    # return {
    #     "predicted_image_byte": masked_image_byte
    # }


# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    # 192.168.100.11 buat lan
    # 192.168.100.6 buat wifi
    uvicorn.run(app, host="192.168.100.11")

# See PyCharm help at https://www.jetbrains.com/help/pycharm/
