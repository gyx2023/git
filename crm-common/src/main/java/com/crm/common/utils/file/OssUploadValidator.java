package com.crm.common.utils.file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 【安全】OSS 上传文件校验器 —— 修复"上传零类型校验 + Content-Type 信任客户端"漏洞。
 *
 * <p>背景：SysOssServiceImpl.upload(MultipartFile) 原先不对上传文件做任何类型/内容校验，
 * 直接沿用客户端声明的文件名后缀与 Content-Type 写入对象存储；当对象是 public 直链或拿到
 * 120s 预签名 URL 时，若文件为 HTML/SVG/PDF 且元数据是把控的 text/html，浏览器直接打开即执行
 * （存储型 XSS）。本校验器在上传入口统一收口：
 * <ul>
 *   <li>① 后缀白名单（与若依 DEFAULT_ALLOWED_EXTENSION 一致，不含 jsp/html/svg/js/php 等可执行/可脚本类型）</li>
 *   <li>② 高危内容兜底：内容开头若命中 HTML/脚本标记，一律拒绝（防“白名单内后缀伪装 HTML”）</li>
 *   <li>③ 常见类型魔数校验：图片/pdf/docx…/zip，防“.jpg 伪装 HTML”</li>
 *   <li>④ 成功则返回服务端判定的 Content-Type（按扩展名映射，绝不采用客户端声明值）</li>
 * </ul>
 *
 * <p>校验为“拒绝制”：白名单外或高危内容直接抛异常（消息经 ServiceException 返回给前端），
 * 不做静默降级——避免不可渲染的文件静默吞掉导致用户无感知失败。
 *
 * <p>接入点（单点收口）：{@code com.crm.system.service.sys.impl.SysOssServiceImpl} 的
 * upload(MultipartFile) / upload(File,String) / uploadFileByPath(File,String) 三个方法，
 * 覆盖 crm-admin、crm-system、crm-job-server 全部真实上传链路。
 */
public final class OssUploadValidator {

    private OssUploadValidator() {
    }

    /**
     * 允许上传的后缀白名单（小写、无点）。
     * 判定为安全 EOL 到 web 浏览的常见业务附件 + 内部任务（导出 .xlsx/.csv/.txt 结果文件）；
     * 显式排除 .html/.htm/.svg/.xml/.swf/.jsp/.js/.vbs/.php 等可被当作脚本/文档渲染的类型。
     */
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
        // 图片
        "bmp", "gif", "jpg", "jpeg", "png", "ico", "webp",
        // office / pdf / 文本
        "doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf", "txt", "csv", "md",
        // 压缩
        "rar", "zip", "7z", "gz", "bz2", "tar",
        // 音视频
        "mp3", "wav", "mp4", "avi"
    ));

    /**
     * 需要魔数强校验的扩展名。白名单内绝大多数字节型扩展名内容首字节有可靠魔数；
     * 文本文类（txt/csv/md）与旧封装(zip 家族 rar/7z)魔数歧义大，不做魔数强校验，
     * 但其内容会被第 ② 步的高危标记扫描兜底。
     */
    private static final Set<String> MAGIC_CHECKED_EXTS = new HashSet<>(Arrays.asList(
        "jpg", "jpeg", "png", "gif", "bmp", "ico", "webp",
        "pdf", "docx", "xlsx", "pptx", "zip", "mp3", "mp4", "wav"
    ));

    /** 高危 HTML/脚本泄漏标记（对二进制/可执行面文件全量扫描）：命中即判不可信。 */
    private static final String[] SCRIPT_MARKERS = {
        "<html", "<head", "<body", "<script", "</script", "<!doctype", "<?xml", "<svg", "<iframe", "javascript:"
    };

    /** 文本文类(txt/csv/md)可视层标记子集：纯文本里出现 <html>/<body> 属正常说明不构成 XSS，不据此拒；
     *  仅命中<script/iframe/javascript 等真正会导致执行/控制的内容才拒。 */
    private static final Set<String> TEXT_EXTENSIONS = new HashSet<>(Arrays.asList("txt", "csv", "md"));

    private static final int MAGIC_READ_BYTES = 16;

    /** 扫描文件头部字节数：覆盖典型 HTML/脚本头，超限内容无需读全；调用方读取头部调用此值 */
    public static final int SCAN_BYTES = 2048;

    /** 扩展名 -> 服务端 Content-Type（只放行 business 附件；避免采用客户端声明） */
    private static final Map<String, String> TYPE_MAP = new HashMap<>();

    static {
        TYPE_MAP.put("bmp", "image/bmp");
        TYPE_MAP.put("gif", "image/gif");
        TYPE_MAP.put("jpg", "image/jpeg");
        TYPE_MAP.put("jpeg", "image/jpeg");
        TYPE_MAP.put("png", "image/png");
        TYPE_MAP.put("ico", "image/x-icon");
        TYPE_MAP.put("webp", "image/webp");
        TYPE_MAP.put("pdf", "application/pdf");
        TYPE_MAP.put("doc", "application/msword");
        TYPE_MAP.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        TYPE_MAP.put("xls", "application/vnd.ms-excel");
        TYPE_MAP.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        TYPE_MAP.put("ppt", "application/vnd.ms-powerpoint");
        TYPE_MAP.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        TYPE_MAP.put("txt", "text/plain; charset=UTF-8");
        TYPE_MAP.put("csv", "text/plain; charset=UTF-8");
        TYPE_MAP.put("md", "text/plain; charset=UTF-8");
        TYPE_MAP.put("rar", "application/vnd.rar");
        TYPE_MAP.put("zip", "application/zip");
        TYPE_MAP.put("7z", "application/x-7z-compressed");
        TYPE_MAP.put("gz", "application/gzip");
        TYPE_MAP.put("bz2", "application/x-bzip2");
        TYPE_MAP.put("tar", "application/x-tar");
        TYPE_MAP.put("mp3", "audio/mpeg");
        TYPE_MAP.put("wav", "audio/wav");
        TYPE_MAP.put("mp4", "video/mp4");
        TYPE_MAP.put("avi", "video/x-msvideo");
    }

    /**
     * 校验上传文件名(MultipartFile/File)与内容，返回服务端应写入的 Content-Type。
     * 不合法/高危内容直接抛 {@link IllegalArgumentException}（由调用层转业务提示），
     * 绝不让异常文件进入对象存储。
     *
     * @param originalFilename 客户端原始文件名（可能是中文/带路径，仅取扩展名）
     * @param readBytes        由调用方读取的、用于校验的文件头部字节（建议 ≥1KB，够含典型 HTML 头）
     */
    public static String validate(InputStream in, String originalFilename, int scanBytes) {
        String ext = extractExtension(originalFilename);
        if (ext == null) {
            throw new IllegalArgumentException("不支持的文件类型（无扩展名）");
        }
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("不支持的文件类型: ." + ext);
        }
        // 读取头部（魔数 + 高危标记扫描共用一次 IO；流不关闭由调用方负责）
        byte[] head;
        try {
            head = readHeader(in, scanBytes);
        } catch (IOException e) {
            throw new IllegalArgumentException("文件内容读取失败: " + e.getMessage());
        }
        if (head.length <= 0) {
            throw new IllegalArgumentException("文件内容为空");
        }
        checkHighRiskMarkers(head, ext);
        if (MAGIC_CHECKED_EXTS.contains(ext)) {
            String magic = probeByHeader(head);
            if (!magicMatches(ext, magic)) {
                throw new IllegalArgumentException("文件内容与扩展名不符，疑似伪装或恶意文件");
            }
        }
        String type = TYPE_MAP.get(ext);
        return type != null ? type : "application/octet-stream";
    }

    /** 高危脚本标记；对纯文本(txt/csv/md)用执行子集，其余类型用全量（含 <html> 等结构泄漏标记）。 */
    private static void checkHighRiskMarkers(byte[] head, String ext) {
        String lower = new String(head, 0, head.length, StandardCharsets.ISO_8859_1).toLowerCase();
        if (TEXT_EXTENSIONS.contains(ext)) {
            for (String marker : new String[]{"<script", "</script", "javascript:", "<iframe"}) {
                if (lower.contains(marker)) {
                    throw new IllegalArgumentException("禁止上传包含 HTML/JS 脚本内容的文件");
                }
            }
        } else {
            for (String marker : SCRIPT_MARKERS) {
                if (lower.contains(marker)) {
                    throw new IllegalArgumentException("禁止上传包含 HTML/JS 脚本内容的文件");
                }
            }
        }
    }

    /**
     * 返回白名单校验后规范化扩展名（小写、带前导点，供服务端拼对象 key，避免大小写/脏后缀透传）。
     * 未通过白名单则抛异常。
     */
    public static String resolveExtension(String originalFilename) {
        String ext = extractExtension(originalFilename);
        if (ext == null) {
            throw new IllegalArgumentException("不支持的文件类型（无扩展名）");
        }
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("不支持的文件类型: ." + ext);
        }
        return "." + ext;
    }

    /** 扩展名（小写、去点、取最后一个 '.' 之后、过滤路径分隔符）；空返回 null */
    private static String extractExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        int slash = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
        int dot = filename.lastIndexOf('.');
        if (dot <= slash) {
            return null;
        }
        String ext = filename.substring(dot + 1).toLowerCase();
        return ext.isEmpty() ? null : ext;
    }

    private static byte[] readHeader(InputStream in, int maxLen) throws IOException {
        byte[] buf = new byte[maxLen];
        int off = 0;
        while (off < maxLen) {
            int n = in.read(buf, off, maxLen - off);
            if (n < 0) {
                break;
            }
            off += n;
        }
        return Arrays.copyOf(buf, off);
    }

    /** 按文件头魔数探测 MIME（覆盖图片/pdf/docx 家族/zip/mp4/音频），未知返回 null */
    private static String probeByHeader(byte[] b) {
        if (b.length < 4) {
            return null;
        }
        int n = b.length;
        if (n >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8) return "image/jpeg";
        if (n >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') return "image/png";
        if (n >= 6 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8') return "image/gif";
        if (n >= 2 && b[0] == 'B' && b[1] == 'M') return "image/bmp";
        if (n >= 4 && b[0] == 0x00 && b[1] == 0x00 && b[2] == 0x01 && b[3] == 0x00) return "image/x-icon";
        if (n >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
            && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return "image/webp";
        if (n >= 5 && b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F' && b[4] == '-') return "application/pdf";
        if (n >= 4 && b[0] == 'P' && b[1] == 'K' && b[2] == 0x03 && b[3] == 0x04) return "application/zip";
        if (n >= 8 && b[4] == 'f' && b[5] == 't' && b[6] == 'y' && b[7] == 'p') return "video/mp4";
        if (n >= 3 && b[0] == 'I' && b[1] == 'D' && b[2] == '3') return "audio/mpeg";
        // 无 ID3 的 MP3：MPEG 帧头 FF Ex
        if (n >= 2 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xE0) == 0xE0) return "audio/mpeg";
        if (n >= 4 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
            && n >= 12 && b[8] == 'W' && b[9] == 'A' && b[10] == 'V' && b[11] == 'E') return "audio/wav";
        return null;
    }

    /** 魔数与扩展名预期是否匹配 */
    private static boolean magicMatches(String ext, String magic) {
        if (magic == null) {
            return false; // 需魔数校验类型探不到 → 伪装
        }
        switch (ext) {
            case "jpg":
            case "jpeg":
                return "image/jpeg".equals(magic);
            case "png":
                return "image/png".equals(magic);
            case "gif":
                return "image/gif".equals(magic);
            case "bmp":
                return "image/bmp".equals(magic);
            case "ico":
                return "image/x-icon".equals(magic);
            case "webp":
                return "image/webp".equals(magic);
            case "pdf":
                return "application/pdf".equals(magic);
            case "docx":
            case "xlsx":
            case "pptx":
            case "zip":
                return "application/zip".equals(magic); // docx/xlsx/pptx 均为 zip 容器
            case "mp4":
                return "video/mp4".equals(magic);
            case "mp3":
                return "audio/mpeg".equals(magic);
            case "wav":
                return "audio/wav".equals(magic);
            default:
                return true;
        }
    }
}
