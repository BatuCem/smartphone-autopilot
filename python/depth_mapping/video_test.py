import cv2
cap=cv2.VideoCapture(0)
while(cap.isOpened()):
    success,img=cap.read()
    if not success:
        break
    img=cv2.resize(img,(600,400))
    cv2.putText(img,"LIVE",(100,300),cv2.FONT_HERSHEY_SIMPLEX,5,(0,0,255),2)
    cv2.imshow("vid",img)
    if cv2.waitKey(1)==ord('q'):
        break
cap.release()
cv2.destroyAllWindows()