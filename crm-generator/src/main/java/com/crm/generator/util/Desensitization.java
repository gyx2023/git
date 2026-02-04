package com.crm.generator.util;

public class Desensitization {
    /**
     * 脱敏公司或集团名称
     * 规则：保留前1后2汉字
     *
     * @param companyName 公司或集团的完整名称
     * @return 脱敏后的名称，如果输入为 null 或空字符串则返回原输入
     */
    public static String desensitizeCompanyName(String companyName) {
        // 1. 处理 null或空字符串的情况
        if (companyName == null || companyName.trim().isEmpty()) {
            return companyName;
        }


        String trimmedName = companyName.trim();
        int length = trimmedName.length();

        // 2. 如果名称长度小于等于3，直接返回原名称（无需脱敏）
        if (length <= 3) {
            return trimmedName;
        }

        // 3. 计算需要脱敏的字符数量，并生成相应数量的 '*'
        int starsCount = length - 3;
        String stars = generateAsterisks(starsCount);

        // 4. 获取最后两位字符，并与 '*' 拼接
        String lastTwoChars = trimmedName.substring(length - 2);
        String startOneChar = trimmedName.substring(0, 1);
        return startOneChar + stars + lastTwoChars;
    }

    /**
     * 脱敏个人姓名
     * 规则：保留第一个字符，后面的所有字符用 '*' 替换。
     *
     * @param fullName 个人的完整姓名
     * @return 脱敏后的姓名，如果输入为 null 或空字符串则返回原输入
     */
    public static String desensitizeName(String fullName) {
        // 1. 处理 null 或空字符串的情况
        if (fullName == null || fullName.trim().isEmpty()) {
            return fullName;
        }

        String trimmedName = fullName.trim();
        int length = trimmedName.length();

        // 2. 如果名称长度小于等于1，直接返回原名称
        if (length == 1) {
            return trimmedName;
        }

        // 3. 获取第一个字符并添加相应数量的星号
        String firstChar = trimmedName.substring(0, 1);
        int starsCount = length - 1;
        String stars = generateAsterisks(starsCount);
        return firstChar + stars;
    }

    /**
     * 生成指定数量的 '*' 字符串
     * @param count 需要生成的 '*' 的数量
     * @return 由 count 个 '*' 组成的字符串
     */
    private static String generateAsterisks(int count) {
        // 使用 StringBuilder 效率更高，尤其是在 count 较大时
        if (count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append('*');
        }
        return sb.toString();
    }

    /**
     * 脱敏合同名称
     * 规则：保留前两位和后两位字符，中间的所有字符用 '*' 替换。
     *
     * @param contractName 合同的完整名称
     * @return 脱敏后的名称，如果输入为 null 或空字符串则返回原输入
     */
    public static String desensitizeContractName(String contractName) {
        // 1. 处理 null或空字符串的情况
        if (contractName == null || contractName.trim().isEmpty()) {
            return contractName;
        }

        String trimmedName = contractName.trim();
        int length = trimmedName.length();

        // 2. 如果名称长度小于等于4，直接返回原名称（无需脱敏）
        if (length <= 4) {
            return trimmedName;
        }

        // 3. 获取前两位和后两位字符
        String startTwoChars = trimmedName.substring(0, 2);
        String endTwoChars = trimmedName.substring(length - 2);

        // 4. 计算需要脱敏的字符数量，最多保留8个'*'
        int starsCount = Math.min(length - 4, 8);
        String stars = generateAsterisks(starsCount);

        // 5. 拼接结果
        return startTwoChars + stars + endTwoChars;
    }

    /**
     * 脱敏手机号码
     * 规则：保留前3位和后4位，中间4位用 '*' 替换。
     *
     * @param phoneNumber 完整的手机号码
     * @return 脱敏后的手机号码，如果输入为 null 或空字符串则返回原输入
     */
    public static String desensitizePhoneNumber(String phoneNumber) {
        // 1. 处理 null 或空字符串的情况
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return phoneNumber;
        }

        String trimmedPhone = phoneNumber.trim();

        // 2. 检查手机号码长度是否符合标准（通常为11位）
        if (trimmedPhone.length() < 7) {
            // 如果长度小于7，直接返回原号码（不做脱敏处理）
            return trimmedPhone;
        }

        // 3. 提取前3位和后4位
        String startThreeDigits = trimmedPhone.substring(0, 3);
        String endFourDigits = trimmedPhone.substring(trimmedPhone.length() - 4);

        // 4. 中间部分用4个星号替换
        return startThreeDigits + "****" + endFourDigits;
    }
}
