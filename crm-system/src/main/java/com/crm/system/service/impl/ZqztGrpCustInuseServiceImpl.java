package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.common.core.domain.dto.*;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.helper.LoginHelper;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.utils.StringUtils;
import com.crm.generator.util.Desensitization;
import com.crm.system.domain.ZqztGrpCustInuse;
import com.crm.system.domain.bo.ZqztGrpCustInuseBo;
import com.crm.system.domain.param.SysParamDo;
import com.crm.system.domain.vo.ZqztGrpCustInuseVo;
import com.crm.system.mapper.param.SysParamMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.crm.system.mapper.ZqztGrpCustInuseMapper;
import com.crm.system.service.IZqztGrpCustInuseService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 合同和关联客户IDService业务层处理
 *
 * @author crm
 * @date 2026-01-19
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class ZqztGrpCustInuseServiceImpl implements IZqztGrpCustInuseService {

    private final ZqztGrpCustInuseMapper baseMapper;
    @Resource
    private SysParamMapper sysParamMapper;

    /**
     * 查询政企主体-实际使用客户（zqzt_grp_cust_inuse）
     */
    @Override
    public ZqztGrpCustInuseVo queryById(String inuseId){
        return baseMapper.selectVoById(inuseId);
    }

    /**
     * 查询政企主体-实际使用客户（zqzt_grp_cust_inuse）列表
     */
    @Override
    public TableDataInfo<ZqztGrpCustInuseVo> queryPageList(ZqztGrpCustInuseBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ZqztGrpCustInuse> lqw = buildQueryWrapper(bo);
        Page<ZqztGrpCustInuseVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询政企主体-实际使用客户（zqzt_grp_cust_inuse）列表
     */
    @Override
    public List<ZqztGrpCustInuseVo> queryList(ZqztGrpCustInuseBo bo) {
        LambdaQueryWrapper<ZqztGrpCustInuse> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ZqztGrpCustInuse> buildQueryWrapper(ZqztGrpCustInuseBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ZqztGrpCustInuse> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getContractId()), ZqztGrpCustInuse::getContractId, bo.getContractId());
        lqw.like(StringUtils.isNotBlank(bo.getContractName()), ZqztGrpCustInuse::getContractName, bo.getContractName());
        lqw.eq(bo.getGrpIdA() != null, ZqztGrpCustInuse::getGrpIdA, bo.getGrpIdA());
        lqw.eq(bo.getRelation() != null, ZqztGrpCustInuse::getRelation, bo.getRelation());
        lqw.eq(bo.getGrpIdB() != null, ZqztGrpCustInuse::getGrpIdB, bo.getGrpIdB());
        lqw.eq(StringUtils.isNotBlank(bo.getIsValid()), ZqztGrpCustInuse::getIsValid, bo.getIsValid());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpCodeA()), ZqztGrpCustInuse::getGrpCodeA, bo.getGrpCodeA());
        lqw.eq(StringUtils.isNotBlank(bo.getHqGrpCodeA()), ZqztGrpCustInuse::getHqGrpCodeA, bo.getHqGrpCodeA());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpNameA()), ZqztGrpCustInuse::getGrpNameA, bo.getGrpNameA());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpCodeB()), ZqztGrpCustInuse::getGrpCodeB, bo.getGrpCodeB());
        lqw.eq(StringUtils.isNotBlank(bo.getHqGrpCodeB()), ZqztGrpCustInuse::getHqGrpCodeB, bo.getHqGrpCodeB());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpNameB()), ZqztGrpCustInuse::getGrpNameB, bo.getGrpNameB());
        lqw.eq(StringUtils.isNotBlank(bo.getRegionId()), ZqztGrpCustInuse::getRegionId, bo.getRegionId());
        lqw.eq(StringUtils.isNotBlank(bo.getCountyId()), ZqztGrpCustInuse::getCountyId, bo.getCountyId());
        lqw.eq(StringUtils.isNotBlank(bo.getOperatorId()), ZqztGrpCustInuse::getOperatorId, bo.getOperatorId());
        return lqw;
    }

    /**
     * 新增政企主体-实际使用客户（zqzt_grp_cust_inuse）
     */
    @Override
    public Boolean insertByBo(ZqztGrpCustInuseBo bo) {
        ZqztGrpCustInuse add = BeanUtil.toBean(bo, ZqztGrpCustInuse.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setInuseId(add.getInuseId());
        }
        return flag;
    }

    /**
     * 修改政企主体-实际使用客户（zqzt_grp_cust_inuse）
     */
    @Override
    public Boolean updateByBo(ZqztGrpCustInuseBo bo) {
        ZqztGrpCustInuse update = BeanUtil.toBean(bo, ZqztGrpCustInuse.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ZqztGrpCustInuse entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除政企主体-实际使用客户（zqzt_grp_cust_inuse）
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
    /**
     * 查询合同和关联客户ID列表
     */
    @Override
    public TableDataInfo<ZqztGrpCustInuseRespDTO> queryPageList(ZqztGrpCustReqDTO reqDTO, PageQuery pageQuery) {
        // 获取总记录数
        int total = baseMapper.queryZqztGrpCustInuseListTotal(reqDTO);

        // 计算分页起始位置和结束位置
        int startIndex = (pageQuery.getPageNum() - 1) * pageQuery.getPageSize();
        int endIndex = Math.min(startIndex + pageQuery.getPageSize(), total);
        reqDTO.setStartIndex(startIndex);
        reqDTO.setEndIndex(endIndex);

        // 获取完整数据列表
        List<ZqztGrpCustInuseRespDTO> list = baseMapper.queryZqztGrpCustInuseList(reqDTO);

        // 创建分页对象
        Page<ZqztGrpCustInuseRespDTO> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());

        // 设置分页数据
        page.setRecords(list);
        page.setTotal(total);

        SysParamDo sysParamDO = sysParamMapper.selectOne(new LambdaQueryWrapper<SysParamDo>().eq(SysParamDo::getType, "SENSITIVE_ROLE"));
        List<String> sensitiveRoles = Arrays.asList(sysParamDO.getValue().split(","));
        LoginUser loginUser = LoginHelper.getLoginUser();
        List<RoleDTO> roles = loginUser.getRoles();
        boolean isSensitive = sensitiveRoles.stream()
            .anyMatch(roleStr -> roles.stream()
                .anyMatch(roleDTO -> roleStr.equals(String.valueOf(roleDTO.getRoleId()))));

        // 根据用户角色决定是否对查询结果进行脱敏处理
        if (!isSensitive) {
            List<ZqztGrpCustInuseRespDTO> desensitizedList = page.getRecords().stream()
                .map(this::desensitizeVo)
                .collect(Collectors.toList());
            page.setRecords(desensitizedList);
        }

        return TableDataInfo.build(page);
    }

    /**
     * 对ZqztGrpCustInuseRespDTO对象进行脱敏处理
     *
     * @param custInuseDTO 要脱敏的ZqztGrpCustInuseRespDTO对象
     * @return 脱敏后的ZqztGrpCustInuseRespDTO对象
     */
    private ZqztGrpCustInuseRespDTO desensitizeVo(ZqztGrpCustInuseRespDTO custInuseDTO) {
        if (custInuseDTO == null) {
            return null;
        }

        // 对合同名称进行脱敏
        custInuseDTO.setContractName(Desensitization.desensitizeCompanyName(custInuseDTO.getContractName()));
        // 对签约集团名称进行脱敏
        custInuseDTO.setGrpNameA(Desensitization.desensitizeName(custInuseDTO.getGrpNameA()));
        // 对关联客户集团名称进行脱敏
        custInuseDTO.setGrpNameB(Desensitization.desensitizeName(custInuseDTO.getGrpNameB()));

        return custInuseDTO;
    }

    /**
     * 批量删除合同和关联客户ID
     */
    @Override
    public Boolean deleteWithValidByIds(String[] ids, Boolean isValid) {
        Map<String, String> loginUser = getLoginUser();

        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.updateByHqGrpCodeB(ids,loginUser);
    }

    /**
     * 查询关联客户用户列表
     *
     * @param reqDTO
     * @param pageQuery
     * @return
     */
    @Override
    public TableDataInfo<ZqztGrpCustInuseUserListRespDTO> queryPageUserList(ZqztGrpCustReqDTO reqDTO, PageQuery pageQuery) {
        // 获取完整数据列表
        List<ZqztGrpCustInuseUserListRespDTO> list = baseMapper.queryPageUserList(reqDTO);

        // 创建分页对象
        Page<ZqztGrpCustInuseUserListRespDTO> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());

        // 获取总记录数
        int total = list.size();

        // 计算分页起始位置和结束位置
        int startIndex = (pageQuery.getPageNum() - 1) * pageQuery.getPageSize();
        int endIndex = Math.min(startIndex + pageQuery.getPageSize(), total);

        // 分页处理
        List<ZqztGrpCustInuseUserListRespDTO> pagedList = new ArrayList<>();
        if (startIndex < total) {
            pagedList = list.subList(startIndex, endIndex);
        }

        // 设置分页数据
        page.setRecords(pagedList);
        page.setTotal(total);

        SysParamDo sysParamDO = sysParamMapper.selectOne(new LambdaQueryWrapper<SysParamDo>().eq(SysParamDo::getType, "SENSITIVE_ROLE"));
        List<String> sensitiveRoles = Arrays.asList(sysParamDO.getValue().split(","));
        LoginUser loginUser = LoginHelper.getLoginUser();
        List<RoleDTO> roles = loginUser.getRoles();
        boolean isSensitive = sensitiveRoles.stream()
            .anyMatch(roleStr -> roles.stream()
                .anyMatch(roleDTO -> roleStr.equals(String.valueOf(roleDTO.getRoleId()))));

        // 根据用户角色决定是否对查询结果进行脱敏处理
        if (!isSensitive) {
            List<ZqztGrpCustInuseUserListRespDTO> desensitizedList = page.getRecords().stream()
                .map(this::desensitizeUserListVo)
                .collect(Collectors.toList());
            page.setRecords(desensitizedList);
        }

        return TableDataInfo.build(page);
    }

    /**
     * 对ZqztGrpCustInuseUserListRespDTO对象进行脱敏处理
     *
     * @param custInuseUserListDTO 要脱敏的ZqztGrpCustInuseUserListRespDTO对象
     * @return 脱敏后的ZqztGrpCustInuseUserListRespDTO对象
     */
    private ZqztGrpCustInuseUserListRespDTO desensitizeUserListVo(ZqztGrpCustInuseUserListRespDTO custInuseUserListDTO) {
        if (custInuseUserListDTO == null) {
            return null;
        }

        // 对集团名称进行脱敏
        custInuseUserListDTO.setCustName(Desensitization.desensitizeCompanyName(custInuseUserListDTO.getCustName()));
        // 对业务号码进行脱敏
        custInuseUserListDTO.setSvcNum(Desensitization.desensitizeCompanyName(custInuseUserListDTO.getSvcNum()));

        return custInuseUserListDTO;
    }

    @Override
    public int deleteUserListWithValidByIds(String[] subscrbids, Boolean isValid) {
        Map<String, String> loginUser = getLoginUser();
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.updateByInuseId(subscrbids, loginUser);
    }

    /**
     * 查询关联客户列表
     */
    @Override
    public TableDataInfo<ZqztGrpCustInuseRespDTO> queryPageGrpList(ZqztGrpCustReqDTO reqDTO, PageQuery pageQuery) {
        // 获取完整数据列表
        List<ZqztGrpCustInuseRespDTO> list = baseMapper.queryPageGrpList(reqDTO);

        // 创建分页对象
        Page<ZqztGrpCustInuseRespDTO> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());

        // 获取总记录数
        int total = list.size();

        // 计算分页起始位置和结束位置
        int startIndex = (pageQuery.getPageNum() - 1) * pageQuery.getPageSize();
        int endIndex = Math.min(startIndex + pageQuery.getPageSize(), total);

        // 分页处理
        List<ZqztGrpCustInuseRespDTO> pagedList = new ArrayList<>();
        if (startIndex < total) {
            pagedList = list.subList(startIndex, endIndex);
        }

        // 设置分页数据
        page.setRecords(pagedList);
        page.setTotal(total);

        SysParamDo sysParamDO = sysParamMapper.selectOne(new LambdaQueryWrapper<SysParamDo>().eq(SysParamDo::getType, "SENSITIVE_ROLE"));
        List<String> sensitiveRoles = Arrays.asList(sysParamDO.getValue().split(","));
        LoginUser loginUser = LoginHelper.getLoginUser();
        List<RoleDTO> roles = loginUser.getRoles();
        boolean isSensitive = sensitiveRoles.stream()
            .anyMatch(roleStr -> roles.stream()
                .anyMatch(roleDTO -> roleStr.equals(String.valueOf(roleDTO.getRoleId()))));

        // 根据用户角色决定是否对查询结果进行脱敏处理
        if (!isSensitive) {
            List<ZqztGrpCustInuseRespDTO> desensitizedList = page.getRecords().stream()
                .map(this::desensitizeVo)
                .collect(Collectors.toList());
            page.setRecords(desensitizedList);
        }

        return TableDataInfo.build(page);
    }

    /**
     * 更新关联客户
     */
    @Override
    public int addInuse(ZqztGrpCustReqDTO reqDTO) {
        Map<String, String> loginUser = getLoginUser();
        return baseMapper.addInuse(reqDTO, loginUser);
    }

    /**
     * 查询待添加关联客户用户列表
     */
    @Override
    public TableDataInfo<ZqztGrpCustInuseUserListRespDTO> getAddUserList(ZqztGrpCustReqDTO reqDTO, PageQuery pageQuery) {
        if(StringUtils.isEmpty(reqDTO.getHqGrpCodeB())){
            return null;
        }
        // 获取cust_id信息
        String custId = baseMapper.getCustId(reqDTO);
        if(StringUtils.isEmpty(custId)){
            return null;
        }

        // 获取已关联用户列表
        List<String> userIds = baseMapper.queryUserIdArray(reqDTO);

        // 获取完整数据列表
        List<ZqztGrpCustInuseUserListRespDTO> list = baseMapper.queryPageAddUserList(reqDTO.getInuseId(), custId, userIds);

        // 创建分页对象
        Page<ZqztGrpCustInuseUserListRespDTO> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());

        // 获取总记录数
        int total = list.size();

        // 计算分页起始位置和结束位置
        int startIndex = (pageQuery.getPageNum() - 1) * pageQuery.getPageSize();
        int endIndex = Math.min(startIndex + pageQuery.getPageSize(), total);

        // 分页处理
        List<ZqztGrpCustInuseUserListRespDTO> pagedList = new ArrayList<>();
        if (startIndex < total) {
            pagedList = list.subList(startIndex, endIndex);
        }

        // 设置分页数据
        page.setRecords(pagedList);
        page.setTotal(total);

        SysParamDo sysParamDO = sysParamMapper.selectOne(new LambdaQueryWrapper<SysParamDo>().eq(SysParamDo::getType, "SENSITIVE_ROLE"));
        List<String> sensitiveRoles = Arrays.asList(sysParamDO.getValue().split(","));
        LoginUser loginUser = LoginHelper.getLoginUser();
        List<RoleDTO> roles = loginUser.getRoles();
        boolean isSensitive = sensitiveRoles.stream()
            .anyMatch(roleStr -> roles.stream()
                .anyMatch(roleDTO -> roleStr.equals(String.valueOf(roleDTO.getRoleId()))));

        // 根据用户角色决定是否对查询结果进行脱敏处理
        if (!isSensitive) {
            List<ZqztGrpCustInuseUserListRespDTO> desensitizedList = page.getRecords().stream()
                .map(this::desensitizeUserListVo)
                .collect(Collectors.toList());
            page.setRecords(desensitizedList);
        }

        return TableDataInfo.build(page);
    }

    /**
     * 添加关联客户用户
     */
    @Override
    public int addUserList(ZqztGrpCustAddUserListReqDTO reqDTO) {
        Map<String, String> loginUser = getLoginUser();

        if(reqDTO.getAddInuseUserList().size() == 0) {
            return 0;
        }
        String inuseId = reqDTO.getAddInuseUserList().get(0).get("inuseId");
        // 获取待关联用户信息
        List<ZqztGrpCustInuseUserListRespDTO> userList = baseMapper.queryPageAddUserData(inuseId, reqDTO.getAddInuseUserList());
        return baseMapper.addUserList(userList, loginUser);
    }


    public Map<String,String> getLoginUser() {
        Map<String, String> map = new HashMap<>();
        // 数据权限处理 - 根据传入的areaCode进行数据权限过滤
        LoginUser loginUser = LoginHelper.getLoginUser();
        String areaCode = loginUser.getAreaCode();
        Long userId = loginUser.getUserId();
        map.put("operatorId", String.valueOf(userId));
        // 如果不是省本级(330000)，则需要进行数据权限过滤
        // 判断区域代码级别
        if (areaCode.endsWith("0000")) {
            map.put("regionId", "330000");
            map.put("countyId", "330000");
            // 省级用户(虽然前面已经排除了330000，但为了完整性保留此判断)
        } else if (areaCode.endsWith("00")) {
            // 地市级用户(如330100)，只能查看本市数据
            map.put("regionId", areaCode);
            map.put("countyId", areaCode);
        } else {
            // 区县级用户(如330102)，可查看所属地市的所有数据
            // 提取地市代码(前4位+00)
            String cityCode = areaCode.substring(0, 4) + "00";
            map.put("regionId", cityCode);
            map.put("countyId", areaCode);
        }
        return map;
    }
}
