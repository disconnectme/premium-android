package pro.disconnect.me.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pro.disconnect.me.R;

public class SettingsContent {
    public static final String MAIN_MENU = "main_menu";
    public static final String INFO_MENU = "info_menu";
    public static final String PRIVACY_MENU = "privacy_menu";

    /**
     * An map of settings items.
     */
    public static final Map<String, List<SettingItem>> SETTINGS_ITEMS = new HashMap<>();

    static {
        List<SettingItem> mainMenuItems = new ArrayList<SettingItem>();
        mainMenuItems.add(new SettingItem(R.drawable.ic_info_green, R.string.setting_info, true, null));
        mainMenuItems.add(new SettingItem(R.drawable.ic_poll_green, R.string.settings_reporting, true, null));
        // Notifications to be implemented mainMenuItems.add(new SettingItem(R.drawable.feature_icon_alerts, R.string.settings_notification, false, null));
        mainMenuItems.add(new SettingItem(R.drawable.ic_account_circle_green, R.string.settings_account, false, null));
        mainMenuItems.add(new SettingItem(R.drawable.ic_restore_green, R.string.settings_restore, false, null));
        SETTINGS_ITEMS.put(MAIN_MENU, mainMenuItems);

        // Info menu
        List<SettingItem> infoMenuItems = new ArrayList<SettingItem>();
        infoMenuItems.add(new SettingItem(0, R.string.settings_privacy_policy, false, "https://disconnect.me/privacy"));
        infoMenuItems.add(new SettingItem(0, R.string.settings_terms, false,  "https://disconnect.me/terms"));
        infoMenuItems.add(new SettingItem(0, R.string.settings_support, false, "https://disconnect.me/help"));
        SETTINGS_ITEMS.put(INFO_MENU, infoMenuItems);

        // Privacy menu
        List<SettingItem> privacyMenuItems = new ArrayList<SettingItem>();
        SettingItem privacyMenu = new SettingItem(0, R.string.setting_purge_database, false, null);
        privacyMenuItems.add(privacyMenu);
        SETTINGS_ITEMS.put(PRIVACY_MENU, privacyMenuItems);
    }


    /**
     * A setting item representing a setting menu item.
     */
    public static class SettingItem {
        public final int mIconResId;
        public final int mContentResId;
        public final boolean mHasMore;
        public final String mUrl;

        public SettingItem(int aIconResId, int aContentResId, boolean aHasMore, String aUrl) {
            mIconResId = aIconResId;
            mHasMore = aHasMore;
            mContentResId = aContentResId;
            mUrl = aUrl;
        }

        @Override
        public String toString() {
            return String.format("%d", mContentResId);
        }
    }
}
