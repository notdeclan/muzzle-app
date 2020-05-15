package wtf.declan.muzzle.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

/**
 * Util class is used to assist and reduce boilerplate code when checking values from the apps
 * shared preferences
 */
public class Preferences {

    /** Session Preference Keys **/
    private static final String PREF_KEY_SESSION_AUTO_ACCEPT_STATE = "session_auto_accept_state";

    /** Notification Preference Keys **/
    private static final String PREF_KEY_NOTIFICATION_STATE = "notification_state";
    private static final String PREF_KEY_NOTIFICATION_LIGHTS_STATE = "notification_lights_state";
    private static final String PREF_KEY_NOTIFICATION_VIBRATION_STATE = "notification_vibration_state";

    /**
     * Return whether the user has allowed notifications
     */
    public static boolean useNotifications(@NonNull Context context) {
        return getPreferenceBoolean(context, PREF_KEY_NOTIFICATION_STATE);
    }

    /**
     * Return whether the user has allowed the use of lights with notifications
     */
    public static boolean useLights(@NonNull Context context) {
        return getPreferenceBoolean(context, PREF_KEY_NOTIFICATION_LIGHTS_STATE);
    }

    /**
     * Return whether the user has allowed the use of vibrations with notifications
     */
    public static boolean useVibration(@NonNull Context context) {
        return getPreferenceBoolean(context, PREF_KEY_NOTIFICATION_VIBRATION_STATE);
    }

    /**
     * Return whether the user has allowed sessions to auto accept when received
     */
    public static boolean sessionAutoAccept(@NonNull Context context) {
        return getPreferenceBoolean(context, PREF_KEY_SESSION_AUTO_ACCEPT_STATE);
    }

    private static boolean getPreferenceBoolean(@NonNull Context context, String preferenceKey) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(preferenceKey, false);
    }

}
