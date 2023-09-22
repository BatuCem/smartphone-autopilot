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
cap=cv2.VideoCapture(0);
while(cap.isOpened()):
    success,img=cap.read()
    if not success:
        break
    imgHeight,imgWidth,channels= img.shape
    start=time.time()
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    input_batch = transform(img).to(device)
    with torch.no_grad():
        prediction = midas(input_batch)

        prediction = torch.nn.functional.interpolate(
            prediction.unsqueeze(1),
            size=img.shape[:2],
            mode="bicubic",
            align_corners=False,
        ).squeeze()
    output = prediction.cpu().numpy()
    end=time.time()
    fps=1/(end-start)
    cv2.putText(img,f"{fps:.2f} FPS", (20,30), cv2.FONT_HERSHEY_SIMPLEX,1.5,(0,255,0),2)
    cv2.imshow("img",img)
    cv2.imshow("out",(output-output.min())/(output.max()-output.min()))
    if cv2.waitKey(1)==ord('q'):
        break
cap.release()
cv2.destroyAllWindows()
"""
path_model="models/"
model_name="dpt_swin2_large_384.pt"
model_name="model-small.onnx"

#model=cv2.dnn.readNetFromONNX(onnxFile=path_model+model_name)

 model.setPreferableBackend(cv2.dnn.DNN_BACKEND_CUDA)
model.setPreferableTarget(cv2.dnn.DNN_TARGET_CUDA)
cap=cv2.VideoCapture(0);
while(cap.isOpened()):
    success,img=cap.read()
    if not success:
        break
    imgHeight,imgWidth,channels= img.shape
    start=time.time()
    blob=cv2.dnn.blobFromImage(img,1/255.,(384,384),(123.675,116.28,103.53),True,False)
    model.setInput(blob)
    output=model.forward()
    output=output[0,:,:]
    output=cv2.resize(output,(imgWidth,imgHeight))
    output=cv2.normalize(output,None,0,1,cv2.NORM_MINMAX,cv2.CV_32F)
    end=time.time()
    fps=1/(end-start)
    cv2.putText(img,f"{fps:.2f} FPS", (20,30), cv2.FONT_HERSHEY_SIMPLEX,1.5,(0,255,0),2)
    cv2.imshow("img",img)
    cv2.imshow("out",output)
    if cv2.waitKey(1)==ord('q'):
        break
cap.release()
cv2.destroyAllWindows() """