package org.bahmni.module.bahmnicore.web.v1_0.mapper;

import org.junit.Test;
import org.openmrs.obs.ComplexData;

import static org.junit.Assert.*;

public class ComplexDataMapperTest {

    @Test
    public void shouldMakeComplexDataAsNull() {
        ComplexDataMapper complexDataMapper = new ComplexDataMapper();
        ComplexData complexData = new ComplexData("title", "some object");
        complexData.setMimeType("mimeType");
        complexData.setLength((long) 10);

        ComplexData newComplexData = complexDataMapper.getNullifiedComplexData(complexData);

        assertEquals(null, newComplexData.getData());
        assertEquals("title", newComplexData.getTitle());
        assertEquals("mimeType", newComplexData.getMimeType());
    }

    @Test
    public void shouldReturnNullIfComplexDataIsNull() {
        ComplexDataMapper complexDataMapper = new ComplexDataMapper();
        ComplexData complexData = null;

        ComplexData newComplexData = complexDataMapper.getNullifiedComplexData(complexData);

        assertEquals(null, newComplexData);
    }
}