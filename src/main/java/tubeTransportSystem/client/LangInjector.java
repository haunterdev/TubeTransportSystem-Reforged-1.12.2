package tubeTransportSystem.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.Locale;
import net.minecraft.util.text.translation.LanguageMap;

/**
 * Loads assets/tts/lang/en_us.lang from the classpath and injects it into the active
 * client {@link Locale} on every resource reload (plus once at registration).
 *
 * Why this exists: in this packaging Forge's resource pipeline does not surface the
 * mod's lang to the Locale, getAllResources for "tts:lang/en_us.lang" throws even
 * though the file is present in the jar and the "tts" domain registers (textures from
 * the same domain load fine). The lang file itself is byte-correct, so we read it
 * directly and merge it into the Locale's key map. Reload-listener order favours us:
 * vanilla's LanguageManager (registered during Minecraft construction) clears + loads
 * its data first, then this listener (registered at mod init) re-adds the tts keys.
 */
public class LangInjector implements IResourceManagerReloadListener {
    private static final Pattern NUMERIC = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");
    private static final String LANG_PATH = "/assets/tts/lang/en_us.lang";

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        inject();
    }

    /**
     * Merge the tts lang into BOTH translation tables.
     *
     * The client {@link Locale} behind {@link I18n#format} is only half of it. Item and
     * block display names go through Item.getItemStackDisplayName, which calls the
     * deprecated net.minecraft.util.text.translation.I18n and reads a separate
     * LanguageMap. LanguageManager fills that one with
     * LanguageMap.replaceWith(CURRENT_LOCALE.properties) during its own reload, which
     * copies the map before this listener runs, so putting keys into the Locale alone
     * leaves every item name showing its raw key while the creative tab label resolves.
     */
    public static void inject() {
        try {
            Map<String, String> entries = read();
            if (entries.isEmpty()) {
                return;
            }
            Locale locale = currentLocale();
            if (locale != null) {
                Map<String, String> props = localeProps(locale);
                if (props != null) {
                    props.putAll(entries);
                }
            }
            InputStream in = LangInjector.class.getResourceAsStream(LANG_PATH);
            if (in != null) {
                try {
                    LanguageMap.inject(in);
                } finally {
                    in.close();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static Map<String, String> read() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        InputStream in = LangInjector.class.getResourceAsStream(LANG_PATH);
        if (in == null) {
            return map;
        }
        BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        try {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.isEmpty() || line.charAt(0) == '#') {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq < 0) {
                    continue;
                }
                String key = line.substring(0, eq);
                // Match vanilla Locale: %d/%f format specifiers become positional %s-style.
                String val = NUMERIC.matcher(line.substring(eq + 1)).replaceAll("%$1s");
                map.put(key, val);
            }
        } finally {
            r.close();
        }
        return map;
    }

    /** The Locale instance I18n.format reads from is a static field on I18n. */
    private static Locale currentLocale() throws Exception {
        for (Field f : I18n.class.getDeclaredFields()) {
            if (Locale.class.isAssignableFrom(f.getType())) {
                f.setAccessible(true);
                return (Locale) f.get(null);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> localeProps(Locale locale) throws Exception {
        for (Field f : Locale.class.getDeclaredFields()) {
            if (Map.class.isAssignableFrom(f.getType())) {
                f.setAccessible(true);
                return (Map<String, String>) f.get(locale);
            }
        }
        return null;
    }
}
