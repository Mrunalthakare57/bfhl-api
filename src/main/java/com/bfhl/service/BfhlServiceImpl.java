package com.bfhl.service;

import com.bfhl.dto.BfhlRequest;
import com.bfhl.dto.BfhlResponse;
import com.bfhl.dto.Summary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class BfhlServiceImpl implements BfhlService {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("^-?\\d+(\\.\\d+)?$");
    private static final Set<Character> VOWELS = Set.of('A', 'E', 'I', 'O', 'U');

    @Override
    public BfhlResponse processData(BfhlRequest request, String requestId) {
        long startTime = System.currentTimeMillis();
        log.info("Processing request id={}", requestId);

        List<String> data = request.getData();
        int totalReceived = data.size();

        // Step 1: filter invalid (null, empty, whitespace-only)
        List<String> validElements = data.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .collect(Collectors.toList());
        int validCount = validElements.size();
        int invalidCount = totalReceived - validCount;

        // Step 2: detect duplicates and deduplicate
        Set<String> seen = new LinkedHashSet<>();
        boolean hasDuplicates = false;
        for (String s : validElements) {
            if (!seen.add(s)) {
                hasDuplicates = true;
            }
        }
        List<String> uniqueElements = new ArrayList<>(seen);
        int uniqueCount = uniqueElements.size();

        // Step 3: categorise each unique element
        List<String> numberStrings = new ArrayList<>();
        List<BigDecimal> numberValues = new ArrayList<>();
        List<String> alphabets = new ArrayList<>();
        List<String> specialCharacters = new ArrayList<>();

        for (String element : uniqueElements) {
            if (isNumeric(element)) {
                numberStrings.add(element);
                numberValues.add(new BigDecimal(element));
            } else if (isPureAlpha(element)) {
                alphabets.add(element.toUpperCase());
            } else if (isPureSpecial(element)) {
                specialCharacters.add(element);
            } else {
                // Mixed (alphanumeric or alpha+special):
                // extract each alpha character individually (uppercase)
                for (char c : element.toCharArray()) {
                    if (Character.isLetter(c)) {
                        alphabets.add(String.valueOf(Character.toUpperCase(c)));
                    }
                }
                // numeric segments from mixed strings are intentionally excluded
                // from number processing (sum, odd/even, count) per spec examples
            }
        }

        // Step 4: compute number stats
        List<String> oddNumbers = new ArrayList<>();
        List<String> evenNumbers = new ArrayList<>();
        BigDecimal sum = BigDecimal.ZERO;

        for (int i = 0; i < numberValues.size(); i++) {
            BigDecimal n = numberValues.get(i);
            String ns = numberStrings.get(i);
            sum = sum.add(n);

            // only whole numbers go into odd/even buckets
            if (n.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                BigInteger intVal = n.toBigInteger();
                if (intVal.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
                    evenNumbers.add(ns);
                } else {
                    oddNumbers.add(ns);
                }
            }
        }

        String sumStr = numberValues.isEmpty() ? "0"
                : sum.stripTrailingZeros().toPlainString();

        // largest and smallest: keep original string, compare by value
        String largestStr = null;
        String smallestStr = null;
        if (!numberValues.isEmpty()) {
            BigDecimal maxVal = Collections.max(numberValues);
            BigDecimal minVal = Collections.min(numberValues);
            for (int i = 0; i < numberValues.size(); i++) {
                if (largestStr == null && numberValues.get(i).compareTo(maxVal) == 0) {
                    largestStr = numberStrings.get(i);
                }
                if (smallestStr == null && numberValues.get(i).compareTo(minVal) == 0) {
                    smallestStr = numberStrings.get(i);
                }
            }
        }

        // sorted numbers ascending (keep original string form, order by value)
        List<String> sortedNumbers = IntStream.range(0, numberStrings.size())
                .boxed()
                .sorted(Comparator.comparing(numberValues::get))
                .map(numberStrings::get)
                .collect(Collectors.toList());

        // Step 5: alphabet stats
        int alphabetCount = alphabets.size();
        Map<String, Integer> alphabetFrequency = new LinkedHashMap<>();
        int vowelCount = 0;
        int consonantCount = 0;

        for (String alpha : alphabets) {
            for (char c : alpha.toCharArray()) {
                char upper = Character.toUpperCase(c);
                alphabetFrequency.merge(String.valueOf(upper), 1, Integer::sum);
                if (VOWELS.contains(upper)) {
                    vowelCount++;
                } else {
                    consonantCount++;
                }
            }
        }

        // longest / shortest alphabetic value
        String longestAlpha = alphabets.stream()
                .max(Comparator.comparingInt(String::length))
                .orElse(null);
        String shortestAlpha = alphabets.stream()
                .min(Comparator.comparingInt(String::length))
                .orElse(null);

        long processingTimeMs = System.currentTimeMillis() - startTime;
        log.info("Request id={} completed in {}ms", requestId, processingTimeMs);

        return BfhlResponse.builder()
                .success(true)
                .requestId(requestId)
                .oddNumbers(oddNumbers)
                .evenNumbers(evenNumbers)
                .alphabets(alphabets)
                .specialCharacters(specialCharacters)
                .sum(sumStr)
                .largestNumber(largestStr)
                .smallestNumber(smallestStr)
                .alphabetCount(alphabetCount)
                .numberCount(numberValues.size())
                .specialCharacterCount(specialCharacters.size())
                .containsDuplicates(hasDuplicates)
                .processingTimeMs(processingTimeMs)
                .uniqueElementCount(uniqueCount)
                .sortedNumbers(sortedNumbers)
                .vowelCount(vowelCount)
                .consonantCount(consonantCount)
                .alphabetFrequency(alphabetFrequency)
                .longestAlphabeticValue(longestAlpha)
                .shortestAlphabeticValue(shortestAlpha)
                .summary(new Summary(totalReceived, validCount, invalidCount))
                .build();
    }

    private boolean isNumeric(String s) {
        return NUMBER_PATTERN.matcher(s).matches();
    }

    private boolean isPureAlpha(String s) {
        return !s.isEmpty() && s.chars().allMatch(Character::isLetter);
    }

    private boolean isPureSpecial(String s) {
        return !s.isEmpty() && s.chars().noneMatch(Character::isLetterOrDigit);
    }
}
