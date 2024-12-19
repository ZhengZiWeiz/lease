package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.common.utils.JwtUtil;
import com.atguigu.lease.model.entity.SystemUser;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.admin.mapper.SystemPostMapper;
import com.atguigu.lease.web.admin.service.LoginService;
import com.atguigu.lease.web.admin.vo.login.CaptchaVo;
import com.atguigu.lease.web.admin.vo.login.LoginVo;
import com.wf.captcha.SpecCaptcha;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SystemPostMapper systemPostMapper;

    @Override
    public CaptchaVo getCaptcha() {
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 4);

        String code = specCaptcha.text().toLowerCase();
        String key = "admin:user"+ UUID.randomUUID();
        String image = specCaptcha.toBase64();

        stringRedisTemplate.opsForValue().set(key, code, 60, TimeUnit.SECONDS);

        return new CaptchaVo(image, key);
    }

    @Override
    public String login(LoginVo loginVo) {
        // 验证  如果验证成功返回 token

        // 1.判断是否输入例如验证码
        if( !StringUtils.hasText(loginVo.getCaptchaCode()) ){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_NOT_FOUND);
        }
        // 2.校验验证码
        String code = stringRedisTemplate.opsForValue().get(loginVo.getCaptchaKey());
        if(  code == null  ){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_EXPIRED);
        }

        if( !code.equals( loginVo.getCaptchaCode().toLowerCase() ) ){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_ERROR);
        }

        // 3.校验用户是否存在

        SystemUser systemUser = systemPostMapper.selectOneByUsername( loginVo.getUsername() );

        if( systemUser == null ){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_ERROR);
        }

        // 4.校验用户是否被禁了
        if( systemUser.getStatus() == BaseStatus.DISABLE ){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_ERROR);
        }

        // 5.校验用户密码
        if (!systemUser.getPassword().equals(DigestUtils.md5Hex(loginVo.getPassword()))) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_ERROR);
        }

        // 6.创建并返回 TOKEN
        return JwtUtil.createToken(systemUser.getId(), systemUser.getUsername());

    }
}
