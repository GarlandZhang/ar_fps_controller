package com.example.ar_fps_run_mobilenet;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.Operator;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
//import org.tensorflow.lite.examples.classification.env.Logger;
//import org.tensorflow.lite.examples.classification.tflite.Classifier.Device;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;
import java.time.Duration;
import java.time.Instant;
import static org.tensorflow.lite.support.model.Model.Device.CPU;
import static org.tensorflow.lite.support.model.Model.Device.GPU;

public class MainActivity extends AppCompatActivity {

    Interpreter model;
    private List<String> labels;
    int imageSizeX = 224;
    int imageSizeY = 224;
    int numClasses = 14;

    private int[] outputShape = {1, numClasses};

    /** Input image TensorBuffer. */
    private TensorImage inputImageBuffer;

    /** Output probability TensorBuffer. */
    private TensorBuffer outputProbabilityBuffer;

    /** Processer to apply post processing of the output probability. */
    private TensorProcessor probabilityProcessor;

    private ImageProcessor imageProcessor =
            new ImageProcessor.Builder()
                    .add(new ResizeOp(imageSizeX, imageSizeY, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                    .add(getPreprocessNormalizeOp())
                    .build();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context context = getApplicationContext();
        final Activity activity = this;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
//                GpuDelegate delegate = new GpuDelegate();

                int i = 0;
                try {
                    MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(context, "model.tflite");
                    Interpreter.Options tfliteOptions = (new Interpreter.Options());//.addDelegate(delegate);

                    for (int t = 0; t < 1; t += 1) {
                        System.out.println("Thread num: " + t);
                        tfliteOptions.setNumThreads(t);

                        model = new Interpreter(tfliteModel, tfliteOptions);
                        labels = FileUtil.loadLabels(context, "labels.txt");

                        // Creates the input tensor.
                        inputImageBuffer = new TensorImage(DataType.FLOAT32);

                        // Creates the output tensor and its processor.
                        outputProbabilityBuffer = TensorBuffer.createFixedSize(outputShape, DataType.FLOAT32);

                        // Creates the post processor for the output probability.
                        probabilityProcessor = new TensorProcessor.Builder().build();

                        try {
                            System.out.println("Open socket connection");

                            ServerSocket serverSocket = new ServerSocket(34567);
                            Socket client = serverSocket.accept();
                            System.out.println("Client was accepted");
                            InputStream in = client.getInputStream();
                            DataInputStream data_in = new DataInputStream(in);

                            while (true) {

                                if (i == 101) break;

                                Instant start = Instant.now();

                                Instant imageStart = Instant.now();

                                // read in bytes for frame index
                                byte[] frame_ind_bytes = new byte[4];
                                data_in.readFully(frame_ind_bytes);
                                ByteBuffer wrapped = ByteBuffer.wrap(frame_ind_bytes);
                                int frame_ind = wrapped.getInt();

                                System.out.println("Frame index: " + frame_ind);

                                byte[] height_bytes = new byte[4];
                                data_in.readFully(height_bytes);
                                wrapped = ByteBuffer.wrap(height_bytes);
                                int height = wrapped.getInt();

                                byte[] width_bytes = new byte[4];
                                data_in.readFully(width_bytes);
                                wrapped = ByteBuffer.wrap(width_bytes);
                                int width = wrapped.getInt();

                                int channels = 3;

                                byte[] size_bytes = new byte[4];
                                data_in.readFully(size_bytes);
                                wrapped = ByteBuffer.wrap(size_bytes);
                                int size = wrapped.getInt();

                                System.out.println("Size: " + size);

                                byte[] img_bytes = new byte[size];
                                data_in.read(img_bytes);

//                                String image_path;
//                                File image_file;

//                                try {
////                        ims = getAssets().open("images/test" + i + ".jpg");
//                                    image_path = "/data/local/tmp/img" + i + ".jpg";
//                                    System.out.println("Image path: " + image_path);
//                                    image_file = new File(image_path);
//                                    if (!image_file.exists()) {
//                                        System.out.println("No image (file does not exist)");
//                                        continue;
//                                    }
//                                } catch (Exception e) {
//                                    System.out.println("No image");
//                                    continue;
//                                }
//                Instant imageEnd = Instant.now();
//                Duration imageTimeElapsed = Duration.between(imageStart, imageEnd);
//                System.out.println("Image retrieval time taken: " + imageTimeElapsed.toMillis() + " milliseconds");

//                    Bitmap bitmap = BitmapFactory.decodeStream(ims);
//                                Bitmap bitmap = BitmapFactory.decodeFile(image_file.getAbsolutePath());

                                Bitmap bitmap = BitmapFactory.decodeByteArray(img_bytes, 0, size);

                                try {
                                    inputImageBuffer.load(bitmap);
                                } catch (Exception e) {
                                    System.out.println("Cant load bitmap");
                                    continue;
                                }
                                inputImageBuffer = imageProcessor.process(inputImageBuffer);
                                Instant imageEnd = Instant.now();
                                Duration imageTimeElapsed = Duration.between(imageStart, imageEnd);
                                System.out.println("Image retrieval time taken: " + imageTimeElapsed.toMillis() + " milliseconds");

                                Instant modelStart = Instant.now();
                                model.run(inputImageBuffer.getBuffer(), outputProbabilityBuffer.getBuffer().rewind());
                                Instant modelEnd = Instant.now();
                                Duration modelTimeElapsed = Duration.between(modelStart, modelEnd);
                                System.out.println("Model time taken: " + modelTimeElapsed.toMillis() + " milliseconds");

                                Map<String, Float> labeledProbability = new TensorLabel(labels, probabilityProcessor.process(outputProbabilityBuffer))
                                        .getMapWithFloatValue();

                                String map_key = "";
                                float max_prob = 0;

                                for (Map.Entry<String, Float> entry : labeledProbability.entrySet()) {
                                    if (entry.getValue() > max_prob) {
                                        map_key = entry.getKey();
                                        max_prob = entry.getValue();
                                    }
                                }

                                Instant end = Instant.now();
                                Duration timeElapsed = Duration.between(start, end);
                                System.out.println("Total time taken: " + timeElapsed.toMillis() + " milliseconds");

//                    Log.d("class_img", map_key + " for image " + i);
                                Log.d("class_img", map_key);
                                i += 1;
                            }

                            serverSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
                // Clean up
                //delegate.close();
            }
        });

        thread.start();
        System.out.println("Done");
    }

    private static final float PROBABILITY_MEAN = 0.0f;

    private static final float PROBABILITY_STD = 1.0f;

    private Operator<TensorBuffer> getPostprocessNormalizeOp() {
        return new NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD);
    }

    private static final float IMAGE_MEAN = 127.5f;

    private static final float IMAGE_STD = 127.5f;


    private TensorOperator getPreprocessNormalizeOp() {
        return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
    }
}
