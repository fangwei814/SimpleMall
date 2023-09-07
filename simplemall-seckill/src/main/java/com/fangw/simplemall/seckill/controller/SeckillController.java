package com.fangw.simplemall.seckill.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fangw.common.utils.R;
import com.fangw.simplemall.seckill.service.SeckillService;
import com.fangw.simplemall.seckill.to.SeckillSkuRedisTo;

@RestController
public class SeckillController {
    @Autowired
    private SeckillService seckillService;

    /**
     * 返回当前时间可以参与秒杀的商品信息
     * 
     * @return
     */
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus() {
        List<SeckillSkuRedisTo> vos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(vos);
    }
}
