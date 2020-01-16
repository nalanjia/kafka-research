/*
 * Copyright 2016-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.kafka.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * The {@link ConsumerFactory} implementation to produce new {@link Consumer} instances
 * for provided {@link Map} {@code configs} and optional {@link Deserializer}s on each {@link #createConsumer()}
 * invocation.
 * <p>
 * If you are using {@link Deserializer}s that have no-arg constructors and require no setup, then simplest to
 * specify {@link Deserializer} classes against {@link ConsumerConfig#KEY_DESERIALIZER_CLASS_CONFIG} and
 * {@link ConsumerConfig#VALUE_DESERIALIZER_CLASS_CONFIG} keys in the {@code configs} passed to the
 * {@link DefaultKafkaConsumerFactory} constructor.
 * <p>
 * If that is not possible, but you are using {@link Deserializer}s that may be shared between all {@link Consumer}
 * instances (and specifically that their close() method is a no-op), then you can pass in {@link Deserializer}
 * instances for one or both of the key and value deserializers.
 * <p>
 * If neither of the above is true then you may provide a {@link Supplier} for one or both {@link Deserializer}s
 * which will be used to obtain {@link Deserializer}(s) each time a {@link Consumer} is created by the factory.
 *
 * @param <K> the key type.
 * @param <V> the value type.
 *
 * @author Gary Russell
 * @author Murali Reddy
 * @author Artem Bilan
 * @author Chris Gilbert
 */
public class DefaultKafkaConsumerFactory<K, V> implements ConsumerFactory<K, V> {

	private final Map<String, Object> configs;

	private Supplier<Deserializer<K>> keyDeserializerSupplier;

	private Supplier<Deserializer<V>> valueDeserializerSupplier;

	/**
	 * Construct a factory with the provided configuration.
	 * @param configs the configuration.
	 */
	public DefaultKafkaConsumerFactory(Map<String, Object> configs) {
		this(configs, () -> null, () -> null);
	}

	/**
	 * Construct a factory with the provided configuration and deserializers.
	 * @param configs the configuration.
	 * @param keyDeserializer the key {@link Deserializer}.
	 * @param valueDeserializer the value {@link Deserializer}.
	 */
	public DefaultKafkaConsumerFactory(Map<String, Object> configs,
			@Nullable Deserializer<K> keyDeserializer,
			@Nullable Deserializer<V> valueDeserializer) {

		this(configs, () -> keyDeserializer, () -> valueDeserializer);
	}

	/**
	 * Construct a factory with the provided configuration and deserializer suppliers.
	 * @param configs the configuration.
	 * @param keyDeserializerSupplier   the key {@link Deserializer} supplier function.
	 * @param valueDeserializerSupplier the value {@link Deserializer} supplier function.
	 * @since 2.3
	 */
	public DefaultKafkaConsumerFactory(Map<String, Object> configs,
			@Nullable Supplier<Deserializer<K>> keyDeserializerSupplier,
			@Nullable Supplier<Deserializer<V>> valueDeserializerSupplier) {

		this.configs = new HashMap<>(configs);
		this.keyDeserializerSupplier = keyDeserializerSupplier == null ? () -> null : keyDeserializerSupplier;
		this.valueDeserializerSupplier = valueDeserializerSupplier == null ? () -> null : valueDeserializerSupplier;
	}

	public void setKeyDeserializer(@Nullable Deserializer<K> keyDeserializer) {
		this.keyDeserializerSupplier = () -> keyDeserializer;
	}

	public void setValueDeserializer(@Nullable Deserializer<V> valueDeserializer) {
		this.valueDeserializerSupplier = () -> valueDeserializer;
	}

	@Override
	public Map<String, Object> getConfigurationProperties() {
		return Collections.unmodifiableMap(this.configs);
	}

	@Override
	public Deserializer<K> getKeyDeserializer() {
		return this.keyDeserializerSupplier.get();
	}

	@Override
	public Deserializer<V> getValueDeserializer() {
		return this.valueDeserializerSupplier.get();
	}

	@Override
	public Consumer<K, V> createConsumer(@Nullable String groupId, @Nullable String clientIdPrefix,
			@Nullable String clientIdSuffix) {

		return createKafkaConsumer(groupId, clientIdPrefix, clientIdSuffix, null);
	}

	@Override
	public Consumer<K, V> createConsumer(@Nullable String groupId, @Nullable String clientIdPrefix,
			@Nullable final String clientIdSuffixArg, @Nullable Properties properties) {

		return createKafkaConsumer(groupId, clientIdPrefix, clientIdSuffixArg, properties);
	}

	@Deprecated
	protected KafkaConsumer<K, V> createKafkaConsumer(@Nullable String groupId, @Nullable String clientIdPrefix,
			@Nullable String clientIdSuffixArg) {

		return createKafkaConsumer(groupId, clientIdPrefix, clientIdSuffixArg, null);
	}

	protected KafkaConsumer<K, V> createKafkaConsumer(@Nullable String groupId, @Nullable String clientIdPrefix,
			@Nullable String clientIdSuffixArg, @Nullable Properties properties) {

		boolean overrideClientIdPrefix = StringUtils.hasText(clientIdPrefix);
		String clientIdSuffix = clientIdSuffixArg;
		if (clientIdSuffix == null) {
			clientIdSuffix = "";
		}
		boolean shouldModifyClientId = (this.configs.containsKey(ConsumerConfig.CLIENT_ID_CONFIG)
				&& StringUtils.hasText(clientIdSuffix)) || overrideClientIdPrefix;
		if (groupId == null
				&& (properties == null || properties.stringPropertyNames().size() == 0)
				&& !shouldModifyClientId) {
			return createKafkaConsumer(this.configs);
		}
		else {
			return createConsumerWithAdjustedProperties(groupId, clientIdPrefix, properties, overrideClientIdPrefix,
					clientIdSuffix, shouldModifyClientId);
		}
	}

	private KafkaConsumer<K, V> createConsumerWithAdjustedProperties(String groupId, String clientIdPrefix,
			Properties properties, boolean overrideClientIdPrefix, String clientIdSuffix,
			boolean shouldModifyClientId) {

		Map<String, Object> modifiedConfigs = new HashMap<>(this.configs);
		if (groupId != null) {
			modifiedConfigs.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		}
		if (shouldModifyClientId) {
			modifiedConfigs.put(ConsumerConfig.CLIENT_ID_CONFIG,
					(overrideClientIdPrefix ? clientIdPrefix
							: modifiedConfigs.get(ConsumerConfig.CLIENT_ID_CONFIG)) + clientIdSuffix);
		}
		if (properties != null) {
			properties.stringPropertyNames()
					.stream()
					.filter(name -> !name.equals(ConsumerConfig.CLIENT_ID_CONFIG)
							&& !name.equals(ConsumerConfig.GROUP_ID_CONFIG))
					.forEach(name -> modifiedConfigs.put(name, properties.getProperty(name)));
		}
		return createKafkaConsumer(modifiedConfigs);
	}

	protected KafkaConsumer<K, V> createKafkaConsumer(Map<String, Object> configProps) {
		return new KafkaConsumer<>(configProps, this.keyDeserializerSupplier.get(),
				this.valueDeserializerSupplier.get());
	}

	@Override
	public boolean isAutoCommit() {
		Object auto = this.configs.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG);
		return auto instanceof Boolean ? (Boolean) auto
				: auto instanceof String ? Boolean.valueOf((String) auto) : true;
	}

}
