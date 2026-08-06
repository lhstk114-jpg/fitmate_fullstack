package org.spring.backend.chatbot.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@RequiredArgsConstructor
@Configuration
@EnableRabbit
public class RabbitMqConfig {
    private final ConnectionFactory connectionFactory;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;
    @Value("${rabbitmq.queue.question}")
    private String queueQuestion;
    @Value("${rabbitmq.queue.notification}")
    private String queueNotification;
    @Value("${rabbitmq.routing.key.question}")
    private String keyQuestion;
    @Value("${rabbitmq.routing.key.notification}")
    private String keyNotification;
    @Bean
    public Queue questionQueue(){
        return new Queue(queueQuestion, true);
    }
    @Bean
    public Queue notificationQueue() {
        return new Queue(queueNotification, true);
    }
    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(exchange);
    }
    @Bean
    public Binding bindQuestion(){
        return BindingBuilder.bind(questionQueue()).to(topicExchange()).with(keyQuestion);
    }
    @Bean
    public Binding bindNotification(){
        return BindingBuilder.bind(notificationQueue()).to(topicExchange()).with(keyNotification);
    }
    //RabbitMQ메시지를 수신하기 위한 컨테이너를 생성하기위해 각 리스너의 독립적인 환경제공
    @Bean
    public SimpleRabbitListenerContainerFactory myFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            MessageConverter messageConverter){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        //메시지 감지 시 JSON 데이터 해석을 올바르게 수행하도록 컨버터 지정
        factory.setMessageConverter(messageConverter);
        return factory;
    }
    //챗봇용 수동 리스너 컨테이너
    @Bean
    public SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
                                                    MessageListenerAdapter listenerAdapter){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        //챗봇용 큐 감시
        container.setQueueNames(queueQuestion);
        //도착하면 어댑터 실행
        container.setMessageListener((listenerAdapter));
        //큐가 잠시 없어도 서버가 정지하지 않고 대기할수있게설정
        container.setMissingQueuesFatal(false);
        return container;
    }
    //챗봇용 수동 메시지 어댑터
    @Bean
    public MessageListenerAdapter listenerAdapter(Receiver receiver){
        MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(receiver, "receiveQuestion");
        listenerAdapter.setMessageConverter(messageConverter());
        return listenerAdapter;
    }
    //JSON으로 자동 연결
    @Bean
    public MessageConverter messageConverter() {
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("*"); // 모든 패키지 허용

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setClassMapper(classMapper);

        return converter;
    }

}
