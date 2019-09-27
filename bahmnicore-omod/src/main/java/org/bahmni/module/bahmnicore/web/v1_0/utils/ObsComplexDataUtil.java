package org.bahmni.module.bahmnicore.web.v1_0.utils;

import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.obs.ComplexData;

import java.util.Collection;

import static java.util.Objects.isNull;

public class ObsComplexDataUtil {

    public static void makeComplexDataNull(Collection<BahmniObservation> observations, Boolean loadComplexData) {
        if (!isNull(loadComplexData) && !loadComplexData) {
            observations.forEach(observation -> {
               makeComplexDataNull(observation, loadComplexData);
            });
        }
    }

    public static void makeComplexDataNull(BahmniObservation observation, Boolean loadComplexData) {
        if (!isNull(loadComplexData) && !loadComplexData) {
            if (!isNull(observation.getGroupMembers()) && observation.getGroupMembers().size() > 0) {
                makeComplexDataNull(observation.getGroupMembers(), loadComplexData);
            }
            observation.setComplexData(getNullifiedComplexData((ComplexData) observation.getComplexData()));
        }

    }

    private static ComplexData getNullifiedComplexData(ComplexData complexData) {
        if(isNull(complexData)){
            return complexData;
        }
        ComplexData newComplexData = new ComplexData(complexData.getTitle(), null);
        newComplexData.setMimeType(complexData.getMimeType());
        newComplexData.setLength(complexData.getLength());
        return newComplexData;
    }



}
