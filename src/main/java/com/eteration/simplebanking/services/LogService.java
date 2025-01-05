package com.eteration.simplebanking.services;

import com.eteration.simplebanking.config.RabbitMQConfig;
import com.eteration.simplebanking.dto.LogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;

@Service
public class LogService {
    private static final Logger logger = LoggerFactory.getLogger(LogService.class);

    @Value("${app.rabbitmq.logging.enabled:false}")
    private boolean rabbitmqEnabled;

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    /**
     * Hata loglarını kaydeder ve RabbitMQ'ya gönderir
     * @param message Log mesajı
     * @param className Sınıf adı
     * @param methodName Metod adı
     * @param exception Oluşan hata
     */
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
                LogMessage mqMessage = new LogMessage();
                mqMessage.setLevel("ERROR");
                mqMessage.setMessage(message);
                mqMessage.setClassName(className);
                mqMessage.setMethodName(methodName);

                rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    "error." + className,
                    mqMessage
                );
            } catch (Exception e) {
                logger.warn("Failed to send log to RabbitMQ: {}", e.getMessage());
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
                LogMessage mqMessage = new LogMessage();
                mqMessage.setLevel("INFO");
                mqMessage.setMessage(message);
                mqMessage.setClassName(className);
                mqMessage.setMethodName(methodName);

                rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    "info." + className,
                    mqMessage
                );
            } catch (Exception e) {
                logger.warn("Failed to send log to RabbitMQ: {}", e.getMessage());
            }
        }
    }
} 
