package com.crm.framework.config;

import com.crm.common.utils.encrypt.ConfigEncryptUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SystemEnvironmentPropertySource;

/**
 * 配置加密环境后处理器：在 Spring 环境就绪后包装所有可枚举 PropertySource，
 * 读取属性时对 ENC(...) 密文自动解密。
 *
 * 必须保留 EnumerablePropertySource 的枚举能力，否则 @ConfigurationProperties 绑定 Map
 * （如 dynamic-datasource 的 datasource 集合）时无法发现配置键。
 * 环境变量来源（SystemEnvironmentPropertySource）不包装，避免破坏其宽松绑定特性；
 * 主密钥本身通过 -Dconfig.encrypt.key / CONFIG_ENCRYPT_KEY 注入，不经过配置文件。
 */
public class EncryptableEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        boolean hasEncrypted = false;
        for (PropertySource<?> source : environment.getPropertySources()) {
            if (isWrapable(source)) {
                for (String name : ((EnumerablePropertySource<?>) source).getPropertyNames()) {
                    Object value = source.getProperty(name);
                    if (value instanceof String && ConfigEncryptUtils.isEncrypted((String) value)) {
                        hasEncrypted = true;
                        break;
                    }
                }
            }
            if (hasEncrypted) {
                break;
            }
        }
        // 存在密文时立即校验主密钥，缺失则启动失败，避免运行期才暴露
        if (hasEncrypted) {
            ConfigEncryptUtils.resolveMasterKey();
        }
        for (PropertySource<?> source : environment.getPropertySources()) {
            if (isWrapable(source)) {
                environment.getPropertySources().replace(source.getName(),
                    new DecryptablePropertySource<>((EnumerablePropertySource<?>) source));
            }
        }
    }

    private boolean isWrapable(PropertySource<?> source) {
        return source instanceof EnumerablePropertySource && !(source instanceof SystemEnvironmentPropertySource);
    }

    /**
     * 读取时惰性解密的属性源包装，保留枚举能力
     */
    private static class DecryptablePropertySource<T> extends EnumerablePropertySource<T> {

        private final EnumerablePropertySource<T> delegate;

        DecryptablePropertySource(EnumerablePropertySource<T> delegate) {
            super(delegate.getName(), delegate.getSource());
            this.delegate = delegate;
        }

        @Override
        public Object getProperty(String name) {
            Object value = delegate.getProperty(name);
            if (value instanceof String) {
                return ConfigEncryptUtils.tryDecrypt((String) value);
            }
            return value;
        }

        @Override
        public String[] getPropertyNames() {
            return delegate.getPropertyNames();
        }
    }

    @Override
    public int getOrder() {
        // 必须晚于 ConfigDataEnvironmentPostProcessor（HIGHEST_PRECEDENCE+10）运行，
        // 否则 yml 配置尚未加载，包装不到任何配置来源，ENC 值不会被解密
        return Ordered.LOWEST_PRECEDENCE;
    }
}
