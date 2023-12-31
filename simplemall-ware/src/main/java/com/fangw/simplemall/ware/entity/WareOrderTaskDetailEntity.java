package com.fangw.simplemall.ware.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存工作单
 * 
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 20:50:20
 */
@Data
@TableName("wms_ware_order_task_detail")
@AllArgsConstructor
@NoArgsConstructor
public class WareOrderTaskDetailEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     * 购买个数
     */
    private Integer skuNum;
    /**
     * 工作单id
     */
    private Long taskId;
    /**
     * 仓库id
     */
    private Long wareId;
    /**
     * 锁定状态，1-已锁定 2-已解锁 3-扣减
     */
    private Integer lockStatus;

}
