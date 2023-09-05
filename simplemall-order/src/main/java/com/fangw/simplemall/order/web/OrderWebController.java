package com.fangw.simplemall.order.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.fangw.simplemall.order.service.OrderService;
import com.fangw.simplemall.order.vo.OrderConfirmVo;

@Controller
public class OrderWebController {
    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) {
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("OrderConfirmData", confirmVo);
        return "confirm";
    }
}
