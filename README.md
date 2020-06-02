# ar_fps_controller

The goal of this project is to create an interface to fps gaming by interacting with your hands instead using peripherals like mouse and keyboard. Through object detection and classification, we can interpret hand gestures and replicate the corresponding actions in game.

Main features:

- Object detection using YOLO
- Hand classification using Mobilenet
- Android app to run mobilenet for extra computation power
- Hand gesture interpretation and in-game controls 

Main goals:

- Object detector can kind of find hands...
- Hand gesture accuracy 97%+ (on average over all test sets)
- 10 fps end to end (webcam -> object detection -> cropped hand candidate to android app over socket connection -> hand classification -> classification sent back) on a 30fps webcam.

Sources:

https://github.com/victordibia/handtracking

https://github.com/thtrieu/darkflow/tree/master/darkflow

https://github.com/ferhat00/Deep-Learning/tree/master/Transfer%20Learning%20CNN

https://www.stefaanlippens.net/python-asynchronous-subprocess-pipe-reading/
