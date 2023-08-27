package com.fangw.simplemall.ware.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * 
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 20:50:20
 */
@Data
@TableName("wms_purchase_detail")
public class PurchaseDetailEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	@TableId
	private Long id;
	/**
	 * 采购单id
	 */
	private Long purchaseId;
	/**
	 * 采购商品id
	 */
	private Long skuId;
	/**
	 * 采购数量
	 */
	private Integer skuNum;
	/**
	 * 采购金额
	 */
	private BigDecimal skuPrice;
	/**
	 * 仓库id
	 */
	private Long wareId;
	/**
	 * 状态[0新建，1已分配，2正在采购，3已完成，4采购失败]
	 */
	private Integer status;

}
