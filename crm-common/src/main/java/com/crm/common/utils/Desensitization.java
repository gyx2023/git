package com.crm.common.utils;

public class Desensitization {
    /**
     * 脱敏公司或集团名称
     * 规则：当长度>=4时保留前1位和后2位，中间用'*'替换；当长度<4时返回对应数量的'*'。
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

        // 2. 处理1个字的姓名
        if (length == 1) {
            return "*";
        }

        // 3. 处理2个字的姓名
        if (length == 2) {
            String firstChar = trimmedName.substring(0, 1);
            return firstChar + "*";
        }
        // 4. 处理3个字的姓名
        if (length == 3) {
            String firstChar = trimmedName.substring(0, 1);
            return firstChar + "*" + trimmedName.charAt(1);
        }

        // 5. 当长度大于等于4时，保留前1位和后2位，中间用星号替代
        String firstChar = trimmedName.substring(0, 1);
        String lastTwoChars = trimmedName.substring(length - 2);
        int starsCount = length - 3; // 总长度减去保留的3个字符(前1+后2)
        String stars = generateAsterisks(starsCount);

        return firstChar + stars + lastTwoChars;
    }

    /**
     * 脱敏个人姓名
     * 规则：
     * - 1个字：直接返回一个星号
     * - 2个字：第一个字保留，第二个字用 '*' 替换
     * - 3个字及以上：首尾字符保留，中间的所有字符用 '*' 替换
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

        // 2. 处理2个字的姓名
        if (length == 2) {
            String firstChar = trimmedName.substring(0, 1);
            return firstChar + "*";
        }

        // 3. 处理3个字及以上的姓名
        if (length > 2) {
            String firstChar = trimmedName.substring(0, 1);
            String lastChar = trimmedName.substring(length - 1);
            int starsCount = length - 2;
            String stars = generateAsterisks(starsCount);
            return firstChar + stars + lastChar;
        }

        return trimmedName;
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
     * 脱敏手机号：保留前3后4，中间****
     */
    public static String desensitizePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return phone;
        String s = phone.trim();
        if (s.length() <= 7) return s;
        return s.substring(0, 3) + "****" + s.substring(s.length() - 4);
    }

    /**
     * 脱敏证件号码：保留前6后4，中间用 * 填充
     */
    public static String desensitizeCertificate(String certNo) {
        if (certNo == null || certNo.trim().isEmpty()) return certNo;
        String s = certNo.trim();
        if (s.length() <= 10) return s;
        String prefix = s.substring(0, 6);
        String suffix = s.substring(s.length() - 4);
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < s.length() - 10; i++) sb.append('*');
        sb.append(suffix);
        return sb.toString();
    }

    /**
     * 通用脱敏：保留前pre位和后suf位，中间用'*'替换。
     *
     * @param str 待脱敏字符串
     * @param pre 前面保留的字符数
     * @param suf 后面保留的字符数
     * @return 脱敏后的字符串，如果输入为 null 或空字符串则返回原输入
     */
    public static String maskMiddle(String str, int pre, int suf) {
        if (str == null || str.trim().isEmpty()) {
            return str;
        }
        if (pre <= 0 && suf <= 0) {
            return str;
        }
        String s = str.trim();
        int len = s.length();
        if (len > (pre + suf)) {
            return s.substring(0, pre) + generateAsterisks(len - pre - suf) + s.substring(len - suf);
        } else if (len > pre && pre > 0) {
            return s.substring(0, pre) + generateAsterisks(len - pre);
        } else if (len > suf && suf > 0) {
            return generateAsterisks(len - suf) + s.substring(len - suf);
        } else {
            return generateAsterisks(len);
        }
    }

    /**
     * 脱敏地址：仅保留前缀（地址前段），末尾敏感信息用'*'替换。
     * 规则与旧框架 DataMaskingUtils.address 一致。
     *
     * @param address       完整地址
     * @param sensitiveSize 末尾需要脱敏的字符数
     * @return 脱敏后的地址，如果输入为 null 或空字符串则返回原输入
     */
    public static String desensitizeAddress(String address, int sensitiveSize) {
        if (address == null || address.trim().isEmpty()) {
            return address;
        }
        String s = address.trim();
        int len = s.length();
        if (len <= sensitiveSize) {
            return generateAsterisks(len);
        }
        return s.substring(0, len - sensitiveSize) + generateAsterisks(sensitiveSize);
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

        // 4. 计算需要脱敏的字符数量，并生成相应数量的 '*'
        int starsCount = length - 4;
        String stars = generateAsterisks(starsCount);

        // 5. 拼接结果
        return startTwoChars + stars + endTwoChars;
    }

}
