package com.killiancorbel.realtimeapi.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import com.killiancorbel.realtimeapi.controllers.YukiController;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;

@Service
public class NotificationService {
    @Value("${app.firebase-configuration-file}")
    private String firebaseConfigPath;
    private final Logger logger = LoggerFactory.getLogger(YukiController.class);

    @PostConstruct
    public void initFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FileInputStream serviceAccount =
                        new FileInputStream(firebaseConfigPath);

                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                logger.info("Firebase application initialized");
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public void sendDailyNotification(String pushId, String language) {
        Notification notification = Notification.builder()
                .setTitle(getTitleByLanguage(language))
                .setBody(getBodyByLanguage(language))
                .build();
        Message message = Message.builder()
                .setApnsConfig(getApnsConfig("daily"))
                .setAndroidConfig(getAndroidConfig("daily"))
                .setNotification(notification)
                .setToken(pushId)
                .build();
        FirebaseMessaging.getInstance().sendAsync(message);
    }

    private String getTitleByLanguage(String language) {
        if (language.equals("French")) {
            return "Votre leçon du jour est disponible !";
        } else if (language.equals("Spanish")) {
            return "¡Tu lección del día está disponible!";
        } else if (language.equals("Russian")) {
            return "Ваш урок на сегодня доступен!";
        } else if (language.equals("Italian")) {
            return "La tua lezione del giorno è disponibile!";
        } else if (language.equals("German")) {
            return "Deine Tageslektion ist verfügbar!";
        } else if (language.equals("Portuguese")) {
            return "Sua lição do dia está disponível!";
        } else if (language.equals("Japanese")) {
            return "本日のレッスンをご利用いただけます！";
        } else if (language.equals("Korean")) {
            return "오늘의 수업이 준비되었습니다!";
        } else if (language.equals("Chinese")) {
            return "您今天的课程已可用！";
        } else {
            return "Your daily lesson is available!";
        }
    }

    private String getBodyByLanguage(String language) {
        if (language.equals("French")) {
            return "Venez pratiquer !";
        } else if (language.equals("Spanish")) {
            return "¡Ven a practicar!";
        } else if (language.equals("Russian")) {
            return "Приходите практиковаться!";
        } else if (language.equals("Italian")) {
            return "Vieni a praticare!";
        } else if (language.equals("German")) {
            return "Komm zum Üben!";
        } else if (language.equals("Portuguese")) {
            return "Venha praticar!";
        } else if (language.equals("Japanese")) {
            return "練習しに来てください！";
        } else if (language.equals("Korean")) {
            return "연습하러 오세요!";
        } else if (language.equals("Chinese")) {
            return "快来练习吧！";
        } else {
            return "Come and practice!";
        }
    }

    private AndroidConfig getAndroidConfig(String topic) {
        return AndroidConfig.builder()
                .setCollapseKey(topic)
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                        .setTag(topic).build()).build();
    }

    private ApnsConfig getApnsConfig(String topic) {
        return ApnsConfig.builder()
                .setAps(Aps.builder().setCategory(topic).setThreadId(topic).build()).build();
    }
}
