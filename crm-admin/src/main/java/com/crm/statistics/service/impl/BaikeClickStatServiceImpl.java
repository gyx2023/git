package com.crm.statistics.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.page.TableDataInfo;
import com.crm.statistics.domain.dto.BaikeClickStatDTO;
import com.crm.statistics.domain.vo.BaikeClickStatVO;
import com.crm.statistics.service.IBaikeClickStatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 百科点击量统计Service实现
 */
@Slf4j
@Service
public class BaikeClickStatServiceImpl implements IBaikeClickStatService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public TableDataInfo<BaikeClickStatVO> queryList(BaikeClickStatDTO dto) {
        log.info("查询百科点击量统计，参数：{}", dto);

        List<BaikeClickStatVO> list = queryData(dto);

        // 手动分页
        int total = list.size();
        int fromIndex = (dto.getPageNum() - 1) * dto.getPageSize();
        int toIndex = Math.min(fromIndex + dto.getPageSize(), total);

        List<BaikeClickStatVO> pageList = fromIndex < total ? list.subList(fromIndex, toIndex) : new ArrayList<>();

        TableDataInfo<BaikeClickStatVO> tableDataInfo = new TableDataInfo<>();
        tableDataInfo.setRows(pageList);
        tableDataInfo.setTotal(total);
        tableDataInfo.setCode(200);
        tableDataInfo.setMsg("查询成功");

        return tableDataInfo;
    }

    @Override
    public List<BaikeClickStatVO> exportList(BaikeClickStatDTO dto) {
        log.info("导出百科点击量统计，参数：{}", dto);
        return queryData(dto);
    }

    private List<BaikeClickStatVO> queryData(BaikeClickStatDTO dto) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT a.name_ AS areaName, b.name_ AS baikeName, COUNT(bs.baike_id) AS clickCount ");
        sql.append("FROM topp_baike_statistics bs ");
        sql.append("LEFT JOIN topp_baike b ON b.id_ = bs.baike_id ");
        sql.append("LEFT JOIN sys_area a ON bs.visitor_area = a.id_ ");
        sql.append("WHERE (a.id_ LIKE '3300%' OR (a.id_ LIKE '33%' AND type_ = '二级')) ");

        if (dto.getLeftTime() != null && !dto.getLeftTime().isEmpty()) {
            sql.append("AND DATE_FORMAT(bs.visitor_time, '%Y-%m-%d') >= '").append(dto.getLeftTime()).append("' ");
        }
        if (dto.getRightTime() != null && !dto.getRightTime().isEmpty()) {
            sql.append("AND DATE_FORMAT(bs.visitor_time, '%Y-%m-%d') <= '").append(dto.getRightTime()).append("' ");
        }

        sql.append("GROUP BY a.name_, b.name_ ");
        sql.append("ORDER BY a.id_, clickCount DESC ");

        log.info("百科点击量统计SQL: {}", sql);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString());
        List<BaikeClickStatVO> resultList = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            BaikeClickStatVO vo = new BaikeClickStatVO();
            vo.setAreaName(getStringValue(row.get("areaName")));
            vo.setBaikeName(getStringValue(row.get("baikeName")));
            vo.setClickCount(getLongValue(row.get("clickCount")));
            resultList.add(vo);
        }
        return resultList;
    }

    private String getStringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private Long getLongValue(Object value) {
        if (value == null) {
            return 0L;
        }
        try {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            log.warn("转换为Long失败: {}", value);
            return 0L;
        }
    }
}
