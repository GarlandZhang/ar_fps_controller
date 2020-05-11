import cv2
import os
from images import split_data

output_dir = 'frames'

cap = cv2.VideoCapture(0)
i = 0

while True:
  _, frame = cap.read()
  output_file = os.path.join(output_dir, 'img{}.jpg'.format(i))

  cv2.imwrite(output_file, frame)

  i += 1
  cv2.imshow('img', frame)
  k = cv2.waitKey(30) & 0xff
  if k == 27:
    break

cap.release()
cv2.destroyAllWindows()