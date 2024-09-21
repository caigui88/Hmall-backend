package com.hmall.api.client;


import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import feign.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@FeignClient("item-service") //声明服务的名称（即需要拉取的实例的所属的服务的名称）
public interface ItemClient {

    @GetMapping("/items")//声明http请求的类型为Get 模仿生成请求url
    /*声明请求的返回值类型*/ List<ItemDTO> queryItemByIds( /*请求url的请求参数*/ @RequestParam("ids") Collection<Long> ids);

    @PutMapping("/stock/deduct")
    void deductStock(@RequestBody List<OrderDetailDTO> items);

}
