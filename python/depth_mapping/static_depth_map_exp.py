import numpy as np
import cv2
import time
import torch
import matplotlib.pyplot as plt

#model_type = "DPT_Large"     # MiDaS v3 - Large     (highest accuracy, slowest inference speed)
#model_type = "DPT_Hybrid"   # MiDaS v3 - Hybrid    (medium accuracy, medium inference speed)
model_type = "MiDaS_small"  # MiDaS v2.1 - Small   (lowest accuracy, highest inference speed)

midas = torch.hub.load("intel-isl/MiDaS", model_type)
device = torch.device("cuda") if torch.cuda.is_available() else torch.device("cpu")
midas.to(device)
midas.eval()
midas_transforms = torch.hub.load("intel-isl/MiDaS", "transforms")

if model_type == "DPT_Large" or model_type == "DPT_Hybrid":
    transform = midas_transforms.dpt_transform
else:
    transform = midas_transforms.small_transform
    near_normal=cv2.imread("/img_ref_stereo/near_normal.png")
    near_wide=cv2.imread("/img_ref_stereo/near_wide.png")
    img1= cv2.cvtColor(near_normal, cv2.COLOR_BGR2RGB)
    img2=cv2.cvtColor(near_wide,cv2.COLOR_BGR2RGB)
    input_batch1 = transform(img1).to(device)
    input_batch2 = transform(img2).to(device)

    with torch.no_grad():
        prediction1 = midas(input_batch1)
        prediction2= midas(input_batch2)

        prediction1 = torch.nn.functional.interpolate(
            prediction1.unsqueeze(1),
            size=img1.shape[:2],
            mode="bicubic",
            align_corners=False,
        ).squeeze()
        prediction2 = torch.nn.functional.interpolate(
            prediction2.unsqueeze(1),
            size=img2.shape[:2],
            mode="bicubic",
            align_corners=False,
        ).squeeze()
    output1 = prediction1.cpu().numpy()
    output2 = prediction2.cpu().numpy()
    cv2.imshow("img1",near_normal)
    cv2.imshow("out1",output1)
    cv2.imshow("img2",near_wide)
    cv2.imshow("out2",output2)