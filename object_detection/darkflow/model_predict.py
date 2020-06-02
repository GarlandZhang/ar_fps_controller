import numpy as np

from darkflow.net.build import TFNet
import cv2
import tensorflow as tf
import os
import math

options2 = {
    'gpu': 1.0,
    'model': 'cfg/yolov2-tiny-hand.cfg',
    'load': 12040,
    'threshold': 0.3,
    'backup': 'ckpt/'
}
tfnet2 = TFNet(options2)

tfnet2.load_from_ckpt()

cap = cv2.VideoCapture(0)
i = 0

while True:
  _, img = cap.read()
  img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
  res = tfnet2.return_predict(img)
  if len(res) != 0:
    res = res[0]
    print(res)
    top_left = (res['topleft']['x'], res['topleft']['y'])
    bottom_right = (res['bottomright']['x'], res['bottomright']['y'])
    label = res['label']
    img = cv2.rectangle(img, top_left, bottom_right, (0, 255, 0), 7)
    img = cv2.putText(img, label, top_left, cv2.FONT_HERSHEY_COMPLEX, 1, (0, 0, 0), 2)
    # cv2.imwrite('output/img{}.jpg'.format(str(i)), img)
  
  cv2.imshow('img', img)

  if cv2.waitKey(25) & 0xFF == ord('q'):
    cv2.destroyAllWindows()
    break
  i += 1