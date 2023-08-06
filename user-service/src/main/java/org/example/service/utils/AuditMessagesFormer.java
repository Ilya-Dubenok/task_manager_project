package org.example.service.utils;

import org.apache.commons.lang3.tuple.Pair;
import org.example.dao.entities.user.User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuditMessagesFormer {

    private ChangedFieldsOfEntitySearcher<User> userChangedFieldsOfEntitySearcher;

    private final String UPDATE_MESSAGE_FORM = "Запись была обновлена. Следующие изменения:%s";

    private final String UPDATE_MESSAGE_NOT_TO_DISPLAY_FORM = " %s (не отображается);";

    private final String UPDATE_MESSAGE_OLD_NEW_VALUES_FORM = " %s, старое значение->%s, новое значение->%s;";

    private final String USER_CREATED_MESSAGE = "Пользователь был создан";

    private final String USER_REGISTERED_MESSAGE = "Пользователь был зарегистрирован";


    public AuditMessagesFormer(ChangedFieldsOfEntitySearcher<User> userChangedFieldsOfEntitySearcher) {
        this.userChangedFieldsOfEntitySearcher = userChangedFieldsOfEntitySearcher;
    }

    public  String formUpdateAuditMessage(User user1, User user2) {
        Map<String, Pair<String, String>> changes = userChangedFieldsOfEntitySearcher.getChanges(
                user1, user2
        );
        return parseUpdatesToAuditMessage(changes);
    }


    private String parseUpdatesToAuditMessage(Map<String, Pair<String, String>> updates) {
        StringBuilder stringBuilder = new StringBuilder();

        updates.forEach((key, pair) -> {
            if (pair == null) {
                stringBuilder.append(String.format(UPDATE_MESSAGE_NOT_TO_DISPLAY_FORM, key));
                return;
            }
            stringBuilder.append(
                    String.format(
                            UPDATE_MESSAGE_OLD_NEW_VALUES_FORM, key, pair.getKey(), pair.getValue()
                    )
            );


        });

        return String.format(UPDATE_MESSAGE_FORM, stringBuilder);
    }

    public String getUserCreatedAuditMessage() {
        return USER_CREATED_MESSAGE;
    }

    public String getAuditRegisteredMessage() {
        return USER_REGISTERED_MESSAGE;
    }
}
