-- 应用发布前执行；历史记录保留 NULL，表示无超时判定数据。
ALTER TABLE zqzt_jt_notice_record
ADD COLUMN request_timeout_status TINYINT NULL DEFAULT NULL
COMMENT '无人工厂请求超时状态：NULL未判定，0未超时，1已超时';


SELECT CASE WHEN COUNT(1) = 0 THEN 'tongguo' ELSE CAST(COUNT(1) AS CHAR) END AS warning_sql FROM zqzt_jt_notice_record WHERE request_timeout_status = 1 AND update_time >= DATE_SUB(NOW(), INTERVAL 60 MINUTE);
