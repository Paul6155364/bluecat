package com.bluecat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.entity.ShopImage;
import com.bluecat.mapper.ShopImageMapper;
import com.bluecat.service.ShopImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 门店图片表 Service实现
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Service
@RequiredArgsConstructor
public class ShopImageServiceImpl extends ServiceImpl<ShopImageMapper, ShopImage> implements ShopImageService {
}
