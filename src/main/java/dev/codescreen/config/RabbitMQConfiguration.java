package dev.codescreen.config;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfiguration {
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange("transaction-exchange");
    }

    @Bean
    public Queue authQueue() {
        return new Queue("authQueue", true);
    }

    @Bean
    public Queue loadQueue() {
        return new Queue("loadQueue", true);
    }

    @Bean
    public Binding bindingAuth(DirectExchange exchange, @Qualifier("authQueue") Queue authQueue) {
        return BindingBuilder.bind(authQueue).to(exchange).with("authQueue");
    }

    @Bean
    public Binding bindingLoad(DirectExchange exchange,@Qualifier("loadQueue") Queue loadQueue) {
        return BindingBuilder.bind(loadQueue).to(exchange).with("loadQueue");
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
