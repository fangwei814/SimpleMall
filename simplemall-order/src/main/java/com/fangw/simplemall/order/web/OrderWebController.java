package com.fangw.simplemall.order.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fangw.common.exception.NoStockException;
import com.fangw.simplemall.order.service.OrderService;
import com.fangw.simplemall.order.vo.OrderConfirmVo;
import com.fangw.simplemall.order.vo.OrderSubmitVo;
import com.fangw.simplemall.order.vo.SubmitOrderResponseVo;

@Controller
public class OrderWebController {
    @Autowired
    OrderService orderService;

    /**
     * 提交订单
     * 
     * @param vo
     * @param model
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes) {
        // 1.创建订单、验令牌、验价格、验库存
        try {
            SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);
            if (responseVo.getCode() == 0) {
                // 下单成功来到支付选择页
                model.addAttribute("submitOrderResp", responseVo);
                return "pay";
            } else {
                // 下单失败回到订单确认页重新确认订单信息
                String msg = "下单失败: ";
                switch (responseVo.getCode()) {
                    case 1:
                        msg += "订单信息过期，请刷新再次提交";
                        break;
                    case 2:
                        msg += "订单商品价格发生变化，请确认后再次提交";
                        break;
                    case 3:
                        msg += "商品库存不足";
                        break;
                }
                redirectAttributes.addAttribute("msg", msg);
                return "redirect:http://order.simplemall.com/toTrade";
            }
        } catch (Exception e) {
            if (e instanceof NoStockException) {
                String message = e.getMessage();
                redirectAttributes.addFlashAttribute("msg", message);
            }
            return "redirect:http://order.simplemall.com/toTrade";
        }
    }

    /**
     * 获取订单各种信息，生成订单
     * 
     * @param model
     * @return
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model) {
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", confirmVo);
        return "confirm";
    }
}
