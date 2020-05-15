package wtf.declan.muzzle.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;

import wtf.declan.muzzle.R;
import wtf.declan.muzzle.data.db.entities.ConversationEntity;
import wtf.declan.muzzle.data.db.entities.MessageEntity;
import wtf.declan.muzzle.util.Preferences;
import wtf.declan.muzzle.data.receivers.DeleteNotificationReceiver;
import wtf.declan.muzzle.data.receivers.MarkReadReceiver;
import wtf.declan.muzzle.recipient.Recipient;
import wtf.declan.muzzle.view.ui.ConversationActivity;
import wtf.declan.muzzle.view.ui.ConversationPopupActivity;

import static wtf.declan.muzzle.view.ui.ConversationActivity.RECIPIENT_FROM_NOTIFICATION_EXTRA;
import static wtf.declan.muzzle.view.ui.ConversationActivity.RECIPIENT_NUMBER_EXTRA;

/**
 * Class is used to build conversation notifications
 *
 * Works by wrapping around the standard NotificationCompat.Builder to easily create
 * standardized conversation notifications in code.
 *
 * Essentially just makes code in NotificationFactory cleaner and makes it easier to customize
 * and change the notification specifically
 *
 * Future work:
 * technically both setPeople, and setConversation functions could be moved to the initializer
 * as well as the addMessage function but yolo and this works and help's split stuff up a bit.
 *
 * Also, technically the conversation entity doesn't need to be added to the object because
 * only the getId() and getNumber() function are used, which could be passed directly to save a
 * tiny bit of memory. But doesn't really matter because of GC, also yolo.
 *
 */
public class ConversationNotificationBuilder extends NotificationCompat.Builder {

    private static final String TAG = ConversationNotificationBuilder.class.getSimpleName();

    private static final long[]     VIBRATE_PATTERN         = new long[] { 0, 400, 0, 400 };

    private NotificationCompat.MessagingStyle messagingStyle;
    private ConversationEntity  conversation;

    private Recipient recipient;
    private Person self;

    private final Context context;

    ConversationNotificationBuilder(@NonNull Context context, @NonNull String channelId) {
        super(context, channelId);
        this.context = context;
    }

    /**
     * Set the people for the conversation notification
     *
     * @param sender: Person to display messages sent by the client
     * @param recipient: Recipient of the received messages
     */
    void setPeople(@NonNull Person sender, @NonNull Recipient recipient) {
        this.recipient = recipient;
        this.self = sender;
        messagingStyle = new NotificationCompat.MessagingStyle(recipient.getPerson());
        messagingStyle.setConversationTitle(recipient.getDisplayName());
    }

    /**
     * Set the conversation entity from the database
     *
     * Used to get the ID and number across the notification to build intents, and actions as
     * well as set
     *
     * @param conversationEntity: Conversation Entity from the database
     */
    void setConversation(@NonNull ConversationEntity conversationEntity) {
        this.conversation = conversationEntity;
    }

    /**
     * Used to add messages into the notification from the database
     *
     * @param message: MessageEntity from database
     */
    void addMessage(@NonNull MessageEntity message) {
        if(messagingStyle != null) {
            // should be handled by dao but buggy for some reason so just double check
            if(!message.isNotified() && !message.isRead()) {
                messagingStyle.addMessage( // add message to style
                        new NotificationCompat.MessagingStyle.Message(
                                message.getBody(),
                                message.getDate().getTime(),
                                message.isInbox() ? recipient.getPerson() : self // get right person
                        )
                );
            }
        } else {
            Log.e(TAG, "addMessage: setPeople needs to be called before adding messages");
        }
    }

    @Override
    public Notification build() {
        if(conversation == null || recipient == null || messagingStyle.getMessages().isEmpty()) {
            Log.e(TAG, "Cannot build conversation notification, missing required variables");
            return null;
        }

        // Notification Characteristics
        setCategory(NotificationCompat.CATEGORY_MESSAGE);
        setPriority(NotificationCompat.PRIORITY_MAX);
        setSmallIcon(R.drawable.ic_message_white_24dp);
        setLargeIcon(recipient.getIconBitmap());
        setNumber(messagingStyle.getMessages().size());
        setAutoCancel(true);
        setWhen(conversation.getUpdated().getTime());


        // Enable Lights / Vibration based on preferences in Settings
        if(!Preferences.useLights(context)) {
            setLights(recipient.getColor(), 1250, 1000);
        }

        if(Preferences.useVibration(context)) {
            setVibrate(VIBRATE_PATTERN);
        }


        // Add Intents
        setContentIntent(getContentIntent());
        setDeleteIntent(getDeleteIntent());

        // Add Actions
        addAction(getReadAction());
        addAction(getQuickReplyAction());

        // Add Persons
        addPerson(self.getUri());
        addPerson(recipient.getPerson().getUri());

        // Set Style
        setStyle(messagingStyle);

        return super.build();
    }


    private PendingIntent getContentIntent() {
        return TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(
                        new Intent(context, ConversationActivity.class)
                                .putExtra(RECIPIENT_FROM_NOTIFICATION_EXTRA, true)
                                .putExtra(RECIPIENT_NUMBER_EXTRA, recipient.getNumber())
                ).getPendingIntent(
                        ((int) conversation.getId()) + 10000,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
    }


    private PendingIntent getDeleteIntent() {
        Intent intent = DeleteNotificationReceiver.buildIntent(context, conversation.getId());

        return PendingIntent.getBroadcast(
                context,
                (int) (conversation.getId() + 20000),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }


    private NotificationCompat.Action getReadAction() {
        Intent intent = MarkReadReceiver.buildIntent(context, conversation.getId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) (conversation.getId() + 30000),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_check_white_24dp,
                context.getString(R.string.notification_mark_as_read),
                pendingIntent

        ).build();
    }


    private NotificationCompat.Action getQuickReplyAction() {
        Intent intent = new Intent(context, ConversationPopupActivity.class);
        intent.putExtra(ConversationActivity.RECIPIENT_NUMBER_EXTRA, conversation.getNumber());

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) (conversation.getId() + 40000),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_reply_white_24dp,
                context.getString(R.string.notification_quick_reply),
                pendingIntent
        ).build();
    }

}
