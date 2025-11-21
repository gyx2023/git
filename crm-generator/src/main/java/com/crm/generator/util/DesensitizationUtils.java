package com.crm.generator.util;

public class DesensitizationUtils {

    /**
     * 脱敏公司或集团名称
     * 规则：保留最后两位字符，前面的所有字符用 '*' 替换。
     *
     * @param companyName 公司或集团的完整名称
     * @return 脱敏后的名称，如果输入为 null 或空字符串则返回原输入
     */
    public static String desensitizeCompanyName(String companyName) {
        // 1. 处理 null 或空字符串的情况
        if (companyName == null || companyName.trim().isEmpty()) {
            return companyName;
        }

        String trimmedName = companyName.trim();
        int length = trimmedName.length();

        // 2. 如果名称长度小于等于2，直接返回原名称（无需脱敏）
        if (length <= 2) {
            return trimmedName;
        }

        // 3. 计算需要脱敏的字符数量，并生成相应数量的 '*'
        int starsCount = length - 2;
        String stars = generateAsterisks(starsCount);

        // 4. 获取最后两位字符，并与 '*' 拼接
        String lastTwoChars = trimmedName.substring(length - 2);
        return stars + lastTwoChars;
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

        // 对于长度为1的姓名，直接返回
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

    // ==================== 测试方法 ====================
    public static void main(String[] args) {
        System.out.println("============= 公司名称脱敏测试 =============");
        System.out.println("原始: 阿里巴巴集团 -> 脱敏: " + desensitizeCompanyName("阿里巴巴集团"));
        System.out.println("原始: 腾讯科技 -> 脱敏: " + desensitizeCompanyName("腾讯科技"));
        System.out.println("原始: 字节跳动 -> 脱敏: " + desensitizeCompanyName("字节跳动"));
        System.out.println("原始: 百度 -> 脱敏: " + desensitizeCompanyName("百度"));
        System.out.println("原始: 华为 -> 脱敏: " + desensitizeCompanyName("华为"));
        System.out.println("原始: 微软中国有限公司 -> 脱敏: " + desensitizeCompanyName("微软中国有限公司"));
        System.out.println("原始: (空字符串) -> 脱敏: '" + desensitizeCompanyName("") + "'");
        System.out.println("原始: null -> 脱敏: " + desensitizeCompanyName(null));

        System.out.println("\n============= 个人姓名脱敏测试 =============");
        System.out.println("原始: 张三 -> 脱敏: " + desensitizeName("张三"));
        System.out.println("原始: 李四 -> 脱敏: " + desensitizeName("李四"));
        System.out.println("原始: 王老五 -> 脱敏: " + desensitizeName("王老五"));
        System.out.println("原始: 张三丰 -> 脱敏: " + desensitizeName("张三丰"));
        System.out.println("原始: 尼古拉斯·赵四 -> 脱敏: " + desensitizeName("尼古拉斯·赵四"));
        System.out.println("原始: 爱因斯坦 -> 脱敏: " + desensitizeName("爱因斯坦"));
        System.out.println("原始: 陈 -> 脱敏: " + desensitizeName("陈"));
        System.out.println("原始: (空字符串) -> 脱敏: '" + desensitizeName("") + "'");
        System.out.println("原始: null -> 脱敏: " + desensitizeName(null));
    }
}
