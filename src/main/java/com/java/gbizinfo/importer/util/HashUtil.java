package com.java.gbizinfo.importer.util;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class HashUtil {

    private static final String NULL_TOKEN = "<NULL>";

    private HashUtil() {
    }

    public static String md5(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    }

    public static String mergeKey(String... parts) {
        return md5(Stream.of(parts).collect(Collectors.joining("|")));
    }

    public static String normText(String value) {
        if (value == null) {
            return NULL_TOKEN;
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.isEmpty() ? NULL_TOKEN : normalized;
    }

    public static String normBool(Boolean value) {
        if (value == null) {
            return NULL_TOKEN;
        }
        return value ? "true" : "false";
    }

    public static String normInt(Integer value) {
        return value == null ? NULL_TOKEN : value.toString();
    }

    public static String normLong(Long value) {
        return value == null ? NULL_TOKEN : value.toString();
    }

    public static String normNumber(Number value) {
        if (value == null) {
            return NULL_TOKEN;
        }
        if (value instanceof BigDecimal bd) {
            return bd.stripTrailingZeros().toPlainString();
        }
        if (value instanceof Double || value instanceof Float) {
            return BigDecimal.valueOf(value.doubleValue()).stripTrailingZeros().toPlainString();
        }
        return String.valueOf(value);
    }

    public static String normDate(String value) {
        return normText(value);
    }

    public static String normTimestamp(String value) {
        return normText(value);
    }

    public static String itemMergeKey(String value, Boolean isIndustry) {
        return mergeKey(normText(value), normBool(isIndustry));
    }

    public static String patentMergeKey(String patentType,
                                        String registrationNumber,
                                        String applicationDate,
                                        String title,
                                        String url) {
        return mergeKey(
                normText(patentType),
                normText(registrationNumber),
                normDate(applicationDate),
                normText(title),
                normText(url)
        );
    }

    public static String classificationMergeKey(String codeValue,
                                                String codeName,
                                                String japanese) {
        return mergeKey(
                normText(codeValue),
                normText(codeName),
                normText(japanese)
        );
    }

    public static String certificationMergeKey(String dateOfApproval,
                                               String title,
                                               String target,
                                               String governmentDepartments,
                                               String category) {
        return mergeKey(
                normDate(dateOfApproval),
                normText(title),
                normText(target),
                normText(governmentDepartments),
                normText(category)
        );
    }

    public static String subsidyMergeKey(String dateOfApproval,
                                         String title,
                                         String amount,
                                         String target,
                                         String governmentDepartments) {
        return mergeKey(
                normDate(dateOfApproval),
                normText(title),
                normText(amount),
                normText(target),
                normText(governmentDepartments)
        );
    }

    public static String commendationMergeKey(String dateOfCommendation,
                                              String title,
                                              String target,
                                              String category,
                                              String governmentDepartments,
                                              String note) {
        return mergeKey(
                normDate(dateOfCommendation),
                normText(title),
                normText(target),
                normText(category),
                normText(governmentDepartments),
                normText(note)
        );
    }

    public static String procurementMergeKey(String dateOfOrder,
                                             String title,
                                             Long amount,
                                             String governmentDepartments,
                                             String note) {
        return mergeKey(
                normTimestamp(dateOfOrder),
                normText(title),
                normLong(amount),
                normText(governmentDepartments),
                normText(note)
        );
    }

    public static String baseInfoMergeKey(String type,
                                          Number male,
                                          Number female,
                                          Number total,
                                          Number averageAge,
                                          Number overtimeHours) {
        return mergeKey(
                normText(type),
                normNumber(male),
                normNumber(female),
                normNumber(total),
                normNumber(averageAge),
                normNumber(overtimeHours)
        );
    }

    public static String womenActivityMergeKey(String type,
                                               Number proportion,
                                               Number femaleManagerShare,
                                               Number genderTotalManager,
                                               Number femaleOfficerShare,
                                               Number genderTotalOfficer) {
        return mergeKey(
                normText(type),
                normNumber(proportion),
                normNumber(femaleManagerShare),
                normNumber(genderTotalManager),
                normNumber(femaleOfficerShare),
                normNumber(genderTotalOfficer)
        );
    }

    public static String compatChildcareMergeKey(Integer paternityLeave,
                                                 Integer maternityLeave,
                                                 Integer paternityAcquisition,
                                                 Integer maternityAcquisition) {
        return mergeKey(
                normInt(paternityLeave),
                normInt(maternityLeave),
                normInt(paternityAcquisition),
                normInt(maternityAcquisition)
        );
    }

    public static String financeMergeKey(String accountingStandards,
                                         String fiscalYearCoverPage) {
        return mergeKey(
                normText(accountingStandards),
                normText(fiscalYearCoverPage)
        );
    }

    public static String shareholderMergeKey(String name,
                                             Number ratio) {
        return mergeKey(
                normText(name),
                normNumber(ratio)
        );
    }

    public static String managementIndexMergeKey(
            String period,
            Long netSales,
            String netSalesUnitRef,
            Long operatingRevenue1,
            String operatingRevenue1UnitRef,
            Long operatingRevenue2,
            String operatingRevenue2UnitRef,
            Long grossOperatingRevenue,
            String grossOperatingRevenueUnitRef,
            Long ordinaryIncome,
            String ordinaryIncomeUnitRef,
            Long netPremiumsWritten,
            String netPremiumsWrittenUnitRef,
            Long ordinaryIncomeLoss,
            String ordinaryIncomeLossUnitRef,
            Long netIncomeLoss,
            String netIncomeLossUnitRef,
            Long capitalStock,
            String capitalStockUnitRef,
            Long netAssets,
            String netAssetsUnitRef,
            Long totalAssets,
            String totalAssetsUnitRef,
            Long numberOfEmployees,
            String numberOfEmployeesUnitRef
    ) {
        return mergeKey(
                normText(period),
                normLong(netSales),
                normText(netSalesUnitRef),
                normLong(operatingRevenue1),
                normText(operatingRevenue1UnitRef),
                normLong(operatingRevenue2),
                normText(operatingRevenue2UnitRef),
                normLong(grossOperatingRevenue),
                normText(grossOperatingRevenueUnitRef),
                normLong(ordinaryIncome),
                normText(ordinaryIncomeUnitRef),
                normLong(netPremiumsWritten),
                normText(netPremiumsWrittenUnitRef),
                normLong(ordinaryIncomeLoss),
                normText(ordinaryIncomeLossUnitRef),
                normLong(netIncomeLoss),
                normText(netIncomeLossUnitRef),
                normLong(capitalStock),
                normText(capitalStockUnitRef),
                normLong(netAssets),
                normText(netAssetsUnitRef),
                normLong(totalAssets),
                normText(totalAssetsUnitRef),
                normLong(numberOfEmployees),
                normText(numberOfEmployeesUnitRef)
        );
    }
}