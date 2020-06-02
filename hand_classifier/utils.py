import tensorflow as tf
import os
import xml.etree.ElementTree as ET

def sparse_categorical_crossentropy_with_logits(y_true, y_pred):
     return tf.keras.losses.sparse_categorical_crossentropy(y_true, y_pred, True)
    
def load_classifier(model_name='model_simple'):
    return tf.keras.models.load_model(os.path.join(os.path.dirname(__file__), model_name), custom_objects={'sparse_categorical_crossentropy_with_logits': sparse_categorical_crossentropy_with_logits})

def get_class_labels():
    return [
    'curl',  
    'curl_45_out', 
    'curl_45_palm', 
    'curl_90_out', 
    'curl_90_palm', 
    'point_foreward', 
    'point_foreward_45_out', 
    'point_foreward_45_palm', 
    'point_foreward_90_out', 
    'point_foreward_90_palm',
    'point_up',
    'point_up_out',
    'point_up_palm',
    'point_up_thumb'
]

def read_xml(xml_file):
    boxes = []
    classes = []
    data = ET.parse(xml_file)
    root = data.getroot()
    objs = root.findall('object')
    for obj in objs:
        box = obj.find('bndbox')
        xmin = int(box.find('xmin').text)
        ymin = int(box.find('ymin').text)
        xmax = int(box.find('xmax').text)
        ymax = int(box.find('ymax').text)
        box = [xmin, ymin, xmax, ymax]
        boxes.append(box)
        
        name = obj.find('class_name')
        if name is None:
            name = obj.find('name')
        classes.append(name.text)
    return boxes, classes