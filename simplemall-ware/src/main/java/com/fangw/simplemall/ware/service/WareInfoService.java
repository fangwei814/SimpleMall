package com.fangw.simplemall.ware.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fangw.common.utils.PageUtils;
import com.fangw.simplemall.ware.entity.WareInfoEntity;
import com.fangw.simplemall.ware.vo.FareVo;

/**
 * 仓库信息
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 20:50:20
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 运费
     * 
     * @param addrId
     * @return
     */
    FareVo getFare(Long addrId);
}
