package com.crm.generator.util;

public class Desensitization {
    /**
     * 脱敏公司或集团名称
     * 规则：保留最后两位字符，前面的所有字符用 '*' 替换。
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
     * 规则：
     * - 2个字：第一个字保留，第二个字用 '*' 替换。
     * - 3个字及以上：首尾字符保留，中间的所有字符用 '*' 替换。
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
}
