package com.rythusahayak.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComplaintReferenceTest {
    @Test
    void generatesUniqueReferencesInThePublicFormat() {
        Set<String> references = new HashSet<>();

        for (int index = 0; index < 64; index++) {
            String reference = ComplaintReference.generate();
            assertTrue(ComplaintReference.isValid(reference));
            references.add(reference);
        }

        assertEquals(64, references.size());
    }

    @Test
    void rejectsMalformedReferences() {
        assertFalse(ComplaintReference.isValid(null));
        assertFalse(ComplaintReference.isValid("RYT-20260230-1234567890ABCDEF"));
        assertFalse(ComplaintReference.isValid("RYT-20260228-1234567890ABCDEG"));
    }
}
