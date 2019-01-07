package cn.itcast.core.service;

import java.util.Map;

/*
* 根据数据和商品id创建静态页面
 * @param rootMap   商品的各种详细数据
 * @param goodsId   商品id
 */
public interface CmsService {
    public void createStaticPage(Map<String, Object> rootMap, Long goodsId) throws Exception;

    public Map<String, Object> findGoodDate(Long goodsId);
}
