package com.echobaba.milky.common.tool.log;
/*
public class MDCTagEnum extends MDCTag{

    static public MDCTag itemID = MDCTag.of("itemId", "商品ID");

}
 */
public class MDCTag {

    static public MDCTag cost = new MDCTag("cost", "耗时");
    static public MDCTag success = new MDCTag("success", "成功失败");
    static public MDCTag traceId = new MDCTag("traceId", "链路ID");
    static public MDCTag test = new MDCTag("test", "压测标");
    static public MDCTag logTag = new MDCTag("logTag", "日志标");

    private String name;

    private String desc;

    protected MDCTag() {
    }

    private MDCTag(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String name() {
        return name;
    }
    public String desc() {
        return desc;
    }

    static protected MDCTag of(String name, String desc) {
        return new MDCTag(name, desc);
    }
}
