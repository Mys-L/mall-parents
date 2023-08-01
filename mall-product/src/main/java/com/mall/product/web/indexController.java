package com.mall.product.web;

import com.mall.product.entity.CategoryEntity;
import com.mall.product.service.CategoryService;
import com.mall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: indexController
 * @Description:
 * @Author: L
 * @Create: 2023-07-30 21:28
 * @Version: 1.0
 */

@Controller
public class indexController {
    @Autowired
    CategoryService categoryService;

    @GetMapping({"/", "index", "/index.html"})
    public String indexPage(Model model){
        List<CategoryEntity> entities =categoryService.getLevel1Categorys();
        model.addAttribute("categories",entities);
        return "index";
    }

    @ResponseBody
    @GetMapping("index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson(){
        Map<String, List<Catelog2Vo>> map = categoryService.getCatalogJson();
        return map;
    }
}
