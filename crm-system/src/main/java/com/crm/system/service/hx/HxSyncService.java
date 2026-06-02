package com.crm.system.service.hx;

import com.crm.common.utils.DateUtils;
import com.crm.system.domain.dao.topp.ToppContractDo;
import com.crm.system.domain.dao.topp.HxProjectContractDo;
import com.crm.system.domain.dao.topp.HxProjectDo;
import com.hollycrm.ws.hx.client.ContractBean;
import com.hollycrm.ws.hx.client.MsgHeader;
import com.hollycrm.ws.hx.client.OutParam;
import com.hollycrm.ws.hx.client.RemandContractBean;
import com.hollycrm.ws.hx.client.impl.OppoHxcbService;
import com.hollycrm.ws.hx.client.impl.OppoHxcbServiceImplService;
import com.crm.system.mapper.topp.HxProjectContractMapper;
import com.crm.system.mapper.topp.HxProjectMapper;
import com.crm.system.mapper.topp.ToppContractMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 划小系统同步服务
 * 老系统对照：ContractService.syscAddHx (line 1483) / syscRemoveHx (line 1506)
 *
 * 调用链: syscAddHx → HxProjectMapper.selectByOppoId → WebService → HxProjectContractMapper.insert
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HxSyncService {

    private final HxProjectMapper hxProjectMapper;
    private final HxProjectContractMapper hxProjectContractMapper;
    private final ToppContractMapper toppContractMapper;

    @Value("${hx.wsdl.url:http://10.202.2.102:8083/oppo/services/OppoGdServiceSrv?wsdl}")
    private String wsdlUrl;

    @Value("${hx.source.system.id:OPPOHXCB2019}")
    private String sourceSystemId;

    @Value("${hx.source.system.name:商机与划小系统}")
    private String sourceSystemName;

    /**
     * 同步合同关联到划小系统（对照老系统 line 1483）
     */
    @Transactional(rollbackFor = Exception.class)
    public void syscAddHx(Long oppoId, Long contractId) {
        try {
            log.info("-------同步划小开始: oppoId={}, contractId={} -------", oppoId, contractId);

            // 1. 查询 hx_project 表中 oppo_id 关联的划小项目
            List<HxProjectDo> hxProjects = hxProjectMapper.selectByOppoId(oppoId);
            if (hxProjects.isEmpty()) {
                log.info("无需同步: oppoId={} 没有关联的划小项目", oppoId);
                return;
            }
            log.info("本次需要同步至划小项目信息 {} 条", hxProjects.size());

            // 2. 查询合同信息
            ToppContractDo contract = toppContractMapper.selectById(contractId);
            if (contract == null) {
                log.warn("合同不存在: contractId={}", contractId);
                return;
            }

            // 3. 对每个划小项目调用 WebService 同步 + 插入关联记录
            for (HxProjectDo hxProject : hxProjects) {
                // 3a. 调用 Huaxiao WebService 同步
                callHuaxiaoWebService(hxProject.getRemandCode(), contract, "add");

                // 3b. 插入 hx_project_contract 记录
                HxProjectContractDo record = new HxProjectContractDo();
                record.setHxProjectId(hxProject.getId());
                record.setContractId(contractId);
                record.setIsValid("1");
                record.setCreateTime(new Date());
                hxProjectContractMapper.insert(record);
            }

            log.info("-------同步划小结束 -------");
        } catch (Exception e) {
            log.error("-------同步划小异常: oppoId={}, contractId={} -------", oppoId, contractId, e);
        }
    }

    /**
     * 取消合同关联（对照老系统 line 1506）
     */
    @Transactional(rollbackFor = Exception.class)
    public void syscRemoveHx(Long oppoId, Long contractId) {
        try {
            log.info("-------取消同步划小: oppoId={}, contractId={} -------", oppoId, contractId);

            // 1. 查询 hx_project_contract 表中对应记录
            List<HxProjectContractDo> records = hxProjectContractMapper.selectByContractId(contractId);
            if (records.isEmpty()) {
                log.info("无需取消: contractId={} 没有关联的划小项目", contractId);
                return;
            }
            log.info("本次需要取消的划小项目信息 {} 条", records.size());

            // 2. 对每个记录调用 WebService 取消 + 逻辑删除
            for (HxProjectContractDo record : records) {
                // 查询划小项目信息（用于 WebService 调用）
                HxProjectDo hxProject = hxProjectMapper.selectById(record.getHxProjectId());
                String remandCode = hxProject != null ? hxProject.getRemandCode() : null;

                // 2a. 调用 Huaxiao WebService 取消关联
                callHuaxiaoWebService(remandCode, null, "del");

                // 2b. 逻辑删除（is_valid='0'）
                hxProjectContractMapper.updateInvalidById(record.getId());
            }

            log.info("-------取消同步划小结束 -------");
        } catch (Exception e) {
            log.error("-------取消同步划小异常: oppoId={}, contractId={} -------", oppoId, contractId, e);
        }
    }

    /**
     * 调用划小 WebService（老系统: HxProjectWebServiceService.sysnHxProjectContracts）
     * 使用 JAX-WS 生成的桩类进行类型安全调用，与老系统完全一致
     */
    private void callHuaxiaoWebService(String remandCode, ToppContractDo contract, String action) {
        try {
            URL wsdlLocation = new URL(wsdlUrl);
            OppoHxcbServiceImplService service = new OppoHxcbServiceImplService(wsdlLocation);
            OppoHxcbService oppoHxcbService = service.getOppoHxcbServiceImplPort();

            // 构建消息头
            MsgHeader msgHeader = new MsgHeader();
            msgHeader.setSourceSystemId(sourceSystemId);
            msgHeader.setSourceSystemName(sourceSystemName);

            // 构建请求体
            RemandContractBean remandContractBean = new RemandContractBean();
            remandContractBean.setRemandCode(remandCode);

            RemandContractBean.ContractList contractList = new RemandContractBean.ContractList();
            ContractBean contractBean = new ContractBean();

            if ("add".equals(action) && contract != null) {
                // 新增关联（对照老系统 line 79-93）
                contractBean.setContractCode(contract.getContractNo());
                contractBean.setContractName(contract.getContractName());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                if (contract.getStartDate() != null) {
                    contractBean.setContractStartDate(sdf.format(contract.getStartDate()));
                }
                if (contract.getEndDate() != null) {
                    contractBean.setContractEndDate(sdf.format(contract.getEndDate()));
                }
                contractBean.setContractRelateState("1");
                contractBean.setContractRelateTime(DateUtils.dateTimeNow("yyyy-MM-dd HH:mm:ss"));
            } else {
                // 取消关联（对照老系统 line 91-93）
                contractBean.setContractRelateState("0");
                contractBean.setContractCancelTime(DateUtils.dateTimeNow("yyyy-MM-dd HH:mm:ss"));
            }

            contractList.getContractBean().add(contractBean);
            remandContractBean.setContractList(contractList);

            log.info("调用划小WebService: action={}, remandCode={}, contractNo={}",
                action, remandCode, contract != null ? contract.getContractNo() : null);

            // 调用
            OutParam outParam = oppoHxcbService.remandContractSave(msgHeader, remandContractBean);

            if ("2".equals(outParam.getReturnCode())) {
                log.warn("WebService 业务失败: action={}, remandCode={}, msg={}",
                    action, remandCode, outParam.getReturnMsg());
                throw new RuntimeException("划小WebService调用失败: " + outParam.getReturnMsg());
            }

            log.info("WebService调用成功: action={}, remandCode={}", action, remandCode);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("划小WebService调用异常: action={}, remandCode={}", action, remandCode, e);
            throw new RuntimeException("划小WebService调用异常", e);
        }
    }
}
