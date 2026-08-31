package com.crm.test;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * 本地复现：模拟 uac-sso-sdk 的 HttpUtil / XmlHelper 硬编码 TLSv1 调用总部 SSO。
 *
 * 【重要】本类为命令行诊断工具，特意放在 workspace 根目录 tools/ 下，而不是 src/ 或 WebContent/。
 * 原因：若放在 src/，会被 build.xml 全量 javac 编进 WEB-INF/classes，
 * 而 resources/applicationContext.xml 中 <context:component-scan base-package="com.hollycrm">
 * 会扫描到它并用内置 ASM 读取类元数据，触发 ArrayIndexOutOfBoundsException，
 * 导致 Spring Context 初始化失败、应用无法启动。
 *
 * 运行方式（需要先自行 javac 编译到一个临时输出目录，例如）：
 *   mkdir out && javac -encoding UTF-8 -d out tools/com/hollycrm/web/sso/SsoTlsRepro.java
 *
 *   1) 默认 OpenJDK 运行（不加 -D 覆盖）→ 复现服务器上的 TLS 报错：
 *      java -cp out com.hollycrm.web.sso.SsoTlsRepro
 *      SSLHandshakeException: No appropriate protocol (protocol is disabled or cipher suites are inappropriate)
 *
 *   2) 加覆盖参数运行 → 验证方案3修复生效，能成功完成 TLSv1 握手：
 *      java -Djava.security.properties=<项目>/resources/tls-override.security -cp out com.hollycrm.web.sso.SsoTlsRepro
 *
 * 前置：需要有一个只支持 TLSv1.0 的服务器作为"总部 SSO"，例如：
 *   openssl s_server -accept 18445 -cert cert.pem -key key.pem -tls1 -www
 */
public class SsoTlsRepro {

    private static final String targetUrl = "https://uac.sso.chinaunicom.cn/";

    /**
     * 模拟 XmlHelper.httpsRequest / HttpUtil.initHttps：
     * SSLContext.getInstance("TLSv1") + 信任任意证书 + HttpsURLConnection。
     */
    public static String ssoHttpsCall() throws Exception {
        SSLContext ctx = SSLContext.getInstance("TLSv1");
        ctx.init(null, new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(java.security.cert.X509Certificate[] c, String a) {
                    }

                    public void checkServerTrusted(java.security.cert.X509Certificate[] c, String a) {
                    }

                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[0];
                    }
                }
        }, new java.security.SecureRandom());
        SSLSocketFactory sf = ctx.getSocketFactory();
        URL url = new URL(targetUrl);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setHostnameVerifier((String h, javax.net.ssl.SSLSession s) -> true);
        conn.setSSLSocketFactory(sf);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        conn.setRequestMethod("GET");
        conn.connect();
        BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            sb.append(line);
        }
        r.close();
        conn.disconnect();
        return sb.toString();
    }

    public static void main(String[] args) {
        try {
            String resp = ssoHttpsCall();
            System.out.println("=== SSO 调用成功 ===");
            System.out.println(resp.length() > 200 ? resp.substring(0, 200) + "..." : resp);
        } catch (Throwable e) {
            System.out.println("=== SSO 调用失败，完整异常链如下（服务器上报错形态）===");
            Throwable cur = e;
            while (cur != null) {
                System.out.println("  -> " + cur.getClass().getName() + ": " + cur.getMessage());
                StackTraceElement[] st = cur.getStackTrace();
                for (int i = 0; i < Math.min(6, st.length); i++) {
                    System.out.println("        at " + st[i]);
                }
                cur = cur.getCause();
            }
            System.exit(1);
        }
    }
}
