package com.tts.component.config;

import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicWatchedConfiguration;
import com.netflix.config.source.ZooKeeperConfigurationSource;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhaoqi on 2016/8/16 0016.
 */
public class ConfigWriter implements InitializingBean,DisposableBean{

    private static final Logger logger = LoggerFactory.getLogger(ConfigWriter.class);

    private String CONFIG_PATH = "tts/config";

    private String serviceName;

    private String CONFIG_FILE = "global.properties";

    private CuratorFramework client;

    private AtomicBoolean inited = new AtomicBoolean(false);


    @Override
    public void afterPropertiesSet() throws Exception {
        if (StringUtils.isNotBlank(serviceName)) {
            CONFIG_PATH  += "/"+serviceName;
        }
        if (client.checkExists().forPath(CONFIG_PATH) ==null ){
            client.create().creatingParentContainersIfNeeded().forPath(CONFIG_PATH);
        }
        // 已经初始化后直接返回
        if(!inited.compareAndSet(false, true)){
            return;
        }
        // 配置archaius监听
        this.registerToArchaius();
        // 读取初始配置，注册到zk
        this.registerToZookeeper();
    }

    /**
     * 读取配置，并且注册到zk中
     */
    private void registerToZookeeper() {
        try {
            Resource resource = new ClassPathResource(CONFIG_FILE);
            if (! resource.exists()) {
                logger.error("can't find resource {} in path",CONFIG_FILE);
                return;
            }
            Properties props = PropertiesLoaderUtils.loadProperties(resource);

            for (String propName : props.stringPropertyNames()) {
                final String path = CONFIG_PATH+"/"+propName;
                if (client.checkExists().forPath(path) == null) {
                    String value = String.valueOf(props.getProperty(propName));
                    this.client.create().forPath(path, value.getBytes("UTF-8"));

                    logger.debug("register configuration: {} --> {} success", propName, value);
                }
            }
            logger.info("register all configurations success");
        } catch (Exception e) {
            logger.error("register all configurations to zookeeper failed with exception.", e);
        }
    }

    /**
     * <a href = "http://github.com/Netflix/archaius/wiki/ZooKeeper-Dynamic-Configuration">
     */
    private void registerToArchaius() {
        ZooKeeperConfigurationSource zkConfigSource = new ZooKeeperConfigurationSource(client,CONFIG_PATH);
        try{
            zkConfigSource.start();
        } catch (Exception e) {
            logger.error("start zkConfigSource error !!!!");
        }

        DynamicWatchedConfiguration zkDynamicConfig = new DynamicWatchedConfiguration(zkConfigSource);

        ConfigurationManager.install(zkDynamicConfig);
    }

    public void setCONFIG_FILE(String CONFIG_FILE) {
        if (StringUtils.isNotBlank(CONFIG_FILE)) {
            this.CONFIG_FILE = CONFIG_FILE;
        }
    }

    public void setClient(CuratorFramework client) {
        this.client = client;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public void destroy() throws Exception {
        client.close();
    }
}
