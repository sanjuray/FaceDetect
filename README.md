This app is built to detect faces in an image using MLkit dependency of 
face detection. The UI has camerabtn for intent to startActivity for result 
from MediaStore. ACTION_IMAGE_CAPTURE to request image capture. Then we use
onActivityResult() to get the image from intent extras and process data using FaceDetector.
Using FaceDetectorOptions we shall set the requirements of the model which Performance Mode is to
be used and LandMarkMode, and Classification Mode then we pass on the FaceDetectorOptions to FaceDetection.getClient()
to get FaceDetector object. Using this we process and get the properties of each face and displayed on a textView.

<video src="https://github.com/sanjuray/FaceDetect/assets/94555333/a121ef98-813c-4d5a-83d3-30aedea7e6e5" width=450 height=550/>

