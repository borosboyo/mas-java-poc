package com.optahaul.mas_java_poc.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for message queue setup.
 */
@Configuration
public class RabbitMQConfig {

	public static final String QUEUE_NAME = "sample.queue";

	public static final String EXCHANGE_NAME = "sample.exchange";

	public static final String ROUTING_KEY = "sample.routing.key";

	/**
	 * Defines the queue for receiving messages.
	 *
	 * @return Queue instance
	 */
	@Bean
	public Queue queue() {
		return new Queue(QUEUE_NAME, true); // durable queue
	}

	/**
	 * Defines the topic exchange for routing messages.
	 *
	 * @return TopicExchange instance
	 */
	@Bean
	public TopicExchange exchange() {
		return new TopicExchange(EXCHANGE_NAME);
	}

	/**
	 * Binds the queue to the exchange with a routing key.
	 *
	 * @param queue
	 *            the queue to bind
	 * @param exchange
	 *            the exchange to bind to
	 * @return Binding instance
	 */
	@Bean
	public Binding binding(Queue queue, TopicExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
	}

	/**
	 * Configures JSON message converter for RabbitMQ messages.
	 *
	 * @return MessageConverter instance
	 */
	@Bean
	public MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	/**
	 * Configures RabbitTemplate with JSON message converter.
	 *
	 * @param connectionFactory
	 *            the RabbitMQ connection factory
	 * @return RabbitTemplate instance
	 */
	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(messageConverter());
		return rabbitTemplate;
	}

}
