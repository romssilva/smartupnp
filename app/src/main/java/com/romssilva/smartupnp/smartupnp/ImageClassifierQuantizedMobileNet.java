package com.romssilva.smartupnp.smartupnp;

/**
 * Created by romssilva on 2018-04-22.
 */

import android.app.Activity;

import java.io.IOException;

/**
 * This classifier works with the quantized MobileNet model.
 */
public class ImageClassifierQuantizedMobileNet extends ImageClassifier {

    /**
     * An array to hold inference results, to be feed into Tensorflow Lite as outputs.
     * This isn't part of the super class, because we need a primitive array here.
     */
    private byte[][] labelProbArray = null;

    /**
     * Initializes an {@code ImageClassifier}.
     *
     * @param activity
     */
    ImageClassifierQuantizedMobileNet(Activity activity) throws IOException {
        super(activity);
        labelProbArray = new byte[1][getNumLabels()];
    }

    @Override
    protected String getModelPath() {
        // you can download this file from
        // https://storage.googleapis.com/download.tensorflow.org/models/tflite/mobilenet_v1_224_android_quant_2017_11_08.zip
//        return "mobilenet_quant_v1_224.tflite";
//        return "optimized_graph.lite";
        return "mobilenet_v1_1.0_224.lite";

    }

    @Override
    protected String getLabelPath() {
//        return "labels_mobilenet_quant_v1_224.txt";
        return "retrained_labels.txt";
    }

    @Override
    protected int getImageSizeX() {
        return 224;
    }

    @Override
    protected int getImageSizeY() {
        return 224;
    }

    @Override
    protected int getNumBytesPerChannel() {
        // the quantized model uses a single byte only
        return 1;
    }

    @Override
    protected void addPixelValue(int pixelValue) {
        imgData.put((byte) ((pixelValue >> 16) & 0xFF));
        imgData.put((byte) ((pixelValue >> 8) & 0xFF));
        imgData.put((byte) (pixelValue & 0xFF));
    }

    @Override
    protected float getProbability(int labelIndex) {
        return labelProbArray[0][labelIndex];
    }

    @Override
    protected void setProbability(int labelIndex, Number value) {
        labelProbArray[0][labelIndex] = value.byteValue();
    }

    @Override
    protected float getNormalizedProbability(int labelIndex) {
        return (labelProbArray[0][labelIndex] & 0xff) / 255.0f;
    }

    @Override
    protected void runInference() {
        tflite.run(imgData, labelProbArray);
    }
}

