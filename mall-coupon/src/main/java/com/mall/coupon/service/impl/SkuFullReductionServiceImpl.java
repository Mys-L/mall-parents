package com.mall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.to.MemberPrice;
import com.mall.common.to.SkuReductionTo;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.coupon.dao.SkuFullReductionDao;
import com.mall.coupon.entity.MemberPriceEntity;
import com.mall.coupon.entity.SkuFullReductionEntity;
import com.mall.coupon.entity.SkuLadderEntity;
import com.mall.coupon.service.MemberPriceService;
import com.mall.coupon.service.SkuFullReductionService;
import com.mall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    SkuLadderService skuLadderService;
    @Autowired // 会员价格
    MemberPriceService memberPriceService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    /**
     *  SpuInfoServiceImpl 方法：saveSpuInfo-> 6.4) 保存sku 的优惠满减等信息 跨数据库保存mall_sms 数据库->sms_sku_ladder(打折) sms_sku_full_reduction(满减) sms_member_price(会员价格)
     */
    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        //1 保存满减打折,会员价格 sms_sku_ladder(打折)
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
        skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
        skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
        // 是否参加其他优惠
        skuLadderEntity.setAddOther(skuReductionTo.getFullCount());
        // 有的满减条件才保存
        if(skuReductionTo.getFullCount() > 0){
            skuLadderService.save(skuLadderEntity);
        }

        //2 满减的信息 sms_sku_full_reduction(满减)
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo, skuFullReductionEntity);
        this.save(skuFullReductionEntity);

        //3 会员价格 sms_member_price(会员价格)
        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> collect = memberPrice.stream().map(m -> {
            MemberPriceEntity priceEntity = new MemberPriceEntity();
            priceEntity.setSkuId(skuReductionTo.getSkuId());
            priceEntity.setMemberLevelId(m.getId());
            priceEntity.setMemberLevelName(m.getName());
            priceEntity.setMemberPrice(m.getPrice());
            priceEntity.setAddOther(1);
            return priceEntity;
        }).filter(item ->
                // 输入的商品价格必须要大于0才保存
                (item.getMemberPrice().compareTo(new BigDecimal("0")) > 0)
        ).collect(Collectors.toList());
        memberPriceService.saveBatch(collect);
    }

}