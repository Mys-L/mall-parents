package com.mall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.constant.Constant;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.ware.dao.PurchaseDao;
import com.mall.ware.entity.PurchaseDetailEntity;
import com.mall.ware.entity.PurchaseEntity;
import com.mall.ware.service.PurchaseDetailService;
import com.mall.ware.service.PurchaseService;
import com.mall.ware.service.WareSkuService;
import com.mall.ware.vo.MergeVo;
import com.mall.ware.vo.PurchaseDoneVo;
import com.mall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;
    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)
        );

        return new PageUtils(page);
    }

    /*
     * @author: L
     * @description: 合并采购需求
     * @date: 22:07:00
     * @param: mergeVo
     * TODO:
     **/
    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null){
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(Constant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        //TODO 确认采购单状态是0或者1才能合并
        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> list = items.stream().map(i -> {
            PurchaseDetailEntity purchaseDetail = new PurchaseDetailEntity();
            purchaseDetail.setId(i);
            purchaseDetail.setPurchaseId(finalPurchaseId);
            purchaseDetail.setStatus(Constant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return purchaseDetail;
        }).toList();
        purchaseDetailService.updateBatchById(list);
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());

        this.updateById(purchaseEntity);
    }

    /**
     * 领取采购单
     * @param ids：采购单id
     * 过滤采购需求，并同步采购需求的状态
     */
    @Override
    public void received(List<Long> ids) {
        //1 确认当前采购单是新建或者已分配的状态
        List<PurchaseEntity> list = ids.stream().map(id -> {
            PurchaseEntity purchase = this.getById(id);
            return purchase;
        }).filter(item -> {
            if (item.getStatus() == Constant.PurchaseStatusEnum.CREATED.getCode() ||
                    item.getStatus() == Constant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).map(item->{
            item.setStatus(Constant.PurchaseStatusEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).toList();

        //2 改变采购单的状态
        this.updateBatchById(list);

        //3 改变采购项的状态
        list.forEach(item->{
            List<PurchaseDetailEntity> entities =purchaseDetailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> detailList = entities.stream().map(entity -> {
                PurchaseDetailEntity detail = new PurchaseDetailEntity();
                detail.setId(entity.getId());
                detail.setStatus(Constant.PurchaseDetailStatusEnum.BUYING.getCode());
                return detail;
            }).toList();
            purchaseDetailService.updateBatchById(detailList);
        });
    }

    /**
     * 完成采购单
     */
    @Transactional
    @Override
    public void done(PurchaseDoneVo doneVo) {
        Long id = doneVo.getId();
        // 2 改变采购项的状态
        Boolean flag=true;
        List<PurchaseItemDoneVo> items = doneVo.getItems();
        List<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemDoneVo item:items) {
            PurchaseDetailEntity purchaseDetail = new PurchaseDetailEntity();
            if (item.getStatus() == Constant.PurchaseDetailStatusEnum.HASERROR.getCode()){
                flag = false;
                purchaseDetail.setStatus(item.getStatus());
            }else {
                purchaseDetail.setStatus(Constant.PurchaseDetailStatusEnum.FINISH.getCode());
                // 3 将成功的采购入库
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum());
            }
            purchaseDetail.setId(item.getItemId());
            updates.add(purchaseDetail);
        }
        purchaseDetailService.updateBatchById(updates);
        // 1 改变采购单的状态 (此状态依赖于采购项的状态)
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag?Constant.PurchaseStatusEnum.FINISH.getCode() : Constant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);


    }

}