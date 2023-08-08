package com.mall.search.service.impl;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.mall.common.to.elasticsearch.SkuElasticModel;
import com.mall.common.utils.R;
import com.mall.search.config.ElasticSearchConfig;
import com.mall.search.constant.ElasticSearchConstant;
import com.mall.search.feign.ProductFeignService;
import com.mall.search.service.MallSearchService;
import com.mall.search.vo.AttrResponseVo;
import com.mall.search.vo.BrandVo;
import com.mall.search.vo.SearchParam;
import com.mall.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author L
 */
@Slf4j
@Service
public class MallSearchServiceImpl implements MallSearchService {

//    @Autowired
//    ElasticsearchClient client;
//
//    @Override
//    public ElasticSearchResult search(ElasticSearchParam param) {
//        //返回结果对象
//        ElasticSearchResult result = null;
//        // 1 动态构建DSL语句，准备检索请求
//        SearchRequest.Builder builder = buildSearchRequrest(param);
//        // 2 执行检索请求
//        try {
//            SearchRequest searchRequest = builder.build();
//            System.out.println("代码生产的DSL语句："+searchRequest.query());
//            SearchResponse<ElasticSearchResult> searchResponse = client.search(searchRequest, ElasticSearchResult.class);
//            System.out.println("查询聚合结果："+searchResponse.aggregations());
//            System.out.println("查询结果："+searchResponse);
//            // 3 分析检索结果，封装整所需格式
//            result = buildSearchResult(searchResponse,param);
//        } catch (Exception e) {
//            log.error("elasticsearch 检索出现异常! 原因:{}",e.getMessage());
//            throw new RuntimeException(e);
//        }
//
//        return result;
//    }
//
//    /**
//     * 构建查询条件
//     * search -> 1 动态构建DSL语句，准备检索请求
//     * #模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存），排序，分页，高亮，聚合分析
//     * https://huaweicloud.csdn.net/637ef6bbdf016f70ae4cac71.html?spm=1001.2101.3001.6650.4&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7Eactivity-4-125076259-blog-126851584.235%5Ev38%5Epc_relevant_sort_base1&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7Eactivity-4-125076259-blog-126851584.235%5Ev38%5Epc_relevant_sort_base1&utm_relevant_index=9
//     */
//    private SearchRequest.Builder buildSearchRequrest(ElasticSearchParam param) {
//        SearchRequest.Builder searchBuilder = new SearchRequest.Builder();
//        searchBuilder.index(ElasticSearchConstant.PRODUCT_ES_INDEX);
//        /**
//         * 查询 模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存）
//         */
//        //1.1、bool - must-模糊匹配，
//        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
//        if (!StringUtils.isEmpty(param.getKeyword())) {
//            boolQuery.must(MatchQuery.of(m->m.field("skuTitle").query(param.getKeyword()))._toQuery());
//        }
//        //1.2、bool - filter - term 按照三级分类id查询
//        //构建filter
//        if (param.getCatalog3Id() != null) {
//            boolQuery.filter(TermQuery.of(t->t.field("catalogId").value(param.getCatalog3Id()))._toQuery());
//        }
//        //1.2-1、bool - filter - terms - 按照品牌id查询
//        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
//            List<Long> brandIds = param.getBrandId();
//            List<FieldValue> values = new ArrayList<>();
//            for (int i = 0; i < brandIds.size(); i++) {
//                values.add(FieldValue.of(brandIds.get(i)));
//            }
//            boolQuery.filter( new TermsQuery.Builder().field("brandId").terms(t -> t.value(values)).build()._toQuery());
//        }
//        //1.2-2、bool - filter - nested - terms 按照所有指定的属性进行查询
//        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
//            for (String attrStr : param.getAttrs()) {
//                NestedQuery.Builder nested = new NestedQuery.Builder();
//                nested.path("attrs");
//                String[] s = attrStr.split("_");
//                String attrId = s[0];
//                String[] attrValues = s[1].split(":");
//
//                List<FieldValue> values = new ArrayList<>();
//                for (int i = 0; i < attrValues.length; i++) {
//                    values.add(FieldValue.of(attrValues[i]));
//                }
//                TermQuery termQuery = new TermQuery.Builder().field("attrs.attrId").value(attrId).build();
//                TermsQuery termsQuery = new TermsQuery.Builder().field("attrs.attrValue").terms(ts -> ts.value(values)).build();
//                nested.query(q->q.bool(m->m.must(termQuery._toQuery()).must(termsQuery._toQuery())));
//                boolQuery.filter(f->f.nested(nested.build()));
//            }
//        }
//        //1.2-3、bool - filter - term - 按照库存是否有进行查询
//        if(param.getHasStock() != null){
//            boolQuery.filter(f->f.term(t->t.field("hasStock").value(param.getHasStock()==1)));
//        }
//        //1.2-4、bool - filter - range按照价格区间
//        if (!StringUtils.isEmpty(param.getSkuPrice())){
//            RangeQuery.Builder rangeQuery = new RangeQuery.Builder();
//            String[] s = param.getSkuPrice().split("_");
//            if (s.length == 2) {
//                rangeQuery.field("skuPrice").gte(JsonData.fromJson(s[0])).lte(JsonData.fromJson(s[1]));
//            } else if (s.length == 1) {
//                if (param.getSkuPrice().startsWith("_")) {
//                    rangeQuery.field("skuPrice").lte(JsonData.fromJson(s[0]));
//                }
//                if (param.getSkuPrice().endsWith("_")) {
//                    rangeQuery.field("skuPrice").gte(JsonData.fromJson(s[0]));
//                }
//            }
//            boolQuery.filter(f->f.range(rangeQuery.build()));
//        }
//
//        /**
//         * 排序，分页，高亮
//         */
//        if (!StringUtils.isEmpty(param.getSort())) {
//            SortOptions.Builder sort = new SortOptions.Builder();
//            String s= param.getSort();
//            //sort=hotScore_asc/desc
//            String[] str = s.split("_");
//            sort.field(f->f.field(str[0]).order("asc".equalsIgnoreCase(str[1])? SortOrder.Asc:SortOrder.Desc));
////            多个排序
////            SortOptions build = sort.build();
////            List<SortOptions> builders = Arrays.asList(build);
//            searchBuilder.sort(sort.build());
//        }
//        //from = (pageNum-1)*size 分页
//        searchBuilder.from((param.getPageNum() - 1) * ElasticSearchConstant.PRODUCT_ES_PASIZE);
//        searchBuilder.size(ElasticSearchConstant.PRODUCT_ES_PASIZE);
//        // 高亮
//        if (!StringUtils.isEmpty(param.getKeyword())) {
//            Highlight highlight = Highlight.of(h -> h.fields("skuTitle", highlightFieldBuilder -> highlightFieldBuilder).preTags("<b style='color:red'>").postTags("</b>"));
//            searchBuilder.highlight(highlight);
//        }
//
//        Query.Builder queryBuilder = new Query.Builder();
//        queryBuilder.bool(boolQuery.build());
//        searchBuilder.query(queryBuilder.build());
//        /**
//         * 聚合分析
//         */
//
//        searchBuilder
//                .aggregations("brand_agg",brand->brand.terms(t->t.field("brandId").size(50))
//                        .aggregations("brand_name_agg", brandName->brandName.terms(t->t.field("brandName").size(100))))
//                .aggregations("catelog_agg", catelog->catelog.terms(t->t.field("catalogId").size(50))
//                        .aggregations("catalog_name_agg", catelogName->catelogName.terms(t->t.field("catalogName").size(100))))
//                .aggregations("attr_agg", attr->attr.nested(n->n.path("attrs"))
//                        .aggregations("attr_id_agg",attrId->attrId.terms(t->t.field("attrs.attrId").size(50))
//                                .aggregations("attr_value_agg", value->value.terms(t->t.field("attrs.attrValue").size(100)))));
//
//        return searchBuilder;
//    }
//
//    /**
//     * 构建结果数据
//     * search -> 3 分析检索的结果,将结果进行封装整整理成为所需格式,最终返回数据
//     */
//    private ElasticSearchResult buildSearchResult(SearchResponse<ElasticSearchResult> searchResponse, ElasticSearchParam param) {
//        ElasticSearchResult result = new ElasticSearchResult();
//        //1所有商品的信息
//        List<Hit<ElasticSearchResult>> hits = searchResponse.hits().hits();
//        List<SkuElasticModel> skuElasticModelList = new ArrayList<>();
//        if (hits != null && hits.size() > 0) {
//            for (int i = 0; i < hits.size(); i++) {
//                //TODO 类型一直无法转换
//                ElasticSearchResult source = hits.get(i).source();
//                String str = hits.get(i).source().toString();
//                SkuElasticModel model = new SkuElasticModel();
//
//
//                skuElasticModelList.add(model);
//            }
//        }
//        result.setProducts(skuElasticModelList);
//        //2商品涉及的属性信息
//        List<ElasticSearchResult.AttrVo> attrVos = new ArrayList<>();
//        LongTermsAggregate attrAttrIdAgg = searchResponse.aggregations().get("attr_agg").nested().aggregations().get("attr_id_agg").lterms();
//        List<LongTermsBucket> attrAttrIdBuckets = attrAttrIdAgg.buckets().array();
//        for (LongTermsBucket bucket:attrAttrIdBuckets) {
//            ElasticSearchResult.AttrVo attrVo = new ElasticSearchResult.AttrVo();
//            attrVo.setAttrId(bucket.key());
//            List<String> values = bucket.aggregations().values().stream().map(item -> {
//                return item._get().toString();
//            }).toList();
//            attrVo.setAttrValue(values);
//            attrVos.add(attrVo);
//        }
//
//        //3商品涉及的品牌信息
//        List<ElasticSearchResult.BrandVo> brandVos = new ArrayList<>();
//        LongTermsAggregate brandAgg= searchResponse.aggregations().get("brand_agg").lterms();
//        List<LongTermsBucket> brandBuckets = brandAgg.buckets().array();
//        for (LongTermsBucket bucket:brandBuckets) {
//            ElasticSearchResult.BrandVo brandVo = new ElasticSearchResult.BrandVo();
//            brandVo.setBrandId(bucket.key());
//            String brandNameAgg = bucket.aggregations().get("brand_name_agg").sterms().buckets().array().get(0).toString();
//            brandVo.setBrandName(brandNameAgg);
//            brandVos.add(brandVo);
//        }
//        //4商品涉及的分类信息
//        List<ElasticSearchResult.CatalogVo> catalogVos = new ArrayList<>();
//        LongTermsAggregate catelogAgg = searchResponse.aggregations().get("catelog_agg").lterms();
//        List<LongTermsBucket> catelogBuckets = catelogAgg.buckets().array();
//        for (LongTermsBucket bucket:catelogBuckets) {
//            ElasticSearchResult.CatalogVo catalogVo = new ElasticSearchResult.CatalogVo();
//            catalogVo.setCatalogId(bucket.key());
//            String catalogNameAgg = bucket.aggregations().get("catalog_name_agg").sterms().buckets().array().get(0).toString();
//            catalogVo.setCatalogName(catalogNameAgg);
//            catalogVos.add(catalogVo);
//        }
//        //==================以上从聚合信息中获取==================
//        //5分页信息
//        result.setPageNum(param.getPageNum());
//        long total = searchResponse.hits().total().value();
//        result.setTotal(total);
//        int totalPages = (int) (total % ElasticSearchConstant.PRODUCT_ES_PASIZE == 0 ? total / ElasticSearchConstant.PRODUCT_ES_PASIZE : (total / ElasticSearchConstant.PRODUCT_ES_PASIZE + 1));
//        result.setTotalPages(totalPages);
//        List<Integer> pageNavs = new ArrayList<>();
//        for (int i=1;i<=totalPages;i++){
//            pageNavs.add(i);
//        }
//        result.setPageNavs(pageNavs);
//
//        return null;
//    }


    @Autowired
    RestHighLevelClient client;
    @Autowired
    ProductFeignService productFeignService;
    @Override
    public SearchResult search(SearchParam param) {
        //1、动态构建出查询需要的DSL语句
        SearchResult result = null;
        //1、准备检索请求
        SearchRequest searchRequest = buildSearchRequrest(param);
        try {
            //2、执行检索请求
            SearchResponse response = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
            //3、分析响应数据封装成我们需要的格式
            result = buildSearchResult(response, param);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 准备检索请求
     * #模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存），排序，分页，高亮，聚合分析
     */
    private SearchRequest buildSearchRequrest(SearchParam param) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();//构建DSL语句的
        /*
         * 查询：过滤（按照属性，分类，品牌，价格区间，库存）
         */
        //1、构建bool - query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //1.1、must-模糊匹配，.keyword
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        //1.2、bool - filter - 按照三级分类id查询
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        //1.2、bool - filter - 按照品牌id查询
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        //1.2、bool - filter - 按照所有指定的属性进行查询
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            for (String attrStr : param.getAttrs()) {
                //attrs=1_5寸:8寸&attrs=2_16G:8G
                BoolQueryBuilder nestedboolQuery = QueryBuilders.boolQuery();
                //attr = 1_5寸:8寸
                String[] s = attrStr.split("_");
                String attrId = s[0]; //检索的属性id
                String[] attrValues = s[1].split(":"); //这个属性的检索用的值
                nestedboolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedboolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                //每一个必须都得生成一个nested查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedboolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }
        //1.2、bool - filter - 按照库存是否有进行查询
        if(param.getHasStock() != null){
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }
        //1.2、bool - filter - 按照价格区间
        if (!StringUtils.isEmpty(param.getSkuPrice())) {

            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
                //区间
                rangeQuery.gte(s[0]).lte(s[1]);
            } else if (s.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    rangeQuery.lte(s[0]);
                }
                if (param.getSkuPrice().endsWith("_")) {
                    rangeQuery.gte(s[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }
        //把以前的所有条件都拿来进行封装
        sourceBuilder.query(boolQuery);
        /**
         * 排序，分页，高亮，
         */
        //2.1、排序
        if (!StringUtils.isEmpty(param.getSort())) {
            String sort = param.getSort();
            //sort=hotScore_asc/desc
            String[] s = sort.split("_");
            SortOrder order = "asc".equalsIgnoreCase(s[1]) ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(s[0], order);
        }
        //2.2、分页  pageSize:5
        //  pageNum:1  from:0  size:5  [0,1,2,3,4]
        // pageNum:2  from:5   size:5
        //from = (pageNum-1)*size
        sourceBuilder.from((param.getPageNum() - 1) * ElasticSearchConstant.PRODUCT_ES_PASIZE);
        sourceBuilder.size(ElasticSearchConstant.PRODUCT_ES_PASIZE);
        //2.3、高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style='color:red'>");
            builder.postTags("</b>");
            sourceBuilder.highlighter(builder);
        }
        /*
         * 聚合分析
         */
        //1、品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        //品牌聚合的子聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
//        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        //TODO 1、聚合brand
        sourceBuilder.aggregation(brand_agg);
        //2、分类聚合 catalog_agg
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        //TODO 2、聚合catalog
        sourceBuilder.aggregation(catalog_agg);
        //3、属性聚合 attr_agg
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        //聚合出当前所有的attrId
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        //聚合分析出当前attr_id对应的名字
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        //聚合分析出当前attr_id对应的所有可能的属性值attrValue
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attr_agg.subAggregation(attr_id_agg);
        //TODO 3、聚合attr
        sourceBuilder.aggregation(attr_agg);
        String s = sourceBuilder.toString();
        System.out.println("构建的DSL = " + s);
        SearchRequest searchRequest = new SearchRequest(new String[]{ElasticSearchConstant.PRODUCT_ES_INDEX}, sourceBuilder);
        return searchRequest;
    }

    /**
     * 构建结果数据
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        SearchResult result = new SearchResult();
        //1、返回的所有查询到的商品
        SearchHits hits = response.getHits();
        List<SkuElasticModel> esModels = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuElasticModel esModel = JSON.parseObject(sourceAsString, SkuElasticModel.class);
                if(!StringUtils.isEmpty(param.getKeyword())){
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(string);
                }
                esModels.add(esModel);
            }
        }
        result.setProducts(esModels);
        //2、当前所有商品涉及到的所有属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            //1、得到属性的id
            long attrId = bucket.getKeyAsNumber().longValue();
            //2、得到属性的名字
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            //3、得到属性的所有值
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
                String keyAsString = ((Terms.Bucket) item).getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());
            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);
        //3、当前所有商品涉及到的所有品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            //1、得到品牌的id
            long brandId = bucket.getKeyAsNumber().longValue();
            //2、得到品牌的名
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            //3、得到品牌的图片
//            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brandName);
//            brandVo.setBrandImg(brandImg);
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);
        //4、当前所有商品涉及到的所有分类信息
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //得到分类id
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));
            //得到分类名
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalog_name = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalog_name);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);
        //========以上从聚合信息中获取======
        //5、分页信息-页码
        result.setPageNum(param.getPageNum());
        //5、分页信息-总记录树
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        //5、分页信息-总页码-计算  11/2 = 5 .. 1
        int totalPages = (int) (total % ElasticSearchConstant.PRODUCT_ES_PASIZE == 0 ? total / ElasticSearchConstant.PRODUCT_ES_PASIZE : (total / ElasticSearchConstant.PRODUCT_ES_PASIZE + 1));
        result.setTotalPages(totalPages);
        List<Integer> pageNavs = new ArrayList<>();
        for (int i=1;i<=totalPages;i++){
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);
        //6、构建面包屑导航功能
        if(param.getAttrs()!=null && param.getAttrs().size()>0){
            List<SearchResult.NavVo> collect = param.getAttrs().stream().map(attr -> {
                //1、分析每个attrs传过来的查询参数值。
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                //attrs=2_5存:6寸
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                result.getAttrIds().add(Long.parseLong(s[0]));
                if(r.getCode() == 0){
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName( data.getAttrName());
                }else{
                    navVo.setNavName(s[0]);
                }
                //2、取消了这个面包屑以后，我们要跳转到那个地方.将请求地址的url里面的当前置空
                //拿到所有的查询条件，去掉当前。
                //attrs=  15_海思（Hisilicon）
                String replace = replaceQueryString(param, attr,"attrs");
                navVo.setLink("http://search.mall.com/list.html?"+replace);
                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(collect);
        }
        //品牌，分类
        if(param.getBrandId()!=null && param.getBrandId().size()>0){
            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");
            //TODO 远程查询所有品牌
            R r = productFeignService.brandsInfo(param.getBrandId());
            if(r.getCode() == 0){
                List<BrandVo> brand = r.getData("brand", new TypeReference<List<BrandVo>>() {
                });
                StringBuffer buffer = new StringBuffer();
                String replace = "";
                for (BrandVo brandVo : brand) {
                    buffer.append(brandVo.getBrandName()+";");
                    replace = replaceQueryString(param, brandVo.getBrandId()+"","brandId");
                }
                navVo.setNavValue(buffer.toString());
                navVo.setLink("http://search.mall.com/list.html?"+replace);
            }
            navs.add(navVo);
        }
        //TODO 分类：不需要导航取消
        return result;
    }

    private String replaceQueryString(SearchParam param, String value, String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            encode = encode.replace("+","%20");//浏览器对空格编码和java不一样
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return param.get_queryString().replace("&"+key+"=" + encode, "");
    }

}
