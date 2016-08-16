package com.luthfihm.virtualtour.ar;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;

import java.io.Serializable;

/**
 * Created by luthfi on 8/14/16.
 */
public class TrainModel implements Serializable {
    private Mat image;
    private MatOfKeyPoint keyPoint;
    private Mat descriptor;
    private String objectId;
    private boolean enabled = true;

    public TrainModel() {
        image = null;
        keyPoint = null;
        descriptor = null;
        objectId = null;
    }

    public TrainModel(TrainModel trainModel) {
        this.image = trainModel.getImage();
        this.keyPoint = trainModel.getKeyPoint();
        this.descriptor = trainModel.getDescriptor();
        this.objectId = trainModel.getObjectId();
        this.enabled = trainModel.isEnabled();
    }
    public TrainModel(Mat image, MatOfKeyPoint keyPoint, Mat descriptor, String objectId, boolean enabled) {
        this.image = image;
        this.keyPoint = keyPoint;
        this.descriptor = descriptor;
        this.objectId = objectId;
        this.enabled = enabled;
    }

    public Mat getImage() {
        return image;
    }

    public void setImage(Mat image) {
        this.image = image;
    }

    public MatOfKeyPoint getKeyPoint() {
        return keyPoint;
    }

    public void setKeyPoint(MatOfKeyPoint keyPoint) {
        this.keyPoint = keyPoint;
    }

    public Mat getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(Mat descriptor) {
        this.descriptor = descriptor;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
