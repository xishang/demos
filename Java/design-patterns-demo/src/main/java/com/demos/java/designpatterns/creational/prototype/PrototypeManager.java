package com.demos.java.designpatterns.creational.prototype;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/25
 * <p>
 * 原型管理器: 登记原型对象
 */
public class PrototypeManager {

    private static PrototypeManager INSTANCE = new PrototypeManager();

    private Map<String, Document> map;

    private PrototypeManager() {
        map = new HashMap<>();
        map.put("analysis", new AnalysisReport());
        map.put("requirement", new RequirementDocument());
    }

    public static PrototypeManager getInstance() {
        return INSTANCE;
    }

    public Document clone(String type) {
        return map.get(type).clone();
    }

    public Document deepClone(String type) {
        return map.get(type).deepClone();
    }

}
