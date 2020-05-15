package wtf.declan.muzzle.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import androidx.preference.Preference;

import java.util.List;

import wtf.declan.muzzle.R;
import wtf.declan.muzzle.data.db.entities.ConversationEntity;
import wtf.declan.muzzle.data.db.entities.MessageEntity;
import wtf.declan.muzzle.data.db.repositories.ConversationRepository;
import wtf.declan.muzzle.data.db.repositories.MessageRepository;
import wtf.declan.muzzle.util.Preferences;
import wtf.declan.muzzle.recipient.Recipient;
import wtf.declan.muzzle.recipient.RecipientFactory;
import wtf.declan.muzzle.view.ui.ConversationActivity;

import static wtf.declan.muzzle.view.ui.ConversationActivity.RECIPIENT_FROM_NOTIFICATION_EXTRA;
import static wtf.declan.muzzle.view.ui.ConversationActivity.RECIPIENT_NUMBER_EXTRA;

public class NotificationFactory {

    private static final String     TAG                     = NotificationFactory.class.getSimpleName();

    private static final String     DEFAULT_CHANNEL_ID      = "DEFAULT_NOTIFICATIONS";
    private static final String     CHANNEL_PREFIX          = "NOTIFICATIONS_";

    private static final long[]     VIBRATE_PATTERN         = new long[] { 0, 250, 0, 250 };

    private final Context context;

    private final NotificationManager       notificationManager;

    private final ConversationRepository    conversationRepository;
    private final MessageRepository         messageRepository;

    public NotificationFactory(Context context) {
        this.context                = context;
        this.notificationManager    = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.conversationRepository = new ConversationRepository(context);
        this.messageRepository      = new MessageRepository(context);

        createDefaultChannel();
    }

    public void update(long conversationId) {
        if(!Preferences.useNotifications(context)) {
            return;
        }

        Log.d(TAG, "update: " + conversationId);

        if(getNotificationChannel(conversationId) == null) { // if notification already exists
            createConversationChannel(conversationId);
        }

        // Get messages from the database
        List<MessageEntity> messages        = messageRepository.getUnreadUnNotifiedMessages(conversationId);

        if(messages.isEmpty()) {
            notificationManager.cancel((int) conversationId);            // cancel conversation
            notificationManager.cancel((int) conversationId + 5000); // cancel failed
            return;
        }

        // Get conversation from the database
        ConversationEntity  conversation    =   conversationRepository.getConversation(conversationId);

        // Get the recipient
        Recipient           recipient       =   RecipientFactory.getRecipientFromNumber(context, conversation.getNumber());

        // Create notification builder
        ConversationNotificationBuilder notification = new ConversationNotificationBuilder(
                context,
                getChannelIdForNotification(conversationId)
        );

        // Set Conversation
        notification.setConversation(conversation);

        // Set People
        Person self = new Person.Builder()
                .setName("Me")
                .build(); // mark a person for messages sent by the client

        notification.setPeople(self, recipient);

        // Add Messages
        messages.forEach(notification::addMessage);

        notificationManager.notify((int) conversationId, notification.build());
    }


    public void notifyFailed(ConversationEntity conversation) {
        if(!Preferences.useNotifications(context)) {
            return;
        }

        Recipient           recipient       = RecipientFactory.getRecipientFromNumber(context, conversation.getNumber());

        PendingIntent pi = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(
                        new Intent(context, ConversationActivity.class)
                                .putExtra(RECIPIENT_FROM_NOTIFICATION_EXTRA, true)
                                .putExtra(RECIPIENT_NUMBER_EXTRA, conversation.getNumber())
                ).getPendingIntent(
                        ((int) conversation.getId()) + 10000,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // TODO: Create a separate class for building failed notification's like ConversationNotificationBuilder

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, getChannelIdForNotification(conversation.getId()))
                .setContentTitle(context.getString(R.string.notification_failed_content_title))
                .setContentText(context.getString(R.string.notification_failed_content_text, recipient.getDisplayName()))
                .setColor(recipient.getColor())
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSmallIcon(R.drawable.ic_sms_failed_white_24dp)
                .setAutoCancel(true)
                .setContentIntent(pi);

        // Enable Lights / Vibration based on preferences in Settings
        if(Preferences.useVibration(context)) {
            notification.setVibrate(VIBRATE_PATTERN);
        }

        if(Preferences.useLights(context)) {
            notification.setLights(Color.RED, 750, 2000);
        }

        notificationManager.notify((int) conversation.getId() + 5000, notification.build());
    }

    private void createDefaultChannel() {
        NotificationChannel channel = new NotificationChannel(
                DEFAULT_CHANNEL_ID, "Default", NotificationManager.IMPORTANCE_HIGH
        );

        channel.enableVibration(true);
        channel.setVibrationPattern(VIBRATE_PATTERN);
        channel.enableLights(true);
        channel.setLightColor(Color.GREEN);

        notificationManager.createNotificationChannel(channel);
    }

    private void createConversationChannel(long conversationId) {
        if(getNotificationChannel(conversationId) != null) { // if notification already exists
            Log.d(TAG, "createNotificationChannel: channel already exists");
            return;
        }

        ConversationEntity  conversation    = conversationRepository.getConversation(conversationId);
        Recipient           recipient       = RecipientFactory.getRecipientFromNumber(context, conversation.getNumber());

        String              channelId       = buildNotificationChannelId(conversation.getId());
        String              title           = recipient.getDisplayName();

        NotificationChannel channel         = new NotificationChannel(
                channelId, title, NotificationManager.IMPORTANCE_HIGH
        );

        if(Preferences.useVibration(context)) {
            channel.setVibrationPattern(VIBRATE_PATTERN);
            channel.enableVibration(true);
        }

        if(Preferences.useLights(context)) {
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);
        }

        Uri notificationSound = RingtoneManager.getDefaultUri (RingtoneManager.TYPE_NOTIFICATION);
        channel.setSound(notificationSound, new AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_NOTIFICATION)
              .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build());

        Log.d(TAG, "createNotificationChannel: Created channel for conversation id:" + conversationId);
        notificationManager.createNotificationChannel(channel);
    }

    private String buildNotificationChannelId(long conversationId) {
        return CHANNEL_PREFIX + conversationId;
    }

    private NotificationChannel getNotificationChannel(long conversationId) {
        return notificationManager.getNotificationChannel(
                buildNotificationChannelId(conversationId)
        );
    }

    private String getChannelIdForNotification(long conversationId) {
        NotificationChannel channel = getNotificationChannel(conversationId);
        return channel != null ? channel.getId() : DEFAULT_CHANNEL_ID;
    }

}
