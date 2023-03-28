import numpy as np
import os
from PIL import Image
from io import BytesIO
from mrcnn import model as modellib
import matplotlib.pyplot as plt
from matplotlib import patches, lines
from matplotlib.patches import Polygon
from skimage.measure import find_contours

from mrcnn.visualize import apply_mask, random_colors
from mrcnn.config import Config

model = None
ROOT_PATH = os.path.abspath(os.curdir)
MODEL_DIR = os.path.join(ROOT_PATH, "logs")
weight_path = os.path.join(MODEL_DIR, "mask_rcnn_fastfood_0023.h5")

def load_model():
    config = FastFoodConfig()
    model_inner = modellib.MaskRCNN(mode="inference",config=config,model_dir=MODEL_DIR)
    model_inner.load_weights(weight_path, by_name=True)
    print("Model loaded")
    return model_inner

def predict(img: Image.Image):
    global model
    if model is None:
        model = load_model()

    result = model.detect([img], verbose=1)
    return result

def get_masked_image(image, boxes, masks, class_ids, class_names,
                      scores=None, title="",
                      figsize=(16, 16), ax=None,
                      show_mask=True, show_bbox=True,
                      colors=None, captions=None):
    """
    boxes: [num_instance, (y1, x1, y2, x2, class_id)] in image coordinates.
    masks: [height, width, num_instances]
    class_ids: [num_instances]
    class_names: list of class names of the dataset
    scores: (optional) confidence scores for each box
    title: (optional) Figure title
    show_mask, show_bbox: To show masks and bounding boxes or not
    figsize: (optional) the size of the image
    colors: (optional) An array or colors to use with each object
    captions: (optional) A list of strings to use as captions for each object
    """
    # Number of instances
    N = boxes.shape[0]
    if not N:
        print("\n*** No instances to display *** \n")
    else:
        assert boxes.shape[0] == masks.shape[-1] == class_ids.shape[0]

    # If no axis is passed, create one and automatically call show()
    auto_show = False
    if not ax:
        _, ax = plt.subplots(1, figsize=figsize)
        auto_show = True

    # Generate random colors
    colors = colors or random_colors(N)

    # Show area outside image boundaries.
    height, width = image.shape[:2]
    ax.set_ylim(height + 10, -10)
    ax.set_xlim(-10, width + 10)
    ax.axis('off')
    ax.set_title(title)

    masked_image = image.astype(np.uint32).copy()
    for i in range(N):
        color = colors[i]

        # Bounding box
        if not np.any(boxes[i]):
            # Skip this instance. Has no bbox. Likely lost in image cropping.
            continue
        y1, x1, y2, x2 = boxes[i]
        if show_bbox:
            p = patches.Rectangle((x1, y1), x2 - x1, y2 - y1, linewidth=2,
                                  alpha=0.7, linestyle="dashed",
                                  edgecolor=color, facecolor='none')
            ax.add_patch(p)

        # Label
        if not captions:
            class_id = class_ids[i]
            score = scores[i] if scores is not None else None
            label = class_names[class_id]
            caption = "{} {:.3f}".format(label, score) if score else label
        else:
            caption = captions[i]
        ax.text(x1, y1 + 8, caption,
                color='w', size=11, backgroundcolor="none")

        # Mask
        mask = masks[:, :, i]
        if show_mask:
            masked_image = apply_mask(masked_image, mask, color)

        # Mask Polygon
        # Pad to ensure proper polygons for masks that touch image edges.
        padded_mask = np.zeros(
            (mask.shape[0] + 2, mask.shape[1] + 2), dtype=np.uint8)
        padded_mask[1:-1, 1:-1] = mask
        contours = find_contours(padded_mask, 0.5)
        for verts in contours:
            # Subtract the padding and flip (y, x) to (x, y)
            verts = np.fliplr(verts) - 1
            p = Polygon(verts, facecolor="none", edgecolor=color)
            ax.add_patch(p)
    return masked_image
    # ax.imshow(masked_image.astype(np.uint8))
    # if auto_show:
    #     plt.show()

def read_imagefile(file) -> Image.Image:
    image = Image.open(BytesIO(file))
    # convert to numpy array
    image_numpy = np.asarray(image)
    print("read image file")
    print(image_numpy.dtype)
    # image_numpy = np.frombuffer(file, dtype=np.uint8)
    return image_numpy

class FastFoodConfig(Config):
    """Configuration for training on the custom  dataset.
    Derives from the base Config class and overrides some values.
    """
    # Give the configuration a recognizable name
    NAME = "FastFood"

    # We use a GPU with 12GB memory, which can fit two images.
    # Adjust down if you use a smaller GPU.
    IMAGES_PER_GPU = 1
    GPU_COUNT = 1

    # Number of classes (including background)
    NUM_CLASSES = 1 + 6  # Background + dada, nugget, kartu

    # Number of training steps per epoch
    # STEPS_PER_EPOCH = 10

    # Skip detections with < 90% confidence
    DETECTION_MIN_CONFIDENCE = 0.7