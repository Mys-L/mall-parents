package com.mall.common.constant;

/**
 * 常量
 */
public class Constant {
    /*----------------------------------分页常量-----------------------------------------------------------*/
    /**
     * 当前页码
     */
    public static final String PAGE = "page";
    /**
     * 每页显示记录数
     */
    public static final String LIMIT = "limit";
    /**
     * 排序字段
     */
    public static final String ORDER_FIELD = "sidx";
    /**
     * 排序方式
     */
    public static final String ORDER = "order";
    /**
     * 升序
     */
    public static final String ASC = "asc";
    /*-------------------------------------------------------------------------------------------------*/

    /*------------------mall-product 商品系统有关的常量------------------------------------------------------------------------*/
    public enum AttrEnum{
        ATTR_TYPE_BASE(1,"基本属性"),
        ATTR_TYPE_SALE(0,"销售属性");
        private int code;
        private String msg;
        AttrEnum(int code,String msg){
            this.code=code;
            this.msg= msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
    /*-------------------------------------------------------------------------------------------------*/

    /*------------------mall-ware库存系统有关的常量------------------------------------------------------------------------*/
    /** 采购单状态枚举 */
    public enum PurchaseStatusEnum{
        CREATED(0,"新建"),ASSIGNED(1,"已分配"),
        RECEIVE(2,"已领取"),FINISH(3,"已完成"),
        HASERROR(4,"有异常");
        private int code;
        private String msg;
        PurchaseStatusEnum(int code,String msg){
            this.code = code;
            this.msg = msg;
        }
        public int getCode() {
            return code;
        }
        public String getMsg() {
            return msg;
        }
    }

    /** 采购需求枚举 */
    public enum PurchaseDetailStatusEnum{
        CREATED(0,"新建"),ASSIGNED(1,"已分配"),
        BUYING(2,"正在采购"),FINISH(3,"已完成"),
        HASERROR(4,"采购失败");
        private int code;
        private String msg;
        PurchaseDetailStatusEnum(int code,String msg){
            this.code = code;
            this.msg = msg;
        }
        public int getCode() {
            return code;
        }
        public String getMsg() {
            return msg;
        }
    }
    /** 商品 状态 */
    public enum StatusEnum {
        SPU_NEW(0, "新建"), SPU_UP(1, "上架"), SPU_DOWN(2, "下架");
        private int code;
        private String msg;

        StatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
        public int getCode() {
            return code;
        }
        public String getMsg() {
            return msg;
        }
    }
    /*-------------------------------------------------------------------------------------------------*/

    /*------------------mall-auth-server 授权系统有关的常量------------------------------------------------------------------------*/
    public static final String SMS_CODE_CACHE_PREFIX = "sms:code:";
    public static final String LOGIN_USER = "loginUser";
    public static final String NOT_LOGIN = "请先进行登录";
    public static final String SESSION = "GULISESSION";
    /*-------------------------------------------------------------------------------------------------*/

    /*------------------mall-cart购物车服务有关的常量------------------------------------------------------------------------*/
    public static final String TEMP_USER_COOKIE_NAME = "user-key";
    public static final int TEMP_USER_COOKIE_TIMEOUT= 60*60*24*30;
    public final static String CART_PREFIX = "mall:cart:";
}
