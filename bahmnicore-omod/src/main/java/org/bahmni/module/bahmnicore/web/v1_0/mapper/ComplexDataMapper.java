package org.bahmni.module.bahmnicore.web.v1_0.mapper;

import org.openmrs.obs.ComplexData;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

@Component
public class ComplexDataMapper {

    public ComplexData getNullifiedComplexData(ComplexData complexData) {
        if(isNull(complexData)){
            return complexData;
        }
        ComplexData newComplexData = new ComplexData(complexData.getTitle(), null);
        newComplexData.setMimeType(complexData.getMimeType());
        newComplexData.setLength(complexData.getLength());
        return newComplexData;
    }

}
