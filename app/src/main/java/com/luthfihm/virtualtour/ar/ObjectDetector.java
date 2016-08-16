package com.luthfihm.virtualtour.ar;

import android.util.Log;

import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by luthfi on 8/14/16.
 */
public class ObjectDetector {
    private DescriptorMatcher matcher;
    private List<TrainModel> trainModels;
    private FeatureDetector detector;
    private DescriptorExtractor descriptorExtractor;
    private ArrayList<Mat> trainDescriptors;

    // Parameters for matching
    public static final double RATIO_TEST_RATIO = 0.92;
    public static final int RATIO_TEST_MIN_NUM_MATCHES = 32;

    public ObjectDetector(List<TrainModel> trainModels) {
        this.trainModels = trainModels;

        detector = FeatureDetector.create(FeatureDetector.ORB);
        descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);

        trainDescriptors = new ArrayList<Mat>();

        for (TrainModel trainModel: trainModels) {
            trainDescriptors.add(trainModel.getDescriptor());
        }

        matcher.add(trainDescriptors);
        matcher.train();
    }

    public List<TrainModel> getTrainModels() {
        return trainModels;
    }

    public int recognize(Mat sceneGray) {
        MatOfKeyPoint sceneKeyPoint = new MatOfKeyPoint();
        Mat sceneDescriptor = new Mat();
        List<MatOfDMatch> matches = new LinkedList<MatOfDMatch>();

        detector.detect(sceneGray, sceneKeyPoint);
        descriptorExtractor.compute(sceneGray, sceneKeyPoint, sceneDescriptor);

        LinkedList<MatOfDMatch> knnMatches = new LinkedList<MatOfDMatch>();
        DMatch bestMatch, secondBestMatch;

        matcher.knnMatch(sceneDescriptor, knnMatches, 2);
        for (MatOfDMatch matOfDMatch : knnMatches) {
            bestMatch = matOfDMatch.toArray()[0];
            secondBestMatch = matOfDMatch.toArray()[1];
            if (bestMatch.distance / secondBestMatch.distance <= RATIO_TEST_RATIO) {
                MatOfDMatch goodMatch = new MatOfDMatch();
                goodMatch.fromArray(new DMatch[] { bestMatch });
                matches.add(goodMatch);
            }
        }

        int numMatchesInImage[] = new int[trainModels.size()];
        int matchIndex = -1;
        int numMatches = 0;

        for (MatOfDMatch matOfDMatch : matches) {
            DMatch[] dMatch = matOfDMatch.toArray();
            boolean[] imagesMatched = new boolean[trainModels.size()];
            for (int i = 0; i < dMatch.length; i++) {
                if (!imagesMatched[dMatch[i].imgIdx]) {
                    numMatchesInImage[dMatch[i].imgIdx]++;
                    imagesMatched[dMatch[i].imgIdx] = true;
                }
            }
        }
        for (int i = 0; i < numMatchesInImage.length; i++) {
            if (numMatchesInImage[i] > numMatches) {
                matchIndex = i;
                numMatches = numMatchesInImage[i];
            }
        }
        if (numMatches < RATIO_TEST_MIN_NUM_MATCHES) {
            return -1;
        } else {
            return matchIndex;
        }
    }
}
