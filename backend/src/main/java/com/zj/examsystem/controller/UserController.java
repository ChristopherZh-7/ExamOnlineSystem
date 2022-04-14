package com.zj.examsystem.controller;


import com.zj.examsystem.config.security.JwtUtils;
import com.zj.examsystem.entity.User;
import com.zj.examsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 0. 校验验证码
     * 1. 校验用户名和密码
     * 2. 更新SecurityContext中的内容
     * 3. 生成该访问用户的JWT令牌
     * 4. 从Authentication中获取UserDetails
     * 5. 将JWT和UserDetails放入响应包中
     *
     * @param user 登录请求
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody User user) {
        // 创建Token及认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getRoleId() + " " + user.getAccount(), user.getPassword()));
        // 保存认证信息
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 生成自定义的Token
        String jwt = jwtUtils.generateJwtToken(authentication, user.getRoleId());

        // 获取到已认证过的用户信息
        User userDetails = (User) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("account", userDetails.getAccount());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/findUserByAccount")
    @ResponseBody
    public Object findUserByAccount(String account) {
        return userService.findUserByAccount(account);
    }
}
