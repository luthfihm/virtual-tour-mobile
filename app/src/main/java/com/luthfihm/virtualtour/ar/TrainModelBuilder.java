package com.luthfihm.virtualtour.ar;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by luthfi on 8/14/16.
 */
public class TrainModelBuilder {

    private FeatureDetector detector;
    private DescriptorExtractor descriptorExtractor;

    private List<TrainModel> trainModels;

    public TrainModelBuilder(File trainDir){
        File[] files = trainDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                return name.endsWith(".jpg") || name.endsWith(".jpeg");
            }
        });

        detector = FeatureDetector.create(FeatureDetector.ORB);
        descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);

        trainModels = new ArrayList<>();

        for (File file : files) {
            Mat fullSizeTrainImg = Imgcodecs.imread(file.getAbsolutePath());
            Mat resizedTrainImg = new Mat();
            Imgproc.resize(fullSizeTrainImg, resizedTrainImg, new Size(640, 480), 0, 0, Imgproc.INTER_CUBIC);

            String filename = file.getName().substring(0, file.getName().lastIndexOf("."));

            MatOfKeyPoint keyPoint = new MatOfKeyPoint();
            Mat descriptor = new Mat();

            detector.detect(resizedTrainImg, keyPoint);
            descriptorExtractor.compute(resizedTrainImg, keyPoint, descriptor);

            TrainModel trainModel = new TrainModel();
            trainModel.setImage(resizedTrainImg);
            trainModel.setKeyPoint(keyPoint);
            trainModel.setDescriptor(descriptor);
            trainModel.setObjectId(filename);
            trainModels.add(trainModel);
        }
    }

    public List<TrainModel> getTrainModels() {
        return trainModels;
    }
}
