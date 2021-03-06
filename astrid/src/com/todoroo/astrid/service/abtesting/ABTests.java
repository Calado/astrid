/**
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.astrid.service.abtesting;

import java.util.HashMap;
import java.util.Set;

import android.accounts.Account;
import android.content.Context;

import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;

/**
 * Helper class to define options with their probabilities and descriptions
 * @author Sam Bosley <sam@astrid.com>
 *
 */
public class ABTests {

    public ABTests() {
        bundles = new HashMap<String, ABTestBundle>();
        initialize();
    }

    /**
     * Initialization for any tests that require a context or other logic
     * to be initialized should go here. This method is called from the startup
     * service before any test choices are made, so it is safe to add
     * tests here. It's also ok if this method is a no-op sometimes.
     * @param context
     */
    public void externalInit(Context context) {
        // The outer 'if' statement is to prevent one test from being added one time
        // and the other from being added later if the accounts changed
        if (ABChooser.readChoiceForTest(AB_NEW_LOGIN_NO_GOOGLE) == ABChooser.NO_OPTION
                && ABChooser.readChoiceForTest(AB_NEW_LOGIN_YES_GOOGLE) == ABChooser.NO_OPTION) {
            GoogleAccountManager am = new GoogleAccountManager(context);
            Account[] accounts = am.getAccounts();
            if (accounts == null || accounts.length == 0) {
                addTest(AB_NEW_LOGIN_NO_GOOGLE, new int[] { 1, 1 },
                        new int[] { 1, 0 }, new String[] { "old-welcome", "new-welcome" });  //$NON-NLS-1$//$NON-NLS-2$
            } else {
                addTest(AB_NEW_LOGIN_YES_GOOGLE, new int[] { 1, 1, 1 },
                        new int[] { 1, 0, 0 }, new String[] { "old-welcome", "new-welcome", "new-quick-welcome" });  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
            }
        }
    }

    /**
     * Gets the integer array of weighted probabilities for an option key
     * @param key
     * @return
     */
    public synchronized int[] getProbsForTestKey(String key, boolean newUser) {
        if (bundles.containsKey(key)) {
            ABTestBundle bundle = bundles.get(key);
            if (newUser)
                return bundle.newUserProbs;
            else
                return bundle.existingUserProbs;
        } else {
            return null;
        }
    }

    /**
     * Gets the string array of option descriptions for an option key
     * @param key
     * @return
     */
    public String[] getDescriptionsForTestKey(String key) {
        if (bundles.containsKey(key)) {
            ABTestBundle bundle = bundles.get(key);
            return bundle.descriptions;
        } else {
            return null;
        }
    }

    /**
     * Returns the description for a particular choice of the given option
     * @param testKey
     * @param optionIndex
     * @return
     */
    public String getDescriptionForTestOption(String testKey, int optionIndex) {
        if (bundles.containsKey(testKey)) {
            ABTestBundle bundle = bundles.get(testKey);
            if (bundle.descriptions != null && optionIndex < bundle.descriptions.length) {
                return bundle.descriptions[optionIndex];
            }
        }
        return null;
    }

    public Set<String> getAllTestKeys() {
        return bundles.keySet();
    }

    /**
     * Maps keys (i.e. preference key identifiers) to feature weights and descriptions
     */
    private final HashMap<String, ABTestBundle> bundles;

    private static class ABTestBundle {
        protected final int[] newUserProbs;
        protected final int[] existingUserProbs;
        protected final String[] descriptions;

        protected ABTestBundle(int[] newUserProbs, int[] existingUserProbs, String[] descriptions) {
            this.newUserProbs = newUserProbs;
            this.existingUserProbs = existingUserProbs;
            this.descriptions = descriptions;
        }
    }

    public boolean isValidTestKey(String key) {
        return bundles.containsKey(key);
    }

    /**
     * A/B testing options are defined below according to the following spec:
     *
     * @param testKey = "<key>"
     * --This key is used to identify the option in the application and in the preferences
     *
     * @param newUserProbs = { int, int, ... }
     * @param existingUserProbs = { int, int, ... }
     * --The different choices in an option correspond to an index in the probability array.
     * Probabilities are expressed as integers to easily define relative weights. For example,
     * the array { 1, 2 } would mean option 0 would happen one time for every two occurrences of option 1
     *
     * The first array is used for new users and the second is used for existing/upgrading users,
     * allowing us to specify different distributions for each group.
     *
     * (optional)
     * @param descriptions = { "...", "...", ... }
     * --A string description of each option. Useful for tagging events. The index of
     * each description should correspond to the events location in the probability array
     * (i.e. the arrays should be the same length if this one exists)
     *
     */
    public void addTest(String testKey, int[] newUserProbs, int[] existingUserProbs, String[] descriptions) {
        ABTestBundle bundle = new ABTestBundle(newUserProbs, existingUserProbs, descriptions);
        bundles.put(testKey, bundle);
    }

    public static final String AB_FEATURED_LISTS = "android_featured_lists"; //$NON-NLS-1$

    public static final String AB_SOCIAL_REMINDERS = "android_social_reminders";  //$NON-NLS-1$

    public static final String AB_DRAG_DROP = "android_drag_drop"; //$NON-NLS-1$

    public static final String AB_DEFAULT_EDIT_TAB = "android_default_edit_tab"; //$NON-NLS-1$

    public static final String AB_NEW_LOGIN_NO_GOOGLE = "android_new_login_n_google"; //$NON-NLS-1$

    public static final String AB_NEW_LOGIN_YES_GOOGLE = "android_new_login_y_google"; //$NON-NLS-1$

    private void initialize() {

        addTest(AB_FEATURED_LISTS, new int[] { 1, 1 },
                new int[] { 1, 1 }, new String[] { "featured-lists-disabled", "featured-lists-enabled" }); //$NON-NLS-1$ //$NON-NLS-2$

        addTest(AB_SOCIAL_REMINDERS, new int[] { 1, 1 },
                new int[] { 1, 1 }, new String[] { "no-faces", "show-faces" }); //$NON-NLS-1$ //$NON-NLS-2$

        addTest(AB_DRAG_DROP, new int[] { 3, 1 },
                new int[] { 1, 0 }, new String[] { "off-by-default", "on-by-default" }); //$NON-NLS-1$ //$NON-NLS-2$

        addTest(AB_DEFAULT_EDIT_TAB, new int[] { 1, 1 },
                new int[] { 1, 1 }, new String[] { "activity-tab", "details-tab" }); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
