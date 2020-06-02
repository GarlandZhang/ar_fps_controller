import tensorflow as tf
import cv2
import numpy as np
import os
import matplotlib.pyplot as plt
import xml.etree.ElementTree as ET
import copy
from hand_classifier.utils import *
from adb_log_reader import *
import time
import socket

from object_detection.darkflow.darkflow.net.build import TFNet
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense, Conv2D, Flatten, Dropout, MaxPooling2D, Softmax
from tensorflow.keras.preprocessing.image import ImageDataGenerator

# set up adb log reader
process = subprocess.Popen(['adb', 'logcat', '-s', 'class_img'], stdout=subprocess.PIPE)

stdout_queue = Queue()
stdout_reader = AsynchronousFileReader(process.stdout, stdout_queue)
stdout_reader.start()

# model = tf.keras.models.load_model('hand_classifier/Deep-Learning/Transfer Learning CNN/mobilenet_many_angles_new4.h5')
frames_path = 'hand_classifier/frames/point_up'
gt_path = frames_path + '_annotations'
video_path = 'test_video.avi'
assets_dir = 'C:/Users/GZhang/Desktop/side-projects/ar_fps_sim/android_mobilenet/app/src/main/assets/images'

# detector setup
options = {
    'gpu': 1.0,
    'model': 'object_detection/darkflow/cfg/yolov2-tiny-voc-hand.cfg',
    'load': 79440,
    'threshold': 0.1,
    'backup': 'E:/ar_fps/object_detection/ckpt',
}
detector = TFNet(options)
detector.load_from_ckpt()

# ground truth dataa
labels = get_class_labels()
gt_files = os.listdir(gt_path)

# get frames from directory
frames = []
frame_files = os.listdir(frames_path)
for frame_file in frame_files:
  frame = cv2.imread(os.path.join(frames_path, frame_file))
  frames.append(frame)

# time 
total_time = 0
write_img = 0
detect_time = 0

NUM_IMAGES = 100

# set up client socket 
HOST = '10.31.108.96'
PORT = 34567
client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client.connect((HOST, PORT))

for ind, image in enumerate(frames[:NUM_IMAGES + 1]):

  if ind == 1:
    total_time = 0
    write_img = 0
    detect_time = 0

  # print('Index: {}'.format(ind))
  
  # detect with ground truth
  gt_file = os.path.join(gt_path, gt_files[ind])
  boxes, classes = read_xml(gt_file)
  left, top, right, bottom = boxes[0]
  act_left, act_top, act_right, act_bottom = boxes[0]
  actual_label = classes[0]

  cropped = image[top:bottom, left:right]
  
  start_time = time.time()

  # simulate runtime by detecting with actual object detector
  detect_start_time = time.time()
  img = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
  results = detector.return_predict(img)
  detect_end_time = time.time()
  print('Detect time: {}'.format(detect_end_time - detect_start_time))
  detect_time += detect_end_time - detect_start_time

  # write image (which will be classified by app)
  start_write_img = time.time()

  # use adb to pass image
  # cv2.imwrite('crop.jpg', cropped)
  # os.system('adb push crop.jpg /data/local/tmp/img{}.jpg > NUL 2>&1'.format(ind))
  
  # use socket to pass image
  _, img_bytes = cv2.imencode('.jpg', cropped)
  crop_height, crop_width, crop_channels = cropped.shape
  size = img_bytes.shape[0]
  client.sendall(ind.to_bytes(4, 'big'))
  client.sendall(crop_height.to_bytes(4, 'big'))
  client.sendall(crop_width.to_bytes(4, 'big'))
  client.sendall(size.to_bytes(4, 'big'))
  client.sendall(bytearray(img_bytes))

  end_write_img = time.time()
  print('Write time: {}'.format(end_write_img - start_write_img))
  write_img += end_write_img - start_write_img

  # keep reading until log 
  # print('reading for log return')
  while True:
    if not stdout_queue.empty():
      log_output = stdout_queue.get()
      log_str = log_output.decode('utf-8')
      # check if proper format otherwise ignore
      if len(log_str) > 0:
        try:
          # print('Log: {}'.format(log_str))
          id = "class_img: "
          index = log_str.index(id)
          class_name = log_str[index + len(id):]
          print('Class name: {}'.format(class_name))
          break
        except:
          pass

  end_time = time.time()
  print('Time taken: {}'.format(end_time - start_time))
  print('=============================================')
  total_time += end_time - start_time

print('Avg detect time: {}'.format(detect_time / NUM_IMAGES))
print('Avg write time: {}'.format(write_img / NUM_IMAGES))
print('Avg time: {}'.format(total_time / NUM_IMAGES))
print('Avg FPS: {}'.format(NUM_IMAGES / total_time))

# stdout_reader.join()

print('Close process')

client.close()
process.stdout.close()

exit()

