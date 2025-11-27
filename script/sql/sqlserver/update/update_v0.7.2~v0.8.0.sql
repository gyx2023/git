ALTER TABLE [zqzt_sys_oss_config] ADD [access_policy] nchar(1) DEFAULT ('1') NOT NULL
GO

EXEC sp_addextendedproperty
'MS_Description', N'桶权限类型(0=private 1=public 2=custom)',
'SCHEMA', N'dbo',
'TABLE', N'zqzt_sys_oss_config',
'COLUMN', N'access_policy'
GO


ALTER TABLE [zqzt_wf_deploy_form] ADD [form_name] nvarchar(64) DEFAULT '' NULL
GO

EXEC sp_addextendedproperty
    'MS_Description', N'表单名称',
    'SCHEMA', N'dbo',
    'TABLE', N'zqzt_wf_deploy_form',
    'COLUMN', N'form_name'
GO
