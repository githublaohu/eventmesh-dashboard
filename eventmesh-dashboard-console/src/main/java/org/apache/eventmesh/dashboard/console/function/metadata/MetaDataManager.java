package org.apache.eventmesh.dashboard.console.function.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MetaDataManager {


    private final ScheduledThreadPoolExecutor scheduledExecutorService = new ScheduledThreadPoolExecutor(2);

    /**
     *
     */
    private final Map<Class<?>, MetaDataServiceWrapper> metaDataServiceWrapperMap = new ConcurrentHashMap<>();

    private final Map<Class<?>, List<Object>> cacheData = new ConcurrentHashMap<>();

    public void init() {
        scheduledExecutorService.scheduleAtFixedRate(this::run, 1000, 1000 * 10, TimeUnit.MINUTES);
    }


    /**
     * 注册中心 同步 dashboard
     * dashboard 同步  node
     * 单项同步，还是双向同步
     * 1. topic
     * 2. acl
     * 3. user
     * service --> dashboard 有不做识别的
     * 1. group
     * 2. client
     * 3. connection
     * 4. topic
     * 5. acl
     * 6. user
     * dashboard --> service
     * 1. topic
     * 2. acl
     * 3. user
     * 4.
     *
     * @param clazz
     * @param syncsService
     */
    public void addMetaDataService(Class<?> clazz, MetaDataServiceWrapper syncsService) {
        metaDataServiceWrapperMap.put(clazz, syncsService);

    }

    public void run() {
        metaDataServiceWrapperMap.forEach(this::handler);
    }

    public void handler(Class<?> clazz, MetaDataServiceWrapper metaDataServiceWrapper) {
        this.handler(clazz, metaDataServiceWrapper.getDbToService());
        this.handler(clazz, metaDataServiceWrapper.getServiceToDb());
    }

    public void handler(Class<?> clazz, MetaDataServiceWrapper.SingleMetaDataServiceWrapper metaDataServiceWrapper) {
        try {
            // 得到数据 现在的数据
            List<Object> objectList = metaDataServiceWrapper.syncsService.syncsData();
            if (objectList.isEmpty()) {
                return;
            }
            // 得到 之前的数据
            List<Object> cacheDataList = cacheData.get(clazz);
            if (Objects.isNull(cacheDataList)) {
                cacheDataList = new ArrayList<>();
                cacheData.put(clazz, cacheDataList);
            }

            // 计算 新增，修改，删除的数据
            try {

                // 新增
                metaDataServiceWrapper.metaService.addMeta(null);

            } catch (Exception e) {

            }
            try {
                // 修改
                metaDataServiceWrapper.metaService.updateMeta(null);
            } catch (Exception e) {

            }
            try {
                // 删除
                metaDataServiceWrapper.metaService.deleteMeta(null);
            } catch (Exception e) {

            }


        } catch (Throwable e) {

        }
    }


}
