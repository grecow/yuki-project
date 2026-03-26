package com.killiancorbel.realtimeapi.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

@Service
public class NotificationService {
    @Value("${app.firebase-configuration-file}")
    private String firebaseConfigPath;
    private final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private static final Map<String, String[]> DAILY_MESSAGES = Map.ofEntries(
            Map.entry("French", new String[]{"Votre leçon du jour est disponible !", "Venez pratiquer !"}),
            Map.entry("Spanish", new String[]{"¡Tu lección del día está disponible!", "¡Ven a practicar!"}),
            Map.entry("Russian", new String[]{"Ваш урок на сегодня доступен!", "Приходите практиковаться!"}),
            Map.entry("Italian", new String[]{"La tua lezione del giorno è disponibile!", "Vieni a praticare!"}),
            Map.entry("German", new String[]{"Deine Tageslektion ist verfügbar!", "Komm zum Üben!"}),
            Map.entry("Portuguese", new String[]{"Sua lição do dia está disponível!", "Venha praticar!"}),
            Map.entry("Japanese", new String[]{"本日のレッスンをご利用いただけます！", "練習しに来てください！"}),
            Map.entry("Korean", new String[]{"오늘의 수업이 준비되었습니다!", "연습하러 오세요!"}),
            Map.entry("Chinese", new String[]{"您今天的课程已可用！", "快来练习吧！"}),
            Map.entry("English", new String[]{"Your daily lesson is available!", "Come and practice!"})
    );

    private static final Map<String, String> STREAK_RISK_MESSAGES = Map.ofEntries(
            Map.entry("French", "Ne perdez pas votre série de %d jours !"),
            Map.entry("Spanish", "¡No pierdas tu racha de %d días!"),
            Map.entry("Russian", "Не потеряйте серию из %d дней!"),
            Map.entry("Italian", "Non perdere la tua serie di %d giorni!"),
            Map.entry("German", "Verliere nicht deine %d-Tage-Serie!"),
            Map.entry("Portuguese", "Não perca sua sequência de %d dias!"),
            Map.entry("Japanese", "%d日間の連続記録を失わないで！"),
            Map.entry("Korean", "%d일 연속 기록을 잃지 마세요!"),
            Map.entry("Chinese", "不要失去你的%d天连续记录！"),
            Map.entry("English", "Don't lose your %d-day streak!")
    );

    @PostConstruct
    public void initFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FileInputStream serviceAccount = new FileInputStream(firebaseConfigPath);
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
                logger.info("Firebase application initialized");
            }
        } catch (IOException e) {
            logger.error("Failed to init Firebase: {}", e.getMessage());
        }
    }

    public void sendDailyNotification(String pushId, String language) {
        String[] messages = DAILY_MESSAGES.getOrDefault(language, DAILY_MESSAGES.get("English"));
        sendNotification(pushId, messages[0], messages[1], "daily");
    }

    public void sendStreakRiskNotification(String pushId, String language, int streak) {
        String template = STREAK_RISK_MESSAGES.getOrDefault(language, STREAK_RISK_MESSAGES.get("English"));
        String title = "🔥 " + String.format(template, streak);
        String[] daily = DAILY_MESSAGES.getOrDefault(language, DAILY_MESSAGES.get("English"));
        sendNotification(pushId, title, daily[1], "streak_risk");
    }

    private void sendNotification(String pushId, String title, String body, String topic) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();
            Message message = Message.builder()
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder().setCategory(topic).setThreadId(topic).build()).build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setCollapseKey(topic)
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder().setTag(topic).build()).build())
                    .setNotification(notification)
                    .setToken(pushId)
                    .build();
            FirebaseMessaging.getInstance().sendAsync(message);
        } catch (Exception e) {
            logger.error("Failed to send notification to {}: {}", pushId, e.getMessage());
        }
    }
}
