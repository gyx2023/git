package com.crm.system.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Constants {

    public static final String IAS_CODE = "IAS_FIRE";

    public static final String IDENTITY_USER = "user";// 用户session

    /**
     * 用户类型（超管、普管、普通用户）
     */
    public static final String USER_TYPE_SUPER = "ut1";// 超管
    public static final String USER_TYPE_ADMIN = "ut2";// 普管
    public static final String USER_TYPE_NORMAL = "ut3";// 普通用户

    public static final String CODE_UNICOM_TYPE = "unicom_type";// 一级行业编码
    public static final String CODE_UNICOM_TYPE2 = "unicom_type_2";// 二级行业编码

    public static final String CODE_MAIN_PRODUCT = "main_product";// 主要产品/解决方案名称

    public static final String CODE_INDUSTRY_OTHER = "400218";// 行业名称：其他
    /**
     * 是否可用
     */
    public static final String MESSAGE_VALID = "1";// 可用
    public static final String MESSAGE_UNVALID = "0";// 不可用

    public static String SUCCESS = "success"; // 成功
    public static String FAIL = "fail"; // 失败

    //系统信息同步是否有效
    public static String SYS_USER_SYNC_IS_VALID_TRUE = "1";  //有效
    public static String SYS_USER_SYNC_IS_VALID_FALSE = "0";  //无效


    // 日志操作转换的文件名
    public static final String OPERATE_NAME_MAP_CONFIG = "syslog-operate-map.properties";

    // 日志级别
    public static final String SYSLOG_LEVEL_NORMAL = "ll1";
    public static final String SYSLOG_LEVEL_IMPORTANT = "ll2";

    // 应用地址
    public static final String WEB_BASE_URL = "web_base_url";

    // 微信消息类型 普通消息
    public static final String WX_MESSAGE_TYPE_NORMAL = "msg_type_1";
    // 微信消息类型 点单消息
    public static final String WX_MESSAGE_TYPE_ACCEPT = "msg_type_2";
    // 微信消息类型 挂拍清单消息
    public static final String WX_MESSAGE_TYPE_GRAB = "msg_type_3";

    // 微信抢单消息图片
    public static final String WX_MESSAGE_PIC_URL_GRAB = "wx_grab_order_pic_url";
    // 微信接单消息图片
    public static final String WX_MESSAGE_PIC_URL_ACCEPT = "wx_accept_order_pic_url";

    // 抢单url
    public static final String WX_MESSAGE_URL_GRAB = "wx_grab_order_url";
    // 接单url
    public static final String WX_MESSAGE_URL_ACCEPT = "wx_accept_order_url";

    // 待办任务地址配置
    // 线索------
    public static final String CLUE_TRACK_DEAL_URL = "/toppClue/toShow?urlType=deal"; // 线索跟踪处理
    public static final String CLUE_DISTRIBUTE_DEAL_URL = "/toppClue/toShow?urlType=deal"; // 行业总监分配客户经理
    public static final String DIRECTOR_OPPO_AUDIT_URL = "/toppClue/toShow?urlType=deal"; // 行业副总监审核

    // 商机------

    // 支撑类型
    // 挂拍
    public static final String SUPPORT_TYPE_HANDS_UP = "support_hands_up";
    // 点单
    public static final String SUPPORT_TYPE_ORDER = "support_order";
    // 默认
    public static final String SUPPORT_TYPE_DEFAULT = "support_default";

    // 点单
    public static final String TASK_CUST_ORDER = "cust_order";
    // 挂拍
    public static final String TASK_CUST_HANDS_UP = "cust_hands_up";
    // 支持经理接单
    public static final String TASK_SUPPORT_ACCEPT_ORDER = "support_accept_order";
    // 支持经理抢单
    public static final String TASK_SUPPORT_GRAB_ORDER = "support_grab_order";

    // 支撑处理类型
    // 发布
    public static final String SUPPORT_DEAL_PUBLISH = "support_deal_publish";
    // 拒单
    public static final String SUPPORT_DEAL_ACCEPT = "support_deal_accept";
    // 拒单并结束
    public static final String SUPPORT_DEAL_REFUSE = "support_deal_refuse";
    // 接单并且处理完成
    public static final String SUPPORT_DEAL_END = "support_deal_end";

    // 日期年月格式，长度6
    public static final String YEAR_MONTH_PATTERN = "yyyyMM";
    // 日期年月日格式，长度8
    public static final String YEAR_MONTH_DAY_PATTERN = "yyyyMMdd";
    public static final String YEAR_MONTH_DAY_PATTERN2 = "yyyy-MM-dd";
    // 系統全時間，長度19
    public static final String ALL_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /**
     * 用户角色
     */
    public static final int USER_ROLE_NOMAL = 183;// 普通员工
    public static final int USER_ROLE_OPP_MANAGER = 182;// 商机管理员
    public static final int USER_ROLE_INDUSTRY_DIRECTOR = 177;// 行业副总监
    public static final int USER_ROLE_CUST_MANAGER = 178;// 客户经理
    public static final int USER_ROLE_DISIGNER = 180;// 支撑经理
    public static final int USER_ROLE_PROV_SUPPORT = 179;// 省支撑人员-指派人 --改名 支撑管理员
    public static final int USER_ROLE_SYS_ADMIN = 175;// 系统管理员
    public static final int USER_ROLE_JINGGUAN_MANAGER = 185; // 经管管理员
    public static final int USER_ROLE_PROJECT_MANAGER = 186;// 项目经理
    public static final int USER_ROLE_INDUSTRY_DIRECTOR_Z = 187;// 行业总监
    public static final int USER_ROLE_TZGL = 188; // 投资管理员
    public static final int USER_ROLE_JGJL = 189; // 经管经理
    public static final int USER_ROLE_ZCGLJL = 190; // 支撑管理经理
    public static final int USER_ROLE_TDZ = 191; // 支撑团队长
    public static final int USER_ROLE_DISIGNER_Z = 193;// 支撑总监
    public static final int USER_ROLE_BUSINESS_QUERY_ADMIN = 184;// 业务查询管理员
    public static final int USER_ROLE_ORDER_QUERY_ADMIN = 195;// 订单管理员
    public static final int USER_ROLE_PROJECT_MANAGE = 196;// 项目管理员
    public static final int USER_ROLE_ABILITY_MANAGE = 233;// 资质管理员
    public static final int USER_ROLE_PARTNER_SJMANAGE = 199;// 合作伙伴省本级管理员
    public static final int USER_ROLE_PARTNER_DSMANAGE = 200;// 地市合作伙伴管理员	/* 待办任务-任务类型 */
    public static final String USER_ROLE_MARKET_MANAGE = "254";// 营销任务管理员
    public static final String TASK_TYPE_CLUE_DISTRIBUTE = "clue_distribute"; // 行业总监线索分配
    public static final String TASK_TYPE_CLUE_TRACK = "clue_track";// 线索跟踪
    public static final String TASK_TYPE_CUST_ORDER = "cust_order";// 客户经理点单
    public static final String TASK_TYPE_CUST_HANDS_UP = "cust_hands_up";// 客户经理挂拍
    public static final String TASK_TYPE_CUST_HANDS_UP_ORDER = "hans_up_order";// 客户经理点单/挂拍
    public static final String TASK_TYPE_SUPPORT_ACCEPT_ORDER = "support_accept_order";// 支撑管理员接单
    public static final String TASK_TYPE_SUPPORT_GRAB_ORDER = "support_grab_order";// 支撑管理员抢单
    public static final String TASK_TYPE_OPPO_REVIEW = "oppo_review";// 商机方案评审
    public static final String TASK_TYPE_PRO_APPROVAL = "project_approval";// 项目立项
    public static final String TASK_TYPE_PRO_BID = "project_bid";// 项目投标
    public static final String TASK_TYPE_PRO_MILESTONE_SETTING = "project_milestone_setting";// 项目里程碑设置
    public static final String TASK_TYPE_CONNECT_CONTRACT = "connect_contract";// 关联合同
    public static final String TASK_TYPE_CLUE_DIRECTOR_DISTRIBUTE = "clue_director_distribute";// 商机管理员线索分配
    public static final String TASK_TYPE_DIRECTOR_OPPO_AUDIT = "director_oppo_audit";// 行业副总监商机审核
    public static final String TASK_TYPE_DIRECTOR_Z_OPPO_AUDIT = "director_z_oppo_audit";// 行业总监商机审核
    public static final String TASK_TYPE_SUPPORT_LEADER_DISTRIBUTE = "support_leader_distribute";// 支撑经理团队长指派支撑经理
    public static final String TASK_TYPE_PROV_SUPPORT = "prov_support";// 省份支撑经理支撑
    public static final String TASK_TYPE_PROV_SUPPORT_SUPPORT = "prov_support_support";// 省份支撑经理支持
    public static final String TASK_TYPE_CUST_OPPO_EDIT = "cust_oppo_edit";// 客户经理商机修改
    public static final String TASK_TYPE_OPPO_STOP = "oppo_stop";// 商机废弃副总监审核
    public static final String TASK_TYPE_OPPO_DIRECTOR_STOP = "oppo_director_stop";// 商机废弃总监审核

    public static final String TASK_TYPE_DIRECTOR_OPPO_EDITINFO_AUDIT = "director_oppo_editinfo_audit";// 行业副总监商机修改信息审核
    public static final String TASK_TYPE_DIRECTOR_Z_OPPO_EDITINFO_AUDIT = "director_z_oppo_editinfo_audit";// 行业总监商机修改信息审核
    public static final String TASK_TYPE_DIRECTOR_OPPO_EDITINFO_AUDIT_NAME = "行业副总监商机信息修改审核";// 行业副总监商机信息修改审核
    public static final String TASK_TYPE_DIRECTOR_Z_OPPO_EDITINFO_AUDIT_NAME = "行业总监商机信息修改审核";// 行业总监商机信息修改审核

    public static final String TASK_TYPE_OPPO_BID_AUDIT = "oppo_bid_audit";// 投标审核
    public static final String TASK_TYPE_OPPO_BID_DAFEN = "oppo_bid_dafen";// 投标打分任务
    public static final String TASK_TYPE_OPPO_CONTRI = "oppo_contri";// 支撑贡献度任务
    public static final String TASK_TYPE_PROJECT_PRE_EVALUATE = "project_pre_evaluate";// 投资管理员项目前评估

    public static final String TASK_TYPE_ORDER_BSZZ_AUDIT = "order_bszz_audit"; // 标书准备行业总监审核
    public static final String TASK_TYPE_ORDER_BQH_AUDIT = "order_bqhs_audit";// 标前会审审核

    public static final String TASK_TYPE_CONTRACT_AUDIT = "contract_audit";// 合同审核
    public static final String TASK_TYPE_CONTRACT_AUDIT_NAME = "项目管理员合同审核";// 合同审核

    public static final String TASK_TYPE_SUPPORT_AUDIT = "support_audit";// 支撑录入审核
    public static final String TASK_TYPE_SUPPORT_AUDIT_NAME = "团队长支撑录入审核";// 审核

    public static final String TASK_TYPE_SUPPORT_EDIT = "support_edit";// 支撑录入修改
    public static final String TASK_TYPE_SUPPORT_EDIT_NAME = "支撑经理支撑录入修改";

    public static final String TASK_TYPE_DUTY_CHANGE = "duty_change";// 主责支撑经理职责转让
    public static final String TASK_TYPE_DUTY_CHANGE_NAME = "主责支撑经理职责转让";

    /*
     * 支撑工单类型的任务
     */
    public static final String TASK_TYPE_ORDER_BSZZ = "order_bszz"; // 投标材料
    public static final String TASK_TYPE_ORDER_XCJB = "order_xcjb"; // 将标
    public static final String TASK_TYPE_ORDER_EXPLANATION_MATERIAL = "order_explanation_material"; // 讲解材料
    public static final String TASK_TYPE_ORDER_SITE_EXPLANATION = "order_site_explanation"; // 讲解
    public static final String CUST_MANAGER_ORDER_TECHNOLOGY_COMM = "technology_comm"; // 技术交流
    public static final String TASK_TYPE_SUPPORT_PRO_MAINTAIN = "support_pro_maintain";// 支撑经理方案制定
    public static final String TASK_TYPE_CENTER = "order_center";// 参观中心活动

    public static final String TASK_TYPE_CLUE_DISTRIBUTE_NAME = "行业副总监线索分配"; // 行业总监线索分配
    public static final String TASK_TYPE_CLUE_TRACK_NAME = "线索跟踪";// 线索跟踪
    public static final String TASK_TYPE_SUPPORT_PRO_MAINTAIN_NAME = "支撑经理方案制定";// 支撑经理方案制定
    public static final String TASK_TYPE_CUST_ORDER_NAME = "客户经理点单";// 客户经理点单
    public static final String TASK_TYPE_CUST_HANDS_UP_NAME = "客户经理挂拍";// 客户经理挂拍
    public static final String TASK_TYPE_CUST_HANDS_UP_ORDER_NAME = "客户经理点单/挂拍";// 客户经理点单/挂拍
    public static final String TASK_TYPE_SUPPORT_ACCEPT_ORDER_NAME = "支撑经理接单";// 支撑管理员接单
    public static final String TASK_TYPE_SUPPORT_GRAB_ORDER_NAME = "支撑经理抢单";// 支撑管理员抢单
    public static final String TASK_TYPE_OPPO_REVIEW_NAME = "商机方案评审";// 商机方案评审
    public static final String TASK_TYPE_PRO_APPROVAL_NAME = "项目立项";// 项目立项
    public static final String TASK_TYPE_PRO_BID_NAME = "项目投标";// 项目投标
    public static final String TASK_TYPE_PRO_MILESTONE_SETTING_NAME = "项目里程碑设置";// 项目里程碑设置
    public static final String TASK_TYPE_CONNECT_CONTRACT_NAME = "关联合同";// 关联合同
    public static final String TASK_TYPE_CLUE_DIRECTOR_DISTRIBUTE_NAME = "商机管理员线索分配";// 商机管理员线索分配
    public static final String TASK_TYPE_DIRECTOR_OPPO_AUDIT_NAME = "行业副总监商机审核";// 行业副总监商机审核
    public static final String TASK_TYPE_DIRECTOR_Z_OPPO_AUDIT_NAME = "行业总监商机审核";// 行业总监商机审核
    public static final String TASK_TYPE_SUPPORT_LEADER_DISTRIBUTE_NAME = "支撑经理团队长指派支撑经理";// 支撑经理团队长指派支撑经理
    public static final String TASK_TYPE_PROV_SUPPORT_NAME = "省分支撑经理支撑";// 省份支撑经理支撑
    public static final String TASK_TYPE_PROV_SUPPORT_SUPPORT_NAME = "上级支撑经理支持";// 省份支撑经理支持
    public static final String TASK_TYPE_CUST_OPPO_EDIT_NAME = "客户经理商机修改";// 客户经理商机修改
    public static final String TASK_TYPE_OPPO_STOP_NAME = "商机废弃副总监审核";// 商机废弃副总监审核
    public static final String TASK_TYPE_OPPO_DIRECTOR_STOP_NAME = "商机废弃总监审核"; // 商机废弃审核
    public static final String TASK_TYPE_OPPO_BID_AUDIT_NAME = "投标审核";// 投标审核
    public static final String TASK_TYPE_OPPO_BID_ZHONGBIAO_NAME = "中标审核";// 投标信息修改为中标审核
    public static final String TASK_TYPE_OPPO_BID_WEIZHONGBIAO_NAME = "未中标审核";// 投标信息修改为未中标审核
    public static final String TASK_TYPE_OPPO_BID_LIUBIAO_NAME = "流标审核";
    public static final String TASK_TYPE_OPPO_BID_FEIBIAO_NAME = "废标审核";
    public static final String TASK_TYPE_OPPO_BID_DAFEN_NAME = "投标终结支撑总监打分";// 投标审核
    public static final String TASK_TYPE_OPPO_CONTRI_NAME = "支撑贡献度任务";// 投标审核

    public static final String TASK_TYPE_PROJECT_PRE_EVALUATE_NAME = "投资管理员项目前评估";

    public static final String TASK_TYPE_ORDER_SITE_EXPLANATION_NAME = "现场交流"; // 讲解
    public static final String TASK_TYPE_ORDER_EXPLANATION_MATERIAL_NAME = "制作讲解材料"; // 讲解材料
    public static final String TASK_TYPE_ORDER_XCJB_NAME = "现场讲标"; // 将标
    public static final String TASK_TYPE_ORDER_BSZZ_NAME = "标书准备"; // 投标材料
    public static final String TASK_TYPE_ORDER_BSZZ_AUDIT_NAME = "标书准备审核"; // 标书准备行业总监审核
    public static final String TASK_TYPE_ORDER_BQH_AUDIT_NAME = "标前会审审核";// 标前会审行业总监审核
    public static final String TASK_TYPE_ORDER_CENTER_NAME = "体验中心参观";// 参观中心活动名称
    public static final String WIDE_WARNING_DEAL_URL = "/wideWarning/deal?orderId=";
    // helong 添加

    // 省份支撑经理
    public static final String PROV_SUPPORT_USER = "prov_support_user";

    // 团队角色 member_role
    public static final String MEMBER_ROLE_KHJL = "member_role_khjl";
    public static final String MEMBER_ROLE_XMJL = "member_role_xmjl";
    public static final String MEMBER_ROLE_ZCJL = "member_role_zcjl";
    public static final String MEMBER_ROLE_TZGL = "member_role_tzgl";
    public static final String MEMBER_ROLE_SFZC = "member_role_sfzc";

    // 客户经理工单类型 cust_manager_order
    // public static final String CUST_MANAGER_ORDER_CHECK_DESIGNER =
    // "order_check_designer"; //支撑经理
    public static final String CUST_MANAGER_ORDER_DESIGN_PLAN = "support_pro_maintain"; // 支撑方案
    public static final String CUST_MANAGER_ORDER_SITE_EXPLANATION = "order_site_explanation"; // 现场讲解
    public static final String CUST_MANAGER_ORDER_EXPLANATION_MATERIAL = "order_explanation_material"; // 讲解材料
    public static final String CUST_MANAGER_ORDER_XCJB = "order_xcjb"; // 现场讲标
    public static final String CUST_MANAGER_ORDER_BSZZ = "order_bszz"; // 标书准备
    public static final String CUST_MANAGER_ORDER_CENTER = "order_center";// 参观中心活动
    public static final String CUST_MANAGER_ORDER = "cust_manager_order"; // 客户经理工单
    public static final String CUST_MANAGER_ORDER_COMMUNICATE = "order_communicate";// 沟通协调
    public static final String CUST_MANAGER_ORDER_AUDIT = "order_audit";// 标前评审
    public static final String CUST_MANAGER_ORDER_SUPPORT = "order_support";// 资质支撑
    public static final String CUST_MANAGER_ORDER_PRICE_CHECK = "order_price_check";// 投标询价
    public static final String CUST_MANAGER_ORDER_TECH_CHECK = "order_tech_check";// 技术交底

    public static final String CUST_MANAGER_ORDER_PRODUCT_BU = "order_product_bu";// 产品支撑（工业互联网BU）
    public static final String CUST_MANAGER_ORDER_THECASE_BU = "order_thecase_bu";// 全案支撑（工业互联网BU）
    public static final String CUST_MANAGER_ORDER_NEED_BU = "order_need_bu";// 需求挖掘（工业互联网BU）
    public static final String CUST_MANAGER_ORDER_SCHEME_BU = "order_scheme_bu";// 方案支撑（工业互联网BU）
    public static final String CUST_MANAGER_ORDER_SPECIALIST_BU = "order_specialist_bu";// 专家演讲支撑（工业互联网BU）
    public static final String CUST_MANAGER_ORDER_SUPPORT_BSSH = "order_support_bssh";//标书审核
    public static final String CUST_MANAGER_ORDER_SUPPORT_XMSS = "order_support_xmss";// 项目实施

    public static final String CUST_MANAGER_ORDER_PRODUCT_BU_NAME = "产品支撑（工业互联网BU）";
    public static final String CUST_MANAGER_ORDER_THECASE_BU_NAME = "全案支撑（工业互联网BU）";
    public static final String CUST_MANAGER_ORDER_NEED_BU_NAME = "需求挖掘（工业互联网BU）";
    public static final String CUST_MANAGER_ORDER_SCHEME_BU_NAME = "方案支撑（工业互联网BU）";
    public static final String CUST_MANAGER_ORDER_SPECIALIST_BU_NAME = "专家演讲支撑（工业互联网BU）";

    public static final String CUST_MANAGER_SUPPORT_INITACTIVE_NAME = "主动支撑"; //支撑经理工单
    public static final String CUST_MANAGER_SUPPORT = "客户经理创建支撑工单"; //客户经理工单

    // 商机活动活动类型
    public static final String ACTIVITY_TASK_NORMAL = "activity_task_normal"; // 普通活动
    public static final String ACTIVITY_TASK_CONFIRM_DESIGNER = "activity_task_confirm_designer"; // 确定支撑经理
    public static final String ACTIVITY_TASK_MAINTAIN = "activity_task_maintain"; // 支撑经理制定方案
    public static final String ACTIVITY_TASK_TEAM = "activity_task_team"; // 团队建设
    public static final String ACTIVITY_TASK_PRO_SUPPORT = "activity_task_pro_support"; // 提交上级支持

    public static final String ACTIVITY_TASK_SITE_EXPLANATION = "activity_task_site_explanation"; // 现场讲解
    public static final String ACTIVITY_TASK_ORDER_EXPLANATION_MATERIAL = "order_explanation_material"; // 制作讲解材料
    public static final String ACTIVITY_TASK_ORDER_XCJB = "order_xcjb"; // 现场讲标
    public static final String ACTIVITY_TASK_ORDER_BSZZ = "order_bszz"; // 标书准备
    public static final String ACTIVITY_TASK_YB = "activity_task_yb"; // 应标
    public static final String ACTIVITY_TASK_ZSS = "activity_task_zss"; // 转实施
    public static final String ACTIVITY_TASK_GCBF = "activity_task_gcbf"; // 高层拜访
    public static final String ACTIVITY_TASK_BQH = "activity_task_bqh"; // 标前会
    public static final String ACTIVITY_TASK_SCKCD = "activity_task_sckcd"; // 上传勘察单
    public static final String ACTIVITY_TASK_SJYJ = "oppo_to_custManager"; // 商机移交
    public static final String ACTIVITY_TASK_SJZJ = "oppo_stop"; // 商机废弃
    public static final String ACTIVITY_TASK_YXDC = "oppo_yixiang_dc"; // 意向达成
    public static final String ACTIVITY_TASK_CENTER = "activity_task_center";// 参观中心活动

    public static final String DECISION_COMPANY_SHENG = "province_industry_manager";// 省公司行业部经理
    public static final String DECISION_COMPANY_SHI = "city_industry_manager";// 省公司行业部经理
    public static final String DECISION_COMPANY_XIAN = "county_manager";// 省公司行业部经理

    /*
     * oppo_state 商机状态 线索录入：线索录入即亮 商机录入：商机录入亮 oppo_status_new 商机跟踪：审核通过之后就亮
     * oppo_status_genzong 商机转换：增加了“标前会审”活动就开始亮，详细内容为 标前会审到“转实施”之间的活动内容
     * oppo_status_zhuanhuan 项目实施：转实施后即亮，展示内容为立项信息 oppo_status_shishi
     * 签订合同：转实施之后即亮，展示内容为合同签订信息 oppo_status_hetong 达产：有了收入预估后即亮，展示内容为收入预估信息
     * oppo_status_dachan 终结：商机废弃即亮，展示内容为终结原因内容 oppo_status_end
     */
//	public static final String OPP_STATE_DRAFT = "oppo_status_draft"; // 商机草稿
//	public static final String OPP_STATE_NEW = "oppo_status_new"; // 商机录入
//	public static final String OPP_STATE_GENZONG = "oppo_status_genzong"; // 商机跟踪
//	public static final String OPP_STATE_EDITINFO = "oppo_status_editinfo";// 修改信息
//	public static final String OPP_STATE_ZHUANHUAN = "oppo_status_zhuanhuan"; // 商机转换
//	public static final String OPP_STATE_SHISHI = "oppo_status_shishi"; // 项目实施
//	public static final String OPP_STATE_HETONG = "oppo_status_hetong"; // 签订合同
//	public static final String OPP_STATE_DACHAN = "oppo_status_dachan"; // 达产
//	public static final String OPP_STATE_END = "oppo_status_end"; // 终结
    /**
     * 商机生命周期改造
     * 创建商机	oppo_status_cjsj
     * 商机支撑	oppo_status_sjzc
     * 招投标	oppo_status_ztb
     * 商机签约	oppo_status_sjqy
     * 商机实施  oppo_status_sjss
     * 商机达产	oppo_status_sjdc
     * 商机终止	oppo_status_end
     */
    public static final String OPPO_STATUS_CJSJ = "oppo_status_cjsj";
    public static final String OPPO_STATUS_SJZC = "oppo_status_sjzc";
    public static final String OPPO_STATUS_ZTB = "oppo_status_ztb";
    public static final String OPPO_STATUS_SJQY = "oppo_status_sjqy";
    public static final String OPPO_STATUS_SJSS = "oppo_status_sjss";
    public static final String OPPO_STATUS_SJDC = "oppo_status_sjdc";
    public static final String OPPO_STATUS_END = "oppo_status_end";

    /**
     * 商机阶段
     */
    public static final String OPP_PHASE_NEW_CREATE = "new_create";// 新建
    public static final String OPP_PHASE_AUDIT_FINISH = "audit_finish";// 评审通过
    public static final String OPP_PHASE_PLAN_FINISH = "report_plan_finish";// 方案制定完成
    public static final String OPP_PHASE_PROJECT_FINISH = "project_finish";// 立项
    public static final String OPP_PHASE_CONTRACT_FINISH = "contract_finish";// 合同签订
    public static final String OPP_PHASE_PROJECT_PRODUCING = "project_producing";// 达产
    public static final String OPP_PHASE_OPPO_END = "oppo_end";// 终结 2017-10-31
    /**
     * 商机状态 oppo的审核状态auditStatus
     */
    public static final String OPP_STATUS_NEW = "opp_new"; // 新建
    public static final String OPP_STATUS_PASS = "opp_pass"; // 行业副总监审核通过
    public static final String OPP_STATUS_NOTPASS = "opp_notpass"; // 行业副总监审核不通过
    public static final String OPP_STATUS_DIRECTOR_PASS = "opp_director_pass"; // 行业总监审核通过
    public static final String OPP_STATUS_DIRECTOR_NOTPASS = "opp_director_notpass"; // 行业总监审核不通过
    public static final String OPP_STATUS_SUPPORT = "opp_support"; // 客户经理商机跟踪
    public static final String OPP_STATUS_EDITINFO = "opp_editinfo";// 修改信息
    public static final String OPP_STATUS_CHECK_DESIGNER = "opp_status_check_designer"; // 分配支撑经理中
    public static final String OPP_STATUS_DESIGNER = "opp_designer"; // 分配支撑经理
    public static final String OPP_STATUS_DESIGNER_END = "opp_designer_end"; // 方案制定中
    public static final String OPP_STATUS_DESIGN_PLAN_END = "opp_design_plan_end"; // 方案已提交
    public static final String OPP_STATUS_PROV_SUPPORT = "opp_prov_support";// 省份支撑经理分配支撑人员
    public static final String OPP_STATUS_PROJECT = "opp_project"; // 立项
    public static final String OPP_STATUS_PROJECT_END = "opp_peoject_end"; // 立项完成
    public static final String OPP_STATUS_END = "opp_end"; // 结束
    public static final String OPP_STATUS_STOP = "opp_stop"; // 终结

    public static final String CLUE_SOURCE = "clue_source";// 商机来源
    public static final String OPP_TYPE = "opp_type";// 业务类型
    public static final String OPP_TYPE_2 = "opp_type_2";// 业务类型
    // 创新头条商机状态
    public static final String TYPE_FQ = "state_fq_sffq";// 废弃
    public static final String TYPE_LR = "state_lr";// 商机录入
    public static final String TYPE_SFPS = "state_sfps";// 评审通过
    public static final String TYPE_YQY = "state_yqy_sfqy";// 已签约
    public static final String TYPE_ZCWC = "state_zcwc";// 本省支撑完成
    public static final String TYPE_ZCZ = "state_zcz";// 本省支撑中

    public static final String CXTT_OPPO = "oppo_test";
    // 线索状态
    public static final String CLUE_STATE_NEW = "new_create"; // 线索新建
    public static final String CLUE_STATE_ASSIGN_CUST_MANAGER = "stateAssignCustManager"; // 分配客户经理
    public static final String CLUE_STATE_ASSIGN_GRID_MANAGER = "stateAssignGridManager"; // 分配网格经理
    public static final String CLUE_STATE_CLUE_TRACK = "stateClueTrack"; // 线索跟踪
    public static final String CLUE_STATE_BECOMEOPP = "become_opp"; // 转商机中
    public static final String CLUE_STATE_FINISH = "audit_finish"; // 评审通过(转商机)

    // 分公司
    public static final int FLOW_ROLE_ICT_ZHUGUAN = 211;// 集成ICT收入主管
    public static final int USER_ROLE_LINGDAO = 198;// 分公司领导
    public static final int USER_ROLE_ZHUREN = 193;// 分公司集客主任 政企部主任 集客主任 分公司政企部主任
    public static final int USER_ROLE_WGJL = 192; // 分公司网格经理
    public static final int USER_ROLE_PROJECT_MANAGER_NEW = 186;// 项目经理

    // 业务类型
    public static final String BUSINESS_TYPE_ICT = "opp_type_ICT"; // ICT

    // 省公司
    public static final int USER_ROLE_PROV_OPP_MANAGER = 196;// 省公司商机管理员
    public static final int USER_ROLE_BUSINESS_MANAGER = 204;// 业务主管
    public static final int USER_ROLE_PROV_MANAGER = 197;// 省公司支撑室主任（夏主任）
    public static final int USER_ROLE_XMJL_MANAGER = 213;// 项目经理主任
    public static final int USER_ROLE_PROV_DISIGNER_MANAGER = 203;// 省支撑主管

    // 二期迭代新增角色
    public static final int USER_ROLE_ZTJK_S = 224; // 中台监控（省）
    public static final int USER_ROLE_JCZJL = 223; // 集成总经理
    public static final int USER_ROLE_JCFZ = 222; // 集成副总 系统集成分管副总

    public static final String TASK_TYPE_SHENQINGZHICHENG = "task_type_shenqingzhicheng"; // 申请支撑
    public static final String TASK_TYPE_SUPPORT_ZDD = "prov_support_zdd"; // 省公司支撑总调度指派
    public static final String TASK_TYPE_SUPPORT_ZCZRZP = "prov_support_zczrzp"; // 省公司支撑支撑主任指派
    public static final String TASK_TYPE_SUPPORT_ZCJLZP = "prov_support_zcjlzp"; // 省公司支撑经理处理
    public static final String TASK_TYPE_ZHAIPAIZHICHENG = "task_type_zhaipaizhicheng"; // 摘牌支撑

    // 添加商机阶段
    public static final String OPP_PHASE_BAOBEI = "new_create_opp";// 商机报备
    public static final String OPP_PHASE_MINGQUE = "ming_que_opp";// 明确商机
    public static final String OPP_PHASE_TIJIAOFANGAN = "ti_jiao_fang_an_opp";// 提交方案
    public static final String OPP_PHASE_CONTRACT = "contract_opp";// 全部签约
    public static final String OPP_PHASE_PART_CONTRACT = "contract_part_opp";// 部分签约
    public static final String OPP_PHASE_SHISHI = "shi_shi_opp";// 实施交付
    public static final String OPP_PHASE_ZHUANHUAN = "zhuan_huan_opp";// 商机转换
    public static final String OPP_PHASE_QIANYUE = "qian_yue";// 签约
    // 支撑工作录入选择签约阶段或者中标
    public static final String OPP_PHASE_DACHAN = "project_producing";// 达产
    public static final String OPP_PHASE_END = "oppo_end"; // 商机废弃
    public static final String OPP_PHASE_GONGBI = "gong_bi"; // 工毕
    public static final String OPP_PHASE_ZHAOTOUBIAO = "zhao_tou_biao"; // 招投标
    public static final String OPP_PHASE_SHOUHOU = "shou_hou"; // 售后

    //是否全省通用角色
    public static final Integer SYS_ROLE_IS_UNIVERSAL_TRUE = 0;  //是
    public static final Integer SYS_ROLE_IS_UNIVERSAL_FLASE = 1;  //否

    public static final String ORDER_DEAL_URL = "/order/toOrderDeal?urlType=deal&orderId=";
    public static final String INFOSEND_DEAL_URL = "/infoSend/toDeal?urlType=deal&orderId=";
    public static final String RESPONSE_BID_DEAL_URL = "/responseBid/toBidDeal?urlType=deal&orderId=";
    public static final String CONTRACT_AUDIT_URL = "/contract/toAuditContract?urlType=deal&orderId=";
    public static final String OPP_END_AUDIT_URL = "/oppEndAudit/toOppDeal?urlType=deal&orderId=";
    public static final String OWE_WARNING_DEAL_URL = "/oweWarning/deal?orderId=";
    public static final String CONTRACT_DUE_WARNING_DEAL_URL = "/contractDueWarning/deal?orderId=";
    public static final String HMD_WARNING_DEAL_URL = "/userCommonWarning/deal?orderId=";
    public static final String UC_WARNING_DEAL_URL = "/userCustWarning/deal?orderId=";
    public static final String LADER_DEAL_URL = "/order/toLeaderDeal?orderId=";
    public static final String HMD_DEAL_URL = "/order/toLeaderDealHmd?orderId=";
    public static final String CEA_WARNING_DEAL_URL = "/cloudExpireAlarm/deal?orderId=";
    public static final String CR_WARNING_DEAL_URL = "/custReceiptWarning/deal?orderId=";
    public static final String CO_WARNING_DEAL_URL = "/custCutOverWarning/deal?orderId=";
    public static final String DEDICATED_CIRCUIT_DEAL_URL = "/dedicatedCircuit/deal?orderId=";
    public static final String LEASELINE_RELET_DEAL_URL = "/leaselineReletCustWarn/deal?orderId=";
    public static final String ICT_ARREARS_WARNING_DEAL_URL = "/userCustIctWarningAction/deal?orderId=";

    public static final String UC_WARNING_AUDIT_DEAL_URL = "/userCustWarning/auditDeal?orderId=";
    public static final String SUPPORT_ORDER_AUDIT_URL = "/custManagerOrder/toSupportWorkAudit?urlType=deal&orderId=";

    public static final String APPLAY_SUPPORT_DEAL_URL = "/applayTransformSupport/toApplaySupportDeal?urlType=deal&orderId=";
    public static final String APPLAY_CHANGE_SUPPORT_DEAL_URL = "/applayTransformSupport/toApplayChangeSupport?urlType=deal&orderId=";
    public static final String CLUE_ASSIGN_DEAL_URL = "/clueAssign/toDeal?urlType=deal&orderId=";
    public static final String OPPO_AUDIT_INSPECTOR_URL = "/oppoProcess/toOppoAudit?orderId=";
    public static final String OPPO_AUDIT_CUSTMANAGER_URL = "/oppoProcess/toOppoModify?orderId=";
    public static final String OPPO_INPUT_CUSTMANAGER_URL = "/oppoProcess/toOppoInput?orderId=";
    public static final String OPPO_MODIFY_INSPECTOR_URL = "/oppoProcess/toOppoModifyAudit?orderId=";
    public static final String URGE_ORDER_DEAL_URL = "/order/toUrgeOrderDeal?orderId=";

    public static final String CUST_POSITION_DEAL_URL = "/custPosition/toTaskDeal?orderId=";

    public static final String ACT_BUSINESS_ACCEPTANCE_URL = "/businessAcceptance/toBusinessAccept?urlType=deal&orderId=";

    public static final String INCOME_WARNING_DEAL_URL = "/incomeWarning/deal?orderId=";

    // 钉钉，pc form
    public static final String FORM_PC = "pc";
    public static final String FORM_DD = "dd";
    public static final String FORM_H5 = "h5";
    // 任务info_type
    public static final String TASK_TYPE_OPPO = "oppo";
    public static final String TASK_TYPE_ORDER = "order";
    public static final String TASK_TYPE_INFOSEND = "infoSend";
    public static final String TASK_TYPE_CLUE = "clue";
    public static final String TASK_TYPE_CONTRACT = "contract";
    public static final String TASK_TYPE_SUPPORT = "support";
    public static final String TASK_TYPE_BID = "bid";
    public static final String TASK_TYPE_BUSINESS_ACCEPTANCE = "businessAcceptance";
    //
    public static final String PROCESS_KEY = "process_key";
    public static final String TASK_DEF_KEY = "task_def_key";
    // Activiti中bpmn文件的id
    public static final String ACT_SMALLROUTINE = "small_routine";// 中台模式
    public static final String ACT_SMALLROUTINE_NAME = "小程序甩单";
    public static final String ACT_CLOUDY = "cloudy";
    public static final String ACT_CLOUDY_NAME = "多云";
    public static final String ACT_CFBB = "cfbb";
    public static final String ACT_CFBB_NAME = "电路光纤宽带";
    public static final String ACT_JINGHUABIGDATA = "jinghuabigdata";
    public static final String ACT_JINGHUABIGDATA_NAME = "金华大数据";
    public static final String ACT_LOCALELECTRICRESOURCE = "localElectricResource";
    public static final String ACT_LOCALELECTRICRESOURCE_NAME = "本地电路资源核查";
    public static final String ACT_LOCALELECTRICREOPEN = "localElectricOpen";
    public static final String ACT_LOCALELECTRICREOPEN_NAME = "本地电路开通";
    public static final String ACT_YXTFJ = "yxtfj";
    public static final String ACT_YXTFJ_NAME = "云犀停复机";


    public static final String ACT_ZIGBEE = "zigbee";
    public static final String ACT_ZIGBEE_NAME = "物联网";
    public static final String ACT_PPPOE = "PPPOE";
    public static final String ACT_PPPOE_NAME = "宽带拨号";
    public static final String ACT_NEWJOB = "newjob";
    public static final String ACT_NEWJOB_NAME = "本级渠道工号新建";
    public static final String ACT_REDLIST = "redlist";
    public static final String ACT_REDLIST_NAME = "红名单信控";
    public static final String ACT_NOAPP = "noapp";
    public static final String ACT_NOAPP_NAME = "靓号申请";
    public static final String ACT_INFO_SEND = "intelli_recommend_info_send";
    public static final String ACT_INFO_SEND_NAME = "智能信息推荐派发";
    public static final String ACT_CLUE_ASSIGN = "clue_assign";
    public static final String ACT_CLUE_ASSIGN_NAME = "线索指派";
    public static final String ACT_OPPO_AUDIT = "oppo_audit";
    public static final String ACT_OPPO_AUDIT_NAME = "商机审核";
    public static final String ACT_OPPO_MODIFY = "oppo_modify";
    public static final String ACT_OPPO_MODIFY_NAME = "商机修改";
    public static final String ACT_CONTRACT_AUDIT = "contract_audit";
    public static final String ACT_CONTRACT_AUDIT_NAME = "合同审核";
    public static final String ACT_SUPPORT_ORDER = "support_order";
    public static final String ACT_SUPPORT_ORDER_NAME = "支撑订单录入";
    public static final String ACT_RESPONSE_BID = "response_bid";
    public static final String ACT_RESPONSE_BID_NAME = "应标";
    public static final String ACT_OPP_END_AUDIT = "opp_end_audit";
    public static final String ACT_OPP_END_AUDIT_NAME = "商机废弃审核";
    public static final String ACT_URGE_ORDER = "urge_order";
    public static final String ACT_URGE_ORDER_NAME = "催单";
    public static final String ACT_CUST_POSITION_ENTER = "cust_position_enter";
    public static final String ACT_CUST_POSITION_ENTER_NAME = "客户位置信息维护";
    public static final String ACT_MNP = "mnp";
    public static final String ACT_MNP_NAME = "携号转网";
    public static final String ACT_BUSINESS_ACCEPTANCE = "business_acceptance";
    public static final String ACT_BUSINESS_ACCEPTANCE_NAME = "业务受理";
    public static final String ACT_GENERAL_ORDER_SUPPORT = "general_order_support";
    public static final String ACT_GENERAL_ORDER_SUPPORT_NAME = "通用需求订单";
    public static final String ACT_PERSONNEL_SPEC = "personnel_spec";
    public static final String ACT_PERSONNEL_SPEC_NAME = "人员资质申请流程";
    public static final String ACT_COMPANY_QUALIFICATION_APPLY = "companyQualificationApply";
    public static final String ACT_COMPANY_QUALIFICATION_APPLY_NAME = "公司资质申请流程";
    public static final String ACT_GXKD = "gxkd";
    public static final String ACT_GXKD_NAME = "光纤宽带";
    public static final String ACT_PAYMENT = "payment";
    public static final String ACT_PAYMENT_NAME = "趸交款导入流程";
    public static final String ACT_MAIN_CIRCUIT = "mainCircuit";
    public static final String ACT_MAIN_CIRCUIT_NAME = "主发电路实施流程";
    public static final String ACT_YWZF = "ywzf";
    public static final String ACT_YWZF_NAME = "业务资费流程";
    public static final String ACT_MARKET_APPROVE = "market_approve";
    public static final String ACT_MARKET_APPROVE_NAME = "营销任务审批流程";

    public static final String ACT_PARTNER_PROFESSIONAL_CLEAR = "partnerProfessionalClear";
    public static final String ACT_PARTNER_PROFESSIONAL_CLEAR_NAME = "入围专业清退流程";
    public static final String ACT_PARTNER_CLEAR_NAME = "合作伙伴清退流程";
    public static final String ACT_PARTNER_PROFESSIONAL_EDIT = "partnerProfessionalEdit";
    public static final String ACT_PARTNER_PROFESSIONAL_EDIT_NAME = "入围专业修改流程";
    public static final String ACT_PARTNER_PROFESSIONAL_ADD = "partnerProfessionalAdd";
    public static final String ACT_PARTNER_PROFESSIONAL_ADD_NAME = "入围专业新增流程";
    public static final String ACT_PARTNER_ITEM = "partnerItem";
    public static final String ACT_PARTNER_ITEM_NAME = "中选项目评价流程";

    // 小程序甩单
    public static final String ACT_PHASE_SMAll_OPEN_ROUTINE = "front_desk_open_routine";
    public static final String ACT_PHASE_SMAll_OPEN_ROUTINE_NAME = "前台受理开通甩单";
    public static final String ACT_PHASE_SMAll_BUSINESS_DISPATCH = "middle_desk_business_dispatch";
    public static final String ACT_PHASE_SMAll_BUSINESS_DISPATCH_NAME = "中台营业员调度";
    public static final String ACT_PHASE_SMAll_BUSINESS_ACCPET = "middle_desk_business_accept";
    public static final String ACT_PHASE_SMAll_BUSINESS_ACCPET_NAME = "中台营业员业务受理";
    public static final String ACT_PHASE_SMAll_BUSINESS_OPEN = "middle_desk_business_open";
    public static final String ACT_PHASE_SMAll_BUSINESS_OPEN_NAME = "中台营业员业务开通";
    public static final String ACT_PHASE_SMAll_RELATE_CONTRACT = "middle_desk_relate_contract";
    public static final String ACT_PHASE_SMAll_RELATE_CONTRACT_NAME = "中台营业员关联合同";

    // 多云
    public static final String ACT_PHASE_CLOUDY_OPPO_SUBMIT = "cloudy_oppo_submit";
    public static final String ACT_PHASE_CLOUDY_OPPO_SUBMIT_NAME = "商机提交";
    public static final String ACT_PHASE_CLOUDY_PRESALE_SUPPORT = "cloudy_presale_support";
    public static final String ACT_PHASE_CLOUDY_PRESALE_SUPPORT_NAME = "售前支撑";
    public static final String ACT_PHASE_CLOUDY_PRESALE_SUPPORT_CONFIRM = "cloudy_presale_support_confirm";
    public static final String ACT_PHASE_CLOUDY_PRESALE_SUPPORT_CONFIRM_NAME = "客户经理售前确认";
    public static final String ACT_PHASE_CLOUDY_DISPATCH = "cloudy_dispatch";
    public static final String ACT_PHASE_CLOUDY_DISPATCH_NAME = "调度岗";
    public static final String ACT_PHASE_CLOUDY_LIST_CONFIRM_SUB_PROCESS = "cloudy_list_confirm_sub_process";
    public static final String ACT_PHASE_CLOUDY_LIST_CONFIRM = "cloudy_list_confirm";
    public static final String ACT_PHASE_CLOUDY_LIST_CONFIRM_NAME = "名单确认";
    public static final String ACT_PHASE_CLOUDY_CONTRACT_COST_CONFIRM = "cloudy_contract_cost_confirm";
    public static final String ACT_PHASE_CLOUDY_CONTRACT_COST_CONFIRM_NAME = "合同定稿";
    public static final String ACT_PHASE_CLOUDY_ERP_CONTRACT_SIGN = "cloudy_erp_contract_sign";
    public static final String ACT_PHASE_CLOUDY_ERP_CONTRACT_SIGN_NAME = "ERP合同签订";
    public static final String ACT_PHASE_CLOUDY_CONTRACT_FILE = "cloudy_contract_file";
    public static final String ACT_PHASE_CLOUDY_CONTRACT_FILE_NAME = "合同归档";
    public static final String ACT_PHASE_CLOUDY_SYS_FORM = "cloudy_sys_form";
    public static final String ACT_PHASE_CLOUDY_SYS_FORM_NAME = "系统录单";
    public static final String ACT_PHASE_CLOUDY_EXPECT_INVOICE_JUDGE = "cloudy_expect_invoice_judge";
    public static final String ACT_PHASE_CLOUDY_EXPECT_INVOICE_JUDGE_NAME = "预打发票判定";
    public static final String ACT_PHASE_CLOUDY_INVOICE_DEAL = "cloudy_invoice_deal";
    public static final String ACT_PHASE_CLOUDY_INVOICE_DEAL_NAME = "发票处理";
    public static final String ACT_PHASE_CLOUDY_PAY = "cloudy_pay";
    public static final String ACT_PHASE_CLOUDY_PAY_NAME = "收款缴费";
    public static final String ACT_PHASE_CLOUDY_PAY_CONFIRM = "cloudy_pay_confirm";
    public static final String ACT_PHASE_CLOUDY_PAY_CONFIRM_NAME = "确认缴费销账";
    public static final String ACT_PHASE_CLOUDY_BUSINESS_OPEN = "cloudy_business_open";
    public static final String ACT_PHASE_CLOUDY_BUSINESS_OPEN_NAME = "业务开通";
    public static final String ACT_PHASE_CLOUDY_GET_FORM_PRIZE = "cloudy_get_form_prize";
    public static final String ACT_PHASE_CLOUDY_GET_FORM_PRIZE_NAME = "提单并领奖";
    public static final String ACT_PHASE_CLOUDY_MANUFACTURER = "cloudy_manufacturer";
    public static final String ACT_PHASE_CLOUDY_MANUFACTURER_NAME = "厂家制作对接";
    public static final String ACT_PHASE_CLOUDY_APP_GO = "cloudy_app_go";
    public static final String ACT_PHASE_CLOUDY_APP_GO_NAME = "小程序上线";
    public static final String ACT_PHASE_CLOUDY_FINAL_AUDIT = "cloudy_final_audit";
    public static final String ACT_PHASE_CLOUDY_FINAL_AUDIT_NAME = "结束审核";

    //本地电路资源核查
    public static final String ACT_PHASE_RESOURCEAPPLY = "resourceapply";
    public static final String ACT_PHASE_RESOURCEAPPLY_NAME = "本地电路资源核查订单开通";
    public static final String ACT_PHASE_RESOURCEAPPLY_ASSIGN_NAME = "资源核查审核处理";
    public static final String ACT_PHASE_RESOURCEAPPLY_ASSIGN = "resourcehandle";
    public static final String ACT_PHASE_RESOURCEAPPLY_CONFIRM = "custmanagerconfirm";
    public static final String ACT_PHASE_RESOURCEAPPLY_CONFIRM_NAME = "客户经理确认";

    //本地电路开通
    public static final String ACT_PHASE_OPENAPPLY = "bussyopenapply";
    public static final String ACT_PHASE_OPENAPPLY_NAME = "本地电路开通";
    public static final String ACT_PHASE_OPENAPPLY_ASSIGN_NAME = "资源本地电路开通处理";
    public static final String ACT_PHASE_OPENAPPLY_ASSIGN = "bussyopenhandle";
    public static final String ACT_PHASE_OPENAPPLY_CONFIRM = "custmanagerconfirmkt";
    public static final String ACT_PHASE_OPENAPPLY_CONFIRM_NAME = "客户经理确认";


    //金华大数据
    public static final String ACT_PHASE_JINGHUABIGDATA_OPPO_SUBMIT = "jinghuabigdata_oppo_submit";
    public static final String ACT_PHASE_JINGHUABIGDATA_OPPO_SUBMIT_NAME = "大数据(金华)开通订单";
    public static final String ACT_PHASE_JINGHUABIGDATA_OPPO_SUBMIT_ASSIGN_NAME = "调度岗指派";
    public static final String ACT_PHASE_JINGHUABIGDATA_OPPO_SUBMIT_ASSIGN = "jinghuabigdata_oppo_submit_diaodugang";
    public static final String ACT_PHASE_JINGHUABIGDATA_MINGDANQUEREN = "jinghuabigdata_oppo_submit_mingdanqueren";
    public static final String ACT_PHASE_JINGHUABIGDATA_MINGDANQUEREN_NAME = "名单确认";
    public static final String ACT_PHASE_JINGHUABIGDATA_ZFSQNRSH = "jinghuabigdata_oppo_submit_zfsqnrsh";
    public static final String ACT_PHASE_JINGHUABIGDATA_ZFSQNRSH_NAME = "资费申请及内容审核";
    public static final String ACT_PHASE_JINGHUABIGDATA_HETONGDINGGAO = "jinghuabigdata_oppo_submit_hetongdinggao";
    public static final String ACT_PHASE_JINGHUABIGDATA_HETONGDINGGAO_NAME = "合同定稿";

    public static final String ACT_PHASE_JINGHUABIGDATA_LINGDAOSHENHE = "jinghuabigdata_oppo_submit_leaderAudit";
    public static final String ACT_PHASE_JINGHUABIGDATA_LINGDAOSHENHE_NAME = "领导审核";

    public static final String ACT_PHASE_JINGHUABIGDATA_KEHUGAIZHANG = "jinghuabigdata_oppo_submit_kehugaizhang";
    public static final String ACT_PHASE_JINGHUABIGDATA_KEHUGAIZHANG_NAME = "客户盖章";
    public static final String ACT_PHASE_JINGHUABIGDATA_HTGD = "jinghuabigdata_oppo_submit_hetongguidang";
    public static final String ACT_PHASE_JINGHUABIGDATA_HTGD_NAME = "合同归档";
    public static final String ACT_PHASE_JINGHUABIGDATA_YWSL = "jinghuabigdata_oppo_submit_yewushouli";
    public static final String ACT_PHASE_JINGHUABIGDATA_YWSL_NAME = "业务受理";
    public static final String ACT_PHASE_JINGHUABIGDATA_SFJK = "jinghuabigdata_oppo_submit_shoufeijiaokuan";
    public static final String ACT_PHASE_JINGHUABIGDATA_SFJK_NAME = "收费交款";
    public static final String ACT_PHASE_JINGHUABIGDATA_XTJFQR = "jinghuabigdata_oppo_submit_xitongjiaofeiqueren";
    public static final String ACT_PHASE_JINGHUABIGDATA_XTJFQR_NAME = "系统缴费确认";
    public static final String ACT_PHASE_JINGHUABIGDATA_YWKT = "jinghuabigdata_oppo_submit_yewukaitong";
    public static final String ACT_PHASE_JINGHUABIGDATA_YWKT_NAME = "业务开通";
    public static final String ACT_PHASE_JINGHUABIGDATA_JIESHU = "jinghuabigdata_oppo_submit_jieshu";
    public static final String ACT_PHASE_JINGHUABIGDATA_JIESHU_NAME = "结束";

    // 电路光纤业务流程
    public static final String ACT_PHASE_CFBB_APPLY = "act_task_cfbb_apply";
    public static final String ACT_PHASE_CFBB_APPLY_NAME = "发起电路光纤宽带开通订单";
    public static final String ACT_PHASE_CFBB_ASSIGN = "act_task_cfbb_assign";
    public static final String ACT_PHASE_CFBB_ASSIGN_NAME = "调度岗指派";
    public static final String ACT_PHASE_CFBB_RESOURCE = "act_task_cfbb_resource";
    public static final String ACT_PHASE_CFBB_RESOURCE_NAME = "资源勘查";
    public static final String ACT_PHASE_CFBB_LIST_CONFIRM = "act_task_cfbb_list_confirm";
    public static final String ACT_PHASE_CFBB_LIST_CONFIRM_NAME = "名单确认";
    public static final String ACT_PHASE_CFBB_FEE_APPLY = "act_task_cfbb_fee_apply";
    public static final String ACT_PHASE_CFBB_FEE_APPLY_NAME = "资费申请";
    public static final String ACT_PHASE_CFBB_AUDIT_CONFIRM = "act_task_cfbb_audit_confirm";
    public static final String ACT_PHASE_CFBB_AUDIT_CONFIRM_NAME = "资费审核确认";
    public static final String ACT_PHASE_CFBB_APPROVAL = "act_task_cfbb_approval";
    public static final String ACT_PHASE_CFBB_APPROVAL_NAME = "立项";
    public static final String ACT_PHASE_CFBB_CONTRACT_FINAL = "act_task_cfbb_contract_final";
    public static final String ACT_PHASE_CFBB_CONTRACT_FINAL_NAME = "合同定稿";
    public static final String ACT_PHASE_CFBB_CONTRACT_SIGN = "act_task_cfbb_contract_sign";
    public static final String ACT_PHASE_CFBB_CONTRACT_SIGN_NAME = "合同签订";
    public static final String ACT_PHASE_CFBB_CONTRACT_FIL = "act_task_cfbb_contract_fil";
    public static final String ACT_PHASE_CFBB_CONTRACT_FIL_NAME = "合同归档";
    public static final String ACT_PHASE_CFBB_ACCEPT = "act_task_cfbb_accept";
    public static final String ACT_PHASE_CFBB_ACCEPT_NAME = "业务受理";
    public static final String ACT_PHASE_CFBB_BILL_CONFIRM = "act_task_cfbb_bill_confirm";
    public static final String ACT_PHASE_CFBB_BILL_CONFIRM_NAME = "运维开通单起租/退租确认";
    public static final String ACT_PHASE_CFBB_BUSSINESS = "act_task_cfbb_bussiness";
    public static final String ACT_PHASE_CFBB_BUSSINESS_NAME = "营业起租";
    public static final String ACT_PHASE_CFBB_VERIFICATION = "act_task_cfbb_verification";
    public static final String ACT_PHASE_CFBB_VERIFICATION_NAME = "业务核查";
    public static final String ACT_PHASE_CFBB_FINAL = "act_task_cfbb_final";
    public static final String ACT_PHASE_CFBB_FINAL_NAME = "结束审核";
    public static final String ACT_PHASE_CFBB_LEADER_AUDIT = "act_task_cfbb_leader_audit";
    public static final String ACT_PHASE_CFBB_LEADER_AUDIT_NAME = "领导审核";


    // 光纤宽带业务流程
    public static final String ACT_PHASE_GXKD_APPLY = "act_task_gxkd_apply";
    public static final String ACT_PHASE_GXKD_APPLY_NAME = "发起光纤宽带开通订单";
    public static final String ACT_PHASE_GXKD_ASSIGN = "act_task_gxkd_assign";
    public static final String ACT_PHASE_GXKD_ASSIGN_NAME = "调度岗指派";
    public static final String ACT_PHASE_GXKD_RESOURCE = "act_task_gxkd_resource";
    public static final String ACT_PHASE_GXKD_RESOURCE_NAME = "资源勘查";
    public static final String ACT_PHASE_GXKD_FEE_APPLY = "act_task_gxkd_fee_apply";
    public static final String ACT_PHASE_GXKD_FEE_APPLY_NAME = "资费申请";
    public static final String ACT_PHASE_GXKD_AUDIT_CONFIRM = "act_task_gxkd_audit_confirm";
    public static final String ACT_PHASE_GXKD_AUDIT_CONFIRM_NAME = "资费审核确认";
    public static final String ACT_PHASE_GXKD_APPROVAL = "act_task_gxkd_approval";
    public static final String ACT_PHASE_GXKD_APPROVAL_NAME = "立项";
    public static final String ACT_PHASE_GXKD_CONTRACT_FINAL = "act_task_gxkd_contract_final";
    public static final String ACT_PHASE_GXKD_CONTRACT_FINAL_NAME = "合同定稿";
    public static final String ACT_PHASE_GXKD_CONTRACT_SIGN = "act_task_gxkd_contract_sign";
    public static final String ACT_PHASE_GXKD_CONTRACT_SIGN_NAME = "合同签订";
    public static final String ACT_PHASE_GXKD_CONTRACT_FIL = "act_task_gxkd_contract_fil";
    public static final String ACT_PHASE_GXKD_CONTRACT_FIL_NAME = "合同归档";
    public static final String ACT_PHASE_GXKD_ZYPZ = "act_task_gxkd_zypz";
    public static final String ACT_PHASE_GXKD_ZYPZ_NAME = "资源配置";
    public static final String ACT_PHASE_GXKD_ACCEPT = "act_task_gxkd_accept";
    public static final String ACT_PHASE_GXKD_ACCEPT_NAME = "业务受理";
    public static final String ACT_PHASE_GXKD_VERIFICATION = "act_task_gxkd_verification";
    public static final String ACT_PHASE_GXKD_VERIFICATION_NAME = "业务稽核";
    public static final String ACT_PHASE_GXKD_FINAL = "act_task_gxkd_final";
    public static final String ACT_PHASE_GXKD_FINAL_NAME = "结束审核";
    public static final String ACT_PHASE_GXKD_LEADER_AUDIT = "act_task_gxkd_leader_audit";
    public static final String ACT_PHASE_GXKD_LEADER_AUDIT_NAME = "领导审核";


    // 物联网
    public static final String ACT_PHASE_ZIGBEE_OPPO_SUBMIT = "zigbee_oppo_submit";
    public static final String ACT_PHASE_ZIGBEE_OPPO_SUBMIT_NAME = "商机提交";
    public static final String ACT_PHASE_ZIGBEE_DISPATCHER = "zigbee_dispatcher";
    public static final String ACT_PHASE_ZIGBEE_DISPATCHER_NAME = "调度岗";
    public static final String ACT_PHASE_ZIGBEE_OPPO_CREATE = "zigbee_oppo_create";
    public static final String ACT_PHASE_ZIGBEE_OPPO_CREATE_NAME = "总部物联网商机创建";
    public static final String ACT_PHASE_ZIGBEE_ID_CREATE = "zigbee_id_create";
    public static final String ACT_PHASE_ZIGBEE_ID_CREATE_NAME = "创建ID及物联网帐户";
    public static final String ACT_PHASE_ZIGBEE_EXPE_APPLY = "zigbee_expe_apply";
    public static final String ACT_PHASE_ZIGBEE_EXPE_APPLY_NAME = "申请体验卡";
    public static final String ACT_PHASE_ZIGBEE_PLS_APPLY = "zigbee_pls_apply";
    public static final String ACT_PHASE_ZIGBEE_PLS_APPLY_NAME = "申请专线";
    public static final String ACT_PHASE_ZIGBEE_FEE_APPLY = "zigbee_fee_apply";
    public static final String ACT_PHASE_ZIGBEE_FEE_APPLY_NAME = "申请资费";
    public static final String ACT_PHASE_ZIGBEE_CONFIG_FILL = "zigbee_config_fill";
    public static final String ACT_PHASE_ZIGBEE_CONFIG_FILL_NAME = "填写配置单";
    public static final String ACT_PHASE_ZIGBEE_CONFIG_CONFIRM = "zigbee_config_confirm";
    public static final String ACT_PHASE_ZIGBEE_CONFIG_CONFIRM_NAME = "确认配置单";
    public static final String ACT_PHASE_ZIGBEE_CONTRACT_DRAFT = "zigbee_contract_draft";
    public static final String ACT_PHASE_ZIGBEE_CONTRACT_DRAFT_NAME = "合同订稿";
    public static final String ACT_PHASE_ZIGBEE_CONTRACT_SIGN = "zigbee_contract_sign";
    public static final String ACT_PHASE_ZIGBEE_CONTRACT_SIGN_NAME = "签订合同";
    public static final String ACT_PHASE_ZIGBEE_CONTRACT_ARCHIVE = "zigbee_contract_archive";
    public static final String ACT_PHASE_ZIGBEE_CONTRACT_ARCHIVE_NAME = "合同归档";
    public static final String ACT_PHASE_ZIGBEE_ORDER_SUBMIT = "zigbee_order_submit";
    public static final String ACT_PHASE_ZIGBEE_ORDER_SUBMIT_NAME = "订单提交";
    public static final String ACT_PHASE_ZIGBEE_TASK_FINAL = "zigbee_task_final";
    public static final String ACT_PHASE_ZIGBEE_TASK_FINAL_NAME = "结束审核";
    // 宽带拨号
    public static final String PPPOE_OPPO_SUBMIT_NAME = "商机提交";
    public static final String PPPOE_OPPO_SUBMIT = "pppoe_oppo_submit";
    public static final String PPPOE_DISPATCH_NAME = "调度岗指派";
    public static final String PPPOE_DISPATCH = "pppoe_dispatch";
    public static final String PPPOE_ACCEPT_NAME = "业务受理";
    public static final String PPPOE_ACCEPT = "pppoe_accept";
    public static final String PPPOE_FINAL_AUDIT_NAME = "结束审核";
    public static final String PPPOE_FINAL_AUDIT = "pppoe_final_audit";
    //本级渠道工号新建
    public static final String NEWJOB_BUSINESS_APPLY = "newjob_business_apply";
    public static final String NEWJOB_BUSINESS_APPLY_NAME = "业务申请";
    public static final String NEWJOB_BUSINESS_OPERATIONS1 = "newjob_business_operations1";
    public static final String NEWJOB_BUSINESS_OPERATIONS1_NAME = "业务操作1";
    public static final String NEWJOB_BUSINESS_AUDIT = "newjob_business_audit";
    public static final String NEWJOB_BUSINESS_AUDIT_NAME = "业务审核";
    public static final String NEWJOB_BUSINESS_OPERATIONS = "newjob_business_operations";
    public static final String NEWJOB_BUSINESS_OPERATIONS_NAME = "业务操作2";
    public static final String NEWJOB_NUMBER_CREATION = "newjob_number_creation";
    public static final String NEWJOB_NUMBER_CREATION_NAME = "工号创建";
    //红名单，信控创建流程

    public static final String REDLIST_BUSINESS_APPLY = "redlist_business_apply";
    public static final String REDLIST_BUSINESS_APPLY_NAME = "业务申请";
    public static final String REDLIST_LEADER_APPROVAL = "redlist_leader_approval";
    public static final String REDLIST_LEADER_APPROVAL_NAME = "业务审批";
    public static final String REDLIST_BUSINESS_APPROVAL = "redlist_business_approval";
    public static final String REDLIST_BUSINESS_APPROVAL_NAME = "上级审批";
    public static final String REDLIST_SERVICE_AUDIT = "redlist_service_audit";
    public static final String REDLIST_SERVICE_AUDIT_NAME = "业务审核";
    public static final String REDLIST_INFORMATION_OPERATION = "redlist_Information_operation";
    public static final String REDLIST_INFORMATION_OPERATION_NAME = "系统受理";
    public static final String REDLIST_BUSINESS_AUDIT = "redlist_business_audit";
    public static final String REDLIST_BUSINESS_AUDIT_NAME = "业务稽核";
    public static final String REDLIST_EVALUATE = "redlist_evaluate";
    public static final String REDLIST_EVALUATE_NAME = "发起人评价";
    // 靓号申请
    public static final String ACT_PHASE_NOAPP_OPPO_SUBMIT = "noapp_oppo_submit";
    public static final String ACT_PHASE_NOAPP_OPPO_SUBMIT_NAME = "商机提交";
    public static final String ACT_PHASE_NOAPP_PRE_SELECT = "noapp_pre_select";
    public static final String ACT_PHASE_NOAPP_PRE_SELECT_NAME = "码号预选";
    public static final String ACT_PHASE_NOAPP_APPLICATION = "noapp_application";
    public static final String ACT_PHASE_NOAPP_APPLICATION_NAME = "码号申请";
    public static final String ACT_PHASE_NOAPP_CONFIRM_ALLOCATION = "noapp_confirm_allocation";
    public static final String ACT_PHASE_NOAPP_CONFIRM_ALLOCATION_NAME = "码号申请确认";
    public static final String ACT_PHASE_NOAPP_REGISTRATION = "noapp_registration";
    public static final String ACT_PHASE_NOAPP_REGISTRATION_NAME = "放号操作";
    public static final String ACT_PHASE_NOAPP_CONTRACT_SIGN = "noapp_contract_sign";
    public static final String ACT_PHASE_NOAPP_CONTRACT_SIGN_NAME = "合同签订及受理";
    public static final String ACT_PHASE_NOAPP_ACCOUNT_AUDIT = "noapp_account_audit";
    public static final String ACT_PHASE_NOAPP_ACCOUNT_AUDIT_NAME = "帐务稽核";
    public static final String ACT_PHASE_NOAPP_FINAL = "noapp_final";
    public static final String ACT_PHASE_NOAPP_FINAL_NAME = "结束审核";


    // 线索指派
    public static final String ACT_PHASE_CLUE_ASSIGN_CLUE_ENTER = "clue_assign_clue_enter";
    public static final String ACT_PHASE_CLUE_ASSIGN_CLUE_ENTER_NAME = "普通员工录入线索";
    public static final String ACT_PHASE_CLUE_ASSIGN_SENDER_ASSIGN = "clue_assign_sender_assign";
    public static final String ACT_PHASE_CLUE_ASSIGN_SENDER_ASSIGN_NAME = "线索指派员指派";
    public static final String ACT_PHASE_CLUE_ASSIGN_INSPECTOR_ASSIGN = "clue_assign_inspector_assign";
    public static final String ACT_PHASE_CLUE_ASSIGN_INSPECTOR_ASSIGN_NAME = "总监指派";
    public static final String ACT_PHASE_CLUE_ASSIGN_CUSTMANAGER_ACCEPT = "clue_assign_custmanager_accept";
    public static final String ACT_PHASE_CLUE_ASSIGN_CUSTMANAGER_ACCEPT_NAME = "客户经理接单";
    // 商机审核
    public static final String ACT_PHASE_OPPO_AUDIT_CUSTMANAGER_ENTER = "oppo_audit_custmanager_enter";
    public static final String ACT_PHASE_OPPO_AUDIT_CUSTMANAGER_ENTER_NAME = "客户经理录入商机";
    public static final String ACT_PHASE_OPPO_AUDIT_VICE_INSPECTOR_AUDIT = "oppo_audit_vice_inspector_audit";
    public static final String ACT_PHASE_OPPO_AUDIT_VICE_INSPECTOR_AUDIT_NAME = "行业副总监审核";
    public static final String ACT_PHASE_OPPO_AUDIT_INSPECTOR_AUDIT = "oppo_audit_inspector_audit";
    public static final String ACT_PHASE_OPPO_AUDIT_INSPECTOR_AUDIT_NAME = "行业总监审核";
    public static final String ACT_PHASE_OPPO_AUDIT_CUSTMANAGER_MODIFY = "oppo_audit_custmanager_modify";
    public static final String ACT_PHASE_OPPO_AUDIT_CUSTMANAGER_MODIFY_NAME = "客户经理修改";
    // 商机修改
    public static final String ACT_PHASE_OPPO_MODIFY_CUSTMANAGER_EDIT = "oppo_modify_custmanager_edit";
    public static final String ACT_PHASE_OPPO_MODIFY_CUSTMANAGER_EDIT_NAME = "客户经理修改商机";
    public static final String ACT_PHASE_OPPO_MODIFY_INSPECTOR_AUDIT = "oppo_modify_inspector_audit";
    public static final String ACT_PHASE_OPPO_MODIFY_INSPECTOR_AUDIT_NAME = "行业总监审核";
    // 催单
    public static final String ACT_PHASE_URGE_ORDER = "urge_start";
    public static final String ACT_PHASE_URGE_ORDER_NAME = "发起催单";
    public static final String ACT_PHASE_URGE_ORDER_DEAL = "urge_order_deal";
    public static final String ACT_PHASE_URGE_ORDER_DEAL_NAME = "催单处理";

    /*
     * 订单流程节点审批状态码
     */
    public static final String ACT_AUDIT_STATUS_BACK = "0";
    public static final String ACT_AUDIT_STATUS_NEXT = "1";
    public static final String ACT_AUDIT_STATUS_TRANS = "2";
    public static final String ACT_AUDIT_STATUS_END = "3";
    public static final String ACT_AUDIT_SAVE = "4";
    public static final String ACT_AUDIT_UPGRADE = "5";
    // order_status
    public static final String ORDER_STATUS_DRAFT = "draft";// 待提交
    public static final String ORDER_STATUS_RUNNING = "running";
    public static final String ORDER_STATUS_TRACKING = "tracking";
    public static final String ORDER_STATUS_END = "end";
    public static final String ORDER_STATUS_ABANDON = "abandon";

    // 申请支撑
    public static final String APPLAY_SUPPORT_FLOW_CODE = "applay_support";
    public static final String APPLAY_SUPPORT_FLOW_NAME = "客户申请支撑";
    public static final String TASK_TYPE_APPLAY_SUPPORT_APPLAY = "applay_support_applay";
    public static final String TASK_TYPE_APPLAY_SUPPORT_APPLAY_NAME = "客户申请支撑";
    public static final String TASK_TYPE_APPLAY_SUPPORT_TEAM_LEADER = "applay_support_team_leader";
    public static final String TASK_TYPE_APPLAY_SUPPORT_TEAM_LEADER_NAME = "团队长指派";
    public static final String TASK_TYPE_APPLAY_SUPPORT_DESIGNER = "applay_support_designer";
    public static final String TASK_TYPE_APPLAY_SUPPORT_DESIGNER_NAME = "支撑经理处理";
    public static final String TASK_TYPE_APPLAY_SUPPORT_SUPPORT_ADMIN = "applay_support_support_admin";
    public static final String TASK_TYPE_APPLAY_SUPPORT_SUPPORT_ADMIN_NAME = "支撑管理员处理";
    public static final String TASK_TYPE_APPLAY_SUPPORT_DESIGNER_RAP = "applay_support_designer_rap";
    public static final String TASK_TYPE_APPLAY_SUPPORT_DESIGNER_RAP_NAME = "支撑经理抢单";
    public static final String TASK_TYPE_APPLAY_SUPPORT_ORDER_DESIGNER = "applay_support_order_designer";
    public static final String TASK_TYPE_APPLAY_SUPPORT_ORDER_DESIGNER_NAME = "客户经理点单";
    // 申请支撑转单
    public static final String CHANGE_SUPPORT_FLOW_CODE = "change_support";
    public static final String CHANGE_SUPPORT_FLOW_NAME = "支撑经理申请转单";
    public static final String TASK_TYPE_APPLAY_CHANGE_SUPPORT = "applay_change_support";
    public static final String TASK_TYPE_APPLAY_CHANGE_SUPPORT_NAME = "支撑经理申请转单";
    public static final String TASK_TYPE_CHANGE_SUPPORT_TEAM_LEADER = "change_support_team_leader";
    public static final String TASK_TYPE_CHANGE_SUPPORT_TEAM_LEADER_NAME = "团队长指派";
    public static final String TASK_TYPE_CHANGE_SUPPORT_DESIGNER = "change_support_designer";
    public static final String TASK_TYPE_CHANGE_SUPPORT_DESIGNER_NAME = "支撑经理处理";
    public static final String TASK_TYPE_CHANGE_SUPPORT_SUPPORT_ADMIN = "change_support_support_admin";
    public static final String TASK_TYPE_CHANGE_SUPPORT_SUPPORT_ADMIN_NAME = "支撑管理员处理";

    // 添加支持经理流程
    public static final String PROCESS_ADD_SUPPORT_MANAGER = "addSupportManager";
    public static final String PROCESS_ADD_SUPPORT_MANAGER_NAME = "添加支撑经理";
    public static final String TASK_ADD_SUPPORT_MANAGER_ADD = "add";
    public static final String TASK_ADD_SUPPORT_MANAGER_ADD_NAME = "添加支撑经理";
    public static final String TASK_ADD_SUPPORT_MANAGER_CONFIRM = "confirm";
    public static final String TASK_ADD_SUPPORT_MANAGER_CONFIRM_NAME = "支撑经理确认";

    public static final String NEW_OPP_PHASE_OPP_GENZONG_LINK = "opp_genzong_link";// 商机跟踪环节
    public static final String NEW_OPP_PHASE_OPP_SIGN_LINK = "opp_sign_link";// 签约环节
    public static final String NEW_OPP_PHASE_OPP_ACCEPT_LINK = "opp_accept_link";// 业务受理开通环节
    public static final String NEW_OPP_PHASE_OPP_KEEP_LINK = "opp_keep_link";// 服务维系
    public static final String NEW_OPP_PHASE_OPP_END_LINK = "opp_end_link";// 废弃

    public static final String NEW_OPP_PHASE_SUPPORT = "support_plan";//

    // 太平洋阶段
    public static final String ORDERTYPE_PACIFIC_CODE = "T000000001";// 太平洋保险续约订单
    // 类型
    public static final String ORDERTYPE_PACIFIC_CODE_NAME = "太平洋保险续约";// 太平洋保险续约订单
    // 类型 名称

    public static final String PACIFIC_SUBMIT = "pacificOcean_oppo_submit"; // 下单阶段
    public static final String ACT_PACIFIC_CODE = "pacificOcean";// 续约客户经理处理任务编码
    public static final String ACT_PACIFIC_NAME = "续约客户经理处理"; // 任务名称
    public static final String photoOne = "photoOne";// 太平洋编码
    public static final String photoTwo = "photoTwo";// 太平洋编码
    public static final String photoXc = "photoXc";// 太平洋编码

    // 携号转网
    public static final String ORDER_TYPE_MNP = "mnp";
    public static final String ORDER_TYPE_MNP_NAME = "携号转网";
    public static final String ACT_PHASE_MNP_START = "mnp_start";
    public static final String ACT_PHASE_MNP_START_NAME = "创建订单";
    //
    public static final String ACT_PHASE_MNP_MANAGER_DEAL = "mnp_manager_deal";
    public static final String ACT_PHASE_MNP_MANAGER_DEAL_NAME = "客户经理处理";

    //欠费预警
    public static final String ORDER_TYPE_QFWARN = "qf_warn";
    public static final String ORDER_TYPE_OWE_WARNING_NAME = "欠费预警";
    public static final String ACT_PHASE_OWE_WARNING_START = "owe_warning_start";
    public static final String ACT_PHASE_OWE_WARNING_START_NAME = "创建订单";
    public static final String ACT_PHASE_OWE_WARNING_MANAGER_DEAL = "owe_warning_manager_deal";
    public static final String ACT_PHASE_OWE_WARNING_MANAGER_DEAL_NAME = "客户经理处理";

    //合同到期预警
    public static final String ORDER_TYPE_HTDQWARN = "htdq_warn";
    public static final String ORDER_TYPE_CONTRACT_DUE_WARNING_NAME = "合同到期预警";
    public static final String ACT_PHASE_CONTRACT_DUE_WARNING_START = "contract_due_warning_start";
    public static final String ACT_PHASE_CONTRACT_DUE_WARNING_START_NAME = "创建订单";
    public static final String ACT_PHASE_CONTRACT_DUE_WARNING_MANAGER_DEAL = "contract_due_warning_manager_deal";
    public static final String ACT_PHASE_CONTRACT_DUE_WARNING_MANAGER_DEAL_NAME = "客户经理处理";

    //余额不足预警
    public static final String ORDER_TYPE_YEBZ = "yebz_warn";
    public static final String ORDER_TYPE_YEBZ_WARNING_NAME = "余额不足预警";
    public static final String ORDER_TYPE_YEBZ_MANAGER = "manager";
    public static final String ORDER_TYPE_YEBZ_MANAGER_NAME = "客户经理处理";

    //红名单到期预警
    public static final String ORDER_TYPE_HMDWARN = "hmd_warn";
    public static final String ORDER_TYPE_HMD_WARNING_NAME = "红名单到期预警";
    public static final String ACT_PHASE_HMD_WARNING_START = "hmd_warning_start";
    public static final String ACT_PHASE_HMD_WARNING_START_NAME = "创建订单";
    public static final String ACT_PHASE_HMD_WARNING_MANAGER_DEAL = "hmd_warn_manager_deal";
    public static final String ACT_PHASE_HMD_WARNING_MANAGER_DEAL_NAME = "客户经理处理";
    public static final String ACT_PHASE_HMD_WARNING_LEADER_DEAL = "hmd_warn_leader_deal";
    public static final String ACT_PHASE_HMD_WARNING_LEADER_DEAL_NAME = "领导审核";
    //特殊活动到期预警
    public static final String ORDER_TYPE_SAWARN = "special_activity_warn";
    public static final String ORDER_TYPE_SA_WARNING_NAME = "特殊活动到期预警";
    public static final String ACT_PHASE_SA_WARNING_START_NAME = "创建订单";
    public static final String ACT_PHASE_SA_WARNING_MANAGER_DEAL = "special_activity_warn_manager_deal";
    public static final String ACT_PHASE_SA_WARNING_MANAGER_DEAL_NAME = "客户经理处理";

    //团购折扣到期预警
    public static final String ORDER_TYPE_GBWWARN = "group_buy_warn";
    public static final String ORDER_TYPE_GBW_WARNING_NAME = "团购折扣到期预警";
    public static final String ACT_PHASE_GBW_WARNING_START_NAME = "创建订单";
    public static final String ACT_PHASE_GBW_WARNING_MANAGER_DEAL = "group_buy_warn_manager_deal";
    public static final String ACT_PHASE_GBW_WARNING_MANAGER_DEAL_NAME = "客户经理处理";

    //用户欠费预警
    public static final String ORDER_TYPE_UCOWARN = "user_cust_owe_warn";
    public static final String ORDER_TYPE_UCO_WARNING_NAME = "通服欠费预警";
    public static final String ACT_PHASE_UCO_WARNING_START_NAME = "创建订单";
    public static final String ACT_PHASE_UCO_WARNING_MANAGER_DEAL = "user_cust_owe_warn_manager_deal";
    public static final String ACT_PHASE_UCO_WARNING_MANAGER_DEAL_NAME = "客户经理处理";

    //终端合约到期预警
    public static final String ORDER_TYPE_TCWARN = "terminal_contract_warn";
    public static final String ORDER_TYPE_TC_WARNING_NAME = "终端合约到期预警";
    public static final String ACT_PHASE_TC_WARNING_START_NAME = "创建订单";
    public static final String ACT_PHASE_TC_WARNING_MANAGER_DEAL = "terminal_contract_warn_manager_deal";
    public static final String ACT_PHASE_TC_WARNING_MANAGER_DEAL_NAME = "客户经理处理";

    //收入波动预警
    public static final String ORDER_TYPE_INCOMEWARN = "income_wave_warn";
    public static final String ORDER_TYPE_INCOMEWARN_NAME = "收入波动预警";
    public static final String ACT_PHASE_INCOME_WARNING_START_NAME = "创建订单";
    public static final String ACT_PHASE_INCOME_WARNING_MANAGER_DEAL = "income_wave_warn_manager_deal";
    public static final String ACT_PHASE_INCOME_WARNING_MANAGER_DEAL_NAME = "客户经理处理";

    public static final String ORDER_TYPE_DOUBLEZERO = "double_zero_warn";
    public static final String ORDER_TYPE_DOUBLEZERO_NAME = "双线0出账预警";
    public static final String ACT_PHASE_DOUBLEZERO_WARNING_START_NAME = "创建订单";
    public static final String ACT_PHASE_DOUBLEZERO_WARNING_MANAGER_DEAL = "double_zero_warn_manager_deal";
    public static final String ACT_PHASE_DOUBLEZERO_WARNING_MANAGER_DEAL_NAME = "客户经理处理";
    public static final String ACT_PHASE_DOUBLEZERO_WARNING_AUDIT_DEAL = "double_zero_warn_audit_deal";
    public static final String ACT_PHASE_DOUBLEZERO_WARNING_AUDIT_DEAL_NAME = "稽核人员处理";

    public static final String ORDER_TYPE_PCDNWARN = "pcdn_warn";
    public static final String ORDER_TYPE_PCDNARN_NAME = "违规PCDN业务稽核";
    public static final String ACT_PHASE_PCDN_WARNING_START_NAME = "创建订单";
    public static final String ACT_PHASE_PCDN_WARNING_MANAGER_DEAL = "pcdn_warn_manager_deal";
    public static final String ACT_PHASE_PCDN_WARNING_MANAGER_DEAL_NAME = "客户经理处理";
    public static final String ACT_PHASE_PCDN_WARNING_AUDIT_DEAL = "pcdn_warn_audit_deal";
    public static final String ACT_PHASE_PCDN_WARNING_AUDIT_DEAL_NAME = "稽核人员处理";

    //宽带系统自动预警
    public static final String ORDER_TYPE_WIDE = "Wide";
    public static final String ORDER_TYPE_WIDE_NAME = "宽带到期预警";
    public static final String ORDER_TYPE_WIDE_START = "wide_warning_start";
    public static final String ORDER_TYPE_WIDE_START_NAME = "创建订单";
    public static final String ACT_PHASE_MANAGER = "manager";
    public static final String ACT_PHASE_MANAGER_NAME = "客户经理处理";

    //双线预警
    public static final String ORDER_TYPE_DOUBLE_WIDE = "double_wide";
    public static final String ORDER_TYPE_DOUBLE_WIDE_NAME = "双线到期预警";
    public static final String ACT_PHASE_DOUBLE_WIDE_START = "double_wide_start";
    public static final String ACT_PHASE_DOUBLE_WIDE_START_NAME = "创建订单";
    public static final String ACT_PHASE_DOUBLE_WIDE_MANAGER = "manager";
    public static final String ACT_PHASE_DOUBLE_WIDE_MANAGER_NAME = "客户经理处理";

    //三方稽核，四个业务类型共用一个
    public static final String ORDER_TYPE_THIRD_CHECK = "third_check_warn";
    public static final String ORDER_TYPE_THIRD_CHECK_NAME = "三方稽核预警";
    public static final String ACT_PHASE_THIRD_CHECK_START = "check_net_start";
    public static final String ACT_PHASE_THIRD_CHECK_START_NAME = "创建订单";
    public static final String ACT_PHASE_THIRD_CHECK_MANAGER_DEAL = "third_check_warn_manager_deal";
    public static final String ACT_PHASE_THIRD_CHECK_MANAGER_DEAL_NAME = "客户经理处理";

    //云业务到期预警
    public static final String ORDER_TYPE_CEAWARN = "cloud_expire_alarm";
    public static final String ORDER_TYPE_CEA_WARNING_NAME = "云业务到期预警";
    public static final String ACT_PHASE_CEA_WARNING_START = "cloud_expire_alarm_start";
    public static final String ACT_PHASE_CEA_WARNING_START_NAME = "创建订单";
    public static final String ACT_PHASE_CEA_WARNING_MANAGER_DEAL = "cloud_expire_alarm_manager_deal";
    public static final String ACT_PHASE_CEA_WARNING_MANAGER_DEAL_NAME = "客户经理处理";

    //预打发票预警
    public static final String ORDER_TYPE_CRWARN = "cust_receipt_warn";
    public static final String ORDER_TYPE_CR_WARNING_NAME = "预打发票预警";
    public static final String ACT_PHASE_CR_WARNING_START = "cust_receipt_warning_start";
    public static final String ACT_PHASE_CR_WARNING_START_NAME = "创建订单";
    public static final String ACT_PHASE_CR_WARNING_MANAGER_DEAL = "cust_receipt_warn_manager_deal";
    public static final String ACT_PHASE_CR_WARNING_MANAGER_DEAL_NAME = "客户经理处理";

    //割接预警
    public static final String ORDER_TYPE_COWARN = "cut_over_warn";
    public static final String ORDER_TYPE_CO_WARNING_NAME = "割接预警";
    public static final String ACT_PHASE_CO_WARNING_START = "cut_over_warning_start";
    public static final String ACT_PHASE_CO_WARNING_START_NAME = "创建订单";
    public static final String ACT_PHASE_CO_WARNING_MANAGER_DEAL = "cut_over_warn_manager_deal";
    public static final String ACT_PHASE_CO_WARNING_MANAGER_DEAL_NAME = "客户经理处理";

    //维系订单大类
    public static final String ORDER_TYPE_WXORDER = "maintenance_order";

    //智能推荐信息派发流程
    public static final String ACT_PHASE_INFO_SEND = "info_send";
    public static final String ACT_PHASE_INFO_SEND_NAME = "信息派发";
    public static final String ACT_PHASE_SUPPORT_MANAGER_RESPONDBID_JUDGE = "support_manager_respondbid_judge";
    public static final String ACT_PHASE_SUPPORT_MANAGER_RESPONDBID_JUDGE_NAME = "支撑经理应标判断";
    public static final String ACT_PHASE_DIRECTOR_ASSIGN = "director_assign";
    public static final String ACT_PHASE_DIRECTOR_ASSIGN_NAME = "行业总监指派";
    public static final String ACT_PHASE_CUST_MANAGER_FOLLOW = "cust_manager_follow";
//	public static final String ACT_PHASE_CUST_MANAGER_FOLLOW_NAME = "客户经理跟踪";

    //信息派发状态
    public static final String INFOSEND_RESPON_BID = "respon_bid";
    public static final String INFOSEND_NOT_RESPON_BID_ABANDON = "not_respon_bid_abandon";
    public static final String INFOSEND_LINK_CLUE = "link_clue";
    public static final String INFOSEND_LINK_OPPO = "link_oppo";
    public static final String INFOSEND_CREATE_CLUE = "create_clue";
    public static final String INFOSEND_ABANDON = "abandon";

    // 合同审核流程
    public static final String ACT_PHASE_CONTRACT_ADD = "contract_add";
    public static final String ACT_PHASE_CONTRACT_ADD_NAME = "合同添加";
    public static final String ACT_PHASE_XMGLY_AUDIT = "xmgly_audit";
    public static final String ACT_PHASE_XMGLY_AUDIT_NAME = "项目管理员审核";

    // 里程碑修改流程
    public static final String PROCESS_MILESTONE_EDIT = "milestoneEdit";
    public static final String PROCESS_MILESTONE_EDIT_NAME = "里程碑修改";
    public static final String TASK_MILESTONE_EDIT_APPLY = "milestoneEdit_apply";
    public static final String TASK_MILESTONE_EDIT_APPLY_NAME = "里程碑修改申请";
    public static final String TASK_MILESTONE_EDIT_AUDIT = "milestoneEdit_audit";
    public static final String TASK_MILESTONE_EDIT_AUDIT_NAME = "行业总监或部门经理审核";

    // 支撑工单审核流程
    public static final String ACT_PHASE_SUPPORT_ADD = "support_add";
    public static final String ACT_PHASE_SUPPORT_ADD_NAME = "创建支撑订单";
    public static final String ACT_PHASE_DESIGNER_AUDIT = "designer_audit";
    public static final String ACT_PHASE_DESIGNER_AUDIT_NAME = "支撑经理支撑";
    public static final String ACT_PHASE_TEAM_LEADER_AUDIT = "team_leader_audit";
    public static final String ACT_PHASE_TEAM_LEADER_AUDIT_NAME = "团队长审核";
    public static final String ACT_PHASE_DESIGNER_EDIT = "designer_edit";
    public static final String ACT_PHASE_DESIGNER_EDIT_NAME = "支撑经理修改工单";
    public static final String ACT_PHASE_SUPPORT_CUSTMANAGER_GRADE_NAME = "客户经理打分";
    public static final String ACT_PHASE_SUPPORT_CUSTMANAGER_GRADE = "cust_manager_grade";

    public static final String ACT_PHASE_DESIGNER_PRODUCT_MANAGER = "productManager";
    public static final String ACT_PHASE_DESIGNER_PRODUCT_MANAGER_NAME = "产品经理确认";

    public static final String ACT_PHASE_DESIGNER_PRODUCT_MANAGER_LEADER = "productManagerLeader";
    public static final String ACT_PHASE_DESIGNER_PRODUCT_MANAGER_LEADER_NAME = "产品经理领导确认";


    //应标流程
    public static final String ACT_PHASE_CUST_MANAGER_START = "cust_manager_start_bid";
    public static final String ACT_PHASE_CUST_MANAGER_START_NAME = "主责支撑经理发起应标";
    public static final String ACT_PHASE_CUST_MANAGER_MODIFY_NAME = "应标客户经理修改";
    public static final String ACT_PHASE_INDUSTRY_DIRECTOR_AUDIT = "industry_director_audit";
    public static final String ACT_PHASE_INDUSTRY_DIRECTOR_AUDIT_NAME = "应标支撑团队长审核";
    public static final String ACT_PHASE_SUPPORT_DIRECTOR_SCORE = "support_director_score";
    public static final String ACT_PHASE_SUPPORT_DIRECTOR_SCORE_NAME = "应标支撑总监打分";


    //技术解析评审流程
    public static final String ONLINE_PROGRAM_CODE = "onlineProgram";
    public static final String ONLINE_PROGRAM_NAME = "线上技术解析评审";
    public static final String ANALYSISREVIEW_URL = "/analysisReview/toComplete?urlType=deal&orderId=";
    //线下技术解析评审
    public static final String OFFLINE_SIGN_CODE = "1";
    //线上技术解析评审
    public static final String ONLINE_SIGN_CODE = "0";
    //审批通过
    public static final String ONLINE_RESULT_0 = "0";
    public static final Integer ONLINE_RESULT_INTEGER_0 = 0;
    //审批退回
    public static final String ONLINE_RESULT_1 = "1";
    public static final Integer ONLINE_RESULT_INTEGER_1 = 1;
    //审批归档
    public static final String ONLINE_RESULT_2 = "2";
    //市
    public static final String ONLINE_LEVEL_0 = "0";
    //省
    public static final String ONLINE_LEVEL_1 = "1";
    public static final String USERTASK_1 = "usertask_1";
    public static final String USERTASK_2 = "usertask_2";
    public static final String USERTASK_3 = "usertask_3";
    public static final String USERTASK_4 = "usertask_4";
    public static final String USERTASK_5 = "usertask_5";
    public static final String USERTASK_6 = "usertask_6";
    public static final String USERTASK_8 = "usertask_8";
    public static final String USERTASK_9 = "usertask_9";
    public static final String USERTASK_10 = "usertask_10";
    public static final String USERTASK_1_NAME = "主责支撑经理发起";
    public static final String USERTASK_2_NAME = "市评审组成员审核(并签)";
    public static final String USERTASK_3_NAME = "市评审负责人审核";
    public static final String USERTASK_4_NAME = "主责支撑经理确认";
    public static final String USERTASK_5_NAME = "市评审接口人点单";
    public static final String USERTASK_6_NAME = "省评审接口人点单";
    public static final String USERTASK_8_NAME = "省评审组成员审核(并签)";
    public static final String USERTASK_9_NAME = "省评审负责人审核";
    public static final String USERTASK_10_NAME = "主责支撑经理确认";

    //投标处理
    public static final String BID_PROGRAM_DEAL_CODE = "responseBidDeal";
    public static final String BID_PROGRAM_DEAL_NAME = "投标处理";
    public static final String BID_PHASE_CUST_MANAGER_START_BID_DEAL = "cust_manager_start_bid_deal";
    public static final String BID_PHASE_CUST_MANAGER_START_NAME_DEAL = "主责支撑经理发起投标";
    public static final String BID_PROCESS_DEAL_URL = "/bidNew/deal?orderId=";

    //应标流程
    public static final String BID_URL = "/bidNew/toBidComplete?urlType=deal&orderId=";
    // 投标编辑
    public static final String BID_EDIT_URL = "/bidNew/toSaveEdit?oppId=";
    public static final String BID_PROGRAM_CODE = "responseBidnew";
    public static final String BID_PROGRAM_NAME = "招投标流程";
    public static final String ACT_PHASE_CUST_MANAGER_START_NEW = "cust_manager_start_bidNew";
    public static final String ACT_PHASE_CUST_MANAGER_START_NAME_NEW = "主责支撑经理发起应标";
    public static final String ACT_PHASE_INDUSTRY_DIRECTOR_AUDIT_NEW = "industry_director_auditNew";
    public static final String ACT_PHASE_INDUSTRY_DIRECTOR_AUDIT_NAME_NEW = "支撑团队长审核";

    //投标状态bid_result
    public static final String BID_RESULT_MATCH_BID = "match_bid";//中标
//	public static final String BID_RESULT_NOT_MATCH_BID = "not_match_bid";//未中标
//	public static final String BID_RESULT_BOT_BID = "not_bid";//未投标
//	public static final String BID_RESULT_LIU_BID = "liu_bid";//流标
//	public static final String BID_RESULT_FEI_BID = "fei_bid";//废标

    //商机废弃审核
    public static final String ACT_PHASE_CUST_MANAGER_START_OPP_END = "cust_manager_start_opp_end";
    public static final String ACT_PHASE_CUST_MANAGER_START_OPP_END_NAME = "客户经理发起商机废弃审核";
    public static final String ACT_PHASE_INDUSTRY_DIRECTOR_AUDIT_OPP_END = "industry_director_audit_opp_end";
    public static final String ACT_PHASE_INDUSTRY_DIRECTOR_AUDIT_OPP_END_NAME = "商机废弃行业总监审核";

    //客户位置信息维护
    public static final String ACT_PHASE_CUST_MANAGER_SUBMIT_POSITION = "cust_manager_submit_position";
    public static final String ACT_PHASE_CUST_MANAGER_SUBMIT_POSITION_NAME = "客户经理提交位置信息";
    public static final String ACT_PHASE_ADMINISTRATOR_AUDIT = "administrator_audit";
    public static final String ACT_PHASE_ADMINISTRATOR_AUDIT_NAME = "客户管理员审核";

    //业务受理订单流程
    public static final String ACT_PHASE_BUSSINESS_ADD = "business_add";
    public static final String ACT_PHASE_BUSSINESS_ADD_NAME = "客户经理下单";
    public static final String ACT_PHASE_BUSSINESS_ACCEPT = "business_accept";
    public static final String ACT_PHASE_BUSSINESS_ACCEPT_NAME = "业务受理";
    public static final String ACT_PHASE_BUSSINESS_CONFIRM = "business_confirm";
    public static final String ACT_PHASE_BUSSINESS_CONFIRM_NAME = "客户经理确认";

    //通用需求订单流程
    public static final String ACT_PHASE_GENERAL_ORDER_ADD = "customer_manager_create_order";
    public static final String ACT_PHASE_GENERAL_ORDER_ADD_NAME = "客户经理下单";
    public static final String ACT_PHASE_GENERAL_ORDER_ACCEPT = "order_accept";
    public static final String ACT_PHASE_GENERAL_ORDER_ACCEPT_NAME = "业务受理";
    public static final String ACT_PHASE_GENERAL_ORDER_CONFIRM = "customer_manager_confirmation";
    public static final String ACT_PHASE_GENERAL_ORDER_CONFIRM_NAME = "客户经理确认";

    //人员资质申请流程
    public static final String ACT_PHASE_PERSONNEL_APPLY = "personnel_apply";
    public static final String ACT_PHASE_PERSONNEL_APPLY_NAME = "客户经理发起资质申请";
    public static final String ACT_PHASE_PERSONNEL_AUDIT = "personnel_audit";
    public static final String ACT_PHASE_PERSONNEL_AUDIT_NAME = "资质管理员审批";
    public static final String ACT_PHASE_PERSONNEL_RECEIVE_NOTICE = "personnel_receive_notice";
    public static final String ACT_PHASE_PERSONNEL_RECEIVE_NOTICE_NAME = "资质管理员发起领取通知";
    public static final String ACT_PHASE_PERSONNEL_RECEIVE_CONFIRM = "personnel_receive_confirm";
    public static final String ACT_PHASE_PERSONNEL_RECEIVE_CONFIRM_NAME = "资质申请人领取确认";
    public static final String ACT_PHASE_PERSONNEL_RETURN_NOTICE = "personnel_return_notice";
    public static final String ACT_PHASE_PERSONNEL_RETURN_NOTICE_NAME = "资质管理员发起归还通知";
    public static final String ACT_PHASE_PERSONNEL_RETURN_CONFIRM_C = "personnel_return_confirm_c";
    public static final String ACT_PHASE_PERSONNEL_RETURN_CONFIRM_C_NAME = "资质申请人归还确认";

    //公司资质申请流程
    public static final String ACT_PHASE_COMPANY_APPLY = "company_apply";
    public static final String ACT_PHASE_COMPANY_APPLY_NAME = "客户经理发起资质申请";
    public static final String ACT_PHASE_COMPANY_AUDIT = "company_audit";
    public static final String ACT_PHASE_COMPANY_AUDIT_NAME = "资质管理员审批";
    public static final String ACT_PHASE_COMPANY_RECEIVE_NOTICE = "company_receive_notice";
    public static final String ACT_PHASE_COMPANY_RECEIVE_NOTICE_NAME = "资质管理员发起领取通知";
    public static final String ACT_PHASE_COMPANY_RECEIVE_CONFIRM = "company_receive_confirm";
    public static final String ACT_PHASE_COMPANY_RECEIVE_CONFIRM_NAME = "资质申请人领取确认";
    public static final String ACT_PHASE_COMPANY_RETURN_NOTICE = "company_return_notice";
    public static final String ACT_PHASE_COMPANY_RETURN_NOTICE_NAME = "资质管理员发起归还通知";
    public static final String ACT_PHASE_COMPANY_RETURN_CONFIRM_C = "company_return_confirm_c";
    public static final String ACT_PHASE_COMPANY_RETURN_CONFIRM_C_NAME = "资质申请人归还确认";

    //合作伙伴入围专业清退流程
    public static final String ACT_PHASE_PARTNER_PROFESSIONAL_DEL_USER_SUBMIT = "user_submit";
    public static final String ACT_PHASE_PARTNER_PROFESSIONAL_DEL_USER_SUBMIT_NAME = "发起清退流程";
    public static final String ACT_PHASE_PARTNER_PROFESSIONAL_EDIT_USER_SUBMIT = "user_submit";
    public static final String ACT_PHASE_PARTNER_PROFESSIONAL_EDIT_USER_SUBMIT_NAME = "发起修改流程";
    public static final String ACT_PHASE_PARTNER_ITEM_USER_SUBMIT = "user_submit";
    public static final String ACT_PHASE_PARTNER_ITEM_USER_SUBMIT_NAME = "发起中选项目评价流程";
    public static final String ACT_PHASE_PARTNER_PROFESSIONAL_ADD_USER_SUBMIT = "user_submit";
    public static final String ACT_PHASE_PARTNER_PROFESSIONAL_ADD_USER_SUBMIT_NAME = "发起新增流程";
    public static final String ACT_PHASE_PARTNER_PROFESSIONAL_MANAGE_AUDIT = "manage_audit";
    public static final String ACT_PHASE_PARTNER_PROFESSIONAL_MANAGE_AUDIT_NAME = "部门经理审批";
    public static final String ACT_PHASE_PARTNER_PROFESSIONAL_REGION_AUDIT = "region_audit";
    public static final String ACT_PHASE_PARTNER_PROFESSIONAL_REGION_AUDIT_NAME = "地市管理员审批";
    public static final String ACT_PHASE_PARTNER_PROFESSIONAL_PROVINCE_AUDIT = "province_audit";
    public static final String ACT_PHASE_PARTNER_PROFESSIONAL_PROVINCE_AUDIT_NAME = "省分管理员审批";
    public static final String ACT_PHASE_PARTNER_PROFESSIONAL_LEADER_AUDIT = "leader_audit";
    public static final String ACT_PHASE_PARTNER_PROFESSIONAL_LEADER_AUDIT_NAME = "省分领导审批";
    public static final String ACT_PHASE_PARTNER_ITEM_RATED = "item_rated";
    public static final String ACT_PHASE_PARTNER_ITEM_RATED_NAME = "中选项目评价";
    //伙伴基本信息实体
    public static final String CLASS_PARTNER = "PartnerBasic";
    public static final String CLASS_PARTNER_REG = "registeredCapital";
    public static final String CLASS_PARTNER_MAJ = "majorProducts";
    public static final String CLASS_PARTNER_QUA = "qualificationCertificate";
    public static final String CLASS_PARTNER_ASS = "assessmentRating";

    //伙伴专业信息实体
    public static final String CLASS_PARTNER_PROFE = "PartnerProfessional";
    public static final String CLASS_PARTNER_PROFE_HIS = "hisCase";
    public static final String FTP_CHARSET_UTF8 = "UTF-8";
    public static final String FTP_CHARSET_GB2312 = "GB2312";
    // ftp文件编码
    public static final String FTP_CHARSET = "GBK";
    // ftp文件中的字段间的分隔符
    public static final String FTP_FIELD_DELIMITER = ",";


    // 订单来源
    public static final String ORDER_SOURCE = "order_source";

    // 首页显示的重要业务数量
    public static final Integer MAX_NUMBER_OF_IMPORTANT_BAIKE = 4;

    public static final String ORDER_WARNING = "0";
    public static final String PROCESS_WARNING = "1";

    // 告警状态
    public static final String UNWARNING = "0"; // 未告警
    public static final String YELLOW_WARNING = "1";
    public static final String RED_WARNING = "2";
    public static final String DUE_WARNING = "3";
    public static final String WARNING_END = "4";    //告警结束
    public static final String UNWARNING_END = "5"; //未告警结束


    public static final int SHENG_AREA_ID = 330000;
    public static final int SHENG_AREA_ID_SHORT = 33;

    // 职务岗位
    public static final String USER_POST_SUPPORT_MANAGER = "support_manager";// 支撑经理
    public static final String USER_POST_SUPPORT_TEAM_LEADER = "support_team_leader";// 支撑团队长
    public static final String USER_POST_SUPPORT_THE_DIRECTOR = "support_the_director";// 支撑总监
    public static final String USER_POST_SUPPORT_MANAGEMENT_POST = "support_management_post";// 支撑管理岗
    public static final String USER_POST_CLUES_POINT_TO_DISPATCH = "clues_point_to_dispatch";// 线索指派员
    public static final String USER_POST_DIRECTOR_OF_INDUSTRY = "director_of_industry";// 行业总监
    public static final String USER_POST_DEPUTY_INDUSTRY_DIRECTOR = "deputy_industry_director";// 行业副总监
    public static final String USER_POST_CUST_MANAGER = "cust_manager";// 客户经理
    public static final String USER_POST_GENERAL_STAFF = "general_staff";// 普通员工

    //大项目统计
    public static final String STATISTICS_TYPE_BXQJ = "bxqj";//百县千家
    public static final String STATISTICS_TYPE_KHBP = "khbp";//考核大项目
    public static final String STATISTICS_TYPE_JF = "jf";//经分
    //常用实体的映射表名
    public static final String PRIMARY_KEY = "id_";

    //id=3307（金华）的数据
    public static final String JINHUA_PRAENT = "3307%";
    //顺序依次为 光纤宽带、多云、物联网、电路、大数据
    public static final String JINHUA_CODETYPE = "('cloudy','gxkd','cfbb','zigbee','jinghuabigdata')";
    public static final String FLOW_AUTH_SETTINGSTR = "3300,3307,";//对应sys_code表的setting_str1字段
    public static final String JINHUA_AREA_CODE = "3307";
    /**
     * 能力库-人员资质-资质有效期
     */
    public static final String YGQ = "ygq";
    public static final String YGY = "ygy";
    public static final String SGY = "sgy";
    public static final String BN = "bn";
    public static final String CQ = "cq";


    //主发跨域电路资源核查
    public static final String ACT_PHASE_CDCV = "cdcv";
    public static final String ACT_PHASE_CDCV_NAME = "主发跨域电路资源核查";
    public static final String ACT_PHASE_CDCV_APPLY = "cdcv_apply";
    public static final String ACT_PHASE_CDCV_APPLY_NAME = "资源核查申请";
    public static final String ACT_PHASE_CDCV_INITIATE = "cdcv_initiate";
    public static final String ACT_PHASE_CDCV_INITIATE_NAME = "资源核查审核发起";
    public static final String ACT_PHASE_CDCV_DEAL = "cdcv_deal";
    public static final String ACT_PHASE_CDCV_DEAL_NAME = "资源核查审核处理";
    public static final String ACT_PHASE_CDCV_CUSTMANAGER_CONFIRM = "cdcv_custmanager_confirm";
    public static final String ACT_PHASE_CDCV_CUSTMANAGER_CONFIRM_NAME = "客户经理确认";

    //主发跨域电路开通
    public static final String ACT_PHASE_CDCO = "cdco";
    public static final String ACT_PHASE_CDCO_NAME = "主发跨域电路开通";
    public static final String ACT_PHASE_CDCO_APPLY = "cdco_apply";
    public static final String ACT_PHASE_CDCO_APPLY_NAME = "业务开通申请";
    public static final String ACT_PHASE_CDCO_INITIATE = "cdco_initiate";
    public static final String ACT_PHASE_CDCO_INITIATE_NAME = "业务开通发起";
    public static final String ACT_PHASE_CDCO_DEAL = "cdco_deal";
    public static final String ACT_PHASE_CDCO_DEAL_NAME = "业务开通处理";
    public static final String ACT_PHASE_CDCO_CUSTMANAGER_CONFIRM = "cdco_custmanager_confirm";
    public static final String ACT_PHASE_CDCO_CUSTMANAGER_CONFIRM_NAME = "客户经理确认";

    //本地电路资源核查
    public static final String ACT_PHASE_LER = "ler";
    public static final String ACT_PHASE_LER_NAME = "本地电路资源核查";
    public static final String ACT_PHASE_LER_APPLY = "ler_apply";
    public static final String ACT_PHASE_LER_APPLY_NAME = "资源核查申请";
    public static final String ACT_PHASE_LER_DEAL = "ler_deal";
    public static final String ACT_PHASE_LER_DEAL_NAME = "资源核查审核处理";
    public static final String ACT_PHASE_LER_CUSTMANAGER_CONFIRM = "ler_custmanager_confirm";
    public static final String ACT_PHASE_LER_CUSTMANAGER_CONFIRM_NAME = "客户经理确认";

    //本地电路开通
    public static final String ACT_PHASE_LEO = "leo";
    public static final String ACT_PHASE_LEO_NAME = "本地电路开通";
    public static final String ACT_PHASE_LEO_APPLY = "leo_apply";
    public static final String ACT_PHASE_LEO_APPLY_NAME = "业务开通申请";
    public static final String ACT_PHASE_LEO_DEAL = "leo_deal";
    public static final String ACT_PHASE_LEO_DEAL_NAME = "业务开通处理";
    public static final String ACT_PHASE_LEO_CUSTMANAGER_CONFIRM = "leo_custmanager_confirm";
    public static final String ACT_PHASE_LEO_CUSTMANAGER_CONFIRM_NAME = "客户经理确认";

    //大客户支撑流程
    public static final String ACT_PHASE_BC = "bc";
    public static final String ACT_PHASE_BC_NAME = "大客户支撑";
    public static final String ACT_PHASE_BC_APPLY = "bc_apply";
    public static final String ACT_PHASE_BC_APPLY_NAME = "业务申请";
    public static final String ACT_PHASE_BC_AUDIT_NAME = "运维审核";
    public static final String ACT_PHASE_BC_AUDIT = "bc_audit";
    public static final String ACT_PHASE_BC_DEAL = "bc_deal";
    public static final String ACT_PHASE_BC_DEAL_NAME = "支撑处理";
    public static final String ACT_PHASE_BC_AUDIT1_NAME = "业务审核";
    public static final String ACT_PHASE_BC_AUDIT1 = "bc_audit1";
    public static final String ACT_PHASE_BC_STATEMENT = "bc_statement";
    public static final String ACT_PHASE_BC_STATEMENT_NAME = "发起人结单";
    public static final String ACT_PHASE_BC_CONFIRM = "bc_confirm";

    // 趸交款导入流程
    public static final String ACT_PHASE_PAYMENT_APPLY = "act_task_payment_apply";
    public static final String ACT_PHASE_PAYMENT_APPLY_NAME = "业务申请";
    public static final String ACT_PHASE_PAYMENT_AUDIT = "act_task_payment_audit";
    public static final String ACT_PHASE_PAYMENT_AUDIT_NAME = "财务审核";
    public static final String ACT_PHASE_PAYMENT_YWSH = "act_task_payment_ywsh";
    public static final String ACT_PHASE_PAYMENT_YWSH_NAME = "业务审核";
    public static final String ACT_PHASE_PAYMENT_YWHC = "act_task_payment_ywhc";
    public static final String ACT_PHASE_PAYMENT_YWHC_NAME = "业务核查";
    public static final String ACT_PHASE_PAYMENT_NOTICE = "act_task_payment_notice";
    public static final String ACT_PHASE_PAYMENT_NOTICE_NAME = "抄送岗";

    // 业务资费流程
    public static final String ACT_PHASE_YWZF_YWBL = "ywzf_ywbl";
    public static final String ACT_PHASE_YWZF_YWBL_NAME = "业务办理";
    public static final String ACT_PHASE_YWZF_YWSQ = "ywzf_ywsq";
    public static final String ACT_PHASE_YWZF_YWSQ_NAME = "业务申请审核";
    public static final String ACT_PHASE_YWZF_YYSL = "ywzf_yysl";
    public static final String ACT_PHASE_YWZF_YYSL_NAME = "营业受理";
    public static final String ACT_PHASE_YWZF_QXSP = "ywzf_qxsp";
    public static final String ACT_PHASE_YWZF_QXSP_NAME = "区县审批";
    public static final String ACT_PHASE_YWZF_XXHCL = "ywzf_xxhcl";
    public static final String ACT_PHASE_YWZF_XXHCL_NAME = "信息化处理";
    public static final String ACT_PHASE_YWZF_SJSP = "ywzf_sjsp";
    public static final String ACT_PHASE_YWZF_SJSP_NAME = "市级审批";
    public static final String ACT_PHASE_YWZF_FGSP = "ywzf_fgsp";
    public static final String ACT_PHASE_YWZF_FGSP_NAME = "市分管领导审批";
    public static final String ACT_PHASE_YWZF_YWJH = "ywzf_ywjh";
    public static final String ACT_PHASE_YWZF_YWJH_NAME = "业务稽核";
    public static final String ACT_PHASE_YWZF_CONFIRM = "ywzf_confirm";
    public static final String ACT_PHASE_YWZF_CONFIRM_NAME = "发起人确认";
    public static final String ACT_PHASE_YWZF_NOTICE = "ywzf_notice";
    public static final String ACT_PHASE_YWZF_NOTICE_NAME = "抄送岗";
    public static final String ACT_PHASE_YWZF_YWSH = "ywzf_ywsh";
    public static final String ACT_PHASE_YWZF_YWSH_NAME = "业务审核";

    // 维系待办 订单类型set
    public final static Set<String> ORDER_TYPE_FILTER;

    static {
        HashSet<String> set = new HashSet<>();
        set.add(ORDER_TYPE_MNP);
        set.add(ORDER_TYPE_QFWARN);
        set.add(ORDER_TYPE_HTDQWARN);
        set.add(ORDER_TYPE_HMDWARN);
        set.add(ORDER_TYPE_SAWARN);
        set.add(ORDER_TYPE_GBWWARN);
        set.add(ORDER_TYPE_TCWARN);
        set.add(ORDER_TYPE_WIDE);
        set.add(ORDER_TYPE_DOUBLE_WIDE);
        set.add(ORDER_TYPE_CEAWARN);
        set.add(ORDER_TYPE_CRWARN);
        set.add(ORDER_TYPE_COWARN);
        set.add(ORDER_TYPE_UCOWARN);
        ORDER_TYPE_FILTER = Collections.unmodifiableSet(set);
    }

    // 合作伙伴 订单类型set
    public final static Set<String> PARTNER_TYPE_FILTER;

    static {
        HashSet<String> set = new HashSet<>();
        set.add(ACT_PARTNER_PROFESSIONAL_CLEAR);
        set.add(ACT_PARTNER_PROFESSIONAL_EDIT);
        set.add(ACT_PARTNER_PROFESSIONAL_ADD);
        set.add(ACT_PARTNER_ITEM);
        PARTNER_TYPE_FILTER = Collections.unmodifiableSet(set);
    }

    //维系物料
    public static final String ACT_PHASE_WXPRODUCT = "wxProduct";
    public static final String ACT_PHASE_WXPRODUCT_NAME = "维系物料";
    public static final String ACT_PHASE_WX_APPLY = "wx_apply";
    public static final String ACT_PHASE_WX_APPLY_NAME = "维系物料申请";
    public static final String ACT_PHASE_WX_LEADER_CONFIRM = "wx_leader_confirm";
    public static final String ACT_PHASE_WX_LEADER_CONFIRM_NAME = "领导审核";
    public static final String ACT_PHASE_WX_MANAGER_CONFIRM = "wx_manager_confirm";
    public static final String ACT_PHASE_WX_MANAGER_CONFIRM_NAME = "维系管理岗审核";
    public static final String ACT_PHASE_WX_SUPERIOR_LEADER_CONFIRM = "wx_superior_leader_confirm";
    public static final String ACT_PHASE_WX_SUPERIOR_LEADER_CONFIRM_NAME = "上级领导审核";
    public static final String ACT_PHASE_YW_COMFIRM = "wx_yw_comfirm";
    public static final String ACT_PHASE_YW_COMFIRM_NAME = "业务审核";
    public static final String ACT_PHASE_WX_WAREHOUSE_MATERIAL = "wx_warehouse_material";
    public static final String ACT_PHASE_WX_WAREHOUSE_MATERIAL_NAME = "仓库发料";
    public static final String ACT_PHASE_WX_PRODUCT_CONFIRM = "wx_product_confirm";
    public static final String ACT_PHASE_WX_PRODUCT_CONFIRM_NAME = "维系物品确认签收";

    //营销任务流程流程
    public static final String ACT_PHASE_MARKET_APPLY = "market_apply";
    public static final String ACT_PHASE_MARKET_APPLY_NAME = "省/市营销人员发起";
    public static final String ACT_PHASE_MARKET_AUDIT = "market_audit";
    public static final String ACT_PHASE_MARKET_AUDIT_NAME = "省/市政企指挥审批";

    // 主发电路开通流程
    public static final String ACT_PHASE_MAINCIRCUIT_APPLY = "mainCircuit_apply";
    public static final String ACT_PHASE_MAINCIRCUIT_APPLY_NAME = "资源实施申请";

    public static final String ACT_PHASE_MAINCIRCUIT_KTHM = "mainCircuit_kthm";
    public static final String ACT_PHASE_MAINCIRCUIT_KTHM_NAME = "营业受理";
    public static final String ACT_PHASE_MAINCIRCUIT_YWFQ = "mainCircuit_ywfq";
    public static final String ACT_PHASE_MAINCIRCUIT_YWFQ_NAME = "资源实施支撑发起";
    public static final String ACT_PHASE_MAINCIRCUIT_YWKT = "mainCircuit_ywkt";
    public static final String ACT_PHASE_MAINCIRCUIT_YWKT_NAME = "资源实施支撑处理";
    public static final String ACT_PHASE_MAINCIRCUIT_CONFIRM = "mainCircuit_confirm";
    public static final String ACT_PHASE_MAINCIRCUIT_CONFIRM_NAME = "客户经理确认起租时间";
    public static final String ACT_PHASE_MAINCIRCUIT_YYSLQZ = "mainCircuit_yyslqz";
    public static final String ACT_PHASE_MAINCIRCUIT_YYSLQZ_NAME = "营业受理起租";
    public static final String ACT_PHASE_MAINCIRCUIT_YZSFK = "mainCircuit_yzsfk";
    public static final String ACT_PHASE_MAINCIRCUIT_YZSFK_NAME = "一站式反馈";
    public static final String ACT_PHASE_MAINCIRCUIT_YWJH = "mainCircuit_ywjh";
    public static final String ACT_PHASE_MAINCIRCUIT_YWJH_NAME = "业务稽核";
    public static final String ACT_PHASE_MAINCIRCUIT_DDPJ = "mainCircuit_ddpj";
    public static final String ACT_PHASE_MAINCIRCUIT_DDPJ_NAME = "发起人评价";

    //电路资源核查
    public static final String ACT_PHASE_CRV = "crv";
    public static final String ACT_PHASE_CRV_NAME = "主发电路资源核查流程";
    public static final String ACT_PHASE_CRV_APPLY = "crv_apply";
    public static final String ACT_PHASE_CRV_APPLY_NAME = "资源核查申请";
    public static final String ACT_PHASE_CRV_INITIATE = "crv_initiate";
    public static final String ACT_PHASE_CRV_INITIATE_NAME = "业务受理";
    public static final String ACT_PHASE_CRV_DEAL = "crv_deal";
    public static final String ACT_PHASE_CRV_DEAL_NAME = "资源核查支撑处理";
    public static final String ACT_PHASE_CRV_CUSTMANAGER_CONFIRM = "crv_custmanager_confirm";
    public static final String ACT_PHASE_CRV_CUSTMANAGER_CONFIRM_NAME = "客户经理确认";
    public static final String ACT_PHASE_CRV_CRV_CUSTMANAGE_ORDER = "crv_custmanage_order";
    public static final String ACT_PHASE_CRV_CRV_CUSTMANAGE_ORDER_NAME = "发起人评价";

    //业务受理流程
    public static final String ACT_PHASE_BUSINESSACCEPTANCE = "businessAcceptance";
    public static final String ACT_PHASE_BUSINESSACCEPTANCE_NAME = "业务受理";
    public static final String ACT_PHASE_BA_APPLY = "ba_apply";
    public static final String ACT_PHASE_BA_APPLY_NAME = "业务受理申请";
    public static final String ACT_PHASE_BA_SCHEDULING = "ba_scheduling";
    public static final String ACT_PHASE_BA_SCHEDULING_NAME = "业务受理调度";
    public static final String ACT_PHASE_BA_ACCEPT = "ba_accept";
    public static final String ACT_PHASE_BA_ACCEPT_NAME = "业务受理";
    public static final String ACT_PHASE_BA_ACCEPT_CONFIRM = "ba_accept_confirm";
    public static final String ACT_PHASE_BA_ACCEPT_CONFIRM_NAME = "业务受理确认";
    public static final String ACT_PHASE_BA_INFORMATION_COMPLETE = "ba_Information_complete";
    public static final String ACT_PHASE_BA_INFORMATION_COMPLETE_NAME = "资料补全";
    public static final String ACT_PHASE_BA_INFORMATION_COMPLETE_CONFIRM = "ba_Information_complete_confirm";
    public static final String ACT_PHASE_BA_INFORMATION_COMPLETE_CONFIRM_NAME = "资料补全确认";
    public static final String ACT_PHASE_BA_CUSTMANAGER_EVALUATION = "ba_custmanager_evaluation";
    public static final String ACT_PHASE_BA_CUSTMANAGER_EVALUATION_NAME = "发起人评价";
    public static final String ACT_PHASE_BA_KTJF = "ba_ktjf";
    public static final String ACT_PHASE_BA_KTJF_NAME = "开通交付";

    //合同关联
    public static final String ORDER_TYPE_CONTRACTASSOCIATED_CODE = "contractAssociated";
    public static final String ORDER_TYPE_CONTRACTASSOCIATED_NAME = "合同关联";
    public static final String ACT_PHASE_CONTRACTASSOCIATED_START = "contractAssociated_start";
    public static final String ACT_PHASE_CONTRACTASSOCIATED_START_NAME = "创建订单";
    public static final String ACT_CONTRACT_ENTRY = "act_contract_entry";
    public static final String ACT_CONTRACT_ENTRY_NAME = "合同录入";

    // 营销任务派单
    public static final String PROCESS_MARKETTASK = "marketTask";
    public static final String PROCESS_MARKETTASK_NAME = "营销任务派单";
    public static final String TASK_MARKETTASK_SHENGSTART = "marketTask_shengStart";
    public static final String TASK_MARKETTASK_SHISTART = "marketTask_shiStart";
    public static final String TASK_MARKETTASK_SHENGHY = "marketTask_shenghy";
    public static final String TASK_MARKETTASK_SHIZQ = "marketTask_shizq";
    public static final String TASK_MARKETTASK_SHIHY = "marketTask_shihy";
    public static final String TASK_MARKETTASK_QUXIAN = "marketTask_quxian";
    public static final String TASK_MARKETTASK_KHJL = "marketTask_khjl";
    public static final String TASK_MARKETTASK_TOOPPO = "marketTask_toOppo";
    public static final String TASK_MARKETTASK_RELATEOPPO = "marketTask_relateOppo";
    public static final String TASK_MARKETTASK_COMPLETE = "marketTask_complete";
    public static final String TASK_MARKETTASK_CLOSE = "marketTask_close";
    public static final String TASK_STATUS_YFP = "5";
    public static final String TASK_STATUS_GZZ = "6";

    public static final String ACT_ZQ_ORDER_PROCESS = "orderProcess";
    public static final String ACT_ZQ_ORDER_PROCESS_NAME = "政企专区流程";
    public static final String ACT_ZQ_ORDER_PROCESS_SJZT = "usertask1";
    public static final String ACT_ZQ_ORDER_PROCESS_QXZT = "usertask2";
    public static final String ACT_ZQ_ORDER_PROCESS_SJJL = "usertask4";
    public static final String ACT_ZQ_ORDER_PROCESS_QXJL = "usertask5";

    /**
     * 支撑统计数据查询权限控制按钮id
     */
    public static final String SYS_FUNCTION_SUPPORT_QS = "415";
    public static final String SYS_FUNCTION_SUPPORT_DS = "394";

    public static final String SYS_FUNCTION_SUPPORT_AREA_QS = "417";
    public static final String SYS_FUNCTION_SUPPORT_AREA_DS = "396";

    //用款申请流程
    public static final String ACT_MONEY_APPLY_URL = "/moneyApply/toMoneyApplyAudit?urlType=deal&orderId=";
    public static final String ACT_MONEY_APPLY = "moneyApply";
    public static final String ACT_MONEY_APPLY_NAME = "用款申请（集团客户）";
    public static final String ACT_PHASE_MONEY_APPLY_ADD = "moneyApply_apply";
    public static final String ACT_PHASE_MONEY_APPLY_ADD_NAME = "客户经理申请";
    public static final String ACT_PHASE_MONEY_AUDIT = "moneyApply_audit";
    public static final String ACT_PHASE_MONEY_AUDIT_NAME = "财务经理办理";


    //用款申请流程(其他客户)
    public static final String ACT_MONEY_APPLY_QT_PROCESS_TYPE = "process_type_moneyApplyQt";
    public static final String ACT_MONEY_APPLY_QT_AUDIT_URL = "/moneyApplyQt/toMoneyApplyQtAudit?urlType=deal&orderId=";
    public static final String ACT_MONEY_APPLY_QT_EDIT_URL = "/moneyApplyQt/toMoneyApplyQtEdit?urlType=deal&orderId=";
    public static final String ACT_MONEY_APPLY_QT = "moneyApplyQt";
    public static final String ACT_MONEY_APPLY_QT_NAME = "用款申请（集团客户）";
    public static final String ACT_PHASE_MONEY_APPLY_QT_ADD = "moneyApplyQt_apply";
    public static final String ACT_PHASE_MONEY_APPLY_QT_ADD_NAME = "客户经理填写";
    public static final String ACT_PHASE_MONEY_QT_BMSH = "moneyApplyQt_bmsh";
    public static final String ACT_PHASE_MONEY_QT_BMSH_NAME = "部门领导审核";
    public static final String ACT_PHASE_MONEY_APPLY_QT_ZQJBRSH = "moneyApplyQt_zqjbrsh";
    public static final String ACT_PHASE_MONEY_APPLY_QT_ZQJBRSH_NAME = "政企经办人审核";
    public static final String ACT_PHASE_MONEY_QT_ZQLDSH = "moneyApplyQt_zqldsh";
    public static final String ACT_PHASE_MONEY_QT_ZQLDSH_NAME = "政企部领导审核";
    public static final String ACT_PHASE_MONEY_APPLY_QT_CWLDSH = "moneyApplyQt_cwldsh";
    public static final String ACT_PHASE_MONEY_APPLY_QT_CWLDSH_NAME = "财务领导审核";
    public static final String ACT_PHASE_MONEY_QT_CWJLBL = "moneyApplyQt_cwjlbl";
    public static final String ACT_PHASE_MONEY_QT_CWJLBL_NAME = "财务经理办理";
    public static final String ACT_PHASE_MONEY_QT_ZHCW = "moneyApplyQt_zhcw";
    public static final String ACT_PHASE_MONEY_QT_ZHCW_NAME = "知会财务接口人";

    //退款申请流程
    public static final String ACT_REFUND_URL = "/refund/toAudit?urlType=deal&orderId=";
    public static final String ACT_REFUND = "act_refund";
    public static final String ACT_REFUND_NAME = "退款申请";
    public static final String ACT_PHASE_REFUND_APPLY = "refund_apply";
    public static final String ACT_PHASE_REFUND_APPLY_NAME = "客户经理申请";
    public static final String ACT_PHASE_REFUND_AUDIT = "refund_audit";
    public static final String ACT_PHASE_REFUND_AUDIT_NAME = "财务经理办理";
    public static final String ACT_PHASE_REFUND_NOTICE = "refund_notice";
    public static final String ACT_PHASE_REFUND_NOTICE_NAME = "知会财务接口人";

    //发票申请流程
    public static final String ACT_INVOICEAPPLY = "invoiceApply";
    public static final String ACT_INVOICEAPPLY_NAME = "发票申请";
    public static final String ACT_PHASE_INVOICEAPPLY_ADD = "invoiceApply_add";
    public static final String ACT_PHASE_INVOICEAPPLY_ADD_NAME = "客户经理下单";
    public static final String ACT_PHASE_INVOICEAPPLY_ACCEPT = "invoiceApply_accept";
    public static final String ACT_PHASE_INVOICEAPPLY_ACCEPT_NAME = "业务受理";
    public static final String ACT_PHASE_INVOICEAPPLY_CONFIRM = "invoiceApply_confirmation";
    public static final String ACT_PHASE_INVOICEAPPLY_CONFIRM_NAME = "客户经理确认";

    //账本开立、付款账户维护申请流程
    public static final String ACT_ZBKL_URL = "/zbkl/toZbklAudit?urlType=deal&orderId=";
    public static final String ACT_ZBKL = "zbkl";
    public static final String ACT_ZBKL_NAME = "账本开立、付款账户维护申请";
    public static final String ACT_PHASE_ZBKL_APPLY = "zbkl_apply";
    public static final String ACT_PHASE_ZBKL_APPLY_NAME = "客户经理申请";
    public static final String ACT_PHASE_ZBKL_AUDIT = "zbkl_audit";
    public static final String ACT_PHASE_ZBKL_AUDIT_NAME = "财务经理办理";

    //领款申请流程
    public static final String ACT_WITHDRAW_URL = "/withDraw/toWithDrawAudit?urlType=deal&orderId=";
    public static final String ACT_WITHDRAW = "withDraw";
    public static final String ACT_WITHDRAW_NAME = "领款申请";
    public static final String ACT_PHASE_WITHDRAW_APPLY = "withDraw_apply";
    public static final String ACT_PHASE_WITHDRAW_APPLY_NAME = "客户经理申请";
    public static final String ACT_PHASE_WITHDRAW_AUDIT = "withDraw_audit";
    public static final String ACT_PHASE_WITHDRAW_AUDIT_NAME = "财务经理办理";

    //领款申请流程
    public static final String ACT_GROUPAUDIT_URL = "/groupAudit/toGroupAuditAudit?urlType=deal&orderId=";
    public static final String ACT_GROUPAUDIT = "groupAudit";
    public static final String ACT_GROUPAUDIT_NAME = "群组审批";
    public static final String ACT_PHASE_GROUPAUDIT_APPLY = "groupAudit_apply";
    public static final String ACT_PHASE_GROUPAUDIT_APPLY_NAME = "客户经理申请";
    public static final String ACT_PHASE_GROUPAUDIT_AUDIT = "groupAudit_audit";
    public static final String ACT_PHASE_GROUPAUDIT_AUDIT_NAME = "财务经理办理";

    //到账款打标流程
    public static final String ACT_DZKDB_URL = "/dzkdb/toDzkdbAudit?urlType=deal&orderId=";
    public static final String ACT_DZKDB = "dzkdb";
    public static final String ACT_DZKDB_NAME = "到账款打标流程";
    public static final String ACT_PHASE_DZKDB_APPLY = "dzkdb_apply";
    public static final String ACT_PHASE_DZKDB_APPLY_NAME = "客户经理申请";
    public static final String ACT_PHASE_DZKDB_AUDIT = "dzkdb_audit";
    public static final String ACT_PHASE_DZKDB_AUDIT_NAME = "财务经理办理";


    //资产调拨流程
    public static final String ACT_ASSET_TRANSFER_URL = "/assetTransfer/toAssetTransferAudit?urlType=deal&orderId=";
    public static final String ACT_ASSET_TRANSFER = "assetTransfer";
    public static final String ACT_ASSET_TRANSFER_NAME = "资产调拨流程";
    public static final String ACT_PHASE_ASSET_TRANSFER_ADD = "assetTransfer_apply";
    public static final String ACT_PHASE_ASSET_TRANSFER_ADD_NAME = "客户经理填写";
    public static final String ACT_PHASE_ASSET_TRANSFER_BMSH = "assetTransfer_bmsh";
    public static final String ACT_PHASE_ASSET_TRANSFER_BMSH_NAME = "部门领导审核";
    public static final String ACT_PHASE_ASSET_TRANSFER_ZQJBRSH = "assetTransfer_zqjbrsh";
    public static final String ACT_PHASE_ASSET_TRANSFER_ZQJBRSH_NAME = "政企经办人审核";
    public static final String ACT_PHASE_ASSET_TRANSFER_ZQJLSH = "assetTransfer_zqjlsh";
    public static final String ACT_PHASE_ASSET_TRANSFER_ZQJLSH_NAME = "政企部经理审核";
    public static final String ACT_PHASE_ASSET_TRANSFER_CWJLBL = "assetTransfer_cwjlbl";
    public static final String ACT_PHASE_ASSET_TRANSFER_CWJLBL_NAME = "财务经理办理";

    //支撑总监 支撑总监贡献度打分
    public static final String SUPPORT_CONTRIBUTION = "supportContribution"; //支撑贡献度打分
    public static final String SUPPORT_CONTRIBUTION_NAME = "支撑总监贡献度打分";//支撑总监贡献度打分
    public static final String SUPPORT_DF = "support_df";
    public static final String SUPPORT_DF_NAME = "贡献度打分";
    //新版红名单流程
    public static final String ACT_RED_LIST_URL = "/redList/toRedListAudit?urlType=deal&orderId=";
    public static final String ACT_RED_LIST = "redListNew";
    public static final String ACT_RED_LIST_NAME = "红名单流程";
    public static final String ACT_PHASE_RED_LIST_APPLY = "red_apply";
    public static final String ACT_PHASE_RED_LIST_APPLY_NAME = "业务申请";
    public static final String ACT_PHASE_RED_LIST_ZJSH = "red_zjsh";
    public static final String ACT_PHASE_RED_LIST_ZJSH_NAME = "总监审核";
    public static final String ACT_PHASE_RED_LIST_SJSH = "red_sjsh";
    public static final String ACT_PHASE_RED_LIST_SJSH_NAME = "上级审批";
    public static final String ACT_PHASE_RED_LIST_FGSH = "red_fgsh";
    public static final String ACT_PHASE_RED_LIST_FGSH_NAME = "分管领导审批";
    public static final String ACT_PHASE_RED_LIST_ZJLSH = "red_zjlsh";
    public static final String ACT_PHASE_RED_LIST_ZJLSH_NAME = "总经理审批";
    public static final String ACT_PHASE_RED_LIST_YWSL = "red_ywsl";
    public static final String ACT_PHASE_RED_LIST_YWSL_NAME = "业务受理";
    public static final String ACT_PHASE_RED_LIST_YWSH = "red_ywsh";
    public static final String ACT_PHASE_RED_LIST_YWSH_NAME = "业务审核";

    //滞纳金减免流程
    public static final String ACT_LATE_FEE_URL = "/lateFee/toLateFeeAudit?urlType=deal&orderId=";
    public static final String ACT_LATE_FEE = "lateFee";
    public static final String ACT_LATE_FEE_NAME = "滞纳金减免流程";
    public static final String ACT_PHASE_LATE_FEE_APPLY = "lateFee_apply";
    public static final String ACT_PHASE_LATE_FEE_APPLY_NAME = "业务申请";
    public static final String ACT_PHASE_LATE_FEE_ZJSH = "lateFee_zjsh";
    public static final String ACT_PHASE_LATE_FEE_ZJSH_NAME = "总监审核";
    public static final String ACT_PHASE_LATE_FEE_SJSH = "lateFee_sjsh";
    public static final String ACT_PHASE_LATE_FEE_SJSH_NAME = "上级审批";
    public static final String ACT_PHASE_LATE_FEE_FGSH = "lateFee_fgsh";
    public static final String ACT_PHASE_LATE_FEE_FGSH_NAME = "分管领导审批";
    public static final String ACT_PHASE_LATE_FEE_ZJLSH = "lateFee_zjlsh";
    public static final String ACT_PHASE_LATE_FEE_ZJLSH_NAME = "总经理审批";
    public static final String ACT_PHASE_LATE_FEE_YWSL = "lateFee_ywsl";
    public static final String ACT_PHASE_LATE_FEE_YWSL_NAME = "业务受理";
    public static final String ACT_PHASE_LATE_FEE_YWSH = "lateFee_ywsh";
    public static final String ACT_PHASE_LATE_FEE_YWSH_NAME = "业务审核";

    //缴费期流程
    public static final String ACT_PAYOUT_PERIOD_URL = "/payoutPeriod/toPayoutPeriodAudit?urlType=deal&orderId=";
    public static final String ACT_PAYOUT_PERIOD = "payoutPeriod";
    public static final String ACT_PAYOUT_PERIOD_NAME = "缴费期流程";
    public static final String ACT_PHASE_PAYOUT_PERIOD_APPLY = "payoutPeriod_apply";
    public static final String ACT_PHASE_PAYOUT_PERIOD_APPLY_NAME = "业务申请";
    public static final String ACT_PHASE_PAYOUT_PERIOD_ZJSH = "payoutPeriod_zjsh";
    public static final String ACT_PHASE_PAYOUT_PERIOD_ZJSH_NAME = "总监审核";
    public static final String ACT_PHASE_PAYOUT_PERIOD_SJSH = "payoutPeriod_sjsh";
    public static final String ACT_PHASE_PAYOUT_PERIOD_SJSH_NAME = "上级审批";
    public static final String ACT_PHASE_PAYOUT_PERIOD_FGSH = "payoutPeriod_fgsh";
    public static final String ACT_PHASE_PAYOUT_PERIOD_FGSH_NAME = "分管领导审批";
    public static final String ACT_PHASE_PAYOUT_PERIOD_ZJLSH = "payoutPeriod_zjlsh";
    public static final String ACT_PHASE_PAYOUT_PERIOD_ZJLSH_NAME = "总经理审批";
    public static final String ACT_PHASE_PAYOUT_PERIOD_YWSL = "payoutPeriod_ywsl";
    public static final String ACT_PHASE_PAYOUT_PERIOD_YWSL_NAME = "业务受理";
    public static final String ACT_PHASE_PAYOUT_PERIOD_YWSH = "payoutPeriod_ywsh";
    public static final String ACT_PHASE_PAYOUT_PERIOD_YWSH_NAME = "业务审核";

    //	商企能人收单
    public static final String ACT_PHASE_RECEIPT_LIST_NAME = "能人收单";
    public static final String ACT_PHASE_RECEIPT_CODE = "sq_receipt";

    //营销线索预警订单
    public static final String CLUE_WARNNING_ORDER = "clue_warnning_order";
    public static final String CLUE_WARNNING_ORDER_NAME = "营销线索预警";
    public static final String CLUE_MANAGER_DEAL = "clue_manager_deal";
    public static final String CLUE_MANAGER_DEAL_NAME = "领导审核";

    // 宽带升级预警(维系效果)
    public static final String ORDER_TYPE_WIDE_UP = "wide_up";
    public static final String ORDER_TYPE_NEW_WIDE_UP = "new_wide_up";
    public static final String ORDER_TYPE_WIDE_UP_NAME = "宽带升级预警";
    public static final String ACT_PHASE_WIDE_UP_WARNING_MANAGER_DEAL = "wide_up_warn_manager_deal";
    public static final String ACT_PHASE_NEW_WIDE_UP_WARNING_MANAGER_DEAL = "leader";
    public static final String ACT_PHASE_WIDE_UP_WARNING_MANAGER_DEAL_NAME = "领导处理";
    public static final String ORDER_TYPE_WIDE_UP_LEADER = "leader";


    // 红名单升级预警
    public static final String ORDER_TYPE_HMD_UP = "hmd_up";
    public static final String ORDER_TYPE_NEW_HMD_UP = "new_hmd_up";
    public static final String ORDER_TYPE_HMD_UP_NAME = "红名单升级预警";
    public static final String ACT_PHASE_HMD_UP_WARNING_MANAGER_DEAL = "hmd_up_warn_manager_deal";
    public static final String ACT_PHASE_NEW_HMD_UP_WARNING_MANAGER_DEAL = "hmd_leader";
    public static final String ACT_PHASE_HMD_UP_WARNING_MANAGER_DEAL_NAME = "领导处理";

    // 双线起租预警 C00019
    public static final String ORDER_TYPE_TC_LEASELINE_RELET = "leaseline_relet";
    public static final String ORDER_TYPE_TC_LEASELINE_RELET_NAME = "双线起租预警";
    public static final String ACT_PHASE_TC_LEASELINE_RELET_MANAGER_DEAL = "manager";
    public static final String ACT_PHASE_TC_LEASELINE_RELET_MANAGER_DEAL_NAME = "客户经理处理";

    // SI结算稽核预警 C00020
    public static final String ORDER_TYPE_SI_SETTLE = "si_settle_warn";
    public static final String ORDER_TYPE_SI_SETTLE_NAME = "SI结算稽核预警";
    public static final String ACT_PHASE_SI_SETTLE_WARNING_MANAGER_DEAL = "si_settle_manager_deal";
    public static final String ACT_PHASE_SI_SETTLE_WARNING_MANAGER_DEAL_NAME = "客户经理处理";
    public static final String ACT_PHASE_SI_SETTLE_WARNING_AUDIT_DEAL = "si_settle_audit_deal";
    public static final String ACT_PHASE_SI_SETTLE_WARNING_AUDIT_DEAL_NAME = "稽核人员处理";

    // 宽带二次预警
    public static final String ORDER_TYPE_WIDE_SECOND = "wide_second";
    public static final String ORDER_TYPE_WIDE_SECOND_NAME = "宽带二次预警";
    public static final String ACT_PHASE_WIDE_SECOND_MANAGER = "manager";
    public static final String ACT_PHASE_WIDE_SECOND_MANAGER_NAME = "客户经理处理";

    // 双线到期二次预警
    public static final String ORDER_TYPE_DW_SECOND = "double_wide_second";
    public static final String ORDER_TYPE_DW_SECOND_NAME = "双线二次预警";
    public static final String ACT_PHASE_DW_SECOND_MANAGER = "manager";
    public static final String ACT_PHASE_DW_SECOND_MANAGER_NAME = "客户经理处理";


    //宽带升级预警（维系执行）

    public static final String WIDE_UP_EXCUTION = "wide_up_execution";
    public static final String NEW_WIDE_UP_EXCUTION = "new_wide_up_execution";
    public static final String WIDE_UP_EXCUTION_NAME = "宽带升级预警（维系执行）";
    public static final String WIDE_UP_EXCUTION_LEADER = "leader";
    public static final String WIDE_UP_EXCUTION_LEADER_NAME = "总经理/副总经理处理";

    // 云犀停复机
    // 流程节点id
    public static final String ACT_PHASE_YXTFJ_APPLY = "act_task_yxtfj_apply";
    // 流程节点名称
    public static final String ACT_PHASE_YXTFJ_APPLY_NAME = "发起云犀停复机申请订单";
    // 流程节点id
    public static final String ACT_PHASE_YXTFJ_CONFIRM = "act_task_yxtfj_confirm";
    // 流程节点名称
    public static final String ACT_PHASE_YXTFJ_CONFIRM_NAME = "云犀停复机申请审核";

    public static final String YXTFJ_DEAL_URL = "/yxtfj/toOrderDeal?urlType=deal&orderId=";

    // 局方停机原因查询请求参数
    public static final String QRYDOWNTIMEREASON_REQ = "QRYDOWNTIMEREASON_REQ";
    // 局方停开机处理请求参数
    public static final String OPN_STP_NUMBER_REQ = "OPN_STP_NUMBER_REQ";
    // 用户资料三户接口请求参数
    public static final String SP_THREEPART_CHECK_REQ = "SP_THREEPART_CHECK_REQ";
    // 局方停机原因 url sys_param code
    public static final String QR_DOWNTIME_REASON_URL_CODE = "qrydowntimereason_url";
    // 局方开机 url sys_param code
    public static final String QR_OPEN_URL_CODE = "qryopen_url";
    // 三户接口 url sys_param code
    public static final String SP_THREE_PART_CHECK_URL_CODE = "spthreepartcheck_url";
    // 处理人 sys_param code
    public static final String DEAL_USER = "yxtfj_user";
    // 局方停机原因查询返回参数
    public static final String QRYDOWNTIMEREASON_RSP = "QRYDOWNTIMEREASON_RSP";
    // 局方开机返回参数
    public static final String OPN_STP_NUMBER_RSP = "OPN_STP_NUMBER_RSP";
    //三户接口返回参数
    public static final String MICROSERVICE_USERS_THREEPARTCHECK_RSP = "SP_THREEPART_CHECK_RSP";

    public static final String OPN_STP_METHOAD = "ecaop.trades.sell.mob.cbss.open.stop";

    public static final String OPN_STP_APPKEY = "zjltsjxt";

    // 停复机接口调用开关 sys_param code
    public static final String PROOF_NUMBER_CHECK_OPEN = "proof_number_check_open";

    // ict欠费预警
    public static final String ORDER_TYPE_ICT_ARREARS_WARN_CODE = "ictArrearsWarn";
    public static final String ORDER_TYPE_ICT_ARREARS_WARN_NAME = "ICT欠费预警";
    public static final String ACT_PHASE_ICT_ARREARS_WARN_MANAGER = "cust_manager_handle";
    public static final String ACT_PHASE_ICT_ARREARS_WARN_MANAGER_NAME = "客户经理处理";
    public static final String ACT_PHASE_ICT_ARREARS_WARN_CITY_ADMIN = "city_arrears_admin_handle";
    public static final String ACT_PHASE_ICT_ARREARS_WARN_CITY_ADMIN_NAME = "地市欠费管理员处理";
    public static final String ACT_PHASE_ICT_ARREARS_LEADER = "leader_audit";
    public static final String ACT_PHASE_ICT_ARREARS_LEADER_NAME = "领导审核";

    //未关联合同商机关闭开关
    public static final String UNBOUND_OPPORTUNITY_OFF_SWITCH = "unbound_opportunity_off_switch";
    //拜访时间到期商机关闭开关
    public static final String VISIT_TIME_OFF_SWITCH = "visit_time_off_switch";
    //无拜访时间到期商机关闭开关
    public static final String NOT_VISIT_TIME_OFF_SWITCH = "not_visit_time_off_switch";

    //经分文件订购 sftp 配置类型
    public static final String JF_COMMON_FTP = "jf_common_ftp";

    //管理中台文件 sftp 配置
    public static final String PERSONNEL_FILE_FTP_INFO = "personnel_file_ftp_info";
}
