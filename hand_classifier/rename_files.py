import os
from images.split_data import check_and_create_dir

DIR_NAME = 'point_fore/'
files = os.listdir(DIR_NAME)

target_dir = 'point_foreward/'
for i, f in enumerate(files):
  check_and_create_dir(target_dir)
  target = os.path.join(target_dir, f[:3] + str(i) + f[-4:])
  os.rename(os.path.join(DIR_NAME, f), target)