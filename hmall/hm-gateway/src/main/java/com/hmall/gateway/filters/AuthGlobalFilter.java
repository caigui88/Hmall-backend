package com.hmall.gateway.filters;

import cn.hutool.core.text.AntPathMatcher;
import com.hmall.common.exception.UnauthorizedException;
import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;

    private final JwtTool jwtTool;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        System.out.println("进行统一的网关检测登陆校验");
        //1. 获取用户学习 request对象
        ServerHttpRequest request = exchange.getRequest();
        //2. 判断是否需要登陆拦截
        System.out.println("进行登陆拦截检测");
        if(isExclude(request.getPath().toString())){
            //放行
            System.out.println("登陆操作，可以直接放行");
            return chain.filter(exchange);
        }

        //3. 获取token
        System.out.println("token的获取");
        String token = null;

        List<String> headers = request.getHeaders().get("authorization");
        if (headers != null && !headers.isEmpty()) {
            token = headers.get(0);
        }
        //4. 校验和解析token
        System.out.println("校验和解析token");
        Long userId = null;
        try {
            userId = jwtTool.parseToken(token);
            System.out.println("用户id解析结果为："+userId);
        }catch (UnauthorizedException e){
            //拦截，设置状态码为401 未登录，未授权
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // TODO 5. 传递用户信息
        System.out.println("传递用户信息");
        String userInfo = userId.toString();

        ServerWebExchange swe = exchange.mutate()
                .request(builder -> builder.header("user-info", userInfo))
                .build();
        System.out.println("userId =: " + userId);

        //6. 放行
        System.out.println("放行");
        return chain.filter(swe);
//        return chain.filter(exchange);
    }

    private boolean isExclude(String path) {

        for (String pathPattern : authProperties.getExcludePaths()){
            if(antPathMatcher.match(pathPattern,path)) return true;
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
