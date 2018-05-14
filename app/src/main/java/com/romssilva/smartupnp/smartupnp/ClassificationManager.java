package com.romssilva.smartupnp.smartupnp;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by romssilva on 2018-04-25.
 */

public class ClassificationManager {

    private HashMap<String, Float> classifications;
    private int classificationCount;

    public ClassificationManager() {
        classifications = new HashMap<>();
        classificationCount = 0;
    }

    public void addOrUpdateClassification(String key, Float value) {
        if (classifications.containsKey(key)) {
            classifications.put(key, classifications.get(key) + value);
        } else {
            classifications.put(key, value);
        }
        classificationCount++;
    }

    public int getClassificationCount() {
        return classificationCount;
    }

    public int getClassifiedItensCount() {
        return classifications.size();
    }

    public void resetClassifications() {
        classifications.clear();
        classificationCount = 0;
    }

    public Map.Entry<String, Float> getMostLikelyClass() {
        Set<String> keys = classifications.keySet();
        String mostLikelyClass = null;
        Float maxValue = new Float(0);

        for (String key : keys) {
            if (classifications.get(key) > maxValue) {
                maxValue = classifications.get(key);
                mostLikelyClass = key;
            }
        }

        final String finalMostLikelyClass = mostLikelyClass;
        final Float finalMaxValue = maxValue;
        return new Map.Entry<String, Float>() {
            @Override
            public String getKey() {
                return finalMostLikelyClass;
            }

            @Override
            public Float getValue() {
                return finalMaxValue;
            }

            @Override
            public Float setValue(Float aFloat) {
                return null;
            }
        };
    }
}
