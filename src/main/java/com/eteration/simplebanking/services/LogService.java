package com.eteration.simplebanking.services;

import com.eteration.simplebanking.config.RabbitMQConfig;
import com.eteration.simplebanking.dto.LogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.PrintWriter;
import java.io.StringWriter;

@Service
public class LogService {
    private static final Logger logger = LoggerFactory.getLogger(LogService.class);

    @Value("${app.rabbitmq.logging.enabled:false}")
    private boolean rabbitmqEnabled;

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        if (rabbitmqEnabled && rabbitTemplate != null) {
            try {
                // Test connection at startup
                rabbitTemplate.execute(channel -> {
                    channel.queueDeclarePassive(RabbitMQConfig.QUEUE_NAME);
                    return null;
                });
                logger.info("RabbitMQ connection established successfully");
                
                // Send a test message
                LogMessage testMessage = new LogMessage("INFO", "Application started", this.getClass().getSimpleName(), "init", null);
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, testMessage);
                logger.info("Test message sent to RabbitMQ");
            } catch (Exception e) {
                logger.error("Failed to establish RabbitMQ connection: {}", e.getMessage());
            }
        }
    }

    public void logError(String message, String className, String methodName, Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        
        String logMessage = String.format("[%s] [%s] Error in %s.%s: %s. Exception: %s", 
            Thread.currentThread().getName(), 
            className,
            methodName,
            message,
            exception.getMessage(),
            sw.toString());
        
        // Always log to console/file
        logger.error(logMessage);

        // Only send to RabbitMQ if enabled
        if (rabbitmqEnabled && rabbitTemplate != null) {
            try {
                LogMessage mqMessage = new LogMessage("ERROR", message, className, methodName, sw.toString());
                rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY,
                    mqMessage
                );
                logger.info("Error message sent to RabbitMQ");
            } catch (Exception e) {
                logger.error("Failed to send log to RabbitMQ: {}", e.getMessage());
            }
        }
    }

    /**
     * Bilgi loglarını kaydeder ve RabbitMQ'ya gönderir
     * @param message Log mesajı
     * @param className Sınıf adı
     * @param methodName Metod adı
     */
    public void logInfo(String message, String className, String methodName) {
        String logMessage = String.format("[%s] [%s] Info in %s.%s: %s", 
            Thread.currentThread().getName(), 
            className,
            className,
            methodName,
            message);

        // Always log to console/file
        logger.info(logMessage);

        // Only send to RabbitMQ if enabled
        if (rabbitmqEnabled && rabbitTemplate != null) {
            try {
                LogMessage mqMessage = new LogMessage("INFO", message, className, methodName, null);
                rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY,
                    mqMessage
                );
                logger.info("Info message sent to RabbitMQ");
            } catch (Exception e) {
                logger.error("Failed to send log to RabbitMQ: {}", e.getMessage());
            }
        }
    }
} 
