package com.bfhl.service;

import com.bfhl.dto.BfhlRequest;
import com.bfhl.dto.BfhlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BfhlServiceImplTest {

    private BfhlServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BfhlServiceImpl();
    }

    private BfhlRequest req(String... items) {
        BfhlRequest r = new BfhlRequest();
        r.setData(Arrays.asList(items));
        return r;
    }

    private BfhlRequest reqWithNulls(List<String> items) {
        BfhlRequest r = new BfhlRequest();
        r.setData(items);
        return r;
    }

    // ── Example 1 ────────────────────────────────────────────────────────────
    @Test
    void example1_basicInput() {
        BfhlResponse res = service.processData(req("A", "1", "22", "$", "B", "7"), "REQ-1001");

        assertTrue(res.isSuccess());
        assertEquals("REQ-1001", res.getRequestId());
        assertEquals(List.of("1", "7"), res.getOddNumbers());
        assertEquals(List.of("22"), res.getEvenNumbers());
        assertEquals(List.of("A", "B"), res.getAlphabets());
        assertEquals(List.of("$"), res.getSpecialCharacters());
        assertEquals("30", res.getSum());
        assertEquals("22", res.getLargestNumber());
        assertEquals("1", res.getSmallestNumber());
        assertEquals(2, res.getAlphabetCount());
        assertEquals(3, res.getNumberCount());
        assertEquals(1, res.getSpecialCharacterCount());
        assertFalse(res.isContainsDuplicates());
        assertEquals(List.of("1", "7", "22"), res.getSortedNumbers());
    }

    // ── Example 2 – alphanumeric strings ─────────────────────────────────────
    @Test
    void example2_alphanumericStrings() {
        BfhlResponse res = service.processData(
                req("A1B2", "100", "#", "Test123", "Z", "55"), "REQ-1002");

        assertTrue(res.isSuccess());
        assertEquals(List.of("55"), res.getOddNumbers());
        assertEquals(List.of("100"), res.getEvenNumbers());
        // A and B from A1B2; T,E,S,T from Test123; Z standalone
        assertTrue(res.getAlphabets().containsAll(List.of("A", "B", "T", "E", "S", "Z")));
        assertEquals(7, res.getAlphabetCount());
        assertEquals(List.of("#"), res.getSpecialCharacters());
        assertEquals("155", res.getSum());
        assertEquals("100", res.getLargestNumber());
        assertEquals("55", res.getSmallestNumber());
        assertEquals(2, res.getNumberCount());
        assertFalse(res.isContainsDuplicates());
    }

    // ── Example 3 – duplicates, null, empty ──────────────────────────────────
    @Test
    void example3_duplicatesNullAndEmpty() {
        List<String> data = Arrays.asList("10", "10", "A", "A", "", null, "&", "5");
        BfhlResponse res = service.processData(reqWithNulls(data), "REQ-1003");

        assertTrue(res.isContainsDuplicates());
        assertEquals(List.of("5"), res.getOddNumbers());
        assertEquals(List.of("10"), res.getEvenNumbers());
        assertEquals(List.of("A"), res.getAlphabets());
        assertEquals(List.of("&"), res.getSpecialCharacters());
        assertEquals("15", res.getSum());
        assertEquals("10", res.getLargestNumber());
        assertEquals("5", res.getSmallestNumber());
        assertEquals(1, res.getAlphabetCount());
        assertEquals(2, res.getNumberCount());
        // 4 unique valid elements: 10, A, &, 5
        assertEquals(4, res.getUniqueElementCount());
        // summary
        assertEquals(8, res.getSummary().getTotalElementsReceived());
        assertEquals(6, res.getSummary().getValidElementsProcessed());
        assertEquals(2, res.getSummary().getInvalidElementsIgnored());
    }

    // ── Example 4 – negatives and decimals ───────────────────────────────────
    @Test
    void example4_negativeAndDecimalNumbers() {
        BfhlResponse res = service.processData(
                req("-10", "25.5", "-100.75", "B", "@", "5", "A9"), "REQ-1004");

        assertEquals(List.of("5"), res.getOddNumbers());
        assertEquals(List.of("-10"), res.getEvenNumbers());
        // decimals must NOT appear in odd/even
        assertFalse(res.getOddNumbers().contains("25.5"));
        assertFalse(res.getEvenNumbers().contains("-100.75"));

        assertEquals("-80.25", res.getSum());
        assertEquals("25.5", res.getLargestNumber());
        assertEquals("-100.75", res.getSmallestNumber());
        assertEquals(4, res.getNumberCount());
        assertFalse(res.isContainsDuplicates());

        // A extracted from alphanumeric "A9"
        assertTrue(res.getAlphabets().contains("A"));
        // B from standalone "B"
        assertTrue(res.getAlphabets().contains("B"));
    }

    // ── Sorted numbers ────────────────────────────────────────────────────────
    @Test
    void sortedNumbers_ascendingOrder() {
        BfhlResponse res = service.processData(req("100", "-50", "0", "25"), "REQ-SORT");

        assertEquals(List.of("-50", "0", "25", "100"), res.getSortedNumbers());
    }

    // ── Empty data array ──────────────────────────────────────────────────────
    @Test
    void emptyDataArray_returnsZeroCountsAndSuccessTrue() {
        BfhlResponse res = service.processData(req(), "REQ-EMPTY");

        assertTrue(res.isSuccess());
        assertEquals("0", res.getSum());
        assertEquals(0, res.getNumberCount());
        assertEquals(0, res.getAlphabetCount());
        assertEquals(0, res.getSpecialCharacterCount());
        assertFalse(res.isContainsDuplicates());
        assertEquals(0, res.getUniqueElementCount());
        assertTrue(res.getSortedNumbers().isEmpty());
        assertNull(res.getLargestNumber());
        assertNull(res.getSmallestNumber());
        assertEquals(0, res.getSummary().getTotalElementsReceived());
    }

    // ── All invalid elements ──────────────────────────────────────────────────
    @Test
    void allInvalidElements_summaryReflectsIgnored() {
        List<String> data = Arrays.asList(null, "", "   ", null);
        BfhlResponse res = service.processData(reqWithNulls(data), "REQ-INVALID");

        assertTrue(res.isSuccess());
        assertEquals(4, res.getSummary().getTotalElementsReceived());
        assertEquals(0, res.getSummary().getValidElementsProcessed());
        assertEquals(4, res.getSummary().getInvalidElementsIgnored());
        assertEquals(0, res.getNumberCount());
    }

    // ── Vowel counting ────────────────────────────────────────────────────────
    @Test
    void vowelCount_correctlyIdentifiesVowels() {
        BfhlResponse res = service.processData(req("ABC", "xyz", "AEIOU"), "REQ-VOWEL");

        // ABC: A=vowel, B,C=consonants
        // xyz → XYZ: X,Y,Z=consonants
        // AEIOU: all vowels
        assertEquals(6, res.getVowelCount()); // A from ABC + A,E,I,O,U from AEIOU
    }

    // ── Alphabet frequency ────────────────────────────────────────────────────
    @Test
    void alphabetFrequency_countsCorrectly() {
        BfhlResponse res = service.processData(req("A", "AB", "A1B2"), "REQ-FREQ");

        // "A" → A
        // "AB" → pure alpha → AB uppercase = AB → chars A,B
        // "A1B2" → mixed → A,B individually
        // Total A: A (from "A") + A (from "AB") + A (from "A1B2") = 3
        // Total B: B (from "AB") + B (from "A1B2") = 2
        assertEquals(3, res.getAlphabetFrequency().get("A"));
        assertEquals(2, res.getAlphabetFrequency().get("B"));
    }

    // ── Duplicate detection ───────────────────────────────────────────────────
    @Test
    void duplicateDetection_marksCorrectly() {
        BfhlResponse res = service.processData(req("X", "Y", "X", "Z"), "REQ-DUP");

        assertTrue(res.isContainsDuplicates());
        // After dedup: X, Y, Z
        assertEquals(3, res.getUniqueElementCount());
        assertEquals(List.of("X", "Y", "Z"), res.getAlphabets());
    }

    // ── Special characters only ───────────────────────────────────────────────
    @Test
    void specialCharactersOnly() {
        BfhlResponse res = service.processData(req("$", "#", "@", "%"), "REQ-SPEC");

        assertEquals(4, res.getSpecialCharacterCount());
        assertEquals(0, res.getNumberCount());
        assertEquals(0, res.getAlphabetCount());
        assertTrue(res.getOddNumbers().isEmpty());
        assertTrue(res.getEvenNumbers().isEmpty());
    }

    // ── Longest and shortest alphabetic values ────────────────────────────────
    @Test
    void longestAndShortestAlphabeticValue() {
        BfhlResponse res = service.processData(req("HELLO", "AB", "Z"), "REQ-LEN");

        assertEquals("HELLO", res.getLongestAlphabeticValue());
        assertEquals("Z", res.getShortestAlphabeticValue());
    }

    // ── Zero is even ──────────────────────────────────────────────────────────
    @Test
    void zero_isEven() {
        BfhlResponse res = service.processData(req("0"), "REQ-ZERO");

        assertTrue(res.getEvenNumbers().contains("0"));
        assertFalse(res.getOddNumbers().contains("0"));
    }

    // ── Negative numbers odd/even ─────────────────────────────────────────────
    @Test
    void negativeNumbers_oddEvenClassification() {
        BfhlResponse res = service.processData(req("-3", "-4"), "REQ-NEG");

        assertTrue(res.getOddNumbers().contains("-3"));
        assertTrue(res.getEvenNumbers().contains("-4"));
    }

    // ── Request ID echo ───────────────────────────────────────────────────────
    @Test
    void requestId_isEchoedInResponse() {
        BfhlResponse res = service.processData(req("1"), "MY-CUSTOM-ID");

        assertEquals("MY-CUSTOM-ID", res.getRequestId());
    }

    // ── Processing time is set ────────────────────────────────────────────────
    @Test
    void processingTimeMs_isSetAndNonNegative() {
        BfhlResponse res = service.processData(req("1", "A"), "REQ-TIME");

        assertTrue(res.getProcessingTimeMs() >= 0);
    }

    // ── Summary object ────────────────────────────────────────────────────────
    @Test
    void summaryObject_countsMatchInput() {
        List<String> data = Arrays.asList("1", "A", null, "", "2");
        BfhlResponse res = service.processData(reqWithNulls(data), "REQ-SUMM");

        assertEquals(5, res.getSummary().getTotalElementsReceived());
        assertEquals(3, res.getSummary().getValidElementsProcessed());
        assertEquals(2, res.getSummary().getInvalidElementsIgnored());
    }
}
