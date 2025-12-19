-- 用户名
INSERT INTO sys_param (
    code_, 
    name_, 
    value_, 
    remark_, 
    type_, 
    input_type, 
    is_system
) 
VALUES (
    'sftp.username', 
    'IOB_GOV2表数据导出至sftp用户名', 
    'test', 
    'IOB_GOV2表数据导出至sftp用户名', 
    'iob_gov2_table_export', 
    'string', 
    1
);

-- 密码
INSERT INTO sys_param (
    code_, 
    name_, 
    value_, 
    remark_, 
    type_, 
    input_type, 
    is_system
) 
VALUES (
    'sftp.password', 
    'IOB_GOV2表数据导出至sftp密码', 
    '1qaz1QAZ', 
    'IOB_GOV2表数据导出至sftp密码', 
    'iob_gov2_table_export', 
    'string', 
    1
);

-- 域名
INSERT INTO sys_param (
    code_, 
    name_, 
    value_, 
    remark_, 
    type_, 
    input_type, 
    is_system
) 
VALUES (
    'sftp.host', 
    'IOB_GOV2表数据导出至sftp域名', 
    '192.168.124.191', 
    'IOB_GOV2表数据导出至sftp域名', 
    'iob_gov2_table_export', 
    'string', 
    1
);

-- 端口
INSERT INTO sys_param (
    code_, 
    name_, 
    value_, 
    remark_, 
    type_, 
    input_type, 
    is_system
) 
VALUES (
    'sftp.port', 
    'IOB_GOV2表数据导出至sftp端口', 
    '22', 
    'IOB_GOV2表数据导出至sftp端口', 
    'iob_gov2_table_export', 
    'string', 
    1
);

-- 文件路径
INSERT INTO sys_param (
    code_, 
    name_, 
    value_, 
    remark_, 
    type_, 
    input_type, 
    is_system
) 
VALUES (
    'sftp.directory', 
    'IOB_GOV2表数据导出至sftp文件路径', 
    '/upload', 
    'IOB_GOV2表数据导出至sftp文件路径', 
    'iob_gov2_table_export', 
    'string', 
    1
);