package org.bahmni.module.bahmnicore.web.v1_0.utils;

import org.junit.Test;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.obs.ComplexData;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

public class ObsComplexDataUtilTest {
    @Test
    public void shouldMakeComplexDataAsNullWhenLoadComplexDataIsFalse() {
        ComplexData complexData = new ComplexData("title", "some object");
        complexData.setMimeType("mimeType");
        complexData.setLength((long) 10);
        BahmniObservation observation = new BahmniObservation();
        observation.setComplexData(complexData);

        ObsComplexDataUtil.makeComplexDataNull(observation, false);

        ComplexData actualComplexData = (ComplexData)observation.getComplexData();
        assertNull(actualComplexData.getData());
        assertEquals("title", actualComplexData.getTitle());
        assertEquals("mimeType", actualComplexData.getMimeType());
    }

    @Test
    public void shouldNotMakeComplexDataAsNullWhenLoadComplexDataIsTrue() {
        ComplexData complexData = new ComplexData("title", "some object");
        complexData.setMimeType("mimeType");
        complexData.setLength((long) 10);
        BahmniObservation observation = new BahmniObservation();
        observation.setComplexData(complexData);
        Collection<BahmniObservation> observations = Arrays.asList(observation);

        ObsComplexDataUtil.makeComplexDataNull(observations, true);

        ComplexData actualComplexData = (ComplexData)observation.getComplexData();
        assertEquals("some object", actualComplexData.getData());
        assertEquals("title", actualComplexData.getTitle());
        assertEquals("mimeType", actualComplexData.getMimeType());
    }

    @Test
    public void shouldLoadComplexDataWhenLoadComplexDataIsNull() {
        ComplexData complexData = new ComplexData("title", "some object");
        complexData.setMimeType("mimeType");
        complexData.setLength((long) 10);
        BahmniObservation observation = new BahmniObservation();
        observation.setComplexData(complexData);

        ObsComplexDataUtil.makeComplexDataNull(observation, null);

        ComplexData actualComplexData = (ComplexData)observation.getComplexData();
        assertEquals("some object", actualComplexData.getData());
        assertEquals("title", actualComplexData.getTitle());
        assertEquals("mimeType", actualComplexData.getMimeType());
    }

    @Test
    public void shouldReturnNullWhenComplexDataIsNullAndLoadComplexDataIsTrue() {
        ComplexData complexData = null;
        BahmniObservation observation = new BahmniObservation();
        observation.setComplexData(complexData);

        ObsComplexDataUtil.makeComplexDataNull(observation, true);

        ComplexData actualComplexData = (ComplexData)observation.getComplexData();

        assertNull(actualComplexData);
    }

    @Test
    public void shouldReturnNullWhenComplexDataIsNullAndLoadComplexDataIsFalse() {
        ComplexData complexData = null;
        BahmniObservation observation = new BahmniObservation();
        observation.setComplexData(complexData);
        Collection<BahmniObservation> observations = Arrays.asList(observation);

        ObsComplexDataUtil.makeComplexDataNull(observations, false);

        ComplexData actualComplexData = (ComplexData)observation.getComplexData();

        assertNull(actualComplexData);
    }

}